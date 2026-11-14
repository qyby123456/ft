#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  shadow_sword_tex_fix.cjs —— 暗影剑贴图"清晰度"优化（只动明度，不动色相）
 *
 *  为什么做这个（2026-09-15，用户："太高分辨率反而游戏里不清晰"）：
 *    实测数据说明这**不是分辨率问题** ——
 *      · 剑长 1.5 格，视距 5 格时屏幕上约 232px，贴图 256 列，压缩比≈1.1（不浪费）
 *      · alpha 已经基本二值（40% 全透明 / 52% 全实心 / 中间 0%），mipmap 不会掺灰
 *      · **真正的问题：亮度 66% 的像素 < 40，35% 的像素亮度只有 8~20，亮部只有 0.7%**
 *        动态范围 55（<60 就是"整体糊成一团"）→ 在游戏里就是一团看不清的黑块
 *
 *  做法（保守，不动美术意图）：
 *    1) **保色相、重排明度**：按感知亮度算 gamma 曲线，把暗部拉开
 *    2) **暗部不吃纯黑**：保底提亮，避免在暗环境里彻底看不见
 *    3) **亮部增益**：原本就 >140 的像素更亮一档，让裂纹/符文跳出来
 *    4) alpha **一位不动**（形状/镂空/半透明完全保留）
 *
 *  用法：
 *    node tools/model3d/shadow_sword_tex_fix.cjs                    # 直接写入（自动备份）
 *    node tools/model3d/shadow_sword_tex_fix.cjs --dry              # 只看报告不写
 *    node tools/model3d/shadow_sword_tex_fix.cjs --gamma 0.78 --dark 30 --bright 1.25
 *    node tools/model3d/shadow_sword_tex_fix.cjs --restore          # 从备份还原
 *
 *  参数：--gamma（越小越亮，默认 0.72）
 *        --knee（暗部膝点，默认 45）  --dark（暗部保底亮度，默认 22）
 *        --bright（亮部增益，默认 1.22）
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const { decodePNG, encodePNG } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const TEXDIR = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'textures', 'item');

/** 一起处理的贴图（ready 是常态的提亮派生版，必须同一组曲线，否则切换时会跳变）。 */
const FILES = ['shadow_sword.png', 'shadow_sword_ready.png'];

const argv = (typeof process !== 'undefined' && process.argv ? process.argv.slice(2) : []);

/**
 * 布尔开关。
 *
 * <p>★ 这里连踩两个坑（都记下来，别再犯）：
 *   ① 取值型参数 `arg()` 会把紧跟在后面的 `--apply` 当成自己的值吃掉 →
 *      后面的 `includes('--apply')` 变 false → **写入静默失效**。
 *   ② Windows shell 传进来的参数可能**带不可见 BOM**（`\uFEFF--apply`），
 *      精确 `includes('--apply')` 同样匹配不到。
 * 现在：取值参数缺值直接报错；开关用**去 BOM + 正则**匹配；
 * 而且把"写入"设成默认动作，想只看报告用 `--dry` —— 默认动作是安全的那个，
 * 就不会再出现"以为写了其实没写"。
 */
function flag(name) {
    const re = new RegExp('^\\uFEFF?--' + name + '$');

    return argv.some(a => re.test(String(a).trim()));
}

function arg(name, fallback) {
    const i = argv.findIndex(a => String(a).replace(/^\uFEFF/, '').trim() === '--' + name);

    if (i < 0) return fallback;

    const raw = argv[i + 1];

    if (raw === undefined || String(raw).trim().startsWith('-')) {
        throw new Error(`--${name} 后面缺少数值（拿到的是 ${JSON.stringify(raw)}）`);
    }

    const v = Number(String(raw).replace(/^\uFEFF/, '').trim());

    if (!Number.isFinite(v)) {
        throw new Error(`--${name} 的值不是数字：${JSON.stringify(raw)}`);
    }

    return v;
}

/** 暗部 gamma：<1 = 整体提亮。 */
const GAMMA = arg('gamma', 0.72);
/** 亮度低于膝点的像素抬到至少 DARK_FLOOR，避免死黑。 */
const DARK_KNEE = arg('knee', 45);
const DARK_FLOOR = arg('dark', 22);
/** 亮部增益。 */
const BRIGHT_GAIN = arg('bright', 1.22);

const lum = (r, g, b) => 0.2126 * r + 0.7152 * g + 0.0722 * b;

