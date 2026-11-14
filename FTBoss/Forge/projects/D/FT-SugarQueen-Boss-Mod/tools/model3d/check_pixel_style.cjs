#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  check_pixel_style.cjs —— 像素风格一致性检查（纹理像素密度 / 细碎程度）
 *
 *  为什么有它（2026-09-16）：
 *    "贴图太大导致游戏里看不清"这类判断里，**PNG 尺寸本身不是关键**，
 *    关键是**同一个面分配了多少纹理像素** —— 同样长 16 模型单位的刃：
 *        16 px 长 → 1 px/单位（接近原版尺度）
 *        32 px 长 → 2 px/单位（适度精细）
 *        64 px 长 → 4 px/单位（容易细碎）
 *       128 px 长 → 8 px/单位（与原版环境尺度差异明显）
 *    所以 256×256 图集不一定有问题：分配合理就能保持粗像素风格。
 *
 *  项目标准（用户 2026-09-16 定）：
 *    · 主体目标密度 **1~2 px/模型单位**
 *    · 刃口/魔眼允许局部精细，超过 **3 px/单位** 列为复查项
 *    · 同一面横纵密度相差 > **1.5 倍** → 提示纹理拉伸
 *    · 单纹素小于一个屏幕像素 → 提示容易闪烁或消失（需 --screen-ppu）
 *  只做提示，**不自动缩图、不改 UV**。
 *
 *  与 python 版的差异：
 *    · 工程是 Node + 自写 PNG 编解码、零外部依赖，所以这版用 Node 实现
 *      （原 python 版需要 pillow；而且它第 88 行 `for in range(3)` 是语法错误，
 *       这版把该处修成正常的邻域比较）
 *    · 面积统计改用真实 cube 尺寸，不再乘 |uv_size|（避免负 uv_size 把面积算成负）
 *
 *  用法：
 *    node tools/model3d/check_pixel_style.cjs <geo> <texture> [选项]
 *      --target 2           目标上限 px/模型单位
 *      --screen-ppu 0.4     可选：当前画面每模型单位占多少屏幕像素
 *      --out art/xxx.json   报告输出路径
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const { decodePNG } = require('./png.cjs');

const argv = process.argv.slice(2);
const positional = argv.filter(a => !a.startsWith('--'));

function opt(name, fallback) {
    const i = argv.findIndex(a => a === '--' + name);
    if (i < 0) return fallback;

    const raw = argv[i + 1];

    if (raw === undefined || raw.startsWith('--')) {
        throw new Error(`--${name} 后面缺少数值`);
    }

    const v = Number(raw);

    if (!Number.isFinite(v)) {
        throw new Error(`--${name} 的值不是数字：${raw}`);
    }

    return v;
}

function str(name, fallback) {
    const i = argv.findIndex(a => a === '--' + name);

    return i >= 0 && argv[i + 1] ? argv[i + 1] : fallback;
}

const ROOT = path.resolve(__dirname, '..', '..');

const GEO = positional[0]
    ? path.resolve(positional[0])
    : path.join(ROOT, 'src/main/resources/assets/ftboss/geo/shadow_sword.geo.json');
const TEX = positional[1]
    ? path.resolve(positional[1])
    : path.join(ROOT, 'src/main/resources/assets/ftboss/textures/item/shadow_sword.png');

const TARGET = opt('target', 2.0);
const SCREEN_PPU = argv.includes('--screen-ppu') ? opt('screen-ppu', 0) : null;
const OUT = path.resolve(str('out', path.join(ROOT, 'art', 'pixel_style_report.json')));

if (!(TARGET > 0)) throw new Error('--target 必须是正的有限数值');
if (SCREEN_PPU !== null && !(SCREEN_PPU > 0)) throw new Error('--screen-ppu 必须是正的有限数值');

/* ============================================================
 * 纹理细碎度：量化色数 + 相邻强色差占比
 * ==========================================================*/

