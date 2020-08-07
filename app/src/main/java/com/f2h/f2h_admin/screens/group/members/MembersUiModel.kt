package com.f2h.f2h_admin.screens.group.members

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MembersUiModel(
    var userId: Long = 0,
    var userName: String = "",
    var deliveryAddress: String = "",
    var mobile: String = "",
    var email: String = "",
    var roles: String = "",
    var groupMembershipId: Long = 0,
    var deliveryAreaId: Long = 0,
    var isBuyerRequested : Boolean = false
): Parcelable
