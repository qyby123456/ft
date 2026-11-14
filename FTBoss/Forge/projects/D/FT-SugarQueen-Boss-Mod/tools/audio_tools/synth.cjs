/* ============================================================================
 * 纯 Node 合成工具箱(2026-09-13 v99) —— 给《Boss 战 24 分钟组曲》各段共用
 *
 * 从 compose_epic.cjs(v82 那首 20 分钟史诗曲)里抽出可复用的部分, 并补上
 * Boss 战需要而原来没有的打击乐: 底鼓/嗵鼓/铜锣/上升镲(反向镲)/冲击音/低音持续音/脉冲。
 *
 * 依赖: wasm-media-encoders(纯 WASM 的 Ogg Vorbis 编码器, 机器上没有 ffmpeg)。
 * 所有音色都是逐样本合成, 无外部采样。
 * ==========================================================================*/
const { createOggEncoder } = require('wasm-media-encoders');

const SR = 44100;
const TAU = Math.PI * 2;

/* ---------------- 随机(可复现) ---------------- */
let seed = 20260913;
function setSeed(s) { seed = s >>> 0; }
function rnd() { seed = (seed * 1664525 + 1013904223) >>> 0; return seed / 4294967296; }

/** 音名 -> 频率(A4=440, 十二平均律) */
const NOTE_BASE = { C: -9, D: -7, E: -5, F: -4, G: -2, A: 0, B: 2 };
function freq(name, octave) {
    const m = /^([A-G])([#b]?)/.exec(name);
    let semi = NOTE_BASE[m[1]] + (m[2] === '#' ? 1 : m[2] === 'b' ? -1 : 0);
    semi += (octave - 4) * 12;
    return 440 * Math.pow(2, semi / 12);
}

/* ---------------- 混响(Schroeder: 4 comb + 2 allpass) ---------------- */
class Reverb {
    constructor() {
        this.comb = [1557, 1617, 1491, 1422].map((n) => new Float32Array(Math.round(n * SR / 44100)));
        this.combIdx = this.comb.map(() => 0);
        this.combFb = [0.805, 0.827, 0.783, 0.764];
        this.ap = [225, 556].map((n) => new Float32Array(Math.round(n * SR / 44100)));
        this.apIdx = this.ap.map(() => 0);
    }
    process(x) {
        let acc = 0;
        for (let i = 0; i < this.comb.length; i++) {
            const buf = this.comb[i], idx = this.combIdx[i], y = buf[idx];
            acc += y;
            buf[idx] = x + y * this.combFb[i];
            this.combIdx[i] = (idx + 1) % buf.length;
        }
        acc *= 0.25;
        for (let i = 0; i < this.ap.length; i++) {
            const buf = this.ap[i], idx = this.apIdx[i], y = buf[idx];
            const out = -acc + y;
            buf[idx] = acc + y * 0.5;
            this.apIdx[i] = (idx + 1) % buf.length;
            acc = out;
        }
        return acc;
    }
}

/**
 * 立体声总线。**给 Stem 用时不要开限幅**(`Bus.finish(false)`)——
 * 各轨要能线性相加, 限幅是非线性的, 会让"分轨之和 ≠ 整轨"。
 */
class Bus {
    constructor(len) {
        this.L = new Float32Array(len);
        this.R = new Float32Array(len);
        this.rev = new Reverb();
        this.dlL = new Float32Array(Math.round(0.34 * SR));
        this.dlR = new Float32Array(Math.round(0.47 * SR));
        this.dlIdxL = 0; this.dlIdxR = 0;
    }
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
    /** 后期: 立体声延迟(+ 可选软限幅)。gain=1 且 limit=false 时是**线性**的, 分轨可相加。 */
    finish(gain = 1, limit = false) {
        for (let i = 0; i < this.L.length; i++) {
            const dL = this.dlL[this.dlIdxL], dR = this.dlR[this.dlIdxR];
            this.dlL[this.dlIdxL] = this.L[i] + dR * 0.22;
            this.dlR[this.dlIdxR] = this.R[i] + dL * 0.22;
            this.dlIdxL = (this.dlIdxL + 1) % this.dlL.length;
            this.dlIdxR = (this.dlIdxR + 1) % this.dlR.length;
            this.L[i] += dL * 0.18;
            this.R[i] += dR * 0.18;
            this.L[i] *= gain;
            this.R[i] *= gain;
            if (limit) {
                this.L[i] = Math.tanh(this.L[i]) * 0.95;
                this.R[i] = Math.tanh(this.R[i]) * 0.95;
            }
        }
    }
}

/** 把一个 stemp 总线同时写到"和轨道"上(用于测量整混峰值) */
class Tee {
    constructor(a, b) { this.a = a; this.b = b; }
    add(i, l, r, send) { this.a.add(i, l, r, send); if (this.b) this.b.add(i, l, r, send); }
}

/* ---------------- 包络 ---------------- */
function env(i, dur, a, d, s, r) {
    if (i < a) return i / a;
    if (i < a + d) return 1 - (1 - s) * (i - a) / d;
    if (i < dur) return s;
    const k = (i - dur) / r;
    return k >= 1 ? 0 : s * (1 - k);
}
const at = (t0, t) => Math.round((t0 + t) * SR);

/* ============================ 音色 ============================ */

/** 弦乐垫: 3 个失谐锯齿 + 一阶低通, 慢起音 */
function strings(bus, t0, dur, f, vel, pan = 0.5, send = 0.45) {
    const n = Math.round((dur + 0.25) * SR), det = [0.997, 1.0, 1.004];
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.13, 0.2, 0.82, 0.25) * vel;
        if (e <= 0) continue;
        let s = 0;
        for (let d = 0; d < 3; d++) s += (((t * f * det[d]) % 1) * 2 - 1) * 0.33;
        lp += (s * (1 + Math.sin(TAU * 5.2 * t) * 0.0035) - lp) * 0.28;
        const g = lp * e * 0.16;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/** 铜管: 锯齿+方波, 快起音, 轻微上滑 */
function brass(bus, t0, dur, f, vel, pan = 0.5, send = 0.4) {
    const n = Math.round((dur + 0.2) * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.045, 0.12, 0.78, 0.16) * vel;
        if (e <= 0) continue;
        const ph = (t * f * (1 + 0.012 * Math.exp(-t * 14))) % 1;
        lp += ((ph * 2 - 1) * 0.7 + (ph < 0.5 ? 1 : -1) * 0.3 - lp) * 0.42;
        const g = lp * e * 0.15;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/** 大提琴/低音弦(旋律用): 比 strings 更暗、起音稍快 */
function cello(bus, t0, dur, f, vel, pan = 0.5, send = 0.4) {
    const n = Math.round((dur + 0.2) * SR), det = [0.995, 1.0, 1.006];
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.07, 0.15, 0.8, 0.3) * vel;
        if (e <= 0) continue;
        let s = 0;
        for (let d = 0; d < 3; d++) s += (((t * f * det[d]) % 1) * 2 - 1) * 0.33;
        lp += (s - lp) * 0.2;
        const g = lp * e * 0.19;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/** 合唱垫 */
function choir(bus, t0, dur, f, vel, pan = 0.5, send = 0.6) {
    const n = Math.round((dur + 0.4) * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.35, 0.3, 0.85, 0.5) * vel;
        if (e <= 0) continue;
        const fv = f * (1 + Math.sin(TAU * 4.6 * t) * 0.006);
        const s = Math.sin(TAU * fv * t) * 0.6 + Math.sin(TAU * fv * 2 * t) * 0.22
                + Math.sin(TAU * fv * 3 * t) * 0.1 + Math.sin(TAU * fv * 5 * t) * 0.05;
        const g = s * e * 0.085;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/** 低音(正弦+三角+拨弦瞬态) */
function bass(bus, t0, dur, f, vel, send = 0.18) {
    const n = Math.round((dur + 0.1) * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.012, 0.1, 0.72, 0.1) * vel;
        if (e <= 0) continue;
        const tri = 4 * Math.abs((t * f) % 1 - 0.5) - 1;
        const s = Math.sin(TAU * f * t) * 0.6 + tri * 0.4 + Math.exp(-t * 60) * 0.25 * (rnd() * 2 - 1);
        const g = s * e * 0.2;
        bus.add(at(t0, t), g * 0.72, g * 0.72, send);
    }
}

/** 脉冲(八分音符 staccato, 电子感) —— "Pulse" 层的主力 */
function pulse(bus, t0, dur, f, vel, pan = 0.5, send = 0.25) {
    const n = Math.round((dur + 0.12) * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 0.008, 0.06, 0.5, 0.09) * vel;
        if (e <= 0) continue;
        const ph = (t * f) % 1;
        const sq = ph < 0.5 ? 1 : -1;
        lp += (sq * 0.6 + (ph * 2 - 1) * 0.4 - lp) * (0.5 * Math.exp(-t * 18) + 0.06);
        const g = lp * e * 0.085;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/** 低音持续音(黑暗序幕/铺底) */
function drone(bus, t0, dur, f, vel, send = 0.5) {
    const n = Math.round((dur + 0.5) * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = env(t, dur, 1.6, 0.5, 0.9, 1.2) * vel;
        if (e <= 0) continue;
        const s = Math.sin(TAU * f * t) * 0.7 + Math.sin(TAU * f * 1.5 * t) * 0.15
                + Math.sin(TAU * f * 2.01 * t) * 0.1 + (rnd() * 2 - 1) * 0.02;
        lp += (s - lp) * 0.05;
        const g = lp * e * 0.14;
        bus.add(at(t0, t), g * 0.66, g * 0.72, send);
    }
}

/* ---------------- 打击乐 ---------------- */

/** 定音鼓 */
function timpani(bus, t0, f, vel, send = 0.5) {
    const dur = 0.9, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 3.1) * vel;
        let s = Math.sin(TAU * f * (1 + 0.09 * Math.exp(-t * 22)) * t);
        if (t < 0.02) s += (rnd() * 2 - 1) * 0.7;
        const g = s * e * 0.32;
        bus.add(at(t0, t), g * 0.7, g * 0.7, send);
    }
}

/** 底鼓: 音高下扫正弦 + 点击 */
function kick(bus, t0, vel, send = 0.25) {
    const dur = 0.42, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 8.5) * vel;
        const f = 52 + 90 * Math.exp(-t * 28);
        let s = Math.sin(TAU * f * t);
        if (t < 0.006) s += (rnd() * 2 - 1) * 0.5;
        const g = s * e * 0.5;
        bus.add(at(t0, t), g * 0.74, g * 0.74, send);
    }
}

