package com.f2h.f2h_admin.screens.group.pre_order

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
import androidx.navigation.fragment.navArgs
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentPreOrderBinding


/**
 * A simple [Fragment] subclass.
 */
class PreOrderFragment : Fragment() {

    private lateinit var binding: FragmentPreOrderBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: PreOrderViewModelFactory by lazy { PreOrderViewModelFactory(dataSource, application) }
    private val viewModel: PreOrderViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        PreOrderViewModel::class.java) }

    val args: PreOrderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_pre_order, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        //Call the API to fetch item data to populate page
        viewModel.fetchAllData(args.itemId)


        // Item list recycler view
        val adapter = PreOrderItemsAdapter(PreOrderItemClickListener {} )
        binding.preOrderItemRecyclerView.adapter = adapter
        viewModel.availabilityItems.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })


        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }
}
