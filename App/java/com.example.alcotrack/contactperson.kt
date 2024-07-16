package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class contactperson : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var ContactFNameTxt: EditText
    private lateinit var ContactLNameTxt: EditText
    private lateinit var ContactPhoneTxt: EditText
    private lateinit var ContactEmailTxt: EditText
    private lateinit var ContactEditBtn: Button

    private var isEditMode = false // Flag to track if in edit mode
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactperson)

        // Initialize the EditTexts and Button
        ContactFNameTxt = findViewById(R.id.ContactFNameTxt)
        ContactLNameTxt = findViewById(R.id.ContactLNameTxt)
        ContactPhoneTxt = findViewById(R.id.ContactPhoneTxt)
        ContactEmailTxt = findViewById(R.id.editTextTextEmailAddress)
        ContactEditBtn = findViewById(R.id.ContactEditBtn)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener(this)

        // Setup ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Enable the Up button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve email from intent extras

        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")
        email = intent.getStringExtra("Email") ?: ""

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        headerSubtitle.text = "$FName $LName"

        // Fetch and populate contact details
        fetchContactDetails(email)

        // Set click listener for Edit button
        ContactEditBtn.setOnClickListener {
            if (!isEditMode) {
                enableEditMode()
                isEditMode = true
                ContactEditBtn.text = "Save"
                Toast.makeText(this, "Edit mode", Toast.LENGTH_SHORT).show()
            } else {
                saveChangesToDatabase()
                disableEditMode()
                isEditMode = false
                ContactEditBtn.text = "Edit"
                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchContactDetails(email: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = getContactDetailsFromDatabase(email)
            runOnUiThread {
                updateUI(result)
            }
        }
    }

    private fun getContactDetailsFromDatabase(email: String): JSONObject {
        val responseJson = JSONObject()

        try {
            val url = URL("http://192.168.158.182/alcotrack/get_contact_details.php?email=$email")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                responseJson.put("data", JSONObject(response))
                Log.d("getContactDetails", "JSON Response: $response")
            } else {
                responseJson.put("message", "HTTP error code: $responseCode")
            }
            conn.disconnect()
        } catch (e: Exception) {
            responseJson.put("message", "Error: ${e.message}")
        }

        return responseJson
    }

    private fun updateUI(result: JSONObject) {
        Log.d("updateUI", "Result JSON: $result")

        val dataObject = result.optJSONObject("data")
        val fName = dataObject?.optString("C_FName", "")
        val lName = dataObject?.optString("C_LName", "")
        val phone = dataObject?.optString("C_No", "")
        val email = dataObject?.optString("C_ID", "")

        ContactFNameTxt.setText(fName)
        ContactLNameTxt.setText(lName)
        ContactPhoneTxt.setText(phone)
        ContactEmailTxt.setText(email)
    }

    private fun enableEditMode() {
        ContactFNameTxt.isEnabled = true
        ContactLNameTxt.isEnabled = true
        ContactPhoneTxt.isEnabled = true
        ContactEmailTxt.isEnabled = true
    }

    private fun disableEditMode() {
        ContactFNameTxt.isEnabled = false
        ContactLNameTxt.isEnabled = false
        ContactPhoneTxt.isEnabled = false
        ContactEmailTxt.isEnabled = false
    }

    private fun saveChangesToDatabase() {
        val editedFirstName = ContactFNameTxt.text.toString().trim()
        val editedLastName = ContactLNameTxt.text.toString().trim()
        val editedPhone = ContactPhoneTxt.text.toString().trim()
        val editedEmail = ContactEmailTxt.text.toString().trim()

        val jsonObject = JSONObject()
        jsonObject.put("C_FName", editedFirstName)
        jsonObject.put("C_LName", editedLastName)
        jsonObject.put("C_No", editedPhone)
        jsonObject.put("C_ID", editedEmail)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.158.182/alcotrack/update_contact_details.php")
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val wr = OutputStreamWriter(conn.outputStream)
                wr.write(jsonObject.toString())
                wr.flush()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d("saveChangesToDatabase", "Update Response: $response")
                } else {
                    Log.e("saveChangesToDatabase", "HTTP error code: $responseCode")
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("saveChangesToDatabase", "Error: ${e.message}")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")
        val email = intent.getStringExtra("Email") ?: ""
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, home::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_status -> {
                val intent = Intent(this, status::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_contact -> {
                Toast.makeText(this, "Already on Contact page", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_history -> {
                val intent = Intent(this, History::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_profile -> {
                val intent = Intent(this, profile::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                builder.setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout") { _, _ ->
                        navigateToLogin()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
        drawerLayout.closeDrawers() // Close the drawer
        return true
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
