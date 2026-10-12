# Android Auto: oorzaak en haalbaarheid

Onderzocht op 12 oktober 2026. De gebruiker meldt dat de APK geïnstalleerd is, maar niet
in **Launcher aanpassen** verschijnt, ondanks ontwikkelaarsopties en onbekende bronnen.
De gebruiker vereist bediening vanuit Android Auto en heeft de smartphonefallback
expliciet geschrapt. De huidige APK voldoet niet aan die eis.

## Conclusie

De huidige telefoonlauncher kan niet als ondersteunde Android Auto-app worden aangeboden
door alleen manifestregels toe te voegen. Voor de beschreven functie is geen passende
officiële Android Auto-appcategorie gevonden. Er is ook geen geverifieerd publiek
Android Auto/OpenAI-contract gevonden waarmee deze companion ChatGPT Voice op de
telefoon kan starten of bedienen. Daarom is geen appcode gewijzigd en geen nieuwe APK
gebouwd. Telefoonverbeteringen, een namaak-media-app en omzeilingen worden niet ontwikkeld.

Een eventuele opdracht via de ingebouwde auto-assistent blijft een afzonderlijke,
onbevestigde toesteltest. Dit onderzoek bewijst niet dat iedere combinatie van telefoon,
assistent en auto zo'n opdracht technisch weigert. Het biedt wel onvoldoende bewijs om
een werkende Android Auto-integratie te beloven of daarop een nieuwe APK te bouwen.

## Waarom de APK ontbreekt

Het [bronmanifest](../app/src/main/AndroidManifest.xml) bevat één geëxporteerde
`MainActivity` met `android.intent.action.MAIN` en `android.intent.category.LAUNCHER`.
Dat registreert een **telefoon**-app. `<queries>` maakt uitsluitend de officiële
ChatGPT-app (`com.openai.chatgpt`) zichtbaar voor de pakketcontrole.

De code heeft `androidx.car.app:app:1.7.0` als dependency en observeert
`CarConnection.type`. Dat is verbindingsdetectie, geen registratie voor het
Android Auto-scherm. Er is geen `CarAppService`, automotive-appmetadata,
media-browse-service, messaging-notificatie-integratie of parked-appregistratie.

Voor een templated Android Auto-app documenteert Google onder andere
`com.google.android.gms.car.application`, een `automotive_app_desc` met
`<uses name="template"/>`, en een geëxporteerde `CarAppService` met de juiste
intentactie en een passende categorie. De functionaliteit moet werkelijk aan de
categoriecriteria voldoen. Een registratie zonder zulke functionaliteit is geen oplossing.

Google zegt over onbekende bronnen letterlijk:

> This setting applies to media, messaging notifications, and parked apps but doesn't
> apply to apps built using the Android for Cars App Library.

De instelling betreft de installatiebron van toepasselijke auto-apps. Ze verandert een
willekeurige Android-activiteit niet in een Android Auto-app. Android-ontwikkelaarsopties
en Android Auto-ontwikkelaarsmodus zijn bovendien afzonderlijke instellingen.

## Officiële categorieën

| Categorie | Android Auto | Geschikt voor deze ChatGPT-startknop? |
| --- | --- | --- |
| Media/audio | Ja; templated media heeft aanvullende Early Access-/testtrackvoorwaarden | Nee: de companion levert geen eigen media-aanbod of mediasessie |
| Messaging via notificaties | Ja | Nee: geen eigen berichten/reply-integratie; geen publieke ChatGPT-berichteninterface gevonden |
| Templated messaging | Labs; interne/gesloten testtracks | Nee: geen eigen gesprekshistorie of ondersteunde verzendinterface |
| Calling | Labs; interne/gesloten testtracks | Nee: geen beldienst of Telecom-integratie; ChatGPT Voice openen levert die niet |
| Navigatie | Ja | Nee: geen routebegeleiding |
| Points of Interest | Ja | Nee: geen locatie-/parkeer-/laadfunctionaliteit |
| Internet of Things | Ja | Nee: geen bediening van aangesloten apparaten; een andere telefoonapp openen is geen dergelijke functie |
| Weer | Ja | Nee: geen weerapp |
| Games | Alleen geparkeerd | Nee: geen game en geen handsfree Voice-integratie |
| Video en browsers | Android Automotive OS, niet Android Auto in de geraadpleegde tabel | Nee; Android Automotive OS is niet de telefoonprojectie Android Auto |

Er staat geen algemene launcher- of AI-assistentcategorie in de officiële categorietabel.
Categorie, templates, kwaliteitseisen en distributievoorwaarden horen samen; alleen een
manifestcategorie kiezen maakt de beoogde functie niet ondersteund.
De messaging-kwaliteitseis MF-5 zegt bijvoorbeeld: **The app must implement a
peer-to-peer messaging service**. Een startknop voor een externe AI-app levert dat niet.

## Publieke startinterfaces en assistenten

`CarContext.startCarApp()` start een app **op het autoscherm**. De gedocumenteerde
intents betreffen navigatie, telefoongesprekken of de eigen `CarAppService`. Voor een
expliciet andere component documenteert Android een `SecurityException`. Het is dus
geen universele startinterface voor `com.openai.chatgpt`.

