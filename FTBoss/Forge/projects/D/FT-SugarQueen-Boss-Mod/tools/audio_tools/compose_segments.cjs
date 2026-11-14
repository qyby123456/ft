/* ============================================================================
 * 《Boss 战 24 分钟组曲》第二段 / 第三段 + 短文件(intro / outro / transition)
 * 2026-09-13 —— 与 compose_segment01.cjs 完全同一套工程约定：
 *
 *   · 120 BPM / 4/4 / D 小调 / 1 小节 = 2 秒 (与第一段严格同速同调, 段间才能无缝)
 *   · 每段 240 小节 = 480.000 s
 *   · 输出 4 条**等长、同起点**的线性 Stem: base / pulse / percussion / bass
 *     (线性 = 不做限幅, 4 条相加 == 整轨; 峰值由统一系数控制)
 *   · 先按 STEM_TRIM 配平, 再统一缩放到 GAIN_TARGET, 保证"任意层子集"都不削顶
 *
 * ★ 为什么只有 4 条 stem 而不是规格里的 7 层（重要, 改动前先读）：
 *   8 分钟的 stem 只能走 `"stream": true`, 而 Minecraft 的**流式声道池 =
 *   clamp((int)sqrt(总源数), 2, 8) —— 最多 8 条**（Library.loadLibrary 反编译结论）。
 *   4 条 × 交叉淡化时新旧重叠 = 8 条, 已经卡满池, 原版音乐/唱片会被饿死。
 *   所以 strings / choir / boss / finale 这四层**写进 base 与 pulse 的内容里**
 *   （由段内的 intensity 渐变体现), 运行时仍只暴露 4 个可控轴。
 *   如果非要 7 条独立 stem, 必须先解决声道池问题, 不要直接加 stem。
 *
 * ★ 段内硬切点：0:00 / 4:00(第 120 小节) / 8:00 —— 12 分钟档要按 4:00 切, 别打乱。
 *
 * 用法:
 *   node tools/audio_tools/compose_segments.cjs --seg 2
 *   node tools/audio_tools/compose_segments.cjs --seg 3
 *   node tools/audio_tools/compose_segments.cjs --seg short
 *   node tools/audio_tools/compose_segments.cjs --seg all --out <目录>
 *   node tools/audio_tools/compose_segments.cjs --seg 2 --bars 8     # 冒烟测试(只渲 8 小节)
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const S = require('./synth.cjs');

const BPM = 120;
const BAR = 60 / BPM * 4;                  // 2.0 秒
const DEFAULT_BARS = 240;                  // 240 × 2s = 480s = 8:00
const GAIN_TARGET = 0.85;                  // ≈ -1.4 dBFS, 与第一段一致(三段必须等电平)

const argv = process.argv.slice(2);
const arg = (n, d) => { const i = argv.indexOf('--' + n); return i >= 0 && argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[i + 1] : d; };
const OUTDIR = arg('out', path.join(__dirname, '..', '..', 'src', 'main', 'resources', 'assets', 'ftboss', 'sounds', 'music'));
const ONLY = arg('seg', 'all');
const DEBUG_BARS = parseInt(arg('bars', '0'), 10) || 0;
/** --mix: 额外写一条 `segment_0X_full_mix.ogg`(4 层线性相加的整轨), 给"试听唱片"用。
 *  注意: 是**已 finish 过的 stem 之和**, 不再二次 finish —— 所以它与游戏里 4 层全开的
 *  实际输出逐样本相同(唱片听到的就是正式效果)。 */
const WRITE_MIX = argv.includes('--mix');

/* ---------------- 和声 ---------------- */
const CHORD = {
    Dm: ['D', 'F', 'A'], Gm: ['G', 'Bb', 'D'], Bb: ['Bb', 'D', 'F'],
    C: ['C', 'E', 'G'], A: ['A', 'C#', 'E'], Eb: ['Eb', 'G', 'Bb'], F: ['F', 'A', 'C'],
};
const RISE4 = ['Dm', 'C', 'Bb', 'A'];       // 连战升级: Dm→C→Bb→A

