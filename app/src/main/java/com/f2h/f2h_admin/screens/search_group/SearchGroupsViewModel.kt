package com.f2h.f2h_admin.screens.search_group

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_GROUP_ADMIN
import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_GROUP_ADMIN_REQUESTED
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.GroupApi
import com.f2h.f2h_admin.network.GroupMembershipApi
import com.f2h.f2h_admin.network.LocalityApi
import com.f2h.f2h_admin.network.models.Group
import com.f2h.f2h_admin.network.models.GroupMembershipRequest
import kotlinx.coroutines.*

class SearchGroupsViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _groups = MutableLiveData<List<SearchGroupsItemsModel>>()
    val group: LiveData<List<SearchGroupsItemsModel>>
        get() = _groups

    private val _localities = MutableLiveData<List<String>>()
    val localities: LiveData<List<String>>
        get() = _localities

    private val _selectedLocality = MutableLiveData<List<String>>()
    val selectedLocality: LiveData<List<String>>
        get() = _selectedLocality


    private val roles = listOf<String>(USER_ROLE_GROUP_ADMIN, USER_ROLE_GROUP_ADMIN_REQUESTED)
    private var userGroups = listOf<Group>()
    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        fetchAllLocalities()
        getUserGroupsInformation()
    }

    private fun fetchAllLocalities(){
        _isProgressBarActive.value = true
        coroutineScope.launch {
            var getLocalitiesDataDeferred = LocalityApi.retrofitService(getApplication()).getLocalityDetails()
            try {
                var localityNames = arrayListOf<String>()
                var localityList = getLocalitiesDataDeferred.await()
                localityList.forEach{locality ->
                    localityNames.add(locality.locality ?: "")
                }
                _localities.value = localityNames.sorted()
            } catch (t:Throwable){
                println(t.message)
            }
        }
    }

    private fun getUserGroupsInformation() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            userSession = retrieveSession()
            var getMemberGroupsDataDeferred = GroupApi.retrofitService(getApplication()).getUserGroups(userSession.userId, roles.joinToString())
            try {
                userGroups = getMemberGroupsDataDeferred.await()
                getGroupsInfoForLocalities()
            } catch (t:Throwable){
                println(t.message)
            }
        }
    }

    private fun getGroupsInfoForLocalities() {
        coroutineScope.launch {
            var getSearchGroupsDataDeferred = GroupApi.retrofitService(getApplication()).searchGroupsByLocality(_selectedLocality.value ?: listOf())
            try {
                var searchedGroups = getSearchGroupsDataDeferred.await()
                _groups.value = createSearchGroupsItemList(userGroups, searchedGroups)
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }

    private fun createSearchGroupsItemList(userGroups: List<Group>, searchedGroups: List<Group>): List<SearchGroupsItemsModel>? {
        var searchGroupsItemList = arrayListOf<SearchGroupsItemsModel>()
        searchedGroups.forEach { group ->
            var uiElement = SearchGroupsItemsModel(
                group.groupId ?: -1,
                group.ownerUserId ?: -1,
                group.groupName ?: "",
                group.imageLink ?: "",
                group.description ?: "",
                false
            )

            userGroups.forEach{ userGroup ->
                if (uiElement.groupId.equals(userGroup.groupId)) {
                    uiElement.isAlreadyMember = true
                }
            }
            searchGroupsItemList.add(uiElement)
        }

        return searchGroupsItemList.sortedBy { x -> x.groupName }
    }


    fun onLocalitiesSelected(position: Int) {
        _selectedLocality.value = arrayListOf(localities.value?.get(position) ?: "")
        getUserGroupsInformation()
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

    fun requestMembership(group: SearchGroupsItemsModel) {
        var membershipRequest = GroupMembershipRequest(
            group.groupId,
            null,
            userSession.userId,
            null,
            USER_ROLE_GROUP_ADMIN_REQUESTED,
            userSession.userName
        )
        coroutineScope.launch {
            var getGroupMembershipDataDeferred = GroupMembershipApi.retrofitService(getApplication()).requestGroupMembership(membershipRequest)
            try {
                var requestedMembership = getGroupMembershipDataDeferred.await()
                getUserGroupsInformation()
            } catch (t:Throwable){
                println(t.message)
            }
        }
    }
}