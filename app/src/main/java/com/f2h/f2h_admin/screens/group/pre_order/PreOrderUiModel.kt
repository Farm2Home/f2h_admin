package com.f2h.f2h_admin.screens.group.pre_order

import java.util.*


data class PreOrderUiModel (
    var itemId: Long = -1,
    var itemName: String = "",
    var currency: String = "",
    var itemDescription: String = "",
    var itemUom: String = "",
    var itemImageLink: String = "",
    var itemPrice: Double = 0.0,
    var farmerName: String = ""
)
