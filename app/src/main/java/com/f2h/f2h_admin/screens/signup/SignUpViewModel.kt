package com.f2h.f2h_admin.screens.signup

import android.app.Application
import android.util.Base64
import android.util.Base64.DEFAULT
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.User
import com.f2h.f2h_admin.network.models.UserCreateRequest
import kotlinx.coroutines.*
import java.lang.Long



class SignUpViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    var mVerificationId: String? = "default"

    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val locality = MutableLiveData<String>()
    val city = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val pincode = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val mobile = MutableLiveData<String>()
    val otp = MutableLiveData<String>()

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _isEnteringMobile = MutableLiveData<Boolean>()
    val isEnteringMobile: LiveData<Boolean>
        get() = _isEnteringMobile

    private val _isVerifyingOtp = MutableLiveData<Boolean>()
    val isVerifyingOtp: LiveData<Boolean>
        get() = _isVerifyingOtp

    private val _isMobileVerified = MutableLiveData<Boolean>()
    val isMobileVerified: LiveData<Boolean>
        get() = _isMobileVerified

    private val _isSignUpComplete = MutableLiveData<Boolean>()
    val isSignUpComplete: LiveData<Boolean>
        get() = _isSignUpComplete

    private val _isSendOtpClicked = MutableLiveData<Boolean>()
    val isSendOtpClicked: LiveData<Boolean>
        get() = _isSendOtpClicked

    private val _resendOtpClicked = MutableLiveData<Boolean>()
    val resendOtpClicked: LiveData<Boolean>
        get() = _resendOtpClicked

    private val _verifyOtpClicked = MutableLiveData<Boolean>()
    val verifyOtpClicked: LiveData<Boolean>
        get() = _verifyOtpClicked

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        _isProgressBarActive.value = false
        updateUi(STATE_INITIALIZED)
        _isSendOtpClicked.value = false
        _verifyOtpClicked.value = false
        _resendOtpClicked.value = false
        initCallback()
    }

    fun onSendOtpButtonClick(){
        _isProgressBarActive.value = true
        if (mobile.value.isNullOrBlank() || !isNumeric(mobile.value.toString())) {
            _toastText.value = "Please enter a valid mobile number"
            _isProgressBarActive.value = false
        }
        else {
            _toastText.value = "Sending otp to your mobile number"
            _isSendOtpClicked.value = true

        }
    }

    fun onVerifyOtpButtonClick(){
        _isProgressBarActive.value = true
        if (otp.value.isNullOrBlank() || !isNumeric(otp.value.toString())) {
            _toastText.value = "Please enter a valid Otp"
        }
        else {
            // [START verify_with_code]
            _verifyOtpClicked.value = true
            // [END verify_with_code]
        }
    }

    fun onResendButtonClick(){
        if (mobile.value.isNullOrBlank() || !isNumeric(mobile.value.toString())) {
            _toastText.value = "Please enter a valid mobile number"
            _isProgressBarActive.value = false
        }
        else {
            _toastText.value = "Re-Sending otp to your mobile number"
            _resendOtpClicked.value = true
        }
    }


    fun onCreateButtonClick() {
        if(isAnyFieldInvalid()){
            return
        }
        _isProgressBarActive.value = true
        var newUser = createUserRequestObject()
        coroutineScope.launch {
            val getCreatedUser = UserApi.retrofitService.createUser(newUser)
            try {
                var createdUser = getCreatedUser.await()
                if (createdUser != null){
                    saveSession(createdUser)
                }
                _isSignUpComplete.value = true
            } catch (t:Throwable){
                println(t.message)
                _toastText.value = "Oops, looks like the user already exists"
            }
            _isProgressBarActive.value = false
        }
    }

    fun isAnyFieldInvalid(): Boolean{
        if (userName.value.isNullOrBlank()) {
            _toastText.value = "Please enter a name"
            return true
        }
        if (password.value.isNullOrBlank()) {
            _toastText.value = "Please enter a password"
            return true
        }
        if (confirmPassword.value.isNullOrBlank()) {
            _toastText.value = "Please confirm password"
            return true
        }
        if (!confirmPassword.value.equals(password.value)) {
            _toastText.value = "Passwords do not match"
            return true
        }
        _isProgressBarActive.value = false
        return false
    }

    protected fun isNumeric(numberString: String): Boolean{
        try {
            val num = Long.parseLong(numberString)
        } catch (e: NumberFormatException) {
            return false
        }
        return true;
    }

    fun initCallback(){
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                _toastText.value = "Phone number Verified"
                // [START_EXCLUDE silent]
                updateUi(STATE_VERIFY_SUCCESS)
                // [END_EXCLUDE]
//                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                println(e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    _toastText.value = "Invalid phone number."
                    updateUi(STATE_CODE_NOT_SENT)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    _toastText.value = "Unable to Verify"
                    updateUi(STATE_VERIFY_FAILED)
                }
                else{
                    _toastText.value = "Something went wrong."
                    _isProgressBarActive.value = false
                }
            }

            override fun onCodeSent(verificationId: String,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                _toastText.value = "Otp Sent"
                updateUi(STATE_CODE_SENT)
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                _toastText.value = "Auto read failed, Please enter OTP"
            }
        }
        // [END phone_auth_callbacks]
    }


    fun updateUi(uiState: Int){
        when(uiState){
            STATE_INITIALIZED -> {
                _isSignUpComplete.value = false
                _isMobileVerified.value = false
                _isEnteringMobile.value = true
                _isVerifyingOtp.value = false
                _isProgressBarActive.value = false
                _isSendOtpClicked.value = false
            }
            STATE_CODE_SENT -> {
                _isSignUpComplete.value = false
                _isMobileVerified.value = false
                _isEnteringMobile.value = true
                _isVerifyingOtp.value = true
                _isProgressBarActive.value = false
                _resendOtpClicked.value = false
            }
            STATE_CODE_NOT_SENT -> {
                _isSignUpComplete.value = false
                _isMobileVerified.value = false
                _isEnteringMobile.value = true
                _isVerifyingOtp.value = false
                _isProgressBarActive.value = false
                _isSendOtpClicked.value = false
            }
            STATE_VERIFY_SUCCESS -> {
                _isSignUpComplete.value = false
                _isMobileVerified.value = true
                _isEnteringMobile.value = false
                _isVerifyingOtp.value = false
                _isProgressBarActive.value = false
            }
            STATE_VERIFY_FAILED -> {
                _isSignUpComplete.value = false
                _isMobileVerified.value = false
                _isEnteringMobile.value = true
                _isVerifyingOtp.value = true
                _isProgressBarActive.value = false
                _resendOtpClicked.value = false
            }
            STATE_SIGNIN_SUCCESS -> {
                _isSignUpComplete.value = true
                _isMobileVerified.value = false
                _isEnteringMobile.value = false
                _isVerifyingOtp.value = false
                _isProgressBarActive.value = false
            }
        }

    }

    companion object {
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_CODE_NOT_SENT = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }


    private fun createUserRequestObject() : UserCreateRequest{
        var userObject = UserCreateRequest()
        userObject.userName = userName.value
        var address = arrayListOf(locality.value?:"", city.value?:"", state.value?:"",
            pincode.value?:"")
        address.removeIf(String::isEmpty)
        userObject.address  = address.joinToString()
        userObject.mobile = mobile.value
        userObject.email = email.value
        userObject.password = Base64.encodeToString(password.value?.toByteArray(), DEFAULT)
        userObject.createdBy = userName.value
        userObject.updatedBy = userName.value
        return userObject
    }

    private suspend fun saveSession(updatedUserData: User) {
        return withContext(Dispatchers.IO) {
            database.clearSessions()
            var savedSession = SessionEntity()
            savedSession.address = updatedUserData.address ?: ""
            savedSession.email = updatedUserData.email ?: ""
            savedSession.userId = updatedUserData.userId ?: -1L
            savedSession.mobile = updatedUserData.mobile ?: ""
            savedSession.userName = updatedUserData.userName ?: ""
            savedSession.password = updatedUserData.password ?: ""
            database.insert(savedSession)
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}