# BYD Seal U en Android Auto: fysieke testchecklist

**Status: NIET UITGEVOERD IN DE ECHTE WAGEN.** Deze lijst is voor een geparkeerde setup en
stapsgewijze validatie. De gebruiker opgegeven baseline is BYD Seal U 2024, DiLink V2.3.0
en Android Auto 17.3; geen van die exacte softwareversies is hier fysiek bevestigd.

Stop als een scherm meer aandacht vraagt dan veilig geparkeerd beschikbaar is. Gebruik geen
rijtest om een permissie, modeldownload, account, telefooninstelling of nieuwe feature te
configureren.

## Testgegevens

| Gegeven | Waarde |
|---|---|
| Datum / tester | — |
| Telefoonmodel / Android-versie | — |
| Android Auto-versie / kabel of draadloos | — |
| CarVoice-versie / APK-hash / installatiemethode | — |
| ChatGPT-versie | — |
| Spotify-versie / sessiestatus | — |
| BYD Seal U / DiLink-firmware | — |
| Navigatie-app | — |
| Resultaat per test | NIET UITGEVOERD |

## Distributie en zichtbaarheid

| Test | Uitvoering | Resultaat |
|---|---|---|
| Vertrouwde testinstallatie | Installeer exact de 0.2.0 build via de geregelde Google Play Internal Testing- of Internal App Sharing-route met toegang voor dit Play-account. Noteer Play-account en testtrack. | NIET UITGEVOERD |
| Launcher/category discovery | Verbind Android Auto en kijk of ChatGPT Car Voice als POI-app beschikbaar is. Noteer ook **Launcher aanpassen**; forceer geen andere categorie of hidden-source workaround. | NIET UITGEVOERD |
| Hostvalidatie | Open de POI-app via Android Auto en noteer host-/reviewfouten. Controleer dat de daadwerkelijk geteste variant past bij de distributiebron. | NIET UITGEVOERD |
| DHU met telefoon | Start de officiële Desktop Head Unit-test met de Android Auto-telefoon aangesloten. Herhaal de POI-open/searchflow; DHU alleen zonder telefoon is geen hostintegratietest. | NIET UITGEVOERD |

## POI, geocoding en navigatiehandoff

| Test | Uitvoering | Resultaat |
|---|---|---|
| Adres zoeken | Zoek een exact adres met meerdere onderdelen. Controleer dat resultaten zichtbaar zijn en dat er maximaal vijf worden getoond. | NIET UITGEVOERD |
| Ambigue bestemming | Zoek een veelvoorkomende plaatsnaam. Selecteer bewust het juiste resultaat via het detailscherm. | NIET UITGEVOERD |
| Niets gevonden / geocoderfout | Zoek een onzinnige tekst en herhaal indien mogelijk met geocoder niet beschikbaar. Controleer duidelijke fout en geen valse bestemming. | NIET UITGEVOERD |
| Routehandoff | Kies een bekende bestemming en controleer dat de actieve navigatie-app de locatie ontvangt en zelf de route berekent. Noteer gekozen app. | NIET UITGEVOERD |
| Nabije laadpaal/supermarkt | Kies de nabijheidsquery en controleer dat die als zoekvraag naar de navigatie-app gaat. Noteer of de navigatie-app resultaten vindt; CarVoice levert geen eigen POI-inventaris. | NIET UITGEVOERD |
| Favorieten | Stel Thuis, Werk en een andere favoriet in de telefoonconfiguratie in. Kies elk in de auto en controleer de juiste coördinaten en bestemming. Verwijder daarna één favoriet. | NIET UITGEVOERD |

## Automicrofoon en offline spraak

