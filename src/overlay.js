const webLine = document.getElementById('webLine');
const spidey = document.getElementById('spidey');
const bubble = document.getElementById('bubble');
const bubbleTime = document.getElementById('bubbleTime');
const bubbleMessage = document.getElementById('bubbleMessage');

let spideyW = 220;
let spideyH = 220; // recalculated once the image loads
let spideyX = 0;
let targetY = 40;
let startY = -340;
let animState = 'idle'; // idle | dropping | shown | retracting
let animStart = 0;
const DROP_MS = 580;
const RETRACT_MS = 380;

function layoutConstants() {
  const screenW = window.innerWidth;
  spideyX = screenW - 320;
  startY = -spideyH - 100;
  spidey.style.width = spideyW + 'px';
}

function setFrame(y) {
  spidey.style.left = spideyX + 'px';
  spidey.style.top = y + 'px';

  const webX = spideyX + spideyW * 0.52;
  const webEndY = y + spideyH * 0.15;
  webLine.style.left = webX + 'px';
  webLine.style.height = Math.max(0, webEndY) + 'px';

  const bubbleX = spideyX - 340;
  const bubbleY = y + 120;
  bubble.style.left = bubbleX + 'px';
  bubble.style.top = bubbleY + 'px';

  if (bubbleX > 20 && y > -50) {
    bubble.classList.add('visible');
  } else {
    bubble.classList.remove('visible');
  }
}

function tick(now) {
  if (animState === 'dropping') {
    const progress = (now - animStart) / DROP_MS;
    const p = Math.min(1, progress);
    const ease = 1 - Math.pow(1 - p, 3);
    let bounce = 0;
    if (p > 0.85) {
      bounce = Math.sin(((p - 0.85) / 0.15) * Math.PI) * 12;
    }
    const y = startY + (targetY - startY) * ease + bounce;
    setFrame(y);
    if (progress >= 1) {
      animState = 'shown';
      setFrame(targetY);
      return;
    }
    requestAnimationFrame(tick);
  } else if (animState === 'retracting') {
    const progress = (now - animStart) / RETRACT_MS;
    const p = Math.min(1, progress);
    const ease = Math.pow(p, 2); // ease-in, snappy retract
    const y = targetY + (startY - targetY) * ease;
    setFrame(y);
    if (progress >= 1) {
      animState = 'idle';
      window.spydy.dismissOverlay();
      return;
    }
    requestAnimationFrame(tick);
  }
}

function startDrop(message) {
  bubbleMessage.textContent = message;
  bubbleTime.textContent = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  layoutConstants();
  animState = 'dropping';
  animStart = performance.now();
  window.spydyAudio.playWebSound();
  requestAnimationFrame(tick);
}

function retractAndClose() {
  if (animState === 'retracting' || animState === 'idle') return;
  animState = 'retracting';
  animStart = performance.now();
  window.spydyAudio.playDismissSound();
  requestAnimationFrame(tick);
}

if (spidey.complete) {
  spideyH = spidey.naturalHeight ? Math.round(spideyW * (spidey.naturalHeight / spidey.naturalWidth)) : 220;
} else {
  spidey.addEventListener('load', () => {
    spideyH = Math.round(spideyW * (spidey.naturalHeight / spidey.naturalWidth));
  });
}

document.body.addEventListener('click', retractAndClose);
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') retractAndClose();
});

window.spydy.onPlayDrop((message) => startDrop(message));
