package com.f2h.f2h_admin.screens.group.assign_delivery


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListAssignDeliveryItemsBinding


class AssignDeliveryItemsAdapter(val checkBoxClickListener: CheckBoxClickListener):
    ListAdapter<AssignDeliveryItemsModel, AssignDeliveryItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, checkBoxClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListAssignDeliveryItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: AssignDeliveryItemsModel,
            checkBoxClickListener: CheckBoxClickListener
        ) {
            binding.uiModel = item
            binding.checkBoxClickListener = checkBoxClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListAssignDeliveryItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<AssignDeliveryItemsModel>() {
    override fun areItemsTheSame(oldItem: AssignDeliveryItemsModel, newItem: AssignDeliveryItemsModel): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: AssignDeliveryItemsModel, newItem: AssignDeliveryItemsModel): Boolean {
        return oldItem == newItem
    }
}


class OrderedItemClickListener(val clickListener: (uiModel: AssignDeliveryItemsModel) -> Unit) {
    fun onClick(uiModel: AssignDeliveryItemsModel) = clickListener(uiModel)
}

class CheckBoxClickListener(val clickListener: (uiModel: AssignDeliveryItemsModel) -> Unit) {
    fun onClick(uiModel: AssignDeliveryItemsModel) = clickListener(uiModel)
}

class SendCommentButtonClickListener(val clickListener: (uiModel: AssignDeliveryItemsModel) -> Unit) {
    fun onClick(uiModel: AssignDeliveryItemsModel) = clickListener(uiModel)
}