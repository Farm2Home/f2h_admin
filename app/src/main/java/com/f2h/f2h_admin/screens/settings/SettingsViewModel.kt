package com.f2h.f2h_admin.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.User
import kotlinx.coroutines.*

class SettingsViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _response = MutableLiveData<User>()
    val response: LiveData<User>
        get() = _response

    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getProfileInformation()
    }

    fun getProfileInformation() {
        coroutineScope.launch {
            userSession = retrieveSession()
                var userData = User (
                    userSession.userId,
                    userSession.userName,
                    userSession.address,
                    userSession.email,
                    userSession.mobile,
                    userSession.password
                )
                _response.value = userData;
        }
    }


    private suspend fun retrieveSession() : SessionEntity {
        return withContext(Dispatchers.IO) {
            val sessions = database.getAll()
            var session = SessionEntity()
            if (sessions != null && sessions.size==1) {
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