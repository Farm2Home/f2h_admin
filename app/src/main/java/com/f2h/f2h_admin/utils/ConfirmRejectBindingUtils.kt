package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_REJECTED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PAID
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PENDING
import com.f2h.f2h_admin.screens.group.confirm_reject.ConfirmRejectItemsModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("priceFormatted")
fun TextView.setPriceFormatted(data: ConfirmRejectItemsModel?){
    data?.let {
        text =  String.format("₹ %.0f /%s", data.price, data.itemUom)
    }
}


@BindingAdapter("itemDetailsFormatted")
fun TextView.setItemDetailsFormatted(data: ConfirmRejectItemsModel?){
    data?.let {
        text = String.format("%s  (%s)", data.itemName, data.sellerName )
    }
}



@BindingAdapter("orderDateFormatted")
fun TextView.setOrderDateFormatted(data: ConfirmRejectItemsModel?){
    data?.let {
        text = data.orderedDate
    }
}


@BindingAdapter("quantityChangeButtonState")
fun Button.setQuantityChangeButtonState(data: ConfirmRejectItemsModel){
    if(data.orderStatus.equals(ORDER_STATUS_DELIVERED)){
        isEnabled = false
        return
    }
    isEnabled = true
}


@BindingAdapter("confirmedQuantityFormatted")
fun TextView.setConfirmedQuantityFormatted(data: ConfirmRejectItemsModel){
    text = getFormattedQtyNumber(data.confirmedQuantity)
}


private fun getFormattedQtyNumber(number: Double?): String {
    if (number == null) return ""
    return if (number.compareTo(number.toLong()) == 0)
        String.format("%d", number.toLong())
    else
        String.format("%.2f", number)
}


@BindingAdapter("discountFormatted")
fun TextView.setDiscountFormatted(data: ConfirmRejectItemsModel){
    if (data.discountAmount > 0) {
        text = String.format("Discount  ₹%.0f", data.discountAmount)
    } else {
        text = ""
    }
}


@BindingAdapter("totalPriceFormatted")
fun TextView.setTotalPriceFormatted(data: ConfirmRejectItemsModel){

    if(data.orderAmount <= 0) {
        text = ""
        return
    }

    var markupPrice = ""
    if (data.discountAmount > 0) {
        markupPrice = String.format("₹%.0f", data.orderAmount + data.discountAmount)
    }

    val receivableString = String.format("Receivable  %s ₹%.0f \n%s", markupPrice, data.orderAmount, data.paymentStatus)
    val receivaableStringFormatted = SpannableString(receivableString)
    receivaableStringFormatted.setSpan(StrikethroughSpan(),11,12+markupPrice.length,0)
    receivaableStringFormatted.setSpan(ForegroundColorSpan(Color.parseColor("#dbdbdb")),11,12+markupPrice.length,0)
    receivaableStringFormatted.setSpan(RelativeSizeSpan(0.6F), receivableString.length-data.paymentStatus.length, receivableString.length,0)

    //Make PAID Green colour
    if(data.paymentStatus.equals(PAYMENT_STATUS_PAID)) {
        receivaableStringFormatted.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context,R.color.green_status)),
            receivableString.length - data.paymentStatus.length,
            receivableString.length,
            0
        )
    }

    //Make PENDING RED colour
    if(data.paymentStatus.equals(PAYMENT_STATUS_PENDING)) {
        receivaableStringFormatted.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context,R.color.red_status)),
            receivableString.length - data.paymentStatus.length,
            receivableString.length,
            0
        )
    }

    text = receivaableStringFormatted
}



@BindingAdapter("statusFormatted")
fun TextView.setStatusFormatted(data: ConfirmRejectItemsModel){

    var displayedStatus: String = data.orderStatus

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


private fun isOrderFreezed(data: ConfirmRejectItemsModel) : Boolean {
    if (data.isFreezed.equals(false) &&
        (data.orderStatus.equals(ORDER_STATUS_ORDERED) ||
                data.orderStatus.isBlank())){
        return false
    }
    return true
}

@BindingAdapter("commentFormatted")
fun TextView.setCommentFormatted(data: ConfirmRejectItemsModel){
    val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    val formatter: DateFormat = SimpleDateFormat("dd-MMMM, hh:mm a")
    var displayText = ""
    data.comments.sortByDescending { comment -> parser.parse(comment.createdAt) }
    data.comments.forEach { comment ->
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        var date = formatter.format(parser.parse(comment.createdAt))
        displayText = String.format("%s%s : %s - %s\n\n", displayText, date, comment.commenter, comment.comment)
    }
    text = displayText
}


@BindingAdapter("moreDetailsLayoutFormatted")
fun ConstraintLayout.setMoreDetailsLayoutFormatted(data: ConfirmRejectItemsModel){
    if(data.isMoreDetailsDisplayed){
        visibility = View.VISIBLE
        return
    }
    visibility = View.GONE
}

