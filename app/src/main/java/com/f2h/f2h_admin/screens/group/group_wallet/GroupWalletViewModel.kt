package com.f2h.f2h_admin.screens.group.group_wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.WalletApi
import com.f2h.f2h_admin.network.models.Wallet
import com.f2h.f2h_admin.network.models.WalletTransaction
import com.f2h.f2h_admin.network.models.WalletTransactionRequest
import kotlinx.coroutines.*
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class GroupWalletViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var transactionAmount = ""
    var transactionDescription = ""

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet>
        get() = _wallet

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private var _visibleUiData = MutableLiveData<MutableList<WalletItemsModel>>()
    val visibleUiData: LiveData<MutableList<WalletItemsModel>>
        get() = _visibleUiData

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

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
        allUiData.clear()
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

    fun onAddMoneyButtonClick() {
        if(isAnyFieldInvalid()){
            return
        }
        _isProgressBarActive.value = true
        var newTransaction = createWalletTransactionRequestObject()
        coroutineScope.launch {
            val createTransactionDeferred = WalletApi.retrofitService.createWalletTransaction(newTransaction)
            try {
                val response = createTransactionDeferred.await()
            } catch (t:Throwable){
                println(t.message)
                _toastText.value = "Oops, something went wrong"
            }
            _isProgressBarActive.value = false
            getWalletInformation()
        }
    }


    private fun createWalletTransactionRequestObject(): WalletTransactionRequest {
        var today = Calendar.getInstance()
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        var transactionRequest = WalletTransactionRequest (
            wallet.value?.userId,
            wallet.value?.groupId,
            null,
            formatter.format(today.time),
            transactionDescription,
            transactionAmount.toDouble()
        )
        return transactionRequest
    }


    fun isAnyFieldInvalid(): Boolean{
        if (transactionAmount.isNullOrBlank()) {
            try {
                transactionAmount.toDouble()
            } catch (e: Exception) {
                _toastText.value = "Amount is an invalid number"
                return true
            }
            _toastText.value = "Please enter an amount"
            return true
        }
        if (transactionDescription.isNullOrBlank()) {
            transactionDescription = "Adding money to wallet"
            return false
        }

        return false
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
