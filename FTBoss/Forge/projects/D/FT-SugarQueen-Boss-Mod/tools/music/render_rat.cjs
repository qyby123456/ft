/*
 * 鼠群 (Rat Swarm) Boss 战斗BGM —— 「鼠潮」版 (v1, 完全原创)
 *
 * 参考取向: "偷偷摸摸 / 擦擦索索 / 数量众多 / 突然爆发" 这一**体裁语汇**。
 *   只借用"鼠潮"这个意象与技法语汇(pizzicato 碎步、高频摩擦噪声、吱吱点缀、
 *   突然的全体静默后再爆发), **不引用任何现有商业曲目的旋律/和声**,
 *   本曲的旋律与和声走向全部为本项目原创。
 *
 * 与既有两首的关系(刻意错开语汇):
 *   沙耶《茶の唄》        = b小调 6/8 女声叙事曲, 音乐盒摇篮 + 心跳
 *   姜饼王《木偶剧场·加洛普》= e小调 2/4+7/8 手摇风琴加洛普
 *   本曲                  = a小调 6/8 碎步(不等距重音错位), 拨弦/玩具钢琴/口哨/沙锤/吱吱
 *                           —— 不用音乐盒摇篮与酒馆舞曲两套语汇
 *
 * 曲式(见下方 sec() 表):
 *   索索试探 → 小跑 → 尖啸 → **[全体急停] 静默** → 鼠潮(全奏加速) → 消散
 *   戏剧点: 全体重击 → 真静默(只剩远处吱吱 + 一声心跳般低音) → 鼠潮爆发
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const TAIL = 8.0;

/* ================= 时间轴 =================
 * beats = 该小节占几个「四分音符」; 6/8 → beats 3, bpm = 附点四分音符速度。
 * 速度一律控制在 132~168(终段 ramp 到 184), 每小节 12 个八分(= 4 个附点四分)。
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
sec('P', 12, 3, 144);                       //  0- 11  索索试探 (稀疏, 几乎无节奏)
sec('Q', 18, 3, 152);                       // 12- 29  小跑 (节奏化 pizzicato 与沙锤)
sec('R', 16, 3, 160);                       // 30- 45  尖啸 (高频群奏, 密集)
sec('H', 1, 3, 160);                        // 46      全体重击 (惊吓点)
sec('S', 4, 3, 150);                        // 47- 50  静默 (只剩远处吱吱 + 一声心跳低音)
sec('T', 26, 3, null, [168, 184]);          // 51- 76  鼠潮 (全奏急速, 半音上行)
sec('U', 13, 3, 138);                       // 77- 89  消散 (远去, 渐弱收尾)

const BARS = METER.length;
const tBar = [];
let TOTAL = 0;
for (const m of METER) { tBar.push(TOTAL); TOTAL += m.beats * 60 / m.bpm; }
const barDur = bar => { const b = Math.max(0, Math.min(METER.length - 1, Math.floor(bar))); return METER[b].beats * 60 / METER[b].bpm; };
const bpmAt = bar => METER[Math.max(0, Math.min(METER.length - 1, Math.floor(bar)))].bpm;
const qOf = bar => 60 / bpmAt(bar);           // 一个四分音符的秒数
const e8Of = bar => qOf(bar) / 2;             // 一个八分音符(碎步的基本单位)的秒数
/** 支持小数小节 */
const tBeat = (bar, q) => {
  const b0 = Math.floor(bar), fr = bar - b0;
  const bb = Math.max(0, Math.min(METER.length - 1, b0));
  return tBar[bb] + fr * barDur(bb) + q * 60 / METER[bb].bpm;
};
/** 第 e 个八分音符位置(0..11) */
const t8 = (bar, e) => tBeat(bar, e * 0.5);

const N = Math.ceil((TOTAL + TAIL) * SR);
const S = t => Math.round(t * SR);

const rnd = (() => { let a = 20261007; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
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
function mkBp(fc, q) { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; }
function bpRun(src, co, st) { const o = co.b0 * src + st.z1; st.z1 = co.b2 * src - co.a1 * o - co.a2 * st.z2; st.z2 = o; return o; }

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
const T_STR = makeTable(sawH(30, 14));                       // 弦乐
const T_SAW = makeTable(sawH(40, 26));                       // 哨/吱吱的激励源
const T_NAY = makeTable([1, 0.05, 0.55, 0.08, 0.28, 0.14, 0.07, 0.04]); // 口哨(近似管腔)

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
function bassDrum(bl, br, start, amp) {
  const len = Math.floor(0.42 * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 46 + (128 - 46) * Math.exp(-t / 0.024);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.115) * (1 - Math.exp(-t / 0.0014));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.22 * (w - lp);
    s += lp * Math.exp(-t / 0.006) * 0.38;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
function snare(bl, br, start, amp, pan) {
  const len = Math.floor(0.19 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let yPrev = 0, wPrev = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const hp = 0.93 * (yPrev + w - wPrev); yPrev = hp; wPrev = w;
    ph += 2 * Math.PI * 212 / SR;
    const s = (hp * Math.exp(-t / 0.042) * 0.9 + Math.sin(ph) * Math.exp(-t / 0.030) * 0.30) * amp;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
function snareRoll(bl, br, start, durSec, amp) {
  const step = 0.055;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    snare(bl, br, start + t, amp * (0.22 + 0.78 * x), Math.sin(t * 29) * 0.45);
  }
}
/** 定音鼓: note 直接就是实发音高 —— 落在 82~110Hz(E2~A2)。
 *  不要再用 0.5 往下压八度: 定音鼓一旦落到 37~55Hz, 它会独占整个低频(实测 37.7Hz 一条
 *  正弦就占了 30% 能量), 既听不清又抢走全部响度余量。 */
function timp(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note);
  const len = Math.floor(0.95 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * 0.9 + f0 * 0.16 * Math.exp(-t / 0.05);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.30) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.10 * (w - lp);
    s += lp * Math.exp(-t / 0.012) * 0.4;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
function timpRoll(bl, br, start, durSec, amp, note) {
  const step = 0.052;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    timp(bl, br, start + t, amp * (0.16 + 0.52 * x) * (1 + 0.16 * Math.sin(t * 38)), note, (x - 0.5) * 0.5);
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
/** 闷响(惊吓点的皮下低频) */
function thud(bl, br, start, amp) {
  const len = Math.floor(0.7 * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 44 + (92 - 44) * Math.exp(-t / 0.05);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.20) * (1 - Math.exp(-t / 0.002));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.10 * (w - lp);
    s += lp * Math.exp(-t / 0.018) * 0.5;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
  }
}
/** 心跳般的低音: 静默段的那一声(基频 72~106Hz, 不往 subsonic 里钻) */
function heart(start, amp, pan) {
  const len = Math.floor(0.30 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 72 + 34 * Math.exp(-t / 0.03);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.080) * (1 - Math.exp(-t / 0.004));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.12 * (w - lp);
    s += lp * Math.exp(-t / 0.010) * 0.35;
    put(dryL, startI + i, s * amp * pl); put(dryR, startI + i, s * amp * pr);
  }
}

/* ================= 本曲新音色(鼠潮语汇) ================= */

/** 拨弦/拨奏 pizzicato: 极短促、干、颗粒分明 —— "小爪子在跑" 的主音色。
 *  非谐分音(每音三根弦各自失谐)+ 快起快落 + 6ms 拨片咬弦噪声;
 *  echo>0 时低音会多一次 28ms 的"跳爪"重复音(flam), 像小脚踩了两下。 */
function pizz(bl, br, pan, start, note, amp, opt) {
  opt = opt || {};
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note) * Math.pow(2, (opt.detune || 0) / 1200);
  const body = opt.body !== undefined ? opt.body : 0.055;
  const br0 = opt.bright !== undefined ? opt.bright : 1.0;
  const rings = (opt.echo || 0) > 0 && f0 < 900 && rnd() < (opt.echo || 0);
  const reps = rings ? 2 : 1;
  const startI = S(start);
  for (let r = 0; r < reps; r++) {
    const st = S(start + r * (r === 0 ? 0.028 : 0));
    const g = r === 0 ? 1 : 0.45;
    const str = [-0.42, 0, 0.38].map(c => f0 * Math.pow(2, c / 10) * Math.pow(2, ((rnd() * 2 - 1) * 4) / 1200));
    const dec = [body, body * 0.62, body * 0.40];
    const ph = str.map(() => 0);
    let hp = 0, hpPrev = 0, wPrev = 0;
    for (let i = 0; i < S(body * 7 + 0.10); i++) {
      const t = i / SR;
      let s = 0;
      for (let k = 0; k < 3; k++) {
        ph[k] += 2 * Math.PI * str[k] * (1 + 0.0012 * (k + 1)) / SR;
        s += Math.sin(ph[k]) * Math.exp(-t / (dec[k] * br0)) / (1 + k * 0.7);
      }
      const w = rnd() * 2 - 1;
      hp = 0.90 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
      s += hp * Math.exp(-t / 0.006) * 0.85;          // 拨片咬弦
      s *= (1 - Math.exp(-t / 0.0012));
      s += (rnd() * 2 - 1) * Math.exp(-t / 0.004) * 0.10;
      put(bl, startI + i, s * amp * g * pl); put(br, startI + i, s * amp * g * pr);
    }
  }
}

/** 木琴/玩具钢琴: 高音、明亮、颗粒感。非谐分音 + 短促木槌瞬态 + 少量铃样泛音 */
function toyPiano(bl, br, pan, start, note, amp, opt) {
  opt = opt || {};
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note) * Math.pow(2, (opt.detune || 0) / 1200);
  const B = opt.B !== undefined ? opt.B : 0.0035;
  const dec = opt.dec || 0.42;
  const len = Math.floor((dec * 3.4 + 0.18) * SR), startI = S(start);
  const parts = [1, 2, 3, 4, 5];
  const ph = parts.map(() => 0);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < parts.length; k++) {
      const fk = f0 * parts[k] * (1 + B * parts[k] * parts[k]);
      ph[k] += 2 * Math.PI * fk / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / (dec * Math.pow(0.62, k))) / (1 + k * 1.35);
    }
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.0045) * 0.55;   // 木槌瞬态
    s += Math.sin(2 * Math.PI * f0 * 5.42 * t) * Math.exp(-t / 0.055) * 0.055; // 铃样泛音
    const w = rnd() * 2 - 1; lp += 0.5 * (w - lp);
    s += lp * Math.exp(-t / 0.008) * 0.22;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}

