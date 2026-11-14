/*
 * 沙耶(茶树Boss) 专属战斗BGM —— 「茶の唄」版 (v6, 完全原创)
 * 参考取向: 《沙耶の唄》(Saya no Uta) 主题曲的语汇 —— 缓慢、哀伤、空灵的女声叙事曲,
 *           音乐盒摇篮动机 + 暗色弦乐铺底 + 心跳般的低频脉动 + 崩坏段的不谐和音簇。
 *
 * 重要: 参考曲是商业版权作品, 这里**不复制其旋律**, 只沿用"悲恋叙事曲"这一体裁语汇,
 *       旋律/和声/配器全部为本项目另写。
 *
 * 与既有 5 版的差别:
 *   节拍   : 6/8 摇篮摆动           (v5=2/4加洛普, v3=3/4圆舞曲, 其余=4/4)
 *   调性   : b 小调, 含 ♭II(C) 那不勒斯与 F#7 的 A# 张力   (v2=a, v3/v4=d, v5=e)
 *   主体   : 女声独唱(共振峰合成) + 音乐盒  (v4 只把合唱当色彩, 从未以独唱为主体)
 *   脉动   : 心跳(而非战鼓) + 低音持续音drone
 *   结构   : 摇篮 → 唄I → 崩坏 → 静 → 唄II → 终  (6 段, 133s + 尾)
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const TAIL = 8.0;

/* ================= 时间轴 ================= */
const METER = [];
const SEC = {};
let _b = 0;
function sec(name, count, beats, bpm) {
  const o = { start: _b, count, beats };
  for (let i = 0; i < count; i++) METER.push({ beats, bpm });
  _b += count;
  SEC[name] = o;
  return o;
}
sec('A', 8, 3, 126);    // 摇篮
sec('B', 20, 3, 132);   // 唄 I
sec('C', 20, 3, 168);   // 崩坏
sec('D', 8, 3, 120);    // 静
sec('E', 28, 3, 132);   // 唄 II
sec('F', 8, 3, 108);    // 终

const BARS = METER.length;
const tBar = [];
let TOTAL = 0;
for (const m of METER) { tBar.push(TOTAL); TOTAL += m.beats * 60 / m.bpm; }
const barDur = bar => { const b = Math.max(0, Math.min(METER.length - 1, Math.floor(bar))); return METER[b].beats * 60 / METER[b].bpm; };
const bpmAt = bar => METER[Math.max(0, Math.min(METER.length - 1, Math.floor(bar)))].bpm;
const tBeat = (bar, q) => {
  const b0 = Math.floor(bar), fr = bar - b0;
  const bb = Math.max(0, Math.min(METER.length - 1, b0));
  return tBar[bb] + fr * barDur(bb) + q * 60 / METER[bb].bpm;
};

const N = Math.ceil((TOTAL + TAIL) * SR);
const S = t => Math.round(t * SR);

