/*
 * 《堆肥·冬夜》—— 炫彩答辩超人 BGM (2026-09-12)
 * 风格取向: 钢琴抒情独奏 / 小调 / 循环低音型 / 冬夜静谧渐强  (致敬坂本龙一那种"留白钢琴"的味道)
 * **旋律完全原创**, 不引用任何现有作品的主题。
 * 结构: A 引子(固定低音型) -> B 主题(右手旋律) -> C 中段(转平行大调, 加弦乐垫) -> D 高潮(高八度+和弦) -> E 收束(单音消散)
 * 速度 76 BPM, 4/4, 约 2 分 20 秒; 以质量 0.15 编码(约 1.1MB)
 * 同时注册音效: assets/ftboss/sounds.json 的 boss_music_compost + FTSounds.BOSS_MUSIC_COMPOST
 * 用法: node render_compost_music.cjs <项目根>
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const { encode } = require(path.join(ROOT, 'tools/music/wav2ogg.cjs'));

const SR = 44100, BPM = 76, SPB = 60 / BPM;
const BARS = 44, DUR = BARS * 4 * SPB + 4.0;
const N = Math.floor(DUR * SR);
const L = new Float32Array(N), R = new Float32Array(N);
const rnd = (() => { let s = 20260912; return () => (s = (s * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff * 2 - 1; })();
const hz = m => 440 * Math.pow(2, (m - 69) / 12);

/* ---------- 钢琴音色: 多分音 + 非谐性 + 击弦噪声 + 指数衰减 ---------- */
function piano(t, f, amp, bright) {
    if (t < 0) return 0;
    const inh = 1 + 0.00042 * f;                       // 非谐性(越高越明显)
    let s = 0;
    const parts = [[1, 1.0, 3.1], [2.001 * inh, 0.42, 4.4], [3.004 * inh, 0.2, 5.8], [4.01 * inh, 0.11, 7.2], [5.02 * inh, 0.06, 8.8]];
    for (const [k, a, d] of parts) s += Math.sin(2 * Math.PI * f * k * t) * a * Math.exp(-t * d);
    s += rnd() * Math.exp(-t * 260) * 0.5;             // 击弦瞬态
    const atk = 1 - Math.exp(-t * 420);
    return s * amp * atk * (0.72 + 0.28 * (bright === undefined ? 1 : bright));
}
/* ---------- 弦乐垫: 慢起 + 轻微失谐 ---------- */
function pad(t, f, amp) {
    if (t < 0) return 0;
    const a = Math.min(1, t / 1.4) * Math.exp(-t * 0.16);
    return (Math.sin(2 * Math.PI * f * t) * 0.5 + Math.sin(2 * Math.PI * f * 1.004 * t) * 0.34 + Math.sin(2 * Math.PI * f * 0.996 * t) * 0.28) * amp * a;
}
const mix = (t, l, r) => { const i = Math.floor(t * SR); if (i >= 0 && i < N) { L[i] += l; R[i] += r; } };
function note(t0, midi, amp, pan = 0.5, bright) {
    const f = hz(midi), dur = 3.2;
    const len = Math.floor(dur * SR);
    for (let i = 0; i < len; i++) {
        const t = i / SR;
        let v = piano(t, f, amp, bright);
        /* 高音衰减更快, 低音留得更久 */
        if (midi < 55) v *= 1 + 0.6 * Math.exp(-t * 1.0);
        mix(t0 + t, v * (1 - pan), v * pan);
    }
}
/* ---------- 结构 ---------- */
const A = [57, 60, 64], F = [53, 57, 60], C = [48, 55, 60], G = [55, 59, 62], E = [52, 56, 59];
const PROG = [A, F, C, G];
/* 右手主题(原创): 以 A 小调为核心的抒情线条, 每 4 小节一句 */
const THEME = [
    69, 72, 76, 72, 74, 72, 69, 67,
    65, 69, 72, 69, 71, 69, 65, 64,
    67, 72, 76, 79, 76, 72, 69, 72,
    71, 69, 67, 64, 67, 69, 72, 69
];
for (let bar = 0; bar < BARS; bar++) {
    const t0 = bar * 4 * SPB;
    const ch = PROG[bar % PROG.length];
    const isBridge = bar >= 16 && bar < 24;         // C 段: 平行大调色彩(更亮)
    const isClimax = bar >= 28 && bar < 40;         // D 段: 高潮
    const isOutro = bar >= 40;
    /* 左手: 固定低音型(每小节 8 个八分音, 根-五-八-五 循环) */
    if (!isOutro) {
        const pattern = [0, 2, 1, 2, 0, 2, 1, 2];
        for (let i = 0; i < 8; i++) {
            const t = t0 + i * SPB / 2;
            const off = pattern[i];
            note(t, ch[0] - 12 + (off === 1 ? 7 : off === 2 ? 12 : 0), 0.13, 0.36, 0.55);
        }
        /* 低音长音铺底 */
        note(t0, ch[0] - 24, 0.19, 0.5, 0.4);
    }
    /* 右手旋律: B 段起 */
    if (bar >= 8 && !isOutro) {
        const idx = ((bar - 8) * 2) % THEME.length;
        const n1 = THEME[idx], n2 = THEME[(idx + 1) % THEME.length];
        const shift = isBridge ? 3 : 0;                     // 中段整体上移小三度(色彩变化)
        const oct = isClimax ? 12 : 0;
        note(t0 + SPB * 0.0, n1 + shift + oct, isClimax ? 0.20 : 0.165, 0.62, 0.8);
        note(t0 + SPB * 1.5, n2 + shift + oct, isClimax ? 0.17 : 0.14, 0.60, 0.8);
        note(t0 + SPB * 3.0, n1 + shift + oct + (bar % 2 ? 4 : 5), 0.12, 0.58, 0.7);
    }
    /* 中段与高潮加弦乐垫 */
    if (isBridge || isClimax) {
        for (const semi of ch) note(t0, semi - 12, isClimax ? 0.075 : 0.055, 0.5, 0.35);
        const len = Math.floor(4 * SPB * SR);
        for (let i = 0; i < len; i++) {
            const t = i / SR;
            let v = 0;
            for (const semi of ch) v += pad(t, hz(semi - 12), isClimax ? 0.052 : 0.038);
            mix(t0 + t, v * 0.5, v * 0.5);
        }
    }
    /* 高潮: 右手和弦加厚 */
    if (isClimax && bar % 2 === 0) {
        for (const semi of ch) note(t0 + SPB * 2, semi, 0.085, 0.5, 0.6);
    }
}
/* E 段: 单音消散(尾奏) */
{
    const t0 = 40 * 4 * SPB;
    const tail = [69, 72, 69, 64, 60, 57];
    tail.forEach((m, i) => note(t0 + i * SPB * 1.6, m, 0.13 - i * 0.012, 0.55 - i * 0.02, 0.5));
}
/* 软削波 + 归一 */
let peak = 0;
for (let i = 0; i < N; i++) { L[i] = Math.tanh(L[i] * 1.05); R[i] = Math.tanh(R[i] * 1.05); peak = Math.max(peak, Math.abs(L[i]), Math.abs(R[i])); }
const g = 0.9 / (peak || 1);
for (let i = 0; i < N; i++) { L[i] *= g; R[i] *= g; }

