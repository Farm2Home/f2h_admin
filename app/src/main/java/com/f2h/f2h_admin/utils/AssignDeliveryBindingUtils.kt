package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_REJECTED
import com.f2h.f2h_admin.screens.group.assign_delivery.AssignDeliveryItemsModel


@BindingAdapter("priceFormatted")
fun TextView.setPriceFormatted(data: AssignDeliveryItemsModel?){
    data?.let {
//        text =  String.format("₹ %.0f /%s", data.price, data.itemUom)
        text = ""
    }
}


@BindingAdapter("buyerDetailsFormatted")
fun TextView.setBuyerDetailsFormatted(data: AssignDeliveryItemsModel?){
    var displayText = ""
    data?.let {
        displayText = String.format("%s", data.buyerName)
        if (!data.deliveryArea.isNullOrBlank()){
            displayText = String.format("%s (%s)", displayText, data.deliveryArea)
        }
    }
    text = displayText
}


@BindingAdapter("deliveryUserDetailsFormatted")
fun TextView.setDeliveryUserDetailsFormatted(data: AssignDeliveryItemsModel?){
    data?.let {
        if (data.deliveryBoyName.isNullOrBlank()){
            text = ""
            return
        }
        text = String.format("Delivery by : %s  on %s", data.deliveryBoyName, data.deliveryDate)
    }
}


@BindingAdapter("numberOfPacketsFormatted")
fun TextView.setNumberOfPacketsFormatted(data: AssignDeliveryItemsModel?){
    data?.let {
        text = String.format("₹%.0f  (%s Packets)", data.finalAmount, data.totalNumberOfPackets)
    }
}


@BindingAdapter("addressFormatted")
fun TextView.setNameFormatted(data: AssignDeliveryItemsModel){
    var address = String.format("%s", data.deliveryAddress)
    text = address
}


@BindingAdapter("statusFormatted")
fun TextView.setStatusFormatted(data: AssignDeliveryItemsModel){

//    var displayedStatus: String = data.orderStatus
    var displayedStatus = "ORDERED"

    val colouredText = SpannableString(displayedStatus)
    var color = Color.DKGRAY
    when (displayedStatus) {
        ORDER_STATUS_ORDERED -> color = ContextCompat.getColor(context, R.color.orange_status)
        ORDER_STATUS_CONFIRMED -> color = ContextCompat.getColor(context, R.color.orange_status)
        ORDER_STATUS_REJECTED -> color = ContextCompat.getColor(context, R.color.red_status)
        ORDER_STATUS_DELIVERED -> color = ContextCompat.getColor(context, R.color.green_status)
    }
    colouredText.setSpan(ForegroundColorSpan(color),0, displayedStatus.length,0)

    text = colouredText
}






