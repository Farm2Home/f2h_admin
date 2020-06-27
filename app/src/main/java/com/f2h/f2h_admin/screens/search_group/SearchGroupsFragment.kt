package com.f2h.f2h_admin.screens.search_group

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentSearchGroupsBinding


/**
 * A simple [Fragment] subclass.
 */
class SearchGroupsFragment : Fragment() {

    private lateinit var binding: FragmentSearchGroupsBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: SearchGroupsViewModelFactory by lazy { SearchGroupsViewModelFactory(dataSource, application) }
    private val viewModel: SearchGroupsViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(SearchGroupsViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_groups , container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel


        // Adapter for List of Searched groups
        val adapter = SearchGroupsAdapter(GroupClickListener { group ->
            onGroupSelected(group)
        }, RequestMembershipButtonClickListener { group ->
            viewModel.requestMembership(group)
        })
        binding.groupListRecyclerView.adapter = adapter
        viewModel.group.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })


        // Localities Selection Spinner
        binding.localitiesSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onLocalitiesSelected(position)
            }
        }


        //Set app bar title to group name here
        (context as AppCompatActivity).supportActionBar!!.title = "Farm To Home"
        return binding.root
    }


    fun onGroupSelected(group: SearchGroupsItemsModel){
        // preview of group here?
//        viewModel.updateSessionWithGroupInfo(group)
//        val action = GroupsFragmentDirections.actionGroupsFragmentToGroupDetailsTabsFragment(group.groupName ?: "")
//        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}
