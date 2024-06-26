package com.example.myapplication.network

import java.io.Serializable

data class ResponseResult<T>(
    val code: Int = -1,
    val msg: String? = "",
    val data: T? = null
): Serializable {
    fun isSuccess() = code == 200
}

data class LoginRequest(
    val username: String,
    val password: String,
    val uuid: String
) : Serializable

data class Token(
    val code: Int,
    val msg: String?,
    val token: String?
) : Serializable

data class LiveRecordRequest(
    val username: String,
    val records: List<LiveRecord>
) : Serializable

data class AddMessageRequest(
    val liveMessage: String
) : Serializable

data class LiveRecord(
    val channelName: String,
    val isPass: Boolean,
    var watchMinute: Int,
    var failedTime: Int,
    val url: String
) : Serializable


data class VodRecordRequest(
    val records: List<VodRecord>
) : Serializable

data class VodRecord(
    val watchMinute: Long,
    val vodName: String?,
    val typeName: String?,
    val vodPic: String?,
    val vodRemarks: String?, // 评论
    val vodYear: String?,
    val vodArea: String?,
    val vodDirector: String?,
    val vodActor: String?,
    val vodContent: String?,
    val vodWith: Int?,
    val vodHeight: Int?,
    val siteName: String?,
    val siteApi: String?,
    val siteType: Int?,
    val configName: String?,
    val configUrl: String?,
    val playUrl: String?,
    val playHeader: String?
) : Serializable