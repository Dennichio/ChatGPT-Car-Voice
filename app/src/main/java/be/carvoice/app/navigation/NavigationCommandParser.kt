package be.carvoice.app.navigation

/** Commands recognized in Dutch speech text. Nearby queries are handed to a navigation host. */
sealed interface NavigationCommand {
    data class Navigate(val query: String) : NavigationCommand
    data class GoToFavorite(val id: String) : NavigationCommand
    data class SearchNearby(val query: String) : NavigationCommand
}

object NavigationCommandParser {
    /** Returns null for text that is not one of the supported navigation commands. */
    fun parse(input: String): NavigationCommand? {
        val text = input.trim().trimEnd('.', ',', '!', '?').trim()
        if (text.isEmpty()) return null
        val normalized = text.lowercase()

        favoriteId(normalized)?.let { return NavigationCommand.GoToFavorite(it.value) }

        val navigatePrefix = "navigeer naar "
        if (normalized.startsWith(navigatePrefix)) {
            val query = text.drop(navigatePrefix.length).trim()
            if (query.isNotEmpty()) return NavigationCommand.Navigate(query)
        }

        val bringPrefix = "breng me naar "
        if (normalized.startsWith(bringPrefix)) {
            val destination = normalized.removePrefix(bringPrefix).trim()
            favoriteId(destination)?.let { return NavigationCommand.GoToFavorite(it.value) }
            val query = text.drop(bringPrefix.length).trim()
            if (query.isNotEmpty()) return NavigationCommand.Navigate(query)
        }

        val searchPrefix = "zoek "
        val nearbySuffix = " in de buurt"
        if (normalized.startsWith(searchPrefix) && normalized.endsWith(nearbySuffix)) {
            val query = text.substring(searchPrefix.length, text.length - nearbySuffix.length)
                .trim()
                .removeArticle()
            if (query.isNotEmpty()) return NavigationCommand.SearchNearby(query)
        }
        return null
    }

    private fun favoriteId(destination: String): FavoriteId? = when (
        destination.trim().removePrefix("mijn ").removePrefix("naar ").trim()
    ) {
        "thuis", "huis" -> FavoriteId.HOME
        "werk" -> FavoriteId.WORK
        else -> null
    }

    private fun String.removeArticle(): String = when {
        startsWith("een ", ignoreCase = true) -> drop(4).trim()
        startsWith("de ", ignoreCase = true) -> drop(3).trim()
        startsWith("het ", ignoreCase = true) -> drop(4).trim()
        else -> this
    }
}
