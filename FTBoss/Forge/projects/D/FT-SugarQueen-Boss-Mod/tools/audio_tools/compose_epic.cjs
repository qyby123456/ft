/* ============================================================================
 *  《Forge 的祝福 · 史诗战斗曲》—— 20 分钟交响化战斗 BGM 生成器
 *  (2026-09-13 v82 按用户要求"开始作曲，大约20分钟，激昂的史诗战斗音乐")
 *
 *  纯 Node 实现: 自己写振荡器/包络/混响, 用 wasm-media-encoders 流式编码 Ogg Vorbis
 *  (机器上没有 ffmpeg; 该包是纯 WASM, 不依赖联网下载二进制)。
 *
 *  作曲结构(1200 秒, D 小调为主, 皮卡迪三度收尾):
 *    0:00 序章     76 BPM  Dm   低音持续 + 定音鼓滚奏 + 弦乐渐强 + 远号
 *    1:20 主题 A  132 BPM  Dm   主旋律(铜管) + 弦乐八分音符 + 战鼓
 *    3:30 发展 B  132 BPM  F    关系大调明亮变奏 + 合唱垫 + 铃
 *    6:00 再现 A' 138 BPM  Dm   全奏 + 对位副旋律
 *    8:30 危机 C  100 BPM  Gm   半音下行 + 不安定音鼓 + 铜管重击
 *   10:30 高潮    144 BPM  Dm   最大动态 + 双倍鼓点 + 合唱/铃/号角
 *   13:00 间歇    120 BPM  Bb   弦乐琶音 + 独奏
 *   15:00 终局    144 BPM  Dm   主题最后一次全奏
 *   18:00 尾声     90→60   Dm→D 宏大终止 + 长和弦淡出
 *
 *  用法: node tools/audio_tools/compose_epic.cjs [输出ogg路径]
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const { createOggEncoder } = require('wasm-media-encoders');

const SR = 44100;
const TOTAL_SECONDS = 20 * 60;              // 20 分钟
const OUT = process.argv[2] || path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'assets', 'ftboss', 'sounds', 'music', 'manor_epic.ogg');
const SEED = 20260913;
/** 总线增益(见 Bus.finish 的说明): 第一版峰值只有 0.30 太小, 这里整体抬到接近满刻度 */
const MASTER_GAIN = 2.9;

/* ---------------- 工具 ---------------- */
let seed = SEED;
function rnd() { seed = (seed * 1664525 + 1013904223) >>> 0; return seed / 4294967296; }

