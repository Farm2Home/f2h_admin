package com.f2h.f2h_admin.screens.group.group_wallet

data class WalletItemsModel (
    var walletLedgerId: Long = 0,
    var transactionDate: String = "",
    var transactionDescription: String = "",
    var amount: Double = 0.0
)