/* ---------------- 主题(与第一段同一素材, 保证 24 分钟是"一首曲") ---------------- */
const THEME8 = [
    ['D4', 'F4', 'G4', 'A4'], ['A4', 'G4', 'F4', 'D4'], ['Bb3', 'D4', 'F4', 'G4'], ['F4', 'D4', 'C4', 'A3'],
    ['D4', 'F4', 'G4', 'A4'], ['C5', 'A4', 'G4', 'F4'], ['E4', 'F4', 'G4', 'A4'], ['A4', 'G4', 'F4', 'D4'],
];
const APPLE_MOTIF = ['D4', 'A3', 'D4', 'F4', 'E4', 'D4'];
const CHOIR_LINE = ['A3', 'A3', 'Bb3', 'A3', 'G3', 'F3', 'E3', 'D3'];   // 合唱长音线, 落在和弦音上

/* ---------------- 两段的段落表 ---------------- */
const SEGMENTS = {
    2: {
        file: 'segment_02',
        label: '第二段 · Boss 4-7「推进·变奏」',
        loop8: ['Dm', 'Dm', 'Gm', 'Gm', 'Bb', 'C', 'Dm', 'A'],
        // [起始小节, 结束小节, 强度 0..5]
        plan: [[0, 32, 1], [32, 80, 2], [80, 120, 3], [120, 168, 4], [168, 200, 4], [200, 224, 5], [224, 240, 2]],
        tail: { 238: 'Dm', 239: 'A' },       // 收束留悬念, 接第三段的 Dm
        stringsOct: 4,                        // 弦乐垫音区(比第一段高八度 → 更亮)
        choirFrom: 4, brassFrom: 4, pulseOctStrong: 1, motifEvery: 4,
        hollow: null,
    },
    3: {
        file: 'segment_03',
        label: '第三段 · Boss 8-11「终局·全奏」',
        loop8: ['Dm', 'Dm', 'Bb', 'C', 'Dm', 'Dm', 'Eb', 'A'],   // 含 ♭II 那不勒斯
        plan: [[0, 32, 5], [32, 80, 5], [80, 120, 5], [120, 160, 0], [160, 200, 5], [200, 224, 5], [224, 240, 3]],
        tail: { 236: 'Dm', 237: 'Dm', 238: 'Dm', 239: 'Dm' },     // 回主和弦 → 可无缝循环(32 分钟档要用)
        stringsOct: 4,
        choirFrom: 2, brassFrom: 2, pulseOctStrong: 1, motifEvery: 2,
        hollow: [120, 160],                   // 抽空段: 只留 base 的钟/拨弦 + 心跳
    },
};

const ctxOf = (n) => {
    const seg = SEGMENTS[n];
    const bars = DEBUG_BARS || DEFAULT_BARS;
    return { n, ...seg, bars, total: bars * BAR };
};

/** 小节 -> 和弦。200~224 小节走 Dm→C→Bb→A 推进, 尾小节用显式覆盖保证转场/循环 */
function chordOf(ctx, bar) {
    if (ctx.tail[bar]) return ctx.tail[bar];
    if (bar >= 200 && bar < 224) return RISE4[Math.floor(bar / 2) % 4];
    return ctx.loop8[bar % 8];
}
const intensityOf = (ctx, bar) => {
    for (const [from, to, v] of ctx.plan) if (bar >= from && bar < to) return v;
    return ctx.plan[ctx.plan.length - 1][2];
};
const isHollow = (ctx, bar) => !!ctx.hollow && bar >= ctx.hollow[0] && bar < ctx.hollow[1];

