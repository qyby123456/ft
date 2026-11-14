/*
 * 茶树Boss 战斗BGM —— 「恐怖木偶剧场 · 加洛普」版 (v5, 完全重写)
 * 参考取向: Black Souls II OST - Theatre of Darkness, the Grand Guignol
 *          （暗黑大剧院 / 恐怖木偶剧）
 *
 * 与前三版的差别（刻意错开）:
 *   节拍   : 2/4 加洛普 + 7/8 跛行木偶  (v2=4/4, v3=3/4圆舞曲, v4=4/4分段)
 *   调性   : e 小调 / 弗里吉亚, 以三全音 B♭-E 为核心  (v2=a小调, v3/v4=d小调)
 *   速度   : 168 定速, 末尾加速到 208  (v3=120定速, v4=72→116→88)
 *   低音   : 半音下行帕萨卡利亚 → 终曲半音上行  (v2=八分ostinato, v4=踏板长音)
 *   素材   : 原创「拐脚手摇风琴调」+ 铜管剧场圣咏  (v4=末日经, 其余为氛围/战歌)
 *   音色   : 手摇风琴(calliope)·骨木琴·鞭响·发条棘轮·手风琴·低音大号·木偶关节·观众嘈杂·尖叫
 *   戏剧点 : 中途「全体急停」两小节真静默, 末尾铁链与发条停止
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const TAIL = 7.0;

/* ================= 时间轴: 逐小节定义拍号与速度 =================
 * beats = 该小节占几个「四分音符」；bpm = 四分音符每分钟
 * 2/4 → beats 2 ; 7/8 → beats 3.5
 */
const METER = [];
const SEC = {};
let _b = 0;
function sec(name, count, beats, bpm, ramp) {
  const o = { start: _b, count, beats };
  for (let i = 0; i < count; i++) {
    const v = ramp ? ramp[0] + (ramp[1] - ramp[0]) * (count === 1 ? 0 : i / (count - 1)) : bpm;
    METER.push({ beats, bpm: v });
  }
  _b += count;
  SEC[name] = o;
  return o;
}
/* --- 曲式 --- */
sec('A', 2, 2, 150);                       // 0-1    开幕·上发条 (极短, 开局即入加洛普)
sec('B', 24, 2, 168);                      // 2-25   加洛普 · 主题A
sec('C', 24, 2, 168);                      // 26-49  加洛普 · 剧场圣咏(主题B)
sec('D', 8, 3.5, 105);                     // 50-57  木偶跛行 (7/8)
sec('E', 32, 2, 176);                      // 58-89  卡农追逐
sec('F', 4, 2, 168);                       // 90-93  急停·静默
sec('G', 46, 2, null, [176, 208]);         // 94-139 终曲·加速加洛普
sec('H', 4, 2, 160);                       // 140-143 落幕

const BARS = METER.length;
const tBar = [];
let TOTAL = 0;
for (const m of METER) { tBar.push(TOTAL); TOTAL += m.beats * 60 / m.bpm; }
const barDur = bar => { const b = Math.max(0, Math.min(METER.length - 1, Math.floor(bar))); return METER[b].beats * 60 / METER[b].bpm; };
const bpmAt = bar => METER[Math.max(0, Math.min(METER.length - 1, Math.floor(bar)))].bpm;
/** 支持小数小节（供主题增值/减值使用） */
const tBeat = (bar, q) => {
  const b0 = Math.floor(bar), fr = bar - b0;
  const bb = Math.max(0, Math.min(METER.length - 1, b0));
  return tBar[bb] + fr * barDur(bb) + q * 60 / METER[bb].bpm;
};

const N = Math.ceil((TOTAL + TAIL) * SR);
const S = t => Math.round(t * SR);