/** 口哨(偷偷吹的那种): 管腔音色 + 大量吹气噪声 + 揉音 + 滑音。
 *  glide 单位 = 半音(相对起始音高), 在 durSec 内滑到 note+glide */
function whistle(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const [pl, pr] = panGains(pan || 0);
  const atk = opt.atk !== undefined ? opt.atk : 0.075, rel = opt.rel !== undefined ? opt.rel : 0.13;
  const glide = opt.glide || 0;
  const vibHz = opt.vibHz !== undefined ? opt.vibHz : 5.0, vibC = opt.vibCents !== undefined ? opt.vibCents : 16;
  const breath = opt.breath !== undefined ? opt.breath : 1.0;
  const len = Math.ceil((atk + durSec + rel + 0.05) * SR), startI = S(start);
  const f0 = midiHz(note);
  const VOW = [0.5, 0.75, 1.0, 0.85, 0.55, 0.3, 0.15, 0.08];
  const biq = [0.62, 0.28].map((g, k) => ({ g, co: mkBp(1800 + k * 1100, 1.1), st: { z1: 0, z2: 0 } }));
  let ph = 0, pn = 0, pnPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let a = adsr(t, atk + durSec, atk, 0.10, 1, rel);
    if (a <= 0) continue;
    const x = Math.min(1, t / (durSec + rel));
    const bend = glide * x + (vibC * Math.sin(2 * Math.PI * vibHz * t) * (t > 0.16 ? 1 : t / 0.16)) / 12;
    const f = f0 * Math.pow(2, bend / 12);
    ph += 2 * Math.PI * f / SR;
    let s = 0;
    for (let k = 0; k < VOW.length; k++) s += Math.sin(ph * (k + 1)) * VOW[k];
    const w = rnd() * 2 - 1;
    pn = 0.72 * (pnPrev + w - wPrev); pnPrev = pn; wPrev = w;      // 带通噪声
    let air = 0;
    for (const b of biq) air += bpRun(w, b.co, b.st) * b.g;
    a *= 0.82 + 0.18 * Math.sin(2 * Math.PI * 0.7 * t + note);
    put(bl, startI + i, (s * 0.42 + pn * 0.16 + air * 2.1 * breath) * a * amp * pl);
    put(br, startI + i, (s * 0.42 + pn * 0.16 + air * 2.1 * breath) * a * amp * pr);
  }
}

/** 沙锤/纸盒/鼠爪踩地: 高频摩擦噪声, 节奏化。ticks 控制纸盒棱的摩擦感 */
function shaker(bl, br, start, amp, pan, opt) {
  opt = opt || {};
  const durS = opt.dur || 0.075, accent = opt.accent !== undefined ? opt.accent : 1;
  const len = Math.floor((durS + 0.05) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    hp = 0.95 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
    let s = hp * Math.exp(-t / (durS * 0.34));
    if (opt.ticks) s *= 0.55 + 0.45 * Math.abs(Math.sin(2 * Math.PI * (opt.tickHz || 190) * t));
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.006) * 0.45;            // 起手的纸盒边
    const env = 1 - Math.exp(-t / 0.0012);
    const g = accent * (opt.grain ? (0.75 + 0.25 * rnd()) : 1);
    put(bl, startI + i, s * env * amp * g * pl); put(br, startI + i, s * env * amp * g * pr);
  }
}

/** 高频"吱吱": 短促上滑音 + 两个共鸣峰 + 快速抖。wob=1 时加入不规则的抖 */
function squeak(bl, br, pan, start, note, amp, opt) {
  opt = opt || {};
  const [pl, pr] = panGains(pan || 0);
  const durS = opt.dur || 0.11, glide = opt.glide !== undefined ? opt.glide : 14;
  const len = Math.floor((durS + 0.09) * SR), startI = S(start);
  const biq = [{ g: 1.0, co: mkBp(3200, 11), st: { z1: 0, z2: 0 } },
               { g: 0.6, co: mkBp(5400, 9), st: { z1: 0, z2: 0 } }];
  const f0 = midiHz(note);
  let ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, durS, 0.004, 0.02, 1, 0.08);
    if (a <= 0) continue;
    const x = Math.min(1, t / durS);
    const f = f0 * Math.pow(2, (glide * x * x + (opt.wob ? 0.9 * Math.sin(2 * Math.PI * 34 * t) : 0)) / 12);
    ph += 2 * Math.PI * f / SR;
    const src = ph % (2 * Math.PI) < Math.PI ? 1 : -1;             // 方波激励
    let s = src * 0.32;
    for (const b of biq) s += bpRun(src, b.co, b.st) * b.g * 1.5;
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.02) * 0.16;
    put(bl, startI + i, s * a * amp * pl); put(br, startI + i, s * a * amp * pr);
  }
}

