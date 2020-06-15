package com.f2h.f2h_admin.screens.group.confirm_reject

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_REJECTED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PENDING
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.OrderApi
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ConfirmRejectViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var isAllItemsSelected: Boolean = false

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _reportUiFilterModel = MutableLiveData<ConfirmRejectUiModel>()
    val reportUiFilterModel: LiveData<ConfirmRejectUiModel>
        get() = _reportUiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<ConfirmRejectItemsModel>>()
    val visibleUiData: LiveData<MutableList<ConfirmRejectItemsModel>>
        get() = _visibleUiData

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage


    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val sessionData = MutableLiveData<SessionEntity>()
    private var allUiData = ArrayList<ConfirmRejectItemsModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        createAllUiFilters()
        getOrdersReportForGroup()
    }

    fun getOrdersReportForGroup() {

        //refresh data
        _isProgressBarActive.value = true
        _visibleUiData.value = arrayListOf()
        allUiData = arrayListOf()

        coroutineScope.launch {
            sessionData.value = retrieveSession()
            var getOrdersDataDeferred = OrderApi.retrofitService.getOrdersForGroup(sessionData.value!!.groupId)
            try {
                var orders = getOrdersDataDeferred.await()
                var userIds = orders.map { x -> x.buyerUserId ?: -1}
                    .plus(orders.map { x -> x.sellerUserId ?: -1}).distinct()
                var availabilityIds = orders.map { x -> x.itemAvailabilityId ?: -1 }

                var getUserDetailsDataDeferred =
                    UserApi.retrofitService.getUserDetailsByUserIds(userIds)

                var getItemAvailabilitiesDataDeferred =
                    ItemAvailabilityApi.retrofitService.getItemAvailabilities(availabilityIds)

                var itemAvailabilities = getItemAvailabilitiesDataDeferred.await()
                var userDetailsList = getUserDetailsDataDeferred.await()

                allUiData = createAllUiData(itemAvailabilities, orders, userDetailsList)
                if (allUiData.size > 0) {
                    filterVisibleItems()
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createAllUiData(itemAvailabilitys: List<ItemAvailability>,
                                orders: List<Order>, userDetailsList: List<UserDetails>): ArrayList<ConfirmRejectItemsModel> {
        var allUiData = ArrayList<ConfirmRejectItemsModel>()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)
        orders.forEach { order ->

            var uiElement = ConfirmRejectItemsModel()
            var item = Item()
            try {
                item = jsonAdapter.fromJson(order.orderDescription) ?: Item()
            } catch (e: Exception){
                Log.e("Parse Error", e.message)
            }

            // Check item availability for the order. freezed etc
            itemAvailabilitys.forEach { availability ->
                if (availability.itemAvailabilityId != null) {
                    if (availability.itemAvailabilityId.equals(order.itemAvailabilityId)) {
                        uiElement.isFreezed = availability.isFreezed ?: false
                        uiElement.availableQuantity = availability.availableQuantity ?: 0.0
                    }
                }
            }

            if (item != null) {
                uiElement.itemId = item.itemId ?: -1
                uiElement.itemName = item.itemName ?: ""
                uiElement.itemDescription = item.description ?: ""
                uiElement.itemUom = item.uom ?: ""
                uiElement.itemImageLink = item.imageLink ?: ""
                uiElement.price = item.pricePerUnit ?: 0.0
                uiElement.confirmedQuantityJump = item.confirmQtyJump ?: 0.0
            }
            uiElement.orderedDate = formatter.format(df.parse(order.orderedDate))
            uiElement.orderedQuantity = order.orderedQuantity ?: 0.0
            if(order.orderStatus.equals(ORDER_STATUS_ORDERED)) {
                uiElement.confirmedQuantity =  order.orderedQuantity ?: 0.0
            } else {
                uiElement.confirmedQuantity = order.confirmedQuantity ?: 0.0
            }
            uiElement.orderId = order.orderId ?: -1L
            uiElement.orderAmount = order.orderedAmount ?: 0.0
            uiElement.discountAmount = order.discountAmount ?: 0.0
            uiElement.orderStatus = order.orderStatus ?: ""
            uiElement.paymentStatus = order.paymentStatus ?: ""
            uiElement.orderComment = order.orderComment ?: ""
            uiElement.buyerName = userDetailsList.filter { x -> x.userId?.equals(order.buyerUserId) ?: false }.single().userName ?: ""
            uiElement.sellerName = userDetailsList.filter { x -> x.userId?.equals(order.sellerUserId) ?: false }.single().userName ?: ""
            uiElement.deliveryAddress = order.deliveryLocation ?: ""
            uiElement.displayQuantity = getDisplayQuantity(uiElement.orderStatus, uiElement.orderedQuantity, uiElement.confirmedQuantity)
            allUiData.add(uiElement)
        }

        allUiData.sortByDescending { formatter.parse(it.orderedDate) }
        return allUiData
    }

    private fun getDisplayQuantity(displayStatus: String, orderedQuantity: Double, confirmedQuantity: Double): Double {
        if (displayStatus.equals(ORDER_STATUS_ORDERED)) return orderedQuantity
        return confirmedQuantity
    }


    private fun createAllUiFilters() {
        var filters = ConfirmRejectUiModel()

        filters.itemList = arrayListOf("ALL").plus(allUiData.sortedBy { uiElement -> uiElement.itemName }
            .filter { uiElement -> !uiElement.itemName.isBlank() }
            .map { uiElement -> uiElement.itemName }.distinct().sorted())

        filters.orderStatusList = arrayListOf("ALL", "Open Orders", "Delivered Orders", "Payment Pending")

        filters.paymentStatusList = arrayListOf("ALL").plus(allUiData.sortedBy { uiElement -> uiElement.paymentStatus }
            .filter { uiElement -> !uiElement.paymentStatus.isBlank() }
            .map { uiElement -> uiElement.paymentStatus }.distinct().sorted())

        filters.buyerNameList = arrayListOf("ALL").plus(allUiData.sortedBy { uiElement -> uiElement.buyerName }
            .filter { uiElement -> !uiElement.buyerName.isBlank() }
            .map { uiElement -> uiElement.buyerName }.distinct().sorted())

        filters.farmerNameList = arrayListOf("ALL").plus(allUiData.sortedBy { uiElement -> uiElement.sellerName }
            .filter { uiElement -> !uiElement.sellerName.isBlank() }
            .map { uiElement -> uiElement.sellerName }.distinct().sorted())

        filters.timeFilterList = arrayListOf("Today", "Tomorrow", "Next 7 days")

        filters.selectedItem = "ALL"
        filters.selectedPaymentStatus = "ALL"
        filters.selectedOrderStatus = "ALL"
        filters.selectedFarmer = "ALL"
        filters.selectedBuyer = "ALL"
        setTimeFilterRange(0,0) //Today

        _reportUiFilterModel.value = filters
    }


    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<ConfirmRejectItemsModel>()
        val selectedItem = reportUiFilterModel.value?.selectedItem ?: ""
        val selectedOrderStatus = reportUiFilterModel.value?.selectedOrderStatus ?: ""
        val selectedPaymentStatus = reportUiFilterModel.value?.selectedPaymentStatus ?: ""
        val selectedStartDate = reportUiFilterModel.value?.selectedStartDate ?: formatter.format(todayDate.time)
        val selectedEndDate = reportUiFilterModel.value?.selectedEndDate ?: formatter.format(todayDate.time)
        val selectedBuyer = reportUiFilterModel.value?.selectedBuyer ?: ""
        val selectedFarmer = reportUiFilterModel.value?.selectedFarmer ?: ""

        elements.forEach { element ->
            if ((selectedItem == "ALL" || element.itemName.equals(selectedItem)) &&
                (selectedOrderStatus == "ALL" || selectedOrderStatus.split(",").contains(element.orderStatus)) &&
                (selectedPaymentStatus == "ALL" || element.paymentStatus.equals(selectedPaymentStatus))  &&
                (selectedBuyer == "ALL" || element.buyerName.equals(selectedBuyer)) &&
                (selectedFarmer == "ALL" || element.sellerName.equals(selectedFarmer)) &&
                (isInSelectedDateRange(element, selectedStartDate, selectedEndDate))) {

                //TODO - add date range not just one date
                filteredItems.add(element)
            }
        }
        filteredItems.sortByDescending { formatter.parse(it.orderedDate) }
        _visibleUiData.value = filteredItems
    }

    private fun isInSelectedDateRange(
        element: ConfirmRejectItemsModel,
        selectedStartDate: String,
        selectedEndDate: String
    ) : Boolean {

        if (element.orderedDate.isBlank() ||
                selectedEndDate.isBlank() ||
                selectedStartDate.isBlank()) return true

        return formatter.parse(element.orderedDate) >= formatter.parse(selectedStartDate) &&
                formatter.parse(element.orderedDate) <= formatter.parse(selectedEndDate)
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


    fun onItemSelected(position: Int) {
        _reportUiFilterModel.value?.selectedItem = _reportUiFilterModel.value?.itemList?.get(position) ?: ""
        filterVisibleItems()
    }

    fun onOrderStatusSelected(position: Int) {
        if (position == 0) {
            _reportUiFilterModel.value?.selectedOrderStatus = "ALL"
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        if (position == 1) {
            _reportUiFilterModel.value?.selectedOrderStatus =
                String.format("%s,%s", ORDER_STATUS_ORDERED, ORDER_STATUS_CONFIRMED)
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        if (position == 2) {
            _reportUiFilterModel.value?.selectedOrderStatus = ORDER_STATUS_DELIVERED
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        if (position == 3) {
            _reportUiFilterModel.value?.selectedOrderStatus = "ALL"
            _reportUiFilterModel.value?.selectedPaymentStatus = PAYMENT_STATUS_PENDING
        }
        filterVisibleItems()
    }


    fun onTimeFilterSelected(position: Int) {
        if (position.equals(0)) setTimeFilterRange(0,0) //Today
        if (position.equals(1)) setTimeFilterRange(1,1) //Tomorrow
        if (position.equals(2)) setTimeFilterRange(0,7) //Next 7 Days
        filterVisibleItems()
    }

    fun onBuyerSelected(position: Int) {
        _reportUiFilterModel.value?.selectedBuyer = _reportUiFilterModel.value?.buyerNameList?.get(position) ?: ""
        filterVisibleItems()
    }

    fun onFarmerSelected(position: Int) {
        _reportUiFilterModel.value?.selectedFarmer = _reportUiFilterModel.value?.farmerNameList?.get(position) ?: ""
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


    fun onCheckBoxClicked(selectedUiModel: ConfirmRejectItemsModel) {
        var isChecked = visibleUiData.value
            ?.filter { it.orderId.equals(selectedUiModel.orderId) }
            ?.first()
            ?.isItemChecked ?: true

        _visibleUiData.value
            ?.filter { it.orderId.equals(selectedUiModel.orderId) }
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



    // increase order qty till max available qty
    fun increaseConfirmedQuantity(selectedUiModel: ConfirmRejectItemsModel){
        _visibleUiData.value?.forEach { element ->
            if (element.orderId.equals(selectedUiModel.orderId)){
                element.confirmedQuantity = element.confirmedQuantity.plus(element.confirmedQuantityJump)
                element.quantityChange = element.quantityChange.plus(element.confirmedQuantityJump)

                // logic to prevent increasing quantity beyond maximum
                if (element.quantityChange > element.availableQuantity) {
                    element.confirmedQuantity = element.confirmedQuantity.minus(element.confirmedQuantityJump)
                    element.quantityChange = element.quantityChange.minus(element.confirmedQuantityJump)
                    _toastMessage.value = "No more stock"
                }
                element.orderAmount = calculateOrderAmount(element)
            }
        }
        _visibleUiData.value = _visibleUiData.value
    }


    // decrease order qty till min 0
    fun decreaseConfirmedQuantity(selectedUiModel: ConfirmRejectItemsModel){
        _visibleUiData.value?.forEach { element ->
            if (element.orderId.equals(selectedUiModel.orderId)){
                element.confirmedQuantity = element.confirmedQuantity.minus(element.confirmedQuantityJump)
                element.quantityChange = element.quantityChange.minus(element.confirmedQuantityJump)

                if (element.confirmedQuantity < 0) {
                    element.confirmedQuantity = 0.0
                    element.quantityChange = element.quantityChange.plus(element.confirmedQuantityJump)
                }
            }
            element.orderAmount = calculateOrderAmount(element)
        }
        _visibleUiData.value = _visibleUiData.value
    }



    fun onRejectOrderButtonClicked() {
        var orderUpdateRequests = createRejectOrderRequests(visibleUiData.value)
        _isProgressBarActive.value = true;
        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.updateOrders(orderUpdateRequests)
            try{
                updateOrdersDataDeferred.await()
                _toastMessage.value = "Successfully rejected orders"
                getOrdersReportForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }
    }

    private fun createRejectOrderRequests(uiDataElements: MutableList<ConfirmRejectItemsModel>?): List<OrderUpdateRequest> {
        var orderUpdateRequestList: ArrayList<OrderUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked }?.forEach { element ->
            var updateRequest = OrderUpdateRequest(
                orderId = element.orderId,
                orderStatus = ORDER_STATUS_REJECTED,
                paymentStatus = "",
                orderedQuantity = null,
                confirmedQuantity = 0.0,
                discountAmount = 0.0,
                orderedAmount = 0.0
            )
            orderUpdateRequestList.add(updateRequest)
        }
        return orderUpdateRequestList
    }


    fun onConfirmOrderButtonClicked() {
        var orderUpdateRequests = createConfirmedOrderRequests(visibleUiData.value)
        _isProgressBarActive.value = true
        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.updateOrders(orderUpdateRequests)
            try{
                updateOrdersDataDeferred.await()
                _toastMessage.value = "Successfully confirmed orders"
                getOrdersReportForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }
    }

    private fun createConfirmedOrderRequests(uiDataElements: MutableList<ConfirmRejectItemsModel>?): List<OrderUpdateRequest> {
        var orderUpdateRequestList: ArrayList<OrderUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked }?.forEach { element ->
            var updateRequest = OrderUpdateRequest(
                orderId = element.orderId,
                orderStatus = ORDER_STATUS_CONFIRMED,
                paymentStatus = "",
                orderedQuantity = null,
                confirmedQuantity = element.confirmedQuantity,
                discountAmount = element.discountAmount,
                orderedAmount = calculateOrderAmount(element)
            )
            orderUpdateRequestList.add(updateRequest)
        }
        return orderUpdateRequestList
    }

    private fun calculateOrderAmount(element: ConfirmRejectItemsModel): Double {
        return element.confirmedQuantity * element.price - element.discountAmount
    }

}