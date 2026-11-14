/* ============================================================================
 *  v3_edge_light.cjs —— §8-2「刃口高光窄而有间断，避免连续白色灯带」
 *   改动(纯贴图):
 *     ① 刃口最外那条**连续**的近白线(C6 #EAD5F5) -> 改成**断续**(每 0.36 画 0.16, 覆盖率约 44%)
 *     ② 剑尖三级收薄的亮线 C6 -> 降一档到 C5(#A96CDC 魔法紫), 只在最外一格保留 C6(§8: 尖端收边)
 *     ③ 验收: 重新统计近白像素(判定 R>200,G>190,B>220) —— 目标从 187 降到 ≤80(先收敛, 不一次砍到底)
 *   不变量: 几何/UV/骨骼/动画全部不动。
 *  用法: node tools/model3d/v3_edge_light.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const GEN = path.join(ROOT, 'tools', 'model3d', 'regen_shadow_sword.cjs');
let s = fs.readFileSync(GEN, 'utf8');
const hit = [];
const rep = (tag, a, b) => { const n = s.split(a).length - 1; hit.push([tag, n]); if (n > 0) s = s.split(a).join(b); };

/* ① 刃口最外线: 连续 -> 断续 */
rep('dashed-edge',
    `    vLine(6.0 + dx + notch + 0.02, y0, y1, A(C6, 248));                    // 最外刃线`,
    `    /* 最外刃线: **断续**高光(每 0.36 画 0.16) —— §8「窄而有间断, 不要连续白色灯带」 */
    for (let yy = y0 + 0.04; yy < y1 - 0.10; yy += 0.36) {
        vLine(6.0 + dx + notch + 0.02, yy, Math.min(y1, yy + 0.16), A(C6, 238));
    }`);

/* ② 剑尖亮线降档 */
rep('tip-a', `vLine(6.22, 23.40, 23.85, A(C6, 244));`, `vLine(6.22, 23.40, 23.85, A(C5, 205));`);
rep('tip-b', `vLine(6.77, 23.85, 24.25, A(C6, 240));`, `vLine(6.77, 23.85, 24.25, A(C5, 200));`);
rep('tip-c', `vLine(7.37, 24.25, 24.65, A(C6, 238));`, `vLine(7.37, 24.25, 24.65, A(C5, 195));`);

fs.writeFileSync(GEN, s);
hit.forEach(([t, n]) => console.log('  ' + (n === 1 ? '✓' : '✗') + ' ' + t.padEnd(13) + ' 命中 ' + n));