/** 低音提琴的不安震音 tremolo drone: 弓在弦上来回, 两个微失谐音快速交替 + 缓起缓落。
 *  note 是"写谱音高", 实际发声再上一八度(+12):
 *  这样 drone 落在 82~110Hz(听得见的低音区), 而不是 31~41Hz —— 后者在这里只会
 *  把 0-40Hz 塞满(实测占 74% 能量)、挤掉响度余量却几乎听不见。 */
function tremDrone(start, durSec, note, amp, opt) {
  opt = opt || {};
  const tHz = opt.tHz || 13;
  const len = Math.floor(durSec * SR), startI = S(start);
  const f = midiHz(note + 12);
  const det = opt.detune !== undefined ? opt.detune : 7;
  const ph = [0, 0, 0];
  const ratios = [1, 2, 3];
  const fset = [f, f * Math.pow(2, det / 1200), f * 1.5 * Math.pow(2, -det / 1200)];
  const swayHz = opt.swayHz || 0.22;
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const env = Math.min(1, t / (opt.atk || 1.6)) * Math.min(1, (durSec - t) / (opt.rel || 2.0));
    const tr = 0.45 + 0.55 * (0.5 + 0.5 * Math.sin(2 * Math.PI * tHz * t + Math.sin(2 * Math.PI * swayHz * t) * 2));
    let s = 0;
    for (let k = 0; k < 3; k++) {
      ph[k] += 2 * Math.PI * fset[k] * ratios[k] * (1 + 0.0011 * k) / SR;
      s += Math.sin(ph[k]) * [1, 0.30, 0.13][k];
    }
    s *= (1 - tr) * 0.32 + tr;                                     // 震音
    const w = rnd() * 2 - 1; lp += 0.5 * (w - lp);
    s += lp * 0.06;                                                // 弓毛
    put(musL, startI + i, s * amp * env);
    put(musR, startI + i, s * amp * env * 0.93);
  }
}

/** 全体重击: 低音 + 噪声 + 铁片 —— 惊吓点。
 *  各层在 30ms 内错开(而不是同刻硬叠): 既保持"全体"的冲击力, 又把瞬态峰值摊开 ~3dB。 */
function smash(start, amp) {
  bassDrum(dryL, dryR, start, amp * 0.85);
  thud(dryL, dryR, start + 0.006, amp * 0.66);
  timp(dryL, dryR, start + 0.013, amp * 0.58, 45, 0);
  cymbal(dryL, dryR, start + 0.005, amp * 0.28, 2.8, 0);
  gong(musL, musR, start + 0.018, amp * 0.19, 118, 3.2);
  const amp2 = amp * 0.55;
  const len = Math.floor(0.55 * SR), startI = S(start);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    hp = 0.86 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
    const s = hp * Math.exp(-t / 0.09) * (1 - Math.exp(-t / 0.0008)) * 0.5;
    put(dryL, startI + i, s * amp2); put(dryR, startI + i, s * amp2 * 0.92);
  }
  for (let k = 0; k < 6; k++) {
    const m = [59, 62, 65, 68, 71, 74][k];
    tableVoice(musL, musR, -0.5 + k * 0.2, start + 0.002 + k * 0.0035, T_STR, m, amp * 0.062, 0.008, 0.44, 0.36, { detune: (k - 3) * 9 });
  }
}

/* ================= 和声 (a 小调 + 半音阶色彩) =================
 * 12 小节循环; 刻意保留 ♭2(Bb)、#4(D# 由 tremDrone 的失谐给)、以及那不勒斯
 * ♭II(Bb) 的用法, 制造"鼠"的滑腻感。
 */
const CH = {
  Am: { bass: 33, pad: [57, 60, 64] },    // Am    A1 / A3 C4 E4
  F: { bass: 29, pad: [57, 60, 65] },     // F     F1 / A3 C4 F4
  Dm: { bass: 26, pad: [57, 62, 65] },    // Dm    D1 / A3 D4 F4
  E: { bass: 28, pad: [56, 59, 64] },     // E     E1 / G#3 B3 E4 (和声小调 V)
  C: { bass: 36, pad: [55, 60, 64] },     // C     C2 / G3 C4 E4
  Bb: { bass: 34, pad: [58, 62, 65] },    // Bb    Bb1/ Bb3 D4 F4  (♭II 那不勒斯)
  Cshd: { bass: 25, pad: [55, 61, 64] },  // C#dim C#1/ G3 C#4 E4
  B7: { bass: 35, pad: [59, 62, 65] }     // B7    B1 / B3 D4 F4
};
const LOOP = ['Am', 'F', 'Dm', 'E', 'Am', 'C', 'Bb', 'Cshd', 'Dm', 'E', 'F', 'E'];
const chordAt = bar => CH[LOOP[((bar % 12) + 12) % 12]];

/** 鼠潮段的半音上行: 每 12 小节整条循环 +1 半音 */
const CHROM = [
  { r: 28, ch: [57, 61, 64] },  // E  +  A  C  E   (A/C 双调性压 E)
  { r: 29, ch: [57, 61, 65] },  // F
  { r: 30, ch: [58, 62, 65] },  // F#
  { r: 31, ch: [59, 62, 67] },  // G
  { r: 32, ch: [59, 63, 68] },  // G#
  { r: 33, ch: [60, 64, 69] },  // A
  { r: 34, ch: [61, 64, 70] },  // Bb
  { r: 35, ch: [62, 65, 71] },  // B
  { r: 36, ch: [62, 66, 72] },  // C
  { r: 36, ch: [61, 66, 72] },  // C  (♭5)
  { r: 35, ch: [61, 64, 70] },  // B
  { r: 28, ch: [56, 59, 64] }   // E  回落
];
const chromAt = (bar, lift) => {
  const c = CHROM[((bar % 12) + 12) % 12];
  return { r: c.r + (lift || 0), ch: c.ch.map(m => m + (lift || 0)) };
};