/* ---------------- 噪声鼓的确定性种子(两遍渲染必须一致) ---------------- */
const KIND = { kick: 1, snare: 2, tom: 3, cymbal: 4, riser: 5, gong: 6, impact: 7, timpani: 8 };
const srand = (bar, slot, kind) => S.setSeed((bar * 1000003 + slot * 131 + kind * 7919) >>> 0);

const STEMS = ['base', 'pulse', 'percussion', 'bass'];
/** 与第一段完全相同的配平 —— 三段必须等电平, 否则连战里跨段会忽大忽小 */
const STEM_TRIM = { base: 2.4, pulse: 2.5, percussion: 0.85, bass: 1.7 };

/* ============================ 各层的小节渲染 ============================ */
function renderBar(ctx, bus, bar, stem) {
    const T = bar * BAR;
    const chord = chordOf(ctx, bar);
    const notes = CHORD[chord];
    const inten = intensityOf(ctx, bar);
    const hollow = isHollow(ctx, bar);
    const beat = (b) => T + b * (BAR / 4);
    const isFillBar = (bar % 8) === 7;

    /* ---------- base: 持续音 + 弦乐垫 (+ 合唱 + 钟), strings/choir 层写在这里 ---------- */
    if (stem === 'base') {
        if (hollow) {
            // 抽空段: 只有钟与拨弦(钢琴化的"世界安静"), 外加一拍一次的心跳
            if (bar % 2 === 0) {
                const nm = THEME8[(bar / 2) % 8][0];
                S.bell(bus, T, S.freq(nm, 5), 0.42, 0.7);
                S.pluck(bus, T + BAR / 4, S.freq(nm, 4), 0.5, 0.42, 0.5);
            }
            S.drone(bus, T, BAR * 1.02, S.freq(notes[0], 2), 0.30, 0.5);
            S.kick(bus, T + BAR / 2, 0.30, 0.15);            // 心跳, 不是战鼓
            return;
        }
        const lvl = inten <= 1 ? 0.85 : inten === 2 ? 0.72 : 0.62;
        S.drone(bus, T, BAR * 1.05, S.freq(notes[0], 2), 0.55 * lvl, 0.55);
        S.strings(bus, T, BAR * 0.99, S.freq(notes[0], ctx.stringsOct), 0.50 * lvl, 0.28, 0.55);
        S.strings(bus, T, BAR * 0.99, S.freq(notes[1], ctx.stringsOct), 0.42 * lvl, 0.52, 0.55);
        S.strings(bus, T, BAR * 0.99, S.freq(notes[2], ctx.stringsOct), 0.38 * lvl, 0.74, 0.55);
        if (inten >= ctx.choirFrom && bar % 4 === 0) {
            const cn = CHOIR_LINE[(bar / 4) % 8];
            S.choir(bus, T, BAR * 3.95, S.freq(cn, 3), 0.50, 0.42, 0.72);
            S.choir(bus, T, BAR * 3.95, S.freq(cn === 'D3' ? 'A3' : cn, 3), 0.34, 0.62, 0.72);
        }
        if (inten >= 4 && bar % 4 === 2) S.bell(bus, T, S.freq(notes[0], 5), 0.30, 0.65);
        if (bar >= ctx.bars - 2) S.strings(bus, T, BAR * 1.6, S.freq(notes[0], 3), 0.5, 0.5, 0.65);
        return;
    }

    /* ---------- bass: 低音线 ---------- */
    if (stem === 'bass') {
        if (hollow || inten < 1) return;
        const root = S.freq(notes[0], 2), fifth = S.freq(notes[2], 2);
        if (inten <= 2) {
            S.bass(bus, beat(0), BAR / 2 * 0.92, root, 0.9);
            S.bass(bus, beat(2), BAR / 2 * 0.92, inten === 1 ? root : fifth, 0.8);
        } else if (inten <= 4) {
            for (let b = 0; b < 4; b++) S.bass(bus, beat(b), BAR / 4 * 0.9, b % 2 === 0 ? root : fifth, b === 0 ? 0.95 : 0.72);
        } else {
            for (let e = 0; e < 8; e++) {
                const f = e % 4 === 0 ? root : (e % 2 === 0 ? fifth : S.freq(notes[0], 3));
                S.bass(bus, T + e * BAR / 8, BAR / 8 * 0.85, f, e === 0 ? 1.0 : 0.6);
            }
        }
        return;
    }

    /* ---------- pulse: 主题旋律 + 八分脉冲 (+ 铜管加倍 / 苹果动机) ---------- */
    if (stem === 'pulse') {
        if (hollow || inten < 2) return;
        const t8 = THEME8[bar % 8];
        const strong = inten >= ctx.brassFrom;
        for (let i = 0; i < 4; i++) {
            const name = t8[i];
            const f = S.freq(name, strong ? ctx.pulseOctStrong : 0);
            (strong ? S.brass : S.cello)(bus, beat(i), BAR / 4 * (strong ? 0.82 : 0.95), f, strong ? 0.74 : 0.6, 0.45, 0.42);
            if (strong) S.cello(bus, beat(i), BAR / 4 * 0.9, S.freq(name, 0), 0.45, 0.3, 0.4);
            if (bar % 8 >= 4) {
                const mid = S.freq(name, 0) * Math.pow(2, 1 / 12);
                S.cello(bus, beat(i) + BAR / 8, BAR / 8 * 0.8, mid, 0.26, 0.55, 0.35);
            }
        }
        const pl = inten === 2 ? 0.5 : inten === 3 ? 0.65 : inten === 4 ? 0.8 : 0.95;
        for (let e = 0; e < 8; e++) {
            const f = S.freq(notes[e % 2 === 0 ? 0 : 2], 3);
            S.pulse(bus, T + e * BAR / 8, BAR / 8 * 0.8, f * (e % 4 === 0 ? 1 : 2), pl, e % 2 ? 0.62 : 0.38, 0.28);
        }
        if (bar % ctx.motifEvery === 0 && inten >= 3) {
            let t = 0;
            for (let i = 0; i < APPLE_MOTIF.length; i++) {
                const d = i === APPLE_MOTIF.length - 1 ? 2 : 1;
                S.cello(bus, T + t * BAR / 4, d * BAR / 4 * 0.95, S.freq(APPLE_MOTIF[i], 0), 0.68, 0.35, 0.45);
                t += d;
            }
        }
        return;
    }

    /* ---------- percussion ---------- */
    if (stem === 'percussion') {
        if (hollow || inten < 3) return;
        for (let b = 0; b < 4; b++) { srand(bar, b, KIND.kick); S.kick(bus, beat(b), b === 0 ? 1.0 : 0.78); }
        srand(bar, 10, KIND.snare); S.snare(bus, beat(1), 0.85);
        srand(bar, 11, KIND.snare); S.snare(bus, beat(3), 0.95);
        srand(bar, 12, KIND.tom); S.tom(bus, beat(0), 150, 0.7);
        srand(bar, 13, KIND.tom); S.tom(bus, beat(2), 130, 0.75);
        if (inten >= 4) {
            srand(bar, 14, KIND.cymbal); S.cymbal(bus, beat(0), 0.55);
            srand(bar, 15, KIND.snare); S.snare(bus, beat(2) + BAR / 8, 0.4);
        }
        if (inten >= 5) {
            for (let e = 1; e < 8; e += 2) { srand(bar, 20 + e, KIND.snare); S.snare(bus, T + e * BAR / 8, 0.35); }
            srand(bar, 30, KIND.cymbal); S.cymbal(bus, beat(2), 0.45);
        }
        if (isFillBar) {
            srand(bar, 40, KIND.kick); S.kick(bus, beat(0), 1.0);
            srand(bar, 41, KIND.gong); S.gong(bus, beat(1), S.freq(notes[0], 2), 0.85);
            srand(bar, 42, KIND.riser); S.riser(bus, beat(2), BAR / 4 * 0.95, 0.9);
            srand(bar, 43, KIND.impact); S.impact(bus, beat(3), 0.95);
        }
        if (inten >= 4 && bar % 4 === 0) { srand(bar, 50, KIND.timpani); S.timpani(bus, beat(0), S.freq(notes[0], 2), 0.7); }
        if (inten >= 4 && bar % 4 === 2) { srand(bar, 51, KIND.timpani); S.timpani(bus, beat(2), S.freq(notes[2], 2), 0.6); }
        // 抽空段回来的那一小节: 一记冲击当成"重新开战"
        if (ctx.hollow && bar === ctx.hollow[1]) {
            srand(bar, 70, KIND.impact); S.impact(bus, beat(0), 1.0);
            srand(bar, 71, KIND.gong); S.gong(bus, beat(0), S.freq('D', 2), 0.8);
        }
        return;
    }
}
/** S 的音色函数都自带 bus.add, 这里不再需要包装 */

