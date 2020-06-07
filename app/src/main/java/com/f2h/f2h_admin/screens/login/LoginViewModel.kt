package com.f2h.f2h_admin.screens.login

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.LoginApi
import com.f2h.f2h_admin.network.models.User
import kotlinx.coroutines.*
import retrofit2.await


class LoginViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    val loginPassword = MutableLiveData<String>()
    val loginMobile = MutableLiveData<String>()

    private val _loginResponse = MutableLiveData<User>()
    val loginResponse: LiveData<User>
        get() = _loginResponse

    private val _isLoginComplete = MutableLiveData<Boolean>()
    val isLoginComplete: LiveData<Boolean>
        get() = _isLoginComplete

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _isLoginComplete.value = false
        _isProgressBarActive.value = true
        fetchSavedSession()
    }

    fun onClickLoginButton() {
        _isProgressBarActive.value = true
        val mobile: String = loginMobile.value.toString()
        val password: String = Base64.encodeToString(loginPassword.value.toString().toByteArray(), Base64.DEFAULT)
        var session = SessionEntity(mobile = mobile, password = password )
        tryToLogin(session)
    }


    private suspend fun saveSession(updatedUserData: User, preSavedSession: SessionEntity) {
        return withContext(Dispatchers.IO) {
            database.clearSessions()
            preSavedSession.address = updatedUserData.address ?: ""
            preSavedSession.email = updatedUserData.email ?: ""
            preSavedSession.userId = updatedUserData.userId ?: -1L
            preSavedSession.mobile = updatedUserData.mobile ?: ""
            preSavedSession.userName = updatedUserData.userName ?: ""
            preSavedSession.password = updatedUserData.password ?: ""
            database.insert(preSavedSession)
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

    private fun fetchSavedSession(): SessionEntity {
        var session = SessionEntity()
        coroutineScope.launch {
            session = retrieveSession()
            loginPassword.value = Base64.decode(session.password, Base64.DEFAULT).toString()
            loginMobile.value = session.mobile
            if (session.id != 0L){
                tryToLogin(session)
            } else{
                _isLoginComplete.value = false
                _isProgressBarActive.value = false
            }
        }
        return session
    }

    private fun tryToLogin (session: SessionEntity){
        coroutineScope.launch {
            var getUserDataDeferred = LoginApi.retrofitService.tryUserLogin(session.mobile, session.password)
            try {
                var updatedUserData = getUserDataDeferred.await()
                if (updatedUserData != null){
                    _loginResponse.value = updatedUserData
                    saveSession(updatedUserData, session)
                    println("Successfully logged in : "+ updatedUserData.toString())
                }
                _isLoginComplete.value = true
            } catch (t:Throwable){
                println(t.message)
                _loginResponse.value = null
                _isLoginComplete.value = true
            }
            _isProgressBarActive.value = false
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}