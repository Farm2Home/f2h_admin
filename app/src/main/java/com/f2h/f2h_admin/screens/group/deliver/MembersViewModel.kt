package com.f2h.f2h_admin.screens.group.deliver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.DELIVERY_AREA_NOT_ASSIGNED
import com.f2h.f2h_admin.constants.F2HConstants.DELIVERY_STATUS_COMPLETED
import com.f2h.f2h_admin.constants.F2HConstants.DELIVERY_STATUS_NOT_DONE
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.TIME_SELECTION_TODAY
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


class MembersViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _uiFilterModel = MutableLiveData<UiFilterModel>()
    val uiFilterModel: LiveData<UiFilterModel>
        get() = _uiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<MembersUiModel>>()
    val visibleUiData: LiveData<MutableList<MembersUiModel>>
        get() = _visibleUiData

    private var _selectedUiElement = MutableLiveData<MembersUiModel>()
    val selectedUiElement: LiveData<MembersUiModel>
        get() = _selectedUiElement

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private var _selectedDate = MutableLiveData<String>()
    private var _selectedArea = MutableLiveData<String>()
    private var _selectedStatus = MutableLiveData<String>()

    private val _hasExitGroup = MutableLiveData<Boolean>()
    val hasExitGroup: LiveData<Boolean>
        get() = _hasExitGroup

    private val sessionData = MutableLiveData<SessionEntity>()

    val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")

    private var allUiData = ArrayList<MembersUiModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        _hasExitGroup.value = false
        getUserDetailsInGroup()
    }


    fun getUserDetailsInGroup() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData.value = retrieveSession()
            val getOrdersForDeliveryAgentDeferred = OrderApi.retrofitService(getApplication()).getOrderHeaderForGroup(sessionData.value!!.groupId, startDate(), endDate())
            try {
                val orderHeaders = getOrdersForDeliveryAgentDeferred.await()
                val buyerIds = arrayListOf<Long>(-1).plus(orderHeaders.map { x -> x.buyerUserId ?: -1})
                    .plus(orderHeaders.flatMap { x -> x.orders!!.map { it.sellerUserId }}).distinct()

                val getMemberWallets = WalletApi.retrofitService(getApplication()).getWalletDetails(sessionData.value!!.groupId, null)
                val getDeliveryArea = DeliveryAreaApi.retrofitService(getApplication()).getDeliveryAreaDetails(sessionData.value!!.groupId)
                val getUserDetailsDeferred = UserApi.retrofitService(getApplication()).getUserDetailsByUserIds(buyerIds.joinToString())
                val getUserMembershipDetailsDeferred = GroupMembershipApi.retrofitService(getApplication()).getGroupMembership(sessionData.value!!.groupId, null)

                val buyerUserDetails = getUserDetailsDeferred.await()
                val buyerMembershipDetails = getUserMembershipDetailsDeferred.await()
                val deliveryAreaList = getDeliveryArea.await()
                val memberWallets = getMemberWallets.await()

                allUiData = createAllUiData(orderHeaders, buyerUserDetails, buyerMembershipDetails,
                    deliveryAreaList, memberWallets)
                createAllUiFilters()
                filterVisibleItems()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }



    private fun createAllUiData(orderHeaders: List<OrderHeader>, userDetails: List<UserDetails>,
                                userMemberships: List<GroupMembership>, deliveryAreaList: List<DeliveryArea>,
                                memberWallets: List<Wallet>): ArrayList<MembersUiModel> {
        val allUiData = ArrayList<MembersUiModel>()


        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)

        orderHeaders.forEach{orderHeader ->
            val userMembership =
                userMemberships.firstOrNull { x ->
                    x.userId?.equals(orderHeader.buyerUserId) ?: false
                }
            val deliveryArea = deliveryAreaList.firstOrNull { x ->
                x.deliveryAreaId?.equals(userMembership?.deliveryAreaId) ?: false
            }
            val buyer = userDetails.firstOrNull { x ->
                x.userId?.equals(orderHeader.buyerUserId) ?: false
            }
            val wallet = memberWallets.firstOrNull{ x ->
                x.userId?.equals(orderHeader.buyerUserId) ?: false
            }
            val uiElement = MembersUiModel()
            uiElement.currency = sessionData.value?.groupCurrency ?: ""
            uiElement.orderHeaderId = orderHeader.orderHeaderId ?: -1
            uiElement.userId = orderHeader.buyerUserId ?: -1
            uiElement.userName = buyer!!.userName ?: ""
            uiElement.deliveryAddress = buyer.address ?: ""
            uiElement.mobile = buyer.mobile ?: ""
            uiElement.email = buyer.email ?: ""
            uiElement.walletId = wallet!!.walletId ?: -1
            uiElement.walletBalance = wallet.balance ?: 0.0



            uiElement.packingNumber = orderHeader.packingNumber ?: -1
            uiElement.deliveryDate = orderHeader.deliveryDate ?: ""
            uiElement.totalAmount = orderHeader.final_amount ?: 0.0
            uiElement.deliverySequence = userMembership?.deliverySequence ?: 0
            uiElement.groupMembershipId = userMembership?.groupMembershipId
            uiElement.deliveryArea = deliveryArea?.deliveryArea ?: ""

            val deliveryItemsList = arrayListOf<DeliverItemsModel>()
            orderHeader.orders!!.forEach { order ->
                if (order.orderStatus == F2HConstants.ORDER_STATUS_REJECTED){
                    return@forEach
                }
                val deliverItemsModel = DeliverItemsModel()
                var item = Item()
                try {
                    item = jsonAdapter.fromJson(order.orderDescription) ?: Item()
                } catch (e: Exception){
                    Log.e("Parse Error", e.message ?: "")
                }
                deliverItemsModel.currency = sessionData.value?.groupCurrency ?: ""
                deliverItemsModel.itemId = item.itemId ?: -1
                deliverItemsModel.itemName = item.itemName ?: ""
                deliverItemsModel.itemDescription = item.description ?: ""
                deliverItemsModel.itemUom = item.uom ?: ""
                deliverItemsModel.itemImageLink = item.imageLink ?: ""
                deliverItemsModel.price = item.pricePerUnit ?: 0.0

                deliverItemsModel.orderedQuantity = order.orderedQuantity ?: 0.0

                if(order.orderStatus.equals(F2HConstants.ORDER_STATUS_ORDERED)) {
                    deliverItemsModel.confirmedQuantity =  order.orderedQuantity ?: 0.0
                } else {
                    deliverItemsModel.confirmedQuantity = order.confirmedQuantity ?: 0.0
                }
                val sellerUserDetails =
                    userDetails.firstOrNull { x -> x.userId?.equals(order.sellerUserId) ?: false }

                deliverItemsModel.sellerName = sellerUserDetails?.userName ?: ""
                deliverItemsModel.sellerMobile = sellerUserDetails?.mobile ?: ""

                deliverItemsModel.orderDescription = order.orderDescription ?: ""
                deliverItemsModel.orderId = order.orderId ?: -1L
                deliverItemsModel.orderAmount = order.orderedAmount ?: 0.0
                deliverItemsModel.discountAmount = order.discountAmount ?: 0.0
                deliverItemsModel.orderStatus = order.orderStatus ?: ""
                deliverItemsModel.paymentStatus = order.paymentStatus ?: ""
                deliverItemsModel.sellerUserId = order.sellerUserId ?: -1

                deliverItemsModel.displayQuantity = getDisplayQuantity(deliverItemsModel.orderStatus,
                    deliverItemsModel.orderedQuantity, deliverItemsModel.confirmedQuantity)
                deliverItemsModel.packetCount = order.numberOfPackets ?: 0


                deliverItemsModel.receivedPacketCount = order.receivedNumberOfPackets ?: -1
                if (deliverItemsModel.receivedPacketCount == -1L){
                    deliverItemsModel.receivedPacketCount = deliverItemsModel.packetCount
                    deliverItemsModel.isReceived = false
                }
                else{
                    deliverItemsModel.isReceived = true
                }

                deliveryItemsList.add(deliverItemsModel)
            }
            uiElement.deliveryItems = deliveryItemsList
            uiElement.anyDeliveredOrder = getDeliveredOrder(uiElement.deliveryItems)
            uiElement.anyOpenOrder = getAnyOpenOrder(uiElement.deliveryItems)
            uiElement.remainingAmount = getRemainingAmount(uiElement)
            allUiData.add(uiElement)
        }
        return allUiData
    }

    private fun getRemainingAmount(uiElement: MembersUiModel): Double{
        var remainingAmount = 0.0
        uiElement.deliveryItems.filter { x -> x.paymentStatus != F2HConstants.PAYMENT_STATUS_PAID }.forEach {
            remainingAmount += it.orderAmount
        }
        uiElement.serviceOrder.filter { x -> x.paymentStatus != F2HConstants.PAYMENT_STATUS_PAID }.forEach {
            remainingAmount += it.amount?:0.0
        }
        return remainingAmount
    }

    private fun getDeliveredOrder(element: List<DeliverItemsModel>): Boolean{
        return element.filter{x -> x.orderStatus == ORDER_STATUS_DELIVERED }.any()
    }

    private fun getAnyOpenOrder(element: List<DeliverItemsModel>): Boolean{
        return element.filter{x -> x.orderStatus != ORDER_STATUS_DELIVERED }.any()
    }


    private fun getDisplayQuantity(displayStatus: String, orderedQuantity: Double, confirmedQuantity: Double): Double {
        if (displayStatus == F2HConstants.ORDER_STATUS_ORDERED) return orderedQuantity
        return confirmedQuantity
    }


    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<MembersUiModel>()
        val selectedStartDate = _selectedDate.value ?: formatter.format(todayDate.time)
        val selectedEndDate = _selectedDate.value ?: formatter.format(todayDate.time)
        val selectedDeliveryArea = _selectedArea.value ?: ""
        val selectedStatus = _selectedStatus.value ?: ""

        elements.forEach { element ->
            if (isInSelectedDateRange(element, selectedStartDate, selectedEndDate) &&
                (selectedDeliveryArea == "ALL"
                        || element.deliveryArea == selectedDeliveryArea
                        || (selectedDeliveryArea == DELIVERY_AREA_NOT_ASSIGNED && element.deliveryArea == "")) &&
                ((selectedStatus == DELIVERY_STATUS_NOT_DONE && element.anyOpenOrder)
                        ||(selectedStatus == DELIVERY_STATUS_COMPLETED && element.anyDeliveredOrder))
            ) {
                filteredItems.add(element)
            }
        }

        filteredItems.sortBy { it.userName }
        filteredItems.sortBy { it.deliverySequence }
        _visibleUiData.value = filteredItems
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

    private fun isInSelectedDateRange(
        element: MembersUiModel,
        selectedStartDate: String,
        selectedEndDate: String
    ) : Boolean {

        var orderedDateObject = parser.parse(element.deliveryDate)
        val cal = Calendar.getInstance()
        cal.time = orderedDateObject
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        orderedDateObject = cal.time

        return orderedDateObject >= formatter.parse(selectedStartDate) &&
                orderedDateObject <= formatter.parse(selectedEndDate)

    }

    private fun startDate(): String {
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'")
        val today = Calendar.getInstance()
        today.add(Calendar.DATE, -8)
        return formatter.format(today.time)
    }

    private fun endDate(): String {
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'")
        val today = Calendar.getInstance()
        today.add(Calendar.DATE, 7)
        return formatter.format(today.time)
    }

    // call button
    fun onCallUserButtonClicked(uiElement: MembersUiModel){
        _selectedUiElement.value = uiElement
    }


