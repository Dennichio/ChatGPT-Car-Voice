# ChatGPT Car Voice

ChatGPT Car Voice is het bestaande Android-project met package name `be.carvoice.app`, nu
uitgebreid met een echte Points of Interest (POI)-bestemmingenzoeker voor Android Auto. De
app gebruikt de POI-categorie omdat ze echte bestemmingen toont en overdraagt. Ze is geen
navigatie-engine en de integratie is nog niet bevestigd in de BYD Seal U.

Via het telefoonconfiguratiescherm kan de gebruiker microfoontoestemming geven, expliciet
een offline Nederlands Vosk-model downloaden, en Thuis, Werk en andere favorieten opslaan.
Op het autoscherm kan de gebruiker een adres zoeken, een geocode-resultaat kiezen of een
vrije nabijheidsquery aan de geïnstalleerde navigatie-app overdragen. Nederlandse
spraakopdrachten worden herkend met `CarAudioRecord` en het gedownloade model; korte
bevestigingen gebruiken Android TextToSpeech. Androids geocoder werkt best effort en
bestemmingshandoff is afhankelijk van een geschikte navigatie-app.

Spotify-besturing is beperkt tot een al actieve Spotify-mediasessie. Nadat de gebruiker
Spotify zelf gestart heeft en Androids notificatietoegang heeft verleend, kan de app
afspeel-, pauzeer-, volgende-, vorige- en huidige-trackacties aanvragen voor zover Spotify
deze acties aanbiedt. De app zoekt niet in tracks of playlists, kiest geen afspeelapparaat,
gebruikt geen Spotify OAuth en bevat geen Spotify SDK.

De telefoonknop opent de officiële ChatGPT-app via Androids publieke launcherinterface. Een
experimentele Android Auto-actie probeert optioneel een ongedocumenteerde ChatGPT-activiteit
te starten. Deze staat standaard uit en is geen ondersteunde Voice-API. Geen van beide
routes geeft CarVoice toegang tot een ChatGPT-gesprek of abonnementsbridge. Er is geen OpenAI
API, backend, API-sleutel of aparte API-facturatie.

## Spraakmodel en privacy

Het Vosk-model zit **niet** in de APK. De gebruiker start de download zelf vanuit de
telefoonconfiguratie wanneer de auto geparkeerd staat. De app controleert de archiefstructuur
en laadt het model lokaal voordat het klaar wordt gemarkeerd. De upstream host gaf tijdens
onderzoek HTTP 403; de licentie van deze modelgewichten is niet bevestigd en mag daarom niet
als Apache gelicentieerd worden voorgesteld. De app noteert de SHA-256 van de ontvangen bytes,
maar vergelijkt die niet met een vertrouwde upstream-checksum.

Microfoontoestemming wordt vooraf aangevraagd; opname start pas na een expliciete
microfoonactie in de auto. Herkenning is ontworpen om lokaal te draaien. Favoriete adressen
en coördinaten staan op het toestel. Androids systeemgeocoder en de gekozen navigatie-app
kunnen zoektermen of bestemmingen volgens hun eigen diensten en privacybeleid verwerken.
Spotify-toegang gebruikt Androids mediatoestemming; CarVoice leest de actieve Spotify-sessie,
geen notificatietekst.

## Build en installatie

De eerste `0.1.0`-debug-APK blijft bewaard als telefoonlauncher-checkpoint. De huidige
broncode gebruikt `0.2.0`. [Download de nieuwe APK](https://github.com/Dennichio/ChatGPT-Car-Voice/raw/refs/heads/main/downloads/chatgpt-car-voice-android-auto-0.2.0-debug.apk).
Het bestand staat in `downloads/`, met de [SHA-256-controlesommen](downloads/SHA256SUMS).
Build, lint en 37 unit-tests slagen; de emulatorinstrumentatie kon wegens een uitvallende
Android-pakketservice geen tests uitvoeren. Het is een ontwikkelbuild met nog onbevestigde
voertuigwerking. Zie
[installatie](docs/INSTALLATION.md) voor distributie en eenmalige configuratie.

Voor Car App Library-apps is onbekende bronnen inschakelen op zichzelf onvoldoende voor
Android Auto. Internal Testing vereist een voor Play Console geschikte, ondertekende
releasebuild (meestal een AAB), een ingerichte testtrack en toegestane Play-accounttoegang;
de debug-APK is niet rechtstreeks een release voor die track. Internal App Sharing kan een
debug-APK accepteren, maar Play herondertekent die met de Internal App Sharing-sleutel. Dat
certificaat verschilt van de bewaarde debug-sleutel en kan een directe update blokkeren.
Een gewone lokale APK-update werkt alleen met dezelfde signing key. Hetzelfde package name
garandeert geen update. Plan migratie van lokale favorieten/modeldata voordat je de
bestaande installatie via een ander certificaat vervangt.

## Projectstatus

De broncode bevat een echte POI-service, best-effort platform-geocoding, favorieten,
navigatiehandoff, expliciet gestarte offline spraak, beperkte Spotify-acties op een actieve
mediasessie en een optionele ChatGPT-activiteitprobe. De volledige autoritworkflow is niet
geverifieerd. Er is nog geen Android Auto Desktop Head Unit (DHU)-hosttest of fysieke BYD-test
uitgevoerd en het spraakmodel is nog niet op een echt toestel getest. Zie de
[Android Auto-notities](docs/ANDROID_AUTO_FEASIBILITY.md),
[compatibiliteitsgids](docs/CAR_COMPATIBILITY.md),
[grenzen van publieke interfaces](docs/PUBLIC_INTERFACES.md),
[voertuigchecklist](docs/VEHICLE_TEST_CHECKLIST.md),
[derde-partijvermeldingen](THIRD_PARTY_CODE.md) en het door de hoofdagent bijgehouden
[testverslag](docs/TEST_REPORT.md).
