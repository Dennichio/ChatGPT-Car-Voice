# Third-party code, dependencies en model

Deze lijst bevat daadwerkelijk hergebruik in de huidige source tree. De projectrepository
gebruikt package name `be.carvoice.app`; geen compleet extern project is gekopieerd.

## Hergebruikte bronpatronen

| Bron | Vastgezette revisie | Licentie en copyright | Gebruik in CarVoice |
|---|---|---|---|
| [`kangrio/Assistant`](https://github.com/kangrio/Assistant/tree/5e2838bae9e47c06a3d98e3c5f91af7cc19b0c13) | `5e2838bae9e47c06a3d98e3c5f91af7cc19b0c13` | MIT, Copyright 2026 KangRio | Alleen het intentpatroon uit `Utils.kt` voor een optionele `ACTION_ASSIST`-probe naar de ongedocumenteerde ChatGPT Voice-activiteit. Geen activity, wakeword, service, firmware-/stuurknopcode of volledige app overgenomen. De MIT-licentietekst staat in `app/src/main/assets/licenses/BYD-Assistant-MIT.txt`. |
| [`android/car-samples`](https://github.com/android/car-samples/tree/9be0b199986770cd5cde7765765c0eaef0758d15) | `9be0b199986770cd5cde7765765c0eaef0758d15` | Apache License 2.0, Copyright 2021 The Android Open Source Project | SearchTemplate/SearchScreen-structuur en HostValidator-/CarAppService-patronen aangepast tot de POI-bestemmingzoeker. Samplecode, demo-data en sample-navigation-engine zijn niet als app gekopieerd. De Apache-notice staat in `app/src/main/assets/licenses/Android-Car-Samples-Apache-2.0.txt`. |
| AndroidX Car App Library 1.7.0 hostallowlist | Versie `1.7.0` (AndroidX-dependency) | Apache License 2.0, Android Open Source Project | De release host certificate allowlist is aangepast van AndroidX Car App 1.7.0 in `app/src/main/res/values/car_hosts.xml`; release HostValidator gebruikt deze expliciete lijst. De Apache-notice staat bij de Android Car Samples-code. |
| [`alphacep/vosk-api`](https://github.com/alphacep/vosk-api/tree/05adbfcc0df27a1535913c6accd4b7fc60ffd59d) | `05adbfcc0df27a1535913c6accd4b7fc60ffd59d` | Apache License 2.0, Copyright 2019 Alpha Cephei Inc. | Stream-decode-patroon van `SpeechStreamService.java` aangepast voor `CarAudioRecord` PCM-invoer en Vosk offlineherkenning. Geen Vosk-server, webinterface of service gekopieerd. Het bijbehorende Vosk API-notice moet bij verdere redistributie behouden blijven. |

Car Samples service-, query- en detailcode bevat de Apache 2.0 copyright-/license-header.
De repositorynotice staat als asset in de APK-bronmap. BYD Assistant en Vosk worden voor
kleine intent-/streampatronen gebruikt; hun voorwaarden en notices blijven van toepassing.

## Runtime- en Android-dependencies

| Dependency | Gebruik | Licentie-opmerking |
|---|---|---|
| `androidx.car.app:app:1.7.0`, `app-projected:1.7.0` | Car App Library en Android Auto hostintegratie | AndroidX/Android Open Source Project Apache 2.0-voorwaarden |
| `com.alphacephei:vosk-android:0.3.75` | Offline speech-recognition runtime | Vosk Android distribution; Apache 2.0 code license, notice in `app/src/main/assets/licenses/Vosk-Apache-2.0.txt` |
| `net.java.dev.jna:jna:5.18.1` | Native bridge die Vosk gebruikt | Dual licensed LGPL 2.1-or-later / Apache 2.0; distributie volgt Apache 2.0-optie, terms in `app/src/main/assets/licenses/JNA-License.txt` |
| AndroidX Compose, Lifecycle, Kotlin/Android platform APIs | UI, lifecycle, coroutines en platformfuncties | Licentie-/noticevoorwaarden van de gebruikte AndroidX/Kotlin release behouden |

Een build kan ook transitieve runtime-componenten bevatten. Voor distributie moet de
samengevoegde dependency-/licentiecontrole uit de daadwerkelijke release-build worden
gebruikt; deze bronlijst vervangt geen gegenereerde volledige dependencynotice.

## Nederlands Vosk-model: niet gebundeld en licentie niet geverifieerd

De app bevat geen modelgewichten. Op expliciete gebruikersactie probeert de configuratie
`vosk-model-small-nl-0.22.zip` op te halen via
`https://alphacephei.com/vosk/models/vosk-model-small-nl-0.22.zip`. Downloadhostcontrole,
archiefpad-/groottevalidatie, structuurcontrole en native model-load worden lokaal uitgevoerd.
De berekende SHA-256 wordt lokaal vastgelegd maar niet vergeleken met een vertrouwde
upstream-checksum.

Tijdens dit onderzoek gaf de modelhost HTTP 403; licentiebestand, modelherkomst en modelinhoud
zijn daardoor niet onafhankelijk bevestigd. **Claim dit specifieke Nederlandse model niet
als Apache 2.0 gelicentieerd.** Het model wordt niet in de APK opgenomen of vanuit deze
documentatie herverdeeld. Bevestig licentie en betrouwbare checksum voordat distributie of
productiegebruik als afgerond wordt voorgesteld.

## Alleen onderzocht, niet gebruikt

- Spotify Android SDK / App Remote SDK: onderzocht als mogelijke officiële integratie,
  maar niet toegevoegd, niet gelinkt en geen Spotify developer-appclient/OAuth ingesteld.
  De daadwerkelijke code gebruikt Androids publieke actieve `MediaSession`-interface.
- OpenAI API, realtime audio API, sign-in/subscription APIs, private account endpoints of
  backend: niet toegevoegd. ChatGPT-appactiviteitprobe is ongedocumenteerd en geen officieel
  ChatGPT-integratiecontract.

## Noticedossiers

- MIT: `app/src/main/assets/licenses/BYD-Assistant-MIT.txt`
- Apache 2.0: `app/src/main/assets/licenses/Android-Car-Samples-Apache-2.0.txt`
- Vosk: Apache 2.0 code notice van Alpha Cephei in `app/src/main/assets/licenses/Vosk-Apache-2.0.txt`.
- JNA: dual-licensekeuze en verwijzingen in `app/src/main/assets/licenses/JNA-License.txt`.
