package com.example.emergencyaid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat


class EmergencyCountdownActivity : AppCompatActivity() {

    private lateinit var countdownText: TextView
    private var counter = 3
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_countdown)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        val rootLayout = findViewById<LinearLayout>(R.id.emergencyLayout)
        rootLayout.setBackgroundColor(android.graphics.Color.RED)

        countdownText = findViewById(R.id.countdownText)
        startCountdown()
    }

    private fun startCountdown() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                countdownText.text = counter.toString()
                if (counter == 0) {
                    makeFakeCall()
                } else {
                    counter--
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun makeFakeCall() {
        Toast.makeText(this, "📞 Emergency call triggered", Toast.LENGTH_LONG).show()

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:911".toUri()
        }
        startActivity(intent)
        finish()
    }
}