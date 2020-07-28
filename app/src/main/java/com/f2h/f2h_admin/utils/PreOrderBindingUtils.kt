package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.REPEAT_NO_REPEAT
import com.f2h.f2h_admin.constants.F2HConstants.REPEAT_WEEKLY
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
    val df_out: DateFormat = SimpleDateFormat("dd-MMM, EEEE")
    data?.let {
        var date: String = df_out.format(df.parse(data.availableDate))
        text = String.format("%s", date)
    }
}

@BindingAdapter("freezeTextFormatted")
fun TextView.setFreezeTextFormatted(data: AvailabilityItemsModel?){
    var freezeText = ""
    data?.let {
        if (data.isFreezed) {
            freezeText = "Freeze"
        }
    }
    val colouredText = SpannableString(freezeText)
    var color = ContextCompat.getColor(context, R.color.orange_status)
    colouredText.setSpan(ForegroundColorSpan(color),0, freezeText.length,0)
    text = colouredText
}

@BindingAdapter("repeatTextFormatted")
fun TextView.setRepeatTextFormatted(data: AvailabilityItemsModel?){
    var repeatText = ""
    data?.let {
        if (data.repeatDay.equals(0L)){
            repeatText = REPEAT_NO_REPEAT
        }
        if (data.repeatDay.equals(7L)){
            repeatText = REPEAT_WEEKLY
        }
    }
    val colouredText = SpannableString(repeatText)
    var color = ContextCompat.getColor(context, R.color.blue_status)
    colouredText.setSpan(ForegroundColorSpan(color),0, repeatText.length,0)
    text = colouredText
}