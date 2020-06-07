package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_REJECTED
import com.f2h.f2h_admin.screens.group.pre_order.PreOrderItemsModel
import com.f2h.f2h_admin.screens.group.pre_order.PreOrderUiModel
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.text.DateFormat
import java.text.SimpleDateFormat


@BindingAdapter("namePriceFormattedPreOrder")
fun TextView.setNamePriceFormattedFromPreOrderUiModel(data: PreOrderUiModel?){
    data?.let {
        text = String.format("%s (₹%.0f/%s)", data.itemName, data.itemPrice, data.itemUom)
    }
}


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
fun TextView.setDateFormattedPreOrderItems(data: PreOrderItemsModel?){
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    val df_out: DateFormat = SimpleDateFormat("dd-MMM\nEEEE")
    data?.let {
        var date: String = df_out.format(df.parse(data.availableDate))
        text = String.format("%s", date)
    }
}


@BindingAdapter("orderedQuantityFormattedPreOrder")
fun TextView.setOrderedQuantityFormattedPreOrder(data: PreOrderItemsModel?){
    data?.let {
        var freezeString = ""
        if (isFreezeStringDisplayed(data)){
            freezeString = "\nFreeze"
        }
        var orderedString = String.format("%s %s", getFormattedQtyNumber(data.orderedQuantity), freezeString)
        if (data.orderStatus.equals(ORDER_STATUS_CONFIRMED)){
            orderedString = String.format("%s %s",getFormattedQtyNumber(data.confirmedQuantity), freezeString)
        }
        text = orderedString
    }
}

private fun isFreezeStringDisplayed(data: PreOrderItemsModel) =
    isOrderFreezed(data) && (ORDER_STATUS_ORDERED.equals(data.orderStatus) || ORDER_STATUS_ORDERED.isBlank())


@BindingAdapter("availableQuantityFormattedPreOrder")
fun TextView.setAvailableQuantityFormattedPreOrder(data: PreOrderItemsModel?){
    data?.let {
        var availabileQuantityText = getFormattedQtyNumber(data.availableQuantity - data.quantityChange)
        var itemUom = data.itemUom
        if(data.availableQuantity - data.quantityChange > 1000){
            availabileQuantityText = "Unlimited"
            itemUom = ""
        }

        var orderedString = String.format("%s  %s", availabileQuantityText, itemUom)
        text = orderedString
    }
}


@BindingAdapter("statusFormatted")
fun TextView.setStatusFormatted(data: PreOrderItemsModel){

    var displayedStatus: String = data.orderStatus

    if (displayedStatus.isBlank()){
        text = ""
        return
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
fun Button.setButtonVisibilityFormatted(data: PreOrderItemsModel){
    isEnabled = !isOrderFreezed(data)
}


private fun getFormattedQtyNumber(number: Double): String {
    return if (number.compareTo(number.toLong()) == 0)
        String.format("%d", number.toLong())
    else
        String.format("%.2f", number)
}

private fun isOrderFreezed(data: PreOrderItemsModel) : Boolean {
    if (data.isFreezed.equals(false) &&
        (data.orderStatus.equals(ORDER_STATUS_ORDERED) ||
                data.orderStatus.isBlank())){
        return false
    }
    return true
}