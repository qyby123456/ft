/*
 * 茶树Boss 战斗BGM —— 「黑暗剧院」版
 * 参考取向: Black Souls II OST - Theatre of Darkness, the Grand Guignol
 *   (暗黑大剧院 / 恐怖木偶剧: 扭曲的 3/4 圆舞曲 + 教堂管风琴 + 音乐盒 + 不谐和弦 + 定音鼓/军鼓滚奏)
 * 124 BPM 的三拍子(四分音符=120), d小调, 72 小节 ≈ 108 秒
 * 编制: 管风琴 / 音乐盒 / 弦乐(stabs+震音) / 簧管式主奏 / 铜管 / 定音鼓 / 军鼓 / 长音镲 / 锣 / 气声合唱
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const BPM = 120;            // 四分音符
const BEAT = 60 / BPM;
const BAR = BEAT * 3;       // 3/4
const BARS = 72;
const TAIL = 5;
const N = Math.ceil((BARS * BAR + TAIL) * SR);

const rnd = (() => { let a = 20260910; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);
const tBeat = (bar, beat) => (bar * 3 + beat) * BEAT;
const S = t => Math.round(t * SR);

const musL = new Float32Array(N), musR = new Float32Array(N);
const dryL = new Float32Array(N), dryR = new Float32Array(N);

function panGains(p) { const a = (Math.max(-1, Math.min(1, p)) + 1) * Math.PI / 4; return [Math.cos(a), Math.sin(a)]; }
function put(bus, i, v) { if (i >= 0 && i < N) bus[i] += v; }
function adsr(t, dur, a, d, s, r) {
  if (t < 0 || t > dur + r) return 0;
  let amp;
  if (t < a) amp = t / a;
  else if (t < a + d) amp = 1 + (s - 1) * (t - a) / d;
  else amp = s;
  if (t > dur) amp *= Math.max(0, 1 - (t - dur) / r);
  return Math.max(0, amp);
}

const SIZE = 4096;
function makeTable(harm) {
  const t = new Float32Array(SIZE); let mx = 0;
  for (let i = 0; i < SIZE; i++) {
    let v = 0;
    for (let h = 0; h < harm.length; h++) v += harm[h] * Math.sin(2 * Math.PI * (h + 1) * i / SIZE);
    t[i] = v; if (Math.abs(v) > mx) mx = Math.abs(v);
  }
  for (let i = 0; i < SIZE; i++) t[i] /= mx;
  return t;
}
const sawH = (n, tilt) => { const a = []; for (let i = 1; i <= n; i++) a.push((1 / i) * Math.exp(-i / tilt)); return a; };
const T_ORGAN = makeTable([1, 0.52, 0.30, 0.20, 0.13, 0.09, 0.06, 0.045, 0.03, 0.02, 0.015]);
const T_STR_MD = makeTable(sawH(30, 16));
const T_STR_LO = makeTable(sawH(26, 10));
const T_REED = makeTable([1, 0, 0.42, 0, 0.26, 0, 0.18, 0, 0.12, 0, 0.08]);   // 中空簧管/木笛音色
const T_BRASS = makeTable([1, 0.62, 0.44, 0.30, 0.2, 0.14, 0.1, 0.07, 0.05, 0.035, 0.02]);
const T_AIR = makeTable([1, 0.06, 0.03, 0.012]);

function tableVoice(bl, br, pan, start, table, freq, amp, atk, hold, rel, opt) {
  opt = opt || {};
  const det = opt.detune || 0, vibHz = opt.vibHz || 0, vibC = opt.vibCents || 0;
  const [pl, pr] = panGains(pan);
  const total = Math.ceil((atk + hold + rel) * SR);
  const inc0 = midiHz(freq) * Math.pow(2, det / 1200) / SR * SIZE;
  let phase = rnd() * SIZE;
  const startI = S(start);
  for (let i = 0; i < total; i++) {
    const t = i / SR;
    const a = adsr(t, atk + hold, atk, 0.06, 1, rel);
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
/** 音乐盒: 高音、极快起音、带金属非谐泛音 */
function musicBox(bl, br, pan, start, note, amp) {
  const fc = midiHz(note);
  const len = Math.floor(1.7 * SR), startI = S(start);
  const [pl, pr] = panGains(pan);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const e = Math.exp(-t / 0.5) * (1 - Math.exp(-t / 0.0012));
    if (e < 1e-5) break;
    const tone = Math.sin(2 * Math.PI * fc * t) * 0.62
      + Math.sin(2 * Math.PI * fc * 4.03 * t) * 0.24 * Math.exp(-t / 0.09)
      + Math.sin(2 * Math.PI * fc * 6.87 * t) * 0.11 * Math.exp(-t / 0.05);
    const s = tone * e * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 拨弦(羽管键琴/拨奏弦) */
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
  const len = Math.floor(decaySec * SR), startI = S(start);
  const relStart = len - Math.floor(0.25 * SR);
  let idx = 0;
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
/** 定音鼓 */
function timp(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note);
  const dur = 0.9, len = Math.floor(dur * SR), startI = S(start);
  const [pl, pr] = panGains(pan === undefined ? 0 : pan);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * 0.88 + f0 * 0.22 * Math.exp(-t / 0.04);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.30) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.10 * (w - lp);
    s += lp * Math.exp(-t / 0.012) * 0.35;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 军鼓(可做滚奏) */
