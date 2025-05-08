package com.example.adminhdapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminhdapp.databinding.OrderdetailsItemsBinding

class OrderDetailsAdapter(
    private var context: Context,
    private var fooNames: ArrayList<String>,
    private var foodImages: ArrayList<String>,
    private var foodQuantitys: ArrayList<Int>,
    private var foodPrices: ArrayList<String>
) : RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val binding =
            OrderdetailsItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = fooNames.size

    inner class OrderDetailsViewHolder(private val binding: OrderdetailsItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {

                foodName.text =  fooNames[position]
                quantity.text = foodQuantitys[position].toString()
                foodPrice.text = foodPrices[position]

                val uriString = foodImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(fImage)


            }
        }

    }

}