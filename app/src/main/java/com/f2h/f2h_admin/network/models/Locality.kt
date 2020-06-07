package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class Locality (
    @Json(name = "locality_id") val localityId: Long? = -1,
    @Json(name = "locality") val locality: String? = "",
    @Json(name = "description") val description: String? = ""
)

