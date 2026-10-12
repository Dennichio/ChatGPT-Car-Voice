# Installatie en eerste telefoontest

Deze handleiding beschrijft de bestaande telefoon-APK. Ze is geen Android Auto-app en
verschijnt niet in **Launcher aanpassen**. Bediening vanuit Android Auto is nu verplicht;
smartphoneverbeteringen zijn als fallback geschrapt. Zie
[de haalbaarheidsconclusie](ANDROID_AUTO_FEASIBILITY.md).

## APK installeren

1. [Download de APK via GitHub](https://github.com/Dennichio/ChatGPT-Car-Voice/raw/refs/heads/main/downloads/chatgpt-car-voice-milestone1-debug.apk)
   op je Android-telefoon (Android 8+). Op de GitHub-bestandspagina kun je ook
   **Download raw file** gebruiken. De controlesom staat in [`SHA256SUMS`](../downloads/SHA256SUMS).
2. Open het bestand en geef, als Android daarom vraagt, die bestandsapp toestemming
   om deze APK te installeren. De APK is lokaal ondertekend als ontwikkelbuild;
   er is geen Play Store-publicatie of release-signering uitgevoerd.
3. Open **ChatGPT Car Voice**. De launcher vraagt geen microfoon- of internettoestemming.
4. Controleer of de officiële **ChatGPT**-app geïnstalleerd en ingeschakeld is in hetzelfde
   Android-profiel. De app toont de gevonden versie. Als ze ontbreekt, opent
   **Installeer officiële app** de officiële Play Store-pagina.
5. Tik op **Open ChatGPT**. Verwacht dat Android de officiële app opent. Dit is een
   startopdracht, geen bevestiging dat er al een spraakgesprek gestart is.

Met ADB:

```bash
adb install -r downloads/chatgpt-car-voice-milestone1-debug.apk
adb shell am start -n be.carvoice.app/.MainActivity
```

## Voice automatisch starten

Meld je uitsluitend in de officiële ChatGPT-app aan. Open daar **Settings → Voice** en
activeer **Start with Voice**, als beschikbaar. De Nederlandse naam kan **Starten met Voice**
zijn. Maak daar een **nieuw/leeg gesprek**. Keer terug naar CarVoice en tik op **Open ChatGPT**.
Noteer of Voice opent. Herhaal met een bestaand gesprek: hervatten daarvan is mogelijk en
niet automatisch Voice. Gebruik dan het officiële Voice-pictogram.

De launcher kan deze instelling niet lezen, wijzigen, een nieuw gesprek afdwingen of
controleren of de microfoon actief is. Er is geen gedocumenteerde Voice-deeplink gebruikt.

## Achtergrondgesprekken

Activeer **Background conversations / Achtergrondgesprekken** in ChatGPT als je een
reeds gestart gesprek wilt voortzetten terwijl een andere app open is of het scherm
vergrendeld is. Test beide situaties expliciet. Het gesprek eindigt wanneer je het stopt,
ChatGPT geforceerd sluit, een gebruikslimiet bereikt of de maximale sessieduur bereikt.
Mute/stop via de bediening in **ChatGPT**, niet via CarVoice.

## Android Auto en BYD

Test eerst geparkeerd en volg [de voertuigchecklist](VEHICLE_TEST_CHECKLIST.md).
Een projectie-indicator bewijst geen toegang tot de BYD-microfoon. Test telefoonmic versus
automicrofoon, gesproken uitvoer, Spotify hervatten, navigatiemeldingen en inkomende oproepen.
Er is geen eigen scherm op Android Auto en geen stuurknoponderschepping.

Geef na de eerste test door: Android-versie, ChatGPT-versie, beschikbaarheid van beide
Voice-instellingen, nieuw/bestaand gesprek, bekabelde/draadloze Android Auto en werkelijke
audio-/microfoonroute. Verdere ontwikkeling vereist een aantoonbaar ondersteunde
bedieningsroute vanuit Android Auto; telefoonbediening is geen geaccepteerde fallback.