/** 音名 -> 频率(十二平均律, A4=440) */
const NOTE_BASE = { C: -9, D: -7, E: -5, F: -4, G: -2, A: 0, B: 2 };
function freq(name, octave) {
    const m = /^([A-G])([#b]?)/.exec(name);
    let semi = NOTE_BASE[m[1]] + (m[2] === '#' ? 1 : m[2] === 'b' ? -1 : 0);
    semi += (octave - 4) * 12;
    return 440 * Math.pow(2, semi / 12);
}

/* ---------------- 混响(Schroeder: 4 comb + 2 allpass, 立体声尾) ---------------- */
class Reverb {
    constructor() {
        this.combTune = [1557, 1617, 1491, 1422].map((n) => Math.round(n * SR / 44100));
        this.comb = this.combTune.map((n) => new Float32Array(n));
        this.combIdx = this.combTune.map(() => 0);
        this.combFb = [0.805, 0.827, 0.783, 0.764];
        this.apTune = [225, 556].map((n) => Math.round(n * SR / 44100));
        this.ap = this.apTune.map((n) => new Float32Array(n));
        this.apIdx = this.apTune.map(() => 0);
    }
    /** 输入干声, 返回湿声(逐样本) */
    process(x) {
        let acc = 0;
        for (let i = 0; i < this.comb.length; i++) {
            const buf = this.comb[i];
            const idx = this.combIdx[i];
            const y = buf[idx];
            acc += y;
            buf[idx] = x + y * this.combFb[i];
            this.combIdx[i] = (idx + 1) % buf.length;
        }
        acc *= 0.25;
        for (let i = 0; i < this.ap.length; i++) {
            const buf = this.ap[i];
            const idx = this.apIdx[i];
            const y = buf[idx];
            const out = -acc + y;
            buf[idx] = acc + y * 0.5;
            this.apIdx[i] = (idx + 1) % buf.length;
            acc = out;
        }
        return acc;
    }
}

/* ---------------- 立体声总线(带混响发送 + 延迟 + 软限幅) ---------------- */
class Bus {
    constructor(len) {
        this.L = new Float32Array(len);
        this.R = new Float32Array(len);
        this.rev = new Reverb();
        this.dlL = new Float32Array(Math.round(0.34 * SR));
        this.dlR = new Float32Array(Math.round(0.47 * SR));
        this.dlIdxL = 0; this.dlIdxR = 0;
    }
    clear() { this.L.fill(0); this.R.fill(0); }
    /** 加一个采样(带声像 pan: 0=左 1=右, 与混响发送量 send) */
    add(i, l, r, send) {
        if (i < 0 || i >= this.L.length) return;
        this.L[i] += l;
        this.R[i] += r;
        if (send > 0) {
            const wet = this.rev.process((l + r) * 0.5 * send);
            this.L[i] += wet * 0.55;
            this.R[i] += wet * 0.62;
        }
    }
    /** 后期: 立体声延迟 + 软限幅 */
    finish() {
        for (let i = 0; i < this.L.length; i++) {
            const dL = this.dlL[this.dlIdxL], dR = this.dlR[this.dlIdxR];
            this.dlL[this.dlIdxL] = this.L[i] + dR * 0.22;
            this.dlR[this.dlIdxR] = this.R[i] + dL * 0.22;
            this.dlIdxL = (this.dlIdxL + 1) % this.dlL.length;
            this.dlIdxR = (this.dlIdxR + 1) % this.dlR.length;
            this.L[i] += dL * 0.18;
            this.R[i] += dR * 0.18;
            /* 总线增益 + 软限幅: 让整首曲子达到"能和其他 BGM 齐平"的电平
               (第一版峰值只有 0.30 / RMS 0.05, 太小声; 这里 ×MASTER_GAIN 后交给 tanh 轻微压限) */
            this.L[i] = Math.tanh(this.L[i] * MASTER_GAIN) * 0.95;
            this.R[i] = Math.tanh(this.R[i] * MASTER_GAIN) * 0.95;
        }
    }
}

/* ---------------- 乐器(全部逐样本合成, 写进 Bus) ---------------- */
const TAU = Math.PI * 2;

function env(i, dur, a, d, s, r) {          // ADSR, i = 已过秒数
    if (i < a) return i / a;
    if (i < a + d) return 1 - (1 - s) * (i - a) / d;
    if (i < dur) return s;
    const k = (i - dur) / r;
    return k >= 1 ? 0 : s * (1 - k);
}

/** 弦乐: 3 个失谐锯齿 + 柔和低通, 慢起音 */
function strings(bus, t0, dur, f, vel, pan = 0.5, send = 0.45) {
    const n = Math.round((dur + 0.25) * SR);
    const det = [0.997, 1.0, 1.004];
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = env(t, dur, 0.13, 0.2, 0.82, 0.25) * vel;
        if (e <= 0) continue;
        let s = 0;
        for (let d = 0; d < 3; d++) {
            const ph = (t * f * det[d]) % 1;
            s += (ph * 2 - 1) * 0.33;
        }
        const vib = 1 + Math.sin(TAU * 5.2 * t) * 0.0035;
        lp += (s * vib - lp) * 0.28;                  // 一阶低通
        const g = lp * e * 0.16;
        bus.add(Math.round((t0 + t) * SR), g * (1 - pan), g * pan, send);
    }
}

/** 铜管: 锯齿+方波混合, 快起音, 轻微上滑 */
function brass(bus, t0, dur, f, vel, pan = 0.5, send = 0.4) {
    const n = Math.round((dur + 0.2) * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = env(t, dur, 0.045, 0.12, 0.78, 0.16) * vel;
        if (e <= 0) continue;
        const bend = 1 + 0.012 * Math.exp(-t * 14);
        const ph = (t * f * bend) % 1;
        const saw = ph * 2 - 1;
        const sq = ph < 0.5 ? 1 : -1;
        const s = saw * 0.7 + sq * 0.3;
        lp += (s - lp) * 0.42;
        const g = lp * e * 0.15;
        bus.add(Math.round((t0 + t) * SR), g * (1 - pan), g * pan, send);
    }
}

/** 合唱/人声垫: 基频 + 谐波 + 慢颤音 */
function choir(bus, t0, dur, f, vel, pan = 0.5, send = 0.6) {
    const n = Math.round((dur + 0.4) * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = env(t, dur, 0.35, 0.3, 0.85, 0.5) * vel;
        if (e <= 0) continue;
        const vib = 1 + Math.sin(TAU * 4.6 * t) * 0.006;
        const fv = f * vib;
        let s = Math.sin(TAU * fv * t) * 0.6
              + Math.sin(TAU * fv * 2 * t) * 0.22
              + Math.sin(TAU * fv * 3 * t) * 0.1
              + Math.sin(TAU * fv * 5 * t) * 0.05;
        const g = s * e * 0.085;
        bus.add(Math.round((t0 + t) * SR), g * (1 - pan), g * pan, send);
    }
}

/** 低音: 正弦 + 三角 + 拨弦瞬态 */
function bass(bus, t0, dur, f, vel, send = 0.18) {
    const n = Math.round((dur + 0.1) * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = env(t, dur, 0.012, 0.1, 0.72, 0.1) * vel;
        if (e <= 0) continue;
        const ph = (t * f) % 1;
        const tri = 4 * Math.abs(ph - 0.5) - 1;
        const s = Math.sin(TAU * f * t) * 0.6 + tri * 0.4;
        const clk = Math.exp(-t * 60) * 0.25 * (rnd() * 2 - 1);
        const g = (s + clk) * e * 0.2;
        bus.add(Math.round((t0 + t) * SR), g * 0.72, g * 0.72, send);
    }
}

/** 定音鼓: 下滑正弦 + 噪声击打 */
function timpani(bus, t0, f, vel, send = 0.5) {
    const dur = 0.9, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = Math.exp(-t * 3.1) * vel;
        const ff = f * (1 + 0.09 * Math.exp(-t * 22));
        let s = Math.sin(TAU * ff * t);
        if (t < 0.02) s += (rnd() * 2 - 1) * 0.7;
        const g = s * e * 0.32;
        bus.add(Math.round((t0 + t) * SR), g * 0.7, g * 0.7, send);
    }
}

