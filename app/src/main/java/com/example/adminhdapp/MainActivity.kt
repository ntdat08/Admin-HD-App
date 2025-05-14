package com.example.adminhdapp

import CloudinaryHelper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.adminhdapp.databinding.ActivityMainBinding
import com.example.adminhdapp.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth : FirebaseAuth
    private lateinit var completedOrderReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CloudinaryHelper.initialize(this)
        auth = FirebaseAuth.getInstance()
        binding.logOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }

        binding.panding.setOnClickListener {
            startActivity(Intent(this, PendingOrderActivity::class.java))
        }
        binding.createNewUser.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
        binding.profile.setOnClickListener {
            startActivity(Intent(this, AdminProfileActivity::class.java))
        }
        binding.dispach.setOnClickListener {
            startActivity(Intent(this, OutForDeliveryActivity::class.java))
        }
        binding.addMenu.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }
        binding.allItem.setOnClickListener {
            startActivity(Intent(this, AllItemsActivity::class.java))
        }

        pendingOrders()
        completedOrders()
        allTimeEarning()

    }

    private fun allTimeEarning() {

        val listOfTotalPay = mutableListOf<Int>()
        completedOrderReference = FirebaseDatabase.getInstance().reference.child("Completed Orders")
        completedOrderReference.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (orderSnapshot in snapshot.children){
                    val completedOrder = orderSnapshot.getValue(OrderDetails::class.java)
                    completedOrder?.totalPrice?.replace("$","")?.toIntOrNull()
                        ?.let { i ->
                            listOfTotalPay.add(i)
                        }
                }

                binding.totalEarning.text = listOfTotalPay.sum().toString() + "$"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun completedOrders() {

        val completedOrderRf = database.reference.child("Completed Orders")
        var completedOrderCount = 0

        completedOrderRf.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                completedOrderCount = snapshot.childrenCount.toInt()
                binding.completedOrder.text = completedOrderCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun pendingOrders() {
        database = FirebaseDatabase.getInstance()
        val pendingOrderReference = database.reference.child("Order Details")
        var pendingOrderItemCount = 0
        pendingOrderReference.addListenerForSingleValueEvent( object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                pendingOrderItemCount = snapshot.childrenCount.toInt()
                binding.pendingCount.text = pendingOrderItemCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}