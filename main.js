const { app, BrowserWindow, ipcMain, screen, Tray, Menu, nativeImage } = require('electron');
const path = require('path');
const fs = require('fs');

const STORE_PATH = path.join(app.getPath('userData'), 'reminders.json');

let controlWin = null;
let overlayWin = null;
let tray = null;

// In-memory reminder store: { id, message, targetTime (epoch ms) }
let reminders = [];
let tickTimer = null;

function loadReminders() {
  try {
    const raw = fs.readFileSync(STORE_PATH, 'utf-8');
    const parsed = JSON.parse(raw);
    const now = Date.now();
    // Drop anything that already expired while the app was closed
    reminders = (Array.isArray(parsed) ? parsed : []).filter(r => r.targetTime > now);
  } catch (e) {
    reminders = [];
  }
}

function saveReminders() {
  try {
    fs.writeFileSync(STORE_PATH, JSON.stringify(reminders), 'utf-8');
  } catch (e) {
    // Non-fatal: reminders just won't persist across restarts
  }
}

function createControlPanel() {
  controlWin = new BrowserWindow({
    width: 460,
    height: 720,
    minWidth: 420,
    minHeight: 600,
    title: 'Spydy Reminder',
    backgroundColor: '#121212',
    icon: path.join(__dirname, 'assets', 'logo.png'),
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  controlWin.setMenuBarVisibility(false);
  controlWin.loadFile(path.join(__dirname, 'src', 'control-panel.html'));

  controlWin.on('close', (e) => {
    // Minimize to tray instead of quitting, so scheduled reminders keep running
    if (!app.isQuitting) {
      e.preventDefault();
      controlWin.hide();
    }
  });
}

function createOverlayWindow() {
  const primary = screen.getPrimaryDisplay();
  const { width, height } = primary.bounds;

  overlayWin = new BrowserWindow({
    x: primary.bounds.x,
    y: primary.bounds.y,
    width,
    height,
    frame: false,
    transparent: true,
    backgroundColor: '#00000000',
    alwaysOnTop: true,
    skipTaskbar: true,
    resizable: false,
    movable: false,
    minimizable: false,
    maximizable: false,
    focusable: true,
    hasShadow: false,
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  overlayWin.setAlwaysOnTop(true, 'screen-saver');
  overlayWin.setVisibleOnAllWorkspaces(true, { visibleOnFullScreen: true });
  overlayWin.loadFile(path.join(__dirname, 'src', 'overlay.html'));

  overlayWin.on('closed', () => {
    overlayWin = null;
  });
}

function showOverlayWithMessage(message) {
  if (!overlayWin || overlayWin.isDestroyed()) {
    createOverlayWindow();
    overlayWin.once('ready-to-show', () => {
      overlayWin.show();
      overlayWin.webContents.send('play-drop', message);
    });
    return;
  }
  overlayWin.show();
  overlayWin.focus();
  overlayWin.webContents.send('play-drop', message);
}

function broadcastReminders() {
  if (!controlWin || controlWin.isDestroyed()) return;
  const now = Date.now();
  const payload = reminders.map(r => ({
    id: r.id,
    message: r.message,
    secondsLeft: Math.max(0, Math.round((r.targetTime - now) / 1000))
  }));
  controlWin.webContents.send('reminders-updated', payload);
}

function checkDueReminders() {
  const now = Date.now();
  const due = reminders.filter(r => r.targetTime <= now);
  if (due.length) {
    reminders = reminders.filter(r => r.targetTime > now);
    saveReminders();
    // Fire them one at a time, spaced out slightly if multiple land together
    due.forEach((r, idx) => {
      setTimeout(() => showOverlayWithMessage(r.message), idx * 600);
    });
  }
  broadcastReminders();
}

function startTicking() {
  if (tickTimer) clearInterval(tickTimer);
  tickTimer = setInterval(checkDueReminders, 1000);
}

function createTray() {
  const icon = nativeImage.createFromPath(path.join(__dirname, 'assets', 'logo.png'));
  const trayIcon = icon.resize({ width: 16, height: 16 });
  tray = new Tray(trayIcon);
  tray.setToolTip('Spydy Reminder');
  const menu = Menu.buildFromTemplate([
    { label: 'Show Spydy Reminder', click: () => { controlWin.show(); controlWin.focus(); } },
    { label: 'Test Drop Now', click: () => showOverlayWithMessage('Spider-Man Reminder!') },
    { type: 'separator' },
    { label: 'Quit', click: () => { app.isQuitting = true; app.quit(); } }
  ]);
  tray.setContextMenu(menu);
  tray.on('click', () => { controlWin.show(); controlWin.focus(); });
}

app.whenReady().then(() => {
  loadReminders();
  createControlPanel();
  createOverlayWindow();
  createTray();
  startTicking();
  broadcastReminders();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createControlPanel();
    else { controlWin.show(); }
  });
});

app.on('window-all-closed', () => {
  // Keep running in tray on Windows/Linux unless the user explicitly quits
  if (process.platform === 'darwin') return;
});

app.on('before-quit', () => {
  app.isQuitting = true;
});

// ---- IPC handlers ----

ipcMain.handle('add-reminder', (event, { message, seconds }) => {
  const rem = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    message: message || "Time's up!",
    targetTime: Date.now() + seconds * 1000
  };
  reminders.push(rem);
  saveReminders();
  broadcastReminders();
  return { ok: true };
});

ipcMain.handle('cancel-reminder', (event, { id }) => {
  reminders = reminders.filter(r => r.id !== id);
  saveReminders();
  broadcastReminders();
  return { ok: true };
});

ipcMain.handle('trigger-immediate', (event, { message }) => {
  showOverlayWithMessage(message || 'Spider-Man Reminder!');
  return { ok: true };
});

ipcMain.handle('get-reminders', () => {
  const now = Date.now();
  return reminders.map(r => ({
    id: r.id,
    message: r.message,
    secondsLeft: Math.max(0, Math.round((r.targetTime - now) / 1000))
  }));
});

ipcMain.on('overlay-dismissed', () => {
  if (overlayWin && !overlayWin.isDestroyed()) {
    overlayWin.hide();
  }
});