const rnd = (() => { let a = 20260911; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
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
const T_REED = makeTable([1, 0.04, 0.62, 0.05, 0.34, 0.04, 0.20, 0.03, 0.13, 0.02, 0.08, 0.05]);   // 自由簧(手风琴/簧风琴)
const T_BRASS = makeTable([1, 0.66, 0.5, 0.36, 0.26, 0.18, 0.13, 0.09, 0.06, 0.04]);
const T_STR = makeTable(sawH(32, 18));
const T_SAW = makeTable(sawH(40, 26));
const T_ORGAN_P = makeTable([1, 0.3, 0.5, 0.12, 0.06]);

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

/* ================= 打击 / 效果 ================= */
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
function timpRoll(bl, br, start, durSec, amp, note) {
  const step = 0.055;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    timp(bl, br, start + t, amp * (0.18 + 0.5 * x) * (1 + 0.15 * Math.sin(t * 40)), note, (x - 0.5) * 0.5);
  }
}
function snare(bl, br, start, amp, pan) {
  const len = Math.floor(0.22 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const hp = 0.93 * (yPrev + w - wPrev); yPrev = hp; wPrev = w;
    ph += 2 * Math.PI * 208 / SR;
    const s = (hp * Math.exp(-t / 0.055) * 0.9 + Math.sin(ph) * Math.exp(-t / 0.035) * 0.32) * amp;
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
  const len = Math.floor(0.5 * SR), startI = S(start);
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

/* ================= v5 新音色 ================= */
/** 手摇风琴 / 蒸汽风琴: 强基音+八度, 吹口气息, 走音(wow) —— 坏掉的游乐场音色 */
function calliope(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const det = opt.detune || 0, wow = opt.wow !== undefined ? opt.wow : 0.5, wowHz = opt.wowHz || 0.55;
  const atk = opt.atk !== undefined ? opt.atk : 0.05, rel = opt.rel !== undefined ? opt.rel : 0.11;
  const [pl, pr] = panGains(pan);
  const len = Math.ceil((atk + durSec + rel + 0.08) * SR), startI = S(start);
  const f0 = midiHz(note) * Math.pow(2, det / 1200);
  const pg = [1, 0.44, 0.21, 0.11], ph = [0, 0, 0, 0];
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, atk + durSec, atk, 0.05, 1, rel);
    if (a <= 0) continue;
    const wob = 1 + wow * 0.012 * Math.sin(2 * Math.PI * wowHz * t + note * 0.7);
    let s = 0;
    for (let k = 0; k < 4; k++) { ph[k] += 2 * Math.PI * f0 * (k + 1) * wob / SR; s += Math.sin(ph[k]) * pg[k]; }
    const w = rnd() * 2 - 1; lp += 0.45 * (w - lp);
    s = s * 0.78 + lp * Math.exp(-t / 0.022) * 0.55;
    const j = startI + i;
    put(bl, j, s * a * amp * pl); put(br, j, s * a * amp * pr);
  }
}
/** 骨木琴 / 骷髅木琴: 非谐分音 + 敲击瞬态 */
function bones(bl, br, pan, start, note, amp) {
  const f = midiHz(note);
  const parts = [[1, 1, 0.17], [3.0, 0.4, 0.09], [5.4, 0.22, 0.06], [8.2, 0.12, 0.035]];
  const len = Math.floor(0.40 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ph = parts.map(() => 0);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < parts.length; k++) {
      ph[k] += 2 * Math.PI * f * parts[k][0] / SR;
      s += Math.sin(ph[k]) * parts[k][1] * Math.exp(-t / parts[k][2]);
    }
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.004) * 0.35;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 木偶关节敲击(木鱼式) */
function woodblock(bl, br, start, amp, pan, freq) {
  const f = freq || 1200;
  const len = Math.floor(0.12 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const w = 2 * Math.PI * f / SR, c = Math.cos(w), al = Math.sin(w) / (2 * 14), a0 = 1 + al;
  const co = { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 };
  let z1 = 0, z2 = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const src = rnd() * 2 - 1;
    const o = co.b0 * src + z1; z1 = co.b2 * src - co.a1 * o - co.a2 * z2; z2 = o;
    ph += 2 * Math.PI * f * 0.9 / SR;
    const s = (o * 3.0 * Math.exp(-t / 0.018) + Math.sin(ph) * Math.exp(-t / 0.03) * 0.5) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 鞭响 (fouetté) */
function whip(bl, br, start, amp, pan) {
  const len = Math.floor(0.18 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let hp = 0, hpPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    hp = 0.9 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
    ph += 2 * Math.PI * (2600 * Math.exp(-t / 0.02) + 900) / SR;
    const s = (hp * Math.exp(-t / 0.012) * 1.1 + Math.sin(ph) * Math.exp(-t / 0.008) * 0.5) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 发条钟摆滴答 */
function tick(bl, br, start, amp, pan, freq) {
  const len = Math.floor(0.035 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const f = freq || 2400;
  let ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    ph += 2 * Math.PI * f / SR;
    const s = (w * Math.exp(-t / 0.0022) * 0.8 + Math.sin(ph) * Math.exp(-t / 0.006) * 0.45) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 上发条棘轮(逐渐加速) */
function ratchet(bl, br, start, durSec, amp) {
  let t = 0, k = 0;
  while (t < durSec) {
    const x = t / durSec;
    tick(bl, br, start + t, amp * (0.5 + 0.5 * x), (k % 2 ? 0.35 : -0.35), 1700 + 500 * x);
    t += 0.085 * (1 - 0.55 * x); k++;
  }
}
/** 低音大号 (加洛普的 "嘭") */
function tuba(bl, br, pan, start, note, amp, hold, rel) {
  const [pl, pr] = panGains(pan || 0);
  const atk = 0.03, len = Math.ceil((atk + hold + rel + 0.05) * SR), startI = S(start);
  const f0 = midiHz(note);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, atk + hold, atk, 0.10, 1, rel);
    if (a <= 0) continue;
    const f = f0 * (1 + 0.07 * Math.exp(-t / 0.05));
    ph += 2 * Math.PI * f / SR;
    let s = Math.sin(ph) * 0.82 + Math.sin(2 * ph) * 0.12 + Math.sin(3 * ph) * 0.07;
    const w = rnd() * 2 - 1; lp += 0.18 * (w - lp); s += lp * 0.10;
    put(bl, startI + i, s * a * amp * pl); put(br, startI + i, s * a * amp * pr);
  }
}
/** 走音的酒吧钢琴 (三根弦各自偏移 + 非谐性) */
function piano(bl, br, pan, start, note, amp, durSec) {
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note);
  const len = Math.floor((durSec + 0.25) * SR), startI = S(start);
  const str = [-14, 0, 11].map(c => f0 * Math.pow(2, c / 1200));
  const ph = str.map(() => 0);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < 3; k++) {
      const f = str[k] * (1 + 0.00035 * (k + 1) * (k + 1));
      ph[k] += 2 * Math.PI * f / SR;
      s += Math.sin(ph[k]) * (k === 1 ? 0.62 : 0.5);
    }
    const e = Math.exp(-t / (durSec * 0.38)) * (1 - Math.exp(-t / 0.003));
    s = (s / 1.6 + (rnd() * 2 - 1) * Math.exp(-t / 0.006) * 0.25) * e;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 尖叫 (共振峰人声 + 下滑音 + 快速颤音) */
const VOWEL = { a: [800, 1150, 2900], o: [500, 1000, 2400], u: [340, 700, 2300], e: [520, 1800, 2500], i: [300, 2200, 3000] };
function scream(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const v = VOWEL[opt.vowel || 'a'];
  const [pl, pr] = panGains(pan || 0);
  const glide = opt.glide !== undefined ? opt.glide : -8;
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const gain = [1.0, 0.5, 0.25];
  const cL = v.map((fc, k) => mk(fc, [7, 9, 11][k]));
  const cR = v.map((fc, k) => mk(fc * 1.012, [7, 9, 11][k]));
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  const atk = opt.atk !== undefined ? opt.atk : 0.06, rel = opt.rel !== undefined ? opt.rel : 0.35;
  const len = Math.ceil((atk + durSec + rel + 0.2) * SR), startI = S(start);
  let ph = rnd();
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, atk + durSec, atk, 0.1, 1, rel);
    if (a <= 0) continue;
    const x = Math.min(1, t / (durSec + rel));
    const f = midiHz(note) * Math.pow(2, (glide * x + 0.4 * Math.sin(2 * Math.PI * 7.5 * t)) / 12);
    const inc = f / SR * SIZE;
    const i0 = ph | 0, fr = ph - i0;
    const src = (T_SAW[i0] + (T_SAW[(i0 + 1) & (SIZE - 1)] - T_SAW[i0]) * fr) * 0.85;
    ph += inc; if (ph >= SIZE) ph -= SIZE;
    let oL = 0, oR = 0;
    for (let k = 0; k < 3; k++) {
      let o = cL[k].b0 * src + sL[k].z1; sL[k].z1 = cL[k].b2 * src - cL[k].a1 * o - cL[k].a2 * sL[k].z2; sL[k].z2 = o; oL += o * gain[k];
      let o2 = cR[k].b0 * src + sR[k].z1; sR[k].z1 = cR[k].b2 * src - cR[k].a1 * o2 - cR[k].a2 * sR[k].z2; sR[k].z2 = o2; oR += o2 * gain[k];
    }
    const j = startI + i;
    put(bl, j, oL * a * amp * pl * 5.0); put(br, j, oR * a * amp * pr * 5.0);
  }
}
/** 铁链 */
function chain(bl, br, start, amp) {
  const ratios = [1, 1.37, 1.83, 2.41, 3.16, 4.05, 5.2, 6.7];
  const base = 520;
  const len = Math.floor(0.9 * SR), startI = S(start);
  const ph = ratios.map(() => 0);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      const a = Math.exp(-t / (0.5 / (1 + k * 0.35))) * (1 - Math.exp(-t / 0.0015)) / (1 + k * 0.6);
      ph[k] += 2 * Math.PI * base * ratios[k] / SR;
      s += Math.sin(ph[k]) * a;
    }
    const w = rnd() * 2 - 1; lp += 0.5 * (w - lp);
    s += lp * Math.exp(-t / 0.01) * 0.5;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
/** 落幕闷响 */
function thud(bl, br, start, amp) {
  const len = Math.floor(0.8 * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 32 + (90 - 32) * Math.exp(-t / 0.05);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.22) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.10 * (w - lp);
    s += lp * Math.exp(-t / 0.02) * 0.5;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
/** 三角铁/发条铃: 高频晶莹层, 补 3~6kHz 让整体不闷 */
function triangle(bl, br, start, amp, pan, freq) {
  const f = freq || 3000;
  const ratios = [1, 2.76, 5.40, 8.93];
  const dec = [0.55, 0.30, 0.17, 0.10];
  const len = Math.floor(1.6 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ph = ratios.map(() => 0);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      ph[k] += 2 * Math.PI * f * ratios[k] / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / dec[k]) / (1 + k);
    }
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.002) * 0.3;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 观众席嘈杂 (带通噪声) */
function crowd(start, durSec, amp) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const fc = 650, q = 1.1;
  const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al;
  const co = { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 };
  let z1 = 0, z2 = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const src = rnd() * 2 - 1;
    const o = co.b0 * src + z1; z1 = co.b2 * src - co.a1 * o - co.a2 * z2; z2 = o;
    const env = Math.min(1, t / 0.8) * Math.min(1, (durSec - t) / 1.5);
    const s = o * amp * env * (0.6 + 0.25 * Math.sin(2 * Math.PI * 0.31 * t) + 0.15 * Math.sin(2 * Math.PI * 0.13 * t + 1.7));
    put(musL, startI + i, s); put(musR, startI + i, s * 0.9);
  }
}

