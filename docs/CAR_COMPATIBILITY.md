# Android Auto en BYD Seal U: compatibiliteitsplan

## Companionfunctie

ChatGPT Car Voice is een launcher-companion voor de officiële ChatGPT-app voor Android (`com.openai.chatgpt`). De companion gebruikt Android `PackageManager.getLaunchIntentForPackage()` om die app te openen. Als ChatGPT niet geïnstalleerd, uitgeschakeld of niet startbaar is, meldt de companion dat. De officiële Play Store-vermelding staat op [Google Play](https://play.google.com/store/apps/details?id=com.openai.chatgpt).

De companion neemt geen audio op en beheert geen ChatGPT-account, gesprek, backend of API-sleutel. Na de start beheert ChatGPT de microfoon, Voice, audiofocus, antwoorden, onderbrekingen en achtergrondgedrag. Android geeft de companion geen belofte dat de launch-intent rechtstreeks Voice activeert of het laatste gesprek vervangt.

## Voice starten en een gesprek hervatten

De officiële ChatGPT Android-app heeft volgens OpenAI de instelling **Instellingen → Spraak → Starten met Voice**. Als dit is ingeschakeld, opent Voice automatisch bij het openen van een **nieuw of leeg gesprek** op ondersteunde mobiele apparaten. Een launcher kan ChatGPT ook naar een eerder geopend, niet-leeg gesprek terugbrengen; dan is automatische Voice-start niet gegarandeerd en kan de gebruiker in ChatGPT op het spraakpictogram tikken.

De companion kan geen dynamische shortcut van ChatGPT aanroepen en kan niet bepalen welk gesprek ChatGPT toont. Als de Android-launcher zelf een ChatGPT-shortcut aanbiedt, kan de gebruiker die daar handmatig bekijken of vastzetten; dit is geen companionfunctie. De gebruiker kan ChatGPT eventueel als standaard digitale assistent instellen in Android als die optie op het toestel wordt aangeboden. Beschikbaarheid van ChatGPT in die keuzelijst, geschiktheid voor de Assistant-rol en werking van een eventuele BYD-stuurknop zijn niet bevestigd. De companion onderschept of omleidt de stuurknop niet.

## Achtergrondgesprekken en audio in de auto

Met **Achtergrondgesprekken** ingeschakeld kan ChatGPT Voice doorgaan wanneer de gebruiker naar een andere app gaat of de telefoon vergrendelt. OpenAI noemt beperkingen: gesprekken eindigen wanneer de gebruiker ze handmatig stopt, de app geforceerd sluit, gebruikslimieten bereikt zijn of de maximale sessieduur wordt bereikt. De companion kan ChatGPT's microfoonstatus niet uitlezen of een actieve opname/Voice-sessie stoppen; beëindig een gesprek in ChatGPT zelf.

De companion biedt geen Android Auto-scherm en is geen Android Auto-media- of navigatie-app. Android Auto-projectie bewijst niet dat ChatGPT op het autoscherm verschijnt, de BYD-microfoon gebruikt of antwoordgeluid via de autospeakers afspeelt. Microfoon, speakerroute, Spotify, navigatiemeldingen, oproepen en audiofocus hangen af van de Android-telefoon, ChatGPT-versie, Android Auto en DiLink; fysieke verificatie blijft nodig.

## BYD- en versiestatus

BYD Seal U bouwjaar 2024, DiLink V2.3.0 en Android Auto 17.3 zijn de door de gebruiker opgegeven testbaseline. Een nieuwere officieel beschikbare Europese BYD-firmware is niet geverifieerd. Er is nog geen test met een echte telefoon/account of in de BYD uitgevoerd; alle launcher-, Voice- en voertuiggedragingen blijven praktisch te controleren.

De officiële Android-referenties en de OpenAI-FAQ's hieronder zijn in deze taak opgehaald met HTTP 200. Eerdere pogingen naar Android-documentatie waren door de proxy geweigerd; die eerdere fout is niet langer de actuele bronstatus. De BYD-firmwarestatus is afzonderlijk niet bevestigd.

## Officiële referenties

- [OpenAI: Voice Mode FAQ](https://help.openai.com/en/articles/8400625-voice-mode-faq)
- [OpenAI: ChatGPT Android-app FAQ](https://help.openai.com/en/articles/8142208-chatgpt-android-app-faq)
- [ChatGPT op Google Play](https://play.google.com/store/apps/details?id=com.openai.chatgpt)
- [Android `PackageManager.getLaunchIntentForPackage`](https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String))
- [Android package visibility](https://developer.android.com/training/package-visibility)
- [Android for Cars: app-typen en templates](https://developer.android.com/training/cars/apps)
- [Android for Cars: media-apps](https://developer.android.com/training/cars/media)
- [AndroidX CarConnection](https://developer.android.com/reference/androidx/car/app/connection/CarConnection)