const rnd = (() => { let a = 20260912; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
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

/* ================= 波表 ================= */
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
const T_STR = makeTable(sawH(32, 16));
const T_STR_DK = makeTable(sawH(26, 7));
const T_SAW = makeTable(sawH(40, 26));
const T_ORGAN = makeTable([1, 0.5, 0.32, 0.2, 0.13, 0.08, 0.05]);

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

/* ================= 打击 ================= */
function timp(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note);
  const len = Math.floor(1.0 * SR), startI = S(start);
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
function bell(bl, br, start, amp, note, durSec) {
  const f = midiHz(note);
  const parts = [[0.5, 1.00, 1.0], [1.0, 0.85, 1.5], [1.19, 0.55, 1.1], [1.56, 0.42, 0.9], [2.00, 0.34, 0.8], [2.51, 0.24, 0.6], [2.66, 0.20, 0.55], [3.01, 0.14, 0.45], [4.10, 0.08, 0.3]];
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
    s += lp * Math.exp(-t / 0.006) * 0.55;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}

/* ================= v6 音色 ================= */
/** 音乐盒: 高频非谐分音 + 极短"齿音"瞬态, 长衰减但很轻 —— 摇篮动机的音色 */
function musicBox(bl, br, pan, start, note, amp, opt) {
  opt = opt || {};
  const det = opt.detune || 0;
  const f = midiHz(note) * Math.pow(2, det / 1200);
  const ratios = [1, 2.02, 3.11, 4.37, 5.61];
  const dec = [0.9, 0.42, 0.22, 0.13, 0.08];
  const len = Math.floor(2.2 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ph = ratios.map(() => 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      ph[k] += 2 * Math.PI * f * ratios[k] / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / dec[k]) / (1 + k * 1.2);
    }
    const w = rnd() * 2 - 1;
    hp = 0.86 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
    s += hp * Math.exp(-t / 0.0025) * 0.5;                 // 齿音打击
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 心跳: 一次"咚"（低频正弦 + 极短包络） */
function heart(start, amp, pan) {
  const len = Math.floor(0.30 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 52 + 30 * Math.exp(-t / 0.03);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.075) * (1 - Math.exp(-t / 0.004));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.12 * (w - lp);
    s += lp * Math.exp(-t / 0.01) * 0.35;
    put(dryL, startI + i, s * amp * pl); put(dryR, startI + i, s * amp * pr);
  }
}
/** 柔音钢琴: 轻微非谐性, 无走音 */
function pianoSoft(bl, br, pan, start, note, amp, durSec) {
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note);
  const len = Math.floor((durSec + 0.3) * SR), startI = S(start);
  const B = 0.0004;
  const ph = [0, 0, 0];
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < 3; k++) {
      const f = f0 * (k + 1) * (1 + B * (k + 1) * (k + 1));
      ph[k] += 2 * Math.PI * f / SR;
      s += Math.sin(ph[k]) * [1, 0.36, 0.16][k];
    }
    const e = Math.exp(-t / (durSec * 0.42)) * (1 - Math.exp(-t / 0.004));
    s = (s / 1.5 + (rnd() * 2 - 1) * Math.exp(-t / 0.005) * 0.18) * e;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 低音持续音 drone: 基频 + 微失谐五度, 缓慢起落 */
function drone(start, durSec, note, amp) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const f = midiHz(note);
  const ph = [0, 0, 0];
  const ratios = [1, 2, 3];
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const env = Math.min(1, t / 2.0) * Math.min(1, (durSec - t) / 2.5);
    let s = 0;
    for (let k = 0; k < 3; k++) {
      ph[k] += 2 * Math.PI * f * ratios[k] * (1 + (k === 2 ? 0.0012 : 0)) / SR;
      s += Math.sin(ph[k]) * [1, 0.22, 0.08][k];
    }
    put(musL, startI + i, s * amp * env);
    put(musR, startI + i, s * amp * env * 0.94);
  }
}

/* ================= 不谐和音簇 ================= */
function cluster(start, durSec, lo, hi, amp) {
  for (let m = lo; m <= hi; m++) {
    const pan = -0.6 + (m - lo) / Math.max(1, hi - lo) * 1.2;
    tableVoice(musL, musR, pan, start, T_STR, m, amp, durSec * 0.55, durSec * 0.35, 1.1, { detune: (m % 2 ? -11 : 11), vibHz: 2.0, vibCents: 5 });
  }
}

/* ================= 女声(锯齿过三共振峰) ================= */
const VOWEL = { a: [800, 1150, 2900], o: [500, 1000, 2400], u: [340, 700, 2300], e: [520, 1800, 2500], i: [300, 2200, 3000] };
function voice(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const v = VOWEL[opt.vowel || 'a'];
  const vibHz = opt.vibHz || 5.0, vibC = opt.vibCents || 18;
  const [pl, pr] = panGains(pan);
  const atk = opt.atk !== undefined ? opt.atk : 0.35;
  const rel = opt.rel !== undefined ? opt.rel : 0.7;
  const glide = opt.glide || 0;
  const len = Math.floor((durSec + rel + 0.2) * SR), startI = S(start);
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const gain = [1.0, 0.5, 0.22];
  const cL = v.map((fc, k) => mk(fc, [9, 11, 13][k]));
  const cR = v.map((fc, k) => mk(fc * 1.008, [9, 11, 13][k]));
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  let ph = rnd();
  const inc0 = midiHz(note) / SR * SIZE;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, durSec, atk, 0.2, 1, rel);
    if (a <= 0) continue;
    const x = Math.min(1, t / (durSec + rel));
    const bend = glide * x / 12;
    const inc = inc0 * Math.pow(2, bend + vibC * Math.sin(2 * Math.PI * vibHz * t) * (t > 0.25 ? 1 : t / 0.25) / 1200);
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

