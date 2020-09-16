package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json


data class OrderHeader (
    @Json(name = "order_header_id") val orderHeaderId: Long? = -1L,
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "packing_number") val packingNumber: Long? = -1L,
    @Json(name = "buyer_user_id") val buyerUserId: Long? = -1L,
    @Json(name = "delivery_slot_id") val deliverySlotId: Long? = -1L,
    @Json(name = "delivery_user_id") val deliveryUserId: Long? = -1L,
    @Json(name = "delivery_date") val deliveryDate: String? = "",
    @Json(name = "delivery_location") val deliveryLocation: String? = "",
    @Json(name = "total_farmer_amount") val totalFarmerAmount: Double? = 0.0,
    @Json(name = "total_v2_amount") val totalV2Amount: Double? = 0.0,
    @Json(name = "handling_amount") val handlingAmount: Double? = 0.0,
    @Json(name = "final_amount") val final_amount: Double? = 0.0,
    @Json(name = "orders") val orders: List<Order>? = arrayListOf(),
    @Json(name = "service_orders") val serviceOrders: List<ServiceOrder>? = arrayListOf()
)


data class OrderDeliveryAssignRequest(
    @Json(name = "order_header_id") var orderHeaderId: Long?,
    @Json(name = "delivery_user_id") var deliveryUserId: Long?
)