function snare(bl, br, start, amp, pan) {
  const len = Math.floor(0.28 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const hp = 0.93 * (yPrev + w - wPrev); yPrev = hp; wPrev = w;
    ph += 2 * Math.PI * 190 / SR;
    const body = Math.sin(ph) * Math.exp(-t / 0.04) * 0.4;
    const s = (hp * Math.exp(-t / 0.065) * 0.85 + body) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
function snareRoll(bl, br, start, dur, amp) {
  const step = 0.125;
  for (let t = 0; t < dur; t += step) snare(bl, br, start + t, amp * (0.35 + 0.65 * (t / dur)), (t * 7 % 2) - 0.5);
}
function cymbal(bl, br, start, amp, dur, pan) {
  const len = Math.floor((dur + 0.5) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0;
  const ring = [3100, 4400, 5900, 8100];
  const rp = ring.map(() => 0);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const y = 0.87 * (yPrev + w - wPrev); yPrev = y; wPrev = w;
    let s = y * Math.exp(-t / (dur * 0.24)) * 0.9;
    for (let k = 0; k < ring.length; k++) {
      rp[k] += 2 * Math.PI * ring[k] / SR;
      s += Math.sin(rp[k]) * Math.exp(-t / (dur * (0.10 + 0.05 * k))) * 0.05;
    }
    const env = 1 - Math.exp(-t / 0.0015);
    put(bl, startI + i, s * env * amp * pl); put(br, startI + i, s * env * amp * pr);
  }
}
function gong(bl, br, start, amp, base, dur) {
  const ratios = [1, 1.41, 1.93, 2.61, 3.44, 4.51, 5.9];
  const len = Math.floor((dur + 1.5) * SR), startI = S(start);
  const st = ratios.map(() => 0);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    if (t > dur + 1.4) break;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      const a = Math.exp(-t / (dur / (1 + k * 0.5) * 0.32)) * (1 - Math.exp(-t / 0.002)) / (1 + k * 0.8);
      st[k] += 2 * Math.PI * base * ratios[k] / SR;
      s += Math.sin(st[k]) * a;
    }
    const w = rnd() * 2 - 1; lp += 0.3 * (w - lp);
    s += lp * Math.exp(-t / 0.008) * 0.5;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
/** 气声合唱 */
function breath(bl, br, pan, start, noteMidi, amp, dur, formants) {
  const f = midiHz(noteMidi);
  const [pl, pr] = panGains(pan);
  const len = Math.floor((dur + 1.2) * SR), startI = S(start);
  const F = formants || [620, 1250, 2500];
  const mk = () => F.map(fc => { const w = 2 * Math.PI * fc / SR; const c = Math.cos(w); const al = Math.sin(w) / (2 * 1.6); const a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; });
  const cL = mk(), cR = mk();
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, dur, Math.min(1.0, dur * 0.3), 0.5, 0.8, 1.2);
    if (a <= 0) continue;
    const w = (rnd() * 2 - 1) * 0.6 + Math.sin(2 * Math.PI * f * (1 + 0.004 * Math.sin(2 * Math.PI * 5.4 * t))) * 0.3;
    let oL = 0, oR = 0;
    for (let k = 0; k < 3; k++) {
      const g = k === 0 ? 1 : k === 1 ? 0.6 : 0.32;
      let o = cL[k].b0 * w + sL[k].z1;
      sL[k].z1 = cL[k].b2 * w - cL[k].a1 * o - cL[k].a2 * sL[k].z2; sL[k].z2 = o;
      oL += o * g;
      let o2 = cR[k].b0 * (w * 0.93) + sR[k].z1;
      sR[k].z1 = cR[k].b2 * (w * 0.93) - cR[k].a1 * o2 - cR[k].a2 * sR[k].z2; sR[k].z2 = o2;
      oR += o2 * g;
    }
    const j = startI + i;
    put(bl, j, oL * a * amp * pl * 6); put(br, j, oR * a * amp * pr * 6);
  }
}
/** 弦乐震音: 16 分快速反复 */
function tremolo(bl, br, start, durSec, notes, amp) {
  const step = 0.125;
  let k = 0;
  for (let t = 0; t < durSec; t += step) {
    const m = notes[k % notes.length];
    const pan = -0.35 + 0.35 * (k % 3);
    tableVoice(bl, br, pan, start + t, T_STR_MD, m, amp * (0.85 + 0.15 * (k % 2)), 0.012, step * 0.8, 0.06, { detune: (k % 2 ? -8 : 8) });
    k++;
  }
}

