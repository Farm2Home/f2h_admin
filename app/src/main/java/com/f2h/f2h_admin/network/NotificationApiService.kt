package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.Comment
import com.f2h.f2h_admin.network.models.CommentCreateRequest
import com.f2h.f2h_admin.network.models.Notification
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface NotificationApiService{
    @GET("notification/send_groups?")
    fun sendNotificationToGroups(@Query("notification_id") notificationId: Long,
                    @Query("group_ids") group_ids: String): Deferred<String>

    @GET("notification")
    fun getAllNotificationMessages(): Deferred<List<Notification>>
}

object NotificationApi {
    fun retrofitService(context: Context): NotificationApiService {
        return RetrofitInstance.build(context).create(NotificationApiService::class.java)
    }
}
