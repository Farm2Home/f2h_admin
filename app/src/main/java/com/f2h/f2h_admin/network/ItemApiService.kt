package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ItemApiService{

    @POST("item")
    fun createItemForGroup(@Body request: ItemCreateRequest): Deferred<Item>

    @PUT("item/{item_id}")
    fun updateItemForGroup(@Path("item_id") itemId: Long, @Body request: ItemUpdateRequest): Deferred<Item>

    @GET("item")
    fun getItemsForGroup(@Query("group_id") groupId: Long):
            Deferred<List<Item>>

    @GET("item/{item_id}")
    fun getItem(@Path("item_id") itemId: Long):
            Deferred<Item>

    @GET("handling_charge")
    fun getAllHandlingChargesForItem(@Query("item_id") itemId: Long): Deferred<List<HandlingCharge>>

    @POST("handling_charges")
    fun createNewHandlingChargesForItem(@Body request: List<HandlingChargesCreateRequest>): Deferred<List<HandlingCharge>>

    @DELETE("handling_charge")
    fun deleteExistingHandlingCharges(@Query("handling_charge_ids") handlingChargeIds: String?): Deferred<String?>

}

object ItemApi {
    fun retrofitService(context: Context): ItemApiService {
        return RetrofitInstance.build(context).create(ItemApiService::class.java)
    }
}
