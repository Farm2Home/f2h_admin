package com.f2h.f2h_admin.screens.group.freeze_multiple

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentFreezeMultipleBinding


class FreezeMultipleFragment : Fragment() {

    private lateinit var binding: FragmentFreezeMultipleBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: FreezeMultipleViewModelFactory by lazy {
        FreezeMultipleViewModelFactory(
            dataSource,
            application
        )
    }
    private val viewModel: FreezeMultipleViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(
            FreezeMultipleViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_freeze_multiple, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setHasOptionsMenu(true)
        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.all_availabilities, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.shareAvailabilities -> {
                shareAvailabilities()
                return true
            }
        }
        return false
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Daily Orders List recycler view
        val adapter = FreezeMultipleItemsAdapter(CheckBoxClickListener { uiModel ->
            viewModel.onCheckBoxClicked(uiModel)
        })
        binding.reportListRecyclerView.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })


        //Order Status Spinner
        binding.freezeStatusSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onFreezeStatusSelected(position)
            }
        }

        //End Date Selector Spinner
        binding.timeFilterSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onTimeFilterSelected(position)
            }
        }

        //Seller Selector Spinner
        binding.sellerNameSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onSellerSelected(position)
            }
        }



        binding.assignDeliverySwipeRefresh.setOnRefreshListener {
            viewModel.getAvailabilitiesForGroup()
            binding.assignDeliverySwipeRefresh.isRefreshing = false
        }

    }

    private fun shareAvailabilities(){
        var text = viewModel.getAvailabilitiesToShare()
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(sendIntent)
    }

}
