package be.carvoice.app.integration

import android.content.Context

/** Explicit experimental opt-in; disabled on installation and never inferred from app availability. */
class VoiceSettings(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    var experimentalCarVoice: Boolean
        get() = preferences.getBoolean("experimental_car_voice", false)
        set(value) { preferences.edit().putBoolean("experimental_car_voice", value).apply() }
}
