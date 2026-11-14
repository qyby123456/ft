/*
 * 茶树Boss 战斗BGM —— 「安魂弥撒 / 最终决战」版（完全重写，不沿用前几版任何作法）
 * 参考取向: Black Souls II OST - Theatre of Darkness, the Grand Guignol（该作的最终 Boss 战曲）
 * 音乐语言: 以中世纪圣咏《Dies Irae(末日经)》动机为全曲主题 —— 丧钟与管风琴开场,
 *          中段 4/4 重型推进, 女高音独唱与男声低吟合唱, 弦乐不谐和音簇, 定音鼓与军鼓滚奏, 锣收束。
 * 速度分段: 72 BPM(葬仪) → 116 BPM(决战) → 88 BPM(终曲), d小调, 64 小节 ≈ 2:28
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;

/* ---------- 分段变速: 小节 -> 绝对秒 ---------- */
const SECTIONS = [{ bar: 0, bpm: 72 }, { bar: 8, bpm: 116 }, { bar: 56, bpm: 88 }];
const BARS = 64;
const TAIL = 6;
const bpmAt = bar => { let b = SECTIONS[0].bpm; for (const s of SECTIONS) if (bar >= s.bar) b = s.bpm; return b; };
const barDur = bar => 240 / bpmAt(bar);            // 4/4
function barStart(bar) {
  let t = 0;
  for (let i = 0; i < SECTIONS.length; i++) {
    const s = SECTIONS[i];
    const next = SECTIONS[i + 1] ? SECTIONS[i + 1].bar : Infinity;
    if (bar < next) return t + (bar - s.bar) * barDur(s.bar);
    t += (next - s.bar) * barDur(s.bar);
  }
  return t;
}
const tBeat = (bar, beat) => barStart(bar) + beat * (60 / bpmAt(bar));
const N = Math.ceil((barStart(BARS) + TAIL) * SR);
const S = t => Math.round(t * SR);

const rnd = (() => { let a = 20260910; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);

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

/* ---------- 波表 ---------- */
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
const T_ORGAN = makeTable([1, 0.55, 0.34, 0.24, 0.16, 0.11, 0.08, 0.055, 0.04, 0.028, 0.02, 0.014]);   // 全管风琴
const T_ORGAN_P = makeTable([1, 0.3, 0.5, 0.12, 0.06]);                                                // 低音踏板(偏奇次)
const T_STR = makeTable(sawH(32, 18));
const T_STR_DK = makeTable(sawH(28, 9));
const T_SAW = makeTable(sawH(40, 26));                                                                 // 人声源(明亮锯齿)
const T_BRASS = makeTable([1, 0.66, 0.5, 0.36, 0.26, 0.18, 0.13, 0.09, 0.06, 0.04]);
const T_AIR = makeTable([1, 0.06, 0.03, 0.012]);

function tableVoice(bl, br, pan, start, table, freq, amp, atk, hold, rel, opt) {
  opt = opt || {};
  const det = opt.detune || 0, vibHz = opt.vibHz || 0, vibC = opt.vibCents || 0;
  const tremHz = opt.tremHz || 0, tremDepth = opt.tremDepth || 0;
  const [pl, pr] = panGains(pan);
  const total = Math.ceil((atk + hold + rel) * SR);
  const inc0 = midiHz(freq) * Math.pow(2, det / 1200) / SR * SIZE;
  let phase = rnd() * SIZE;
  const startI = S(start);
  for (let i = 0; i < total; i++) {
    const t = i / SR;
    let a = adsr(t, atk + hold, atk, 0.06, 1, rel);
    if (a <= 0) continue;
    if (tremDepth) a *= 1 - tremDepth + tremDepth * (0.5 + 0.5 * Math.sin(2 * Math.PI * tremHz * t));
    const inc = vibC ? inc0 * Math.pow(2, vibC * Math.sin(2 * Math.PI * vibHz * t) / 1200) : inc0;
    const i0 = phase | 0, fr = phase - i0;
    const s0 = table[i0], s1 = table[(i0 + 1) & (SIZE - 1)];
    const s = (s0 + (s1 - s0) * fr) * a * amp;
    phase += inc; if (phase >= SIZE) phase -= SIZE;
    const j = startI + i;
    put(bl, j, s * pl); put(br, j, s * pr);
  }
}

/* ---------- 丧钟(大钟): 非谐分音 + 长衰减 ---------- */
function bell(bl, br, start, amp, note, durSec) {
  const f = midiHz(note);
  const parts = [
    [0.5, 1.00, 1.0],    // hum 低鸣(记谱音下方八度)
    [1.0, 0.85, 1.5],    // 基音
    [1.19, 0.55, 1.1],   // tierce
    [1.56, 0.42, 0.9],
    [2.00, 0.34, 0.8],   // 八度
    [2.51, 0.24, 0.6],
    [2.66, 0.20, 0.55],  // 小三度上方
    [3.01, 0.14, 0.45],
    [4.10, 0.08, 0.3]
  ];
  const dur = durSec || 5.0;
  const len = Math.floor((dur + 0.5) * SR), startI = S(start);
  let lp = 0;
  const ph = parts.map(() => 0);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < parts.length; k++) {
      const [ratio, a0, rel] = parts[k];
      const a = a0 * Math.exp(-t / (dur * 0.30 * rel)) * (1 - Math.exp(-t / 0.004));
      ph[k] += 2 * Math.PI * f * ratio / SR;
      s += Math.sin(ph[k]) * a;
    }
    const w = rnd() * 2 - 1; lp += 0.35 * (w - lp);
    s += lp * Math.exp(-t / 0.006) * 0.55;            // 钟槌撞击
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}

