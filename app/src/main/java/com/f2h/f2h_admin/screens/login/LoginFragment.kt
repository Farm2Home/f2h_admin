package com.f2h.f2h_admin.screens.login

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.f2h.f2h_admin.BuildConfig
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentLoginBinding
import com.f2h.f2h_admin.screens.UserPagesActivity

class LoginFragment: Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: LoginViewModelFactory by lazy { LoginViewModelFactory(dataSource, application) }
    private val viewModel: LoginViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)

        binding.appVersion.text = String.format("%s-%s", BuildConfig.ENVIRONMENT, BuildConfig.VERSION_NAME)

        viewModel.isLoginComplete.observe(viewLifecycleOwner, Observer { isLoginComplete ->
            if(isLoginComplete) {
                onLoginComplete()
            }
        })

        binding.signUp.setOnClickListener {
            onSignUpClicked()
        }

        return binding.root
    }


    private fun onLoginComplete() {
        if (viewModel.loginResponse.value != null) {
            Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this.context, UserPagesActivity::class.java)
             startActivity(intent)
        } else {
            Toast.makeText(this.activity, "Please Login Again", Toast.LENGTH_SHORT).show()
        }
    }


    private fun onSignUpClicked() {
        val action = LoginFragmentDirections.actionLoginFragmentToAgreementFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}