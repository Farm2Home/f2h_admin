package com.f2h.f2h_admin.screens.group.members

data class MembersUiModel (
    var userId: Long = 0,
    var userName: String = "",
    var deliveryAddress: String = "",
    var mobile: String = "",
    var email: String = "",
    var roles: String = ""
)
