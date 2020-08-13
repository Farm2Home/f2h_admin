package com.f2h.f2h_admin.screens.group.accept_reject_membership

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.GroupMembershipApi
import com.f2h.f2h_admin.network.DeliveryAreaApi
import com.f2h.f2h_admin.network.models.GroupMembershipRequest
import kotlinx.coroutines.*

class MembershipRequestViewModel (val database: SessionDatabaseDao, application: Application,
                                  navArgs: MembershipRequestFragmentArgs) : AndroidViewModel(application) {
    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _isMembershipActionComplete = MutableLiveData<Boolean>()
    val isMembershipActionComplete: LiveData<Boolean>
        get() = _isMembershipActionComplete

    private var _requestedRolesUiData = MutableLiveData<MutableList<MembershipRequestUiModel>>()
    val requestedRolesUiData: LiveData<MutableList<MembershipRequestUiModel>>
        get() = _requestedRolesUiData

    private var _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private var _deliveryAddress = MutableLiveData<String>()
    val deliveryAddress: LiveData<String>
        get() = _deliveryAddress

    private var _mobile = MutableLiveData<String>()
    val mobile: LiveData<String>
        get() = _mobile

    private var _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private var _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage

    private var _selectedDeliveryAreaId = MutableLiveData<Long>()
    val selectedDeliveryAreaId: LiveData<Long>
        get() = _selectedDeliveryAreaId

    private var _initialDeliveryAreaId = MutableLiveData<Long>()
    val initialDeliveryAreaId: LiveData<Long>
        get() = _initialDeliveryAreaId

    private var _deliveryAreaItems = MutableLiveData<DeliveryAreaItem>()
    val deliveryAreaItems: LiveData<DeliveryAreaItem>
        get() = _deliveryAreaItems

    private var sessionData = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private var groupMembershipId : Long = -1

    init {
        _isProgressBarActive.value = true
        _isMembershipActionComplete.value = false
        _userName.value = navArgs.memberUiModel.userName ?: ""
        _deliveryAddress.value = navArgs.memberUiModel.deliveryAddress ?: ""
        _mobile.value = navArgs.memberUiModel.mobile ?: ""
        _email.value = navArgs.memberUiModel.email ?: ""
        groupMembershipId = navArgs.memberUiModel.groupMembershipId ?: -1
        _selectedDeliveryAreaId.value = null
        getMembershipsRequested(navArgs)

    }

    fun getMembershipsRequested(navArgs: MembershipRequestFragmentArgs){
        _isProgressBarActive.value = true
        coroutineScope.launch {
            sessionData = retrieveSession()
            var deliveryAreaNameList = ArrayList<String>()
            var deliveryAreaIdList = ArrayList<Long>()
            deliveryAreaNameList.add("Not Assigned")
            deliveryAreaIdList.add(-1L)
            try{
                var getDeliveryArea =
                    DeliveryAreaApi.retrofitService.getDeliveryAreaDetails(sessionData.groupId)
                var deliveryArea = getDeliveryArea.await()
                deliveryArea.forEach{
                    deliveryAreaIdList.add(it.deliveryAreaId?:-1L)
                    deliveryAreaNameList.add(it.deliveryArea?:"")
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _deliveryAreaItems.value = DeliveryAreaItem(deliveryAreaIdList, deliveryAreaNameList)
            try {

                var modifiedRoles = navArgs.memberUiModel.roles.split(",")
                var allUiData = ArrayList<MembershipRequestUiModel>()

                modifiedRoles.forEach{ role ->
                    if (role.trim() in F2HConstants.REQUESTED_ROLES) {
                        var uiElement = MembershipRequestUiModel()
                        uiElement.role = role
                        uiElement.requestedRole = true
                        allUiData.add(uiElement)
                    }
                    else if (role.trim() in F2HConstants.ACCEPTED_ROLES) {
                        var uiElement = MembershipRequestUiModel()
                        uiElement.role = role
                        uiElement.requestedRole = false
                        allUiData.add(uiElement)
                    }
                }
                _requestedRolesUiData.value = filterVisibleItems(allUiData)
                _initialDeliveryAreaId.value = navArgs.memberUiModel.deliveryAreaId
            } catch (t:Throwable){
                println(t.message)
            }

            _isProgressBarActive.value = false
        }
    }
    
    private fun filterVisibleItems(elements: List<MembershipRequestUiModel>): ArrayList<MembershipRequestUiModel> {
        var filteredItems = ArrayList<MembershipRequestUiModel>()
        elements.forEach {element ->
            filteredItems.add(element.copy())
        }
        filteredItems.sortBy { it.role }
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

    fun onMembershipActionSelected(position: Int, id: Long, uiElement: MembershipRequestUiModel) {
        uiElement.selected = uiElement.action?.get(position)
    }

    fun onDeliveryAreaSelected(position: Int, id: Long) {
        _selectedDeliveryAreaId.value = _deliveryAreaItems.value?.id?.get(position)
    }

    fun getInitialIndex(): Int{
        var returnVal = _deliveryAreaItems.value?.id?.indexOf(_initialDeliveryAreaId.value)?:0
        return returnVal
    }

    // Accept button
    fun onOkButtonClick(){
        acceptBuyerMembership()
    }

    fun acceptBuyerMembership() {
        _isProgressBarActive.value = true
        var acceptedRoles = ArrayList<String>()
        var is_change = true
        _requestedRolesUiData.value!!.forEach { el ->
            if (!el.requestedRole || el.selected?.contains(F2HConstants.ROLE_REQUEST_PENDING)){
                acceptedRoles.add(el.role.trim())
            }
            else if (el.selected?.contains(F2HConstants.ROLE_REQUEST_ACCEPT)) {
                is_change = true
                F2HConstants.REQUESTED_ROLE_TO_ACCEPTED_ROLE[el.role]?.let {acceptedRoles.add(it)}
            }
            else if (el.selected?.contains(F2HConstants.ROLE_REQUEST_REJECT)){
                is_change = true
            }
        }

        if(is_change && acceptedRoles.size == 0){
            coroutineScope.launch {
                var deleteGroupMembershipDataDeferred =
                    GroupMembershipApi.retrofitService.deleteGroupMembership(groupMembershipId)
                try {
                    var deleteMembership = deleteGroupMembershipDataDeferred.await()
                    _isMembershipActionComplete.value = true
                } catch (t:Throwable){
                    println(t.message)
                }
            }
        }
        else if(is_change){
            var membershipRequest = GroupMembershipRequest(
                null,
                selectedDeliveryAreaId.value,
                null,
                acceptedRoles.joinToString(","),
                null
            )
            coroutineScope.launch {
                var updateGroupMembershipDataDeferred =
                    GroupMembershipApi.retrofitService.updateGroupMembership(groupMembershipId, membershipRequest)
                try {
                    var updatedMembership = updateGroupMembershipDataDeferred.await()
                    _isMembershipActionComplete.value = true
                } catch (t:Throwable){
                    println(t.message)
                }
            }
        }
        else{
            _isMembershipActionComplete.value = true
        }
        _isProgressBarActive.value = false

    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}