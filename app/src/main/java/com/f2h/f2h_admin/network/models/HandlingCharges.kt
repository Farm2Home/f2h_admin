package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class HandlingChargesCreateRequest (
    @Json(name = "item_id") var itemId: Long = -1,
    @Json(name = "group_id") var groupId: Long? = -1,
    @Json(name = "handling_option_id") var handlingOptionId: Long = -1,
    @Json(name = "amount") var amount: Double = 0.0,
    @Json(name = "user_visibility") var userVisibility: Boolean = true,
    @Json(name = "created_by") var createdBy: String = "",
    @Json(name = "updated_by") var updatedBy: String = ""
)


data class HandlingChargesUpdateRequest (
    @Json(name = "handling_charge_id") var handlingChargeId: Long = -1,
    @Json(name = "handling_option_id") var handlingOptionId: Long = -1,
    @Json(name = "item_id") var itemId: Long = -1,
    @Json(name = "group_id") var groupId: Long = -1,
    @Json(name = "description") var description: String = "",
    @Json(name = "name") var name: String = "",
    @Json(name = "amount") var amount: Double = 0.0,
    @Json(name = "user_visibility") var userVisibility: Boolean = true,
    @Json(name = "updated_by") var updatedBy: String = ""
)

data class HandlingCharge (
    @Json(name = "handling_charge_id") var handlingChargeId: Long = -1,
    @Json(name = "handling_option_id") var handlingOptionId: Long = -1,
    @Json(name = "item_id") var itemId: Long = -1,
    @Json(name = "group_id") var groupId: Long = -1,
    @Json(name = "description") var description: String = "",
    @Json(name = "name") var name: String = "",
    @Json(name = "amount") var amount: Double = 0.0,
    @Json(name = "user_visibility") var userVisibility: Boolean = true
)
