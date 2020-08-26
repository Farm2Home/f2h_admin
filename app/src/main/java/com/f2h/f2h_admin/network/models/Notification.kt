package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Notification (
    @Json(name = "notification_id") val notificationId: Long? = -1,
    @Json(name = "title") val title: String? = "",
    @Json(name = "body") val body: String? = "",
    @Json(name = "created_by") var createdBy: String?,
    @Json(name = "created_at") var createdAt: String?,
    @Json(name = "updated_by") var updatedBy: String,
    @Json(name = "updated_at") var updatedAt: String
)


