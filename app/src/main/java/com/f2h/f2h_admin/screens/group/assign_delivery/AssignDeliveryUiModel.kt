package com.f2h.f2h_admin.screens.group.assign_delivery


data class AssignDeliveryUiModel (
    var selectedAssignStatus: String = "",
    var selectedDeliveryArea: String = "",
    var selectedStartDate: String = "",
    var selectedEndDate: String = "",
    var selectedBuyer: String = "",
    var selectedDeliveryBoy: Long = 0,
    var assignStatusList: List<String> = arrayListOf(),
    var deliveryAreaList: List<String> = arrayListOf(),
    var timeFilterList: List<String> = arrayListOf(),
    var buyerNameList: List<String> = arrayListOf(),
    var deliveryBoyNameList: List<String> = arrayListOf(),
    var deliveryBoyIdList: List<Long> = arrayListOf()
)
