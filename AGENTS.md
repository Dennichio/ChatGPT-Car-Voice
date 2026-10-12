# ChatGPT Car Voice — launcher companion

The current authorized architecture is a minimal Android launcher for the official
ChatGPT app (`com.openai.chatgpt`). NO paid OpenAI API, server/backend, WebRTC, API keys,
app-owned speech engine, microphone recording, transcripts, or account authentication.
Use the existing ChatGPT subscription only inside the official ChatGPT app.

Use Android public `PackageManager.getLaunchIntentForPackage`, with narrowly scoped
package visibility. Never hardcode private activities or undocumented Voice deep links,
scrape ChatGPT, reuse cookies/session tokens, intercept accessibility, or bypass car rules.
A launch request is not proof Voice started. The documented Start with Voice setting
works on supported ChatGPT versions only when opening a NEW OR EMPTY conversation.
A generic launch may resume an existing chat; user taps the official Voice icon then.
Only ChatGPT controls its microphone, background conversations, audiofocus and stop.

Keep at most three independent subagents active. Use supported `.codex/agents` roles;
strong models for Android integration/review, lighter models for UI/tests/docs. Verify
actual available model names and use supported equivalents. No nested delegation without
parent approval. Parent owns manifest, Gradle, models and MainActivity; assign other
agents nonoverlapping files. Independent reviewer must not approve own implementation.

Use existing isolated checkout. No worktrees unless explicitly requested. Preserve user
files/changes. Make working commits after tested phases, never push/deploy/publish without
explicit scope. Superseded prototype was archived outside active checkout, not shipped.

Milestone 1: minimal installable APK + launcher and diagnostic tests. Do NOT add tile,
notification, shortcut or hardware-button activation before the requested real-phone
launch/Voice check. Available alternatives belong in research docs until then.
CarConnection is an indication of projection, not a guarantee of car mic/steering/audio.
AudioManager outputs list available devices, not confirmed ChatGPT playback routes.
No Android Auto host UI or MediaSession category workaround.

Cloud: source `/workspace/tooling/cloud-env.sh`. Fresh setup: `bash scripts/cloud-setup.sh`.
Other machines: JDK21, Android SDK37 (compile), API30 AOSP emulator, checked-in Gradle wrapper.
Required build checks: `./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest`.
Connected emulator/device checks: `./gradlew :app:connectedDebugAndroidTest`.
Do not report zero-test runs, mock launch callbacks, or absent-app tests as a real ChatGPT
launch or Voice test. Update `docs/TEST_REPORT.md` with actual counts/limits.
Physical tests follow `docs/VEHICLE_TEST_CHECKLIST.md`; all phone/account/BYD checks remain
unrun until actually performed. Keep UI/docs aligned with final implementation.
