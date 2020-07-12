package com.f2h.f2h_admin.screens.group.all_items

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
import com.f2h.f2h_admin.databinding.FragmentAllItemsBinding
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.screens.group.members.AllItemsViewModelFactory
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentDirections


/**
 * A simple [Fragment] subclass.
 */
class AllItemsFragment : Fragment() {

    private lateinit var binding: FragmentAllItemsBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: AllItemsViewModelFactory by lazy { AllItemsViewModelFactory(dataSource, application) }
    private val viewModel: AllItemsViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        AllItemsViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_all_items, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        viewModel.refreshFragmentData()


        // Item list recycler view
        val adapter = AllItemsAdapter(AllItemClickListener { item ->
            navigateToPreOrderPage(item)
        }, EditButtonClickListener { item ->
            navigateToEditItemPage(item)
        })
        binding.itemListRecyclerView.adapter = adapter
        viewModel.visibleItems.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })

        // Onclick Listener for button, to switch to add item page
        binding.addItemButton.setOnClickListener {
            navigateToAddItemPage()
        }

        return binding.root
    }


    private fun navigateToPreOrderPage(item: Item) {
        val action = GroupDetailsTabsFragmentDirections.actionGroupDetailsTabsFragmentToPreOrderFragment(item.itemId ?: -1L)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun navigateToAddItemPage() {
        val action = GroupDetailsTabsFragmentDirections.actionGroupDetailsTabsFragmentToAddItemsFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

    private fun navigateToEditItemPage(item: Item) {
        val action = GroupDetailsTabsFragmentDirections.actionGroupDetailsTabsFragmentToEditItemFragment(item.itemId ?: -1L)
        view?.let { Navigation.findNavController(it).navigate(action) }
    }
}
