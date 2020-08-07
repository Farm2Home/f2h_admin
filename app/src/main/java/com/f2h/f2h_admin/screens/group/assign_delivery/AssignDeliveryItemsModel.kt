package com.f2h.f2h_admin.screens.group.assign_delivery


data class AssignDeliveryItemsModel (
    var itemId: Long = 0,
    var orderedDate: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var buyerName: String = "",
    var deliveryArea: String = "",
    var sellerName: String= "",
    var buyerMobile: String = "",
    var sellerMobile: String = "",
    var buyerUserId: Long = 0,
    var sellerUserId: Long = 0,
    var price: Double = 0.0,
    var itemUom: String = "",
    var itemImageLink: String = "",
    var orderId: Long = 0,
    var orderedQuantity: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var orderAmount: Double = 0.0,
    var orderStatus: String = "",
    var paymentStatus: String = "",
    var deliveryAddress: String = "",
    var discountAmount: Double = 0.0,
    var isItemChecked: Boolean = false,
    var deliveryBoyId:Long = -1L
)

data class DeliveryBoyItem (
    var id: List<Long> = listOf(),
    var name: List<String> = listOf()
)


