# Android Auto en BYD Seal U: huidige integratie en grenzen

De bestaande `be.carvoice.app`-projectbasis is behouden en uitgebreid. De huidige broncode
bevat een echte Android for Cars App Library POI-service. De oude documentatie stelde dat er
geen geschikte categorie was; dat is vervangen door de inhoudelijke POI-implementatie. De
manifestregistratie alleen is geen bewijs van distributietoelating of werking in een auto.

## Wat de POI-module doet

- `CarVoiceService` registreert de categorie `androidx.car.app.category.POI`, met de
  `automotive_app_desc`-template-metadata en minimum host API level 5.
- `HostValidator` laat in debug alle testhosts toe; een non-debug build gebruikt de
  AndroidX Car App Library 1.7.0 host certificate allowlist in
  `res/values/car_hosts.xml`.
- De autoschermflow biedt een zoekscherm, expliciete selectie uit geocode-resultaten en een
  detailstap voordat een route wordt overgedragen.
- Androids platform-`Geocoder` doet best-effort adresopzoeking en geeft maximaal vijf
  resultaten terug. Dit is geen gegarandeerde dienst of volledige lokale POI-databank.
- Vrije nabijheidsvragen zoals laadpalen en supermarkten worden als `geo:` query aan de
  actieve navigatiehost overgedragen. Exacte locaties worden via coördinaten doorgegeven.
- De gebruiker kan thuis, werk en favorieten op de telefoon instellen; adressen en
  coördinaten worden lokaal in appvoorkeuren opgeslagen.
- Routeberekening, keuze uit navigatie-apps, verkeersinformatie en begeleiding blijven bij
  de geselecteerde navigatie-app. CarVoice bouwt geen eigen navigatie-engine.

Dit is een echte bestemmingzoeker die de POI-categorie inhoudelijk gebruikt. De implementatie
maakt de app niet tot een navigatieapp en belooft niet dat een Android Auto-review, testtrack
of voertuig de build accepteert.

## Spraak en voertuiggeluid

De gebruiker verleent microfoontoestemming vooraf op de telefoon. Een expliciete
**Spreek opdracht**-actie in het autoscherm gebruikt `CarAudioRecord` voor PCM-invoer en
Vosk voor offline Nederlandse herkenning. Het model moet eerst door de gebruiker gedownload
zijn; het wordt niet meegeleverd en er is geen automatische modeldownload. De herkenning is
opdrachtgestuurd, stopt na resultaat of uiterlijk na 30 seconden, en heeft geen ChatGPT-
vervolggesprek. Tekst-naar-spraak geeft korte navigatie-/actiebevestigingen via Androids
geselecteerde audio-uitvoer. Werkelijke microfoon-, speaker-, audiofocus- en BYD-routing zijn
niet bevestigd; CarAudioRecord garandeert geen specifieke BYD-microfoon.

## Spotify

Spotify-integratie leest alleen de actieve `MediaSession` van `com.spotify.music`. De
gebruiker moet Spotify eerst zelf starten en Androids notificatietoegang verlenen. Als de
actieve sessie de actie aanbiedt, kan CarVoice Play, Pause, Next en Previous verzenden en
de door Spotify gepubliceerde titel/artiest tonen. Dat is een transportbesturing van een
bestaande sessie, geen Spotify-appclient of volledige player. Zoeken op artiest, track of
playlist, starten van een niet-actieve sessie en afspeelapparaat kiezen worden niet
ondersteund. Spotify SDK binaries, OAuth en developer app credentials zijn niet gebruikt.

## ChatGPT

De telefoonknop opent de publieke launcheractiviteit van `com.openai.chatgpt`. De optionele
in-car ChatGPT Voice-proef probeert na expliciete toestemming `ACTION_ASSIST` naar
`com.openai.voice.assistant.AssistantActivity` te sturen. Die component is ongedocumenteerd,
kan per appversie ontbreken of door Android worden geblokkeerd, en is geen OpenAI API.
Een geslaagd `startActivity`-resultaat betekent uitsluitend dat Android het verzoek aannam;
het bewijst geen luisterende Voice-sessie, gespreksturn, automicrofoon of vervolgvragen.
ChatGPT Voice blijft daarom experimenteel en uitgeschakeld als standaard.

CarVoice beheert geen ChatGPT-account, betaalde API, abonnement, gesprekshistorie of
achtergrondgesprek. Er is geen officiële subscription bridge of algemene OpenAI-
gespreksinterface gevonden.

## Publicatie en testvoorwaarden

Android Auto **Onbekende bronnen** is niet genoeg om een Car App Library-app in de launcher
te krijgen. Voor de daadwerkelijke hostdistributie is een door Android Auto vertrouwde
installatie via een vertrouwde Play-route nodig, met toegang voor het Play-account van de
tester. Internal Testing vereist een voor Play Console geschikte releasebuild, doorgaans
een AAB; de debug-APK is niet zelf een Internal Testing-release. Internal App Sharing kan
een debug-APK delen, maar herondertekent die met het Internal App Sharing-certificaat. Een
bestaande lokaal ondertekende debug-APK kan daardoor niet als normale update worden
overschreven. Een review/testtrack en hostkwaliteitseisen kunnen ook gelden.
Een lokaal geïnstalleerde debug-APK kan dus correct gebouwd zijn en toch ontbreken in
**Launcher aanpassen**. De nieuwe 0.2.0 APK en een vertrouwde distributieroute zijn nog niet
als succesvol zichtbaar in een voertuig bevestigd.

Android Auto Desktop Head Unit (DHU) is een test-hostweergave die met een Android Auto-
telefoon verbonden moet worden. Een DHU-host op de computer zonder die telefoonintegratie is
geen test van `CarAppService`. Er is nog geen DHU-hosttest van deze POI-service uitgevoerd.

## BYD Seal U-status

BYD Seal U 2024, DiLink V2.3.0 en Android Auto 17.3 zijn de opgegeven voertuigbaseline, niet
door ons fysiek geverifieerde versies. Er is geen test in de echte wagen uitgevoerd. Tot de
fysieke checklist is voltooid blijven Android Auto-launcherzichtbaarheid, BYD-microfoon,
autospeakers, Spotify-audiofocus, navigatiehandoff en stuurbediening onbevestigd. CarVoice
onderschept of omleidt geen stuurknoppen.

Zie [installatie](INSTALLATION.md), [haalbaarheid](ANDROID_AUTO_FEASIBILITY.md),
[publieke interfacegrenzen](PUBLIC_INTERFACES.md) en de
[voertuigtestchecklist](VEHICLE_TEST_CHECKLIST.md).
