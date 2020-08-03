package com.f2h.f2h_admin.screens.deliver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListDeliverItemsBinding

class DeliverItemsAdapter(val clickListener: OrderedItemClickListener,
                          val checkBoxClickListener: CheckBoxClickListener,
                          val callUserButtonClickListener: CallUserButtonClickListener,
                          val sendCommentButtonClickListener: SendCommentButtonClickListener
): ListAdapter<DeliverItemsModel, DeliverItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, checkBoxClickListener,
            callUserButtonClickListener, sendCommentButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListDeliverItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: DeliverItemsModel,
            clickListener: OrderedItemClickListener,
            checkBoxClickListener: CheckBoxClickListener,
            callUserButtonClickListener: CallUserButtonClickListener,
            sendCommentButtonClickListener: SendCommentButtonClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.checkBoxClickListener = checkBoxClickListener
            binding.callUserButtonClickListener = callUserButtonClickListener
            binding.sendCommentButtonClickListener = sendCommentButtonClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListDeliverItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<DeliverItemsModel>() {
    override fun areItemsTheSame(oldItem: DeliverItemsModel, newItem: DeliverItemsModel): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: DeliverItemsModel, newItem: DeliverItemsModel): Boolean {
        return oldItem == newItem
    }
}


class OrderedItemClickListener(val clickListener: (uiModel: DeliverItemsModel) -> Unit) {
    fun onClick(uiModel: DeliverItemsModel) = clickListener(uiModel)
}

class CheckBoxClickListener(val clickListener: (uiModel: DeliverItemsModel) -> Unit) {
    fun onClick(uiModel: DeliverItemsModel) = clickListener(uiModel)
}

class CallUserButtonClickListener(val clickListener: (uiModel: DeliverItemsModel) -> Unit) {
    fun onClick(uiModel: DeliverItemsModel) = clickListener(uiModel)
}

class SendCommentButtonClickListener(val clickListener: (uiModel: DeliverItemsModel) -> Unit) {
    fun onClick(uiModel: DeliverItemsModel) = clickListener(uiModel)
}
