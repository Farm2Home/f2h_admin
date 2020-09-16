package com.f2h.f2h_admin.screens.group.assign_delivery


data class AssignDeliveryItemsModel (
    var orderHeaderId: Long = 0,
    var packingNumber: Long = 0,
    var totalNumberOfPackets: Long = 0,
    var finalAmount: Double = 0.0,
    var deliveryDate: String = "",
    var buyerName: String = "",
    var deliveryArea: String = "",
    var buyerMobile: String = "",
    var buyerUserId: Long = 0,
    var deliveryAddress: String = "",
    var isItemChecked: Boolean = false,
    var deliveryBoyId:Long = -1L,
    var deliveryBoyName: String = "",
    var orders: List<OrderUiElement> = arrayListOf()
)


data class OrderUiElement (
    var orderId: Long = 0,
    var itemName: String = "",
    var itemImageLink: String = "",
    var farmerName: String = "",
    var uom: String = "",
    var orderedQuantity: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var orderStatus: String = "",
    var numberOfPackets: Long = 0
)


data class DeliveryBoyItem (
    var id: List<Long> = listOf(),
    var name: List<String> = listOf()
)


