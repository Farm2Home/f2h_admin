package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.Locality
import com.f2h.f2h_admin.network.models.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface LocalityApiService{
    @GET("locality")
    fun getLocalityDetails():
            Deferred<List<Locality>>
}

object LocalityApi {
    fun retrofitService(context: Context): LocalityApiService {
        return RetrofitInstance.build(context).create(LocalityApiService::class.java)
    }
}
