package be.carvoice.app.integration

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/**
 * Requests the system-configured assistant through Android's public implicit Intent.
 * A dispatched request does not prove that a listening session started.
 * Android Auto may block the launch or display the assistant on the phone.
 */
class DefaultAssistantLauncher(private val context: Context) {
    fun launch(): AssistantLaunchResult = try {
        context.startActivity(Intent(Intent.ACTION_ASSIST).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        AssistantLaunchResult.REQUEST_SENT
    } catch (_: ActivityNotFoundException) {
        AssistantLaunchResult.NO_HANDLER
    } catch (_: SecurityException) {
        AssistantLaunchResult.BLOCKED
    } catch (_: IllegalStateException) {
        AssistantLaunchResult.BLOCKED
    }
}

enum class AssistantLaunchResult { REQUEST_SENT, NO_HANDLER, BLOCKED }
