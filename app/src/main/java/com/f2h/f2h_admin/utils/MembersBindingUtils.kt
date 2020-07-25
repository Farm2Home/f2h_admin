package com.f2h.f2h_admin.utils

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.screens.group.members.MembersUiModel


@BindingAdapter("rolesFormatted")
fun TextView.setRolesFormatted(data: MembersUiModel){
    text = data.roles
}

@BindingAdapter("acceptButtonColor")
fun ImageButton.setAcceptButtonColor(data: MembersUiModel){
    if (data.isBuyerRequested){
        backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_status)
    }
    else{
        backgroundTintList = ContextCompat.getColorStateList(context, R.color.cardview_shadow_start_color)
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
