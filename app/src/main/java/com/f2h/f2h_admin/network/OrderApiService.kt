package com.f2h.f2h_admin.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.Order
import com.f2h.f2h_admin.network.models.OrderAssignRequest
import com.f2h.f2h_admin.network.models.OrderCreateRequest
import com.f2h.f2h_admin.network.models.OrderUpdateRequest
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import okhttp3.OkHttpClient
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

interface OrderApiService {

    @Headers("Cache-Control: max-age=640000", "User-Agent: My-App-Name")
    @GET("order")
    fun getOrdersForGroup(@Query("group_id") groupId: Long, @Query("start_date") startDate: String?,
                          @Query("end_date") endDate: String?):
            Deferred<List<Order>>

    @GET("order")
    fun getOrdersForGroupUserAndItem(@Query("group_id") groupId: Long, @Query("buyer_user_id") buyerUserId: Long,
                                     @Query("item_id") itemId: Long, @Query("start_date") startDate: String,
                                     @Query("end_date") endDate: String):
            Deferred<List<Order>>

    @PUT("orders/update_all")
    fun updateOrders(@Body orderUpdateRequests: List<OrderUpdateRequest>): Deferred<List<Order>>

    @PUT("orders/assign_delivery")
    fun assignOrders(@Body orderAssignRequest: List<OrderAssignRequest>): Deferred<List<Order>>

    @PUT("orders/cash_collected")
    fun cashCollectedAndUpdateOrders(@Body orderUpdateRequests: List<OrderUpdateRequest>): Deferred<List<Order>>

    @POST("orders/save_all")
    fun createOrders(@Body createOrders: List<OrderCreateRequest>): Deferred<List<Order>>

}

object OrderApi {
    val retrofitService : OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }
}
