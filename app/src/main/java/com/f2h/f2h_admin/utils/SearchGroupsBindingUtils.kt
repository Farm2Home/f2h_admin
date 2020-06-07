package com.f2h.f2h_admin.utils


import android.widget.Button
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.screens.search_group.SearchGroupsItemsModel


@BindingAdapter("buttonVisibilityFormatted")
fun Button.setButtonVisibilityFormatted(data: SearchGroupsItemsModel){
    isEnabled = !data.isAlreadyMember
}
