#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  shadow_sword_de_densify.cjs —— 把最密的那个件"降有效密度"（不动 UV、不动图集）
 *
 *  为什么这么做（2026-09-16）：
 *    check_pixel_style.cjs 实测暗影剑 **主体中位密度 12.54 px/模型单位**，
 *    而项目标准是 **1~2**（>3 才列复查）。罪魁是几个小件被给了过多纹理像素：
 *      eye_mount 70.47（占 58 单位面积，最大）、crystal_link 66、pupil 37、crystal 21.7
 *    用户给的修法**不是缩图**，而是"降低剑身大面的有效细节密度：黑铁保留成块明暗、
 *    裂纹减少分叉、刃口用连续亮边、魔眼保留少量高光"。
 *
 *  做法：**把颜色量化到更少的台阶**（成块明暗），并压掉孤立的高频噪点。
 *    · 不碰几何、不碰 UV、不碰图集排布 → 零风险
 *    · alpha 一位不动 → 形状完全不变
 *    · 只对"高密度骨骼对应的 UV 区域"做量化，低密度区域保持原样
 *
 *  用法：
 *    node tools/model3d/shadow_sword_de_densify.cjs --levels 6 --dry   # 只看报告
 *    node tools/model3d/shadow_sword_de_densify.cjs --levels 6         # 写入
 *    node tools/model3d/shadow_sword_de_densify.cjs --restore          # 还原
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const { decodePNG, encodePNG } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const FILES = ['shadow_sword.png', 'shadow_sword_ready.png'];

const argv = (typeof process !== 'undefined' && process.argv ? process.argv.slice(2) : []);

function flag(name) {
    return argv.some(a => new RegExp('^\\uFEFF?--' + name + '$').test(String(a).trim()));
}

function arg(name, fallback) {
    const i = argv.findIndex(a => String(a).replace(/^\uFEFF/, '').trim() === '--' + name);
    if (i < 0) return fallback;

    const raw = argv[i + 1];
    if (raw === undefined || String(raw).trim().startsWith('-')) {
        throw new Error(`--${name} 后面缺少数值`);
    }

    const v = Number(String(raw).replace(/^\uFEFF/, '').trim());
    if (!Number.isFinite(v)) throw new Error(`--${name} 不是数字：${raw}`);

    return v;
}

/** 量化台阶数：每通道切成几段。越小越"成块"。 */
const LEVELS = Math.max(2, Math.round(arg('levels', 6)));
/** 超过这个密度的骨骼才处理（px/模型单位）。 */
const DENSITY_GATE = arg('gate', 20);
/** 是否压掉孤立噪点。 */
const DESPECKLE = !flag('no-despeckle');

/* ---------- 1) 找出高密度骨骼占用的 UV 区域 ---------- */

function highDensityRects() {
    const doc = JSON.parse(fs.readFileSync(GEO, 'utf8'));
    const g = doc['minecraft:geometry'][0];
    const tw = g.description.texture_width;
    const th = g.description.texture_height;

    const rects = [];

    for (const bone of g.bones) {
        for (const c of (bone.cubes || [])) {
            const [ax, ay, az] = c.size.map(Math.abs);
            const dims = {
                north: [ax, ay], south: [ax, ay],
                east: [az, ay], west: [az, ay],
                up: [ax, az], down: [ax, az]
            };

            for (const [side, f] of Object.entries(c.uv || {})) {
                if (!dims[side] || !f.uv || !f.uv_size) continue;

                const [w, h] = dims[side];
                if (!(w > 0) || !(h > 0)) continue;

                const du = Math.abs(f.uv_size[0]) / w;
                const dv = Math.abs(f.uv_size[1]) / h;

                if (Math.max(du, dv) < DENSITY_GATE) continue;

                const [u, v] = f.uv;
                const [uw, vh] = f.uv_size;
                const xs = [u, u + uw].sort((a, b) => a - b);
                const ys = [v, v + vh].sort((a, b) => a - b);

                rects.push({
                    bone: bone.name,
                    x0: Math.max(0, Math.floor(xs[0])),
                    y0: Math.max(0, Math.floor(ys[0])),
                    x1: Math.min(tw, Math.ceil(xs[1])),
                    y1: Math.min(th, Math.ceil(ys[1])),
                    density: Number(Math.max(du, dv).toFixed(1))
                });
            }
        }
    }

    return rects;
}

/* ---------- 2) 处理 ---------- */

const q = (v, levels) => {
    const step = 255 / (levels - 1);
    return Math.round(Math.round(v / step) * step);
};