/** 小鼓/战鼓 */
function snare(bus, t0, vel, send = 0.3) {
    const dur = 0.22, n = Math.round(dur * SR);
    let lp = 0, hp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = Math.exp(-t * 22) * vel;
        const nz = rnd() * 2 - 1;
        lp += (nz - lp) * 0.5;
        hp = lp - hp * 0.02;
        const body = Math.sin(TAU * 190 * t) * Math.exp(-t * 30) * 0.4;
        const g = (hp * 0.8 + body) * e * 0.2;
        bus.add(Math.round((t0 + t) * SR), g * 0.62, g * 0.68, send);
    }
}

/** 镲/吊镲 */
function cymbal(bus, t0, vel, send = 0.5) {
    const dur = 1.4, n = Math.round(dur * SR);
    let hp = 0, prev = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = Math.exp(-t * 2.4) * vel;
        const nz = rnd() * 2 - 1;
        hp = 0.85 * (hp + nz - prev);
        prev = nz;
        const g = hp * e * 0.06;
        bus.add(Math.round((t0 + t) * SR), g * 0.55, g * 0.7, send);
    }
}

/** 铃/钟: 非谐波 FM */
function bell(bus, t0, f, vel, send = 0.6) {
    const dur = 2.6, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = Math.exp(-t * 1.5) * vel;
        const mod = Math.sin(TAU * f * 2.76 * t) * 2.2 * Math.exp(-t * 3);
        const s = Math.sin(TAU * f * t + mod);
        const g = s * e * 0.075;
        bus.add(Math.round((t0 + t) * SR), g * 0.5, g * 0.62, send);
    }
}

