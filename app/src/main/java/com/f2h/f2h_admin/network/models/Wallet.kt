package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Wallet (
    @Json(name = "wallet_id") val walletId: Long? = -1,
    @Json(name = "user_id") val userId: Long? = -1,
    @Json(name = "group_id") val groupId: Long? = -1,
    @Json(name = "balance") val balance: Double? = 0.0
)


data class WalletTransaction (
    @Json(name = "wallet_ledger_id") val walletLedgerId: Long? = -1,
    @Json(name = "transaction_date") val transactionDate: String? = "",
    @Json(name = "description") val transactionDescription: String? = "",
    @Json(name = "amount") val amount: Double? = 0.0
)

data class WalletTransactionRequest (
    @Json(name = "recipient_user_id") val recipientUserId: Long? = -1,
    @Json(name = "group_id") val groupId: Long? = -1,
    @Json(name = "order_id") val orderId: Long? = -1,
    @Json(name = "transaction_date") val transactionDate: String? = "",
    @Json(name = "description") val transactionDescription: String? = "",
    @Json(name = "amount") val amount: Double? = 0.0
)
