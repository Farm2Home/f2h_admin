package com.f2h.f2h_admin.screens.group.freeze_multiple

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.AVAILABLE_STATUS
import com.f2h.f2h_admin.constants.F2HConstants.FREEZED_STATUS
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FreezeMultipleViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var isAllItemsSelected: Boolean = false

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _reportUiFilterModel = MutableLiveData<FreezeMultipleUiModel>()
    val reportUiFilterModel: LiveData<FreezeMultipleUiModel>
        get() = _reportUiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<FreezeMultipleItemsModel>>()
    val visibleUiData: LiveData<MutableList<FreezeMultipleItemsModel>>
        get() = _visibleUiData

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val requestFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val sessionData = MutableLiveData<SessionEntity>()
    private var allUiData = ArrayList<FreezeMultipleItemsModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        setUpDefaultSelectedFilters()
        getAvailabilitiesForGroup()
    }

    fun getAvailabilitiesForGroup() {

        //refresh data
        _isProgressBarActive.value = true
        _visibleUiData.value = arrayListOf()
        allUiData = arrayListOf()

        coroutineScope.launch {
            sessionData.value = retrieveSession()
//            var startDate = Calendar.getInstance()
//            startDate.add(Calendar.DATE, -1)
//            var startDateString = requestFormatter.format(startDate.time)
//            var endDate = Calendar.getInstance()
//            endDate.add(Calendar.DATE, 3)
//            var endDateString = requestFormatter.format(endDate.time)

            var getItemsDeferred =
                ItemApi.retrofitService.getItemsForGroup(sessionData.value?.groupId!!)

            try {
                var items = getItemsDeferred.await()

                var itemIdsList = items
                    .map { x -> x.itemId ?: -1}

                var userIds = items.map { x -> x.farmerUserId ?: -1}
                    .distinct()

                var getAvailabilitiesDeferred =
                    ItemAvailabilityApi.retrofitService.getItemAvailabilitiesByItemId(itemIdsList)

                var getUserDetailsDataDeferred =
                    UserApi.retrofitService.getUserDetailsByUserIds(userIds)

                var userDetailsList = getUserDetailsDataDeferred.await()
                var availabilities = getAvailabilitiesDeferred.await()

                allUiData = createAllUiData(availabilities, items, userDetailsList)
                createAllUiFilters()
                filterVisibleItems()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createAllUiData(availabilities: List<ItemAvailability>,
                                items: List<Item>,
                                userDetailsList: List<UserDetails>): ArrayList<FreezeMultipleItemsModel> {
        var allUiData = ArrayList<FreezeMultipleItemsModel>()
        availabilities.forEach { availability ->
            var uiElement = FreezeMultipleItemsModel()

            uiElement.availableDate = formatter.format(df.parse(availability?.availableDate))
            val itemDetail = items.filter { x -> x.itemId?.equals(availability.itemId) ?: false }.firstOrNull()
            val sellerUserDetails = userDetailsList.filter{ x-> x.userId?.equals(itemDetail?.farmerUserId)?: false}.firstOrNull()
            if (itemDetail != null) {
                uiElement.itemId = itemDetail.itemId ?: -1
                uiElement.itemName = itemDetail.itemName ?: ""
                uiElement.itemDescription = itemDetail.description ?: ""
                uiElement.itemImageLink = itemDetail.imageLink ?: ""
                uiElement.itemPrice = itemDetail.pricePerUnit ?: 0.0
                uiElement.itemUom = itemDetail.uom ?: ""
            }
            uiElement.sellerUserId = sellerUserDetails?.userId ?: -1
            uiElement.sellerName = sellerUserDetails?.userName ?: ""
            uiElement.sellerMobile = sellerUserDetails?.mobile ?: ""
            uiElement.availabilityId = availability.itemAvailabilityId ?: -1
            uiElement.isFreezed = availability.isFreezed ?: false
            uiElement.availableQuantity = availability.availableQuantity ?: 0.0
            uiElement.committedQuantity = availability.committedQuantity?: 0.0
            uiElement.repeatDay = availability.repeatDay?: -1
            uiElement.availableTimeSlot = availability.availableTimeSlot?: ""
            allUiData.add(uiElement)
        }

        allUiData.sortByDescending { formatter.parse(it.availableDate) }
        return allUiData
    }



    fun setUpDefaultSelectedFilters() {
        _reportUiFilterModel.value = FreezeMultipleUiModel()
        _reportUiFilterModel.value?.selectedFreezeStatus = "ALL"
        _reportUiFilterModel.value?.selectedSeller = "ALL"

        // Set date range as today
        var rangeStartDate = Calendar.getInstance()
        var rangeEndDate = Calendar.getInstance()
        _reportUiFilterModel.value?.selectedStartDate = formatter.format(rangeStartDate.time)
        _reportUiFilterModel.value?.selectedEndDate = formatter.format(rangeEndDate.time)
    }


    private fun createAllUiFilters() {
        _reportUiFilterModel.value?.freezeStatusList = arrayListOf("ALL", FREEZED_STATUS,
            AVAILABLE_STATUS)
        _reportUiFilterModel.value?.sellerNameList = arrayListOf("ALL")
//        var date_2 = Calendar.getInstance()
//        date_2.add(Calendar.DATE, 2)
//        var dateString2 = formatter.format(date_2.time)
//
//        var date_3 = Calendar.getInstance()
//        date_3.add(Calendar.DATE, 3)
//        var dateString3 = formatter.format(date_3.time)

//        _reportUiFilterModel.value?.timeFilterList = arrayListOf("Today", "Tomorrow") //, dateString2, dateString3)

        val dateList = arrayListOf("Today", "Tomorrow")
        for(i in 2..7){
            var date = Calendar.getInstance()
            date.add(Calendar.DATE, i)
            var dateString = formatter.format(date.time)
            dateList.add(dateString)
        }
        _reportUiFilterModel.value?.timeFilterList = dateList
        //Refresh filter
        _reportUiFilterModel.value = _reportUiFilterModel.value


    }


    private fun reCreateBuyerNameFilterList() {
        _reportUiFilterModel.value?.sellerNameList = arrayListOf("ALL")
            .plus(_visibleUiData.value
                ?.filter { uiElement -> !uiElement.sellerName.isBlank() }
                ?.distinctBy { it.sellerUserId }
                ?.map { uiElement -> generateUniqueFilterName(uiElement.sellerName,uiElement.sellerMobile) }
                ?.sorted() ?: listOf())
        //Refresh filter
        _reportUiFilterModel.value = _reportUiFilterModel.value
    }


    private fun generateUniqueFilterName(name: String, mobile: String): String{
        return String.format("%s (%s)",name, mobile)
    }


    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<FreezeMultipleItemsModel>()
        val selectedFreezeStatus = reportUiFilterModel.value?.selectedFreezeStatus ?: ""
        val selectedStartDate = reportUiFilterModel.value?.selectedStartDate ?: formatter.format(todayDate.time)
        val selectedEndDate = reportUiFilterModel.value?.selectedEndDate ?: formatter.format(todayDate.time)
        val selectedSeller = reportUiFilterModel.value?.selectedSeller ?: ""

        elements.forEach { element ->
            if ((selectedFreezeStatus == "ALL"
                        || checkAssignedStatus(selectedFreezeStatus, element)) &&
                (selectedSeller == "ALL"
                        || generateUniqueFilterName(element.sellerName, element.sellerMobile).equals(selectedSeller)) &&
                (isInSelectedDateRange(element, selectedStartDate, selectedEndDate))) {

                //TODO - add date range not just one date
                filteredItems.add(element)
            }
        }
        filteredItems.sortBy { it.itemName }
        _visibleUiData.value = filteredItems
        reCreateBuyerNameFilterList()
    }

    private fun checkAssignedStatus(selectedStatus: String, element: FreezeMultipleItemsModel): Boolean{
        if (selectedStatus == FREEZED_STATUS && element.isFreezed){
            return true
        }
        else if (selectedStatus == AVAILABLE_STATUS && !element.isFreezed){
            return true
        }
        return false
    }

    private fun isInSelectedDateRange(
        element: FreezeMultipleItemsModel,
        selectedStartDate: String,
        selectedEndDate: String
    ) : Boolean {

        if (element.availableDate.isBlank() ||
            selectedEndDate.isBlank() ||
            selectedStartDate.isBlank()) return true

        return formatter.parse(element.availableDate) >= formatter.parse(selectedStartDate) &&
                formatter.parse(element.availableDate) <= formatter.parse(selectedEndDate)
    }


    private suspend fun retrieveSession() : SessionEntity {
        return withContext(Dispatchers.IO) {
            val sessions = database.getAll()
            var session = SessionEntity()
            if (sessions.size==1) {
                session = sessions[0]
                println(session.toString())
            } else {
                database.clearSessions()
            }
            return@withContext session
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }



    fun onFreezeStatusSelected(position: Int) {

        _reportUiFilterModel.value?.selectedFreezeStatus = _reportUiFilterModel.value?.freezeStatusList?.get(position) ?: ""

        filterVisibleItems()
    }


    fun onTimeFilterSelected(position: Int) {
        for(i in 0..7){
            if (position == i) setTimeFilterRange(i,i)
        }
//        if (position.equals(0)) setTimeFilterRange(0,0) //Today
//        if (position.equals(1)) setTimeFilterRange(1,1) //Tomorrow
//        if (position.equals(2)) setTimeFilterRange(2,2) //3rd day
//        if (position.equals(3)) setTimeFilterRange(3,3) //4th day
//        if (position.equals(4)) setTimeFilterRange(4,4) //5th day
//        if (position.equals(5)) setTimeFilterRange(5,5) //5th day
        filterVisibleItems()
    }

    fun onSellerSelected(position: Int) {
        _reportUiFilterModel.value?.selectedSeller = _reportUiFilterModel.value?.sellerNameList?.get(position) ?: ""
        filterVisibleItems()
    }

    fun setTimeFilterRange(startDateOffset: Int, endDateOffset: Int) {
        var rangeStartDate = Calendar.getInstance()
        var rangeEndDate = Calendar.getInstance()
        rangeStartDate.add(Calendar.DATE, startDateOffset)
        rangeEndDate.add(Calendar.DATE, endDateOffset)
        _reportUiFilterModel.value?.selectedStartDate = formatter.format(rangeStartDate.time)
        _reportUiFilterModel.value?.selectedEndDate = formatter.format(rangeEndDate.time)
        filterVisibleItems()
    }


    fun onCheckBoxClicked(selectedUiModel:FreezeMultipleItemsModel) {
        var isChecked = visibleUiData.value
            ?.filter { it.availabilityId.equals(selectedUiModel.availabilityId) }
            ?.first()
            ?.isItemChecked ?: true

        _visibleUiData.value
            ?.filter { it.availabilityId.equals(selectedUiModel.availabilityId) }
            ?.first()
            ?.isItemChecked = !isChecked
    }


    fun onAllItemsCheckBoxClicked() {
        isAllItemsSelected = !isAllItemsSelected
        _visibleUiData.value?.forEach { data ->
            data.isItemChecked = isAllItemsSelected
        }
        _visibleUiData.value = _visibleUiData.value
    }

    fun updateAvailability(availabilityUpdateRequests: List<ItemAvailabilityUpdateRequest>){
        _isProgressBarActive.value = true
        coroutineScope.launch {
            var freezeAvailabilityDataDeferred = ItemAvailabilityApi.retrofitService.updateItemAvailabilities(availabilityUpdateRequests)
            try{
                freezeAvailabilityDataDeferred.await()
                _toastMessage.value = "Successfull"
                getAvailabilitiesForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }

    }

    fun onFreezeButtonClicked() {
        var availabilityUpdateRequests = createFreezeRequests(visibleUiData.value, true)
        updateAvailability(availabilityUpdateRequests)

    }

    fun onUnFreezeButtonClicked() {
        var availabilityUpdateRequests = createFreezeRequests(visibleUiData.value, false)
        updateAvailability(availabilityUpdateRequests)
    }

    private fun createFreezeRequests(uiDataElements: MutableList<FreezeMultipleItemsModel>?, isFreezed: Boolean): List<ItemAvailabilityUpdateRequest> {
        var availabilityRequestList: ArrayList<ItemAvailabilityUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked }?.forEach { element ->
            var availabilityRequest = ItemAvailabilityUpdateRequest(
                itemAvailabilityId = element.availabilityId,
                isFreezed = isFreezed,
                availableDate = null,
                availableQuantity = null,
                updatedBy = sessionData.value?.userName ?: "",
                repeatDay = null,
                availableTimeSlot = null,
                committedQuantity = null
            )
            availabilityRequestList.add(availabilityRequest)
        }
        return availabilityRequestList
    }

    fun getAvailabilitiesToShare(): String{
        var itemShareArray: ArrayList<String> = arrayListOf()
        visibleUiData.value?.filter { it.isItemChecked }?.forEach { element ->
            element.itemName
            element.itemPrice.toString()
            itemShareArray.add(element.itemName + "\t - \t"  + element.itemPrice.toString() + "/" +element.itemUom)
        }
        return itemShareArray.joinToString("\n")

    }


}