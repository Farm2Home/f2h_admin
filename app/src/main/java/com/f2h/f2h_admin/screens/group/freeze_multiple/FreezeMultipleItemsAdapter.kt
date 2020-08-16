package com.f2h.f2h_admin.screens.group.freeze_multiple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListFreezeMultipleBinding


class FreezeMultipleItemsAdapter(val checkBoxClickListener: CheckBoxClickListener):
    ListAdapter<FreezeMultipleItemsModel, FreezeMultipleItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, checkBoxClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListFreezeMultipleBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: FreezeMultipleItemsModel,
            checkBoxClickListener: CheckBoxClickListener
        ) {
            binding.uiModel = item
            binding.checkBoxClickListener = checkBoxClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListFreezeMultipleBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<FreezeMultipleItemsModel>() {
    override fun areItemsTheSame(oldItem: FreezeMultipleItemsModel, newItem: FreezeMultipleItemsModel): Boolean {
        return oldItem.availabilityId == newItem.availabilityId
    }

    override fun areContentsTheSame(oldItem: FreezeMultipleItemsModel, newItem: FreezeMultipleItemsModel): Boolean {
        return oldItem == newItem
    }
}


class CheckBoxClickListener(val clickListener: (uiModel: FreezeMultipleItemsModel) -> Unit) {
    fun onClick(uiModel: FreezeMultipleItemsModel) = clickListener(uiModel)
}