/* ============================ 渲染 / 汇总 / 编码 ============================ */
function scaleInPlace(bus, k) { for (let i = 0; i < bus.L.length; i++) { bus.L[i] *= k; bus.R[i] *= k; } }
function peakOf(bus) { let p = 0, sq = 0, n = 0; for (let i = 0; i < bus.L.length; i++) { const a = Math.abs(bus.L[i]); if (a > p) p = a; const b = Math.abs(bus.R[i]); if (b > p) p = b; sq += bus.L[i] * bus.L[i] + bus.R[i] * bus.R[i]; n += 2; } return { peak: p, rms: Math.sqrt(sq / n) }; }
/** 4 条 stem 线性相加后的真峰值(不额外开一个大数组) */
function combinedPeak(buses) {
    const arrs = STEMS.map((s) => buses[s]);
    const len = arrs[0].L.length;
    let p = 0;
    for (let i = 0; i < len; i++) {
        let l = 0, r = 0;
        for (const b of arrs) { l += b.L[i]; r += b.R[i]; }
        const al = Math.abs(l), ar = Math.abs(r);
        if (al > p) p = al;
        if (ar > p) p = ar;
    }
    return p;
}
const db = (x) => (x <= 0 ? -99 : 20 * Math.log10(x));

function renderStem(ctx, stem) {
    const len = Math.round(ctx.total * S.SR);
    const bus = new S.Bus(len);
    const t0 = Date.now();
    for (let bar = 0; bar < ctx.bars; bar++) {
        renderBar(ctx, bus, bar, stem);
        if (ctx.bars > 32 && bar % 40 === 0 && bar > 0) {
            process.stdout.write(`\r    ${stem.padEnd(11)} ${(bar / ctx.bars * 100).toFixed(0)}%  (${((Date.now() - t0) / 1000).toFixed(0)}s)`);
        }
    }
    bus.finish(1, false);                     // ★ 线性: 绝不开限幅
    scaleInPlace(bus, STEM_TRIM[stem]);
    if (ctx.bars > 32) process.stdout.write(`\r    ${stem.padEnd(11)} 100%  (${((Date.now() - t0) / 1000).toFixed(0)}s)\n`);
    return bus;
}

