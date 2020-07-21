package com.f2h.f2h_admin.screens.group.pre_order

data class AvailabilityItemsModel (
    var itemAvailabilityId: Long = -1,
    var availableDate: String = "",
    var availableTimeSlot: String = "",
    var itemUom: String = "",
    var committedQuantity: String = "0.0",
    var availableQuantity: String = "0.0"
)
