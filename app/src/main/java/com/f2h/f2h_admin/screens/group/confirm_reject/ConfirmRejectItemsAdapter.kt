package com.f2h.f2h_admin.screens.group.confirm_reject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListConfirmRejectItemsBinding

class ConfirmRejectItemsAdapter(val clickListener: OrderedItemClickListener,
                                val checkBoxClickListener: CheckBoxClickListener,
                                val increaseButtonClickListener: IncreaseButtonClickListener,
                                val decreaseButtonClickListener: DecreaseButtonClickListener
): ListAdapter<ConfirmRejectItemsModel, ConfirmRejectItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, checkBoxClickListener,
            increaseButtonClickListener, decreaseButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListConfirmRejectItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ConfirmRejectItemsModel,
            clickListener: OrderedItemClickListener,
            checkBoxClickListener: CheckBoxClickListener,
            increaseButtonClickListener: IncreaseButtonClickListener,
            decreaseButtonClickListener: DecreaseButtonClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.checkBoxClickListener = checkBoxClickListener
            binding.increaseButtonClickListener = increaseButtonClickListener
            binding.decreaseButtonClickListener = decreaseButtonClickListener
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

class IncreaseButtonClickListener(val clickListener: (uiModel: ConfirmRejectItemsModel) -> Unit) {
    fun onClick(uiModel: ConfirmRejectItemsModel) = clickListener(uiModel)
}

class DecreaseButtonClickListener(val clickListener: (uiModel: ConfirmRejectItemsModel) -> Unit) {
    fun onClick(uiModel: ConfirmRejectItemsModel) = clickListener(uiModel)
}