async function renderSegment(n) {
    const ctx = ctxOf(n);
    console.log(`\n=== ${ctx.label}  ${(ctx.total / 60).toFixed(2)} 分钟 / ${ctx.bars} 小节 / ${BPM} BPM / D 小调 ===`);
    if (!fs.existsSync(OUTDIR)) fs.mkdirSync(OUTDIR, { recursive: true });

    console.log('  ① 逐层渲染并全部保留(只渲一遍; 4 条总线约 680 MB)…');
    const buses = {};
    for (const stem of STEMS) {
        buses[stem] = renderStem(ctx, stem);
        const p = peakOf(buses[stem]);
        console.log(`     ${stem.padEnd(11)} 配平 ${STEM_TRIM[stem]}×  峰值 ${p.peak.toFixed(3)} (${db(p.peak).toFixed(1)} dBFS)  RMS ${p.rms.toFixed(4)} (${db(p.rms).toFixed(1)} dB)`);
    }

    const cp = combinedPeak(buses);
    const g = GAIN_TARGET / cp;
    console.log(`  ② 整混峰值 ${cp.toFixed(3)} (${db(cp).toFixed(1)} dBFS) → 统一系数 ${g.toFixed(4)}`);

    for (const stem of STEMS) scaleInPlace(buses[stem], g);

    if (WRITE_MIX) {
        // 线性相加 == 游戏里 4 层全开。已经是 finish 过的样本, 所以**不能再 finish 一次**
        // (finish 里有立体声延迟, 二次调用会多一层回声)。
        const len = buses.base.L.length;
        const ML = new Float32Array(len), MR = new Float32Array(len);
        for (let i = 0; i < len; i++) {
            let l = 0, r = 0;
            for (const s of STEMS) { l += buses[s].L[i]; r += buses[s].R[i]; }
            ML[i] = l; MR[i] = r;
        }
        const out = path.join(OUTDIR, `${ctx.file}_full_mix.ogg`);
        const r = await S.encodeOgg(out, ML, MR, { vbrQuality: 0.5 });
        console.log(`     ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)} (${db(r.peak).toFixed(1)} dBFS)  RMS ${r.rms.toFixed(4)}  ${(r.size / 1048576).toFixed(2)} MB  ${(ctx.total).toFixed(3)} s  ← 试听唱片用`);
    }

    for (const stem of STEMS) {
        const out = path.join(OUTDIR, `${ctx.file}_${stem}.ogg`);
        const r = await S.encodeOgg(out, buses[stem].L, buses[stem].R, { vbrQuality: 0.5 });
        console.log(`     ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)} (${db(r.peak).toFixed(1)} dBFS)  RMS ${r.rms.toFixed(4)}  ${(r.size / 1048576).toFixed(2)} MB  ${(ctx.total).toFixed(3)} s`);
        buses[stem].L = null; buses[stem].R = null;   // 及时释放
    }
}

