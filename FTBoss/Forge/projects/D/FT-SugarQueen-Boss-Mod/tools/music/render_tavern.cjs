/*
 * 酒桶王 (Wine Baron) 专属战斗BGM —— 「酒馆 · 醉舞曲」版 (v1, 完全原创)
 *
 * 曲风取向: 热闹的酒馆舞曲 —— 6/8 吉格(jig) + oom-pah 律动, G 混合利底亚(♭VII=F 的民间感),
 *           中段转同主音小调 g + ♭VI(E♭) 并"喝醉"(速度微飘 / 音高游移 / 走音 / 低音不谐和).
 *
 * 与既有两首专属曲的差别:
 *   节拍   : 6/8 吉格        (saya=6/8 摇篮 / puppet=2/4 加洛普 + 7/8)
 *   调性   : G 混合利底亚 → g 小调  (saya=b 小调 / puppet=e 弗里吉亚)
 *   律动   : oom-pah-pah 低音提琴 walking + 手风琴和弦  (saya=心跳 / puppet=加洛普)
 *   音色   : 手风琴(双簧失谐) · 口琴/口哨 · 曼陀林扫弦 · 低音提琴 pizz · 手鼓/铃鼓/木桶 · 碰杯 · 人群嘈杂
 *   戏剧点 : "醉意间奏" —— 只剩拨弦与哼唱, 速度从 112 漂到 88, 三全音低音潜行, 再被一记木桶敲醒
 *
 * 旋律/和声全部为本项目原创, 不引用任何现成曲目.
 * 框架(合成/DSP/混响/母带/写 WAV)沿用 render_saya.cjs v6.
 *
 * 实测 (node render_tavern.cjs tavern.wav out):
 *   86 小节 / 123.92 秒 / finalRms 0.1000 / 峰值 0.594 / 末级峰值 0.900 / 饱和样本 9 / 峰均比 18.9 dB
 *   A 0.0-12.0s ♩=120 开门迎客 | B 12.0-33.8s ♩=132 第一轮舞曲 | C 33.8-54.7s ♩=138 合唱式副歌
 *   D 54.7-72.8s ♩=112→88 醉意间奏 | E 72.8-105.9s ♩=148→156 更快的终段 | F 105.9-115.9s ♩=144 收尾
 *   末后 8.0s 为尾混响. 醉意段 RMS 比终段低约 9 dB (强弱对比).
 */
const fs = require('fs');
const path = require('path');

const SR = 44100;
const TAIL = 8.0;

/* ================= 时间轴 =================
 * beats = 该小节占几个「四分音符」(6/8 → 3)
 * bpm   = 四分音符每分钟 → 吉格的八分音符脉冲 = 2*bpm 每分钟
 * ramp  = [起始bpm, 结束bpm] 线性 (用于醉意与终段加速)
 * jit   = 每小节速度微飘的比例 (醉意)
 */
const METER = [];
const SEC = {};
let _b = 0;
function sec(name, count, beats, bpm, ramp, jit) {
  const o = { start: _b, count, beats };
  for (let i = 0; i < count; i++) {
    let v = ramp ? ramp[0] + (ramp[1] - ramp[0]) * (count === 1 ? 0 : i / (count - 1)) : bpm;
    if (jit) v *= 1 + jit * (0.55 * Math.sin(i * 1.7 + 1.1) + 0.30 * Math.sin(i * 2.9 + 0.4) + 0.15 * Math.sin(i * 4.3 + 2.2));
    METER.push({ beats, bpm: v });
  }
  _b += count;
  SEC[name] = o;
  return o;
}
/* --- 曲式 (共 86 小节) --- */
sec('A', 8, 3, 120);                     // 0-7    开门迎客 (手风琴引子 + 酒馆嘈杂 + 木桶敲击)
sec('B', 16, 3, 132);                    // 8-23   第一轮舞曲 (oom-pah 主歌, 吉格主题A)
sec('C', 16, 3, 138);                    // 24-39  合唱式副歌 (全奏, 主题B + 拍手/碰杯)
sec('D', 10, 3, null, [112, 88], 0.055); // 40-49  醉意间奏 (速度微飘 / 走音 / 只剩拨弦与哼唱)
sec('E', 28, 3, null, [148, 156]);       // 50-77  更快的终段 (跺脚/碰杯/全场跟唱)
sec('F', 8, 3, 144);                     // 78-85  收尾 (碰杯欢笑 + 杯子碎裂 + 尾混响)

const BARS = METER.length;
const tBar = [];
let TOTAL = 0;
for (const m of METER) { tBar.push(TOTAL); TOTAL += m.beats * 60 / m.bpm; }
const barDur = bar => { const b = Math.max(0, Math.min(METER.length - 1, Math.floor(bar))); return METER[b].beats * 60 / METER[b].bpm; };
const bpmAt = bar => METER[Math.max(0, Math.min(METER.length - 1, Math.floor(bar)))].bpm;
/** 支持小数小节 */
const tBeat = (bar, q) => {
  const b0 = Math.floor(bar), fr = bar - b0;
  const bb = Math.max(0, Math.min(METER.length - 1, b0));
  return tBar[bb] + fr * barDur(bb) + q * 60 / METER[bb].bpm;
};

const N = Math.ceil((TOTAL + TAIL) * SR);
const S = t => Math.round(t * SR);

