/* ============================================================================
 *  v3_blade.cjs —— 裂瞳 V3 · §4 剑刃改造（只动 blade 内的 cube，不碰骨骼/动画）
 *   ① 每段 10 条细碎切片 -> **8 条三级厚度**：
 *        薄刃口 0.42 / 0.62 -> 剑面 1.15 -> 剑脊 2.00(略厚) + 保留一道血槽(1.45 比两侧 2.00 薄)
 *      宽度合计仍是 4.0 -> **保留现有剑长与主要宽度**（用户 §4）
 *   ② 新增 **2 个主要倒钩**(背侧、斜后掠 22.5°、静态旋转) —— 用户 §4「另一侧保留 2 个主要倒钩」
 *   ③ 贴图画法自动跟随(它按 STRIP_W/STRIP_D/STRIP_R 数组遍历), 无需改画法
 *  不变量: blade 骨骼名/父级/pivot[0,5.1,0] 不变; 动画文件一字不改; 其他骨骼不动
 *  用法: node tools/model3d/v3_blade.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const GEN = path.join(ROOT, 'tools', 'model3d', 'regen_shadow_sword.cjs');
let s = fs.readFileSync(GEN, 'utf8');
const hit = [];
const rep = (tag, a, b) => { const n = s.split(a).length - 1; hit.push([tag, n]); if (n > 0) s = s.split(a).join(b); };

/* ① 三级厚度 8 条（宽度合计 4.0 保持不变） */
rep('strips',
    `const STRIP_W = [0.22, 0.30, 0.38, 1.12, 0.20, 0.14, 0.18, 0.14, 0.20, 1.12];
const STRIP_D = [0.30, 0.48, 0.72, 1.10, 1.70, 1.95, 1.35, 1.95, 1.70, 1.10];
const STRIP_R = ['edge', 'edge', 'edge3', 'plate', 'spine2', 'spine', 'groove', 'spine', 'spine2', 'plate'];`,
    `/* V3: 8 条三级厚度(薄刃口 -> 剑面 -> 略厚剑脊 + 一道血槽)。宽度合计仍 4.0 -> 轮廓/剑长不变。
   旧的 10 条里有 4 条 0.14~0.20 的细碎切片, 正是用户说的"细碎锯齿", 已删除。 */
const STRIP_W = [0.34, 0.52, 1.05, 0.26, 0.24, 0.30, 0.24, 1.05];
const STRIP_D = [0.42, 0.62, 1.15, 1.75, 2.00, 1.45, 2.00, 1.75];
const STRIP_R = ['edge', 'edge3', 'plate', 'spine2', 'spine', 'groove', 'spine', 'spine2'];`);

/* ② 两个主要倒钩（背侧 = x 大侧；斜后掠 22.5°，静态旋转） */
rep('barbs',
    `put('tip_a', 6.20, 10.20, 23.40, 23.85, 1.10, 'face');`,
    `/* V3 倒钩: 背侧两个主要倒钩(斜后掠 22.5°), 替代原来那些细碎小凸块 */
put('barb_lo', 9.55, 10.55, 14.35, 15.65, 1.30, 'edge3', 0, [22.5, 'z', [9.6, 15.0, 8]]);
put('barb_hi', 9.55, 10.55, 18.35, 19.65, 1.30, 'edge3', 0, [22.5, 'z', [9.6, 19.0, 8]]);
put('tip_a', 6.20, 10.20, 23.40, 23.85, 1.10, 'face');`);

fs.writeFileSync(GEN, s);
hit.forEach(([t, n]) => console.log('  ' + (n === 1 ? '✓' : '✗') + ' ' + t.padEnd(10) + ' 命中 ' + n));