/** 保色相地按比例缩放 RGB，让明度落到目标值。 */
function setLum(r, g, b, target) {
    const L = lum(r, g, b);

    if (L < 0.5) {
        // 原本就是纯黑：没色相可保，给一点冷紫的底，避免纯黑死块
        return [target * 0.82, target * 0.78, target * 1.0];
    }

    const k = target / L;

    return [r * k, g * k, b * k];
}

function transformPixel(r, g, b) {
    const L = lum(r, g, b);

    /* 1) gamma 提亮暗部：L' = 255 * (L/255)^GAMMA   （GAMMA<1 → 变亮） */
    let out = 255 * Math.pow(Math.max(0, L) / 255, GAMMA);

    /* 2) 暗部保底 */
    if (L < DARK_KNEE) {
        out = Math.max(out, DARK_FLOOR);
    }

    /* 3) 亮部增益（只对本来就是亮部的像素，避免把暗部一起冲白） */
    if (L > 140) {
        out = Math.min(255, out * BRIGHT_GAIN);
    }

    return setLum(r, g, b, out);
}

function process(file) {
    const src = path.join(TEXDIR, file);
    const t = decodePNG(src);
    const out = new Uint8Array(t.w * t.h * 4);

    let changed = 0;
    let before = 0;
    let after = 0;
    let solid = 0;

    for (let i = 0; i < t.w * t.h; i++) {
        const o = i * 4;
        const a = t.data[o + 3];

        out[o + 3] = a;                                    // alpha 一位不动

        if (a < 128) {                                     // 透明像素：颜色原样保留
            out[o] = t.data[o];
            out[o + 1] = t.data[o + 1];
            out[o + 2] = t.data[o + 2];
            continue;
        }

        const [nr, ng, nb] = transformPixel(t.data[o], t.data[o + 1], t.data[o + 2]);

        out[o] = Math.max(0, Math.min(255, Math.round(nr)));
        out[o + 1] = Math.max(0, Math.min(255, Math.round(ng)));
        out[o + 2] = Math.max(0, Math.min(255, Math.round(nb)));

        before += lum(t.data[o], t.data[o + 1], t.data[o + 2]);
        after += lum(out[o], out[o + 1], out[o + 2]);
        solid++;

        if (out[o] !== t.data[o] || out[o + 1] !== t.data[o + 1] || out[o + 2] !== t.data[o + 2]) {
            changed++;
        }
    }

    return {
        file, t, out, changed, solid,
        avgBefore: solid ? before / solid : 0,
        avgAfter: solid ? after / solid : 0
    };
}

function report(r) {
    console.log(`  ${r.file}`);
    console.log(`    实心像素 ${r.solid}，改动 ${r.changed} (${(r.changed / r.solid * 100).toFixed(1)}%)`);
    console.log(`    平均亮度 ${r.avgBefore.toFixed(1)} → ${r.avgAfter.toFixed(1)}`);
}

function main() {
    console.log('=== 暗影剑贴图明度优化 ===');
    console.log(`  参数: gamma=${GAMMA} 暗部保底=${DARK_FLOOR}(膝点${DARK_KNEE}) 亮部增益=${BRIGHT_GAIN}`);
    console.log('');

    if (flag('restore')) {
        let n = 0;

        for (const f of FILES) {
            const bak = path.join(TEXDIR, f + '.lumfix.bak');

            if (fs.existsSync(bak)) {
                fs.copyFileSync(bak, path.join(TEXDIR, f));
                console.log(`  已还原 ${f}`);
                n++;
            }
        }

        console.log(n ? `\n还原 ${n} 个文件。` : '\n没有找到备份。');
        return 0;
    }

    const results = FILES.map(process);
    results.forEach(report);

    if (flag('dry')) {
        console.log('');
        console.log('（--dry 预览，未写文件。去掉 --dry 直接写入）');
        console.log('调参：--gamma 0.72（越小越亮） --dark 22（暗部保底） --bright 1.22');
        return 0;
    }

    console.log('');

    for (const r of results) {
        const dst = path.join(TEXDIR, r.file);
        const bak = dst + '.lumfix.bak';

        if (!fs.existsSync(bak)) {
            fs.copyFileSync(dst, bak);
            console.log(`  备份 → ${path.basename(bak)}`);
        }

        fs.writeFileSync(dst, encodePNG(r.t.w, r.t.h, r.out));
        console.log(`  已写入 ${r.file}（alpha 未动）`);
    }

    console.log('');
    console.log('进游戏按 F3+T 重载资源即可看到效果（不用重新构建）。');
    console.log('不满意：node tools/model3d/shadow_sword_tex_fix.cjs --restore');

    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
