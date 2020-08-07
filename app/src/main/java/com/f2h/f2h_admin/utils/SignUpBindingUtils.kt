package com.f2h.f2h_admin.utils

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("imageButtonVisibility")
fun ImageButton.setImageButtonVisibility(isVisible: Boolean){
    if (isVisible){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}

@BindingAdapter("editTextVisibility")
fun EditText.setEditTextVisibility(isVisible: Boolean){
    if (isVisible){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}


@BindingAdapter("buttonVisibility")
fun Button.setButtonVisibility(isVisible: Boolean){
    if (isVisible){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}

@BindingAdapter("textViewVisibility")
fun TextView.setEditTextVisibility(isVisible: Boolean){
    if (isVisible){
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}
