package com.f2h.f2h_admin.screens.group.confirm_reject

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentConfirmRejectBinding

/**
 * A simple [Fragment] subclass.
 */
class ConfirmRejectFragment : Fragment() {

    private lateinit var binding: FragmentConfirmRejectBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: ConfirmRejectViewModelFactory by lazy {
        ConfirmRejectViewModelFactory(
            dataSource,
            application
        )
    }
    private val viewModel: ConfirmRejectViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(
            ConfirmRejectViewModel::class.java
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_confirm_reject, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Daily Orders List recycler view
        val adapter = ConfirmRejectItemsAdapter(OrderedItemClickListener { uiDataElement ->
            println("Clicked Report Item")
        }, CheckBoxClickListener {uiModel ->
            viewModel.onCheckBoxClicked(uiModel)
        }, IncreaseButtonClickListener { uiModel ->
            viewModel.increaseConfirmedQuantity(uiModel)
        }, DecreaseButtonClickListener { uiModel ->
            viewModel.decreaseConfirmedQuantity(uiModel)
        }, CallUserButtonClickListener { uiDataElement ->
            viewModel.onCallUserButtonClicked(uiDataElement)
            startPhoneCall()
        })
        binding.reportListRecyclerView.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })


        //Item Spinner
        binding.itemSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onItemSelected(position)
            }
        }


        //Order Status Spinner
        binding.orderStatusSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onOrderStatusSelected(position)
            }
        }


        //End Date Selector Spinner
        binding.timeFilterSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        //Buyer Selector Spinner
        binding.buyerNameSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onBuyerSelected(position)
            }
        }

        //Farmer Selector Spinner
        binding.farmerNameSelector?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onFarmerSelected(position)
            }
        }
    }

    fun startPhoneCall() {
        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),42)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
            Toast.makeText(activity, "Please accept permission request to continue", Toast.LENGTH_SHORT).show()
            return
        }
        if(viewModel.selectedUiElement.value?.buyerMobile.isNullOrBlank()){
            Toast.makeText(activity, "Invalid mobile number", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + viewModel.selectedUiElement.value?.buyerMobile))
        startActivity(intent)
    }


}
