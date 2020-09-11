package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Item (
    @Json(name = "item_id") var itemId: Long? = -1,
    @Json(name = "group_id") var groupId: Long? = -1,
    @Json(name = "farmer_user_id") var farmerUserId: Long? = -1,
    @Json(name = "farmer_user_name") var farmerUserName: String? = "",
    @Json(name = "image_link") var imageLink: String? = "",
    @Json(name = "item_name") var itemName: String? = "",
    @Json(name = "description") var description: String? = "",
    @Json(name = "uom") var uom: String? = "",
    @Json(name = "price_per_unit") var pricePerUnit: Double? = 0.0,
    @Json(name = "confirm_qty_jump") var confirmQtyJump: Double? = 0.0,
    @Json(name = "order_qty_jump") var orderQtyJump: Double? = 0.0,
    @Json(name = "item_availability") var itemAvailability: List<ItemAvailability> = listOf()
)


data class ItemCreateRequest (
    @Json(name = "item_name") var itemName: String?,
    @Json(name = "group_id") var groupId: Long?,
    @Json(name = "farmer_user_id") var farmerUserId: Long?,
    @Json(name = "farmer_user_name") var farmerUserName: String?,
    @Json(name = "image_link") var imageLink: String?,
    @Json(name = "description") var description: String?,
    @Json(name = "uom") var uom: String?,
    @Json(name = "farmer_price") var farmerPrice: Double?,
    @Json(name = "v2_price") var v2Price: Double?,
    @Json(name = "confirm_qty_jump") var confirmQtyJump: Double?,
    @Json(name = "order_qty_jump") var orderQtyJump: Double?,
    @Json(name = "created_by") var createdBy: String?,
    @Json(name = "updated_by") var updatedBy: String?,
    @Json(name = "handling_charges") var handlingCharges: List<HandlingChargesCreateRequest>
)


data class ItemUpdateRequest (
    @Json(name = "item_name") var itemName: String?,
    @Json(name = "group_id") var groupId: Long?,
    @Json(name = "farmer_user_id") var farmerUserId: Long?,
    @Json(name = "farmer_user_name") var farmerUserName: String?,
    @Json(name = "image_link") var imageLink: String?,
    @Json(name = "description") var description: String?,
    @Json(name = "uom") var uom: String?,
    @Json(name = "price_per_unit") var pricePerUnit: Double?,
    @Json(name = "confirm_qty_jump") var confirmQtyJump: Double?,
    @Json(name = "order_qty_jump") var orderQtyJump: Double?,
    @Json(name = "updated_by") var updatedBy: String?
//    @Json(name = "handling_charges") var handlingCharges: List<HandlingChargesUpdateRequest>
)
