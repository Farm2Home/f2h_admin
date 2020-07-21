package com.f2h.f2h_admin.screens.group.add_item

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentAddItemBinding
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentDirections
import com.github.dhaval2404.imagepicker.ImagePicker


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


        binding.addImageButton?.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(720, 720)	//Final image resolution will be less than 720 x 720(Optional)
                .start()
        }

        viewModel.isAddItemActionComplete.observe(viewLifecycleOwner, Observer { isAddItemActionComplete ->
            if (isAddItemActionComplete){
                navigateBack()
            }
        })

        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val filePath: String? = ImagePicker.getFilePath(data)
            viewModel.imageFilePath.value = filePath ?: ""
            context?.let { Glide.with(it).load(filePath).into(binding.addImagePreview) }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this.context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this.context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateBack() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

}
