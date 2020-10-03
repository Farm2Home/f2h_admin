package com.f2h.f2h_admin.screens.group.deliver

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListDeliveryMembersBinding

class MemberItemsAdapter(val callUserButtonClickListener: CallUserButtonClickListener,
                         val membersItemClickListener: MembersItemClickListener,
                         val viewModel: MembersViewModel):
    ListAdapter<MembersUiModel, MemberItemsAdapter.ViewHolder>(ListMemberDiffCallback()){

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, callUserButtonClickListener, membersItemClickListener, viewModel)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListDeliveryMembersBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MembersUiModel,
            callUserButtonClickListener: CallUserButtonClickListener,
            membersItemClickListener: MembersItemClickListener,
            viewModel: MembersViewModel
        ) {
            binding.uiModel = item
            binding.callUserButtonClickListener = callUserButtonClickListener
            binding.clickListener = membersItemClickListener
            binding.deliverClickListener = DeliverButtonClickListener { uiDataElement ->
                viewModel.onDeliverButtonClicked(uiDataElement)
            }

            val adapter = DeliverItemsAdapter(OrderedItemClickListener { uiDataElement ->
                viewModel.moreDetailsButtonClicked(uiDataElement)
            }, CheckBoxClickListener{uiDataElement ->
                viewModel.onCheckboxClicked(uiDataElement)
            }, ReceiveButtonClickListener{uiDataElement ->
                viewModel.onReceiveButtonClicked(uiDataElement)
            }, SendCommentButtonClickListener{uiDataElement ->
                viewModel.onSendCommentButtonClicked(uiDataElement)
            })
            adapter.submitList(item.deliveryItems)
            binding.itemListRecyclerView2.adapter = adapter

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListDeliveryMembersBinding.inflate(view, parent, false)
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


class CallUserButtonClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}

class MembersItemClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}

class DeliverButtonClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}