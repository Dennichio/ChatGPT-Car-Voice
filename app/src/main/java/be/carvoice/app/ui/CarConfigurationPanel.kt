package be.carvoice.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.carvoice.app.navigation.FavoriteStore
import be.carvoice.app.navigation.NavigationRepository
import be.carvoice.app.navigation.Poi
import be.carvoice.app.voice.SpeechModelStore
import be.carvoice.app.spotify.SpotifyStatus
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One-time parked setup; normal destination selection and microphone controls are on the car screen. */
@Composable
fun CarConfigurationPanel(
    context: Context,
    microphoneGranted: Boolean,
    onGrantMicrophone: () -> Unit,
    spotifyStatus: SpotifyStatus,
    onGrantSpotify: () -> Unit,
    onRefreshSpotify: () -> Unit,
) {
    val store = remember { FavoriteStore(context.applicationContext) }
    val repository = remember { NavigationRepository(context.applicationContext) }
    val model = remember { SpeechModelStore(context.applicationContext) }
    val modelState by model.status.collectAsState()
    val scope = rememberCoroutineScope()
    var slot by remember { mutableStateOf("home") }
    var label by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf(emptyList<Poi>()) }
    var saved by remember { mutableStateOf(runCatching { store.favorites() }.getOrDefault(emptyList())) }
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text("Eenmalige Android Auto-configuratie", style = MaterialTheme.typography.titleMedium)
            Text("Stel dit geparkeerd in. Gebruik op het autoscherm bestemmingsspraak en beschikbare Spotify-opdrachten. Android bepaalt welke standaardassistent een afzonderlijk startverzoek ontvangt.")
            Spacer(Modifier.height(12.dp))
            Text(if (microphoneGranted) "Microfoontoestemming: verleend" else "Microfoontoestemming: ontbreekt")
            if (!microphoneGranted) OutlinedButton(onClick = onGrantMicrophone) { Text("Microfoon toestaan") }
            Text(when {
                modelState.installing -> "Spraakmodel downloaden: ${modelState.progressPercent?.let { "$it%" } ?: "bezig"}"
                modelState.ready -> "Nederlands offline spraakmodel beschikbaar"
                else -> "Offline spraakmodel nog niet geïnstalleerd"
            })
            modelState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = !modelState.installing && !modelState.ready, onClick = {
                scope.launch { model.install() }
            }) { Text("Download Nederlands spraakmodel") }
            Text("Eenmalige download vanaf de officiële Vosk-modelbron. Daarna wordt bestemmingsspraak lokaal verwerkt. Deze app begint pas te luisteren na je microfoonactie in Android Auto.")

            Spacer(Modifier.height(16.dp))
            Text("Thuis, werk en favoriete plaatsen", style = MaterialTheme.typography.titleMedium)
            Row {
                listOf("home" to "Thuis", "work" to "Werk", "custom" to "Andere").forEach { (id, text) ->
                    TextButton(enabled = !busy, onClick = { slot = id; matches = emptyList() }) {
                        Text(if (slot == id) "✓ $text" else text)
                    }
                }
            }
            if (slot == "custom") OutlinedTextField(value = label, onValueChange = { label = it.take(80) }, label = { Text("Naam") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it.take(250); matches = emptyList() }, label = { Text("Volledig adres of plaats") }, modifier = Modifier.fillMaxWidth())
            Button(enabled = !busy && address.isNotBlank(), onClick = {
                busy = true
                status = null
                scope.launch {
                    try {
                        matches = repository.search(address)
                        status = if (matches.isEmpty()) "Geen adres gevonden. Er is niets opgeslagen." else "Kies het juiste adres om te bewaren."
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { status = "Adreszoeken niet beschikbaar. Probeer later opnieuw." }
                    finally { busy = false }
                }
            }) { Text(if (busy) "Bezig…" else "Adres zoeken") }
            matches.forEach { poi ->
                OutlinedButton(enabled = !busy, onClick = {
                    busy = true
                    val name = when (slot) { "home" -> "Thuis"; "work" -> "Werk"; else -> label.trim().ifBlank { poi.name } }
                    val id = if (slot == "custom") "favorite-${UUID.randomUUID()}" else slot
                    scope.launch {
                        try {
                            saved = withContext(Dispatchers.IO) { store.upsert(poi.copy(id = id, name = name)); store.favorites() }
                            matches = emptyList()
                            status = "$name opgeslagen."
                        } catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { status = "Opslaan is mislukt." }
                        finally { busy = false }
                    }
                }) { Text("Bewaar: ${poi.address}") }
            }
            status?.let { Text(it) }
            saved.forEach { poi ->
                Text("${poi.name}: ${poi.address}")
                TextButton(enabled = !busy, onClick = {
                    busy = true
                    scope.launch {
                        try { saved = withContext(Dispatchers.IO) { store.delete(poi.id); store.favorites() } }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { status = "Verwijderen is mislukt." }
                        finally { busy = false }
                    }
                }) { Text("Verwijder ${poi.name}") }
            }
            Spacer(Modifier.height(16.dp))
            Text("Spotify-mediabediening", style = MaterialTheme.typography.titleMedium)
            Text(spotifyStatus.problem ?: spotifyStatus.track ?: "Actieve Spotify-mediasessie gevonden.")
            Text("Start eenmalig muziek in Spotify. Android vraagt notificatietoegang voor mediasessies. Die toestemming biedt toegang tot meldingen; deze implementatie leest geen meldinginhoud en gebruikt alleen de actieve Spotify-mediasessie.")
            if (!spotifyStatus.accessGranted) OutlinedButton(onClick = onGrantSpotify) { Text("Spotify-mediatoegang instellen") }
            OutlinedButton(onClick = onRefreshSpotify) { Text("Controleer Spotify-sessie") }
            Text("Pauzeren, hervatten, volgend/vorig en de titel opvragen werken alleen wanneer Spotify een actieve sessie met de vereiste mogelijkheden aanbiedt. Zoeken op artiest- of playlistnaam is niet beschikbaar.")
            Spacer(Modifier.height(16.dp))
            Text("Standaard Android-spraakassistent", style = MaterialTheme.typography.titleMedium)
            Text("De knop op het Android Auto-scherm vraagt Android om de ingestelde standaardassistent te starten. De app kiest geen ChatGPT-component. Android Auto kan dit blokkeren; de assistent kan zelf een telefoonscherm tonen. Volledig schermloos gebruik is niet gegarandeerd.")
        }
    }
}
