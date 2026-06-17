# LustigeWitze Android

Native Android MVP scaffold for UI and feature parity with the LustigeWitze iOS SwiftUI app.

Implemented in this first slice:

- Jetpack Compose app shell with Bottom Tabs: Feed, Zufall, Rangliste, Profil
- Comic/stitch-inspired design tokens, thick outlines, warm surfaces and compact cards
- Feed cards with score, vote, superlike and favorite controls
- Random-card flow with swipe threshold, action buttons, undo and haptic feedback
- Auth bottom sheet for Login/Register
- Joke detail bottom sheet with comments, share/report affordances and auth gate
- Theme toggle for light/dark mode
- Floating create CTA with composer UI kept behind MVP/backend caveat
- API expectations documented in `docs/API_EXPECTATIONS.md`

Local verification note:

- This workspace currently has no `gradle` executable or Android wrapper, so the Compose build was scaffolded but not compiled here. Open in Android Studio or add a Gradle wrapper to run `./gradlew :app:assembleDebug`.
