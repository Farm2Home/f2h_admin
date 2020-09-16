package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json


data class ServiceOrder (
    @Json(name = "service_order_id") val serviceOrderId: Long? = -1L,
    @Json(name = "name") val name: String? = "",
    @Json(name = "description") val description: String? = "",
    @Json(name = "amount") val amount: Double? = 0.0
)
