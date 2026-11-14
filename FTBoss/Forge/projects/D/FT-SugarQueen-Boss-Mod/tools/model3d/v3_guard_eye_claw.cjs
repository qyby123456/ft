/* ============================================================================
 *  v3_guard_eye_claw.cjs —— 裂瞳 V3 · §5 护手眼眶 + 护爪关节（只动 cube，不碰骨骼/动画）
 *   ① 眼眶: 挂到 **eye_mount**(已有空骨骼, 父级 guard, 不随虹膜缩放) —— 4 条边条组成包住虹膜的眼眶,
 *      深度 2.75 < 虹膜 3.15 -> 虹膜前后各凸出 0.2(嵌入感), 顶条止于 Java y 9.45 < 刀身底 9.60(不会顶到刀身)
 *   ② 护爪: 爪根加厚(h+0.15)、爪尖收细(h-0.45 且更薄) —— 用户「爪根厚、尖端薄」
 *  不变量: 全部骨骼名/父级/pivot/rotation 不变; 动画文件一字不改; eye/pupil 的运动能力不变
 *  坐标说明: 转换器的关节块使用 geo 空间(Java x-8 / y-4.5 / z-8), 下面数值已换算好
 *  用法: node tools/model3d/v3_guard_eye_claw.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const CONV = path.join(ROOT, 'tools', 'model3d', 'java_to_geo_rig.cjs');
let s = fs.readFileSync(CONV, 'utf8');
const hit = [];
const rep = (tag, a, b) => { const n = s.split(a).length - 1; hit.push([tag, n]); if (n > 0) s = s.split(a).join(b); };

/* ① 眼眶挂到 eye_mount */
rep('eye-socket',
    `    if (!byName['eye_mount']) bones.push({ name: 'eye_mount', parent: 'guard', pivot: fix(PIVOT.eye_mount), cubes: [] });`,
    `    if (!byName['eye_mount']) {
        /* 眼眶: 4 条边条包住虹膜(虹膜前后各凸 0.2); 挂在 eye_mount -> **不随虹膜缩放**(用户 §5 硬要求) */
        const eUv = (byName['eye'] && byName['eye'].cubes[0] && byName['eye'].cubes[0].uv) || { north: { uv: [0, 0], uv_size: [2, 2] } };
        const bar = (x0, x1, y0, y1) => ({ origin: [x0, y0, -1.375], size: [+(x1 - x0).toFixed(3), +(y1 - y0).toFixed(3), 2.75], uv: eUv });
        bones.push({
            name: 'eye_mount', parent: 'guard', pivot: fix(PIVOT.eye_mount),
            cubes: [bar(-1.75, -1.35, 3.30, 4.80), bar(1.35, 1.75, 3.30, 4.80), bar(-1.35, 1.35, 4.70, 4.95), bar(-1.35, 1.35, 3.05, 3.30)],
        });
    }`);

/* ② 护爪: 根厚、尖细 */
rep('claw-root', `            b.cubes = [{ origin: [rootX, y0 + 0.15, z0], size: [rootW, h - 0.3, d], uv: c.uv }];`,
    `            b.cubes = [{ origin: [rootX, y0 - 0.05, z0], size: [rootW, h + 0.15, d], uv: c.uv }];   // V3: 爪根加厚`);
rep('claw-tip', `                cubes: [{ origin: [tipX, y0 - 0.1, z0 + 0.1], size: [tipW, h + 0.5, Math.max(0.2, d - 0.2)], uv: c.uv }],`,
    `                cubes: [{ origin: [tipX, y0 + 0.15, z0 + 0.22], size: [tipW, h - 0.45, Math.max(0.2, d - 0.45)], uv: c.uv }],   // V3: 爪尖收细`);

fs.writeFileSync(CONV, s);
hit.forEach(([t, n]) => console.log('  ' + (n === 1 ? '✓' : '✗') + ' ' + t.padEnd(12) + ' 命中 ' + n));
