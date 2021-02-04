package com.f2h.f2h_admin.screens.group.add_item

data class HandlingChargesItemsModel (
    var handlingOptionId: Long = 0,
    var name: String = "",
    var description: String = "",
    var handlingCharge: Double = 0.0,
    var currency: String = "",
    var isItemChecked: Boolean = false
)