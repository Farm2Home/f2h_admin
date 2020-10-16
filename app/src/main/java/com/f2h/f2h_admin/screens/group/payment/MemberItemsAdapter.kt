package com.f2h.f2h_admin.screens.group.payment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListPaymentMembersBinding

class MemberItemsAdapter(val membersItemClickListener: MembersItemClickListener,
                         val viewModel: MembersViewModel):
    ListAdapter<MembersUiModel, MemberItemsAdapter.ViewHolder>(ListMemberDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, membersItemClickListener, viewModel)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListPaymentMembersBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MembersUiModel,
            membersItemClickListener: MembersItemClickListener,
            viewModel: MembersViewModel
        ) {
            binding.uiModel = item
            binding.clickListener = membersItemClickListener
            binding.cashCollectedClickListener = CashCollectedButtonClickListener { uiDataElement ->
                viewModel.onCashCollectedButtonClicked(uiDataElement)
            }

            val adapter = DeliverItemsAdapter(OrderedItemClickListener { uiDataElement ->
                viewModel.moreDetailsButtonClicked(uiDataElement)
            }, CheckBoxClickListener{uiDataElement ->
                viewModel.onCheckboxClicked(uiDataElement)
            }, SendCommentButtonClickListener{uiDataElement ->
                viewModel.onSendCommentButtonClicked(uiDataElement)
            })
            adapter.submitList(item.paymentItems)
            binding.itemListRecyclerView2.adapter = adapter

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListPaymentMembersBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListMemberDiffCallback : DiffUtil.ItemCallback<MembersUiModel>() {
    override fun areItemsTheSame(oldItem: MembersUiModel, newItem: MembersUiModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MembersUiModel, newItem: MembersUiModel): Boolean {
        return oldItem == newItem
    }
}


class MembersItemClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}


class CashCollectedButtonClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}