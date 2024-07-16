package com.example.alcotrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import android.content.Intent
import android.view.MenuItem
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class profile : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var profileFNameTxt: EditText
    private lateinit var profileLNameTxt: EditText
    private lateinit var profileEmailTxt: EditText
    private lateinit var profilePhoneTxt: EditText
    private lateinit var profilePassTxt: EditText
    private lateinit var profileEditBtn: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private var isEditMode = false
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")


        // Initialize the EditTexts and Button
        profileFNameTxt = findViewById(R.id.profileFNameTxt)
        profileLNameTxt = findViewById(R.id.profileLNameTxt)
        profileEmailTxt = findViewById(R.id.profileEmailTxt)
        profilePhoneTxt = findViewById(R.id.profilePhoneTxt)
        profilePassTxt = findViewById(R.id.profilePasswordTxt)
        profileEditBtn = findViewById(R.id.profileEditBtn)

        // Initialize NavigationView and DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Setup ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Enable the Up button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        headerSubtitle.text = "$FName $LName"

        // Retrieve email from intent extras
        email = intent.getStringExtra("Email") ?: ""

        // Fetch and populate profile details
        fetchProfileDetails(email)

        // Set click listener for Edit button
        profileEditBtn.setOnClickListener {
            if (!isEditMode) {
                enableEditMode()
                isEditMode = true
                profileEditBtn.text = "Save"
                Toast.makeText(this, "Edit mode", Toast.LENGTH_SHORT).show()
            } else {
                saveChangesToDatabase()
                disableEditMode()
                isEditMode = false
                profileEditBtn.text = "Edit"
                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchProfileDetails(email: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val result = getProfileDetailsFromDatabase(email)
            runOnUiThread {
                updateUI(result)
            }
        }
    }

    private fun getProfileDetailsFromDatabase(email: String): JSONObject {
        val responseJson = JSONObject()
        try {
            val url = URL("http://192.168.158.182/alcotrack/get_profile_details.php?email=$email")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                responseJson.put("data", JSONObject(response))
                Log.d("getProfileDetails", "JSON Response: $response")
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
        val dataObject = result.optJSONObject("data")?.optJSONObject("data")

        if (dataObject != null) {
            val firstName = dataObject.optString("FirstName", "")
            val lastName = dataObject.optString("LastName", "")
            val phone = dataObject.optString("Phone", "")
            val password = dataObject.optString("Password", "")

            // Update EditTexts with retrieved data
            profileFNameTxt.setText(firstName)
            profileLNameTxt.setText(lastName)
            profileEmailTxt.setText(email)
            profilePhoneTxt.setText(phone)
            profilePassTxt.setText(password)
        } else {
            Log.e("updateUI", "No data found in JSON response")
        }
    }

    private fun enableEditMode() {
        profileFNameTxt.isEnabled = true
        profileLNameTxt.isEnabled = true
        profileEmailTxt.isEnabled = true
        profilePhoneTxt.isEnabled = true
        profilePassTxt.isEnabled = true
    }

    private fun disableEditMode() {
        profileFNameTxt.isEnabled = false
        profileLNameTxt.isEnabled = false
        profileEmailTxt.isEnabled = false
        profilePhoneTxt.isEnabled = false
        profilePassTxt.isEnabled = false
    }

    private fun saveChangesToDatabase() {
        val editedFirstName = profileFNameTxt.text.toString().trim()
        val editedLastName = profileLNameTxt.text.toString().trim()
        val editedEmail = profileEmailTxt.text.toString().trim()
        val editedPhone = profilePhoneTxt.text.toString().trim()
        val editedPassword = profilePassTxt.text.toString().trim()

        val jsonObject = JSONObject().apply {
            put("FirstName", editedFirstName)
            put("LastName", editedLastName)
            put("Email", editedEmail)
            put("Phone", editedPhone)
            put("Password", editedPassword)
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.158.182/alcotrack/update_profile_details.php")
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
                val intent = Intent(this, contactperson::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_history -> {
                val intent = Intent(this, History::class.java)
                intent.putExtra("FName", FName)
                intent.putExtra("LName", LName)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            R.id.nav_profile -> {
                Toast.makeText(this, "Already on Profile page", Toast.LENGTH_SHORT).show()
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
        drawerLayout.closeDrawers()
        return true
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
