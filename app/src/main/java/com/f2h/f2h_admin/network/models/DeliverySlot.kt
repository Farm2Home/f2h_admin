package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class DeliverySlot (
    @Json(name = "delivery_slot_id") val deliverySlotId: Long? = -1L,
    @Json(name = "name") val name: String? = "",
    @Json(name = "description") val description: String? = ""
)