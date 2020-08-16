package com.f2h.f2h_admin.utils
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.AVAILABLE_STATUS
import com.f2h.f2h_admin.constants.F2HConstants.FREEZED_STATUS
import com.f2h.f2h_admin.screens.group.freeze_multiple.FreezeMultipleItemsModel


@BindingAdapter("priceFormatted")
fun TextView.setPriceFormatted(data: FreezeMultipleItemsModel?){
    data?.let {
//        text =  String.format("₹ %.0f /%s", data.itemPrice, data.itemUom)
        text = String.format("₹ %.0f", data.itemPrice)
    }
}


@BindingAdapter("itemDetailsFormatted")
fun TextView.setItemDetailsFormatted(data: FreezeMultipleItemsModel?){
    data?.let {
        text = String.format("%s  (%s)", data.itemName, data.sellerName )
    }
}



@BindingAdapter("availableDateFormatted")
fun TextView.setAvailableDateFormatted(data: FreezeMultipleItemsModel?){
    data?.let {
        text = data.availableDate
    }
}





@BindingAdapter("confirmedQuantityFormatted")
fun TextView.setConfirmedQuantityFormatted(data: FreezeMultipleItemsModel){
    text = String.format("%s", data.committedQuantity)
}



@BindingAdapter("availableQuantityFormatted")
fun TextView.setAvailableQuantityFormatted(data: FreezeMultipleItemsModel){
    var orderedString = String.format("%s", data.availableQuantity)
    text = orderedString
}



@BindingAdapter("statusFormatted")
fun TextView.setStatusFormatted(data: FreezeMultipleItemsModel){
    var displayedStatus: String = AVAILABLE_STATUS
    var color = ContextCompat.getColor(context, R.color.green_status)
    if (data.isFreezed){
        displayedStatus = FREEZED_STATUS
        color = ContextCompat.getColor(context, R.color.orange_status)
    }
    val colouredText = SpannableString(displayedStatus)
    colouredText.setSpan(ForegroundColorSpan(color),0, displayedStatus.length,0)
    text = colouredText
}






