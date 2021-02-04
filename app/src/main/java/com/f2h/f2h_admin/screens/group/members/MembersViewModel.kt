package com.f2h.f2h_admin.screens.group.members

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//import androidx.navigation.Navigation
import com.f2h.f2h_admin.constants.F2HConstants
//import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_BUYER
//import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_BUYER_REQUESTED
//import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_GROUP_ADMIN_REQUESTED
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.*
//import com.f2h.f2h_admin.screens.group..MembershipRequestFragmentDirections
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

    private var _selectedUiElement = MutableLiveData<MembersUiModel>()
    val selectedUiElement: LiveData<MembersUiModel>
        get() = _selectedUiElement

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
            var getGroupMembershipsDeferred = GroupMembershipApi.retrofitService(getApplication()).getGroupMembership(sessionData.value!!.groupId, null)
            try {
                var memberships = getGroupMembershipsDeferred.await()
                var userIds = memberships.map { x -> x.userId ?: -1 }.distinct()
                var getUserDetailsDataDeferred = UserApi.retrofitService(getApplication()).getUserDetailsByUserIds(userIds.joinToString())
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
                uiElement.currency = sessionData.value?.groupCurrency ?: ""
                uiElement.userId = membershipUserDetail.userId ?: -1
                uiElement.userName = membershipUserDetail.userName ?: ""
                uiElement.deliveryAddress = membershipUserDetail.address ?: ""
                uiElement.mobile = membershipUserDetail.mobile ?: ""
                uiElement.email = membershipUserDetail.email ?: ""
                uiElement.roles = membership.roles ?: ""
                uiElement.deliveryCharge = membership.baseDeliveryCharge ?: 0.0
                uiElement.groupMembershipId = membership.groupMembershipId ?: -1
                uiElement.deliveryAreaId = membership.deliveryAreaId ?: -1
                var roles = membership.roles?.split(",")



//                if(roles?.contains(USER_ROLE_BUYER_REQUESTED) ?: false || roles?.contains(
//                        USER_ROLE_GROUP_ADMIN_REQUESTED) ?: false){
//                    uiElement.isBuyerRequested = true
//                }

                F2HConstants.REQUESTED_ROLES?.forEach {
                    if (roles!!.contains(it)){
                        uiElement.isBuyerRequested = true
                    }
                }

//                if(roles?.any(F2HConstants.REQUESTED_ROLES::contains)?: false){
//                    uiElement.isBuyerRequested = true
//                }
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
        filteredItems.sortByDescending { it.isBuyerRequested }
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

    // delete button
    fun onDeleteUserButtonClicked(uiElement: MembersUiModel){
        _toastMessage.value = String.format("Delete user clicked for %s", uiElement.userName)
    }

    // call button
    fun onCallUserButtonClicked(uiElement: MembersUiModel){
        _selectedUiElement.value = uiElement
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}