const rnd = (() => { let a = 20261007; return () => { a |= 0; a = a + 0x6D2B79F5 | 0; let t = Math.imul(a ^ a >>> 15, 1 | a); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; })();
const midiHz = n => 440 * Math.pow(2, (n - 69) / 12);

/* 三条总线: mus=旋律/和声/低音, dry=打击, amb=人群env */
const musL = new Float32Array(N), musR = new Float32Array(N);
const dryL = new Float32Array(N), dryR = new Float32Array(N);
const ambL = new Float32Array(N), ambR = new Float32Array(N);

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
const T_REED = makeTable([1, 0.04, 0.62, 0.05, 0.34, 0.04, 0.20, 0.03, 0.13, 0.02, 0.08, 0.05]);      // 自由簧(手风琴)
const T_HARP = makeTable([1, 0.10, 0.75, 0.08, 0.50, 0.06, 0.34, 0.05, 0.22, 0.04, 0.14, 0.03, 0.09]); // 口琴(偏奇次)
const T_WHIST = makeTable([1, 0.05, 0.11, 0.02]);                                                     // 口哨(近正弦)
const T_SAW = makeTable(sawH(40, 26));
const T_STR = makeTable(sawH(32, 16));

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

/* ================= 醉意 (音高游移) ================= */
/** DRUNK: 0=清醒 1=烂醉; 段落装配时切换 (装配是顺序渲染, 直接读全局) */
let DRUNK = 0;
/** 平滑伪随机 [-1,1], 用绝对时间做种子 → 不同乐器/音高各自游移, 合起来"走音" */
function wob(t, seed) {
  return Math.sin(t * 0.53 + seed * 0.21) * 0.5 + Math.sin(t * 0.91 + seed * 0.37) * 0.3 + Math.sin(t * 1.77 + seed * 0.11) * 0.2;
}
function driftCents(t, seed) {
  if (DRUNK <= 0) return 0;
  return DRUNK * 27 * wob(t, seed);
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
function bassDrum(bl, br, start, amp) {
  const len = Math.floor(0.5 * SR), startI = S(start);
  let ph = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 46 + (150 - 46) * Math.exp(-t / 0.026);
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.13) * (1 - Math.exp(-t / 0.0014));
    let s = Math.sin(ph) * e;
    const w = rnd() * 2 - 1; lp += 0.22 * (w - lp);
    s += lp * Math.exp(-t / 0.007) * 0.4;
    put(bl, startI + i, s * amp); put(br, startI + i, s * amp);
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
    snare(bl, br, start + t, amp * (0.28 + 0.72 * x), Math.sin(t * 31) * 0.45);
  }
}
/** 木桶敲击 (酒桶王的招牌音色): 空心木腔模态 + 低频闷响 + 敲击瞬态 */
function barrel(bl, br, start, amp, pan, opt) {
  opt = opt || {};
  const base = (opt.freq || 172) * (1 + (opt.detune || 0) / 1200);
  const modes = [[1, 1.00, 0.13], [2.92, 0.52, 0.085], [5.61, 0.30, 0.055], [9.05, 0.16, 0.032]];
  const len = Math.floor(0.42 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const ph = modes.map(() => 0);
  let th = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (let k = 0; k < modes.length; k++) {
      ph[k] += 2 * Math.PI * base * modes[k][0] / SR;
      s += Math.sin(ph[k]) * modes[k][1] * Math.exp(-t / modes[k][2]);
    }
    th += 2 * Math.PI * (78 * Math.exp(-t / 0.05) + 57) / SR;
    s += Math.sin(th) * Math.exp(-t / 0.10) * 0.72;
    const w = rnd() * 2 - 1; lp += 0.55 * (w - lp);
    s += lp * Math.exp(-t / 0.004) * 0.6;
    const e = 1 - Math.exp(-t / 0.0008);
    put(bl, startI + i, s * e * amp * pl); put(br, startI + i, s * e * amp * pr);
  }
}
/** 手鼓 (bodhrán 式): 皮膜音高下滑 + 鼓皮噪声 + 边缘"啪" */
function frameDrum(bl, br, start, amp, pan, opt) {
  opt = opt || {};
  const f0 = opt.freq || 118;
  const len = Math.floor(0.38 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, lp = 0, hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = f0 * (1 + (opt.bend !== undefined ? opt.bend : 0.85) * Math.exp(-t / 0.02));
    ph += 2 * Math.PI * f / SR;
    const e = Math.exp(-t / 0.13) * (1 - Math.exp(-t / 0.0015));
    let s = Math.sin(ph) * e * 0.9;
    const w = rnd() * 2 - 1;
    lp += 0.35 * (w - lp);
    s += lp * Math.exp(-t / 0.038) * 0.55;
    const h = 0.9 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    s += h * Math.exp(-t / 0.006) * 0.20;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 铃鼓/小手鼓圈 (tambourine): 高频金属环 + 沙沙噪声 */
function tambourine(bl, br, start, amp, pan) {
  const len = Math.floor(0.30 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  const ring = [4600, 5900, 7400, 8800, 10200];
  const rp = ring.map(() => rnd() * 6.283);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const h = 0.94 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    let s = h * Math.exp(-t / 0.035) * 0.9;
    for (let k = 0; k < ring.length; k++) {
      rp[k] += 2 * Math.PI * ring[k] / SR;
      s += Math.sin(rp[k]) * Math.exp(-t / (0.05 + 0.03 * k)) * 0.09;
    }
    const env = 1 - Math.exp(-t / 0.0008);
    put(bl, startI + i, s * env * amp * pl); put(br, startI + i, s * env * amp * pr);
  }
}
/** 铃鼓摇奏 (fills) */
function tamShake(bl, br, start, durSec, amp) {
  const step = 0.032;
  let k = 0;
  for (let t = 0; t < durSec; t += step) {
    const x = t / durSec;
    tambourine(bl, br, start + t, amp * (0.35 + 0.65 * x) * (0.7 + 0.3 * ((k * 7) % 5) / 4), (k % 2 ? 0.30 : -0.30));
    k++;
  }
}
/** 跺脚 (全场跺地) */
function stomp(bl, br, start, amp, pan) {
  const len = Math.floor(0.32 * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let ph = 0, ph2 = 0, lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const f = 62 + 46 * Math.exp(-t / 0.02);
    ph += 2 * Math.PI * f / SR;
    ph2 += 2 * Math.PI * f * 1.62 / SR;                 // 地板共振
    const e = Math.exp(-t / 0.075) * (1 - Math.exp(-t / 0.001));
    let s = (Math.sin(ph) * 0.9 + Math.sin(ph2) * 0.22 * Math.exp(-t / 0.03)) * e;
    const w = rnd() * 2 - 1; lp += 0.42 * (w - lp);
    s += lp * Math.exp(-t / 0.014) * 0.5;
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 拍手 (3 层微延时) */
function clap(bl, br, start, amp, pan) {
  const [pl, pr] = panGains(pan || 0);
  [0, 0.009, 0.020].forEach((off, k) => {
    const len = Math.floor(0.16 * SR), startI = S(start + off);
    let hp = 0, hpPrev = 0, wPrev = 0;
    for (let i = 0; i < len; i++) {
      const t = i / SR;
      const w = rnd() * 2 - 1;
      const h = 0.90 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
      const co = 0.0105 * (k === 2 ? 1.5 : 1.0);
      const s = h * Math.exp(-t / (k === 2 ? 0.11 : 0.02)) * amp;
      put(bl, startI + i, s * pl * co * 90); put(br, startI + i, s * pr * co * 90);
    }
  });
}
/** 把一段"多分音叠加"的临时缓冲归一化到峰值 1.0 —— 避免瞬态失控冲垮整首的峰均比 */
function mixIn(bl, br, startI, tmp, amp, pan) {
  const [pl, pr] = panGains(pan || 0);
  let mx = 0;
  for (let i = 0; i < tmp.length; i++) { const a = Math.abs(tmp[i]); if (a > mx) mx = a; }
  const g = mx > 0 ? amp / mx : 0;
  for (let i = 0; i < tmp.length; i++) {
    const s = tmp[i] * g;
    put(bl, startI + i, s * pl); put(br, startI + i, s * pr);
  }
}
/** 碰杯 (清脆瞬态 + 长鸣): 玻璃高非谐分音 */
function glassClink(bl, br, start, amp, pan) {
  const base = 1350 + rnd() * 950;
  const ratios = [1, 2.34, 3.81, 5.42, 7.16, 9.10];
  const dec = [0.85, 0.52, 0.36, 0.25, 0.17, 0.11];
  const len = Math.floor(1.5 * SR), startI = S(start);
  const tmp = new Float32Array(len);
  const ph = ratios.map(() => 0);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const bend = 1 - 0.006 * Math.min(1, t / 0.5);
    let s = 0;
    for (let k = 0; k < ratios.length; k++) {
      ph[k] += 2 * Math.PI * base * ratios[k] * bend / SR;
      s += Math.sin(ph[k]) * Math.exp(-t / dec[k]) / (1 + k * 0.55);
    }
    const w = rnd() * 2 - 1;
    const h = 0.9 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    s += h * Math.exp(-t / 0.002) * 0.7;
    tmp[i] = s * (1 - Math.exp(-t / 0.0006));
  }
  mixIn(bl, br, startI, tmp, amp, pan);
}
/** 杯子碎裂 (收尾的招牌瞬态) */
function glassBreak(bl, br, start, amp) {
  const len = Math.floor(1.2 * SR), startI = S(start);
  const tmp = new Float32Array(len);
  const par = [];
  for (let k = 0; k < 26; k++) par.push({ f: 1900 + rnd() * 8800, p: rnd() * 6.283, d: 0.03 + rnd() * 0.42, a: 0.25 + rnd() * 0.7 });
  const shard = [];
  for (let k = 0; k < 12; k++) shard.push({ t: 0.02 + rnd() * 0.6, f: 520 + rnd() * 1500, p: 0, d: 0.05 + rnd() * 0.22, a: 0.2 + rnd() * 0.5 });
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let s = 0;
    for (const q of par) { q.p += 2 * Math.PI * q.f / SR; s += Math.sin(q.p) * q.a * Math.exp(-t / q.d); }
    const w = rnd() * 2 - 1;
    const h = 0.92 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    s += h * Math.exp(-t / 0.05) * 0.7;
    for (const q of shard) if (t >= q.t) { q.p += 2 * Math.PI * q.f / SR; s += Math.sin(q.p) * q.a * Math.exp(-(t - q.t) / q.d); }
    tmp[i] = s * (1 - Math.exp(-t / 0.0005));
  }
  mixIn(bl, br, startI, tmp, amp, 0);
}

/* ================= 酒馆人群 (嘈杂 / 低语 / 笑 / 欢呼) ================= */
function bpCoeff(fc, q) { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b1: 0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; }
function bpRun(x, st, co) { const y = co.b0 * x + st.z1; st.z1 = co.b1 * x - co.a1 * y + st.z2; st.z2 = co.b2 * x - co.a2 * y; return y; }
/** 人群底噪: 三个带通噪声层 + 慢速起伏 (压在 amb 总线, 电平常低) */
function crowdMurmur(start, durSec, amp) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const co = [bpCoeff(420, 0.7), bpCoeff(1450, 0.9), bpCoeff(2350, 1.0)];
  const st = [{ z1: 0, z2: 0 }, { z1: 0, z2: 0 }, { z1: 0, z2: 0 }];
  const gw = [0.55, 0.90, 0.26];
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    let s = 0;
    for (let k = 0; k < 3; k++) s += bpRun(w, st[k], co[k]) * gw[k];
    const mod = 0.60 + 0.20 * Math.sin(2 * Math.PI * 0.23 * t + 0.7) + 0.13 * Math.sin(2 * Math.PI * 0.41 * t + 2.1) + 0.06 * Math.sin(2 * Math.PI * 1.31 * t);
    const env = Math.min(1, t / 1.4) * Math.min(1, (durSec - t) / 1.8);
    const v = s * mod * env * amp;
    put(ambL, startI + i, v); put(ambR, startI + i, v * 0.86);
  }
}
/** 人声频段嗡嗡 (共振峰合成, 男声区, 极低电平) —— 只当"有人在说话"的色彩 */
const VOWEL = { a: [800, 1150, 2900], o: [500, 1000, 2400], u: [340, 700, 2300], e: [520, 1800, 2500], i: [300, 2200, 3000], m: [260, 1100, 2150] };
function voice(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const v = VOWEL[opt.vowel || 'a'];
  const vibHz = opt.vibHz || 5.0, vibC = opt.vibCents || 18;
  const [pl, pr] = panGains(pan);
  const atk = opt.atk !== undefined ? opt.atk : 0.35;
  const rel = opt.rel !== undefined ? opt.rel : 0.7;
  const glide = opt.glide || 0;
  const det = driftCents(start, note);
  const len = Math.floor((durSec + rel + 0.2) * SR), startI = S(start);
  const mk = (fc, q) => { const w = 2 * Math.PI * fc / SR, c = Math.cos(w), al = Math.sin(w) / (2 * q), a0 = 1 + al; return { b0: al / a0, b2: -al / a0, a1: -2 * c / a0, a2: (1 - al) / a0 }; };
  const gain = [1.0, 0.5, 0.22];
  const cL = v.map((fc, k) => mk(fc, [9, 11, 13][k]));
  const cR = v.map((fc, k) => mk(fc * 1.008, [9, 11, 13][k]));
  const sL = cL.map(() => ({ z1: 0, z2: 0 })), sR = cR.map(() => ({ z1: 0, z2: 0 }));
  let ph = rnd();
  const inc0 = midiHz(note) * Math.pow(2, det / 1200) / SR * SIZE;
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
/** 酒客低语/吆喝: 短促的音节 (随机音高与元音) */
function babble(start, durSec, amp) {
  let t = 0;
  while (t < durSec - 0.3) {
    const n = 2 + Math.floor(rnd() * 4);
    for (let k = 0; k < n; k++) {
      const d = 0.10 + rnd() * 0.20;
      const note = 45 + Math.floor(rnd() * 13);
      const pan = rnd() * 1.5 - 0.75;
      voice(ambL, ambR, pan, start + t, note, amp * (0.55 + rnd() * 0.6), d,
        { vowel: ['a', 'o', 'e', 'u'][Math.floor(rnd() * 4)], vibHz: 4 + rnd() * 3, vibCents: 12 + rnd() * 20, atk: 0.03 + rnd() * 0.05, rel: 0.05 + rnd() * 0.08, glide: (rnd() - 0.5) * 2.2 });
      t += d + 0.05 + rnd() * 0.35;
    }
    t += 0.5 + rnd() * 1.4;
  }
}
/** 笑声 "哈哈哈" (下滑音节) */
function laughter(start, amp) {
  const n = 4 + Math.floor(rnd() * 3);
  let t = 0;
  const pan = rnd() * 1.2 - 0.6;
  const base = 52 + rnd() * 10;
  for (let k = 0; k < n; k++) {
    voice(ambL, ambR, pan + (rnd() - 0.5) * 0.3, start + t, base - k * 1.1 - rnd(), amp * (1 - 0.10 * k), 0.115,
      { vowel: k % 2 ? 'a' : 'e', vibHz: 6.5, vibCents: 26, atk: 0.02, rel: 0.05, glide: -1.6 });
    t += 0.145 + rnd() * 0.05;
  }
  // 呼气
  const len = Math.floor(0.4 * SR), startI = S(start + t);
  let hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const tt = i / SR;
    const w = rnd() * 2 - 1;
    const h = 0.9 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    const e = Math.exp(-tt / 0.16) * (1 - Math.exp(-tt / 0.01));
    put(ambL, startI + i, h * e * amp * 0.5); put(ambR, startI + i, h * e * amp * 0.45);
  }
}
/** 全场欢呼 (渐强噪声 + 多人和声 + 拍手) */
function cheer(start, durSec, amp) {
  const len = Math.floor(durSec * SR), startI = S(start);
  const co = bpCoeff(1100, 0.8), co2 = bpCoeff(2600, 1.0);
  const st = { z1: 0, z2: 0 }, st2 = { z1: 0, z2: 0 };
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const w = rnd() * 2 - 1;
    const a1 = bpRun(w, st, co), a2 = bpRun(w, st2, co2);
    const env = Math.min(1, t / 0.25) * Math.max(0, 1 - Math.pow(t / durSec, 1.6));
    const v = (a1 * 1.0 + a2 * 0.55) * env * amp;
    put(ambL, startI + i, v); put(ambR, startI + i, v * 0.9);
  }
  const n = Math.floor(durSec * 6);
  for (let k = 0; k < n; k++) {
    const t = rnd() * durSec * 0.8;
    voice(ambL, ambR, rnd() * 1.6 - 0.8, start + t, 45 + rnd() * 16, amp * 1.6, 0.12 + rnd() * 0.2,
      { vowel: ['a', 'o', 'e'][Math.floor(rnd() * 3)], vibHz: 5 + rnd() * 3, vibCents: 20, atk: 0.03, rel: 0.08, glide: 2.5 * rnd() });
  }
  for (let t = 0; t < Math.min(durSec, 2.2); t += 0.19 + rnd() * 0.06) clap(ambL, ambR, start + t, amp * 1.4, rnd() * 1.4 - 0.7);
}

