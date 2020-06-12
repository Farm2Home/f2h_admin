package com.f2h.f2h_admin.screens.group.group_wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.WalletApi
import com.f2h.f2h_admin.network.models.Wallet
import com.f2h.f2h_admin.network.models.WalletTransaction
import kotlinx.coroutines.*

class GroupWalletViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet>
        get() = _wallet

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _visibleUiData = MutableLiveData<MutableList<WalletItemsModel>>()
    val visibleUiData: LiveData<MutableList<WalletItemsModel>>
        get() = _visibleUiData

    private val selectedUserId = MutableLiveData<Long>()
    fun setSelectedUserId(id: Long){
        selectedUserId.value = id
    }

    private var allUiData = ArrayList<WalletItemsModel>()
    private var userSession = SessionEntity()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        getWalletInformation()
    }

    private fun getWalletInformation() {
        _isProgressBarActive.value = true
        coroutineScope.launch {
            userSession = retrieveSession()
            try {
                var walletTransactions = listOf<WalletTransaction>()
                var activeWalletData = WalletApi.retrofitService.getWalletDetails(userSession.groupId, selectedUserId.value ?: -1).await()
                var walletData = activeWalletData.firstOrNull() ?: Wallet()
                if(walletData != null){
                    var activeWalletTransactionData = WalletApi.retrofitService.getWalletTransactionDetails(walletData.walletId ?: -1).await()
                    walletTransactions = activeWalletTransactionData
                }
                _wallet.value = walletData
                walletTransactions.forEach { transaction ->
                    var walletItemsModel = WalletItemsModel(
                        transaction.walletLedgerId ?: -1,
                        transaction.transactionDate ?: "",
                        transaction.transactionDescription ?: "",
                        transaction.amount ?: 0.0
                    )
                    allUiData.add(walletItemsModel)
                }
                allUiData.sortByDescending { it.transactionDate }
                _visibleUiData.value = allUiData
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }


    private suspend fun retrieveSession() : SessionEntity {
        return withContext(Dispatchers.IO) {
            val sessions = database.getAll()
            var session = SessionEntity()
            if (sessions.size==1) {
                session = sessions[0]
                println(session.toString())
            } else {
                database.clearSessions()
            }
            return@withContext session
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
