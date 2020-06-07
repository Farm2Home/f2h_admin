package com.f2h.f2h_admin.screens.group.daily_orders

data class DailyOrdersUiModel (
    var itemId: Long = 0,
    var orderedDate: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var farmerName: String = "",
    var price: Double = 0.0,
    var itemUom: String = "",
    var orderId: Long = 0,
    var quantityChange: Double = 0.0,
    var orderedQuantity: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var availableQuantity: Double = 0.0,
    var orderQtyJump: Double = 0.0,
    var orderAmount: Double = 0.0,
    var orderStatus: String = "",
    var orderComment: String = "",
    var deliveryComment: String = "",
    var paymentStatus: String = "",
    var isFreezed: Boolean = false,
    var discountAmount: Double = 0.0
)
