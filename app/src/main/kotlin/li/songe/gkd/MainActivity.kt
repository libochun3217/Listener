package li.songe.gkd

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dylanc.activityresult.launcher.PickContentLauncher
import com.dylanc.activityresult.launcher.RequestPermissionLauncher
import com.dylanc.activityresult.launcher.StartActivityLauncher
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import li.songe.gkd.composition.CompositionActivity
import li.songe.gkd.composition.CompositionExt.useLifeCycleLog
import li.songe.gkd.service.ManageService
import li.songe.gkd.ui.NavGraphs
import li.songe.gkd.ui.component.ConfirmDialog
import li.songe.gkd.ui.theme.AppTheme
import li.songe.gkd.util.AuthDialog
import li.songe.gkd.util.LocalLauncher
import li.songe.gkd.util.LocalNavController
import li.songe.gkd.util.LocalPickContentLauncher
import li.songe.gkd.util.LocalRequestPermissionLauncher
import li.songe.gkd.util.UpgradeDialog
import li.songe.gkd.util.appInfoCacheFlow
import li.songe.gkd.util.initOrResetAppInfoCache
import li.songe.gkd.util.map
import li.songe.gkd.util.storeFlow

@AndroidEntryPoint
class MainActivity : CompositionActivity({
    this as MainActivity
    useLifeCycleLog()
    val launcher = StartActivityLauncher(this)
    val pickContentLauncher = PickContentLauncher(this)
    val requestPermissionLauncher = RequestPermissionLauncher(this)

    lifecycleScope.launch {
        (app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).let { manager ->
            manager.appTasks.forEach { task ->
                task?.setExcludeFromRecents(true)
            }
        }
    }

    ManageService.autoStart(this)
    mainVm

    setContent {
        val navController = rememberNavController()

        AppTheme {
            CompositionLocalProvider(
                LocalLauncher provides launcher,
                LocalPickContentLauncher provides pickContentLauncher,
                LocalRequestPermissionLauncher provides requestPermissionLauncher,
                LocalNavController provides navController
            ) {
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController
                )
            }
            ConfirmDialog()
            AuthDialog()
            UpgradeDialog()
        }
    }
}) {
    val mainVm by viewModels<MainViewModel>()

    override fun onStart() {
        super.onStart()
        activityVisibleFlow.update { it + 1 }
    }

    override fun onResume() {
        super.onResume()

        // https://github.com/gkd-kit/gkd/issues/543
        // ContextCompat.checkSelfPermission(app, Manifest.permission.QUERY_ALL_PACKAGES) always is GRANTED
        if (appInfoCacheFlow.value.count { e -> !e.value.isSystem && !e.value.hidden } < 16) {
            lifecycleScope.launch(Dispatchers.IO) {
                initOrResetAppInfoCache()
            }
        }

        val c = ComponentName(this, MainActivity::class.java)
        packageManager.setComponentEnabledSetting(c, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }

    override fun onStop() {
        super.onStop()
        activityVisibleFlow.update { it - 1 }
    }
}

val activityVisibleFlow = MutableStateFlow(0)