function textureDetail(img, rect) {
    const [x0, y0, x1, y1] = rect;
    const colors = new Set();
    let hard = 0;
    let comparisons = 0;

    const at = (x, y) => {
        const o = (y * img.w + x) * 4;
        return [img.data[o], img.data[o + 1], img.data[o + 2], img.data[o + 3]];
    };

    for (let y = y0; y < y1; y++) {
        for (let x = x0; x < x1; x++) {
            const c = at(x, y);
            if (c[3] < 128) continue;

            // 粗略量化色彩（每通道 //16），减少轻微色差干扰
            colors.add(((c[0] >> 4) << 8) | ((c[1] >> 4) << 4) | (c[2] >> 4));

            for (const [nx, ny] of [[x + 1, y], [x, y + 1]]) {
                if (nx >= x1 || ny >= y1) continue;

                const other = at(nx, ny);
                if (other[3] < 128) continue;

                comparisons++;

                // ★ 原 python 版这里写成 `max(abs(color[i] - other[i]) for in range(3))`，
                //   `for in` 缺变量名 → SyntaxError。这里修成正常的取三通道最大差。
                const d = Math.max(
                    Math.abs(c[0] - other[0]),
                    Math.abs(c[1] - other[1]),
                    Math.abs(c[2] - other[2])
                );

                if (d >= 48) hard++;
            }
        }
    }

    return {
        quantized_colors: colors.size,
        strong_neighbor_change_ratio: comparisons ? Number((hard / comparisons).toFixed(4)) : 0
    };
}

/* ============================================================
 * 主流程
 * ==========================================================*/

