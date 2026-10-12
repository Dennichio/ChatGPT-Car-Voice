package be.carvoice.app.navigation

import androidx.car.app.CarContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NavigationAndroidTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun favoriteStorePersistsJsonAcrossInstancesAndDeletesIt() {
        val id = "test-${UUID.randomUUID()}"
        val first = FavoriteStore(context)
        val second = FavoriteStore(context)
        val poi = Poi(id, "Werk", "Stationsstraat 10, Diest", 50.98, 5.05)

        try {
            first.upsert(poi)
            assertEquals(poi, second.get(id))
            assertTrue(second.delete(id))
            assertEquals(null, first.get(id))
        } finally {
            first.delete(id)
        }
    }

    @Test
    fun navigationIntentsUseCarNavigationActionAndGeoUris() {
        val poiIntent = NavigationIntents.forPoi(Poi("one", "Station", "Stationsstraat 10", 50.98, 5.05))
        assertEquals(CarContext.ACTION_NAVIGATE, poiIntent.action)
        assertEquals("geo", poiIntent.data?.scheme)
        assertEquals("geo:50.98,5.05", poiIntent.data.toString())

        val queryIntent = NavigationIntents.forQuery("laadpaal in de buurt")
        assertEquals(CarContext.ACTION_NAVIGATE, queryIntent.action)
        assertEquals("laadpaal in de buurt", queryIntent.data?.getQueryParameter("q"))
        assertNotNull(queryIntent.data)
    }
}
