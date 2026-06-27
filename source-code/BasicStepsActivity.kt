package com.example.emergencyaid

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.toColorInt

class BasicStepsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(40, 60, 40, 60)
        }


        val title = TextView(this).apply {
            text = "🔥 Basic Emergency Steps"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        val desc = TextView(this).apply {
            text = """
                Here are some essential first-aid steps you can follow 
                even without internet access or an account.
                
                These are not a substitute for medical care, 
                but can help you provide immediate support 
                while waiting for professionals.
            """.trimIndent()
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 20, 0, 40)
        }
        layout.addView(desc)


        layout.addView(makeStep(
            "1️⃣ Stop Bleeding",
            "Apply firm pressure using a clean cloth or bandage. " +
                    "Do not remove the cloth if it gets soaked — add another layer on top. " +
                    "Keep pressure until bleeding stops."
        ))


        layout.addView(makeStep(
            "2️⃣ Perform CPR (if needed)",
            "If the person isn’t breathing, push hard and fast in the center of the chest — about 100–120 compressions per minute. " +
                    "If trained, alternate with rescue breaths (30:2 ratio)."
        ))


        layout.addView(makeStep(
            "3️⃣ Managing a Fever",
            "Encourage rest and fluids. Use a cool compress and avoid heavy clothing. " +
                    "Seek medical help if temperature exceeds 39°C (102°F) or lasts over 3 days."
        ))


        layout.addView(makeStep(
            "4️⃣ Treat Burns",
            "Cool the burn under running water for 10–15 minutes. " +
                    "Do not apply ice or toothpaste. Cover lightly with a clean, non-stick dressing."
        ))


        layout.addView(makeStep(
            "5️⃣ Unconscious Person",
            "Check responsiveness and breathing. If breathing, place the person in recovery position. " +
                    "If not breathing, start CPR and call for emergency help immediately."
        ))


        layout.addView(makeStep(
            "6️⃣ Snake Bite",
            "Keep the person calm and still. Do not try to suck out venom or cut the wound. " +
                    "Keep the bite area below heart level and seek emergency medical care."
        ))


        layout.addView(makeStep(
            "7️⃣ Poisoning",
            "Do not induce vomiting. Identify the substance if possible and contact emergency services or poison control right away."
        ))


        layout.addView(makeStep(
            "8️⃣ Electric Shock",
            "Do not touch the person if they are still in contact with electricity. Turn off the source first, then check for breathing and pulse."
        ))


        layout.addView(makeStep(
            "9️⃣ Heatstroke",
            "Move to a cooler place, loosen clothing, and apply cool water on skin. " +
                    "Do not give large amounts of water if the person is unconscious."
        ))


        layout.addView(makeStep(
            "🔟 Heart Attack",
            "Call emergency services immediately. Have the person sit and stay calm. " +
                    "If available and not allergic, give aspirin while waiting for help."
        ))

        val backButton = Button(this).apply {
            text = "⬅️ Back"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor("#7A1505".toColorInt())
            setOnClickListener { finish() }
        }

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 40, 0, 20)
            addView(backButton)
        }

        layout.addView(buttonContainer)

        scrollView.addView(layout)
        setContentView(scrollView)
    }


    private fun makeStep(title: String, details: String): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 20)
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }

        val detailsView = TextView(this).apply {
            text = details
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 10, 0, 0)
        }

        container.addView(titleView)
        container.addView(detailsView)
        return container
    }
}
