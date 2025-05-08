package com.example.adminhdapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminhdapp.adapters.DeliveryAdapter
import com.example.adminhdapp.databinding.ActivityOutForDeliveryBinding
import com.example.adminhdapp.model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OutForDeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOutForDeliveryBinding
    private lateinit var database: FirebaseDatabase
    private var completeOrderList: ArrayList<OrderDetails> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutForDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Retrieve and display completed order
        retrieveCompleteOrderDetails()


        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private fun retrieveCompleteOrderDetails() {

        // Initialize firebase database
        database = FirebaseDatabase.getInstance()

        val completeOrderReference = database.reference.child("Completed Orders")
        completeOrderReference.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear the list before populating it with new data
                completeOrderList.clear()

                for (orderSnapshot in snapshot.children){

                    val completeOrder = orderSnapshot.getValue(OrderDetails::class.java)
                    completeOrder?.let {
                        completeOrderList.add(it)
                    }
                }
                // Reverse the list to display latest order first
                completeOrderList.reverse()

                setDataIntoRecyclerView()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setDataIntoRecyclerView() {

        // Initialization list to hold costumer name and payment status

        val costumerName = mutableListOf<String>()
        val moneyStatus = mutableListOf<Boolean>()

        for (order in completeOrderList){
            order.userName?.let {
                costumerName.add(it)
            }
            moneyStatus.add(order.paymentReceived)
        }
        val adapter = DeliveryAdapter(costumerName,moneyStatus)
        binding.deliveryRv.layoutManager = LinearLayoutManager(this)
        binding.deliveryRv.adapter = adapter
    }
}