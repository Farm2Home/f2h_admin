package com.f2h.f2h_admin.screens.settings.edit_profile

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.User
import com.f2h.f2h_admin.network.models.UserCreateRequest
import kotlinx.coroutines.*

class EditProfileViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _response = MutableLiveData<User>()
    val response: LiveData<User>
        get() = _response

    val userName = MutableLiveData<String>()
    val mobile = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val email = MutableLiveData<String>()

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getProfileInformation()
    }

    private fun getProfileInformation() {
        coroutineScope.launch {
            _isProgressBarActive.value = true
            userSession = retrieveSession()
            var getUserDataDeferred = UserApi.retrofitService.getUserDetails(userSession.userId)
            try {
                var userData = getUserDataDeferred.await()
                saveSession(userData, userSession)
                _response.value = userData;
                userName.value = userData.userName
                mobile.value = userData.mobile
                password.value = Base64.decode(userData.password, Base64.DEFAULT).toString()
                email.value = userData.email
                address.value = userData.address
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    fun onSaveButtonClicked() {
        coroutineScope.launch {
            _isProgressBarActive.value = true
            userSession = retrieveSession()
            var updatedUser = UserCreateRequest (
                userName.value,
                address.value,
                email.value,
                null,
                Base64.encodeToString(password.value?.toByteArray(), Base64.DEFAULT),
                null,
                userName.value
            )

            var updateUserData = UserApi.retrofitService.updateUser(userSession.userId, updatedUser)
            try {
                var userData = updateUserData.await()
                _response.value = userData;
                _toastText.value = "Profile updated successfully"
            } catch (t:Throwable){
                println(t.message)
                _toastText.value = "Oops, something went wrong"
            }
            getProfileInformation()
            _isProgressBarActive.value = false
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

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}