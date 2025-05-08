package com.example.adminhdapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.adminhdapp.databinding.ActivityAddItemBinding
import com.example.adminhdapp.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import java.io.ByteArrayOutputStream

class AddItemActivity : AppCompatActivity() {

    // Food item details
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private var foodImageUri: Uri? = null
    private lateinit var foodIngredient: String

    private lateinit var autn: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var binding: ActivityAddItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.image.visibility = View.GONE

        // Initialize Firebase
        autn = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.addItemBtn.setOnClickListener {
            // Get all text from edittext
            foodName = binding.enterFoodName.text.toString().trim()
            foodPrice = binding.enterFoodPrice.text.toString().trim()
            foodDescription = binding.description.text.toString().trim()
            foodIngredient = binding.ingredients.text.toString().trim()

            if (foodName.isBlank() || foodPrice.isBlank() || foodDescription.isBlank() || foodIngredient.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                dataUpload()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        binding.addImage.setOnClickListener {
            picImage.launch("image/*")
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun dataUpload() {
        val menuRef = database.getReference("Menu")
        val newItemKey = menuRef.push().key

        if (foodImageUri != null) {
            val imageBase64 = uriToBase64(foodImageUri!!)
            if (imageBase64 == null) {
                Toast.makeText(this, "Image conversion failed", Toast.LENGTH_SHORT).show()
                return
            }

            val menuItem = AllMenu(
                newItemKey,
                foodName = foodName,
                foodPrice = foodPrice,
                foodDescription = foodDescription,
                foodIngredient = foodIngredient,
                foodImage = imageBase64
            )

            newItemKey?.let { key ->
                menuRef.child(key).setValue(menuItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
        }
    }

    // Convert image URI to Base64 string
    private fun uriToBase64(uri: Uri): String? {
        return try {
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Pick image from gallery
    private val picImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            binding.image.visibility = View.VISIBLE
            binding.image.setImageURI(it)
            foodImageUri = it
        }
    }
}
