/* arc_probe.cjs —— 只读探针：按附件 (22)/(23) 给的确切数值，算出弧刃的关键几何
 *   主弧 center(-0.65,10) r=9.8  115°→265°（150°）
 *   副弧 center( 0.65,10) r=5.9  -70°→ 70°（140°）
 * 用来定：中央体（斧眼）要多大才能同时咬住两弧根层、以及枢轴能不能落在中央体内。
 * 用法: node tools/model3d/arc_probe.cjs
 */
'use strict';
const D2R = Math.PI / 180;
const ARCS = {
  main: { c: [-0.65, 10], r: 9.8, a0: 115, a1: 265, layers: [[4.90, 7.40], [7.00, 9.00], [8.60, 9.45], [9.20, 9.80]] },
  secondary: { c: [0.65, 10], r: 5.9, a0: -70, a1: 70, layers: [[2.95, 4.45], [4.21, 5.42], [5.18, 5.69], [5.54, 5.90]] },
};
const p = (c, r, a) => [c[0] + r * Math.cos(a * D2R), c[1] + r * Math.sin(a * D2R)];

for (const [name, a] of Object.entries(ARCS)) {
  console.log(`\n=== ${name} 弧  center ${a.c}  r ${a.r}  ${a.a0}°→${a.a1}° ===`);
  const inR = a.layers[0][0], outR = a.layers[3][1];
  console.log(`  根层内缘 r=${inR}  外缘 r=${outR}`);
  const lo = Math.min(a.a0, a.a1), hi = Math.max(a.a0, a.a1);
  let minX = 1e9, maxX = -1e9, minY = 1e9, maxY = -1e9;
  let inMinX = 1e9, inMaxX = -1e9, inMinY = 1e9, inMaxY = -1e9;
  let farIn = -1e9, farOut = -1e9;
  for (let d = lo; d <= hi; d += 0.25) {
    const o = p(a.c, outR, d), i = p(a.c, inR, d);
    minX = Math.min(minX, o[0]); maxX = Math.max(maxX, o[0]);
    minY = Math.min(minY, o[1]); maxY = Math.max(maxY, o[1]);
    inMinX = Math.min(inMinX, i[0]); inMaxX = Math.max(inMaxX, i[0]);
    inMinY = Math.min(inMinY, i[1]); inMaxY = Math.max(inMaxY, i[1]);
  }
  console.log(`  外缘 x[${minX.toFixed(2)}, ${maxX.toFixed(2)}]  y[${minY.toFixed(2)}, ${maxY.toFixed(2)}]`);
  console.log(`  内缘 x[${inMinX.toFixed(2)}, ${inMaxX.toFixed(2)}]  y[${inMinY.toFixed(2)}, ${inMaxY.toFixed(2)}]`);
  /* 内缘朝"离圆心最远/最近"的点 */
  for (const d of [lo, (lo + hi) / 2, hi]) {
    const i = p(a.c, inR, d);
    console.log(`    ${d}° 内缘 (${i[0].toFixed(2)}, ${i[1].toFixed(2)})   外缘 (${p(a.c, outR, d).map(v => v.toFixed(2)).join(', ')})`);
  }
  /* 每个分层的外端 x（用来判断中央体要多宽才咬得住根层） */
  a.layers.forEach((L, i) => {
    const pts = [];
    for (let d = lo; d <= hi; d += 0.5) pts.push(p(a.c, L[0], d), p(a.c, L[1], d));
    console.log(`    层${i} r[${L[0]},${L[1]}] x[${Math.min(...pts.map(q => q[0])).toFixed(2)}, ${Math.max(...pts.map(q => q[0])).toFixed(2)}]`);
  });
}

/* 三段划分（上翼 / 静止中刃 / 下翼）：附件 (23) 的 locator 角度就是段中点 */
console.log('\n=== 三段划分（用附件 (23) 的 locator 角度反推）===');
console.log('主弧 150° 三等分 → 每段 50.0°：上翼 115°→165°（中 140°）中刃 165°→215°（中 190°）下翼 215°→265°（中 240°）');
console.log('  但 locator 是 123.333/190/256.667 → 附件其实按 **九段** 分的（每段 16.667°）：');
for (const [nm, c, r] of [['main', ARCS.main.c, 9.8], ['secondary', ARCS.secondary.c, 5.9]]) {
  const a0 = nm === 'main' ? 115 : -70, sweep = nm === 'main' ? 150 : 140, n = 9;
  const step = sweep / n;
  console.log(`  ${nm}: step=${step.toFixed(3)}°  九段中点角度 = ${[0, 1, 2, 3, 4, 5, 6, 7, 8].map(i => (a0 + (i + 0.5) * step).toFixed(2)).join(', ')}`);
  const groups = { upper: [0, 1, 2], static: [3, 4, 5], lower: [6, 7, 8] };
  for (const [g, idx] of Object.entries(groups)) {
    const gs = idx.map(i => a0 + i * step), ge = a0 + (idx[idx.length - 1] + 1) * step;
    const pi = p(c, 9.8 * (nm === 'main' ? 1 : r / 9.8), a0 + (idx[0] + 0.5) * step);
    console.log(`    ${g}: ${gs[0].toFixed(2)}° → ${ge.toFixed(2)}°（跨度 ${(ge - gs[0]).toFixed(2)}°）`);
  }
  /* 三翼分段数 3/3/3 → 每翼跨度 50°（主弧） */
}
