package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class DeliveryArea (
    @Json(name = "delivery_area_id") val deliveryAreaId: Long? = -1L,
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "delivery_area") val deliveryArea: String? = "",
    @Json(name = "created_by") val createdBy: String? = "",
    @Json(name = "updated_by") val updatedBy: String? = ""
)