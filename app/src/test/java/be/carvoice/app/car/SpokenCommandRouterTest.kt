package be.carvoice.app.car

import be.carvoice.app.navigation.NavigationCommand
import be.carvoice.app.spotify.SpotifyCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exercises the combined router used by the car microphone, rather than either parser alone. */
class SpokenCommandRouterTest {
    @Test fun chargingSearchCannotBeConsumedBySpotifyNameSearch() {
        assertEquals(
            SpokenCommand.Navigation(NavigationCommand.SearchNearby("laadpaal")),
            SpokenCommandRouter.route("Zoek een laadpaal in de buurt."),
        )
    }

    @Test fun supermarketSearchCannotBeConsumedBySpotifyNameSearch() {
        assertEquals(
            SpokenCommand.Navigation(NavigationCommand.SearchNearby("supermarkt")),
            SpokenCommandRouter.route("zoek een supermarkt in de buurt"),
        )
    }

    @Test fun addressKeepsItsDestinationThroughCombinedRouting() {
        assertEquals(
            SpokenCommand.Navigation(NavigationCommand.Navigate("Stationsstraat 10 in Diest")),
            SpokenCommandRouter.route("Navigeer naar Stationsstraat 10 in Diest"),
        )
    }

    @Test fun favoriteStillRoutesToSavedDestination() {
        assertEquals(
            SpokenCommand.Navigation(NavigationCommand.GoToFavorite("work")),
            SpokenCommandRouter.route("Breng me naar mijn werk"),
        )
    }

    @Test fun explicitSpotifyTransportAndMetadataStillRouteToMusic() {
        assertEquals(SpokenCommand.Music(SpotifyCommand.Pause), SpokenCommandRouter.route("pauzeer muziek"))
        assertEquals(SpokenCommand.Music(SpotifyCommand.Next), SpokenCommandRouter.route("volgend nummer"))
        assertEquals(SpokenCommand.MusicStatus, SpokenCommandRouter.route("Welk nummer speelt er?"))
    }

    @Test fun unsupportedMusicSearchProducesARealCapabilityError() {
        val command = SpokenCommandRouter.route("Speel muziek van Coldplay")
        assertTrue(command is SpokenCommand.Unsupported)
        assertTrue((command as SpokenCommand.Unsupported).reason.contains("niet beschikbaar"))
    }

    @Test fun generalQuestionDoesNotPretendToBeProcessedByChatGpt() {
        val command = SpokenCommandRouter.route("Hoe werkt een warmtepomp?")
        assertTrue(command is SpokenCommand.Unsupported)
        assertTrue((command as SpokenCommand.Unsupported).reason.contains("Opdracht niet herkend"))
    }
}
