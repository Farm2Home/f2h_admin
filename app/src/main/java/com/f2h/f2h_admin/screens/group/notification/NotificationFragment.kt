package com.f2h.f2h_admin.screens.group.notification

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentNotificationBinding
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentArgs

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewViewModelFactory: NotificationViewModelFactory by lazy { NotificationViewModelFactory(dataSource, application) }
    private val viewModel: NotificationViewModel by lazy { ViewModelProvider(this, viewViewModelFactory).get(
        NotificationViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.toastText.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Daily Orders List recycler view
        val adapter = NotificationItemsAdapter(NotificationItemClickListener { uiDataElement ->
            viewModel.onNotificationMessageSelected(uiDataElement)
        })
        binding.notificationOptionsList.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })
    }

}