/** 战鼓/小鼓 */
function snare(bus, t0, vel, send = 0.3) {
    const dur = 0.22, n = Math.round(dur * SR);
    let lp = 0, hp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 22) * vel;
        lp += ((rnd() * 2 - 1) - lp) * 0.5;
        hp = lp - hp * 0.02;
        const g = (hp * 0.8 + Math.sin(TAU * 190 * t) * Math.exp(-t * 30) * 0.4) * e * 0.2;
        bus.add(at(t0, t), g * 0.62, g * 0.68, send);
    }
}

/** 嗵鼓 */
function tom(bus, t0, f, vel, send = 0.32) {
    const dur = 0.45, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 7.5) * vel;
        let s = Math.sin(TAU * f * (1 + 0.18 * Math.exp(-t * 18)) * t);
        if (t < 0.008) s += (rnd() * 2 - 1) * 0.4;
        const g = s * e * 0.34;
        bus.add(at(t0, t), g * 0.66, g * 0.7, send);
    }
}

/** 吊镲 */
function cymbal(bus, t0, vel, send = 0.5) {
    const dur = 1.4, n = Math.round(dur * SR);
    let hp = 0, prev = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 2.4) * vel;
        const nz = rnd() * 2 - 1;
        hp = 0.85 * (hp + nz - prev);
        prev = nz;
        const g = hp * e * 0.06;
        bus.add(at(t0, t), g * 0.55, g * 0.7, send);
    }
}

