package com.f2h.f2h_admin.utils

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.R
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat


@BindingAdapter("availableDateFormatted")
fun TextView.setAvailableDateFormatted(item: Item?){
    item?.let {
        val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val formatter: DateFormat = SimpleDateFormat("MMM-dd-yyy")
        var formattedDate: String = ""
        val NOT_AVAILABLE: String = "Not Available"

        try {
            var date = item.itemAvailability.get(0).availableDate
            formattedDate = formatter.format(parser.parse(date))
        }catch (e: Exception){
            println(e)
        }

        if(formattedDate.isBlank()){
            formattedDate = NOT_AVAILABLE
        }

        val finalFormattedText = SpannableString("Available On - " + formattedDate)
        if (formattedDate.equals(NOT_AVAILABLE)){
            finalFormattedText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.red_status)),
                finalFormattedText.length - formattedDate.length,
                finalFormattedText.length,
                0
            )
        } else if(item.itemAvailability[0].isFreezed?:false){
            finalFormattedText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.freeze_availability)),
                finalFormattedText.length - formattedDate.length,
                finalFormattedText.length,
                0
            )

        } else {
            finalFormattedText.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.green_status)),
                finalFormattedText.length - formattedDate.length,
                finalFormattedText.length,
                0
            )
        }

        text = finalFormattedText
    }
}

