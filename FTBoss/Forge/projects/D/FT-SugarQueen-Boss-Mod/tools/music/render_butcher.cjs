/*
 * 屠夫 / 「肉山」 Boss 战斗BGM —— v6 引擎, 完全原创
 *
 * 体裁语汇: 工业恐怖 · 屠宰场 · 沉重压迫  (与《茶の唄》《木偶剧场》同一引擎, 音乐内容全部另写)
 *   - 调性: d 小调 (半音下行 + 减五度/小二度不谐和音簇)
 *   - 拍号: 4/4 为主, 第 21 小节插入 1 小节 3/4 造成"跛行"
 *   - 速度: 100 → 112 → 118 → (3/4 处 94) → 112 → 140 → 158 → 90
 *   - 结构: 逼近 → 挥砍 → 肉山怒吼 → 喘息 → 终局狂奔 → 收尾一击 (6 段, 明显动态对比)
 *
 * 禁止使用音乐盒(calliope/musicBox)语汇 —— 那是沙耶主题曲的音色。
 *
 * 用法: node render_butcher.cjs butcher.wav out
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const TAIL = 8.0;

/* ================= 时间轴: 逐小节定义拍号与速度 =================
 * beats = 该小节占几个「四分音符」; bpm = 四分音符每分钟
 * 4/4 → beats 4 ; 3/4 → beats 3 ; 7/8 → beats 3.5
 */
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
/* --- 曲式 --- */
sec('A', 6, 4, 100);    // 0-5    逼近: drone + 心跳式低音 + 远处磨刀石
sec('B', 14, 4, 112);   // 6-19   挥砍动机: 切分重音(铁器+砧板)
sec('C', 12, 4, 118);   // 20-31  肉山怒吼: 全奏, 半音下行 (含 21 号 3/4 跛行)
sec('D', 6, 4, 112);    // 32-37  喘息: 只剩滴血点状音与远处摩擦 (骤弱)
sec('E', 6, 2, 140);    // 38-43  终局狂奔(提速, 7/8 式密集重击)
sec('F', 28, 4, 158);   // 44-71  终局狂奔 · 三重冲击 (最快)
sec('G', 3, 4, 90);     // 72-74  收尾一击 + 尾混响

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

