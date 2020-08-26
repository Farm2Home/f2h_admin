package com.f2h.f2h_admin.screens.group.notification

data class NotificationItemsModel (
    var notificationId: Long? = 0,
    var title: String? = "",
    var body: String? = "",
    var isSelected: Boolean = false
)