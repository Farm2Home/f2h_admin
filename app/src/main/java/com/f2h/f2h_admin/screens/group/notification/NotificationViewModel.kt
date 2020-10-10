package com.f2h.f2h_admin.screens.group.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.NotificationApi
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class NotificationViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _visibleUiData = MutableLiveData<MutableList<NotificationItemsModel>>()
    val visibleUiData: LiveData<MutableList<NotificationItemsModel>>
        get() = _visibleUiData

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private var selectedNotificationItem = NotificationItemsModel()
    private var allUiData = ArrayList<NotificationItemsModel>()
    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getNotificationMessageList()
    }

    private fun getNotificationMessageList() {
        //Clear all screen data
        allUiData.clear()

        _isProgressBarActive.value = true
        coroutineScope.launch {
            userSession = retrieveSession()
            try {
                var notificationMessages = NotificationApi.retrofitService(getApplication()).getAllNotificationMessages().await()
                notificationMessages.forEach{ message ->
                    var notificationMessageItem = NotificationItemsModel(
                        message.notificationId,
                        message.title,
                        message.body,
                        false
                    )
                    allUiData.add(notificationMessageItem)
                }

                allUiData.sortByDescending { it.title }
                _visibleUiData.value = allUiData
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }

    fun onSendNotificationButtonClicked() {
        if (selectedNotificationItem.notificationId == null ||
            selectedNotificationItem.notificationId!! <= 0){
            _toastText.value = "Please select at least one item"
            return
        }
        _isProgressBarActive.value = true
        coroutineScope.launch {
            val sendNotificationDeferred = NotificationApi.retrofitService(getApplication())
                .sendNotificationToGroups(selectedNotificationItem.notificationId!!,
                    userSession.groupId!!.toString())
            try {
                sendNotificationDeferred.await()
            } catch (t:Throwable){
                println(t.message)
                _toastText.value = "Successfully sent alerts"
            }
            _isProgressBarActive.value = false
        }
    }


    fun onNotificationMessageSelected(selectedItem: NotificationItemsModel){
        selectedNotificationItem = selectedItem
        _visibleUiData.value?.forEach { item ->
            item.isSelected = false
            if (item.notificationId == selectedItem.notificationId){
                item.isSelected = !item.isSelected
            }
        }
        _visibleUiData.value = _visibleUiData.value
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
