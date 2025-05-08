package com.example.adminhdapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminhdapp.databinding.AllItemBinding
import com.example.adminhdapp.model.AllMenu
import com.google.firebase.database.DatabaseReference

class MenuItemAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    databaseRef: DatabaseReference,
    private val onDelete : (position : Int) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.viewHolder>() {

    private val itemQuantities = IntArray(menuList.size) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = AllItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }


    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class viewHolder(private val binding: AllItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            binding.apply {

                val menuItem = menuList[position]
                val uriString = menuItem.foodImage
                val uri = Uri.parse(uriString)

                cartFoodName.text = menuItem.foodName
                cartPrice.text = menuItem.foodPrice
                Glide.with(context).load(uri).into(imageView4)

                minusBtn.setOnClickListener {
                    decreaseQuantity(position)
                }
                plusBtn.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteBtn.setOnClickListener {
                    onDelete(position)
                }
            }

        }

        private fun deleteItem(position: Int) {
            menuList.removeAt(position)
            menuList.removeAt(position)
            menuList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, menuList.size)
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                binding.carItemQuantity.text = itemQuantities[position].toString()
            }
        }


        private fun decreaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                binding.carItemQuantity.text = itemQuantities[position].toString()
            }
        }


    }


}