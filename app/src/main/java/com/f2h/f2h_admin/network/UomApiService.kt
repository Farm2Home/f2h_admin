package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
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

interface UomApiService{

    @GET("uom")
    fun getAllUoms(): Deferred<List<Uom>>

}

object UomApi {
    fun retrofitService(context: Context): UomApiService {
        return RetrofitInstance.build(context).create(UomApiService::class.java)
    }
}
