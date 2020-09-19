package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.User
import com.f2h.f2h_admin.network.models.UserCreateRequest
import com.f2h.f2h_admin.network.models.UserDetails
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface UserApiService{
    @GET("user/{user_id}")
    fun getUserDetails(@Path("user_id") userId: Long):
            Deferred<User>

    @GET("user")
    fun getUserDetailsByUserIds(@Query("user_ids") userIds: String):
            Deferred<List<UserDetails>>

    @POST("user")
    fun createUser(@Body user: UserCreateRequest) : Deferred<User>

    @PUT("user/{user_id}")
    fun updateUser(@Path("user_id") userId: Long, @Body user: UserCreateRequest) : Deferred<User>
}

object UserApi {
    fun retrofitService(context: Context): UserApiService {
        return RetrofitInstance.build(context).create(UserApiService::class.java)
    }
}