/* ================= 和声 (b 小调) ================= */
const CH = {
  Bm: { bass: 35, pad: [59, 62, 66] },   // B1 / B3 D4 F#4
  G: { bass: 31, pad: [59, 62, 67] },   // G1 / B3 D4 G4
  D: { bass: 38, pad: [57, 62, 66] },   // D2 / A3 D4 F#4
  A: { bass: 33, pad: [57, 61, 64] },   // A1 / A3 C#4 E4  (和声小调 V)
  Em: { bass: 28, pad: [59, 64, 67] },   // E1 / B3 E4 G4
  Fsh: { bass: 30, pad: [57, 61, 66] },   // F#1 / A3 C#4 F#4
  C: { bass: 36, pad: [60, 64, 67] },   // C2 / C4 E4 G4  (♭II 那不勒斯: 压迫感)
  Fsh7: { bass: 30, pad: [57, 61, 63, 66] } // F#1 / A3 A#3 C#4 F#4 (A# = 张力)
};
const LOOP = ['Bm', 'G', 'D', 'A', 'Bm', 'Em', 'C', 'Fsh7'];
const chordOf = bar => CH[LOOP[((bar % 8) + 8) % 8]];

/* ================= 主题 ================= */
/** 主题A(唄 I): [拍, midi, 时长(拍)] */
const SONG_A = [
  [0, 66, 1.0], [1.0, 69, 0.5], [1.5, 71, 1.0],
  [3, 71, 1.5], [4.5, 69, 1.0],
  [6, 66, 1.0], [7.0, 64, 0.5], [7.5, 66, 1.0],
  [9, 62, 2.5],
  [12, 66, 1.0], [13.0, 69, 0.5], [13.5, 71, 1.0],
  [15, 73, 1.5], [16.5, 71, 1.0],
  [18, 74, 1.0], [19.0, 73, 0.5], [19.5, 71, 1.0],
  [21, 66, 2.5]
];
/** 主题B(唄 II, 高八度区) */
const SONG_B = [
  [0, 78, 1.5], [1.5, 76, 1.0],
  [3, 74, 1.0], [4.0, 73, 0.5], [4.5, 71, 1.0],
  [6, 69, 1.5], [7.5, 71, 1.0],
  [9, 73, 2.5],
  [12, 78, 1.0], [13.0, 81, 0.5], [13.5, 78, 1.0],
  [15, 76, 1.5], [16.5, 74, 1.0],
  [18, 73, 1.0], [19.0, 71, 0.5], [19.5, 69, 1.0],
  [21, 66, 2.5]
];
/** 音乐盒摇篮动机(4 小节一轮): [拍, midi, 时长] */
const CRADLE = [
  [0, 90, 0.5], [1.0, 95, 0.5], [2.0, 93, 0.5], [2.5, 90, 0.5],
  [3, 88, 0.5], [4.0, 93, 0.5], [5.0, 90, 1.0],
  [6, 90, 0.5], [7.0, 95, 0.5], [8.0, 97, 0.5], [8.5, 95, 0.5],
  [9, 93, 0.5], [10.0, 90, 0.5], [11.0, 88, 1.0]
];

/* ================= 装配 ================= */
console.log('装配中... 总小节', BARS, '预计', TOTAL.toFixed(1), 's');

