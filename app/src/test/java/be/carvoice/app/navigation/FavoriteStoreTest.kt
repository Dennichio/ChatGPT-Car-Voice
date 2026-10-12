package be.carvoice.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteStoreTest {
    @Test
    fun upsertListGetAndDeleteManageNamedFavoriteSlots() {
        val preferences = MemoryPreferences()
        val codec = MemoryCodec()
        val store = FavoriteStore(preferences, codec)
        val home = Poi(FavoriteId.HOME.value, "Thuis", "Straat 1, Diest", 50.98, 5.05)
        val work = Poi(FavoriteId.WORK.value, "Werk", "Stationsstraat 10, Diest", 50.99, 5.06)

        store.upsert(home)
        store.upsert(work)
        store.upsert(home.copy(name = "Thuis", address = "Straat 2, Diest"))

        assertEquals(2, store.favorites().size)
        assertEquals("Straat 2, Diest", store.get(FavoriteId.HOME.value)?.address)
        assertEquals("Werk", store.get(FavoriteId.WORK.value)?.name)
        assertTrue(store.delete(FavoriteId.HOME.value))
        assertNull(store.get(FavoriteId.HOME.value))
        assertFalse(store.delete(FavoriteId.HOME.value))
    }

    @Test
    fun rejectsInvalidCoordinatesAndMissingDisplayFields() {
        val store = FavoriteStore(MemoryPreferences(), MemoryCodec())
        val invalidCoordinates = Poi("home", "Thuis", "Adres", 90.1, 4.0)
        val missingName = Poi("home", " ", "Adres", 50.0, 4.0)

        assertThrows<IllegalArgumentException> { store.upsert(invalidCoordinates) }
        assertThrows<IllegalArgumentException> { store.upsert(missingName) }
        assertTrue(store.favorites().isEmpty())
    }

    private class MemoryPreferences : FavoritePreferences {
        private val values = mutableMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String) { values[key] = value }
    }

    private class MemoryCodec : FavoriteCodec {
        private var values = emptyList<Poi>()
        override fun encode(favorites: List<Poi>): String {
            values = favorites.toList()
            return "memory"
        }
        override fun decode(value: String): List<Poi> = if (value == "memory") values.toList() else emptyList()
    }

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected ${T::class.java.simpleName}")
        } catch (error: Throwable) {
            if (error !is T) throw error
        }
    }
}
