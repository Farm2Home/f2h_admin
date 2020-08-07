package com.f2h.f2h_admin.screens.signup

import android.app.Application
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUpFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentSignupBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: SignUpViewModelFactory by lazy { SignUpViewModelFactory(dataSource, application) }
    private val viewModel: SignUpViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(SignUpViewModel::class.java) }
    private val countDownTimer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.resend.setText((millisUntilFinished / 1000).toInt().toString())
        }

        override fun onFinish() {
            binding.resend.setText("Resend OTP")
            binding.resend.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)

        viewModel.isSignUpComplete.observe(viewLifecycleOwner, Observer { isSignUpComplete ->
            if (isSignUpComplete){
                onSignUpComplete()
            }
        })

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })

        viewModel.isSendOtpClicked.observe(viewLifecycleOwner, Observer { sendOtpClicked ->
            if (sendOtpClicked){
                startPhoneNumberVerification(viewModel.mobile.value?:"")

            }
        })

        viewModel.verifyOtpClicked.observe(viewLifecycleOwner, Observer { verifyOtpClicked ->
            if (verifyOtpClicked){
                val credential = PhoneAuthProvider.getCredential(viewModel.mVerificationId!!, viewModel.otp.value!!)
                signInWithPhoneAuthCredential(credential)
            }
        })

        viewModel.isVerifyingOtp.observe(viewLifecycleOwner, Observer { isVerifyingOtp ->
            if (isVerifyingOtp){
                countDownTimer.start()
            }
        })

        viewModel.resendOtpClicked.observe(viewLifecycleOwner, Observer { resendOtpClicked ->
            binding.resend.isEnabled = false
            if (resendOtpClicked) {
                resendVerificationCode(viewModel.mobile.value!!, viewModel.mResendToken)
                countDownTimer.start()
            }
        })



        return binding.root
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91"+phoneNumber, // Phone number to verify
            60,             // Timeout duration
            TimeUnit.SECONDS,   // Unit of timeout
            requireActivity(),           // Activity (for callback binding)
            viewModel.mCallbacks)        // OnVerificationStateChangedCallbacks
        // [END start_phone_ath]
    }

    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91"+phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            requireActivity(), // Activity (for callback binding)
            viewModel.mCallbacks, // OnVerificationStateChangedCallbacks
            token)             // ForceResendingToken from callbacks
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    viewModel.updateUi(4)
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        viewModel.updateUi(3)
                    }
                }
            }
    }


    private fun onSignUpComplete() {
        Toast.makeText(this.context, "Sign Up Successful, logging you in", Toast.LENGTH_SHORT).show()
        val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }


}