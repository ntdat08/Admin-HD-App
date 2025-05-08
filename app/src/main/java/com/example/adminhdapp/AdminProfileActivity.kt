package com.example.adminhdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.view.View
import android.widget.Toast
import com.example.adminhdapp.databinding.ActivityAdminProfileBinding
import com.example.adminhdapp.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.reference.child("Users")

        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.profileName.isEnabled = false
        binding.profileAddress.isEnabled = false
        binding.profileEmail.isEnabled = false
        binding.profileNumber.isEnabled = false
        binding.profilePassword.isEnabled = false

        var isEnabled = false

        binding.saveBtn.visibility = View.INVISIBLE

        binding.editBtn.setOnClickListener {
            isEnabled = !isEnabled
            binding.profileName.isEnabled = isEnabled
            binding.profileAddress.isEnabled = isEnabled
            binding.profileEmail.isEnabled = isEnabled
            binding.profileNumber.isEnabled = isEnabled
            binding.profilePassword.isEnabled = isEnabled
            binding.saveBtn.visibility = View.VISIBLE

            if (isEnabled) {
                binding.profileName.requestFocus()
            }
        }

        retrieveUserData()

        binding.saveBtn.setOnClickListener {
            updateUserData()
        }
    }


    private fun retrieveUserData() {

        val currentUser = auth.currentUser?.uid
        if (currentUser != null) {
            val userReference = adminReference.child(currentUser)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val ownerNames = snapshot.child("name").getValue()
                        val addresses = snapshot.child("restaurantName").getValue()
                        val emails = snapshot.child("email").getValue()
                        val numbers = snapshot.child("number").getValue()
                        val passwords = snapshot.child("password").getValue()

                        setDataToTextView(ownerNames, addresses, emails, numbers, passwords)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

    }

    private fun setDataToTextView(
        ownerNames: Any?,
        addresses: Any?,
        emails: Any?,
        numbers: Any?,
        passwords: Any?
    ) {
        binding.profileName.setText(ownerNames.toString())
        binding.profileAddress.setText(addresses.toString())
        binding.profileEmail.setText(emails.toString())
        binding.profileNumber.setText(numbers.toString())
        binding.profilePassword.setText(passwords.toString())
    }

    private fun updateUserData() {

        val updateName = binding.profileName.text.toString()
        val updateAddress = binding.profileAddress.text.toString()
        val updateEmail = binding.profileEmail.text.toString()
        val updateNumber = binding.profileNumber.text.toString()
        val updatePassword = binding.profilePassword.text.toString()
        val  currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null){

            val userRf = adminReference.child(currentUserUid)

            userRf.child("name").setValue(updateName)
            userRf.child("password").setValue(updatePassword)
            userRf.child("email").setValue(updateEmail)
            userRf.child("restaurantName").setValue(updateAddress)
            userRf.child("number").setValue(updateNumber)

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            auth.currentUser?.updateEmail(updateEmail)
            auth.currentUser?.updatePassword(updatePassword)

        } else {
            Toast.makeText(this, "Profile updated failed", Toast.LENGTH_SHORT).show()

        }


    }
}