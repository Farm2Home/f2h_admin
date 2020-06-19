package com.f2h.f2h_admin.screens.group.confirm_reject


data class ConfirmRejectItemsModel (
    var itemId: Long = 0,
    var orderedDate: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var buyerName: String = "",
    var sellerName: String= "",
    var buyerUserId: Long = 0,
    var sellerUserId: Long = 0,
    var price: Double = 0.0,
    var itemUom: String = "",
    var itemImageLink: String = "",
    var orderId: Long = 0,
    var orderedQuantity: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var confirmedQuantityJump: Double = 0.0,
    var quantityChange: Double = 0.0,
    var availableQuantity: Double = 0.0,
    var displayQuantity: Double = 0.0,
    var orderAmount: Double = 0.0,
    var orderStatus: String = "",
    var orderComment: String = "",
    var paymentStatus: String = "",
    var deliveryAddress: String = "",
    var isFreezed: Boolean = false,
    var discountAmount: Double = 0.0,
    var isItemChecked: Boolean = false
)


