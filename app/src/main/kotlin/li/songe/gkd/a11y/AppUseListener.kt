package li.songe.gkd.a11y

import android.text.format.DateUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import li.songe.gkd.util.appUseFile
import li.songe.gkd.util.appendTime
import li.songe.gkd.util.format

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
        appUseFile.appendText(liveMessage)
        appUseFile.appendTime()
    }
}