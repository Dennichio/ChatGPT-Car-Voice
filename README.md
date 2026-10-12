# ChatGPT Car Voice

Een Android-launcher voor de **officiële ChatGPT-app**, bedoeld als eenvoudige startknop
op een telefoon die met een BYD Seal U 2024 verbonden is. De app gebruikt je bestaande
ChatGPT-app/account. **Geen OpenAI API, API-sleutel, backend of extra API-facturatie.**
Het normale gebruik en de limieten van je ChatGPT-abonnement blijven van toepassing.

De eerste mijlpaal bevat een grote **Open ChatGPT**-knop, controle van installatie en
appversie, Android Auto-projectiedetectie en een lijst beschikbare audio-uitgangen.
Android opent de publieke launcheractiviteit van `com.openai.chatgpt`; de companion
neemt geen audio op en beheert geen account of spraaksessie.

## Voice instellen

1. Installeer/open de officiële ChatGPT-app en meld je daar aan met je bestaande account.
2. Open in ChatGPT **Settings → Voice → Start with Voice** (in het Nederlands mogelijk
   **Instellingen → Spraak → Starten met Voice**), als je appversie deze instelling heeft.
3. Open een **nieuw of leeg gesprek**. Alleen in die situatie documenteert OpenAI
   automatische Voice-start. Een gewone appstart kan een bestaand gesprek hervatten;
   tik dan op het Voice-pictogram in ChatGPT.
4. Zet in ChatGPT **Background conversations / Achtergrondgesprekken** aan als een
   actief gesprek moet doorgaan bij andere apps of een vergrendeld scherm.
5. Mute/stop altijd in ChatGPT zelf; de companion kan dit niet op afstand doen.

Deze instellingen zijn bevestigd in de [officiële Voice-FAQ](https://help.openai.com/en/articles/8400625-voice-mode-faq).
Automatische Voice-start op jouw telefoon en de BYD-audioroute moeten fysiek getest worden.

## APK en broncode

[Download de APK via GitHub](https://github.com/Dennichio/ChatGPT-Car-Voice/raw/refs/heads/main/downloads/chatgpt-car-voice-milestone1-debug.apk).
De ondertekende debug-APK staat in [`downloads/`](downloads/), samen met de
[SHA-256-controlesom](downloads/SHA256SUMS). Dit is de eerste ontwikkelbuild;
Voice op een echte telefoon en de BYD-audioroute zijn nog niet getest.
De volledige broncode staat in deze repository en is ook te downloaden via
**Code → Download ZIP**. Zie [installatiehandleiding](docs/INSTALLATION.md).

Android 8.0 / API26 of hoger. Voor ontwikkeling: JDK21, Android SDK37 en de gecontroleerde
Gradle-wrapper 9.8.1. Android Gradle Plugin 9.4.1 gebruikt ingebouwde Kotlin 2.2.10;
Compose BOM 2026.09.00 vereist compile SDK37. Cloudsetup:

```bash
cd /workspace/ChatGPT-Car-Voice
bash scripts/cloud-setup.sh
source /workspace/tooling/cloud-env.sh
./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
```

Met een aangesloten emulator of telefoon:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Er is geen unittest-suite voor deze kleine platformlauncher; de zinvolle controles zijn
Android-instrumentatietests en echte telefoon-/voertuigtests. De UI-callbacktests
simuleren uitsluitend knopacties en bewijzen geen echte ChatGPT- of Voice-start.

## Grenzen en volgende stap

- Geen Android Auto-scherm, voertuigbesturing, stuurknopinterceptie of private Voice-deeplink.
- De detectie is een aanwijzing van projectie; beschikbare audio-uitgangen bewijzen niet
  via welke route ChatGPT opneemt of spreekt.
- Starten vanaf de telefoon is de eerste mijlpaal. Quick Settings, eigen snelkoppelingen
  en eventuele notificatiebediening worden pas toegevoegd **na de eerste praktijktest**.
- De companion activeert geen microfoon bij verbinding met de auto.

Zie [compatibiliteit](docs/CAR_COMPATIBILITY.md), [onderzoek naar startmogelijkheden](docs/PUBLIC_INTERFACES.md),
[testverslag](docs/TEST_REPORT.md), [BYD-testchecklist](docs/VEHICLE_TEST_CHECKLIST.md) en
[multi-agentoverzicht](docs/AGENT_WORK.md).
