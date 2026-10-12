/*
 * Copyright (C) 2021 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * Modified from Android Car Samples SearchScreen/SearchTemplateDemoScreen:
 * real geocoding, explicit result selection, favorites and supported navigation handoff.
 */
package be.carvoice.app.car

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import be.carvoice.app.navigation.FavoriteStore
import be.carvoice.app.navigation.NavigationIntents
import be.carvoice.app.navigation.NavigationCommand
import be.carvoice.app.navigation.NavigationRepository
import be.carvoice.app.navigation.Poi
import be.carvoice.app.voice.CarVoiceController
import be.carvoice.app.integration.DefaultAssistantLauncher
import be.carvoice.app.integration.AssistantLaunchResult
import be.carvoice.app.spotify.SpotifyController
import be.carvoice.app.spotify.SpotifyResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PoiSearchScreen(carContext: CarContext) : Screen(carContext) {
    private val repository = NavigationRepository(carContext)
    private val favorites = FavoriteStore(carContext)
    private val spotify = SpotifyController(carContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var query = ""
    private var results = emptyList<Poi>()
    private var error: String? = null
    private var loading = false
    private var searchJob: Job? = null
    private var generation = 0L
    private var listening = false
    private var voiceStatus: String? = null
    private val speech = CarSpeechFeedback(carContext)
    private val voice = CarVoiceController(carContext,
        onListening = { active ->
            listening = active
            if (active) voiceStatus = "Luisteren: spreek een navigatie- of muziekopdracht"
            invalidate()
        },
        onResult = { text ->
            voiceStatus = null
            handleSpokenCommand(text)
            invalidate()
        },
        onError = { message -> showVoiceError(message) })

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { voice.stop(); speech.stop() }
            override fun onDestroy(owner: LifecycleOwner) {
                ++generation
                voice.close()
                speech.close()
                scope.cancel()
            }
        })
    }

    override fun onGetTemplate(): Template {
        val builder = SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) { search(searchText, debounce = true) }
            override fun onSearchSubmitted(searchText: String) { search(searchText, debounce = false) }
        }).setHeaderAction(Action.APP_ICON)
            .setSearchHint("Bestemming, adres of plaats")
            .setInitialSearchText(query)
            .setShowKeyboardByDefault(false)
            .setLoading(loading)
            .setActionStrip(ActionStrip.Builder().addAction(Action.Builder()
                .setTitle(if (listening) "Stop microfoon" else "Spreek opdracht")
                .setOnClickListener {
                    if (listening) {
                        voice.stop()
                        voiceStatus = null
                    } else {
                        searchJob?.cancel()
                        ++generation
                        loading = false
                        voice.start()
                    }
                    invalidate()
                }.build()).build())

        if (!loading) {
            val list = ItemList.Builder().setNoItemsMessage(error ?: if (query.isBlank()) "Zoek een bestemming" else "Geen resultaten; probeer een preciezer adres")
            if (voiceStatus != null) {
                list.addItem(Row.Builder().setTitle(voiceStatus!!)
                    .addText(if (listening) "Bijvoorbeeld: navigeer naar Leuven" else "Zoeken blijft beschikbaar via het zoekveld")
                    .build())
            } else if (query.isBlank()) {
                val saved = try { favorites.favorites() } catch (_: Exception) { emptyList() }
                saved.take(4).forEach { poi -> list.addItem(poiRow(poi)) }
                list.addItem(Row.Builder().setTitle("Laadpalen in de buurt")
                    .addText("Zoeken in je navigatie-app")
                    .setOnClickListener { handoffQuery("laadpalen in de buurt") }.build())
                list.addItem(Row.Builder().setTitle("Supermarkten in de buurt")
                    .addText("Zoeken in je navigatie-app")
                    .setOnClickListener { handoffQuery("supermarkten in de buurt") }.build())
                list.addItem(Row.Builder().setTitle("Standaard-spraakassistent")
                    .addText("Android-systeemverzoek; kan door Android Auto worden geweigerd")
                    .setOnClickListener { requestDefaultAssistant() }.build())
            } else {
                results.take(5).forEach { poi -> list.addItem(poiRow(poi)) }
                // A navigation app can resolve POI queries that Android Geocoder cannot.
                list.addItem(Row.Builder().setTitle("Zoek in navigatie-app")
                    .addText(query.take(120))
                    .addText(error ?: "De navigatie-app kiest of toont de bestemmingen")
                    .setOnClickListener { handoffQuery(query) }.build())
            }
            builder.setItemList(list.build())
        }
        return builder.build()
    }

    private fun poiRow(poi: Poi): Row = Row.Builder().setTitle(poi.name)
        .addText(poi.address)
        .setOnClickListener { screenManager.push(PoiDetailsScreen(carContext, poi)) }.build()

    private fun search(text: String, debounce: Boolean) {
        voice.stop()
        voiceStatus = null
        query = text.trim().take(250)
        val turn = ++generation
        searchJob?.cancel()
        error = null
        results = emptyList()
        if (query.isBlank()) {
            loading = false
            invalidate()
            return
        }
        loading = true
        invalidate()
        val target = query
        searchJob = scope.launch {
            try {
                if (debounce) delay(550)
                val found = withContext(Dispatchers.IO) { repository.search(target) }
                if (turn != generation) return@launch
                results = found
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (turn != generation) return@launch
                error = "Adreszoeken niet beschikbaar; zoek in je navigatie-app"
                CarToast.makeText(carContext, error!!, CarToast.LENGTH_LONG).show()
            } finally {
                if (turn == generation) {
                    loading = false
                    invalidate()
                }
            }
        }
    }

    private fun handoffQuery(query: String) {
        voice.stop()
        speech.speakThen("Navigatieverzoek voor ${query.take(120)}") {
            try {
                carContext.startCarApp(NavigationIntents.forQuery(query))
            } catch (_: Exception) {
                showVoiceError("Geen geschikte navigatie-app beschikbaar")
            }
        }
    }

    private fun handoffPoi(poi: Poi) {
        voice.stop()
        speech.speakThen("Navigatieverzoek voor ${poi.name.take(120)}") {
            try {
                carContext.startCarApp(NavigationIntents.forPoi(poi))
            } catch (_: Exception) {
                showVoiceError("Geen geschikte navigatie-app beschikbaar")
            }
        }
    }

    private fun showVoiceError(message: String) {
        voiceStatus = message
        CarToast.makeText(carContext, message, CarToast.LENGTH_LONG).show()
        speech.speakThen(message) { }
        invalidate()
    }

    private fun handleSpokenCommand(text: String) {
        when (val parsed = SpokenCommandRouter.route(text)) {
            is SpokenCommand.Navigation -> when (val command = parsed.command) {
                is NavigationCommand.Navigate -> handoffQuery(command.query)
                is NavigationCommand.SearchNearby -> handoffQuery("${command.query} in de buurt")
                is NavigationCommand.GoToFavorite -> {
                    val poi = try { favorites.get(command.id) } catch (_: Exception) { null }
                    if (poi == null) showVoiceError("Bewaar Thuis of Werk eerst bij de eenmalige instellingen")
                    else handoffPoi(poi)
                }
            }
            is SpokenCommand.Music -> {
                val message = when (val result = spotify.execute(parsed.command)) {
                    is SpotifyResult.Dispatched -> "Bedieningsverzoek aan Spotify verzonden"
                    is SpotifyResult.Unavailable -> result.reason
                    is SpotifyResult.Unsupported -> result.reason
                }
                voiceStatus = message
                CarToast.makeText(carContext, message, CarToast.LENGTH_LONG).show()
                speech.speakThen(message) { }
            }
            SpokenCommand.MusicStatus -> {
                val status = spotify.status()
                val message = status.problem ?: status.track?.let { "Spotify meldt: $it" } ?: "Spotify geeft geen nummerinformatie"
                voiceStatus = message
                CarToast.makeText(carContext, message, CarToast.LENGTH_LONG).show()
                speech.speakThen(message) { }
            }
            is SpokenCommand.Unsupported -> showVoiceError(parsed.reason)
        }
    }

    private fun requestDefaultAssistant() {
        voice.stop()
        speech.stop()
        val message = when (DefaultAssistantLauncher(carContext).launch()) {
            AssistantLaunchResult.REQUEST_SENT ->
                "Verzoek verzonden. Android bepaalt of de standaardassistent start."
            AssistantLaunchResult.NO_HANDLER ->
                "Geen beschikbare Android-spraakassistent gevonden."
            AssistantLaunchResult.BLOCKED ->
                "Android Auto of Android blokkeert het assistentverzoek."
        }
        CarToast.makeText(carContext, message, CarToast.LENGTH_LONG).show()
    }
}
