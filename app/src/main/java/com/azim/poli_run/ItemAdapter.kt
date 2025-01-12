package com.azim.poli_run

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.azim.poli_run.databinding.RecyclerItemBinding

class ItemAdapter(private val dataList : MutableList<Data>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // Inflate the RecyclerItem layout using ViewBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = dataList[position]
        Log.d("ItemAdapter", "Binding data at position: $position, Topic: ${data.topic}")
        holder.bind(data)
    }

    // Return the size of the data list
    override fun getItemCount(): Int {
        return dataList.size
    }

    // Function to update the adapter with new data (using DiffUtil for efficient updates)
    fun setData(data: List<Data>) {
        val diffCallback = DataDiffCallback(this.dataList, data)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        // Clear the old data and add new data (without replacing the reference)
        this.dataList.clear()
        this.dataList.addAll(data)

        // Notify the adapter of the changes
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil Callback to compare old and new data
    class DataDiffCallback(
        private val oldList: List<Data>,
        private val newList: List<Data>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Assuming each data has a unique ID or it's unique by its content
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Compare the contents of the items
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
    // ViewHolder class to hold the views and bind data
    class ItemViewHolder(private val binding: RecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views in each RecyclerView item
        fun bind(data: Data) {
            binding.recName.text = data.name
            binding.recTitle.text = data.topic
            binding.recDesc.text = data.description

            // Create a formatted string for recRunNo with index and currentMonth
            val runNoText = "PTSN/JKPK/700-1/${data.currentMonth ?: "N/A"}/${data.index ?: "N/A"}"
            binding.recRunNo.text = runNoText  // Assuming recRunNo is your TextView
        }
    }
}
