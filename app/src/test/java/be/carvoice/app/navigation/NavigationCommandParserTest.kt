package be.carvoice.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationCommandParserTest {
    @Test
    fun parsesAddressNavigationAndPreservesSpokenAddress() {
        assertEquals(
            NavigationCommand.Navigate("Stationsstraat 10 in Diest"),
            NavigationCommandParser.parse("Navigeer naar Stationsstraat 10 in Diest."),
        )
    }

    @Test
    fun mapsDutchHomeAndWorkPhrasesToFavoriteSlots() {
        assertEquals(
            NavigationCommand.GoToFavorite(FavoriteId.WORK.value),
            NavigationCommandParser.parse("Breng me naar mijn werk"),
        )
        assertEquals(
            NavigationCommand.GoToFavorite(FavoriteId.HOME.value),
            NavigationCommandParser.parse("Breng me naar huis"),
        )
    }

    @Test
    fun recognizesNearbySearchWithoutPretendingItResolvedAPlace() {
        assertEquals(
            NavigationCommand.SearchNearby("laadpaal"),
            NavigationCommandParser.parse("Zoek een laadpaal in de buurt"),
        )
        assertEquals(
            NavigationCommand.SearchNearby("supermarkt"),
            NavigationCommandParser.parse("Zoek een supermarkt in de buurt"),
        )
    }

    @Test
    fun rejectsBlankOrUnrelatedCommands() {
        assertNull(NavigationCommandParser.parse("   "))
        assertNull(NavigationCommandParser.parse("Wat is het weer vandaag?"))
        assertNull(NavigationCommandParser.parse("Navigeer naar"))
    }
}
