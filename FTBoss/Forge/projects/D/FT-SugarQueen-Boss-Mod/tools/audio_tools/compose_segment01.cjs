/* ============================================================================
 * 《Boss 战 24 分钟组曲》第一段 segment_01 —— 8:00 / 120 BPM / 4/4 / D 小调
 * (2026-09-13 v99, 按用户给出的音乐规格制作)
 *
 * 严格照规格:
 *   · 总长 8:00 = 240 小节 × 2 秒(120 BPM, 4/4)
 *   · 和声循环 8 小节: Dm Dm | Bb Bb | F F | C C        (每和弦 2 小节)
 *   · 分区: 0:00-1:20 黑暗序幕 / 1:20-2:40 Boss1 腐化苹果 / 2:40-4:20 Boss2 赤焰南瓜
 *           4:20-6:20 Boss3 深渊胡萝卜 / 6:20-7:20 连战升级 / 7:20-8:00 段落收束
 *   · 主题旋律 = 规格里那 8 小节(D4 F4 G4 A4 / A4 G4 F4 D4 / Bb3 D4 F4 G4 / F4 D4 C4 A3 /
 *                     D4 F4 G4 A4 / C5 A4 G4 F4 / E4 F4 G4 A4 / A4 G4 F4 D4)
 *   · 苹果 Boss 动机 D4-A3-D4-F4-E4-D4
 *   · Boss 切换和声 Dm → C → Bb → A(A 是 D 小调的属和弦)
 *   · 打击乐: 基础 Kick 1,3 / Snare 2,4; Boss 战 Kick 全拍 + Snare 2,4 + Tom 1,3;
 *             每 8 小节末: 第1拍底鼓 / 第2拍铜锣 / 第3拍反向镲 / 第4拍冲击
 *
 * 输出 4 条**等长、同起点、同循环点**的 Stem(8:00 整):
 *   segment_01_base.ogg  segment_01_pulse.ogg  segment_01_percussion.ogg  segment_01_bass.ogg
 *
 * 两条重要工程约定(为了让游戏内能"随时淡入某一层"):
 *   ① 每条 Stem 都**线性**(不做限幅), 所以 4 条相加 == 整轨; 峰值由统一系数控制;
 *   ② 先跑一遍"全轨"测出整混峰值 P, 再按 0.85/P 的**同一个系数**缩放出各 Stem ——
 *      这样游戏里无论开了几层都不会削顶(开启的子集 ≤ 全量)。
 *
 * 用法: node tools/audio_tools/compose_segment01.cjs [输出目录]
 * ==========================================================================*/
const path = require('path');
const S = require('./synth.cjs');

const SR = S.SR;
const BPM = 120;
const BAR = 60 / BPM * 4;                  // 2.0 秒
const BARS = 240;                          // 240 × 2s = 480s = 8:00
const TOTAL = BARS * BAR;
/* 第一个非 -- 开头的参数才是输出目录(否则 --preview 会被当成目录名) */
const ARG_DIR = process.argv.slice(2).find((a) => !a.startsWith('--'));
const OUTDIR = ARG_DIR || path.join(__dirname, '..', '..',
    'src', 'main', 'resources', 'assets', 'ftboss', 'sounds', 'music');

/* ---------------- 和声 ---------------- */
const CHORD = {
    Dm: ['D', 'F', 'A'], Bb: ['Bb', 'D', 'F'], F: ['F', 'A', 'C'],
    C: ['C', 'E', 'G'], A: ['A', 'C#', 'E'],
};
const LOOP8 = ['Dm', 'Dm', 'Bb', 'Bb', 'F', 'F', 'C', 'C'];   // 规格给定的 8 小节循环
const RISE4 = ['Dm', 'C', 'Bb', 'A'];                          // Boss 切换: Dm→C→Bb→A

/** 小节 -> 和弦名。198 小节起(连战升级)换成 Dm→C→Bb→A 的推进, 收束段回到主和弦 */
function chordOf(bar) {
    if (bar >= 220) return bar % 8 < 4 ? 'Dm' : 'A';           // 收束: 主 → 属, 留悬念
    if (bar >= 190) return RISE4[Math.floor(bar / 2) % 4];      // 连战升级: 2 小节一个
    return LOOP8[bar % 8];
}