/* ================= 主题素材(全部原创) ================= */
/** 鼠探动机 T_PROBE: 偷偷摸摸, 半音邻近 + 二三度来回, 不解决 */
const T_PROBE = [
  [0, 0, 69, 1], [0, 2, 71, 1], [0, 4, 70, 1],
  [1, 1, 67, 1], [1, 3, 69, 2],
  [2, 0, 72, 1], [2, 2, 71, 1], [2, 4, 68, 1],
  [3, 1, 65, 1], [3, 3, 69, 2],
  [4, 0, 64, 1], [4, 2, 65, 1], [4, 4, 69, 1],
  [5, 1, 71, 1], [5, 3, 69, 2]
];
/** 小跑主题 T_TROT: 上行碎步 + 偶发大跳, 落点错位 */
const T_TROT = [
  [0, 0, 69, 1], [0, 1, 72, 1], [0, 2, 71, 1], [0, 3, 72, 1], [0, 5, 74, 1],
  [1, 0, 72, 1], [1, 1, 71, 1], [1, 2, 69, 1], [1, 4, 68, 2],
  [2, 0, 69, 1], [2, 1, 72, 1], [2, 2, 74, 1], [2, 3, 75, 1], [2, 5, 76, 1],
  [3, 0, 74, 1], [3, 1, 72, 1], [3, 3, 71, 2],
  [4, 0, 76, 1], [4, 1, 74, 1], [4, 2, 72, 1], [4, 4, 71, 1], [4, 5, 69, 1],
  [5, 0, 71, 1], [5, 1, 68, 1], [5, 3, 69, 2],
  [6, 0, 69, 1], [6, 1, 72, 1], [6, 2, 74, 1], [6, 3, 76, 1], [6, 5, 74, 1],
  [7, 0, 72, 1], [7, 2, 71, 1], [7, 4, 69, 2]
];
/** 尖啸群奏 T_SHRIEK: 半音爬升 + 突然收窄, 高八度 */
const T_SHRIEK = [
  [0, 0, 81, 1], [0, 1, 82, 1], [0, 2, 83, 1], [0, 4, 84, 2],
  [1, 0, 83, 1], [1, 1, 81, 1], [1, 3, 80, 1], [1, 4, 79, 2],
  [2, 0, 81, 1], [2, 1, 84, 1], [2, 2, 85, 1], [2, 4, 87, 2],
  [3, 0, 85, 1], [3, 1, 84, 1], [3, 3, 81, 1], [3, 4, 80, 2],
  [4, 0, 88, 1], [4, 1, 87, 1], [4, 2, 85, 1], [4, 4, 84, 1], [4, 5, 83, 1],
  [5, 0, 81, 1], [5, 1, 80, 1], [5, 3, 81, 2],
  [6, 0, 84, 1], [6, 1, 85, 1], [6, 2, 87, 1], [6, 4, 88, 2],
  [7, 0, 86, 1], [7, 2, 84, 1], [7, 4, 81, 2]
];
/** 消散段: 下行半音碎片回归 a 小调 */
const T_FADE = [
  [0, 0, 81, 2], [1, 0, 80, 2], [2, 0, 79, 2], [3, 0, 76, 3],
  [4, 0, 74, 2], [5, 0, 72, 2], [6, 0, 71, 3],
  [8, 0, 69, 2], [9, 0, 68, 2], [10, 0, 67, 2], [12, 0, 69, 4]
];

/** 统一演奏器: opts.voice = pizz | toy | whistle */
function playTune(tune, bar0, opt) {
  opt = opt || {};
  const oct = opt.oct || 0, amp = opt.amp !== undefined ? opt.amp : 0.1;
  const v = opt.voice || 'pizz', pan = opt.pan || 0;
  const holdScale = opt.holdScale !== undefined ? opt.holdScale : 1;
  tune.forEach(([b, e, m, dq]) => {
    const bar = bar0 + b, note = m + oct;
    const st = t8(bar, e);
    if (v === 'pizz') pizz(musL, musR, pan, st, note, amp, { detune: opt.detune || 0, body: opt.body, bright: opt.bright, echo: opt.echo });
    else if (v === 'toy') toyPiano(musL, musR, pan, st, note, amp, { detune: opt.detune || 0 });
    else whistle(musL, musR, pan, st, note, amp, dq * e8Of(bar) * holdScale, { glide: opt.glide || 0, breath: opt.breath });
  });
}

/* ================= 节奏型(重音错位) ================= */
/** 不等距重音: 12 个八分里的优先落点, 每 4 小节换一组 —— 像很多小脚不齐 */
function swarmAccents(bar) {
  const set = [[0, 3, 5, 7, 10], [0, 2, 5, 6, 9], [0, 3, 4, 7, 11], [1, 2, 5, 8, 10]][((bar % 4) + 4) % 4];
  const out = set.slice();
  if (rnd() < 0.5) { const j = 1 + Math.floor(rnd() * 10); if (out.indexOf(j) < 0) out.push(j); }
  return out;
}
/** 鼠潮鼓组: 密集、不稳 */
function swarmDrums(bar, amp, opt) {
  opt = opt || {};
  const dens = opt.dens !== undefined ? opt.dens : 1;
  bassDrum(dryL, dryR, t8(bar, 0), 0.40 * amp);
  bassDrum(dryL, dryR, t8(bar, 6), 0.26 * amp);
  [2, 5, 8, 11].forEach((e, k) => snare(dryL, dryR, t8(bar, e), (k % 2 ? 0.24 : 0.29) * amp, k % 2 ? 0.28 : -0.28));
  const extra = dens > 1 ? [3, 7, 9] : [3, 7];
  extra.forEach(e => snare(dryL, dryR, t8(bar, e), 0.105 * amp, e % 2 ? -0.34 : 0.34));
  for (let e = 0; e < 12; e++) shaker(dryL, dryR, t8(bar, e), (e % 3 === 0 ? 0.075 : 0.048) * amp, ((e % 4) - 1.5) * 0.28, { dur: 0.05, accent: 1 });
  swarmAccents(bar).forEach((e, k) => shaker(dryL, dryR, t8(bar, e), 0.055 * amp, (k % 2 ? 0.5 : -0.5), { dur: 0.045, ticks: true, tickHz: 210, grain: true }));
  if (opt.fill) { snareRoll(dryL, dryR, t8(bar, 8), e8Of(bar) * 4 * 0.98, 0.20 * amp); timpRoll(dryL, dryR, t8(bar, 8), e8Of(bar) * 4 * 0.98, 0.30 * amp, (opt.root || 28) + 12); }
}

/* ================= 装配 ================= */
console.log('装配中... 总小节', BARS, '预计', TOTAL.toFixed(1), 's (', (TOTAL + TAIL).toFixed(1), 's 含尾 )');

/** 弦乐持续音铺底(暗色, 很轻) */
function strPad(bar, amp, oct) {
  const p = chordAt(bar);
  p.pad.forEach((m, i) => tableVoice(musL, musR, -0.5 + i * 0.5, t8(bar, 0), T_STR, m + (oct || 0), amp * (i === 0 ? 1 : 0.82), 0.5, barDur(bar) * 0.94, 0.8, { detune: (i - 1) * 5, vibHz: 3.4, vibCents: 5 }));
}
/** 全体静默: 把器乐总线清零(真正的"突然全体静默"; 混响尾已被 send 提前取出, 所以尾巴还在) */
function muteAt(tStart, tEnd) {
  const a = S(tStart), b = S(tEnd);
  for (let i = a; i < b; i++) { musL[i] = 0; musR[i] = 0; dryL[i] = 0; dryR[i] = 0; }
}
/** 半音上行音簇推进(尖啸/鼠潮用): 七声高音簇, 每半秒整簇上行半音 */
function clusterPush(start, durSec, amp) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const voices = [];
  for (let m = 0; m < 7; m++) {
    const pan = -0.6 + m * 0.2, g = panGains(pan);
    voices.push({ m: 72 + m, ph: 0, gl: g[0], gr: g[1], det: (m % 2 ? -13 : 13) });
  }
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const x = t / durSec;
    const base = Math.pow(2, (Math.floor(x * 12) * 0.5) / 12);
    let sl = 0, sr = 0;
    for (const v of voices) {
      v.ph += 2 * Math.PI * midiHz(v.m) * base * Math.pow(2, v.det / 1200) / SR;
      const s = Math.sin(v.ph);
      sl += s * v.gl; sr += s * v.gr;
    }
    const env = Math.min(1, t / 1.4) * Math.min(1, (durSec - t) / 0.9);
    put(musL, startI + i, sl * env * amp * 0.42);
    put(musR, startI + i, sr * env * amp * 0.42);
  }
}

