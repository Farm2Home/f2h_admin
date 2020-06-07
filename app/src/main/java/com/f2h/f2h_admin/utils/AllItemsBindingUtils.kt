package com.f2h.f2h_admin.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.network.models.Item
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat


@BindingAdapter("availableDateFormatted")
fun TextView.setAvailableDateFormatted(item: Item?){
    item?.let {
        val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val formatter: DateFormat = SimpleDateFormat("MMM-dd-yyy")
        var formattedDate: String = ""

        try {
            var date = item.itemAvailability.get(0).availableDate
            formattedDate = formatter.format(parser.parse(date))
        }catch (e: Exception){
            println(e)
        }

        if(formattedDate.isBlank()){
            formattedDate = "Not Available"
        }
        text = "Earliest Available - " + formattedDate
    }
}

