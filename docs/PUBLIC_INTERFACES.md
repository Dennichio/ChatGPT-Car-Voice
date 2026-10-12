# Publieke interfaces en ontwikkelstrategie

Onderzoek op 12 oktober 2026. OpenAI- en Android-documentatie hieronder zijn daadwerkelijk
opgehaald. Beschikbaarheid op de specifieke telefoon/ChatGPT-build blijft een fysieke test.

| Mogelijkheid | Bewijs en status | Implementatie |
| --- | --- | --- |
| Officiële app openen | Android publieke PackageManager-launcherinterface; pakket `com.openai.chatgpt` bevestigd via OpenAI Android-FAQ | In de APK; vaste pakketnaam, scoped package visibility, foutafhandeling |
| Rechtstreeks Voice afdwingen | Geen officieel publiek Voice-intent/deeplinkcontract gevonden | Niet geïmplementeerd; geen private activiteiten/URL's |
| Start with Voice | OpenAI FAQ: ondersteunde mobiele versies, **nieuw of leeg gesprek** | Instelling in officiële app; automatische start op telefoon nog niet getest |
| Background conversations | OpenAI FAQ: actief gesprek kan doorgaan in andere apps/op vergrendeld scherm, tot eind-/gebruiks-/sessielimieten | Instelling in officiële app; wrapper kan sessie niet inspecteren of stoppen |
| Eigen startschermsnelkoppeling | Android `ShortcutManager.requestPinShortcut`, bevestiging via ondersteunde launcher | Onderzochte latere uitbreiding; nog niet toegevoegd |
| ChatGPT Voice-shortcuts lezen/starten | `LauncherApps.startShortcut` vereist shortcut-hostrechten (launcher of actieve voice-interaction service) | Gewone companion gebruikt dit niet; gebruiker kan eigen launcher controleren |
| Quick Settings-tegel | `TileService` met `BIND_QUICK_SETTINGS_TILE`, bewuste klik; API34+ immutable PendingIntent-overload | Onderzochte latere uitbreiding; nog niet toegevoegd |
| Notificatie-startknop | Publieke Activity PendingIntent; Android13+ vraagt POST_NOTIFICATIONS | Optioneel later; geen blijvende notificatie/service in eerste APK |
| Standaardassistent | Android biedt assistentrol en standaard-appinstellingen; ChatGPT moet in keuzelijst staan | Gebruiker controleert dit handmatig; app-/toestelgeschiktheid niet geverifieerd |
| Stuurknop/BYD hardware | Geen publieke generieke knopinterceptie voor deze companion bevestigd | Niet geïmplementeerd; geen omzeiling van Android Auto/DiLink |
| Android Auto-scherm | Launcher-companion is geen ondersteunde projectiecategorie | Niet geïmplementeerd; geen misleidende mediacategorisatie |

`RoleManager.createRequestRoleIntent(ROLE_ASSISTANT)` vraagt de rol voor de **aanroepende
app**. Die gebruiken om CarVoice assistent te maken is geen manier om ChatGPT te selecteren.
Controleer desgewenst **Android-instellingen → Apps → Standaard-apps → Digitale assistent**
en kies ChatGPT alleen als Android het aanbiedt. Dit bewijst geen stuurknopkoppeling via
Android Auto. We maken de companion geen launcher of voice-interaction service om
shortcutrechten te verkrijgen.

De eerste mijlpaal blijft beperkt tot openen en diagnostiek. Aanvullende bedieningsmogelijkheden
volgen pas na controle op de echte telefoon, zoals de gebruiker vraagt. Geen betaalde API,
WebRTC, backend, sessiecookies, scraping of Accessibility-automatisering.

## Bronnen

- [OpenAI Voice FAQ](https://help.openai.com/en/articles/8400625-voice-mode-faq)
- [OpenAI Android FAQ](https://help.openai.com/en/articles/8142208-chatgpt-android-app-faq)
- [Officiële Android-app](https://play.google.com/store/apps/details?id=com.openai.chatgpt)
- [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String))
- [Package visibility](https://developer.android.com/training/package-visibility/declaring)
- [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps)
- [ShortcutManager](https://developer.android.com/reference/android/content/pm/ShortcutManager)
- [TileService](https://developer.android.com/reference/android/service/quicksettings/TileService)
- [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager)
- [Android for Cars](https://developer.android.com/training/cars)
