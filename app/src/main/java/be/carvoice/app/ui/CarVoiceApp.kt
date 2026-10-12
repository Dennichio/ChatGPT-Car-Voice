package be.carvoice.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.carvoice.app.model.LaunchAvailability
import be.carvoice.app.model.LauncherState

private val Night = Color(0xFF071521)
private val Panel = Color(0xFF102535)
private val Teal = Color(0xFF43D9C3)
private val Muted = Color(0xFFADC1CC)

@Composable
fun CarVoiceApp(
    state: LauncherState,
    onLaunch: () -> Unit,
    onRefresh: () -> Unit,
    onInstall: () -> Unit,
    setupContent: @Composable () -> Unit = {},
) {
    val ready = state.availability == LaunchAvailability.READY
    val availabilityText = when (state.availability) {
        LaunchAvailability.READY -> "ChatGPT is geïnstalleerd"
        LaunchAvailability.NOT_INSTALLED -> "ChatGPT is niet geïnstalleerd"
        LaunchAvailability.DISABLED -> "ChatGPT is uitgeschakeld"
        LaunchAvailability.NO_LAUNCH_ACTIVITY -> "ChatGPT kan niet worden geopend"
    }
    val availabilityDetail = when (state.availability) {
        LaunchAvailability.READY -> state.chatGptVersion?.let { "Versie $it" } ?: "Klaar om te openen"
        LaunchAvailability.NOT_INSTALLED -> "Installeer de officiële ChatGPT-app om verder te gaan."
        LaunchAvailability.DISABLED -> "Schakel ChatGPT in via de Android-appinstellingen."
        LaunchAvailability.NO_LAUNCH_ACTIVITY -> "Er is geen startscherm beschikbaar voor ChatGPT. Werk de app bij of installeer haar opnieuw."
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Teal,
        onPrimary = Night,
        secondary = Teal,
        background = Night,
        surface = Panel,
        onBackground = Color.White,
        onSurface = Color.White,
        outline = Muted,
        error = Color(0xFFFF8C82),
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = Night) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Text("CARVOICE", color = Teal, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("ChatGPT voor onderweg", fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(12.dp).background(if (ready) Teal else Muted, CircleShape),
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(availabilityText, modifier = Modifier.weight(1f), fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(availabilityDetail, color = Muted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp))
                        state.status?.takeIf(String::isNotBlank)?.let {
                            Text(it, color = Muted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onLaunch,
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                        .semantics { contentDescription = "Open ChatGPT" },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Night),
                ) {
                    Text("Open ChatGPT", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                if (state.availability == LaunchAvailability.NOT_INSTALLED) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                    ) { Text("Installeer officiële app", fontSize = 16.sp) }
                }

                Spacer(Modifier.height(18.dp))
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("Vernieuw status", fontSize = 16.sp) }

                Spacer(Modifier.height(24.dp))
                InfoCard(title = "Auto", value = state.carConnection)
                if (state.audioOutputs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    InfoCard(title = "Beschikbare audio-uitgangen", value = state.audioOutputs.joinToString("\n"))
                }

                Spacer(Modifier.height(20.dp))
                setupContent()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Spraak starten", color = Teal, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "In ChatGPT: open een nieuw, leeg gesprek om Spraak automatisch te starten op ondersteunde telefoons. " +
                                "Zet dit aan via Instellingen › Spraak › Starten met Voice (Start with Voice), als deze optie beschikbaar is in jouw geïnstalleerde app. " +
                                "Bij een bestaand gesprek kan ChatGPT het gesprek hervatten; tik dan op het spraakpictogram.",
                            color = Muted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            "Achtergrondgesprekken stel je apart in bij Instellingen › Spraak. CarVoice start geen spraaksessie.",
                            color = Muted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "CarVoice opent de officiële ChatGPT-app. Android Auto-opdrachten gebruiken apart de automicrofoon na je tik, met lokale spraakherkenning. ChatGPT beheert zijn eigen gesprek en account.",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Teal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Muted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