/** 音乐盒琶音: 把和弦音高八度按八分音型铺开 */
function mbArp(bar, amp, det) {
  const pad = chordOf(bar).pad.map(m => m + 12);
  const pat = [0, 1, 2, 1, 2, 0];
  for (let k = 0; k < 6; k++) {
    const m = pad[pat[k] % pad.length];
    musicBox(musL, musR, -0.45 + k * 0.18, tBeat(bar, k * 0.5), m, amp, { detune: det || 0 });
  }
}
/** 弦乐持续和弦 */
function strPad(bar, amp, oct) {
  const p = chordOf(bar);
  p.pad.forEach((m, i) => tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(bar, 0), T_STR_DK, m + (oct || 0), amp * (i === 0 ? 1 : 0.8), 0.6, barDur(bar) * 0.95, 0.9, { detune: (i - 1) * 5, vibHz: 3.6, vibCents: 6 }));
  tableVoice(musL, musR, 0, tBeat(bar, 0), T_STR_DK, p.bass, amp * 1.1, 0.5, barDur(bar) * 0.95, 0.9, { detune: 2 });
}
/** 主题演唱 */
function sing(tune, bar0, opt) {
  opt = opt || {};
  const amp = opt.amp !== undefined ? opt.amp : 0.10, oct = opt.oct || 0;
  tune.forEach(([q, m, d]) => {
    const st = tBeat(bar0 + Math.floor(q / 3), q % 3);
    voice(musL, musR, opt.pan || 0, st, m + oct, amp, d * (60 / bpmAt(bar0)) * 0.92,
      { vowel: opt.vowel || 'a', vibHz: 4.6, vibCents: 20, atk: opt.atk || 0.3, rel: opt.rel || 0.8, glide: opt.glide || 0 });
  });
}
/** 心跳脉动: 每小节 lub-dub */
function pulse(bar, amp, extra) {
  heart(tBeat(bar, 0), amp, -0.15);
  heart(tBeat(bar, 0.4), amp * 0.62, 0.15);
  if (extra) heart(tBeat(bar, 1.5), amp * 0.5, 0.1);
}

/* ---- I. 摇篮 (A: 0-7) 音乐盒与心跳, 极简 ---- */
{
  const s = SEC.A.start;
  drone(tBeat(s, 0), barDur(s) * 8.6, 35, 0.037);          // B1 持续音
  bell(musL, musR, tBeat(s, 0), 0.16, 35, 6.0);
  for (let i = 0; i < 8; i++) {
    const b = s + i;
    mbArp(b, 0.055);
    if (i >= 2) pulse(b, 0.20);
    if (i >= 4) strPad(b, 0.030);
  }
  [0, 3, 6].forEach(i => musicBox(musL, musR, 0, tBeat(s + i, 0), CRADLE[(i % 4) * 3][1], 0.075));
  tableVoice(musL, musR, 0, tBeat(s + 4, 0), T_STR_DK, 47, 0.030, 1.2, barDur(s) * 3.6, 1.5, { vibHz: 3, vibCents: 5 });
}

/* ---- II. 唄 I (B: 8-27) 女声首次唱出主题 ---- */
{
  const s = SEC.B.start;
  drone(tBeat(s, 0), barDur(s) * 20, 35, 0.034);
  for (let i = 0; i < 20; i++) {
    const b = s + i;
    mbArp(b, i < 4 ? 0.045 : 0.052);
    strPad(b, i < 4 ? 0.032 : 0.042);
    pulse(b, i < 8 ? 0.20 : 0.24);
    const p = chordOf(b);
    if (i % 2 === 0) pianoSoft(musL, musR, -0.3, tBeat(b, 0), p.pad[0] - 12, 0.045, barDur(b) * 1.6);
    if (i % 4 === 2) pianoSoft(musL, musR, 0.35, tBeat(b, 1.5), p.pad[2], 0.038, barDur(b) * 1.2);
  }
  sing(SONG_A, s + 4, { amp: 0.105, vowel: 'a' });          // 第一遍
  sing(SONG_A, s + 12, { amp: 0.098, vowel: 'o', pan: 0.15 }); // 第二遍换元音
  timp(dryL, dryR, tBeat(s + 16, 0), 0.16, 35, 0);
  timp(dryL, dryR, tBeat(s + 18, 0), 0.14, 42, 0.2);
}

