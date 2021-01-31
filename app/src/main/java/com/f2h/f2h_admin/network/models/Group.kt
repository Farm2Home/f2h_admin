package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Group (
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "owner_user_id") val ownerUserId: Long? = -1L,
    @Json(name = "group_name") val groupName: String? = "",
    @Json(name = "currency") val groupCurrency: String? = "",
    @Json(name = "image_link") val imageLink: String? = "",
    @Json(name = "description") val description: String? = ""
)
