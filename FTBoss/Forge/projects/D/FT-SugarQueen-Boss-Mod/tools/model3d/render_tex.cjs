// 带贴图的 geo 预览器: 读 Bedrock geo + PNG 贴图, 按 box-UV 采样真实像素渲染四视图
// 用法: node render_tex.cjs <geo.json> <tex.png> <out.png> [箱宽] [箱高]
const fs = require('fs');
const { Canvas, decodePNG } = require('./png.cjs');

const geoFile = process.argv[2], texFile = process.argv[3], outFile = process.argv[4] || 'tex_preview.png';
const hbW = parseFloat(process.argv[5] || '0'), hbH = parseFloat(process.argv[6] || '0');

const g = JSON.parse(fs.readFileSync(geoFile, 'utf8'));
const geo = g['minecraft:geometry'][0];
const TW = geo.description.texture_width, TH = geo.description.texture_height;
const tex = decodePNG(texFile);
console.log(`geo ${geo.description.identifier} | 贴图 ${tex.w}x${tex.h} (声明 ${TW}x${TH})`);

/* ---------- 矩阵(Bedrock: y 向上, 前端 -Z) ---------- */
const I = () => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
const mul = (a, b) => { const o = new Array(16); for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) { let s = 0; for (let k = 0; k < 4; k++) s += a[r * 4 + k] * b[k * 4 + c]; o[r * 4 + c] = s; } return o; };
const tr = (x, y, z) => [1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1];
const rX = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1]; };
const rY = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1]; };
const rZ = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; };
const xf = (m, p) => [m[0] * p[0] + m[1] * p[1] + m[2] * p[2] + m[3], m[4] * p[0] + m[5] * p[1] + m[6] * p[2] + m[7], m[8] * p[0] + m[9] * p[1] + m[10] * p[2] + m[11]];

/* ---------- 收集 cube ---------- */
const faces = [];   // {corners:[8][3], uvOrigin:[u,v], size:[w,h,d], bone}
const boneOrder = [];
function walk(bones, parentM) {
  for (const b of bones) {
    const piv = b.pivot || [0, 0, 0];
    // 2026-09-11: 支持骨骼 position(动画里的位移) —— 顺序 = 父级 · T(position) · T(pivot) · R · T(-pivot)
    const bp = b.position || [0, 0, 0];
    let m = mul(parentM, tr(bp[0], bp[1], bp[2]));
    m = mul(m, tr(piv[0], piv[1], piv[2]));
    if (b.rotation) { m = mul(m, rZ(b.rotation[2] || 0)); m = mul(m, rY(b.rotation[1] || 0)); m = mul(m, rX(b.rotation[0] || 0)); }
    m = mul(m, tr(-piv[0], -piv[1], -piv[2]));
    if (!boneOrder.includes(b.name)) boneOrder.push(b.name);
    for (const c of (b.cubes || [])) {
      const inf = c.inflate || 0;
      const s = [c.size[0] + inf * 2, c.size[1] + inf * 2, c.size[2] + inf * 2];
      const o = [c.origin[0] - inf, c.origin[1] - inf, c.origin[2] - inf];
      let cm = m;
      if (c.rotation) {
        const ctr = [o[0] + s[0] / 2, o[1] + s[1] / 2, o[2] + s[2] / 2];
        cm = mul(cm, tr(ctr[0], ctr[1], ctr[2]));
        cm = mul(cm, rZ(c.rotation[2] || 0)); cm = mul(cm, rY(c.rotation[1] || 0)); cm = mul(cm, rX(c.rotation[0] || 0));
        cm = mul(cm, tr(-ctr[0], -ctr[1], -ctr[2]));
      }
      const corners = [];
      for (let i = 0; i < 8; i++) {
        corners.push(xf(cm, [o[0] + (i & 1 ? s[0] : 0), o[1] + (i & 2 ? s[1] : 0), o[2] + (i & 4 ? s[2] : 0)]));
      }
      faces.push({ corners, uv: c.uv, size: c.size, bone: b.name });
    }
    if (b.children) walk(b.children, m);
  }
}
function toTree(bones) {
  if (bones.some(b => b.children)) return bones;
  const map = {}; for (const b of bones) map[b.name] = Object.assign({}, b, { children: [] });
  const roots = []; for (const b of bones) { if (b.parent && map[b.parent]) map[b.parent].children.push(map[b.name]); else roots.push(map[b.name]); }
  return roots;
}
walk(toTree(geo.bones), I());
console.log(`cube ${faces.length} 个`);

