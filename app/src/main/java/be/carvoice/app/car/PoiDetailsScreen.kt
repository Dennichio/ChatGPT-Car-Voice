package be.carvoice.app.car

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import be.carvoice.app.navigation.FavoriteStore
import be.carvoice.app.navigation.NavigationIntents
import be.carvoice.app.navigation.Poi

/** Explicit confirmation avoids choosing the wrong destination when names are ambiguous. */
internal class PoiDetailsScreen(carContext: CarContext, private val poi: Poi) : Screen(carContext) {
    private val speech = CarSpeechFeedback(carContext)
    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { speech.stop() }
            override fun onDestroy(owner: LifecycleOwner) { speech.close() }
        })
    }
    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(Row.Builder().setTitle(poi.name).addText(poi.address).build())
            .addAction(Action.Builder().setTitle("Navigeer").setOnClickListener {
                speech.speakThen("Navigatieverzoek voor ${poi.name.take(120)}") {
                    try {
                        carContext.startCarApp(NavigationIntents.forPoi(poi))
                    } catch (_: Exception) {
                        CarToast.makeText(carContext, "Navigatie kon niet starten", CarToast.LENGTH_LONG).show()
                    }
                }
            }.build())
            .addAction(Action.Builder().setTitle("Bewaar favoriet").setOnClickListener {
                try {
                    FavoriteStore(carContext).upsert(poi)
                    CarToast.makeText(carContext, "Bestemming bewaard", CarToast.LENGTH_SHORT).show()
                    speech.speakThen("Bestemming bewaard") { }
                } catch (_: Exception) {
                    CarToast.makeText(carContext, "Bestemming kon niet worden bewaard", CarToast.LENGTH_LONG).show()
                }
            }.build())
            .build()
        return PaneTemplate.Builder(pane).setTitle("Bestemming kiezen").setHeaderAction(Action.BACK).build()
    }
}
