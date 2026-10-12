# Android Auto: POI-route, distributie en resterende onzekerheid

Bijgewerkt op 12 oktober 2026 na toevoeging van een echte bestemmingzoeker aan de bestaande
app. De oude conclusie dat geen enkele officiële Android Auto-categorie paste, is achterhaald:
de app implementeert nu de Points of Interest (POI)-categorie met werkelijk zoeken en een
routehandoff. De oorspronkelijke app, applicationId en Gradle-projectbasis zijn behouden.

## Huidige stand

De broncode registreert een AndroidX Car App Library service met:

- `androidx.car.app.category.POI` en `CarVoiceService`;
- `com.google.android.gms.car.application`-metadata naar `automotive_app_desc.xml` met de
  template descriptor;
- een `HostValidator`: debug accepteert ontwikkelhosts; non-debug gebruikt de AndroidX
  Car App Library 1.7.0 host-certificaatallowlist in `res/values/car_hosts.xml`;
- minimum Android for Cars host API level 5;
- een `SearchTemplate`, resultaatselectie, bestemmingsdetails, favorieten en
  `CarContext.ACTION_NAVIGATE`-handoff.

Deze keuze is inhoudelijk verdedigbaar omdat de module echte adressen/plaatsen laat vinden
en geselecteerde bestemmingen overdraagt. Nabijheidsqueries worden bewust door de actieve
navigatie-app afgehandeld; de app pretendeert geen volledige POI-database of route-engine te
zijn. Zie [compatibiliteit](CAR_COMPATIBILITY.md) voor de exacte scope.

Manifest- en templatecode zijn noodzakelijk, maar garanderen geen zichtbaarheid in Android
Auto. De build is een debug APK en de nieuwe app is nog niet via een vertrouwde Play-testtrack
of Internal App Sharing in de auto getest. Een gebruiker kan Developer Mode en unknown
sources ingeschakeld hebben en de app toch niet in **Launcher aanpassen** zien: sideloading
alleen voldoet niet aan de trusted-sourcevoorwaarden voor Car App Library-apps.

## Hostvalidator en testomgeving

`CarVoiceService` staat in debug-builds alle car hosts toe voor ontwikkelwerk. Een niet-
debug/releasevariant gebruikt de hostallowlist van AndroidX Car App Library. De huidige APK
is een debugbuild; zij is geen Play-goedgekeurde of release-ondertekende distributie.

Er is nog geen Desktop Head Unit (DHU)-hostintegratietest van deze service uitgevoerd.
DHU moet met een telefoon verbonden worden waarop Android Auto draait; alleen de hostcomputer
starten valideert de `CarAppService`-clientflow niet. Ook moet de daadwerkelijke installatie
via een voor Android Auto vertrouwde Play-testroute plaatsvinden om distributie en
launcherzichtbaarheid te beoordelen. Internal Testing vraagt een voor Play Console geschikte
releasebuild, doorgaans een AAB; de debug-APK is niet rechtstreeks een Internal Testing-
release. Internal App Sharing kan een debug-APK accepteren, maar herondertekent die met een
ander certificaat dan de lokale debug-sleutel.

## Besturing vanaf het autoscherm

Het POI-scherm biedt een expliciete zoekknop en Android Auto-zoekveld. De CarAudioRecord-
microfoonactie is afhankelijk van vooraf verleende microfoontoestemming en een door de
gebruiker geïnstalleerd Nederlands offline model. Ze start geen OpenAI-/ChatGPT-
conversatie. De herkende tekst wordt naar de beperkte bestemmings- of Spotify-commando's
gerouteerd. Niet-ondersteunde ChatGPT-vragen leveren een foutmelding op.

Een afzonderlijke, optionele experimentele actie kan `ACTION_ASSIST` naar een
ongedocumenteerde ChatGPT Voice-activiteit sturen. De component kan ontoegankelijk of
afwezig zijn en er bestaat geen publiek contract dat dit in Android Auto een luisterende
ChatGPT-sessie start. De optie blijft standaard uit; dit is geen basis voor een
handsfree ChatGPT-productflow.

De telefoonactie opent ChatGPT met Androids publieke launcherinterface. Dat blijft los van
de POI-functie en maakt ChatGPT zelf niet zichtbaar als een functie op het autoscherm.

## Vereiste verificatie

De volgende controles zijn nog nodig voor een Android Auto-releaseclaim:

1. De exacte `0.2.0` APK en checksum publiceren onder dezelfde package name. Alleen de
   directe debug-APK-update vereist dezelfde debug-ondertekeningssleutel; een Play-track
   gebruikt de Play-ondertekeningsidentiteit en Internal App Sharing herondertekent de APK.
   Plan eventuele lokale dataoverdracht voordat een andere signing key wordt geïnstalleerd.
2. De app via Play Internal Testing of Internal App Sharing aan een toegelaten Play-account
   beschikbaar stellen.
3. De APK op een echte Android Auto-telefoon installeren en de launcher-/POI-flow vanuit de
   host testen.
4. DHU met die telefoon verbinden en templatevalidatie, zoekresultaten, selectie en
   navigatiehandoff controleren.
5. In de BYD Seal U valideren of CarAudioRecord de gewenste microfoon ontvangt en of TTS,
   Spotify, navigatiemeldingen en oproepen correct gedeeld worden.
6. Een volledige route van gesproken bestemming naar werkelijk gestarte navigatie herhalen
   zonder bediening van het telefoonscherm tijdens de rit.

Tot die tests zijn afgerond is de correcte status: **POI-module geïmplementeerd in broncode;
Android Auto-host, vertrouwde distributie en BYD-werking nog niet bevestigd.** De root-
beheerde [teststatus](TEST_REPORT.md) bevat de actuele build- en emulatortellingen.

## Bronnen

- [Android for Cars: categorieën](https://developer.android.com/training/cars)
- [Android Auto-app aanmaken en manifest instellen](https://developer.android.com/training/cars/apps/auto)
- [Car App Library project instellen](https://developer.android.com/training/cars/apps/library/set-up-project)
- [Android for Cars appkwaliteit](https://developer.android.com/docs/quality-guidelines/car-app-quality)
- [Android Auto testen en onbekende bronnen](https://developer.android.com/training/cars/testing)
- [CarContext.startCarApp](https://developer.android.com/reference/androidx/car/app/CarContext#startCarApp(android.content.Intent))
- [Android `Geocoder`](https://developer.android.com/reference/android/location/Geocoder)
- [OpenAI Voice FAQ](https://help.openai.com/en/articles/8400625-voice-mode-faq)