const rnd = (() => { let a = 20260913; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);

/* mus = 送混响总线(持续型/色彩) ; dry = 干总线(打击/低频瞬态), 混响只喂一点点 */
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
const T_SAW = makeTable(sawH(40, 26));
const T_STR_DK = makeTable(sawH(28, 8));
/* 低音铜管: 奇次为主, 8~12 次有个"吹奏芯"(共振峰), 大号/长号式的粗木管感 */
const T_BRASS = makeTable([1, 0.42, 0.66, 0.22, 0.38, 0.10, 0.30, 0.07, 0.26, 0.05, 0.22, 0.12, 0.08]);
/* 肉山低吼的声源: 大量次谐波 + 粗糙奇次 */
const T_MON = makeTable([1, 0.72, 0.55, 0.50, 0.34, 0.30, 0.22, 0.20, 0.14, 0.13, 0.10, 0.09, 0.06]);

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

/* ================= 屠宰场打击 ================= */
/** 剁骨刀: 高频噪声瞬态 + 金属刃口共鸣 + 砧板闷体 */
function cleaver(bl, br, start, amp, pan, opt) {
  opt = opt || {};
  const ph0 = opt.pitch ? midiHz(opt.pitch) : 430;
  const len = Math.floor(0.55 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ratios = [1, 2.41, 3.77, 5.63, 7.91, 10.4];
  const dec = [0.30, 0.16, 0.10, 0.06, 0.04, 0.025];
  const ph = ratios.map(() => 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      ph[k] += 2 * Math.PI * ph0 * ratios[k] / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / dec[k]) / (1 + k * 0.9);
    }
    const w = rnd() * 2 - 1;
    hp = 0.55 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;      // 高频"刃"噪声
    s += hp * Math.exp(-t / 0.0022) * 0.65;
    let lp = 0; lp += 0.22 * (w - lp);                              // 砧板闷体
    s += lp * Math.exp(-t / 0.035) * 0.30;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 铁锤砸砧: 更长更亮的金属非谐分音 + 低沉共鸣体 */
function anvil(bl, br, start, amp, pan, opt) {
  opt = opt || {};
  const base = opt.base ? midiHz(opt.base) : 265;
  const dur = opt.dur || 1.5;
  const len = Math.floor((dur + 0.4) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ratios = [0.5, 1, 1.61, 2.09, 2.53, 3.17, 4.02, 5.11, 6.43, 8.07];
  const dec = [1.5, 1.0, 0.72, 0.5, 0.34, 0.22, 0.14, 0.09, 0.06, 0.04];
  const ph = ratios.map(() => 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      ph[k] += 2 * Math.PI * base * ratios[k] / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / (dur * dec[k] * 0.42)) / (1 + k * 0.55);
    }
    const w = rnd() * 2 - 1;
    hp = 0.82 * (hpPrev + w - wPrev); hpPrev = hp; wPrev = w;
    s += hp * Math.exp(-t / 0.0018) * 0.85;                          // 铁器瞬态
    let lp = 0; lp += 0.05 * (w - lp);
    s += lp * Math.exp(-t / 0.02) * 0.35;                            // 砸击低频体
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 砧板式重击(肉山跺地): 正弦下坠 + 低频噪声 + 皮/肉闷响 */
function kick(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note || 26);
  const len = Math.floor(0.75 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp1 = 0, lp2 = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * (1 + 1.9 * Math.exp(-t / 0.028));
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.30) * (1 - Math.exp(-t / 0.0018));
    let s = Math.tanh(Math.sin(ph) * 1.25) * e;
    const w = rnd() * 2 - 1;
    lp1 += 0.055 * (w - lp1);
    s += lp1 * Math.exp(-t / 0.05) * 0.55;
    lp2 += 0.42 * (w - lp2);
    s += lp2 * Math.exp(-t / 0.0035) * 0.40;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 肉块落地 / 钝击: 只有"扑"的一声, 无金属 */
function meatHit(bl, br, start, amp, pan) {
  const len = Math.floor(0.45 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let lp = 0, ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    lp += 0.055 * (w - lp);
    const e = Math.exp(-t / 0.085) * (1 - Math.exp(-t / 0.002));
    ph += 2 * Math.PI * (58 + 40 * Math.exp(-t / 0.02)) / SR;
    const s = (lp * 1.5 + Math.sin(ph) * 0.5) * e;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 链条抖动: 非谐金属分音群 + 随机敲击簇 */
function chain(bl, br, start, amp, durSec, pan) {
  const dur = durSec || 0.9;
  const len = Math.floor((dur + 0.3) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ratios = [1, 1.37, 1.83, 2.41, 3.16, 4.05, 5.2, 6.7, 8.4];
  const ph = ratios.map(() => 0);
  const buf = new Float32Array(len);
  for (let k = 0; k < ratios.length; k++) {
    const t0 = Math.floor(Math.pow(rnd(), 1.6) * dur * SR * 0.7);
    const dec2 = 0.02 + rnd() * 0.09;
    for (let i = t0; i < len; i++) {
      const t = (i - t0) / SR;
      ph[k] += 2 * Math.PI * 620 * ratios[k] / SR;
      buf[i] += Math.sin(ph[k]) * Math.exp(-t / dec2) / (1 + k * 0.5);
    }
  }
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const env = Math.min(1, t / 0.02) * Math.max(0, 1 - t / (dur + 0.25));
    const w = rnd() * 2 - 1;
    const s = (buf[i] * 0.5 + w * Math.exp(-((t % 0.13)) / 0.006) * 0.25) * env;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 磨刀石 / 砂轮: 高频带通噪声 + 转速起伏 + 磨擦颗粒 */
function grind(bl, br, start, durSec, amp, pan) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const bp = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const c = bp(3400, 3.6);
  let z1 = 0, z2 = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const rpm = 1 + 0.28 * Math.sin(2 * Math.PI * 0.27 * t) + 0.12 * Math.sin(2 * Math.PI * 0.61 * t + 1.2);
    const w = rnd() * 2 - 1;
    const o = c.b0 * w + z1; z1 = c.b2 * w - c.a1 * o - c.a2 * z2; z2 = o;
    lp += 0.06 * (w - lp);
    const env = Math.min(1, t / 1.2) * Math.min(1, (durSec - t) / 1.5);
    const s = (o * 2.4 + lp * 0.5) * rpm * env;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 滴血: 点状高音, 带下滑的"嘀—嗒" */
function drip(bl, br, start, note, amp, pan) {
  const len = Math.floor(0.30 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note || 93);
  let ph = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * Math.pow(2, (-1.6 * t / 0.12) / 12);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.05) * (1 - Math.exp(-t / 0.0012));
    const s = (Math.sin(ph) + 0.28 * Math.sin(2 * ph)) * e;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 铜锣/铁板大击: 用于乐句终点 */
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
/** 低音定音鼓(心跳式) —— 只作低频脉动, 不承担旋律 */
function timp(bl, br, start, amp, note, pan) {
  const f0 = midiHz(note);
  const len = Math.floor(1.1 * SR), startI = S(start);
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

/* ================= 低音铜管 / 拨弦 / 低吼 ================= */
/** 低音铜管(大号/长号式): 锯齿过 320/750Hz 两个共振峰 + 低通, 起音有滑入 */
function brassLow(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const vibHz = opt.vibHz || 3.1, vibC = opt.vibCents || 8;
  const [pl, pr] = panGains(pan);
  const atk = opt.atk !== undefined ? opt.atk : 0.09;
  const rel = opt.rel !== undefined ? opt.rel : 0.55;
  const len = Math.ceil((atk + durSec + rel + 0.2) * SR), startI = S(start);
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const F = [320, 742, 1180];
  const gain = [1.0, 0.42, 0.16];
  const cs = F.map((fc, k) => mk(fc * (opt.form || 1), [4, 5, 6][k]));
  const st = F.map(() => ({ z1: 0, z2: 0 }));
  const [gl, gr] = opt.detune ? [Math.pow(2, -opt.detune / 1200), Math.pow(2, opt.detune / 1200)] : [1, 1.006];
  let phL = 0, phR = 0;
  const base = midiHz(note);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, atk + durSec, atk, 0.08, 1, rel);
    if (a <= 0) continue;
    const x = Math.min(1, t / (durSec + rel));
    const bend = (opt.glide || 0) * (1 - Math.min(1, t / 0.16));
    const vib = vibC * Math.sin(2 * Math.PI * vibHz * t) * Math.min(1, t / 0.3);
    const f = base * Math.pow(2, (bend + vib) / 12);
    const inc = f / SR * SIZE;
    const i0 = phL | 0, fr = phL - i0;
    const s0 = T_BRASS[i0], s1 = T_BRASS[(i0 + 1) & (SIZE - 1)];
    const src = s0 + (s1 - s0) * fr;
    phL += inc * gl; if (phL >= SIZE) phL -= SIZE;
    phR += inc * gr; if (phR >= SIZE) phR -= SIZE;
    let o = 0;
    for (let k = 0; k < 3; k++) {
      const y = cs[k].b0 * src + st[k].z1;
      st[k].z1 = cs[k].b2 * src - cs[k].a1 * y - cs[k].a2 * st[k].z2; st[k].z2 = y;
      o += y * gain[k];
    }
    lp += 0.30 * (o * 2.6 - lp);
    const j = startI + i;
    put(bl, j, lp * a * amp * pl); put(br, j, lp * a * amp * pr);
  }
}
/** 低音提琴拨弦: 短促、有指板噪声、快速衰减 */
function pizz(bl, br, start, note, amp, pan) {
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note);
  const dur = 0.55;
  const len = Math.floor((dur + 0.2) * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * (1 + 0.06 * Math.exp(-t / 0.012));
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.16) * (1 - Math.exp(-t / 0.0025));
    let s = Math.sin(ph) * 0.86 + Math.sin(2 * ph) * 0.34 + Math.sin(3.01 * ph) * 0.14;
    const w = rnd() * 2 - 1; lp += 0.5 * (w - lp);
    s += lp * Math.exp(-t / 0.004) * 0.28;
    put(bl, startI + i, s * amp * e * pl); put(br, startI + i, s * amp * e * pr);
  }
}
/** 低八度共振峰"男声低吼": 女声 voice() 的思路降到低八度 + 粗糙度(次谐波/抖动/AM) */
const VOWEL = { a: [800, 1150, 2900], o: [500, 1000, 2400], u: [340, 700, 2300], e: [520, 1800, 2500], i: [300, 2200, 3000] };
/* 咆哮用的共振峰: 整体下移(喉腔变大), 并收窄 F2 */
const GROWL_F = { a: [420, 830, 2200], o: [300, 640, 1900], u: [240, 520, 1700], e: [360, 1250, 2100], i: [220, 1500, 2300] };
function growl(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const v = GROWL_F[opt.vowel || 'o'];
  const vibHz = opt.vibHz || 4.0, vibC = opt.vibCents || 15;
  const rough = opt.rough !== undefined ? opt.rough : 0.42;      // 粗糙度
  const [pl, pr] = panGains(pan);
  const atk = opt.atk !== undefined ? opt.atk : 0.18;
  const rel = opt.rel !== undefined ? opt.rel : 0.5;
  const glide = opt.glide || 0;
  const len = Math.ceil((atk + durSec + rel + 0.25) * SR), startI = S(start);
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const gain = [1.0, 0.55, 0.26];
  const cL = v.map((fc, k) => mk(fc, [7, 9, 11][k]));
  const cR = v.map((fc, k) => mk(fc * 1.011, [7, 9, 11][k]));
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  const base = midiHz(note);
  let phA = rnd(), phB = rnd();
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, durSec, atk, 0.25, 1, rel);
    if (a <= 0) continue;
    const x = Math.min(1, t / (durSec + rel));
    const bend = glide * x / 12;
    const vib = vibC * Math.sin(2 * Math.PI * vibHz * t) * Math.min(1, t / 0.35);
    const f = base * Math.pow(2, (bend + vib) / 12);
    const inc = f / SR * SIZE;
    const i0 = phA | 0, fr = phA - i0;
    const sA0 = T_MON[i0], sA1 = T_MON[(i0 + 1) & (SIZE - 1)];
    const srcA = sA0 + (sA1 - sA0) * fr;
    const j0 = phB | 0, jf = phB - j0;
    const sB0 = T_SAW[j0], sB1 = T_SAW[(j0 + 1) & (SIZE - 1)];
    const srcB = sB0 + (sB1 - sB0) * jf;
    const src = (srcA * 0.85 + srcB * 0.5) * (1 + rough * 0.55 * Math.sin(2 * Math.PI * (27 + 9 * Math.sin(2 * Math.PI * 0.7 * t)) * t));
    phA += inc; if (phA >= SIZE) phA -= SIZE;
    phB += inc * 0.5; if (phB >= SIZE) phB -= SIZE;
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
    put(bl, j, oL * a * amp * pl * 4.6); put(br, j, oR * a * amp * pr * 4.6);
  }
}
/** 纯五度/八度 drone: 工业低频底座 */
function drone(start, durSec, note, amp, opt) {
  opt = opt || {};
  const len = Math.floor(durSec * SR), startI = S(start);
  const f = midiHz(note);
  const ph = [0, 0, 0, 0];
  const rat = [1, 1.5, 2, 2.996];
  const g = [1, 0.30, 0.16, 0.06];
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const env = Math.min(1, t / (opt.atk || 2.2)) * Math.min(1, (durSec - t) / 2.5);
    let s = 0;
    for (let k = 0; k < 4; k++) {
      ph[k] += 2 * Math.PI * f * rat[k] * (1 + (k === 3 ? 0.0016 : 0)) / SR;
      s += Math.sin(ph[k]) * g[k];
    }
    const w = rnd() * 2 - 1; lp += 0.03 * (w - lp);
    s += lp * 0.12 * (1 + 0.5 * Math.sin(2 * Math.PI * 0.21 * t));
    put(musL, startI + i, s * amp * env);
    put(musR, startI + i, s * amp * env * 0.93);
  }
}

/* ================= 和声 (d 小调, 半音下行 + 减五度/小二度音簇) ================= */
const CH = {
  Dm: { bass: 26, pad: [50, 53, 57], cls: [] },            // D1 / D3 F3 A3
  Bb: { bass: 34, pad: [50, 53, 58], cls: [] },            // Bb1 / D3 F3 Bb3
  Gm: { bass: 31, pad: [50, 55, 58], cls: [] },            // G1 / D3 G3 Bb3
  A7: { bass: 33, pad: [49, 52, 55, 58], cls: [] },        // A1 / C#3 E3 G3 Bb3
  Eb: { bass: 27, pad: [51, 55, 58], cls: [] },            // Eb1 / Eb3 G3 Bb3 (那不勒斯式压迫)
  Ab: { bass: 32, pad: [51, 55, 56], cls: [56, 57] },      // Ab1 / Eb3 G3 Ab3(小二度)
  DmT: { bass: 26, pad: [50, 53, 56], cls: [56, 57] },     // D1 / F3 Ab3(减五度) + Ab3/A3 音簇
  Ebm: { bass: 27, pad: [51, 54, 58], cls: [54, 55] },
  Fs: { bass: 30, pad: [54, 57, 61], cls: [] },            // F#1 / F#3 A3 C#4
  Cdim: { bass: 24, pad: [48, 51, 54], cls: [] }           // C1 / C3 Eb3 F#3
};
/* 挥砍段循环(8 小节): 半音下行 D → C# → C 的影子 + 减五度 */
const LOOP_B = ['Dm', 'Dm', 'Bb', 'Bb', 'Gm', 'Gm', 'A7', 'DmT'];
/* 怒吼/终局循环(8 小节): 更暗, 减五度与那不勒斯 */
const LOOP_C = ['Dm', 'Ebm', 'Dm', 'Ab', 'Dm', 'Bb', 'Eb', 'DmT'];
const chordOf = (bar, loop) => CH[(loop || LOOP_B)[((bar % 8) + 8) % 8]];

/* ================= 主题 =================
 * 挥砍动机: 切分重音 —— 重音永远落在弱拍/后半拍(剁下去的那一下)
 * 用 [拍, 半音偏移, 时长] 表示相对和弦根音的位移, 便于跟着和声走 */
const CHOPS = [
  [0, 12, 0.5], [1.5, 10, 0.5], [2.5, 12, 0.5], [3, 13, 1.0],
  [4, 10, 0.5], [5.5, 12, 0.5], [6.5, 8, 1.5]
];
/* 肉山怒吼(半音下行长音): [小节偏移, 拍, midi, 时长] —— 全奏主体 */
const ROAR = [
  [0, 0, 62, 2.5], [0, 2.5, 61, 0.5], [0, 3, 60, 1.0],
  [2, 0, 60, 2.0], [2, 2, 58, 2.0],
  [4, 0, 58, 2.5], [4, 2.5, 57, 0.5], [4, 3, 56, 1.0],
  [6, 0, 56, 3.0], [6, 3, 55, 1.0]
];
/* 终局狂奔动机: 密集八分下行 + 小二度夹击 */
const RUSH = [
  [0, 0, 62, 0.5], [0, 1, 60, 0.5], [0, 2, 58, 0.5], [0, 3, 57, 0.5],
  [1, 0, 56, 1.0], [1, 2, 57, 0.5], [1, 3, 56, 0.5],
  [2, 0, 62, 0.5], [2, 1, 63, 0.5], [2, 2, 62, 0.5], [2, 3, 60, 0.5],
  [3, 0, 58, 1.5], [3, 2, 57, 1.0]
];

/* ================= 装配辅助 ================= */
console.log('装配中... 总小节', BARS, '预计', TOTAL.toFixed(1), 's');

/** 弦乐/铜管低音铺底 */
function strPad(bar, amp, oct) {
  const p = chordOf(bar, LOOP_C);
  p.pad.forEach((m, i) => {
    tableVoice(musL, musR, -0.45 + i * 0.45, tBeat(bar, 0), T_STR_DK, m + (oct || 0), amp * (i === 0 ? 1 : 0.78),
      Math.min(0.7, barDur(bar) * 0.22), barDur(bar) * 0.9, 0.8, { detune: (i - 1) * 6, vibHz: 3.2, vibCents: 7 });
  });
  tableVoice(musL, musR, 0, tBeat(bar, 0), T_STR_DK, p.bass + 12, amp * 1.05, 0.4, barDur(bar) * 0.9, 0.8, { detune: 3 });
}
/** 不谐和音簇: 小二度/减五度堆叠, 缓慢推进 */
function cluster(start, durSec, lo, hi, amp, bus) {
  const bl = bus === 'dry' ? dryL : musL, br = bus === 'dry' ? dryR : musR;
  for (let m = lo; m <= hi; m++) {
    const pan = -0.6 + (m - lo) / Math.max(1, hi - lo) * 1.2;
    tableVoice(bl, br, pan, start, T_STR_DK, m, amp, durSec * 0.5, durSec * 0.4, 1.0,
      { detune: (m % 2 ? -13 : 13), vibHz: 2.3, vibCents: 6 });
  }
}
/** 挥砍动机(剁骨 + 铁砧 + 砧板) */
function chopRiff(bar, amp, opt) {
  opt = opt || {};
  const p = chordOf(bar, opt.loop);
  CHOPS.forEach(([q, off, d], k) => {
    const st = tBeat(bar, q);
    const heavy = (k % 3 === 0);
    anvil(dryL, dryR, st, amp * (heavy ? 0.30 : 0.19), (k % 2 ? 0.34 : -0.34),
      { base: p.bass + 12 + off, dur: heavy ? 1.4 : 0.9 });
    cleaver(dryL, dryR, st + 0.012, amp * (heavy ? 0.24 : 0.30), (k % 2 ? -0.5 : 0.5), { pitch: 300 + (k % 4) * 90 });
    if (heavy) meatHit(dryL, dryR, st, amp * 0.42, k % 4 ? 0.2 : -0.2);
    void d;
  });
  if (opt.extra) {
    cleaver(dryL, dryR, tBeat(bar, 1), amp * 0.16, 0.6, { pitch: 900 });
    cleaver(dryL, dryR, tBeat(bar, 3.5), amp * 0.14, -0.6, { pitch: 1200 });
  }
}
/** 终局狂奔: 每拍重击 + 八分铁器 */
function rushBar(bar, amp, opt) {
  opt = opt || {};
  const p = chordOf(bar, LOOP_C);
  for (let q = 0; q < 4; q++) {
    kick(dryL, dryR, tBeat(bar, q), amp * (q === 0 ? 1.0 : 0.72), p.bass, 0);
    if (q % 2 === 0) timp(dryL, dryR, tBeat(bar, q), amp * 0.55, p.bass + 12, q ? 0.25 : -0.25);
  }
  for (let k = 0; k < 8; k++) {
    const st = tBeat(bar, k * 0.5);
    anvil(dryL, dryR, st, amp * (k % 4 === 0 ? 0.26 : 0.15), (k % 2 ? 0.45 : -0.45), { base: p.bass + 12 + (k % 3) * 4, dur: 0.8 });
    cleaver(dryL, dryR, st + 0.008, amp * 0.19, (k % 2 ? -0.6 : 0.6), { pitch: 360 + (k % 5) * 110 });
  }
  if (opt.fill) {
    for (let k = 0; k < 6; k++) cleaver(dryL, dryR, tBeat(bar, 2 + k * 0.25), amp * 0.16, (k % 2 ? -0.7 : 0.7), { pitch: 700 + k * 120 });
    gong(dryL, dryR, tBeat(bar, 3.5), amp * 0.30, 96, 2.2);
  }
}

/* ---- I. 逼近 (A: 0-5, 100 BPM) 只有 drone / 远处磨刀石 / 稀疏铁器 ---- */
{
  const s = SEC.A.start;
  drone(tBeat(s, 0), barDur(s) * 6.5, 26, 0.075, { atk: 3.0 });          // D1 持续音
  grind(musL, musR, tBeat(s, 0), barDur(s) * 5.6, 0.055, -0.55);
  grind(musL, musR, tBeat(s + 2, 2), barDur(s) * 3.4, 0.038, 0.6);
  for (let i = 0; i < 6; i++) {
    const b = s + i;
    timp(dryL, dryR, tBeat(b, 0), 0.30 + i * 0.035, 26, -0.15);          // 缓慢心跳
    timp(dryL, dryR, tBeat(b, 2.5), 0.17 + i * 0.02, 26, 0.15);
    if (i >= 2) { meatHit(dryL, dryR, tBeat(b, 3), 0.22, i % 2 ? 0.3 : -0.3); }
    if (i >= 3) { chain(dryL, dryR, tBeat(b, 3.5), 0.13 + i * 0.02, 1.1, i % 2 ? 0.5 : -0.5); }
    if (i === 4) { anvil(dryL, dryR, tBeat(b, 0), 0.20, -0.3, { base: 38, dur: 2.4 }); }
    if (i === 5) {
      anvil(dryL, dryR, tBeat(b, 0), 0.26, 0.25, { base: 38, dur: 2.6 });
      growl(musL, musR, 0, tBeat(b, 2), 26, 0.085, barDur(b) * 1.1, { vowel: 'o', rough: 0.5, atk: 0.6 });
    }
  }
  [0.5, 1.5, 2.5, 4.5].forEach((q, k) => drip(musL, musR, tBeat(s + 3 + (k % 2), q), 93 - k * 2, 0.040, k % 2 ? 0.6 : -0.6));
}

/* ---- II. 挥砍动机 (B: 6-19, 112 BPM) 切分重音, 铁器与砧板 ---- */
{
  const s = SEC.B.start;
  drone(tBeat(s, 0), barDur(s) * 14.2, 26, 0.070, { atk: 1.4 });
  for (let i = 0; i < 14; i++) {
    const b = s + i;
    const p = chordOf(b);
    const grow = Math.min(1, 0.45 + i / 13 * 0.55);
    strPad(b, 0.030 * grow + 0.012);
    chopRiff(b, (0.86 + i * 0.030), { extra: i >= 8 });
    /* 低音提琴拨弦: 每小节两下, 后半拍 */
    pizz(dryL, dryR, tBeat(b, 0), p.bass + 12, 0.125 * grow);
    pizz(dryL, dryR, tBeat(b, 2.5), p.bass + 12, 0.095 * grow);
    /* 低音铜管长音 */
    if (i % 2 === 0) brassLow(musL, musR, -0.15, tBeat(b, 0), p.bass + 12, 0.052 * grow, barDur(b) * 1.85, { detune: 7 });
    if (i % 4 === 3) chain(dryL, dryR, tBeat(b, 3), 0.10, 0.8, i % 8 ? -0.5 : 0.5);
    if (i === 6) gong(musL, musR, tBeat(b, 0), 0.075, 92, 2.6);
    if (i >= 9) { cleaver(dryL, dryR, tBeat(b, 1.5), 0.13, -0.55, { pitch: 780 }); cleaver(dryL, dryR, tBeat(b, 3.25), 0.12, 0.55, { pitch: 1040 }); }
    if (i === 12) cluster(tBeat(b, 0), barDur(b) * 2.4, 49, 56, 0.020);
  }
  /* 挥砍动机最后一次由低吼喊出, 接入怒吼段 */
  growl(musL, musR, 0, tBeat(s + 12, 0), 38, 0.075, barDur(s) * 2.2, { vowel: 'a', rough: 0.45, glide: -0.6, vibCents: 22 });
  growl(musL, musR, 0.12, tBeat(s + 13, 2), 37, 0.070, barDur(s) * 1.6, { vowel: 'o', rough: 0.55, glide: -1.2 });
}

/* ---- III. 肉山怒吼 (C: 20-31, 118 BPM, 第 21 小节 3/4 跛行) 全奏 ---- */
{
  const s = SEC.C.start;
  drone(tBeat(s, 0), barDur(s) * 12.2, 26, 0.078, { atk: 1.0 });
  for (let i = 0; i < 12; i++) {
    const b = s + i;
    const p = chordOf(b, LOOP_C);
    strPad(b, 0.042);
    chopRiff(b, 0.96, { loop: LOOP_C, extra: true });
    kick(dryL, dryR, tBeat(b, 0), 0.78, p.bass, 0);
    kick(dryL, dryR, tBeat(b, 2), 0.58, p.bass, 0);
    timp(dryL, dryR, tBeat(b, 3.5), 0.30, p.bass + 12, i % 2 ? 0.3 : -0.3);
    pizz(dryL, dryR, tBeat(b, 0), p.bass + 12, 0.10);
    pizz(dryL, dryR, tBeat(b, 1.5), p.bass + 19, 0.075);
    pizz(dryL, dryR, tBeat(b, 2.75), p.bass + 12, 0.070);
    brassLow(musL, musR, -0.2, tBeat(b, 0), p.bass + 12, 0.062, barDur(b) * 0.95, { detune: 9, form: 0.94 });
    brassLow(musL, musR, 0.2, tBeat(b, 3.0), p.bass + 19, 0.040, barDur(b) * 0.45, { detune: 5, atk: 0.14 });
    /* 不谐和音簇(减五度/小二度)随乐句推进 */
    if (i === 4) cluster(tBeat(b, 0), barDur(b) * 2.6, 50, 56, 0.024);
    if (i === 8) cluster(tBeat(b, 0), barDur(b) * 3.2, 49, 57, 0.030);
    if (i === 11) cluster(tBeat(b, 0), barDur(b) * 1.2, 50, 58, 0.034, 'dry');
    if (i === 2 || i === 6 || i === 10) gong(dryL, dryR, tBeat(b, 0), 0.16, 88 - i, 3.0);
    if (i % 4 === 3) cymbal(dryL, dryR, tBeat(b, 3.5), 0.14, 2.0, i % 8 ? -0.4 : 0.4);
    if (i === 1) chain(dryL, dryR, tBeat(b, 0), 0.16, 1.4, 0.5);
    if (i === 10) { chain(dryL, dryR, tBeat(b, 3), 0.15, 1.2, -0.5); anvil(dryL, dryR, tBeat(b, 2), 0.30, 0.4, { base: 40, dur: 3.0 }); }
  }
  /* 半音下行的怒吼(主题) + 全奏齐奏 */
  ROAR.forEach(([bo, q, m, d]) => {
    const b = s + bo;
    growl(musL, musR, -0.05, tBeat(b, q), m - 12, 0.098, d * 60 / bpmAt(b) * 0.94,
      { vowel: (bo % 4 === 0 ? 'a' : 'o'), rough: 0.5, atk: 0.12, rel: 0.4, vibHz: 4.4, vibCents: 24 });
    brassLow(musL, musR, 0.1, tBeat(b, q), m - 12, 0.075, d * 60 / bpmAt(b) * 0.9,
      { detune: 10, atk: 0.06, form: 0.92 });
    brassLow(musL, musR, -0.35, tBeat(b, q), m - 24, 0.058, d * 60 / bpmAt(b) * 0.9, { detune: 6, atk: 0.08 });
  });
  /* 段末: 半音下行到底 + 全停前的最后一击 */
  for (let k = 0; k < 4; k++) {
    const b = s + 11;
    anvil(dryL, dryR, tBeat(b, 2 + k * 0.5), 0.24 - k * 0.03, k % 2 ? 0.5 : -0.5, { base: 42 - k * 2, dur: 1.2 });
  }
  gong(dryL, dryR, tBeat(s + 11, 3.5), 0.30, 76, 3.4);
}

/* ---- IV. 喘息 (D: 32-37, 112 BPM) 骤弱: 只剩滴血点状音与远处摩擦 ---- */
{
  const s = SEC.D.start;
  drone(tBeat(s, 0), barDur(s) * 6.4, 26, 0.022, { atk: 1.6 });
  grind(musL, musR, tBeat(s + 1, 0), barDur(s) * 4.0, 0.024, 0.65);
  for (let i = 0; i < 6; i++) {
    const b = s + i;
    if (i % 2 === 0) timp(dryL, dryR, tBeat(b, 0), 0.10, 26, -0.1);
    if (i === 3) { chain(dryL, dryR, tBeat(b, 1.5), 0.075, 2.2, -0.6); }
    if (i === 5) { meatHit(dryL, dryR, tBeat(b, 0), 0.12, 0.4); }
  }
  /* 滴血: 不规则点状高音, 像血滴落进铁盆 */
  const drops = [[0, 1.5, 95], [0, 3.25, 92], [1, 0.75, 88], [1, 2.5, 95], [2, 1.25, 90], [2, 3.5, 97],
  [3, 0.5, 86], [3, 2.75, 93], [4, 1.5, 89], [4, 3.25, 96], [5, 0.75, 91], [5, 2.25, 87]];
  drops.forEach(([bo, q, m], k) => drip(musL, musR, tBeat(s + bo, q), m, 0.052 - k * 0.001, (k % 3 - 1) * 0.55));
  /* 远处的一声低吼, 预告爆发 */
  growl(musL, musR, 0.15, tBeat(s + 5, 0), 38, 0.060, barDur(s) * 1.4, { vowel: 'u', rough: 0.6, atk: 0.5, glide: -1.5 });
  cymbal(dryL, dryR, tBeat(s + 5, 2), 0.055, 3.0, 0);
}

/* ---- V. 终局狂奔 · 起步 (E: 38-43, 140 BPM) 提速, 密集重击 ---- */
{
  const s = SEC.E.start;
  drone(tBeat(s, 0), barDur(s) * 6.2, 26, 0.072, { atk: 0.7 });
  for (let i = 0; i < 6; i++) {
    const b = s + i;
    const p = chordOf(b, LOOP_C);
    strPad(b, 0.036);
    rushBar(b, 0.78, { fill: i === 5 });
    pizz(dryL, dryR, tBeat(b, 0), p.bass + 12, 0.10);
    brassLow(musL, musR, 0, tBeat(b, 0), p.bass + 12, 0.058, barDur(b) * 1.1, { detune: 12, atk: 0.05 });
    if (i % 2 === 1) chain(dryL, dryR, tBeat(b, 1), 0.10, 0.7, i % 4 ? -0.6 : 0.6);
    if (i >= 2) growl(musL, musR, 0, tBeat(b, 2), 26 + (i % 2), 0.070, barDur(b) * 0.55, { vowel: 'o', rough: 0.55, atk: 0.06, rel: 0.2 });
  }
}

/* ---- VI. 终局狂奔 · 三重冲击 (F: 44-71, 158 BPM) 最快 ---- */
{
  const s = SEC.F.start;
  drone(tBeat(s, 0), barDur(s) * 28.4, 26, 0.075, { atk: 0.6 });
  for (let i = 0; i < 28; i++) {
    const b = s + i;
    const p = chordOf(b, LOOP_C);
    strPad(b, 0.038);
    const ph = Math.floor(i / 8) % 3;              // 三个 8 小节冲击, 一层比一层狠
    rushBar(b, 0.80 + ph * 0.09, { fill: (i % 8 === 7) });
    pizz(dryL, dryR, tBeat(b, 0), p.bass + 12, 0.105);
    pizz(dryL, dryR, tBeat(b, 2.5), p.bass + 19, 0.075);
    brassLow(musL, musR, 0, tBeat(b, 0), p.bass + 12, 0.062 + ph * 0.006, barDur(b) * 1.05, { detune: 12, atk: 0.04 });
    if (ph >= 1) brassLow(musL, musR, 0.3, tBeat(b, 2), p.bass + 24, 0.034, barDur(b) * 0.45, { detune: 8, atk: 0.05 });
    if (i % 4 === 2) cymbal(dryL, dryR, tBeat(b, 3), 0.11, 1.8, i % 8 ? -0.45 : 0.45);
    if (i % 8 === 3) cluster(tBeat(b, 0), barDur(b) * 2.2, 50, 57, 0.022);
    if (i % 8 === 4) { chain(dryL, dryR, tBeat(b, 0), 0.11, 1.0, 0.55); gong(dryL, dryR, tBeat(b, 0), 0.13, 82, 2.4); }
    /* 怒吼动机在半音下行, 每 8 小节一轮 */
    if (ph >= 1) {
      ROAR.filter(r2 => r2[0] === (i % 8)).forEach(([, q, m, d]) => growl(musL, musR, -0.05, tBeat(b, q), m - 12, 0.088 + ph * 0.008,
        d * 60 / bpmAt(b) * 0.92, { vowel: 'a', rough: 0.58, atk: 0.06, rel: 0.25, vibHz: 5.0, vibCents: 26 }));
    }
  }
  /* 最后的半音下行冲刺 + 收束 */
  for (let k = 0; k < 8; k++) {
    const b = s + 26, st = tBeat(b, k * 0.5);
    anvil(dryL, dryR, st, 0.30 - k * 0.012, k % 2 ? 0.55 : -0.55, { base: 45 - k, dur: 1.1 });
    growl(musL, musR, 0, st, 50 - k, 0.070, 0.22, { vowel: 'a', rough: 0.6, atk: 0.02, rel: 0.12 });
  }
  growl(musL, musR, 0, tBeat(s + 27, 0), 26, 0.100, barDur(s) * 1.0, { vowel: 'o', rough: 0.5, atk: 0.05, rel: 0.3, glide: -1.0 });
  gong(dryL, dryR, tBeat(s + 27, 0), 0.34, 70, 3.6);
  cymbal(dryL, dryR, tBeat(s + 27, 2), 0.16, 2.6, 0);
}

/* ---- VII. 收尾一击 (G: 72-74, 90 BPM) + 尾混响 ---- */
{
  const s = SEC.G.start;
  const b = s;
  /* 全体一击 */
  kick(dryL, dryR, tBeat(b, 0), 1.0, 26, 0);
  anvil(dryL, dryR, tBeat(b, 0), 0.42, 0, { base: 38, dur: 3.4 });
  cleaver(dryL, dryR, tBeat(b, 0), 0.30, -0.4, { pitch: 220 });
  cleaver(dryL, dryR, tBeat(b, 0.02), 0.22, 0.45, { pitch: 1500 });
  meatHit(dryL, dryR, tBeat(b, 0), 0.55, 0);
  gong(dryL, dryR, tBeat(b, 0), 0.34, 62, 4.2);
  cymbal(dryL, dryR, tBeat(b, 0), 0.20, 3.4, 0);
  chain(dryL, dryR, tBeat(b, 0.25), 0.16, 1.8, 0.5);
  brassLow(musL, musR, 0, tBeat(b, 0), 38, 0.085, barDur(b) * 1.2, { detune: 8, atk: 0.03, glide: -2 });
  brassLow(musL, musR, -0.4, tBeat(b, 0), 26, 0.075, barDur(b) * 1.4, { detune: 4, atk: 0.03, glide: -1.5 });
  growl(musL, musR, 0, tBeat(b, 0), 26, 0.105, barDur(b) * 1.1, { vowel: 'o', rough: 0.55, atk: 0.05, rel: 0.8, glide: -2.2, vibHz: 3.2 });
  cluster(tBeat(b, 0), barDur(b) * 1.4, 50, 57, 0.030);
  drone(tBeat(b, 0), barDur(b) * 4.2, 26, 0.060, { atk: 0.05 });
  /* 尾部只剩环境: 血滴 / 链条余响 */
  drip(musL, musR, tBeat(s + 1, 1.5), 93, 0.045, -0.5);
  drip(musL, musR, tBeat(s + 1, 3.25), 86, 0.038, 0.6);
  drip(musL, musR, tBeat(s + 2, 2.5), 90, 0.030, 0.2);
  chain(dryL, dryR, tBeat(s + 2, 0), 0.06, 2.4, -0.6);
  grind(musL, musR, tBeat(s + 3, 0), barDur(s) * 3.5, 0.018, 0.7);
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
  sendL[i] = musL[i - PRE] * 0.60 + dryL[i - PRE] * 0.10;
  sendR[i] = musR[i - PRE] * 0.60 + dryR[i - PRE] * 0.10;
}
/* 屠宰场是"大而潮湿的水泥/铁皮空间": 短一些、更暗、更浑浊 */
const revL = reverbChannel(sendL, 0, 0.86, 0.22, 0.05);
const revR = reverbChannel(sendR, 29, 0.86, 0.22, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const dryRms = Math.sqrt((rmsOf(musL) ** 2 + rmsOf(dryL) ** 2) / 2);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.0, dryRms * 0.40 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) { outL[i] = musL[i] + dryL[i] + revL[i] * wetGain; outR[i] = musR[i] + dryR[i] + revR[i] * wetGain; }
const fadeIn = S(1.2), fadeOut = S(6.5);
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
/* 工业恐怖: 低频留 40-60 的压迫, 250-400 让给"肉"的钝响, 3k/6k 给铁器刃口 */
const EQ_CHAIN = [hpCoeff(30, 0.707), peakCoeff(200, 1.0, -2.2), peakCoeff(420, 1.0, -1.6), peakCoeff(900, 1.1, -1.0), peakCoeff(2900, 0.9, 1.6), peakCoeff(5600, 0.9, 1.5), shelfCoeff(9000, 1.5)];

/* ---- 瞬态安全阀: 把打击峰压到阈值(线性软拐点), 保证后续 0.62/peak 归一化后不被 tanh 啃 ---- */
const LIM_T = 0.70;                    // 软拐点: 该点以下完全线性(零失真)
const LIM_CEIL = 0.88;                 // 硬上限
let limMax = 0;
for (let i = 0; i < N; i++) {
  const a = Math.abs(outL[i]); if (a > limMax) limMax = a;
  const b = Math.abs(outR[i]); if (b > limMax) limMax = b;
}
function limiterRun(a) {
  const out = new Float32Array(a.length);
  const cAtk = 1 - Math.exp(-1 / (SR * 0.0006));   // 0.6 ms 抓峰
  const cRel = 1 - Math.exp(-1 / (SR * 0.090));    // 90 ms 释放
  let env = 0, g = 1;
  for (let i = 0; i < a.length; i++) {
    const x = a[i], ax = Math.abs(x);
    env = ax > env ? ax : cRel * env + (1 - cRel) * ax;
    const want = env <= LIM_T ? 1 : LIM_T / env;
    g = want < g ? g + cAtk * (want - g) : g + cRel * (want - g);
    if (g > 1) g = 1; else if (g < 0.05) g = 0.05;
    let y = x * g;
    if (y > LIM_CEIL) y = LIM_CEIL; else if (y < -LIM_CEIL) y = -LIM_CEIL;
    out[i] = y;
  }
  return out;
}
const preLimPeak = limMax;
let eqL = limiterRun(outL), eqR = limiterRun(outR);
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

let peak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > peak) peak = a; const b = Math.abs(eqR[i]); if (b > peak) peak = b; }
// 本曲是打击驱动的工业曲: 峰值天然很高, 若沿用 1.5/peak 会把大量重击推进 tanh 变成"糊"。
// 已先用线性软拐点限幅器把瞬态压到 0.70/0.88 (其下零失真), 再用 0.62/peak 归一化:
// 于是 0.7 的饱和阈值恰好只作兜底, satSamples 保持 ≈0, 动态交给最后的 RMS 归一化。
const norm = peak > 0 ? 0.64 / peak : 1;
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
const TARGET_RMS = 0.102;   // 与《茶の唄》0.0999 / 《木偶剧场》0.104 对齐
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
let _s = 0; for (let i = 0; i < N; i++) _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
const finalRms = Math.sqrt(_s / (2 * N));
let finalPeak = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > finalPeak) finalPeak = a; const b = Math.abs(eqR[i]); if (b > finalPeak) finalPeak = b; }

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'butcher.wav');
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
console.log(JSON.stringify({
  file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS,
  sections: Object.keys(SEC).map(k => `${k}:${SEC[k].start}+${SEC[k].count}`).join(' '),
  peak, finalPeak: +finalPeak.toFixed(3), satSamples: sat, wetGain: +wetGain.toFixed(3), preLimPeak: +preLimPeak.toFixed(3),
  finalRms: +finalRms.toFixed(4), profile: rows
}, null, 0));