/* ---------------- 段落与强度 ---------------- */
const SECTIONS = [
    { name: '黑暗序幕',     from: 0,   to: 40 },
    { name: 'Boss1 腐化苹果', from: 40,  to: 80 },
    { name: 'Boss2 赤焰南瓜', from: 80,  to: 130 },
    { name: 'Boss3 深渊胡萝卜', from: 130, to: 190 },
    { name: '连战升级',     from: 190, to: 220 },
    { name: '段落收束',     from: 220, to: 240 },
];
const sectionOf = (bar) => SECTIONS.find((s) => bar >= s.from && bar < s.to) || SECTIONS[SECTIONS.length - 1];

/** 全局强度 0..5(决定各层的密度; 与游戏内"击败 Boss 数"解锁层数的节奏对齐) */
function intensity(bar) {
    if (bar < 16) return 0;                 // 只有 base
    if (bar < 40) return 1;                 // + bass
    if (bar < 80) return 2;                 // + pulse(主题进来)
    if (bar < 130) return 3;                // + percussion
    if (bar < 190) return 4;                // 全奏
    if (bar < 220) return 5;                // 连战升级(最密)
    return 3;                               // 收束
}

/* ---------------- 主题(规格原文的 8 小节) ---------------- */
const THEME8 = [
    ['D4', 'F4', 'G4', 'A4'],
    ['A4', 'G4', 'F4', 'D4'],
    ['Bb3', 'D4', 'F4', 'G4'],
    ['F4', 'D4', 'C4', 'A3'],
    ['D4', 'F4', 'G4', 'A4'],
    ['C5', 'A4', 'G4', 'F4'],
    ['E4', 'F4', 'G4', 'A4'],
    ['A4', 'G4', 'F4', 'D4'],
];
/** 苹果 Boss 动机 */
const APPLE_MOTIF = ['D4', 'A3', 'D4', 'F4', 'E4', 'D4'];

const oct = (n, k) => n.replace(/(\d)$/, (m, d) => String(parseInt(d, 10) + k));

/* ---------------- 噪声鼓的确定性种子(两遍渲染必须一致) ---------------- */
const KIND = { kick: 1, snare: 2, tom: 3, cymbal: 4, riser: 5, gong: 6, impact: 7, timpani: 8, click: 9 };
const srand = (bar, slot, kind) => S.setSeed((bar * 1000003 + slot * 131 + kind * 7919) >>> 0);

/* ============================ 各层的小节渲染 ============================ */
const STEMS = ['base', 'pulse', 'percussion', 'bass'];

/**
 * 渲染一个小节里属于 `stem` 的内容。
 * @param bus  目标总线(Tee: 同时写进"和轨道"用于测峰值)
 * @param bar  小节序号 0..239
 * @param stem 层名
 */
