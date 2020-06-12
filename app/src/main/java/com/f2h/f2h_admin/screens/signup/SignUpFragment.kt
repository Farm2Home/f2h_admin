package com.f2h.f2h_admin.screens.signup

import android.app.Application
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
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentSignupBinding

class SignUpFragment: Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: SignUpViewModelFactory by lazy { SignUpViewModelFactory(dataSource, application) }
    private val viewModel: SignUpViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(SignUpViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        return binding.root
    }


    private fun onSignUpComplete() {
        Toast.makeText(this.context, "Sign Up Successful, logging you in", Toast.LENGTH_SHORT).show()
        val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}