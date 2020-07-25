package com.f2h.f2h_admin.screens.group.pre_order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListPreorderItemBinding
import com.f2h.f2h_admin.network.models.Item

class PreOrderItemsAdapter(val clickListener: PreOrderItemClickListener,
                           val editButtonClickListener: EditButtonClickListener
): ListAdapter<AvailabilityItemsModel, PreOrderItemsAdapter.ViewHolder>(
    TableComponentDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, editButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }


    class ViewHolder private constructor(val binding: ListPreorderItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            uiItemsModel: AvailabilityItemsModel?,
            clickListener: PreOrderItemClickListener,
            editButtonClickListener: EditButtonClickListener
        ) {
            binding.uiModel = uiItemsModel
            binding.clickListener = clickListener
            binding.editButtonClickListener = editButtonClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListPreorderItemBinding.inflate(view, parent, false)
                return ViewHolder(
                    binding
                )
            }
        }
    }
}

class TableComponentDiffCallback : DiffUtil.ItemCallback<AvailabilityItemsModel>() {
    override fun areItemsTheSame(oldItem: AvailabilityItemsModel, newItem: AvailabilityItemsModel): Boolean {
        return oldItem.itemAvailabilityId == newItem.itemAvailabilityId
    }

    override fun areContentsTheSame(oldItem: AvailabilityItemsModel, newItem: AvailabilityItemsModel): Boolean {
        return oldItem == newItem
    }
}

class PreOrderItemClickListener(val clickListener: (availability: AvailabilityItemsModel) -> Unit) {
    fun onClick(availability: AvailabilityItemsModel) = clickListener(availability)
}

class EditButtonClickListener(val clickListener: (availability: AvailabilityItemsModel) -> Unit) {
    fun onClick(availability: AvailabilityItemsModel) = clickListener(availability)
}