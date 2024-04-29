package com.example.myapplication.network

import com.blankj.utilcode.util.LogUtils
import com.example.myapplication.Constants
import com.example.myapplication.Constants.Companion.NETWORK_TAG
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "http://139.9.181.116:9896"
//private const val BASE_URL = "http://192.168.215.2:8080"

val retrofit by lazy {
    val okHttp = OkHttpClient.Builder().apply {
        connectTimeout(10L, TimeUnit.SECONDS)    // 连接超时 5s
        writeTimeout(15L, TimeUnit.SECONDS)     // 写操作超时时间 10s
        readTimeout(15L, TimeUnit.SECONDS)      // 读操作超时时间 10s
    }

    Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp.addNetworkInterceptor(PrettyLogInterceptor {
            LogUtils.dTag(NETWORK_TAG, it)
        }).build())
        .build()
}

val executor = java.util.concurrent.Executors.newFixedThreadPool(5 * 2)


fun <T> Call<T>.req(res: (T?) -> Unit) {
    executor.execute {
        try {
            res.invoke(this.execute().body())
        } catch (e: Exception) {
            LogUtils.dTag(Constants.NETWORK_TAG, "network error ${e.message}")
        }
    }
}