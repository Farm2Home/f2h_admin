package com.f2h.f2h_admin.screens.group.edit_item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListEditHandlingChargesBinding


class HandlingChargesItemsAdapter(val checkBoxClickListener: CheckBoxClickListener):
    ListAdapter<HandlingChargesItemsModel, HandlingChargesItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, checkBoxClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListEditHandlingChargesBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: HandlingChargesItemsModel,
            checkBoxClickListener: CheckBoxClickListener
        ) {
            binding.uiModel = item
            binding.checkBoxClickListener = checkBoxClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListEditHandlingChargesBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<HandlingChargesItemsModel>() {
    override fun areItemsTheSame(oldItem: HandlingChargesItemsModel, newItem: HandlingChargesItemsModel): Boolean {
        return oldItem.handlingOptionId == newItem.handlingOptionId
    }

    override fun areContentsTheSame(oldItem: HandlingChargesItemsModel, newItem: HandlingChargesItemsModel): Boolean {
        return oldItem == newItem
    }
}


class CheckBoxClickListener(val clickListener: (uiModel: HandlingChargesItemsModel) -> Unit) {
    fun onClick(uiModel: HandlingChargesItemsModel) = clickListener(uiModel)
}
