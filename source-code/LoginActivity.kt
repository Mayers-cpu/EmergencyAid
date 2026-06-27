package com.example.emergencyaid

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var startLoginButton: Button
    private lateinit var emergencyButton: Button
    private lateinit var formLayout: LinearLayout
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordBtn: TextView
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var signUpText: TextView
    private lateinit var exploreButton: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        val savedEmail = prefs.getString("email", null)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (savedEmail != null && currentUser != null && currentUser.isEmailVerified) {
            // User is already logged in
            startActivity(Intent(this, UserDashboardActivity::class.java))
            finish()
            return
        }

        // -------------------- CONNECT XML IDs --------------------
        startLoginButton = findViewById(R.id.startLoginButton)
        emergencyButton = findViewById(R.id.emergencyButton)
        formLayout = findViewById(R.id.formLayout)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordBtn = findViewById(R.id.togglePasswordBtn)
        loginButton = findViewById(R.id.loginButton)
        forgotPassword = findViewById(R.id.forgotPassword)
        signUpText = findViewById(R.id.signUpText)
        exploreButton = findViewById(R.id.exploreButton)

        // ---------------- SHOW THE LOGIN FORM ----------------
        startLoginButton.setOnClickListener {
            formLayout.visibility = LinearLayout.VISIBLE
            startLoginButton.visibility = Button.GONE
            emergencyButton.visibility = Button.GONE
        }

        // ----------------- PASSWORD VISIBILITY TOGGLE -----------------
        togglePasswordBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.text = getString(R.string.hide)
            } else {
                passwordInput.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.text = getString(R.string.show)
            }

            // Prevent font glitch
            passwordInput.typeface = Typeface.DEFAULT

            // Move cursor to end
            passwordInput.setSelection(passwordInput.text.length)
        }

        // ------------------------- LOGIN BUTTON -------------------------
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Admin login check
            if (email == "admin@admin.com" && password == "admin123") {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Firebase Login
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val user = it.user

                    // CHECK IF EMAIL IS VERIFIED
                    if (user != null && !user.isEmailVerified) {
                        FirebaseAuth.getInstance().signOut()

                        AlertDialog.Builder(this)
                            .setTitle("Email Not Verified")
                            .setMessage("Please verify your Gmail first before logging in.")
                            .setPositiveButton("OK", null)
                            .show()

                        return@addOnSuccessListener
                    }

                    // ----- SAVE EMAIL SESSION -----
                    val userEmail = user?.email ?: email

                    getSharedPreferences("user", MODE_PRIVATE).edit()
                        .putString("email", userEmail)
                        .apply()

                    // ----- Show Welcome Dialog -----
                    val dialogView = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(40, 40, 40, 40)
                        setBackgroundColor("#B71C1C".toColorInt())

                        val title = TextView(context).apply {
                            text = getString(R.string.emergency_welcome_title)
                            textSize = 22f
                            setTypeface(null, Typeface.BOLD)
                            setTextColor(android.graphics.Color.WHITE)
                            gravity = Gravity.CENTER
                        }

                        val message = TextView(context).apply {
                            text = getString(R.string.emergency_welcome_message)
                            textSize = 16f
                            setTextColor(android.graphics.Color.WHITE)
                            gravity = Gravity.CENTER
                            setPadding(0, 20, 0, 0)
                        }

                        addView(title)
                        addView(message)
                    }

                    AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            startActivity(Intent(this, UserDashboardActivity::class.java))
                            finish()
                        }
                        .create()
                        .show()
                }

                .addOnFailureListener {
                    AlertDialog.Builder(this)
                        .setTitle("🚨 Login Failed")
                        .setMessage(it.message)
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
                }
        }

        // ------------------ FORGOT PASSWORD ------------------
        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        val fullText = "Don't have an account? Sign Up"
        val highlight = "Sign Up"

        val spannable = android.text.SpannableString(fullText)
        val start = fullText.indexOf(highlight)

// Make "Sign Up" red
        spannable.setSpan(
            android.text.style.ForegroundColorSpan("#7A1505".toColorInt()),
            start,
            start + highlight.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

// Make "Don't have an account?" black
        spannable.setSpan(
            android.text.style.ForegroundColorSpan("#000000".toColorInt()),
            0,
            start,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signUpText.text = spannable

        signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --------------------- EXPLORE WITHOUT LOGIN ---------------------
        exploreButton.setOnClickListener {
            startActivity(Intent(this, GuestDashboardActivity::class.java))
        }

        // --------------------- EMERGENCY QUICK ACCESS ---------------------
        emergencyButton.setOnClickListener {
            val intent = Intent(this, EmergencyActivity::class.java)
            intent.putExtra("emergency_type", "Emergency")
            startActivity(intent)
        }
    }
}
