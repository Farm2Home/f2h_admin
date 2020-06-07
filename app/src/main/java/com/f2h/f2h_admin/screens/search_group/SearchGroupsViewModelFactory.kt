package com.f2h.f2h_admin.screens.search_group

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.f2h.f2h_admin.database.SessionDatabaseDao

class SearchGroupsViewModelFactory (
    private val dataSource: SessionDatabaseDao,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchGroupsViewModel::class.java)) {
            return SearchGroupsViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}