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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.adminhdapp.databinding.ActivityAddItemBinding
import com.example.adminhdapp.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddItemActivity : AppCompatActivity() {

    // Food item details
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private var foodImageUri: Uri? = null
    private lateinit var imageItemUrl : String

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
    private fun uploadImageAndSaveData(callback: (String?) -> Unit) {
        foodImageUri?.let { uri ->
            try {
                val file = createTempFileFromUri(uri)
                binding.addItemBtn.isEnabled = false

                MediaManager.get().upload(file.absolutePath)
                    .option("folder", "menu_images")
                    .option("resource_type", "auto")
                    .option("secure", true)
                    .callback(object : UploadCallback {
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as? String

                            file.delete()
                            runOnUiThread {
                                binding.addItemBtn.isEnabled = true
                                if (imageUrl != null) {
                                    Toast.makeText(this@AddItemActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                                }
                            }
                            callback(imageUrl)
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            file.delete()
                            runOnUiThread {
                                binding.addItemBtn.isEnabled = true
                                Toast.makeText(this@AddItemActivity, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                            }
                            callback(null)
                        }

                        override fun onStart(requestId: String) {}
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            callback(null)
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                binding.addItemBtn.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } ?: run {
            callback(null)
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        return File(cacheDir, "temp_${System.currentTimeMillis()}.jpg").apply {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(this).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw Exception("Cannot open input stream from URI")
        }
    }

    private fun dataUpload() {
        if (foodImageUri != null) {
            uploadImageAndSaveData { imageUrl ->
                if (imageUrl != null) {
                    saveItemToDatabase(imageUrl)
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveItemToDatabase(imageUrl: String) {
        val menuRef = database.getReference("AllMenu")
        val newItemKey = menuRef.push().key ?: return

        val menuItem = AllMenu(
            key = newItemKey,
            foodName = foodName,
            foodPrice = foodPrice,
            foodDescription = foodDescription,
            foodIngredient = foodIngredient,
            foodImage = imageUrl
        )

        menuRef.child(newItemKey).setValue(menuItem)
            .addOnSuccessListener {
                runOnUiThread {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }



    // Convert image URI to Base64 string
    private fun uriToBase64(uri: Uri): String? {
        imageItemUrl = ""
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