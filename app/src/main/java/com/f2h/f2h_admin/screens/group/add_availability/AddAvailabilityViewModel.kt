package com.f2h.f2h_admin.screens.group.add_availability

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.ItemAvailabilityCreateRequest
import com.f2h.f2h_admin.network.models.Uom
import com.f2h.f2h_admin.network.models.UserDetails
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddAvailabilityViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    val itemId = MutableLiveData<Long>()
    val availableQuantity = MutableLiveData<String>()
    val selectedDate = MutableLiveData<String>()
    val selectedRepeatFeature = MutableLiveData<String>()
    val dateStringList = MutableLiveData<List<String>>()
    val repeatFeaturesList = MutableLiveData<List<String>>()

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private val _isAvailabilityActionComplete = MutableLiveData<Boolean>()
    val isAvailabilityActionComplete: LiveData<Boolean>
        get() = _isAvailabilityActionComplete

    private var _sessionData = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val df_iso: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00'Z'")
    private val df: DateFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")

    init {
        _isAvailabilityActionComplete.value = false
        fetchSessionData()
        createSpinnerEntries()
    }

    private fun fetchSessionData() {
        coroutineScope.launch {
            _sessionData = retrieveSession()
        }
    }

    private fun createSpinnerEntries() {
        dateStringList.value = getDateList()
        repeatFeaturesList.value = arrayListOf("No Repeat", "Weekly")
    }

    private fun getDateList(): ArrayList<String> {
        val dateList = arrayListOf<String>()
        val date: Calendar = Calendar.getInstance()
        date.set(Calendar.HOUR, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        dateList.add(df.format(date.time))
        for (dayOffset in 0..6){
            date.add(Calendar.DATE, 1)
            dateList.add(df.format(date.time))
        }
        return dateList
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


    fun onRepeatFeatureSelected(position: Int) {
        selectedRepeatFeature.value = repeatFeaturesList.value?.get(position)
    }

    fun onDateSelected(position: Int) {
        selectedDate.value = dateStringList.value?.get(position)
    }


    private fun isAnyFieldInvalid(): Boolean {

        if (selectedRepeatFeature.value.isNullOrBlank()) {
            _toastText.value = "Please select a valid Repeat Feature"
            return true
        }
        if (selectedDate.value.isNullOrBlank()) {
            _toastText.value = "Please select a valid date"
            return true
        }
        if (!isDecimal(availableQuantity.value.toString())) {
            _toastText.value = "Please enter a valid available quantity number"
            return true
        }

        return false
    }

    protected fun isDecimal(numberString: String): Boolean{
        try {
            val num = numberString.toDouble()
        } catch (e: Exception) {
            return false
        }
        return true;
    }

    fun onAddAvailabilityButtonClicked() {
        if(isAnyFieldInvalid()){
            return
        }
        createItemAvailability()
    }


    fun calculateRepeatDayFromRepeatFeature(repeatFeature: String?): Long  {
        if (repeatFeature.equals("No Repeat")){
            return 0
        }
        if (repeatFeature.equals("Weekly")){
            return 7
        }
        return 0
    }

    fun calculateISODateFromFormatted(date: String?): String {
        return df_iso.format(df.parse(date))
    }

    private fun createItemAvailability() {
        var requestBody = ItemAvailabilityCreateRequest(
            itemId = itemId.value,
            availableDate = calculateISODateFromFormatted(selectedDate.value),
            availableTimeSlot = "",
            committedQuantity = 0.0,
            availableQuantity = availableQuantity.value?.toDouble(),
            isFreezed = false,
            repeatDay = calculateRepeatDayFromRepeatFeature(selectedRepeatFeature.value),
            createdBy = _sessionData.userName,
            updatedBy = _sessionData.userName
        )

        _isProgressBarActive.value = true
        coroutineScope.launch {
            val createItemAvailabilityDataDeferred = ItemAvailabilityApi.retrofitService.createItemAvailability(requestBody)
            try {
                createItemAvailabilityDataDeferred.await()
                _toastText.value = "Successfully created a new availability"
                _isAvailabilityActionComplete.value = true
            } catch (t: Throwable) {
                _toastText.value = "Oops something went wrong, maybe availability exists"
            }
            _isProgressBarActive.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}