Dit is geen algemene uitspraak dat Android nooit een telefoonactiviteit kan starten.
Een gewone telefoon-`startActivity()` registreert echter geen geschikte auto-app en
levert geen gedocumenteerde ChatGPT-bediening op het autoscherm.

| Mogelijke route | Vastgesteld | Beperking |
| --- | --- | --- |
| Eigen Android Auto-appknop → ChatGPT | Geen passende categorie/publieke Voice-interface gevonden | Niet implementeren via een oneigenlijke media-, communicatie- of IoT-categorie |
| App Actions | Android-documentatie zegt dat gebruikers App Actions alleen op Android-telefoons kunnen gebruiken | Geen bewijs van een Android Auto-route voor deze APK |
| Standaard digitale assistent | Android heeft `ROLE_ASSISTANT`, `ACTION_ASSIST` en `VoiceInteractionService` | Niet geverifieerd of de geïnstalleerde ChatGPT-versie die rol aanbiedt; de telefoonkeuze bewijst geen overname door Android Auto of de BYD-stuurknop |
| Android Auto Assistant action / aangepaste assistentopdracht | Alleen als de functie op het toestel wordt aangeboden een kandidaat voor een praktijktest | De Google Help-bronnen waren met HTTP 403 geblokkeerd; geen geverifieerde garantie dat een willekeurige telefoonapp hiermee start |
| Stuurknop | Werking via de aanwezige auto-assistent kan fysiek worden gecontroleerd | Geen publieke generieke onderscheppings-/omleidingsinterface gevonden voor deze companion |
| Start with Voice | OpenAI documenteert automatische Voice-start op ondersteunde mobiele versies bij een nieuw/leeg gesprek | Een gewone appstart kan een bestaand gesprek hervatten; geen algemene directe Voice-startgarantie |
| Background conversations | Een al gestart Voice-gesprek kan doorgaan in andere apps/op vergrendeld scherm | Start geen nieuw gesprek vanuit Android Auto en registreert geen auto-app |

De Android for Cars-pagina noemt spraakstart van apps die **in het voertuig** zijn
geïnstalleerd. Dat is geen bewijs dat Android Auto willekeurige **telefoon**-apps kan
openen. De algemene Assistant-pagina beschrijft `Hey Google, open AppName` voor op het
toestel aanwezige Play Store-apps; deze verklaring bewijst evenmin de Android Auto-route.

## Gerichte praktijktest vóór verdere ontwikkeling

Deze test is nog niet uitgevoerd. Test geparkeerd, met Android Auto daadwerkelijk
verbonden. Start de assistent via het autoscherm of de normale stuurknop en zeg
**Open ChatGPT**. Als Android Auto zelf een **Assistant action** onder
**Launcher aanpassen → Snelkoppeling toevoegen** aanbiedt, kan dezelfde opdracht daar
worden geprobeerd. De naam en beschikbaarheid van die optie kunnen verschillen.

Noteer afzonderlijk:

1. Reageert de assistent vanuit Android Auto, en welke assistent is dat?
2. Wordt ChatGPT op de telefoon daadwerkelijk geopend, of wordt de opdracht geweigerd?
3. Start Voice zonder telefoonaanraking, met Start with Voice en een nieuw/leeg gesprek?
4. Werken de microfoon en autospeakers tijdens het gesprek?
5. Kan het gesprek vanuit Android Auto worden beëindigd of opnieuw gestart?

Alleen geluid horen of een gesprek vooraf op de telefoon starten haalt de gevraagde
Android Auto-startvoorwaarde niet. Een eventueel geslaagde opdracht opent rechtstreeks
de officiële ChatGPT-app; ze bewijst niet dat onze APK een Android Auto-app is geworden.
Tot deze route aangetoond is, wordt ze als onbevestigd behandeld.

## Bronnen en onderzoeksgrenzen

Deze primaire Android-bronnen zijn tijdens dit onderzoek met HTTP 200 opgehaald:

- [Android for Cars: officiële categorietabel](https://developer.android.com/training/cars)
- [Android Auto toevoegen: registratie en descriptor](https://developer.android.com/training/cars/apps/auto)
- [Car App Library: project en categorie instellen](https://developer.android.com/training/cars/apps/library/set-up-project)
- [Appkwaliteit voor auto's: onder andere messaging-eis MF-5](https://developer.android.com/docs/quality-guidelines/car-app-quality)
- [Testen: onbekende bronnen](https://developer.android.com/training/cars/testing#allow-unknown-sources)
- [CarContext: startCarApp](https://developer.android.com/reference/androidx/car/app/CarContext#startCarApp(android.content.Intent))
- [Assistant en App Actions](https://developer.android.com/develop/devices/assistant/overview)
- [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager)
- [VoiceInteractionService](https://developer.android.com/reference/android/service/voice/VoiceInteractionService)

De researchagent raadpleegde ook [OpenAI ChatGPT Voice](https://help.openai.com/en/articles/20001274-chatgpt-voice).
Dit biedt geen gevonden publiek Android Auto-startcontract. De netwerkproxy weigerde
Google Help (`support.google.com`); dit is een onderzoeksbeperking en geen bewijs dat
een assistentopdracht op het toestel nooit werkt. Er is geen netwerkbeleid omzeild.
Geen ontwikkelaarstest met echte Android Auto, BYD, ChatGPT-account of Voice uitgevoerd.
De melding over de ontbrekende launcher-app is een gebruikerswaarneming.
