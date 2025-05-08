package com.example.adminhdapp.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminhdapp.databinding.PendingOrderItemBinding
import com.example.adminhdapp.model.OrderDetails

class PendingOrderAdapter(
    private val context: Context,
    private val customerName: MutableList<String>,
    private val quantitys: MutableList<String>,
    private val fImages: MutableList<String>,
    private val itemClicked: OnItemClicked

) : RecyclerView.Adapter<PendingOrderAdapter.ViewHolder>() {

    interface OnItemClicked{

        fun OnItemClickListener (position: Int)
        fun OnItemAcceptClickListener (position: Int)
        fun OnItemDispatchClickListener (position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = customerName.size

    inner class ViewHolder(private val binding: PendingOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var isAccepted = false
        fun bind(position: Int) {
            binding.apply {
                buyerName.text = customerName[position]
                quantity.text = quantitys[position]

                val uriString = fImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(fImage)

                acceptBtn.apply {
                    if (!isAccepted) {
                        text = "Accept"
                    } else {
                        text = "Dispatch"
                    }
                    setOnClickListener {
                        if (!isAccepted) {
                            text = "Dispatch"
                            isAccepted = true
                            showToast("Order is Accepted")
                            itemClicked.OnItemAcceptClickListener(position)

                        } else{
                            customerName.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            showToast("Order Is Dispatch")
                            itemClicked.OnItemDispatchClickListener(position)
                        }
                    }
                }
                itemView.setOnClickListener {
                    itemClicked.OnItemClickListener(position)
                }
            }

        }
        private  fun showToast (message : String){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
}