/** 上升镲(反向镲) —— Boss 出场前 4 拍的那个"吸气" */
function riser(bus, t0, dur, vel, send = 0.55) {
    const n = Math.round(dur * SR);
    let hp = 0, prev = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, u = t / dur;
        const e = Math.pow(u, 2.6) * vel;
        const nz = rnd() * 2 - 1;
        hp = 0.9 * (hp + nz - prev);
        prev = nz;
        const tone = Math.sin(TAU * (200 + 1400 * u * u) * t) * 0.25 * u;
        const g = (hp * 0.7 + tone) * e * 0.055;
        bus.add(at(t0, t), g * 0.6, g * 0.66, send);
    }
}

/** 大型铜锣: 非谐波簇 + 长衰减 */
function gong(bus, t0, f, vel, send = 0.7) {
    const dur = 4.5, n = Math.round(dur * SR);
    const part = [1.0, 1.51, 2.13, 2.71, 3.42, 4.31, 5.5];
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 0.85) * vel;
        let s = 0;
        for (let i = 0; i < part.length; i++) s += Math.sin(TAU * f * part[i] * t) * (0.9 / (i + 1.6));
        if (t < 0.05) s += (rnd() * 2 - 1) * 0.5;
        const g = s * e * 0.09;
        bus.add(at(t0, t), g * 0.6, g * 0.68, send);
    }
}