| Test | Uitvoering | Resultaat |
|---|---|---|
| Setup-permissie | Parkeer, geef RECORD_AUDIO toestemming op de telefoon en controleer de getoonde status. | NIET UITGEVOERD |
| Modeldownload | Download het Nederlandse Vosk-model expliciet via de telefoonconfiguratie. Noteer bron, grootte, tijd en resultaat; de licentie van de gewichten is nog niet geverifieerd. | NIET UITGEVOERD |
| Stemactie | Open Android Auto POI-scherm en tik **Spreek opdracht**. Controleer de luisterstatus en eventuele Android Auto-hostfout. | NIET UITGEVOERD |
| Automicrofoon | Spreek rustig: “Navigeer naar Leuven”. Bevestig welke microfoon daadwerkelijk gebruikt werd (BYD, headset of anders); stemherkenning op de telefoon alleen is geen bewijs van CarAudioRecord-ingang. | NIET UITGEVOERD |
| Lokale herkenning | Herhaal met model geïnstalleerd, netwerk uit en model al klaar. Controleer dat opdracht herkend wordt en dat de model/downloadactie niet tijdens de opname start. | NIET UITGEVOERD |
| Niet-herkende opdracht | Geef een vraag die niet bij navigatie of ondersteunde Spotify-commando's hoort. Controleer foutmelding en dat er geen ChatGPT-antwoord wordt gesuggereerd. | NIET UITGEVOERD |
| Stop/timeout/interruption | Stop opname; test ook audiofocusverlies door een oproep en de 30-secondenlimiet. Controleer dat partial text geen actie uitvoert. | NIET UITGEVOERD |
| TTS-handoff | Laat een bestemming bevestigen en noteer of de korte TTS via autospeakers of andere uitvoer klinkt en of navigatieaudio terugkomt. | NIET UITGEVOERD |

## Spotify actieve sessie

| Test | Uitvoering | Resultaat |
|---|---|---|
| Grant vereist | Parkeer, open Androids notificatietoegangsinstelling en geef toegang bewust. Controleer status in CarVoice. | NIET UITGEVOERD |
| Geen actieve sessie | Sluit/stop Spotify en vraag een media-actie. Controleer dat de app eerlijk meldt dat geen sessie bestaat. | NIET UITGEVOERD |
| Actieve sessie | Open Spotify en start zelf een nummer. Controleer herkenning van tracklabel. | NIET UITGEVOERD |
| Transportacties | Test pauze, hervat, volgend en vorig nummer. Noteer per actie of Spotify die actie in de actieve sessie aanbiedt en werkelijk uitvoert. | NIET UITGEVOERD |
| Niet-ondersteunde zoekopdracht | Vraag een artiest of playlist. Controleer dat CarVoice meldt dat naamzoeken niet beschikbaar is en niets verzint. | NIET UITGEVOERD |
| Notificatieprivacy | Controleer dat CarVoice enkel de mediasessie gebruikt en geen inhoud van andere meldingen toont. | NIET UITGEVOERD |

## ChatGPT-proef en fysieke afronding

| Test | Uitvoering | Resultaat |
|---|---|---|
| Normale telefoonknoop | Op telefoon, open ChatGPT via de launcher. Noteer dat dit alleen de officiële app opent. | NIET UITGEVOERD |
| ChatGPT Voice probe | Alleen indien expliciet ingeschakeld, test geparkeerd de experimentele `ACTION_ASSIST`-probe. Noteer componentbeschikbaarheid, eventuele weigering en of Voice werkelijk luistert. Een startresultaat is geen supported-API bewijs. | NIET UITGEVOERD |
| Gesprek in auto | Start geen rijtest op basis van alleen een geslaagde probe. Noteer of er een echte ChatGPT Voice-conversatie, car-mic, antwoorden en vervolgvragen zijn; dit is nu geen ondersteunde CarVoice-functie. | NIET UITGEVOERD |
| Geen telefoonbediening tijdens rit | Pas na alle veilige parkeerchecks: laat een tweede persoon observeren; controleer of bestemming van autoscherm af tot routehandoff en verstaanbare feedback zonder telefoonschermbediening lukt. | NIET UITGEVOERD |
| Android Auto / BYD combinatie | Herhaal korte geparkeerde controle van audiofocus, inkomende oproep en Android Auto-reconnect. Noteer firmware en bedraad/draadloos. | NIET UITGEVOERD |

Markeer iedere rij afzonderlijk als **GESLAAGD**, **MISLUKT** of **NIET UITGEVOERD** en noteer
de feitelijke observatie. Een geslaagde emulator- of unit-test vervangt geen DHU-, Play-track-
of BYD-controle. Raadpleeg het root-beheerde [testverslag](TEST_REPORT.md) voor uitsluitend
werkelijk uitgevoerde automatische en buildcontroles.