/* ================= 和声 ================= */
/** 半音下行帕萨卡利亚 (8 小节循环): E → E♭ → D → C#dim → C → Bm → B♭ → B7 */
const PASS = [
  { root: 28, ch: [64, 67, 71] },   // Em
  { root: 27, ch: [63, 67, 70] },   // E♭  (仅一音之差, 半音下行)
  { root: 26, ch: [62, 65, 69] },   // Dm
  { root: 25, ch: [61, 64, 67] },   // C#dim
  { root: 24, ch: [60, 64, 67] },   // C
  { root: 23, ch: [59, 62, 66] },   // Bm
  { root: 22, ch: [58, 62, 65] },   // B♭  ← 三全音
  { root: 23, ch: [59, 63, 66] }    // B7
];
/** 终曲半音上行 (焦虑式大三和弦爬升) */
const ASC = [
  { root: 28, ch: [64, 67, 71] },   // E
  { root: 29, ch: [65, 69, 72] },   // F
  { root: 30, ch: [66, 70, 73] },   // F#
  { root: 31, ch: [67, 71, 74] },   // G
  { root: 32, ch: [68, 72, 75] },   // G#
  { root: 33, ch: [69, 73, 76] },   // A
  { root: 34, ch: [70, 74, 77] },   // B♭
  { root: 35, ch: [71, 74, 78] }    // B
];
const passOf = i => PASS[((i % 8) + 8) % 8];

