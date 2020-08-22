package com.f2h.f2h_admin.screens.group.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListNotificationItemsBinding

class NotificationItemsAdapter(val clickListener: NotificationItemClickListener): ListAdapter<NotificationItemsModel, NotificationItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListNotificationItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: NotificationItemsModel,
            clickListener: NotificationItemClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListNotificationItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<NotificationItemsModel>() {
    override fun areItemsTheSame(oldItem: NotificationItemsModel, newItem: NotificationItemsModel): Boolean {
        return oldItem.notificationId == newItem.notificationId
    }

    override fun areContentsTheSame(oldItem: NotificationItemsModel, newItem: NotificationItemsModel): Boolean {
        return oldItem == newItem
    }
}


class NotificationItemClickListener(val clickListener: (uiModel: NotificationItemsModel) -> Unit) {
    fun onClick(uiModel: NotificationItemsModel) = clickListener(uiModel)
}

