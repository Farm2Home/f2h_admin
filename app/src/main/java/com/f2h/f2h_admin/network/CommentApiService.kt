package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.Comment
import com.f2h.f2h_admin.network.models.CommentCreateRequest
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

private const val BASE_URL = SERVER_URL

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface CommentApiService{
    @GET("comment")
    fun getComments(@Query("order_id") orderId: Long): Deferred<List<Comment>>

    @POST("comment")
    fun createComment(@Body createRequest: CommentCreateRequest): Deferred<Comment>
}

object CommentApi {
    val retrofitService : CommentApiService by lazy {
        retrofit.create(CommentApiService::class.java)
    }
}
