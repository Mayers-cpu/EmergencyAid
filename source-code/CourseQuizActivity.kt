package com.example.emergencyaid

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.Typeface
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore



class CourseQuizActivity : Activity() {

    private lateinit var questions: List<Pair<String, String>>
    private lateinit var options: Map<Int, List<String>>
    private val userAnswers = mutableListOf<String>()

    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var layout: LinearLayout
    private lateinit var questionText: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var questionCard: LinearLayout
    private lateinit var courseTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#FFFFFF".toColorInt())
            setPadding(30, 30, 30, 30)
        }

        courseTitle = intent.getStringExtra("course_title") ?: "Quiz"

        val quizTitle = TextView(this).apply {
            text = getString(R.string.course_quiz_title, courseTitle)
            textSize = 22f
            setTextColor("#B00020".toColorInt())
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 30)
        }

        questionText = TextView(this).apply {
            textSize = 18f
            setTextColor("#000000".toColorInt())
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 40, 0, 20)
        }

        radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        questionCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
            background = ResourcesCompat.getDrawable(resources, R.drawable.question_card_background, null)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 20)
            }
        }

        questionCard.addView(questionText)
        questionCard.addView(radioGroup)

        submitButton = Button(this).apply {
            text = getString(R.string.submit)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor("#FFFFFF".toColorInt())
            setOnClickListener { handleAnswer() }
        }

        layout.addView(quizTitle)
        layout.addView(questionCard)
        layout.addView(submitButton)

        setContentView(layout)

        loadQuizFromFirestore(courseTitle)
    }

    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
            val (question, _) = questions[currentQuestionIndex]
            questionText.text = getString(
                R.string.question_format,
                currentQuestionIndex + 1,
                questions.size,
                question
            )
            radioGroup.removeAllViews()

            options[currentQuestionIndex]?.forEach { option ->
                val radioButton = RadioButton(this).apply {
                    text = option
                    textSize = 16f
                    setTextColor("#333333".toColorInt())
                    setPadding(10, 20, 10, 20)
                }
                radioGroup.addView(radioButton)
            }
        } else {
            showScore()

        }
    }


    private fun handleAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId

        if (selectedId != -1) {
            val selectedButton = findViewById<RadioButton>(selectedId)
            val selectedText = selectedButton?.text.toString()


            val currentOptions = options[currentQuestionIndex] ?: emptyList()
            val selectedOptionKey = when (selectedText) {
                currentOptions.getOrNull(0) -> "A"
                currentOptions.getOrNull(1) -> "B"
                currentOptions.getOrNull(2) -> "C"
                currentOptions.getOrNull(3) -> "D"
                else -> ""
            }

            val correctAnswer = questions[currentQuestionIndex].second

            userAnswers.add(selectedText)

            if (selectedOptionKey == correctAnswer) {
                score++
            }

            currentQuestionIndex++
            loadQuestion()
        } else {
            showNoAnswerDialog()
        }
    }

    private fun loadQuizFromFirestore(courseTitle: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("courses")
            .document(courseTitle)
            .collection("quiz")
            .get()
            .addOnSuccessListener { result ->
                val questionList = mutableListOf<Pair<String, String>>()
                val optionMap = mutableMapOf<Int, List<String>>()

                for ((index, document) in result.documents.withIndex()) {
                    val question = document.getString("question") ?: continue
                    val correctAnswer = document.getString("correctAnswer") ?: ""


                    val optionsMap = document.get("options") as? Map<String, String> ?: emptyMap()


                    val optionsList = listOf(
                        optionsMap["A"] ?: "",
                        optionsMap["B"] ?: "",
                        optionsMap["C"] ?: "",
                        optionsMap["D"] ?: ""
                    )

                    questionList.add(Pair(question, correctAnswer))
                    optionMap[index] = optionsList
                }

                if (questionList.isNotEmpty()) {
                    questions = questionList
                    options = optionMap
                    currentQuestionIndex = 0
                    loadQuestion()
                } else {
                    Toast.makeText(this, "No quiz found for this course.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load quiz: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }


    private fun saveQuizResultToFirestore(courseTitle: String, correctAnswers: Int, totalQuestions: Int) {
        val email = getSharedPreferences("user", MODE_PRIVATE).getString("email", "") ?: ""
        val db = FirebaseFirestore.getInstance()

        val formattedScore = "$correctAnswers/$totalQuestions"

        val record = hashMapOf(
            "email" to email,
            "title" to courseTitle,
            "action" to "Completed",
            "score" to formattedScore,
            "total_questions" to totalQuestions,
            "timestamp" to System.currentTimeMillis(),
            "status" to "Completed"
        )


        db.collection("user_activities")
            .document(email)
            .collection("quiz")
            .add(record)
            .addOnSuccessListener {
                Log.d("QuizResult", "📌 Quiz result added under user_activities.")
            }


        db.collection("records")
            .add(record)
            .addOnSuccessListener {
                Log.d("QuizResult", "✅ Quiz result added to records.")
            }
            .addOnFailureListener { e ->
                Log.e("QuizResult", "❌ Failed to add quiz result: ${e.message}")
            }
    }


    private fun showNoAnswerDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("⚠️ Action Required")
            .setMessage("Please select an answer before submitting.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.WHITE)
            dialog.window?.setBackgroundDrawable("#D32F2F".toColorInt().toDrawable())
        }

        dialog.show()
    }

    private fun showScore() {
        layout.removeAllViews()

        val scoreText = TextView(this).apply {
            text = getString(R.string.your_score, score, questions.size)
            textSize = 24f
            setTextColor("#7A1505".toColorInt())
            setPadding(0, 0, 0, 20)
        }

        layout.addView(scoreText)

        // Show answers
        for (i in questions.indices) {
            val (question, correctAnswer) = questions[i]
            val userAnswer = userAnswers.getOrNull(i) ?: "No answer"

            val answerSummary = TextView(this).apply {
                text = getString(
                    R.string.quiz_result_summary,
                    i + 1,
                    question,
                    userAnswer,
                    correctAnswer
                )
                textSize = 16f
                setPadding(0, 10, 0, 20)
                setTextColor(
                    if (userAnswer == correctAnswer) "#2E7D32".toColorInt()
                    else "#D32F2F".toColorInt()
                )
            }

            layout.addView(answerSummary)
        }

        // ⭐ Save quiz record to Firestore with correct score formatting
        val courseTitle = intent.getStringExtra("course_title") ?: "Unknown Course"
        saveQuizResultToFirestore(courseTitle, score, questions.size)

        // Show feedback dialog
        val scoreResult = "$score/${questions.size}"
        val performanceFeedback = when {
            score == questions.size -> "Excellent! You got a perfect score!"
            score >= questions.size * 0.7 -> "Great job! You have strong first aid knowledge."
            score >= questions.size * 0.4 -> "Good effort! Review some areas for improvement."
            else -> "Keep practicing! Don’t give up — learn from the course again."
        }

        AlertDialog.Builder(this)
            .setTitle("Quiz Completed 🎉")
            .setMessage("Your score: $scoreResult\n\nFeedback: $performanceFeedback")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()

        val finishButton = Button(this).apply {
            id = R.id.finishButton
            text = getString(R.string.finish)
            setBackgroundColor("#7A1505".toColorInt())
            setTextColor("#FFFFFF".toColorInt())
            setOnClickListener {
                Toast.makeText(
                    this@CourseQuizActivity,
                    "Your score: $scoreResult has been recorded!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        layout.addView(finishButton)
    }
}
