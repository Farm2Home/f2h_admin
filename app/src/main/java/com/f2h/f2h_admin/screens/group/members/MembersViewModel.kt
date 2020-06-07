package com.f2h.f2h_admin.screens.group.members

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.*
import kotlinx.coroutines.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class MembersViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _visibleUiData = MutableLiveData<MutableList<MembersUiModel>>()
    val visibleUiData: LiveData<MutableList<MembersUiModel>>
        get() = _visibleUiData

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private val sessionData = MutableLiveData<SessionEntity>()


    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
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
            var getGroupMembershipsDeferred = GroupMembershipApi.retrofitService.getGroupMembership(sessionData.value!!.groupId)
            try {
                var memberships = getGroupMembershipsDeferred.await()
                var userIds = memberships.map { x -> x.userId ?: -1 }.distinct()
                var getUserDetailsDataDeferred = UserApi.retrofitService.getUserDetailsByUserIds(userIds)
                var userDetails = getUserDetailsDataDeferred.await()
                allUiData = createAllUiData(memberships, userDetails)
                if (allUiData.size > 0) {
                    _visibleUiData.value = filterVisibleItems(allUiData)
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }



    private fun createAllUiData(memberships: List<GroupMembership>, userDetails: List<UserDetails>): ArrayList<MembersUiModel> {
        var allUiData = ArrayList<MembersUiModel>()
        memberships.forEach { membership ->
            var uiElement = MembersUiModel()
            var membershipUserDetail = userDetails.filter { x -> x.userId?.equals(membership.userId) ?: false }.first()
            if (membershipUserDetail != null){
                uiElement.userId = membershipUserDetail.userId ?: -1
                uiElement.userName = membershipUserDetail.userName ?: ""
                uiElement.deliveryAddress = membershipUserDetail.address ?: ""
                uiElement.mobile = membershipUserDetail.mobile ?: ""
                uiElement.email = membershipUserDetail.email ?: ""
                uiElement.roles = membership.roles ?: ""
            }
            allUiData.add(uiElement)
        }

        return allUiData
    }



    private fun filterVisibleItems(elements: List<MembersUiModel>): ArrayList<MembersUiModel> {
        var filteredItems = ArrayList<MembersUiModel>()
        elements.forEach {element ->
            filteredItems.add(element.copy())
        }
        filteredItems.sortBy { it.userName }
        return filteredItems
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

    // decrease order qty till min 0
    fun decreaseOrderQuantity(updateElement: MembersUiModel){
        _toastMessage.value = String.format("Delete user clicked for %s", updateElement.userName)
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}