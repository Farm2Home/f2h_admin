package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.GroupMembership
import com.f2h.f2h_admin.network.models.GroupMembershipRequest
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

interface GroupMembershipApiService{

    @GET("group_membership")
    fun getGroupMembership(@Query("group_id") groupId: Long, @Query("roles") roles: String?):
            Deferred<List<GroupMembership>>

    @PUT("group_membership/{group_membership_id}")
    fun updateGroupMembership(@Path("group_membership_id") groupMembershipId: Long, @Body updateMembership: GroupMembershipRequest):
            Deferred<GroupMembership>

    @POST("group_membership")
    fun requestGroupMembership(@Body createMembership: GroupMembershipRequest): Deferred<GroupMembership>

    @DELETE("group_membership/{group_membership_id}")
    fun deleteGroupMembership(@Path("group_membership_id") groupMembershipId: Long): Deferred<GroupMembership>

}

object GroupMembershipApi {
    val retrofitService : GroupMembershipApiService by lazy {
        retrofit.create(GroupMembershipApiService::class.java)
    }
}