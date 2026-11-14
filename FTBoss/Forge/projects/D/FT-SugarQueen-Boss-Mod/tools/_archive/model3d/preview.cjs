// 离线 Bedrock geo 预览器: 输出四视图 PNG(轮廓/比例自检) + 包围盒数值
// 用法: node preview.cjs <geo.json> <out.png> [hitboxW_blocks] [hitboxH_blocks]
const fs = require('fs');
const { Canvas } = require('./png.cjs');

/* ---------- 矩阵: 4x4 行主序 ---------- */
const I = () => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
function mul(a, b) { const o = new Array(16); for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) { let s = 0; for (let k = 0; k < 4; k++) s += a[r * 4 + k] * b[k * 4 + c]; o[r * 4 + c] = s; } return o; }
function trans(x, y, z) { return [1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1]; }
function rotX(d) { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1]; }
function rotY(d) { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1]; }
function rotZ(d) { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; }
function xform(m, p) { return [m[0] * p[0] + m[1] * p[1] + m[2] * p[2] + m[3], m[4] * p[0] + m[5] * p[1] + m[6] * p[2] + m[7], m[8] * p[0] + m[9] * p[1] + m[10] * p[2] + m[11]]; }

const geoFile = process.argv[2], outFile = process.argv[3] || 'preview.png';
const hbW = parseFloat(process.argv[4] || '0'), hbH = parseFloat(process.argv[5] || '0');
const g = JSON.parse(fs.readFileSync(geoFile, 'utf8'));
const geo = g['minecraft:geometry'][0];
/** Bedrock 的 bones 是扁平列表(+parent 字段); 也兼容嵌套 children 写法 —— 这里统一成树 */
function toTree(bones) {
  if (bones.some(b => b.children)) return bones;
  const map = {};
  for (const b of bones) map[b.name] = Object.assign({}, b, { children: [] });
  const roots = [];
  for (const b of bones) {
    if (b.parent && map[b.parent]) map[b.parent].children.push(map[b.name]);
    else roots.push(map[b.name]);
  }
  return roots;
}
const GEO_BONES = toTree(geo.bones);

/* ---------- 收集所有 cube(带世界变换与本骨名) ---------- */
const PALETTE = [
  [200, 120, 60], [170, 95, 45], [140, 80, 40], [120, 70, 35],
  [210, 170, 70], [180, 145, 55], [150, 120, 45], [230, 200, 90],
  [130, 40, 45], [160, 50, 55], [100, 30, 35], [190, 60, 60],
  [90, 90, 100], [120, 120, 130], [70, 70, 80], [150, 150, 160],
  [80, 130, 80], [60, 100, 60], [200, 200, 210], [230, 230, 240]
];
const cubes = [];
const boneNames = [];
function walk(bones, parentM, depth) {
  for (const b of bones) {
    const piv = b.pivot || [0, 0, 0];
    let m = mul(parentM, trans(piv[0], piv[1], piv[2]));
    if (b.rotation) { m = mul(m, rotZ(b.rotation[2] || 0)); m = mul(m, rotY(b.rotation[1] || 0)); m = mul(m, rotX(b.rotation[0] || 0)); }
    m = mul(m, trans(-piv[0], -piv[1], -piv[2]));
    if (!boneNames.includes(b.name)) boneNames.push(b.name);
    for (const c of (b.cubes || [])) {
      const inf = c.inflate || 0;
      const o = c.origin, s = [c.size[0] + inf * 2, c.size[1] + inf * 2, c.size[2] + inf * 2];
      const oo = [o[0] - inf, o[1] - inf, o[2] - inf];
      let cm = m;
      if (c.rotation) {
        const ctr = [oo[0] + s[0] / 2, oo[1] + s[1] / 2, oo[2] + s[2] / 2];
        cm = mul(cm, trans(ctr[0], ctr[1], ctr[2]));
        cm = mul(cm, rotZ(c.rotation[2] || 0)); cm = mul(cm, rotY(c.rotation[1] || 0)); cm = mul(cm, rotX(c.rotation[0] || 0));
        cm = mul(cm, trans(-ctr[0], -ctr[1], -ctr[2]));
      }
      const corners = [];
      for (let i = 0; i < 8; i++) {
        const p = [oo[0] + (i & 1 ? s[0] : 0), oo[1] + (i & 2 ? s[1] : 0), oo[2] + (i & 4 ? s[2] : 0)];
        corners.push(xform(cm, p));
      }
      const col = PALETTE[boneNames.indexOf(b.name) % PALETTE.length];
      cubes.push({ bone: b.name, corners, col, depthHint: depth });
    }
    if (b.children) walk(b.children, m, depth + 1);
  }
}
walk(GEO_BONES, I(), 0);

