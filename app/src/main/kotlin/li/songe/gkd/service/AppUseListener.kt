package li.songe.gkd.service

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.myapplication.network.UserService

object AppUseListener {
    private val appUse = HashMap<String, Int>()
    private var lastUpload = System.currentTimeMillis()
    private var lastRead = System.currentTimeMillis()
    private val intervalTime = 1000 * 60 * 60 * 12
    private var clearTime = System.currentTimeMillis()


    fun onAccessibilityEvent(
        event: AccessibilityEvent?,
        rootInActiveWindow: AccessibilityNodeInfo?
    ) {
        if ((System.currentTimeMillis() - lastRead) < 1000 * 5) return


        val app = event?.packageName.toString()
        val use = appUse[app]?.plus(1) ?: 0
        appUse[app] = use

        if ((System.currentTimeMillis() - lastUpload) > intervalTime) {
            uploadMessage()
            lastUpload = System.currentTimeMillis()
        }

        if ((System.currentTimeMillis() - clearTime) > intervalTime * 2 * 7) {
            uploadMessage()
            clearTime = System.currentTimeMillis()
            appUse.clear()
        }
        lastRead = System.currentTimeMillis()

    }

    private fun uploadMessage() {
        var liveMessage = ""
        appUse.map { liveMessage = "$it\n$liveMessage" }
        UserService.login {
            UserService.uploadMessage(it, liveMessage)
        }
    }
}