package be.carvoice.app.navigation

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationRepositoryTest {
    @Test
    fun trimsQueryFiltersInvalidCoordinatesAndCapsResultsAtFive() = runBlocking {
        var receivedQuery = ""
        var receivedLimit = 0
        val candidates = (0..5).map { index ->
            Poi("$index", "Locatie $index", "Adres $index", 50.0 + index, 4.0)
        } + Poi("invalid", "Ongeldig", "Onbekend", 91.0, 4.0)
        val repository = NavigationRepository(GeocoderAccess { query, limit ->
            receivedQuery = query
            receivedLimit = limit
            candidates
        })

        val result = repository.search("  Leuven  ")

        assertEquals("Leuven", receivedQuery)
        assertEquals(NavigationRepository.MAX_RESULTS, receivedLimit)
        assertEquals(5, result.size)
        assertEquals("0", result.first().id)
        assertEquals("4", result.last().id)
    }

    @Test
    fun blankQueriesDoNotCallGeocoder() = runBlocking {
        var calls = 0
        val repository = NavigationRepository(GeocoderAccess { _, _ ->
            calls++
            emptyList()
        })

        assertEquals(emptyList<Poi>(), repository.search("   "))
        assertEquals(0, calls)
    }
}
