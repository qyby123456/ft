/*
 * 茶树Boss 战斗BGM —— 激昂版
 * 100 BPM, a小调(i - VI - VII - V 的英雄式进行), 52 小节 ≈ 2:05
 * 编制: 战鼓(底鼓/军鼓/太鼓/长音镲) + 低音弦乐八分音符 ostinato + 铜管式和声冲击
 *       + 弦乐/铜管主题(古筝同度加倍保留茶树音色) + 气声合唱 + 长音镲/锣收尾
 * 目标: 开局即高潮, 整轨 RMS ≈ 0.095 (与整合包其它 BGM 持平)
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const BPM = 100;
const BEAT = 60 / BPM;
const BAR = BEAT * 4;
const BARS = 52;
const TAIL = 5;
const TOTAL = BARS * BAR + TAIL;
const N = Math.ceil(TOTAL * SR);

const rnd = (() => { let a = 20260910; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);
const tBeat = (bar, beat) => (bar * 4 + beat) * BEAT;
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

// ---------- 波表 ----------
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
const T_STR_LO = makeTable(sawH(26, 10));
const T_STR_MD = makeTable(sawH(30, 16));
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

// ---------- 古筝拨弦 ----------
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
  const relStart = len - Math.floor(0.3 * SR);
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

// ---------- 打击乐 ----------
function kick(bl, br, start, amp) {
  const dur = 0.38, len = Math.floor(dur * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 48 + (160 - 48) * Math.exp(-t / 0.022);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.11) * (1 - Math.exp(-t / 0.0012));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.25 * (w - lp);
    s += lp * Math.exp(-t / 0.006) * 0.35;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
function snare(bl, br, start, amp, pan) {
  const len = Math.floor(0.3 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const hp = 0.93 * (yPrev + w - wPrev); yPrev = hp; wPrev = w;
    ph += 2 * Math.PI * 196 / SR;
    const body = Math.sin(ph) * Math.exp(-t / 0.04) * 0.45;
    const s = (hp * Math.exp(-t / 0.07) * 0.85 + body) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 太鼓/战鼓: 有音高的鼓 */
