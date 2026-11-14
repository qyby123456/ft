#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  boss_arm_axis.cjs —— 离线算"折耳根举剑"的手臂姿态，并画出对比图
 *
 *  为什么写它：
 *    2026-09-15 那几轮里，`EasterBossModel.SWORD_POSE_{X,Y,Z}_ROT` 哪边是"往内"、
 *    哪边是"往前"，靠游戏截图判了两次都判错（手在模型后面 + 相机俯视）。
 *    其实这件事**可以离线算死**——EasterBossModel extends PlayerModel extends
 *    HumanoidModel，骨骼链就是原版那套，数值确定。
 *
 *  坐标（与 hand_check.cjs 的参照体一致，实机验证过与游戏同比例）：
 *    脚底 y=0，1 方块 = 16 模型单位
 *    躯干 8×12×4 @ x±4  y12..24  z±2
 *    臂   4×12×4 @ x -8..-4(右)   y12..24  z±2
 *    锚点：PlayerModel.createMesh 把四肢 pivot 放在 y = 2 模型单位（肩上）
 *
 *  用法：
 *    node tools/model3d/boss_arm_axis.cjs           # 打印数值表
 *    node tools/model3d/boss_arm_axis.cjs --png     # 另出 art/boss_arm_axis.png
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const U = 1 / 16;

const PIVOT_Y = 2 * U;
const ARM_LEN = 12 * U;
const ARM_HALF = 2 * U;
const TORSO_HALF = 4 * U;
const ANCHOR_X = -(TORSO_HALF + ARM_HALF);
const BODY_X_ROT = 0.07;

/**
 * 与 EasterBossModel 完全一致的映射（右臂 sign=+1）：
 *   xRot = -FORWARD, yRot = -YAW, zRot = +ROLL
 * 再用 HumanoidModel 的 PartPose 顺序 Z → Y → X 作用到"手臂朝 -Y"的端点上。
 */
function handPoint(forward, yaw, roll, bodyXRot = BODY_X_ROT) {
    let x = 0;
    let y = -ARM_LEN;
    let z = 0;

    const zRot = roll;
    const yRot = -yaw;
    const xRot = -forward;

    let c = Math.cos(zRot);
    let s = Math.sin(zRot);
    [x, y] = [x * c - y * s, x * s + y * c];

    c = Math.cos(yRot);
    s = Math.sin(yRot);
    [x, z] = [x * c + z * s, -x * s + z * c];

    c = Math.cos(xRot);
    s = Math.sin(xRot);
    [y, z] = [y * c - z * s, y * s + z * c];

    x += ANCHOR_X;
    y += PIVOT_Y;

    c = Math.cos(bodyXRot);
    s = Math.sin(bodyXRot);
    [y, z] = [y * c - z * s, y * s + z * c];

    return [x, y, z];
}

const REST = handPoint(0, 0, 0);

/** 现在三个角度已解耦：fwd=前抬, yaw=正往内, roll=正往外 */
const CASES = [
    { label: '原始  fwd=0.785 yaw=+0.30 roll=-0.30', forward: 0.785, yaw: 0.30, roll: -0.30 },
    { label: '第2版 fwd=0.120 yaw=-0.10 roll=+0.10', forward: 0.120, yaw: -0.10, roll: 0.10 },
    { label: '第3版 fwd=0.120 yaw=+0.30 roll=-0.30', forward: 0.120, yaw: 0.30, roll: -0.30 },
    { label: '当前  fwd=0.200 yaw=+0.10 roll=-0.10', forward: 0.200, yaw: 0.10, roll: -0.10 },
    { label: '更贴  fwd=0.200 yaw=+0.45 roll=-0.20', forward: 0.200, yaw: 0.45, roll: -0.20 },
    { label: '静止  fwd=0     yaw=0     roll=0', forward: 0.0, yaw: 0.0, roll: 0.0 }
];

