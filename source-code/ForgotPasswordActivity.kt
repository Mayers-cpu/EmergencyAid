package com.example.emergencyaid

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.graphics.toColorInt
import androidx.appcompat.app.AlertDialog
import android.graphics.Color

class ForgotPasswordActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#FFF5F5F5".toColorInt())
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.emergency_logo)
            layoutParams = LinearLayout.LayoutParams(400, 400).apply {
                topMargin = 50
                bottomMargin = 10
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }
        rootLayout.addView(logo)

        val formLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 50
                marginEnd = 50
            }
        }

        val emailInput = EditText(this).apply {
            hint = "Email"
            background = inputBackground()
            setPadding(40, 30, 40, 30)
            setTextColor("#000000".toColorInt())
            setHintTextColor("#7A1505".toColorInt())

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }

        val newPasswordInput = EditText(this).apply {
            hint = "New Password"
            background = inputBackground()
            setPadding(40, 30, 40, 30)

            // Force text and hint color to black
            setTextColor("#000000".toColorInt())
            setHintTextColor("#7A1505".toColorInt())
            background?.colorFilter = null
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }

        val resetButton = Button(this).apply {
            text = getString(R.string.reset_password)
            background = buttonBackground()
            setTextColor("#FFFFFF".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        resetButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val newPassword = newPasswordInput.text.toString().trim()

            var isValid = true

            // Reset field backgrounds
            emailInput.background = inputBackground()
            newPasswordInput.background = inputBackground()

            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                emailInput.background = inputBackgroundError()
                isValid = false
            }

            if (newPassword.isEmpty()) {
                newPasswordInput.error = "New password is required"
                newPasswordInput.background = inputBackgroundError()
                isValid = false
            }

            if (!isValid) {
                val dialogBuilder = AlertDialog.Builder(this)
                val dialogLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(50, 40, 50, 40)
                    setBackgroundColor("#B71C1C".toColorInt()) // dark red
                }

                val titleView = TextView(this).apply {
                    text = getString(R.string.alert_title_empty)
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 20)
                }

                val messageView = TextView(this).apply {
                    text = getString(R.string.alert_message_empty)
                    setTextColor(Color.WHITE)
                    textSize = 16f
                    gravity = Gravity.CENTER
                }

                dialogLayout.addView(titleView)
                dialogLayout.addView(messageView)

                dialogBuilder.setView(dialogLayout)
                dialogBuilder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = dialogBuilder.create()
                dialog.show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                    setBackgroundColor("#D32F2F".toColorInt())
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                }
                return@setOnClickListener
            }

            val dbHelper = DatabaseHelper()
            dbHelper.updatePasswordByEmail(email, newPassword) { success ->
                if (success) {
                    // ✅ Success dialog
                    val successDialogBuilder = AlertDialog.Builder(this)
                    val dialogLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(50, 40, 50, 40)
                        setBackgroundColor("#B71C1C".toColorInt())
                    }

                    val titleView = TextView(this).apply {
                        text = getString(R.string.alert_title_success)
                        setTextColor(Color.WHITE)
                        textSize = 20f
                        setTypeface(null, Typeface.BOLD)
                        gravity = Gravity.CENTER
                        setPadding(0, 0, 0, 20)
                    }

                    val messageView = TextView(this).apply {
                        text = getString(R.string.alert_message_password_reset)
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }

                    dialogLayout.addView(titleView)
                    dialogLayout.addView(messageView)

                    successDialogBuilder.setView(dialogLayout)
                    successDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }

                    val dialog = successDialogBuilder.create()
                    dialog.show()

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                        setBackgroundColor("#D32F2F".toColorInt())
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                    }

                } else {
                    // ❌ Failure dialog
                    val errorDialogBuilder = AlertDialog.Builder(this)
                    val dialogLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(50, 40, 50, 40)
                        setBackgroundColor("#B71C1C".toColorInt())
                    }

                    val titleView = TextView(this).apply {
                        text = getString(R.string.reset_failed_title)
                        setTextColor(Color.WHITE)
                        textSize = 20f
                        setTypeface(null, Typeface.BOLD)
                        gravity = Gravity.CENTER
                        setPadding(0, 0, 0, 20)
                    }

                    val messageView = TextView(this).apply {
                        text = getString(R.string.reset_failed_message)
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER
                    }

                    dialogLayout.addView(titleView)
                    dialogLayout.addView(messageView)

                    errorDialogBuilder.setView(dialogLayout)
                    errorDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

                    val dialog = errorDialogBuilder.create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                        setBackgroundColor("#D32F2F".toColorInt())
                        setTextColor(Color.WHITE)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
            }
        }
        formLayout.addView(emailInput)
        formLayout.addView(newPasswordInput)
        formLayout.addView(resetButton)
        rootLayout.addView(formLayout)

        val backToLogin = TextView(this).apply {
            text = getString(R.string.back_to_login)
            gravity = Gravity.CENTER
            setTextColor("#000000".toColorInt())
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 30
                bottomMargin = 20
            }

            setOnClickListener {
                startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
                finish()
            }
        }
        formLayout.addView(backToLogin)

        setContentView(rootLayout)
    }

    // Normal input background
    private fun inputBackground(): GradientDrawable = GradientDrawable().apply {
        setColor("#FFFFFF".toColorInt())
        cornerRadius = 25f
        setStroke(2, "#CCCCCC".toColorInt())
    }

    // Error input background
    private fun inputBackgroundError(): GradientDrawable = GradientDrawable().apply {
        setColor("#FFFFFF".toColorInt())
        cornerRadius = 25f
        setStroke(3, "#FF0000".toColorInt())
    }

    // Button background
    private fun buttonBackground(): GradientDrawable = GradientDrawable().apply {
        setColor("#7A1505".toColorInt())
        cornerRadius = 50f
    }
}
