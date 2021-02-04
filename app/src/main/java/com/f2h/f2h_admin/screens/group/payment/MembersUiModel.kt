package com.f2h.f2h_admin.screens.group.payment

import com.f2h.f2h_admin.network.models.Order
import com.f2h.f2h_admin.network.models.ServiceOrder

data class MembersUiModel (
    var userId: Long = 0,
    var orderHeaderId: Long = 0,
    var userName: String = "",
    var currency: String = "",
    var deliveryAddress: String = "",
    var mobile: String = "",
    var email: String = "",
    var anyPaymentCompletedOrder: Boolean = true,
    var anyPaymentPendingOrder: Boolean = true,
    var groupMembershipId: Long? = 0,
    var totalAmount: Double = 0.0,
    var remainingAmount: Double = 0.0,
    var amountCollected: Double = 0.0,
    var packingNumber: Long = 0,
    var deliveryDate: String = "",
    var isProgressBarActive: Boolean = false,
    var isItemsDisplayed: Boolean = false,
    var paymentItems: List<PaymentItemsModel> = arrayListOf(),
    var serviceOrder: List<ServiceOrder> = arrayListOf(),
    var walletId: Long = -1L,
    var walletBalance: Double = 0.0
)
