package be.carvoice.app.car

import be.carvoice.app.navigation.NavigationCommand
import be.carvoice.app.navigation.NavigationCommandParser
import be.carvoice.app.spotify.SpotifyCommand
import be.carvoice.app.spotify.SpotifyCommandParser
import be.carvoice.app.spotify.SpotifyParseResult

internal sealed interface SpokenCommand {
    data class Navigation(val command: NavigationCommand) : SpokenCommand
    data class Music(val command: SpotifyCommand) : SpokenCommand
    data object MusicStatus : SpokenCommand
    data class Unsupported(val reason: String) : SpokenCommand
}

/** One production routing decision: "zoek" can be a POI query as well as a music request. */
internal object SpokenCommandRouter {
    fun route(text: String): SpokenCommand {
        // Resolve complete navigation commands before Spotify's broader name-search detection.
        NavigationCommandParser.parse(text)?.let { return SpokenCommand.Navigation(it) }
        return when (val music = SpotifyCommandParser.parse(text)) {
            is SpotifyParseResult.Command -> SpokenCommand.Music(music.command)
            SpotifyParseResult.ShowStatus -> SpokenCommand.MusicStatus
            is SpotifyParseResult.Unsupported -> SpokenCommand.Unsupported(music.reason)
            SpotifyParseResult.Unknown -> SpokenCommand.Unsupported(
                "Opdracht niet herkend. Zeg navigeer naar een bestemming, zoek een plaats in de buurt, of een muziekcommando."
            )
        }
    }
}