/** 竖琴/拨弦(八分音符跑动) */
function pluck(bus, t0, f, vel, pan = 0.5, send = 0.35) {
    const dur = 0.6, n = Math.round(dur * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR;
        const e = Math.exp(-t * 7.5) * vel;
        const ph = (t * f) % 1;
        const s = ph * 2 - 1;
        lp += (s - lp) * (0.55 * Math.exp(-t * 4) + 0.08);
        const g = lp * e * 0.11;
        bus.add(Math.round((t0 + t) * SR), g * (1 - pan), g * pan, send);
    }
}

/* ---------------- 乐谱素材 ---------------- */
/* 自然小调音级(1..7) -> 半音偏移 */
const MINOR = [0, 2, 3, 5, 7, 8, 10];
const MAJOR = [0, 2, 4, 5, 7, 9, 11];

function deg2freq(rootName, rootOct, scale, degree, octShift) {
    const idx = ((degree - 1) % 7 + 7) % 7;
    const oct = Math.floor((degree - 1) / 7) + octShift;
    return freq(rootName, rootOct + oct) * Math.pow(2, (scale === 'major' ? MAJOR : MINOR)[idx] / 12);
}

/* 主旋律动机: [音级, 拍数] —— 一个 8 小节的英雄主题 */
const THEME = [
    [5, 1], [5, 0.5], [6, 0.5], [5, 1], [3, 1],
    [4, 1], [3, 1], [2, 2],
    [5, 1], [5, 0.5], [6, 0.5], [8, 1], [7, 1],
    [6, 1], [5, 1], [5, 2],
    [3, 1], [4, 0.5], [5, 0.5], [6, 1], [5, 1],
    [4, 1], [3, 1], [2, 2],
    [2, 0.5], [3, 0.5], [4, 1], [5, 1], [6, 1],
    [5, 1], [3, 1], [2, 2],
];
/* 副题(对位/发展段用): 更流畅 */
const THEME_B = [
    [8, 1], [7, 1], [6, 1], [5, 1],
    [6, 1], [7, 1], [8, 2],
    [9, 0.5], [8, 0.5], [7, 1], [6, 1], [5, 1],
    [4, 1], [3, 1], [2, 2],
];
/* 危机段落: 半音下行的固定音型 */
const THEME_C = [
    [5, 0.5], [5, 0.5], [5, 0.5], [4, 0.5], [5, 0.5], [5, 0.5], [6, 0.5], [5, 0.5],
    [4, 0.5], [4, 0.5], [4, 0.5], [3, 0.5], [4, 0.5], [4, 0.5], [5, 0.5], [4, 0.5],
    [3, 0.5], [3, 0.5], [3, 0.5], [2, 0.5], [3, 0.5], [3, 0.5], [4, 0.5], [3, 0.5],
    [2, 1], [4, 1], [5, 2],
];

/* 和声进行(音级三元组: 根/三/五 的音级与八度偏移) —— 用"音级+八度"描述, 便于移调 */
function chord(rootDeg, scale, octShift) {
    return [deg2freq('D', 3, scale, rootDeg, octShift),
            deg2freq('D', 3, scale, rootDeg + 2, octShift),
            deg2freq('D', 3, scale, rootDeg + 4, octShift)];
}

