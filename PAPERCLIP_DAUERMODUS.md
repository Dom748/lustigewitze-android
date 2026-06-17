# Paperclip Dauermodus – Android Watchdog

## Ziel
Verhindern, dass Paperclip wieder stillsteht, obwohl Android-Tickets offen sind.

## Mechanik
Es gibt jetzt einen autonomen Hermes-Watchdog, der regelmäßig den lokalen Paperclip-Status prüft und bei Android-Stalls nachfasst.

## Bestandteile

### 1. Android-spezifischer Snapshot-Script
Pfad:
```bash
/home/ai/.hermes/scripts/paperclip_watchdog.py
```

Das Script liest:
- `/api/health`
- Company Dashboard
- Agents
- Projekte
- Android-Issues

Es erkennt u.a.:
- alle Agents idle / keiner running
- Android-Projekt offen, aber blockiert
- `needs_attention`-Blocker
- Statusänderungen seit dem letzten Lauf

### 2. Android-Cron-Watchdog
Job-Name:
```text
paperclip-watchdog-android
```

Intervall:
```text
every 15m
```

### 3. Globaler Snapshot-Script
Pfad:
```bash
/home/ai/.hermes/scripts/paperclip_global_watchdog.py
```

Der globale Guard schaut nicht nur auf Android, sondern auf **alle aktiven/planned/in_progress Projekte** in Paperclip.

Er erkennt u.a.:
- offene Projekte ohne laufende Agents
- Projekte mit vielen `needs_attention`-Blockern
- Stalls in anderen aktiven Tracks wie Web, iOS oder Crypto
- Statusänderungen projektübergreifend

### 4. Globaler Cron-Watchdog
Job-Name:
```text
paperclip-watchdog-global
```

Intervall:
```text
every 30m
```

## Verhalten des Watchdogs
Pro Lauf:
1. Snapshot lesen
2. LustigeWitze Android prüfen
3. bei klaren Stalls / stale Blockern passende Operator-Kommentare posten
4. sinnvolle Statusänderungen zurück in den Chat melden
5. bei keinem relevanten Fortschritt still bzw. sehr kurz bleiben

## Fokus
Der Watchdog kümmert sich primär um:
- Android-Blocker
- fehlende Handoffs
- idle Agents trotz offener High-Priority-Issues
- QA-/Delivery-Stalls

## Wichtige IDs
- Company: `ae5d9f73-00ba-4327-97d7-b34325ca8dc3`
- Android Project: `7584365c-df7c-4b78-9b98-ed206a8b63de`

## Später anpassbar
Wenn nötig, kann ich erweitern auf:
- alle aktiven Projekte
- härtere Eskalationslogik
- tägliche Management-Zusammenfassung
- dedizierte Watchdogs für Web / iOS / Crypto
