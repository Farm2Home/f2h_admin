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


@BindingAdapter("priceFormattedFromItem")
fun TextView.setPriceFormattedFromItem(data: Item?){
    data?.let {
        text = "\u20B9 " + String.format("%.0f", data.pricePerUnit) + "/" + data.uom
    }
}


@BindingAdapter("buttonStatus")
fun Button.setButtonStatus(status: Boolean){
    isEnabled = status
}

@BindingAdapter("radioButtonState")
fun RadioButton.setButtonStatus(status: Boolean){
    isChecked = status
}

@BindingAdapter("imageButtonStatus")
fun ImageButton.setImageButtonStatus(status: Boolean){
    isEnabled = status
}

@BindingAdapter("spinnerStatus")
fun Spinner.setSpinnerStatus(status: Boolean){
    isEnabled = status
}

@BindingAdapter("progressBarVisibility")
fun ProgressBar.setProgressBarVisibility(isVisible: Boolean){
    if (isVisible){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}


@BindingAdapter("loadSquareRoundedImage")
fun loadSquareRoundedImage(view: ImageView, url: String?) {
    Glide.with(view)
        .load(url ?: "")
        .transform(CenterCrop(), RoundedCorners(25))
        .into(view)
}


@BindingAdapter("loadImage")
fun loadImage(view: ImageView, url: String?) {
    Glide.with(view)
        .load(url ?: "")
        .apply(RequestOptions()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image))
        .centerCrop()
        .into(view)
}

@BindingAdapter("loadGroupImage")
fun loadGroupImage(view: ImageView, url: String?) {
    Glide.with(view)
        .load(url ?: "")
        .apply(RequestOptions()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image))
        .circleCrop()
        .into(view)
}
