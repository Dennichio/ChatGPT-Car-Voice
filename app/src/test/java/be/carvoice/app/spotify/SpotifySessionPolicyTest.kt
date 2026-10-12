package be.carvoice.app.spotify

import android.media.session.PlaybackState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotifySessionPolicyTest {
    @Test
    fun transportRequiresPermissionSessionAndTheSpecificAdvertisedAction() {
        val actions = PlaybackState.ACTION_PLAY or PlaybackState.ACTION_SKIP_TO_NEXT

        assertTrue(SpotifySessionPolicy.canDispatch(true, true, actions, PlaybackState.ACTION_PLAY))
        assertTrue(SpotifySessionPolicy.canDispatch(true, true, actions, PlaybackState.ACTION_SKIP_TO_NEXT))
        assertFalse(SpotifySessionPolicy.canDispatch(false, true, actions, PlaybackState.ACTION_PLAY))
        assertFalse(SpotifySessionPolicy.canDispatch(true, false, actions, PlaybackState.ACTION_PLAY))
        assertFalse(SpotifySessionPolicy.canDispatch(true, true, actions, PlaybackState.ACTION_PAUSE))
    }
}
