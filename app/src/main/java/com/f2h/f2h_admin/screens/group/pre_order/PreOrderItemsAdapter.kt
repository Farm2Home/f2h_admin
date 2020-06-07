package com.f2h.f2h_admin.screens.group.pre_order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListPreorderItemBinding

class PreOrderItemsAdapter(val clickListener: PreOrderItemClickListener,
                           val increaseButtonClickListener: IncreaseButtonClickListener,
                           val decreaseButtonClickListener: DecreaseButtonClickListener
): ListAdapter<PreOrderItemsModel, PreOrderItemsAdapter.ViewHolder>(
    TableComponentDiffCallback()
) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener, increaseButtonClickListener, decreaseButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }


    class ViewHolder private constructor(val binding: ListPreorderItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            uiItemsModel: PreOrderItemsModel?,
            clickListener: PreOrderItemClickListener,
            increaseButtonClickListener: IncreaseButtonClickListener,
            decreaseButtonClickListener: DecreaseButtonClickListener
        ) {
            binding.uiModel = uiItemsModel
            binding.clickListener = clickListener
            binding.increaseButtonClickListener = increaseButtonClickListener
            binding.decreaseButtonClickListener = decreaseButtonClickListener
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

class TableComponentDiffCallback : DiffUtil.ItemCallback<PreOrderItemsModel>() {
    override fun areItemsTheSame(oldItem: PreOrderItemsModel, newItem: PreOrderItemsModel): Boolean {
        return oldItem.itemAvailabilityId == newItem.itemAvailabilityId
    }

    override fun areContentsTheSame(oldItem: PreOrderItemsModel, newItem: PreOrderItemsModel): Boolean {
        return oldItem == newItem
    }
}

class PreOrderItemClickListener(val clickListener: (preOrder: PreOrderItemsModel) -> Unit) {
    fun onClick(preOrder: PreOrderItemsModel) = clickListener(preOrder)
}

class IncreaseButtonClickListener(val clickListener: (preOrder: PreOrderItemsModel) -> Unit) {
    fun onClick(preOrder: PreOrderItemsModel) = clickListener(preOrder)
}

class DecreaseButtonClickListener(val clickListener: (preOrder: PreOrderItemsModel) -> Unit) {
    fun onClick(preOrder: PreOrderItemsModel) = clickListener(preOrder)
}