package com.f2h.f2h_admin.screens.search_group

data class SearchGroupsItemsModel (
    var groupId: Long = 0,
    var ownerUserId: Long = 0,
    var groupName: String = "",
    var description: String = "",
    var isAlreadyMember: Boolean = false
)