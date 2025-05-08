package com.example.adminhdapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminhdapp.adapters.PendingOrderAdapter
import com.example.adminhdapp.databinding.ActivityPendingOrderBinding
import com.example.adminhdapp.model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {
    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfName: MutableList<String> = mutableListOf()
    private var listOfTotalPrice: MutableList<String> = mutableListOf()
    private var listOfImageFirstFoodOrder: MutableList<String> = mutableListOf()
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization of database
        database = FirebaseDatabase.getInstance()

        // Initialization of databaseReference
        databaseOrderDetails = database.reference.child("Order Details")

        getOrderDetails()

        binding.backBtn.setOnClickListener {
            finish()
        }


    }

    private fun getOrderDetails() {

        // Retrieve order details from firebase database
        databaseOrderDetails.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnapshot in snapshot.children) {
                    var orderDetails = orderSnapshot.getValue(OrderDetails::class.java)

                    orderDetails?.let {
                        listOfOrderItem.add(it)
                    }
                }
                addDataToListForRecyclerview()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addDataToListForRecyclerview() {
        for (orderItem in listOfOrderItem) {

            // Add data to respective list for populating the recyclerview
            orderItem.userName?.let { listOfName.add(it) }
            orderItem.totalPrice?.let { listOfTotalPrice.add(it) }
            orderItem.foodItemImage?.filterNot { it.isEmpty() }?.forEach {
                listOfImageFirstFoodOrder.add(it)
            }
        }
        setAdapter()
    }

    private fun setAdapter() {

        binding.pRv.layoutManager = LinearLayoutManager(this)
        val adapter = PendingOrderAdapter(
            this, listOfName, listOfTotalPrice, listOfImageFirstFoodOrder, this
        )
        binding.pRv.adapter = adapter
    }

    override fun OnItemClickListener(position: Int) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("UserOrderDetails", userOrderDetails)
        startActivity(intent)
    }

    override fun OnItemAcceptClickListener(position: Int) {

        // Handel item acceptance and update data on database
        val childItemPushKey = listOfOrderItem[position].itemPushKey
        val clickItemOrderReference = childItemPushKey?.let {
            database.reference.child("Order Details").child(it)
        }
        clickItemOrderReference?.child("orderAccepted")?.setValue(true)
        updateOrderAcceptStatus(position)
    }


    override fun OnItemDispatchClickListener(position: Int) {

        // Handel item dispatch and update data on database
        val dispatchItemPushKey = listOfOrderItem[position].itemPushKey
        val dispatchItemOrderReference = database.reference.child("Completed Orders").child(dispatchItemPushKey!!)
        dispatchItemOrderReference.setValue(listOfOrderItem[position])
            .addOnSuccessListener {
                deleteThisItemFromOrderDetails(dispatchItemPushKey)
            }

    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String){
        val orderDetailsItemReference = database.reference.child("Order Details").child(dispatchItemPushKey)
        orderDetailsItemReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Order is dispatched", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Order is not accepted", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateOrderAcceptStatus(position: Int) {

        // Update order acceptance in users buy history and order details
        val userIdOfClickedItem = listOfOrderItem[position].userId
        val pushKeyOfClickedItem = listOfOrderItem[position].itemPushKey
        val buyHistoryReference =
            database.reference.child("Users").child(userIdOfClickedItem!!).child("Buy History")
                .child(pushKeyOfClickedItem!!)
        buyHistoryReference.child("orderAccepted").setValue(true)
        databaseOrderDetails.child(pushKeyOfClickedItem).child("orderAccepted").setValue(true)
    }


}