const fmt = v => (v >= 0 ? ' ' : '') + v.toFixed(4);

console.log('=== 折耳根举剑：手掌端点离线计算 ===');
console.log(`躯干中线 x=0，右臂锚点 x=${ANCHOR_X.toFixed(4)} 格（肩上，y=${PIVOT_Y.toFixed(4)}）`);
console.log('');
console.log('姿态                                 手掌 x     手掌 y     手掌 z   横向(比静止)  前后(比静止)');
console.log('-------------------------------------------------------------------------------------------');

for (const c of CASES) {
    const [x, y, z] = handPoint(c.forward, c.yaw, c.roll);
    const dx = x - REST[0];
    const dz = z - REST[2];

    console.log(
        c.label + '  ' +
        fmt(x).padStart(9) + fmt(y).padStart(11) + fmt(z).padStart(11) +
        ((dx >= 0 ? '+' : '') + dx.toFixed(4)).padStart(14) +
        ((dz >= 0 ? '+' : '') + dz.toFixed(4)).padStart(14)
    );
}

console.log('');
console.log('=== 关键结论 ===');
console.log(`  静止时手掌 = (${fmt(REST[0])}, ${fmt(REST[1])}, ${fmt(REST[2])})`);
console.log('');
const a = handPoint(0.785, 0.30, -0.30);
const b = handPoint(0.200, 0.10, -0.10);
console.log('  ★ "往外飘"的主因是 fwd（前抬），不是左右：');
console.log(`     原始 fwd=0.785 → 手伸到身体前方 z=+${a[2].toFixed(3)} 格`);
console.log(`     当前 fwd=0.200 → z=+${b[2].toFixed(3)} 格（收回 ${(a[2] - b[2]).toFixed(3)} 格）`);
console.log('');
console.log('  ★ yaw 与 roll 解耦后的单调关系（fwd 固定 0.20）：');

for (const yaw of [-0.30, 0.0, 0.30, 0.60]) {
    const p = handPoint(0.20, yaw, 0.0);
    console.log(`     yaw=${yaw.toFixed(2).padStart(5)} roll=0 → 手掌 x=${fmt(p[0])}  ${p[0] > REST[0] ? '（比静止靠内）' : '（比静止靠外）'}`);
}

console.log('');
console.log('  说明：这只算到"手掌末端中心"。剑的最终位置还要叠加 ItemInHandLayer 的');
console.log('        位移与物品 display，所以别把这里的 x/z 当成剑尖坐标。');

if (process.argv.includes('--png')) {
    drawPng();
}

/* ---------- 画对比图：正视 + 俯视 ---------- */