// ================= 和声 =================
// 低音(大谱表)与和弦
const CH = {
  Dm: { bass: 38, pad: [62, 65, 69] },   // D F A
  Gm: { bass: 43, pad: [58, 62, 67] },   // Bb D G
  A7: { bass: 45, pad: [61, 64, 67] },   // C# E G
  Bb: { bass: 46, pad: [58, 62, 65] },   // Bb D F
  F:  { bass: 41, pad: [57, 60, 65] },   // A C F
  Eb: { bass: 39, pad: [58, 63, 67] },   // Bb Eb G  (那不勒斯/♭II)
  Ddim: { bass: 38, pad: [62, 65, 68] }  // D F Ab
};
const PROG = [];
const setProg = (startBar, list) => list.forEach((c, i) => { PROG[startBar + i] = c; });
setProg(0, ['Dm', 'Dm', 'A7', 'A7']);
setProg(4, ['Dm', 'Gm', 'A7', 'Dm', 'Dm', 'Bb', 'A7', 'Dm', 'Dm', 'Gm', 'A7', 'Dm', 'Bb', 'Gm', 'A7', 'Dm']);
setProg(20, ['Bb', 'F', 'Gm', 'Dm', 'Eb', 'Bb', 'A7', 'A7', 'Bb', 'F', 'Gm', 'Dm', 'Eb', 'Bb', 'A7', 'A7']);
setProg(36, ['Ddim', 'Ddim', 'Eb', 'Eb', 'Ddim', 'Ddim', 'A7', 'A7']);
setProg(44, ['Dm', 'Gm', 'A7', 'Dm', 'Dm', 'Bb', 'A7', 'Dm', 'Dm', 'Gm', 'A7', 'Dm', 'Bb', 'Gm', 'A7', 'Dm',
             'Bb', 'F', 'Gm', 'Dm']);
setProg(64, ['Dm', 'Dm', 'Bb', 'A7', 'Dm', 'Dm', 'Gm', 'Dm']);
const chordOf = b => CH[PROG[b] || 'Dm'];

// 主题: [小节偏移, 拍偏移(0..2), midi, 时值拍]
const A_THEME = [
  [0, 0, 74, 1], [0, 1, 77, 0.5], [0, 1.5, 81, 1.5],
  [1, 0, 79, 1], [1, 1, 77, 2],
  [2, 0, 76, 1], [2, 1, 74, 1], [2, 2, 73, 1],
  [3, 0, 74, 3],
  [4, 0, 74, 1], [4, 1, 77, 0.5], [4, 1.5, 81, 1.5],
  [5, 0, 82, 1], [5, 1, 81, 2],
  [6, 0, 79, 1], [6, 1, 76, 1], [6, 2, 73, 1],
  [7, 0, 74, 3]
];
const B_THEME = [
  [0, 0, 82, 1], [0, 1, 84, 0.5], [0, 1.5, 86, 1.5],
  [1, 0, 84, 1], [1, 1, 81, 2],
  [2, 0, 82, 1], [2, 1, 79, 1], [2, 2, 77, 1],
  [3, 0, 74, 3],
  [4, 0, 87, 1], [4, 1, 86, 2],
  [5, 0, 82, 1], [5, 1, 79, 2],
  [6, 0, 81, 1], [6, 1, 79, 1], [6, 2, 76, 1],
  [7, 0, 73, 3]
];
const C_THEME = [ // 半音下行
  [0, 0, 84, 3], [1, 0, 83, 3], [2, 0, 82, 3], [3, 0, 81, 3],
  [4, 0, 80, 3], [5, 0, 79, 3],
  [6, 0, 78, 1], [6, 1, 77, 1], [6, 2, 76, 1],
  [7, 0, 75, 3]
];

