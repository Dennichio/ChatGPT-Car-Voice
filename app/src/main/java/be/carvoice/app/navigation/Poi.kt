package be.carvoice.app.navigation

/** A destination returned by Android's platform Geocoder or stored by the user. */
data class Poi(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

internal fun Poi.hasValidCoordinates(): Boolean =
    latitude.isFinite() && longitude.isFinite() &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0

enum class FavoriteId(val value: String) {
    HOME("home"),
    WORK("work"),
}
