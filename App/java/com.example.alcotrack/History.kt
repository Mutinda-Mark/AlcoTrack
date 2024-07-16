package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.CalendarView
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
import java.net.HttpURLConnection
import java.net.URL

class History : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var historyNameTextView: TextView
    private lateinit var highestAlcTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize views
        historyNameTextView = findViewById(R.id.HistoryNameTxt)
        highestAlcTextView = findViewById(R.id.HistoryHighAlcTxt)
        timeTextView = findViewById(R.id.HistoryTimeTxt)
        calendarView = findViewById(R.id.HistoryCalenderImg)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Initialize NavigationView and DrawerLayout
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Setup ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Enable the Up button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Retrieve FName and LName from intent extras
        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        headerSubtitle.text = "$FName $LName"

        // Set FName and LName in TextView
        historyNameTextView.text = "$FName $LName"

        // Set OnDateChangeListener for CalendarView
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth" // month is zero-based
            fetchHighestAlcoholReading(selectedDate)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchHighestAlcoholReading(selectedDate: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://192.168.158.182/alcotrack/get_highest_alcohol_reading.php?date=$selectedDate")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)

                    // Parse JSON response
                    val highestAlc = jsonObject.optString("HighestAlcohol", "N/A")
                    val timeOfHighest = jsonObject.optString("TimeOfHighest", "N/A")

                    // Update UI on the main thread
                    runOnUiThread {
                        highestAlcTextView.text = highestAlc
                        timeTextView.text = timeOfHighest
                    }
                } else {
                    Log.e("History", "HTTP error code: $responseCode")
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("History", "Error fetching data: ${e.message}")
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
                Toast.makeText(this, "Already on History page", Toast.LENGTH_SHORT).show()

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