/* 各段的和声循环(音级): i - VI - III - VII 之类 */
const PROG_MINOR = [1, 6, 3, 7];      // Dm - Bb - F - C
const PROG_MAJOR = [3, 7, 1, 6];      // F  - C  - Dm - Bb
const PROG_DARK  = [1, 4, 5, 5];      // Gm - Cm - Dm - Dm(小调)
const PROG_SOFT  = [6, 3, 7, 1];      // Bb - F  - C  - Dm

/* ---------------- 段落定义 ---------------- */
const SECTIONS = [
    { name: '序章',     start: 0,    end: 80,   bpm: 76,  scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'intro' },
    { name: '主题A',    start: 80,   end: 210,  bpm: 132, scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'themeA' },
    { name: '发展B',    start: 210,  end: 360,  bpm: 132, scale: 'major', prog: PROG_MAJOR, key: 'D', keyOct: 2,
      mode: 'themeB' },
    { name: '再现A',    start: 360,  end: 510,  bpm: 138, scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'themeA2' },
    { name: '危机C',    start: 510,  end: 630,  bpm: 100, scale: 'minor', prog: PROG_DARK,  key: 'G', keyOct: 2,
      mode: 'dark' },
    { name: '高潮',     start: 630,  end: 780,  bpm: 144, scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'climax' },
    { name: '间歇',     start: 780,  end: 900,  bpm: 120, scale: 'major', prog: PROG_SOFT,  key: 'D', keyOct: 2,
      mode: 'bridge' },
    { name: '终局',     start: 900,  end: 1080, bpm: 144, scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'finale' },
    { name: '尾声',     start: 1080, end: 1200, bpm: 88,  scale: 'minor', prog: PROG_MINOR, key: 'D', keyOct: 2,
      mode: 'coda' },
];

/* ---------------- 主流程: 分块渲染 + 流式编码 ---------------- */
async function main() {
    console.log(`开始作曲: ${TOTAL_SECONDS / 60} 分钟, ${SR} Hz 立体声, 种子 ${SEED}`);
    const enc = await createOggEncoder();
    enc.configure({ sampleRate: SR, channels: 2, vbrQuality: 1 });

    const CHUNK = 5;                       // 每块 5 秒
    const out = fs.createWriteStream(OUT);
    const bus = new Bus(CHUNK * SR + SR);  // 多留 1 秒给混响/延迟尾巴

    const t0 = Date.now();
    let written = 0;
    for (let pos = 0; pos < TOTAL_SECONDS; pos += CHUNK) {
        bus.clear();
        /* 落在本块里的所有段落(可能跨段) */
        for (const sec of SECTIONS) {
            const from = Math.max(sec.start, pos);
            const to = Math.min(sec.end, pos + CHUNK);
            if (to <= from) continue;
            /* 段落整体渲染一次太贵 —— 用"局部渲染"技巧: 把段落渲染到临时偏移,
               这里直接按段内相对时间渲染整段中**与本块相交的小节** */
            renderSectionWindow(bus, sec, from - pos, pos);
        }
        bus.finish();
        const L = new Float32Array(CHUNK * SR), R = new Float32Array(CHUNK * SR);
        L.set(bus.L.subarray(0, CHUNK * SR));
        R.set(bus.R.subarray(0, CHUNK * SR));
        const chunk = enc.encode([L, R]);
        out.write(Buffer.from(chunk));
        written += chunk.length;
        /* 电平统计(每 60 秒一行): 确认"有声音、没削爆" */
        let peak = 0, sum = 0;
        for (let i = 0; i < L.length; i += 7) {
            const a = Math.abs(L[i]), b = Math.abs(R[i]);
            if (a > peak) peak = a;
            if (b > peak) peak = b;
            sum += (L[i] * L[i] + R[i] * R[i]) * 0.5;
        }
        const rms = Math.sqrt(sum / (L.length / 7));
        if (pos % 60 === 0) {
            const sn = (SECTIONS.find((s) => pos >= s.start && pos < s.end) || { name: '?' }).name;
            console.log(`\n  [${String(Math.floor(pos / 60)).padStart(2, '0')}:00 ${sn}] peak ${peak.toFixed(3)} rms ${rms.toFixed(3)}`);
        }
        process.stdout.write(`\r  已渲染 ${((pos + CHUNK) / TOTAL_SECONDS * 100).toFixed(1)}%  (${((Date.now() - t0) / 1000).toFixed(0)}s, ${(written / 1048576).toFixed(1)} MB)`);
    }
    out.write(Buffer.from(enc.finalize()));
    out.end();
    await new Promise((res) => out.on('close', res));
    const size = fs.statSync(OUT).size;
    console.log(`\n写出 ${OUT}`);
    console.log(`  时长 ${(TOTAL_SECONDS / 60).toFixed(1)} 分钟, 文件 ${(size / 1048576).toFixed(1)} MB`);
}

