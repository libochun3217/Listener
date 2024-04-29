package com.example.myapplication.network

import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.SPUtils

object UserService {

    fun userName() =
        "${Build.BRAND}_${DeviceUtils.getModel()}_${AppUtils.getAppVersionName()}_Android-${DeviceUtils.getSDKVersionName()}_${DeviceUtils.getUniqueDeviceId()}"

    fun buildLoginReq(): LoginRequest {
        val uuid = DeviceUtils.getUniqueDeviceId()
        val password = EncryptUtils.encryptMD5ToString("$uuid${userName()}ruoyi")
        return LoginRequest(userName(), password, uuid)
    }

    fun login(onSuccess: (String) -> Unit) {
        ServerApi.instance.login(buildLoginReq()).req {
            it?.token?.let {
                onSuccess.invoke(it)
            }
        }
    }

    fun uploadMessage(token: String, messageList: List<String>) {

    }
}
