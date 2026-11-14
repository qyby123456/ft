/* arc_fit.cjs —— 只读参数求解：给副刃找一组满足全部硬约束的弧参数
 *
 * 约束（全部来自生成器/守门工具的真实判据，不做保守近似）：
 *   ① 副刃伸出（含旋转盒角点）尽量接近 主刃伸出 × 0.60
 *   ② **精确碰撞**：柄的实体（柄杆/握柄/柄尾的 AABB）不得落进副刃的
 *      "环形扇区"里（内半径 2.95 ~ 外半径 5.90、角度 [start, start+sweep]）。
 *      上一版用"内缘最左 x"当代理判据，把合法解全排除了 —— 因为开口朝上时
 *      内缘当然会伸到轴的左边，但那是**空心**，不碰柄。
 *   ③ 副刃的实体不与主刃实体重叠（主刃实体 x ≤ 主弧根层内缘最右值 = -3.95）
 *   ④ 弧的 y 跨度要有主刃的 50%~95%
 * 半径固定 5.9（附件原值）；搜索圆心 (cx,cy)、起始角、跨度。
 * 用法: node tools/model3d/arc_fit.cjs
 */
'use strict';
const D2R = Math.PI / 180;
const LAYERS = [[2.95, 4.45], [4.21, 5.42], [5.18, 5.69], [5.54, 5.90]];
const RIN = 2.95, ROUT = 5.90;
const WF = 1.15;
const MAIN_REACH = 10.389, TARGET = MAIN_REACH * 0.60;
const MAIN_SOLID_MAX_X = -3.95;                 // 主刃实体最右的 x（主弧根层内缘极值）
const MAIN_TOP = 21.50, MAIN_BOT = 1.26;        // 主刃 y 跨度（生成器实测）
/* 柄的实体（生成器里的实际值；AABB） */
const HANDLE = [
  { n: 'shaft', x: [-1.15, 1.15], y: [-20.0, 0.45] },
  { n: 'grip', x: [-1.95, 1.95], y: [-20.0, 0.45] },
  { n: 'band', x: [-1.95, 1.95], y: [-20.0, 0.45] },
  { n: 'pommel', x: [-1.50, 1.50], y: [-20.0, 0.45] },
];
const HEAD = { x: [-3.00, 3.22], y: [0.10, 15.55] };   // 中央体（副刃要咬进去，不算冲突）

function aabbOf(step, cx, cy, angle, r0, r1) {
  const a = angle * D2R, ca = Math.cos(a), sa = Math.sin(a);
  const rm = (r0 + r1) / 2, lx = cx + rm * ca, ly = cy + rm * sa;
  const tw = 2 * r1 * Math.tan(step / 2 * D2R) * WF, hw = tw / 2;
  const ox = lx - (r1 - r0) / 2 * ca + hw * sa, oy = ly - (r1 - r0) / 2 * sa - hw * ca;
  const cnt = [ox + (r1 - r0) / 2, oy + tw / 2], hx = (r1 - r0) / 2, hy = tw / 2;
  const c = Math.cos(a), s = Math.sin(a), out = [];
  for (const dx of [-hx, hx]) for (const dy of [-hy, hy]) out.push([cnt[0] + dx * c - dy * s, cnt[1] + dx * s + dy * c]);
  return out;
}
/** 点是否落进环形扇区（副刃的实体） */
function inSector(px, py, cx, cy, start, sweep) {
  const dx = px - cx, dy = py - cy;
  const d = Math.hypot(dx, dy);
  if (d < RIN || d > ROUT) return false;
  let a = Math.atan2(dy, dx) / D2R;
  while (a < start) a += 360; while (a >= start + 360) a -= 360;
  return a >= start - 1e-9 && a <= start + sweep + 1e-9;
}
/** 柄的 AABB 是否与扇区相交。
 *  解析做法（不做网格采样，否则 5 万组参数会跑到天荒地老）：
 *    ① 把 AABB 的 4 个角点相对圆心的极角区间求出来；
 *    ② 该区间与扇区角度区间有交集，且角点里存在半径落在 [RIN,ROUT] 的；
 *    ③ 再用"扇区边界与 AABB 各边的交点"补一轮（覆盖 AABB 完全包住扇区的情况）。 */
