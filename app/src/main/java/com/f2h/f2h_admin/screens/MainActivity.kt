package com.f2h.f2h_admin.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.constants.F2HConstants
import com.f2h.f2h_admin.databinding.ActivityMainBinding
import com.f2h.f2h_admin.network.RetrofitInstance
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

private const val BASE_URL = F2HConstants.SERVER_URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navController = this.findNavController(R.id.mainActivityNavHostFragment)
    }


    private fun setupRetrofitAndOkHttp() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

//        val httpLoggingInterceptor = HttpLoggingInterceptor()
//        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpCacheDirectory = File(cacheDir, "offlineCache")

        //10 MB
        val cache = Cache(httpCacheDirectory, 10 * 1024 * 1024)
        val httpClient = OkHttpClient.Builder()
            .cache(cache)
//            .addInterceptor(httpLoggingInterceptor)
//            .addNetworkInterceptor(provideCacheInterceptor())
//            .addInterceptor(provideOfflineCacheInterceptor())
            .build()
        val retrofit: Retrofit = Retrofit.Builder()
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(httpClient)
            .baseUrl(BASE_URL)
            .build()


//        apiService = retrofit.create(APIService::class.java)
    }

}
