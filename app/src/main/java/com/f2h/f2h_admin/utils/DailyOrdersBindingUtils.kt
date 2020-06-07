package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_REJECTED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PAID
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PENDING
import com.f2h.f2h_admin.screens.group.daily_orders.DailyOrdersUiModel


@BindingAdapter("priceFormatted")
fun TextView.setPriceFormatted(data: DailyOrdersUiModel?){
    data?.let {
        text = "₹ " + String.format("%.0f", data.price) + "/" + data.itemUom
    }
}


@BindingAdapter("orderedQuantityFormatted")
fun TextView.setOrderedQuantityFormatted(data: DailyOrdersUiModel){
    var freezeString = ""

    if (isFreezeStringDisplayed(data)){
        freezeString = "\nFreeze"
    }

    var orderedString = String.format("%s  %s", getFormattedQtyNumber(data.orderedQuantity), freezeString)
    if (data.orderStatus.equals(ORDER_STATUS_CONFIRMED)){
        orderedString = String.format("%s  %s",getFormattedQtyNumber(data.confirmedQuantity), freezeString)
    }

    text = orderedString
}

private fun isFreezeStringDisplayed(data: DailyOrdersUiModel) =
    isOrderFreezed(data) && (ORDER_STATUS_ORDERED.equals(data.orderStatus) || data.orderStatus.isBlank())

private fun getFormattedQtyNumber(number: Double): String {
    return if (number.compareTo(number.toLong()) == 0)
        String.format("%d", number.toLong())
    else
        String.format("%.2f", number)
}


@BindingAdapter("discountFormatted")
fun TextView.setDiscountFormatted(data: DailyOrdersUiModel){
    if (data.discountAmount > 0) {
        text = String.format("Discount  ₹%.0f", data.discountAmount)
    } else {
        text = ""
    }
}


@BindingAdapter("commentFormatted")
fun TextView.setCommentFormatted(data: DailyOrdersUiModel){
    var comment = data.orderComment
    if(data.orderStatus.equals(ORDER_STATUS_DELIVERED)){
        comment = data.deliveryComment
    }

    text = comment
}



@BindingAdapter("totalPriceFormatted")
fun TextView.setTotalPriceFormatted(data: DailyOrdersUiModel){

    if(data.orderAmount <= 0) {
        text = ""
        return
    }

    var markupPrice = ""
    if (data.discountAmount > 0) {
        markupPrice = String.format("₹%.0f", data.orderAmount + data.discountAmount)
    }

    val payableString = String.format("Payable  %s ₹%.0f \n%s", markupPrice, data.orderAmount, data.paymentStatus)
    val payableStringFormatted = SpannableString(payableString)
    payableStringFormatted.setSpan(StrikethroughSpan(),9,10+markupPrice.length,0)
    payableStringFormatted.setSpan(ForegroundColorSpan(Color.parseColor("#dbdbdb")),9,10+markupPrice.length,0)
    payableStringFormatted.setSpan(RelativeSizeSpan(0.6F), payableString.length-data.paymentStatus.length, payableString.length,0)

    //Make PAID Green colour
    if(data.paymentStatus.equals(PAYMENT_STATUS_PAID)) {
        payableStringFormatted.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context,R.color.green_status)),
            payableString.length - data.paymentStatus.length,
            payableString.length,
            0
        )
    }

    //Make PENDING RED colour
    if(data.paymentStatus.equals(PAYMENT_STATUS_PENDING)) {
        payableStringFormatted.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context,R.color.red_status)),
            payableString.length - data.paymentStatus.length,
            payableString.length,
            0
        )
    }


    text = payableStringFormatted
}



@BindingAdapter("totalAmountFormatted")
fun TextView.setTotalAmountFormatted(list: List<DailyOrdersUiModel>?){
    if (list != null) {
        var totalAmount = (0).toDouble()
        list.forEach { element ->
            totalAmount += (element.orderAmount)
        }
        text = String.format("₹%.0f", totalAmount)
    }
}


@BindingAdapter("textVisibility")
fun TextView.setNoOrdersVisibility(list: List<DailyOrdersUiModel>?){
    if (list.isNullOrEmpty()) {
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}


@BindingAdapter("statusFormatted")
fun TextView.setStatusFormatted(data: DailyOrdersUiModel){

    var displayedStatus: String = data.orderStatus

    if (data.orderStatus.equals(ORDER_STATUS_CONFIRMED)){
        displayedStatus = data.orderStatus
    }

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



@BindingAdapter("buttonVisibilityFormatted")
fun Button.setButtonVisibilityFormatted(data: DailyOrdersUiModel){
    isEnabled = !isOrderFreezed(data)
}


private fun isOrderFreezed(data: DailyOrdersUiModel) : Boolean {
    if (data.isFreezed.equals(false) &&
        (data.orderStatus.equals(ORDER_STATUS_ORDERED) ||
                data.orderStatus.isBlank())){
        return false
    }
    return true
}