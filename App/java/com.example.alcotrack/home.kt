package com.example.alcotrack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")
        val email = intent.getStringExtra("Email") ?: ""

        val welcomeTextView = findViewById<TextView>(R.id.DisWelcomeTxt)
        welcomeTextView.text = "Welcome, $FName $LName"

        val statusImg = findViewById<ImageView>(R.id.StatusImg)
        val contactImg = findViewById<ImageView>(R.id.ContactImg)
        val historyImg = findViewById<ImageView>(R.id.HistoryImg)
        val profileImg = findViewById<ImageView>(R.id.ProfileImg)

        statusImg.setOnClickListener {
            val intent = Intent(this, status::class.java)
            intent.putExtra("FName", FName)
            intent.putExtra("LName", LName)
            intent.putExtra("Email", email)
            startActivity(intent)
        }

        contactImg.setOnClickListener {
            val intent = Intent(this, contactperson::class.java)
            intent.putExtra("FName", FName)
            intent.putExtra("LName", LName)
            intent.putExtra("Email", email)
            startActivity(intent)
        }

        historyImg.setOnClickListener {
            val intent = Intent(this, History::class.java)
            intent.putExtra("FName", FName)
            intent.putExtra("LName", LName)
            intent.putExtra("Email", email)
            startActivity(intent)
        }

        profileImg.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            intent.putExtra("FName", FName)
            intent.putExtra("LName", LName)
            intent.putExtra("Email", email)
            startActivity(intent)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
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
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Build a confirmation dialog
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    // Navigate back to log in activity
                    navigateToLogin()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val FName = intent.getStringExtra("FName")
        val LName = intent.getStringExtra("LName")
        val email = intent.getStringExtra("Email") ?: ""

        when (item.itemId) {
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
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
