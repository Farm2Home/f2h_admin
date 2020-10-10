package com.f2h.f2h_admin.network

import android.content.Context
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.constants.F2HConstants.SERVER_URL
import com.f2h.f2h_admin.network.models.ItemAvailability
import com.f2h.f2h_admin.network.models.ItemAvailabilityCreateRequest
import com.f2h.f2h_admin.network.models.ItemAvailabilityUpdateRequest
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ItemAvailabilityApiService{
    @GET("item_availability")
    fun getItemAvailabilities(@Query("item_availability_ids") availabilityIds: String):
            Deferred<List<ItemAvailability>>

    @GET("item_availability")
    fun getItemAvailabilitiesByItemId(@Query("item_ids") itemIds: Long):
            Deferred<List<ItemAvailability>>

    @GET("item_availability")
    fun getItemAvailabilitiesByItemId(@Query("item_ids") itemIds: List<Long>):
            Deferred<List<ItemAvailability>>

    @PUT("item_availabilities")
    fun updateItemAvailabilities(@Body availabilityUpdateRequests: List<ItemAvailabilityUpdateRequest>):
            Deferred<List<ItemAvailability>>

    @POST("item_availability")
    fun createItemAvailability(@Body availabilityCreateRequest: ItemAvailabilityCreateRequest):
            Deferred<ItemAvailability>
}


object ItemAvailabilityApi {
    fun retrofitService(context: Context): ItemAvailabilityApiService {
        return RetrofitInstance.build(context).create(ItemAvailabilityApiService::class.java)
    }
}
