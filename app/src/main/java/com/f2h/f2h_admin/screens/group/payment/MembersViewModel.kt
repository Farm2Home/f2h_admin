package com.f2h.f2h_admin.screens.group.payment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_DELIVERED
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PAID
import com.f2h.f2h_admin.constants.F2HConstants.PAYMENT_STATUS_PENDING
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
    private var _selectedStatus = MutableLiveData<String>()

    private val sessionData = MutableLiveData<SessionEntity>()

    private val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")

    private var allUiData = ArrayList<MembersUiModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        getUserDetailsInGroup()
    }


    fun getUserDetailsInGroup() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData.value = retrieveSession()
            val getOrdersHeaderDeferred = OrderApi.retrofitService(getApplication()).getOrderHeaderForGroup(sessionData.value!!.groupId, startDate(), endDate())
            try {
                val orderHeaders = getOrdersHeaderDeferred.await()
                val buyerIds = arrayListOf<Long>(-1).plus(orderHeaders.map { x -> x.buyerUserId ?: -1})
                    .plus(orderHeaders.flatMap { x -> x.orders!!.map { it.sellerUserId }}).distinct()

                val getMemberWallets = WalletApi.retrofitService(getApplication()).getWalletDetails(sessionData.value!!.groupId, null)
                val getUserDetailsDeferred = UserApi.retrofitService(getApplication()).getUserDetailsByUserIds(buyerIds.joinToString())
                val getUserMembershipDetailsDeferred = GroupMembershipApi.retrofitService(getApplication()).getGroupMembership(sessionData.value!!.groupId, null)

                val buyerUserDetails = getUserDetailsDeferred.await()
                val buyerMembershipDetails = getUserMembershipDetailsDeferred.await()
                val memberWallets = getMemberWallets.await()

                allUiData = createAllUiData(orderHeaders, buyerUserDetails, buyerMembershipDetails, memberWallets)
                createAllUiFilters()
                filterVisibleItems()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }



    private fun createAllUiData(orderHeaders: List<OrderHeader>, userDetails: List<UserDetails>,
                                userMemberships: List<GroupMembership>,
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
            val buyer = userDetails.firstOrNull { x ->
                x.userId?.equals(orderHeader.buyerUserId) ?: false
            }
            val wallet = memberWallets.firstOrNull{ x ->
                x.userId?.equals(orderHeader.buyerUserId) ?: false
            }
            val uiElement = MembersUiModel()
            uiElement.currency = sessionData.value?.groupCurrency ?: ""
            uiElement.userId = orderHeader.buyerUserId ?: -1
            uiElement.orderHeaderId = orderHeader.orderHeaderId ?: -1
            uiElement.userName = buyer!!.userName ?: ""
            uiElement.deliveryAddress = buyer.address ?: ""
            uiElement.mobile = buyer.mobile ?: ""
            uiElement.email = buyer.email ?: ""
            uiElement.walletId = wallet!!.walletId ?: -1
            uiElement.walletBalance = wallet.balance ?: 0.0
            uiElement.packingNumber = orderHeader.packingNumber ?: -1
            uiElement.deliveryDate = orderHeader.deliveryDate ?: ""
            uiElement.totalAmount = orderHeader.final_amount ?: 0.0
            uiElement.groupMembershipId = userMembership?.groupMembershipId
            uiElement.serviceOrder = orderHeader.serviceOrders?: arrayListOf()
            val paymentItemsList = arrayListOf<PaymentItemsModel>()
            orderHeader.orders!!.forEach { order ->
                if (order.orderStatus == F2HConstants.ORDER_STATUS_REJECTED){
                    return@forEach
                }
                val paymentItemsModel = PaymentItemsModel()
                var item = Item()
                try {
                    item = jsonAdapter.fromJson(order.orderDescription?:"") ?: Item()
                } catch (e: Exception){
                    Log.e("Parse Error", e.message ?: "")
                }
                paymentItemsModel.currency = sessionData.value?.groupCurrency ?: ""
                paymentItemsModel.itemId = item.itemId ?: -1
                paymentItemsModel.itemName = item.itemName ?: ""
                paymentItemsModel.itemDescription = item.description ?: ""
                paymentItemsModel.itemUom = item.uom ?: ""
                paymentItemsModel.itemImageLink = item.imageLink ?: ""
                paymentItemsModel.price = item.pricePerUnit ?: 0.0

                paymentItemsModel.orderedQuantity = order.orderedQuantity ?: 0.0

                if(order.orderStatus.equals(F2HConstants.ORDER_STATUS_ORDERED)) {
                    paymentItemsModel.confirmedQuantity =  order.orderedQuantity ?: 0.0
                } else {
                    paymentItemsModel.confirmedQuantity = order.confirmedQuantity ?: 0.0
                }
                val sellerUserDetails =
                    userDetails.firstOrNull { x -> x.userId?.equals(order.sellerUserId) ?: false }

                paymentItemsModel.sellerName = sellerUserDetails?.userName ?: ""
                paymentItemsModel.sellerMobile = sellerUserDetails?.mobile ?: ""

                paymentItemsModel.orderId = order.orderId ?: -1L
                paymentItemsModel.orderDescription = order.orderDescription ?: ""
                paymentItemsModel.orderAmount = order.orderedAmount ?: 0.0
                paymentItemsModel.discountAmount = order.discountAmount ?: 0.0
                paymentItemsModel.orderStatus = order.orderStatus ?: ""
                paymentItemsModel.paymentStatus = order.paymentStatus ?: ""
                paymentItemsModel.sellerUserId = order.sellerUserId ?: -1

                paymentItemsModel.displayQuantity = getDisplayQuantity(paymentItemsModel.orderStatus,
                    paymentItemsModel.orderedQuantity, paymentItemsModel.confirmedQuantity)

                paymentItemsList.add(paymentItemsModel)
            }
            uiElement.paymentItems = paymentItemsList
            uiElement.anyPaymentCompletedOrder = getPaymentCompletedOrder(uiElement.paymentItems)
            uiElement.anyPaymentPendingOrder = getPaymentPendingOrder(uiElement.paymentItems)
            uiElement.remainingAmount = getRemainingAmount(uiElement)

            uiElement.amountCollected = uiElement.remainingAmount - uiElement.walletBalance
            if (uiElement.amountCollected < 0){
                uiElement.amountCollected = 0.0
            }
            allUiData.add(uiElement)
        }
        return allUiData
    }

    private fun getPaymentCompletedOrder(element: List<PaymentItemsModel>): Boolean{
        return element.filter{x -> x.paymentStatus.toUpperCase() == PAYMENT_STATUS_PAID }.any()
    }

    private fun getRemainingAmount(uiElement: MembersUiModel): Double{
        var remainingAmount = 0.0
        uiElement.paymentItems.filter { x -> x.paymentStatus != PAYMENT_STATUS_PAID }.forEach {
            remainingAmount += it.orderAmount
        }
        uiElement.serviceOrder.filter { x -> x.paymentStatus != PAYMENT_STATUS_PAID }.forEach {
            remainingAmount += it.amount?:0.0
        }
        return remainingAmount
    }

    private fun getPaymentPendingOrder(element: List<PaymentItemsModel>): Boolean{
        return element.filter{x -> x.paymentStatus.toUpperCase() != PAYMENT_STATUS_PAID }.any()
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
        val selectedStatus = _selectedStatus.value ?: ""

        elements.forEach { element ->
            if (isInSelectedDateRange(element, selectedStartDate, selectedEndDate) &&
                ((selectedStatus == PAYMENT_STATUS_PENDING && element.anyPaymentPendingOrder)
                        ||(selectedStatus == PAYMENT_STATUS_PAID && element.anyPaymentCompletedOrder))
            ) {
                filteredItems.add(element)
            }
        }

        filteredItems.sortBy { it.userName }
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

    fun getInitialStatusIndex(): Int{
        return _uiFilterModel.value?.statusList?.indexOf(_selectedStatus.value)?:0
    }

    fun getInitialTimeIndex(): Int{
        val rangeStartDate = Calendar.getInstance()
        val today = formatter.format(rangeStartDate.time)
        if (_selectedDate.value!! == today){
            return 0
        }
        return _uiFilterModel.value?.timeFilterList?.indexOf(_selectedDate.value!!)?:0
    }

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
            uiModel.statusList = arrayListOf(PAYMENT_STATUS_PENDING, PAYMENT_STATUS_PAID)

            // Set date range as today
            setTimeFilterRange(TIME_SELECTION_TODAY)
            _selectedStatus.value = PAYMENT_STATUS_PENDING
            //Refresh filter
            _uiFilterModel.value = uiModel
        }
    }

    fun onTimeFilterSelected(position: Int) {
        val selectedTime = _uiFilterModel.value?.timeFilterList?.get(position) ?: TIME_SELECTION_TODAY
        setTimeFilterRange(selectedTime)
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


    fun onCheckboxClicked(selectedUiModel: PaymentItemsModel) {
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



    private fun setCommentProgressBar(isProgressActive: Boolean, element: PaymentItemsModel){
        element.isCommentProgressBarActive = isProgressActive
        _visibleUiData.value = _visibleUiData.value
    }

    private fun clearCommentTypeBox(element: PaymentItemsModel){
        element.newComment = ""
        _visibleUiData.value = _visibleUiData.value
    }


    private fun fetchCommentsForOrder(element: PaymentItemsModel) {
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

    fun moreDetailsButtonClicked(element: PaymentItemsModel) {
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

    fun onSendCommentButtonClicked(element: PaymentItemsModel){
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


    fun onCashCollectedButtonClicked(element: MembersUiModel) {
        _isProgressBarActive.value = true
        val deliveredOrderUpdateRequest = createPaymentOrderRequests(element.paymentItems)
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
                _toastMessage.value = "Payment collected Successfully"
                element.paymentItems.filter { it.isItemChecked }.forEach { deliverItem ->
                    deliverItem.isItemChecked = false
                }
                updatedOrders.orders?.forEach {updatedOrder ->
                    val order = element.paymentItems.first { x-> updatedOrder.orderId == x.orderId }
                    order.orderStatus = updatedOrder.orderStatus ?: ""
                    order.paymentStatus = updatedOrder.paymentStatus ?:""
                    order.orderAmount = updatedOrder.orderedAmount ?: 0.0
                }
                element.anyPaymentCompletedOrder = getPaymentCompletedOrder(element.paymentItems)
                element.anyPaymentPendingOrder = getPaymentPendingOrder(element.paymentItems)
                element.remainingAmount = getRemainingAmount(element)

                element.amountCollected = element.remainingAmount - element.walletBalance
                if (element.amountCollected < 0){
                    element.amountCollected = 0.0
                }
                filterVisibleItems()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
            _isProgressBarActive.value = false
            element.isProgressBarActive = false
        }
    }


    private fun createPaymentOrderRequests(uiDataElements: List<PaymentItemsModel>?): List<OrderUpdateRequest> {
        val orderUpdateRequestList: ArrayList<OrderUpdateRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked && it.paymentStatus != PAYMENT_STATUS_PAID
                && it.orderStatus != F2HConstants.ORDER_STATUS_REJECTED}?.forEach { element ->
            val updateRequest = OrderUpdateRequest(
                orderId = element.orderId,
                orderStatus = ORDER_STATUS_DELIVERED,
                paymentStatus = PAYMENT_STATUS_PAID,
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