function drawPng() {
    let PNG;

    try {
        PNG = require('./png.cjs');
    } catch (e) {
        console.log('\n（没有 png.cjs，跳过出图）');
        return;
    }

    const { encodePNG } = PNG;

    const CELL = 200;
    const SS = 2;
    const W = CELL * CASES.length;
    const H = CELL * 2;

    const buf = new Uint8Array(W * SS * H * SS * 4);

    const put = (px, py, r, g, b) => {
        if (px < 0 || py < 0 || px >= W * SS || py >= H * SS) return;
        const o = (py * W * SS + px) * 4;
        buf[o] = r; buf[o + 1] = g; buf[o + 2] = b; buf[o + 3] = 255;
    };

    const rect = (x0, y0, x1, y1, r, g, b) => {
        for (let y = Math.round(y0 * SS); y < Math.round(y1 * SS); y++) {
            for (let x = Math.round(x0 * SS); x < Math.round(x1 * SS); x++) {
                put(x, y, r, g, b);
            }
        }
    };

    const line3 = (cell, view, a, b, r, g, bl) => {
        // view: 'front' (x-y) 或 'top' (x-z)
        const px = v => view === 'front' ? v[0] : v[0];
        const py = v => view === 'front' ? -v[1] : v[2];

        const ox = cell * CELL + CELL / 2;
        const oy = (view === 'front' ? CELL / 2 : CELL + CELL / 2);
        const k = CELL / 2.0;   // 视野 ±1 格

        const steps = 40;
        for (let i = 0; i <= steps; i++) {
            const t = i / steps;
            const x = px(a) + (px(b) - px(a)) * t;
            const y = py(a) + (py(b) - py(a)) * t;
            rect(ox + x * k - 1, oy + y * k - 1 * (view === 'front' ? -1 : -1),
                ox + x * k + 1, oy + y * k + 1, r, g, bl);
        }
    };

    for (let i = 0; i < W * SS * H * SS; i++) {
        buf[i * 4] = 246; buf[i * 4 + 1] = 246; buf[i * 4 + 2] = 249; buf[i * 4 + 3] = 255;
    }

    CASES.forEach((c, i) => {
        const [hx, hy, hz] = handPoint(c.forward, c.inward);

        // 躯干 + 右臂（正视 x-y）
        const drawFront = () => {
            const ox = i * CELL + CELL / 2;
            const oy = CELL / 2;
            const k = CELL / 2.0;
            const P = (x, y) => [ox + x * k, oy - y * k];

            // 躯干
            let [x0, y0] = P(-TORSO_HALF, 1.5);
            let [x1, y1] = P(TORSO_HALF, 0.75);
            rect(x0, y0, x1, y1, 150, 150, 160);

            // 右臂：肩 → 手
            let [ax, ay] = P(ANCHOR_X, PIVOT_Y);
            let [bx, by] = P(hx, hy);
            rect(Math.min(ax, bx) - 3, Math.min(ay, by) - 3, Math.max(ax, bx) + 3, Math.max(ay, by) + 3, 90, 150, 200);

            // 手（红点）
            rect(bx - 5, by - 5, bx + 5, by + 5, 220, 60, 60);

            // 中线
            let [mx] = P(0, 0);
            rect(mx, 0, mx + 1, CELL, 200, 200, 210);
        };

        // 俯视（x-z）
        const drawTop = () => {
            const ox = i * CELL + CELL / 2;
            const oy = CELL + CELL / 2;
            const k = CELL / 2.0;
            const P = (x, z) => [ox + x * k, oy + z * k];

            let [x0, z0] = P(-TORSO_HALF, -2 * U);
            let [x1, z1] = P(TORSO_HALF, 2 * U);
            rect(x0, z0, x1, z1, 150, 150, 160);

            let [ax, az] = P(ANCHOR_X, 0);
            let [bx, bz] = P(hx, hz);
            rect(Math.min(ax, bx) - 3, Math.min(az, bz) - 3, Math.max(ax, bx) + 3, Math.max(az, bz) + 3, 90, 150, 200);
            rect(bx - 5, bz - 5, bx + 5, bz + 5, 220, 60, 60);
        };

        drawFront();
        drawTop();
    });

    // 降采样
    const out = new Uint8Array(W * H * 4);

    for (let y = 0; y < H; y++) {
        for (let x = 0; x < W; x++) {
            let r = 0, g = 0, b = 0;
            for (let dy = 0; dy < SS; dy++) {
                for (let dx = 0; dx < SS; dx++) {
                    const o = ((y * SS + dy) * W * SS + x * SS + dx) * 4;
                    r += buf[o]; g += buf[o + 1]; b += buf[o + 2];
                }
            }
            const o = (y * W + x) * 4;
            out[o] = r / (SS * SS); out[o + 1] = g / (SS * SS); out[o + 2] = b / (SS * SS); out[o + 3] = 255;
        }
    }

    const file = path.join(ROOT, 'art', 'boss_arm_axis.png');
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, encodePNG(W, H, out));
    console.log(`\n对比图：${path.relative(ROOT, file)}（上排=正视，下排=俯视；红点=手掌，蓝=手臂，灰=躯干）`);
}
