package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json
import java.util.*


data class Order (
    @Json(name = "order_id") val orderId: Long? = -1L,
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "seller_user_id") val sellerUserId: Long? = -1L,
    @Json(name = "buyer_user_id") var buyerUserId: Long? = -1L,
    @Json(name = "item_availability_id") val itemAvailabilityId: Long? = -1L,
    @Json(name = "order_description") val orderDescription: String? = "",
    @Json(name = "delivery_location") var deliveryLocation: String? = "",
    @Json(name = "ordered_quantity") val orderedQuantity: Double? = 0.0,
    @Json(name = "confirmed_quantity") val confirmedQuantity: Double? = 0.0,
    @Json(name = "ordered_amount") val orderedAmount: Double? = 0.0,
    @Json(name = "farmer_amount") val farmerAmount: Double? = 0.0,
    @Json(name = "v2_amount") val v2Amount: Double? = 0.0,
    @Json(name = "discount_amount") val discountAmount: Double? = 0.0,
    @Json(name = "order_status") val orderStatus: String? = "",
    @Json(name = "payment_status") val paymentStatus: String? = "",
    @Json(name = "delivery_comment") val deliveryComment: String? = "",
    @Json(name = "order_comment") val orderComment: String? = "",
    @Json(name = "delivered_date") val deliveredDate: String? = "",
    @Json(name = "delivery_time_slot")  val deliveryTimeSlot: String? = "",
    @Json(name = "ordered_date") val orderedDate: String? = "",
    @Json(name = "created_by") val createdBy: String? = "",
    @Json(name = "updated_by") val updatedBy: String? = "",
    @Json(name= "delivery_user_id") val deliveryUserId: Long? = -1L,
    @Json(name = "number_of_packets") val numberOfPackets: Long? = 0L,
    @Json(name = "received_number_of_packets") val receivedNumberOfPackets: Long? = -1
)


data class OrderUpdateRequest (
    @Json(name = "order_id") var orderId: Long?,
    @Json(name = "order_status") var orderStatus: String?,
    @Json(name = "payment_status") var paymentStatus: String?,
    @Json(name = "order_comment") var orderComment: String?,
    @Json(name = "order_description") val orderDescription: String? = "",
    @Json(name = "delivery_comment") var deliveryComment: String?,
    @Json(name = "ordered_quantity") var orderedQuantity: Double?,
    @Json(name = "confirmed_quantity") var confirmedQuantity: Double?,
    @Json(name = "discount_amount") var discountAmount: Double?,
    @Json(name = "ordered_amount") var orderedAmount: Double?
)

data class OrderReceivedNumberUpdateRequest (
    @Json(name = "order_id") var orderId: Long?,
    @Json(name = "received_number_of_packets") var receivedNumberOfPackets: Long?
)

data class OrderCreateRequest (
    @Json(name = "buyer_user_id") var buyerUserId: Long?,
    @Json(name = "item_availability_id") var itemAvailabilityId: Long?,
    @Json(name = "order_description") var orderDescription: String?,
    @Json(name = "delivery_location") var deliveryLocation: String?,
    @Json(name = "ordered_quantity") var orderedQuantity: Double?,
    @Json(name = "ordered_amount") var orderedAmount: Double?,
    @Json(name = "discount_amount") var discountAmount: Double?,
    @Json(name = "order_status") var orderStatus: String?,
    @Json(name = "payment_status") var paymentStatus: String?,
    @Json(name = "created_by") var createdBy: String?,
    @Json(name = "updated_by") var updatedBy: String?
)


data class OrderHeaderDeliveryRequest (
    @Json(name = "order_header_id") var orderHeaderId: Long?,
    @Json(name = "collected_cash") var collectedCash: Double?,
    @Json(name = "wallet_id") var walletId: Long?,
    @Json(name = "buyer_id") var buyerId: Long?,
    @Json(name = "group_id") var groupId: Long?,
    @Json(name = "buyer_name") var buyerName: String?,
    @Json(name = "delivery_date") var deliveryDate: String?,
    @Json(name = "orders") var orders: List<OrderUpdateRequest>?,
    @Json(name = "updated_by") var updatedBy: String?
)
