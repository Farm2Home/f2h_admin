package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LoginApiService{
    @GET("user/login")
    fun tryUserLogin(@Query("mobile") mobile: String, @Query("password") password: String):
            Call<User>
}

object LoginApi {
    fun retrofitService(context: Context): LoginApiService {
        return RetrofitInstance.build(context).create(LoginApiService::class.java)
    }
}