/* ---------------- 短文件: intro 0:30 / outro 1:00 / transition 0:04 ---------------- */
async function renderShort(which) {
    const spec = {
        intro: { secs: 30, label: '引子 0:30' },
        outro: { secs: 60, label: '尾声 1:00' },
        transition_01: { secs: 4, label: '过场 0:04 (#1)' },
        transition_02: { secs: 4, label: '过场 0:04 (#2)' },
    }[which];
    if (!spec) return;
    const bars = Math.round(spec.secs / BAR);
    const len = Math.round(bars * BAR * S.SR);
    const bus = new S.Bus(len);
    const notes = CHORD.Dm;
    console.log(`\n=== ${spec.label}  ${bars} 小节 ===`);
    // 每个文件不同种子 → transition_01/02 不会渲染成同一份字节
    S.setSeed(20260913 + which.length * 7919 + which.charCodeAt(which.length - 1) * 131);

    if (which === 'intro') {
        // 黑暗降临: 持续音 → 钟声主题碎片 → 上升镲接定音鼓, 停在 A(属和弦) 等第一段进
        for (let bar = 0; bar < bars; bar++) {
            const T = bar * BAR;
            S.drone(bus, T, BAR * 1.05, S.freq(notes[0], 2), bar < 6 ? 0.35 : 0.5, 0.55);
            if (bar >= 4) S.strings(bus, T, BAR * 0.99, S.freq(notes[bar % 2 ? 1 : 0], 4), 0.34, 0.3, 0.6);
            if (bar >= 6 && bar % 2 === 0) S.bell(bus, T, S.freq(THEME8[(bar / 2) % 8][0], 5), 0.4, 0.7);
            if (bar >= 10 && bar % 4 === 0) S.choir(bus, T, BAR * 3.95, S.freq('A3', 3), 0.3, 0.5, 0.75);
        }
        S.riser(bus, (bars - 2) * BAR, BAR * 1.2, 0.9, 0.6);
        S.timpani(bus, (bars - 1) * BAR, S.freq('A', 2), 0.85, 0.5);
        S.timpani(bus, (bars - 1) * BAR + BAR / 2, S.freq('A', 2), 0.6, 0.5);
    } else if (which === 'outro') {
        // 谢幕: 主和弦回归, 逐层抽走, 最后一个钟
        for (let bar = 0; bar < bars; bar++) {
            const T = bar * BAR;
            const fade = Math.max(0, 1 - bar / (bars - 6));
            const ch = bar < 12 ? CHORD.Dm : CHORD[bar % 8 < 4 ? 'Dm' : 'Bb'];
            S.strings(bus, T, BAR * 0.99, S.freq(ch[0], 4), 0.46 * fade + 0.12, 0.3, 0.6);
            S.strings(bus, T, BAR * 0.99, S.freq(ch[2], 4), 0.36 * fade + 0.1, 0.7, 0.6);
            if (bar < 20) S.choir(bus, T, BAR * 3.95, S.freq(ch[1], 3), 0.34 * fade, 0.5, 0.75);
            S.drone(bus, T, BAR * 1.05, S.freq(ch[0], 2), 0.4 * fade + 0.15, 0.55);
            if (bar % 4 === 0 && bar < 24) S.bell(bus, T, S.freq(THEME8[(bar / 4) % 8][0], 5), 0.32 * fade, 0.7);
        }
        S.bell(bus, (bars - 3) * BAR, S.freq('D', 5), 0.5, 0.8);
        S.gong(bus, (bars - 2) * BAR, S.freq('D', 2), 0.5, 0.7);
    } else {
        // 过场 2 小节: 上升镲 + 冲击, 第 2 小节第 1 拍落到 Dm, 供交叉淡化垫着用
        const T = 0;
        S.riser(bus, T, BAR * 1.9, 0.95, 0.6);
        S.impact(bus, T + BAR, 1.0, 0.6);
        S.gong(bus, T + BAR, S.freq('D', 2), 0.8, 0.7);
        S.timpani(bus, T + BAR, S.freq('D', 2), 0.8, 0.5);
        S.strings(bus, T + BAR, BAR * 0.95, S.freq('D', 4), 0.4, 0.4, 0.6);
    }

    bus.finish(1, true);                       // 短文件是单曲, 允许软限幅当兜底
    const p = peakOf(bus);
    scaleInPlace(bus, GAIN_TARGET / Math.max(p.peak, 1e-6));
    const out = path.join(OUTDIR, `${which}.ogg`);
    const r = await S.encodeOgg(out, bus.L, bus.R, { vbrQuality: 0.5 });
    console.log(`   ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)} (${db(r.peak).toFixed(1)} dBFS)  RMS ${r.rms.toFixed(4)}  ${(r.size / 1024).toFixed(0)} KB  ${(bars * BAR).toFixed(2)} s`);
}

