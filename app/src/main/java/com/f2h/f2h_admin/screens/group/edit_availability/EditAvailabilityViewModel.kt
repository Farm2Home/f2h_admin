package com.f2h.f2h_admin.screens.group.edit_availability

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.REPEAT_NO_REPEAT
import com.f2h.f2h_admin.constants.F2HConstants.REPEAT_WEEKLY
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.ItemAvailability
import com.f2h.f2h_admin.network.models.ItemAvailabilityUpdateRequest
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class EditAvailabilityViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    val itemAvailabilityId = MutableLiveData<Long>()
    val availableQuantity = MutableLiveData<String>()
    val isAvailabilityFreezed = MutableLiveData<Boolean>()
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

    private val df_iso: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val df: DateFormat = SimpleDateFormat("EEEE, dd-MM-yyyy")
    private var existingAvailability = ItemAvailability()
    private var _sessionData = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        _isAvailabilityActionComplete.value = false
        fetchSessionData()
        fetchAvailability()
    }

    private fun fetchAvailability() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            val getItemAvailabilityDataDeferred = ItemAvailabilityApi
                .retrofitService.getItemAvailabilities((itemAvailabilityId.value ?: -1).toString())
            try {
                existingAvailability = getItemAvailabilityDataDeferred.await().first()
                availableQuantity.value = existingAvailability.availableQuantity.toString()
                isAvailabilityFreezed.value = existingAvailability.isFreezed
                createSpinnerEntries()
            } catch (t: Throwable) {
                _toastText.value = "Oops something went wrong"
            }
            _isProgressBarActive.value = false
        }
    }

    private fun fetchSessionData() {
        coroutineScope.launch {
            _sessionData = retrieveSession()
        }
    }

    private fun createSpinnerEntries() {
        dateStringList.value = getFormattedDateList()
        repeatFeaturesList.value = arrayListOf(getRepeatFeatureFromRepeatDay(existingAvailability.repeatDay ?: 0))
            .plus(arrayListOf("No Repeat", "Weekly").minus(getRepeatFeatureFromRepeatDay(existingAvailability.repeatDay ?: 0)))
    }

    private fun getFormattedDateList(): ArrayList<String> {
        return arrayListOf(df.format(df_iso.parse(existingAvailability.availableDate).time) ?: "")
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
        selectedDate.value = existingAvailability.availableDate
    }


    private fun isAnyFieldInvalid(): Boolean {
        if (selectedRepeatFeature.value.isNullOrBlank()) {
            _toastText.value = "Please select a valid Repeat Feature"
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

    fun onUpdateAvailabilityButtonClicked() {
        if(isAnyFieldInvalid()){
            return
        }
        updateItemAvailability()
    }


    fun calculateRepeatDayFromRepeatFeature(repeatFeature: String?): Long  {
        if (repeatFeature.equals(REPEAT_NO_REPEAT)){
            return 0
        }
        if (repeatFeature.equals(REPEAT_WEEKLY)){
            return 7
        }
        return 0
    }

    fun getRepeatFeatureFromRepeatDay(repeatDay: Long): String {
        if (repeatDay.equals(0L)){
            return REPEAT_NO_REPEAT
        }
        if (repeatDay.equals(7L)){
            return REPEAT_WEEKLY
        }
        return ""
    }


    private fun updateItemAvailability() {
        var requestBody = ItemAvailabilityUpdateRequest(
            itemAvailabilityId = itemAvailabilityId.value,
            availableDate = null, //Should not change
            availableTimeSlot = null,
            committedQuantity = null,
            availableQuantity = availableQuantity.value?.toDouble(),
            isFreezed = isAvailabilityFreezed.value,
            repeatDay = calculateRepeatDayFromRepeatFeature(selectedRepeatFeature.value),
            updatedBy = _sessionData.userName
        )

        _isProgressBarActive.value = true
        coroutineScope.launch {
            val updateItemAvailabilityDataDeferred = ItemAvailabilityApi
                .retrofitService.updateItemAvailabilities(arrayListOf(requestBody))
            try {
                updateItemAvailabilityDataDeferred.await()
                _toastText.value = "Successfully updated item availability"
                _isAvailabilityActionComplete.value = true
            } catch (t: Throwable) {
                _toastText.value = "Oops something went wrong"
            }
            _isProgressBarActive.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}