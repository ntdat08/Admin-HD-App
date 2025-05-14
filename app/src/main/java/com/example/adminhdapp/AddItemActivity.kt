package com.example.adminhdapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.adminhdapp.databinding.ActivityAddItemBinding
import com.example.adminhdapp.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddItemActivity : AppCompatActivity() {

    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private lateinit var foodIngredient: String
    private var foodImageUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAddItemBinding

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.image.visibility = View.GONE

        auth = FirebaseAuth.getInstance()

        binding.addImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.addItemBtn.setOnClickListener {
            foodName = binding.enterFoodName.text.toString().trim()
            foodPrice = binding.enterFoodPrice.text.toString().trim()
            foodDescription = binding.description.text.toString().trim()
            foodIngredient = binding.ingredients.text.toString().trim()

            if (foodName.isBlank() || foodPrice.isBlank() || foodDescription.isBlank() || foodIngredient.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else if (foodImageUri == null) {
                Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show()
            } else {
                uploadToCloudinary(foodImageUri!!)
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun uploadToCloudinary(imageUri: Uri) {
        val file = uriToFile(imageUri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .addFormDataPart("upload_preset", "hdapp_upload_image")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dt8rs7d8v/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AddItemActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                val imageUrl = json.getString("secure_url")

                runOnUiThread {
                    uploadDataToRealtimeDatabase(imageUrl)
                }
            }
        })
    }

    private fun uploadDataToRealtimeDatabase(imageUrl: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("AllMenu")
        val itemKey = dbRef.push().key!!

        val newItem = AllMenu(
            key = itemKey,
            foodName = foodName,
            foodPrice = foodPrice,
            foodDescription = foodDescription,
            foodIngredient = foodIngredient,
            foodImage = imageUrl
        )

        dbRef.child(itemKey).setValue(newItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", ".jpg", cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            foodImageUri = it
            binding.image.setImageURI(it)
            binding.image.visibility = View.VISIBLE
        }
    }
}
