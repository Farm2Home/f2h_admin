package com.f2h.f2h_admin.screens.confirm_reject

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.database.SessionDatabaseDao

class ConfirmRejectViewModelFactory (
    private val dataSource: SessionDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfirmRejectViewModel::class.java)) {
            return ConfirmRejectViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}