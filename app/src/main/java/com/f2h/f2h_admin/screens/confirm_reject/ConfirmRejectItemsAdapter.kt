package com.f2h.f2h_admin.screens.confirm_reject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListConfirmRejectItemsBinding
import com.f2h.f2h_admin.databinding.ListReportItemsBinding
import com.f2h.f2h_admin.screens.group.pre_order.DecreaseButtonClickListener

class ConfirmRejectItemsAdapter(val clickListener: OrderedItemClickListener,
                                val checkBoxClickListener: CheckBoxClickListener
): ListAdapter<ConfirmRejectItemsModel, ConfirmRejectItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, checkBoxClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListConfirmRejectItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ConfirmRejectItemsModel,
            clickListener: OrderedItemClickListener,
            checkBoxClickListener: CheckBoxClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.checkBoxClickListener = checkBoxClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListConfirmRejectItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<ConfirmRejectItemsModel>() {
    override fun areItemsTheSame(oldItem: ConfirmRejectItemsModel, newItem: ConfirmRejectItemsModel): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: ConfirmRejectItemsModel, newItem: ConfirmRejectItemsModel): Boolean {
        return oldItem == newItem
    }
}


class OrderedItemClickListener(val clickListener: (uiModel: ConfirmRejectItemsModel) -> Unit) {
    fun onClick(uiModel: ConfirmRejectItemsModel) = clickListener(uiModel)
}

class CheckBoxClickListener(val clickListener: (uiModel: ConfirmRejectItemsModel) -> Unit) {
    fun onClick(uiModel: ConfirmRejectItemsModel) = clickListener(uiModel)
}

