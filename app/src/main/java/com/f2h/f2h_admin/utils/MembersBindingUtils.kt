package com.f2h.f2h_admin.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.screens.group.members.MembersUiModel


@BindingAdapter("rolesFormatted")
fun TextView.setRolesFormatted(data: MembersUiModel){
    text = data.roles
}
