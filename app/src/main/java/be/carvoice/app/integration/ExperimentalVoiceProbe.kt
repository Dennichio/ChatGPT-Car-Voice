package be.carvoice.app.integration

import android.app.Activity
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Intent pattern adapted from kangrio/Assistant, Utils.kt (MIT, Copyright 2026 KangRio).
 * See THIRD_PARTY_CODE.md and assets/licenses/BYD-Assistant-MIT.txt.
 * This is an explicit diagnostic, NOT a stable OpenAI API or an Android Auto voice bridge.
 */
class ExperimentalVoiceProbe(private val context: Context) {
    private val component = ComponentName(
        ChatGptLauncher.PACKAGE_NAME,
        "com.openai.voice.assistant.AssistantActivity",
    )

    fun availability(): String? {
        val info = try {
            @Suppress("DEPRECATION")
            context.packageManager.getActivityInfo(component, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return "Deze ChatGPT-versie biedt de experimentele activiteit niet aan."
        }
        if (!info.enabled || !info.applicationInfo.enabled) return "De Voice-activiteit is uitgeschakeld."
        if (!info.exported) return "De Voice-activiteit is niet toegankelijk voor andere apps."
        if (!info.permission.isNullOrBlank() &&
            context.checkSelfPermission(info.permission) != PackageManager.PERMISSION_GRANTED
        ) return "De Voice-activiteit vereist een toestemming die deze app niet heeft."
        return null
    }

    /** Dispatch alone does not establish that Voice starts or can use the vehicle microphone. */
    fun launch(): String {
        availability()?.let { return it }
        return try {
            val intent = Intent(Intent.ACTION_ASSIST).setComponent(component).putExtra("isAssistant", true)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "Experimenteel startverzoek verzonden. Controleer zelf of ChatGPT werkelijk luistert."
        } catch (_: ActivityNotFoundException) {
            "De Voice-activiteit kon niet worden geopend; de gewone Open ChatGPT-knop blijft beschikbaar."
        } catch (_: SecurityException) {
            "Android heeft het experimentele startverzoek geweigerd."
        }
    }
}
