package com.f2h.f2h_admin.utils

import android.view.View
import android.widget.*
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.network.models.Item
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val utcFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

fun fetchOrderDate(dateOffset: Int): String {
    var date = Calendar.getInstance()
    date.add(Calendar.DATE, dateOffset)
    return utcFormatter.format(date.time)
}

