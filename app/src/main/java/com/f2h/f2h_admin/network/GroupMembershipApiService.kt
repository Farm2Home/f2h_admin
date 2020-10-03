package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.network.models.GroupMembership
import com.f2h.f2h_admin.network.models.GroupMembershipRequest
import com.f2h.f2h_admin.network.models.GroupMembershipUpdateRequest
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface GroupMembershipApiService{

    @GET("group_membership")
    fun getGroupMembership(@Query("group_id") groupId: Long, @Query("roles") roles: String?):
            Deferred<List<GroupMembership>>

    @PUT("group_membership/{group_membership_id}")
    fun updateGroupMembership(@Path("group_membership_id") groupMembershipId: Long, @Body updateMembership: GroupMembershipRequest):
            Deferred<GroupMembership>

    @PUT("group_membership")
    fun updateGroupMembershipList(@Body updateMembershipList: List<GroupMembershipUpdateRequest>): Deferred<List<GroupMembership>>


    @POST("group_membership")
    fun requestGroupMembership(@Body createMembership: GroupMembershipRequest): Deferred<GroupMembership>

    @DELETE("group_membership/{group_membership_id}")
    fun deleteGroupMembership(@Path("group_membership_id") groupMembershipId: Long): Deferred<GroupMembership>

}

object GroupMembershipApi {
    fun retrofitService(context: Context): GroupMembershipApiService {
        return RetrofitInstance.build(context).create(GroupMembershipApiService::class.java)
    }
}