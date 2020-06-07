package com.f2h.f2h_admin.utils

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.screens.group.group_wallet.WalletItemsModel
import com.f2h.f2h_admin.screens.report.ReportItemsModel
import java.text.DateFormat
import java.text.SimpleDateFormat


@BindingAdapter("walletBalanceFormatted")
fun TextView.setWalletBalanceFormatted(balance: Double?) {
    text = String.format("Balance : ₹%.0f", balance)
}


@BindingAdapter("transactionAmountFormatted")
fun TextView.setPriceFormatted(data: WalletItemsModel?){
    data?.let {
        val colouredText = SpannableString(String.format("₹ %.0f", data.amount))

        if (data.amount > 0){
            colouredText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.green_status)),0, colouredText.length,0)
        }

        if (data.amount < 0){
            colouredText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red_status)),0, colouredText.length,0)
        }

        text = colouredText
    }
}


@BindingAdapter("transactionDateFormatted")
fun TextView.settransactionDateFormatted(data: WalletItemsModel?) {
    data?.let {
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val df_out: DateFormat = SimpleDateFormat("dd-MMM-EEEE")
        if (data.transactionDate.isNullOrBlank()) {
            text = ""
            return
        }

        var formattedDate: String = df_out.format(df.parse(data.transactionDate))
        text = String.format("%s", formattedDate)
    }

}

