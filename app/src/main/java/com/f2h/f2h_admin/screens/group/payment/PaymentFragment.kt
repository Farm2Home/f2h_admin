package com.f2h.f2h_admin.screens.group.payment

import android.app.Application
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentPaymentBinding

/**
 * A simple [Fragment] subclass.
 */
class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: MembersViewModelFactory by lazy { MembersViewModelFactory(dataSource, application) }
    private val viewModel: MembersViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        MembersViewModel::class.java) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        binding = inflate(inflater, R.layout.fragment_payment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Members List recycler view
        val adapter = MemberItemsAdapter( MembersItemClickListener { uiDataElement ->
            viewModel.onMemberSelected(uiDataElement)
        }, viewModel)
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

        //End Date Selector Spinner
        binding.memberTimeFilterSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        binding.statusSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onStatusSelected(position)
            }
        }

        viewModel.uiFilterModel.observe(viewLifecycleOwner, Observer { uiItems ->


            val statusSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, uiItems.statusList
            )
            statusSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.statusSelector.adapter = statusSpinnerArrayAdapter
            var pos = viewModel.getInitialStatusIndex()
            binding.statusSelector.setSelection(pos)

            val timeSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, uiItems.timeFilterList
            )
            timeSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.memberTimeFilterSelector.adapter = timeSpinnerArrayAdapter
            pos = viewModel.getInitialTimeIndex()
            binding.memberTimeFilterSelector.setSelection(pos)

        })


    }


    override fun onResume() {
        super.onResume()
        viewModel.getUserDetailsInGroup()
    }


}
