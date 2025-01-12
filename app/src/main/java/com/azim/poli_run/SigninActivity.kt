package com.azim.poli_run

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azim.poli_run.databinding.ActivityMainBinding
import com.azim.poli_run.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SigninActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        registerClick()

        binding.loginButton.setOnClickListener {
            val email = binding.logEmail.text.toString()
            val password = binding.logPass.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                    "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, navigate to next screen
                    val user: FirebaseUser? = auth.currentUser
                    Toast.makeText(baseContext, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()

                    // Example: Navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign-in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun registerClick() {
        // Assuming you have ViewBinding set up
        val textView: TextView = binding.signInRegText

        // Define the full text
        val fullText = "If you did not have an account, please register"

        // Create a SpannableString from the full text
        val spannableString = SpannableString(fullText)

        // Find the "register" text position
        val start = fullText.indexOf("register")
        val end = start + "register".length

        // Create a ClickableSpan for the "register" part
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle the click event, e.g., navigate to a registration screen
                widget.context.startActivity(Intent(widget.context, SignupActivity::class.java))
                Toast.makeText(widget.context, "Register clicked!", Toast.LENGTH_SHORT).show()
            }
        }

        // Apply the ClickableSpan to the "register" portion
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the TextView
        textView.text = spannableString

        // Make the text clickable
        textView.movementMethod = LinkMovementMethod.getInstance()

    }
}