// ================= 声部 =================
/** 圆舞曲伴奏: 第1拍低音, 第2/3拍和弦短击(oom-pah-pah) */
function waltz(bar, bassAmp, stabAmp, bassTable, stabTable, oct) {
  const c = chordOf(bar);
  tableVoice(musL, musR, 0, tBeat(bar, 0), bassTable, c.bass + (oct || 0), bassAmp, 0.02, BEAT * 0.9, 0.25, { detune: 3 });
  for (const bt of [1, 2]) {
    const pan = bt === 1 ? -0.22 : 0.22;
    c.pad.forEach((m, i) => {
      tableVoice(musL, musR, pan + (i - 1) * 0.2, tBeat(bar, bt), stabTable, m - 12, stabAmp * (i === 0 ? 1 : 0.8), 0.012, BEAT * 0.42, 0.12, { detune: (i - 1) * 6 });
    });
  }
}
/** 主奏旋律 */
function playLine(list, startBar, amp, opts) {
  opts = opts || {};
  const oct = opts.oct || 0, table = opts.table || T_REED, box = !!opts.box, plk = !!opts.pluck;
  list.forEach(([b, bt, m, d]) => {
    const note = m + oct;
    const st = tBeat(startBar + b, bt);
    const hold = Math.max(0.12, d * BEAT * 0.9);
    tableVoice(musL, musR, -0.12, st, table, note, amp, 0.03, hold, 0.3, { detune: -5, vibHz: 5.0, vibCents: 12 });
    tableVoice(musL, musR, 0.12, st, T_STR_MD, note, amp * 0.45, 0.05, hold, 0.3, { detune: 6 });
    if (box) musicBox(musL, musR, 0.3, st, note + 12, amp * 0.5);
    if (plk) pluck(musL, musR, -0.3, st, note, amp * 0.5, 1.6, 0.5);
  });
}

// ================= 装配 =================
console.log('装配中...');

// ---- 引子 (0-3): 剧院幕布拉开, 圆舞曲节奏立刻进 ----
waltz(0, 0.16, 0.055, T_ORGAN, T_STR_LO, 0);
gong(musL, musR, tBeat(0, 0), 0.12, 150, 2.2);
timp(dryL, dryR, tBeat(0, 0), 0.5, 38, 0);
for (let b = 1; b < 4; b++) waltz(b, 0.16, 0.055, T_ORGAN, T_STR_LO, 0);
musicBox(musL, musR, 0.4, tBeat(0, 0), 74, 0.22);
musicBox(musL, musR, -0.4, tBeat(1, 1), 82, 0.18);
musicBox(musL, musR, 0.35, tBeat(2, 2), 77, 0.16);
musicBox(musL, musR, -0.3, tBeat(3, 0), 74, 0.20);
[1, 3].forEach(b => timp(dryL, dryR, tBeat(b, 0), 0.34, 38, b % 2 ? -0.2 : 0.2));
snareRoll(dryL, dryR, tBeat(3, 1.5), 1.4, 0.18);
cymbal(dryL, dryR, tBeat(3, 2), 0.14, 1.2, 0.35);

// ---- A 段 (4-19): 主圆舞曲 ----
for (let b = 4; b < 20; b++) {
  waltz(b, 0.20, 0.075, T_ORGAN, T_STR_MD, 0);
  timp(dryL, dryR, tBeat(b, 0), 0.36, chordOf(b).bass - 12, (b % 2 ? 0.2 : -0.2));
  if (b % 4 === 3) cymbal(dryL, dryR, tBeat(b, 2), 0.10, 0.8, -0.3);
  if (b % 8 === 7) snareRoll(dryL, dryR, tBeat(b, 1), 2.0, 0.16);
}
playLine(A_THEME, 4, 0.19, { table: T_REED, box: true, pluck: true });
playLine(A_THEME, 12, 0.20, { table: T_REED, box: true, pluck: true });
// 管风琴长音撑底
for (let b = 4; b < 20; b++) {
  const c = chordOf(b);
  c.pad.forEach((m, i) => tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(b, 0), T_ORGAN, m - 12, 0.055, 0.25, BAR * 0.9, 0.4, { detune: (i - 1) * 4 }));
}

