package com.f2h.f2h_admin.screens.group.accept_reject_membership
import com.f2h.f2h_admin.constants.F2HConstants.ROLE_REQUEST_ACCEPT
import com.f2h.f2h_admin.constants.F2HConstants.ROLE_REQUEST_PENDING
import com.f2h.f2h_admin.constants.F2HConstants.ROLE_REQUEST_REJECT

data class MembershipRequestUiModel(
    var role: String = "",
    var requestedRole: Boolean = false,
    var action: List<String> = listOf<String>(ROLE_REQUEST_PENDING, ROLE_REQUEST_ACCEPT, ROLE_REQUEST_REJECT),
    var selected: String = ROLE_REQUEST_PENDING
)

data class DeliveryAreaItem (
    var id: List<Long> = listOf(),
    var name: List<String> = listOf()
)
