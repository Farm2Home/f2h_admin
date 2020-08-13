package com.f2h.f2h_admin.screens.group_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.constants.F2HConstants.USER_ROLE_GROUP_ADMIN
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.GroupApi
import com.f2h.f2h_admin.network.models.Group
import kotlinx.coroutines.*

class GroupsViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _groups = MutableLiveData<List<Group>>()

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _isGroupListEmpty = MutableLiveData<Boolean>()
    val isGroupListEmpty: LiveData<Boolean>
        get() = _isGroupListEmpty

    val group: LiveData<List<Group>>
        get() = _groups


    private val roles = listOf<String>(USER_ROLE_GROUP_ADMIN)
    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        _isGroupListEmpty.value = false
        _isProgressBarActive.value = true
        getUserGroupsInformation()
    }

    fun updateSessionWithGroupInfo(group: Group){
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                database.updateActiveGroup(group.groupId ?: -1, group.description ?: "", group.groupName ?: "")
            }
        }
    }

    private fun getUserGroupsInformation() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            userSession = retrieveSession()
            var getGroupsDataDeferred = GroupApi.retrofitService.getUserGroups(userSession.userId, roles)
            try {
                var userGroups = getGroupsDataDeferred.await()
                if (userGroups != null && userGroups.size > 0) {
                    _groups.value = userGroups
                    _isGroupListEmpty.value = false
                } else {
                    _groups.value = userGroups
                    _isGroupListEmpty.value = true
                }
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
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