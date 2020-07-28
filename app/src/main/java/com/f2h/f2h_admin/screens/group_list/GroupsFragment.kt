package com.f2h.f2h_admin.screens.group_list

import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentGroupsBinding
import com.f2h.f2h_admin.network.models.Group


/**
 * A simple [Fragment] subclass.
 */
class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: GroupsViewModelFactory by lazy { GroupsViewModelFactory(dataSource, application) }
    private val viewModel: GroupsViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(GroupsViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_groups , container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val adapter = GroupsAdapter(GroupClickListener { group ->
            onGroupSelected(group)
        })
        binding.groupListRecyclerView.adapter = adapter
        viewModel.group.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })


        // No group text
        viewModel.isGroupListEmpty.observe(viewLifecycleOwner, Observer { isGroupListEmpty ->
            if(isGroupListEmpty){
                binding.emptyGroupsText.visibility = View.VISIBLE
                binding.joinButton.visibility = View.VISIBLE
            } else {
                binding.emptyGroupsText.visibility = View.GONE
                binding.joinButton.visibility = View.GONE
            }
        })

        binding.joinButton.setOnClickListener {
            onJoinGroupButtonClicked()
        }


        //Set app bar title to group name here
        (context as AppCompatActivity).supportActionBar!!.title = "Farm To Home"

        return binding.root
    }

    fun onGroupSelected(group: Group){
        viewModel.updateSessionWithGroupInfo(group)
        val action = GroupsFragmentDirections.actionGroupsFragmentToGroupDetailsTabsFragment(group.groupName ?: "")
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    fun onJoinGroupButtonClicked() {
        val action = GroupsFragmentDirections.actionGroupsFragmentToSearchGroupsFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}
