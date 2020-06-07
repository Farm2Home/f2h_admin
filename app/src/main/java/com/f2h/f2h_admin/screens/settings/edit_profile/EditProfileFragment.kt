package com.f2h.f2h_admin.screens.settings.edit_profile

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentEditProfileBinding
import com.f2h.f2h_admin.screens.settings.SettingsViewModelFactory

/**
 * A simple [Fragment] subclass.
 */
class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: EditProfileViewModelFactory by lazy {
        EditProfileViewModelFactory(
            dataSource,
            application
        )
    }
    private val viewModel: EditProfileViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        EditProfileViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.isProgressBarActive.observe(viewLifecycleOwner, Observer { isProgressBarActive ->
            if(isProgressBarActive){
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })

        //Set app bar title to group name here
        (context as AppCompatActivity).supportActionBar!!.title = "Edit Profile"

        return binding.root
    }

}
