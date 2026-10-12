# Testverslag — eerste launcher-APK

**Actuele onderzoeksstatus:** de gebruiker meldt installatie van de APK en afwezigheid
in Android Auto **Launcher aanpassen**. De verklaring en primaire bronnen staan in
[het haalbaarheidsverslag](ANDROID_AUTO_FEASIBILITY.md). Tijdens dat vervolgonderzoek
is geen appcode gewijzigd, geen APK gebouwd en geen nieuwe instrumentatierun uitgevoerd.
Onderstaande acht tests gelden voor de bestaande telefoonlauncher en bewijzen geen
Android Auto-bediening. De broncode en die APK zijn inmiddels op GitHub gepubliceerd;
herstel in een nieuwe cloudtaak blijft afzonderlijk onbevestigd.

Uitgevoerd op 12 oktober 2026. De officiële ChatGPT-app is niet geïnstalleerd in de
testemulator; er is geen echt ChatGPT-account, Voice-gesprek of BYD getest.

| Controle | Resultaat | Reikwijdte |
| --- | --- | --- |
| Debug-APK build | GESLAAGD | AGP9.4.1, Gradle9.8.1, ingebouwde Kotlin2.2.10, compile/target SDK37, min SDK26 |
| Android-test-APK build | GESLAAGD | Bestaande JUnit4/Compose tests daadwerkelijk gecompileerd |
| Android lint | GESLAAGD | 0 fouten, 0 waarschuwingen in actuele lint-XML na geforceerde regeneratie |
| Reproduceerbare cloudsetup | GESLAAGD | `bash scripts/cloud-setup.sh` opnieuw uitgevoerd; bron-/dependencybestanden niet door installatiescript herschreven |
| Emulatorstart | GESLAAGD MET BEPERKING | AOSP Android11/API30 x86_64, software-emulatie zonder KVM, `sys.boot_completed=1` |
| Aangesloten instrumentatietests | 8 GESLAAGD | 0 fouten, 0 overgeslagen; actuele XML bevat acht benoemde testcase-elementen |
| APK-ondertekening | GESLAAGD | `apksigner verify`: één signer, v2-signature geldig; ontwikkelbuild |
| APK-manifest | GESLAAGD | Geen INTERNET, RECORD_AUDIO of services; alleen AndroidX app-specifieke signature-permission |
| Native appstart | BEPERKTE BEVESTIGING | MainActivity wordt RESUMED en UI rendert; `am start -W` gaf timing-timeout van circa 14,5 seconden in trage emulator |
| ChatGPT werkelijk openen | NIET UITGEVOERD | Officiële app ontbreekt in emulator; op echte telefoon testen |
| Voice automatisch starten | NIET UITGEVOERD | Op echte telefoon, met Start with Voice én nieuw/leeg gesprek |
| Voice/background/audio/Android Auto/BYD | NIET UITGEVOERD | Fysieke checklist vereist |
| Herstel in nieuwe cloudtaak / publicatie | NIET UITGEVOERD | Draftsave en huidige-instancevalidatie bewijzen geen nieuwe snapshot/restauratie |

## Acht werkelijk uitgevoerde tests

- `be.carvoice.app.integration.ChatGptLauncherTest.absentChatGptIsReportedUnavailableAndLaunchFailsSafely`: geslaagd (23.635s).
- `be.carvoice.app.integration.ChatGptLauncherTest.mergedAppManifestRequestsNoMicrophoneOrInternetPermission`: geslaagd (0.690s).
- `be.carvoice.app.integration.ChatGptLauncherTest.launcherActivityIsDiscoverableThroughAndroidMainLauncherIntent`: geslaagd (0.001s).
- `be.carvoice.app.ui.CarVoiceAppTest.disabledStateNeverEnablesLaunch`: geslaagd (11.815s).
- `be.carvoice.app.ui.CarVoiceAppTest.notInstalledStateDisablesLaunchAndOffersOfficialInstall`: geslaagd (11.671s).
- `be.carvoice.app.ui.CarVoiceAppTest.guidanceExplainsVoiceStartAndManualFallback`: geslaagd (14.569s).
- `be.carvoice.app.ui.CarVoiceAppTest.readyStateShowsDetectedVersionAndInvokesLaunchCallback`: geslaagd (12.080s).
- `be.carvoice.app.ui.CarVoiceAppTest.missingLaunchActivityNeverEnablesLaunch`: geslaagd (10.631s).

Vijf UI-tests gebruiken expliciete fixture-statussen en tellen callbacks. Zij bewijzen
geen echte ChatGPT-start, Bluetoothverbinding of Voice-sessie. Drie integratietests
gebruiken Android zelf: ontbrekende-appafhandeling, daadwerkelijk merged manifest en
de eigen publieke launcherregistratie. Er is geen fake app met de ChatGPT-pakketnaam
geïnstalleerd en er zijn geen gesimuleerde OpenAI API-calls gebruikt.

## Omgevingsproblemen en correcties

De cloudmachine biedt geen `/dev/kvm`. De zware API36/Google-emulator is gestopt; de
launcherchecks zijn op de lichtere AOSP API30-emulator uitgevoerd. Beide cold starts
zouden geen betrouwbare maat voor prestaties op een echte telefoon zijn.

Een eerste APK-installatie tijdens onvolledige Android-opstart mislukte omdat de
PackageManager nog niet volledig geïnitialiseerd was. Na boot-readiness installeerde de
APK succesvol. De eerste Gradle connected-run voerde nul app-tests uit: DDMLib kon de
SDK-versie niet binnen zijn standaard getprop-timeout van twee seconden lezen. Met de
bestaande SDK-overrides `ddmlib.getprop.timeout.sec=60` en installatie-timeout van 90s
herkende de runner API30 en voltooide hij alle acht tests. Asserties zijn niet uitgeschakeld.

De software-emulator liet ook een Android-systeem-ANR zien (`Process system isn't
responding`). De systeemdiagnostiek wees op PointerEventDispatcher tijdens een MENU-key
bij emulatorstart; dit was een Android-systeemmelding. Het native `am start -W`-timingvenster liep af. Dit beperkt de
responsiviteitsvalidatie; het is geen bewijs van foutloze werking op de telefoon. De
companion zelf werd zichtbaar/RESUMED en de aangesloten tests slaagden.

Een onafhankelijke agent controleerde broncode, publieke intents, gecompileerd manifest,
permissies en claims. Gevonden testmatcher-/assertieproblemen en oude projectinstructies
zijn gecorrigeerd. Geen hoog-ernstige beveiligingsbevinding bleef open.

## Artefact

APK: `chatgpt-car-voice-milestone1-debug.apk` — 13398825 bytes.

SHA256: `3d181d35524af0f8773294b3c4231cc4d8134cd8610ec51c74a2cf18e83cc4e7`.