/* ---------- 人声: 锯齿源 + 三个共振峰 ---------- */
const VOWEL = { a: [800, 1150, 2900], o: [500, 1000, 2400], u: [340, 700, 2300], e: [520, 1800, 2500], i: [300, 2200, 3000] };
function voice(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const v = VOWEL[opt.vowel || 'a'];
  const vibHz = opt.vibHz || 5.0, vibC = opt.vibCents || 18;
  const [pl, pr] = panGains(pan);
  const f = midiHz(note);
  const atk = opt.atk !== undefined ? opt.atk : 0.35;
  const rel = opt.rel !== undefined ? opt.rel : 0.6;
  const len = Math.floor((durSec + rel + 0.2) * SR), startI = S(start);
  // 每共振峰一个并联带通(RBJ), Q 越大越"唱"
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const gain = [1.0, 0.5, 0.22];
  const cL = v.map((fc, k) => mk(fc, [9, 11, 13][k]));
  const cR = v.map((fc, k) => mk(fc * 1.008, [9, 11, 13][k]));
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  let ph = rnd();
  const inc0 = f / SR * SIZE;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, durSec, atk, 0.2, 1, rel);
    if (a <= 0) continue;
    const inc = inc0 * Math.pow(2, vibC * Math.sin(2 * Math.PI * vibHz * t) * (t > 0.25 ? 1 : t / 0.25) / 1200);
    const i0 = ph | 0, fr = ph - i0;
    const src = (T_SAW[i0] + (T_SAW[(i0 + 1) & (SIZE - 1)] - T_SAW[i0]) * fr) * 0.8;
    ph += inc; if (ph >= SIZE) ph -= SIZE;
    let oL = 0, oR = 0;
    for (let k = 0; k < 3; k++) {
      let o = cL[k].b0 * src + sL[k].z1;
      sL[k].z1 = cL[k].b2 * src - cL[k].a1 * o - cL[k].a2 * sL[k].z2; sL[k].z2 = o;
      oL += o * gain[k];
      let o2 = cR[k].b0 * src + sR[k].z1;
      sR[k].z1 = cR[k].b2 * src - cR[k].a1 * o2 - cR[k].a2 * sR[k].z2; sR[k].z2 = o2;
      oR += o2 * gain[k];
    }
    const j = startI + i;
    put(bl, j, oL * a * amp * pl * 5.5); put(br, j, oR * a * amp * pr * 5.5);
  }
}
/** 合唱声部: 一组人声, 略带随机错位 */
function choir(start, notes, amp, dur, vowel, spread) {
  notes.forEach((m, i) => {
    const pan = spread ? (-0.55 + i * (1.1 / Math.max(1, notes.length - 1))) : 0;
    voice(musL, musR, pan, start + 0.02 * i, m, amp * (i === 0 ? 1 : 0.85), dur, { vowel, vibHz: 4.4 + i * 0.2, vibCents: 14 });
  });
}