/* ---- I. 索索试探 (P: 0-11) 稀疏拨弦 + 吱吱, 几乎无节奏 ---- */
{
  const s = SEC.P.start;
  tremDrone(t8(s, 0), barDur(s) * 6.2, 45, 0.060, { tHz: 9.5, detune: 9 });        // 写谱 A1 → 实发 110Hz
  tremDrone(t8(s + 6, 0), barDur(s) * 5.6, 43, 0.055, { tHz: 8, detune: 13 });     // 写谱 G1 → 实发 98Hz
  for (let i = 0; i < 12; i++) {
    const b = s + i;
    // 稀疏的试探动机, 隔三差五才动一下
    if (i >= 2 && i <= 7) playTune(T_PROBE.slice(0, 3), b, { voice: 'pizz', amp: 0.085, pan: (i % 2 ? 0.28 : -0.28), body: 0.05 });
    if (i === 9) playTune(T_PROBE.slice(6, 9), b, { voice: 'pizz', amp: 0.080, pan: -0.2 });
    // 远处吱吱: 随机分布
    const nk = 1 + (rnd() < 0.45 ? 1 : 0);
    for (let k = 0; k < nk; k++) {
      const e = Math.floor(rnd() * 12);
      squeak(musL, musR, (rnd() * 2 - 1) * 0.8, t8(b, e), 93 + Math.floor(rnd() * 8), 0.030 + rnd() * 0.016, { glide: 10 + rnd() * 10, dur: 0.08 + rnd() * 0.07, wob: rnd() < 0.4 });
    }
    // 纸盒/爪子的零星摩擦(第 4 小节起才开始有节奏感)
    if (i >= 4) [0, 4, 7].forEach((e, k) => shaker(dryL, dryR, t8(b, e + (i % 2)), (k === 0 ? 0.055 : 0.038), (k % 2 ? 0.42 : -0.42), { dur: 0.05, ticks: true, tickHz: 175, grain: true }));
    if (i >= 6 && i % 3 === 0) strPad(b, 0.022);
    // 心跳般的低音从后半开始铺垫
    if (i >= 7) heart(t8(b, 0), 0.15 + (i - 7) * 0.012, -0.12);
  }
  whistle(musL, musR, 0.15, t8(s + 3, 0), 76, 0.055, e8Of(s + 3) * 7, { glide: -2, breath: 1.15 });
  toyPiano(musL, musR, -0.4, t8(s + 10, 6), 88, 0.055, {});
  toyPiano(musL, musR, 0.4, t8(s + 11, 2), 91, 0.045, {});
}

/* ---- II. 小跑 (Q: 12-29) 节奏化 pizzicato 与沙锤 ---- */
{
  const s = SEC.Q.start;
  tremDrone(t8(s, 0), barDur(s) * 18.4, 45, 0.068, { tHz: 11, detune: 11 });
  for (let i = 0; i < 18; i++) {
    const b = s + i, p = chordAt(b), full = i >= 6;
    // 低音拨弦 ostinato: 根音在 0/4/8 八分, 五度在 6/10
    pizz(musL, musR, -0.15, t8(b, 0), p.bass, 0.115, { body: 0.06 });
    pizz(musL, musR, 0.15, t8(b, 4), p.bass, 0.095, { body: 0.055 });
    if (full) {
      pizz(musL, musR, -0.2, t8(b, 8), p.bass, 0.100, { body: 0.055 });
      pizz(musL, musR, 0.2, t8(b, 6), p.bass + 7, 0.070, { body: 0.05 });
      pizz(musL, musR, 0.25, t8(b, 10), p.bass + 7, 0.065, { body: 0.05 });
    }
    // 沙锤: 前段只在 0/3/6/9, 后段布满 + 错位重音
    if (!full) [0, 3, 6, 9].forEach((e, k) => shaker(dryL, dryR, t8(b, e), k === 0 ? 0.062 : 0.044, ((k % 2) - 0.5) * 0.7, { dur: 0.055, ticks: true, tickHz: 185, grain: true }));
    else for (let e = 0; e < 12; e++) shaker(dryL, dryR, t8(b, e), (e % 3 === 0 ? 0.070 : 0.042), ((e % 4) - 1.5) * 0.26, { dur: 0.05, ticks: e % 2 === 0, tickHz: 190, grain: true });
    if (full) { bassDrum(dryL, dryR, t8(b, 0), 0.24); if (i % 2 === 1) bassDrum(dryL, dryR, t8(b, 6), 0.16); }
    if (full && i % 4 === 3) snare(dryL, dryR, t8(b, 11), 0.16, 0.2);
    if (full) strPad(b, 0.026);
    // 玩具钢琴点缀: 落点错位
    if (i % 3 === 0) toyPiano(musL, musR, -0.42, t8(b, 5), p.pad[2] + 12, 0.048, {});
    if (i % 5 === 2) toyPiano(musL, musR, 0.42, t8(b, 9), p.pad[0] + 24, 0.040, {});
    // 吱吱
    const nk = rnd() < 0.4 ? 2 : 1;
    for (let k = 0; k < nk; k++) squeak(musL, musR, (rnd() * 2 - 1) * 0.75, t8(b, Math.floor(rnd() * 12)), 91 + Math.floor(rnd() * 10), 0.026 + rnd() * 0.014, { glide: 9 + rnd() * 9, dur: 0.075 + rnd() * 0.06, wob: rnd() < 0.5 });
  }
  // 小跑主题: 8 小节两遍(第二遍换声部与八度)
  playTune(T_TROT, s + 2, { voice: 'pizz', amp: 0.100, oct: 12, pan: -0.35, body: 0.045, bright: 1.15 });
  playTune(T_TROT, s + 10, { voice: 'toy', amp: 0.062, pan: 0.38 });
  playTune(T_TROT, s + 10, { voice: 'pizz', amp: 0.070, oct: 0, pan: 0.15, body: 0.06, echo: 0.35 });
  whistle(musL, musR, -0.2, t8(s + 16, 0), 76, 0.058, e8Of(s + 16) * 5, { glide: 1, breath: 1.0 });
  whistle(musL, musR, 0.2, t8(s + 17, 6), 79, 0.050, e8Of(s + 17) * 4, { glide: -1, breath: 1.0 });
}

