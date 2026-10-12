# Werkpakketten en behoudsinventaris

## Bestaande codebasis

De opdracht is uitgevoerd als doorontwikkeling van de bestaande repository. De aanvankelijke
`0.1.0`-debugbaseline had een groene build- en launchercontrole volgens het
[root-beheerde testverslag](TEST_REPORT.md). Die checkpoint en de broncode zijn niet
vervangen door een nieuw project. De eerste tests bewijzen alleen de toenmalige
telefoonlauncher; ze valideren de nieuw toegevoegde auto-, audio- of voice-code niet.

| Indeling | Wat is behouden of veranderd |
|---|---|
| **BEHOUDEN** | Bestaande GitHub-repository en Android Studio/Gradle-project; naam ChatGPT Car Voice; package/applicationId `be.carvoice.app`; bestaande MainActivity/Compose-launcher; publieke ChatGPT-appstart; bestaande signing-identiteit voor updatecompatibiliteit; de `0.1.0` APK als checkpoint. |
| **UITBREIDEN** | AndroidX Car App Library POI-service en echte bestemmingzoekflow; platform-geocoder; bestemmingsselectie, favorieten en geo-handoff; expliciete telefoonconfiguratie; optionele download van een offline Nederlands spraakmodel; CarAudioRecord opdrachtinvoer; beperkte Spotify active-session transportbesturing; experimentele opt-in ChatGPT-componentprobe. |
| **VERVANGEN / corrigeren** | Documentatie die de app alleen als telefoonlauncher beschreef of stelde dat geen POI-categorie paste, is bijgewerkt voor de huidige echte POI-module. Geen werkende projectstructuur, package name, appidentiteit of launcherbaseline is vanwege architectuurvoorkeur vervangen. Geen volledige herbouw vanaf nul. |

De huidige code gebruikt dezelfde package name en `versionCode=2`, `versionName=0.2.0`.
Een rechtstreekse lokale APK-update over de oude debuginstallatie vereist dezelfde
debugsigning key; hetzelfde package name is onvoldoende. Internal Testing gebruikt de
Play-ondertekeningsidentiteit en Internal App Sharing herondertekent gedeelde APK's. Beide
kunnen een update van de oude debuginstallatie weigeren en lokale data vereisen eerst een
migratieplan. De 0.2.0-build, vertrouwde Play-distributie en voertuighost zijn nog geen
bevestigde eind-tot-eindvalidatie.

## Configuratie en werkpakketten

De repository bevat de bestaande Codex-agentrollen onder `.codex/agents/` voor Android Auto,
Android UI, voice, voertuigonderzoek, security review, test/build en documentatie. De
hoofdagent blijft eigenaar van interfaces, integratie, build en releasebeslissingen. De
werkpakketten zijn gescheiden naar Android Auto/POI, navigation/Spotify, voice/OpenAI-grenzen,
UI/setup, build/test, onafhankelijke review en documentatie; modules zijn onder de bestaande
`app/src/main/java/be/carvoice/app/` packages geïntegreerd.

Open-sourcehergebruik is beperkt tot concrete Car Samples-template/host-validatorpatronen,
een intentpatroon uit BYD Assistant voor de expliciet experimentele Voice-probe en de
Vosk-stream-decode-aanpassing. Herkomst en voorwaarden staan in
[`THIRD_PARTY_CODE.md`](../THIRD_PARTY_CODE.md). Geen volledige externe app is gekopieerd.

## Huidige functiestatus

- **Android Auto POI:** manifest en CarAppService zijn geïmplementeerd. Android Auto/
  DHU-host, Play-trusted distribution en launcherzichtbaarheid zijn nog niet bevestigd.
- **Navigatie:** Android Geocoder-resultaten en `ACTION_NAVIGATE`-handoff zijn in code;
  volledige route is afhankelijk van de navigatiehost en fysiek niet getest.
- **Spraak:** de app heeft een CarAudioRecord/Vosk-pad dat expliciet gestart wordt na
  permissie en een apart gedownload model. Het Nederlands model is niet meegeleverd;
  licentie van de gewichten is niet vastgesteld. Echt automicrofoon-/decodergebruik is
  niet getest.
- **Spotify:** code kan alleen acties opvragen voor een al actieve Spotify MediaSession die
  de actie adverteert. OAuth, cataloguszoeken, appdevicekeuze en starten van inactieve Spotify
  zijn geen geïmplementeerde onderdelen.
- **ChatGPT:** publieke telefoonstart plus een standaard-uit experimentele componentprobe.
  Geen ondersteunde Android Auto ChatGPT Voice sessie, abonnementbridge, API of vervolgvraag.
- **BYD:** geen echte wagenvalidatie. Zie [voertuigchecklist](VEHICLE_TEST_CHECKLIST.md).

De hoofdtaak houdt `docs/TEST_REPORT.md` bij. Dit document bevat geen nieuwe build-, lint-,
emulator-, APK-hash- of fysieke testclaims; controleer daar de resultaten na de 0.2.0-build.
