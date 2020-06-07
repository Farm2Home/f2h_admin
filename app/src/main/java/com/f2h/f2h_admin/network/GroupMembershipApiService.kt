package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.network.models.GroupMembership
import com.f2h.f2h_admin.network.models.GroupMembershipRequest
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://f2h.herokuapp.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface GroupMembershipApiService{

    @GET("group_membership")
    fun getGroupMembership(@Query("group_id") groupId: Long):
            Deferred<List<GroupMembership>>

    @POST("group_membership")
    fun requestGroupMembership(@Body createMembership: GroupMembershipRequest): Deferred<GroupMembership>

}

object GroupMembershipApi {
    val retrofitService : GroupMembershipApiService by lazy {
        retrofit.create(GroupMembershipApiService::class.java)
    }
}