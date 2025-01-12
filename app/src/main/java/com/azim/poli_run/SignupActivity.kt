package com.azim.poli_run

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.azim.poli_run.databinding.ActivitySigninBinding
import com.azim.poli_run.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.regButton.setOnClickListener {
            registerUser()

        }
    }

    private fun registerUser() {
        val name = binding.regName.text.toString()
        val jabatan = binding.regJabatan.text.toString()
        val email = binding.regEmail.text.toString()
        val password = binding.regPass.text.toString()

        if (name.isEmpty() || jabatan.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Save user information in Firestore
                        saveUserToFirestore(user, name, jabatan)
                    }
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to another activity (e.g., login)
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, name: String, jabatan: String) {
        // Create a user object to store in Firestore
        val userMap = hashMapOf(
            "name" to name,
            "email" to user.email,
            "jabatan" to jabatan
        )

        // Save the user data under the "users" collection
        db.collection("users")
            .document(user.uid)  // Store data with the user's UID as the document ID
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration and Firestore Update Successful", Toast.LENGTH_SHORT).show()
                // Optionally navigate to another screen (e.g., login or home screen)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user to Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up ViewBinding reference
    }
}