// ---- B 段 (20-35): 音乐盒与拨弦, 空气感 ----
for (let b = 20; b < 36; b++) {
  const c = chordOf(b);
  tableVoice(musL, musR, 0, tBeat(b, 0), T_ORGAN, c.bass, 0.11, 0.03, BEAT * 0.9, 0.3);
  [1, 2].forEach(bt => pluck(musL, musR, bt === 1 ? -0.4 : 0.4, tBeat(b, bt), c.pad[bt % c.pad.length] - 12, 0.10, 0.9, 0.5));
  if (b % 4 === 0) timp(dryL, dryR, tBeat(b, 0), 0.26, c.bass, b % 8 ? 0.25 : -0.25);
  if (b % 8 === 6) snareRoll(dryL, dryR, tBeat(b, 2), 1.2, 0.12);
}
playLine(B_THEME, 20, 0.17, { table: T_REED, box: true });
playLine(B_THEME, 28, 0.18, { table: T_REED, box: true, pluck: true });
breath(musL, musR, -0.5, tBeat(24, 0), 62, 0.05, BAR * 3.5, [620, 1250, 2500]);
breath(musL, musR, 0.5, tBeat(32, 0), 65, 0.045, BAR * 3.5, [600, 1200, 2400]);

// ---- C 段 (36-43): 半音下行 + 震音, 压迫 ----
for (let b = 36; b < 44; b++) {
  const c = chordOf(b);
  const st = tBeat(b, 0);
  // 低音管风琴踏板
  tableVoice(musL, musR, 0, st, T_ORGAN, c.bass - 12, 0.16, 0.06, BAR * 0.95, 0.4);
  // 弦乐震音铺和弦
  tremolo(musL, musR, st, BAR * 0.98, [c.pad[0] - 12, c.pad[1] - 12, c.pad[2] - 12, c.pad[1] - 12], 0.052);
  timp(dryL, dryR, tBeat(b, 0), 0.3, c.bass - 12, b % 2 ? -0.2 : 0.2);
}
playLine(C_THEME, 36, 0.17, { table: T_REED, box: true });
playLine(C_THEME, 40, 0.18, { table: T_REED, box: true });
snareRoll(dryL, dryR, tBeat(42, 0), BAR * 2, 0.20);
cymbal(dryL, dryR, tBeat(43, 0), 0.18, 1.8, 0.3);

// ---- D 段 (44-63): 全奏高潮 ----
for (let b = 44; b < 64; b++) {
  waltz(b, 0.24, 0.095, T_BRASS, T_STR_MD, 0);
  const c = chordOf(b);
  c.pad.forEach((m, i) => tableVoice(musL, musR, -0.55 + i * 0.5, tBeat(b, 0), T_ORGAN, m, 0.065, 0.15, BAR * 0.9, 0.35, { detune: (i - 1) * 5 }));
  timp(dryL, dryR, tBeat(b, 0), 0.44, c.bass - 12, (b % 2 ? 0.22 : -0.22));
  if (b % 2 === 1) timp(dryL, dryR, tBeat(b, 2), 0.22, c.bass, (b % 4 ? -0.3 : 0.3));
  if (b % 4 === 0) cymbal(dryL, dryR, tBeat(b, 0), 0.20, 1.6, b % 8 ? -0.35 : 0.35);
  if (b % 8 === 7) snareRoll(dryL, dryR, tBeat(b, 1), 2.0, 0.20);
}
playLine(A_THEME, 44, 0.24, { table: T_REED, box: true, pluck: true });
playLine(A_THEME, 52, 0.25, { table: T_REED, box: true, pluck: true });
playLine(A_THEME, 44, 0.10, { oct: 12, box: false });
playLine(A_THEME, 52, 0.11, { oct: 12, box: false });
// 铜管推进短句
for (const b of [47, 51, 55, 59, 63]) {
  const c = chordOf(b);
  [1.5, 2].forEach(bt => c.pad.forEach((m, i) => tableVoice(musL, musR, -0.4 + i * 0.4, tBeat(b, bt), T_BRASS, m - 12, 0.075, 0.02, BEAT * 0.5, 0.12, { detune: (i - 1) * 7 })));
}
breath(musL, musR, -0.55, tBeat(44, 0), 62, 0.055, BAR * 5, [640, 1300, 2600]);
breath(musL, musR, 0.55, tBeat(52, 0), 69, 0.05, BAR * 5, [620, 1250, 2500]);
tableVoice(musL, musR, -0.7, tBeat(48, 0), T_AIR, 88, 0.018, 3.0, BAR * 3, 3, { vibHz: 0.2, vibCents: 12 });
tableVoice(musL, musR, 0.7, tBeat(56, 0), T_AIR, 91, 0.016, 3.0, BAR * 3, 3, { vibHz: 0.18, vibCents: 14 });
[46, 50, 54, 58].forEach(b => gong(musL, musR, tBeat(b, 0), 0.075, 190, 1.3));

