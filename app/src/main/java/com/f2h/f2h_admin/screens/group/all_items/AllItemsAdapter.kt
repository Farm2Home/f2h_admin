package com.f2h.f2h_admin.screens.group.all_items

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListAllItemsBinding
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.screens.group.pre_order.IncreaseButtonClickListener

class AllItemsAdapter(val clickListener: AllItemClickListener,
                      val editButtonClickListener: EditButtonClickListener
): ListAdapter<Item, AllItemsAdapter.ViewHolder>(AllItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, editButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListAllItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Item,
            clickListener: AllItemClickListener,
            editButtonClickListener: EditButtonClickListener
        ) {
            binding.item = item
            binding.clickListener = clickListener
            binding.editButtonClickListener = editButtonClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListAllItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class AllItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.itemId == newItem.itemId
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}

class AllItemClickListener(val clickListener: (item: Item) -> Unit) {
    fun onClick(item: Item) = clickListener(item)
}

class EditButtonClickListener(val clickListener: (item: Item) -> Unit) {
    fun onClick(item: Item) = clickListener(item)
}