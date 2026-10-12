package be.carvoice.app

import android.media.AudioDeviceCallback
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.content.ActivityNotFoundException
import android.provider.Settings
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.car.app.connection.CarConnection
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import be.carvoice.app.integration.DefaultAssistantLauncher
import be.carvoice.app.integration.AssistantLaunchResult
import be.carvoice.app.model.LauncherState
import be.carvoice.app.ui.CarVoiceApp
import be.carvoice.app.ui.CarConfigurationPanel
import be.carvoice.app.spotify.SpotifyController
import be.carvoice.app.spotify.SpotifyStatus

class MainActivity : ComponentActivity() {
    private lateinit var audio: AudioManager
    private var state by mutableStateOf(LauncherState())
    private var microphoneGranted by mutableStateOf(false)
    private var spotifyStatus by mutableStateOf(SpotifyStatus(false, false, null, false, false, false, false))
    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        microphoneGranted = granted
    }
    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) { refresh() }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) { refresh() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = getSystemService(AudioManager::class.java)
        audio.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        try {
            CarConnection(applicationContext).type.observe(this) { type ->
                state = state.copy(carConnection = when (type) {
                    CarConnection.CONNECTION_TYPE_PROJECTION -> "Android Auto-projectie gedetecteerd"
                    CarConnection.CONNECTION_TYPE_NATIVE -> "Android Automotive gedetecteerd"
                    else -> "Geen projectie gedetecteerd; Bluetooth kan afzonderlijk verbonden zijn"
                })
            }
        } catch (_: Exception) {
            state = state.copy(carConnection = "Detectie niet beschikbaar; handmatig starten blijft mogelijk")
        }
        setContent {
            Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                CarVoiceApp(
                    state = state,
                    onLaunch = { launchDefaultAssistant() },
                    onRefresh = { refresh(); state = state.copy(status = "Verbindingen opnieuw gecontroleerd.") },
                    setupContent = {
                        CarConfigurationPanel(
                            context = this@MainActivity,
                            microphoneGranted = microphoneGranted,
                            onGrantMicrophone = { microphonePermission.launch(Manifest.permission.RECORD_AUDIO) },
                            spotifyStatus = spotifyStatus,
                            onGrantSpotify = {
                                try { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                                catch (_: ActivityNotFoundException) { state = state.copy(status = "Android biedt dit toestemmingenscherm niet aan.") }
                                catch (_: SecurityException) { state = state.copy(status = "Android heeft het toestemmingenscherm geblokkeerd.") }
                            },
                            onRefreshSpotify = { spotifyStatus = SpotifyController(this@MainActivity).status() },
                        )
                    },
                )
            }
        }
    }

    private fun launchDefaultAssistant() {
        state = state.copy(status = when (DefaultAssistantLauncher(this).launch()) {
            AssistantLaunchResult.REQUEST_SENT -> "Android kreeg het assistentverzoek. Of de ingestelde assistent echt luistert, is niet bevestigd."
            AssistantLaunchResult.NO_HANDLER -> "Android heeft geen assistent gevonden die dit verzoek kan verwerken."
            AssistantLaunchResult.BLOCKED -> "Android heeft het assistentverzoek geweigerd."
        })
    }

    private fun refresh() {
        if (!::audio.isInitialized) return
        microphoneGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        spotifyStatus = SpotifyController(this).status()
        state = state.copy(
            audioOutputs = audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { device ->
                when (device.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth-media-uitgang beschikbaar"
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth-handsfree-uitgang beschikbaar"
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> "Bluetooth LE-headset beschikbaar"
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Telefoonluidspreker beschikbaar"
                    AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB-audio-uitgang beschikbaar"
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Bedrade audio-uitgang beschikbaar"
                    else -> "Audio-uitgang beschikbaar (Android-type ${device.type})"
                }
            }.distinct(),
        )
    }

    override fun onResume() { super.onResume(); refresh() }
    override fun onDestroy() {
        if (::audio.isInitialized) audio.unregisterAudioDeviceCallback(deviceCallback)
        super.onDestroy()
    }
}
