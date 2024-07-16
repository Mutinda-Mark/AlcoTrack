package com.example.alcotrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class login : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Find the views by their IDs
        val loginBtn = findViewById<Button>(R.id.Loginbtn)
        emailInput = findViewById(R.id.loginEmail)
        passwordInput = findViewById(R.id.LoginPass)

        val loginFPass = findViewById<TextView>(R.id.LoginFPass)
        val loginReg = findViewById<TextView>(R.id.LoginReg)

        // Set click listeners for each button/text view
        loginBtn.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            // Execute login request
            GlobalScope.launch(Dispatchers.IO) {
                val result = login(email, password)
                runOnUiThread {
                    Log.d("LoginDebug", "Login result: ${result.first}, Response: ${result.second}")
                    if (result.first) {
                        showSuccessDialog(result.second.getString("FName"), result.second.getString("LName"), email)
                    } else {
                        Toast.makeText(this@login, result.second.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loginFPass.setOnClickListener {
            val intent = Intent(this, recovery::class.java)
            startActivity(intent)
        }

        loginReg.setOnClickListener {
            val intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }

    private fun login(email: String, password: String): Pair<Boolean, JSONObject> {
        var success = false
        val responseJson = JSONObject()

        try {
            val url = URL("http://192.168.158.182/alcotrack/login.php")
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "POST"
            conn.doOutput = true

            val postData = "email=$email&password=$password"
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(postData)
            writer.flush()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(conn.inputStream)).use {
                    it.readText()
                }
                val jsonResponse = JSONObject(response)
                if (jsonResponse.getString("status") == "success") {
                    success = true
                    responseJson.put("FName", jsonResponse.getString("FName"))
                    responseJson.put("LName", jsonResponse.getString("LName"))
                }
                responseJson.put("success", success)
                responseJson.put("data", jsonResponse)
            } else {
                responseJson.put("success", false)
                responseJson.put("message", "HTTP error code: $responseCode")
            }
            conn.disconnect()
        } catch (e: Exception) {
            responseJson.put("success", false)
            responseJson.put("message", "Error: ${e.message}")
        }

        // Debug statement
        Log.d("LoginDebug", "ResponseJson: $responseJson")

        return Pair(success, responseJson)
    }

    private fun showSuccessDialog(firstName: String, lastName: String, email: String) {
        AlertDialog.Builder(this)
            .setTitle("Login Successful")
            .setMessage("Welcome $firstName $lastName!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this@login, home::class.java)
                intent.putExtra("FName", firstName)
                intent.putExtra("LName", lastName)
                intent.putExtra("Email", email) // Pass email to home activity
                startActivity(intent)
                finish()
            }
            .show()
    }
}
