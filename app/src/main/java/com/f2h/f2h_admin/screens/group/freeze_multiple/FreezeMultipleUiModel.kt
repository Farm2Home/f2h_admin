package com.f2h.f2h_admin.screens.group.freeze_multiple

data class FreezeMultipleUiModel (
    var selectedFreezeStatus: String = "",
    var selectedStartDate: String = "",
    var selectedEndDate: String = "",
    var selectedSeller: String = "",
    var freezeStatusList: List<String> = arrayListOf(),
    var timeFilterList: List<String> = arrayListOf(),
    var sellerNameList: List<String> = arrayListOf()
)
