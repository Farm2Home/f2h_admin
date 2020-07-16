package com.f2h.f2h_admin.screens.group.add_item

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentAddItemBinding
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentDirections


/**
 * A simple [Fragment] subclass.
 */
class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddItemBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: AddItemViewModelFactory by lazy { AddItemViewModelFactory(dataSource, application) }
    private val viewModel: AddItemViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        AddItemViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_add_item, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })


        //Item Uom Spinner
        binding.addUomSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onItemUomSelected(position)
            }
        }

        //Item Uom Spinner
        binding.addFarmerSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onItemFarmerSelected(position)
            }
        }


        return binding.root
    }

}