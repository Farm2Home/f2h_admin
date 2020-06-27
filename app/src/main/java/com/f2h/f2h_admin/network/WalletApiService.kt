package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.User
import com.f2h.f2h_admin.network.models.Wallet
import com.f2h.f2h_admin.network.models.WalletTransaction
import com.f2h.f2h_admin.network.models.WalletTransactionRequest
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = SERVER_URL

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

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
    val retrofitService : WalletApiService by lazy {
        retrofit.create(WalletApiService::class.java)
    }
}
