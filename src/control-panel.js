const msgInput = document.getElementById('msgInput');
const presetRow = document.getElementById('presetRow');
const quickRow = document.getElementById('quickRow');
const customMin = document.getElementById('customMin');
const setCustomBtn = document.getElementById('setCustomBtn');
const testDropBtn = document.getElementById('testDropBtn');
const remList = document.getElementById('remList');

const PRESETS = [
  ['💧 Drink Water', 'Time to drink water! 💧'],
  ['☕ Take a Break', 'Time to stretch and rest your eyes! ☕'],
  ['🧍 Posture Check', 'Check your posture! Sit straight 🧍'],
  ['📝 Task Check', 'Time to check your task list! 📝']
];

const TIME_PRESETS = [
  ['⚡ Test 5s', 5],
  ['1 Min', 60],
  ['5 Min', 300],
  ['15 Min', 900],
  ['30 Min', 1800],
  ['1 Hour', 3600]
];

PRESETS.forEach(([label, value]) => {
  const b = document.createElement('button');
  b.className = 'preset-btn';
  b.textContent = label;
  b.addEventListener('click', () => { msgInput.value = value; });
  presetRow.appendChild(b);
});

TIME_PRESETS.forEach(([label, secs]) => {
  const b = document.createElement('button');
  b.className = 'quick-btn' + (secs === 5 ? ' primary' : '');
  b.textContent = label;
  b.addEventListener('click', () => addQuickReminder(secs));
  quickRow.appendChild(b);
});

async function addQuickReminder(seconds) {
  const msg = msgInput.value.trim() || "Time's up!";
  await window.spydy.addReminder(msg, seconds);
  window.spydyAudio.playWebSound();
}

setCustomBtn.addEventListener('click', async () => {
  const mins = parseFloat(customMin.value);
  if (!mins || mins <= 0) {
    alert('Please enter a valid positive number for minutes.');
    return;
  }
  await addQuickReminder(Math.round(mins * 60));
});

testDropBtn.addEventListener('click', async () => {
  const msg = msgInput.value.trim() || 'Spider-Man Reminder!';
  await window.spydy.triggerImmediate(msg);
});

function formatTime(secsLeft) {
  const h = Math.floor(secsLeft / 3600);
  const m = Math.floor((secsLeft % 3600) / 60);
  const s = secsLeft % 60;
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function renderReminders(reminders) {
  remList.innerHTML = '';
  if (!reminders.length) {
    const p = document.createElement('p');
    p.className = 'empty-state';
    p.textContent = 'No active reminders scheduled.';
    remList.appendChild(p);
    return;
  }
  reminders.forEach(r => {
    const row = document.createElement('div');
    row.className = 'rem-item';

    const text = document.createElement('span');
    text.className = 'rem-text';
    text.innerHTML = `<span class="rem-time">[${formatTime(r.secondsLeft)}]</span> ${escapeHtml(r.message)}`;

    const del = document.createElement('button');
    del.textContent = '❌';
    del.addEventListener('click', async () => {
      await window.spydy.cancelReminder(r.id);
    });

    row.appendChild(text);
    row.appendChild(del);
    remList.appendChild(row);
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

window.spydy.onRemindersUpdated(renderReminders);
window.spydy.getReminders().then(renderReminders);