/* ---------- 不谐和音簇(弦乐) ---------- */
function cluster(start, durSec, lo, hi, amp) {
  for (let m = lo; m <= hi; m++) {
    const pan = -0.6 + (m - lo) / Math.max(1, hi - lo) * 1.2;
    tableVoice(musL, musR, pan, start, T_STR, m, amp, durSec * 0.55, durSec * 0.35, 0.9, { detune: (m % 2 ? -9 : 9), vibHz: 2.2, vibCents: 6 });
  }
}

/* ---------- 打击 ---------- */
function timp(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note);
  const dur = 1.0, len = Math.floor(dur * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * 0.9 + f0 * 0.16 * Math.exp(-t / 0.05);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.34) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.10 * (w - lp);
    s += lp * Math.exp(-t / 0.012) * 0.4;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
function timpRoll(bl, br, start, durSec, amp, note) {
  const step = 0.055;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    timp(bl, br, start + t, amp * (0.18 + 0.5 * x) * (1 + 0.15 * Math.sin(t * 40)), note, (x - 0.5) * 0.5);
  }
}
function snare(bl, br, start, amp, pan) {
  const len = Math.floor(0.26 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const hp = 0.93 * (yPrev + w - wPrev); yPrev = hp; wPrev = w;
    ph += 2 * Math.PI * 188 / SR;
    const s = (hp * Math.exp(-t / 0.062) * 0.9 + Math.sin(ph) * Math.exp(-t / 0.04) * 0.35) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
function snareRoll(bl, br, start, durSec, amp) {
  const step = 0.0625;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    snare(bl, br, start + t, amp * (0.25 + 0.75 * x), Math.sin(t * 31) * 0.45);
  }
}
function bassDrum(bl, br, start, amp) {
  const dur = 0.5, len = Math.floor(dur * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 44 + (150 - 44) * Math.exp(-t / 0.026);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.13) * (1 - Math.exp(-t / 0.0014));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.22 * (w - lp);
    s += lp * Math.exp(-t / 0.007) * 0.4;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
function cymbal(bl, br, start, amp, dur, pan) {
  const len = Math.floor((dur + 0.5) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0;
  const ring = [3050, 4300, 5800, 7900];
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
  const ratios = [1, 1.43, 1.97, 2.63, 3.47, 4.55, 5.93];
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

/* ================= 和声(安魂曲式) ================= */
const CH = {
  Dm: { bass: 26, pad: [62, 65, 69] },    // D2 记谱低音写到 D1
  Gm: { bass: 31, pad: [62, 67, 70] },
  A7: { bass: 33, pad: [61, 64, 67] },
  Bb: { bass: 34, pad: [58, 62, 65] },
  Bbm: { bass: 34, pad: [58, 61, 65] },   // iv 小三(悲怆)
  Fsh: { bass: 30, pad: [62, 66, 69] },   // F#dim
  Eb: { bass: 27, pad: [58, 63, 67] },    // ♭II 那不勒斯
  C: { bass: 24, pad: [60, 64, 67] },
  D: { bass: 26, pad: [62, 66, 69] }      // 终曲皮卡迪三度
};
const PROG = [];
const setProg = (b0, list) => list.forEach((c, i) => { PROG[b0 + i] = c; });
setProg(0, ['Dm', 'Dm', 'Bbm', 'Bbm', 'Gm', 'Gm', 'A7', 'A7']);
setProg(8, ['Dm', 'Dm', 'Gm', 'A7', 'Dm', 'Bb', 'Eb', 'A7', 'Dm', 'Bbm', 'Gm', 'A7', 'Dm', 'Bb', 'A7', 'Dm',
            'Dm', 'Dm', 'Gm', 'A7', 'Dm', 'Bb', 'Eb', 'A7']);
setProg(32, ['Dm', 'Dm', 'Bbm', 'Bbm', 'Gm', 'Gm', 'A7', 'A7']);
setProg(40, ['Dm', 'Dm', 'Gm', 'A7', 'Dm', 'Bb', 'Eb', 'A7', 'Dm', 'Bbm', 'Gm', 'A7', 'Dm', 'Bb', 'A7', 'Dm']);
setProg(56, ['Dm', 'Bbm', 'Gm', 'A7', 'Dm', 'Dm', 'A7', 'D']);
const chordOf = b => CH[PROG[b] || 'Dm'];

/* ---- 《Dies Irae(末日经)》主题: [小节, 拍, midi] ---- */
const DIES = [
  [0, 0, 74], [0, 1, 74], [0, 2, 75], [0, 3, 74],
  [1, 0, 72], [1, 1, 74], [1, 2, 75], [1, 3, 74],
  [2, 0, 72], [2, 1, 70], [2, 2, 69], [2, 3, 70],
  [3, 0, 72], [3, 1, 74], [3, 2, 75], [3, 3, 77],
  [4, 0, 74], [4, 1, 75], [4, 2, 77], [4, 3, 75],
  [5, 0, 74], [5, 1, 72], [5, 2, 70], [5, 3, 72],
  [6, 0, 74], [6, 1, 73], [6, 2, 71], [6, 3, 69],
  [7, 0, 70, 4]
];
/** 主题陈述: 铜管/弦乐齐奏 + 齐唱 */
function playDies(startBar, amp, opt) {
  opt = opt || {};
  const oct = opt.oct || 0;
  DIES.forEach(([b, bt, m, d]) => {
    const note = m + oct, st = tBeat(startBar + b, bt);
    const hold = (d || 1) * (60 / bpmAt(startBar + b)) * 0.88;
    tableVoice(musL, musR, -0.14, st, T_BRASS, note, amp, 0.05, hold, 0.22, { detune: -5 });
    tableVoice(musL, musR, 0.14, st, T_STR, note, amp * 0.75, 0.06, hold, 0.22, { detune: 6, vibHz: 5.0, vibCents: 8 });
    if (!opt.noLow) tableVoice(musL, musR, 0, st, T_STR_DK, note - 12, amp * 0.5, 0.06, hold, 0.22, { detune: 2 });
  });
}
/** 低音推进: 8 分音符 */
function bassDrive(bar, amp, perBar) {
  const c = chordOf(bar), step = 4 / perBar;
  for (let k = 0; k < perBar; k++) {
    const bt = k * step;
    tableVoice(musL, musR, 0, tBeat(bar, bt), T_ORGAN_P, c.bass, amp * (k === 0 ? 1.25 : 0.85), 0.01, (60 / bpmAt(bar)) * step * 0.7, 0.1, { detune: 3 });
  }
}
/** 管风琴和声(带轻微颤音 tremulant) */
function organChord(bar, amp, oct) {
  const c = chordOf(bar), st = tBeat(bar, 0);
  c.pad.forEach((m, i) => {
    tableVoice(musL, musR, -0.5 + i * 0.5, st, T_ORGAN, m + (oct || 0), amp * (i === 0 ? 1 : 0.8), 0.12, barDur(bar) * 0.9, 0.3, { detune: (i - 1) * 4, tremHz: 5.2, tremDepth: 0.10 });
  });
  tableVoice(musL, musR, 0, st, T_ORGAN_P, c.bass, amp * 1.15, 0.06, barDur(bar) * 0.95, 0.3, { detune: 2 });
}

/* ================= 装配 ================= */
console.log('装配中...');

/* ---- I. 葬仪 (0-7, 72 BPM): 丧钟与管风琴 ---- */
bell(musL, musR, tBeat(0, 0), 0.30, 50, 6.5);
organChord(0, 0.085);
for (let b = 1; b < 8; b++) organChord(b, 0.075);
bell(musL, musR, tBeat(2, 0), 0.24, 50, 6.0);
bell(musL, musR, tBeat(4, 0), 0.26, 50, 6.0);
bell(musL, musR, tBeat(6, 0), 0.28, 50, 6.5);
gong(musL, musR, tBeat(0, 0), 0.10, 118, 3.5);
choir(tBeat(2, 0), [38, 45, 50], 0.055, barDur(2) * 4.5, 'o');           // 男声低吟 D2 A2 D3
choir(tBeat(4, 0), [41, 46, 50], 0.050, barDur(4) * 3.5, 'u');
// 女高音独唱: 慢速末日经 (第一乐句)
[[3, 0, 74], [3, 2, 75], [4, 0, 74], [4, 2, 72], [5, 0, 74], [5, 2, 75], [6, 0, 74, 2], [7, 0, 72, 3]].forEach(([b, bt, m, d]) => {
  voice(musL, musR, -0.25, tBeat(b, bt), m, 0.115, (d || 2) * (60 / bpmAt(b)) * 0.92, { vowel: 'a', vibHz: 5.0, vibCents: 22, atk: 0.35, rel: 0.7 });
});
cluster(tBeat(5, 0), barDur(5) * 2.6, 61, 66, 0.035);                     // 弦乐不谐和音簇渐强
timpRoll(dryL, dryR, tBeat(6, 0), barDur(6) * 1.9, 0.55, 26);
cymbal(dryL, dryR, tBeat(7, 0), 0.16, 2.6, 0.3);

/* ---- II. 决战主段 (8-31, 116 BPM) ---- */
for (let b = 8; b < 32; b++) {
  const p2 = b >= 24;                                     // 后半更凶
  organChord(b, p2 ? 0.105 : 0.09);
  bassDrive(b, p2 ? 0.16 : 0.145, 8);
  const c = chordOf(b);
  timp(dryL, dryR, tBeat(b, 0), 0.42, c.bass + 12, (b % 2 ? 0.22 : -0.22));
  timp(dryL, dryR, tBeat(b, 2), 0.26, c.bass + 19, (b % 4 < 2 ? -0.3 : 0.3));
  if (b % 2 === 1) bassDrum(dryL, dryR, tBeat(b, 2), 0.55);
  if (b % 4 === 0) cymbal(dryL, dryR, tBeat(b, 0), 0.18, 1.6, b % 8 ? -0.35 : 0.35);
  snareRoll(dryL, dryR, tBeat(b, 3), (60 / bpmAt(b)) * 0.9, p2 ? 0.14 : 0.10);
  if (b % 4 === 3) snare(dryL, dryR, tBeat(b, 3.5), 0.34, 0.25);
  if (b % 8 === 0) bell(musL, musR, tBeat(b, 0), 0.16, 50, 5.5);
}
playDies(8, 0.17);
playDies(16, 0.185);
playDies(24, 0.20);
choir(tBeat(8, 0), [50, 57, 62, 65], 0.045, barDur(8) * 7, 'o');
choir(tBeat(16, 0), [50, 57, 62, 65], 0.050, barDur(16) * 7, 'a');
choir(tBeat(24, 0), [50, 57, 62, 65], 0.055, barDur(24) * 7, 'a');
// 每段末的铜管推进
for (const b of [15, 23, 31]) {
  const c = chordOf(b);
  [3.5, 4].forEach(bt => c.pad.forEach((m, i) => tableVoice(musL, musR, -0.4 + i * 0.4, tBeat(b, bt), T_BRASS, m - 12, 0.085, 0.02, (60 / bpmAt(b)) * 0.45, 0.1, { detune: (i - 1) * 6 })));
}

/* ---- III. 静场 (32-39): 只剩钟、管风琴与独唱 ---- */
bell(musL, musR, tBeat(32, 0), 0.28, 50, 6.5);
bell(musL, musR, tBeat(36, 0), 0.26, 50, 6.0);
gong(musL, musR, tBeat(32, 0), 0.12, 130, 3.5);
for (let b = 32; b < 40; b++) organChord(b, 0.075);
choir(tBeat(32, 0), [38, 45, 50], 0.055, barDur(32) * 5, 'u');
choir(tBeat(36, 0), [39, 46, 51], 0.050, barDur(36) * 3.5, 'o');
[[32, 0, 74], [32, 2, 72], [33, 0, 74], [33, 2, 75], [34, 0, 74, 2], [35, 0, 72, 3],
 [36, 0, 75], [36, 2, 74], [37, 0, 72], [37, 2, 70], [38, 0, 69, 3], [39, 0, 70, 4]].forEach(([b, bt, m, d]) => {
  voice(musL, musR, -0.2, tBeat(b, bt), m, 0.105, (d || 2) * (60 / bpmAt(b)) * 0.9, { vowel: 'a', vibHz: 5.2, vibCents: 20, atk: 0.3, rel: 0.6 });
});
cluster(tBeat(38, 0), barDur(38) * 1.9, 62, 67, 0.032);
timpRoll(dryL, dryR, tBeat(39, 0), barDur(39) * 0.95, 0.55, 26);
snareRoll(dryL, dryR, tBeat(38, 2), barDur(38) * 1.9, 0.16);

/* ---- IV. 最终高潮 (40-55): 全奏 + 女高音华彩 ---- */
for (let b = 40; b < 56; b++) {
  organChord(b, 0.12);
  bassDrive(b, 0.175, 8);
  const c = chordOf(b);
  timp(dryL, dryR, tBeat(b, 0), 0.5, c.bass + 12, (b % 2 ? 0.24 : -0.24));
  timp(dryL, dryR, tBeat(b, 1.5), 0.24, c.bass + 19, 0.3);
  timp(dryL, dryR, tBeat(b, 2.5), 0.24, c.bass + 19, -0.3);
  bassDrum(dryL, dryR, tBeat(b, 0), 0.6);
  if (b % 2 === 1) bassDrum(dryL, dryR, tBeat(b, 2), 0.5);
  if (b % 4 === 0) cymbal(dryL, dryR, tBeat(b, 0), 0.22, 1.8, b % 8 ? -0.35 : 0.35);
  snareRoll(dryL, dryR, tBeat(b, 3), (60 / bpmAt(b)) * 0.95, 0.17);
  if (b % 4 === 3) snare(dryL, dryR, tBeat(b, 3.5), 0.36, -0.25);
  if (b % 8 === 0) bell(musL, musR, tBeat(b, 0), 0.18, 50, 5.5);
}
playDies(40, 0.22);
playDies(48, 0.23);
playDies(40, 0.10, { oct: 12, noLow: true });
playDies(48, 0.11, { oct: 12, noLow: true });
choir(tBeat(40, 0), [50, 57, 62, 65, 69], 0.052, barDur(40) * 7.5, 'a');
choir(tBeat(48, 0), [50, 57, 62, 65, 69], 0.056, barDur(48) * 7.5, 'a');
// 女高音长音华彩(高音区, 长气息)
[[40, 0, 86, 4], [42, 0, 84, 4], [44, 0, 82, 6], [47, 0, 81, 4],
 [48, 0, 86, 4], [50, 0, 84, 4], [52, 0, 82, 4], [54, 0, 81, 8]].forEach(([b, bt, m, d]) => {
  voice(musL, musR, 0.35, tBeat(b, bt), m, 0.075, d * (60 / bpmAt(b)) * 0.95, { vowel: 'e', vibHz: 5.6, vibCents: 24, atk: 0.5, rel: 1.0 });
});
[42, 46, 50, 54].forEach(b => gong(musL, musR, tBeat(b, 0), 0.09, 175, 1.6));

/* ---- V. 终曲 (56-63, 88 BPM): 大终止 + 皮卡迪三度 ---- */
for (let b = 56; b < 64; b++) {
  organChord(b, 0.115);
  const c = chordOf(b);
  timp(dryL, dryR, tBeat(b, 0), 0.48, c.bass + 12, (b % 2 ? 0.2 : -0.2));
  if (b % 2 === 1) bassDrum(dryL, dryR, tBeat(b, 2), 0.5);
}
playDies(56, 0.19);
bell(musL, musR, tBeat(56, 0), 0.26, 50, 6.5);
bell(musL, musR, tBeat(60, 0), 0.30, 50, 7.0);
choir(tBeat(56, 0), [38, 45, 50, 57], 0.060, barDur(56) * 6, 'o');
choir(tBeat(60, 0), [50, 57, 62, 66], 0.055, barDur(60) * 3, 'a');
// 最后: Dm -> D 大三(皮卡迪三度) 终止
const EB = 62;
bell(musL, musR, tBeat(EB, 0), 0.34, 50, 8.0);
gong(musL, musR, tBeat(EB, 0), 0.20, 130, 4.0);
bassDrum(dryL, dryR, tBeat(EB, 0), 0.7);
timp(dryL, dryR, tBeat(EB, 0), 0.6, 26, 0);
[62, 65, 69].forEach((m, i) => tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(EB, 0), T_ORGAN, m, 0.12, 0.02, barDur(EB) * 1.6, 2.5, { detune: (i - 1) * 5, tremHz: 5.0, tremDepth: 0.08 }));
[62, 66, 69].forEach((m, i) => tableVoice(musL, musR, -0.4 + i * 0.4, tBeat(EB, 0), T_ORGAN, m, 0.10, 0.03, barDur(EB) * 1.8, 3.0, { detune: (i - 1) * 5 }));   // 大三度(皮卡迪)
tableVoice(musL, musR, 0, tBeat(EB, 0), T_ORGAN_P, 26, 0.16, 0.02, barDur(EB) * 2, 3.0);
choir(tBeat(EB, 0), [38, 45, 50, 62, 66], 0.065, barDur(EB) * 2, 'a');
cymbal(dryL, dryR, tBeat(EB, 0), 0.24, 2.4, 0);

/* ================= 混响 / 母带 ================= */
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
const PRE = S(0.03);
const sendL = new Float32Array(N), sendR = new Float32Array(N);
for (let i = PRE; i < N; i++) {
  sendL[i] = musL[i - PRE] * 0.36 + dryL[i - PRE] * 0.11;
  sendR[i] = musR[i - PRE] * 0.36 + dryR[i - PRE] * 0.11;
}
const revL = reverbChannel(sendL, 0, 0.86, 0.20, 0.05);   // 大教堂般的长尾
const revR = reverbChannel(sendR, 23, 0.86, 0.20, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.5, dryRms * 0.42 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) {
  outL[i] = musL[i] + dryL[i] + revL[i] * wetGain;
  outR[i] = musR[i] + dryR[i] + revR[i] * wetGain;
}
const fadeIn = S(0.4), fadeOut = S(5.0);
for (let i = 0; i < N; i++) {
  let g = 1;
  if (i < fadeIn) g *= i / fadeIn;
  const endI = N - S(0.3);
  if (i > N - fadeOut) g *= Math.max(0, (endI - i) / (fadeOut - S(0.3)));
  if (i >= endI) g = 0;
  outL[i] *= g; outR[i] *= g;
}
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
const EQ_CHAIN = [hpCoeff(30, 0.707), peakCoeff(220, 1.1, -3.2), peakCoeff(1000, 0.9, 1.4), shelfCoeff(3400, 2.5)];
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
const TARGET_RMS = 0.105;
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'requiem.wav');
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
console.log(JSON.stringify({ file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, sections: SECTIONS, peak, norm: +norm.toFixed(3), satSamples: sat, wetGain: +wetGain.toFixed(3), finalRms: +finalRms.toFixed(4), profile: rows }, null, 0));
