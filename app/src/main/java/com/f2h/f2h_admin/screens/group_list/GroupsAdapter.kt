package com.f2h.f2h_admin.screens.group_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListGroupsBinding
import com.f2h.f2h_admin.network.models.Group

class GroupsAdapter(val clickListener: GroupClickListener): ListAdapter<Group, GroupsAdapter.ViewHolder>(GroupDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListGroupsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Group,
            clickListener: GroupClickListener
        ) {
            binding.group = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListGroupsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
    override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem.groupId == newItem.groupId
    }

    override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
        return oldItem == newItem
    }
}

class GroupClickListener(val clickListener: (group: Group) -> Unit) {
    fun onClick(group: Group) = clickListener(group)
}