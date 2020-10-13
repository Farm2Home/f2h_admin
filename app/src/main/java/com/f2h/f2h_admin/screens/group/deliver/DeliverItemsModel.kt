package com.f2h.f2h_admin.screens.group.deliver

import com.f2h.f2h_admin.network.models.Comment


data class DeliverItemsModel (
    var itemId: Long = 0,
    var orderedDate: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var buyerMobile: String = "",
    var sellerMobile: String = "",
    var buyerName: String = "",
    var buyerUserId: Long = 0,
    var sellerUserId: Long = 0,
    var sellerName: String= "",
    var price: Double = 0.0,
    var itemUom: String = "",
    var itemImageLink: String = "",
    var orderId: Long = 0,
    var orderedQuantity: Double = 0.0,
    var confirmedQuantity: Double = 0.0,
    var displayQuantity: Double = 0.0,
    var orderAmount: Double = 0.0,
    var orderStatus: String = "",
    var paymentStatus: String = "",
    var deliveryAddress: String = "",
    var discountAmount: Double = 0.0,
    var isItemChecked: Boolean = true,
    var isMoreDetailsDisplayed: Boolean = false,
    var comments: ArrayList<Comment> = arrayListOf(),
    var newComment: String = "",
    var isCommentProgressBarActive: Boolean = false,
    var receivedPacketCount: Long = 0,
    var isReceived: Boolean = false,
    var packetCount: Long = 1,
    var orderDescription: String = ""
)


