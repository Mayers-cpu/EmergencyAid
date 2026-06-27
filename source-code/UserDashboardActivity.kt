package com.example.emergencyaid

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.util.TypedValue

private lateinit var navButtons: MutableList<Button>

class UserDashboardActivity : Activity() {

    private fun handleNotificationRedirection() {
        val tabToOpen = intent.getStringExtra("open_tab")

        when (tabToOpen) {
            "Courses" -> {

                startActivity(Intent(this, CoursesActivity::class.java))
            }
        }
    }

    private fun applyStatusBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }
    }
    private lateinit var emergencyOverlay: LinearLayout
    private lateinit var bottomNav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationRedirection()

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val currentUserEmail = intent.getStringExtra("user_email") ?: "default@example.com"

        val frameLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor("#FFF5F5F5".toColorInt())
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val welcomeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#7A1505".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            applyStatusBarPadding(this)

            val welcomeText = TextView(this@UserDashboardActivity).apply {
                text = getString(R.string.welcome_user)
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor("#FFFFFF".toColorInt())
                gravity = Gravity.CENTER
            }
            addView(welcomeText)
        }

        val emergencyTextLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val emergencyText = TextView(this@UserDashboardActivity).apply {
                text = getString(R.string.are_you_in_emergency)
                setTypeface(null, Typeface.BOLD)
                textSize = 16f
                setTextColor("#7A1505".toColorInt())
            }

            val descriptionText = TextView(this@UserDashboardActivity).apply {
                text = getString(R.string.sos_description)
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor("#000000".toColorInt())
            }

            addView(emergencyText)
            addView(descriptionText)
        }

        val sosLogo = ImageView(this).apply {
            setImageResource(R.drawable.sos3)
            val logoSize = (screenWidth * 0.65).toInt()

            layoutParams = LinearLayout.LayoutParams(logoSize, logoSize).apply {
                gravity = Gravity.CENTER
                bottomMargin = (screenHeight * 0.02).toInt()
            }

            scaleType = ImageView.ScaleType.FIT_CENTER


            setOnClickListener {
                val intent = Intent(this@UserDashboardActivity, EmergencyActivity::class.java)
                startActivity(intent)
            }
        }

        val emergencyLabel = TextView(this).apply {
            text = getString(R.string.what_is_your_emergency)
            textSize = 16f
            setPadding(30, 10, 30, 10)
            gravity = Gravity.CENTER_HORIZONTAL
            setTextColor("#7A1505".toColorInt())
        }

        val emergencyGrid = GridLayout(this).apply {
            columnCount = 2
            setPadding(30, 16, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val emergencies = mapOf(
            "Choking" to "\uD83C\uDD98 Choking – Quick Steps\n" +
                    "" +
                    "Check: Can the person cough, talk, or breathe?\n" +
                    "" +
                    "✅ Yes - Encourage coughing.\n" +
                    "" +
                    "❌ No - Act fast.\n" +
                    "" +
                    "If Severe Choking (can’t breathe/speak):\n" +
                    "" +
                    "Give 5 back blows between shoulder blades.\n" +
                    "" +
                    "If not working - Do 5 abdominal thrusts (Heimlich).\n" +
                    "" +
                    "Repeat until object comes out or they collapse.\n" +
                    "" +
                    "If Person Becomes Unresponsive:\n" +
                    "" +
                    "Call emergency number immediately.\n" +
                    "" +
                    "Start CPR (30 compressions, 2 breaths).\n" +
                    "" +
                    "⚠\uFE0F Special Note\n" +
                    "" +
                    "Infants (<1 year): 5 back blows + 5 chest thrusts (no abdominal thrusts).\n" +
                    "" +
                    "Pregnant/obese adults: Use chest thrusts, not abdominal.",

            "Heart Attack" to "❤\uFE0F Heart Attack – Quick Steps\n" +
                    "" +
                    "Recognize Symptoms:\n" +
                    "" +
                    "-Chest pain/pressure (may spread to arm, neck, back, or jaw).\n" +
                    "" +
                    "-Shortness of breath, sweating, nausea, or dizziness.\n" +
                    "" +
                    "Act Immediately:\n" +
                    "" +
                    "-☎\uFE0F Call emergency services right away.\n" +
                    "" +
                    "-Have the person sit and rest in a comfortable position.\n" +
                    "" +
                    "Give Aspirin (if available & not allergic):\n" +
                    "" +
                    "-One adult tablet (325 mg) → chew slowly.\n" +
                    "" +
                    "If Unconscious & No Breathing:\n" +
                    "" +
                    "-Start CPR (30 compressions, 2 breaths).\n" +
                    "" +
                    "-Use AED if available and follow prompts.\n" +
                    "" +
                    "⚠\uFE0F Note\n" +
                    "" +
                    "Do not let them walk around.\n" +
                    "" +
                    "Stay calm and keep them reassured until help arrives.",

            "Stroke" to "" +
                    "" +
                    "\uD83E\uDDE0 Stroke – Quick Steps\n" +
                    "" +
                    "Recognize FAST Signs:\n" +
                    "" +
                    "-Face drooping = ask them to smile.\n" +
                    "" +
                    "-Arm weakness = ask them to raise both arms.\n" +
                    "" +
                    "-Speech trouble = slurred or strange speech.\n" +
                    "" +
                    "-Time = call emergency services immediately.\n" +
                    "" +
                    "While Waiting for Help:\n" +
                    "" +
                    "-Keep the person sitting or lying on their side (if drowsy/unconscious).\n" +
                    "" +
                    "-Do not give food, drink, or medicine.\n" +
                    "" +
                    "-Stay calm and monitor breathing.\n" +
                    "" +
                    "If Unconscious & Not Breathing:\n" +
                    "" +
                    "-Start CPR until help arrives.\n" +
                    "" +
                    "⚠\uFE0F Note: Every minute counts. Stroke treatment is most effective within the first few hours.",

            "Seizure" to "⚡ Seizure – Quick Steps\n" +
                    "" +
                    "Stay Calm & Keep Safe:\n" +
                    "" +
                    "-Move objects away to prevent injury.\n" +
                    "" +
                    "-Cushion their head if possible.\n" +
                    "" +
                    "-Loosen tight clothing around neck.\n" +
                    "" +
                    "Do Not:\n" +
                    "" +
                    "❌ Do not hold them down.\n" +
                    "" +
                    "❌ Do not put anything in their mouth.\n" +
                    "" +
                    "After Seizure Stops:\n" +
                    "" +
                    "-Place them on their side (recovery position).\n" +
                    "" +
                    "-Stay with them until fully alert.\n" +
                    "" +
                    "Call Emergency Services If:\n" +
                    "" +
                    "-Seizure lasts longer than 5 minutes.\n" +
                    "" +
                    "-Another seizure starts immediately.\n" +
                    "" +
                    "-They have trouble breathing, are injured, or this is their first seizure.",

            "Severe Bleeding" to "\uD83E\uDE78 Severe Bleeding – Quick Steps\n" +
                    "" +
                    "Call Emergency Services immediately.\n" +
                    "" +
                    "Apply Direct Pressure\n" +
                    "" +
                    "-Use a clean cloth, bandage, or your hand.\n" +
                    "" +
                    "-Keep firm, steady pressure on the wound.\n" +
                    "" +
                    "Do Not Remove the Cloth/Bandage\n" +
                    "" +
                    "-If blood soaks through, place another layer on top.\n" +
                    "" +
                    "If Possible\n" +
                    "" +
                    "-Keep the injured area raised above heart level.\n" +
                    "" +
                    "-Apply a tourniquet only if trained and bleeding is life-threatening.\n" +
                    "" +
                    "Monitor\n" +
                    "" +
                    "-Keep person warm and calm.\n" +
                    "" +
                    "-Watch for signs of shock (pale, cold, weak, confused).",

            "Allergic Reaction" to "\uD83C\uDF3F Allergic Reaction – Quick Steps\n" +
                    "Recognize Severe Reaction (Anaphylaxis)\n" +
                    "" +
                    "-Swelling of face, lips, or throat.\n" +
                    "" +
                    "-Difficulty breathing or swallowing.\n" +
                    "" +
                    "-Rash, hives, or itching.\n" +
                    "" +
                    "-Feeling faint, dizzy, or signs of shock.\n" +
                    "" +
                    "Act Immediately:\n" +
                    "" +
                    "-☎\uFE0F Call emergency services.\n" +
                    "" +
                    "-Use epinephrine auto-injector (EpiPen) if available → inject into outer thigh.\n" +
                    "" +
                    "-Keep person lying down with legs raised, unless breathing is easier sitting up.\n" +
                    "" +
                    "While Waiting for Help:\n" +
                    "" +
                    "-If symptoms don’t improve in 5–15 minutes, give a second epinephrine dose (if available).\n" +
                    "" +
                    "-Keep them calm and monitor breathing.\n" +
                    "" +
                    "-Be ready to start CPR if they stop breathing.\n" +
                    "" +
                    "⚠\uFE0F Note: Even if symptoms improve after epinephrine, the person must still go to the hospital — reactions can return."
        )

        for ((label, description) in emergencies) {
            val button = Button(this).apply {
                text = label
                setBackgroundColor("#FFFFFF".toColorInt())
                setTextColor("#7A1505".toColorInt())
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(16, 16, 16, 16)
                }

                setOnClickListener {
                    // Create the TextView
                    val messageView = TextView(this@UserDashboardActivity).apply {
                        text = description
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        setLineSpacing(1.2f, 1.2f)
                        setPadding(dp(20), dp(16), dp(20), dp(16))
                        setTextColor("#000000".toColorInt())
                        setTypeface(null, Typeface.NORMAL)
                    }


                    val scrollView = ScrollView(this@UserDashboardActivity).apply {
                        addView(messageView)
                        setPadding(20, 20, 20, 20)
                    }


                    val dialog = AlertDialog.Builder(this@UserDashboardActivity)
                        .setTitle(label)
                        .setView(scrollView)
                        .setPositiveButton("OK") { d: DialogInterface, _: Int ->
                            d.dismiss()
                        }
                        .setNegativeButton("Chat with AidBot") { _, _ ->
                            val intent = Intent(this@UserDashboardActivity, AidBotChatActivity::class.java)
                            intent.putExtra("EMERGENCY_MESSAGE", label)
                            intent.putExtra("user_email", currentUserEmail)
                            startActivity(intent)
                        }
                        .create()

                    // Show dialog
                    dialog.show()

                    //Style buttons + add space
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                        setBackgroundColor("#7A1505".toColorInt())
                        setTextColor("#FFFFFF".toColorInt())

                        val params = (layoutParams as LinearLayout.LayoutParams).apply {
                            marginStart = 20
                        }
                        layoutParams = params
                    }

                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                        setBackgroundColor("#7A1505".toColorInt())
                        setTextColor("#FFFFFF".toColorInt())

                        val params = (layoutParams as LinearLayout.LayoutParams).apply {
                            marginEnd = 20
                        }
                        layoutParams = params
                    }
                }
            }
            emergencyGrid.addView(button)
        }

        bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val selectedColor = "#FFFFFF".toColorInt()
        val unselectedColor = "#7A1505".toColorInt()
        val selectedTextColor = "#7A1505".toColorInt()
        val unselectedTextColor = "#FFFFFF".toColorInt()

        navButtons = mutableListOf()

        val tabs = listOf("HOME", "Courses", "Chat", "Record", "Profile")
        fun createNavButton(label: String): Button {
            return Button(this).apply {
                text = label
                setTypeface(null, Typeface.BOLD)
                setTextColor(unselectedTextColor)
                setBackgroundColor(unselectedColor)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                setOnClickListener {
                    navButtons.forEach {
                        it.setBackgroundColor(unselectedColor)
                        it.setTextColor(unselectedTextColor)
                    }
                    setBackgroundColor(selectedColor)
                    setTextColor(selectedTextColor)

                    val intent = when (label) {
                        "Courses" -> Intent(this@UserDashboardActivity, CoursesActivity::class.java)
                        "Chat" -> Intent(this@UserDashboardActivity, AidBotChatActivity::class.java)
                        "Record" -> Intent(this@UserDashboardActivity, RecordActivity::class.java)
                        "Profile" -> Intent(this@UserDashboardActivity, ProfileActivity::class.java)
                        else -> null
                    }

                    intent?.apply {
                        putExtra("user_email", currentUserEmail)

                        if (label !in tabs) {
                            if (label != "Chat") {
                                putExtra("EMERGENCY_MESSAGE", label)
                            }
                        }

                        startActivity(this)
                    }
                }
            }
        }

        tabs.forEach { label ->
            val button = createNavButton(label)
            navButtons.add(button)
            bottomNav.addView(button)
        }

        val selectedTab = intent.getStringExtra("selected_tab") ?: "HOME"
        val defaultButton = navButtons.find { it.text == selectedTab }
        defaultButton?.performClick()


        val contentWrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

            addView(welcomeLayout)
            addView(emergencyTextLayout)

            // ---- TOP SECTION: Only SOS Logo ----
            addView(LinearLayout(this@UserDashboardActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    20f
                )

                addView(Space(this@UserDashboardActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                })

                addView(sosLogo.apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                })

                addView(Space(this@UserDashboardActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    )
                })
            })

            // ---- PUSH BUTTONS DOWNWARD ----
            addView(Space(this@UserDashboardActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            })

            // ---- BUTTON SECTION (Label + 6 buttons) ----
            addView(LinearLayout(this@UserDashboardActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                addView(emergencyLabel.apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    textSize = 16f
                    setPadding(0, dp(6), 0, dp(6))
                })

                addView(emergencyGrid)
            })
        }

        rootLayout.addView(contentWrapper)
        rootLayout.addView(bottomNav)

        emergencyOverlay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#6D1606".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            val alertText = TextView(this@UserDashboardActivity).apply {
                text = getString(R.string.calling_emergency_services)
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                setTextColor("#FFFFFF".toColorInt())
                gravity = Gravity.CENTER
            }

            addView(alertText)
            visibility = LinearLayout.GONE
        }
        frameLayout.addView(rootLayout)
        frameLayout.addView(emergencyOverlay)
        setContentView(frameLayout)

    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        handleNotificationRedirection()
    }

    private fun dp(value: Int): Int {
        val scale = resources.displayMetrics.density
        return (value * scale).toInt()
    }



}
