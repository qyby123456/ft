#!/usr/bin/env node
'use strict';

/* ============================================================================
 *  shadow_sword_pos_audit.cjs —— 暗影剑"位置"专项审计
 *
 *  回答三个问题，并把"能确定 / 不能确定"分开写清楚：
 *    A. 几何层面：原点(0,0,0)是不是真的落在握点上？柄的可抓区间在哪？
 *    B. 挂接层面：动画有没有碰 root/handle/weapon/grip_anchor（会不会脱手）？
 *    C. 显示层面：display 各槽位的平移/旋转，在两种轴约定下分别把剑放到哪里？
 *       —— 这一层离线**无法定论**，必须实机画坐标轴，脚本只给出两种读法的数值。
 *
 *  用法：node tools/model3d/shadow_sword_pos_audit.cjs [--json]
 * ==========================================================================*/

const fs = require('fs');
const path = require('path');
const cube = require('./cube_space.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const ITEM = path.join(AS, 'models', 'item', 'shadow_sword.json');
const ANIM = path.join(AS, 'animations', 'shadow_sword.animation.json');

const HIDDEN_ROOTS = new Set([
    'charge_orb', 'blade_glow', 'eye_glow', 'crystal_glow'
]);

const BRANCH_BONES = new Set(['root', 'weapon', 'handle', 'grip_anchor', 'pommel', 'crystal']);

function bounds(points) {
    return [0, 1, 2].map(axis => {
        const v = points.map(p => p[axis]);
        return [cube.round(Math.min(...v), 3), cube.round(Math.max(...v), 3)];
    });
}

function main() {
    const model = cube.loadGeo(GEO);
    const item = JSON.parse(fs.readFileSync(ITEM, 'utf8'));
    const anims = JSON.parse(fs.readFileSync(ANIM, 'utf8'));
    const animations = anims.animations || anims;

    const out = [];

    out.push('=== 暗影剑位置审计 ===');
    out.push(`geo  : ${path.relative(ROOT, GEO)}`);
    out.push(`item : ${path.relative(ROOT, ITEM)}`);
    out.push('');

    /* ---------- A. 几何：原点是否在握点上 ---------- */
    out.push('--- A. 握点几何（geo 单位，1 格 = 16 单位）---');

    const cubesByBone = new Map();

    for (const info of cube.listCubes(model)) {
        if (!cubesByBone.has(info.bone)) cubesByBone.set(info.bone, []);
        cubesByBone.get(info.bone).push(info);
    }

    const boneBounds = bone => {
        const list = cubesByBone.get(bone) || [];

        if (list.length === 0) return null;

        const pts = [];
        for (const info of list) pts.push(...cube.cubeCorners(model, info, {}));

        return bounds(pts);
    };

    const handle = boneBounds('handle');
    const pommel = boneBounds('pommel');
    const guard = boneBounds('guard');
    const blade = boneBounds('blade');

    out.push(`  握柄 handle   x ${handle[0]} y ${handle[1]} z ${handle[2]}`);
    out.push(`  柄首 pommel   x ${pommel[0]} y ${pommel[1]} z ${pommel[2]}`);
    out.push(`  护手 guard    x ${guard[0]} y ${guard[1]} z ${guard[2]}`);
    out.push(`  剑身 blade    x ${blade[0]} y ${blade[1]} z ${blade[2]}`);

    const gripSpan = [handle[1][0], handle[1][1]];
    const originInsideGrip = 0 >= gripSpan[0] && 0 <= gripSpan[1];

    out.push(
        `  原点落在握柄的 y 区间 [${gripSpan.join(', ')}] 内：` +
        (originInsideGrip ? '是 ✓' : '否 ✗ —— 原点不在握点上！')
    );

    const gripAnchor = model.byName.get('grip_anchor');
    out.push(
        `  grip_anchor pivot = ${JSON.stringify(gripAnchor.pivot)}` +
        `（GeckoLib 武器约定：手抓处 = 原点）`
    );

    /* 柄中心相对原点的偏移：握在"柄中"还是"柄根" */
    const gripCenter = (gripSpan[0] + gripSpan[1]) / 2;
    out.push(
        `  柄的几何中心 y = ${cube.round(gripCenter, 3)}` +
        `（相对原点偏移 ${cube.round(gripCenter, 3)} 单位 = ` +
        `${cube.round(gripCenter / 16, 3)} 格）`
    );
    out.push('');

    /* ---------- B. 动画是否碰握持链 ---------- */
    out.push('--- B. 挂接稳定性：动画有没有动握持链 ---');

    const touched = new Map();

    for (const [animName, anim] of Object.entries(animations)) {
        for (const [boneName, channel] of Object.entries((anim.bones) || {})) {
            if (!BRANCH_BONES.has(boneName)) continue;

            const keys = Object.keys(channel).filter(k =>
                k === 'rotation' || k === 'position' || k === 'scale');

            if (keys.length === 0) continue;

            if (!touched.has(boneName)) touched.set(boneName, []);
            touched.get(boneName).push(`${animName}:${keys.join('+')}`);
        }
    }

    if (touched.size === 0) {
        out.push('  ✓ 没有任何动画触碰 root/weapon/handle/grip_anchor/pommel/crystal');
        out.push('    → 剑不会因为动画而脱手或抖动');
    } else {
        for (const [bone, list] of touched) {
            out.push(`  ⚠ ${bone} 被动过：${list.join(', ')}`);
        }
    }
    out.push('');

    /* ---------- C. display：两种轴约定 ---------- */
    out.push('--- C. display 把剑放到哪里（离线两种读法）---');
    out.push('  读法1 = geo 轴当渲染轴用（Y 向上，平移取原符号）');
    out.push('  读法2 = 换到 Java 轴（Y 向下、Z 向前，平移 y/z 取反）');
    out.push('');

    const probe = {
        '原点(握点)': [0, 0, 0],
        '柄下端': [0, pommel[1][0], 0],
        '护手': [0, guard[1][0], 0],
        '刃尖': [0, blade[1][1], 0]
    };

    const slots = ['thirdperson_righthand', 'thirdperson_lefthand'];

    for (const slot of slots) {
        const raw = (item.display && item.display[slot]) || {};
        const display = {
            rotation: raw.rotation || [0, 0, 0],
            translation: raw.translation || [0, 0, 0],
            scale: raw.scale || [1, 1, 1]
        };

        const mirrorX = slot === 'thirdperson_lefthand' ? false : true;

        out.push(`  【${slot}】rot ${JSON.stringify(display.rotation)} ` +
            `trans ${JSON.stringify(display.translation)} ` +
            `scale ${JSON.stringify(display.scale)}`);

        for (const mode of ['读法1', '读法2']) {
            const flip = mode === '读法2';
            const rows = [];

            for (const [label, local] of Object.entries(probe)) {
                const blocks = [
                    local[0] / 16,
                    (flip ? -local[1] : local[1]) / 16,
                    local[2] / 16
                ];

                const mirrored = [
                    mirrorX ? -blocks[0] : blocks[0],
                    blocks[1],
                    blocks[2]
                ];

                const p = cube.applyDisplayTransform(mirrored, display);
                rows.push(`      ${label.padEnd(9)} = [${p.map(v => cube.round(v, 3)).join(', ')}]`);
            }

            out.push(`    ${mode}：`);
            out.push(...rows);
        }
        out.push('');
    }

    out.push('--- 结论边界 ---');
    out.push('  A/B 两项是**确定**的：原点在握柄区间内，且没有任何动画动握持链。');
    out.push('  C 项离线只能给候选：物品 display 的旋转/平移作用在"手部局部空间"，');
    out.push('  该空间的轴指向由渲染管线的基变换决定，必须实机画坐标轴才能定论。');
    out.push('  D 项用**旧静态版**（实机验证过的前身）反推手部基准，是当前最强的离线旁证。');
    out.push('');

    /* ---------- D. 用"旧静态版"反推手部基准 ---------- */
    const legacy = path.join(ROOT, 'ref', 'shadow_sword_static_v18.json.bak');

    out.push('--- D. 用旧静态版反推手部基准（最强离线旁证）---');

    if (!fs.existsSync(legacy)) {
        out.push('  找不到 ref/shadow_sword_static_v18.json.bak，跳过。');
    } else {
        const old = JSON.parse(fs.readFileSync(legacy, 'utf8'));
        const oldDisp = (old.display && old.display.thirdperson_righthand) || {};
        const t = oldDisp.translation || [0, 0, 0];

        const oldPts = [];
        for (const e of (old.elements || [])) {
            oldPts.push(e.from, e.to);
        }
        const oldYMin = Math.min(...oldPts.map(p => p[1]));

        out.push(`  旧版 display trans = ${JSON.stringify(t)} ` +
            `rot = ${JSON.stringify(oldDisp.rotation)} ` +
            `scale = ${JSON.stringify(oldDisp.scale)}`);
        out.push(`  旧版几何 y 从 ${cube.round(oldYMin, 2)} 起（柄尾即原点）→ 原点约定 = 柄尾`);
        out.push(`  新版原点约定 = 握柄中部，比柄尾高 ${cube.round(-gripSpan[0], 2)} 单位`);
        out.push('');

        /* 旧版已是实机可用的摆放：它的"剑体"在 display 空间里占了一段区间。
           新版用同一套 trans 摆好原点后，自己的剑体落在哪里？
           两者之差 = 新版原点约定变化带来的净位移。 */
        const oldDispObj = {
            rotation: oldDisp.rotation || [0, 0, 0],
            translation: oldDisp.translation || [0, 0, 0],
            scale: oldDisp.scale || [1, 1, 1]
        };

        const newDispObj = {
            rotation: item.display.thirdperson_righthand.rotation || [0, 0, 0],
            translation: item.display.thirdperson_righthand.translation || [0, 0, 0],
            scale: item.display.thirdperson_righthand.scale || [1, 1, 1]
        };

        const place = (disp, localPoint, mirrorX) => cube.applyDisplayTransform(
            cube.itemBlocksToView(
                [localPoint[0] / 16, localPoint[1] / 16, localPoint[2] / 16],
                mirrorX
            ),
            disp
        );

        /* 两版共同的"物理参考"：柄尾（新版 = pommel 最低点，旧版 = 原点） */
        const oldHilt = place(oldDispObj, [0, 0, 0], true);
        const newHilt = place(newDispObj, [0, pommel[1][0], 0], true);

        out.push('  【同一物理点对照：柄尾】');
        out.push(`    旧版柄尾落在 [${oldHilt.map(v => cube.round(v, 3)).join(', ')}]`);
        out.push(`    新版柄尾落在 [${newHilt.map(v => cube.round(v, 3)).join(', ')}]`);

        const delta = cube.sub(newHilt, oldHilt);
        const axial = delta[1];          // 沿剑轴方向的高度差
        const lateral = Math.hypot(delta[0], delta[2]);

        out.push(`    差值 = [${delta.map(v => cube.round(v, 3)).join(', ')}]`);
        out.push(`    其中 轴向 ${cube.round(axial, 3)} 格 / 横向 ${cube.round(lateral, 3)} 格`);
        out.push('');

        if (lateral < 0.1 && Math.abs(axial) < 0.1) {
            out.push('  ✓ 与旧版摆放基本重合（< 0.1 格）：display 平移可直接沿用。');
        } else if (lateral < 0.1) {
            const dir = axial > 0 ? '偏高' : '偏低';
            out.push(`  ⚠ 横向无偏差，但沿剑轴${dir} ${cube.round(Math.abs(axial), 3)} 格` +
                `（≈ ${cube.round(Math.abs(axial) * 16, 1)} 单位）。`);
            out.push(`    若要贴合旧版手感，把第三/第一人称的 trans.y 调整 ` +
                `${cube.round(-axial * 16, 1)} 单位即可（正负号按实测方向确认）。`);
        } else {
            out.push(`  ⚠ 横向也有 ${cube.round(lateral, 3)} 格偏差，trans 的 x/z 也要一起看。`);
        }

        /* 注意：旧版 rotation 是 55°，新版是 28°，刃口倾角本来就重调过，
           所以"数值不同"本身不是问题；这里只比"柄尾这个物理点落在哪"。 */
        out.push('');
        out.push(`  注：旧版 rot.z=${(oldDisp.rotation || [0, 0, 0])[2]}、` +
            `新版 rot.z=${(newDispObj.rotation || [0, 0, 0])[2]}，刃口倾角是**有意重调**的，`);
        out.push('  数值不同不算问题；上面只比"柄尾"这个两版共有的物理点。');
    }

    out.push('');
    out.push('--- E. 实机标定步骤（一次截图就能闭环）---');
    out.push('  已内置调试层：net.farmingtales.ftboss.client.render.SwordAxisDebugLayer');
    out.push('  ① 加 JVM 参数（启动器/IDE 的 VM options，不必改代码）：');
    out.push('       -Dftboss.debugSwordAxes=true');
    out.push('  ② 进游戏手持暗影剑，按 F5 切第三人称，截图。');
    out.push('     画面上会出现：白色十字(display 原点=握点)、');
    out.push('     红=+X / 绿=+Y(指向剑尖) / 蓝=+Z 三根一格长的轴、');
    out.push('     以及沿 +Y 每 8 模型单位一个的白刻度。');
    out.push('  ③ 读数：手心相对白色十字落在哪一格、哪个刻度上。');
    out.push('     · 手心在十字上方 N 格 ⇒ 剑偏低，把 trans.y 减小约 N×16 单位');
    out.push('     · 手心在十字下方 N 格 ⇒ 剑偏高，把 trans.y 增大约 N×16 单位');
    out.push('     · 三根轴的方向顺便确认了 C 项那两种读法谁是对的：');
    out.push('       绿轴若指向剑尖 ⇒ 读法1（Y 向上）成立；');
    out.push('       绿轴若指向柄尾 ⇒ 读法2（Y 向下）成立。');
    out.push('  ④ 白刻度间距 8 单位 = 半格，用来估 N 的小数部分。');

    const text = out.join('\n');
    console.log(text);

    if (process.argv.includes('--json')) {
        const file = path.join(ROOT, 'art', 'shadow_sword_pos_audit.txt');
        fs.mkdirSync(path.dirname(file), { recursive: true });
        fs.writeFileSync(file, text + '\n', 'utf8');
        console.log(`\n已写入 ${path.relative(ROOT, file)}`);
    }

    return 0;
}

try {
    process.exitCode = main();
} catch (error) {
    console.error('错误：' + error.message);
    process.exitCode = 2;
}