/* ---- III. 尖啸 (R: 30-45) 高频群奏, 密集 ---- */
{
  const s = SEC.R.start;
  tremDrone(t8(s, 0), barDur(s) * 16.2, 45, 0.072, { tHz: 12.5, detune: 8 });
  for (let i = 0; i < 16; i++) {
    const b = s + i, p = chordAt(b), dens = i < 8 ? 1 : 2;
    // 高音拨弦 16 分碎步(每八分两下)
    for (let e = 0; e < 12; e++) {
      if (e % 2 === 1 && i < 4) continue;
      const m = p.pad[(e + i) % 3] + 24 + ((e % 4 === 3) ? 1 : 0);
      pizz(musL, musR, -0.5 + ((e * 7) % 5) * 0.25, t8(b, e), m, (e % 3 === 0 ? 0.052 : 0.038), { body: 0.038, bright: 1.2 });
      if (dens === 2 && e % 2 === 1) pizz(musL, musR, 0.5 - ((e * 5) % 5) * 0.25, t8(b, e) + e8Of(b) * 0.5, m + 5, 0.028, { body: 0.032, bright: 1.35 });
    }
    // 玩具钢琴的密集颗粒
    for (let e = 0; e < 12; e += 2) toyPiano(musL, musR, ((e % 4) - 1.5) * 0.4, t8(b, e), p.pad[(e / 2) % 3] + 24, e % 4 === 0 ? 0.046 : 0.032, {});
    // 沙锤铺满
    for (let e = 0; e < 12; e++) shaker(dryL, dryR, t8(b, e), (e % 3 === 0 ? 0.080 : 0.048), ((e % 4) - 1.5) * 0.28, { dur: 0.048, ticks: e % 2 === 0, tickHz: 200, grain: true });
    swarmAccents(b).forEach((e, k) => shaker(dryL, dryR, t8(b, e), 0.052, (k % 2 ? 0.55 : -0.55), { dur: 0.042, ticks: true, tickHz: 215, grain: true }));
    // 鼓
    bassDrum(dryL, dryR, t8(b, 0), 0.30);
    if (i >= 4) bassDrum(dryL, dryR, t8(b, 6), 0.20);
    [2, 5, 8, 11].forEach((e, k) => snare(dryL, dryR, t8(b, e), (i >= 8 ? 0.22 : 0.17), k % 2 ? 0.3 : -0.3));
    if (i % 4 === 3) { snareRoll(dryL, dryR, t8(b, 8), e8Of(b) * 4 * 0.98, 0.18); timpRoll(dryL, dryR, t8(b, 8), e8Of(b) * 4 * 0.98, 0.26, p.bass + 12); }
    strPad(b, 0.028);
    // 高音群奏尖啸
    if (i % 2 === 0) {
      const t0 = t8(b, 0);
      [0, 3, 7].forEach(k => squeak(musL, musR, (k - 1) * 0.5, t0 + k * 0.028, 96 + k * 2 + (i % 3), 0.040, { glide: 12, dur: 0.14, wob: true }));
    }
    const nk = 2 + (rnd() < 0.5 ? 1 : 0);
    for (let k = 0; k < nk; k++) squeak(musL, musR, (rnd() * 2 - 1) * 0.85, t8(b, Math.floor(rnd() * 12)), 94 + Math.floor(rnd() * 12), 0.028 + rnd() * 0.016, { glide: 8 + rnd() * 12, dur: 0.07 + rnd() * 0.07, wob: rnd() < 0.55 });
  }
  playTune(T_SHRIEK, s + 4, { voice: 'toy', amp: 0.058, pan: -0.4 });
  playTune(T_SHRIEK, s + 4, { voice: 'pizz', amp: 0.062, pan: 0.4, body: 0.034, bright: 1.25 });
  playTune(T_SHRIEK, s + 12, { voice: 'toy', amp: 0.070, pan: 0.0 });
  playTune(T_SHRIEK, s + 12, { voice: 'pizz', amp: 0.075, oct: -12, pan: -0.35, body: 0.05, echo: 0.2 });
  clusterPush(t8(s + 8, 0), barDur(s + 8) * 7.6, 0.030);
  gong(musL, musR, t8(s + 14, 0), 0.10, 128, 2.4);
}

/* ---- IV. 全体重击 (H: 46) → 静默 (S: 47-50) ---- */
{
  const h = SEC.H.start, s = SEC.S.start;
  // 全体重击: 惊吓点
  smash(t8(h, 0), 0.62);
  for (let e = 2; e < 12; e += 2) snare(dryL, dryR, t8(h, e), 0.16, ((e % 4) - 2) * 0.16);
  // 47 小节开头: 全体静默 —— 把总线清零, 只剩混响尾巴
  muteAt(t8(h, 2.0), t8(s, 1.0));
  // 真静默: 只留远处吱吱 + 一声心跳般的低音
  for (let i = 0; i < 4; i++) {
    const b = s + i;
    const nk = i === 0 ? 2 : 1;
    for (let k = 0; k < nk; k++) squeak(musL, musR, (rnd() * 2 - 1) * 0.9, t8(b, 2 + Math.floor(rnd() * 8)), 90 + Math.floor(rnd() * 9), 0.020 + rnd() * 0.010, { glide: 7 + rnd() * 8, dur: 0.06 + rnd() * 0.05, wob: rnd() < 0.5 });
  }
  heart(t8(s + 1, 0), 0.30, -0.1);
  heart(t8(s + 1, 3), 0.13, 0.1);
  tremDrone(t8(s + 1, 0), barDur(s) * 2.9, 45, 0.040, { tHz: 6, detune: 4, atk: 0.8, rel: 1.2 });
  whistle(musL, musR, -0.3, t8(s + 2, 6), 69, 0.040, e8Of(s + 2) * 5, { glide: -3, breath: 1.3 });
  toyPiano(musL, musR, 0.35, t8(s + 3, 0), 93, 0.032, {});
  // 48-50: 极轻的碎步渐起(预备鼠潮)
  for (let i = 1; i < 4; i++) {
    const b = s + i;
    for (let e = i === 3 ? 0 : 4; e < 12; e += 2) {
      shaker(dryL, dryR, t8(b, e), (i === 3 ? 0.030 + e * 0.004 : 0.018), ((e % 4) - 1.5) * 0.3, { dur: 0.045, grain: true });
    }
    pizz(musL, musR, 0, t8(b, 0), 33, 0.05 + i * 0.012, { body: 0.07 });
  }
  // 最后一小节: 定音鼓滚奏冲进鼠潮
  timpRoll(dryL, dryR, t8(s + 3, 0), barDur(s + 3) * 0.97, 0.34, 52);
  cymbal(dryL, dryR, t8(s + 3, 0), 0.10, 2.0, 0);
}

