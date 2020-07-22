package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class ItemAvailability (
    @Json(name = "item_id") val itemId: Long? = -1,
    @Json(name = "item_availability_id") val itemAvailabilityId: Long? = -1L,
    @Json(name = "available_date") val availableDate: String? = "",
    @Json(name = "available_time_slot") val availableTimeSlot: String? = "",
    @Json(name = "committed_quantity") val committedQuantity: Double? = 0.0,
    @Json(name = "available_quantity") val availableQuantity: Double? = 0.0,
    @Json(name = "is_freezed") val isFreezed: Boolean? = false
)

data class ItemAvailabilityCreateRequest (
    @Json(name = "item_id") val itemId: Long? = -1,
    @Json(name = "available_date") val availableDate: String?,
    @Json(name = "available_time_slot") val availableTimeSlot: String?,
    @Json(name = "committed_quantity") val committedQuantity: Double?,
    @Json(name = "available_quantity") val availableQuantity: Double?,
    @Json(name = "is_freezed") val isFreezed: Boolean?,
    @Json(name = "repeat_day") val repeatDay: Long?,
    @Json(name = "created_by") val createdBy: String?,
    @Json(name = "updated_by") val updatedBy: String?
)

data class ItemAvailabilityUpdateRequest (
    @Json(name = "item_availability_id") val itemAvailabilityId: Long?,
    @Json(name = "available_date") val availableDate: String?,
    @Json(name = "available_time_slot") val availableTimeSlot: String?,
    @Json(name = "committed_quantity") val committedQuantity: Double?,
    @Json(name = "available_quantity") val availableQuantity: Double?,
    @Json(name = "is_freezed") val isFreezed: Boolean?,
    @Json(name = "repeat_day") val repeatDay: Long?,
    @Json(name = "updated_by") val updatedBy: String?
)
