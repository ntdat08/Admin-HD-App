package com.example.adminhdapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.adminhdapp.databinding.ActivityLoginBinding
import com.example.adminhdapp.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {
    private var name: String? = null
    private var restaurantName: String? = null
    private lateinit var password: String
    private lateinit var email: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var binding : ActivityLoginBinding

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        auth = Firebase.auth

        database = Firebase.database.reference

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)
        binding.loginBtn.setOnClickListener {
            // get text from edit text
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (email.isBlank()||password.isBlank()){
                Toast.makeText(this, "Please fill the all filled", Toast.LENGTH_SHORT).show()
            } else{
                userLogin(email,password)
            }
        }

        binding.googleBtn1.setOnClickListener {

            val signInIntent= googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        binding.dont.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }
    }

    private fun userLogin(email : String, password : String) {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Account login successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            } else{
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {task->
                    if (task.isSuccessful){
                        saveUserData()
                        Toast.makeText(this, "Create user & login successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this, "Account Authentication failed : ${task.exception} ", Toast.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }

    // Save data in to database
    private fun saveUserData() {
        // Get text from edittext
        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(name,restaurantName,email,password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        // save user data firebase database
        userId.let {
            database.child("Users").child(it).setValue(user)


        }

    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful){
                val account : GoogleSignInAccount = task.result
                val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken,null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful){
                        // Successfully Sign In With Google
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                        Toast.makeText(this, "Successfully sign-in with Google ", Toast.LENGTH_SHORT).show()
                    } else{
                        Toast.makeText(this, "Google sign-in failed : ${authTask.exception}", Toast.LENGTH_SHORT).show()
                    }

                }
            }else{
                Toast.makeText(this, "Google sign-in failed : ${task.exception}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}