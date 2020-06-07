package com.f2h.f2h_admin.screens.group.members

import android.app.Application
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

        // Daily Orders List recycler view
        val adapter = OrderedItemsAdapter(DeleteUserButtonClickListener { uiDataElement ->
            viewModel.decreaseOrderQuantity(uiDataElement)
        })
        binding.itemListRecyclerView.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })


        //Progress Bar loader
        viewModel.isProgressBarActive.observe(viewLifecycleOwner, Observer { isProgressBarActive ->
            if(isProgressBarActive){
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })


        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

    }


    override fun onResume() {
        super.onResume()
        viewModel.getUserDetailsInGroup()
    }

    private fun navigateToPreOrderPage(uiData: MembersUiModel) {
        val action = GroupDetailsTabsFragmentDirections.actionGroupDetailsTabsFragmentToPreOrderFragment(uiData.userId)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}
