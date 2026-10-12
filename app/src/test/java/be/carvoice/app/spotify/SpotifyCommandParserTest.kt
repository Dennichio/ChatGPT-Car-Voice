package be.carvoice.app.spotify

import org.junit.Assert.assertEquals
import org.junit.Test

class SpotifyCommandParserTest {
    @Test
    fun parsesExplicitDutchPlaybackAndStatusCommands() {
        assertEquals(SpotifyParseResult.Command(SpotifyCommand.Pause), SpotifyCommandParser.parse("Pauzeer muziek"))
        assertEquals(SpotifyParseResult.Command(SpotifyCommand.Next), SpotifyCommandParser.parse("Volgend nummer"))
        assertEquals(SpotifyParseResult.Command(SpotifyCommand.Previous), SpotifyCommandParser.parse("Vorig nummer"))
        assertEquals(SpotifyParseResult.Command(SpotifyCommand.Play), SpotifyCommandParser.parse("Hervat muziek"))
        assertEquals(SpotifyParseResult.ShowStatus, SpotifyCommandParser.parse("Welk nummer speelt er?"))
    }

    @Test
    fun namedSearchIsClearlyUnsupportedAndUnknownTextIsNotDispatched() {
        assertEquals(SpotifyParseResult.Unsupported(
            "Zoeken op artiest-, nummer- of playlistnaam is niet beschikbaar via de actieve mediasessie."
        ), SpotifyCommandParser.parse("Speel Coldplay"))
        assertEquals(SpotifyParseResult.Unknown, SpotifyCommandParser.parse("start navigatie"))
    }
}
