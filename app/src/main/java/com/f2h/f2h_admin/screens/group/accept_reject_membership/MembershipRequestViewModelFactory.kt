package com.f2h.f2h_admin.screens.group.accept_reject_membership

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgs
import com.f2h.f2h_admin.database.SessionDatabaseDao

class MembershipRequestViewModelFactory(
    private val dataSource: SessionDatabaseDao,
    private val application: Application,
    private val navArgs: MembershipRequestFragmentArgs) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MembershipRequestViewModel::class.java)) {
            return MembershipRequestViewModel(dataSource, application, navArgs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}