//    fun getInitialDeliveryIndex(): Int{
//        return _uiFilterModel.value?.deliveryAreaList?.indexOf(_selectedArea.value)?:0
//    }
//
//    fun getInitialStatusIndex(): Int{
//        return _uiFilterModel.value?.statusList?.indexOf(_selectedStatus.value)?:0
//    }

//    fun getInitialTimeIndex(): Int{
//        val rangeStartDate = Calendar.getInstance()
//        val today = formatter.format(rangeStartDate.time)
//        if (_selectedDate.value!! == today){
//            return 0
//        }
//        return _uiFilterModel.value?.timeFilterList?.indexOf(_selectedDate.value!!)?:0
//    }

    private fun createAllUiFilters() {

        val uiModel = UiFilterModel()
        coroutineScope.launch {
            sessionData.value = retrieveSession()

            val orderedDates = allUiData.map{ x -> formatter.format(parser.parse(x.deliveryDate))}

            val rangeStartDate = Calendar.getInstance()
            val today = formatter.format(rangeStartDate.time)

            uiModel.timeFilterList = arrayListOf(TIME_SELECTION_TODAY).plus(
                orderedDates
                    .filter { uiElement -> uiElement != today }
                    .distinctBy { it }
                    .sorted())
            uiModel.statusList = arrayListOf(DELIVERY_STATUS_NOT_DONE, DELIVERY_STATUS_COMPLETED)
            uiModel.deliveryAreaList = arrayListOf("ALL").plus(
                allUiData
                    .filter { uiElement -> !uiElement.deliveryArea.isBlank() }
                    .distinctBy { it.deliveryArea }
                    .map { uiElement -> uiElement.deliveryArea }
                    .sorted())
            .plus(DELIVERY_AREA_NOT_ASSIGNED)

            //Refresh filter
            _uiFilterModel.value = uiModel
        }
    }

    fun onTimeFilterSelected(position: Int) {
        val selectedTime = _uiFilterModel.value?.timeFilterList?.get(position) ?: TIME_SELECTION_TODAY
        setTimeFilterRange(selectedTime)
        filterVisibleItems()
    }

    fun onDeliveryAreaSelected(position: Int) {
        _selectedArea.value = _uiFilterModel.value?.deliveryAreaList?.get(position) ?: F2HConstants.ALL_DELIVERY_AREA
        filterVisibleItems()
    }

    fun onStatusSelected(position: Int) {
        _selectedStatus.value = _uiFilterModel.value?.statusList?.get(position) ?: ""
        filterVisibleItems()
    }

    private fun setTimeFilterRange(selectedDate: String) {
        val rangeStartDate = Calendar.getInstance()
        val today = formatter.format(rangeStartDate.time)
        if (selectedDate == TIME_SELECTION_TODAY) {
            _selectedDate.value = today
        }
        else{
            _selectedDate.value = selectedDate
        }
    }

    fun onUpdateDeliverySequenceButtonClicked() {
        _isProgressBarActive.value = true
        var position: Long = 0
        val requests: ArrayList<GroupMembershipUpdateRequest> = arrayListOf()
        visibleUiData.value?.forEach { element ->
            element.deliverySequence = position
            position++

            val deliverySequenceUpdateRequest = GroupMembershipUpdateRequest(
                element.groupMembershipId,
                element.deliverySequence,
                sessionData.value?.userName
            )

            requests.add(deliverySequenceUpdateRequest)
        }

        coroutineScope.launch {
            val getGroupMembershipDataDeferred = GroupMembershipApi.retrofitService(getApplication()).updateGroupMembershipList(requests)
            try {
                getGroupMembershipDataDeferred.await()
            } catch (t:Throwable){
                println(t.message)
                _toastMessage.value = "Oops, something went wrong!"
            }
            _isProgressBarActive.value = false
        }
    }


    fun onCheckboxClicked(selectedUiModel: DeliverItemsModel) {
        selectedUiModel.isItemChecked = !selectedUiModel.isItemChecked
        _visibleUiData.value = _visibleUiData.value

    }

    fun onMemberSelected(element: MembersUiModel) {
        if(element.isItemsDisplayed){
            element.isItemsDisplayed = false
            _visibleUiData.value = _visibleUiData.value
            return
        }

        _visibleUiData.value?.forEach { data ->
            data.isItemsDisplayed = data.userId == element.userId
        }
        _visibleUiData.value = _visibleUiData.value
    }

    fun onReceiveButtonClicked(element: DeliverItemsModel) {
        element.isReceived = true
        updateAsReceived(element)
        _visibleUiData.value = _visibleUiData.value

    }

    private fun updateAsReceived(element: DeliverItemsModel) {
        val updateReceivedNumber = OrderReceivedNumberUpdateRequest(
            orderId = element.orderId,
            receivedNumberOfPackets = element.receivedPacketCount
        )
        println(updateReceivedNumber)
        coroutineScope.launch {
            val updateReceivedOrderApi = OrderApi.retrofitService(getApplication()).updateReceivedNumber(arrayListOf(updateReceivedNumber))
            try {
                updateReceivedOrderApi.await()

            } catch (t: Throwable) {
                println(t.message)
            }
            _visibleUiData.value = _visibleUiData.value
        }
    }

    private fun setCommentProgressBar(isProgressActive: Boolean, element: DeliverItemsModel){
        element.isCommentProgressBarActive = isProgressActive
        _visibleUiData.value = _visibleUiData.value
    }

    private fun clearCommentTypeBox(element: DeliverItemsModel){
        element.newComment = ""
        _visibleUiData.value = _visibleUiData.value
    }


    private fun fetchCommentsForOrder(element: DeliverItemsModel) {
        setCommentProgressBar(true, element)
        coroutineScope.launch {
            val getCommentsDataDeferred = CommentApi.retrofitService.getComments(element.orderId)
            try {
                val comments: List<Comment> = getCommentsDataDeferred.await()
                element.comments = ArrayList(comments)
                _visibleUiData.value = _visibleUiData.value
            } catch (t: Throwable) {
                println(t.message)
            }
            setCommentProgressBar(false, element)
        }
    }

    fun moreDetailsButtonClicked(element: DeliverItemsModel) {
        if(element.isMoreDetailsDisplayed){
            element.isMoreDetailsDisplayed = false
            _visibleUiData.value = _visibleUiData.value
            return
        }
        // Do API call to fetch comments
        fetchCommentsForOrder(element)
        element.isMoreDetailsDisplayed = true
        _visibleUiData.value = _visibleUiData.value
    }

    fun onSendCommentButtonClicked(element: DeliverItemsModel){
        if(element.newComment.isBlank()){
            return
        }

        val request = CommentCreateRequest(
            comment = element.newComment,
            commenter = sessionData.value?.userName ?: "",
            commenterUserId = sessionData.value?.userId ?: -1,
            orderId = element.orderId,
            createdBy = sessionData.value?.userName ?: "",
            updatedBy = sessionData.value?.userName ?: ""
        )

        setCommentProgressBar(true, element)
        coroutineScope.launch {
            val createCommentsDataDeferred = CommentApi.retrofitService.createComment(request)
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

    fun onDeliverButtonClicked(element: MembersUiModel) {
        _isProgressBarActive.value = true
        val deliveredOrderUpdateRequest = createDeliverOrderRequests(element.deliveryItems)
        if (deliveredOrderUpdateRequest.isEmpty()){
            _isProgressBarActive.value = false
            _toastMessage.value = "Please select the orders to deliver"
            return
        }

        val deliveryRequest =  OrderHeaderDeliveryRequest (
            orders = deliveredOrderUpdateRequest,
            collectedCash = element.amountCollected,
            orderHeaderId = element.orderHeaderId,
            walletId = element.walletId,
            groupId = sessionData.value?.groupId,
            buyerId = element.userId,
            buyerName = element.userName,
            deliveryDate = element.deliveryDate,
            updatedBy = sessionData.value?.userName
        )

        _isProgressBarActive.value = true
        element.isProgressBarActive = true
        coroutineScope.launch {
            try{
                val deliverOrdersDataDeferred = OrderApi.retrofitService(getApplication()).headerCashCollected(deliveryRequest)
                val updatedOrders = deliverOrdersDataDeferred.await()
                _toastMessage.value = "Successfully delivered orders"
                element.deliveryItems.filter { it.isItemChecked }.forEach { deliverItem ->
                    deliverItem.orderStatus = ORDER_STATUS_DELIVERED
                    deliverItem.isItemChecked = false
                }
                updatedOrders.orders?.forEach {updatedOrder ->
                    val order = element.deliveryItems.first { x-> updatedOrder.orderId == x.orderId }
                    order.orderStatus = updatedOrder.orderStatus ?: ""
                    order.paymentStatus = updatedOrder.paymentStatus ?:""
                    order.orderAmount = updatedOrder.orderedAmount ?: 0.0
                }
                element.anyDeliveredOrder = getDeliveredOrder(element.deliveryItems)
                element.anyOpenOrder = getAnyOpenOrder(element.deliveryItems)
                element.remainingAmount = getRemainingAmount(element)
                element.amountCollected = 0.0
                filterVisibleItems()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
            _isProgressBarActive.value = false
            element.isProgressBarActive = false
        }
    }


    private fun createDeliverOrderRequests(uiDataElements: List<DeliverItemsModel>?): List<OrderUpdateRequest> {
        val orderUpdateRequestList: ArrayList<OrderUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked && it.orderStatus != ORDER_STATUS_DELIVERED
                && it.orderStatus != F2HConstants.ORDER_STATUS_REJECTED}?.forEach { element ->
            val updateRequest = OrderUpdateRequest(
                orderId = element.orderId,
                orderStatus = ORDER_STATUS_DELIVERED,
                paymentStatus = F2HConstants.PAYMENT_STATUS_PAID,
                orderDescription = element.orderDescription,
                orderedQuantity = null,
                confirmedQuantity = element.confirmedQuantity,
                discountAmount = null,
                orderedAmount = null,
                orderComment = null,
                deliveryComment = null
            )
            orderUpdateRequestList.add(updateRequest)
        }
        return orderUpdateRequestList
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}