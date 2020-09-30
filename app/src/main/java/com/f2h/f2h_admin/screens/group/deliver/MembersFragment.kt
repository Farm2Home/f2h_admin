package com.f2h.f2h_admin.screens.group.deliver

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.f2h.f2h_deliver.R
import com.f2h.f2h_deliver.database.F2HDatabase
import com.f2h.f2h_deliver.database.SessionDatabaseDao
import com.f2h.f2h_deliver.databinding.FragmentMembersBinding
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class MembersFragment : Fragment() {

    private lateinit var binding: FragmentMembersBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: MembersViewModelFactory by lazy { MembersViewModelFactory(dataSource, application) }
    private val viewModel: MembersViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        MembersViewModel::class.java) }

    private val args: MembersFragmentArgs by navArgs()

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {

                    val adapter = recyclerView.adapter as MemberItemsAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    Collections.swap(binding.viewModel?.visibleUiData?.value , from, to)
                    adapter.notifyItemMoved(from, to)
                    return true
                }

                override fun isItemViewSwipeEnabled(): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    TODO("Not yet implemented")
                }


            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        binding = inflate(inflater, R.layout.fragment_members, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        itemTouchHelper.attachToRecyclerView(binding.itemListRecyclerView)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.group_options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.exitGroup) {
            viewModel.onClickExitGroup()
            true
        } else {
            NavigationUI.onNavDestinationSelected(item, requireView().findNavController()) ||
                    super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Set app bar title to group name here
        (context as AppCompatActivity).supportActionBar!!.title = args.groupName

        // Members List recycler view
        val adapter = MemberItemsAdapter(CallUserButtonClickListener { uiDataElement ->
            viewModel.onCallUserButtonClicked(uiDataElement)
            startPhoneCall()
        }, MembersItemClickListener { uiDataElement ->
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
        binding.deliveryAreaSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onDeliveryAreaSelected(position)
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
            val areaSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, uiItems!!.deliveryAreaList
            )
            areaSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.deliveryAreaSelector.adapter = areaSpinnerArrayAdapter
            var pos = viewModel.getInitialDeliveryIndex()
            binding.deliveryAreaSelector.setSelection(pos)

            val statusSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, uiItems.statusList
            )
            statusSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.statusSelector.adapter = statusSpinnerArrayAdapter
            pos = viewModel.getInitialStatusIndex()
            binding.statusSelector.setSelection(pos)

            val timeSpinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, uiItems.timeFilterList
            )
            timeSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.memberTimeFilterSelector.adapter = timeSpinnerArrayAdapter
            pos = viewModel.getInitialTimeIndex()
            binding.memberTimeFilterSelector.setSelection(pos)

        })

//        viewModel.initialDeliveryArea.observe(viewLifecycleOwner, Observer { initialDeliveryAreaId ->
//
//        })


    }




    private fun startPhoneCall(){
        requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),42)
    }

//    fun onMemberSelected(element: MembersUiModel){
//        val action = MembersFragmentDirections.actionMembersFragmentToDeliverFragment(element.userId,
//            viewModel.selectedDate.value, viewModel.selectedDate.value)
//        view?.let { Navigation.findNavController(it).navigate(action) }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(grantResults[0] == PERMISSION_DENIED){
            Toast.makeText(activity, "Please accept permission request to continue", Toast.LENGTH_SHORT).show()
            return
        }
        if(viewModel.selectedUiElement.value?.mobile.isNullOrBlank()){
            Toast.makeText(activity, "Invalid mobile number", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + viewModel.selectedUiElement.value?.mobile))
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserDetailsInGroup()
    }


}
