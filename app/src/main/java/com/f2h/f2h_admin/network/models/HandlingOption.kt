package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class HandlingOption (
    @Json(name = "handling_option_id") var handlingOptionId: Long = -1,
    @Json(name = "description") var description: String = "",
    @Json(name = "name") var name: String = ""
)