function main() {
    const doc = JSON.parse(fs.readFileSync(GEO, 'utf8'));
    const geometries = doc['minecraft:geometry'] || [];

    if (geometries.length === 0) throw new Error('未找到 minecraft:geometry');

    const img = decodePNG(TEX);
    const faces = [];
    const skipped = [];

    geometries.forEach((geometry, gi) => {
        const desc = geometry.description || {};
        const declaredW = desc.texture_width;
        const declaredH = desc.texture_height;

        if (!(declaredW > 0) || !(declaredH > 0)) {
            throw new Error('模型声明的贴图尺寸必须大于零');
        }

        // PNG 比声明尺寸大时，按比例换算实际纹素
        const sx = img.w / declaredW;
        const sy = img.h / declaredH;

        for (const bone of geometry.bones || []) {
            (bone.cubes || []).forEach((cube, ci) => {
                const label = `${gi}/${bone.name}/cube[${ci}]`;
                const uv = cube.uv;

                if (!uv || typeof uv !== 'object') {
                    skipped.push({ part: label, reason: 'Box UV 未展开，未参与判断' });
                    return;
                }

                const [ax, ay, az] = cube.size.map(Math.abs);

                const dims = {
                    north: [ax, ay],
                    south: [ax, ay],
                    east: [az, ay],
                    west: [az, ay],
                    up: [ax, az],
                    down: [ax, az]
                };

                for (const [side, face] of Object.entries(uv)) {
                    if (!dims[side]) continue;

                    const [width, height] = dims[side];
                    if (!(width > 0) || !(height > 0)) continue;

                    if (!face || !face.uv || !face.uv_size) {
                        skipped.push({ part: `${label}/${side}`, reason: '缺少显式 uv 或 uv_size' });
                        continue;
                    }

                    const [u, v] = face.uv;
                    const [uw, vh] = face.uv_size;

                    // 支持负 uv_size 的镜像面
                    const xs = [u * sx, (u + uw) * sx].sort((a, b) => a - b);
                    const ys = [v * sy, (v + vh) * sy].sort((a, b) => a - b);
                    const [left, right] = xs;
                    const [top, bottom] = ys;

                    if (left < 0 || top < 0 || right > img.w || bottom > img.h
                        || right <= left || bottom <= top) {
                        skipped.push({ part: `${label}/${side}`, reason: 'UV 越界或零面积' });
                        continue;
                    }

                    const du = (right - left) / width;
                    const dv = (bottom - top) / height;
                    const density = Math.sqrt(du * dv);
                    const stretch = Math.max(du, dv) / Math.min(du, dv);
                    const flags = [];

                    if (Math.max(du, dv) > TARGET * 1.5) {
                        flags.push('纹素密度偏高，复查细裂纹与高光');
                    }

                    if (stretch > 1.5) {
                        flags.push('横纵像素比例不一致（纹理被拉伸）');
                    }

                    // UV 边界必须落在实际纹素上
                    for (const p of [left, top, right, bottom]) {
                        if (Math.abs(p - Math.round(p)) > 1e-6) {
                            flags.push('UV 边界未对齐实际纹素');
                            break;
                        }
                    }

                    const rect = [
                        Math.floor(left), Math.floor(top),
                        Math.ceil(right), Math.ceil(bottom)
                    ];

                    const detail = textureDetail(img, rect);

                    if (detail.strong_neighbor_change_ratio > 0.30 && Math.max(du, dv) > TARGET) {
                        flags.push('高密度且相邻色差频繁，可能显得碎杂');
                    }

                    let screenTexels = null;

                    if (SCREEN_PPU !== null) {
                        // 细方向上 1 个纹素占多少屏幕像素
                        screenTexels = SCREEN_PPU / Math.max(du, dv);

                        if (screenTexels < 1) {
                            flags.push('细方向单纹素不足一个屏幕像素（易闪烁/消失）');
                        }
                    }

                    faces.push({
                        part: `${label}/${side}`,
                        model_area: Number((width * height).toFixed(3)),
                        density_u: Number(du.toFixed(3)),
                        density_v: Number(dv.toFixed(3)),
                        density_geometric_mean: Number(density.toFixed(3)),
                        stretch_ratio: Number(stretch.toFixed(3)),
                        screen_pixels_per_texel_min: screenTexels === null ? null : Number(screenTexels.toFixed(3)),
                        ...detail,
                        flags
                    });
                }
            });
        }
    });

    const totalArea = faces.reduce((s, f) => s + f.model_area, 0);
    const highArea = faces
        .filter(f => Math.max(f.density_u, f.density_v) > TARGET * 1.5)
        .reduce((s, f) => s + f.model_area, 0);

    const flagged = faces.filter(f => f.flags.length > 0);

    /* ---------- 按骨骼分类统计 ----------
       为什么必须分类：整模的"高密度面积占比"会被**光效定位小块**带偏 ——
       blade_glow / eye_glow / crystal_glow 那种 size=0.05 的种子块，
       随便给个 24x24 的 UV 就是 480 px/单位，把 p90 直接拉到荒谬值。
       它们还按工程约定是要隐藏的（HIDDEN_ROOTS），根本不该进主体统计。 */
    const byBone = new Map();

    for (const f of faces) {
        const bone = f.part.split('/')[1];
        const entry = byBone.get(bone) || { bone, faces: 0, area: 0, maxDensity: 0, sumDensity: 0 };
        entry.faces++;
        entry.area += f.model_area;
        const d = Math.max(f.density_u, f.density_v);
        entry.maxDensity = Math.max(entry.maxDensity, d);
        entry.sumDensity += d;
        byBone.set(bone, entry);
    }

    const boneStats = [...byBone.values()]
        .map(e => ({
            bone: e.bone,
            faces: e.faces,
            model_area: Number(e.area.toFixed(2)),
            mean_density: Number((e.sumDensity / e.faces).toFixed(2)),
            max_density: Number(e.maxDensity.toFixed(2)),
            over_target: e.sumDensity / e.faces > TARGET
        }))
        .sort((a, b) => b.mean_density - a.mean_density);

    /* 主体 = 排除"光效种子块"（平均密度离谱且面积极小的那些） */
    const glowish = boneStats.filter(b => b.mean_density > TARGET * 5 && b.model_area < 1.0);
    const glowNames = new Set(glowish.map(b => b.bone));

    const bodyFaces = faces.filter(f => !glowNames.has(f.part.split('/')[1]));
    const bodyArea = bodyFaces.reduce((s, f) => s + f.model_area, 0);
    const bodyHigh = bodyFaces
        .filter(f => Math.max(f.density_u, f.density_v) > TARGET * 1.5)
        .reduce((s, f) => s + f.model_area, 0);
    const bodyDensities = bodyFaces
        .map(f => Math.max(f.density_u, f.density_v))
        .sort((a, b) => a - b);
    const bodyMedian = bodyDensities.length
        ? bodyDensities[Math.floor(bodyDensities.length / 2)]
        : 0;

    const report = {
        geo: path.relative(ROOT, GEO),
        texture: path.relative(ROOT, TEX),
        texture_size: [img.w, img.h],
        target_density: TARGET,
        screen_ppu: SCREEN_PPU,
        checked_faces: faces.length,
        flagged_faces: flagged.length,
        high_density_area_ratio: totalArea ? Number((highArea / totalArea).toFixed(4)) : 0,
        density_percentiles: percentiles(faces),
        body_excluding_glow: {
            excluded_bones: [...glowNames],
            faces: bodyFaces.length,
            median_density: Number(bodyMedian.toFixed(2)),
            high_density_area_ratio: bodyArea ? Number((bodyHigh / bodyArea).toFixed(4)) : 0,
            verdict: bodyMedian > TARGET * 1.5
                ? `主体中位密度 ${bodyMedian.toFixed(2)} px/单位，超过目标 ${TARGET} 的 1.5 倍 → 纹理明显偏精细`
                : `主体中位密度 ${bodyMedian.toFixed(2)} px/单位，在目标范围内`
        },
        by_bone: boneStats,
        status: flagged.length ? '需要复查' : '未发现规则警告',
        limitations: [
            '使用静态 cube 尺寸，不计骨骼动画、inflate 和游戏显示缩放',
            '面积包含遮挡面和内部面，不代表实际屏幕可见面积',
            '纹素密度不等于有效细节密度，最近邻放大的贴图也会被提示',
            '相邻色差只用于定位纹理，不是像素美术质量评分',
            'Box UV 或不完整 UV 会跳过；跳过项不代表通过',
            '未验证游戏纹理过滤、Mipmaps 和运动中的闪烁',
            '整模汇总值会被极小的光效种子块带偏，判断请看 body_excluding_glow 与 by_bone'
        ],
        skipped,
        faces: faces.sort((a, b) =>
            Math.max(b.density_u, b.density_v) - Math.max(a.density_u, a.density_v))
    };

    fs.mkdirSync(path.dirname(OUT), { recursive: true });
    fs.writeFileSync(OUT, JSON.stringify(report, null, 2), 'utf8');

    /* ---------- 控制台摘要 ---------- */
    console.log('=== 像素风格一致性检查 ===');
    console.log(`  geo     : ${report.geo}`);
    console.log(`  贴图    : ${report.texture}  (${img.w}x${img.h})`);
    console.log(`  目标密度: ${TARGET} px/模型单位（>${(TARGET * 1.5).toFixed(1)} 列复查）`);
    console.log('');
    console.log(`  已检查面: ${faces.length}   跳过: ${skipped.length}`);
    console.log(`  整模密度分位: ` +
        `p50 ${report.density_percentiles.p50}  p75 ${report.density_percentiles.p75}  ` +
        `p90 ${report.density_percentiles.p90}  max ${report.density_percentiles.max}`);
    console.log('');
    console.log('  ★ 去掉光效种子块后的主体（判断看这一段）：');
    console.log(`      排除骨骼: ${report.body_excluding_glow.excluded_bones.join(', ') || '（无）'}`);
    console.log(`      面数 ${report.body_excluding_glow.faces}   ` +
        `中位密度 ${report.body_excluding_glow.median_density} px/单位   ` +
        `高密度面积占比 ${(report.body_excluding_glow.high_density_area_ratio * 100).toFixed(1)}%`);
    console.log(`      ${report.body_excluding_glow.verdict}`);
    console.log('');
    console.log('  按骨骼（平均密度降序，前 10）:');

    for (const b of report.by_bone.slice(0, 10)) {
        console.log(`      ${b.bone.padEnd(16)} 面 ${String(b.faces).padStart(4)}  ` +
            `面积 ${String(b.model_area).padStart(8)}  ` +
            `均 ${String(b.mean_density).padStart(7)}  最大 ${String(b.max_density).padStart(7)}` +
            (b.over_target ? '  ⚠ 超目标' : ''));
    }

    console.log('');
    console.log(`  有提示的面: ${flagged.length}`);
    console.log(`  判断: ${report.status}`);
    console.log(`  报告: ${path.relative(ROOT, OUT)}`);
    console.log('');
    console.log('  只做提示，不自动缩图、不改 UV。');

    return flagged.length ? 1 : 0;
}

function percentiles(faces) {
    const d = faces.map(f => Math.max(f.density_u, f.density_v)).sort((a, b) => a - b);

    if (d.length === 0) return { p50: 0, p75: 0, p90: 0, max: 0 };

    const at = p => Number(d[Math.min(d.length - 1, Math.floor(d.length * p))].toFixed(3));

    return { p50: at(0.5), p75: at(0.75), p90: at(0.9), max: Number(d[d.length - 1].toFixed(3)) };
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
