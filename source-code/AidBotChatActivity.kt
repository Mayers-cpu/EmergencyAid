package com.example.emergencyaid

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import android.text.InputType
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import android.view.WindowManager

class AidBotChatActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesCollection: com.google.firebase.firestore.CollectionReference
    private lateinit var currentUserEmail: String
    private lateinit var voiceInputLauncher: ActivityResultLauncher<Intent>
    private lateinit var navButtons: MutableList<Button>
    private lateinit var chatLayout: LinearLayout
    private var chatListener: ListenerRegistration? = null
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        // Firestore init
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        messagesCollection = firestore.collection("messages")

        currentUserEmail = getSharedPreferences("user", MODE_PRIVATE)
            .getString("email", "unknown@example.com") ?: "unknown@example.com"

        // === Voice Input Setup ===
        voiceInputLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val spokenText = result.data
                        ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                        ?.get(0)

                    if (spokenText != null) {
                        saveUserMessageToFirestore(spokenText)
                    } else {
                        Toast.makeText(this, "No speech recognized", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        // === ROOT LAYOUT ===
        val rootLayout = ConstraintLayout(this).apply {
            id = View.generateViewId()
            setBackgroundColor("#FFF5F5F5".toColorInt())
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
        }

        // === TITLE ELEMENTS ===
        val title = TextView(this).apply {
            text = getString(R.string.chat_title)
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.chat_subtitle)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor("#7A1505".toColorInt())
        }

        val desc = TextView(this).apply {
            text = getString(R.string.chat_description)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(30, 20, 30, 80)
            setTextColor("#7A1505".toColorInt())
        }

        // === SCROLLVIEW (chat history) ===
        scrollView = ScrollView(this).apply {
            id = View.generateViewId()
            // width MATCH_PARENT, height 0 so ConstraintLayout can resize it (adjustResize)
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                0
            )
        }

        val contentWrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 20)
        }

        chatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // add title and chat container
        contentWrapper.apply {
            addView(title)
            addView(subtitle)
            addView(desc)
            addView(chatLayout)
        }
        scrollView.addView(contentWrapper)

        // === MESSAGE INPUT (inside a horizontal LinearLayout inputBar) ===
        val messageInput = EditText(this).apply {
            hint = getString(R.string.chat_hint)
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_CLASS_TEXT
            maxLines = 5
            background = null
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            // this layoutParams used inside the horizontal inputBar LinearLayout
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val voiceButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            background = null
            setOnClickListener {
                val voiceIntent =
                    Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now")
                    }
                try {
                    voiceInputLauncher.launch(voiceIntent)
                } catch (e: Exception) {
                    Toast.makeText(this@AidBotChatActivity, "Voice input not supported", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val sendButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            background = null
            setOnClickListener {
                if (!isNetworkAvailable(this@AidBotChatActivity)) {
                    Toast.makeText(this@AidBotChatActivity, "No Internet connection", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val userMessage = messageInput.text.toString().trim()
                if (userMessage.isNotEmpty()) {
                    saveUserMessageToFirestore(userMessage)
                    messageInput.text.clear()
                }
            }
        }

        val inputBar = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 10, 20, 10)
            background = ResourcesCompat.getDrawable(resources, R.drawable.rounded_background, null)
            // ConstraintLayout params so we can anchor inputBar
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            addView(messageInput)
            addView(voiceButton)
            addView(sendButton)
        }

        // === BOTTOM NAV ===
        val bottomNav = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor("#7A1505".toColorInt())
            gravity = Gravity.CENTER
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // === Add nav buttons (same logic you had) ===
        val tabs = listOf("HOME", "Courses", "Chat", "Record", "Profile")
        navButtons = mutableListOf()
        tabs.forEach { label ->
            val button = Button(this).apply {
                text = label
                setTypeface(null, Typeface.BOLD)
                val selectedColor = "#FFFFFF".toColorInt()
                val unselectedColor = "#7A1505".toColorInt()
                val selectedTextColor = "#7A1505".toColorInt()
                val unselectedTextColor = "#FFFFFF".toColorInt()

                setTextColor(if (label == "Chat") selectedTextColor else unselectedTextColor)
                setBackgroundColor(if (label == "Chat") selectedColor else unselectedColor)

                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    when (label) {
                        "HOME" -> startActivity(Intent(this@AidBotChatActivity, UserDashboardActivity::class.java))
                        "Courses" -> startActivity(Intent(this@AidBotChatActivity, CoursesActivity::class.java))
                        "Chat" -> { /* already here */ }
                        "Record" -> startActivity(Intent(this@AidBotChatActivity, RecordActivity::class.java))
                        "Profile" -> startActivity(Intent(this@AidBotChatActivity, ProfileActivity::class.java))
                    }
                }
            }
            navButtons.add(button)
            bottomNav.addView(button)
        }

        // === Add views to root and set constraints ===
        rootLayout.addView(scrollView)
        rootLayout.addView(inputBar)
        rootLayout.addView(bottomNav)

        // ScrollView constraints: top -> parent top, bottom -> inputBar top
        (scrollView.layoutParams as ConstraintLayout.LayoutParams).apply {
            topToTop = rootLayout.id
            bottomToTop = inputBar.id
            startToStart = rootLayout.id
            endToEnd = rootLayout.id
            height = 0 // important for adjustResize to resize this view
            width = ConstraintLayout.LayoutParams.MATCH_PARENT
        }

        // inputBar constraints: bottom -> top of bottomNav
        (inputBar.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomToTop = bottomNav.id
            startToStart = rootLayout.id
            endToEnd = rootLayout.id
        }

        // bottomNav constraints: anchored to parent bottom
        (bottomNav.layoutParams as ConstraintLayout.LayoutParams).apply {
            bottomToBottom = rootLayout.id
            startToStart = rootLayout.id
            endToEnd = rootLayout.id
        }

        setContentView(rootLayout)

        // If activity started with an emergency message, post it and ask bot
        val emergencyMessage = intent.getStringExtra("EMERGENCY_MESSAGE")
        if (!emergencyMessage.isNullOrEmpty()) {
            val timestamp = com.google.firebase.Timestamp.now()
            val baseData = hashMapOf(
                "userEmail" to currentUserEmail,
                "userMessage" to emergencyMessage,
                "timestamp" to timestamp,
                "source" to "emergency"
            )

            val messagesRef = firestore.collection("messages").document()
            val chatHistoryRef = firestore.collection("chatHistory").document()

            messagesRef.set(baseData)
            chatHistoryRef.set(baseData)

            addMessageBubble("You: $emergencyMessage", true)
            // Save the emergency message as a Record
            saveMessageToRecords(title = "Chat", action = emergencyMessage)

            // Ask AidBot for reply
            OpenAiService.sendMessage(emergencyMessage) { reply ->
                runOnUiThread {
                    if (reply != null) {
                        addMessageBubble("AidBot: $reply", false)

                        val botUpdate = mapOf(
                            "botReply" to reply,
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )

                        messagesRef.update(botUpdate)
                        chatHistoryRef.update(botUpdate)

                        Log.d("Firestore", "✅ Bot reply saved for emergency message.")
                    } else {
                        Log.e("Firestore", "⚠️ No bot reply received.")
                    }
                }
            }
        }
    }

    private fun saveUserMessageToFirestore(message: String) {
        val docRef = messagesCollection.document()
        val data = hashMapOf(
            "userEmail" to currentUserEmail,
            "userMessage" to message,
            "botReply" to null,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        docRef.set(data).addOnSuccessListener {

            saveMessageToRecords(title = "Chat", action = message)

            // === Call OpenAI for bot reply ===
            OpenAiService.sendMessage(message) { reply ->
                runOnUiThread {

                    val finalReply = reply ?: "AidBot: Couldn't generate a response."

                    // Save bot reply to Firestore
                    docRef.update("botReply", finalReply)
                        .addOnSuccessListener {
                            Log.d("Firestore", "✅ Bot reply saved")
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "❌ Failed to save bot reply", it)
                        }

                    // Display bubble
                    addMessageBubble("AidBot: $finalReply", false)
                }
            }
        }
    }

    private fun saveMessageToRecords(title: String, action: String) {
        val firestore = FirebaseFirestore.getInstance()
        val recordsRef = firestore.collection("records")

        val recordData = hashMapOf(
            "email" to currentUserEmail,
            "title" to title,
            "action" to action,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        recordsRef.add(recordData)
            .addOnSuccessListener {
                Log.d("RecordSync", "✅ New record added for $title")
            }
            .addOnFailureListener { e ->
                Log.e("RecordSync", "❌ Failed to add record", e)
            }
    }

    // Firestore listener for chat history
    private fun startListeningToChatHistory() {
        chatListener?.remove()
        chatListener = messagesCollection
            .whereEqualTo("userEmail", currentUserEmail)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error loading chat history", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    chatLayout.removeAllViews()
                    for (doc in snapshot.documents) {
                        val userMessage = doc.getString("userMessage") ?: ""
                        val botReply = doc.getString("botReply") ?: ""
                        if (userMessage.isNotEmpty()) addMessageBubble("You: $userMessage", true)
                        if (botReply.isNotEmpty()) addMessageBubble("AidBot: $botReply", false)
                    }
                }
            }
    }

    private fun addMessageBubble(message: String, isUser: Boolean) {
        val textView = TextView(this).apply {
            text = message
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(20, 20, 20, 20)
            background = if (isUser)
                ResourcesCompat.getDrawable(resources, R.drawable.user_bubble, null)
            else
                ResourcesCompat.getDrawable(resources, R.drawable.bot_bubble, null)
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (isUser) Gravity.END else Gravity.START
            topMargin = 12
            marginEnd = 20
            marginStart = 20
        }

        chatLayout.addView(textView, layoutParams)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onResume() {
        super.onResume()
        startListeningToChatHistory()
    }

    override fun onPause() {
        super.onPause()
        chatListener?.remove()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}
