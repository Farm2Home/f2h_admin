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
