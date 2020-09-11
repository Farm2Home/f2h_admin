package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.HandlingOption
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.network.models.Uom
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = SERVER_URL

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface HandlingOptionApiService{

    @GET("handling_option")
    fun getAllHandlingOptions(): Deferred<List<HandlingOption>>

}

object HandlingOptionApi {
    val retrofitService : HandlingOptionApiService by lazy {
        retrofit.create(HandlingOptionApiService::class.java)
    }
}
