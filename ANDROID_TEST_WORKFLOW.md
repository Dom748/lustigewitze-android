# LustigeWitze Android – lokaler Test-Workflow

## Ziel
Sobald Paperclip einen ersten Android-Build liefert, kann die App hier direkt im Emulator installiert und getestet werden.

## Voraussetzungen
Bereits lokal vorbereitet:
- Java 21
- Android SDK
- adb
- emulator
- AVD `LustigeWitze_API35`

Env laden:

```bash
source ~/.local/bin/android-env.sh
```

## APK testen

```bash
~/.local/bin/lustigewitze-android-test.sh /absolute/path/to/app.apk
```

Das Script macht:
1. Emulator starten, falls noch keiner läuft
2. auf `adb`-Gerät warten
3. APK installieren
4. optional die App starten, wenn `ANDROID_PACKAGE_NAME` und `ANDROID_ACTIVITY_NAME` gesetzt sind
5. Smoke-Test-Hinweise ausgeben

## Optional: Auto-Launch konfigurieren
Wenn Paketname und Main Activity bekannt sind:

```bash
export ANDROID_PACKAGE_NAME=com.broappstudio.lustigewitze
export ANDROID_ACTIVITY_NAME=.MainActivity
~/.local/bin/lustigewitze-android-test.sh /absolute/path/to/app.apk
```

## Nützliche Kommandos

### Emulator-Status
```bash
source ~/.local/bin/android-env.sh
adb devices -l
```

### Screenshot ziehen
```bash
adb shell screencap -p /sdcard/lustigewitze-home.png
adb pull /sdcard/lustigewitze-home.png .
```

### Logs prüfen
```bash
adb logcat -d | tail -n 200
```

### App neu installieren
```bash
adb uninstall com.broappstudio.lustigewitze || true
adb install -r /absolute/path/to/app.apk
```

## QA-Checkliste für den ersten Build
- App startet ohne Crash
- Login / Register funktioniert
- Feed lädt echte Daten
- Random lädt und swipen funktioniert
- Like / Dislike / Superlike reagieren sofort
- Kommentare / Detailseite öffnen korrekt
- Witz erstellen funktioniert
- Ranking / Profil laden sauber
- Dark Mode / Theme wirkt korrekt
- Kein harter Fehler bei kurzem Netzverlust

## Aktueller Emulator
- Name: `LustigeWitze_API35`
- Android: 15 / API 35
- Typ: Google APIs x86_64