function renderBar(bus, bar, stem) {
    const T = bar * BAR;
    const chord = chordOf(bar);
    const notes = CHORD[chord];
    const inten = intensity(bar);
    const sec = sectionOf(bar);
    const beat = (b) => T + b * (BAR / 4);
    const isFillBar = (bar % 8) === 7;                      // 每 8 小节末的过门小节

    /* ---------- base: 持续音 + 弦乐垫 + 铃(始终存在, 是"底") ---------- */
    if (stem === 'base') {
        const lvl = inten === 0 ? 0.85 : inten <= 2 ? 0.7 : 0.6;
        // 低音持续音(黑暗序幕铺底)
        S.drone(bus, T, BAR * 1.05, S.freq(notes[0], 2), 0.55 * lvl, 0.55);
        // 弦乐垫: 三和弦长音
        if (inten >= 1 || bar % 4 !== 3) {
            S.strings(bus, T, BAR * 0.99, S.freq(notes[0], 3), 0.5 * lvl, 0.3, 0.55);
            S.strings(bus, T, BAR * 0.99, S.freq(notes[1], 3), 0.42 * lvl, 0.5, 0.55);
            S.strings(bus, T, BAR * 0.99, S.freq(notes[2], 3), 0.38 * lvl, 0.7, 0.55);
        }
        // 中后期加一层合唱垫(暗色)
        if (inten >= 4 && bar % 4 === 0) {
            S.choir(bus, T, BAR * 3.95, S.freq(notes[1], 3), 0.5, 0.45, 0.7);
            S.choir(bus, T, BAR * 3.95, S.freq(notes[2], 3), 0.42, 0.6, 0.7);
        }
        // 序幕末尾给一个主题的"钟声预兆"
        if (bar >= 32 && bar < 40 && bar % 2 === 0) {
            const n = THEME8[(bar - 32) % 8][0];
            S.bell(bus, T, S.freq(n, 5), 0.4, 0.7);
        }
        // 收束段: 最后两小节留一个悬着的属和弦长音
        if (bar >= 238) S.strings(bus, T, BAR * 1.6, S.freq('A', 3), 0.5, 0.5, 0.65);
        return;
    }

    /* ---------- bass: 低音线 ---------- */
    if (stem === 'bass') {
        if (inten < 1) return;
        const root = S.freq(notes[0], 2);
        const fifth = S.freq(notes[2], 2);
        if (inten <= 2) {                                   // 半音符: 1、3 拍
            S.bass(bus, beat(0), BAR / 2 * 0.92, root, 0.9);
            S.bass(bus, beat(2), BAR / 2 * 0.92, inten === 1 ? root : fifth, 0.8);
        } else if (inten <= 4) {                            // 四分音符: 每拍
            for (let b = 0; b < 4; b++) {
                S.bass(bus, beat(b), BAR / 4 * 0.9, b % 2 === 0 ? root : fifth, b === 0 ? 0.95 : 0.72);
            }
        } else {                                            // 连战升级: 八分音符驱动
            for (let e = 0; e < 8; e++) {
                const f = e % 4 === 0 ? root : (e % 2 === 0 ? fifth : S.freq(notes[0], 3));
                S.bass(bus, T + e * BAR / 8, BAR / 8 * 0.85, f, e === 0 ? 1.0 : 0.6);
            }
        }
        return;
    }

    /* ---------- pulse: 主题旋律 + 脉冲层 + 苹果动机 ---------- */
    if (stem === 'pulse') {
        if (inten < 2) return;
        const t8 = THEME8[bar % 8];                          // 8 小节主题循环
        const strong = inten >= 4;
        /* 主题: 前 4 小节四分音符为主, 后 4 小节加八分经过音(规格原文的建议) */
        for (let i = 0; i < 4; i++) {
            const nt = beat(i);
            const name = t8[i];
            const f = S.freq(name, strong ? 1 : 0);          // 强段翻高八度
            (strong ? S.brass : S.cello)(bus, nt, BAR / 4 * (strong ? 0.82 : 0.95), f, strong ? 0.75 : 0.6, 0.45, 0.42);
            if (strong) S.cello(bus, nt, BAR / 4 * 0.9, S.freq(name, 0), 0.45, 0.3, 0.4);
            if (bar % 8 >= 4) {                              // 后四小节: 补一个八分经过音
                const mid = S.freq(name, 0) * Math.pow(2, 1 / 12);   // 上方半音做"挤"感
                S.cello(bus, nt + BAR / 8, BAR / 8 * 0.8, mid, 0.28, 0.55, 0.35);
            }
        }
        /* 脉冲层: 八分音符 staccato(第 2 层标志性律动) */
        const pl = inten === 2 ? 0.5 : inten === 3 ? 0.65 : inten === 4 ? 0.8 : 0.95;
        for (let e = 0; e < 8; e++) {
            const f = S.freq(notes[e % 2 === 0 ? 0 : 2], 3);
            S.pulse(bus, T + e * BAR / 8, BAR / 8 * 0.8, f * (e % 4 === 0 ? 1 : 2), pl, e % 2 ? 0.62 : 0.38, 0.28);
        }
        /* 苹果 Boss 动机: Boss1 段每 8 小节出现一次; Boss3 起更频繁 */
        const motifEvery = inten >= 4 ? 4 : 8;
        if (bar % motifEvery === 0 && inten >= 3) {
            let t = 0;
            for (let i = 0; i < APPLE_MOTIF.length; i++) {
                const d = i === APPLE_MOTIF.length - 1 ? 2 : 1;      // 最后一个音延长
                S.cello(bus, T + t * BAR / 4, d * BAR / 4 * 0.95, S.freq(APPLE_MOTIF[i], 0), 0.7, 0.35, 0.45);
                t += d;
            }
        }
        return;
    }

    /* ---------- percussion: 打击乐(规格原文的节奏型) ---------- */
    if (stem === 'percussion') {
        if (inten < 3) return;
        const bossStyle = inten >= 3;
        if (!bossStyle) {                                   // 基础: Kick 1、3 / Snare 2、4
            srand(bar, 0, KIND.kick); S.kick(bus, beat(0), 0.9);
            srand(bar, 1, KIND.kick); S.kick(bus, beat(2), 0.85);
            srand(bar, 2, KIND.snare); S.snare(bus, beat(1), 0.8);
            srand(bar, 3, KIND.snare); S.snare(bus, beat(3), 0.9);
            return;
        }
        // Boss 战: Kick 全拍 + Snare 2、4 + Tom 1、3
        for (let b = 0; b < 4; b++) {
            srand(bar, b, KIND.kick); S.kick(bus, beat(b), b === 0 ? 1.0 : 0.78);
        }
        srand(bar, 10, KIND.snare); S.snare(bus, beat(1), 0.85);
        srand(bar, 11, KIND.snare); S.snare(bus, beat(3), 0.95);
        srand(bar, 12, KIND.tom); S.tom(bus, beat(0), 150, 0.7);
        srand(bar, 13, KIND.tom); S.tom(bus, beat(2), 130, 0.75);
        if (inten >= 4) {
            srand(bar, 14, KIND.cymbal); S.cymbal(bus, beat(0), 0.55);
            srand(bar, 15, KIND.snare); S.snare(bus, beat(2) + BAR / 8, 0.4);        // 反拍
        }
        if (inten >= 5) {                                   // 连战升级: 双倍鼓点
            for (let e = 1; e < 8; e += 2) { srand(bar, 20 + e, KIND.snare); S.snare(bus, T + e * BAR / 8, 0.35); }
            srand(bar, 30, KIND.cymbal); S.cymbal(bus, beat(2), 0.45);
        }
        // 每 8 小节末的过门: 第1拍底鼓 / 第2拍铜锣 / 第3拍反向镲 / 第4拍冲击
        if (isFillBar) {
            srand(bar, 40, KIND.kick); S.kick(bus, beat(0), 1.0);
            srand(bar, 41, KIND.gong); S.gong(bus, beat(1), S.freq(notes[0], 2), 0.85);
            srand(bar, 42, KIND.riser); S.riser(bus, beat(2), BAR / 4 * 0.95, 0.9);
            srand(bar, 43, KIND.impact); S.impact(bus, beat(3), 0.95);
        }
        // 定音鼓加强(中后期)
        if (inten >= 4 && bar % 4 === 0) {
            srand(bar, 50, KIND.timpani); S.timpani(bus, beat(0), S.freq(notes[0], 2), 0.7);
        }
        if (inten >= 4 && bar % 4 === 2) {
            srand(bar, 51, KIND.timpani); S.timpani(bus, beat(2), S.freq(notes[2], 2), 0.6);
        }
        // 收束段: 最后一小节一记冲击收尾
        if (bar === 239) { srand(bar, 60, KIND.impact); S.impact(bus, beat(0), 1.0); srand(bar, 61, KIND.gong); S.gong(bus, beat(2), S.freq('A', 2), 0.8); }
        return;
    }
}

