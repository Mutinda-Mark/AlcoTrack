package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.alcotrack.network.RegisterResponse
import com.example.alcotrack.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class register : AppCompatActivity() {

    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var contactFirstNameInput: EditText
    private lateinit var contactLastNameInput: EditText
    private lateinit var contactPhoneInput: EditText
    private lateinit var contactEmailInput: EditText

    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firstNameInput = findViewById(R.id.RegFNameTxt)
        lastNameInput = findViewById(R.id.regLNameTxt)
        phoneInput = findViewById(R.id.regPhoneTxt)
        emailInput = findViewById(R.id.regEmailTxt)
        passwordInput = findViewById(R.id.regPassTxt)
        confirmPasswordInput = findViewById(R.id.regCPassTxt)

        contactFirstNameInput = findViewById(R.id.ContactFNameTxt)
        contactLastNameInput = findViewById(R.id.ContactLNameTxt)
        contactPhoneInput = findViewById(R.id.ContactPhoneTxt)
        contactEmailInput = findViewById(R.id.ContactEmailTxt)

        registerButton = findViewById(R.id.RegisterBtn)

        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val firstName = firstNameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        val contactFirstName = contactFirstNameInput.text.toString().trim()
        val contactLastName = contactLastNameInput.text.toString().trim()
        val contactPhone = contactPhoneInput.text.toString().trim()
        val contactEmail = contactEmailInput.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || contactFirstName.isEmpty() || contactLastName.isEmpty() || contactPhone.isEmpty() || contactEmail.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.apiService.registerUser(
            firstName, lastName, phone, email, password,contactFirstName, contactLastName, contactPhone, contactEmail)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        if (registerResponse != null) {
                            Toast.makeText(this@register, registerResponse.message, Toast.LENGTH_SHORT).show()
                            // Redirect to login page after 3 seconds
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@register, login::class.java)
                                startActivity(intent)
                                finish()
                            }, 3000) // 3000 milliseconds = 3 seconds
                        }
                    } else {
                        Toast.makeText(this@register, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@register, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
