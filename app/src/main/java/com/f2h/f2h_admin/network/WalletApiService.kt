package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.network.models.Wallet
import com.f2h.f2h_admin.network.models.WalletTransaction
import com.f2h.f2h_admin.network.models.WalletTransactionRequest
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface WalletApiService{
    @GET("wallet")
    fun getWalletDetails(@Query("group_id") groupId: Long, @Query("user_id") userId: Long):
            Deferred<List<Wallet>>

    @GET("transaction")
    fun getWalletTransactionDetails(@Query("wallet_id") walletId: Long):
            Deferred<List<WalletTransaction>>

    @POST("transaction")
    fun createWalletTransaction(@Body transactionRequest: WalletTransactionRequest):
            Deferred<WalletTransactionRequest>
}

object WalletApi {
    fun retrofitService(context: Context): WalletApiService {
        return RetrofitInstance.build(context).create(WalletApiService::class.java)
    }
}
