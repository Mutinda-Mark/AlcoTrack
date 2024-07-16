package com.example.alcotrack

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.os.Handler
import java.io.OutputStreamWriter
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle

class status : AppCompatActivity(), LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private lateinit var StaNameTextView: TextView
    private lateinit var StaAlcoTxt: TextView
    private lateinit var StaEngTxt: TextView
    private lateinit var contactButton: Button

    private lateinit var locationManager: LocationManager
    private lateinit var contactNumber: String

    private val handler = Handler()
    private lateinit var refreshRunnable: Runnable

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        // Initialize the views
        StaNameTextView = findViewById(R.id.StaNameTxt)
        StaAlcoTxt = findViewById(R.id.StaAlcoTxt)
        StaEngTxt = findViewById(R.id.StaEngTxt)
        contactButton = findViewById(R.id.ContactAidBtn)

        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")
        val email = intent.getStringExtra("Email")

        StaNameTextView.text = "$FName $LName"


        drawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val headerView = navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        headerSubtitle.text = "$FName $LName"

        // Fetch and display readings
        GlobalScope.launch(Dispatchers.IO) {
            val result = fetchReadings()
            runOnUiThread {
                updateUI(result)
            }
        }

        // Start periodic data refresh
        startPeriodicDataRefresh()

        // Fetch contact details

        if (email != null) {
            GlobalScope.launch(Dispatchers.IO) {
                val contactResult = fetchContactDetails(email)
                runOnUiThread {
                    if (contactResult.first) {
                        if (contactResult.second.has("C_No")) {
                            contactNumber = contactResult.second.getString("C_No")
                            Log.d("ContactNumber", "Contact Number: $contactNumber")
                        } else {
                            Toast.makeText(this@status, "Contact number found.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@status, contactResult.second.optString("message", "An error occurred."), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("EmailError", "Email is null")
            Toast.makeText(this, "Email not provided.", Toast.LENGTH_SHORT).show()
        }

        // Set click listener for contact button
        contactButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                sendLocationSMS()
            }
        }
    }

    private fun startPeriodicDataRefresh() {
        refreshRunnable = object : Runnable {
            override fun run() {
                fetchDataAndUpdateUI()
                handler.postDelayed(this, 3000) // Refresh every 3000 milliseconds (3 seconds)
            }
        }

        // Schedule the first execution
        handler.post(refreshRunnable)
    }

    private fun fetchDataAndUpdateUI() {
        GlobalScope.launch(Dispatchers.IO) {
            val result = fetchReadings()
            runOnUiThread {
                updateUI(result)
            }
        }
    }

    private fun fetchReadings(): JSONObject {
        val responseJson = JSONObject()

        try {
            val url = URL("http://192.168.158.182/alcotrack/get_readings.php")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                responseJson.put("data", JSONObject(response))
                Log.d("fetchReadings", "JSON Response: $response")
            } else {
                responseJson.put("message", "HTTP error code: $responseCode")
            }
            conn.disconnect()
        } catch (e: Exception) {
            responseJson.put("message", "Error: ${e.message}")
        }

        return responseJson
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun updateUI(result: JSONObject) {
        Log.d("updateUI", "Result JSON: $result") // Log the entire JSON object to check its content

        // Retrieve the 'data' object from the JSON response
        val dataObject = result.optJSONObject("data")

        // Check if 'data' object exists and retrieve 'status' from it
        val status = dataObject?.optString("status")

        Log.d("updateUI", "Status: $status") // Log the status to understand why it's not 'success'

        if (status == "success") {
            // Retrieve V_AlcoholLevel as an integer
            val alcoholLevelStr = dataObject?.optString("V_AlcoholLevel")
            val alcoholLevel = alcoholLevelStr?.toIntOrNull() ?: 0

            Log.d("updateUI", "Alcohol Level: $alcoholLevel")

            StaAlcoTxt.text = alcoholLevel.toString()

            if (alcoholLevel < 300) {
                StaEngTxt.text = "Running"
            } else {
                StaEngTxt.text = "Off"
            }
        } else {
            StaEngTxt.text = "No data available"
        }
    }

    private fun fetchContactDetails(email: String): Pair<Boolean, JSONObject> {
        val responseJson = JSONObject()
        var success = false

        try {
            val url = URL("http://192.168.158.182/alcotrack/get_contact.php")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "POST"
            conn.doOutput = true

            val postData = "email=$email"
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(postData)
            writer.flush()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                Log.d("fetchContactDetails", "Response: $response") // Log the response
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getString("status") == "success") {
                    success = true
                    // Safely retrieve C_No
                    val contactObject = jsonResponse.optJSONObject("contact")
                    if (contactObject != null) {
                        val contactNumberStr = contactObject.optString("C_No", null)
                        if (contactNumberStr != null) {
                            contactNumber = contactNumberStr.toString()
                            Log.d("ContactNumber", "Contact Number: $contactNumber")
                        } else {
                            Log.e("fetchContactDetails", "C_No not found in response")
                        }
                    }
                } else {
                    responseJson.put("success", false)
                    responseJson.put("message", "Failed to fetch contact details: ${jsonResponse.optString("message", "No message")}")
                }
            } else {
                responseJson.put("success", false)
                responseJson.put("message", "HTTP error code: $responseCode")
            }
            conn.disconnect()
        } catch (e: Exception) {
            responseJson.put("success", false)
            responseJson.put("message", "Error: ${e.message}")
        }

        return Pair(success, responseJson)
    }


    private fun sendLocationSMS() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
        } catch (e: SecurityException) {
            Log.e("sendLocationSMS", "Location permission error: ${e.message}")
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        val countryCode = "+254" // Change to your desired country code

        // Generate Google Maps link
        val locationLink = "https://www.google.com/maps/@$latitude,$longitude,30z" // Adjust zoom level as needed

        Log.d("sendLocationSMS", "Contact Number before sending: $contactNumber")

        if (contactNumber.isNotEmpty()) {
            val formattedContactNumber = "$countryCode$contactNumber"
            val message = "Hey, I'm at $locationLink. Kindly help."

            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(formattedContactNumber, null, message, null, null)
                Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "SMS sending failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("sendLocationSMS", "Contact number is empty")
            Toast.makeText(this, "Contact number is not available.", Toast.LENGTH_SHORT).show()
        }

        // Stop requesting location updates to save battery
        locationManager.removeUpdates(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            sendLocationSMS()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
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
                 // Optional: Finish current activity if you don't want to keep it in the back stack
            }


            R.id.nav_status -> {
                Toast.makeText(this, "Already on Status page", Toast.LENGTH_SHORT).show()
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

            // Add more cases for other menu items if needed
        }
        // Close the navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks when the activity is destroyed
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
}
