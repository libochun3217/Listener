package li.songe.gkd.util

import android.text.format.DateUtils
import com.blankj.utilcode.util.LogUtils
import li.songe.gkd.META
import li.songe.gkd.app
import li.songe.gkd.appScope
import li.songe.gkd.permission.allPermissionStates
import java.io.File

fun File.autoMk(): File {
    if (!exists()) {
        mkdirs()
    }
    return this
}

fun File.autoCreate(): File {
    if (!exists()) {
        createNewFile()
    }
    return this
}

fun File.appendTime() {
    appendText(
        "\ntime->${
            System.currentTimeMillis().format("yyyy-MM-dd HH:mm:ss")
        }\n"
    )
    appScope.launchTry {
        checkUpload()
    }
}

private val filesDir: File by lazy {
    app.getExternalFilesDir(null) ?: error("failed getExternalFilesDir")
}

val dbFolder: File
    get() = filesDir.resolve("db").autoMk()
val shFolder: File
    get() = filesDir.resolve("sh").autoMk()
val storeFolder: File
    get() = filesDir.resolve("store").autoMk()
val subsFolder: File
    get() = filesDir.resolve("subscription").autoMk()
val snapshotFolder: File
    get() = filesDir.resolve("snapshot").autoMk()

val privateStoreFolder: File
    get() = app.filesDir.resolve("store").autoMk()

private val cacheDir by lazy { app.externalCacheDir ?: app.cacheDir }
val coilCacheDir: File
    get() = cacheDir.resolve("coil").autoMk()
val sharedDir: File
    get() = cacheDir.resolve("shared").autoMk()
private val tempDir: File
    get() = cacheDir.resolve("temp").autoMk()
private val listenerDir: File
    get() = filesDir.resolve("listener").autoMk()
val appUseFile: File
    get() = listenerDir.resolve("appUse.txt").autoCreate()
val appListenerFile: File
    get() = listenerDir.resolve("appListener.txt")

fun createTempDir(): File {
    return tempDir
        .resolve(System.currentTimeMillis().toString())
        .apply { mkdirs() }
}

private fun removeExpired(dir: File) {
    dir.listFiles()?.forEach { f ->
        if (System.currentTimeMillis() - f.lastModified() > DateUtils.HOUR_IN_MILLIS) {
            if (f.isDirectory) {
                f.deleteRecursively()
            } else if (f.isFile) {
                f.delete()
            }
        }
    }
}

suspend fun checkUpload() {
    listenerDir.listFiles()?.map {
        if (it.length() > 1024*50) {
            upload(it.readText())
            it.delete()
        }
    }
}

fun clearCache() {
    removeExpired(sharedDir)
    removeExpired(tempDir)
}

fun buildLogFile(): File {
    val tempDir = createTempDir()
    val files = mutableListOf(dbFolder, storeFolder, subsFolder)
    LogUtils.getLogFiles().firstOrNull()?.parentFile?.let { files.add(it) }
    tempDir.resolve("appList.json").also {
        it.writeText(json.encodeToString(""))
        files.add(it)
    }
    tempDir.resolve("permission.txt").also {
        it.writeText(allPermissionStates.joinToString("\n") { state ->
            state.name + ": " + state.stateFlow.value.toString()
        })
        files.add(it)
    }
    tempDir.resolve("gkd-${META.versionCode}-v${META.versionName}.json").also {
        it.writeText(json.encodeToString(META))
        files.add(it)
    }
    val logZipFile = sharedDir.resolve("log-${System.currentTimeMillis()}.zip")
    ZipUtils.zipFiles(files, logZipFile)
    tempDir.deleteRecursively()
    return logZipFile
}
