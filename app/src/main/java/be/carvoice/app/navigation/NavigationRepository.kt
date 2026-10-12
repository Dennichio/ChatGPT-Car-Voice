package be.carvoice.app.navigation

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeocoderUnavailableException : Exception("Adreszoeken is niet beschikbaar op dit toestel.")

class GeocoderQueryException(cause: Throwable? = null) :
    Exception("Adreszoeken is mislukt. Probeer een andere zoekopdracht.", cause)

/**
 * Address lookup backed by Android's best-effort, device-provided Geocoder. Results are capped
 * at five. Use [NavigationIntents.forQuery] for nearby category searches: platform geocoding is
 * not a reliable commercial nearby-POI search service.
 */
class NavigationRepository internal constructor(
    geocoderAccess: GeocoderAccess,
) {
    private val geocoder = geocoderAccess
    constructor(context: Context) : this(PlatformGeocoderAccess(context.applicationContext))

    suspend fun search(query: String): List<Poi> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return emptyList()
        return withTimeout(15_000) { withContext(Dispatchers.IO) {
            try {
                geocoder.search(normalizedQuery, MAX_RESULTS)
                    .asSequence()
                    .filter(Poi::hasValidCoordinates)
                    .take(MAX_RESULTS)
                    .toList()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (known: GeocoderUnavailableException) {
                throw known
            } catch (known: GeocoderQueryException) {
                throw known
            } catch (failure: Exception) {
                throw GeocoderQueryException(failure)
            }
        } }
    }

    companion object {
        const val MAX_RESULTS = 5
    }
}

internal fun interface GeocoderAccess {
    suspend fun search(query: String, maxResults: Int): List<Poi>
}

private class PlatformGeocoderAccess(context: Context) : GeocoderAccess {
    private val locale = Locale.forLanguageTag("nl-BE")
    private val geocoder = Geocoder(context, locale)

    override suspend fun search(query: String, maxResults: Int): List<Poi> {
        if (!Geocoder.isPresent()) throw GeocoderUnavailableException()
        val addresses = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocodeAsync(query, maxResults)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, maxResults).orEmpty()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (unavailable: GeocoderUnavailableException) {
            throw unavailable
        } catch (failure: IOException) {
            throw GeocoderQueryException(failure)
        } catch (failure: Exception) {
            throw GeocoderQueryException(failure)
        }
        return addresses.mapNotNull { it.toPoi(query) }
    }

    @android.annotation.SuppressLint("NewApi")
    private suspend fun geocodeAsync(query: String, maxResults: Int): List<Address> =
        suspendCancellableCoroutine { continuation ->
            try {
                geocoder.getFromLocationName(query, maxResults, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) continuation.resume(addresses)
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) continuation.resumeWithException(GeocoderQueryException())
                    }
                })
            } catch (failure: Exception) {
                if (continuation.isActive) continuation.resumeWithException(GeocoderQueryException(failure))
            }
        }

    private fun Address.toPoi(query: String): Poi? {
        if (!hasLatitude() || !hasLongitude()) return null
        val lat = latitude
        val lon = longitude
        if (!lat.isFinite() || !lon.isFinite() || lat !in -90.0..90.0 || lon !in -180.0..180.0) return null

        val line = getAddressLine(0)?.trim().orEmpty()
        val feature = featureName?.trim().orEmpty()
        val label = feature.ifBlank { line.substringBefore(',').ifBlank { return null } }
        val fullAddress = line.ifBlank { label }
        val stableId = UUID.nameUUIDFromBytes("$label|$fullAddress|$lat|$lon".toByteArray()).toString()
        return Poi(stableId, label.ifBlank { query }, fullAddress, lat, lon)
    }
}
