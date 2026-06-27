package com.example.emergencyaid

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import androidx.cardview.widget.CardView
import android.text.Editable
import android.text.TextWatcher


class RecordActivity : Activity() {

    @Suppress("unused")
    private fun formatTimestamp(timestampString: String): String {
        return try {
            val timestamp = timestampString.toLong()
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            timestampString
        }
    }

    private fun createListItem(
        title: String,
        action: String,
        timestamp: String,
        score: String?,
        context: Activity
    ): View {
        val cardView = CardView(context).apply {
            radius = 16f
            cardElevation = 8f
            useCompatPadding = true
            setCardBackgroundColor("#FFFFFF".toColorInt())
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 10, 20, 10)
            }
        }

        // Container inside the card
        val rowLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            setPadding(24, 20, 24, 20)
            gravity = Gravity.CENTER_VERTICAL
        }

        val rawScore = score?.substringBefore("/") ?: ""
        val rawTotal = score?.substringAfter("/") ?: ""

        val titleView = TextView(context).apply {
            text = when {
                rawScore.isNotEmpty() && rawTotal.isNotEmpty() ->
                    "$title (Score: $rawScore/$rawTotal)"
                action.contains("chat", true) -> "Chat"
                action.contains("call", true) -> "Call"
                else -> title
            }
            setTypeface(null, Typeface.BOLD)
            textSize = 14f
            setTextColor("#000000".toColorInt())  // <-- Make text black
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
        }

        val completedKeywords = listOf("Completed", "Finished", "Marked", "Done")
        val statusText = when {
            !score.isNullOrEmpty() && !score.startsWith("0/") -> "Score: $score"
            lowerActionContains(action, "quiz") -> "Completed"
            title.contains("chat", true) || action.contains("chat", true) -> "Chat"
            title.contains("call", true) || action.contains("call", true) -> "Call"
            completedKeywords.any { action.contains(it, true) } -> "Completed"
            else -> "Ongoing"
        }
        val statusColor = when {
            statusText.startsWith("Score:") -> "#388E3C"
            statusText == "Completed" -> "#388E3C"
            statusText == "Chat" -> "#7A1505"
            statusText == "Call" -> "#7A1505"
            else -> "#FFA500"
        }

        val statusView = TextView(context).apply {
            text = statusText
            textSize = 13f
            setTextColor("#FFFFFF".toColorInt())
            setBackgroundColor(statusColor.toColorInt())
            setPadding(20, 8, 20, 8)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }



        val dateView = TextView(context).apply {
            text = if (timestamp.isNotBlank()) timestamp else "Unknown date"
            textSize = 12f
            setTextColor("#888888".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Add all views
        rowLayout.addView(titleView)
        rowLayout.addView(statusView)
        rowLayout.addView(dateView)
        cardView.addView(rowLayout)

        rowLayout.setOnClickListener {
            val lowerTitle = title.lowercase()
            val lowerAction = action.lowercase()

            when {
                // CHAT
                lowerAction.contains("chat") || lowerTitle.contains("chat") ->
                    context.startActivity(Intent(context, AidBotChatActivity::class.java))

                lowerAction.contains("quiz") -> {
                    val scoreText = score ?: "No score available"
                    AlertDialog.Builder(context)
                        .setTitle("Quiz Result")
                        .setMessage("Score: $scoreText")
                        .setPositiveButton("OK", null)
                        .show()
                }

                // COURSE (separate from quiz)
                lowerAction.contains("course") || lowerTitle.contains("cpr") ||
                        lowerTitle.contains("bleeding") || lowerTitle.contains("burn") ||
                        lowerTitle.contains("choking") ->
                    context.startActivity(Intent(context, CoursesActivity::class.java))

                // PROFILE
                lowerAction.contains("profile") || lowerTitle.contains("profile") ->
                    context.startActivity(Intent(context, ProfileActivity::class.java))

                // HOME
                lowerAction.contains("home") || lowerTitle.contains("home") ->
                    context.startActivity(Intent(context, UserDashboardActivity::class.java))

                else ->
                    Toast.makeText(context, "No linked activity found", Toast.LENGTH_SHORT).show()
            }
        }

        return cardView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DatabaseHelper()
        val email = getSharedPreferences("user", MODE_PRIVATE)
            .getString("email", null)
            ?: intent.getStringExtra("user_email")
            ?: ""

        // listContainer MUST be declared BEFORE filters and scrollView
        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        // ---------------------------------------------------------
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#FFF5F5F5".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // Search + filters section
        val searchAndFilters = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 40, 20, 20)
        }

        val search = EditText(this).apply {
            hint = "Search records..."
            setPadding(20, 10, 20, 10)
            background = ResourcesCompat.getDrawable(resources, android.R.drawable.edit_text, null)
            setTextColor("#000000".toColorInt())
            setHintTextColor("#000000".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }

        val filterRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }
        val filters = listOf("All", "Chat", "Course", "Quiz")

        filters.forEach { filter ->
            filterRow.addView(Button(this).apply {
                text = filter
                textSize = 12f
                setPadding(20, 10, 20, 10)
                background = GradientDrawable().apply {
                    setColor("#7A1505".toColorInt())
                    cornerRadius = 40f
                }
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply { marginEnd = 10 }

                setOnClickListener {
                    applyFilter(filter, email, listContainer, db)
                }
            })
        }
        // ---------------------------------------------------------

        searchAndFilters.addView(filterRow)

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#7A1505".toColorInt())
            setPadding(16, 16, 16, 16)
            weightSum = 3f
        }

        val headers = listOf("Guides", "Completed", "Date")
        headers.forEach { title ->
            headerRow.addView(TextView(this).apply {
                text = title
                setTypeface(null, Typeface.BOLD)
                setTextColor("#FFFFFF".toColorInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
        }

        searchAndFilters.addView(search)
        searchAndFilters.addView(headerRow)

        // ScrollView wraps the listContainer
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            addView(listContainer)
        }

        searchAndFilters.addView(scrollView)
        contentLayout.addView(searchAndFilters)

        // Load records
        val prefs = getSharedPreferences("ui", MODE_PRIVATE)
        prefs.edit(commit = true) { putBoolean("hide_records", false) }
        val hideRecords = prefs.getBoolean("hide_records", false)

        if (!hideRecords) {
            db.getRecordsByEmail(email) { recordMaps ->
                runOnUiThread {
                    listContainer.removeAllViews()

                    recordMaps.forEach { map ->
                        val title = map["title"] as? String ?: ""
                        val action = map["action"] as? String ?: ""

                        val timestampMillis = when (val ts = map["timestamp"]) {
                            is com.google.firebase.Timestamp -> ts.toDate().time
                            is Long -> ts
                            else -> System.currentTimeMillis()
                        }

                        val formattedDate = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                            .format(Date(timestampMillis))

                        val rawScore = (map["score"] as? Long)?.toInt() ?: -1
                        val rawTotal = (map["total_questions"] as? Long)?.toInt() ?: -1

                        val score = if (rawScore >= 0 && rawTotal > 0) "$rawScore/$rawTotal" else null

                        val item = createListItem(title, action, formattedDate, score, this)
                        listContainer.addView(item)
                    }
                }
            }
        }

        // Search filter
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()

                db.getRecordsByEmail(email) { recordMaps ->
                    runOnUiThread {
                        listContainer.removeAllViews()

                        recordMaps.filter { m ->
                            val title = (m["title"] as? String)?.lowercase() ?: ""
                            val action = (m["action"] as? String)?.lowercase() ?: ""
                            title.contains(query) || action.contains(query)
                        }.forEach { map ->
                            val title = map["title"] as? String ?: ""
                            val action = map["action"] as? String ?: ""
                            val timestamp = System.currentTimeMillis().toString()

                            val rawScore = map["score"]?.toString()
                            val rawTotal = map["total_questions"]?.toString()

                            val scoreUI = if (!rawScore.isNullOrEmpty() && !rawTotal.isNullOrEmpty())
                                "$rawScore/$rawTotal"
                            else rawScore

                            listContainer.addView(createListItem(title, action, timestamp, scoreUI, this@RecordActivity))
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Clear history button
        val clearButton = Button(this).apply {
            text = "Clear History"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                cornerRadius = 60f
                setColor("#7A1505".toColorInt())
            }

            setOnClickListener {
                AlertDialog.Builder(this@RecordActivity)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to clear all history?")
                    .setPositiveButton("Yes") { _, _ ->
                        db.clearRecordsByEmail(email) { success ->
                            runOnUiThread {
                                if (success) {
                                    prefs.edit { putBoolean("hide_records", true) }
                                    listContainer.removeAllViews()
                                    Toast.makeText(this@RecordActivity, "History cleared", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        val clearHistoryLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(20, 10, 20, 10)
            addView(clearButton)
        }

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER

            val tabs = listOf("Home", "Courses", "Chat", "Record", "Profile")
            tabs.forEach { label ->
                addView(Button(this@RecordActivity).apply {
                    text = label
                    setTextColor(if (label == "Record") "#7A1505".toColorInt() else Color.WHITE)
                    setBackgroundColor(if (label == "Record") Color.WHITE else "#7A1505".toColorInt())
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)

                    setOnClickListener {
                        when (label) {
                            "Home" -> startActivity(Intent(this@RecordActivity, UserDashboardActivity::class.java))
                            "Courses" -> startActivity(Intent(this@RecordActivity, CoursesActivity::class.java))
                            "Chat" -> startActivity(Intent(this@RecordActivity, AidBotChatActivity::class.java))
                            "Profile" -> startActivity(Intent(this@RecordActivity, ProfileActivity::class.java))
                        }
                    }
                })
            }
        }

        rootLayout.addView(contentLayout)
        rootLayout.addView(clearHistoryLayout)
        rootLayout.addView(bottomNav)

        setContentView(rootLayout)
    }

    private fun applyFilter(
        filter: String,
        email: String,
        listContainer: LinearLayout,
        db: DatabaseHelper
    ) {
        db.getRecordsByEmail(email) { recordMaps ->
            runOnUiThread {
                listContainer.removeAllViews()

                val filtered = when (filter) {

                    "Chat" -> recordMaps.filter {
                        val title = (it["title"] as? String)?.lowercase() ?: ""
                        title.contains("chat")
                    }

                    "Course" -> recordMaps.filter {
                        val type = (it["type"] as? String)?.lowercase() ?: ""
                        type.contains("course")
                    }

                    "Quiz" -> recordMaps.filter {
                        val type = (it["action"] as? String)?.lowercase() ?: ""
                        type.contains("quiz")
                    }

                    else -> recordMaps
                }

                filtered.forEach { map ->
                    val title = map["title"] as? String ?: ""
                    val action = map["action"] as? String ?: ""

                    val timestampMillis = when (val ts = map["timestamp"]) {
                        is com.google.firebase.Timestamp -> ts.toDate().time
                        is Long -> ts
                        else -> System.currentTimeMillis()
                    }

                    val formattedDate = SimpleDateFormat(
                        "MMM dd, yyyy - hh:mm a",
                        Locale.getDefault()
                    ).format(Date(timestampMillis))

                    val score = map["score"]?.toString()

                    listContainer.addView(
                        createListItem(title, action, formattedDate, score, this)
                    )
                }
            }
        }
    }
    private fun lowerActionContains(action: String, keyword: String): Boolean {
        return action.lowercase().contains(keyword.lowercase())
    }
}