/* ---------- 包围盒 ---------- */
let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
for (const c of cubes) for (const p of c.corners) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], p[i]); mx[i] = Math.max(mx[i], p[i]); }
const u = v => +v.toFixed(2), ub = v => +(v / 16).toFixed(2);
console.log(`骨骼数=${boneNames.length} cube数=${cubes.length}`);
console.log(`包围盒 X ${u(mn[0])}..${u(mx[0])} (${ub(mx[0] - mn[0])}格)  Y ${u(mn[1])}..${u(mx[1])} (${ub(mx[1] - mn[1])}格)  Z ${u(mn[2])}..${u(mx[2])} (${ub(mx[2] - mn[2])}格)`);
if (hbW) console.log(`碰撞箱 宽 ${hbW} 格 高 ${hbH} 格  ->  模型宽/箱宽 = ${(ub(mx[0] - mn[0]) / hbW).toFixed(2)}  模型高/箱高 = ${(ub(mx[1] - mn[1]) / hbH).toFixed(2)}`);

/* ---------- 面 ---------- */
/* 角点编号: bit0=x, bit1=y, bit2=z  =>  0(x0,y0,z0) 1(x1,y0,z0) 2(x0,y1,z0) 3(x1,y1,z0) 4(x0,y0,z1) 5(x1,y0,z1) 6(x0,y1,z1) 7(x1,y1,z1) */
const F = [
  { n: [0, 0, -1], idx: [0, 1, 3, 2] },   // -Z : z=z0
  { n: [0, 0, 1], idx: [4, 5, 7, 6] },    // +Z : z=z1
  { n: [-1, 0, 0], idx: [0, 2, 6, 4] },   // -X : x=x0
  { n: [1, 0, 0], idx: [1, 3, 7, 5] },    // +X : x=x1
  { n: [0, -1, 0], idx: [0, 1, 5, 4] },   // -Y : y=y0
  { n: [0, 1, 0], idx: [2, 3, 7, 6] }     // +Y : y=y1
];
const VIEWS = [
  { name: 'front', proj: p => [p[0], -p[1]], depth: p => -p[2] },
  { name: 'left', proj: p => [p[2], -p[1]], depth: p => -p[0] },
  { name: 'top', proj: p => [p[0], p[2]], depth: p => p[1] },
  { name: 'iso', proj: p => [(p[0] - p[2]) * 0.866, -(p[1]) + (p[0] + p[2]) * 0.5], depth: p => p[0] + p[2] - p[1] }
];

