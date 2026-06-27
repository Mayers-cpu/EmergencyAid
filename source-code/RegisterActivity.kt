package com.example.emergencyaid

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.*
import androidx.core.graphics.toColorInt
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : Activity() {

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var emergencyContactInput: EditText
    private lateinit var genderInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var conditionsInput: EditText
    private lateinit var allergiesInput: EditText
    private lateinit var bloodTypeInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // ----------------- BIND UI -----------------
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        emergencyContactInput = findViewById(R.id.emergencyContactInput)
        genderInput = findViewById(R.id.genderInput)
        ageInput = findViewById(R.id.ageInput)
        conditionsInput = findViewById(R.id.conditionsInput)
        allergiesInput = findViewById(R.id.allergiesInput)
        bloodTypeInput = findViewById(R.id.bloodTypeInput)
        locationInput = findViewById(R.id.locationInput)

        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        val togglePasswordBtn = findViewById<TextView>(R.id.togglePasswordBtn)
        val toggleConfirmPasswordBtn = findViewById<TextView>(R.id.toggleConfirmPasswordBtn)

        // ---------- PASSWORD TOGGLE ----------
        togglePasswordBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.text = "Hide"
            } else {
                passwordInput.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.text = "Show"
            }

            // Prevent font glitch & keep cursor
            passwordInput.typeface = android.graphics.Typeface.DEFAULT
            passwordInput.setSelection(passwordInput.text.length)
        }

// ---------- CONFIRM PASSWORD TOGGLE ----------
        toggleConfirmPasswordBtn.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible

            if (isConfirmPasswordVisible) {
                confirmPasswordInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleConfirmPasswordBtn.text = "Hide"
            } else {
                confirmPasswordInput.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleConfirmPasswordBtn.text = "Show"
            }

            confirmPasswordInput.typeface = android.graphics.Typeface.DEFAULT
            confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
        }

        val termsCheckbox = findViewById<CheckBox>(R.id.termsCheckbox)
        val createButton = findViewById<Button>(R.id.createButton)
        val signInText = findViewById<TextView>(R.id.signInText)

        // ----------------- HIGHLIGHT "SIGN IN" -----------------
        val fullText = "Already have an account? Sign In"
        val highlight = "Sign In"
        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(highlight)

        if (startIndex >= 0) {
            spannable.setSpan(
                ForegroundColorSpan("#7A1505".toColorInt()),
                startIndex,
                startIndex + highlight.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        signInText.text = spannable
        signInText.setTextColor(Color.BLACK)
        signInText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // ----------------- CREATE BUTTON LOGIC -----------------
        createButton.setOnClickListener {

            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val emergencyContact = emergencyContactInput.text.toString().trim()
            val gender = genderInput.text.toString().trim()
            val age = ageInput.text.toString().trim()
            val conditions = conditionsInput.text.toString().trim()
            val allergies = allergiesInput.text.toString().trim()
            val bloodType = bloodTypeInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            // ----------------- VALIDATION -----------------
            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                emergencyContact.isEmpty() || gender.isEmpty() ||
                age.isEmpty() || conditions.isEmpty() || allergies.isEmpty() ||
                bloodType.isEmpty() || location.isEmpty() || password.isEmpty()
            ) {
                showErrorDialog("Please fill in all fields.")
                return@setOnClickListener
            }

            if (phone.length != 11) {
                phoneInput.error = "Phone number must be exactly 11 digits"
                return@setOnClickListener
            }

            if (emergencyContact.length != 11) {
                emergencyContactInput.error = "Emergency contact must be exactly 11 digits"
                return@setOnClickListener
            }

            if (age.toIntOrNull() == null || age.toInt() <= 0) {
                ageInput.error = "Enter a valid age"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            if (!email.endsWith("@gmail.com")) {
                emailInput.error = "Must use a Gmail address"
                return@setOnClickListener
            }

            if (!termsCheckbox.isChecked) {
                showErrorDialog("You must agree to the terms and conditions.")
                return@setOnClickListener
            }

            // ----------------- FIREBASE AUTH CREATE -----------------
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser!!.uid

                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "emergencyContact" to emergencyContact,
                        "gender" to gender,
                        "age" to age,
                        "conditions" to conditions,
                        "allergies" to allergies,
                        "bloodType" to bloodType,
                        "location" to location
                    )

                    firestore.collection("users")
                        .document(email)
                        .set(userData)
                        .addOnSuccessListener {

                            // Save email to SharedPreferences
                            val prefs = getSharedPreferences("user", MODE_PRIVATE)
                            prefs.edit().putString("email", email).apply()

                            // 🔥 SEND EMAIL VERIFICATION
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    showSuccessDialog(
                                        "A verification link has been sent to your Gmail. Please verify before logging in."
                                    )
                                }
                                ?.addOnFailureListener {
                                    showErrorDialog("Account created, but verification email failed to send.")
                                }
                        }
                        .addOnFailureListener {
                            showErrorDialog("Failed to save user data.")
                        }
                }
                .addOnFailureListener {
                    showErrorDialog(it.message ?: "Registration failed.")
                }
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    fun showSuccessDialog(message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }
}