/* ================= 主题 ================= */
/** 主题A「拐脚手摇风琴调」: 8 小节 × 2 拍; 用到 ♭2(F) 与 ♭5(B♭), 结尾 B→B♭→E 解开三全音 */
const TUNE_A = [
  [0, 0, 76, 0.5], [0, 0.5, 76, 0.5], [0, 1, 77, 0.5], [0, 1.5, 76, 0.5],
  [1, 0, 74, 0.5], [1, 0.5, 76, 0.5], [1, 1, 71, 1.0],
  [2, 0, 76, 0.5], [2, 0.5, 76, 0.5], [2, 1, 79, 0.5], [2, 1.5, 77, 0.5],
  [3, 0, 76, 0.5], [3, 0.5, 74, 0.5], [3, 1, 71, 1.0],
  [4, 0, 72, 0.5], [4, 0.5, 71, 0.5], [4, 1, 70, 0.5], [4, 1.5, 69, 0.5],
  [5, 0, 67, 0.5], [5, 0.5, 69, 0.5], [5, 1, 71, 1.0],
  [6, 0, 76, 0.5], [6, 0.5, 77, 0.5], [6, 1, 76, 0.5], [6, 1.5, 74, 0.5],
  [7, 0, 71, 0.5], [7, 0.5, 70, 0.5], [7, 1, 76, 1.0]
];
/** 主题B「剧场圣咏」: 方正的 8 小节, 铜管齐奏 */
const TUNE_B = [
  [0, 0, 76, 1], [0, 1, 76, 0.5], [0, 1.5, 74, 0.5],
  [1, 0, 72, 1], [1, 1, 72, 0.5], [1, 1.5, 71, 0.5],
  [2, 0, 69, 1], [2, 1, 71, 0.5], [2, 1.5, 72, 0.5],
  [3, 0, 71, 2],
  [4, 0, 76, 1], [4, 1, 77, 0.5], [4, 1.5, 76, 0.5],
  [5, 0, 79, 1], [5, 1, 77, 0.5], [5, 1.5, 76, 0.5],
  [6, 0, 74, 1], [6, 1, 72, 0.5], [6, 1.5, 69, 0.5],
  [7, 0, 71, 2]
];
/** 终曲: 半音上行 16 音 (E4→G5), 疯狂感 */
const FINALE = [
  [0, 0, 64, 1], [0, 1, 65, 1], [1, 0, 66, 1], [1, 1, 67, 1],
  [2, 0, 68, 1], [2, 1, 69, 1], [3, 0, 70, 1], [3, 1, 71, 1],
  [4, 0, 72, 1], [4, 1, 73, 1], [5, 0, 74, 1], [5, 1, 75, 1],
  [6, 0, 76, 1], [6, 1, 77, 1], [7, 0, 78, 1], [7, 1, 79, 1]
];

/** 统一主题演奏器: opts.voice = calliope|bones|reed|brass|piano|strings */
function playTune(tune, bar0, opt) {
  opt = opt || {};
  const oct = opt.oct || 0, amp = opt.amp !== undefined ? opt.amp : 0.1;
  const v = opt.voice || 'calliope', ts = opt.timeScale || 1;
  const pan = opt.pan || 0;
  tune.forEach(([b, bt, m, d]) => {
    const note = m + oct;
    const bar = bar0 + b * ts, q = bt * ts;
    const st = tBeat(bar, q);
    const hold = (d || 1) * ts * (60 / bpmAt(bar)) * 0.9;
    if (v === 'calliope') calliope(musL, musR, pan, st, note, amp, hold * 0.75, { detune: opt.detune || 0, wow: opt.wow !== undefined ? opt.wow : 0.5 });
    else if (v === 'bones') bones(musL, musR, pan, st, note, amp);
    else if (v === 'reed') reed(musL, musR, pan, st, note, amp, hold, 0.13, {});
    else if (v === 'piano') piano(musL, musR, pan, st, note, amp, hold);
    else if (v === 'strings') tableVoice(musL, musR, pan, st, T_STR, note, amp, 0.05, hold, 0.2, { detune: 5, vibHz: 5, vibCents: 7 });
    else tableVoice(musL, musR, pan, st, T_BRASS, note, amp, 0.04, hold, 0.16, { detune: opt.detune || -4 });
  });
}
/** 簧风琴 */
function reed(bl, br, pan, start, note, amp, hold, rel, opt) {
  opt = opt || {};
  const det = opt.detune || 0, tr = opt.tremHz || 5.6, td = opt.tremDepth || 0.17;
  const [pl, pr] = panGains(pan);
  const atk = opt.atk !== undefined ? opt.atk : 0.03;
  const total = Math.ceil((atk + hold + rel + 0.05) * SR), startI = S(start);
  const inc0 = midiHz(note) * Math.pow(2, det / 1200) / SR * SIZE;
  let phase = rnd() * SIZE;
  for (let i = 0; i < total; i++) {
    const t = i / SR;
    let a = adsr(t, atk + hold, atk, 0.08, 1, rel);
    if (a <= 0) continue;
    a *= 1 - td + td * (0.5 + 0.5 * Math.sin(2 * Math.PI * tr * t + pan));
    const i0 = phase | 0, fr = phase - i0;
    const s = (T_REED[i0] + (T_REED[(i0 + 1) & (SIZE - 1)] - T_REED[i0]) * fr) * a * amp;
    phase += inc0; if (phase >= SIZE) phase -= SIZE;
    const j = startI + i;
    put(bl, j, s * pl); put(br, j, s * pr);
  }
}

