package com.f2h.f2h_admin.screens.group.assign_delivery

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_CONFIRMED
import com.f2h.f2h_admin.constants.F2HConstants.ORDER_STATUS_ORDERED
import com.f2h.f2h_admin.constants.F2HConstants.ASSIGN_STATUS_ASSIGNED
import com.f2h.f2h_admin.constants.F2HConstants.ASSIGN_STATUS_NOT_ASSIGNED
import com.f2h.f2h_admin.constants.F2HConstants.DELIVERY_AREA_NOT_ASSIGNED
import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_DELIVER
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.*
import com.f2h.f2h_admin.screens.deliver.DeliverItemsModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AssignDeliveryViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var isAllItemsSelected: Boolean = false

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _reportUiFilterModel = MutableLiveData<AssignDeliveryUiModel>()
    val reportUiFilterModel: LiveData<AssignDeliveryUiModel>
        get() = _reportUiFilterModel

    private var _visibleUiData = MutableLiveData<MutableList<AssignDeliveryItemsModel>>()
    val visibleUiData: LiveData<MutableList<AssignDeliveryItemsModel>>
        get() = _visibleUiData

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private var _selectedUiElement = MutableLiveData<AssignDeliveryItemsModel>()
//    val selectedUiElement: LiveData<AssignDeliveryItemsModel>
//        get() = _selectedUiElement

    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val requestFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val formatter: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
    private val sessionData = MutableLiveData<SessionEntity>()
    private var allUiData = ArrayList<AssignDeliveryItemsModel>()
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
            var startDate = Calendar.getInstance()
            startDate.add(Calendar.DATE, -1)
            var startDateString = requestFormatter.format(startDate.time)
            var endDate = Calendar.getInstance()
            endDate.add(Calendar.DATE, 3)
            var endDateString = requestFormatter.format(endDate.time)
            var getOrdersDataDeferred =
                OrderApi.retrofitService.getOrdersForGroup(sessionData.value!!.groupId, startDateString, endDateString)
            var getDeliveryArea =
                DeliveryAreaApi.retrofitService.getDeliveryAreaDetails(sessionData.value!!.groupId)

            var getGroupMembershipsDeferred =
                GroupMembershipApi.retrofitService.getGroupMembership(sessionData.value!!.groupId, null)
            try {
                var orders = getOrdersDataDeferred.await()
                var groupMembershipList = getGroupMembershipsDeferred.await()

                var deliveryUserIdsList = groupMembershipList
                    .filter { x -> x.roles!!.split(',').contains(USER_ROLE_DELIVER) }
                    .map { x -> x.userId ?: -1}

                var userIds = orders.map { x -> x.buyerUserId ?: -1}
                    .plus(orders.map { x -> x.sellerUserId ?: -1})
                    .plus(deliveryUserIdsList)
                    .distinct()


                var getUserDetailsDataDeferred =
                    UserApi.retrofitService.getUserDetailsByUserIds(userIds.joinToString())

                var deliveryAreaList = getDeliveryArea.await()
                var userDetailsList = getUserDetailsDataDeferred.await()
                var deliverUserNamesList = deliveryUserIdsList.map { x-> userDetailsList.filter { y -> y.userId == x }.first().userName?: "" }

                var toDeliverOrders = orders.filter { x -> x.orderStatus!!.contains(ORDER_STATUS_ORDERED)
                        || x.orderStatus!!.contains(ORDER_STATUS_CONFIRMED)}

                allUiData = createAllUiData(groupMembershipList, toDeliverOrders, userDetailsList, deliveryAreaList)
                createAllUiFilters(deliveryAreaList, deliverUserNamesList)

                _reportUiFilterModel.value!!.deliveryBoyNameList = arrayListOf("Remove").plus(deliverUserNamesList)
                _reportUiFilterModel.value!!.deliveryBoyIdList = arrayListOf(-1L).plus(deliveryUserIdsList)
                filterVisibleItems()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createAllUiData(groupMemberships: List<GroupMembership>,
                                orders: List<Order>,
                                userDetailsList: List<UserDetails>,
                                deliveryAreaList: List<DeliveryArea>): ArrayList<AssignDeliveryItemsModel> {
        var allUiData = ArrayList<AssignDeliveryItemsModel>()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)
        orders.forEach { order ->
            var uiElement = AssignDeliveryItemsModel()
            var item = Item()
            try {
                item = jsonAdapter.fromJson(order.orderDescription) ?: Item()
            } catch (e: Exception){
                Log.e("Parse Error", e.message)
            }

            if (item != null) {
                uiElement.itemId = item.itemId ?: -1
                uiElement.itemName = item.itemName ?: ""
                uiElement.itemDescription = item.description ?: ""
                uiElement.itemUom = item.uom ?: ""
                uiElement.itemImageLink = item.imageLink ?: ""
                uiElement.price = item.pricePerUnit ?: 0.0
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
            val deliveryUserDetails = userDetailsList.filter { x -> x.userId?.equals(order.deliveryUserId) ?: false }.firstOrNull()
            val buyerMembershipDetails = groupMemberships.filter { x -> x.userId?.equals(order.buyerUserId) ?: false }.firstOrNull()
            val deliveryArea = deliveryAreaList.filter{ x-> x.deliveryAreaId?.equals(buyerMembershipDetails?.deliveryAreaId)?: false}.firstOrNull()

            uiElement.buyerName = buyerUserDetails?.userName ?: ""
            uiElement.buyerMobile = buyerUserDetails?.mobile ?: ""
            uiElement.sellerName = sellerUserDetails?.userName ?: ""
            uiElement.sellerMobile = sellerUserDetails?.mobile ?: ""
            uiElement.deliveryArea = deliveryArea?.deliveryArea ?: ""
            uiElement.orderId = order.orderId ?: -1L
            uiElement.orderAmount = order.orderedAmount ?: 0.0
            uiElement.discountAmount = order.discountAmount ?: 0.0
            uiElement.orderStatus = order.orderStatus ?: ""
            uiElement.paymentStatus = order.paymentStatus ?: ""
            uiElement.buyerUserId = order.buyerUserId ?: -1
            uiElement.sellerUserId = order.sellerUserId ?: -1
            uiElement.deliveryAddress = order.deliveryLocation ?: ""
            uiElement.deliveryBoyId = order.deliveryUserId ?: -1L
            uiElement.deliveryBoyName = deliveryUserDetails?.userName?: ""
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
        _reportUiFilterModel.value = AssignDeliveryUiModel()
        _reportUiFilterModel.value?.selectedDeliveryArea = "ALL"
        _reportUiFilterModel.value?.selectedAssignStatus = "ALL"
        _reportUiFilterModel.value?.selectedBuyer = "ALL"

        // Set date range as today
        var rangeStartDate = Calendar.getInstance()
        var rangeEndDate = Calendar.getInstance()
        _reportUiFilterModel.value?.selectedStartDate = formatter.format(rangeStartDate.time)
        _reportUiFilterModel.value?.selectedEndDate = formatter.format(rangeEndDate.time)
    }


    private fun createAllUiFilters(deliveryAreaList: List<DeliveryArea>,
                                   deliverUserNamesList: List<String>) {
        _reportUiFilterModel.value?.assignStatusList = arrayListOf("ALL", ASSIGN_STATUS_ASSIGNED,
            ASSIGN_STATUS_NOT_ASSIGNED).plus(deliverUserNamesList)

        _reportUiFilterModel.value?.deliveryAreaList = arrayListOf("ALL").plus(deliveryAreaList
            .map{ area -> area.deliveryArea?:""}).plus(arrayListOf(DELIVERY_AREA_NOT_ASSIGNED))

        _reportUiFilterModel.value?.buyerNameList = arrayListOf("ALL")

        var date_2 = Calendar.getInstance()
        date_2.add(Calendar.DATE, 2)
        var dateString2 = formatter.format(date_2.time)

        var date_3 = Calendar.getInstance()
        date_3.add(Calendar.DATE, 3)
        var dateString3 = formatter.format(date_3.time)

        _reportUiFilterModel.value?.timeFilterList = arrayListOf("Today", "Tomorrow", dateString2, dateString3)

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


    private fun generateUniqueFilterName(name: String, mobile: String): String{
        return String.format("%s (%s)",name, mobile)
    }


    private fun filterVisibleItems() {
        val elements = allUiData
        val todayDate = Calendar.getInstance()
        val filteredItems = ArrayList<AssignDeliveryItemsModel>()
        val selectedAssignStatus = reportUiFilterModel.value?.selectedAssignStatus ?: ""
        val selectedDeliveryArea = reportUiFilterModel.value?.selectedDeliveryArea ?: ""
        val selectedStartDate = reportUiFilterModel.value?.selectedStartDate ?: formatter.format(todayDate.time)
        val selectedEndDate = reportUiFilterModel.value?.selectedEndDate ?: formatter.format(todayDate.time)
        val selectedBuyer = reportUiFilterModel.value?.selectedBuyer ?: ""

        elements.forEach { element ->
            if ((selectedAssignStatus == "ALL"
                        || checkAssignedStatus(selectedAssignStatus, element)) &&
                (selectedDeliveryArea == "ALL"
                        || element.deliveryArea.equals(selectedDeliveryArea)
                        || (selectedDeliveryArea == DELIVERY_AREA_NOT_ASSIGNED && element.deliveryArea.equals(""))) &&
                (selectedBuyer == "ALL"
                        || generateUniqueFilterName(element.buyerName, element.buyerMobile).equals(selectedBuyer)) &&
                (isInSelectedDateRange(element, selectedStartDate, selectedEndDate))) {

                //TODO - add date range not just one date
                filteredItems.add(element)
            }
        }
        filteredItems.sortBy { it.buyerName }
        _visibleUiData.value = filteredItems
        reCreateBuyerNameFilterList()
    }

    private fun checkAssignedStatus(selectedAssignStatus: String, element: AssignDeliveryItemsModel): Boolean{
        if (selectedAssignStatus == ASSIGN_STATUS_ASSIGNED && element.deliveryBoyId != -1L){
            return true
        }
        else if (selectedAssignStatus ==ASSIGN_STATUS_NOT_ASSIGNED && element.deliveryBoyId == -1L){
            return true
        }
        else if (element.deliveryBoyName.equals(selectedAssignStatus)){
            return true
        }
        return false
    }

    private fun isInSelectedDateRange(
        element: AssignDeliveryItemsModel,
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



    fun onAssignStatusSelected(position: Int) {

        _reportUiFilterModel.value?.selectedAssignStatus = _reportUiFilterModel.value?.assignStatusList?.get(position) ?: ""

        filterVisibleItems()
    }


    fun onTimeFilterSelected(position: Int) {
        if (position.equals(0)) setTimeFilterRange(0,0) //Today
        if (position.equals(1)) setTimeFilterRange(1,1) //Tomorrow
        if (position.equals(2)) setTimeFilterRange(2,2) //3rd day
        if (position.equals(3)) setTimeFilterRange(3,3) //4th day
        filterVisibleItems()
    }

    fun onBuyerSelected(position: Int) {
        _reportUiFilterModel.value?.selectedBuyer = _reportUiFilterModel.value?.buyerNameList?.get(position) ?: ""
        filterVisibleItems()
    }

    fun onDeliveryAreaSelected(position: Int) {
        _reportUiFilterModel.value?.selectedDeliveryArea = _reportUiFilterModel.value?.deliveryAreaList?.get(position) ?: ""
        filterVisibleItems()
    }

    fun onDeliveryBoySelected(position: Int){
        _reportUiFilterModel.value?.selectedDeliveryBoy = _reportUiFilterModel.value!!.deliveryBoyIdList?.get(position)
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


    fun onCheckBoxClicked(selectedUiModel: AssignDeliveryItemsModel) {
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

    fun onAssignOrderButtonClicked() {
        var orderUpdateRequests = createAssignOrderRequests(visibleUiData.value)
        _isProgressBarActive.value = true
        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.assignOrders(orderUpdateRequests)
            try{
                updateOrdersDataDeferred.await()
                _toastMessage.value = "Successfully assigned orders"
                getOrdersReportForGroup()
            } catch (t:Throwable){
                _toastMessage.value = "Oops, Something went wrong " + t.message
            }
        }
    }

    private fun createAssignOrderRequests(uiDataElements: MutableList<AssignDeliveryItemsModel>?): List<OrderAssignRequest> {
        var orderAssignRequestList: ArrayList<OrderAssignRequest> = arrayListOf()
        uiDataElements?.filter { it.isItemChecked }?.forEach { element ->
            var assignRequest = OrderAssignRequest(
                orderId = element.orderId,
                deliveryUserId = _reportUiFilterModel.value?.selectedDeliveryBoy
            )
            orderAssignRequestList.add(assignRequest)
        }
        return orderAssignRequestList
    }

    private fun calculateOrderAmount(element: AssignDeliveryItemsModel): Double {
        return element.confirmedQuantity * element.price - element.discountAmount
    }

}