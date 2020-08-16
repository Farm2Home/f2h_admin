package com.f2h.f2h_admin.screens.group.freeze_multiple

data class FreezeMultipleItemsModel (
    var itemId: Long = 0,
    var availabilityId: Long = 0,
    var availableDate: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var sellerName: String= "",
    var sellerMobile: String = "",
    var sellerUserId: Long = 0,
    var itemImageLink: String = "",
    var itemPrice: Double = 0.0,
    var isItemChecked: Boolean = false,
    var availableQuantity: Double = 0.0,
    var isFreezed: Boolean = false,
    var repeatDay: Long = 0,
    var availableTimeSlot: String = "",
    var committedQuantity: Double = 0.0
)