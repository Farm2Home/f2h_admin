package com.f2h.f2h_admin.screens.group.members

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListMembersBinding

class OrderedItemsAdapter(val deleteUserButtonClickListener: DeleteUserButtonClickListener): ListAdapter<MembersUiModel, OrderedItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, deleteUserButtonClickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListMembersBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MembersUiModel,
            deleteUserButtonClickListener: DeleteUserButtonClickListener
        ) {
            binding.uiModel = item
            binding.deleteUserButtonClickListener = deleteUserButtonClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListMembersBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<MembersUiModel>() {
    override fun areItemsTheSame(oldItem: MembersUiModel, newItem: MembersUiModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MembersUiModel, newItem: MembersUiModel): Boolean {
        return oldItem == newItem
    }
}

class DeleteUserButtonClickListener(val clickListener: (uiModel: MembersUiModel) -> Unit) {
    fun onClick(uiModel: MembersUiModel) = clickListener(uiModel)
}
