/* ============================================================================
 *  苹果爵士·蜜脆 BGM (2026-09-12)
 *  风格: **黑帮摇摆爵士**(礼帽/金腰带/墨镜的"果园教父") —— 不是答辩超人那套军工电子。
 *    摇摆八分(后半拍落在 2/3 拍位) + 走动低音 + 大乐队铜管切分 + 单簧管式主题 + 刷鼓
 *  一阶段 108 BPM A 小调; 二阶段 132 BPM 升 2 个半音(虫蛀·暴走, 加重鼓与风琴)
 *  用法: node render_apple_bgm.cjs <modRoot>
 *  产物: assets/ftboss/sounds/music/apple.ogg / apple_p2.ogg
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
const tri = (p) => { const x = p - Math.floor(p); return x < 0.5 ? 4 * x - 1 : 3 - 4 * x; };

function voice(t0, dur, pan, gen, gain) {
    const i0 = Math.max(0, Math.floor(t0 * SR));
    const i1 = Math.min(N, Math.floor((t0 + dur) * SR));
    const a = pan * Math.PI / 2;
    const gl = Math.cos(a) * gain, gr = Math.sin(a) * gain;
    for (let i = i0; i < i1; i++) { const v = gen((i - i0) / SR); L[i] += v * gl; R[i] += v * gr; }
}

/* ---------------- 鼓: 摇摆刷鼓 ---------------- */
function kick(t0, amp = 1) {
    voice(t0, 0.26, 0.5, (t) => {
        const f = 48 + 90 * Math.exp(-t * 42);
        return Math.tanh(Math.sin(2 * Math.PI * f * t) * Math.exp(-t * 15) * 1.3);
    }, 0.62 * amp);
}
function brush(t0, amp = 1) {   /* 军鼓用"刷"的音色: 长噪声尾巴 */
    let prev = 0;
    voice(t0, 0.22, 0.52, (t) => {
        const n = rnd(); const hp = n - prev * 0.6; prev = n;
        return hp * Math.exp(-t * 16) * 0.55;
    }, 0.5 * amp);
}
function ride(t0, amp = 1) {    /* 摇摆拍点(ride 镲) */
    let prev = 0;
    voice(t0, 0.11, 0.62, (t) => {
        const n = rnd(); const hp = n - prev; prev = n;
        return hp * Math.exp(-t * 42);
    }, 0.26 * amp);
}
function crash(t0, amp = 1) {
    let prev = 0;
    voice(t0, 1.0, 0.5, (t) => { const n = rnd(); const hp = n - prev; prev = n; return hp * Math.exp(-t * 3.0); }, 0.3 * amp);
}

/* ---------------- 走动低音(爵士 walking bass) ---------------- */
function bass(t0, midi, dur, amp) {
    const f = hz(midi);
    let p = 0, p2 = 0;
    voice(t0, dur, 0.46, (t) => {
        p += f / SR; p2 += f * 2 / SR;
        const env = Math.min(1, t * 200) * Math.exp(-t * 3.2) * Math.min(1, (dur - t) * 60);
        return (Math.sin(2 * Math.PI * p) * 0.8 + Math.sin(2 * Math.PI * p2) * 0.2) * env;
    }, amp);
}
/* ---------------- 大乐队铜管切分 ---------------- */
function brass(t0, midis, dur, amp) {
    for (const m of midis) {
        const f = hz(m);
        const det = [0.994, 1.0, 1.008];
        const ph = [0, 0.3, 0.6];
        voice(t0, dur, 0.5, (t) => {
            let s = 0;
            for (let k = 0; k < 3; k++) { ph[k] += f * det[k] / SR; s += saw(ph[k]); }
            const env = Math.min(1, t * 60) * Math.exp(-t * 2.2) * Math.min(1, (dur - t) * 35);
            return Math.tanh((s / 3) * 1.7) * env;
        }, amp);
    }
}
/* ---------------- 单簧管式主题 ---------------- */
function lead(t0, midi, dur, amp) {
    const f = hz(midi);
    let p = 0, p2 = 0;
    voice(t0, dur, 0.58, (t) => {
        const vib = 1 + 0.005 * Math.sin(2 * Math.PI * 5.0 * t) * Math.min(1, t * 5);
        p += (f * vib) / SR; p2 += (f * vib * 0.5) / SR;
        const env = Math.min(1, t * 90) * Math.exp(-t * 0.7) * Math.min(1, (dur - t) * 45);
        return (sqr(p) * 0.5 + tri(p2) * 0.5) * env;
    }, amp);
}
/* ---------------- 风琴(二阶段加厚) ---------------- */
function organ(t0, midis, dur, amp) {
    for (const m of midis) {
        const f = hz(m);
        let p = 0;
        voice(t0, dur, 0.5, (t) => {
            p += f / SR;
            const env = Math.min(1, t / 0.08) * Math.min(1, (dur - t) / 0.3);
            return (Math.sin(2 * Math.PI * p) + 0.4 * Math.sin(4 * Math.PI * p) + 0.2 * Math.sin(6 * Math.PI * p)) * env * 0.5;
        }, amp);
    }
}