/** 孤立噪点：与周围 4 邻域都差很多的像素，向邻域中位数靠拢。 */
function despeckle(img, inMask) {
    const src = Uint8Array.from(img.data);
    let n = 0;

    for (let y = 1; y < img.h - 1; y++) {
        for (let x = 1; x < img.w - 1; x++) {
            const i = y * img.w + x;
            if (!inMask[i]) continue;
            if (src[i * 4 + 3] < 128) continue;

            const nb = [[1, 0], [-1, 0], [0, 1], [0, -1]]
                .map(([dx, dy]) => (y + dy) * img.w + (x + dx))
                .filter(j => src[j * 4 + 3] >= 128);

            if (nb.length < 3) continue;

            for (let ch = 0; ch < 3; ch++) {
                const mine = src[i * 4 + ch];
                const vals = nb.map(j => src[j * 4 + ch]).sort((a, b) => a - b);
                const med = vals[Math.floor(vals.length / 2)];

                if (Math.abs(mine - med) > 40) {          // 明显离群
                    img.data[i * 4 + ch] = med;
                    n++;
                }
            }
        }
    }

    return n;
}

function process(file, rects) {
    const src = path.join(AS, 'textures', 'item', file);
    const img = decodePNG(src);
    const before = Uint8Array.from(img.data);

    // 掩膜：只有落在高密度 UV 区域里的像素才处理
    const mask = new Uint8Array(img.w * img.h);

    for (const r of rects) {
        for (let y = r.y0; y < r.y1; y++) {
            for (let x = r.x0; x < r.x1; x++) {
                mask[y * img.w + x] = 1;
            }
        }
    }

    let touched = 0;

    for (let i = 0; i < img.w * img.h; i++) {
        if (!mask[i]) continue;
        if (img.data[i * 4 + 3] < 128) continue;           // 透明不动

        for (let ch = 0; ch < 3; ch++) {
            const v = img.data[i * 4 + ch];
            const nv = q(v, LEVELS);
            if (nv !== v) touched++;
            img.data[i * 4 + ch] = nv;
        }
    }

    const speckled = DESPECKLE ? despeckle(img, mask) : 0;

    // 统计唯一色下降
    const countColors = d => {
        const s = new Set();
        for (let i = 0; i < img.w * img.h; i++) {
            if (d[i * 4 + 3] < 128) continue;
            s.add((d[i * 4] << 16) | (d[i * 4 + 1] << 8) | d[i * 4 + 2]);
        }
        return s.size;
    };

    return {
        file, src, img,
        maskPixels: mask.reduce((s, v) => s + v, 0),
        touched, speckled,
        colorsBefore: countColors(before),
        colorsAfter: countColors(img.data)
    };
}

function main() {
    const rects = highDensityRects();

    const bones = new Map();
    for (const r of rects) {
        const e = bones.get(r.bone) || { n: 0, max: 0 };
        e.n++;
        e.max = Math.max(e.max, r.density);
        bones.set(r.bone, e);
    }

    console.log('=== 暗影剑「降有效密度」：颜色台阶量化 ===');
    console.log(`  量化台阶 ${LEVELS} 级/通道   密度阈值 ${DENSITY_GATE} px/单位   去噪点 ${DESPECKLE ? '开' : '关'}`);
    console.log('');
    console.log(`  命中高密度 UV 区域 ${rects.length} 块，涉及骨骼：`);
    for (const [b, e] of [...bones.entries()].sort((a, b) => b[1].max - a[1].max)) {
        console.log(`    ${b.padEnd(16)} ${String(e.n).padStart(3)} 块  最大密度 ${e.max}`);
    }
    console.log('');

    if (flag('restore')) {
        let n = 0;
        for (const f of FILES) {
            const bak = path.join(AS, 'textures', 'item', f + '.dedens.bak');
            if (fs.existsSync(bak)) {
                fs.copyFileSync(bak, path.join(AS, 'textures', 'item', f));
                console.log(`  已还原 ${f}`);
                n++;
            }
        }
        console.log(n ? `\n还原 ${n} 个文件。` : '\n没有备份。');
        return 0;
    }

    const results = FILES.map(f => process(f, rects));

    for (const r of results) {
        console.log(`  ${r.file}`);
        console.log(`    命中区域像素 ${r.maskPixels}，量化改动 ${r.touched}，去噪点 ${r.speckled}`);
        console.log(`    实心唯一色 ${r.colorsBefore} → ${r.colorsAfter}`);
    }

    if (flag('dry')) {
        console.log('');
        console.log('（--dry 预览，未写文件）');
        return 0;
    }

    console.log('');

    for (const r of results) {
        const bak = r.src + '.dedens.bak';
        if (!fs.existsSync(bak)) {
            fs.copyFileSync(r.src, bak);
            console.log(`  备份 → ${path.basename(bak)}`);
        }
        fs.writeFileSync(r.src, encodePNG(r.img.w, r.img.h, r.img.data));
        console.log(`  已写入 ${r.file}`);
    }

    console.log('');
    console.log('复查：node tools/model3d/check_pixel_style.cjs');
    console.log('还原：node tools/model3d/shadow_sword_de_densify.cjs --restore');

    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
