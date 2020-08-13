package com.f2h.f2h_admin.screens.deliver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PAID
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.CommentApi
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.OrderApi
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.*
import com.f2h.f2h_admin.screens.group.members.MembersUiModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DeliverViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var isAllItemsSelected: Boolean = false

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _reportUiFilterModel = MutableLiveData<DeliverUiModel>()
    val reportUiFilterModel: LiveData<DeliverUiModel>
        get() = _reportUiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<DeliverItemsModel>>()
    val visibleUiData: LiveData<MutableList<DeliverItemsModel>>
        get() = _visibleUiData

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private var _selectedUiElement = MutableLiveData<DeliverItemsModel>()
    val selectedUiElement: LiveData<DeliverItemsModel>
        get() = _selectedUiElement

    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val sessionData = MutableLiveData<SessionEntity>()
    private var allUiData = ArrayList<DeliverItemsModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        setUpDefaultSelectedFilters()
        getOrdersReportForGroup()
    }

    fun getOrdersReportForGroup() {

        //refresh data
        _isProgressBarActive.value = true
        _visibleUiData.value = arrayListOf()
        allUiData = arrayListOf()

        coroutineScope.launch {
            sessionData.value = retrieveSession()
            var getOrdersDataDeferred = OrderApi.retrofitService.getOrdersForGroup(sessionData.value!!.groupId, null, null)
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
                createAllUiFilters()
                filterVisibleItems()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createAllUiData(itemAvailabilitys: List<ItemAvailability>,
                                orders: List<Order>, userDetailsList: List<UserDetails>): ArrayList<DeliverItemsModel> {
        var allUiData = ArrayList<DeliverItemsModel>()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)
        orders.forEach { order ->

            var uiElement = DeliverItemsModel()
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

            val buyerUserDetails = userDetailsList.filter { x -> x.userId?.equals(order.buyerUserId) ?: false }.firstOrNull()
            val sellerUserDetails = userDetailsList.filter { x -> x.userId?.equals(order.sellerUserId) ?: false }.firstOrNull()
            uiElement.buyerName = buyerUserDetails?.userName ?: ""
            uiElement.buyerMobile = buyerUserDetails?.mobile ?: ""
            uiElement.sellerName = sellerUserDetails?.userName ?: ""
            uiElement.sellerMobile = sellerUserDetails?.mobile ?: ""

            uiElement.orderId = order.orderId ?: -1L
            uiElement.orderAmount = order.orderedAmount ?: 0.0
            uiElement.discountAmount = order.discountAmount ?: 0.0
            uiElement.orderStatus = order.orderStatus ?: ""
            uiElement.paymentStatus = order.paymentStatus ?: ""
            uiElement.buyerUserId = order.buyerUserId ?: -1
            uiElement.sellerUserId = order.sellerUserId ?: -1
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


    fun setUpDefaultSelectedFilters() {
        _reportUiFilterModel.value = DeliverUiModel()
        _reportUiFilterModel.value?.selectedItem = "ALL"
        _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        _reportUiFilterModel.value?.selectedOrderStatus = "Open Orders"
        _reportUiFilterModel.value?.selectedFarmer = "ALL"
        _reportUiFilterModel.value?.selectedBuyer = "ALL"

        // Set date range as today
        var rangeStartDate = Calendar.getInstance()
        var rangeEndDate = Calendar.getInstance()
        _reportUiFilterModel.value?.selectedStartDate = formatter.format(rangeStartDate.time)
        _reportUiFilterModel.value?.selectedEndDate = formatter.format(rangeEndDate.time)
    }


    private fun createAllUiFilters() {
        _reportUiFilterModel.value?.itemList = arrayListOf("ALL").plus(allUiData
            .filter { uiElement -> !uiElement.itemName.isBlank() }
            .distinctBy { it.itemId }
            .map { uiElement -> generateUniqueFilterName(uiElement.itemName, uiElement.itemId.toString()) }.sorted())

        _reportUiFilterModel.value?.orderStatusList = arrayListOf("Open Orders")

        _reportUiFilterModel.value?.paymentStatusList = arrayListOf("ALL").plus(allUiData
            .filter { uiElement -> !uiElement.paymentStatus.isBlank() }
            .map { uiElement -> uiElement.paymentStatus }.distinct().sorted())

        _reportUiFilterModel.value?.buyerNameList = arrayListOf("ALL")

        _reportUiFilterModel.value?.farmerNameList = arrayListOf("ALL")

        _reportUiFilterModel.value?.timeFilterList = arrayListOf("Today", "Tomorrow", "Next 7 days", "Last 7 days", "Last 15 days", "Last 30 days")

        //Refresh filter
        _reportUiFilterModel.value = _reportUiFilterModel.value
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

    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<DeliverItemsModel>()
        val selectedItem = reportUiFilterModel.value?.selectedItem ?: ""
        val selectedOrderStatus = reportUiFilterModel.value?.selectedOrderStatus ?: ""
        val selectedPaymentStatus = reportUiFilterModel.value?.selectedPaymentStatus ?: ""
        val selectedStartDate = reportUiFilterModel.value?.selectedStartDate ?: formatter.format(todayDate.time)
        val selectedEndDate = reportUiFilterModel.value?.selectedEndDate ?: formatter.format(todayDate.time)
        val selectedBuyer = reportUiFilterModel.value?.selectedBuyer ?: ""
        val selectedFarmer = reportUiFilterModel.value?.selectedFarmer ?: ""

        elements.forEach { element ->
            if ((selectedItem == "ALL" || generateUniqueFilterName(element.itemName, element.itemId.toString()).equals(selectedItem)) &&
                (selectedOrderStatus == "ALL" || selectedOrderStatus.split(",").contains(element.orderStatus)) &&
                (selectedPaymentStatus == "ALL" || element.paymentStatus.equals(selectedPaymentStatus))  &&
                (selectedBuyer == "ALL" || generateUniqueFilterName(element.buyerName, element.buyerMobile).equals(selectedBuyer)) &&
                (selectedFarmer == "ALL" || generateUniqueFilterName(element.sellerName, element.sellerMobile).equals(selectedFarmer)) &&
                (isInSelectedDateRange(element, selectedStartDate, selectedEndDate))) {

                //TODO - add date range not just one date
                filteredItems.add(element)
            }
        }
        filteredItems.sortByDescending { formatter.parse(it.orderedDate) }
        _visibleUiData.value = filteredItems
        reCreateBuyerNameFilterList()
        reCreateFarmerNameFilterList()
    }

    private fun isInSelectedDateRange(
        element: DeliverItemsModel,
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
            _reportUiFilterModel.value?.selectedOrderStatus =
                String.format("%s,%s", ORDER_STATUS_ORDERED, ORDER_STATUS_CONFIRMED)
            _reportUiFilterModel.value?.selectedPaymentStatus = "ALL"
        }
        filterVisibleItems()
    }


    fun onTimeFilterSelected(position: Int) {
        if (position.equals(0)) setTimeFilterRange(0,0) //Today
        if (position.equals(1)) setTimeFilterRange(1,1) //Tomorrow
        if (position.equals(2)) setTimeFilterRange(0,7) //Next 7 Days
        if (position.equals(3)) setTimeFilterRange(-7,0) //Last week
        if (position.equals(4)) setTimeFilterRange(-15,0)  //Last 15 days
        if (position.equals(5)) setTimeFilterRange(-30,0) //Last 30 days
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


    fun onCheckBoxClicked(selectedUiModel: DeliverItemsModel) {
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


    fun onCashCollectedbuttonClicked() {
        var deliveredOrderUpdateRequests = createDeliverOrderRequests(visibleUiData.value)
        _isProgressBarActive.value = true;
        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.cashCollectedAndUpdateOrders(deliveredOrderUpdateRequests)
            try{
                updateOrdersDataDeferred.await()
                _toastMessage.value = "Successfully paid and delivered orders"
                getOrdersReportForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }
    }



    fun onDeliverButtonClicked() {
        var deliveredOrderUpdateRequests = createDeliverOrderRequests(visibleUiData.value)
        _isProgressBarActive.value = true
        coroutineScope.launch {
            var deliverOrdersDataDeferred = OrderApi.retrofitService.updateOrders(deliveredOrderUpdateRequests)
            try{
                deliverOrdersDataDeferred.await()
                _toastMessage.value = "Successfully delivered orders"
                getOrdersReportForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }
    }


    private fun createDeliverOrderRequests(uiDataElements: MutableList<DeliverItemsModel>?): List<OrderUpdateRequest> {
        var orderUpdateRequestList: ArrayList<OrderUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked }?.forEach { element ->
            var updateRequest = OrderUpdateRequest(
                orderId = element.orderId,
                orderStatus = ORDER_STATUS_DELIVERED,
                paymentStatus = PAYMENT_STATUS_PAID,
                orderedQuantity = null,
                confirmedQuantity = null,
                discountAmount = null,
                orderedAmount = null,
                orderComment = null,
                deliveryComment = null
            )
            orderUpdateRequestList.add(updateRequest)
        }
        return orderUpdateRequestList
    }


    // call button
    fun onCallUserButtonClicked(uiElement: DeliverItemsModel){
        _selectedUiElement.value = uiElement
    }


    fun moreDetailsButtonClicked(element: DeliverItemsModel) {
        if(element.isMoreDetailsDisplayed){
            _visibleUiData.value?.filter { data -> data.orderId.equals(element.orderId) }
                ?.firstOrNull()?.isMoreDetailsDisplayed = false
            _visibleUiData.value = _visibleUiData.value
            return
        }

        // Do API call to fetch comments
        fetchCommentsForOrder(element)

        _visibleUiData.value?.filter { data -> data.orderId.equals(element.orderId) }
            ?.firstOrNull()?.isMoreDetailsDisplayed = true
        _visibleUiData.value = _visibleUiData.value
    }

    private fun fetchCommentsForOrder(element: DeliverItemsModel) {
        setCommentProgressBar(true, element)
        coroutineScope.launch {
            var getCommentsDataDeferred = CommentApi.retrofitService.getComments(element.orderId)
            try {
                val comments: List<Comment> = getCommentsDataDeferred.await()
                _visibleUiData.value?.filter { data -> data.orderId.equals(element.orderId) }
                    ?.firstOrNull()?.comments = ArrayList(comments)
                _visibleUiData.value = _visibleUiData.value
            } catch (t: Throwable) {
                println(t.message)
            }
            setCommentProgressBar(false, element)
        }
    }


    fun onSendCommentButtonClicked(element: DeliverItemsModel){
        if(element.newComment.isNullOrBlank()){
            return
        }

        var request = CommentCreateRequest(
            comment = element.newComment,
            commenter = sessionData.value?.userName ?: "",
            commenterUserId = sessionData.value?.userId ?: -1,
            orderId = element.orderId,
            createdBy = sessionData.value?.userName ?: "",
            updatedBy = sessionData.value?.userName ?: ""
        )

        setCommentProgressBar(true, element)
        coroutineScope.launch {
            var createCommentsDataDeferred = CommentApi.retrofitService.createComment(request)
            try{
                createCommentsDataDeferred.await()
                // Do API call to refresh comments
                fetchCommentsForOrder(element)
                clearCommentTypeBox(element)
            } catch (t:Throwable){
                println(t.message)
            }
            setCommentProgressBar(false, element)
        }

    }


    private fun setCommentProgressBar(isProgressActive: Boolean, element: DeliverItemsModel){
        _visibleUiData.value?.filter { data -> data.orderId.equals(element.orderId) }
            ?.firstOrNull()?.isCommentProgressBarActive = isProgressActive
        _visibleUiData.value = _visibleUiData.value
    }

    private fun clearCommentTypeBox(element: DeliverItemsModel){
        _visibleUiData.value?.filter { data -> data.orderId.equals(element.orderId) }
            ?.firstOrNull()?.newComment = ""
        _visibleUiData.value = _visibleUiData.value
    }

}