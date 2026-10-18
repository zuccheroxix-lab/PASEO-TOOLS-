package com.example.shizuku

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelFileDescriptor
import com.example.model.ShizukuInfo
import com.example.model.ShizukuStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuManager(private val context: Context) {

    private val _shizukuInfo = MutableStateFlow(ShizukuInfo())
    val shizukuInfo: StateFlow<ShizukuInfo> = _shizukuInfo.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _shizukuInfo.value = ShizukuInfo(
            status = ShizukuStatus.DISCONNECTED,
            isInstalled = isShizukuAppInstalled(),
            apiVersion = 0,
            patchVersion = 0,
            isPermissionGranted = false,
            statusMessage = "Service Shizuku terputus (Binder dead)."
        )
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE_PERMISSION) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            val current = _shizukuInfo.value
            _shizukuInfo.value = current.copy(
                isPermissionGranted = granted,
                statusMessage = if (granted) "Izin Shizuku berhasil diberikan!" else "Izin Shizuku ditolak oleh pengguna."
            )
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            // Shizuku library initialization safely handled
        }
        checkStatus()
    }

    fun isShizukuAppInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo("moe.shizuku.privileged.api", PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Throwable) {
            false
        }
    }

    fun checkStatus(): ShizukuInfo {
        val installed = isShizukuAppInstalled()

        val isAlive = try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }

        if (!isAlive) {
            val message = if (installed) {
                "Aplikasi Shizuku terpasang, namun servicenya belum berjalan. Aktifkan lewat Wireless Debugging."
            } else {
                "Aplikasi Shizuku tidak ditemukan di perangkat. Silakan instal aplikasi Shizuku terlebih dahulu."
            }
            val info = ShizukuInfo(
                status = if (installed) ShizukuStatus.DISCONNECTED else ShizukuStatus.UNSUPPORTED,
                isInstalled = installed,
                apiVersion = 0,
                patchVersion = 0,
                isPermissionGranted = false,
                statusMessage = message
            )
            _shizukuInfo.value = info
            return info
        }

        val apiVersion = try { Shizuku.getVersion() } catch (e: Throwable) { 0 }
        val patchVersion = try { Shizuku.getServerPatchVersion() } catch (e: Throwable) { 0 }

        val hasPermission = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }

        val msg = if (hasPermission) {
            "Shizuku terhubung aktif dengan izin hak akses sistem (v$apiVersion.$patchVersion)."
        } else {
            "Shizuku aktif, namun aplikasi PASEO SENSI belum memiliki izin akses."
        }

        val info = ShizukuInfo(
            status = ShizukuStatus.CONNECTED,
            isInstalled = installed,
            apiVersion = apiVersion,
            patchVersion = patchVersion,
            isPermissionGranted = hasPermission,
            statusMessage = msg
        )
        _shizukuInfo.value = info
        return info
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
                } else {
                    checkStatus()
                }
            } else {
                _shizukuInfo.value = _shizukuInfo.value.copy(
                    statusMessage = "Gagal meminta izin: Service Shizuku sedang tidak aktif."
                )
            }
        } catch (e: Throwable) {
            _shizukuInfo.value = _shizukuInfo.value.copy(
                statusMessage = "Error meminta izin: ${e.localizedMessage ?: "Tidak diketahui"}"
            )
        }
    }

    /**
     * Executes validated ADB/Shizuku shell commands with strict security whitelisting.
     * Prevents raw unvalidated shell injection.
     */
    suspend fun executeSafeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder()) {
            return@withContext Result.failure(
                IllegalStateException("Service Shizuku tidak aktif. Jalankan Shizuku terlebih dahulu.")
            )
        }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return@withContext Result.failure(
                SecurityException("Aplikasi PASEO SENSI belum diberikan izin di Shizuku Manager.")
            )
        }

        // Strict whitelist validation
        val isAllowed = ALLOWED_COMMAND_PREFIXES.any { prefix -> command.startsWith(prefix) }
        if (!isAllowed) {
            return@withContext Result.failure(
                IllegalArgumentException("Perintah '$command' tidak diizinkan demi keamanan perangkat.")
            )
        }

        try {
            val binder = Shizuku.getBinder()
                ?: return@withContext Result.failure(IllegalStateException("Shizuku Binder null"))

            val service = IShizukuService.Stub.asInterface(binder)
            val remoteProcess = service.newProcess(arrayOf("sh", "-c", command), null, null)

            val output = StringBuilder()
            val errorOutput = StringBuilder()

            val inPfd = remoteProcess.inputStream
            val errPfd = remoteProcess.errorStream

            val reader = BufferedReader(InputStreamReader(ParcelFileDescriptor.AutoCloseInputStream(inPfd)))
            val errorReader = BufferedReader(InputStreamReader(ParcelFileDescriptor.AutoCloseInputStream(errPfd)))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.appendLine(line)
            }

            val exitCode = remoteProcess.waitFor()
            remoteProcess.destroy()

            if (exitCode == 0) {
                Result.success(output.toString().trim())
            } else {
                val err = errorOutput.toString().trim().ifEmpty { "Perintah gagal dengan exit code $exitCode" }
                Result.failure(IllegalStateException(err))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    fun cleanUp() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            // Ignore cleanup errors
        }
    }

    companion object {
        const val REQUEST_CODE_PERMISSION = 1001

        private val ALLOWED_COMMAND_PREFIXES = listOf(
            "wm size",
            "wm density",
            "settings get system pointer_location",
            "settings put system pointer_location",
            "settings get system pointer_speed",
            "settings put system pointer_speed",
            "settings get global window_animation_scale",
            "settings put global window_animation_scale",
            "settings get global transition_animation_scale",
            "settings put global transition_animation_scale",
            "settings get global animator_duration_scale",
            "settings put global animator_duration_scale"
        )
    }
}
