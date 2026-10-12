package be.carvoice.app.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.AppManager
import androidx.car.app.OnDoneCallback
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.testing.ScreenController
import androidx.car.app.testing.TestAppManager
import androidx.car.app.testing.TestCarContext
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import be.carvoice.app.navigation.Poi
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Car App Library host fakes verify templates and dispatched intents, not real vehicle routing. */
@RunWith(AndroidJUnit4::class)
class CarTemplatesTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test fun manifestRegistersAnExportedPoiService() {
        val context = instrumentation.targetContext
        val intent = Intent(CarAppService.SERVICE_INTERFACE)
            .addCategory(CarAppService.CATEGORY_POI_APP).setPackage(context.packageName)
        val services = context.packageManager.queryIntentServices(intent, 0)
        val service = services.single { it.serviceInfo.name == CarVoiceService::class.java.name }.serviceInfo
        assertTrue(service.exported)
        assertEquals(context.packageName, service.packageName)
    }

    @Test fun enteringPoiSearchShowsRealActionsWithoutOpeningMicrophone() {
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val screen = PoiSearchScreen(context)
            val controller = ScreenController(screen).moveToState(Lifecycle.State.CREATED)
            try {
                val template = screen.onGetTemplate() as SearchTemplate
                assertFalse(template.isShowKeyboardByDefault)
                assertEquals("Bestemming, adres of plaats", template.searchHint)
                assertEquals("Spreek opdracht", template.actionStrip!!.actions.single().title!!.toString())
                assertTrue(template.itemList!!.items.any { (it as Row).title.toString() == "Laadpalen in de buurt" })
                assertNull((context.getCarService(AppManager::class.java) as TestAppManager).openMicrophoneRequest)
                assertTrue(context.startCarAppIntents.isEmpty())
            } finally {
                controller.moveToState(Lifecycle.State.DESTROYED)
            }
        }
    }

    @Test fun confirmingResolvedPoiDispatchesItsCoordinatesToCarNavigation() {
        instrumentation.runOnMainSync {
            val context = TestCarContext.createCarContext(instrumentation.targetContext)
            val poi = Poi("test-station", "Station Diest", "Stationsstraat 10, Diest", 50.98, 5.05)
            val screen = PoiDetailsScreen(context, poi)
            val controller = ScreenController(screen).moveToState(Lifecycle.State.CREATED)
            try {
                val template = screen.onGetTemplate() as PaneTemplate
                assertEquals(poi.name, template.pane.rows.single().title.toString())
                template.pane.actions.first().onClickDelegate!!.sendClick(object : OnDoneCallback { })
                val request = context.startCarAppIntents.single()
                assertEquals(CarContext.ACTION_NAVIGATE, request.action)
                assertEquals("geo:50.98,5.05", request.data.toString())
                assertNull(request.component)
            } finally {
                controller.moveToState(Lifecycle.State.DESTROYED)
            }
        }
    }
}
