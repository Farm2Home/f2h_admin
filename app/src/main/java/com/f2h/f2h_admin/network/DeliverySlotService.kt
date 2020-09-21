package com.f2h.f2h_admin.network

import android.content.Context
import android.util.Log
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.DeliveryArea
import com.f2h.f2h_admin.network.models.DeliverySlot
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.IOException


interface DeliverySlotApiService{
    @GET("delivery_slot")
    fun getDeliverySlotsForGroup(@Query("group_ids") groupId: Long):
            Deferred<List<DeliverySlot>>
}

object DeliverySlotApi {
    fun retrofitService(context: Context): DeliverySlotApiService {
        return RetrofitInstance.build(context).create(DeliverySlotApiService::class.java)
    }
}