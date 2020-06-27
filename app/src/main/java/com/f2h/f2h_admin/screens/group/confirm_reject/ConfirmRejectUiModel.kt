package com.f2h.f2h_admin.screens.group.confirm_reject

data class ConfirmRejectUiModel (
    var selectedItem: String = "",
    var selectedOrderStatus: String = "",
    var selectedPaymentStatus: String = "",
    var selectedStartDate: String = "",
    var selectedEndDate: String = "",
    var selectedBuyer: String = "",
    var selectedFarmer: String = "",
    var itemList: List<String> = arrayListOf(),
    var orderStatusList: List<String> = arrayListOf(),
    var paymentStatusList: List<String> = arrayListOf(),
    var timeFilterList: List<String> = arrayListOf(),
    var buyerNameList: List<String> = arrayListOf(),
    var farmerNameList: List<String> = arrayListOf()
)
