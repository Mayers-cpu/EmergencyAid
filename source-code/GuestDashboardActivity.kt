package com.example.emergencyaid

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.graphics.toColorInt

class GuestDashboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌟 Root Layout
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 60, 40, 60)
        }

        // ✅ Scrollable content area
        val scrollContent = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // makes scroll content take up remaining space
            )

            addView(LinearLayout(this@GuestDashboardActivity).apply {
                orientation = LinearLayout.VERTICAL

                // 🧭 Title
                val titleView = TextView(this@GuestDashboardActivity).apply {
                    text = "Welcome, Guest 👋"
                    textSize = 24f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor("#7A1505".toColorInt())
                    gravity = Gravity.CENTER
                }
                addView(titleView)

                // 🧠 Description / Intro
                val descView = TextView(this@GuestDashboardActivity).apply {
                    text = """
        Welcome to the Emergency Aid App!
        
        This app helps users learn how to respond during emergencies,
        access first aid knowledge, and complete short interactive courses.
        
        As a guest, you can:
        • View basic emergency response steps  
        • Explore available courses and their details  
        
        To save progress, take quizzes, and earn completion records,
        please log in or create an account.
    """.trimIndent()
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                    setTypeface(typeface, Typeface.BOLD) // <-- Makes text bold
                    setPadding(0, 30, 0, 50)
                }
                addView(descView)

                // 🔥 Basic Emergency Steps Button
                val emergencyStepsBtn = Button(this@GuestDashboardActivity).apply {
                    text = "🔥 Basic Emergency Steps"
                    setTextColor(Color.WHITE)
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    setBackgroundColor("#B71C1C".toColorInt())
                    setPadding(0, 30, 0, 30)
                    setOnClickListener {
                        val intent = Intent(this@GuestDashboardActivity, BasicStepsActivity::class.java)
                        intent.putExtra("guest_mode", true)
                        startActivity(intent)
                    }
                }
                addView(emergencyStepsBtn)

                // 🩺 Info Section: How It Works
                val howItWorks = TextView(this@GuestDashboardActivity).apply {
                    text = """
        🩺 How It Works:
        
        1. Learn — Read about first aid topics and watch short lessons.  
        2. Practice — Test your knowledge through interactive quizzes.  
        3. Chat — Chat with AidBot for guidance.  
        4. Record — Registered users can track progress and achievements.  
        5. Apply — Use your learning to help others in real-life emergencies.
    """.trimIndent()
                    textSize = 16f
                    setTextColor(Color.parseColor("#333333"))
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 50, 0, 50)

                    // Add spacing between lines
                    setLineSpacing(10f, 1.2f) // extra spacing 10px, line height multiplier 1.2
                }
                addView(howItWorks)
            })
        }

        // 🔙 Back to Login Button (fixed near bottom)
        val backBtn = Button(this).apply {
            text = "⬅ Back to Login"
            setTextColor("#7A1505".toColorInt())
            textSize = 16f
            setPadding(0, 25, 0, 25)
            background = null
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }

        rootLayout.addView(scrollContent)
        rootLayout.addView(backBtn)

        setContentView(rootLayout)
    }
}
