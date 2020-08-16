package com.f2h.f2h_admin.network

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

private const val BASE_URL = SERVER_URL

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()


interface ItemAvailabilityApiService{
    @GET("item_availability")
    fun getItemAvailabilities(@Query("item_availability_ids") availabilityIds: List<Long>):
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
    val retrofitService : ItemAvailabilityApiService by lazy {
        retrofit.create(ItemAvailabilityApiService::class.java)
    }
}
