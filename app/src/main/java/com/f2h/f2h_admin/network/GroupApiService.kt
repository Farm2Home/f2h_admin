package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.Group
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

interface GroupApiService{

    @GET("group")
    fun getUserGroups(@Query("user_id") userId: Long, @Query("roles") roles: List<String>):
            Deferred<List<Group>>

    @GET("group/{group_id}")
    fun getGroupDetails(@Path("group_id") groupId: Long):
            Deferred<Group>

    @GET("group/search")
    fun searchGroupsByLocality(@Query("localities") localities: List<String>):
            Deferred<List<Group>>

}

object GroupApi {
    val retrofitService : GroupApiService by lazy {
        retrofit.create(GroupApiService::class.java)
    }
}