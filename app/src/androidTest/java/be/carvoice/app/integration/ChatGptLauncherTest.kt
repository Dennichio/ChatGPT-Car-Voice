package be.carvoice.app.integration

import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.carvoice.app.MainActivity
import be.carvoice.app.model.LaunchAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatGptLauncherTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun absentChatGptIsReportedUnavailableAndLaunchFailsSafely() {
        val packageManager = context.packageManager
        val appInstalled = try {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(ChatGptLauncher.PACKAGE_NAME, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
        assumeFalse("This test exercises the absent-app path", appInstalled)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val launcher = ChatGptLauncher(activity)
                assertEquals(LaunchAvailability.NOT_INSTALLED, launcher.availability())
                assertEquals(null, launcher.version())
                assertEquals(LaunchResult.UNAVAILABLE, launcher.launch())
            }
        }
    }

    @Test
    fun mergedAppManifestRequestsOnlyTheRequiredCommandPermissions() {
        @Suppress("DEPRECATION")
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val requested = packageInfo.requestedPermissions.orEmpty().toSet()

        assertTrue("Car commands need explicit microphone permission", "android.permission.RECORD_AUDIO" in requested)
        assertTrue("Model download and geocoding need internet access", "android.permission.INTERNET" in requested)
        assertFalse("No firmware or assistant-settings bypass", "android.permission.WRITE_SECURE_SETTINGS" in requested)
        assertFalse("No background phone microphone service", "android.permission.FOREGROUND_SERVICE_MICROPHONE" in requested)
    }

    @Test
    fun launcherActivityIsDiscoverableThroughAndroidMainLauncherIntent() {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(context.packageName)
        val resolved = context.packageManager.queryIntentActivities(intent, 0)

        assertNotNull("CarVoice must expose its own launcher activity", resolved.firstOrNull())
        assertEquals(MainActivity::class.java.name, resolved.first().activityInfo.name)
        assertEquals(true, resolved.first().activityInfo.exported)
    }
}
