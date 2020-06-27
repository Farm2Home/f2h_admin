package com.f2h.f2h_admin.utils

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.screens.group.members.MembersUiModel


@BindingAdapter("rolesFormatted")
fun TextView.setRolesFormatted(data: MembersUiModel){
    text = data.roles
}

@BindingAdapter("isAcceptButtonEnabled")
fun ImageButton.setIsAcceptButtonEnabled(data: MembersUiModel){
    if (data.isBuyerRequested){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}

@BindingAdapter("isWalletButtonEnabled")
fun ImageButton.setIsWalletButtonEnabled(data: MembersUiModel){
    if (data.isBuyerRequested){
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
    }
}
