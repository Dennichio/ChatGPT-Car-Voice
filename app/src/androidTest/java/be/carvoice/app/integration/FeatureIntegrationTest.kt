package be.carvoice.app.integration

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.carvoice.app.car.CarVoiceService
import be.carvoice.app.spotify.SpotifyCommand
import be.carvoice.app.spotify.SpotifyController
import be.carvoice.app.spotify.SpotifyResult
import be.carvoice.app.spotify.SpotifySessionListener
import be.carvoice.app.voice.SpeechModelStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeatureIntegrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun automotiveManifestPublishesPoiServiceAndMinimumCarApiFive() {
        @Suppress("DEPRECATION")
        val app = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, CarVoiceService::class.java),
            PackageManager.GET_META_DATA,
        )
        val poiService = context.packageManager.queryIntentServices(
            Intent("androidx.car.app.CarAppService")
                .addCategory("androidx.car.app.category.POI")
                .setPackage(context.packageName),
            0,
        ).firstOrNull { it.serviceInfo.name == CarVoiceService::class.java.name }

        assertTrue("Android Auto host validation needs the API 5 minimum", app.metaData.getInt("androidx.car.app.minCarApiLevel") >= 5)
        assertTrue("POI car service must be exported for Android Auto", service.exported)
        assertNull("POI service does not require a signature permission", service.permission)
        assertNotNull("POI service must declare both the car service action and POI category", poiService)
    }

    @Test
    fun spotifyNotificationListenerUsesAndroidProtectedBindingPermission() {
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, SpotifySessionListener::class.java),
            0,
        )
        val listenerServices = context.packageManager.queryIntentServices(
            Intent("android.service.notification.NotificationListenerService")
                .setPackage(context.packageName),
            0,
        )

        assertEquals("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE", service.permission)
        assertTrue(service.exported)
        assertTrue(listenerServices.any { it.serviceInfo.name == SpotifySessionListener::class.java.name })
    }

    @Test
    fun spotifyAccessDeniedReturnsUnavailableWithoutDispatchingCommands() {
        val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val listener = ComponentName(context, SpotifySessionListener::class.java)
        assumeFalse("This test exercises the denied-notification-access path", notifications.isNotificationListenerAccessGranted(listener))

        val status = SpotifyController(context).status()
        val result = SpotifyController(context).execute(SpotifyCommand.Play)

        assertFalse(status.accessGranted)
        assertFalse(status.sessionFound)
        assertFalse(status.canPlay)
        assertTrue(result is SpotifyResult.Unavailable)
    }

    @Test
    fun experimentalVoiceProbeReportsMissingChatGptActivityWhenAppIsAbsent() {
        val installed = try {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(ChatGptLauncher.PACKAGE_NAME, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
        assumeFalse("This test exercises the missing-ChatGPT path", installed)

        val probe = ExperimentalVoiceProbe(context)
        val result = probe.availability()

        assertNotNull("Missing ChatGPT must produce an unavailable diagnostic", result)
        assertEquals(result, probe.launch())
    }

    @Test
    fun voiceSettingsAreOffByDefaultAndExplicitOptInPersists() {
        val preferences = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
        val hadValue = preferences.contains("experimental_car_voice")
        val previousValue = preferences.getBoolean("experimental_car_voice", false)
        try {
            assertTrue("Test starts with a clean opt-in setting", preferences.edit().clear().commit())
            val settings = VoiceSettings(context)
            assertFalse("Experimental car Voice must default to off", settings.experimentalCarVoice)

            settings.experimentalCarVoice = true
            assertTrue("Opt-in must persist across settings instances", VoiceSettings(context).experimentalCarVoice)

            settings.experimentalCarVoice = false
            assertFalse(VoiceSettings(context).experimentalCarVoice)
        } finally {
            val restore = preferences.edit().clear()
            if (hadValue) restore.putBoolean("experimental_car_voice", previousValue)
            assertTrue("Restore the emulator's original test preference", restore.commit())
        }
    }

    @Test
    fun constructingSpeechModelStoreDoesNotCreateOrDownloadAModel() {
        val modelRoot = context.filesDir.resolve("offline-speech")
        assumeFalse("This test exercises first-run model setup", modelRoot.exists())

        val store = SpeechModelStore(context)

        assertFalse(store.isReady())
        assertFalse(store.status.value.ready)
        assertFalse(store.status.value.installing)
        assertFalse("Construction must not create model files or begin downloading", modelRoot.exists())
    }
}
