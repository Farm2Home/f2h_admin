package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.screens.group.pre_order.AvailabilityItemsModel
import com.f2h.f2h_admin.screens.group.pre_order.PreOrderUiModel
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.text.DateFormat
import java.text.SimpleDateFormat

@BindingAdapter("descriptionFormatted")
fun TextView.setDescriptionFormatted(data: PreOrderUiModel?){
    data?.let {
        text = String.format("%s", data.itemDescription)
    }
}


@BindingAdapter("toolbarTitleFormatted")
fun CollapsingToolbarLayout.setToolbarTitleFormattedFromPreOrderUiModel(data: PreOrderUiModel?){
    data?.let {
        title = String.format("%s (₹%.0f/%s)", data.itemName, data.itemPrice, data.itemUom)
        setExpandedTitleColor(Color.WHITE)
    }
}

@BindingAdapter("dateFormattedPreOrderItems")
fun TextView.setDateFormattedPreOrderItems(data: AvailabilityItemsModel?){
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    val df_out: DateFormat = SimpleDateFormat("dd-MMM\nEEEE")
    data?.let {
        var date: String = df_out.format(df.parse(data.availableDate))
        text = String.format("%s", date)
    }
}

