package com.f2h.f2h_admin.screens.group.accept_reject_membership

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentMembershipRequestBinding


class MembershipRequestFragment : Fragment() {

    private lateinit var binding: FragmentMembershipRequestBinding
    val navArgs: MembershipRequestFragmentArgs by navArgs()
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewModelFactory: MembershipRequestViewModelFactory by lazy { MembershipRequestViewModelFactory(dataSource, application, navArgs) }
    private val viewModel: MembershipRequestViewModel by lazy { ViewModelProvider(this, viewModelFactory).get(
        MembershipRequestViewModel::class.java) }





//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return activity?.let {
//            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater;
//            builder.setView(inflater.inflate(R.layout.fragment_membership_request, null))
//                .setPositiveButton("Ok",
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // User cancelled the dialog
//                    })
//                .setNegativeButton("Cancel",
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // Open my orders page
//                    })
//            // Create the AlertDialog object and return it
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_membership_request, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Members List recycler view
        val adapter = MembershipRequestAdapter(viewModel::onMembershipActionSelected)
        binding.requestListRecyclerView.adapter = adapter
        viewModel.requestedRolesUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })
        viewModel.isMembershipActionComplete.observe(viewLifecycleOwner, Observer { isMembershipActionComplete ->
            if (isMembershipActionComplete){
                onMembershipActionComplete()
            }
        })

//        (context as AppCompatActivity).supportActionBar!!.title =
        //Toast Message
        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

    }

    private fun onMembershipActionComplete() {
        view?.let { Navigation.findNavController(it).popBackStack() }
    }

}