package com.f2h.f2h_admin.screens.group.members

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentMembersBinding
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentDirections


/**
 * A simple [Fragment] subclass.
 */
class MembersFragment : Fragment() {

    private lateinit var binding: FragmentMembersBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: MembersViewModelFactory by lazy { MembersViewModelFactory(dataSource, application) }
    private val viewModel: MembersViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        MembersViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_members, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Members List recycler view
        val adapter = MemberItemsAdapter(DeleteUserButtonClickListener { uiDataElement ->
            viewModel.onDeleteUserButtonClicked(uiDataElement)
        }, CallUserButtonClickListener { uiDataElement ->
            viewModel.onCallUserButtonClicked(uiDataElement)
            startPhoneCall()
        }, AcceptUserButtonClickListener { uiDataElement ->
            viewModel.onAcceptUserButtonClicked(uiDataElement)
        }, OpenUserWalletButtonClickListener { uiDataElement ->
            openSelectedUserWallet(uiDataElement)
        })
        binding.itemListRecyclerView.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })


        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

    }

    private fun openSelectedUserWallet(uiDataElement: MembersUiModel) {
        val action = GroupDetailsTabsFragmentDirections
            .actionGroupDetailsTabsFragmentToGroupWalletFragment(uiDataElement.userId, uiDataElement.userName)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }


    fun startPhoneCall(){
        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),42)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(viewModel.selectedUiElement.value?.mobile == null){
            Toast.makeText(activity, "Invalid mobile number", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + viewModel.selectedUiElement.value?.mobile))
        startActivity(intent)
    }


    override fun onResume() {
        super.onResume()
        viewModel.getUserDetailsInGroup()
    }

}
