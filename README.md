# Spydy Reminder — Windows Desktop App (Electron)

This is a web-tech rebuild of the original Python/Tkinter Spydy Reminder. It's an
**Electron app**, not a browser page — that's the only way to get a real overlay
that drops down on top of *all* your windows (browsers can't do that on their own).

What you get:
- A control panel window (same dark theme, presets, quick timers, custom delay)
- A transparent, always-on-top, click-through-when-hidden overlay window that
  plays the drop animation and shows the reminder speech bubble
- A tray icon (closing the control panel minimizes it to the tray — reminders
  keep running in the background)
- Reminders persist across restarts (saved to a local JSON file)
- Web-shoot / dismiss sound effects re-created with the Web Audio API (no
  `winsound` dependency, so this also runs on macOS/Linux for development)

## Requirements

- [Node.js](https://nodejs.org) 18+ (includes npm) installed on the Windows machine

## Run it in development mode

```
npm install
npm start
```

Or just double-click `start_dev.bat` on Windows — it does both steps for you.

## Build a real Windows installer / .exe

```
npm install
npm run dist
```

This uses `electron-builder` and produces:
- `dist\Spydy Reminder Setup <version>.exe` — a proper installer (adds Start
  Menu + Desktop shortcuts)

(`npm run pack` builds an unpacked folder instead, useful for quick testing.)

## Project layout

```
main.js              Electron main process: windows, tray, reminder scheduling
preload.js            Safe IPC bridge exposed to the renderer as window.spydy
src/
  control-panel.html/css/js   The main window UI
  overlay.html/css/js         The fullscreen transparent drop overlay
  audio.js                    Web Audio re-creation of the original beep sounds
assets/
  logo.png             App / tray icon
  spiderman.png         Character art used in the drop animation
```

## Notes

- The overlay only intercepts clicks while a reminder is actively showing —
  the rest of the time it's hidden, so it never blocks your other apps.
- Multi-monitor: the overlay currently drops on your **primary** display,
  same as the original.
- If Windows SmartScreen flags the unsigned .exe on first run, that's normal
  for an app that isn't code-signed — click "More info" → "Run anyway".
