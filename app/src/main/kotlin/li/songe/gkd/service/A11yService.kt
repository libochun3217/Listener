package li.songe.gkd.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.google.android.accessibility.selecttospeak.SelectToSpeakService
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.a11y.appChangeTime
import li.songe.gkd.a11y.isUseful
import li.songe.gkd.a11y.onA11yFeatInit
import li.songe.gkd.a11y.setGeneratedTime
import li.songe.gkd.app
import li.songe.gkd.util.OnA11yLife
import li.songe.gkd.util.componentName

@SuppressLint("AccessibilityPolicy")
abstract class A11yService : AccessibilityService(), OnA11yLife {
    override fun onCreate() = onCreated()
    override fun onServiceConnected() = onA11yConnected()
    override fun onInterrupt() {}
    override fun onDestroy() = onDestroyed()
    override val a11yEventCbs = mutableListOf<(AccessibilityEvent) -> Unit>()
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!event.isUseful()) return
        onA11yEvent(event)
    }

    val startTime = System.currentTimeMillis()
    var justStarted: Boolean = true
        get() {
            if (field) {
                field = System.currentTimeMillis() - startTime < 3_000
            }
            return field
        }

    val safeActiveWindow: AccessibilityNodeInfo?
        get() = try {
            // 某些应用耗时 554ms
            // java.lang.SecurityException: Call from user 0 as user -2 without permission INTERACT_ACROSS_USERS or INTERACT_ACROSS_USERS_FULL not allowed.
            rootInActiveWindow?.setGeneratedTime()
        } catch (_: Throwable) {
            null
        }.apply {
//            a11yContext.rootCache.value = this
        }

    val safeActiveWindowAppId: String?
        get() = safeActiveWindow?.packageName?.toString()

    override val scope = useScope()
    var isInteractive = true
        private set
    var onScreenForcedActive = {}
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            val action = intent?.action ?: return
            LogUtils.d("screenStateReceiver->${action}")
            isInteractive = when (action) {
                Intent.ACTION_SCREEN_ON -> true
                Intent.ACTION_SCREEN_OFF -> false
                Intent.ACTION_USER_PRESENT -> true
                else -> isInteractive
            }
            if (isInteractive) {
                val t = System.currentTimeMillis()
                if (t - appChangeTime > 500) { // 37.872(a11y) -> 38.228(onReceive)
                    onScreenForcedActive()
                }
            }
        }
    }

    @Volatile
    var willDestroyByBlock = false

    init {
        useLogLifecycle()
        useAliveFlow(isRunning)
        onCreated { a11yRef = this }
        onDestroyed { a11yRef = null }
        onCreated {
            isInteractive = app.powerManager.isInteractive
            ContextCompat.registerReceiver(
                this,
                screenStateReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT)
                },
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        onDestroyed { unregisterReceiver(screenStateReceiver) }
        onA11yFeatInit()
    }

    companion object {
        val a11yCn by lazy { SelectToSpeakService::class.componentName }

        val isRunning = MutableStateFlow(false)
        private var a11yRef: A11yService? = null
        val instance: A11yService?
            get() = a11yRef
    }
}