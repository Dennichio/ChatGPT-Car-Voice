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
    setupContent: @Composable () -> Unit = {},
) {
    val ready = true
    val availabilityText = "Android-standaardassistent"
    val availabilityDetail = "Het startverzoek gebruikt de standaardassistent die in Android is ingesteld."

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
                    Text("Spraak en bestemmingen onderweg", fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
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
                        .semantics { contentDescription = "Start standaardassistent" },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = Night),
                ) {
                    Text("Start standaardassistent", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        Text("Standaardassistent starten", color = Teal, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "De app gebruikt Android ACTION_ASSIST, zonder een vaste ChatGPT-app te openen. Android Auto kan deze actie blokkeren.",
                            color = Muted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            "Een verzonden verzoek bewijst niet dat de assistent luistert. Sommige assistenten openen een venster op het telefoonscherm.",
                            color = Muted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "De zelfstandige navigatie- en muziekopdrachten gebruiken de automicrofoon via Android Auto. Een systeemassistentverzoek werkt apart en wordt door Android afgehandeld.",
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
