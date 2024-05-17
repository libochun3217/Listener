package li.songe.gkd.service

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

object QListener {
    private var lastRead = System.currentTimeMillis()
    private var user = ""
    private val messageList = ArrayList<String>()
    private val allMessageList = ArrayList<String>()
    private var uploading = false
    private val TAG = "QListener"


    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
        rootInActiveWindow: AccessibilityNodeInfo?
    ) {
        if ((System.currentTimeMillis() - lastRead) < 1000 * 5) return

        val app = event?.packageName.toString()
        if (app != "com.tencent.mobileqq") return
        if (messageList.size > 100 && !uploading) {
            allMessageList.addAll(messageList)
//            uploadMessage()
            if (allMessageList.size > 100 * 1000) allMessageList.clear()
        }
        rootInActiveWindow?.let {
            lastRead = System.currentTimeMillis()
            Log.d(TAG, "qq start")
            findAll(it, 0)
        }
    }

    private fun findAll(node: AccessibilityNodeInfo, level: Int) {
        if (level > 25) {
            return
        }
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

//                addMessage(message)
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
}