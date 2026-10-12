package be.carvoice.app.navigation

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Stores explicit user-saved places on-device. The store contains no credentials or API keys. */
class FavoriteStore internal constructor(
    private val preferences: FavoritePreferences,
    private val codec: FavoriteCodec,
) {
    constructor(context: Context) : this(
        SharedPreferencesFavoritePreferences(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        ),
        JsonFavoriteCodec,
    )

    @Synchronized
    fun favorites(): List<Poi> = preferences.read(KEY_FAVORITES)?.let(codec::decode).orEmpty()

    @Synchronized
    fun get(id: String): Poi? = favorites().firstOrNull { it.id == id }

    /** Adds or replaces a place by id; use `home` and `work` for the dedicated saved slots. */
    @Synchronized
    fun upsert(poi: Poi) {
        require(poi.id.isNotBlank()) { "A favorite id is required." }
        require(poi.name.isNotBlank()) { "A favorite name is required." }
        require(poi.address.isNotBlank()) { "A favorite address is required." }
        require(poi.hasValidCoordinates()) { "Favorite coordinates are outside valid ranges." }
        val updated = favorites().filterNot { it.id == poi.id } + poi
        preferences.write(KEY_FAVORITES, codec.encode(updated))
    }

    @Synchronized
    fun delete(id: String): Boolean {
        val existing = favorites()
        val updated = existing.filterNot { it.id == id }
        if (updated.size == existing.size) return false
        preferences.write(KEY_FAVORITES, codec.encode(updated))
        return true
    }

    companion object {
        private const val PREFERENCES_NAME = "navigation_favorites"
        private const val KEY_FAVORITES = "favorites_json"
    }
}

internal interface FavoritePreferences {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

internal interface FavoriteCodec {
    fun encode(favorites: List<Poi>): String
    fun decode(value: String): List<Poi>
}

private class SharedPreferencesFavoritePreferences(
    private val preferences: SharedPreferences,
) : FavoritePreferences {
    override fun read(key: String): String? = preferences.getString(key, null)
    override fun write(key: String, value: String) {
        check(preferences.edit().putString(key, value).commit()) { "Could not save favorite destinations." }
    }
}

private object JsonFavoriteCodec : FavoriteCodec {
    override fun encode(favorites: List<Poi>): String = JSONArray().apply {
        favorites.forEach { poi ->
            put(JSONObject().apply {
                put("id", poi.id)
                put("name", poi.name)
                put("address", poi.address)
                put("latitude", poi.latitude)
                put("longitude", poi.longitude)
            })
        }
    }.toString()

    override fun decode(value: String): List<Poi> = try {
        val array = JSONArray(value)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val poi = Poi(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    address = item.optString("address"),
                    latitude = item.optDouble("latitude", Double.NaN),
                    longitude = item.optDouble("longitude", Double.NaN),
                )
                if (poi.id.isNotBlank() && poi.name.isNotBlank() && poi.address.isNotBlank() && poi.hasValidCoordinates()) {
                    add(poi)
                }
            }
        }
    } catch (malformed: JSONException) {
        throw IllegalStateException("Saved favorite destinations are invalid.", malformed)
    }
}
