package be.carvoice.app.spotify

import android.service.notification.NotificationListenerService

/** Notification access is used only as the system grant for MediaSessionManager access. */
class SpotifySessionListener : NotificationListenerService()