/* ---------- UV 区域: 支持两种写法 ----------
   (a) box-UV: "uv": [U,V]  —— 按 cube 尺寸推算六面区域
   (b) 逐面 UV: "uv": { "north": { "uv":[u,v], "uv_size":[w,h] }, ... } —— 直接取该面矩形
   角点编号: bit0=x, bit1=y, bit2=z */
const BOX_IDX = {
  up: [2, 3, 7, 6], down: [0, 1, 5, 4],
  east: [1, 5, 7, 3], west: [4, 0, 2, 6],
  north: [0, 1, 3, 2], south: [5, 4, 6, 7]
};
function faceUV(f, which) {
  if (f.uv && !Array.isArray(f.uv)) {
    const e = f.uv[which];
    if (!e || !Array.isArray(e.uv)) return null;          // 该面未定义
    const s = Array.isArray(e.uv_size) ? e.uv_size : [f.size[0], f.size[1]];
    let w = Math.abs(s[0]), h = Math.abs(s[1]);
    if (!(w > 0)) w = f.size[0];
    if (!(h > 0)) h = f.size[1];
    return { r: [e.uv[0], e.uv[1], w, h], idx: BOX_IDX[which], mirrorU: s[0] < 0, mirrorV: s[1] < 0 };
  }
  const [U, V] = f.uv, [w, h, d] = f.size;
  switch (which) {
    case 'up': return { r: [U + d, V, w, d], idx: BOX_IDX.up };
    case 'down': return { r: [U + d + w, V, w, d], idx: BOX_IDX.down };
    case 'east': return { r: [U, V + d, d, h], idx: BOX_IDX.east };
    case 'west': return { r: [U + d + w, V + d, d, h], idx: BOX_IDX.west };
    case 'north': return { r: [U + d, V + d, w, h], idx: BOX_IDX.north };
    case 'south': return { r: [U + d + w + d, V + d, w, h], idx: BOX_IDX.south };
  }
}
const FACE_N = { up: [0, 1, 0], down: [0, -1, 0], east: [1, 0, 0], west: [-1, 0, 0], north: [0, 0, -1], south: [0, 0, 1] };

/* ---------- 画布 ---------- */
// 面板尺寸/缩放可用环境变量放大(小模型如小姜饼人需要 FT_PANEL=600 FT_SCALE=14 才看得清五官)
const PANEL = parseInt(process.env.FT_PANEL || '250', 10), PAD = 8,
      SCALE = parseFloat(process.env.FT_SCALE || '4.2'), OY = parseFloat(process.env.FT_OY || '210'), OX = PANEL / 2;
