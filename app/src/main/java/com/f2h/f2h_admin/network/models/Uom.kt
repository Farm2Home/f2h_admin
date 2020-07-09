package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Uom (
    @Json(name = "uom_id") var uomId: Long? = -1,
    @Json(name = "description") var description: String? = "",
    @Json(name = "uom") var uom: String? = ""
)
