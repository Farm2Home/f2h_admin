package com.f2h.f2h_admin.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.f2h.f2h_admin.constants.F2HConstants
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Dns
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


private const val BASE_URL = F2HConstants.SERVER_URL

object RetrofitInstance {

    fun build(context: Context): Retrofit {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val cacheVal = createHttpClientCache(context)
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor { chain ->
                var request = chain.request()
                if (!isOnline(context)!!){
                    val cacheControl = CacheControl.Builder()
                        .maxAge(60, TimeUnit.SECONDS)
                        .build()

                    request.newBuilder()
                        .header("Cache-Control", cacheControl.toString())
                        .build();
                }
                chain.proceed(request)
            }
            .cache(cacheVal)
            .build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(BASE_URL)
            .build()
    }

    fun createHttpClientCache(context: Context): Cache? {
        return try {
            val cacheDir: File  = context.getDir("service_api_cache", Context.MODE_PRIVATE)
            Cache(cacheDir, 10 * 1024 * 1024)
        } catch (e: IOException) {
            Log.e("Order API", "Couldn't create http cache because of IO problem.", e)
            null
        }
    }

    fun isOnline(context: Context): Boolean? {
        var isConnected: Boolean? = false // Initial Value
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }


}