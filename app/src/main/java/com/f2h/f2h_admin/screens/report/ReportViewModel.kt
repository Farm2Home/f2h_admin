package com.f2h.f2h_admin.screens.report

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PENDING
import com.f2h.f2h_admin.constants.F2HConstants.TIME_SELECTION_TODAY
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.CommentApi
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.OrderApi
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.*
import com.f2h.f2h_admin.utils.fetchOrderDate
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ReportViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {


    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _reportUiFilterModel = MutableLiveData<ReportUiModel>()
    val reportUiFilterModel: LiveData<ReportUiModel>
        get() = _reportUiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<ReportItemsModel>>()
    val visibleUiData: LiveData<MutableList<ReportItemsModel>>
        get() = _visibleUiData


    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val sessionData = MutableLiveData<SessionEntity>()
    private var allUiData = ArrayList<ReportItemsModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getOrdersReportForGroup()
    }

    private fun getOrdersReportForGroup() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData.value = retrieveSession()
            val getOrderHeaderDataDeferred = OrderApi.retrofitService(getApplication()).getOrderHeaderForGroup(sessionData.value!!.groupId, fetchOrderDate(-15), fetchOrderDate(10))
            try {
                val orderHeaders = getOrderHeaderDataDeferred.await()
                val orders = arrayListOf<Order>()
                val serviceOrders = arrayListOf<ServiceOrder>()
                orderHeaders.forEach { header ->
                    header.orders?.map { it ->
                        it.deliveryLocation = header.deliveryLocation
                        it.buyerUserId = header.buyerUserId
                    }
                    orders.addAll(header.orders ?: arrayListOf())
                    serviceOrders.addAll(header.serviceOrders ?: arrayListOf())
                }
                val userIds = orderHeaders.map { x -> x.buyerUserId ?: -1}
                    .plus(orders.map { x -> x.sellerUserId ?: -1}).distinct()
                val availabilityIds = orders.map { x -> x.itemAvailabilityId ?: -1 }.distinct()

                val getUserDetailsDataDeferred =
                    UserApi.retrofitService(getApplication()).getUserDetailsByUserIds(userIds.joinToString())

                val getItemAvailabilitiesDataDeferred =
                    ItemAvailabilityApi.retrofitService(getApplication()).getItemAvailabilities(availabilityIds.joinToString())

                val itemAvailabilities = getItemAvailabilitiesDataDeferred.await()
                val userDetailsList = getUserDetailsDataDeferred.await()

                allUiData = createAllUiData(itemAvailabilities, orders, userDetailsList)
                _reportUiFilterModel.value = createAllUiFilters()
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
                                orders: List<Order>, userDetailsList: List<UserDetails>): ArrayList<ReportItemsModel> {
        val allUiData = ArrayList<ReportItemsModel>()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)
        orders.forEach { order ->

            val uiElement = ReportItemsModel()
            var item = Item()
            try {
                item = jsonAdapter.fromJson(order.orderDescription) ?: Item()
            } catch (e: Exception){
                Log.e("Parse Error", e.message?:"")
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

            val buyerUserDetails =
                userDetailsList.firstOrNull { x -> x.userId?.equals(order.buyerUserId) ?: false }
            val sellerUserDetails =
                userDetailsList.firstOrNull { x -> x.userId?.equals(order.sellerUserId) ?: false }
            uiElement.currency = sessionData.value?.groupCurrency ?: ""
            uiElement.buyerName = buyerUserDetails?.userName ?: ""
            uiElement.buyerMobile = buyerUserDetails?.mobile ?: ""
            uiElement.sellerName = sellerUserDetails?.userName ?: ""
            uiElement.sellerMobile = sellerUserDetails?.mobile ?: ""

            uiElement.v2Commission = order.v2Amount ?: 0.0
            uiElement.farmerCommission = order.farmerAmount ?: 0.0
            uiElement.orderedDate = formatter.format(df.parse(order.orderedDate))
            uiElement.orderedQuantity = order.orderedQuantity ?: 0.0
            uiElement.confirmedQuantity = order.confirmedQuantity ?: 0.0
            uiElement.orderId = order.orderId ?: -1L
            uiElement.orderAmount = order.orderedAmount ?: 0.0
            uiElement.discountAmount = order.discountAmount ?: 0.0
            uiElement.orderStatus = order.orderStatus ?: ""
            uiElement.paymentStatus = order.paymentStatus ?: ""
            uiElement.orderComment = order.orderComment ?: ""
            uiElement.buyerUserId = order.buyerUserId ?: -1
            uiElement.sellerUserId = order.sellerUserId ?: -1
            uiElement.deliveryAddress = order.deliveryLocation ?: ""
            uiElement.displayQuantity = getDisplayQuantity(uiElement.orderStatus, uiElement.orderedQuantity, uiElement.confirmedQuantity)

            uiElement.itemId = item.itemId ?: -1
            uiElement.itemName = item.itemName ?: ""
            uiElement.itemDescription = item.description ?: ""
            uiElement.itemUom = item.uom ?: ""
            uiElement.itemImageLink = item.imageLink ?: ""
            uiElement.price = item.pricePerUnit ?: 0.0
            uiElement.itemHandlingCharges = item.handlingCharges
            uiElement.handlingCharges = item.handlingCharges
            uiElement.itemHandlingCharges.forEach { handlingCharge ->
                handlingCharge.currency = sessionData.value?.groupCurrency ?: ""
                handlingCharge.amount = handlingCharge.amount * uiElement.displayQuantity
            }

            allUiData.add(uiElement)
        }

        allUiData.sortByDescending { formatter.parse(it.orderedDate) }
        return allUiData
    }

    private fun getDisplayQuantity(displayStatus: String, orderedQuantity: Double, confirmedQuantity: Double): Double {
        if (displayStatus == "ORDERED") return orderedQuantity
        return confirmedQuantity
    }


    private fun createAllUiFilters(): ReportUiModel {
        val filters = ReportUiModel()

        val orderedDates = allUiData.map{ x -> formatter.format(formatter.parse(x.orderedDate))}
        val rangeStartDate = Calendar.getInstance()
        val today = formatter.format(rangeStartDate.time)
        filters.timeFilterList = arrayListOf(TIME_SELECTION_TODAY).plus(
            orderedDates
                .filter { uiElement -> uiElement != today }
                .distinctBy { it }
                .sorted())

        filters.itemList = arrayListOf("ALL").plus(allUiData
            .filter { uiElement -> !uiElement.itemName.isBlank() }
            .distinctBy { it.itemId }
            .map { uiElement -> generateUniqueFilterName(uiElement.itemName, uiElement.itemId.toString()) }.sorted())

        filters.orderStatusList = arrayListOf("ALL", "Open Orders", "Delivered Orders", "Payment Pending", "Ordered", "Confirmed")

        filters.paymentStatusList = arrayListOf("ALL").plus(allUiData
            .filter { uiElement -> !uiElement.paymentStatus.isBlank() }
            .map { uiElement -> uiElement.paymentStatus }.sorted())

        filters.buyerNameList = arrayListOf("ALL")

        filters.farmerNameList = arrayListOf("ALL")

//        filters.timeFilterList = arrayListOf("Today", "Tomorrow", "Next 7 days", "Last 15 days")

        filters.selectedItem = "ALL"
        filters.selectedPaymentStatus = "ALL"
        filters.selectedOrderStatus = "ALL"
        filters.selectedFarmer = "ALL"
        filters.selectedBuyer = "ALL"
        setTimeFilterRange(TIME_SELECTION_TODAY) //Today

        return filters
    }

    private fun reCreateBuyerNameFilterList() {
        _reportUiFilterModel.value?.buyerNameList = arrayListOf("ALL")
            .plus(_visibleUiData.value
                ?.filter { uiElement -> !uiElement.buyerName.isBlank() }
                ?.distinctBy { it.buyerUserId }
                ?.map { uiElement -> generateUniqueFilterName(uiElement.buyerName,uiElement.buyerMobile) }
                ?.sorted() ?: listOf())
        //Refresh filter
        _reportUiFilterModel.value = _reportUiFilterModel.value
    }

    private fun reCreateFarmerNameFilterList() {
        _reportUiFilterModel.value?.farmerNameList = arrayListOf("ALL")
            .plus(_visibleUiData.value
                ?.filter { uiElement -> !uiElement.sellerName.isBlank() }
                ?.distinctBy { it.sellerUserId }
                ?.map { uiElement -> generateUniqueFilterName(uiElement.sellerName,uiElement.sellerMobile)  }
                ?.sorted() ?: listOf())
        //Refresh filter
        _reportUiFilterModel.value = _reportUiFilterModel.value
    }

    private fun generateUniqueFilterName(name: String, mobile: String): String{
        return String.format("%s (%s)",name, mobile)
    }

    private fun setTimeFilterRange(selectedDate: String) {
        val rangeStartDate = Calendar.getInstance()
        val today = formatter.format(rangeStartDate.time)
        if (selectedDate == TIME_SELECTION_TODAY) {
            _reportUiFilterModel.value?.selectedStartDate = today
            _reportUiFilterModel.value?.selectedEndDate = today
        }
        else{
            _reportUiFilterModel.value?.selectedStartDate = selectedDate
            _reportUiFilterModel.value?.selectedEndDate = selectedDate
        }
    }

    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<ReportItemsModel>()
        val selectedItem = reportUiFilterModel.value?.selectedItem ?: ""
        val selectedOrderStatus = reportUiFilterModel.value?.selectedOrderStatus ?: ""
        val selectedPaymentStatus = reportUiFilterModel.value?.selectedPaymentStatus ?: ""
        val selectedStartDate = reportUiFilterModel.value?.selectedStartDate ?: formatter.format(todayDate.time)
        val selectedEndDate = reportUiFilterModel.value?.selectedEndDate ?: formatter.format(todayDate.time)
        val selectedBuyer = reportUiFilterModel.value?.selectedBuyer ?: ""
        val selectedFarmer = reportUiFilterModel.value?.selectedFarmer ?: ""

        elements.forEach { element ->
            if ((selectedItem == "ALL" || generateUniqueFilterName(element.itemName, element.itemId.toString()) == selectedItem) &&
                (selectedOrderStatus == "ALL" || selectedOrderStatus.split(",").contains(element.orderStatus)) &&
                (selectedPaymentStatus == "ALL" || element.paymentStatus == selectedPaymentStatus)  &&
                (selectedBuyer == "ALL" || generateUniqueFilterName(element.buyerName,element.buyerMobile) == selectedBuyer) &&
                (selectedFarmer == "ALL" || generateUniqueFilterName(element.sellerName,element.sellerMobile) == selectedFarmer) &&
                (isInSelectedDateRange(element, selectedStartDate, selectedEndDate))) {

                filteredItems.add(element)
            }
        }
        filteredItems.sortByDescending { formatter.parse(it.orderedDate) }
        _visibleUiData.value = filteredItems
        reCreateBuyerNameFilterList()
        reCreateFarmerNameFilterList()
    }

    private fun isInSelectedDateRange(
        element: ReportItemsModel,
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
        if (position == 4) {
            _reportUiFilterModel.value?.selectedOrderStatus = ORDER_STATUS_ORDERED
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        if (position == 5) {
            _reportUiFilterModel.value?.selectedOrderStatus = ORDER_STATUS_CONFIRMED
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        filterVisibleItems()
    }


    fun onTimeFilterSelected(position: Int) {
//        if (position.equals(0)) setTimeFilterRange(0,0) //Today
//        if (position.equals(1)) setTimeFilterRange(1,1) //Tomorrow
//        if (position.equals(2)) setTimeFilterRange(0,7) //Next 7 Days
//        if (position.equals(3)) setTimeFilterRange(-15,0) //Last 15 days
        val selectedTime = _reportUiFilterModel.value?.timeFilterList?.get(position) ?: TIME_SELECTION_TODAY
        setTimeFilterRange(selectedTime)
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

//    fun setTimeFilterRange(startDateOffset: Int, endDateOffset: Int) {
//        var rangeStartDate = Calendar.getInstance()
//        var rangeEndDate = Calendar.getInstance()
//        rangeStartDate.add(Calendar.DATE, startDateOffset)
//        rangeEndDate.add(Calendar.DATE, endDateOffset)
//        _reportUiFilterModel.value?.selectedStartDate = formatter.format(rangeStartDate.time)
//        _reportUiFilterModel.value?.selectedEndDate = formatter.format(rangeEndDate.time)
//        filterVisibleItems()
//    }

    fun moreDetailsButtonClicked(element: ReportItemsModel) {
        if(element.isMoreDetailsDisplayed){
            element.isMoreDetailsDisplayed = false
//            _visibleUiData.value?.firstOrNull { data -> data.orderId.equals(element.orderId) }?.isMoreDetailsDisplayed = false
            _visibleUiData.value = _visibleUiData.value
            return
        }

        // Do API call to fetch comments
        fetchCommentsForOrder(element)
        element.isMoreDetailsDisplayed = true
//        _visibleUiData.value?.firstOrNull { data -> data.orderId == element.orderId }?.isMoreDetailsDisplayed = true
        _visibleUiData.value = _visibleUiData.value
    }

    private fun fetchCommentsForOrder(element: ReportItemsModel) {
        setCommentProgressBar(true, element)
        coroutineScope.launch {
            val getCommentsDataDeferred = CommentApi.retrofitService.getComments(element.orderId)
            try {
                val comments: List<Comment> = getCommentsDataDeferred.await()
                element.comments = ArrayList(comments)
//                _visibleUiData.value?.firstOrNull { data -> data.orderId == element.orderId }?.comments = ArrayList(comments)
                _visibleUiData.value = _visibleUiData.value
            } catch (t: Throwable) {
                println(t.message)
            }
            setCommentProgressBar(false, element)
        }
    }

    private fun setCommentProgressBar(isProgressActive: Boolean, element: ReportItemsModel){
        element.isCommentProgressBarActive = isProgressActive
//        _visibleUiData.value?.firstOrNull { data -> data.orderId == element.orderId }?.isCommentProgressBarActive = isProgressActive
        _visibleUiData.value = _visibleUiData.value
    }
}