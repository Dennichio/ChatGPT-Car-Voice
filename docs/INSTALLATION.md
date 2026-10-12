# Installatie en eerste configuratie

Deze handleiding geldt voor de uitgebreide `0.2.0`-broncode en de Android Auto POI-app.
De oudere `0.1.0` APK in `downloads/` is alleen de bewaarde telefoonlauncher-checkpoint.
[Download de nieuwe APK](https://github.com/Dennichio/ChatGPT-Car-Voice/raw/refs/heads/main/downloads/chatgpt-car-voice-android-auto-0.2.0-debug.apk).
De [controlesommen](../downloads/SHA256SUMS) staan naast het bestand in `downloads/`.
Build en unit-tests zijn geslaagd; echte Android Auto-, Voice- en BYD-validatie ontbreken.

De debug-APK is een ontwikkelbuild, geen Play Store-release. Car App Library apps moeten
via een door Android Auto vertrouwde distributieroute geïnstalleerd worden. Onbekende
bronnen inschakelen is hiervoor niet genoeg. De app moet daarnaast aan Play- en Android Auto
review-/testvoorwaarden voldoen voordat zichtbaarheid in de Android Auto-launcher kan
worden verwacht. Een normale sideload is geschikt voor telefoon- of lokale debugtests, maar
bewijst geen toelating tot de auto-launcher.

Er zijn twee verschillende Play-testpaden:

- **Internal Testing:** upload een voor Play Console geschikte, ondertekende releasebuild
  (meestal een Android App Bundle) naar de ingerichte testtrack. De gebruiker moet met een
  toegestaan Google Play-account de track kunnen openen. De debug-APK wordt niet rechtstreeks
  als Internal Testing-release gebruikt.
- **Internal App Sharing:** kan een debug-APK accepteren, maar Play herondertekent de
  gedeelde download met het Internal App Sharing-certificaat. Dat certificaat verschilt van
  de bewaarde lokale debug-sleutel.

De gewone lokale debug-APK kan alleen als in-place update installeren wanneer de bestaande
APK met dezelfde signing key is ondertekend. Hetzelfde package name garandeert geen update.
Een Play-test-APK of Internal App Sharing-download kan daarom een certificaatconflict met de
reeds geïnstalleerde debugversie geven. Android zal de update dan weigeren. Plan vóór
migratie hoe lokale favorieten en andere appdata behouden worden; modelbestanden moeten na
verwijderen van de oude app opnieuw worden gedownload. Verwijder de bestaande app niet zonder
een geaccepteerd backup-/migratieplan. Gebruik geen ADB- of package-installertruc om de
ondertekeningscontrole te omzeilen.

## Installeren

1. Download de 0.2.0 APK en verifieer de gepubliceerde SHA-256-controlesom.
2. Installeer via de beschikbare Google Play-testuitnodiging of Internal App Sharing-link
   op de Android-telefoon en accepteer de testtoegang met het juiste Play-account.
3. Controleer de ondertekenaar van de download en de bestaande installatie. Alleen een
   APK met dezelfde certificaatketen kan als normale Android-update over de lokale debug-
   APK installeren. Een afwijkende Play/Internal App Sharing-handtekening vereist een
   vooraf afgesproken migratie; ga niet uit van behoud van lokale data.
4. Open **ChatGPT Car Voice** op de telefoon voor eenmalige configuratie.

Voor een lokale debugtest, zodra de APK beschikbaar is:

```bash
adb install -r downloads/chatgpt-car-voice-android-auto-0.2.0-debug.apk
adb shell am start -n be.carvoice.app/.MainActivity
```

Een lokaal geïnstalleerde debug-APK hoeft niet als Android Auto-app te verschijnen. Een
correcte categorie en manifestregistratie vervangen de vertrouwde distributie-/testtrack
eis niet.

## Eenmalige configuratie op de telefoon

Voer deze stappen stilstaand uit voordat je de app in de auto gebruikt:

1. Geef CarVoice microfoontoegang. De app start geen opname tijdens toestemming of installatie.
2. Tik op **Download Nederlands spraakmodel** om het Vosk-model apart te downloaden.
   De download is expliciet, niet automatisch. Het model wordt niet met de APK meegeleverd.
   De upstream modellicentie kon tijdens onderzoek niet worden bevestigd; zie
   [derde-partijcode](../THIRD_PARTY_CODE.md).
3. Zoek en bewaar desgewenst **Thuis**, **Werk** en andere favoriete adressen. Kies de
   juiste geocode-uitkomst; het platform kan nul, meerdere of onjuiste resultaten geven.
4. Om Spotify-commando's te gebruiken, start eerst Spotify zelf en geef notificatietoegang
   via Android-instellingen. Die brede systeemtoestemming wordt alleen gebruikt om een
   actieve Spotify-mediasessie te vinden. CarVoice leest geen notificatietekst.
5. De ChatGPT Voice-proef blijft standaard uit. Alleen inschakelen als je de
   ongedocumenteerde startpoging bewust wilt testen; een geaccepteerd startverzoek bewijst
   niet dat ChatGPT luistert of dat er een bruikbare Voice-sessie bestaat.

Er is geen accountkoppeling in CarVoice. Meld je desgewenst alleen in de officiële ChatGPT-
of Spotify-app aan. CarVoice gebruikt geen OpenAI API of ChatGPT-abonnementsbridge.

## Android Auto en Desktop Head Unit

1. Zorg dat de POI-app via een vertrouwde Play-testtrack of Internal App Sharing is
   geïnstalleerd en dat Android Auto de build mag tonen.
2. Verbind de Android Auto-telefoon met de Desktop Head Unit (DHU) volgens de officiële
   Android-testinstructies. DHU is de hostcomputerweergave; zonder echte Android Auto-
   telefoonintegratie test DHU op zichzelf geen CarAppService-hostflow.
3. Open de app vanuit Android Auto. Als hij niet zichtbaar is, noteer distributiebron,
   Play-account, Android Auto-versie en eventuele review-/testtrackstatus. Wissel niet naar
   een niet-POI-categorie om de app zichtbaar te forceren.
4. Zoek een adres, selecteer het gewenste resultaat en controleer dat de gekozen
   navigatie-app de locatie ontvangt. Nabije categorieën zoals laadpalen worden als query
   naar de navigatie-app overgedragen; ze zijn geen eigen POI-databank.
5. Test gesproken opdrachten alleen nadat het model klaar is en microfoontoestemming
   verleend is. Tik op **Spreek opdracht**, wacht op de luisterstatus en geef een korte
   bestemming- of ondersteunde Spotify-opdracht. Opname stopt na opdracht of na maximaal
   30 seconden. Bij fout: stop en onderzoek permissie, model, host en audioingang.

Voor ChatGPT is er geen in-car gesprekssessie. De telefoonknop opent de officiële ChatGPT-
app. De optionele ChatGPT-activiteitprobe gebruikt een ongedocumenteerde interface en kan
ontbreken of geweigerd worden; ze mag niet als ondersteunde Auto- of Voice-functie worden
behandeld.

Voer daarna de [BYD Seal U-checklist](VEHICLE_TEST_CHECKLIST.md) uit. Geen van deze stappen
is bevestigd in een fysieke BYD; de teststatus staat in het root-beheerde
[testverslag](TEST_REPORT.md).
