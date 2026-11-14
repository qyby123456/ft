#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  sword_grip_fit.cjs —— 算"手柄该穿进手心多少"才能看着像握住
 *
 *  背景（2026-09-15）：
 *    用户要"部分模型穿过玩家的手"来做出握着的感觉（三叉戟那种）。
 *    原版三叉戟不是物品模型——它由硬编码的 TridentRenderer 渲染，JSON 抄不到，
 *    所以只能按手里的几何反算 display.translation。
 *
 *  手里的几何（1 方块 = 16 单位，全部有出处）：
 *    · 拳 ≈ 4×4×4 单位（玩家手臂 4×12×4，拳头就是末端那段）
 *    · 拳心（离线算过，见 boss_arm_axis.cjs）：
 *        横向 x = -0.375，竖直 y = 0.667，前后 z ≈ 0
 *    · 剑的柄：y 从 -2.67（金属箍）到 +3.37（护手下沿），剑原点在柄中部
 *
 *  结论：让**拳心对应柄上偏下一点的握点**（真实握法：拳心比剑原点略低），
 *        柄的上半段就自然穿进拳里 —— 不需要凭空加穿透量。
 *
 *  用法：node tools/model3d/sword_grip_fit.cjs [--apply]
 *        --apply 会直接写进 item/shadow_sword.json（自动备份）
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const MODEL = path.join(
    ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'models', 'item', 'shadow_sword.json'
);

const U = 1 / 16;

/* ---- 手里的几何 ---- */
const FIST = {
    center: [-0.375, 0.667, 0.0],   // 拳心（离线算）
    half: 2 / 16                    // 拳半宽 = 2 单位
};

/* ---- 剑的柄（geo 单位，自 shadow_sword_pos_audit 的实测）---- */
const HANDLE = { yMin: -2.67, yMax: 3.37, gripCenter: 0.35 };

/* ---- 目标：拳心落在柄上的位置（单位，相对剑原点）---- */
/** 向下偏 2 单位 = 握在柄中偏下，柄上半段穿进拳里。 */
const GRIP_OFFSET = -2.0;

const target = [FIST.center[0], FIST.center[1] + GRIP_OFFSET * U, FIST.center[2]];

/* display 把剑原点放到哪里：origin = R·S·(0,0,0) + trans/16 = trans/16 */
/* 所以要让原点落在 target，就得 trans = target * 16 ... 但还要考虑镜像与显示空间，
   这里只给"竖直方向"的可信换算（y 是唯一我们能离线定死方向的轴）。*/
const current = JSON.parse(fs.readFileSync(MODEL, 'utf8'));
const cur = current.display.thirdperson_righthand.translation;

const curOriginY = cur[1] * U;
const wantOriginY = target[1];
const deltaY = (wantOriginY - curOriginY) * 16;

console.log('=== 握持贴合计算 ===');
console.log(`拳心（离线算）      = [${FIST.center.map(v => v.toFixed(4)).join(', ')}] 格`);
console.log(`目标握点（拳心往下 ${Math.abs(GRIP_OFFSET)} 单位） = y = ${target[1].toFixed(4)} 格`);
console.log('');
console.log(`柄的几何范围 y = ${HANDLE.yMin} .. ${HANDLE.yMax}（剑原点 = 0）`);
console.log(`  柄上半段 ${-HANDLE.yMin} 单位会落在拳心以下的位置 → 穿进拳里`);
console.log('');
console.log('当前 display（thirdperson_righthand）：');
console.log(`  rotation    ${JSON.stringify(current.display.thirdperson_righthand.rotation)}`);
console.log(`  translation ${JSON.stringify(cur)}`);
console.log(`  → 剑原点 y = ${curOriginY.toFixed(4)} 格`);
console.log('');
console.log(`要让原点落到 y = ${wantOriginY.toFixed(4)}，translation.y 需要 ${deltaY >= 0 ? '+' : ''}${deltaY.toFixed(2)}`);
console.log(`  ⇒ 建议 translation = [${cur[0]}, ${(cur[1] + deltaY).toFixed(2)}, ${cur[2]}]`);
console.log('');
console.log('⚠ 只有 y 轴能离线定死方向（x/z 受手部空间基影响，必须进游戏看）。');
console.log('   所以先按建议值改 y，x/z 保持标准套件的 0 / 0.5 不动。');

if (process.argv.includes('--apply')) {
    const bak = MODEL + '.grip.bak';

    if (!fs.existsSync(bak)) {
        fs.copyFileSync(MODEL, bak);
        console.log(`\n已备份：${path.relative(ROOT, bak)}`);
    }

    const json = fs.readFileSync(MODEL, 'utf8');
    const before = JSON.parse(json);

    for (const slot of ['thirdperson_righthand', 'thirdperson_lefthand']) {
        before.display[slot].translation[1] = Number((cur[1] + deltaY).toFixed(2));
    }

    fs.writeFileSync(MODEL, JSON.stringify(before, null, 2) + '\n', 'utf8');

    console.log('\n已写入（third/lefthand 的 translation.y）：');
    console.log('  ' + JSON.stringify(before.display.thirdperson_righthand.translation));
    console.log('  ' + JSON.stringify(before.display.thirdperson_lefthand.translation));
    console.log('\n游戏里按 F3+T 重载资源即可看到效果（不用重新构建）。');
} else {
    console.log('\n（加 --apply 才会写入）');
}
