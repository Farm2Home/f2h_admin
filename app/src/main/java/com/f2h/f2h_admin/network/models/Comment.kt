package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Comment (
    @Json(name = "comment_id") val commentId: Long? = -1,
    @Json(name = "comment") val comment: String? = "",
    @Json(name = "commenter") val commenter: String? = "",
    @Json(name = "commenter_user_id") val commenterUserId: Long? = -1,
    @Json(name = "order_id") val orderId: Long? = -1,
    @Json(name = "created_by") var createdBy: String?,
    @Json(name = "created_at") var createdAt: String?
)


data class CommentCreateRequest (
    @Json(name = "comment") val comment: String,
    @Json(name = "commenter") val commenter: String,
    @Json(name = "commenter_user_id") val commenterUserId: Long,
    @Json(name = "order_id") val orderId: Long,
    @Json(name = "created_by") var createdBy: String,
    @Json(name = "updated_by") var updatedBy: String
)