/* ---------------- 技能音效: 落到 sounds/effects/ (MUSIC_SPEC §2) ---------------- */
async function renderSfx() {
    const dir = path.resolve(OUTDIR, '..', 'effects');
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    const SPEC = {
        //         秒数   生成内容
        apple_boss_attack: 1.0,
        apple_boss_seed: 1.3,
        apple_boss_dash: 1.6,
        apple_boss_death: 3.6,
    };
    console.log(`\n=== 技能音效 → ${dir} ===`);
    for (const [name, secs] of Object.entries(SPEC)) {
        const len = Math.round(secs * S.SR);
        const bus = new S.Bus(len);
        S.setSeed(20260913 + name.length * 7919 + name.charCodeAt(0) * 131);
        const D3 = S.freq('D', 3), D2 = S.freq('D', 2), A2 = S.freq('A', 2);

        if (name === 'apple_boss_attack') {
            // 挥击: 短促上扫噪声 + 低频闷响 + 木质撞击(苹果是"果实", 用木质感而非金属)
            S.riser(bus, 0, 0.30, 0.85, 0.45);
            S.impact(bus, 0.26, 0.95, 0.5);
            S.kick(bus, 0.26, 0.8, 0.2);
            S.tom(bus, 0.30, 210, 0.7, 0.3);
            S.snare(bus, 0.28, 0.5, 0.25);
        } else if (name === 'apple_boss_seed') {
            // 发射种子: 拨弦"啪" + 快速上滑 + 气流嘶声
            S.pluck(bus, 0, S.freq('D', 5), 0.9, 0.5, 0.2);
            S.pluck(bus, 0.05, S.freq('A', 5), 0.6, 0.5, 0.2);
            S.riser(bus, 0.05, 0.5, 0.55, 0.35);
            S.pulse(bus, 0.14, 0.22, S.freq('D', 4), 0.7, 0.5, 0.2);
            S.tom(bus, 0.10, 260, 0.45, 0.25);
        } else if (name === 'apple_boss_dash') {
            // 冲撞: 长一点的上扫 + 重冲击 + 定音鼓 + 镲, 末尾碎屑
            S.riser(bus, 0, 0.55, 0.95, 0.5);
            S.impact(bus, 0.52, 1.0, 0.6);
            S.timpani(bus, 0.52, A2, 0.9, 0.5);
            S.cymbal(bus, 0.52, 0.7, 0.5);
            S.tom(bus, 0.60, 180, 0.6, 0.3);
            S.snare(bus, 0.66, 0.35, 0.3);
        } else {
            // 死亡: 铜锣 + 重击 + 下行弦乐 + 低音持续尾
            S.gong(bus, 0, D2, 0.95, 0.7);
            S.impact(bus, 0.02, 1.0, 0.6);
            S.timpani(bus, 0.10, D2, 0.85, 0.5);
            for (let i = 0; i < 6; i++) {
                const f = S.freq(['D4', 'C4', 'Bb3', 'A3', 'G3', 'F3'][i], 0);
                S.cello(bus, 0.35 + i * 0.42, 0.95, f, 0.62, 0.45, 0.5);
                S.brass(bus, 0.35 + i * 0.42, 0.9, f / 2, 0.4, 0.5, 0.45);
            }
            S.drone(bus, 0.6, 2.9, D2, 0.42, 0.6);
            S.choir(bus, 1.0, 2.5, S.freq('D', 3), 0.35, 0.5, 0.75);
        }

        bus.finish(1, true);
        const p = peakOf(bus);
        scaleInPlace(bus, 0.92 / Math.max(p.peak, 1e-6));   // 音效可以比音乐更靠顶
        const out = path.join(dir, `${name}.ogg`);
        const r = await S.encodeOgg(out, bus.L, bus.R, { vbrQuality: 0.5 });
        console.log(`   ✓ ${path.basename(out)}  峰值 ${r.peak.toFixed(3)} (${db(r.peak).toFixed(1)} dBFS)  ${(r.size / 1024).toFixed(0)} KB  ${secs.toFixed(2)} s`);
    }
}

/* ---------------- main ---------------- */
(async function main() {
    console.log(`输出目录: ${OUTDIR}`);
    if (DEBUG_BARS) console.log(`【冒烟测试】只渲染 ${DEBUG_BARS} 小节`);
    if (ONLY === '2' || ONLY === 'all') await renderSegment(2);
    if (ONLY === '3' || ONLY === 'all') await renderSegment(3);
    if (ONLY === 'short' || ONLY === 'all') for (const w of ['intro', 'outro', 'transition_01', 'transition_02']) await renderShort(w);
    if (ONLY === 'sfx' || ONLY === 'all') await renderSfx();
    console.log('\n全部完成。');
})();
