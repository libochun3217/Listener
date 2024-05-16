package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ServerApi {
    companion object {
        val instance by lazy { retrofit.create(ServerApi::class.java) }
    }

    // 登录接口
    @POST("/loginV2")
    fun login(@Body request: LoginRequest): Call<Token>


    // 直播记录上报接口
    @POST("/admin/message")
    fun messageUpload(@Body request: AddMessageRequest, @Header("Authorization") token: String): Call<ResponseResult<String>>
}