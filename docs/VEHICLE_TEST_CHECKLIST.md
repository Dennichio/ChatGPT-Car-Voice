# ChatGPT Car Voice — telefoon- en BYD-testchecklist

**Aangescherpte acceptatie-eis:** starten moet vanuit Android Auto gebeuren. De gebruiker
meldt dat de bestaande geïnstalleerde APK niet in **Launcher aanpassen** verschijnt.
De onderstaande historische telefoontests zijn geen bewijs dat deze eis gehaald is.
Zie de gerichte Android Auto-test in [het haalbaarheidsverslag](ANDROID_AUTO_FEASIBILITY.md).

**Status: NIET UITGEVOERD.** Er is nog geen test gedaan met een echte telefoon waarop de officiële ChatGPT-app is geïnstalleerd en aangemeld, of in de BYD. Voer voertuigtests geparkeerd uit.

## Testgegevens

| Gegeven | Waarde |
|---|---|
| Datum / tester | — |
| Telefoonmodel / Android-versie | — |
| ChatGPT-appversie / accountstatus | — |
| Android Auto-versie | 17.3 volgens gebruikersbaseline; nieuwste versie niet geverifieerd |
| BYD Seal U / DiLink | Bouwjaar 2024 en DiLink V2.3.0 volgens gebruikersbaseline; nieuwste firmware niet geverifieerd |

## Eerste telefoonmijlpaal

| Check | Stap | Verwacht / te noteren | Resultaat |
|---|---|---|---|
| Officiële app | Installeer/open ChatGPT via Google Play en meld aan volgens de app. | Noteer appversie en of de app start en accounttoegang heeft. | NIET UITGEVOERD |
| Companion-launcher | Tik in ChatGPT Car Voice op **Open ChatGPT**. | De officiële app opent; noteer foutmelding als de app ontbreekt, uitgeschakeld is of niet start. | NIET UITGEVOERD |
| Starten met Voice: uit | Schakel in ChatGPT **Instellingen → Spraak → Starten met Voice** uit; open ChatGPT via de companion. | Noteer of ChatGPT het gespreksscherm opent. Voice wordt niet door de companion gestart. | NIET UITGEVOERD |
| Starten met Voice: nieuw/leeg gesprek | Schakel **Starten met Voice** in en open een nieuw of leeg gesprek. | OpenAI documenteert automatische Voice-start op ondersteunde mobiele apparaten voor een nieuw/leeg gesprek. Noteer werkelijk gedrag. | NIET UITGEVOERD |
| Bestaand gesprek hervatten | Open via de companion wanneer ChatGPT een eerder, niet-leeg gesprek kan hervatten. | Automatische Voice-start is niet gegarandeerd; controleer of de gebruiker in ChatGPT op het Voice-pictogram moet tikken. | NIET UITGEVOERD |
| ChatGPT-shortcut | Bekijk de ChatGPT-iconopties in de Android-launcher en pin eventueel een aangeboden shortcut handmatig. | Noteer of de launcher een shortcut aanbiedt. De companion roept geen dynamische ChatGPT-shortcut aan. | NIET UITGEVOERD |
| Standaardassistent | Bekijk Android **Instellingen → Apps → Standaard-apps → Digitale assistent**. | Noteer of ChatGPT als keuze wordt aangeboden. Geen aanbod of stuurknopwerking veronderstellen. | NIET UITGEVOERD |

## Achtergrondgedrag

| Check | Stap | Verwacht / te noteren | Resultaat |
|---|---|---|---|
| Achtergrondgesprekken uit | Schakel **Achtergrondgesprekken** uit, start Voice en ga naar een andere app; vergrendel daarna het toestel. | Noteer of Voice stopt en op welk moment. | NIET UITGEVOERD |
| Achtergrondgesprekken aan | Schakel de instelling in, start Voice en ga naar een andere app; vergrendel daarna het toestel. | OpenAI documenteert doorgaan op de achtergrond, onder voorbehoud van handmatig stoppen, geforceerd sluiten, gebruikslimieten en maximale sessieduur. Noteer gedrag en meldingen. | NIET UITGEVOERD |
| Gesprek handmatig stoppen | Stop een achtergrondgesprek vanuit ChatGPT zelf. | Bevestig dat Voice stopt. De companion kan de ChatGPT-microfoonstatus niet uitlezen of de sessie stoppen. | NIET UITGEVOERD |

## BYD- en Android Auto-tests

| Check | Stap | Verwacht / te noteren | Resultaat |
|---|---|---|---|
| Android Auto actief | Verbind met BYD Android Auto en open ChatGPT via de companion op de telefoon. | Noteer of ChatGPT op de telefoon opent en wat op het autoscherm zichtbaar is. De companion levert geen eigen Android Auto-scherm. | NIET UITGEVOERD |
| Voice in de auto | Start Voice in ChatGPT en stel een korte vraag. | Noteer of antwoord goed hoorbaar is en welke bediening nodig is. Alle Voice-bediening blijft in ChatGPT. | NIET UITGEVOERD |
| Telefoonmicrofoon | Herhaal met de telefoon op een vaste, veilige plek in de cabine. | Noteer herkenning en verstaanbaarheid. | NIET UITGEVOERD |
| BYD-microfoon | Herhaal en bepaal of ChatGPT de BYD-microfoon gebruikt. | Noteer de daadwerkelijke ingang; ondersteuning is niet vooraf bevestigd. | NIET UITGEVOERD |
| Autospeakers | Laat ChatGPT antwoorden met Android Auto verbonden. | Noteer of geluid via de autospeakers, telefoonspeaker of een andere route komt. | NIET UITGEVOERD |
| Spotify en focus | Speel Spotify af; start Voice, laat een antwoord klinken en beëindig het gesprek. | Noteer pauzeren/dempen en hervatten van muziek. | NIET UITGEVOERD |
| Navigatie | Laat navigatie actief tijdens Voice. | Noteer hoe gesproken instructies en ChatGPT-audio samengaan en of navigatie hervat. | NIET UITGEVOERD |
| Inkomende oproep | Test geparkeerd tijdens Voice en tijdens een antwoord. | Noteer onderbreking, audioherstel en of Voice opnieuw gestart moet worden. | NIET UITGEVOERD |
| Stuurknop | Druk op de knop die normaal de ingebouwde assistent start. | Noteer welke assistent reageert. Er is geen bevestigde Android Auto-API voor onderschepping of omleiding naar ChatGPT. | NIET UITGEVOERD |

## Notities

Registreer exacte Android-, ChatGPT-, Android Auto- en DiLink-versies, accountstatus en of de verbinding bedraad of draadloos was. Markeer alleen werkelijk geobserveerd gedrag als geslaagd.
