# Spydy Reminder — Android & Desktop App 🕷️

Spydy Reminder is a Spider-Man themed reminder app featuring full-screen overlay drop animation, custom alerts, sender details, and active reminder tracking across Android & Windows Desktop.

---

## 📱 Android App (.apk) — Native Android Edition

Native Android application with **Display over other apps (Overlay)** permission support, high-priority notifications, audio alerts, and persistence.

### 📥 Download Android APK:
👉 **[Download SpydyReminder.apk](./release/SpydyReminder.apk)**

### 🌟 Key Features:
- 🕷️ **Spider-Man Overlay Drop**: Full-screen overlay alert on top of all Android apps.
- 👤 **Sender Name ("Kiska reminder hai")**: Set custom sender/caller names (e.g. Mom, Boss, Friend).
- 💬 **Reminder Details ("Kya set kiya tha")**: Display message, set time, and countdown.
- 📋 **Active Reminders List**: View all active scheduled reminders directly in the app with one-click Cancel buttons.
- 🔊 **Audio & Vibration Alerts**: Dual `MediaPlayer` + `RingtoneManager` fallback with physical vibration.
- 🔄 **Reboot Auto-Restore**: `ReminderStore` automatically restores all active alarms upon phone reboot.

---

## 💻 Windows Desktop App (Electron)

What you get:
- A control panel window (dark theme, quick presets, custom delay)
- A transparent, always-on-top overlay window with Spider-Man drop animation
- System tray minimization and background scheduling
- Web Audio API sound effects

### 🚀 Running Desktop App
```bash
npm install
npm start
```

---

## 🛠️ Android Build Instructions

```bash
cd android
./gradlew assembleDebug
```
The compiled APK will be generated at `android/app/build/outputs/apk/debug/app-debug.apk`.
