package com.f2h.f2h_admin.screens.group.edit_item

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
import androidx.navigation.fragment.navArgs
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentEditItemBinding
import com.f2h.f2h_admin.screens.group.edit_item.EditItemFragmentArgs.Companion.fromBundle


/**
 * A simple [Fragment] subclass.
 */
class EditItemFragment : Fragment() {

    private lateinit var binding: FragmentEditItemBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: EditItemViewModelFactory by lazy { EditItemViewModelFactory(dataSource, application) }
    private val viewModel: EditItemViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        EditItemViewModel::class.java) }
    val args: EditItemFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater, R.layout.fragment_edit_item, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })

        viewModel.selectedItemId = args.itemId


        //Initialize item Uom spinner
        binding.editUomSelector
        binding.editUomSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        //Farmer Spinner
        binding.editFarmerSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