function handleHits(cx, cy, start, sweep) {
  const a0 = start, a1 = start + sweep;
  const angOf = (px, py) => {
    let a = Math.atan2(py - cy, px - cx) / D2R;
    while (a < a0 - 180) a += 360; while (a > a0 + 180) a -= 360;
    return a;
  };
  const inSectorPt = (px, py, a) => {
    const d = Math.hypot(px - cx, py - cy);
    if (d < RIN || d > ROUT) return false;
    return a >= a0 - 1e-9 && a <= a1 + 1e-9;
  };
  for (const h of HANDLE) {
    const xs = [h.x[0], h.x[1]], ys = [h.y[0], h.y[1]];
    for (const px of xs) for (const py of ys) if (inSectorPt(px, py, angOf(px, py))) return h.n;
    /* 扇区的两条直边（角度 a0 / a1 的射线）与 AABB 求交 */
    for (const a of [a0, a1]) {
      const dx = Math.cos(a * D2R), dy = Math.sin(a * D2R);
      for (const t of [RIN, ROUT, (RIN + ROUT) / 2]) {
        const px = cx + t * dx, py = cy + t * dy;
        if (px >= h.x[0] && px <= h.x[1] && py >= h.y[0] && py <= h.y[1]) return h.n;
      }
    }
    /* AABB 的边与扇区外弧/内弧的交点 */
    for (let d = a0; d <= a1; d += 2) {
      for (const r of [RIN, ROUT]) {
        const px = cx + r * Math.cos(d * D2R), py = cy + r * Math.sin(d * D2R);
        if (px >= h.x[0] && px <= h.x[1] && py >= h.y[0] && py <= h.y[1]) return h.n;
      }
    }
  }
  return null;
}
/** 扇区最左点是否越过主刃实体 */
function hitsMain(cx, cy, start, sweep) {
  for (let d = start; d <= start + sweep; d += 0.5) {
    for (const r of [RIN, (RIN + ROUT) / 2, ROUT]) {
      if (cx + r * Math.cos(d * D2R) < MAIN_SOLID_MAX_X + 0.20) return true;
    }
  }
  return false;
}

function ev(cx, cy, start, sweep) {
  const step = sweep / 9;
  let minX = 1e9, maxX = -1e9, minY = 1e9, maxY = -1e9;
  for (let i = 0; i < 9; i++) {
    const ang = start + (i + 0.5) * step;
    for (const [r0, r1] of LAYERS) for (const p of aabbOf(step, cx, cy, ang, r0, r1)) {
      minX = Math.min(minX, p[0]); maxX = Math.max(maxX, p[0]);
      minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
    }
  }
  /* 副刃根层要能与中央体咬住：根层内缘是否落在中央体的 x 区间里 */
  const rootHitsHead = (() => {
    for (let d = start; d <= start + sweep; d += 0.5) {
      const x = cx + RIN * Math.cos(d * D2R), y = cy + RIN * Math.sin(d * D2R);
      if (x > HEAD.x[0] && x < HEAD.x[1] && y > HEAD.y[0] && y < HEAD.y[1]) return true;
    }
    return false;
  })();
  return { minX, maxX, minY, maxY, reach: maxX, height: maxY - minY, rootHitsHead };
}

const rows = [];
for (let cx = -1.0; cx <= 3.0; cx += 0.1) {
  for (let cy = 4.0; cy <= 16.0; cy += 0.25) {
    for (let sweep = 60; sweep <= 145; sweep += 5) {
      for (let start = -20; start <= 220 - sweep; start += 2) {
        if (handleHits(cx, cy, start, sweep)) continue;
        if (hitsMain(cx, cy, start, sweep)) continue;
        const e = ev(cx, cy, start, sweep);
        if (e.maxX < 4.5 || e.maxX > 8.0) continue;
        if (!e.rootHitsHead) continue;
        const hRatio = e.height / (MAIN_TOP - MAIN_BOT);
        if (hRatio < 0.50 || hRatio > 1.0) continue;
        rows.push({ cx, cy, start, sweep, ...e, hRatio, err: Math.abs(e.reach - TARGET) });
      }
    }
  }
}
rows.sort((a, b) => a.err - b.err);
console.log(`主刃: 伸出 ${MAIN_REACH}  y ${MAIN_BOT}~${MAIN_TOP}  实体最右 x ${MAIN_SOLID_MAX_X}`);
console.log(`目标副刃伸出 ${TARGET.toFixed(3)}   可行解 ${rows.length} 个`);
for (const r of rows.slice(0, 15)) {
  console.log(`  cx=${r.cx.toFixed(2)} cy=${r.cy.toFixed(2)} start=${r.start.toFixed(1)} sweep=${r.sweep}` +
    `  reach=${r.reach.toFixed(3)}(${(r.reach / MAIN_REACH * 100).toFixed(1)}%) 高比=${(r.hRatio * 100).toFixed(0)}%` +
    ` y[${r.minY.toFixed(2)},${r.maxY.toFixed(2)}] x[${r.minX.toFixed(2)},${r.maxX.toFixed(2)}]`);
}