/* ---- III. 崩坏 (C: 28-47) 提速, 音簇与碎裂的摇篮 ---- */
{
  const s = SEC.C.start;
  drone(tBeat(s, 0), barDur(s) * 20, 36, 0.039);           // 转到 C(♭II) 持续音: 压迫
  for (let i = 0; i < 20; i++) {
    const b = s + i;
    const p = chordOf(b);
    strPad(b, 0.048);
    pulse(b, 0.30, true);
    if (i % 2 === 0) timp(dryL, dryR, tBeat(b, 0), 0.30, p.bass, (i % 4 ? 0.25 : -0.25));
    if (i % 4 === 0) {
      musicBox(musL, musR, -0.4, tBeat(b, 0), p.pad[0] + 24, 0.050, { detune: -35 });  // 走调的音乐盒
      musicBox(musL, musR, 0.4, tBeat(b, 1.5), p.pad[2] + 24, 0.045, { detune: 40 });
    }
    if (i % 4 === 3) cymbal(dryL, dryR, tBeat(b, 2), 0.12, 1.4, i % 8 ? -0.35 : 0.35);
  }
  // 不谐和音簇推进
  cluster(tBeat(s + 8, 0), barDur(s + 8) * 3.4, 61, 68, 0.030);
  cluster(tBeat(s + 16, 0), barDur(s + 16) * 3.4, 60, 69, 0.036);
  // 碎裂的女声: 短促、下滑、不解决
  const frag = [[1, 78], [3, 76], [5, 77], [7, 74], [9, 80], [11, 78], [13, 83], [15, 81], [17, 79], [19, 77]];
  frag.forEach(([b, m], k) => voice(musL, musR, k % 2 ? 0.3 : -0.3, tBeat(s + b, 0), m, 0.075, 0.5,
    { vowel: 'e', vibHz: 6.2, vibCents: 30, atk: 0.08, rel: 0.35, glide: -0.7 }));
  gong(musL, musR, tBeat(s + 12, 0), 0.10, 110, 3.0);
  // 收束: 急速回落
  for (let k = 0; k < 6; k++) musicBox(musL, musR, 0, tBeat(s + 19, k * 0.5), [78, 76, 74, 73, 71, 66][k], 0.065, { detune: k * 9 });
}

/* ---- IV. 静 (D: 48-55) 只剩音乐盒与心跳 ---- */
{
  const s = SEC.D.start;
  drone(tBeat(s, 0), barDur(s) * 8, 35, 0.022);
  musicBox(musL, musR, 0, tBeat(s, 0), 66, 0.085);
  for (let i = 0; i < 8; i++) {
    const b = s + i;
    if (i % 2 === 0) { musicBox(musL, musR, -0.35, tBeat(b, 0), CRADLE[(i % 4) * 3][1], 0.062); musicBox(musL, musR, 0.35, tBeat(b, 1.5), CRADLE[(i % 4) * 3 + 1][1], 0.055); }
    pulse(b, 0.16);
  }
  sing(SONG_A, s + 2, { amp: 0.088, vowel: 'u', pan: -0.1, atk: 0.6 });
  cymbal(dryL, dryR, tBeat(s + 7, 0), 0.08, 2.4, 0);
}

/* ---- V. 唄 II (E: 56-83) 主题B + 全奏 + 高潮 ---- */
{
  const s = SEC.E.start;
  drone(tBeat(s, 0), barDur(s) * 28, 35, 0.035);
  for (let i = 0; i < 28; i++) {
    const b = s + i;
    const p = chordOf(b);
    mbArp(b, i < 20 ? 0.048 : 0.040);
    strPad(b, i < 12 ? 0.042 : (i < 20 ? 0.052 : 0.044));
    pulse(b, i < 12 ? 0.22 : 0.28, i >= 12);
    if (i % 2 === 1) pianoSoft(musL, musR, 0.3, tBeat(b, 1.0), p.pad[1] - 12, 0.040, barDur(b) * 1.3);
    if (i >= 12 && i % 4 === 0) timp(dryL, dryR, tBeat(b, 0), 0.24, p.bass, (i % 8 ? 0.2 : -0.2));
    if (i >= 20 && i % 4 === 2) cymbal(dryL, dryR, tBeat(b, 0), 0.09, 1.6, i % 8 ? -0.3 : 0.3);
  }
  sing(SONG_B, s + 4, { amp: 0.100, vowel: 'a' });            // 主题B 第一次
  sing(SONG_A, s + 12, { amp: 0.105, vowel: 'o', pan: -0.12 }); // 主题A 回归
  sing(SONG_B, s + 20, { amp: 0.112, vowel: 'a', oct: 12 });   // 主题B 高八度: 高潮
  // 高潮处的弦乐不谐和与低音推进
  cluster(tBeat(s + 20, 0), barDur(s + 20) * 3.6, 59, 67, 0.028);
  for (let i = 20; i < 26; i++) {
    const b = s + i, p = chordOf(b);
    timp(dryL, dryR, tBeat(b, 1.5), 0.20, p.bass + 12, (i % 2 ? 0.25 : -0.25));
  }
  bell(musL, musR, tBeat(s + 24, 0), 0.14, 35, 5.0);
  // 回落: 只剩女声长音
  voice(musL, musR, 0, tBeat(s + 26, 0), 66, 0.090, barDur(s + 26) * 1.8, { vowel: 'u', vibHz: 4.2, vibCents: 16, atk: 0.8, rel: 1.2, glide: -0.35 });
}

