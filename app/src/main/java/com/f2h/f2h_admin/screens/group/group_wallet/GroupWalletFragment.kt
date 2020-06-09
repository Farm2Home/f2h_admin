package com.f2h.f2h_admin.screens.group.group_wallet

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs

import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.databinding.FragmentGroupWalletBinding
import com.f2h.f2h_admin.screens.group.group_tabs.GroupDetailsTabsFragmentArgs

class GroupWalletFragment : Fragment() {

    private lateinit var binding: FragmentGroupWalletBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }
    private val dataSource: SessionDatabaseDao by lazy { F2HDatabase.getInstance(application).sessionDatabaseDao }
    private val viewViewModelFactory: GroupWalletViewModelFactory by lazy { GroupWalletViewModelFactory(dataSource, application) }
    private val viewModel: GroupWalletViewModel by lazy { ViewModelProvider(this, viewViewModelFactory).get(
        GroupWalletViewModel::class.java) }
    val args: GroupWalletFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_wallet, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.setSelectedUserId(args.userId)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Daily Orders List recycler view
        val adapter = WalletItemsAdapter(WalletItemClickListener { uiDataElement ->
            println("Clicked Wallet Item")
        })
        binding.walletTransactions.adapter = adapter
        viewModel.visibleUiData.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })
    }

}