/* ---------------- 和声 ---------------- */
/* 相对 A 的半音: Am7 / Dm7 / E7 / F / C / G */
const CH = {
    Am: { root: 45, notes: [57, 60, 64, 67] },
    Dm: { root: 50, notes: [57, 62, 65, 69] },
    E7: { root: 52, notes: [56, 59, 62, 68] },
    F: { root: 41, notes: [57, 60, 65, 69] },
    C: { root: 48, notes: [55, 60, 64, 67] },
    G: { root: 43, notes: [55, 59, 62, 67] },
};
const A8 = ['Am', 'Dm', 'Am', 'E7', 'Am', 'Dm', 'F', 'E7'];
const B8 = ['F', 'C', 'Dm', 'Am', 'F', 'G', 'E7', 'E7'];
const BRK = ['Am', 'Am', 'Dm', 'E7'];
/* 主题动机(每小节 4 个音, 相对 A4=69 的半音偏移; 摇摆节奏) */
const MOTIF = [
    [-12, 0, -1, -3], [0, 3, 2, 0], [-1, -3, -5, -3], [0, -1, 0, 3],
    [3, 5, 3, 2], [0, -1, 0, 3], [-1, -3, -5, -7], [-5, -3, -1, 0],
];

function render(cfg) {
    const { bpm, transpose, phase } = cfg;
    const SPB = 60 / bpm;
    const BAR = 4 * SPB;
    const SW = SPB * 2 / 3;                        // 摇摆八分的后半拍位置(三连音 feel)
    const song = [];
    const push = (names, sect) => names.forEach((c) => song.push({ c, sect }));
    push(['Am', 'Am', 'Dm', 'E7'], 'intro');
    push(A8, 'A'); push(A8, 'A2'); push(BRK, 'break'); push(B8, 'B'); push(B8, 'climax');
    push(['Am', 'Dm', 'E7', 'Am'], 'outro');
    N = Math.floor((song.length * BAR + 2.2) * SR);
    L = new Float32Array(N); R = new Float32Array(N);
    let seed = 20260913 + phase * 733;
    rnd = () => ((seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff) * 2 - 1;

    const T = transpose;
    const intense = phase === 2;

    for (let bar = 0; bar < song.length; bar++) {
        const t0 = bar * BAR;
        const { c, sect } = song[bar];
        const ch = CH[c];
        const notes = ch.notes.map((n) => n + T);
        const isIntro = sect === 'intro', isBreak = sect === 'break', isOutro = sect === 'outro';
        const isClimax = sect === 'climax';

        /* --- 鼓 --- */
        if (!isIntro || bar >= 2) {
            kick(t0, 1); kick(t0 + 2 * SPB, 0.85);
            if (intense || isClimax) { kick(t0 + 3.5 * SPB, 0.5); if (bar % 2) kick(t0 + 1.5 * SPB, 0.4); }
            brush(t0 + SPB, 1); brush(t0 + 3 * SPB, 1);
            /* 摇摆 ride: 正拍 + 后半拍(2/3 拍位) */
            for (let b = 0; b < 4; b++) { ride(t0 + b * SPB, 1); ride(t0 + b * SPB + SW, 0.72); }
            if (bar % 8 === 0) crash(t0, 0.9);
        }
        /* --- 走动低音: 每拍一个音, 在和弦音里游走 --- */
        if (!isIntro && !isOutro || bar >= 2) {
            const walk = [0, 4, 7, 10];   // 根音 -> 三音 -> 五音 -> 降七(接近下一和弦)
            for (let b = 0; b < 4; b++) {
                const n = ch.root + T + walk[(b + bar) % 4];
                bass(t0 + b * SPB, n, SPB * 0.92, b === 0 ? 0.30 : 0.25);
            }
        }
        /* --- 铜管切分(大乐队 hits) --- */
        if (!isIntro && !isBreak) {
            const hits = intense || isClimax ? [1 + 2 / 3, 2.5, 3 + 2 / 3] : [1 + 2 / 3, 3 + 2 / 3];
            for (const h of hits) brass(t0 + h * SPB, notes, SPB * 0.42, 0.15);
            if (isClimax) brass(t0, notes.slice(1), SPB * 0.3, 0.12);
        }
        /* --- 主题(单簧管) --- */
        const leadOn = sect === 'A2' || sect === 'B' || sect === 'climax' || sect === 'break';
        if (leadOn) {
            const ph = MOTIF[(bar - 4 + MOTIF.length) % MOTIF.length];
            for (let i = 0; i < 4; i++) {
                const t = t0 + i * SPB + (i % 2 === 1 ? SW - SPB : 0);   // 后半拍摇摆
                const dur = SPB * (i % 2 === 1 ? 0.55 : 0.9);
                const m = 69 + T + ph[i];
                lead(t, m, dur, i === 0 ? 0.19 : 0.16);
                if (intense || isClimax) lead(t, m - 12, dur, 0.06);
            }
        }
        /* --- 二阶段/高潮加风琴垫 --- */
        if (intense && (isClimax || sect === 'B' || sect === 'break')) {
            organ(t0, notes, BAR * 0.95, isClimax ? 0.055 : 0.042);
        }
        /* --- 收尾长和弦 --- */
        if (isOutro && bar === song.length - 1) brass(t0, notes, BAR * 0.9, 0.2);
    }

    let peak = 0;
    for (let i = 0; i < N; i++) {
        L[i] = Math.tanh(L[i] * 2.2); R[i] = Math.tanh(R[i] * 2.2);   // 2026-09-12: 加大压缩, 把 RMS 从 0.095 抬到和其他 BGM 一个量级
        peak = Math.max(peak, Math.abs(L[i]), Math.abs(R[i]));
    }
    const g = 0.78 / (peak || 1);   // 留余量: Vorbis 编码后峰值会过冲 ~0.07
    for (let i = 0; i < N; i++) { L[i] *= g; R[i] *= g; }
    return { seconds: N / SR, bars: song.length };
}

const dir = path.join(ROOT, 'src/main/resources/assets/ftboss/sounds/music');
fs.mkdirSync(dir, { recursive: true });
for (const j of [
    { file: 'apple.ogg', title: 'Orchard Don', bpm: 108, transpose: 0, phase: 1 },
    { file: 'apple_p2.ogg', title: 'Orchard Don (Grub Rampage)', bpm: 132, transpose: 2, phase: 2 },
]) {
    const info = render(j);
    const ogg = encode({ channels: 2, sampleRate: SR, frames: N, data: [L, R] }, 0.15,
        { TITLE: j.title, ARTIST: 'FTBoss' });
    fs.writeFileSync(path.join(dir, j.file), ogg);
    console.log(`${j.file}  ${(ogg.length / 1024).toFixed(0)} KB  ${info.seconds.toFixed(1)}s  ${info.bars} 小节  ${j.bpm} BPM  移调 ${j.transpose >= 0 ? '+' : ''}${j.transpose}  《${j.title}》`);
}
