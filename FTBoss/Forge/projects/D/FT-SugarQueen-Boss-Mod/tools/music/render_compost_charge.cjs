/* ============================================================================
 *  炫彩答辩超人 BGM (2026-09-12 重写): 参考《心灵终结》苏军战役曲风
 *  —— Chitzkalla 那种**高速小调军工电子**: 四踩底鼓 + 军鼓滚奏 + 失真贝斯
 *     + 铜管重音 + 弦乐/合唱垫, 而不是原来那首 76 BPM 的钢琴抒情曲。
 *
 *  旋律/和声都是**原创**(只借曲风, 不复制原曲素材)。
 *  一阶段: 158 BPM, D 小调(带和声小调升七级) ; 二阶段: 176 BPM, 升 2 个半音 + 加厚。
 *
 *  用法: node render_compost_charge.cjs <modRoot>
 *  产物: assets/ftboss/sounds/music/compost.ogg, compost_p2.ogg
 *  (sounds.json / FTSounds / lang 早已注册, 这里只重渲染音频本体)
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const { encode } = require(path.join(ROOT, 'tools/music/wav2ogg.cjs'));

const SR = 44100;
let N = 0, L = null, R = null;
let rnd = () => 0;
const hz = (m) => 440 * Math.pow(2, (m - 69) / 12);
const saw = (p) => 2 * (p - Math.floor(p + 0.5));
const sqr = (p) => (p - Math.floor(p) < 0.5 ? 1 : -1);

/** 等功率声像: pan 0..1 */
function voice(t0, dur, pan, gen, gain) {
  const i0 = Math.max(0, Math.floor(t0 * SR));
  const i1 = Math.min(N, Math.floor((t0 + dur) * SR));
  const a = pan * Math.PI / 2;
  const gl = Math.cos(a) * gain, gr = Math.sin(a) * gain;
  for (let i = i0; i < i1; i++) {
    const v = gen((i - i0) / SR);
    L[i] += v * gl;
    R[i] += v * gr;
  }
}

/* ---------------------------- 鼓组 ---------------------------- */
function kick(t0, amp = 1) {
  voice(t0, 0.30, 0.5, (t) => {
    const f = 44 + 118 * Math.exp(-t * 40);
    const body = Math.sin(2 * Math.PI * f * t) * Math.exp(-t * 12);
    const click = t < 0.010 ? rnd() * 0.55 * (1 - t / 0.010) : 0;
    return Math.tanh((body + click) * 1.4);
  }, 0.95 * amp);
}
function snare(t0, amp = 1) {
  voice(t0, 0.24, 0.5, (t) => {
    const env = Math.exp(-t * 24);
    const body = Math.sin(2 * Math.PI * 186 * t) * Math.exp(-t * 32) * 0.45;
    return (rnd() * 0.85 + body) * env;
  }, 0.72 * amp);
}
function hat(t0, amp = 1, open = false) {
  const d = open ? 0.19 : 0.048;
  let prev = 0;
  voice(t0, d, 0.62, (t) => {
    const n = rnd();
    const hp = n - prev;             // 一阶差分 ≈ 高通, 去掉低频做成"镲"
    prev = n;
    return hp * Math.exp(-t * (open ? 13 : 58));
  }, 0.30 * amp);
}
function crash(t0, amp = 1) {
  let prev = 0;
  voice(t0, 1.1, 0.5, (t) => {
    const n = rnd();
    const hp = n - prev; prev = n;
    return hp * Math.exp(-t * 3.2);
  }, 0.34 * amp);
}

