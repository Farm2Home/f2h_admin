package com.f2h.f2h_admin.screens.group.daily_orders

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.OrderApi
import com.f2h.f2h_admin.network.models.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DailyOrdersViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _visibleUiData = MutableLiveData<MutableList<DailyOrdersUiModel>>()
    val visibleUiData: LiveData<MutableList<DailyOrdersUiModel>>
        get() = _visibleUiData

    private var _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date>
        get() = _selectedDate

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val sessionData = MutableLiveData<SessionEntity>()


    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var allUiData = ArrayList<DailyOrdersUiModel>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _selectedDate.value = Calendar.getInstance().time
        _isProgressBarActive.value = true
        getItemsAndAvailabilitiesForUser()
    }


    fun getItemsAndAvailabilitiesForUser() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData.value = retrieveSession()
            var getOrdersDataDeferred = OrderApi.retrofitService.getOrdersForUserAndGroup(sessionData.value!!.groupId, sessionData.value!!.userId)
            try {
                var orders = getOrdersDataDeferred.await()
                var availabilityIds: ArrayList<Long> = arrayListOf()
                orders.forEach { order ->
                    availabilityIds.add(order.itemAvailabilityId ?: -1)
                }
                var getItemAvailabilitiesDataDeferred = ItemAvailabilityApi.retrofitService.getItemAvailabilities(availabilityIds)
                var itemAvailabilities = getItemAvailabilitiesDataDeferred.await()
                allUiData = createAllUiData(itemAvailabilities, orders)
                if (allUiData.size > 0) {
                    _visibleUiData.value = filterVisibleItems(allUiData)
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }



    private fun createAllUiData(itemAvailabilitys: List<ItemAvailability>, orders: List<Order>): ArrayList<DailyOrdersUiModel> {
        var allUiData = ArrayList<DailyOrdersUiModel>()
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<Item> = moshi.adapter(Item::class.java)

        orders.forEach { order ->
            var uiElement = DailyOrdersUiModel()
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
                uiElement.farmerName = item.farmerUserName ?: ""
                uiElement.price = item.pricePerUnit ?: 0.0
                uiElement.orderQtyJump = item.orderQtyJump ?: 0.0
            }
            uiElement.orderedDate = df.format(df.parse(order.orderedDate))
            uiElement.orderedQuantity = order.orderedQuantity ?: 0.0
            uiElement.confirmedQuantity = order.confirmedQuantity ?: 0.0
            uiElement.orderId = order.orderId ?: -1L
            uiElement.orderAmount = order.orderedAmount ?: 0.0
            uiElement.discountAmount = order.discountAmount ?: 0.0
            uiElement.orderStatus = order.orderStatus ?: ""
            uiElement.paymentStatus = order.paymentStatus ?: ""
            uiElement.orderComment = order.orderComment ?: ""
            uiElement.deliveryComment = order.deliveryComment ?: ""

            allUiData.add(uiElement)
        }

        allUiData.sortBy { it.itemName }
        allUiData.sortByDescending { it.orderedQuantity }
        return allUiData
    }



    private fun filterVisibleItems(elements: List<DailyOrdersUiModel>): ArrayList<DailyOrdersUiModel> {
        var filteredItems = ArrayList<DailyOrdersUiModel>()
        elements.forEach {element ->
            if (isDateEqual(element.orderedDate, df.format(_selectedDate.value))){
                    filteredItems.add(element.copy())
            }
        }
        return filteredItems
    }


    private fun isDateEqual(itemDate: String, selectedDate: String): Boolean {
        return df.format(df.parse(itemDate)).equals(df.format(df.parse(selectedDate)))
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


    fun updateSelectedDate(date: Date){
        _selectedDate.value = date
        _visibleUiData.value = filterVisibleItems(allUiData)
    }


    // increase order qty till max available qty
    fun increaseOrderQuantity(updateElement: DailyOrdersUiModel){
        _visibleUiData.value?.forEach { uiElement ->
            if (uiElement.orderId.equals(updateElement.orderId)){
                uiElement.orderedQuantity = uiElement.orderedQuantity.plus(uiElement.orderQtyJump)
                uiElement.quantityChange = uiElement.quantityChange.plus(uiElement.orderQtyJump)

                // logic to prevent increasing quantity beyond maximum
                if (uiElement.quantityChange > uiElement.availableQuantity) {
                    uiElement.orderedQuantity = uiElement.orderedQuantity.minus(uiElement.orderQtyJump)
                    uiElement.quantityChange = uiElement.quantityChange.minus(uiElement.orderQtyJump)
                    _toastMessage.value = "No more stock"
                }

                uiElement.orderAmount = calculateOrderAmount(uiElement)
            }
        }
        _visibleUiData.value = _visibleUiData.value
    }


    // decrease order qty till min 0
    fun decreaseOrderQuantity(updateElement: DailyOrdersUiModel){
        _visibleUiData.value?.forEach { uiElement ->
            if (uiElement.orderId.equals(updateElement.orderId)){
                uiElement.orderedQuantity = uiElement.orderedQuantity.minus(uiElement.orderQtyJump)
                uiElement.quantityChange = uiElement.quantityChange.minus(uiElement.orderQtyJump)
                if (uiElement.orderedQuantity < 0) uiElement.orderedQuantity = 0.0
                uiElement.orderAmount = calculateOrderAmount(uiElement)
            }
        }
        _visibleUiData.value = _visibleUiData.value
    }


    private fun calculateOrderAmount(uiElement: DailyOrdersUiModel): Double {
        return uiElement.orderedQuantity.times(uiElement.price)
    }


    fun onClickSaveButton() {
        _isProgressBarActive.value = true
        var orderUpdates = arrayListOf<OrderUpdateRequest>()
        _visibleUiData.value?.forEach { uiElement ->
            var orderUpdate = OrderUpdateRequest(
                orderId = uiElement.orderId,
                orderStatus = uiElement.orderStatus,
                discountAmount = uiElement.discountAmount,
                orderedAmount = uiElement.orderAmount,
                orderedQuantity = uiElement.orderedQuantity
            )
            orderUpdates.add(orderUpdate)
        }

        coroutineScope.launch {
            var updateOrdersDataDeferred = OrderApi.retrofitService.updateOrders(orderUpdates)
            try{
                updateOrdersDataDeferred.await()
                getItemsAndAvailabilitiesForUser()
            } catch (t:Throwable){
                println(t.message)
                _toastMessage.value = "Out of stock"
            }
            _isProgressBarActive.value = false
        }
    }


    fun onClickCancelButton() {
        _visibleUiData.value = filterVisibleItems(allUiData)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}