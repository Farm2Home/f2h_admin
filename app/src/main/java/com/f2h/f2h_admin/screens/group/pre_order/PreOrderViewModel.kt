package com.f2h.f2h_admin.screens.group.pre_order

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.ItemApi
import com.f2h.f2h_admin.network.ItemAvailabilityApi
import com.f2h.f2h_admin.network.models.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PreOrderViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _preOrderItems = MutableLiveData<ArrayList<AvailabilityItemsModel>>()
    val availabilityItems: LiveData<ArrayList<AvailabilityItemsModel>>
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
        createPreOrderUiElements(Item(), arrayListOf())
    }


    fun fetchAllData(itemId: Long) {
        selectedItemId = itemId
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData = retrieveSession()
            try {

                // Fetch Item Data
                val getItemDataDeferred = ItemApi.retrofitService(getApplication()).getItem(itemId)
                val item = getItemDataDeferred.await()

                //Fetch all availabilities for the item
                val getItemAvailabilitiesDeferred = ItemAvailabilityApi.retrofitService(getApplication()).getItemAvailabilitiesByItemId(selectedItemId)
                val itemAvailabilities = ArrayList(getItemAvailabilitiesDeferred.await())

                //Create the UI Model to populate UI
                _preOrderItems.value = createPreOrderUiElements(item, itemAvailabilities)

            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun createPreOrderUiElements(item: Item, itemAvailabilities: ArrayList<ItemAvailability>): ArrayList<AvailabilityItemsModel> {
        var list = arrayListOf<AvailabilityItemsModel>()

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

        itemAvailabilities.filter { isWithinDateRange(it) }
            .forEach { availability ->
                var availabilityItem = AvailabilityItemsModel()
                availabilityItem.itemAvailabilityId = availability.itemAvailabilityId ?: -1L
                availabilityItem.availableDate = availability.availableDate ?: ""
                availabilityItem.availableTimeSlot = availability.availableTimeSlot ?: ""
                availabilityItem.availableQuantity = availability.availableQuantity.toString()
                availabilityItem.committedQuantity = availability.committedQuantity.toString()
                availabilityItem.itemUom = item.uom ?: ""
                availabilityItem.isFreezed = availability.isFreezed ?: false
                availabilityItem.repeatDay = availability.repeatDay ?: 0

                list.add(availabilityItem)
            }

        list.sortBy { it.availableDate }
        return list
    }

    private fun isWithinDateRange(it: ItemAvailability) =
        compareDates(it.availableDate, startDate) >= 0 &&
                compareDates(it.availableDate, endDate) <= 0


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



}