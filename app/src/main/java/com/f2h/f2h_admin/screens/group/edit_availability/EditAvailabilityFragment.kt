package com.f2h.f2h_admin.screens.group.edit_availability

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
import com.f2h.f2h_admin.databinding.FragmentEditAvailabilityBinding
import com.f2h.f2h_admin.screens.group.pre_order.PreOrderFragmentArgs


/**
 * A simple [Fragment] subclass.
 */
class EditAvailabilityFragment : Fragment() {

    private lateinit var binding: FragmentEditAvailabilityBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: EditAvailabilityViewModelFactory by lazy { EditAvailabilityViewModelFactory(dataSource, application) }
    private val viewModel: EditAvailabilityViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        EditAvailabilityViewModel::class.java) }

    val args: EditAvailabilityFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_edit_availability, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.itemAvailabilityId.value = args.itemAvailabilityId

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })



        viewModel.deliverySlotList.observe(viewLifecycleOwner, Observer { deliverySlotItem ->
            val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, deliverySlotItem.map { it.name ?: "" }
            )
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.editDeliverySlotSpinner.adapter = spinnerArrayAdapter
        })

        viewModel.initialDeliverySlotId.observe(viewLifecycleOwner, Observer { initialDeliverySlotId ->
            var pos = viewModel.getInitialIndex()
            println(binding.editDeliverySlotSpinner.getItemAtPosition(pos))
            binding.editDeliverySlotSpinner.setSelection(pos)
        })

        //Delivery Slot Spinner
        binding.editDeliverySlotSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        //Date Spinner
        binding.editDateSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        //Item Uom Spinner
        binding.editRepeatSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