/** 冲击音(重击): 低频轰 + 噪声爆 */
function impact(bus, t0, vel, send = 0.6) {
    const dur = 1.6, n = Math.round(dur * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 3.4) * vel;
        lp += ((rnd() * 2 - 1) - lp) * 0.18;
        const s = Math.sin(TAU * (44 - 12 * Math.min(1, t / 0.3)) * t) * 1.2 + lp * 0.8;
        const g = s * e * 0.22;
        bus.add(at(t0, t), g * 0.7, g * 0.7, send);
    }
}

/** 铃/钟 */
function bell(bus, t0, f, vel, send = 0.6) {
    const dur = 2.6, n = Math.round(dur * SR);
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 1.5) * vel;
        const mod = Math.sin(TAU * f * 2.76 * t) * 2.2 * Math.exp(-t * 3);
        const g = Math.sin(TAU * f * t + mod) * e * 0.075;
        bus.add(at(t0, t), g * 0.5, g * 0.62, send);
    }
}

/** 拨弦(竖琴) */
function pluck(bus, t0, f, vel, pan = 0.5, send = 0.35) {
    const dur = 0.6, n = Math.round(dur * SR);
    let lp = 0;
    for (let k = 0; k < n; k++) {
        const t = k / SR, e = Math.exp(-t * 7.5) * vel;
        lp += (((t * f) % 1) * 2 - 1 - lp) * (0.55 * Math.exp(-t * 4) + 0.08);
        const g = lp * e * 0.11;
        bus.add(at(t0, t), g * (1 - pan), g * pan, send);
    }
}

/* ---------------- 编码 ---------------- */
/**
 * 把一段立体声总线编码成 Ogg Vorbis 并写文件(分块喂, 免得一次性占内存)。
 * vbrQuality: 0..1, 约 0.5 ≈ 用户规格里的 Q5/Q6。
 */
async function encodeOgg(path, L, R, opts = {}) {
    const fs = require('fs');
    const q = opts.vbrQuality === undefined ? 0.5 : opts.vbrQuality;
    const enc = await createOggEncoder();
    enc.configure({ sampleRate: SR, channels: 2, vbrQuality: q });
    const chunk = SR * 5;
    const out = fs.createWriteStream(path);
    let peak = 0, sum = 0, n = 0;
    for (let off = 0; off < L.length; off += chunk) {
        const len = Math.min(chunk, L.length - off);
        const l = L.subarray(off, off + len), r = R.subarray(off, off + len);
        for (let i = 0; i < len; i++) {
            const a = Math.abs(l[i]); if (a > peak) peak = a;
            sum += l[i] * l[i] + r[i] * r[i]; n += 2;
        }
        const data = enc.encode([l, r]);
        if (data && data.length) { if (!out.write(Buffer.from(data))) await new Promise((res) => out.once('drain', res)); }
    }
    const tail = enc.finalize();
    if (tail && tail.length) out.write(Buffer.from(tail));
    out.end();
    await new Promise((res) => out.on('close', res));
    const rms = Math.sqrt(sum / Math.max(1, n));
    return { peak, rms, size: fs.statSync(path).size };
}

module.exports = {
    SR, TAU, setSeed, rnd, freq, Reverb, Bus, Tee, env, at, encodeOgg,
    strings, brass, cello, choir, bass, pulse, drone,
    timpani, kick, snare, tom, cymbal, riser, gong, impact, bell, pluck,
};
