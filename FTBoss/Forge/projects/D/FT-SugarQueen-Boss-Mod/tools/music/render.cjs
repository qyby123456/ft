/*
 * 茶树Boss 战斗BGM — 原创，风格参照《沙耶之歌 / The Song of Saya》
 * 纯 JS 离线合成 (Node)，输出 16bit 44.1kHz 立体声 WAV。
 * 编制: 低频弦乐/管风琴 drone + 古筝拨弦主题 + 大提琴声部 + 玻璃铃 + 气声人声 + 心跳/陶锣打击 + 茶壶哨音
 * 调性: a小调 (含 ♭II Bb 与 G# 的暗色和声), 63 BPM, 4/4, 50 小节 ≈ 3:10
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const BPM = 63;
const BEAT = 60 / BPM;
const BAR = BEAT * 4;
const BARS = 42;
const TAIL = 7;                       // 混响尾巴
const TOTAL = BARS * BAR + TAIL;
const N = Math.ceil(TOTAL * SR);

const rnd = (() => { let a = 20260910; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();

const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);
const tBeat = (bar, beat) => (bar * 4 + beat) * BEAT;   // 小节+拍 -> 秒
const S = t => Math.round(t * SR);

// ---------------- 总线 ----------------
const musL = new Float32Array(N), musR = new Float32Array(N);
const perL = new Float32Array(N), perR = new Float32Array(N);

function panGains(p) {
  const a = (Math.max(-1, Math.min(1, p)) + 1) * Math.PI / 4;
  return [Math.cos(a), Math.sin(a)];
}
function put(bus, i, v) { if (i >= 0 && i < N) bus[i] += v; }

// ---------------- 包络 ----------------
function adsr(t, dur, a, d, s, r) {
  if (t < 0 || t > dur + r) return 0;
  let amp;
  if (t < a) amp = t / a;
  else if (t < a + d) amp = 1 + (s - 1) * (t - a) / d;
  else amp = s;
  if (t > dur) amp *= Math.max(0, 1 - (t - dur) / r);
  return Math.max(0, amp);
}

// ---------------- 波表加法合成 ----------------
function makeTable(harm) {
  const SIZE = 4096, t = new Float32Array(SIZE);
  let mx = 0;
  for (let i = 0; i < SIZE; i++) {
    let v = 0;
    for (let h = 0; h < harm.length; h++) v += harm[h] * Math.sin(2 * Math.PI * (h + 1) * i / SIZE);
    t[i] = v; if (Math.abs(v) > mx) mx = Math.abs(v);
  }
  for (let i = 0; i < SIZE; i++) t[i] /= mx;
  return t;
}
const sawH = (n, tilt) => { const a = []; for (let i = 1; i <= n; i++) a.push((1 / i) * Math.exp(-i / tilt)); return a; };

const T_ORGAN  = makeTable([1, 0.34, 0.18, 0.09, 0.05, 0.03, 0.015]);
const T_STR_LO = makeTable(sawH(24, 9));
const T_STR_MD = makeTable(sawH(28, 14));
const T_AIR    = makeTable([1, 0.06, 0.03, 0.012]);
const T_ZITHER = makeTable([1, 0.42, 0.22, 0.13, 0.07, 0.04]);

const SIZE = 4096;
function tableVoice(bl, br, pan, start, table, freq, amp, atk, hold, rel, opt) {
  opt = opt || {};
  const det = opt.detune || 0, vibHz = opt.vibHz || 0, vibC = opt.vibCents || 0;
  const [pl, pr] = panGains(pan);
  const total = Math.ceil((atk + hold + rel) * SR);
  const inc0 = midiHz(freq) * Math.pow(2, det / 1200) / SR * SIZE;
  let phase = rnd() * SIZE;
  const startI = S(start) + (opt.delay ? S(opt.delay) : 0);
  const tw = 1 / Math.max(1, vibHz * SR);
  for (let i = 0; i < total; i++) {
    const t = i / SR;
    const a = adsr(t, atk + hold, atk, 0.08, 1, rel);
    if (a <= 0) continue;
    const inc = vibC ? inc0 * Math.pow(2, vibC * Math.sin(2 * Math.PI * vibHz * t) / 1200) : inc0;
    const i0 = phase | 0, fr = phase - i0;
    const s0 = table[i0], s1 = table[(i0 + 1) & (SIZE - 1)];
    const s = (s0 + (s1 - s0) * fr) * a * amp;
    phase += inc; if (phase >= SIZE) phase -= SIZE;
    const j = startI + i;
    put(bl, j, s * pl); put(br, j, s * pr);
  }
}

// ---------------- 古筝拨弦 (Karplus-Strong) ----------------
function pluck(bl, br, pan, start, freq, amp, decaySec, bright) {
  const f = midiHz(freq);
  const L = Math.max(16, Math.round(SR / f));
  const line = new Float32Array(L);
  let lp = 0;
  for (let i = 0; i < L; i++) { const w = rnd() * 2 - 1; lp += bright * (w - lp); line[i] = lp; }
  let mean = 0; for (let i = 0; i < L; i++) mean += line[i]; mean /= L;
  for (let i = 0; i < L; i++) line[i] -= mean;
  const damp = Math.pow(0.0016, 1 / Math.max(1, decaySec * f));
  const [pl, pr] = panGains(pan);
  const len = Math.floor(decaySec * SR);
  const startI = S(start);
  let idx = 0;
  const relStart = len - Math.floor(0.35 * SR);
  for (let i = 0; i < len; i++) {
    const cur = line[idx], nxt = line[(idx + 1) % L];
    line[idx] = (cur + nxt) * 0.5 * damp;
    idx = (idx + 1) % L;
    let env = 1;
    if (i > relStart) env = Math.max(0, 1 - (i - relStart) / (len - relStart));
    const s = cur * amp * env;
    const j = startI + i;
    put(bl, j, s * pl); put(br, j, s * pr);
  }
}

// ---------------- FM 玻璃铃 ----------------
function fmBell(bl, br, pan, start, noteMidi, amp, dur, ratio, index, detune) {
  const fc = midiHz(noteMidi) * Math.pow(2, (detune || 0) / 1200);
  const [pl, pr] = panGains(pan);
  const len = Math.floor((dur + 1.2) * SR);
  const startI = S(start);
  const md = Math.max(0.05, dur * 0.22);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const e = Math.exp(-t / md);
    const a = (1 - Math.exp(-t / 0.004)) * e;
    if (a < 1e-5) break;
    const mod = Math.sin(2 * Math.PI * fc * ratio * t) * index * e;
    const s = Math.sin(2 * Math.PI * fc * t + mod) * a * amp;
    const j = startI + i;
    put(bl, j, s * pl); put(br, j, s * pr);
  }
}

// ---------------- 气声人声 ----------------
function breath(bl, br, pan, start, noteMidi, amp, dur, formants) {
  const f = midiHz(noteMidi);
  const [pl, pr] = panGains(pan);
  const len = Math.floor((dur + 1.5) * SR);
  const startI = S(start);
  const F = formants || [520, 1180, 2600];
  const fs = F.map(fc => { const w = 2 * Math.PI * fc / SR; const c = Math.cos(w); const al = Math.sin(w) / (2 * 1.4); const a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; });
  const st = fs.map(() => ({ z1: 0, z2: 0 }));
  const fs2 = F.map(fc => { const w = 2 * Math.PI * fc / SR; const c = Math.cos(w); const al = Math.sin(w) / (2 * 1.4); const a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; });
  const st2 = fs2.map(() => ({ z1: 0, z2: 0 }));
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, dur, Math.min(1.4, dur * 0.35), 0.4, 0.85, 1.4) * (0.75 + 0.25 * Math.sin(2 * Math.PI * 4.7 * t));
    if (a <= 0) continue;
    const w = (rnd() * 2 - 1) * 0.6 + Math.sin(2 * Math.PI * f * (1 + 0.004 * Math.sin(2 * Math.PI * 5.1 * t))) * 0.25;
    let accL = 0, accR = 0;
    for (let k = 0; k < 3; k++) {
      const g = k === 0 ? 1 : k === 1 ? 0.55 : 0.3;
      let s = w;
      let o = fs[k].b0 * s + st[k].z1;
      st[k].z1 = fs[k].b2 * s - fs[k].a1 * o - fs[k].a2 * st[k].z2;
      st[k].z2 = o;
      accL += o * g;
      let s2 = w * 0.92;
      let o2 = fs2[k].b0 * s2 + st2[k].z1;
      st2[k].z1 = fs2[k].b2 * s2 - fs2[k].a1 * o2 - fs2[k].a2 * st2[k].z2;
      st2[k].z2 = o2;
      accR += o2 * g * (0.9 + 0.1 * k);
    }
    const j = startI + i;
    put(bl, j, accL * a * amp * pl * 6);
    put(br, j, accR * a * amp * pr * 6);
  }
}

// ---------------- 打击 ----------------
function thump(bl, br, pan, start, amp, f0, f1, dur, noise) {
  const [pl, pr] = panGains(pan);
  const len = Math.floor(dur * SR);
  const startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f1 + (f0 - f1) * Math.exp(-t / (dur * 0.10));
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / (dur * 0.26)) * (1 - Math.exp(-t / 0.004));
    let s = Math.sin(ph) * e;
    if (noise) { const w = rnd() * 2 - 1; lp += 0.18 * (w - lp); s += lp * e * noise; }
    const j = startI + i;
    put(bl, j, s * amp * pl); put(br, j, s * amp * pr);
  }
}
function heartbeat(bl, br, start, amp, pan) {
  const p = pan || 0;
  thump(bl, br, p, start, amp, 74, 40, 0.55, 0.20);
  thump(bl, br, p, start + 0.33, amp * 0.62, 66, 34, 0.45, 0.16);
}
function metalHit(bl, br, pan, start, amp, base, dur) {
  const ratios = [1, 1.82, 2.41, 3.17, 4.33, 5.71, 7.1];
  const [pl, pr] = panGains(pan);
  const len = Math.floor((dur + 0.6) * SR);
  const startI = S(start);
  const st = ratios.map(() => ({ ph: 0 }));
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    if (t > dur + 0.5) break;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      const f = base * ratios[k];
      const dec = dur / (1 + k * 0.55);
      const a = Math.exp(-t / (dec * 0.30)) * (1 - Math.exp(-t / 0.002)) / (1 + k * 0.9);
      st[k].ph += 2 * Math.PI * f / SR;
      s += Math.sin(st[k].ph) * a;
    }
    const w = rnd() * 2 - 1; lp += 0.35 * (w - lp);
    s += lp * Math.exp(-t / 0.006) * 0.5;
    const j = startI + i;
    put(bl, j, s * amp * pl); put(br, j, s * amp * pr);
  }
}
function drum(bl, br, start, amp, f0, f1, dur) {
  thump(bl, br, 0, start, amp, f0, f1, dur, 0.55);
  const len = Math.floor(0.06 * SR), startI = S(start);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const w = rnd() * 2 - 1; lp += 0.3 * (w - lp);
    const e = Math.exp(-i / (0.02 * SR));
    put(bl, startI + i, lp * e * amp * 0.5); put(br, startI + i, lp * e * amp * 0.5);
  }
}
// 噪声+哨音上升 (茶壶哨)
function riser(bl, br, start, dur, amp, whistleMidiFrom, whistleMidiTo) {
  const len = Math.floor(dur * SR), startI = S(start);
  let low = 0, band = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR, x = i / len;
    const fc = 350 * Math.pow(14, x);
    const ff = 2 * Math.sin(Math.PI * Math.min(0.45, fc / SR));
    const inw = rnd() * 2 - 1;
    low += ff * band; const hi = inw - low - 1.2 * band; band += ff * hi;
    const env = Math.pow(x, 1.6) * (1 - Math.pow(x, 8));
    let s = band * env * 0.13;
    if (whistleMidiFrom) {
      const m = whistleMidiFrom + (whistleMidiTo - whistleMidiFrom) * Math.pow(x, 1.4);
      const f = midiHz(m) * (1 + 0.006 * Math.sin(2 * Math.PI * 5.5 * t));
      ph += 2 * Math.PI * f / SR;
      s += Math.sin(ph) * env * 0.07;
    }
    const j = startI + i;
    put(bl, j, s * amp); put(br, j, s * amp);
  }
}

// ================= 编曲 =================
const CHORDS = {
  Am: { pad: [57, 60, 64, 69], bass: 33, mid: [45, 48, 52] },
  F:  { pad: [53, 57, 60, 65], bass: 29, mid: [41, 45, 48] },
  Dm: { pad: [50, 53, 57, 62], bass: 38, mid: [38, 41, 45] },
  E:  { pad: [52, 56, 59, 64], bass: 28, mid: [40, 44, 47] },
  Bb: { pad: [46, 53, 58, 62], bass: 34, mid: [34, 41, 46] }
};
const PROG = {};
// 小节 -> 和弦名
const setProg = (startBar, list) => list.forEach((c, i) => { PROG[startBar + i] = c; });
setProg(0, ['Am', 'Am', 'Am', 'Am']);
setProg(4, ['Am', 'F', 'Dm', 'E', 'Am', 'F', 'Bb', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(16, ['Am', 'F', 'Bb', 'E', 'Am', 'Dm', 'Bb', 'E', 'Am']);
setProg(25, ['Am', 'Am', 'F', 'Dm', 'Bb', 'E']);
setProg(31, ['Am', 'F', 'Dm', 'E', 'Am', 'Bb', 'E', 'Am', 'E']);
setProg(40, ['Am', 'Am']);

// ---- 主题 (古筝) : [拍偏移, midi, 时值(拍)] ----
const THEME = [
  [0, 76, 2], [2, 74, 2],
  [0, 72, 1.5], [1.5, 71, 0.5], [2, 69, 2],
  [0, 74, 2], [2, 77, 2],
  [0, 76, 3],
  [0, 72, 2], [2, 76, 2],
  [0, 77, 1.5], [1.5, 76, 0.5], [2, 74, 2],
  [0, 74, 2], [2, 70, 2],
  [0, 71, 2], [2, 68, 2],
  [0, 69, 2], [2, 72, 2],
  [0, 77, 2], [2, 76, 2],
  [0, 74, 3],
  [0, 71, 2], [2, 68, 1], [3, 64, 1]
];
const playTheme = (startBar, amp, oct, decay) => {
  THEME.forEach(([b, m]) => {
    // 拨两把(轻微失谐)模拟筝的双弦, 再加一条低八度余韵
    pluck(musL, musR, -0.28, tBeat(startBar, b),        m + oct,      amp,        decay,       0.44);
    pluck(musL, musR,  0.30, tBeat(startBar, b),        m + oct,      amp * 0.80, decay,       0.40);
    pluck(musL, musR, -0.10, tBeat(startBar, b + 0.25), m + oct - 12, amp * 0.32, decay * 0.7, 0.32);
  });
};

// ---- 1. 底噪 drone (全曲) ----
function droneLayer(fromBar, toBar, amp, opts) {
  opts = opts || {};
  const dur = (toBar - fromBar) * BAR;
  const st = tBeat(fromBar, 0);
  const notes = opts.notes || [45, 52];
  notes.forEach((m, i) => {
    tableVoice(musL, musR, i === 0 ? -0.12 : 0.12, st, T_ORGAN, m - 12, amp, dur * 0.35, dur * 0.65, 3.5, { detune: i ? 4 : -4, vibHz: 0.13, vibCents: 7 });
    tableVoice(musL, musR, i === 0 ? 0.12 : -0.12, st, T_STR_LO, m - 12, amp * 0.5, dur * 0.4, dur * 0.6, 4, { detune: i ? -6 : 6 });
  });
}

// ---- 2. 低音/大提琴声部 ----
function celloLine(barList, amp, attack, oct) {
  barList.forEach(bar => {
    const c = CHORDS[PROG[bar]];
    const dur = BAR * 0.92;
    const st = tBeat(bar, 0);
    c.mid.forEach((m, i) => {
      tableVoice(musL, musR, -0.3 + i * 0.3, st, T_STR_MD, m + oct + 12, amp * (i ? 0.6 : 1), attack, dur * 0.8, 1.6, { detune: (i - 1) * 5, vibHz: 4.2, vibCents: 8 });
    });
    tableVoice(musL, musR, 0, st, T_STR_LO, c.bass + 12 + oct, amp * 1.15, attack * 1.2, dur, 2.0, { detune: 3 });
  });
}
// ---- 3. 弦乐长音垫 ----
function padChord(bar, amp, attack, rel, oct) {
  const c = CHORDS[PROG[bar]];
  const st = tBeat(bar, 0);
  c.pad.forEach((m, i) => {
    tableVoice(musL, musR, -0.55 + i * 0.36, st, T_STR_MD, m + oct, amp * (0.26 + 0.12 * (i === 3 ? 1 : 0)),
      attack, BAR * 0.85, rel, { detune: (i % 2 ? -7 : 7), vibHz: 3.6, vibCents: 9 });
  });
}

// ================= 装配 =================
console.log('装配中...');

// --- 结构: 0-3 引子 / 4-15 A / 16-24 B / 25-30 C / 31-39 D / 40-41 尾声 ---
const S_A = 4, S_B = 16, S_C = 25, S_D = 31, S_OUT = 40;

// 1) 低频 drone 床
droneLayer(0, S_B, 0.100, { notes: [45, 52] });
droneLayer(S_B, S_C, 0.125, { notes: [45, 52, 57] });
droneLayer(S_C, S_D, 0.085, { notes: [45, 52] });
droneLayer(S_D, S_OUT, 0.145, { notes: [45, 52, 57] });
droneLayer(S_OUT, BARS, 0.080, { notes: [45, 52] });

// 2) 引子 (0-3)
fmBell(musL, musR, -0.5, tBeat(0, 0), 69, 0.16, 3.6, 3.51, 1.7, -4);
breath(musL, musR, 0.35, tBeat(0, 2), 69, 0.10, 5, [560, 1200, 2500]);
fmBell(musL, musR,  0.5, tBeat(1, 2), 76, 0.13, 3.2, 2.77, 1.5, 4);
heartbeat(perL, perR, tBeat(2, 0), 0.22, 0);
fmBell(musL, musR, -0.4, tBeat(2, 2), 72, 0.12, 3.4, 3.51, 1.6, 0);
heartbeat(perL, perR, tBeat(3, 0), 0.24, 0);
pluck(musL, musR, -0.2, tBeat(3, 0), 69, 0.30, 3.2, 0.44);
pluck(musL, musR,  0.25, tBeat(3, 2), 76, 0.24, 3.0, 0.42);
riser(musL, musR, tBeat(3, 0), 3.6, 0.10, 0, 0);

// 3) A段 (4-15): 主题 + 大提琴 + 心跳
for (let b = S_A; b < S_A + 12; b++) {
  celloLine([b], 0.062, 1.4, 0);
  heartbeat(perL, perR, tBeat(b, 0), 0.26, 0);
  if (b % 2 === 1) metalHit(perL, perR, 0.4, tBeat(b, 2), 0.075, 1180, 1.4);
}
playTheme(S_A, 0.46, 0, 3.4);
// A段后半加一层弱弦垫
for (let b = S_A + 6; b < S_A + 12; b++) padChord(b, 0.09, 2.4, 2.2, 0);

// 4) B段 (16-24): 铃音主题 + 弦垫渐强 + 疏打击
for (let b = S_B; b < S_B + 9; b++) {
  celloLine([b], 0.070, 1.2, -12);
  padChord(b, 0.10 + (b - S_B) * 0.012, 2.0, 2.0, 12);
  if (b % 2 === 0) heartbeat(perL, perR, tBeat(b, 0), 0.24, 0.2);
  metalHit(perL, perR, b % 2 ? -0.5 : 0.5, tBeat(b, 0), 0.06 + (b - S_B) * 0.004, 1450, 1.2);
}
// B段: 高八度玻璃铃点缀
[S_B, S_B + 2, S_B + 4, S_B + 6].forEach((bar, i) => {
  const m = [76, 74, 77, 72][i];
  fmBell(musL, musR, i % 2 ? 0.45 : -0.45, tBeat(bar, 0), m, 0.13, 4.0, 3.51, 1.8, i % 2 ? 4 : -3);
  fmBell(musL, musR, i % 2 ? -0.45 : 0.45, tBeat(bar, 2), m - 3, 0.10, 3.6, 2.77, 1.6, 0);
});
pluck(musL, musR, -0.25, tBeat(S_B + 1, 0), 64, 0.28, 3.4, 0.40);
pluck(musL, musR,  0.28, tBeat(S_B + 3, 0), 62, 0.26, 3.4, 0.40);
pluck(musL, musR, -0.22, tBeat(S_B + 5, 0), 62, 0.28, 3.4, 0.40);
pluck(musL, musR,  0.26, tBeat(S_B + 7, 0), 64, 0.26, 3.4, 0.40);
// 不谐和簇(22-23) + 上行推进
[70, 71, 75, 76].forEach((m, i) => tableVoice(musL, musR, -0.6 + i * 0.4, tBeat(S_B + 6, 0), T_STR_MD, m, 0.055, 3.0, BAR * 1.7, 2.5, { detune: (i - 1) * 9 }));
[69, 71, 72, 74, 76, 77, 80].forEach((m, i) => {
  pluck(musL, musR, -0.5 + i * 0.16, tBeat(S_B + 8, i * 0.5), m, 0.22 + i * 0.012, 1.6, 0.44);
});
heartbeat(perL, perR, tBeat(S_B + 8, 0), 0.30, 0);
riser(musL, musR, tBeat(S_B + 8, 2), 2.4, 0.13, 84, 96);

// 5) C段 (25-30): 静场 — 铃+气声, 稀疏
celloLine([S_C, S_C + 1], 0.06, 3.0, -12);
[S_C, S_C + 2, S_C + 4].forEach((bar, i) => {
  fmBell(musL, musR, i % 2 ? -0.5 : 0.5, tBeat(bar, 0), [69, 72, 74][i], 0.15, 5.0, 3.51, 1.9, i % 2 ? 5 : -5);
  fmBell(musL, musR, i % 2 ? 0.5 : -0.5, tBeat(bar, 2), [72, 65, 76][i], 0.10, 4.0, 2.77, 1.6, 0);
});
breath(musL, musR, -0.35, tBeat(S_C, 0), 69, 0.11, 5.5, [600, 1250, 2400]);
breath(musL, musR, 0.4, tBeat(S_C + 2, 0), 65, 0.10, 6.0, [540, 1180, 2550]);
breath(musL, musR, 0.1, tBeat(S_C + 4, 0), 71, 0.10, 5.0, [480, 1120, 2650]);
padChord(S_C + 2, 0.07, 3.5, 3.0, 12);
padChord(S_C + 3, 0.08, 3.0, 3.0, 12);
padChord(S_C + 4, 0.09, 2.5, 2.5, 12);
pluck(musL, musR, -0.3, tBeat(S_C + 1, 2), 72, 0.24, 3.6, 0.38);
pluck(musL, musR,  0.3, tBeat(S_C + 2, 2), 76, 0.24, 3.6, 0.40);
pluck(musL, musR, -0.25, tBeat(S_C + 4, 2), 70, 0.24, 3.6, 0.38);
heartbeat(perL, perR, tBeat(S_C + 4, 0), 0.22, 0);
heartbeat(perL, perR, tBeat(S_C + 5, 0), 0.26, 0);
riser(musL, musR, tBeat(S_C + 5, 2), 4.2, 0.15, 79, 93);

// 6) D段 (31-39): 高潮
for (let b = S_D; b < S_D + 9; b++) {
  celloLine([b], 0.095, 0.7, 0);
  padChord(b, 0.16, 1.4, 1.6, 0);
  padChord(b, 0.10, 1.6, 1.6, 12);
  heartbeat(perL, perR, tBeat(b, 0), 0.32, 0);
  drum(perL, perR, tBeat(b, 0), 0.30, 150, 52, 0.75);
  if (b % 2 === 1) drum(perL, perR, tBeat(b, 2), 0.20, 130, 48, 0.6);
  metalHit(perL, perR, b % 2 ? -0.55 : 0.55, tBeat(b, 0), 0.085, 1300, 1.5);
}
playTheme(S_D, 0.52, 0, 3.6);
pluck(musL, musR, -0.2, tBeat(S_D + 1, 0), 57, 0.32, 3.0, 0.40);
pluck(musL, musR,  0.2, tBeat(S_D + 4, 0), 64, 0.28, 2.6, 0.40);
// 高潮里的不谐和簇 + 高音微光
[70, 71, 75, 76].forEach((m, i) => tableVoice(musL, musR, -0.6 + i * 0.4, tBeat(S_D + 4, 0), T_STR_MD, m, 0.06, 2.0, BAR * 1.8, 2.0, { detune: (i - 1) * 10 }));
tableVoice(musL, musR, -0.7, tBeat(S_D, 0), T_AIR, 88, 0.030, 8.0, BAR * 6, 4, { vibHz: 0.2, vibCents: 12 });
tableVoice(musL, musR,  0.7, tBeat(S_D + 2, 0), T_AIR, 91, 0.026, 8.0, BAR * 6, 4, { vibHz: 0.17, vibCents: 14 });
riser(musL, musR, tBeat(S_D + 7, 0), 5.0, 0.14, 76, 91);

// 7) 尾声 (40-41)
pluck(musL, musR, -0.2, tBeat(S_OUT, 0), 69, 0.28, 4.5, 0.30);
pluck(musL, musR,  0.25, tBeat(S_OUT + 1, 0), 72, 0.20, 4.0, 0.28);
fmBell(musL, musR, 0.4, tBeat(S_OUT + 1, 2), 69, 0.11, 5.0, 3.51, 1.6, 0);
breath(musL, musR, -0.3, tBeat(S_OUT, 0), 57, 0.10, 8, [520, 1150, 2500]);

// ================= 混响 (Freeverb 简化版) =================
console.log('混响...');
function reverbChannel(input, offset) {
  const combTune = [1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617];
  const apTune = [556, 441, 341, 225];
  const fb = 0.865, damp = 0.20, apfb = 0.5, gain = 0.055;
  const combs = combTune.map(l => { const len = l + offset; return { buf: new Float32Array(len), len, idx: 0, st: 0 }; });
  const aps = apTune.map(l => { const len = l + offset; return { buf: new Float32Array(len), len, idx: 0 }; });
  const out = new Float32Array(input.length);
  for (let i = 0; i < input.length; i++) {
    const x = input[i] * gain;
    let acc = 0;
    for (let k = 0; k < combs.length; k++) {
      const c = combs[k];
      const y = c.buf[c.idx];
      c.st = y * (1 - damp) + c.st * damp;
      c.buf[c.idx] = x + c.st * fb;
      c.idx++; if (c.idx >= c.len) c.idx = 0;
      acc += y;
    }
    for (let k = 0; k < aps.length; k++) {
      const a = aps[k];
      const y = a.buf[a.idx];
      const o = -acc + y;
      a.buf[a.idx] = acc + y * apfb;
      a.idx++; if (a.idx >= a.len) a.idx = 0;
      acc = o;
    }
    out[i] = acc;
  }
  return out;
}
const PRE = S(0.028);
const sendL = new Float32Array(N), sendR = new Float32Array(N);
for (let i = PRE; i < N; i++) {
  sendL[i] = musL[i - PRE] * 0.46 + perL[i - PRE] * 0.13;
  sendR[i] = musR[i - PRE] * 0.46 + perR[i - PRE] * 0.13;
}
const revL = reverbChannel(sendL, 0);
const revR = reverbChannel(sendR, 23);

// 混响量按干声 RMS 归一, 保证尾音丰满但不过糊
function rmsOf(a) { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); }
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(perL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(3.0, dryRms * 0.62 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

// ================= 总线合成 + 淡入淡出 =================
const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) {
  outL[i] = musL[i] + perL[i] + revL[i] * wetGain;
  outR[i] = musR[i] + perR[i] + revR[i] * wetGain;
}
// ================= 母线 EQ (减 sub 浑浊 / 提高频光泽) =================
function biquadRun(input, c) {
  const out = new Float32Array(input.length);
  let z1 = 0, z2 = 0;
  for (let i = 0; i < input.length; i++) {
    const x = input[i];
    const y = c.b0 * x + z1;
    z1 = c.b1 * x - c.a1 * y + z2;
    z2 = c.b2 * x - c.a2 * y;
    out[i] = y;
  }
  return out;
}
function hpCoeff(fc, q) {
  const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al;
  return { b0: (1 + c) / 2 / a0, b1: -(1 + c) / a0, b2: (1 + c) / 2 / a0, a1: -2 * c / a0, a2: (1 - al) / a0 };
}
function peakCoeff(fc, q, db) {
  const A = Math.pow(10, db / 40), w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al / A;
  return { b0: (1 + al * A) / a0, b1: -2 * c / a0, b2: (1 - al * A) / a0, a1: -2 * c / a0, a2: (1 - al / A) / a0 };
}
function shelfCoeff(fc, db) {   // RBJ high shelf, S = 1
  const A = Math.pow(10, db / 40), w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / 2 * Math.SQRT2, sA = 2 * Math.sqrt(A) * al;
  const b0 = A * ((A + 1) + (A - 1) * c + sA), b1 = -2 * A * ((A - 1) + (A + 1) * c), b2 = A * ((A + 1) + (A - 1) * c - sA);
  const a0 = (A + 1) - (A - 1) * c + sA, a1 = 2 * ((A - 1) - (A + 1) * c), a2 = (A + 1) - (A - 1) * c - sA;
  return { b0: b0 / a0, b1: b1 / a0, b2: b2 / a0, a1: a1 / a0, a2: a2 / a0 };
}
const EQ_CHAIN = [hpCoeff(32, 0.707), peakCoeff(175, 1.2, -3.5), peakCoeff(400, 1.0, 1.0), shelfCoeff(2600, 3.0)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

// ================= 淡入淡出 =================
// 淡入 1.2s, 结尾 9s 淡出 + 最后 0.3s 硬收
const fadeIn = S(1.2), fadeOut = S(9);
for (let i = 0; i < N; i++) {
  let g = 1;
  if (i < fadeIn) g *= i / fadeIn;
  const endI = N - S(0.3);
  if (i > N - fadeOut) g *= Math.max(0, (endI - i) / (fadeOut - S(0.3)));
  if (i >= endI) g = 0;
  eqL[i] *= g; eqR[i] *= g;
}
// 先按峰值归一, 再做限幅抓瞬态 (保留整体动态)
let peak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > peak) peak = a; const b = Math.abs(eqR[i]); if (b > peak) peak = b; }
const targetPeak = 0.95;
const norm = peak > 0 ? targetPeak / peak : 1;
let clipped = 0;
for (let i = 0; i < N; i++) { eqL[i] *= norm; eqR[i] *= norm; }
// 母线限幅: 只压瞬态不压主体 (阈值 0.86, 瞬时抑制 + 缓慢释放)
const LIM_TH = 0.86;
let lg = 1, minLG = 1;
for (let i = 0; i < N; i++) {
  const a = Math.max(Math.abs(eqL[i]), Math.abs(eqR[i]));
  const target = a > LIM_TH ? LIM_TH / a : 1;
  if (target < lg) lg = target; else lg += (target - lg) * 0.00004;
  if (lg < minLG) minLG = lg;
  eqL[i] *= lg; eqR[i] *= lg;
  // 软削波: 限幅器跟不上时的兼底
  if (eqL[i] > 0.99) { eqL[i] = 0.99; clipped++; }
  if (eqR[i] > 0.99) { eqR[i] = 0.99; clipped++; }
}
// 末端响度归一 (留 0.3dB 余量)
let fp = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > fp) fp = a; const b = Math.abs(eqR[i]); if (b > fp) fp = b; }
const fg = fp > 0 ? 0.97 / fp : 1;
for (let i = 0; i < N; i++) { eqL[i] *= fg; eqR[i] *= fg; }
outL.set(eqL); outR.set(eqR);

// ================= 写 WAV =================
const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'tea_tree_boss.wav');
const dataBytes = N * 2 * 2;
const buf = Buffer.alloc(44 + dataBytes);
buf.write('RIFF', 0); buf.writeUInt32LE(36 + dataBytes, 4); buf.write('WAVE', 8);
buf.write('fmt ', 12); buf.writeUInt32LE(16, 16); buf.writeUInt16LE(1, 20);
buf.writeUInt16LE(2, 22); buf.writeUInt32LE(SR, 24); buf.writeUInt32LE(SR * 4, 28);
buf.writeUInt16LE(4, 32); buf.writeUInt16LE(16, 34);
buf.write('data', 36); buf.writeUInt32LE(dataBytes, 40);
for (let i = 0; i < N; i++) {
  let l = outL[i], r = outR[i];
  l = Math.max(-1, Math.min(1, l)); r = Math.max(-1, Math.min(1, r));
  buf.writeInt16LE(Math.round(l * 32767), 44 + i * 4);
  buf.writeInt16LE(Math.round(r * 32767), 44 + i * 4 + 2);
}
fs.writeFileSync(outFile, buf);

// ================= 分段电平自检 =================
const seg = Math.floor(SR * 5);
const rows = [];
for (let i = 0; i < N; i += seg) {
  let p = 0, s = 0, c = 0;
  for (let j = i; j < Math.min(N, i + seg); j++) { const a = Math.abs(outL[j]); if (a > p) p = a; s += outL[j] * outL[j]; c++; }
  rows.push([+(i / SR).toFixed(1), +p.toFixed(3), +Math.sqrt(s / Math.max(1, c)).toFixed(4)]);
}
console.log(JSON.stringify({
  file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, bpm: BPM,
  peak, norm: +norm.toFixed(3), clipped, wetGain: +wetGain.toFixed(3),
  profile_sec_peak_rms: rows, minLimiterGain: +minLG.toFixed(3)
}, null, 0));
