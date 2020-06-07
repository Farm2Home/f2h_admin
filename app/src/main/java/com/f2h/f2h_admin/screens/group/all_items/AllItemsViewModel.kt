package com.f2h.f2h_admin.screens.group.all_items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.ItemApi
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.network.models.ItemAvailability
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllItemsViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _visibleItems = MutableLiveData<List<Item>>()
    val visibleItems: LiveData<List<Item>>
        get() = _visibleItems

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private val _sessionData = MutableLiveData<SessionEntity>()

    private var allItems = ArrayList<Item>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        getItemsAndAvailabilitiesForGroup()
    }


    fun refreshFragmentData(){
        _isProgressBarActive.value = true
        getItemsAndAvailabilitiesForGroup()
    }


    private fun getItemsAndAvailabilitiesForGroup() {
        coroutineScope.launch {
            _sessionData.value = retrieveSession()
            var getItemsDataDeferred = ItemApi.retrofitService.getItemsForGroup(_sessionData.value!!.groupId)
            try {
                allItems = ArrayList(getItemsDataDeferred.await())
                if (allItems.size > 0) {
                    allItems.sortBy { it.itemName }
                    filterEarliestAvailableItemAsVisibleItems(allItems)
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private fun filterEarliestAvailableItemAsVisibleItems(items: List<Item>) {
        var filteredItems = ArrayList<Item>()
        items.forEach {item ->
            if(!item.itemAvailability.isEmpty()) {
                var earliestItemAvailability = fetchEarliestItemAvailability(item)
                item.itemAvailability = arrayOf(earliestItemAvailability).toList()
            }
            filteredItems.add(item)
        }
        _visibleItems.value = filteredItems
    }


    private fun fetchEarliestItemAvailability(item: Item): ItemAvailability {
        val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var earliestItemAvailability = ItemAvailability()

        var itemUpcoming = removeOlderItemAvailability(item)
        if (!itemUpcoming.itemAvailability.isEmpty()) {
            earliestItemAvailability = itemUpcoming.itemAvailability[0]
            itemUpcoming.itemAvailability.forEach { itemAvailability ->
                val itemAvailabilityDate = parser.parse(itemAvailability.availableDate)
                val earliestAvailabilityDate = parser.parse(earliestItemAvailability.availableDate)
                if (itemAvailabilityDate <= earliestAvailabilityDate) {
                    earliestItemAvailability = itemAvailability
                }
            }
        }
        return earliestItemAvailability
    }


    private fun removeOlderItemAvailability(item: Item): Item {
        var upcomingItemAvailabilities = ArrayList<ItemAvailability>()
        val parser: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        var todaysDate = Calendar.getInstance()
        todaysDate.time = parser.parse(formatter.format(Date()))

        item.itemAvailability.forEach{itemAvailability ->
            val date = parser.parse(itemAvailability.availableDate)
            if(date >= todaysDate.time){
                upcomingItemAvailabilities.add(itemAvailability)
            }
        }
        item.itemAvailability = upcomingItemAvailabilities
        return item
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
}