/* ================= 加洛普伴奏 =================
 * 低频配平要点: 低音大号(41Hz) / 大鼓(44-150Hz) / 定音鼓(82Hz) 三者若同拍同强
 * 会把 80-160Hz 堆满导致"糊"。故: 大号只留一拍 + 高八度短促推进, 大鼓与定音鼓减力,
 * 并新增三角铁高频层补 3~6kHz。
 */
function oomPah(bar, p, amp, opt) {
  opt = opt || {};
  const q = 60 / bpmAt(bar);
  tuba(musL, musR, 0, tBeat(bar, 0), p.root, amp * 0.75, q * 0.40, 0.09);
  if (!opt.noPah) p.ch.forEach((m, i) => reed(musL, musR, -0.45 + i * 0.45, tBeat(bar, 1), (m + (opt.oct || 0)), amp * 0.88, q * 0.42, 0.12, {}));
  // 低八度圆号/长号铺底: 手风琴和弦在 E4 以上、大号在 41Hz, 160-320Hz 会空掉 → 这层补温
  if (opt.horn) p.ch.forEach((m, i) => tableVoice(musL, musR, -0.5 + i * 0.5, tBeat(bar, 0), T_BRASS, m - 12, amp * 0.20, 0.06, q * 1.6, 0.26, { detune: (i - 1) * 6 }));
  if (opt.drive) tuba(musL, musR, 0, tBeat(bar, 1.5), p.root + 12, amp * 0.30, q * 0.20, 0.07);
}
function galopDrums(bar, amp, opt) {
  opt = opt || {};
  const c = opt.chord || PASS[0];
  bassDrum(dryL, dryR, tBeat(bar, 0), 0.20 * amp);
  // 定音鼓压到根音(≈41Hz)而非上方八度(82Hz) —— 82Hz 正是与低音大号二三次谐波打架的糊区
  timp(dryL, dryR, tBeat(bar, 0), 0.20 * amp, c.root, (bar % 2 ? 0.2 : -0.2));
  snare(dryL, dryR, tBeat(bar, 0.5), 0.24 * amp, -0.22);
  snare(dryL, dryR, tBeat(bar, 1.5), 0.28 * amp, 0.22);
  woodblock(dryL, dryR, tBeat(bar, 0.75), 0.20 * amp, 0.32, 1520);
  woodblock(dryL, dryR, tBeat(bar, 1.75), 0.18 * amp, -0.32, 1120);
  tick(dryL, dryR, tBeat(bar, 0.25), 0.05 * amp, 0.28, 2600);
  tick(dryL, dryR, tBeat(bar, 1.25), 0.05 * amp, -0.28, 2600);
  if (opt.sparkle) {
    triangle(dryL, dryR, tBeat(bar, 0.5), 0.055 * amp, -0.45, 3400);
    triangle(dryL, dryR, tBeat(bar, 1.5), 0.050 * amp, 0.45, 4100);
  }
  if (opt.whip) whip(dryL, dryR, tBeat(bar, 1.5), 0.26 * amp, 0.42);
  if (opt.cymbal) cymbal(dryL, dryR, tBeat(bar, 0), 0.15 * amp, 1.5, bar % 8 ? -0.35 : 0.35);
  if (opt.roll) snareRoll(dryL, dryR, tBeat(bar, 1), 60 / bpmAt(bar) * 0.5, 0.11 * amp);
  if (opt.timp) timp(dryL, dryR, tBeat(bar, 1), 0.15 * amp, c.root + 12, 0.28);
}

/* ================= 装配 ================= */
console.log('装配中... 总小节', BARS, '预计', TOTAL.toFixed(1), 's');

/* ---- I. 开幕·上发条 (A: 0-1) 只留 1.6 秒, 免得实战(10~20s)只听见发条 ---- */
{
  const s = SEC.A.start;
  bell(musL, musR, tBeat(s, 0), 0.22, 28, 6.0);
  gong(musL, musR, tBeat(s, 0), 0.090, 96, 3.0);
  ratchet(dryL, dryR, tBeat(s, 0), barDur(s) * 1.2, 0.058);
  for (let b = s; b < s + 2; b++) for (let k = 0; k < 4; k++) tick(dryL, dryR, tBeat(b, k * 0.5), 0.036, k % 2 ? 0.3 : -0.3, 2500);
  woodblock(dryL, dryR, tBeat(s + 1, 1.5), 0.18, 0.2, 1300);
  whip(dryL, dryR, tBeat(s + 1, 1), 0.22, -0.3);
  calliope(musL, musR, 0.10, tBeat(s + 1, 1), 76, 0.060, 0.30, { detune: 8, wow: 0.9 });
  crowd(tBeat(s), barDur(s) * 2.0, 0.020);
}