/* ---------------------------- 音色 ---------------------------- */
function bassNote(t0, midi, dur, amp) {
  const f = hz(midi);
  let p1 = 0, p2 = 0;
  voice(t0, dur, 0.5, (t) => {
    p1 += f / SR; p2 += f * 1.006 / SR;
    const s = saw(p1) * 0.62 + sqr(p2) * 0.38;
    const env = Math.min(1, t * 320) * Math.min(1, (dur - t) * 70) * (0.55 + 0.45 * Math.exp(-t * 2.4));
    return Math.tanh(s * 3.4) * env;          // 失真贝斯
  }, amp);
}
function brass(t0, midis, dur, amp) {
  for (const m of midis) {
    const f = hz(m);
    const det = [0.9935, 1.0, 1.0075];
    const ph = [0, 0.3, 0.6];
    voice(t0, dur, 0.5, (t) => {
      let s = 0;
      for (let k = 0; k < 3; k++) { ph[k] += f * det[k] / SR; s += saw(ph[k]); }
      const env = Math.min(1, t * 95) * Math.exp(-t * 2.6) * Math.min(1, (dur - t) * 45);
      return Math.tanh((s / 3) * 1.5) * env;
    }, amp);
  }
}
function lead(t0, midi, dur, amp) {
  const f = hz(midi);
  let p1 = 0, p2 = 0;
  voice(t0, dur, 0.6, (t) => {
    const vib = 1 + 0.006 * Math.sin(2 * Math.PI * 5.5 * t) * Math.min(1, t * 7);
    p1 += (f * vib) / SR; p2 += (f * vib * 0.5) / SR;
    const s = sqr(p1) * 0.55 + saw(p2) * 0.45;
    const env = Math.min(1, t * 140) * Math.exp(-t * 0.85) * Math.min(1, (dur - t) * 55);
    return Math.tanh(s * 2.1) * env;
  }, amp);
}
function strings(t0, midis, dur, amp) {
  for (const m of midis) {
    const f = hz(m);
    let p1 = 0, p2 = 0;
    voice(t0, dur, 0.5, (t) => {
      p1 += f / SR; p2 += (f * 1.004) / SR;
      const env = Math.min(1, t / 0.45) * Math.min(1, (dur - t) / 0.55);
      return (saw(p1) + saw(p2)) * 0.5 * env;
    }, amp);
  }
}
function choir(t0, midi, dur, amp) {
  const f = hz(midi);
  let p = 0;
  voice(t0, dur, 0.5, (t) => {
    p += f / SR;
    const trem = 1 + 0.12 * Math.sin(2 * Math.PI * 5.0 * t);
    const s = Math.sin(2 * Math.PI * p) + 0.34 * Math.sin(4 * Math.PI * p) + 0.16 * Math.sin(6 * Math.PI * p);
    const env = Math.min(1, t / 0.55) * Math.min(1, (dur - t) / 0.7);
    return s * trem * env / 1.5;
  }, amp);
}
function riser(t0, dur, amp) {
  let prev = 0, p = 0;
  voice(t0, dur, 0.5, (t) => {
    const n = rnd();
    const hp = n - prev; prev = n;
    p += (220 + 900 * (t / dur)) / SR;
    const env = Math.pow(t / dur, 1.6) * Math.min(1, (dur - t) * 30);
    return (hp * 0.8 + sqr(p) * 0.25) * env;
  }, amp);
}

/* ---------------------------- 和声 ---------------------------- */
/* 相对主音(D)的半音偏移 */
const CH = {
  Dm: { bass: 0, notes: [0, 3, 7] },
  Bb: { bass: 8, notes: [8, 12, 15] },
  F: { bass: 3, notes: [3, 7, 10] },
  C: { bass: 10, notes: [10, 14, 17] },
  Gm: { bass: 5, notes: [5, 8, 12] },
  A: { bass: 7, notes: [7, 11, 14] },      // 和声小调的属和弦(升七级)
  Eb: { bass: 1, notes: [1, 5, 8] },       // 那不勒斯色彩
};
const PROG_A = ['Dm', 'Dm', 'Bb', 'C', 'Dm', 'Dm', 'Gm', 'A'];
const PROG_B = ['F', 'C', 'Dm', 'Bb', 'Gm', 'A', 'Dm', 'Dm'];
const PROG_BR = ['Bb', 'F', 'C', 'Dm', 'Eb', 'Bb', 'A', 'A'];
const PROG_BREAK = ['Dm', 'Dm', 'Gm', 'A'];

