package com.example.adminhdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminhdapp.adapters.MenuItemAdapter
import com.example.adminhdapp.databinding.ActivityAllItemsBinding
import com.example.adminhdapp.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllItemsActivity : AppCompatActivity() {

    private lateinit var databaseRef : DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var menu : ArrayList<AllMenu> = ArrayList()

    private lateinit var binding : ActivityAllItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().reference
        retrieveMenuItem()

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()

        val foodRef : DatabaseReference = database.reference.child("Menu")

        // Fetch Data from dataBase
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot : DataSnapshot){
                // Clear exiting data before populating
                menu.clear()

                // loop for through each food item
                for (foodSnapshot in snapshot.children){
                    val  menur = foodSnapshot.getValue(AllMenu::class.java)
                    menur?.let {
                        menu.add(it)
                    }
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError","Error : ${error.message}")
            }


        })
    }
    private fun setAdapter() {

        var adapter = MenuItemAdapter( this,menu,databaseRef){position ->
            deleteMenuItem(position)
        }
        binding.menuRv.layoutManager=LinearLayoutManager(this)
        binding.menuRv.adapter=adapter
    }

    private fun deleteMenuItem(position: Int) {
        val menuItemToDelete = menu[position]
        val menuItemKey = menuItemToDelete.key
        val foodMenuReference = database.reference.child("Menu").child(menuItemKey!!)
        foodMenuReference.removeValue().addOnCompleteListener {task ->
            if (task.isSuccessful){
                menu.removeAt(position)
                binding.menuRv.adapter?.notifyItemRemoved(position)
            }else{
                Toast.makeText(this, "Item not deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}