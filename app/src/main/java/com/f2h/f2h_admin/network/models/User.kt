package com.f2h.f2h_admin.network.models

import com.squareup.moshi.Json

data class User (
    @Json(name = "user_id") val userId: Long? = -1,
    @Json(name = "user_name") val userName: String? = "",
    @Json(name = "address") val address: String? = "",
    @Json(name = "email") val email: String? = "",
    @Json(name = "mobile") val mobile: String? = "",
    @Json(name = "password") val password: String? = ""
)

data class UserDetails (
    @Json(name = "user_id") val userId: Long? = -1,
    @Json(name = "user_name") val userName: String? = "",
    @Json(name = "address") val address: String? = "",
    @Json(name = "email") val email: String? = "",
    @Json(name = "mobile") val mobile: String? = ""
)

data class UserCreateRequest (
    @Json(name = "user_name") var userName: String? = "",
    @Json(name = "address") var address: String? = "",
    @Json(name = "email") var email: String? = "",
    @Json(name = "mobile") var mobile: String? = "",
    @Json(name = "password") var password: String? = "",
    @Json(name = "created_by") var createdBy: String? = "",
    @Json(name = "updated_by") var updatedBy: String? = ""
)