/** 渲染段落中与本块相交的部分(时间偏移到块内) */
function renderSectionWindow(bus, sec, offsetInChunk, chunkStartAbs) {
    const spb = 60 / sec.bpm;
    const barSecs = spb * 4;
    const firstBar = Math.floor((chunkStartAbs - sec.start) / barSecs);
    const lastBar = Math.ceil((chunkStartAbs + 5 - sec.start) / barSecs);
    for (let bar = Math.max(0, firstBar); bar < lastBar; bar++) {
        const absBarStart = sec.start + bar * barSecs;
        const local = absBarStart - chunkStartAbs;          // 相对本块起点
        if (local > 5) break;
        renderBar(bus, sec, bar, local);
    }
}

/* 每小节渲染(和 renderSection 相同逻辑, 但只画一个小节, 便于分块) */
function renderBar(bus, sec, bar, localT) {
    const spb = 60 / sec.bpm;
    const scale = sec.scale;
    const cfg = MODE_CFG[sec.mode];
    const theme = sec.mode === 'dark' ? THEME_C
        : (sec.mode === 'themeB' || sec.mode === 'bridge' ? THEME_B : THEME);
    const oct = sec.keyOct;
    const totalBars = Math.ceil((sec.end - sec.start) / (spb * 4));
    const progDeg = sec.prog[bar % sec.prog.length];
    const ch = chord(progDeg, scale, 0);
    const isLast2 = bar >= totalBars - 2;
    const T = localT;

    if (cfg.strings > 0.05) {
        const dur = spb * 3.9;
        strings(bus, T, dur, ch[0] / 2, 0.7 * cfg.strings, 0.28, 0.5);
        strings(bus, T, dur, ch[1], 0.55 * cfg.strings, 0.5, 0.5);
        strings(bus, T, dur, ch[2], 0.5 * cfg.strings, 0.72, 0.5);
    }
    for (let b = 0; b < 4; b++) {
        const vel = (b === 0 ? 1.0 : b === 2 ? 0.8 : 0.55) * (0.6 + 0.4 * cfg.perc);
        bass(bus, T + b * spb, spb * 0.9, ch[0] / 2, 0.8 * vel);
    }
    if (cfg.perc > 0.2) {
        timpani(bus, T, ch[0] / 2, cfg.timp);
        if (bar % 2 === 1) timpani(bus, T + spb * 2, ch[2] / 2, cfg.timp * 0.8);
        snare(bus, T + spb, 0.8 * cfg.perc);
        snare(bus, T + spb * 3, 0.9 * cfg.perc);
        snare(bus, T + spb * 3.5, 0.45 * cfg.perc);
        cymbal(bus, T, 0.7 * cfg.perc);
        if (cfg.perc > 0.9 && sec.mode !== 'coda') {
            snare(bus, T + spb * 0.5, 0.4 * cfg.perc);
            snare(bus, T + spb * 1.5, 0.5 * cfg.perc);
            snare(bus, T + spb * 2.5, 0.4 * cfg.perc);
            cymbal(bus, T + spb * 2, 0.45 * cfg.perc);
        }
    }
    if (cfg.brass > 0.2 && !isLast2) {
        let tIn = 0;
        for (let ni = 0; ni < theme.length; ni++) {
            const [deg, beats] = theme[ni];
            const nt = T + tIn * spb;
            const dur = beats * spb * 0.95;
            if (tIn >= 4) break;
            if (ni % 4 === 0 || cfg.brass > 0.85) {
                brass(bus, nt, dur, deg2freq(sec.key, oct + 1, scale, deg, 0), 0.8 * cfg.brass, 0.42, 0.42);
            }
            if (cfg.harp > 0.2) {
                pluck(bus, nt, deg2freq(sec.key, oct + 2, scale, deg, 0), 0.55 * cfg.harp, 0.3, 0.4);
                pluck(bus, nt + dur * 0.5, deg2freq(sec.key, oct + 2, scale, deg + 2, 0), 0.4 * cfg.harp, 0.7, 0.4);
            }
            tIn += beats;
        }
    } else if (cfg.strings > 0.3) {
        let tIn = 0;
        for (let ni = 0; ni < theme.length; ni++) {
            const [deg, beats] = theme[ni];
            const nt = T + tIn * spb;
            if (tIn >= 4) break;
            strings(bus, nt, beats * spb * 1.05, deg2freq(sec.key, oct + 1, scale, deg, 0), 0.75 * cfg.strings, 0.5, 0.55);
            tIn += beats;
        }
    }
    if (cfg.choir > 0.3 && bar % 2 === 0) {
        choir(bus, T, spb * 8, ch[1], 0.6 * cfg.choir, 0.45, 0.65);
        choir(bus, T, spb * 8, ch[2], 0.5 * cfg.choir, 0.6, 0.65);
    }
    if (cfg.bell > 0.3 && bar % 4 === 3) {
        bell(bus, T + spb * 2, deg2freq(sec.key, oct + 3, scale, progDeg, 0), cfg.bell, 0.55);
    }
    if (isLast2 && cfg.perc > 0.2) {
        timpani(bus, T, ch[0] / 2, cfg.timp * 1.1);
        timpani(bus, T + spb, ch[0] / 2, cfg.timp * 0.9);
        cymbal(bus, T + spb * 3, 0.8 * cfg.perc);
        for (let r = 0; r < 8; r++) timpani(bus, T + spb * 3 + r * spb / 8, ch[0] / 2, 0.35 * cfg.timp);
    }
}

