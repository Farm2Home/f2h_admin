package com.f2h.f2h_admin.screens.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListReportItemsBinding

class ReportItemsAdapter(val clickListener: OrderedItemClickListener): ListAdapter<ReportItemsModel, ReportItemsAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(val binding: ListReportItemsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ReportItemsModel,
            clickListener: OrderedItemClickListener
        ) {
            binding.uiModel = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListReportItemsBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}


class ListItemDiffCallback : DiffUtil.ItemCallback<ReportItemsModel>() {
    override fun areItemsTheSame(oldItem: ReportItemsModel, newItem: ReportItemsModel): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: ReportItemsModel, newItem: ReportItemsModel): Boolean {
        return oldItem == newItem
    }
}


class OrderedItemClickListener(val clickListener: (uiModel: ReportItemsModel) -> Unit) {
    fun onClick(uiModel: ReportItemsModel) = clickListener(uiModel)
}

