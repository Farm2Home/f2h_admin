package com.f2h.f2h_admin.screens.contact_us

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.GroupApi
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.User
import kotlinx.coroutines.*

class ContactUsViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {


    private val _response = MutableLiveData<User>()

    val response: LiveData<User>
        get() = _response

    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getGroupContactProfileInformation()
    }

    private fun getGroupContactProfileInformation() {
        coroutineScope.launch {
            userSession = retrieveSession()
            try {
                var activeGroupData = GroupApi.retrofitService(getApplication()).getGroupDetails(userSession.groupId).await()
                var userData = activeGroupData.ownerUserId?.let { UserApi.retrofitService(getApplication()).getUserDetails(it).await() }
                _response.value = userData
            } catch (t:Throwable){
                println(t.message)
            }
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