// ---- 尾声 (64-71): 收束 ----
for (let b = 64; b < 72; b++) waltz(b, 0.20, 0.075, T_ORGAN, T_STR_MD, 0);
playLine([[0, 0, 74, 3], [1, 0, 74, 1], [1, 1, 73, 2], [2, 0, 74, 3], [3, 0, 72, 3]], 64, 0.18, { table: T_REED, box: true });
playLine([[0, 0, 74, 3], [1, 0, 73, 3], [2, 0, 74, 3], [3, 0, 74, 3]], 68, 0.15, { table: T_REED, box: true });
// 终止: 全奏 Dm + 定音鼓 + 锣
const EB = 71;
[62, 65, 69, 74].forEach((m, i) => {
  tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(EB, 0), T_ORGAN, m, 0.13, 0.02, BAR * 0.85, 1.6, { detune: (i - 1) * 6 });
  tableVoice(musL, musR, -0.45 + i * 0.45, tBeat(EB, 0), T_BRASS, m - 12, 0.10, 0.03, BAR * 0.7, 1.4, { detune: (i - 1) * 7 });
});
tableVoice(musL, musR, 0, tBeat(EB, 0), T_STR_LO, 38, 0.16, 0.02, BAR * 0.9, 1.6);
musicBox(musL, musR, 0, tBeat(EB, 0), 86, 0.20);
musicBox(musL, musR, -0.3, tBeat(EB, 1), 81, 0.16);
musicBox(musL, musR, 0.3, tBeat(EB, 2), 74, 0.16);
timp(dryL, dryR, tBeat(EB, 0), 0.55, 26, 0);
timp(dryL, dryR, tBeat(EB, 1), 0.3, 26, -0.2);
timp(dryL, dryR, tBeat(EB, 2), 0.3, 33, 0.2);
cymbal(dryL, dryR, tBeat(EB, 0), 0.26, 2.2, 0);
gong(musL, musR, tBeat(EB, 0), 0.18, 130, 3.0);

