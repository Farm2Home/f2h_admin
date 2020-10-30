package com.f2h.f2h_admin.screens.group.deliver

import com.f2h.f2h_admin.network.models.ServiceOrder

data class MembersUiModel (
    var userId: Long = 0,
    var orderHeaderId: Long = 0,
    var userName: String = "",
    var deliveryAddress: String = "",
    var deliveryArea: String = "",
    var mobile: String = "",
    var email: String = "",
    var anyDeliveredOrder: Boolean = true,
    var anyOpenOrder: Boolean = true,
    var deliverySequence: Long = 0,
    var groupMembershipId: Long? = 0,
    var totalAmount: Double = 0.0,
    var remainingAmount: Double = 0.0,
    var amountCollected: Double = 0.0,
    var packingNumber: Long = 0,
    var deliveryDate: String = "",
    var isProgressBarActive: Boolean = false,
    var isItemsDisplayed: Boolean = false,
    var deliveryItems: List<DeliverItemsModel> = arrayListOf(),
    var serviceOrder: List<ServiceOrder> = arrayListOf(),
    var walletId: Long = -1L,
    var walletBalance: Double = 0.0
)
