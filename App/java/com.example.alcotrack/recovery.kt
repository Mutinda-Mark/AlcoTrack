package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class recovery : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        emailInput = findViewById(R.id.RecEmailTxt)
        val recoverBtn = findViewById<Button>(R.id.RecBtn)

        recoverBtn.setOnClickListener {
            val email = emailInput.text.toString()
            sendRecoveryEmail(email)
        }
    }

    private fun sendRecoveryEmail(email: String) {
        val requestBody = FormBody.Builder()
            .add("email", email)
            .build()

        val request = Request.Builder()
            .url("http://192.168.158.182/alcotrack/password_recovery.php") // Update with your PHP URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@recovery, "Failed to retrieve password: ${e.message}", Toast.LENGTH_SHORT).show()
                    returnToLogin()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonObject = JSONObject(responseData)
                val retrievedPassword = jsonObject.optString("password")
                val errorMessage = jsonObject.optString("error")

                if (retrievedPassword.isNotEmpty()) {
                    sendEmail(email, retrievedPassword)
                } else {
                    runOnUiThread {
                        Toast.makeText(this@recovery, errorMessage, Toast.LENGTH_SHORT).show()
                        returnToLogin()
                    }
                }
            }
        })
    }

    private fun sendEmail(email: String, password: String) {
        val senderEmail = "alcolock3@gmail.com"
        val senderPassword = "qwrx zgon jbdy nazs" // Use the App Password here

        val properties = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.auth", "true")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                addRecipient(Message.RecipientType.TO, InternetAddress(email))
                subject = "Password Recovery"
                setText("Your password is: $password")
            }

            Thread {
                try {
                    Transport.send(mimeMessage)
                    runOnUiThread {
                        Toast.makeText(this, "Email sent successfully", Toast.LENGTH_SHORT).show()
                        returnToLogin()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                        returnToLogin()
                    }
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
            returnToLogin()
        }
    }

    private fun returnToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }, 3000) // 3 seconds delay
    }
}
