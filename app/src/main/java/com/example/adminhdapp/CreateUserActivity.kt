package com.example.adminhdapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminhdapp.databinding.ActivityCreateUserBinding
import com.example.adminhdapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = Firebase.auth
        database = Firebase.database.reference

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.createUserBtn.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(name, email, password)
            }
        }
    }

    private fun createAccount(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                saveUserData(name, email, password)
                finish()
            } else {
                Toast.makeText(this, "Account creation failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateUser", "createAccount: Failure", task.exception)
            }
        }
    }

    private fun saveUserData(name: String, email: String, password: String) {
        val userId = auth.currentUser?.uid ?: return

        val user = UserModel(
            name = name,
            restaurantName = null,
            email = email,
            number = null,
            address = null,
            password = password
        )

        database.child("Users").child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d("CreateUser", "User data saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("CreateUser", "Failed to save user data: ${e.message}")
            }
    }
}
