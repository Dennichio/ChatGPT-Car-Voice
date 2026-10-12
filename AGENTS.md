# ChatGPT Car Voice — incremental Android Auto development

Latest user scope: preserve and extend this project, applicationId be.carvoice.app,
UI and signing identity. Operation FROM ANDROID AUTO is mandatory; a phone launcher
is insufficient. Genuine POI/address search, favorites, navigation handoff and car
voice input are authorized. No misleading category registration or rebuilding from zero.
Work in feature/android-auto-poi-voice; pre-change checkpoint is 610c4f5.

No paid OpenAI API, unauthorized endpoints, session cookies, scraping, Accessibility
automation, firmware changes or backend. A subscription does not automatically grant
third-party model API access. Independent AI speech requires a verified permitted,
cost-compatible processing interface. Do not fake unavailable ChatGPT integration.

The latest user authorizes an EXPERIMENTAL opt-in test of kangrio/Assistant's ChatGPT
Voice component. Respect exported/enabled/permission checks and catch failures; never
bypass restrictions. Undocumented activities/extras are unstable, not supported Voice
contracts. Dispatch is not proof of listening or Android Auto integration. Phone
configuration/probes are not substitutes for normal operation from the car screen.

CarAudioRecord, offline speech recognition, TextToSpeech and mic permissions are now
authorized for actual navigation input. Explicit activation, bounded recording and
lifecycle cleanup are required. No always-on wakeword. Do not use SpeechRecognizer FD
injection with an unverified provider: Android documents possible phone-mic fallback.
Reuse Vosk stream recognition for car PCM if a genuine compatible model is installed.

Reuse selected open source only after license/provenance checks; document adaptations
in THIRD_PARTY_CODE.md with notices. Car screens must implement genuine POI functions.
Unrelated ChatGPT/Spotify actions cannot be disguised as POI functionality. Spotify
integration needs a genuine supported interface and appropriate account/app access;
never fake playback or an account connection.

Use existing agents, maximum three concurrent subagents and no nested delegation.
Strong models for Android/Voice, lighter models for bounded UI/tests/docs. Parent owns
manifest, Gradle, MainActivity/integration. Assign disjoint files; independent reviewers
must not approve their own changes. Preserve user files. The user requests an updated
GitHub repository and APK; publish reviewed task-scoped source and artifacts only.
No credentials or signing private keys in Git. Preserve existing app update identity.

Cloud: source /workspace/tooling/cloud-env.sh. Run meaningful unit/build/lint/device
checks. DHU needs a real Android Auto host; template tests do not prove projection,
car mic or steering controls. Never report mocks, zero tests or absent-ChatGPT tests
as real Voice proof. Document actual tests and outstanding phone/account/BYD/Play work.
Do not declare the whole requested workflow complete before actual validation.
