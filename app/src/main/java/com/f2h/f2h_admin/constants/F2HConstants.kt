package com.f2h.f2h_admin.constants

import com.f2h.f2h_admin.BuildConfig

object F2HConstants {
    const val SERVER_URL = BuildConfig.SERVER_URL

    const val ORDER_STATUS_ORDERED = "ORDERED"
    const val ORDER_STATUS_CONFIRMED = "CONFIRMED"
    const val ORDER_STATUS_REJECTED = "REJECTED"
    const val ORDER_STATUS_DELIVERED = "DELIVERED"
    const val ASSIGN_STATUS_ASSIGNED = "ASSIGNED"
    const val ASSIGN_STATUS_NOT_ASSIGNED = "NOT ASSIGNED"
    const val DELIVERY_AREA_NOT_ASSIGNED = "NOT ASSIGNED"
    const val PAYMENT_STATUS_PENDING = "PENDING"
    const val PAYMENT_STATUS_PAID = "PAID"

    const val USER_ROLE_GROUP_ADMIN_REQUESTED = "GROUP_ADMIN_REQUESTED"
    const val USER_ROLE_GROUP_ADMIN = "GROUP_ADMIN"
    const val USER_ROLE_DELIVER = "DELIVER"
    const val ROLE_REQUEST_ACCEPT = "Accept"
    const val ROLE_REQUEST_REJECT = "Reject"
    const val ROLE_REQUEST_PENDING = "Pending"

    const val REPEAT_NO_REPEAT = "No Repeat"
    const val REPEAT_WEEKLY = "Weekly"

    val ACCEPTED_ROLES = arrayOf("BUYER", "GROUP_ADMIN", "FARMER", "DELIVER")
    val REQUESTED_ROLES = arrayOf("BUYER_REQUESTED", "GROUP_ADMIN_REQUESTED", "FARMER_REQUESTED", "DELIVER_REQUESTED")
    val REQUESTED_ROLE_TO_ACCEPTED_ROLE:
            Map<String, String> = REQUESTED_ROLES.zip(ACCEPTED_ROLES).toMap()

}