/* ---- II. 加洛普·主题A (B: 4-27) ---- */
{
  const s = SEC.B.start, n = SEC.B.count;
  for (let i = 0; i < n; i++) {
    const b = s + i, p = passOf(i);
    oomPah(b, p, 0.135, { drive: i >= 8, horn: i >= 8 });
    galopDrums(b, i >= 12 ? 1.12 : 1.0, { chord: p, whip: i >= 16 && i % 2 === 1, cymbal: i % 4 === 0, roll: i % 8 === 7, timp: i >= 12, sparkle: true });
  }
  playTune(TUNE_A, s, { voice: 'calliope', amp: 0.100 });
  playTune(TUNE_A, s, { voice: 'bones', amp: 0.048, oct: -12, pan: 0.4 });
  playTune(TUNE_A, s + 8, { voice: 'calliope', amp: 0.100, oct: 12 });
  playTune(TUNE_A, s + 8, { voice: 'brass', amp: 0.082 });
  playTune(TUNE_A, s + 16, { voice: 'reed', amp: 0.100 });
  playTune(TUNE_A, s + 16, { voice: 'brass', amp: 0.070, oct: -12, pan: -0.25 });
  // 减值追逐: 主题A 提速一倍, 走两遍
  playTune(TUNE_A, s + 20, { voice: 'calliope', amp: 0.085, oct: 12, timeScale: 0.5 });
  playTune(TUNE_A, s + 22, { voice: 'bones', amp: 0.055, timeScale: 0.5, pan: -0.4 });
  [7, 15, 23].forEach(i => timpRoll(dryL, dryR, tBeat(s + i, 1), 60 / bpmAt(s + i), 0.46, passOf(i).root + 12));
  crowd(tBeat(s + 8), barDur(s + 8) * 18, 0.020);
}

/* ---- III. 加洛普·剧场圣咏 (C: 28-51) ---- */
{
  const s = SEC.C.start, n = SEC.C.count;
  for (let i = 0; i < n; i++) {
    const b = s + i, p = passOf(i);
    oomPah(b, p, 0.140, { drive: true, horn: true, oct: i >= 12 ? 0 : 0 });
    galopDrums(b, i >= 8 ? 1.15 : 1.05, { chord: p, whip: i >= 8, cymbal: i % 4 === 0, roll: i % 8 === 7, timp: true, sparkle: true });
  }
  playTune(TUNE_B, s, { voice: 'brass', amp: 0.105 });
  playTune(TUNE_B, s, { voice: 'calliope', amp: 0.070, oct: 12, pan: 0.3 });
  playTune(TUNE_B, s + 8, { voice: 'brass', amp: 0.110, oct: 12 });
  playTune(TUNE_B, s + 8, { voice: 'reed', amp: 0.090, pan: -0.3 });
  // 主题B 与 主题A 对位 (16-23)
  playTune(TUNE_B, s + 16, { voice: 'brass', amp: 0.105, oct: 12 });
  playTune(TUNE_A, s + 16, { voice: 'calliope', amp: 0.078, pan: 0.35 });
  playTune(TUNE_B, s + 16, { voice: 'piano', amp: 0.060, oct: -12, pan: -0.45 });
  // 收束 (24-31 段由 G 接续前的最后四小节做半终止)
  playTune(TUNE_B, s + 20, { voice: 'brass', amp: 0.112 });
  playTune(TUNE_A, s + 20, { voice: 'calliope', amp: 0.080, oct: 12, pan: 0.35 });
  [7, 15, 23].forEach(i => { gong(musL, musR, tBeat(s + i, 0), 0.075, 140, 1.5); timpRoll(dryL, dryR, tBeat(s + i, 1), 60 / bpmAt(s + i), 0.5, passOf(i).root + 12); });
  crowd(tBeat(s), barDur(s) * 22, 0.022);
}

