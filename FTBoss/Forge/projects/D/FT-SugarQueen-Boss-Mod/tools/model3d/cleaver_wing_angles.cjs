#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  cleaver_wing_angles.cjs —— 黑切刃翼"张开多少才看得出来"的离线评估
 *
 *  背景（2026-09-16，用户："会发光了，但是没展开"）：
 *    刃翼拆分、blood_pose、发光遮罩、CleaverBloodVisualState 全部到位，
 *    发光层与展开读的是**同一个 progress** —— 所以"亮 = 标志位和进度都通了"。
 *    真正的问题是 **±8° 太小**：
 *      blade 主体高 5.9 单位、翼各 1.8 单位，8° 只把翼中心抬高 0.36 单位
 *      （= 0.022 格，视距 5 格时约 15 像素）→ 看着就是"没动"。
 *
 *  本脚本回答两个问题：
 *    ① 每个候选角度下，翼尖/翼中心实际移动多少？（"看不看得出来"）
 *    ② 翼与同侧主刃的重叠体积怎么变？
 *       ⚠ 注意：翼在设计上**本来就埋进主刃**（8° 时重叠 13 单位，是有意避免共面闪烁）；
 *         张开会让重叠**变小**（往外拔），所以"重叠下降"是正常的、不是穿模变严重。
 *         真正要防的是"重叠降到 0"（= 翼根脱离主体，会出现裂缝）。
 *
 *  用法：
 *    node tools/model3d/cleaver_wing_angles.cjs                    # 表格
 *    node tools/model3d/cleaver_wing_angles.cjs --sweep 8,12,16,20,24,30
 *    node tools/model3d/cleaver_wing_angles.cjs --bake 20 --out art/cleaver_wing20.geo.json
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const cube = require('./cube_space.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'black_cleaver.geo.json');

const argv = process.argv.slice(2);

function opt(name, fallback) {
    const i = argv.findIndex(a => a === '--' + name);
    if (i < 0) return fallback;

    const raw = argv[i + 1];
    if (raw === undefined) throw new Error(`--${name} 后面缺少数值`);

    return raw;
}

/** 四片翼：骨骼名 → 符号（上翼为 −，下翼为 +；左右反号）。 */
const WINGS = [
    { bone: 'blade_upper_wing', parent: 'blade', sign: -1, side: '左' },
    { bone: 'blade_lower_wing', parent: 'blade', sign: 1, side: '左' },
    { bone: 'blade_right_upper_wing', parent: 'blade_right', sign: 1, side: '右' },
    { bone: 'blade_right_lower_wing', parent: 'blade_right', sign: -1, side: '右' }
];

const model = cube.loadGeo(GEO);
const cubes = cube.listCubes(model);

function boundsOf(bone, pose) {
    const pts = [];

    for (const info of cubes) {
        if (info.bone !== bone) continue;
        pts.push(...cube.cubeCorners(model, info, pose));
    }

    if (pts.length === 0) return null;

    return [0, 1, 2].map(a => {
        const v = pts.map(p => p[a]);
        return [Math.min(...v), Math.max(...v)];
    });
}

function overlapVolume(A, B) {
    const per = [0, 1, 2].map(a =>
        Math.min(A[a][1], B[a][1]) - Math.max(A[a][0], B[a][0]));

    return per.every(v => v > 0.01) ? per.reduce((x, y) => x * y, 1) : 0;
}

const rest = {};
for (const w of WINGS) rest[w.bone] = boundsOf(w.bone, {});

function main() {
    const sweep = opt('sweep', '8,12,16,20,24,30')
        .split(',')
        .map(s => Number(s.trim()))
        .filter(Number.isFinite);

    if (sweep.length === 0) throw new Error('--sweep 至少要有一个角度');

    console.log('=== 黑切刃翼：张开角评估 ===');
    console.log(`  geo: ${path.relative(ROOT, GEO)}`);
    console.log('');
    console.log('  角度   上翼中心y位移   下翼中心y位移   翼尖y位移   与主刃重叠(上/下)   判定');
    console.log('  ' + '-'.repeat(92));

    for (const deg of sweep) {
        const rows = [];

        for (const w of WINGS) {
            const pose = {};
            pose[w.bone] = { rotation: [0, 0, w.sign * deg] };

            const b = boundsOf(w.bone, pose);
            const parentB = boundsOf(w.parent, {});
            const ov = b && parentB ? overlapVolume(b, parentB) : 0;

            const cyRest = (rest[w.bone][1][0] + rest[w.bone][1][1]) / 2;
            const cyNow = (b[1][0] + b[1][1]) / 2;

            rows.push({
                bone: w.bone,
                dyCenter: cyNow - cyRest,
                tipY: b[1][1],
                overlap: ov
            });
        }

        const up = rows[0];
        const lo = rows[1];
        const upR = rows[2];
        const loR = rows[3];

        // 判定：翼中心竖向位移在屏幕上够不够看
        // 以"视距 5 格、窗口 1080、FOV 70"折算：1 模型单位 ≈ 14.5 屏幕像素
        const pxPerUnit = 1080 / (2 * 5 * Math.tan(35 * Math.PI / 180)) / 16;
        const px = Math.abs(up.dyCenter) * pxPerUnit;
        const verdict = px < 6
            ? '几乎看不出'
            : px < 12 ? '勉强能看出' : '明显';

        console.log(
            `  ${String(deg).padStart(3)}°   ` +
            `${up.dyCenter.toFixed(3).padStart(9)}   ` +
            `${lo.dyCenter.toFixed(3).padStart(9)}   ` +
            `${(up.tipY - rest[up.bone][1][1]).toFixed(3).padStart(8)}   ` +
            `${(up.overlap.toFixed(1) + ' / ' + lo.overlap.toFixed(1)).padStart(14)}   ` +
            `${verdict}（约 ${px.toFixed(0)}px）`
        );
    }

    console.log('');
    console.log(`  换算基准：视距 5 格 / 窗口 1080 / FOV 70 → 1 模型单位 ≈ ${(1080 / (2 * 5 * Math.tan(35 * Math.PI / 180)) / 16).toFixed(1)} 屏幕像素`);
    console.log('');
    console.log('  ⚠ 翼在设计上**本来就埋进主刃**（8° 时重叠约 13 单位，避免共面闪烁）；');
    console.log('     张开会让重叠变小（往外拔），所以重叠下降是正常的。');
    console.log('     要防的是重叠降到 ~0 —— 那说明翼根脱离主体，会出现裂缝。');
    console.log('');
    console.log('  改角度：直接在 CleaverBloodVisualState 的 UPPER_DEG / LOWER_DEG');
    console.log('          （以及右侧的 UPPER_RIGHT_DEG / LOWER_RIGHT_DEG）上改，改完 gradle build。');

    const bakeAt = opt('bake', null);

    if (bakeAt !== null) {
        const deg = Number(bakeAt);
        if (!Number.isFinite(deg)) throw new Error('--bake 需要角度值');

        const out = path.resolve(ROOT, opt('out', `art/cleaver_wing${deg}.geo.json`));
        const doc = JSON.parse(fs.readFileSync(GEO, 'utf8'));
        const g = doc['minecraft:geometry'][0];

        for (const w of WINGS) {
            const bone = g.bones.find(b => b.name === w.bone);
            if (!bone) continue;
            bone.rotation = [0, 0, Number((w.sign * deg).toFixed(3))];
        }

        fs.mkdirSync(path.dirname(out), { recursive: true });
        fs.writeFileSync(out, JSON.stringify(doc, null, 2) + '\n', 'utf8');
        console.log(`  已烘焙 ${deg}° 展开态 → ${path.relative(ROOT, out)}`);
        console.log('  （用 render_tex.cjs 出图核对；别把这个文件当成正式 geo）');
    }

    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
