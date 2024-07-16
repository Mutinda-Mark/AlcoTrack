package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the button by its ID
        val loginButton: Button = findViewById(R.id.Homebtn)

        // Set an OnClickListener on the login button
        loginButton.setOnClickListener {
            // Create an Intent to start the login activity
            val intent = Intent(this, login::class.java)
            startActivity(intent)
        }
    }
}