/* ---------- 编码 ---------- */
const musicDir = path.join(ROOT, 'src/main/resources/assets/ftboss/sounds/music');
fs.mkdirSync(musicDir, { recursive: true });
const ogg = encode({ channels: 2, sampleRate: SR, frames: N, data: [L, R] }, 0.15,
    { TITLE: 'Compost Winter Night', ARTIST: 'FTBoss' });
fs.writeFileSync(path.join(musicDir, 'compost.ogg'), ogg);
console.log(`BGM 已渲染: compost.ogg  ${(ogg.length / 1024).toFixed(0)} KB  ${(N / SR).toFixed(1)} 秒  (76 BPM, A 小调钢琴抒情)`);

/* ---------- 注册: sounds.json ---------- */
const sjPath = path.join(ROOT, 'src/main/resources/assets/ftboss/sounds.json');
const sj = JSON.parse(fs.readFileSync(sjPath, 'utf8'));
sj['boss_music_compost'] = {
    subtitle: 'ftboss.subtitle.boss_music_compost',
    sounds: [{ name: 'ftboss:music/compost', stream: true }]
};
fs.writeFileSync(sjPath, JSON.stringify(sj, null, 2));
console.log('sounds.json: 已加入 boss_music_compost');

/* ---------- 注册: FTSounds ---------- */
const fsPath = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss/sound/FTSounds.java');
let js = fs.readFileSync(fsPath, 'utf8');
if (js.includes('BOSS_MUSIC_COMPOST')) console.log('FTSounds: 已存在');
else {
    const lines = js.split('\n');
    const i = lines.findIndex(l => l.includes('BOSS_MUSIC_DEFECT = SOUNDS.register'));
    let j = i; while (j < lines.length && !lines[j].includes('));')) j++;
    lines.splice(j + 1, 0, '',
        '    /** 【炫彩答辩超人】专属BGM《堆肥·冬夜》: assets/ftboss/sounds/music/compost.ogg (2026-09-12, 钢琴抒情风) */',
        '    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST = SOUNDS.register("boss_music_compost",',
        '            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_compost")));');
    fs.writeFileSync(fsPath, lines.join('\n'));
    console.log('FTSounds: 已加入 BOSS_MUSIC_COMPOST');
}
/* ---------- 字幕 lang ---------- */
for (const f of ['zh_cn.json', 'en_us.json']) {
    const p = path.join(ROOT, 'src/main/resources/assets/ftboss/lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['ftboss.subtitle.boss_music_compost'] = f.startsWith('zh') ? '战斗BGM: 《堆肥·冬夜》' : 'BGM: Compost Winter Night';
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('lang: 中英字幕已加');