/* ---- V. 鼠潮 (T: 51-76) 全奏急速, 半音上行, 鼓点密集 ---- */
{
  const s = SEC.T.start, nb = SEC.T.count;
  for (let i = 0; i < nb; i++) {
    const b = s + i;
    const lift = i < 12 ? 0 : (i < 24 ? 1 : 2);
    const c = chromAt(b, lift);
    const heat = i < 6 ? 0.85 : (i < 20 ? 1.0 : 1.12);
    if (i === 0 || i === 12 || i === 24) tremDrone(t8(b, 0), barDur(b) * 12.2, c.r, 0.058, { tHz: 13.5, detune: 9 });
    // 急速拨弦: 每小节 12 个八分 × 2 = 24 个十六分, 音高在 chord tone 上打转
    for (let e = 0; e < 12; e++) {
      const m = c.ch[(e + i) % 3] + 12;
      pizz(musL, musR, ((e % 6) - 2.5) * 0.2, t8(b, e), m, 0.070 * heat, { body: 0.036, bright: 1.2 });
      if (i >= 4) pizz(musL, musR, ((e % 4) - 1.5) * -0.28, t8(b, e) + e8Of(b) * 0.5, m + 7, 0.044 * heat, { body: 0.032, bright: 1.3 });
    }
    // 低音: 根音在 0/3/6/9 八分, 八度跳进
    [0, 3, 6, 9].forEach((e, k) => pizz(musL, musR, -0.3, t8(b, e), c.r + (k % 2 ? 0 : 12), k % 2 ? 0.105 : 0.125, { body: 0.07 }));
    // 玩具钢琴八度群
    for (let e = 0; e < 12; e += 2) toyPiano(musL, musR, ((e % 6) - 2.5) * 0.3, t8(b, e), c.ch[(e / 2) % 3] + 24, e % 4 === 0 ? 0.050 : 0.036, {});
    swarmDrums(b, 1.0 * heat, { dens: i >= 12 ? 2 : 1, fill: i % 4 === 3, root: c.r });
    strPad(b, 0.026, 0);
    tremDrone(t8(b, 0), barDur(b) * 0.9, c.r + 12, 0.030, { tHz: 15, detune: 14, atk: 0.06, rel: 0.12 });
    if (i % 4 === 0) gong(musL, musR, t8(b, 0), 0.062, 132 + lift * 8, 1.4);
    const nk = 2 + (rnd() < 0.6 ? 1 : 0);
    for (let k = 0; k < nk; k++) squeak(musL, musR, (rnd() * 2 - 1) * 0.9, t8(b, Math.floor(rnd() * 12)), 96 + Math.floor(rnd() * 10), 0.030 + rnd() * 0.018, { glide: 10 + rnd() * 12, dur: 0.06 + rnd() * 0.07, wob: rnd() < 0.6 });
  }
  // 鼠潮主题: 三遍, 越来越高
  playTune(T_TROT, s, { voice: 'pizz', amp: 0.092, oct: 12, pan: 0.3, body: 0.036, bright: 1.2 });
  playTune(T_TROT, s, { voice: 'toy', amp: 0.052, oct: 0, pan: -0.35 });
  playTune(T_TROT, s + 8, { voice: 'toy', amp: 0.070, oct: 12, pan: 0.25 });
  playTune(T_TROT, s + 8, { voice: 'pizz', amp: 0.086, oct: 0, pan: -0.3, body: 0.05, echo: 0.15 });
  playTune(T_SHRIEK, s + 16, { voice: 'toy', amp: 0.072, oct: 0, pan: 0.3 });
  playTune(T_TROT, s + 16, { voice: 'pizz', amp: 0.090, oct: 24, pan: -0.3, body: 0.03, bright: 1.3 });
  playTune(T_SHRIEK, s + 22, { voice: 'toy', amp: 0.078, oct: 12, pan: 0.0 });
  playTune(T_TROT, s + 22, { voice: 'pizz', amp: 0.094, oct: 12, pan: 0.35, body: 0.034 });
  clusterPush(t8(s + 12, 0), barDur(s + 12) * 11.8, 0.034);
  clusterPush(t8(s + 24, 0), barDur(s + 24) * 1.9, 0.036);
  // 高潮最后一击 + 急速回落
  smash(t8(s + 25, 0), 0.34);
  [0, 2, 4, 6, 8, 10].forEach(e => {
    const m = [40, 41, 43, 45, 46, 48][e / 2];
    pizz(musL, musR, 0, t8(s + 25, e), m, 0.10, { body: 0.05 });
  });
}

/* ---- VI. 消散 (U: 77-89) 远去, 渐弱收尾 ---- */
{
  const s = SEC.U.start;
  tremDrone(t8(s, 0), barDur(s) * 13.2, 45, 0.060, { tHz: 9, detune: 6, atk: 1.2, rel: 3.0 });
  for (let i = 0; i < 13; i++) {
    const b = s + i, p = chordAt(b), deg = 1 - i / 13;
    // 拨弦越来越稀
    const step = i < 4 ? 3 : (i < 8 ? 4 : 6);
    for (let e = 0; e < 12; e += step) pizz(musL, musR, ((e % 4) - 1.5) * 0.3, t8(b, e), p.pad[(e + i) % 3] + 12, 0.062 * deg + 0.012, { body: 0.05 });
    if (i < 9) pizz(musL, musR, -0.25, t8(b, 0), p.bass, 0.085 * deg + 0.015, { body: 0.07 });
    // 沙锤渐远
    if (i < 10) for (let e = 0; e < 12; e += 2) shaker(dryL, dryR, t8(b, e), (0.030 + e * 0.002) * deg, ((e % 4) - 1.5) * 0.35, { dur: 0.045, grain: true });
    // 吱吱渐少渐远
    if (i < 10) {
      const nk = rnd() < 0.5 ? 1 : 0;
      for (let k = 0; k <= nk; k++) squeak(musL, musR, (rnd() * 2 - 1) * 0.9, t8(b, Math.floor(rnd() * 12)), 92 + Math.floor(rnd() * 8), 0.016 + rnd() * 0.010, { glide: 6 + rnd() * 8, dur: 0.06 + rnd() * 0.05, wob: true });
    }
    if (i < 6 && i % 2 === 0) toyPiano(musL, musR, (i % 4 ? 0.35 : -0.35), t8(b, 4), p.pad[1] + 24, 0.034 * deg + 0.008, {});
    if (i < 5) strPad(b, 0.024 * deg + 0.008);
    if (i < 4) bassDrum(dryL, dryR, t8(b, 0), 0.16 * deg);
  }
  playTune(T_FADE, s + 1, { voice: 'pizz', amp: 0.082, pan: -0.25, body: 0.045 });
  playTune(T_FADE, s + 5, { voice: 'toy', amp: 0.046, pan: 0.32 });
  // 口哨暗号最后一次, 远去
  whistle(musL, musR, 0.0, t8(s + 8, 0), 76, 0.062, e8Of(s + 8) * 9, { glide: -2.5, breath: 1.2, rel: 0.4 });
  whistle(musL, musR, -0.2, t8(s + 9, 6), 72, 0.046, e8Of(s + 9) * 6, { glide: -4, breath: 1.35, rel: 0.5 });
  heart(t8(s + 11, 0), 0.18, -0.1);
  heart(t8(s + 11, 3), 0.09, 0.1);
  // 终: 一声低音拨弦 + 定音鼓, 远去
  pizz(musL, musR, 0, t8(s + 12, 0), 33, 0.12, { body: 0.12 });
  pizz(musL, musR, -0.4, t8(s + 12, 3), 45, 0.07, { body: 0.10 });
  pizz(musL, musR, 0.4, t8(s + 12, 6), 40, 0.05, { body: 0.10 });
  timp(dryL, dryR, t8(s + 12, 0), 0.22, 45, 0);
  gong(musL, musR, t8(s + 12, 0), 0.10, 96, 3.0);
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
  sendL[i] = musL[i - PRE] * 0.32 + dryL[i - PRE] * 0.09;
  sendR[i] = musR[i - PRE] * 0.32 + dryR[i - PRE] * 0.09;
}
const revL = reverbChannel(sendL, 0, 0.82, 0.26, 0.05);   // 鼠潮是干颗粒型, 混响偏短偏暗
const revR = reverbChannel(sendR, 31, 0.82, 0.26, 0.05);
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