/* 原创 4 小节主题动机 (D 和声小调: D E F G A Bb C#) —— 相对 D5(74) 的半音偏移 */
const MOTIF = [
  [-5, 0, 3, 2], [0, -5, -4, -1], [0, 3, 7, 5], [3, 2, 0, -5],
];
/* 贝斯 16 分驱动型(相对根音半音) */
const BASS_16 = [0, 0, 12, 0, 10, 0, 12, 0, 0, 0, 3, 0, 5, 3, 0, 0];

/* ---------------------------- 编曲 ---------------------------- */
function render(cfg) {
  const { bpm, transpose, phase, durSec } = cfg;
  const SPB = 60 / bpm;
  const BAR = 4 * SPB;
  const BARS = Math.ceil(durSec / BAR);
  N = Math.floor((BARS * BAR + 2.5) * SR);
  L = new Float32Array(N); R = new Float32Array(N);
  let seed = 20260912 + phase * 977;
  rnd = () => ((seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff) * 2 - 1;

  const T = transpose;                       // 移调
  const R0 = 38 + T;                         // 贝斯根音 D2
  const LEAD = 74 + T;                       // 主题中心 D5
  const song = [];                           // 每小节: {chord, sect}
  const push = (names, sect) => names.forEach((c) => song.push({ c, sect }));
  /* 段落排布: 前奏 4 | A 8 | A2 8 | 间奏 4 | B 8 | 桥段 8 | 高潮 12 | 尾奏 4 */
  push(['Dm', 'Dm', 'Bb', 'C'], 'intro');
  push(PROG_A, 'A');
  push(PROG_A, 'A2');
  push(PROG_BREAK, 'break');
  push(PROG_B, 'B');
  push(PROG_BR, 'bridge');
  push(PROG_A, 'climax');
  push(['Dm', 'Gm', 'A', 'Dm'], 'climax');
  push(['Dm', 'Dm', 'Dm', 'Dm'], 'outro');

  const intense = phase === 2;
  for (let bar = 0; bar < song.length; bar++) {
    const t0 = bar * BAR;
    const { c, sect } = song[bar];
    const chord = CH[c];
    const isIntro = sect === 'intro', isBreak = sect === 'break';
    const isBridge = sect === 'bridge', isClimax = sect === 'climax', isOutro = sect === 'outro';
    const drums = !isIntro || bar >= 2;

    /* --- 鼓 --- */
    if (drums) {
      const four = !isBreak;
      for (let b = 0; b < 4; b++) {
        if (four || b === 0) kick(t0 + b * SPB, b === 0 ? 1 : 0.9);
      }
      if (intense || isClimax) {                       // 二阶段/高潮: 加 16 分补踩
        for (let s = 0; s < 16; s++) if (s % 4 === 2) kick(t0 + s * SPB / 4, 0.42);
      }
      snare(t0 + SPB, 1); snare(t0 + 3 * SPB, 1);
      const roll = isBreak || (bar % 8 === 7) || (intense && bar % 4 === 3);
      if (roll) {                                      // 军鼓滚奏(军工味)
        for (let s = 0; s < 8; s++) snare(t0 + (2 + s * 0.25) * SPB, 0.34 + s * 0.07);
      }
      const hatStep = (intense || isClimax) ? 0.25 : 0.5;
      for (let s = 0; s < 4; s += hatStep) {
        hat(t0 + s * SPB, s % 1 === 0 ? 0.85 : 0.6, s % 0.5 !== 0);
      }
      if (bar % 8 === 0 && !isIntro) crash(t0, 1);
    }

    /* --- 贝斯: 16 分驱动(间奏停, 前奏只留根音长音) --- */
    if (!isIntro && !isBreak && !isOutro) {
      for (let s = 0; s < 16; s++) {
        const off = BASS_16[s];
        bassNote(t0 + s * SPB / 4, R0 + chord.bass + off, SPB / 4 * 0.92, off === 0 ? 0.30 : 0.22);
      }
    } else if (isIntro) {
      bassNote(t0, R0 + chord.bass, SPB * 1.8, 0.26);
      bassNote(t0 + 2 * SPB, R0 + chord.bass, SPB * 1.8, 0.24);
    } else if (isOutro) {
      bassNote(t0, R0 + chord.bass, SPB * 3.6, 0.30);
    }

    /* --- 铜管重音: 反拍切分 --- */
    if (!isIntro && !isBreak) {
      const hits = isClimax || intense ? [0, 1.5, 2, 3.5] : [1.5, 3.5];
      for (const h of hits) brass(t0 + h * SPB, chord.notes.map((n) => 50 + T + n), SPB * 0.55, 0.16);
    }

    /* --- 弦乐/合唱垫 --- */
    if (isBridge || isClimax || isBreak || isOutro) {
      const amp = isClimax ? 0.055 : 0.045;
      strings(t0, chord.notes.map((n) => 38 + T + n), BAR, amp);
      if (isBridge || isClimax) choir(t0, 50 + T + chord.notes[2], BAR, 0.035);
    }

    /* --- 主题动机(二阶段/高潮整体升八度) --- */
    const leadOn = sect === 'A2' || sect === 'B' || sect === 'bridge' || sect === 'climax';
    if (leadOn) {
      const oct = (isClimax || intense) ? 12 : 0;
      const phrase = MOTIF[(bar - 4) & 3] || MOTIF[bar & 3];
      for (let i = 0; i < 4; i++) {
        const t = t0 + i * SPB;
        const m = LEAD + oct + phrase[i];
        lead(t, m, SPB * 0.95, i === 0 ? 0.20 : 0.165);
        if (isClimax || intense) lead(t, m - 12, SPB * 0.95, 0.07);   // 低八度加厚
      }
    }

    /* --- 桥段前的张力爬升 --- */
    if (sect === 'break' && bar % 4 === 3) riser(t0, BAR, 0.14);
  }

  /* 软削波 + 归一 */
  let peak = 0;
  for (let i = 0; i < N; i++) {
    L[i] = Math.tanh(L[i] * 1.1); R[i] = Math.tanh(R[i] * 1.1);
    peak = Math.max(peak, Math.abs(L[i]), Math.abs(R[i]));
  }
  const g = 0.80 / (peak || 1);          // 留足余量: Vorbis 编码后峰值会略微过冲
  for (let i = 0; i < N; i++) { L[i] *= g; R[i] *= g; }
  return { seconds: N / SR, bars: song.length };
}

/* ---------------------------- 输出 ---------------------------- */
const musicDir = path.join(ROOT, 'src/main/resources/assets/ftboss/sounds/music');
fs.mkdirSync(musicDir, { recursive: true });

const jobs = [
  { file: 'compost.ogg', title: 'Compost Assault', bpm: 158, transpose: 0, phase: 1, durSec: 86 },
  { file: 'compost_p2.ogg', title: 'Compost Meltdown', bpm: 176, transpose: 2, phase: 2, durSec: 82 },
];
for (const j of jobs) {
  const info = render(j);
  const ogg = encode({ channels: 2, sampleRate: SR, frames: N, data: [L, R] }, 0.15,
    { TITLE: j.title, ARTIST: 'FTBoss' });
  fs.writeFileSync(path.join(musicDir, j.file), ogg);
  console.log(`${j.file}  ${(ogg.length / 1024).toFixed(0)} KB  ${info.seconds.toFixed(1)}s  ` +
    `${info.bars} 小节  ${j.bpm} BPM  移调 ${j.transpose >= 0 ? '+' : ''}${j.transpose}  《${j.title}》`);
}
