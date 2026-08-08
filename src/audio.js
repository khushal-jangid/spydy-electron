// Recreates the original winsound.Beep() sequences using the Web Audio API.
let sharedCtx = null;
function getCtx() {
  if (!sharedCtx) {
    sharedCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  return sharedCtx;
}

function beep(freq, durationMs, startAt) {
  const ctx = getCtx();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.type = 'square';
  osc.frequency.value = freq;
  gain.gain.value = 0.05;
  osc.connect(gain);
  gain.connect(ctx.destination);
  const start = ctx.currentTime + startAt;
  const dur = durationMs / 1000;
  gain.gain.setValueAtTime(0.06, start);
  gain.gain.exponentialRampToValueAtTime(0.001, start + dur);
  osc.start(start);
  osc.stop(start + dur + 0.01);
}

function playWebSound() {
  try {
    let t = 0;
    for (let freq = 800; freq < 2400; freq += 200) {
      beep(freq, 15, t);
      t += 0.017;
    }
    beep(1800, 80, t);
    t += 0.09;
    beep(2600, 120, t);
  } catch (e) { /* ignore audio errors */ }
}

function playDismissSound() {
  try {
    let t = 0;
    for (let freq = 2000; freq > 600; freq -= 250) {
      beep(freq, 15, t);
      t += 0.017;
    }
  } catch (e) { /* ignore audio errors */ }
}

window.spydyAudio = { playWebSound, playDismissSound };