// ================= 混响 =================
console.log('混响...');
function reverbChannel(input, offset, fb, damp, gain) {
  const combTune = [1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617];
  const apTune = [556, 441, 341, 225];
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
      a.buf[a.idx] = acc + y * 0.5;
      a.idx++; if (a.idx >= a.len) a.idx = 0;
      acc = o;
    }
    out[i] = acc;
  }
  return out;
}
const PRE = S(0.025);
const sendL = new Float32Array(N), sendR = new Float32Array(N);
for (let i = PRE; i < N; i++) {
  sendL[i] = musL[i - PRE] * 0.34 + dryL[i - PRE] * 0.10;
  sendR[i] = musR[i - PRE] * 0.34 + dryR[i - PRE] * 0.10;
}
const revL = reverbChannel(sendL, 0, 0.84, 0.22, 0.05);   // 剧院大厅感: 尾巴长一点
const revR = reverbChannel(sendR, 23, 0.84, 0.22, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.5, dryRms * 0.40 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) {
  outL[i] = musL[i] + dryL[i] + revL[i] * wetGain;
  outR[i] = musR[i] + dryR[i] + revR[i] * wetGain;
}
// 淡入 0.3s / 结尾 4s 淡出
const fadeIn = S(0.3), fadeOut = S(4.0);
for (let i = 0; i < N; i++) {
  let g = 1;
  if (i < fadeIn) g *= i / fadeIn;
  const endI = N - S(0.25);
  if (i > N - fadeOut) g *= Math.max(0, (endI - i) / (fadeOut - S(0.25)));
  if (i >= endI) g = 0;
  outL[i] *= g; outR[i] *= g;
}
// EQ
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
function hpCoeff(fc, q) { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: (1 + c) / 2 / a0, b1: -(1 + c) / a0, b2: (1 + c) / 2 / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; }
function peakCoeff(fc, q, db) { const A = Math.pow(10, db / 40), w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al / A; return { b0: (1 + al * A) / a0, b1: -2 * c / a0, b2: (1 - al * A) / a0, a1: -2 * c / a0, a2: (1 - al / A) / a0 }; }
function shelfCoeff(fc, db) { const A = Math.pow(10, db / 40), w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / 2 * Math.SQRT2, sA = 2 * Math.sqrt(A) * al; const b0 = A * ((A + 1) + (A - 1) * c + sA), b1 = -2 * A * ((A - 1) + (A + 1) * c), b2 = A * ((A + 1) + (A - 1) * c - sA); const a0 = (A + 1) - (A - 1) * c + sA, a1 = 2 * ((A - 1) - (A + 1) * c), a2 = (A + 1) - (A - 1) * c - sA; return { b0: b0 / a0, b1: b1 / a0, b2: b2 / a0, a1: a1 / a0, a2: a2 / a0 }; }
const EQ_CHAIN = [hpCoeff(33, 0.707), peakCoeff(210, 1.1, -3.0), peakCoeff(900, 0.9, 1.2), shelfCoeff(3200, 2.5)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

let peak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > peak) peak = a; const b = Math.abs(eqR[i]); if (b > peak) peak = b; }
const norm = peak > 0 ? 1.5 / peak : 1;
let sat = 0;
for (let i = 0; i < N; i++) {
  let l = eqL[i] * norm, r = eqR[i] * norm;
  if (Math.abs(l) > 0.7) { l = Math.tanh(l); sat++; }
  if (Math.abs(r) > 0.7) { r = Math.tanh(r); sat++; }
  eqL[i] = l; eqR[i] = r;
}
let fp = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > fp) fp = a; const b = Math.abs(eqR[i]); if (b > fp) fp = b; }
const fg = fp > 0 ? 0.94 / fp : 1;
for (let i = 0; i < N; i++) { eqL[i] *= fg; eqR[i] *= fg; }
let _s0 = 0; for (let i = 0; i < N; i++) _s0 += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const rms0 = Math.sqrt(_s0 / (2 * N));
const TARGET_RMS = 0.110;
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'guignol.wav');
const dataBytes = N * 2 * 2;
const buf = Buffer.alloc(44 + dataBytes);
buf.write('RIFF', 0); buf.writeUInt32LE(36 + dataBytes, 4); buf.write('WAVE', 8);
buf.write('fmt ', 12); buf.writeUInt32LE(16, 16); buf.writeUInt16LE(1, 20);
buf.writeUInt16LE(2, 22); buf.writeUInt32LE(SR, 24); buf.writeUInt32LE(SR * 4, 28);
buf.writeUInt16LE(4, 32); buf.writeUInt16LE(16, 34);
buf.write('data', 36); buf.writeUInt32LE(dataBytes, 40);
for (let i = 0; i < N; i++) {
  let l = Math.max(-1, Math.min(1, eqL[i])), r = Math.max(-1, Math.min(1, eqR[i]));
  buf.writeInt16LE(Math.round(l * 32767), 44 + i * 4);
  buf.writeInt16LE(Math.round(r * 32767), 44 + i * 4 + 2);
}
fs.writeFileSync(outFile, buf);

const seg = Math.floor(SR * 5);
const rows = [];
for (let i = 0; i < N; i += seg) {
  let p = 0, s = 0, c = 0;
  for (let j = i; j < Math.min(N, i + seg); j++) { const a = Math.abs(eqL[j]); if (a > p) p = a; s += eqL[j] * eqL[j]; c++; }
  rows.push([+(i / SR).toFixed(1), +p.toFixed(3), +Math.sqrt(s / Math.max(1, c)).toFixed(4)]);
}
console.log(JSON.stringify({ file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, bpm: BPM, peak, norm: +norm.toFixed(3), satSamples: sat, wetGain: +wetGain.toFixed(3), finalRms: +finalRms.toFixed(4), profile: rows }, null, 0));
