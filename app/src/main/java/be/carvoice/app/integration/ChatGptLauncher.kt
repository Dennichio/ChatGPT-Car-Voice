package be.carvoice.app.integration

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import be.carvoice.app.model.LaunchAvailability

/** Only Android's public launcher interface; no private activity, URI or Voice-mode assumption. */
class ChatGptLauncher(private val activity: Activity) {
    fun availability(): LaunchAvailability {
        val info = try {
            @Suppress("DEPRECATION")
            activity.packageManager.getApplicationInfo(PACKAGE_NAME, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return LaunchAvailability.NOT_INSTALLED
        }
        if (!info.enabled) return LaunchAvailability.DISABLED
        return if (activity.packageManager.getLaunchIntentForPackage(PACKAGE_NAME) != null) {
            LaunchAvailability.READY
        } else LaunchAvailability.NO_LAUNCH_ACTIVITY
    }

    fun version(): String? = try {
        @Suppress("DEPRECATION")
        activity.packageManager.getPackageInfo(PACKAGE_NAME, 0).versionName
    } catch (_: PackageManager.NameNotFoundException) { null }

    fun launch(): LaunchResult {
        val intent = activity.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: return LaunchResult.UNAVAILABLE
        return try {
            activity.startActivity(intent)
            LaunchResult.OPENED
        } catch (_: ActivityNotFoundException) {
            LaunchResult.UNAVAILABLE
        } catch (_: SecurityException) {
            LaunchResult.BLOCKED
        }
    }

    fun openStore(): Boolean = try {
        activity.startActivity(Intent(Intent.ACTION_VIEW, STORE_URL.toUri()))
        true
    } catch (_: ActivityNotFoundException) { false }
      catch (_: SecurityException) { false }

    companion object {
        const val PACKAGE_NAME = "com.openai.chatgpt"
        const val STORE_URL = "https://play.google.com/store/apps/details?id=com.openai.chatgpt"
    }
}
enum class LaunchResult { OPENED, UNAVAILABLE, BLOCKED }
