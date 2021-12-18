package com.example.uaspaba

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemRecyclerAdapter(private var itemList: ArrayList<ItemData>): RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder>() {

    private  lateinit var onItemClickCallback: OnItemClickCallback

    interface OnItemClickCallback {
        fun onItemDelete(data: ItemData)
        fun onItemClicked(data: ItemData)
    }

    fun setOnItemClickCallback(onItemClickCallback: Any) {
        this.onItemClickCallback = onItemClickCallback as OnItemClickCallback
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var itemTitle: TextView
        var itemValue: TextView
        var itemCategory: TextView
        var btnDelete: ImageView

        init {
            itemTitle = itemView.findViewById(R.id.tvTitle)
            itemValue = itemView.findViewById(R.id.tvValue)
            itemCategory = itemView.findViewById(R.id.tvCategory)
            btnDelete = itemView.findViewById(R.id.btnDelete)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemTitle.text = item.title
        holder.itemValue.text = "Rp. ${item.value.toString()}"
        holder.itemCategory.text = item.category
        holder.btnDelete.setOnClickListener {
            onItemClickCallback.onItemDelete(item)
        }
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(item)
        }
    }

}