/* ================= 酒魔 (低音不谐和) ================= */
/** 三全音低音潜行: 根音 + 增四度, 慢拍频 —— "酒魔"的诡异 */
function subGrowl(start, durSec, note, amp) {
  const f = midiHz(note);
  const len = Math.floor(durSec * SR), startI = S(start);
  let p1 = 0, p2 = 0, p3 = 0, lp = 0;
  const tri = Math.pow(2, 6 / 12);
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const env = Math.min(1, t / 0.6) * Math.min(1, (durSec - t) / 1.0);
    p1 += 2 * Math.PI * f * (1 + 0.004 * Math.sin(2 * Math.PI * 0.7 * t)) / SR;
    p2 += 2 * Math.PI * f * tri * (1 + 0.004 * Math.sin(2 * Math.PI * 0.53 * t + 1)) / SR;
    p3 += 2 * Math.PI * f * 0.5 / SR;
    let s = Math.sin(p1) * 0.55 + Math.sin(p2) * 0.30 + Math.sin(p3) * 0.16 + Math.sin(p1 * 2) * 0.10;
    const w = rnd() * 2 - 1; lp += 0.06 * (w - lp); s += lp * 0.10;
    put(musL, startI + i, s * env * amp); put(musR, startI + i, s * env * amp * 0.92);
  }
}

/* ================= 弹拨 ================= */
/** 拨弦 (Karplus-Strong): 曼陀林/木吉他 */
function pluck(bl, br, pan, start, note, amp, opt) {
  opt = opt || {};
  const det = (opt.detune || 0) + driftCents(start, note);
  const f = midiHz(note) * Math.pow(2, det / 1200);
  const Ns = Math.max(3, Math.round(SR / f));
  const buf = new Float32Array(Ns);
  const bright = opt.bright !== undefined ? opt.bright : 0.45;
  let lp = 0;
  for (let i = 0; i < Ns; i++) { const w = rnd() * 2 - 1; lp += bright * (w - lp); buf[i] = lp; }
  let mx = 0; for (let i = 0; i < Ns; i++) { const a = Math.abs(buf[i]); if (a > mx) mx = a; }
  if (mx > 0) for (let i = 0; i < Ns; i++) buf[i] /= mx;
  const dec = opt.decay || 1.0;                              // 衰减到 -60dB 的秒数
  const damp = Math.pow(0.001, 1 / Math.max(1, dec * f));
  const dur = opt.dur || Math.min(1.5, dec * 0.85);
  const len = Math.floor((dur + 0.2) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  let idx = 0, last = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const cur = buf[idx];
    buf[idx] = 0.5 * (cur + last) * damp;
    last = cur;
    idx++; if (idx >= Ns) idx = 0;
    let s = cur;
    s += (rnd() * 2 - 1) * Math.exp(-t / 0.0035) * 0.45;      // 指甲/拨片瞬态
    s *= 1 - Math.exp(-t / 0.0012);
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}
/** 和弦扫弦 (下/上交替, 弦间微延时) */
function strum(bl, br, pan, start, notes, amp, opt) {
  opt = opt || {};
  const up = opt.dir === 'up';
  const spread = opt.spread !== undefined ? opt.spread : 0.020;
  const list = up ? notes.slice().reverse() : notes.slice();
  const n = list.length;
  list.forEach((m, k) => {
    pluck(bl, br, (pan || 0) + (k - (n - 1) / 2) * 0.10, start + k * spread, m,
      amp * (0.72 + 0.28 * Math.abs(Math.sin(k * 1.7 + 0.6))),
      { decay: opt.decay || 0.85, dur: opt.dur || 0.65, bright: opt.bright !== undefined ? opt.bright : 0.52, detune: opt.detune || 0 });
  });
}
/** 曼陀林轮指 (醉意间奏的招牌) */
function mandTrem(bl, br, pan, start, durSec, notes, amp, opt) {
  opt = opt || {};
  const step = opt.step || 0.072;
  const spread = 0.012;
  const n = notes.length;
  let t = 0, k = 0;
  while (t < durSec) {
    notes.forEach((m, j) => pluck(bl, br, (pan || 0) + (j - (n - 1) / 2) * 0.13, start + t + j * spread, m,
      amp * (0.55 + 0.25 * Math.abs(Math.sin(k * 2.1 + j))), { decay: 0.45, dur: 0.30, bright: 0.6, detune: opt.detune || 0 }));
    t += step * (0.90 + 0.18 * (((k * 37) % 7) / 7));
    k++;
  }
}

/* ================= 低音提琴 pizzicato ================= */
function pizz(bl, br, pan, start, note, amp, dur, opt) {
  opt = opt || {};
  const det = (opt.detune || 0) + driftCents(start, note);
  const f0 = midiHz(note) * Math.pow(2, det / 1200);
  const droop = Math.pow(2, (opt.droop || 0) / 1200) - 1;
  const atk = 0.005, len = Math.ceil((dur + 0.6) * SR), startI = S(start);
  const [pl, pr] = panGains(pan || 0);
  const harm = [[1, 0.90], [2, 0.42], [3, 0.20], [4.02, 0.10]];
  const ph = harm.map(() => 0);
  let lp = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const bend = 1 + droop * Math.min(1, t / Math.max(0.05, dur));
    let s = 0;
    for (let k = 0; k < harm.length; k++) {
      ph[k] += 2 * Math.PI * f0 * harm[k][0] * bend / SR;
      s += Math.sin(ph[k]) * harm[k][1] * Math.exp(-t / (dur * (1 - k * 0.16)));
    }
    s *= 1 - Math.exp(-t / atk);
    const w = rnd() * 2 - 1; lp += 0.5 * (w - lp);
    s += lp * Math.exp(-t / 0.012) * 0.5;                     // 手指摩擦
    put(bl, startI + i, s * amp * pl); put(br, startI + i, s * amp * pr);
  }
}

