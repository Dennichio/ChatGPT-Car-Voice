> **0.3.0-dev ontwikkeling, nog niet gevalideerd:** standaard Android-assistent
> wordt nu via een impliciet systeemverzoek aangeroepen. Alle testresultaten en
> APK-checksums hieronder slaan alleen op de oudere 0.2.0-build.
>
# Testverslag — Android Auto-ontwikkelbuild 0.2.0

Uitgevoerd op 12 oktober 2026. Dit is een doorontwikkeling van dezelfde app,
applicationId `be.carvoice.app`, versiecode 2. De volledige gevraagde gebruikersflow
is **niet gevalideerd**: geen echte Android Auto-host, ChatGPT-account, geïnstalleerd
spraakmodel, Spotify-sessie of BYD is getest.

## Uitgevoerde controles

| Controle | Resultaat | Praktische reikwijdte |
| --- | --- | --- |
| Bestaande baselinebuild vóór wijzigingen | GESLAAGD | assembleDebug, lintDebug, assembleDebugAndroidTest; checkpoint 610c4f5 |
| Nieuwe debug-APK en Android-test-APK | GESLAAGD | Actuele broncode compileert; Gradle eindigde met BUILD SUCCESSFUL |
| Unit-tests | 37 GESLAAGD | 8 suites; 0 fouten, 0 errors, 0 overgeslagen |
| Android lint | GESLAAGD MET WAARSCHUWINGEN | 0 fouten, 5 waarschuwingen: vier KTX-conveniencesuggesties en een nieuwere JNA-versie; bekende versie blijft gepind |
| APK-identiteit en handtekening | GESLAAGD | Zelfde app-ID en certificaat als 0.1.0; versie 0.2.0/code 2 |
| Gecompileerd manifest | GESLAAGD | RECORD_AUDIO en INTERNET voor de nieuwe functies, POI-CarAppService, Car API 5, protected notification listener |
| Instrumentatie op emulator | GEEN UITGEVOERDE TESTS IN EERSTE RUN | Android PackageManager viel uit; installatie/uninstall faalden vóór tests |
| Template-, service-, preference- en permissietests | GECOMPILEERD | Werkelijke uitvoeringsstatus wordt hieronder afzonderlijk vastgelegd; geen echte Android Auto-host |
| Onafhankelijke review | UITGEVOERD | Routing, archiefgrenzen, PCM-fragmenten, toestemming, cleanup, hostvalidatie en claims gecontroleerd; concrete fixes aangebracht |
| Android Auto/DHU/BYD | NIET UITGEVOERD | Fysieke Android Auto-host vereist; geen voertuigbewijs |
| Automicrofoon en modeldecodering | NIET UITGEVOERD | Geen echt Nederlands model geïnstalleerd; bronhost in cloud met proxy-403 geblokkeerd |
| ChatGPT Voice-probe | NIET UITGEVOERD OP CHATGPT | Exported/enabled/permission-checks en foutpad geïmplementeerd; daadwerkelijke ChatGPT-versie op telefoon nodig |
| Spotify-playback | NIET UITGEVOERD OP SPOTIFY | Alleen parser/sessiebeleid getest; werkelijke actieve mediasessie en gebruikerstoestemming nodig |

## Unit-tests

- FavoriteStoreTest: 2 — bewaren/vervangen/verwijderen met valide coördinaten.
- NavigationCommandParserTest: 4 — Nederlandse bestemmings- en favoriete-plaatsopdrachten.
- NavigationRepositoryTest: 2 — echte lege uitkomst en begrenzing/foutafhandeling via geïnjecteerde provider.
- SpotifyCommandParserTest: 2 — ondersteunde transportopdrachten en expliciet onbevoegde zoekopdrachten.
- SpotifySessionPolicyTest: 1 — toestemming, sessie en aangeboden transportmogelijkheden verplicht.
- CarPcmInputStreamTest: 5 — PCM-fragmentatie, oneven reads en afgebroken laatste sample.
- ModelArchiveTest: 14 — paden, duplicaten, grootte-/entrygrenzen, annulering en directorypayloads.
- SpokenCommandRouterTest: 7 — gedeelde productie-routing; laadpaal/supermarkt vóór Spotify, geen gefingeerde ChatGPT-verwerking.

Deze tests bewijzen algoritmen en foutpaden, niet dat Android Auto zichtbaar is, de
automicrofoon levert, de navigatiehost accepteert, Spotify afspeelt of ChatGPT luistert.

## Instrumentatie en omgevingsbeperking

De AOSP Android 11/API30-emulator gebruikt software-emulatie zonder KVM. Na de cloudherstart
was ADB bereikbaar en verscheen zelfs `sys.boot_completed=1`, terwijl Androids `package`
en `window`-services tijdelijk verdwenen doordat system_server opnieuw opstartte.
Daarom geldt ADB-connectiviteit of alleen boot_completed niet als voldoende readiness.

De eerste volledige Gradle-run rapporteerde BUILD SUCCESSFUL, maar de connected runner
kon de APK's niet installeren: `cmd: Can't find service: package`. Er ontstond geen JUnit
connected-resultaat; een exit-codebestand met 0 en een lege protobuf-placeholder zijn
**geen geslaagde Androidtests**. De 37 unit-tests en build/lint waren wel uitgevoerd.
Een eventueel afzonderlijk vervolgresultaat wordt hieronder met het daadwerkelijke aantal
testcase-elementen vermeld. Een tweede, afzonderlijke connected-poging na herstelde
servicechecks faalde opnieuw vóór de tests: PackageManager verdween tijdens installatie
met `Failure calling service package: Broken pipe`. Ook die run leverde geen JUnit-XML op.
Definitief: **0 werkelijk uitgevoerde connected-tests voor deze build**. Verdere retries
zijn gestopt omdat de software-emulator geen stabiele Android-pakketservice bood.

De vroegere acht Androidtests voor versie 0.1.0 slaagden in de eerdere cloudrun. Ze worden
niet meegeteld als nieuwe 0.2.0-voertuig- of instrumentatievalidatie.

## Artefact en updatecompatibiliteit

APK: `downloads/chatgpt-car-voice-android-auto-0.2.0-debug.apk` — 54431849 bytes.

SHA256: `be3807983a7a0c2c161211d538f33ec08aa5d3dcb87431a37964a47bb108ad2c`.

Certificaat SHA256:
`d77f93745062d1f85e90642c2c851fda6c990853731ae3ac1b13ab23b07eda9e`.
Dit komt overeen met de eerste APK; een directe update behoudt dezelfde signing identity.
Google Play Internal App Sharing kan opnieuw ondertekenen met een ander certificaat en
vereist daarom een afzonderlijk update-/datamigratieplan. Geen private signing key gebundeld. De finale manifestcorrectie voegt de officiële TTS_SERVICE-query voor Android 11+ toe; assembleDebug en lintDebug zijn daarna opnieuw geslaagd. Dit verandert de hierboven getelde unit-tests niet.

## Resterende acceptatie

Controleer op een echte, correct gedistribueerde Android Auto-installatie: app zichtbaar,
bestemmingen/favorieten, de car-microfoon, modelherkenning, navigation handoff, audiofocus op
Android 15+, TTS en interrupties, Spotify-opdrachten en het opt-in ChatGPT-experiment.
De modellicentie en vertrouwde upstream-checksum zijn nog niet onafhankelijk geverifieerd.
De app bevat geen modelgewichten, betaald OpenAI-gebruik, accountcookies of backend.
