package com.f2h.f2h_admin.screens.group.pre_order

data class PreOrderItemsModel (
    var itemAvailabilityId: Long = -1,
    var availableDate: String = "",
    var availableTimeSlot: String = "",
    var itemUom: String = "",
    var orderId: Long = -1,
    var orderedQuantity: Double = 0.0,
    var quantityChange: Double = 0.0,
    var orderQuantityJump: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var availableQuantity: Double = 0.0,
    var orderStatus: String = "",
    var isFreezed: Boolean = false
)