const MODE_CFG = {
    intro:   { strings: 0.55, brass: 0.25, choir: 0.25, perc: 0.15, harp: 0.0, bell: 0.15, timp: 0.5 },
    themeA:  { strings: 0.75, brass: 0.85, choir: 0.25, perc: 0.85, harp: 0.4, bell: 0.2, timp: 0.5 },
    themeB:  { strings: 0.7,  brass: 0.6,  choir: 0.65, perc: 0.7,  harp: 0.55, bell: 0.35, timp: 0.4 },
    themeA2: { strings: 0.85, brass: 0.9,  choir: 0.4,  perc: 0.95, harp: 0.5, bell: 0.3, timp: 0.6 },
    dark:    { strings: 0.6,  brass: 0.5,  choir: 0.3,  perc: 0.45, harp: 0.15, bell: 0.1, timp: 0.8 },
    climax:  { strings: 1.0,  brass: 1.0,  choir: 0.9,  perc: 1.0,  harp: 0.6, bell: 0.5, timp: 0.9 },
    bridge:  { strings: 0.6,  brass: 0.3,  choir: 0.4,  perc: 0.3,  harp: 0.85, bell: 0.4, timp: 0.2 },
    finale:  { strings: 1.0,  brass: 1.0,  choir: 0.95, perc: 1.0,  harp: 0.7, bell: 0.6, timp: 1.0 },
    coda:    { strings: 0.8,  brass: 0.7,  choir: 0.8,  perc: 0.35, harp: 0.3, bell: 0.5, timp: 0.5 },
};

/* 入口(放在最后, 保证所有 const 都已初始化) */
main();