function taiko(bl, br, start, amp, f0, pan) {
  const dur = 0.55, len = Math.floor(dur * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * 0.72 + f0 * 0.28 * Math.exp(-t / 0.03);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.16) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.16 * (w - lp);
    s += lp * Math.exp(-t / 0.01) * 0.30;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
function cymbal(bl, br, start, amp, dur, pan) {
  const len = Math.floor((dur + 0.5) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0;
  const ring = [3120, 4470, 6010, 8300];
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
    put(bl, startI + i, s * env * amp * pl);
    put(br, startI + i, s * env * amp * pr);
  }
}
/** 长音镲(收尾/过门) */
function crash(bl, br, start, amp) { cymbal(bl, br, start, amp, 2.6, start % 2 < 1 ? -0.35 : 0.35); }
/** 锣: 低沉的不谐和金属音, 用于段落开头与结尾 */
function gong(bl, br, start, amp, base, dur) {
  const ratios = [1, 1.41, 1.93, 2.61, 3.44, 4.51, 5.9];
  const len = Math.floor((dur + 1.5) * SR), startI = S(start);
  let lp = 0;
  const st = ratios.map(() => 0);
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
/** 气声合唱(壮阔感) */
function breath(bl, br, pan, start, noteMidi, amp, dur, formants) {
  const f = midiHz(noteMidi);
  const [pl, pr] = panGains(pan);
  const len = Math.floor((dur + 1.2) * SR), startI = S(start);
  const F = formants || [620, 1250, 2500];
  const mk = () => { const w = 2 * Math.PI * 0; void w; return F.map(fc => { const ww = 2 * Math.PI * fc / SR; const c = Math.cos(ww); const al = Math.sin(ww) / (2 * 1.6); const a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; }); };
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

// ================= 和声 / 主题 =================
const CH = {
  Am: { bass: 33, low: 45, pad: [57, 60, 64] },
  F:  { bass: 29, low: 41, pad: [53, 57, 60] },
  G:  { bass: 31, low: 43, pad: [55, 59, 62] },
  Dm: { bass: 38, low: 50, pad: [50, 53, 57] },
  Bb: { bass: 34, low: 46, pad: [46, 53, 58] },
  E:  { bass: 28, low: 40, pad: [52, 56, 59] }
};
const PROG = [];
setProg(0, ['Am', 'F', 'G', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(8, ['Am', 'F', 'G', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(16, ['Am', 'F', 'G', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(24, ['Dm', 'Bb', 'F', 'E', 'Dm', 'Bb', 'F', 'E']);
setProg(32, ['Am', 'F', 'G', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(40, ['Am', 'F', 'G', 'E', 'Am', 'F', 'Dm', 'E']);
setProg(48, ['Am', 'Am', 'F', 'Am']);
function setProg(startBar, list) { list.forEach((c, i) => { PROG[startBar + i] = c; }); }
const chordOf = b => CH[PROG[b] || 'Am'];

// 主题: [小节偏移, 拍偏移, midi, 时值拍]
const THEME = [
  [0, 0, 76, 1], [0, 1, 76, 1], [0, 2, 79, 1.5], [0, 3.5, 77, 0.5],
  [1, 0, 74, 2], [1, 2, 72, 1], [1, 3, 69, 1],
  [2, 0, 71, 1], [2, 1, 74, 1], [2, 2, 79, 2],
  [3, 0, 80, 1.5], [3, 1.5, 79, 0.5], [3, 2, 76, 2],
  [4, 0, 76, 1], [4, 1, 79, 1], [4, 2, 81, 2],
  [5, 0, 81, 1], [5, 1, 79, 1], [5, 2, 77, 2],
  [6, 0, 74, 1], [6, 1, 77, 1], [6, 2, 81, 2],
  [7, 0, 80, 3]
];
// 过门(bridge)上行张力线
const BRIDGE = [
  [0, 0, 62, 1.5], [0, 1.5, 65, 0.5], [0, 2, 65, 2],
  [1, 0, 65, 1.5], [1, 1.5, 70, 0.5], [1, 2, 70, 2],
  [2, 0, 72, 2], [2, 2, 74, 2],
  [3, 0, 76, 4],
  [4, 0, 74, 1.5], [4, 1.5, 72, 0.5], [4, 2, 70, 2],
  [5, 0, 69, 2], [5, 2, 67, 2],
  [6, 0, 65, 2], [6, 2, 68, 2],
  [7, 0, 71, 4]
];

// ================= 声部 =================
/** 主题: 弦乐/铜管齐奏 + 古筝同度加倍 */
function playTheme(startBar, amp, oct, opts) {
  opts = opts || {};
  THEME.forEach(([b, bt, m, d]) => {
    const note = m + oct;
    const st = tBeat(startBar + b, bt);
    const hold = Math.max(0.12, d * BEAT * 0.92);
    // 主线: 弦乐 + 铜管
    tableVoice(musL, musR, -0.16, st, T_STR_MD, note, amp, 0.05, hold, 0.35, { detune: -6, vibHz: 5.2, vibCents: 10 });
    tableVoice(musL, musR, 0.16, st, T_BRASS, note, amp * 0.7, 0.07, hold, 0.35, { detune: 5 });
    tableVoice(musL, musR, 0, st, T_STR_MD, note - 12, amp * 0.42, 0.06, hold, 0.35, { detune: 3 });
    // 古筝(茶树音色)加倍
    if (opts.pluck !== false) {
      pluck(musL, musR, -0.3, st, note, amp * 0.85, Math.min(2.4, 0.9 + d * BEAT), 0.46);
      pluck(musL, musR, 0.32, st, note, amp * 0.6, Math.min(2.0, 0.8 + d * BEAT), 0.42);
    }
  });
}
/** 低音弦乐 ostinato: 每拍/半拍一个短音, 是"激昂"的发动机 */
function ostinato(bar, perBar, amp, oct) {
  const c = chordOf(bar);
  const step = 4 / perBar;
  for (let k = 0; k < perBar; k++) {
    const bt = k * step;
    const [pl, pr] = panGains(k % 2 ? 0.18 : -0.18);
    const st = tBeat(bar, bt);
    const accent = (k === 0) ? 1.25 : (k % 2 === 0 ? 0.95 : 0.78);
    const f = k === perBar - 1 ? c.low + 7 : c.low;   // 每小节末尾挑一个五度
    tableVoice(musL, musR, k % 2 ? 0.18 : -0.18, st, T_STR_LO, f + oct, amp * accent, 0.008, BEAT * step * 0.72, 0.10, { detune: 4 });
    void pl; void pr;
  }
}
/** 和声冲击(铜管 stabs) */
function stabs(bar, beats, amp) {
  const c = chordOf(bar);
  beats.forEach(bt => {
    const st = tBeat(bar, bt);
    c.pad.forEach((m, i) => {
      tableVoice(musL, musR, -0.5 + i * 0.5, st, T_BRASS, m - 12, amp * (i === 0 ? 1 : 0.8), 0.02, 0.35, 0.25, { detune: (i - 1) * 7 });
    });
  });
}
/** 长音铺底(弦乐持续音) */
function pad(bar, amp, atk, oct) {
  const c = chordOf(bar);
  c.pad.forEach((m, i) => {
    tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(bar, 0), T_STR_MD, m + oct, amp, atk, BAR * 0.9, 0.6, { detune: (i % 2 ? -7 : 7), vibHz: 4.0, vibCents: 9 });
  });
  tableVoice(musL, musR, 0, tBeat(bar, 0), T_STR_LO, c.bass + 12, amp * 0.9, atk * 1.2, BAR * 0.95, 0.8, { detune: 3 });
}
/** 鼓组: 打法由段落决定 */
function drums(bar, cfg) {
  // 底鼓
  (cfg.kick || [0, 1, 2, 3]).forEach(bt => kick(dryL, dryR, tBeat(bar, bt), 0.85));
  // 军鼓
  (cfg.snare || [1, 3]).forEach(bt => snare(dryL, dryR, tBeat(bar, bt), 0.42, bt === 1 ? -0.12 : 0.12));
  // 太鼓
  const per = cfg.taikoPerBar || 8;
  const step = 4 / per;
  for (let k = 0; k < per; k++) {
    const bt = k * step;
    if (cfg.skipTaiko && cfg.skipTaiko.includes(bt)) continue;
    const accent = bt % 1 === 0 ? 0.5 : 0.3;
    taiko(dryL, dryR, tBeat(bar, bt), accent * (cfg.taikoAmp || 1), bt % 2 === 0 ? 150 : 118, (k % 3 - 1) * 0.25);
  }
  // 镲
  if (cfg.crash) crash(dryL, dryR, tBeat(bar, 0), 0.30);
  if (cfg.splash) cymbal(dryL, dryR, tBeat(bar, cfg.splash), 0.16, 0.5, 0.3);
}

// ================= 装配 =================
console.log('装配中...');
// S1 0-15 主段 / S2 16-23 高八度 / S3 24-31 过门 / S4 32-47 高潮 / S5 48-51 收尾
// 铺底
for (let b = 0; b < 24; b++) pad(b, 0.075, 0.35, 0);
for (let b = 24; b < 32; b++) pad(b, 0.065, 0.5, 0);
for (let b = 32; b < 48; b++) pad(b, 0.095, 0.25, 0);
for (let b = 48; b < 52; b++) pad(b, 0.085, 0.30, 0);

// 低音 ostinato
for (let b = 0; b < 16; b++) ostinato(b, 8, 0.135, 0);
for (let b = 16; b < 24; b++) ostinato(b, 8, 0.145, 0);
for (let b = 24; b < 32; b++) ostinato(b, 4, 0.115, 0);    // 过门: 放宽成四分
for (let b = 32; b < 48; b++) ostinato(b, 8, 0.150, 0);
for (let b = 48; b < 51; b++) ostinato(b, 4, 0.110, 0);

// ---- S1 主段 (0-15) ----
drums(0, { kick: [0, 1, 2, 3], snare: [1, 3], taikoPerBar: 8, crash: true });
for (let b = 1; b < 16; b++) drums(b, { kick: [0, 1, 2, 3], snare: [1, 3], taikoPerBar: 8, taikoAmp: 1.0, crash: b % 4 === 0, splash: b % 4 === 3 ? 3.5 : undefined });
playTheme(0, 0.20, 0);
playTheme(8, 0.22, 0);
[3, 7, 11, 15].forEach(b => stabs(b, [3.5], 0.09));
[0, 4, 8, 12].forEach(b => gong(musL, musR, tBeat(b, 0), 0.10, 190, 1.6));

// ---- S2 高八度 (16-23) ----
for (let b = 16; b < 24; b++) drums(b, { kick: [0, 1, 2, 3], snare: [1, 3], taikoPerBar: 16, taikoAmp: 0.55, crash: b % 4 === 0, splash: 2.5 });
playTheme(16, 0.19, 12, { pluck: true });
breath(musL, musR, -0.5, tBeat(16, 0), 69, 0.055, BAR * 3.6, [620, 1250, 2500]);
breath(musL, musR, 0.5, tBeat(20, 0), 72, 0.05, BAR * 3.6, [600, 1200, 2400]);

// ---- S3 过门 (24-31): 半速鼓 + 张力上行 ----
for (let b = 24; b < 32; b++) {
  kick(dryL, dryR, tBeat(b, 0), 0.8);
  snare(dryL, dryR, tBeat(b, 2), 0.45, 0);
  taiko(dryL, dryR, tBeat(b, 0.5), 0.28, 130, -0.2);
  taiko(dryL, dryR, tBeat(b, 3.5), 0.24, 110, 0.25);
  if (b % 4 === 0) crash(dryL, dryR, tBeat(b, 0), 0.26);
}
stabs(25, [0, 2], 0.075);
stabs(27, [0, 2], 0.085);
BRIDGE.forEach(([b, bt, m, d]) => {
  const st = tBeat(24 + b, bt);
  const hold = Math.max(0.15, d * BEAT * 0.9);
  tableVoice(musL, musR, -0.18, st, T_STR_MD, m, 0.19, 0.05, hold, 0.4, { detune: -5, vibHz: 5.0, vibCents: 10 });
  tableVoice(musL, musR, 0.18, st, T_BRASS, m + 12, 0.10, 0.06, hold, 0.4, { detune: 6 });
  pluck(musL, musR, -0.3, st, m, 0.15, 2.0, 0.44);
});
// 过门末尾 16 分上行 + 镲滚
[64, 67, 71, 74, 76, 79, 83, 86].forEach((m, i) => {
  pluck(musL, musR, -0.5 + i * 0.14, tBeat(31, i * 0.5), m, 0.20, 1.6, 0.5);
});
for (let k = 0; k < 8; k++) cymbal(dryL, dryR, tBeat(31, 2 + k * 0.25), 0.10 + k * 0.012, 0.35, (k % 2 ? 1 : -1) * 0.4);

// ---- S4 高潮 (32-47) ----
for (let b = 32; b < 48; b++) {
  const fill = (b % 8 === 7);
  drums(b, {
    kick: fill ? [0, 1, 2, 3, 3.5] : [0, 1, 2, 3],
    snare: [1, 3, fill ? 3.75 : 3],
    taikoPerBar: 8, taikoAmp: 1.15,
    crash: b % 4 === 0, splash: (b % 4 === 3) ? 3.25 : undefined
  });
  if (b % 4 === 2) stabs(b, [2.5], 0.085);
}
playTheme(32, 0.26, 0);
playTheme(32, 0.13, 12, { pluck: false });
playTheme(40, 0.27, 0);
playTheme(40, 0.14, 12, { pluck: false });
breath(musL, musR, -0.55, tBeat(32, 0), 69, 0.06, BAR * 7, [640, 1300, 2600]);
breath(musL, musR, 0.55, tBeat(40, 0), 76, 0.055, BAR * 7, [620, 1250, 2500]);
tableVoice(musL, musR, -0.75, tBeat(36, 0), T_AIR, 93, 0.020, 4.0, BAR * 3.4, 3, { vibHz: 0.2, vibCents: 12 });
tableVoice(musL, musR, 0.75, tBeat(44, 0), T_AIR, 96, 0.018, 4.0, BAR * 3.4, 3, { vibHz: 0.18, vibCents: 14 });
[34, 38, 42, 46].forEach(b => gong(musL, musR, tBeat(b, 0), 0.085, 205, 1.4));

// ---- S5 收尾 (48-51) ----
for (let b = 48; b < 51; b++) {
  for (let k = 0; k < 8; k++) taiko(dryL, dryR, tBeat(b, k * 0.5), k % 2 === 0 ? 0.5 : 0.28, k % 2 === 0 ? 145 : 112, (k % 3 - 1) * 0.3);
  kick(dryL, dryR, tBeat(b, 0), 0.8);
  snare(dryL, dryR, tBeat(b, 2), 0.4, 0);
}
crash(dryL, dryR, tBeat(48, 0), 0.32);
gong(musL, musR, tBeat(48, 0), 0.12, 150, 2.4);
// 终止和弦(全奏) + 大锣
const endBar = 50;
const ec = CH.Am;
ec.pad.forEach((m, i) => {
  tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(endBar, 0), T_BRASS, m - 12, 0.16, 0.02, BAR * 0.8, 1.2, { detune: (i - 1) * 8 });
  tableVoice(musL, musR, -0.45 + i * 0.45, tBeat(endBar, 0), T_STR_MD, m, 0.10, 0.04, BAR * 0.8, 1.2, { detune: (i - 1) * 6 });
});
tableVoice(musL, musR, 0, tBeat(endBar, 0), T_STR_LO, 33, 0.16, 0.02, BAR * 0.9, 1.4, { detune: 4 });
pluck(musL, musR, 0, tBeat(endBar, 0), 69, 0.30, 3.0, 0.5);
pluck(musL, musR, -0.35, tBeat(endBar, 0.25), 76, 0.24, 3.0, 0.5);
pluck(musL, musR, 0.35, tBeat(endBar, 0.5), 81, 0.20, 3.0, 0.5);
kick(dryL, dryR, tBeat(endBar, 0), 1.0);
crash(dryL, dryR, tBeat(endBar, 0), 0.36);
gong(musL, musR, tBeat(endBar, 0), 0.16, 130, 3.2);
for (let k = 0; k < 6; k++) taiko(dryL, dryR, tBeat(endBar, 1 + k * 0.5), 0.34, 120, (k % 2 ? 0.3 : -0.3));

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
const PRE = S(0.022);
const sendL = new Float32Array(N), sendR = new Float32Array(N);
for (let i = PRE; i < N; i++) {
  sendL[i] = musL[i - PRE] * 0.30 + dryL[i - PRE] * 0.085;
  sendR[i] = musR[i - PRE] * 0.30 + dryR[i - PRE] * 0.085;
}
const revL = reverbChannel(sendL, 0, 0.80, 0.25, 0.05);
const revR = reverbChannel(sendR, 23, 0.80, 0.25, 0.05);
function rmsOf(a) { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); }
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.5, dryRms * 0.34 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

// ================= 母线 =================
const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) {
  outL[i] = musL[i] + dryL[i] + revL[i] * wetGain;
  outR[i] = musR[i] + dryR[i] + revR[i] * wetGain;
}
// 淡入/淡出
const fadeIn = S(0.25), fadeOut = S(4.5);
for (let i = 0; i < N; i++) {
  let g = 1;
  if (i < fadeIn) g *= i / fadeIn;
  const endI = N - S(0.25);
  if (i > N - fadeOut) g *= Math.max(0, (endI - i) / (fadeOut - S(0.25)));
  if (i >= endI) g = 0;
  outL[i] *= g; outR[i] *= g;
}
// EQ: 减低频浑浊 / 提亮
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
const EQ_CHAIN = [hpCoeff(34, 0.707), peakCoeff(200, 1.1, -3.0), peakCoeff(700, 0.9, 1.2), shelfCoeff(3000, 2.5)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

// 响度: 峰值留过头给软饱和, 主体抬起来
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
// 目标响度: 战斗曲要比其它 BGM 略跑在前, 但不至于炸耳
let _s0 = 0; for (let i = 0; i < N; i++) _s0 += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const rms0 = Math.sqrt(_s0 / (2 * N));
const TARGET_RMS = 0.115;
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));

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