/* ============================ 渲染 ============================ */
/**
 * 分轨配平(混音平衡) —— 第一版实渲染出来: 打击乐峰值 0.821 而 base/pulse/bass 只有
 * 0.083/0.144/0.091, 等于"鼓 + 一层很淡的垫音", 旋律完全被压没。原因: 鼓是瞬态, 峰值天然高,
 * 而持续音层(垫音/低音/脉冲)本身电平就低。这里按**实测 RMS** 给每条 Stem 一个固定配平系数,
 * 让四层的响度靠拢(鼓不再是唯一主角), 之后再统一缩放到目标峰值。
 */
const STEM_TRIM = { base: 2.4, pulse: 2.5, percussion: 0.85, bass: 1.7 };

function scaleInPlace(bus, k) {
    for (let i = 0; i < bus.L.length; i++) { bus.L[i] *= k; bus.R[i] *= k; }
}
function addInto(dst, src) {
    for (let i = 0; i < dst.L.length; i++) { dst.L[i] += src.L[i]; dst.R[i] += src.R[i]; }
}

/** 只渲染一条 Stem(整轨 240 小节), finish 成线性 */
function renderStem(stem, label) {
    const len = Math.round(TOTAL * SR);
    const bus = new S.Bus(len);
    const t0 = Date.now();
    for (let bar = 0; bar < BARS; bar++) {
        renderBar(bus, bar, stem);
        if (bar % 40 === 0) process.stdout.write(`\r  [${label}] ${(bar / BARS * 100).toFixed(0)}%  (${((Date.now() - t0) / 1000).toFixed(0)}s)`);
    }
    bus.finish(1, false);
    scaleInPlace(bus, STEM_TRIM[stem]);
    process.stdout.write(`\r  [${label}] 100%  (${((Date.now() - t0) / 1000).toFixed(0)}s)\n`);
    return bus;
}

