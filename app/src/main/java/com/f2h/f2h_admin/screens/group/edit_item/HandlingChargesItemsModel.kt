package com.f2h.f2h_admin.screens.group.edit_item

data class HandlingChargesItemsModel (
    var handlingOptionId: Long = 0,
    var handlingChargeId: Long = 0,
    var name: String = "",
    var description: String = "",
    var handlingCharge: Double = 0.0,
    var isItemChecked: Boolean = false
)