/* ---- VI. 终 (F: 84-91) 摇篮最后一次, 渐远 ---- */
{
  const s = SEC.F.start;
  drone(tBeat(s, 0), barDur(s) * 8.8, 35, 0.027);
  for (let i = 0; i < 8; i++) {
    const b = s + i;
    if (i < 6) mbArp(b, 0.050 - i * 0.004);
    if (i < 5) pulse(b, 0.14 - i * 0.012);
    if (i < 4) strPad(b, 0.028);
  }
  sing(SONG_A, s + 0, { amp: 0.085, vowel: 'a', atk: 0.5, rel: 1.4 });
  bell(musL, musR, tBeat(s + 6, 0), 0.20, 35, 8.0);
  musicBox(musL, musR, 0, tBeat(s + 6, 0), 66, 0.075);
  musicBox(musL, musR, 0, tBeat(s + 6, 1.0), 71, 0.062);
  musicBox(musL, musR, 0, tBeat(s + 6, 2.0), 66, 0.058);
  musicBox(musL, musR, 0, tBeat(s + 7, 0.5), 59, 0.050);
  gong(musL, musR, tBeat(s + 7, 0), 0.12, 88, 3.5);
  cymbal(dryL, dryR, tBeat(s + 7, 0), 0.10, 2.6, 0);
}

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
  sendL[i] = musL[i - PRE] * 0.42 + dryL[i - PRE] * 0.08;
  sendR[i] = musR[i - PRE] * 0.42 + dryR[i - PRE] * 0.08;
}
const revL = reverbChannel(sendL, 0, 0.88, 0.18, 0.05);     // 更长更空灵的尾巴
const revR = reverbChannel(sendR, 29, 0.88, 0.18, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.5, dryRms * 0.52 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) { outL[i] = musL[i] + dryL[i] + revL[i] * wetGain; outR[i] = musR[i] + dryR[i] + revR[i] * wetGain; }
const fadeIn = S(0.8), fadeOut = S(7.0);
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
// 女声叙事曲: 中频托住人声, 高频留给音乐盒, 低频压掉 drone 的糊
const EQ_CHAIN = [hpCoeff(32, 0.707), peakCoeff(65, 0.8, -4.5), peakCoeff(150, 1.0, -2.0), peakCoeff(900, 1.0, 1.2), peakCoeff(2600, 0.9, 1.8), shelfCoeff(7000, 2.0)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

let peak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > peak) peak = a; const b = Math.abs(eqR[i]); if (b > peak) peak = b; }
// 本曲是"密集持续型"编曲(峰均比天然低于打击乐驱动的曲子): 若沿用 1.5 的归一化目标,
// 会把大量持续样本推进 tanh 饱和区(实测 1.6%, 是前几版的十几倍)从而压掉动态与女声质感。
// 改用 0.75: 让饱和只当兜底安全阀, 动态留给最后的 RMS 归一化去定响度。
const norm = peak > 0 ? 0.75 / peak : 1;
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
const TARGET_RMS = 0.100;   // 叙事曲略低于战斗曲, 留出女声动态
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'saya.wav');
const dataBytes = N * 2 * 2;
const buf = Buffer.alloc(44 + dataBytes);
buf.write('RIFF', 0); buf.writeUInt32LE(36 + dataBytes, 4); buf.write('WAVE', 8);
buf.write('fmt ', 12); buf.writeUInt32LE(16, 16); buf.writeUInt16LE(1, 20);
buf.writeUInt16LE(2, 22); buf.writeUInt32LE(SR, 24); buf.writeUInt32LE(SR * 4, 28);
buf.writeUInt16LE(4, 32); buf.writeUInt16LE(16, 34);
buf.write('data', 36); buf.writeUInt32LE(dataBytes, 40);
for (let i = 0; i < N; i++) {
  const l = Math.max(-1, Math.min(1, eqL[i])), r = Math.max(-1, Math.min(1, eqR[i]));
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
console.log(JSON.stringify({ file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, sections: Object.keys(SEC).map(k => `${k}:${SEC[k].start}+${SEC[k].count}`).join(' '), peak, satSamples: sat, wetGain: +wetGain.toFixed(3), finalRms: +finalRms.toFixed(4), profile: rows }, null, 0));