const cv = new Canvas(PANEL * 2 + PAD, PANEL * 2 + PAD);
const VIEWS = [
  { name: 'front', proj: p => [p[0], -p[1]], depth: p => -p[2], cam: [0, 0, -1] },
  { name: 'left', proj: p => [p[2], -p[1]], depth: p => -p[0], cam: [-1, 0, 0] },
  { name: 'back', proj: p => [-p[0], -p[1]], depth: p => p[2], cam: [0, 0, 1] },
  { name: 'right', proj: p => [-p[2], -p[1]], depth: p => p[0], cam: [1, 0, 0] },
  { name: 'top', proj: p => [p[0], p[2]], depth: p => p[1], cam: [0, 1, 0] },
  { name: 'iso', proj: p => [(p[0] - p[2]) * 0.866, -p[1] + (p[0] + p[2]) * 0.5], depth: p => p[0] + p[2] - p[1], cam: [-0.6, -0.5, -0.6] }
];
// FT_VIEWS=front,right,back,iso 可指定要哪 4 个视图(默认 正/左/俯/等轴); 做"三视图对照稿"时用 front,right,back,iso
if (process.env.FT_VIEWS) {
    const want = process.env.FT_VIEWS.split(',').map(s => s.trim().toLowerCase());
    const picked = want.map(n => VIEWS.find(v => v.name === n)).filter(Boolean);
    if (picked.length) VIEWS.splice(0, VIEWS.length, ...picked);
}
function sample(u, v) {
  let tx = Math.floor(u), ty = Math.floor(v);
  tx = ((tx % tex.w) + tex.w) % tex.w; ty = ((ty % tex.h) + tex.h) % tex.h;
  const i = (ty * tex.w + tx) * 4;
  return [tex.data[i], tex.data[i + 1], tex.data[i + 2], tex.data[i + 3]];
}
/** 带 UV 的三角形光栅化 */
function triTex(p0, p1, p2, t0, t1, t2, shade) {
  const minX = Math.max(0, Math.floor(Math.min(p0[0], p1[0], p2[0]))), maxX = Math.min(cv.w - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0])));
  const minY = Math.max(0, Math.floor(Math.min(p0[1], p1[1], p2[1]))), maxY = Math.min(cv.h - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1])));
  const d = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1]);
  if (Math.abs(d) < 1e-9) return;
  for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
    const a0 = ((p1[1] - p2[1]) * (x - p2[0]) + (p2[0] - p1[0]) * (y - p2[1])) / d;
    const a1 = ((p2[1] - p0[1]) * (x - p2[0]) + (p0[0] - p2[0]) * (y - p2[1])) / d;
    const a2 = 1 - a0 - a1;
    if (!((a0 >= -1e-6 && a1 >= -1e-6 && a2 >= -1e-6) || (a0 <= 1e-6 && a1 <= 1e-6 && a2 <= 1e-6))) continue;
    const u = a0 * t0[0] + a1 * t1[0] + a2 * t2[0];
    const v = a0 * t0[1] + a1 * t1[1] + a2 * t2[1];
    const c = sample(u, v);
    if (c[3] < 8) continue;      // 透明像素留底
    cv.set(x, y, c[0] * shade, c[1] * shade, c[2] * shade, 1);
  }
}
for (let i = 0; i < 4; i++) {
  const px = (i % 2) * (PANEL + PAD), py = Math.floor(i / 2) * (PANEL + PAD);
  cv.rect(px, py, PANEL, PANEL, 26, 28, 32, 1);
  cv.hline(px, px + PANEL, py + OY, 90, 200, 120, 1);
  if (hbW) {
    const hw = hbW * 16 / 2, hh = hbH * 16, P = (x, y, z) => VIEWS[i].proj([x, y, z]);
    const box = [[-hw, 0, -hw], [hw, 0, -hw], [hw, 0, hw], [-hw, 0, hw], [-hw, hh, -hw], [hw, hh, -hw], [hw, hh, hw], [-hw, hh, hw]];
    const S = box.map(p => { const q = P(p[0], p[1], p[2]); return [Math.round(px + OX + q[0] * SCALE), Math.round(py + OY + q[1] * SCALE)]; });
    for (const [a, b] of [[0, 1], [1, 2], [2, 3], [3, 0], [4, 5], [5, 6], [6, 7], [7, 4], [0, 4], [1, 5], [2, 6], [3, 7]]) cv.line(S[a][0], S[a][1], S[b][0], S[b][1], 80, 150, 255, 0.45);
  }
  const list = [];
  for (const f of faces) {
    for (const which of ['up', 'down', 'east', 'west', 'north', 'south']) {
      const n = FACE_N[which];
      const dd = n[0] * VIEWS[i].cam[0] + n[1] * VIEWS[i].cam[1] + n[2] * VIEWS[i].cam[2];
      if (dd <= 0.01) continue;
      const fu = faceUV(f, which);
      if (!fu) continue;
      const { r, idx, mirrorU, mirrorV } = fu;
      const pts = idx.map(k => f.corners[k]);
      const cen = [0, 1, 2].map(j => pts.reduce((s, p) => s + p[j], 0) / 4);
      let uvs = [[r[0], r[1]], [r[0] + r[2], r[1]], [r[0] + r[2], r[1] + r[3]], [r[0], r[1] + r[3]]];
      if (mirrorU) uvs = uvs.map(([u, v]) => [2 * r[0] + r[2] - u, v]);
      if (mirrorV) uvs = uvs.map(([u, v]) => [u, 2 * r[1] + r[3] - v]);
      list.push({ pts, uvs, depth: VIEWS[i].depth(cen), shade: 0.62 + 0.38 * dd });
    }
  }
  list.sort((a, b) => a.depth - b.depth);
  for (const f of list) {
    const sp = f.pts.map(p => { const q = VIEWS[i].proj(p); return [px + OX + q[0] * SCALE, py + OY + q[1] * SCALE]; });
    triTex(sp[0], sp[1], sp[2], f.uvs[0], f.uvs[1], f.uvs[2], f.shade);
    triTex(sp[0], sp[2], sp[3], f.uvs[0], f.uvs[2], f.uvs[3], f.shade);
  }
}
cv.save(outFile);
console.log('带贴图预览已写出: ' + outFile + '  (视图顺序: ' + VIEWS.map((v, i) => `${['左上', '右上', '左下', '右下'][i]}=${v.name}`).join(' ') + ')');