/* ================= 分段落电平配平(自动化) =================
 * 本曲动态极大: 索索试探 RMS≈0.03 / 鼠潮 RMS≈0.19 —— 相差 12dB, 峰均比约 19dB。
 * 若原样进母带, 要打到 RMS≈0.10 就得整体 +6dB, 峰值会撞到 1.6(削波)。
 * 所以按"写谱意图"给每段一个目标 RMS, 逐段量出实际 RMS 再算出该段增益:
 *   索索试探(P) / 消散(U) = 有意最轻(0.044 / 0.048, 主题上就是"忽远忽近"的远)
 *   小跑(Q) / 尖啸(R)     = 中(0.068 / 0.078)
 *   全体重击(H)           = 最响(0.100)
 *   静默(S)               = 极轻(0.034)
 *   鼠潮(T)               = 响(0.070, 但它是持续的密集群奏, 短时 RMS 已经够高)
 * 这样 12dB 的落差留在音乐里(而不是被动地整曲抬 6dB), 峰值稳在 ~0.6,
 * 母带那一步只剩很小的增益, 于是 satSamples ≈ 0 且没有任何削波。
 * 段落是音乐单位, 边界本来就有句读, 所以逐段(而非逐样本平滑)是本曲更正确的做法。
 */
const TGT = { P: 0.044, Q: 0.068, R: 0.078, H: 0.100, S: 0.034, T: 0.070, U: 0.048 };
const gGain = new Float32Array(N).fill(1);
const gainReport = [];
for (const k of Object.keys(SEC)) {
  const o = SEC[k];
  const a = S(tBar[o.start]), b = S(tBar[o.start] + o.count * barDur(o.start));
  let s = 0, c = 0;
  for (let i = a; i < b; i++) { s += outL[i] * outL[i] + outR[i] * outR[i]; c += 2; }
  const r = Math.sqrt(s / Math.max(1, c));
  const g = r > 1e-7 ? TGT[k] / r : 1;
  for (let i = a; i < b && i < N; i++) gGain[i] = g;
  gainReport.push(`${k}:${g.toFixed(2)}`);
}
console.log('段增益', gainReport.join(' '));
for (let i = 0; i < N; i++) { outL[i] *= gGain[i]; outR[i] *= gGain[i]; }

const fadeIn = S(0.35), fadeOut = S(5.2);
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
// 鼠潮: 拨弦的颗粒在 2~6kHz, 玩具钢琴在 4~10kHz, 沙锤在 6~12kHz。
// 低频只留一点鼠爪踩地的闷响, 所以要压 60~250Hz 并把颗粒频段抬起来。
// 44Hz 高通是为了切掉 tremDrone 过去遗留在 subsonic 区的能量(那部分只剩糊和峰值)。
// 高频只给 +1/+0.8dB 与 +0.8dB 架: 沙锤与吱吱本身已经很亮, 再抬就会刺(实测 40s/70s 的
// 5~11kHz 已占 14~16%, 谱心 6.3~7.0kHz —— 对"颗粒型"曲子属于上限)。
const EQ_CHAIN = [hpCoeff(44, 0.707), peakCoeff(70, 0.8, -3.5), peakCoeff(190, 1.0, -3.0), peakCoeff(700, 1.0, -0.8), peakCoeff(3000, 0.9, 1.0), peakCoeff(6200, 0.9, 0.8), shelfCoeff(9500, 0.8)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

const TARGET_RMS = 0.102;   // 与 saya(0.100)/puppet(0.105) 对齐
let peak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > peak) peak = a; const b = Math.abs(eqR[i]); if (b > peak) peak = b; }
// 与《茶の唄》同样的保守归一化: 先缩到"归一化后的峰值 = capPeak", 让 tanh 只当兜底安全阀。
// capPeak 的下限 0.62: 若峰均比太高(本曲 19dB), 绝不能为了换 RMS 就把整曲压到 0.62 以下 ——
// 那等于把整首按 100 倍衰减再用 trim 抬回来, 峰值反而爆掉(这是本项目踩过的坑)。
// capPeak 的上限 0.94: 若归一化后本来就低于 0.94, 说明峰均比不高, 那就照常取 0.75。
const capPeak = Math.max(0.62, Math.min(0.94, 0.75));
const norm = peak > 0 ? capPeak / peak : 1;
let sat = 0;
for (let i = 0; i < N; i++) {
  let l = eqL[i] * norm, r = eqR[i] * norm;
  if (Math.abs(l) > 0.7) { l = Math.tanh(l); sat++; }
  if (Math.abs(r) > 0.7) { r = Math.tanh(r); sat++; }
  eqL[i] = l; eqR[i] = r;
}
let fp = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > fp) fp = a; const b = Math.abs(eqR[i]); if (b > fp) fp = b; }
const fg = fp > 0 ? Math.min(1.0, 0.94 / fp) : 1;
for (let i = 0; i < N; i++) { eqL[i] *= fg; eqR[i] *= fg; }
let _s0 = 0; for (let i = 0; i < N; i++) _s0 += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const rms0 = Math.sqrt(_s0 / (2 * N));
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }

/* 前瞻限幅(peak tamer): 本曲是"干颗粒 + 密集瞬态"型, 逐段配平后各段短时 RMS 只差
 * 约 11dB, 但单发的拨弦/军鼓/玩具钢琴仍会把瞬时峰值顶到 0.94 之外。
 * 用 3ms 前瞻 + 70ms 释放, 只在取样峰值超过 0.85 时把增益压下去(最多 10dB),
 * 释放用凸曲线以避开抽吸感。这样整曲取样峰值被钉在 0.85 以下 —— 编码成 ogg 也不会削波。 */
function peakLimit(L, R) {
  const TH = 0.88, MAXG = 0.316;                 // 最大衰减 ≈ -10dB
  const la = Math.round(0.003 * SR), rel = Math.max(2, Math.round(0.070 * SR));
  const g = new Float32Array(N); g.fill(1);
  let env = 1;
  for (let i = 0; i < N; i++) {
    const gNeed = Math.max(Math.abs(L[i]), Math.abs(R[i]), 1e-9) / TH;
    if (gNeed > env) env = gNeed;
    if (gNeed < env) env += (Math.max(gNeed, MAXG) - env) / rel;
    if (env < MAXG) env = MAXG;
    g[i] = env > 1 ? 1 / env : 1;
  }
  const oL = new Float32Array(N), oR = new Float32Array(N);
  let h = 1;
  for (let i = 0; i < N; i++) {
    const future = g[Math.min(N - 1, i + la)];
    if (future < h) h = future;
    else h += (future - h) / (rel * 1.6);
    oL[i] = L[i] * h; oR[i] = R[i] * h;
  }
  return [oL, oR];
}
const lim = peakLimit(eqL, eqR);
eqL = lim[0]; eqR = lim[1];
let finalPeak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > finalPeak) finalPeak = a; const b = Math.abs(eqR[i]); if (b > finalPeak) finalPeak = b; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));
console.log('母带: 段增益=' + gainReport.join(' ') + ' capPeak=' + capPeak.toFixed(3) + ' trim=' + trim.toFixed(4) + ' finalPeak=' + finalPeak.toFixed(4));

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'rat.wav');
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
console.log(JSON.stringify({ file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, sections: Object.keys(SEC).map(k => `${k}:${SEC[k].start}+${SEC[k].count}`).join(' '), peak, satSamples: sat, wetGain: +wetGain.toFixed(3), finalPeak: +finalPeak.toFixed(3), finalRms: +finalRms.toFixed(4), profile: rows }, null, 0));