function peakOf(bus) {
    let p = 0, sq = 0, n = 0;
    for (let i = 0; i < bus.L.length; i++) {
        const a = Math.abs(bus.L[i]); if (a > p) p = a;
        const b = Math.abs(bus.R[i]); if (b > p) p = b;
        sq += bus.L[i] * bus.L[i] + bus.R[i] * bus.R[i]; n += 2;
    }
    return { peak: p, rms: Math.sqrt(sq / n) };
}
const db = (x) => (x <= 0 ? -99 : 20 * Math.log10(x));

/**
 * 试听用整混版(不注册进游戏, 只给人听): 4 层按配平相加, 缩放到 0.85。
 * 游戏里是**分别**播放 4 条 Stem, 这个文件只是为了"一键试听"。
 */
async function renderPreviewMix() {
    const len = Math.round(TOTAL * SR);
    console.log('生成试听整混版…');
    const sum = new S.Bus(len);
    for (const stem of STEMS) {
        const bus = renderStem(stem, `preview·${stem}`);
        addInto(sum, bus);
        bus.L = null; bus.R = null;
    }
    sum.finish(1, false);
    const p = peakOf(sum);
    scaleInPlace(sum, 0.85 / p.peak);
    const out = path.join(OUTDIR, 'segment_01_full_mix.ogg');
    const r = await S.encodeOgg(out, sum.L, sum.R, { vbrQuality: 0.5 });
    console.log(`   ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)}  RMS ${r.rms.toFixed(4)} (${db(r.rms).toFixed(1)} dB)  ${(r.size / 1048576).toFixed(2)} MB`);
}

(async function main() {
    if (process.argv.includes('--preview')) { await renderPreviewMix(); return; }
    console.log(`《Boss 战组曲》第一段 segment_01  ${(TOTAL / 60).toFixed(2)} 分钟 / ${BARS} 小节 / ${BPM} BPM / D 小调`);
    const len = Math.round(TOTAL * SR);

    console.log('① 逐层渲染 + 配平, 同时累加成"整轨"以测整混峰值(4 层全开的最坏情况)…');
    const sum = new S.Bus(len);
    for (const stem of STEMS) {
        const bus = renderStem(stem, `测量·${stem}`);
        addInto(sum, bus);                       // 线性相加 == 整轨
        const p = peakOf(bus);
        console.log(`     ${stem.padEnd(11)} 配平 ${STEM_TRIM[stem]}×  峰值 ${p.peak.toFixed(3)}  RMS ${p.rms.toFixed(4)} (${db(p.rms).toFixed(1)} dB)`);
        bus.L = null; bus.R = null;              // 让出内存(4 条一起留会占 ~680 MB)
    }
    sum.finish(1, false);
    const m = peakOf(sum);
    console.log(`   整混: 峰值 ${m.peak.toFixed(3)} (${db(m.peak).toFixed(1)} dBFS)  RMS ${m.rms.toFixed(4)} (${db(m.rms).toFixed(1)} dB)`);
    sum.L = null; sum.R = null;

    const GAIN_TARGET = 0.85;                    // ≈ -1.4 dBFS, 满足"峰值不高于 -1 dBTP"的余量
    const g = GAIN_TARGET / m.peak;
    console.log(`② 统一系数 ${g.toFixed(4)}(×配平) 缩放出 4 条 Stem(线性可相加)并编码…`);

    for (const stem of STEMS) {
        const bus = renderStem(stem, stem);
        scaleInPlace(bus, g);
        const p = peakOf(bus);
        const out = path.join(OUTDIR, `segment_01_${stem}.ogg`);
        const r = await S.encodeOgg(out, bus.L, bus.R, { vbrQuality: 0.5 });
        console.log(`   ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)} (${db(r.peak).toFixed(1)} dBFS)  ` +
            `RMS ${r.rms.toFixed(4)} (${db(r.rms).toFixed(1)} dB)  ${(r.size / 1048576).toFixed(2)} MB`);
        bus.L = null; bus.R = null;
    }
    console.log(`全部写出到 ${OUTDIR}`);
})();
