package com.example.adminhdapp

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.adminhdapp.databinding.ActivitySignUpBinding
import com.example.adminhdapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.log

class SignUpActivity : AppCompatActivity() {
    private lateinit var name: String
    private lateinit var restaurantName: String
    private lateinit var password: String
    private lateinit var email: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase auth
        auth = Firebase.auth
        // Initialize Firebase Database
        database = Firebase.database.reference

        val locationList = arrayOf("Pakistan", "India", "USA", "Nepal")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, locationList)
        val autoCompleteTextView = binding.listOfLocation
        autoCompleteTextView.setAdapter(adapter)

        binding.already.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.signupBtn.setOnClickListener {

            // Get text from edittext
            name = binding.name.text.toString().trim()
            restaurantName = binding.address.text.toString().trim()
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (name.isBlank() || restaurantName.isBlank() || email.isBlank() ||password.isBlank()){
                Toast.makeText(this, "Please fill the all filled", Toast.LENGTH_SHORT).show()
            }else{

                createAccount(email,password)
            }


        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                saveUserData()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                Toast.makeText(this, "Account creation failed : ${task.exception}", Toast.LENGTH_SHORT).show()
                Log.d("Account","createAccount : Failure",task.exception)
            }
        }
    }
    // Save data in to database
    private fun saveUserData() {
        // Get text from edittext
        name = binding.name.text.toString().trim()
        restaurantName = binding.address.text.toString().trim()
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(name,restaurantName,email,password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        // save user data firebase database
        database.child("Users").child(userId).setValue(user)
    }

}