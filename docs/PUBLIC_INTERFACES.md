# Integratiegrenzen en publieke Android-interfaces

De 0.2.0-werkboom behoudt de gewone telefoonlauncher en voegt een echte Android Auto POI-
service, platform-geocoding, offline spraakcommando's en actieve Spotify-mediasessiebediening
toe. Deze interfaces leveren geen generieke ChatGPT-gespreksintegratie. Er wordt geen OpenAI
API, aparte API-facturatie of backend gebruikt.

## Android Auto en navigatie

| Interface | Gebruik | Begrenzing |
|---|---|---|
| AndroidX `CarAppService` / POI-categorie | Echte zoek-/selectieflow via `SearchTemplate`, detailscherm en favorieten | Play/hosttoelating en voertuiggedrag moeten nog worden getest |
| Android `Geocoder` | Best-effort adres-/plaatsresultaten, maximaal vijf | Beschikbaarheid en juistheid verschillen per device/service; geen commerciële nabijheids-POI-database |
| `CarContext.ACTION_NAVIGATE` met `geo:` URI | Geselecteerde coördinaten of vrije plaats-/categoriequery naar de navigatiehost | Route-engine en bestemmingsinterpretatie zijn van de actieve navigatie-app afhankelijk |
| Android `SharedPreferences` | Thuis, werk en door de gebruiker gekozen favorieten | Lokale appopslag; geen account-sync; gegevens zijn niet met een eigen encryptielaag beschermd |
| Android `CarAudioRecord` | PCM-invoer uit de car audio host na de in-car microfoonactie | Vereist RECORD_AUDIO vooraf; echte microfoonroute kan alleen met host/auto worden bevestigd |

Zoeken naar een laadpaal/supermarkt wordt als query naar de navigatie-app overgedragen. Het
wordt niet als lokaal gevonden of bevestigd resultaat voorgesteld. `geo:` handoff laat een
geïnstalleerde navigatie-app de query en route verder afhandelen.

## Lokale spraakherkenning

| Onderdeel | Gebruik | Limiet/status |
|---|---|---|
| Vosk Android library `0.3.75` | Offline herkenning van Nederlandse korte opdrachten | Eenmalig model apart downloaden; geen dialoogmodel of vrije ChatGPT-vragen |
| Model `vosk-model-small-nl-0.22` | Herkenning nadat de gebruiker expliciet download kiest | Niet in APK gebundeld; licentie van modelgewichten niet geverifieerd |
| Android TextToSpeech | Korte navigatie-/Spotify-bevestigingen | Geen volledige ChatGPT-antwoordgeneratie; gekozen TTS-engine en audio-uitgang bepalen de stem |

Modelinstallatie controleert archiefpaden, grenzen, verplichte bestanden en native laden.
De opgeslagen SHA-256 beschrijft ontvangen bytes, maar wordt niet met een vooraf vertrouwde
upstream hash vergeleken. De modelhost gaf bij onderzoek HTTP 403, zodat licentie en exacte
hostinhoud niet bevestigd zijn. Bekijk [derde-partijcode](../THIRD_PARTY_CODE.md) voordat je
modelgewichten distribueert.

## Spotify

De implementatie gebruikt Android `MediaSessionManager` om alleen een actieve sessie met
package `com.spotify.music` te vinden. Android-notificatietoegang is het systeemgrant dat
nodig is om die media-sessie te lezen; de code leest geen notificatie-inhoud. Spotify moet
eerst door de gebruiker zijn gestart.

| Beschikbaar wanneer Spotify het publiceert | Niet aangeboden |
|---|---|
| Huidige tracklabel (titel/artiest) opvragen | Track-, artiest- of playlistsearch |
| Play / hervatten | Spotify-accountkoppeling of OAuth |
| Pause | Spotify-afspeelapparaat kiezen of inventariseren |
| Next / Previous | Starten van een sessie zonder dat de gebruiker Spotify eerst opent |

Een command is alleen verstuurd als de actieve sessie het vereiste transportaction aanbiedt.
Dit bevestigt niet dat Spotify het nummer uiteindelijk afspeelt. Geen Spotify SDK, appclient
credentials of binary zijn opgenomen; zie [licentie-/herkomstnotities](../THIRD_PARTY_CODE.md).

## ChatGPT

| Mogelijkheid | Status |
|---|---|
| Officiële app openen | Android `PackageManager.getLaunchIntentForPackage()` voor `com.openai.chatgpt`; publieke launcheractie |
| OpenAI-account/subscription bridge | Niet beschikbaar in deze app en niet geïmplementeerd |
| Algemene ChatGPT-vragen / vervolgvragen | Niet geïmplementeerd |
| Privé Voice-activity probe | Expliciete opt-in, standaard uit; stuurt `ACTION_ASSIST` naar `com.openai.voice.assistant.AssistantActivity` |
| Stabiel ChatGPT Voice-intentcontract | Niet gevonden; probe is ongedocumenteerd en versieafhankelijk |

De probe controleert of de genoemde component zichtbaar, enabled en exported is en probeert
dan een startverzoek. Android kan dit weigeren. Zelfs wanneer `startActivity` terugkeert,
weet CarVoice niet of ChatGPT luistert, welke conversatie geopend is, of de auto-ingang
gebruikt wordt. Er is geen algemene OpenAI-API, bestaande abonnementsauthenticatie of
gesprekssessie beschikbaar in de launcher.

## Bronnen

- [Android Car App Library](https://developer.android.com/training/cars/apps/library)
- [Android `Geocoder`](https://developer.android.com/reference/android/location/Geocoder)
- [Android `MediaSessionManager`](https://developer.android.com/reference/android/media/session/MediaSessionManager)
- [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- [Android public package-launch intent](https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String))
- [OpenAI Voice FAQ](https://help.openai.com/en/articles/8400625-voice-mode-faq)
- [OpenAI Android FAQ](https://help.openai.com/en/articles/8142208-chatgpt-android-app-faq)
