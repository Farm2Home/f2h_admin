package com.f2h.f2h_admin.screens.group.freeze_multiple

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.database.SessionDatabaseDao

class FreezeMultipleViewModelFactory (
    private val dataSource: SessionDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FreezeMultipleViewModel::class.java)) {
            return FreezeMultipleViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}