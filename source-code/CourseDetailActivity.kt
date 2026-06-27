package com.example.emergencyaid

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore

class CourseDetailActivity : Activity() {

    private lateinit var navButtons: MutableList<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navButtons = mutableListOf()

        val title = intent.getStringExtra("course_title") ?: "Course Title"
        val definition =
            intent.getStringExtra("course_definition") ?: "Course definition coming soon..."
        val link = intent.getStringExtra("course_link")

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#FFF5F5F5".toColorInt())
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }


        val titleView = TextView(this).apply {
            text = title
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }
        contentLayout.addView(titleView)


        val definitionView = TextView(this).apply {
            setTextColor(Color.BLACK)
            text = definition
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }
        contentLayout.addView(definitionView)


        if (link != null && link.isNotEmpty()) {
            // If course has a link (new dynamic course)
            val imageView = ImageView(this).apply {
                visibility = View.GONE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    600
                ).apply {
                    bottomMargin = 20
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val openLinkButton = Button(this).apply {
                text = getString(R.string.open_resource)
                setBackgroundColor("#7A1505".toColorInt())
                setTextColor(Color.WHITE)
            }

            when {
                link.contains("youtube") || link.contains("youtu.be") -> {
                    openLinkButton.text = getString(R.string.watch_on_youtube)
                    openLinkButton.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                    }
                }

                link.endsWith(".jpg") || link.endsWith(".jpeg") || link.endsWith(".png") -> {
                    imageView.visibility = View.VISIBLE
                    Glide.with(imageView.context)
                        .load(link)
                        .into(imageView)
                    openLinkButton.visibility = View.GONE
                }

                else -> {
                    openLinkButton.text = getString(R.string.open_link)
                    openLinkButton.setOnClickListener {

                        startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                    }
                }
            }

            contentLayout.addView(imageView)
            contentLayout.addView(openLinkButton)

        } else {

            when (title) {
                "CPR for Adults" -> {
                    addStaticCourse(
                        contentLayout,
                        R.drawable.cpr_diagram,
                        "android.resource://${packageName}/raw/cpr_video",
                        """
                        Basics of CPR:
                        CPR (Cardiopulmonary Resuscitation) is a life-saving technique used in emergencies such as cardiac arrest.

                        Steps for Adult CPR:
                        1. Check responsiveness and breathing.
                        2. Call emergency services.
                        3. Place the person on a firm, flat surface.
                        4. Start chest compressions: 100–120 per minute, 2 inches deep.
                        5. Give 2 rescue breaths if trained.
                        6. Continue until medical help arrives.
                        """.trimIndent()
                    )
                }

                "Bleeding Control" -> {
                    addStaticCourse(
                        contentLayout,
                        R.drawable.bleeding_control,
                        "android.resource://${packageName}/raw/bleeding_video",
                        """
                        Basics of Bleeding Control:
                        1. Apply direct pressure.
                        2. Elevate injured area.
                        3. Maintain pressure.
                        4. Use tourniquet only if trained.
                        5. Call emergency services if severe.
                        """.trimIndent()
                    )
                }

                "Burn Treatment" -> {
                    addStaticCourse(
                        contentLayout,
                        R.drawable.burn_treatment_diagram,
                        "android.resource://${packageName}/raw/burn_video",
                        """
                        Burn Treatment Basics:
                        1. Cool with running water for 10 mins.
                        2. Remove tight items before swelling.
                        3. Cover burn loosely.
                        4. Avoid ice or ointments.
                        """.trimIndent()
                    )
                }

                "Choking Aid" -> {
                    addStaticCourse(
                        contentLayout,
                        R.drawable.choking_aid_diagram,
                        "android.resource://${packageName}/raw/choking_video",
                        """
                        Choking Aid:
                        1. Ask if choking.
                        2. Give 5 back blows.
                        3. Give 5 abdominal thrusts.
                        4. Alternate until object is expelled.
                        5. Start CPR if unresponsive.
                        """.trimIndent()
                    )
                }
            }
        }

        scrollView.addView(contentLayout)
        rootLayout.addView(scrollView)

        val finishReadingButton = Button(this).apply {
            text = getString(R.string.finish_reading)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            textSize = 18f
            setPadding(0, 16, 0, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
                bottomMargin = 20
                marginStart = 40
                marginEnd = 40
            }
            background = resources.getDrawable(R.drawable.rounded_button_bg, null)

            setOnClickListener {
                val sharedPrefs = getSharedPreferences("user", MODE_PRIVATE)
                val email = sharedPrefs.getString("email", "") ?: ""

                if (email.isEmpty()) {
                    Toast.makeText(
                        this@CourseDetailActivity,
                        "Please log in first.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val userCoursesRef = db.collection("user_activities").document(email).collection("courses")
                val recordsRef = db.collection("records")


                val updates = mapOf(
                    "action" to "Completed",
                    "status" to "Completed",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )


                userCoursesRef
                    .whereEqualTo("title", title)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val courseDoc = snapshot.documents[0].reference
                            courseDoc.update(updates)


                            recordsRef
                                .whereEqualTo("email", email)
                                .whereEqualTo("title", title)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { recordSnapshot ->
                                    if (!recordSnapshot.isEmpty) {
                                        val recordDoc = recordSnapshot.documents[0].reference
                                        recordDoc.update(updates)
                                    } else {

                                        val newRecord = hashMapOf(
                                            "email" to email,
                                            "title" to title,
                                            "action" to "Completed",
                                            "status" to "Completed",
                                            "timestamp" to com.google.firebase.Timestamp.now()
                                        )
                                        recordsRef.add(newRecord)
                                    }
                                }


                            val dbHelper = DatabaseHelper()
                            dbHelper.insertOrUpdateRecord(email, title, "Completed") {}


                            val dialog = AlertDialog.Builder(this@CourseDetailActivity)
                                .setTitle("✅ Course Completed")
                                .setMessage("You’ve successfully finished the course: $title.\n\nYour progress has been updated.")
                                .setPositiveButton("OK") { d, _ -> d.dismiss() }
                                .create()

                            dialog.setOnShowListener {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
                                dialog.window?.setBackgroundDrawable("#7A1505".toColorInt().toDrawable())
                            }
                            dialog.show()

                        } else {
                            Toast.makeText(
                                this@CourseDetailActivity,
                                "⚠️ You haven’t started this course yet.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@CourseDetailActivity,
                            "❌ Error updating record.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


        rootLayout.addView(finishReadingButton)

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER
        }
        val tabs = listOf("HOME", "Courses", "Chat", "Record", "Profile")
        tabs.forEach { label ->
            val button = createNavButton(label)
            navButtons.add(button)
            bottomNav.addView(button)
        }

        rootLayout.addView(bottomNav)
        setContentView(rootLayout)
    }

    private fun addStaticCourse(layout: LinearLayout, imageRes: Int, videoPath: String, content: String) {
        val imageView = ImageView(this).apply {
            setImageResource(imageRes)
            layoutParams = LinearLayout.LayoutParams(1000, 800).apply {
                gravity = Gravity.CENTER
                bottomMargin = 20
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        layout.addView(imageView)

        val videoView = VideoView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                600
            ).apply {
                topMargin = 10
                bottomMargin = 20
            }
            setVideoPath(videoPath)
            setOnPreparedListener { it.isLooping = true }
            setOnClickListener { if (isPlaying) pause() else start() }
        }

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()

        layout.addView(videoView)

        val textView = TextView(this).apply {
            text = content
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }
        layout.addView(textView)
    }

    private fun createNavButton(label: String): Button {
        val selectedColor = "#FFFFFF".toColorInt()
        val unselectedColor = "#7A1505".toColorInt()
        val selectedTextColor = "#7A1505".toColorInt()
        val unselectedTextColor = "#FFFFFF".toColorInt()

        return Button(this).apply {
            text = label
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (label == "Courses") selectedTextColor else unselectedTextColor)
            setBackgroundColor(if (label == "Courses") selectedColor else unselectedColor)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            setOnClickListener {
                navButtons.forEach {
                    it.setBackgroundColor(unselectedColor)
                    it.setTextColor(unselectedTextColor)
                }

                setBackgroundColor(selectedColor)
                setTextColor(selectedTextColor)

                when (label) {
                    "HOME" -> startActivity(Intent(this@CourseDetailActivity, UserDashboardActivity::class.java))
                    "Courses" -> startActivity(Intent(this@CourseDetailActivity, CoursesActivity::class.java))
                    "Chat" -> startActivity(Intent(this@CourseDetailActivity, AidBotChatActivity::class.java))
                    "Record" -> startActivity(Intent(this@CourseDetailActivity, RecordActivity::class.java))
                    "Profile" -> startActivity(Intent(this@CourseDetailActivity, ProfileActivity::class.java))
                }
                finish()
            }
        }
    }
}