const PANEL = 250, PAD = 8, SCALE = 4.2, OY = 210, OX = PANEL / 2;
const cv = new Canvas(PANEL * 2 + PAD, PANEL * 2 + PAD);
for (let i = 0; i < 4; i++) {
  const px = (i % 2) * (PANEL + PAD), py = Math.floor(i / 2) * (PANEL + PAD);
  // 面板底
  cv.rect(px, py, PANEL, PANEL, 26, 28, 32, 1);
  // 16 单位(1格)网格
  for (let k = -3; k <= 3; k++) {
    const gx = px + OX + k * 16 * SCALE;
    if (gx > px && gx < px + PANEL) cv.vline(Math.round(gx), py, py + PANEL, 45, 48, 54, 1);
  }
  for (let k = -1; k <= 4; k++) {
    const gy = py + OY - k * 16 * SCALE;
    if (gy > py && gy < py + PANEL) cv.hline(px, px + PANEL, Math.round(gy), 45, 48, 54, 1);
  }
  // 地面线(y=0)
  cv.hline(px, px + PANEL, py + OY, 90, 200, 120, 1);
  // 碰撞箱线框
  if (hbW) {
    const hw = hbW * 16 / 2, hh = hbH * 16;
    const P = (x, y, z) => VIEWS[i].proj([x, y, z]);
    const box = [[-hw, 0, -hw], [hw, 0, -hw], [hw, 0, hw], [-hw, 0, hw], [-hw, hh, -hw], [hw, hh, -hw], [hw, hh, hw], [-hw, hh, hw]];
    const S = box.map(p => { const q = P(p[0], p[1], p[2]); return [Math.round(px + OX + q[0] * SCALE), Math.round(py + OY + q[1] * SCALE)]; });
    const E = [[0, 1], [1, 2], [2, 3], [3, 0], [4, 5], [5, 6], [6, 7], [7, 4], [0, 4], [1, 5], [2, 6], [3, 7]];
    for (const [a, b] of E) cv.line(S[a][0], S[a][1], S[b][0], S[b][1], 80, 150, 255, 0.55);
  }
  // 面: 只画朝向观察者的, 按深度排序
  const faces = [];
  const camDir = VIEWS[i].name === 'front' ? [0, 0, -1] : VIEWS[i].name === 'left' ? [-1, 0, 0] : VIEWS[i].name === 'top' ? [0, 1, 0] : [-0.6, -0.5, -0.6];
  for (const c of cubes) {
    for (const f of F) {
      const d = f.n[0] * camDir[0] + f.n[1] * camDir[1] + f.n[2] * camDir[2];
      if (d <= 0.01) continue;
      const pts = f.idx.map(k => c.corners[k]);
      const cen = [0, 1, 2].map(j => pts.reduce((s, p) => s + p[j], 0) / 4);
      faces.push({ pts, depth: VIEWS[i].depth(cen), col: c.col, shade: 0.55 + 0.45 * d });
    }
  }
  faces.sort((a, b) => a.depth - b.depth);
  let drawn = 0;
  let x0 = 1e9, x1 = -1e9, y0 = 1e9, y1 = -1e9;
  for (const f of faces) {
    const sp = f.pts.map(p => { const q = VIEWS[i].proj(p); return [px + OX + q[0] * SCALE, py + OY + q[1] * SCALE]; });
    for (const [X, Y] of sp) { x0 = Math.min(x0, X); x1 = Math.max(x1, X); y0 = Math.min(y0, Y); y1 = Math.max(y1, Y); }
    cv.quad(sp, f.col[0] * f.shade, f.col[1] * f.shade, f.col[2] * f.shade, 1);
    drawn++;
  }
  console.log(`  [${VIEWS[i].name}] 面=${drawn} 面板=(${px},${py}) 屏幕x ${x0.toFixed(0)}..${x1.toFixed(0)} 屏幕y ${y0.toFixed(0)}..${y1.toFixed(0)} 画布=${cv.w}x${cv.h}`);
  // 统计该面板内"非背景色"像素, 用来判断面到底有没有落上去
  let nonBg = 0, sample = null;
  for (let y = py; y < Math.min(py + PANEL, cv.h); y++) for (let x = px; x < Math.min(px + PANEL, cv.w); x++) {
    const k = (y * cv.w + x) * 4;
    const r = cv.d[k], g = cv.d[k + 1], b2 = cv.d[k + 2];
    const isBg = (r === 26 && g === 28 && b2 === 32) || (r === 45 && g === 48 && b2 === 54) || (r === 90 && g === 200 && b2 === 120);
    if (!isBg) { nonBg++; if (!sample) sample = [x, y, r, g, b2]; }
  }
  console.log(`         面板内非背景像素 = ${nonBg}${sample ? '  首个样本 @(' + sample[0] + ',' + sample[1] + ') rgb(' + sample[2] + ',' + sample[3] + ',' + sample[4] + ')' : ''}`);
}
cv.save(outFile);
console.log('预览已写出: ' + outFile + '  (左上=正面 右上=左侧面 左下=俯视 右下=等轴)');
