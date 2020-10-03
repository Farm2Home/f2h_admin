package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class GroupMembershipRequest (
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "delivery_area_id") val deliveryAreaId: Long? = -1L,
    @Json(name = "user_id") val userId: Long? = -1L,
    @Json(name = "base_delivery_charge") val baseDeliveryCharge: Double? = 0.0,
    @Json(name = "roles") val roles: String? = "",
    @Json(name = "created_by") val createdBy: String? = ""
)


data class GroupMembership (
    @Json(name = "group_membership_id") val groupMembershipId: Long? = -1L,
    @Json(name = "group_id") val groupId: Long? = -1L,
    @Json(name = "delivery_area_id") val deliveryAreaId: Long? = -1L,
    @Json(name = "user_id") val userId: Long? = -1L,
    @Json(name = "roles") val roles: String? = "",
    @Json(name = "base_delivery_charge") val baseDeliveryCharge: Double? = 0.0,
    @Json(name = "created_by") val createdBy: String? = "",
    @Json(name = "updated_by") val updatedBy: String? = "",
    @Json(name = "delivery_sequence") val deliverySequence: Long? = 0L
)

data class GroupMembershipUpdateRequest (
    @Json(name = "group_membership_id") val groupMembershipId: Long?,
    @Json(name = "delivery_sequence") val deliverySequence: Long?,
    @Json(name = "updated_by") val updatedBy: String?
)
