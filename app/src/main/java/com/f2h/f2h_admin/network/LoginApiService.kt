package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.network.models.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "http://f2h.herokuapp.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface LoginApiService{
    @GET("user/login")
    fun tryUserLogin(@Query("mobile") mobile: String, @Query("password") password: String):
            Call<User>
}

object LoginApi {
    val retrofitService : LoginApiService by lazy {
        retrofit.create(LoginApiService::class.java)
    }
}