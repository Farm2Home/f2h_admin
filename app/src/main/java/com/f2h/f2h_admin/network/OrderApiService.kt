package com.f2h.f2h_admin.network

import com.f2h.f2h_admin.network.models.Order
import com.f2h.f2h_admin.network.models.OrderCreateRequest
import com.f2h.f2h_admin.network.models.OrderUpdateRequest
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

interface OrderApiService {

    @GET("order")
    fun getOrdersForUserAndGroup(@Query("group_id") groupId: Long, @Query("buyer_user_id") buyerUserId: Long):
            Deferred<List<Order>>

    @GET("order")
    fun getOrdersForGroup(@Query("group_id") groupId: Long):
            Deferred<List<Order>>

    @GET("order")
    fun getOrdersForGroupUserAndItem(@Query("group_id") groupId: Long, @Query("buyer_user_id") buyerUserId: Long,
                                     @Query("item_id") itemId: Long, @Query("start_date") startDate: String,
                                     @Query("end_date") endDate: String):
            Deferred<List<Order>>

    @PUT("orders/update_all")
    fun updateOrders(@Body orderUpdateRequests: List<OrderUpdateRequest>): Deferred<List<Order>>

    @POST("orders/save_all")
    fun createOrders(@Body createOrders: List<OrderCreateRequest>): Deferred<List<Order>>

}

object OrderApi {
    val retrofitService : OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
}