/* ---- IV. 木偶跛行 · 7/8 (D: 50-57) 16 秒, 电平抬到听得见的对比而非静场 ---- */
{
  const s = SEC.D.start, n = SEC.D.count;
  for (let i = 0; i < n; i++) {
    const b = s + i;
    tuba(musL, musR, 0, tBeat(b, 0), i < 4 ? 28 : 29, 0.160, barDur(b) * 0.95, 0.30);
    // 跛行和声: 7/8 的第 0,2,3,5 个八分 (缺 1,4,6 = 瘸腿)
    const chord = (i % 4 === 3) ? [58, 62, 65] : [64, 67, 71];
    [0, 2, 3, 5].forEach(e => chord.forEach((m, j) => reed(musL, musR, -0.4 + j * 0.4, tBeat(b, e * 0.5), m, 0.085, 60 / bpmAt(b) * 0.36, 0.20, { detune: (i % 2 ? 16 : -13) })));
    for (let e = 0; e < 7; e++) tick(dryL, dryR, tBeat(b, e * 0.5), 0.042, e % 2 ? 0.3 : -0.3, 2600);
    woodblock(dryL, dryR, tBeat(b, 0), 0.19, 0.25, 1250);
    woodblock(dryL, dryR, tBeat(b, 1.5), 0.16, -0.25, 980);
    if (i % 4 === 2) woodblock(dryL, dryR, tBeat(b, 2.5), 0.15, 0.35, 1400);
    bassDrum(dryL, dryR, tBeat(b, 0), 0.24);
    timp(dryL, dryR, tBeat(b, 2), 0.20, 28, i % 2 ? 0.25 : -0.25);
  }
  // 破碎的主题碎片 (走音手摇风琴)
  const frag = [[0, 0, 76], [1, 1, 77], [2, 0.5, 74], [3, 1.5, 71], [4, 0, 70], [5, 1, 76], [6, 0.5, 79], [7, 1.5, 74]];
  frag.forEach(([b, bt, m], k) => calliope(musL, musR, (k % 2 ? 0.35 : -0.35), tBeat(s + b, bt * 0.5), m, 0.095, 60 / bpmAt(s + b) * 0.5, { detune: ((k % 3) - 1) * 32, wow: 1.2 }));
  // 骨木琴在跛行拍上敲骨架
  [0, 2, 3, 5].forEach(e => bones(musL, musR, e % 2 ? 0.4 : -0.4, tBeat(s + 1, e * 0.5), 64 + e * 2, 0.045));
  [0, 3, 5].forEach(e => bones(musL, musR, e % 2 ? -0.4 : 0.4, tBeat(s + 5, e * 0.5), 71 - e, 0.042));
  scream(musL, musR, 0.30, tBeat(s + 3, 0), 79, 0.070, 0.55, { vowel: 'a', glide: -9 });
  scream(musL, musR, -0.30, tBeat(s + 7, 0), 81, 0.070, 0.70, { vowel: 'e', glide: -7 });
  snareRoll(dryL, dryR, tBeat(s + 7, 3.5), barDur(s + 7) * 0.45, 0.20);
  timpRoll(dryL, dryR, tBeat(s + 7, 3.5), barDur(s + 7) * 0.45, 0.62, 28);
}

/* ---- V. 卡农追逐 (E: 64-95) ---- */
{
  const s = SEC.E.start, n = SEC.E.count;
  for (let i = 0; i < n; i++) {
    const b = s + i, p = passOf(i);
    oomPah(b, p, 0.142, { drive: true, horn: true });
    galopDrums(b, 1.16, { chord: p, whip: i % 2 === 1, cymbal: i % 4 === 0, roll: i % 8 === 7, timp: true, sparkle: true });
    if (i % 4 === 2) bassDrum(dryL, dryR, tBeat(b, 1), 0.42);
  }
  const voices = ['calliope', 'bones', 'reed', 'brass'];
  for (let round = 0; round < 2; round++) {
    voices.forEach((v, k) => playTune(TUNE_A, s + round * 16 + k * 4, {
      voice: v, amp: v === 'bones' ? 0.070 : 0.092, oct: k === 1 ? 12 : 0,
      detune: k === 0 ? 0 : (k === 2 ? 9 : -9), pan: -0.5 + k * 0.33
    }));
  }
  crowd(tBeat(s), barDur(s) * 30, 0.024);
  [7, 15, 23, 31].forEach(i => gong(musL, musR, tBeat(s + i, 0), 0.072, 140, 1.4));
}

/* ---- VI. 急停 · 静默 (F: 96-99) ---- */
{
  const s = SEC.F.start;
  // 全体重击: Em + B♭ 三全音叠置
  bassDrum(dryL, dryR, tBeat(s, 0), 0.80);
  thud(dryL, dryR, tBeat(s, 0), 0.35);
  whip(dryL, dryR, tBeat(s, 0), 0.38, 0);
  cymbal(dryL, dryR, tBeat(s, 0), 0.26, 2.6, 0);
  timp(dryL, dryR, tBeat(s, 0), 0.60, 28, 0);
  gong(musL, musR, tBeat(s, 0), 0.16, 120, 3.0);
  bell(musL, musR, tBeat(s, 0), 0.20, 28, 6.0);
  [64, 67, 71, 58, 70].forEach((m, i) => {
    calliope(musL, musR, -0.5 + i * 0.25, tBeat(s, 0), m, 0.088, 0.5, { detune: (i - 2) * 7 });
    tableVoice(musL, musR, -0.4 + i * 0.2, tBeat(s, 0), T_BRASS, m - 12, 0.088, 0.02, 0.45, 0.3, { detune: (i - 2) * 8 });
  });
  // s+1 = 真静默(只留混响尾巴)
  // s+2: 只剩钟摆
  [0, 1, 1.5].forEach(q => tick(dryL, dryR, tBeat(s + 2, q), 0.050, 0, 2500));
  bones(musL, musR, 0.2, tBeat(s + 2, 0.5), 76, 0.048);
  // s+3: 渐强滚奏冲进终曲
  timpRoll(dryL, dryR, tBeat(s + 3, 0), barDur(s + 3) * 0.98, 0.60, 28);
  snareRoll(dryL, dryR, tBeat(s + 3, 0), barDur(s + 3) * 0.98, 0.20);
}

