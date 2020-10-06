package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.network.models.*
import kotlinx.coroutines.Deferred
import retrofit2.http.*


interface OrderApiService {

    @GET("order")
    fun getOrdersForGroup(@Query("group_id") groupId: Long, @Query("start_date") startDate: String?,
                          @Query("end_date") endDate: String?):
            Deferred<List<Order>>

    @GET("order_header")
    fun getOrderHeaderForGroup(@Query("group_id") groupId: Long, @Query("start_date") startDate: String?,
                          @Query("end_date") endDate: String?):
            Deferred<List<OrderHeader>>

    @GET("order")
    fun getOrdersForGroupUserAndItem(@Query("group_id") groupId: Long, @Query("buyer_user_id") buyerUserId: Long,
                                     @Query("item_id") itemId: Long, @Query("start_date") startDate: String,
                                     @Query("end_date") endDate: String):
            Deferred<List<Order>>

    @PUT("orders/update_all")
    fun updateOrders(@Body orderUpdateRequests: List<OrderUpdateRequest>): Deferred<List<Order>>

    @PUT("order_headers/assign_delivery")
    fun assignOrders(@Body orderDeliveryAssignRequest: List<OrderDeliveryAssignRequest>): Deferred<List<Order>>

    @PUT("orders/cash_collected")
    fun cashCollectedAndUpdateOrders(@Body orderUpdateRequests: List<OrderUpdateRequest>): Deferred<List<Order>>

    @POST("orders/save_all")
    fun createOrders(@Body createOrders: List<OrderCreateRequest>): Deferred<List<Order>>

    @PUT("orders/packet_number")
    fun updateReceivedNumber(@Body receivedNumberUpdateRequests: List<OrderReceivedNumberUpdateRequest>): Deferred<List<Order>>


}

object OrderApi {
    fun retrofitService(context: Context): OrderApiService {
        return RetrofitInstance.build(context).create(OrderApiService::class.java)
    }
}
