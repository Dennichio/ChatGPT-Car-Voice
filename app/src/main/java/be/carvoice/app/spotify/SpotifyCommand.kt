package be.carvoice.app.spotify

sealed interface SpotifyCommand {
    data object Play : SpotifyCommand
    data object Pause : SpotifyCommand
    data object Next : SpotifyCommand
    data object Previous : SpotifyCommand
}

sealed interface SpotifyParseResult {
    data class Command(val command: SpotifyCommand) : SpotifyParseResult
    data object ShowStatus : SpotifyParseResult
    data class Unsupported(val reason: String) : SpotifyParseResult
    data object Unknown : SpotifyParseResult
}

/** Parses a small set of explicit Dutch playback commands; it never invents a track or URI. */
object SpotifyCommandParser {
    fun parse(text: String): SpotifyParseResult {
        val phrase = text.trim().lowercase().replace(Regex("[.!?,]+$"), "").trim()
        return when (phrase) {
            "pauzeer muziek", "pauzeer", "pauze" -> SpotifyParseResult.Command(SpotifyCommand.Pause)
            "volgend nummer", "volgende nummer", "volgende" -> SpotifyParseResult.Command(SpotifyCommand.Next)
            "vorig nummer", "vorige nummer", "vorige" -> SpotifyParseResult.Command(SpotifyCommand.Previous)
            "hervat muziek", "hervat", "speel verder" -> SpotifyParseResult.Command(SpotifyCommand.Play)
            "welk nummer speelt er", "welk nummer speelt", "wat speelt er" -> SpotifyParseResult.ShowStatus
            else -> if (isNameSearch(phrase)) {
                SpotifyParseResult.Unsupported(
                    "Zoeken op artiest-, nummer- of playlistnaam is niet beschikbaar via de actieve mediasessie."
                )
            } else {
                SpotifyParseResult.Unknown
            }
        }
    }

    private fun isNameSearch(phrase: String): Boolean =
        phrase.startsWith("speel ") || phrase.startsWith("zoek ") ||
            phrase.startsWith("zoek naar ") || phrase.startsWith("speel nummer ")
}
