package com.f2h.f2h_admin.screens.group.group_wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListOrderedItemsBinding
import com.f2h.f2h_admin.databinding.ListReportItemsBinding
import com.f2h.f2h_admin.databinding.ListWalletItemsBinding

class WalletItemsAdapter(val clickListener: WalletItemClickListener): ListAdapter<WalletItemsModel, WalletItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListWalletItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: WalletItemsModel,
            clickListener: WalletItemClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListWalletItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<WalletItemsModel>() {
    override fun areItemsTheSame(oldItem: WalletItemsModel, newItem: WalletItemsModel): Boolean {
        return oldItem.walletLedgerId == newItem.walletLedgerId
    }

    override fun areContentsTheSame(oldItem: WalletItemsModel, newItem: WalletItemsModel): Boolean {
        return oldItem == newItem
    }
}


class WalletItemClickListener(val clickListener: (uiModel: WalletItemsModel) -> Unit) {
    fun onClick(uiModel: WalletItemsModel) = clickListener(uiModel)
}