/* ---- VII. 终曲 · 加速加洛普 (G: 100-145) ---- */
{
  const s = SEC.G.start, n = SEC.G.count;
  for (let i = 0; i < n; i++) {
    const b = s + i, p = ASC[i % 8];
    oomPah(b, p, 0.148, { drive: true, horn: true });
    galopDrums(b, 1.24, { chord: p, whip: true, cymbal: i % 4 === 0, roll: i % 8 === 7, timp: true, sparkle: true });
    if (i % 2 === 1) bassDrum(dryL, dryR, tBeat(b, 1), 0.46);
    if (i % 8 === 0) bell(musL, musR, tBeat(b, 0), 0.15, 28, 5.0);
  }
  playTune(FINALE, s, { voice: 'calliope', amp: 0.100 });
  playTune(FINALE, s, { voice: 'brass', amp: 0.072, oct: 12 });
  playTune(TUNE_B, s + 8, { voice: 'brass', amp: 0.105 });
  playTune(TUNE_B, s + 8, { voice: 'calliope', amp: 0.072, oct: 12, pan: 0.3 });
  playTune(TUNE_B, s + 16, { voice: 'brass', amp: 0.112 });
  playTune(TUNE_B, s + 16, { voice: 'bones', amp: 0.062, oct: 12, pan: -0.4 });
  playTune(TUNE_A, s + 24, { voice: 'calliope', amp: 0.105 });
  playTune(TUNE_A, s + 24, { voice: 'brass', amp: 0.092 });
  playTune(TUNE_A, s + 32, { voice: 'calliope', amp: 0.110, oct: 12 });
  playTune(TUNE_A, s + 32, { voice: 'reed', amp: 0.088, pan: -0.3 });
  // 最后冲刺 40-45
  for (let i = 40; i < 46; i++) {
    const b = s + i;
    snareRoll(dryL, dryR, tBeat(b, 0), barDur(b) * 0.98, 0.18);
    timpRoll(dryL, dryR, tBeat(b, 0), barDur(b) * 0.98, 0.50, 28);
    [64, 67, 71].forEach((m, k) => tableVoice(musL, musR, -0.4 + k * 0.4, tBeat(b, 0), T_BRASS, m, 0.062, 0.03, barDur(b) * 0.9, 0.2, { detune: (k - 1) * 6 }));
    whip(dryL, dryR, tBeat(b, 1), 0.28, (i % 2 ? 0.4 : -0.4));
  }
  crowd(tBeat(s), barDur(s) * 44, 0.026);
  [3, 11, 19, 27, 35, 41, 44].forEach((rb, k) => scream(musL, musR, k % 2 ? 0.42 : -0.42, tBeat(s + rb, 0), k % 3 === 0 ? 83 : 79, 0.048, 0.62, { vowel: k % 2 ? 'e' : 'a', glide: -8 }));
}

/* ---- VIII. 落幕 (H: 146-149) ---- */
{
  const s = SEC.H.start;
  [64, 67, 71, 58, 70].forEach((m, i) => {
    calliope(musL, musR, -0.5 + i * 0.25, tBeat(s, 0), m, 0.098, 1.2, { detune: (i - 2) * 7, wow: 1.4 });
    tableVoice(musL, musR, -0.4 + i * 0.2, tBeat(s, 0), T_BRASS, m - 12, 0.098, 0.02, 1.0, 1.6, { detune: (i - 2) * 8 });
  });
  tuba(musL, musR, 0, tBeat(s, 0), 28, 0.20, 1.4, 1.2);
  thud(dryL, dryR, tBeat(s, 0), 0.50);
  bassDrum(dryL, dryR, tBeat(s, 0), 0.80);
  timp(dryL, dryR, tBeat(s, 0), 0.60, 28, 0);
  gong(musL, musR, tBeat(s, 0), 0.22, 110, 4.0);
  bell(musL, musR, tBeat(s, 0), 0.30, 28, 8.0);
  cymbal(dryL, dryR, tBeat(s, 0), 0.24, 2.6, 0);
  chain(musL, musR, tBeat(s, 0.5), 0.125);
  chain(musL, musR, tBeat(s + 1, 0), 0.098);
  chain(musL, musR, tBeat(s + 1, 1), 0.070);
  scream(musL, musR, -0.2, tBeat(s + 1, 1), 72, 0.034, 0.5, { vowel: 'u', glide: -5 });
  // 发条停止
  tick(dryL, dryR, tBeat(s + 1, 0.5), 0.050, 0.2, 2500);
  tick(dryL, dryR, tBeat(s + 1, 1.5), 0.040, -0.2, 2500);
  tick(dryL, dryR, tBeat(s + 2, 0), 0.028, 0, 2400);
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
  sendL[i] = musL[i - PRE] * 0.30 + dryL[i - PRE] * 0.10;
  sendR[i] = musR[i - PRE] * 0.30 + dryR[i - PRE] * 0.10;
}
const revL = reverbChannel(sendL, 0, 0.83, 0.24, 0.05);
const revR = reverbChannel(sendR, 23, 0.83, 0.24, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.5, dryRms * 0.38 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) { outL[i] = musL[i] + dryL[i] + revL[i] * wetGain; outR[i] = musR[i] + dryR[i] + revR[i] * wetGain; }
const fadeIn = S(0.25), fadeOut = S(4.5);
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
const EQ_CHAIN = [hpCoeff(38, 0.707), peakCoeff(110, 0.9, -5.0), peakCoeff(250, 1.1, -1.5), peakCoeff(1200, 1.0, -0.5), peakCoeff(2600, 0.9, 1.8), peakCoeff(4800, 0.9, 1.4), shelfCoeff(8000, 1.8)];
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
const outFile = path.join(outDir, process.argv[2] || 'puppet.wav');
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
