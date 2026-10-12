package be.carvoice.app.spotify

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.app.NotificationManager

private const val SPOTIFY_PACKAGE = "com.spotify.music"

data class SpotifyStatus(
    val accessGranted: Boolean,
    val sessionFound: Boolean,
    val track: String?,
    val canPlay: Boolean,
    val canPause: Boolean,
    val canNext: Boolean,
    val canPrevious: Boolean,
    val problem: String? = null,
)

sealed interface SpotifyResult {
    data class Dispatched(val command: SpotifyCommand) : SpotifyResult
    data class Unavailable(val reason: String) : SpotifyResult
    data class Unsupported(val reason: String) : SpotifyResult
}

/** Dispatches transport requests to an already active Spotify media session only. */
class SpotifyController(private val context: Context) {
    fun status(): SpotifyStatus {
        if (!hasNotificationAccess()) return deniedStatus()
        return try {
            val controller = activeSpotifyController()
                ?: return SpotifyStatus(
                    accessGranted = true,
                    sessionFound = false,
                    track = null,
                    canPlay = false,
                    canPause = false,
                    canNext = false,
                    canPrevious = false,
                    problem = "Geen actieve Spotify-mediasessie gevonden.",
                )
            val actions = controller.playbackState?.actions ?: 0L
            SpotifyStatus(
                accessGranted = true,
                sessionFound = true,
                track = controller.metadata?.toTrackLabel(),
                canPlay = SpotifySessionPolicy.supports(actions, PlaybackState.ACTION_PLAY),
                canPause = SpotifySessionPolicy.supports(actions, PlaybackState.ACTION_PAUSE),
                canNext = SpotifySessionPolicy.supports(actions, PlaybackState.ACTION_SKIP_TO_NEXT),
                canPrevious = SpotifySessionPolicy.supports(actions, PlaybackState.ACTION_SKIP_TO_PREVIOUS),
            )
        } catch (_: SecurityException) {
            deniedStatus()
        } catch (_: RuntimeException) {
            SpotifyStatus(
                accessGranted = true,
                sessionFound = false,
                track = null,
                canPlay = false,
                canPause = false,
                canNext = false,
                canPrevious = false,
                problem = "Spotify-mediasessie kon niet worden gelezen.",
            )
        }
    }

    fun execute(command: SpotifyCommand): SpotifyResult {
        if (!hasNotificationAccess()) {
            return SpotifyResult.Unavailable("Geef eerst notificatietoegang aan deze app.")
        }
        val controller = try {
            activeSpotifyController()
        } catch (_: SecurityException) {
            return SpotifyResult.Unavailable("Notificatietoegang ontbreekt of is ingetrokken.")
        } catch (_: RuntimeException) {
            return SpotifyResult.Unavailable("Spotify-mediasessie is niet beschikbaar.")
        } ?: return SpotifyResult.Unavailable("Geen actieve Spotify-mediasessie gevonden.")

        val actions = controller.playbackState?.actions ?: 0L
        val requiredAction = command.requiredAction
        if (!SpotifySessionPolicy.canDispatch(
                accessGranted = true,
                sessionFound = true,
                actions = actions,
                requiredAction = requiredAction,
            )
        ) {
            return SpotifyResult.Unsupported("Spotify biedt deze actie momenteel niet aan.")
        }
        return try {
            when (command) {
                SpotifyCommand.Play -> controller.transportControls.play()
                SpotifyCommand.Pause -> controller.transportControls.pause()
                SpotifyCommand.Next -> controller.transportControls.skipToNext()
                SpotifyCommand.Previous -> controller.transportControls.skipToPrevious()
            }
            SpotifyResult.Dispatched(command)
        } catch (_: SecurityException) {
            SpotifyResult.Unavailable("Spotify heeft het bedieningsverzoek geweigerd.")
        } catch (_: RuntimeException) {
            SpotifyResult.Unavailable("Het verzoek kon niet naar Spotify worden verstuurd.")
        }
    }

    private fun activeSpotifyController(): MediaController? {
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        return manager.getActiveSessions(ComponentName(context, SpotifySessionListener::class.java))
            .firstOrNull { it.packageName == SPOTIFY_PACKAGE }
    }

    private fun hasNotificationAccess(): Boolean {
        val component = ComponentName(context, SpotifySessionListener::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return manager.isNotificationListenerAccessGranted(component)
        }
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
    }

    private fun deniedStatus() = SpotifyStatus(
        accessGranted = false,
        sessionFound = false,
        track = null,
        canPlay = false,
        canPause = false,
        canNext = false,
        canPrevious = false,
        problem = "Geef notificatietoegang aan deze app om Spotify te bedienen.",
    )
}

private val SpotifyCommand.requiredAction: Long
    get() = when (this) {
        SpotifyCommand.Play -> PlaybackState.ACTION_PLAY
        SpotifyCommand.Pause -> PlaybackState.ACTION_PAUSE
        SpotifyCommand.Next -> PlaybackState.ACTION_SKIP_TO_NEXT
        SpotifyCommand.Previous -> PlaybackState.ACTION_SKIP_TO_PREVIOUS
    }

internal object SpotifySessionPolicy {
    fun supports(actions: Long, action: Long): Boolean = actions and action != 0L

    fun canDispatch(
        accessGranted: Boolean,
        sessionFound: Boolean,
        actions: Long,
        requiredAction: Long,
    ): Boolean = accessGranted && sessionFound && supports(actions, requiredAction)
}

private fun MediaMetadata.toTrackLabel(): String? {
    val title = getString(MediaMetadata.METADATA_KEY_TITLE)?.takeIf(String::isNotBlank)
    val artist = getString(MediaMetadata.METADATA_KEY_ARTIST)?.takeIf(String::isNotBlank)
    return when {
        title == null -> artist
        artist == null -> title
        else -> "$artist — $title"
    }
}