/* ================= 手风琴 / 口琴 / 口哨 / 走音钢琴 ================= */
/** 手风琴: 双簧片失谐(musette) + 可选低音簧 + 簧片噪声 + 风箱颤音 + 走音 */
function accordion(bl, br, pan, start, note, amp, dur, opt) {
  opt = opt || {};
  const atk = opt.atk !== undefined ? opt.atk : 0.05;
  const rel = opt.rel !== undefined ? opt.rel : 0.13;
  const mus = opt.musette !== undefined ? opt.musette : 9;
  const det = (opt.detune || 0) + driftCents(start, note);
  const droop = opt.droop || 0;
  const tremHz = opt.tremHz !== undefined ? opt.tremHz : 5.3;
  const tremD = opt.tremDepth !== undefined ? opt.tremDepth : 0.14;
  const [pl, pr] = panGains(pan || 0);
  const f0 = midiHz(note);
  const banks = [
    { f: f0 * Math.pow(2, (det - mus) / 1200), a: 0.55 },
    { f: f0 * Math.pow(2, (det + mus) / 1200), a: 0.55 }
  ];
  if (opt.low) banks.push({ f: f0 / 2 * Math.pow(2, (det + 4) / 1200), a: 0.30 });
  const bend = 1 + (Math.pow(2, droop / 1200) - 1);
  const len = Math.ceil((atk + dur + rel + 0.1) * SR), startI = S(start);
  const ph = banks.map(() => rnd() * SIZE);
  let nz = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    let a = adsr(t, atk + dur, atk, 0.07, 1, rel);
    if (a <= 0) continue;
    a *= 1 - tremD + tremD * (0.5 + 0.5 * Math.sin(2 * Math.PI * tremHz * t + note));
    const bd = 1 + (bend - 1) * Math.min(1, t / Math.max(0.05, dur));
    let s = 0;
    for (let k = 0; k < banks.length; k++) {
      const inc = banks[k].f * bd / SR * SIZE;
      const i0 = ph[k] | 0, fr = ph[k] - i0;
      s += (T_REED[i0] + (T_REED[(i0 + 1) & (SIZE - 1)] - T_REED[i0]) * fr) * banks[k].a;
      ph[k] += inc; if (ph[k] >= SIZE) ph[k] -= SIZE;
    }
    const w = rnd() * 2 - 1; nz += 0.30 * (w - nz);
    s += nz * Math.exp(-t / 0.02) * 0.28;                      // 簧片起音噪声
    const j = startI + i;
    put(bl, j, s * a * amp * pl); put(br, j, s * a * amp * pr);
  }
}
/** 口琴(kind='harp') / 口哨(kind='whistle'): 吹气噪声 + 簧/正弦音体 */
function mouthHarp(bl, br, pan, start, note, amp, dur, opt) {
  opt = opt || {};
  const kind = opt.kind || 'harp';
  const wh = kind === 'whistle';
  const atk = opt.atk !== undefined ? opt.atk : (wh ? 0.07 : 0.045);
  const rel = opt.rel !== undefined ? opt.rel : (wh ? 0.16 : 0.11);
  const vibHz = opt.vibHz !== undefined ? opt.vibHz : (wh ? 5.0 : 5.6);
  const vibC = opt.vibCents !== undefined ? opt.vibCents : (wh ? 12 : 17);
  const det = (opt.detune || 0) + driftCents(start, note);
  const [pl, pr] = panGains(pan || 0);
  const len = Math.ceil((atk + dur + rel + 0.12) * SR), startI = S(start);
  const inc0 = midiHz(note) * Math.pow(2, det / 1200) / SR * SIZE;
  const tb = wh ? T_WHIST : T_HARP;
  const bd = 1 + (Math.pow(2, (opt.droop || 0) / 1200) - 1);
  let ph = rnd() * SIZE, lp = 0, hp = 0, hpPrev = 0, wPrev = 0;
  for (let i = 0; i < len; i++) {
    const t = i / SR;
    const a = adsr(t, atk + dur, atk, 0.06, 1, rel);
    if (a <= 0) continue;
    const bn = 1 + (bd - 1) * Math.min(1, t / Math.max(0.05, dur));
    const inc = inc0 * bn * Math.pow(2, vibC * Math.sin(2 * Math.PI * vibHz * t) * Math.min(1, t / 0.3) / 1200);
    const i0 = ph | 0, fr = ph - i0;
    let s = tb[i0] + (tb[(i0 + 1) & (SIZE - 1)] - tb[i0]) * fr;
    ph += inc; if (ph >= SIZE) ph -= SIZE;
    const w = rnd() * 2 - 1;
    const h = 0.93 * (hpPrev + w - wPrev); hpPrev = h; wPrev = w;
    lp += (wh ? 0.30 : 0.55) * (w - lp);
    const breath = h * (wh ? 0.9 : 0.6) - lp * (wh ? 0.5 : 0.35);
    const benv = (1 - Math.exp(-t / (atk * 0.8 + 0.004))) * Math.exp(-t / (dur + rel + 0.4));
    s = s * 0.82 + breath * benv * (wh ? 0.55 : 0.40);
    const j = startI + i;
    put(bl, j, s * a * amp * pl * 0.9); put(br, j, s * a * amp * pr * 0.9);
  }
}
/** 走音的酒吧钢琴 (三根弦各自偏移 + 非谐性 + 醉意游移) */
function piano(bl, br, pan, start, note, amp, durSec, opt) {
  opt = opt || {};
  const [pl, pr] = panGains(pan || 0);
  const det = (opt.detune || 0) + driftCents(start, note);
  const f0 = midiHz(note) * Math.pow(2, det / 1200);
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

/* ================= 和声: G 混合利底亚 (♭VII = F) → 同主音小调 g (♭VI = E♭) ================= */
const CH = {
  G:  { bass: 31, pad: [59, 62, 67] },   // G1 / B3 D4 G4
  C:  { bass: 36, pad: [60, 64, 67] },   // C2 / C4 E4 G4
  D:  { bass: 38, pad: [57, 62, 66] },   // D2 / A3 D4 F#4
  D7: { bass: 38, pad: [57, 60, 66] },   // D2 / A3 C4 F#4  (C = ♭7, 三全音张力 → 解决到 G)
  Em: { bass: 40, pad: [59, 64, 67] },   // E2 / B3 E4 G4
  F:  { bass: 41, pad: [60, 65, 69] },   // F2 / C4 F4 A4   ← ♭VII, 混合利底亚的招牌
  Am: { bass: 33, pad: [60, 64, 69] },   // A1 / C4 E4 A4
  Gm: { bass: 31, pad: [58, 62, 67] },   // g 小调
  Eb: { bass: 39, pad: [58, 63, 67] },   // ♭VI
  Bb: { bass: 34, pad: [58, 62, 65] },
  Cm: { bass: 36, pad: [60, 63, 67] }
};
const PROG_A = ['G', 'C', 'G', 'D7', 'Em', 'C', 'F', 'D7'];    // 主歌 8 小节
const PROG_B = ['C', 'G', 'F', 'C', 'G', 'F', 'D7', 'G'];       // 副歌 8 小节
const PROG_D = ['Gm', 'Eb', 'Gm', 'Eb', 'Cm', 'Gm', 'Bb', 'Eb', 'D7', 'D7'];  // 醉意 10 小节
const PROG_TAG = ['G', 'F', 'C', 'D7'];                          // 终段尾声 4 小节
const PROG_F = ['G', 'F', 'C', 'G', 'G', 'F', 'D7', 'G'];        // 收尾 8 小节
const keyA = i => PROG_A[((i % 8) + 8) % 8];
const keyB = i => PROG_B[((i % 8) + 8) % 8];

/* ================= 主题 (全部原创; e = 小节内第几个八分音符, 6/8 → 0..5) ================= */
/** 主题A「酒馆开门调」(吉格, 8 小节) */
const TUNE_A = [
  [[0, 74, 1], [1, 71, 0.5], [1.5, 67, 0.5], [2, 69, 1], [3, 71, 0.5], [3.5, 69, 0.5], [4, 67, 1.5]],
  [[0, 72, 1], [1, 71, 0.5], [1.5, 72, 0.5], [2, 74, 1], [3, 76, 1], [4, 74, 1.5]],
  [[0, 71, 1], [1, 67, 0.5], [1.5, 71, 0.5], [2, 74, 1], [3, 71, 1], [4, 69, 1.5]],
  [[0, 69, 1], [1, 71, 0.5], [1.5, 72, 0.5], [2, 74, 1], [3, 72, 1], [4, 69, 1.5]],
  [[0, 67, 1], [1, 71, 0.5], [1.5, 74, 0.5], [2, 76, 1], [3, 74, 1], [4, 71, 1.5]],
  [[0, 72, 1], [1, 76, 1], [2, 74, 1], [3, 72, 1], [4, 69, 1.5]],
  [[0, 72, 1], [1, 69, 0.5], [1.5, 72, 0.5], [2, 77, 1], [3, 76, 1], [4, 72, 1.5]],
  [[0, 74, 1], [1, 72, 1], [2, 69, 1], [3, 71, 0.5], [3.5, 72, 0.5], [4, 74, 1.5]]
];
/** 主题A' (主歌后半, 上句变尾) */
const TUNE_A2 = [
  [[0, 74, 1], [1, 71, 0.5], [1.5, 67, 0.5], [2, 69, 1], [3, 71, 0.5], [3.5, 74, 0.5], [4, 76, 1.5]],
  [[0, 77, 1], [1, 76, 0.5], [1.5, 74, 0.5], [2, 76, 1], [3, 72, 1], [4, 74, 1.5]],
  [[0, 71, 1], [1, 74, 0.5], [1.5, 71, 0.5], [2, 67, 1], [3, 71, 1], [4, 74, 1.5]],
  [[0, 72, 1], [1, 71, 0.5], [1.5, 69, 0.5], [2, 71, 1], [3, 69, 1], [4, 67, 1.5]],
  [[0, 76, 1], [1, 74, 0.5], [1.5, 76, 0.5], [2, 74, 1], [3, 71, 1], [4, 74, 1.5]],
  [[0, 72, 1], [1, 76, 1], [2, 74, 1], [3, 72, 1], [4, 69, 1.5]],
  [[0, 72, 1], [1, 69, 0.5], [1.5, 72, 0.5], [2, 77, 1], [3, 76, 1], [4, 74, 1.5]],
  [[0, 72, 1], [1, 71, 1], [2, 74, 1], [3, 76, 1], [4, 74, 1.5]]
];
/** 主题B「合唱」(副歌, 8 小节, 高音区、方正、好跟唱) */
const TUNE_B = [
  [[0, 79, 1.5], [1.5, 76, 0.5], [2, 79, 1], [3, 76, 0.5], [3.5, 74, 0.5], [4, 72, 1.5]],
  [[0, 74, 1], [1, 79, 1], [2, 77, 1], [3, 74, 1], [4, 71, 1.5]],
  [[0, 72, 1.5], [1.5, 69, 0.5], [2, 72, 1], [3, 74, 0.5], [3.5, 76, 0.5], [4, 77, 1.5]],
  [[0, 76, 1], [1, 74, 1], [2, 72, 1], [3, 67, 1], [4, 72, 1.5]],
  [[0, 74, 1], [1, 79, 1.5], [2.5, 77, 0.5], [3, 76, 1], [4, 74, 1.5]],
  [[0, 72, 1], [1, 77, 1], [2, 76, 1], [3, 74, 1], [4, 72, 1.5]],
  [[0, 69, 1], [1, 74, 1], [2, 72, 1], [3, 71, 0.5], [3.5, 69, 0.5], [4, 71, 1.5]],
  [[0, 67, 1], [1, 71, 1], [2, 74, 1], [3, 72, 0.5], [3.5, 71, 0.5], [4, 67, 3.0]]
];
/** 醉意主题 (g 小调, 拖沓、下滑、含 ♭6=E♭ 的诡异) */
const TUNE_DRUNK = [
  [[0, 70, 1.5], [2, 74, 1], [3, 75, 0.5], [3.5, 74, 0.5], [4, 70, 1.5]],
  [[0, 75, 1.5], [2, 70, 1], [3, 72, 1], [4, 75, 1.5]],
  [[0, 74, 1], [2, 70, 1], [3, 69, 1], [4, 67, 2]],
  [[0, 75, 2], [2.5, 77, 0.5], [3, 75, 1], [4, 72, 1.5]],
  [[0, 72, 1.5], [2, 67, 1], [3, 70, 1], [4, 72, 1.5]],
  [[0, 74, 1], [2, 70, 1], [3, 67, 1], [4, 62, 2.5]],
  [[0, 70, 1.5], [2, 65, 1], [3, 67, 1], [4, 70, 1.5]],
  [[0, 72, 1], [2, 75, 1], [3, 74, 0.5], [3.5, 72, 0.5], [4, 70, 2.5]],
  [[0, 69, 1], [2, 68, 0.5], [2.5, 69, 0.5], [3, 71, 1], [4, 74, 2]],
  [[0, 72, 1], [2, 71, 1], [3, 69, 1], [4, 62, 3]]
];
/** 迎客引子 (长音版主题A素材) */
const TUNE_WELCOME = [
  [[0, 74, 3], [3, 71, 1.5], [4.5, 72, 1.5]],
  [[0, 74, 3], [3, 76, 3]],
  [[0, 71, 1.5], [1.5, 74, 1.5], [3, 72, 1.5], [4.5, 71, 1.5]],
  [[0, 69, 6]]
];
const TUNE_WELCOME2 = [
  [[0, 67, 1.5], [1.5, 71, 1.5], [3, 74, 1.5], [4.5, 76, 1.5]],
  [[0, 77, 3], [3, 76, 1.5], [4.5, 74, 1.5]],
  [[0, 72, 1.5], [1.5, 74, 1.5], [3, 76, 1.5], [4.5, 77, 1.5]],
  [[0, 74, 6]]
];
/** 终段尾声的加速走句 (4 小节) */
const TUNE_TAG = [
  [[0, 67, 1], [1, 71, 1], [2, 74, 1], [3, 76, 1], [4, 79, 1], [5, 76, 1]],
  [[0, 77, 1], [1, 74, 1], [2, 72, 1], [3, 74, 1], [4, 72, 1], [5, 69, 1]],
  [[0, 72, 1], [1, 76, 1], [2, 79, 1], [3, 81, 1], [4, 79, 1], [5, 76, 1]],
  [[0, 74, 1], [1, 72, 1], [2, 71, 1], [3, 69, 1], [4, 71, 1], [5, 74, 1]]
];

/* ================= 主题演奏器 ================= */
function playTune(tune, bar0, bars, opt) {
  opt = opt || {};
  // 旋律是主角: 统一再抬 1.22 倍, 免得被 oom-pah 和弦顿音盖住
  const oct = opt.oct || 0, amp = (opt.amp !== undefined ? opt.amp : 0.09) * 1.22;
  const v = opt.voice || 'acc', pan = opt.pan || 0;
  const legato = opt.legato !== undefined ? opt.legato : 0.92;
  for (let bi = 0; bi < bars; bi++) {
    const bar = bar0 + bi;
    const notes = tune[bi % tune.length];
    const q = 60 / bpmAt(bar);
    notes.forEach(([e, m, d]) => {
      const st = tBeat(bar, e * 0.5);
      const hold = d * 0.5 * q * legato;
      const note = m + oct;
      if (v === 'acc') accordion(musL, musR, pan, st, note, amp, hold, { low: opt.low, musette: opt.musette, detune: opt.detune, droop: opt.droop, atk: opt.atk, rel: opt.rel, tremDepth: opt.tremDepth });
      else if (v === 'harp') mouthHarp(musL, musR, pan, st, note, amp, hold, { kind: 'harp', detune: opt.detune, droop: opt.droop, atk: opt.atk });
      else if (v === 'whistle') mouthHarp(musL, musR, pan, st, note, amp, hold, { kind: 'whistle', detune: opt.detune, droop: opt.droop, atk: opt.atk });
      else if (v === 'pluck') pluck(musL, musR, pan, st, note, amp, { decay: opt.decay || 0.8, dur: hold * 1.4, bright: opt.bright, detune: opt.detune });
      else if (v === 'piano') piano(musL, musR, pan, st, note, amp, hold * 1.5, { detune: opt.detune });
      else if (v === 'hum') voice(musL, musR, pan, st, note, amp, hold, { vowel: opt.vowel || 'm', vibHz: opt.vibHz || 4.6, vibCents: opt.vibCents || 26, atk: opt.atk !== undefined ? opt.atk : 0.22, rel: opt.rel !== undefined ? opt.rel : 0.5, glide: opt.glide || 0 });
      else tableVoice(musL, musR, pan, st, T_STR, note, amp, 0.06, hold, 0.3, { detune: (opt.detune || 0) + 4, vibHz: 4.5, vibCents: 7 });
    });
  }
}

/* ================= 一小节的酒馆律动 (oom-pah-pah + 打击) =================
 * 6/8 → 每小节 3 个四分 = 6 个八分:
 *   e0 = 低音提琴 "oom"      e2/e4 = 手风琴/曼陀林和弦 "pah-pah"
 *   e3 = 经过低音(五度)       e5 = 半音靠近下一小节根音
 * 打击: 手鼓踩 e0/e3, 铃鼓跟 e2/e4, 木桶敲在 e5 当招牌, 跺脚只在终段.
 */
function approachNote(root, nextBass) {
  const d = nextBass - root;
  if (d > 0) return nextBass - 1;
  if (d < 0) return nextBass + 1;
  return root + 7;
}
function jigBar(bar, key, nextKey, lvl, opt) {
  opt = opt || {};
  const c = CH[key], q = 60 / bpmAt(bar);
  const det = opt.det || 0;
  // --- 低音提琴 pizzicato (walking) ---
  pizz(musL, musR, -0.05, tBeat(bar, 0), c.bass, 0.115 * lvl, q * 0.66, { detune: det, droop: opt.droop || 0 });
  if (opt.bass5) pizz(musL, musR, -0.14, tBeat(bar, 1.5), c.bass + (opt.alt ? 12 : 7), 0.072 * lvl, q * 0.34, { detune: det });
  if (opt.bassPick) pizz(musL, musR, 0.14, tBeat(bar, 2.5), approachNote(c.bass, CH[nextKey].bass), 0.062 * lvl, q * 0.30, { detune: det });
  // --- 和弦: 手风琴顿音 + 曼陀林扫弦 ---
  const comp = opt.comp || 'acc';
  const voic = opt.wide ? c.pad.concat([c.pad[0] + 12]) : c.pad;
  if (comp === 'acc' || comp === 'both') {
    [1.0, 2.0].forEach((qq, k) => {
      voic.forEach((m, i) => accordion(musL, musR, (i - 1) * 0.30, tBeat(bar, qq) + i * 0.0026, m, (k ? 0.112 : 0.126) * lvl, q * 0.40, { musette: 11, detune: det, atk: 0.028, rel: 0.12, tremDepth: 0.12 }));
    });
  }
  if (comp === 'pluck' || comp === 'both') {
    [1.0, 2.0].forEach((qq, k) => {
      strum(musL, musR, (k ? 0.22 : -0.22), tBeat(bar, qq), voic.map(m => m + (opt.strumOct || 0)), 0.045 * lvl * (k ? 0.85 : 1), { dir: k ? 'up' : 'down', decay: 0.55, dur: 0.45, detune: det });
    });
  }
  // --- 打击 ---
  const P = opt.perc !== undefined ? opt.perc : 1;
  if (P > 0) {
    // 低频几件同时砸在同一点会把瞬态叠成孤立尖峰(峰均比恶化), 故各自错开几毫秒 —— 既更真实也更稳
    if (!opt.noDrum) {
      frameDrum(dryL, dryR, tBeat(bar, 0) + 0.0025, 0.115 * lvl * P, -0.18, { freq: opt.drumF || 118 });
      frameDrum(dryL, dryR, tBeat(bar, 1.5) + 0.0025, 0.066 * lvl * P, 0.22, { freq: (opt.drumF || 118) * 1.25, bend: 0.5 });
    }
    tambourine(dryL, dryR, tBeat(bar, 1.0), 0.105 * lvl * P, -0.30);
    tambourine(dryL, dryR, tBeat(bar, 2.0), 0.098 * lvl * P, 0.32);
    if (opt.barrel) barrel(dryL, dryR, tBeat(bar, 2.5) + 0.003, 0.130 * lvl * P, opt.barrelPan || 0.25, { freq: 176 });
    if (opt.stomp) stomp(dryL, dryR, tBeat(bar, 0) + 0.0055, 0.130 * lvl, -0.08);
    if (opt.clap) { clap(ambL, ambR, tBeat(bar, 1.0), 0.12 * lvl, 0.35); clap(ambL, ambR, tBeat(bar, 2.0), 0.11 * lvl, -0.35); }
    if (opt.kick) bassDrum(dryL, dryR, tBeat(bar, 0) + 0.009, 0.095 * lvl);
    if (opt.cym && bar % 4 === 0) cymbal(dryL, dryR, tBeat(bar, 0), 0.085 * lvl, 1.4, 0.35);
  }
}

/* ================= 装配 ================= */
console.log('装配中... 总小节', BARS, '预计', TOTAL.toFixed(1), 's');

/* ---- I. 开门迎客 (A: 0-7) 手风琴引子 + 酒馆嘈杂 + 木桶敲击 ---- */
{
  const s = SEC.A.start;
  crowdMurmur(tBeat(s, 0), barDur(s) * 8.6, 0.135);
  babble(tBeat(s, 0), barDur(s) * 8.2, 0.014);
  // 木桶敲击动机: "咚 咚-咚"(迎客敲门)
  [[0, 0], [0, 1.0], [0, 1.5], [1, 0], [2, 2.5], [3, 0], [3, 2.0]].forEach(([b, q], k) =>
    barrel(dryL, dryR, tBeat(s + b, q), 0.20 - 0.02 * k, k % 2 ? 0.3 : -0.28, { freq: 168 + k * 3 }));
  glassClink(dryL, dryR, tBeat(s + 2, 1.5), 0.16, -0.35);
  glassClink(dryL, dryR, tBeat(s + 2, 1.75), 0.10, 0.30);
  // 引子: 手风琴独奏 (带低音簧), 长音版主题 + 高八度曼陀林轻点 (免得低频箱音太闷)
  playTune(TUNE_WELCOME, s, 4, { voice: 'acc', amp: 0.078, low: true, musette: 7, tremDepth: 0.10 });
  playTune(TUNE_WELCOME, s, 4, { voice: 'pluck', amp: 0.030, oct: 12, pan: 0.42, decay: 0.85 });
  playTune(TUNE_WELCOME2, s + 4, 4, { voice: 'acc', amp: 0.086, low: true, musette: 8, tremDepth: 0.12 });
  playTune(TUNE_WELCOME2, s + 4, 4, { voice: 'pluck', amp: 0.034, oct: 12, pan: 0.42, decay: 0.85 });
  for (let i = 4; i < 8; i++) {
    const b = s + i, c = CH[keyA(i)];
    pizz(musL, musR, -0.05, tBeat(b, 0), c.bass, i < 6 ? 0.085 : 0.105, barDur(b) * 0.7, {});
    if (i >= 5) pizz(musL, musR, 0.12, tBeat(b, 2.0), c.bass + 7, 0.055, barDur(b) * 0.35, {});
    if (i >= 6) strum(musL, musR, 0, tBeat(b, 1.0), c.pad, 0.030, { dir: 'down', decay: 0.5, dur: 0.4 });
    if (i >= 6) strum(musL, musR, 0, tBeat(b, 2.0), c.pad, 0.026, { dir: 'up', decay: 0.5, dur: 0.4 });
    if (i >= 5) tambourine(dryL, dryR, tBeat(b, 1.0), 0.065, -0.3);
    if (i >= 5) tambourine(dryL, dryR, tBeat(b, 2.0), 0.060, 0.3);
    if (i >= 6) frameDrum(dryL, dryR, tBeat(b, 0), 0.085, -0.18, {});
    if (i === 7) tamShake(dryL, dryR, tBeat(b, 2.5), barDur(b) * 0.5, 0.09);
  }
  tamShake(dryL, dryR, tBeat(s + 7, 2.5), barDur(s + 7) * 0.5, 0.075);
  cheer(tBeat(s + 7, 2.0), 1.6, 0.013);
}

/* ---- II. 第一轮舞曲 (B: 8-23) 吉格主歌 ---- */
{
  const s = SEC.B.start, n = SEC.B.count;
  crowdMurmur(tBeat(s, 0), barDur(s) * n, 0.125);
  babble(tBeat(s, 0), barDur(s) * (n - 2), 0.012);
  for (let i = 0; i < n; i++) {
    const b = s + i, k = keyA(i), kn = keyA(i + 1);
    jigBar(b, k, kn, i < 8 ? 0.92 : 1.0, {
      bass5: true, bassPick: i % 2 === 1, comp: 'both', barrel: i % 2 === 1, barrelPan: i % 4 ? 0.28 : -0.28,
      cym: true, kick: i % 4 === 3, wide: i >= 8, alt: i % 4 >= 2
    });
    if (i % 4 === 2) { glassClink(dryL, dryR, tBeat(b, 2.5), 0.075, i % 8 ? 0.4 : -0.4); }
    if (i === 15) tamShake(dryL, dryR, tBeat(b, 2.5), barDur(b) * 0.5, 0.085);
    if (i === 7 || i === 15) { snareRoll(dryL, dryR, tBeat(b, 2.0), barDur(b) * 0.48, 0.055); }
  }
  // 旋律: 手风琴领奏 → 口琴加入 → 后半口琴主奏
  playTune(TUNE_A, s, 8, { voice: 'acc', amp: 0.092, low: true, musette: 10, tremDepth: 0.11 });
  playTune(TUNE_A, s + 4, 4, { voice: 'pluck', amp: 0.052, oct: 12, pan: 0.36, decay: 0.7 });
  playTune(TUNE_A, s + 8, 8, { voice: 'acc', amp: 0.090, low: true, musette: 10, tremDepth: 0.11 });
  playTune(TUNE_A, s + 8, 8, { voice: 'harp', amp: 0.055, oct: 12, pan: 0.22 });
  playTune(TUNE_A2, s + 8, 8, { voice: 'acc', amp: 0.052, oct: -12, pan: -0.34, legato: 1.0 });
  playTune(TUNE_A, s + 12, 4, { voice: 'harp', amp: 0.072, pan: -0.10 });
  playTune(TUNE_A, s + 14, 2, { voice: 'whistle', amp: 0.055, oct: 12, pan: 0.30 });
  // 酒吧钢琴的即兴点缀
  for (let i = 0; i < n; i++) {
    const b = s + i, c = CH[keyA(i)];
    if (i % 4 === 1) piano(musL, musR, 0.32, tBeat(b, 1.5), c.pad[1] + 12, 0.036, barDur(b) * 0.8, {});
    if (i % 8 === 5) piano(musL, musR, 0.32, tBeat(b, 2.0), c.pad[2] + 12, 0.032, barDur(b) * 0.6, {});
  }
  cheer(tBeat(s + 8, 0), 1.4, 0.012);
}

/* ---- III. 合唱式副歌 (C: 24-39) 全奏 · 主题B · 拍手与碰杯 ---- */
{
  const s = SEC.C.start, n = SEC.C.count;
  crowdMurmur(tBeat(s, 0), barDur(s) * n, 0.150);
  babble(tBeat(s, 0), barDur(s) * (n - 2), 0.015);
  cheer(tBeat(s, 0), 2.4, 0.016);
  for (let i = 0; i < n; i++) {
    const b = s + i, k = keyB(i), kn = keyB(i + 1);
    jigBar(b, k, kn, i < 8 ? 1.02 : 1.12, {
      bass5: true, bassPick: true, comp: 'both', wide: true, alt: i % 2 === 1,
      barrel: i % 2 === 0, barrelPan: i % 4 ? -0.3 : 0.3, stomp: i >= 8, clap: i >= 4, kick: i % 2 === 1, cym: true
    });
    glassClink(dryL, dryR, tBeat(b, 0), 0.085, i % 2 ? 0.45 : -0.45);
    if (i % 8 === 7) { snareRoll(dryL, dryR, tBeat(b, 2.0), barDur(b) * 0.48, 0.07); tamShake(dryL, dryR, tBeat(b, 2.0), barDur(b) * 0.48, 0.09); }
    if (i === 15) laughter(tBeat(b, 2.5), 0.030);
  }
  playTune(TUNE_B, s, 8, { voice: 'harp', amp: 0.078, low: false });
  playTune(TUNE_B, s, 8, { voice: 'acc', amp: 0.070, low: true, musette: 12, tremDepth: 0.12 });
  playTune(TUNE_B, s + 8, 8, { voice: 'harp', amp: 0.082, oct: 12 });
  playTune(TUNE_B, s + 8, 8, { voice: 'acc', amp: 0.074, low: true, musette: 12, tremDepth: 0.12 });
  playTune(TUNE_B, s + 8, 8, { voice: 'piano', amp: 0.042, oct: -12, pan: -0.40 });
  playTune(TUNE_A2, s + 8, 8, { voice: 'pluck', amp: 0.040, oct: 12, pan: 0.45, decay: 0.6 });
  playTune(TUNE_B, s + 12, 4, { voice: 'whistle', amp: 0.052, oct: 12, pan: 0.28 });
  cheer(tBeat(s + 8, 0), 2.0, 0.017);
}

/* ---- IV. 醉意间奏 (D: 40-49) 只剩拨弦与哼唱, 速度微飘, 音高游移 ---- */
{
  const s = SEC.D.start, n = SEC.D.count;
  DRUNK = 1;
  crowdMurmur(tBeat(s, 0), barDur(s) * n, 0.105);
  babble(tBeat(s, 0), barDur(s) * (n - 1), 0.010);
  for (let i = 0; i < n; i++) {
    const b = s + i, k = PROG_D[i];
    const c = CH[k];
    const q = 60 / bpmAt(b);
    // 曼陀林轮指琶音 (唯一的"和声")
    const arp = [c.pad[0], c.pad[1], c.pad[2], c.pad[1] + 12];
    mandTrem(musL, musR, i % 2 ? 0.34 : -0.30, tBeat(b, 0), barDur(b) * 0.95, arp, 0.042, { step: 0.070 + 0.012 * Math.sin(i * 2.1), detune: i % 3 ? 9 : -12 });
    // 低音提琴: 踉跄的 walking (拖拍/抢拍/走音/下滑)
    const late = (i % 3 === 0) ? 0.16 : (i % 3 === 1 ? -0.10 : 0.04);
    pizz(musL, musR, -0.06, tBeat(b, 0) + late, c.bass, 0.098, q * 0.75, { detune: (i % 2 ? 14 : -18), droop: -55 });
    if (i % 2 === 0) pizz(musL, musR, 0.16, tBeat(b, 1.5) + late * 1.6, c.bass + (i % 4 === 0 ? 7 : 6), 0.058, q * 0.4, { detune: i % 3 ? -20 : 16, droop: -40 });
    if (i % 4 === 3) pizz(musL, musR, 0.10, tBeat(b, 2.5), approachNote(c.bass, CH[PROG_D[Math.min(n - 1, i + 1)]].bass), 0.052, q * 0.3, { detune: 22 });
    // 稀落的打击: 木桶与手鼓像"醉汉敲桌子"
    if (i % 2 === 1) barrel(dryL, dryR, tBeat(b, 2.5) + 0.03, 0.115, i % 4 ? 0.32 : -0.32, { freq: 162, detune: -30 });
    if (i % 3 === 2) frameDrum(dryL, dryR, tBeat(b, 1.5) + 0.05, 0.070, -0.26, { freq: 104 });
    if (i === 5) glassClink(dryL, dryR, tBeat(b, 2.0), 0.075, 0.42);
  }
  // 哼唱 (g 小调主题, 带下滑与走音)
  playTune(TUNE_DRUNK, s, 10, { voice: 'hum', amp: 0.080, vowel: 'm', vibHz: 4.4, vibCents: 30, atk: 0.24, rel: 0.6, glide: -0.6 });
  playTune(TUNE_DRUNK, s + 4, 6, { voice: 'acc', amp: 0.040, oct: -12, pan: -0.4, droop: -70, musette: 26, legato: 0.95 });
  // 酒魔: 低音三全音潜行 (8-9 小节处浮现, 再被木桶敲醒)
  subGrowl(tBeat(s + 7, 0), barDur(s + 7) * 1.5, 31, 0.048);
  subGrowl(tBeat(s + 8, 0), barDur(s + 8) * 1.9, 30, 0.058);
  laughter(tBeat(s + 6, 2.5), 0.022);
  // 醒酒重击: 木桶 + 跺脚 + 手风琴刺 (末小节最后一个八分)
  barrel(dryL, dryR, tBeat(s + 9, 2.5), 0.30, 0, { freq: 176 });
  stomp(dryL, dryR, tBeat(s + 9, 2.5), 0.26, 0);
  [62, 67, 70, 74].forEach((m, i) => accordion(musL, musR, -0.4 + i * 0.28, tBeat(s + 9, 2.5), m, 0.070, 0.42, { musette: 14, atk: 0.02, rel: 0.16 }));
  DRUNK = 0;
}

/* ---- V. 更快的终段 (E: 50-77) 全场跺脚/碰杯/跟唱 ---- */
{
  const s = SEC.E.start, n = SEC.E.count;
  crowdMurmur(tBeat(s, 0), barDur(s) * n, 0.170);
  babble(tBeat(s, 0), barDur(s) * (n - 2), 0.017);
  cheer(tBeat(s, 0), 2.6, 0.019);
  for (let i = 0; i < n; i++) {
    const b = s + i;
    const k = i < 24 ? keyA(i) : PROG_TAG[i - 24];
    const kn = i < 23 ? keyA(i + 1) : PROG_TAG[Math.min(3, Math.max(0, i + 1 - 24))];
    jigBar(b, k, kn, i < 8 ? 1.08 : (i < 16 ? 1.12 : (i < 24 ? 1.16 : 1.22)), {
      bass5: true, bassPick: true, comp: 'both', wide: true, alt: i % 2 === 1,
      barrel: i % 2 === 1, barrelPan: i % 4 ? 0.32 : -0.32, stomp: true, clap: true, kick: i % 4 === 3, cym: true, strumOct: 0
    });
    if (i % 4 === 0) { glassClink(dryL, dryR, tBeat(b, 0), 0.090, i % 8 ? 0.5 : -0.5); glassClink(dryL, dryR, tBeat(b, 0.25), 0.055, i % 8 ? -0.36 : 0.36); }
    if (i % 8 === 7) { snareRoll(dryL, dryR, tBeat(b, 2.0), barDur(b) * 0.48, 0.075); tamShake(dryL, dryR, tBeat(b, 2.0), barDur(b) * 0.48, 0.10); }
    if (i === 15 || i === 23) laughter(tBeat(b, 2.5), 0.030);
    if (i === 24) cheer(tBeat(b, 0), 2.2, 0.018);
  }
  // 50-57 主题A (全奏, 口琴领奏 + 手风琴八度)
  playTune(TUNE_A, s, 8, { voice: 'harp', amp: 0.084 });
  playTune(TUNE_A, s, 8, { voice: 'acc', amp: 0.068, oct: 12, pan: 0.26, low: false, musette: 10 });
  playTune(TUNE_A2, s, 8, { voice: 'acc', amp: 0.052, oct: -12, pan: -0.36, legato: 1.0 });
  // 58-65 主题B (副歌回归, 更大)
  playTune(TUNE_B, s + 8, 8, { voice: 'harp', amp: 0.086 });
  playTune(TUNE_B, s + 8, 8, { voice: 'acc', amp: 0.074, low: true, musette: 12 });
  playTune(TUNE_B, s + 8, 8, { voice: 'pluck', amp: 0.045, oct: 12, pan: 0.46, decay: 0.6 });
  // 66-73 主题A 变奏 + 口琴/手风琴对唱
  playTune(TUNE_A2, s + 16, 8, { voice: 'harp', amp: 0.086, oct: 12 });
  playTune(TUNE_A, s + 16, 8, { voice: 'acc', amp: 0.078, low: true, musette: 12 });
  playTune(TUNE_A, s + 18, 6, { voice: 'whistle', amp: 0.056, oct: 12, pan: 0.38 });
  playTune(TUNE_B, s + 16, 8, { voice: 'piano', amp: 0.040, oct: -12, pan: -0.46 });
  // 74-77 尾声走句 (加速冲线)
  playTune(TUNE_TAG, s + 24, 4, { voice: 'harp', amp: 0.086, legato: 0.85 });
  playTune(TUNE_TAG, s + 24, 4, { voice: 'acc', amp: 0.066, oct: 12, pan: 0.30, legato: 0.85, musette: 13 });
  playTune(TUNE_TAG, s + 24, 4, { voice: 'pluck', amp: 0.046, oct: -12, pan: -0.34, decay: 0.5 });
  for (let i = 24; i < 28; i++) {
    const b = s + i;
    snareRoll(dryL, dryR, tBeat(b, 0), barDur(b) * 0.97, 0.070 + (i - 24) * 0.008);
    tamShake(dryL, dryR, tBeat(b, 0), barDur(b) * 0.97, 0.085 + (i - 24) * 0.010);
    if (i === 27) { bassDrum(dryL, dryR, tBeat(b, 2.5), 0.42); stomp(dryL, dryR, tBeat(b, 2.5), 0.30, 0); }
  }
  // 终段里的"酒魔": 每 8 小节一次低音不谐和闪现
  subGrowl(tBeat(s + 11, 0), 1.1, 30, 0.042);
  subGrowl(tBeat(s + 19, 0), 1.1, 31, 0.044);
}

/* ---- VI. 收尾 (F: 78-85) 碰杯欢笑 + 杯子碎裂 + 尾混响 ---- */
{
  const s = SEC.F.start, n = SEC.F.count;
  crowdMurmur(tBeat(s, 0), barDur(s) * n, 0.190);
  babble(tBeat(s, 0), barDur(s) * 4.5, 0.018);
  // 78: 收束巨击 (全员)
  {
    const b = s, c = CH.G, q = 60 / bpmAt(b);
    [59, 62, 67, 71, 74].forEach((m, i) => {
      accordion(musL, musR, -0.5 + i * 0.25, tBeat(b, 0), m, 0.072, q * 1.2, { musette: 13, low: i === 0, atk: 0.02, rel: 0.5 });
      pluck(musL, musR, -0.5 + i * 0.25, tBeat(b, 0), m, 0.045, { decay: 1.2, dur: 1.1 });
    });
    strum(musL, musR, 0.2, tBeat(b, 0), c.pad.map(m => m + 12), 0.050, { decay: 1.0, dur: 1.0 });
    pizz(musL, musR, 0, tBeat(b, 0), c.bass, 0.120, q * 1.2, {});
    bassDrum(dryL, dryR, tBeat(b, 0), 0.20);
    stomp(dryL, dryR, tBeat(b, 0), 0.17, 0);
    frameDrum(dryL, dryR, tBeat(b, 0), 0.13, -0.2, {});
    barrel(dryL, dryR, tBeat(b, 0), 0.15, 0.25, { freq: 172 });
    cymbal(dryL, dryR, tBeat(b, 0), 0.13, 2.6, 0.25);
    glassClink(dryL, dryR, tBeat(b, 0.5), 0.14, -0.4);
    cheer(tBeat(b, 0), 2.4, 0.020);
  }
  // 79-81: 尾声短句 (G-F-C-G)
  const TAILTUNE = [
    [[0, 74, 1.5], [1.5, 71, 1.5], [3, 67, 3]],
    [[0, 72, 1.5], [1.5, 69, 1.5], [3, 65, 3]],
    [[0, 67, 1.5], [1.5, 72, 1.5], [3, 76, 3]],
    [[0, 74, 3], [3, 67, 3]]
  ];
  playTune(TAILTUNE, s + 1, 4, { voice: 'acc', amp: 0.080, low: true, musette: 10, tremDepth: 0.12 });
  playTune(TAILTUNE, s + 1, 4, { voice: 'pluck', amp: 0.046, pan: 0.40, decay: 0.9, oct: 12 });
  // 79-81 的和声必须跟着尾句走: G - F - C - G (与 TAILTUNE 的琶音一一对应)
  for (let i = 0; i < 4; i++) {
    const b = s + 1 + i, c = CH[PROG_F[i]];
    pizz(musL, musR, -0.05, tBeat(b, 0), c.bass, 0.10, barDur(b) * 0.75, {});
    if (i % 2 === 1) strum(musL, musR, -0.2, tBeat(b, 1.0), c.pad, 0.036, { decay: 0.8, dur: 0.7 });
    frameDrum(dryL, dryR, tBeat(b, 0), 0.075, -0.2, {});
    tambourine(dryL, dryR, tBeat(b, 2.0), 0.070, 0.3);
    if (i === 3) laughter(tBeat(b, 1.0), 0.034);
  }
  glassClink(dryL, dryR, tBeat(s + 2, 1.0), 0.12, -0.42);
  glassClink(dryL, dryR, tBeat(s + 2, 1.25), 0.09, 0.38);
  glassClink(dryL, dryR, tBeat(s + 3, 2.0), 0.11, 0.20);
  laughter(tBeat(s + 3, 0.5), 0.038);
  // 82: 杯子碎裂
  glassBreak(dryL, dryR, tBeat(s + 4, 0), 0.52);
  glassClink(dryL, dryR, tBeat(s + 4, 0.25), 0.13, -0.3);
  laughter(tBeat(s + 4, 1.5), 0.042);
  cheer(tBeat(s + 4, 0), 2.0, 0.038);
  // 83-85: 余音 (手风琴最后一个和弦 + 低音 + 零星碰杯)
  {
    const b = s + 5;
    [62, 67, 71, 74].forEach((m, i) => accordion(musL, musR, -0.45 + i * 0.3, tBeat(b, 0), m, 0.062, barDur(b) * 2.4, { musette: 9, low: i === 0, atk: 0.10, rel: 1.6, tremDepth: 0.14 }));
    pizz(musL, musR, 0, tBeat(b, 0), 31, 0.095, barDur(b) * 1.6, {});
    frameDrum(dryL, dryR, tBeat(b, 0), 0.070, -0.2, {});
    glassClink(dryL, dryR, tBeat(s + 6, 0.5), 0.085, 0.44);
    glassClink(dryL, dryR, tBeat(s + 7, 1.0), 0.070, -0.40);
    laughter(tBeat(s + 6, 1.0), 0.028);
    babble(tBeat(s + 6, 0), barDur(s + 6) * 2.6, 0.015);
  }
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
  sendL[i] = musL[i - PRE] * 0.34 + dryL[i - PRE] * 0.13 + ambL[i - PRE] * 0.16;
  sendR[i] = musR[i - PRE] * 0.34 + dryR[i - PRE] * 0.13 + ambR[i - PRE] * 0.16;
}
const revL = reverbChannel(sendL, 0, 0.80, 0.30, 0.05);   // 木屋小厅: 比沙耶短、比木偶暖
const revR = reverbChannel(sendR, 27, 0.80, 0.30, 0.05);
const rmsOf = a => { let s = 0; for (let i = 0; i < a.length; i++) s += a[i] * a[i]; return Math.sqrt(s / a.length); };
const bedRms = rmsOf(musL), percRms = rmsOf(dryL), ambRms = rmsOf(ambL);
const dryRms = Math.sqrt((bedRms ** 2 + percRms ** 2 + ambRms ** 2) / 3);
const wetRms = Math.sqrt((rmsOf(revL) ** 2 + rmsOf(revR) ** 2) / 2);
const wetGain = wetRms > 0 ? Math.min(2.0, dryRms * 0.34 / wetRms) : 0;
console.log('wetGain', wetGain.toFixed(3), 'busRms 床=' + bedRms.toFixed(4), '打击=' + percRms.toFixed(4), '人群=' + ambRms.toFixed(4));
const peakOf = a => { let p = 0, ti = 0; for (let i = 0; i < a.length; i++) { const v = Math.abs(a[i]); if (v > p) { p = v; ti = i; } } return p.toFixed(3) + '@' + (ti / SR).toFixed(2); };
console.log('busPeak mus=' + peakOf(musL) + ' dry=' + peakOf(dryL) + ' amb=' + peakOf(ambL) + ' rev=' + peakOf(revL));

const outL = new Float32Array(N), outR = new Float32Array(N);
for (let i = 0; i < N; i++) {
  outL[i] = musL[i] + dryL[i] + ambL[i] + revL[i] * wetGain;
  outR[i] = musR[i] + dryR[i] + ambR[i] + revR[i] * wetGain;
}
const fadeIn = S(0.5), fadeOut = S(6.0);for (let i = 0; i < N; i++) {
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
// 酒馆: 低音提琴(50-200Hz)要清楚但不能糊, 手风琴中频要有"箱音", 碰杯/铃鼓的高频要亮
// (高频提升刻意保守: 2600/5200 的 peaking 会在"全奏落点"上叠加出孤立尖峰, 把整首的峰均比拖坏)
const EQ_CHAIN = [hpCoeff(34, 0.707), peakCoeff(105, 0.9, -3.6), peakCoeff(240, 1.1, -2.2), peakCoeff(700, 1.0, -1.0), peakCoeff(2600, 0.9, 0.5), peakCoeff(5200, 0.9, 0.4), shelfCoeff(9200, 0.4)];
let eqL = outL, eqR = outR;
for (const c of EQ_CHAIN) { eqL = biquadRun(eqL, c); eqR = biquadRun(eqR, c); }

let peak = 0, peakI = 0;
for (let i = 0; i < N; i++) {
  const a = Math.abs(eqL[i]); if (a > peak) { peak = a; peakI = i; }
  const b = Math.abs(eqR[i]); if (b > peak) { peak = b; peakI = i; }
}
// 与《茶の唄》同样保守: 归一化目标 0.75 而不是 1.5, 让 tanh 只当兜底安全阀,
// 响度交给最后的 RMS 归一化 —— 这样 satSamples 才可能 ≈0.
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
const TARGET_RMS = 0.100;
const trim = rms0 > 1e-6 ? TARGET_RMS / rms0 : 1;
for (let i = 0; i < N; i++) { eqL[i] *= trim; eqR[i] *= trim; }
// 兜底: RMS 归一化可能把孤立瞬态(碎杯/碰杯)推过 0dBFS → 会削波; 这里只做极小让步
let fp3 = 0;
for (let i = 0; i < N; i++) { const a = Math.abs(eqL[i]); if (a > fp3) fp3 = a; const b = Math.abs(eqR[i]); if (b > fp3) fp3 = b; }
if (fp3 > 0.985) { const g2 = 0.985 / fp3; for (let i = 0; i < N; i++) { eqL[i] *= g2; eqR[i] *= g2; } }
let _s = 0, finalPeak = 0;
for (let i = 0; i < N; i++) {
  _s += eqL[i] * eqL[i] + eqR[i] * eqR[i];
  const a = Math.abs(eqL[i]); if (a > finalPeak) finalPeak = a;
  const b = Math.abs(eqR[i]); if (b > finalPeak) finalPeak = b;
}
const finalRms = Math.sqrt(_s / (2 * N));

const outDir = process.argv[3] || 'out';
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, process.argv[2] || 'tavern.wav');
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
console.log(JSON.stringify({ file: outFile, seconds: +(N / SR).toFixed(2), bars: BARS, sections: Object.keys(SEC).map(k => `${k}:${SEC[k].start}+${SEC[k].count}`).join(' '), peak: +peak.toFixed(3), peakAt: +(peakI / SR).toFixed(2), finalPeak: +finalPeak.toFixed(3), satSamples: sat, wetGain: +wetGain.toFixed(3), finalRms: +finalRms.toFixed(4), crestDb: +(20 * Math.log10(peak / Math.max(1e-9, rmsOf(outL)))).toFixed(1), busRms: { bed: +bedRms.toFixed(4), perc: +percRms.toFixed(4), amb: +ambRms.toFixed(4) }, profile: rows }, null, 0));
