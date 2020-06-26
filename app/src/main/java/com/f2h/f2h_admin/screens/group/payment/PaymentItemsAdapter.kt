package com.f2h.f2h_admin.screens.group.payment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListPaymentItemsBinding

class PaymentItemsAdapter(val clickListener: OrderedItemClickListener,
                                val checkBoxClickListener: CheckBoxClickListener
): ListAdapter<PaymentItemsModel, PaymentItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, checkBoxClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListPaymentItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PaymentItemsModel,
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
                val binding = ListPaymentItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<PaymentItemsModel>() {
    override fun areItemsTheSame(oldItem: PaymentItemsModel, newItem: PaymentItemsModel): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: PaymentItemsModel, newItem: PaymentItemsModel): Boolean {
        return oldItem == newItem
    }
}


class OrderedItemClickListener(val clickListener: (uiModel: PaymentItemsModel) -> Unit) {
    fun onClick(uiModel: PaymentItemsModel) = clickListener(uiModel)
}

class CheckBoxClickListener(val clickListener: (uiModel: PaymentItemsModel) -> Unit) {
    fun onClick(uiModel: PaymentItemsModel) = clickListener(uiModel)
}

