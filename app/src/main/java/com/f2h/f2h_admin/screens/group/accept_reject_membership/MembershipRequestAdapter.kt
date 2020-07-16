package com.f2h.f2h_admin.screens.group.accept_reject_membership

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_admin.databinding.ListMembershipRequestBinding

class MembershipRequestAdapter(
    val onMembershipActionSelected: (position: Int, id: Long, uiElemet: MembershipRequestUiModel) -> Unit):
    ListAdapter<MembershipRequestUiModel, MembershipRequestAdapter.ViewHolder>(ListItemDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!, onMembershipActionSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListMembershipRequestBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MembershipRequestUiModel,
            onMembershipActionSelected: (position: Int, id: Long, uiElemet: MembershipRequestUiModel) -> Unit
        ) {
            println("bind")
            println(item)
            binding.uiModel = item

            binding.roleActionSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onMembershipActionSelected(position, id, item)
                }
            }

            if (item.requestedRole) {
                binding.roleActionSelector.visibility = View.VISIBLE
            }
            else{
                binding.roleActionSelector.visibility = View.GONE
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                val binding = ListMembershipRequestBinding.inflate(view, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class ListItemDiffCallback : DiffUtil.ItemCallback<MembershipRequestUiModel>() {
    override fun areItemsTheSame(oldItem: MembershipRequestUiModel, newItem: MembershipRequestUiModel): Boolean {
//        println("are items same")
//        println(oldItem)
//        println(newItem)
        return false
    }

    override fun areContentsTheSame(oldItem: MembershipRequestUiModel, newItem: MembershipRequestUiModel): Boolean {
//        println("are contents same")
//        println(oldItem)
//        println(newItem)
        return oldItem == newItem
    }
}

class OnMembershipActionSelectedListner(val onMembershipActionSelected: (position: Int, id: Long) -> Unit) {
    fun onItemSelectedListener(position: Int, id: Long) = onMembershipActionSelected(position, id)
}

