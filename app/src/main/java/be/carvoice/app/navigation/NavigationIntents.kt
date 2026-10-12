package be.carvoice.app.navigation

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext

/** Builds the documented Android for Cars navigation hand-off intents. */
object NavigationIntents {
    /** Starts navigation to a resolved destination through CarContext.startCarApp. */
    fun forPoi(poi: Poi): Intent {
        require(poi.hasValidCoordinates()) { "POI coordinates are outside valid ranges." }
        val uri = Uri.parse("geo:${poi.latitude},${poi.longitude}")
        return Intent(CarContext.ACTION_NAVIGATE, uri)
    }

    /** Asks the active navigation host to resolve a free-form place or nearby-POI query. */
    fun forQuery(query: String): Intent {
        require(query.isNotBlank()) { "A navigation query is required." }
        return Intent(CarContext.ACTION_NAVIGATE, Uri.parse("geo:0,0?q=${Uri.encode(query.trim())}"))
    }
}
