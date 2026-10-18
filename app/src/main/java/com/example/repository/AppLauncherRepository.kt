package com.example.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppLauncherRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val distinctApps = mutableMapOf<String, InstalledApp>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            // Skip PASEO SENSI itself from the launcher list
            if (pkg == context.packageName) continue

            if (!distinctApps.containsKey(pkg)) {
                val appName = try {
                    info.loadLabel(packageManager).toString()
                } catch (e: Exception) {
                    pkg
                }

                val appInfo = info.activityInfo.applicationInfo
                val isGame = isGameApp(appInfo, pkg, appName)

                val versionName = try {
                    val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L))
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(pkg, 0)
                    }
                    pInfo.versionName ?: "1.0"
                } catch (e: Exception) {
                    "1.0"
                }

                val iconDrawable = try {
                    info.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }

                distinctApps[pkg] = InstalledApp(
                    packageName = pkg,
                    appName = appName,
                    isGame = isGame,
                    versionName = versionName,
                    icon = iconDrawable
                )
            }
        }

        distinctApps.values.sortedWith(
            compareByDescending<InstalledApp> { it.isGame }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun isGameApp(appInfo: ApplicationInfo, packageName: String, appName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return true
        }
        @Suppress("DEPRECATION")
        if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) return true

        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()
        val gameKeywords = listOf(
            "game", "freefire", "dts.freefire", "pubg", "cod", "mobilelegends",
            "genshin", "roblox", "minecraft", "unity", "unreal", "supercell",
            "riotgames", "arena", "esport", "shooter", "rpg"
        )
        return gameKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }
    }

    fun launchApp(packageName: String): Result<Unit> {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Tidak dapat menemukan aktivitas pembuka untuk $packageName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
