package com.f2h.f2h_admin.screens.group.add_availability

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentAddAvailabilityBinding
import com.f2h.f2h_admin.network.models.DeliverySlot
import com.f2h.f2h_admin.screens.group.pre_order.PreOrderFragmentArgs


/**
 * A simple [Fragment] subclass.
 */
class AddAvailabilityFragment : Fragment() {

    private lateinit var binding: FragmentAddAvailabilityBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: AddAvailabilityViewModelFactory by lazy { AddAvailabilityViewModelFactory(dataSource, application) }
    private val viewModel: AddAvailabilityViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        AddAvailabilityViewModel::class.java) }

    val args: PreOrderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_add_availability, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.itemId.value = args.itemId

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })


        //Date Spinner
        binding.addDateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onDateSelected(position)
            }
        }

        viewModel.deliverySlotList.observe(viewLifecycleOwner, Observer { deliverySlotItem ->
            val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, deliverySlotItem.map { it.name ?: "" }
            )
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addDeliverySlotSpinner.adapter = spinnerArrayAdapter
        })

        //Delivery Slot Spinner
        binding.addDeliverySlotSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onDeliverySlotSelected(position)
            }
        }


        //Item Uom Spinner
        binding.addRepeatSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onRepeatFeatureSelected(position)
            }
        }


        viewModel.isAvailabilityActionComplete.observe(viewLifecycleOwner, Observer { isAvailabilityActionComplete ->
            if (isAvailabilityActionComplete){
                navigateBack()
            }
        })

        return binding.root
    }

    private fun navigateBack() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

}
