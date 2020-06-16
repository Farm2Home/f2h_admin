package com.f2h.f2h_admin.screens.group.pre_order

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.ItemApi
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.OrderApi
import com.f2h.f2h_admin.network.models.*
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PreOrderViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _preOrderItems = MutableLiveData<ArrayList<PreOrderItemsModel>>()
    val preOrderItems: LiveData<ArrayList<PreOrderItemsModel>>
        get() = _preOrderItems

    private val _preOrderUiModel = MutableLiveData<PreOrderUiModel>()
    val preOrderUiModel: LiveData<PreOrderUiModel>
        get() = _preOrderUiModel

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val df_iso: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'")
    private val preOrderDaysMax = 10
    private var startDate = ""
    private var endDate = ""
    private var selectedItemId = 0L

    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var sessionData = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        setPreOrderDateRange()
        createPreOrderUiElements(Item(), arrayListOf(), arrayListOf())
    }


    fun fetchAllData(itemId: Long) {
        selectedItemId = itemId
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData = retrieveSession()
            try {

                // Fetch Item Data
                val getItemDataDeferred = ItemApi.retrofitService.getItem(itemId)
                val item = getItemDataDeferred.await()

                //Fetch existing Orders Data
                val getOrdersDataDeferred = OrderApi.retrofitService.getOrdersForGroupUserAndItem(item.groupId ?: 0,
                    sessionData.userId, item.itemId!!, startDate, endDate)
                val orders = ArrayList(getOrdersDataDeferred.await())

                //Fetch all availabilities for the item
                val getItemAvailabilitiesDeferred = ItemAvailabilityApi.retrofitService.getItemAvailabilitiesByItemId(item.itemId!!)
                val itemAvailabilities = ArrayList(getItemAvailabilitiesDeferred.await())

                //Create the UI Model to populate UI
                _preOrderItems.value = createPreOrderUiElements(item, orders, itemAvailabilities)

            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createPreOrderUiElements(item: Item, orders: ArrayList<Order>, itemAvailabilities: ArrayList<ItemAvailability>): ArrayList<PreOrderItemsModel> {
        var list = arrayListOf<PreOrderItemsModel>()

        var uiModel = PreOrderUiModel()
        uiModel.itemId = item.itemId ?: -1
        uiModel.itemName = item.itemName ?: ""
        uiModel.itemDescription = item.description ?: ""
        uiModel.itemImageLink = item.imageLink ?: ""
        uiModel.itemPrice = item.pricePerUnit ?: 0.0
        uiModel.itemUom = item.uom ?: ""
        uiModel.farmerName = item.farmerUserName ?: ""
        uiModel.itemImageLink = item.imageLink ?: ""
        _preOrderUiModel.value = uiModel

        itemAvailabilities.filter { compareDates(it.availableDate, startDate) >= 0 &&
                compareDates(it.availableDate, endDate) <= 0 }
            .forEach { availability ->
                var preOrderItem = PreOrderItemsModel()
                preOrderItem.itemAvailabilityId = availability.itemAvailabilityId ?: -1L
                preOrderItem.availableDate = availability.availableDate ?: ""
                preOrderItem.availableTimeSlot = availability.availableTimeSlot ?: ""
                preOrderItem.availableQuantity = availability.availableQuantity ?: 0.0
                preOrderItem.isFreezed = availability.isFreezed ?: false
                preOrderItem.itemUom = item.uom ?: ""
                preOrderItem.orderQuantityJump = item.orderQtyJump ?: 0.0

                var order = orders.filter { it.itemAvailabilityId!!.equals(availability.itemAvailabilityId) }
                if (order.isNotEmpty()) {
                    preOrderItem.orderedQuantity = order.first().orderedQuantity ?: 0.0
                    preOrderItem.confirmedQuantity = order.first().confirmedQuantity ?: 0.0
                    preOrderItem.orderStatus = order.first().orderStatus ?: ""
                    preOrderItem.orderId = order.first().orderId ?: -1L
                }
                list.add(preOrderItem)
            }

        list.sortBy { it.availableDate }
        return list
    }


    private fun compareDates(date1: String?, date2: String?): Long {
        if (date1 == null) return -1
        if (date2 == null) return 1

        val d1 = df.parse(date1).time
        val d2 = df.parse(date2).time
        return d1-d2
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


    private fun setPreOrderDateRange() {
        startDate = getStartDate()
        endDate = getEndDate()
    }

    private fun getStartDate(): String {
        val date: Calendar = Calendar.getInstance()
        val startDate: String = df_iso.format(date.time)
        return startDate
    }


    private fun getEndDate(): String {
        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.DATE, preOrderDaysMax)
        val endDate: String = df_iso.format(date.time)
        return endDate
    }



    // increase order qty till max available qty
    fun increaseOrderQuantity(selectedPreOrder: PreOrderItemsModel){
        _preOrderItems.value?.forEach { preOrderUiElement ->
            if (preOrderUiElement.itemAvailabilityId.equals(selectedPreOrder.itemAvailabilityId)){
                preOrderUiElement.orderedQuantity = preOrderUiElement.orderedQuantity.plus(preOrderUiElement.orderQuantityJump)
                preOrderUiElement.quantityChange = preOrderUiElement.quantityChange.plus(preOrderUiElement.orderQuantityJump)

                // logic to prevent increasing quantity beyond maximum
                if (preOrderUiElement.quantityChange > preOrderUiElement.availableQuantity) {
                    preOrderUiElement.orderedQuantity = preOrderUiElement.orderedQuantity.minus(preOrderUiElement.orderQuantityJump)
                    preOrderUiElement.quantityChange = preOrderUiElement.quantityChange.minus(preOrderUiElement.orderQuantityJump)
                    _toastMessage.value = "No more stock"
                }
            }
        }
        _preOrderItems.value = _preOrderItems.value
    }


    // decrease order qty till min 0
    fun decreaseOrderQuantity(selectedPreOrder: PreOrderItemsModel){
        _preOrderItems.value?.forEach { preOrderUiElement ->
            if (preOrderUiElement.itemAvailabilityId.equals(selectedPreOrder.itemAvailabilityId)){
                preOrderUiElement.orderedQuantity = preOrderUiElement.orderedQuantity.minus(preOrderUiElement.orderQuantityJump)
                preOrderUiElement.quantityChange = preOrderUiElement.quantityChange.minus(preOrderUiElement.orderQuantityJump)

                if (preOrderUiElement.orderedQuantity < 0) {
                    preOrderUiElement.orderedQuantity = 0.0
                    preOrderUiElement.quantityChange = preOrderUiElement.quantityChange.plus(preOrderUiElement.orderQuantityJump)
                }
            }
        }
        _preOrderItems.value = _preOrderItems.value
    }



    fun onClickCancelButton() {
        fetchAllData(selectedItemId)
    }


    fun onClickSaveButton() {
        _isProgressBarActive.value = true
        var newOrders: ArrayList<OrderCreateRequest> = arrayListOf()
        var updatedOrders: ArrayList<OrderUpdateRequest> = arrayListOf()
        _preOrderItems.value?.forEach { preOrder ->

            if(preOrder.quantityChange.equals(0.0)){
                return@forEach
            }

            if (preOrder.orderId <= 0){
                newOrders.add(createNewOrder(preOrder))
            }

            if (preOrder.orderId > 0) {
                updatedOrders.add(createUpdateOrder(preOrder))
            }
        }

        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.updateOrders(updatedOrders)
            var createOrdersDataDeferred = OrderApi.retrofitService.createOrders(newOrders)
            try{
                updateOrdersDataDeferred.await()
                createOrdersDataDeferred.await()
            } catch (t:Throwable){
                println(t.message)
                _toastMessage.value = "Out of stock"
            }

            // Refresh the screen
            fetchAllData(selectedItemId)
        }
    }


    private fun createNewOrder(preOrder: PreOrderItemsModel): OrderCreateRequest {
        var newOrder = OrderCreateRequest(
            buyerUserId = sessionData.userId,
            itemAvailabilityId = preOrder.itemAvailabilityId,
            orderDescription = "Successfully created new order",
            deliveryLocation = sessionData.address,
            orderedQuantity = preOrder.orderedQuantity,
            orderedAmount = calculateOrderAmount(preOrder.orderedQuantity),
            discountAmount = 0.0,
            orderStatus = "ORDERED",
            paymentStatus = "",
            createdBy = "BUYER-" + sessionData.userName,
            updatedBy = "BUYER-" + sessionData.userName
        )
        return newOrder
    }


    private fun createUpdateOrder(preOrder: PreOrderItemsModel): OrderUpdateRequest {
        var updatedOrders = OrderUpdateRequest(
            orderId = preOrder.orderId,
            orderStatus = preOrder.orderStatus,
            paymentStatus = "",
            discountAmount = null,
            orderedQuantity = preOrder.orderedQuantity,
            confirmedQuantity = null,
            orderedAmount = calculateOrderAmount(preOrder.orderedQuantity),
            orderComment = null,
            deliveryComment = null
        )

        return updatedOrders
    }

    private fun calculateOrderAmount(orderedQuantity: Double): Double {
        return orderedQuantity.times(preOrderUiModel.value?.itemPrice ?: 0.0)
    }

}