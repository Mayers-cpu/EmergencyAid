package com.example.emergencyaid

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toColorInt
import androidx.core.content.edit
import android.text.InputType
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.res.ColorStateList
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import android.text.Editable
import android.text.TextWatcher
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log


class AdminDashboardActivity : AppCompatActivity() {


    private val dbHelper = DatabaseHelper()
    private lateinit var contentLayout: FrameLayout
    private var currentTab = "manage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
                100
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#FFF5F5F5".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // logo
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.emergency_logo)
            layoutParams = LinearLayout.LayoutParams(400, 400).apply {
                topMargin = 50
                bottomMargin = 10
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }
        rootLayout.addView(logo)

        // content container (swappable views)
        contentLayout = FrameLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor("#FFFFFF".toColorInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        // bottom nav
        val bottomNav = BottomNavigationView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Colors
            setBackgroundColor("#7A1505".toColorInt())
            itemIconTintList = ColorStateList.valueOf("#FFFFFF".toColorInt())
            itemTextColor = ColorStateList.valueOf("#FFFFFF".toColorInt())
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

            menu.add(0, 1, 0, "Manage").setIcon(android.R.drawable.ic_menu_view)
            menu.add(0, 2, 1, "Add").setIcon(android.R.drawable.ic_input_add)
            menu.add(0, 3, 2, "Edit").setIcon(android.R.drawable.ic_menu_edit)
            menu.add(0, 4, 3, "Delete").setIcon(android.R.drawable.ic_menu_delete)
            menu.add(0, 5, 4, "Profile").setIcon(android.R.drawable.ic_menu_myplaces)
        }


        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                1 -> {
                    currentTab = "manage"
                    loadView(createManageView())
                }
                2 -> loadView(createAddView())
                3 -> loadView(createEditView())
                4 -> loadView(createDeleteView())
                5 -> loadView(createProfileView())
            }
            true
        }

        rootLayout.addView(contentLayout)
        rootLayout.addView(bottomNav)
        setContentView(rootLayout)


        loadView(createManageView())
    }

    private fun loadView(view: View) {
        contentLayout.removeAllViews()
        contentLayout.addView(view)
    }

    // ---------------- Manage (list all users) ----------------
    private fun createManageView(): ScrollView {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val scroll = ScrollView(this).apply { addView(layout) }
        val db = FirebaseFirestore.getInstance()

        // ---------- SEARCH BAR ----------
        val searchInput = AutoCompleteTextView(this).apply {
            hint = "Search by email..."
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
        }
        layout.addView(searchInput)

        val allUserContainers = mutableListOf<Pair<String, LinearLayout>>()
        // Load all users
        db.collection("users").get().addOnSuccessListener { result ->
            val emailsForAutoComplete = mutableListOf<String>()
            if (!result.isEmpty) {
                for (doc in result.documents) {
                    val user = doc.toObject(User::class.java) ?: continue
                    val userEmail = user.email ?: continue
                    emailsForAutoComplete.add(userEmail)

                    // ---------- Container for user info + activities ----------
                    val userContainer = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(20, 20, 20, 20)
                        background = AppCompatResources.getDrawable(
                            this@AdminDashboardActivity,
                            R.drawable.user_card_bg
                        )
                    }

                    // ---------- User Info Text ----------
                    val userInfoText = TextView(this).apply {
                        text = """
                        Name: ${user.name}
                        Phone: ${user.phone}
                        Emergency: ${user.emergencyContact}
                        Gender: ${user.gender}
                        Age: ${user.age}
                        Conditions: ${user.conditions}
                        Allergies: ${user.allergies}
                        Blood Type: ${user.bloodType}
                        Location: ${user.location}
                    """.trimIndent()
                        setTextColor(Color.BLACK)
                        textSize = 14f
                    }
                    userContainer.addView(userInfoText)

                    // ---------- Activities Container ----------
                    val activitiesLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                    userContainer.addView(activitiesLayout)

                    db.collection("records")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener { records ->
                            Log.d("AdminDashboard", "Found ${records.size()} records for '$userEmail'")

                            for (doc in records.documents) {
                                val recordData = doc.data ?: continue

                                // Create TextView for each record
                                val recordText = TextView(this).apply {
                                    text = "Title: ${recordData["title"]}, Action: ${recordData["action"]}"
                                    setTextColor(Color.DKGRAY)
                                    textSize = 12f
                                }
                                // Add to the user's activities container
                                activitiesLayout.addView(recordText)
                            }
                        }

                    // ---------- Download PDF Button (initially hidden) ----------
                    val downloadButton = Button(this).apply {
                        text = "Download PDF"
                        setBackgroundColor("#7A1505".toColorInt())
                        setTextColor(Color.WHITE)
                        setPadding(0, 10, 0, 10)
                        visibility = View.GONE
                        setOnClickListener {
                            generateUserPdf(user, userEmail, db)
                        }
                    }
                    userContainer.addView(downloadButton)

                    // ---------- Show PDF button on container click ----------
                    userContainer.setOnClickListener {
                        downloadButton.visibility = View.VISIBLE
                        Toast.makeText(this, "Email: $userEmail", Toast.LENGTH_SHORT).show()
                    }

                    // ---------- Add Divider ----------
                    layout.addView(userContainer)
                    layout.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 2
                        ).apply { topMargin = 20; bottomMargin = 20 }
                        setBackgroundColor("#CCCCCC".toColorInt())
                    })

                    allUserContainers.add(userEmail to userContainer)
                }

                // ---------- Setup autocomplete ----------
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    emailsForAutoComplete
                )
                searchInput.setAdapter(adapter)
            }

            // ---------- Search Filter ----------
            fun filterUsersByEmail(query: String) {
                allUserContainers.forEach { (email, container) ->
                    container.visibility =
                        if (email.contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                }
            }

            searchInput.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    filterUsersByEmail(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        return scroll
    }

        private fun generateUserPdf(user: User, email: String, db: FirebaseFirestore) {
            val startX = 20f
            val columnWidths = floatArrayOf(180f, 120f, 220f) // Title, Action, Time
            val rowHeight = 30f
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            var yPosition = 30f

            canvas.drawText("User Report", 200f, yPosition.toFloat(), paint)
            yPosition += 30
            canvas.drawText("Name: ${user.name}", 20f, yPosition.toFloat(), paint)
            yPosition += 20
            canvas.drawText("Email: ${user.email}", 20f, yPosition.toFloat(), paint)
            yPosition += 20

            db.collection("records").whereEqualTo("email", email).get()
                .addOnSuccessListener { records ->
                    paint.typeface = Typeface.DEFAULT_BOLD

                    val headers = listOf("Title", "Action", "Date")
                    var xPos = startX
                    paint.style = Paint.Style.STROKE   // border only
                    paint.strokeWidth = 1f
                    headers.forEachIndexed { index, header ->

                        // BORDER
                        paint.style = Paint.Style.STROKE
                        canvas.drawRect(
                            xPos,
                            yPosition,
                            xPos + columnWidths[index],
                            yPosition + rowHeight,
                            paint
                        )

                        // TEXT
                        paint.style = Paint.Style.FILL
                        canvas.drawText(header, xPos + 8, yPosition + 20, paint)

                        xPos += columnWidths[index]
                    }

                    yPosition += rowHeight
                    paint.typeface = Typeface.DEFAULT

                    for (recordDoc in records) {
                        val title = recordDoc.getString("title") ?: "Untitled"
                        val action = recordDoc.getString("action") ?: "Unknown"


                        val dateText = try {
                            val timestamp = recordDoc.getTimestamp("timestamp")
                            if (timestamp != null) {
                                SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                                    .format(timestamp.toDate())
                            } else {
                                "No date"
                            }
                        } catch (e: Exception) {
                            "Invalid date"
                        }

                        var cellX = startX
                        val rowData = listOf(title, action, dateText)

                        rowData.forEachIndexed { index, text ->
                            // draw cell border
                            paint.style = Paint.Style.STROKE
                            canvas.drawRect(
                                cellX,
                                yPosition,
                                cellX + columnWidths[index],
                                yPosition + rowHeight,
                                paint
                            )

    // TEXT
                            paint.style = Paint.Style.FILL
                            canvas.drawText(
                                text,
                                cellX + 8,
                                yPosition + 20,
                                paint
                            )

                            cellX += columnWidths[index]
                        }

                        yPosition += rowHeight
                    }

                    pdfDocument.finishPage(page)

                    val filename = "${user.name}_report.pdf"
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)

                    uri?.let {
                        try {
                            contentResolver.openOutputStream(it)?.use { output ->
                                pdfDocument.writeTo(output)
                            }
                            Toast.makeText(this, "PDF saved to Documents/$filename", Toast.LENGTH_LONG)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG)
                                .show()
                        }
                    } ?: Toast.makeText(this, "Unable to create file", Toast.LENGTH_LONG).show()

                    pdfDocument.close()

                }
        }

    //Add user, course
    private fun createAddView(): ScrollView {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        // --- Add User Section ---
        val nameInput = EditText(this).apply { hint = "Full Name" }
        val emailInput = EditText(this).apply { hint = "Email" }
        val phoneInput = EditText(this).apply {
            hint = "Phone Number"
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val emergencyContactInput = EditText(this).apply {
            hint = "Emergency Contact Number"
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val genderInput = EditText(this).apply { hint = "Gender" }
        val ageInput = EditText(this).apply {
            hint = "Age"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val conditionsInput = EditText(this).apply { hint = "Medical Conditions" }
        val allergiesInput = EditText(this).apply { hint = "Allergies" }
        val bloodTypeInput = EditText(this).apply { hint = "Blood Type" }
        val locationInput = EditText(this).apply { hint = "Location" }
        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val addUserButton = Button(this).apply {
            text = getString(R.string.add_user_button)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
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

                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        context,
                        "Please fill in required fields (name, email, password)",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val user = User(
                    name = name,
                    email = email,
                    phone = phone,
                    emergencyContact = emergencyContact,
                    gender = gender,
                    age = age,
                    conditions = conditions,
                    allergies = allergies,
                    bloodType = bloodType,
                    location = location,
                )

                val db = DatabaseHelper()
                db.insertUser(user) { success ->
                    if (success) {
                        Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(context, "Failed to add user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Course
        val courseTitleInput = EditText(this).apply {
            hint = "Course Title"
        }
        val courseDescInput = EditText(this).apply {
            hint = "Course Description (Admin Notes)"
            inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }
        val courseDefinitionInput = EditText(this).apply {
            hint = "Course Definition"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 5       // starting height
            maxLines = 20      // maximum expansion height
            setHorizontallyScrolling(false)  // allows wrapping and expansion
        }
        val courseLinkInput = EditText(this).apply {
            hint = "Video/Image Link (e.g. YouTube, Etc)"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        // --- QUIZ BUILDER SECTION ---
        val quizContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 20, 10, 20)
        }
        data class QuizFields(
            val questionInput: EditText,
            val optionA: EditText,
            val optionB: EditText,
            val optionC: EditText,
            val optionD: EditText,
            val correctAnswerSpinner: Spinner
        )

        val quizList = mutableListOf<QuizFields>()

        for (i in 1..10) {
            val questionInput = EditText(this).apply {
                hint = "Enter Question $i"
                minLines = 2
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }

            val optionA = EditText(this).apply { hint = "A. " }
            val optionB = EditText(this).apply { hint = "B. " }
            val optionC = EditText(this).apply { hint = "C. " }
            val optionD = EditText(this).apply { hint = "D. " }

            val correctAnswerSpinner = Spinner(this).apply {
                adapter = ArrayAdapter(
                    this@AdminDashboardActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    listOf("A", "B", "C", "D")
                )
            }

            val questionLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
                addView(TextView(this@AdminDashboardActivity).apply {
                    text = "Question $i"
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#7A1505"))
                })
                addView(questionInput)
                addView(optionA)
                addView(optionB)
                addView(optionC)
                addView(optionD)

                val spinnerLabel = TextView(this@AdminDashboardActivity).apply {
                    text = "Select Correct Answer:"
                    setTextColor(Color.parseColor("#7A1505"))
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 10, 0, 5)
                }
                addView(spinnerLabel)
                addView(correctAnswerSpinner)
            }

            quizContainer.addView(questionLayout)
            quizList.add(QuizFields(questionInput, optionA, optionB, optionC, optionD, correctAnswerSpinner))
        }

// --- SAVE BUTTON ---
        val addCourseButton = Button(this).apply {
            text = "Save Course & Quiz"
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)

            setOnClickListener {
                val title = courseTitleInput.text.toString().trim()
                val definition = courseDefinitionInput.text.toString().trim()
                val desc = courseDescInput.text.toString().trim()
                val link = courseLinkInput.text.toString().trim()

                if (title.isBlank() || definition.isBlank()) {
                    Toast.makeText(this@AdminDashboardActivity, "Please fill in title and description", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                var hasEmpty = false
                quizList.forEachIndexed { index, quiz ->
                    val q = quiz.questionInput.text.toString().trim()
                    val a = quiz.optionA.text.toString().trim()
                    val b = quiz.optionB.text.toString().trim()
                    val c = quiz.optionC.text.toString().trim()
                    val d = quiz.optionD.text.toString().trim()

                    if (q.isBlank() || a.isBlank() || b.isBlank() || c.isBlank() || d.isBlank()) {
                        hasEmpty = true
                        quiz.questionInput.setBackgroundColor(Color.parseColor("#FFF0F0"))
                    } else {
                        quiz.questionInput.setBackgroundColor(Color.TRANSPARENT)
                    }
                }

                if (hasEmpty) {
                    Toast.makeText(this@AdminDashboardActivity, "Please fill all questions and options.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = FirebaseFirestore.getInstance()
                val courseDoc = db.collection("courses").document(title)

                val courseData = hashMapOf(
                    "title" to title,
                    "definition" to definition,
                    "description" to desc,
                    "link" to link
                )

                //  Save course first
                courseDoc.set(courseData)

                    .addOnSuccessListener {

                        val quizRef = courseDoc.collection("quiz")


                        quizRef.get()
                            .addOnSuccessListener { snapshot ->
                                for (doc in snapshot.documents) {
                                    doc.reference.delete()
                                }


                                quizList.forEach { quiz ->
                                    val question = quiz.questionInput.text.toString()
                                    val options = mapOf(
                                        "A" to quiz.optionA.text.toString(),
                                        "B" to quiz.optionB.text.toString(),
                                        "C" to quiz.optionC.text.toString(),
                                        "D" to quiz.optionD.text.toString()
                                    )
                                    val correctAnswer = quiz.correctAnswerSpinner.selectedItem.toString()

                                    val quizData = hashMapOf(
                                        "question" to question,
                                        "options" to options,
                                        "correctAnswer" to correctAnswer,
                                        "total_questions" to quizList.size
                                    )

                                    quizRef.add(quizData)
                                }

                                Toast.makeText(
                                    this@AdminDashboardActivity,
                                    "✅ Course & Quiz Saved Successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@AdminDashboardActivity, "❌ Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // --- Layout Composition ---
        layout.addView(TextView(this).apply {
            text = getString(R.string.add_user_title)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        })
        layout.addView(nameInput)
        layout.addView(emailInput)
        layout.addView(phoneInput)
        layout.addView(emergencyContactInput)
        layout.addView(genderInput)
        layout.addView(ageInput)
        layout.addView(conditionsInput)
        layout.addView(allergiesInput)
        layout.addView(bloodTypeInput)
        layout.addView(locationInput)
        layout.addView(passwordInput)
        layout.addView(addUserButton)
        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4
            ).apply { topMargin = 20; bottomMargin = 20 }
            setBackgroundColor("#CCCCCC".toColorInt())
        })

        layout.addView(TextView(this).apply {
            text = getString(R.string.add_course_title)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        })

        layout.addView(courseTitleInput)
        layout.addView(courseDescInput)
        layout.addView(courseDefinitionInput)
        layout.addView(courseLinkInput)

        layout.addView(TextView(this).apply {
            text = "Add Quiz Questions"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        })
        layout.addView(quizContainer)
        layout.addView(addCourseButton)
        return ScrollView(this).apply { addView(layout) }
    }

    // ---------------- Edit user (partial example) ----------------
    private fun createEditView(): ScrollView {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val db = FirebaseFirestore.getInstance()

        // ---------- USER SECTION ----------
        val userTitle = TextView(this).apply {
            text = getString(R.string.edit_user_info)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }

        val emailInput = AutoCompleteTextView(this).apply {
            hint = getString(R.string.hint_user_email_required)
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        db.collection("users").get().addOnSuccessListener { result ->
            val emails = result.documents.mapNotNull { it.getString("email") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emails)
            emailInput.setAdapter(adapter)
        }

        val nameInput = EditText(this).apply { hint = getString(R.string.hint_name) }
        val phoneInput = EditText(this).apply {
            hint = getString(R.string.hint_phone)
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val emergencyContactInput = EditText(this).apply {
            hint = getString(R.string.hint_emergency)
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val genderInput = EditText(this).apply { hint = getString(R.string.hint_gender) }
        val ageInput = EditText(this).apply {
            hint = getString(R.string.hint_age)
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val conditionsInput = EditText(this).apply { hint = getString(R.string.hint_conditions) }
        val allergiesInput = EditText(this).apply { hint = getString(R.string.hint_allergies) }
        val bloodTypeInput = EditText(this).apply { hint = getString(R.string.hint_bloodtype) }
        val locationInput = EditText(this).apply { hint = getString(R.string.hint_location) }
        val passwordInput = EditText(this).apply {
            hint = getString(R.string.hint_new_password)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val updateUserButton = Button(this).apply {
            text = getString(R.string.update_user)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
                val email = emailInput.text.toString().trim()
                if (email.isBlank()) {
                    Toast.makeText(context, getString(R.string.toast_email_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updates = mutableMapOf<String, Any>()
                if (nameInput.text.isNotBlank()) updates["name"] = nameInput.text.toString().trim()
                if (phoneInput.text.isNotBlank()) updates["phone"] = phoneInput.text.toString().trim()
                if (emergencyContactInput.text.isNotBlank()) updates["emergencyContact"] = emergencyContactInput.text.toString().trim()
                if (genderInput.text.isNotBlank()) updates["gender"] = genderInput.text.toString().trim()
                if (ageInput.text.isNotBlank()) updates["age"] = ageInput.text.toString().trim()
                if (conditionsInput.text.isNotBlank()) updates["conditions"] = conditionsInput.text.toString().trim()
                if (allergiesInput.text.isNotBlank()) updates["allergies"] = allergiesInput.text.toString().trim()
                if (bloodTypeInput.text.isNotBlank()) updates["bloodType"] = bloodTypeInput.text.toString().trim()
                if (locationInput.text.isNotBlank()) updates["location"] = locationInput.text.toString().trim()
                if (passwordInput.text.isNotBlank()) updates["password"] = passwordInput.text.toString().trim()

                if (updates.isEmpty()) {
                    Toast.makeText(context, getString(R.string.toast_no_changes), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                dbHelper.updateUserInfo(email, updates) { success ->
                    runOnUiThread {
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            if (success) " User info updated" else " Failed to update user info",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4
            ).apply { topMargin = 30; bottomMargin = 30 }
            setBackgroundColor("#CCCCCC".toColorInt())
        }

        // ---------- COURSE SECTION ----------
        val courseTitle = TextView(this).apply {
            text = getString(R.string.edit_course_info)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }

        val courseNameInput = AutoCompleteTextView(this).apply {
            hint = "Enter or select Course Title"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        db.collection("courses").get().addOnSuccessListener { result ->
            val titles = result.documents.mapNotNull { it.getString("title") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, titles)
            courseNameInput.setAdapter(adapter)
        }

        val courseDescInput = EditText(this).apply {
            hint = "Enter Course Description"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        val courseDefInput = EditText(this).apply {
            hint = "Enter Course Definition"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        val courseLinkInput = EditText(this).apply {
            hint = "Enter Course Link (optional)"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
        }

        // ---------- QUIZ QUESTIONS ----------
        val quizContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 20, 10, 20)
        }

        data class QuizFields(
            val questionId: String,
            val questionInput: EditText,
            val optionA: EditText,
            val optionB: EditText,
            val optionC: EditText,
            val optionD: EditText,
            val correctAnswerSpinner: Spinner
        )

        val quizList = mutableListOf<QuizFields>()

// Listen for course name changes to load quiz data
        courseNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val title = s.toString().trim()
                if (title.isEmpty()) return

                db.collection("courses").whereEqualTo("title", title).get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            val doc = query.documents[0]
                            val courseId = doc.id
                            courseDescInput.setText(doc.getString("description") ?: "")
                            courseDefInput.setText(doc.getString("definition") ?: "")
                            courseLinkInput.setText(doc.getString("link") ?: "")

                            // Clear old quiz fields
                            quizContainer.removeAllViews()
                            quizList.clear()

                            db.collection("courses").document(courseId).collection("quiz").get()
                                .addOnSuccessListener { quizDocs ->
                                    quizDocs.forEachIndexed { i, qDoc ->
                                        val questionInput = EditText(this@AdminDashboardActivity).apply {
                                            hint = "Question ${i + 1}"
                                            setText(qDoc.getString("question") ?: "")
                                            minLines = 2
                                        }

                                        val optionsMap = qDoc.get("options") as? Map<String, String> ?: emptyMap()
                                        val options = listOf(
                                            optionsMap["A"] ?: "",
                                            optionsMap["B"] ?: "",
                                            optionsMap["C"] ?: "",
                                            optionsMap["D"] ?: ""
                                        )
                                        val optionA = EditText(this@AdminDashboardActivity).apply { hint = "Option A"; setText(options.getOrNull(0) ?: "") }
                                        val optionB = EditText(this@AdminDashboardActivity).apply { hint = "Option B"; setText(options.getOrNull(1) ?: "") }
                                        val optionC = EditText(this@AdminDashboardActivity).apply { hint = "Option C"; setText(options.getOrNull(2) ?: "") }
                                        val optionD = EditText(this@AdminDashboardActivity).apply { hint = "Option D"; setText(options.getOrNull(3) ?: "") }

                                        val correctAnswerSpinner = Spinner(this@AdminDashboardActivity).apply {
                                            adapter = ArrayAdapter(
                                                this@AdminDashboardActivity,
                                                android.R.layout.simple_spinner_dropdown_item,
                                                listOf("A", "B", "C", "D")
                                            )
                                            val correct = qDoc.getString("correctAnswer") ?: "A"
                                            setSelection(listOf("A", "B", "C", "D").indexOf(correct))
                                        }

                                        val questionLayout = LinearLayout(this@AdminDashboardActivity).apply {
                                            orientation = LinearLayout.VERTICAL
                                            setPadding(0, 20, 0, 20)
                                            addView(TextView(this@AdminDashboardActivity).apply {
                                                text = "Question ${i + 1}"
                                                textSize = 18f
                                                setTypeface(null, Typeface.BOLD)
                                                setTextColor(Color.parseColor("#7A1505"))
                                            })
                                            addView(questionInput)
                                            addView(optionA)
                                            addView(optionB)
                                            addView(optionC)
                                            addView(optionD)
                                            addView(TextView(this@AdminDashboardActivity).apply {
                                                text = "Select Correct Answer:"
                                                setTextColor(Color.parseColor("#7A1505"))
                                                setTypeface(null, Typeface.BOLD)
                                            })
                                            addView(correctAnswerSpinner)
                                        }

                                        quizContainer.addView(questionLayout)
                                        quizList.add(
                                            QuizFields(
                                                qDoc.id, questionInput, optionA, optionB, optionC, optionD, correctAnswerSpinner
                                            )
                                        )
                                    }
                                }
                        } else {
                            // Clear quiz UI if course not found
                            quizContainer.removeAllViews()
                            quizList.clear()
                        }
                    }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        val updateCourseButton = Button(this).apply {
            text = "Update Course & Quiz"
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)

            setOnClickListener {
                val title = courseNameInput.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(context, "Please select a course", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                db.collection("courses").whereEqualTo("title", title).get().addOnSuccessListener { query ->
                    if (!query.isEmpty) {
                        val docId = query.documents[0].id
                        val courseRef = db.collection("courses").document(docId)

                        val updates = mutableMapOf<String, Any>()
                        if (courseDescInput.text.isNotBlank()) updates["description"] = courseDescInput.text.toString()
                        if (courseDefInput.text.isNotBlank()) updates["definition"] = courseDefInput.text.toString()
                        if (courseLinkInput.text.isNotBlank()) updates["link"] = courseLinkInput.text.toString()

                        courseRef.update(updates)
                            .addOnSuccessListener {
                                // Update quizzes
                                val quizRef = courseRef.collection("quiz")
                                quizList.forEach { quiz ->
                                    val qData = hashMapOf(
                                        "question" to quiz.questionInput.text.toString(),
                                        "options" to listOf(
                                            quiz.optionA.text.toString(),
                                            quiz.optionB.text.toString(),
                                            quiz.optionC.text.toString(),
                                            quiz.optionD.text.toString()
                                        ),
                                        "correctAnswer" to quiz.correctAnswerSpinner.selectedItem.toString()
                                    )
                                    quizRef.document(quiz.questionId).set(qData)
                                }

                                Toast.makeText(context, " Course & Quiz Updated Successfully!", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, " Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Course not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        layout.apply {
            addView(userTitle)
            addView(emailInput)
            addView(nameInput)
            addView(phoneInput)
            addView(emergencyContactInput)
            addView(genderInput)
            addView(ageInput)
            addView(conditionsInput)
            addView(allergiesInput)
            addView(bloodTypeInput)
            addView(locationInput)
            addView(passwordInput)
            addView(updateUserButton)
            addView(divider)
            addView(courseTitle)
            addView(courseNameInput)
            addView(courseDescInput)
            addView(courseDefInput)
            addView(courseLinkInput)
            addView(quizContainer)
            addView(updateCourseButton)
        }

        return ScrollView(this).apply { addView(layout) }
    }




    // ---------------- Delete user ----------------
    private fun createDeleteView(): ScrollView {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val db = FirebaseFirestore.getInstance()

        //  USER DELETE SECTION
        val userTitle = TextView(this).apply {
            text = "Delete User"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }

        //  AUTOCOMPLETE FOR USER EMAIL
        val emailInput = AutoCompleteTextView(this).apply {
            hint = "Enter or select user email"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        // Fetch user emails from Firestore
        db.collection("users").get().addOnSuccessListener { result ->
            val emails = result.documents.mapNotNull { it.getString("email") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emails)
            emailInput.setAdapter(adapter)
        }

        val deleteUserButton = Button(this).apply {
            text = getString(R.string.delete_user)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
                val email = emailInput.text.toString().trim()
                if (email.isBlank()) {
                    Toast.makeText(context, "Please enter or select an email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                dbHelper.deleteUserByEmail(email) { success ->
                    runOnUiThread {
                        Toast.makeText(
                            this@AdminDashboardActivity,
                            if (success) " User deleted successfully" else " User not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        emailInput.text.clear()
                    }
                }
            }
        }

        // 🔸 Divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4
            ).apply { topMargin = 30; bottomMargin = 30 }
            setBackgroundColor("#CCCCCC".toColorInt())
        }

        // 🔹 COURSE DELETE SECTION
        val courseTitle = TextView(this).apply {
            text = "Delete Course"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
        }

        // 🔹 AUTOCOMPLETE FOR COURSE TITLE
        val courseNameInput = AutoCompleteTextView(this).apply {
            hint = "Enter or select course title"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        // Fetch course titles from Firestore
        db.collection("courses").get().addOnSuccessListener { result ->
            val courseTitles = result.documents.mapNotNull { it.getString("title") }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courseTitles)
            courseNameInput.setAdapter(adapter)
        }

        val deleteCourseButton = Button(this).apply {
            text = "Delete Course"
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
                val title = courseNameInput.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(context, "Please enter or select a course title", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                db.collection("courses")
                    .whereEqualTo("title", title)
                    .get()
                    .addOnSuccessListener { query ->
                        if (!query.isEmpty) {
                            for (doc in query.documents) {
                                db.collection("courses").document(doc.id).delete()
                            }
                            Toast.makeText(context, " Course deleted successfully", Toast.LENGTH_SHORT).show()
                            courseNameInput.text.clear()
                        } else {
                            Toast.makeText(context, " No course found with that title", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, " Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 🔹 COMPOSE LAYOUT
        layout.apply {
            addView(userTitle)
            addView(emailInput)
            addView(deleteUserButton)
            addView(divider)
            addView(courseTitle)
            addView(courseNameInput)
            addView(deleteCourseButton)
        }

        return ScrollView(this).apply { addView(layout) }
    }

    // ---------------- Profile ----------------
    private fun createProfileView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 100, 50, 100)
        }
        val profileView = TextView(this).apply {
            text = getString(R.string.admin_profile)
            gravity = Gravity.CENTER
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#000000".toColorInt())
        }
        val logoutButton = Button(this).apply {
            text = getString(R.string.logout_button)
            setBackgroundColor("#B71C1C".toColorInt())
            setTextColor(Color.WHITE)
            setOnClickListener {
                AlertDialog.Builder(this@AdminDashboardActivity)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes") { _, _ ->
                        val prefs = getSharedPreferences("admin", MODE_PRIVATE)
                        prefs.edit { clear() }
                        startActivity(Intent(this@AdminDashboardActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
        layout.addView(profileView)
        layout.addView(logoutButton)
        return layout
    }

}
