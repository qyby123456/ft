/* ============================================================================
 *  v3_segments.cjs —— 让 4 个分段读得出来（用户：多节武器每段要明显, 中间的节再明显一点）
 *   ① 几何: 每段厚度递增 SEGF = [1.00, 1.045, 1.09, 1.14] —— 侧面轮廓出现"一段一段"的台阶,
 *      但宽度与总长不变(轮廓不炸), 只在厚度方向做 4.5% 递增
 *   ② 贴图: 每段交界处从原来的 1 条极淡 AO 线, 改成"节带"三件套:
 *        交界上沿 0.14 高的深色带(C1) + 紧上方 1px 冷色受光脊(C3) + 节带中间一道暗紫 C4(弱)
 *      —— 这是让"节"看得见的关键(凹槽那轮已验证: 有明暗交界才读得出结构)
 *   ③ 中间两节(第 2、3 段交界)再明显一点点: 节带加粗到 0.20 且紫线更亮一档
 *  不变量: 骨骼/UV 布局/动画全部不动; 只改 cube 厚度与贴图画法。
 *  用法: node tools/model3d/v3_segments.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const GEN = path.join(ROOT, 'tools', 'model3d', 'regen_shadow_sword.cjs');
let s = fs.readFileSync(GEN, 'utf8');
const hit = [];
const rep = (tag, a, b) => { const n = s.split(a).length - 1; hit.push([tag, n]); if (n > 0) s = s.split(a).join(b); };

/* ① 每段厚度递增 */
rep('segf',
    `const STRIP_W = [0.34, 0.52, 1.05, 0.26, 0.24, 0.30, 0.24, 1.05];`,
    `/* 每段厚度递增 4.5% -> 侧面看出"一段一段"(宽度与总长不变, 只动厚度) */
const SEGF = [1.00, 1.045, 1.09, 1.14];
const STRIP_W = [0.34, 0.52, 1.05, 0.26, 0.24, 0.30, 0.24, 1.05];`);
rep('strip-depth',
    `STRIP_D[j], STRIP_R[j], j === 5 ? 0.02 : 0);`,
    `+(STRIP_D[j] * SEGF[i]).toFixed(3), STRIP_R[j], j === 5 ? 0.02 : 0);`);

/* ② ③ 节带三件套（中间两节更明显） */
rep('joint-band',
    `    fillModel(6.0 + dx + notch, y1 - 0.04, 6.0 + dx + notch + 4.0, y1, () => A(C1, 120));   // 段间 AO`,
    `    /* 节带: 段交界上沿的深色带 + 受光脊 + 中间暗紫线; 中间两节加粗、紫线更亮 */
    {
        const jx = 6.0 + dx + notch;
        const strong = (i === 1 || i === 2);
        const bh = strong ? 0.20 : 0.14;
        fillModel(jx, y1 - bh, jx + 4.0, y1, () => A(C1, strong ? 246 : 226));
        hLine(y1 - bh - 0.06, jx, jx + 4.0, A(C3, strong ? 205 : 175));
        hLine(y1 - bh * 0.5, jx + 0.5, jx + 3.5, A(C4, strong ? 170 : 120));
    }`);

fs.writeFileSync(GEN, s);
hit.forEach(([t, n]) => console.log('  ' + (n === 1 ? '✓' : '✗') + ' ' + t.padEnd(13) + ' 命中 ' + n));
