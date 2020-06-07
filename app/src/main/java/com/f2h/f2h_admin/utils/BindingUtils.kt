package com.f2h.f2h_admin.utils

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.network.models.Item


@BindingAdapter("priceFormattedFromItem")
fun TextView.setPriceFormattedFromItem(data: Item?){
    data?.let {
        text = "\u20B9 " + String.format("%.0f", data.pricePerUnit) + "/" + data.uom
    }
}


@BindingAdapter("customImageFormatted")
fun ImageView.setCustomImageFormatted(itemId: Long){
    var wvItems = arrayListOf<Long>(138,139,153,154,1065,1067,1069)
    var wvTonedMilk = arrayListOf<Long>(147,1166,1168,1170)
    var egg = arrayListOf<Long>(176,1900)
    var pavakka = arrayListOf<Long>(1901)
    var beans = arrayListOf<Long>(1902)
    var pappaya = arrayListOf<Long>(1903)
    var fish = arrayListOf<Long>(1904)

    if (wvItems.contains(itemId)) {
        setImageResource(R.drawable.wv_milk)
        return
    }

    if (wvTonedMilk.contains(itemId)) {
        setImageResource(R.drawable.toned_milk)
        return
    }

    if (egg.contains(itemId)) {
        setImageResource(R.drawable.egg)
        return
    }

    if (pavakka.contains(itemId)) {
        setImageResource(R.drawable.item_1901)
        return
    }

    if (beans.contains(itemId)) {
        setImageResource(R.drawable.item_1902)
        return
    }

    if (pappaya.contains(itemId)) {
        setImageResource(R.drawable.item_1903)
        return
    }

    if (fish.contains(itemId)) {
        setImageResource(R.drawable.item_1904)
        return
    }

    setImageResource(R.drawable.main_logo)
}

@BindingAdapter("buttonStatus")
fun Button.setButtonStatus(status: Boolean){
    isEnabled = status
}
