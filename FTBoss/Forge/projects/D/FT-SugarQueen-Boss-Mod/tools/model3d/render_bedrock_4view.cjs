// Bedrock geometry(GeckoLib/Blockbench 的 minecraft:geometry) → **四视图 + 真实贴图采样**渲染器
//
// 用途: 看一份 .geo.json 到底长什么样, 尤其是**箱子 UV 有没有接对** —— 光看模型文件看不出来。
// 对拍基准: 工程里在用的 geo/apple_boss.geo.json + textures/entity/apple_boss.png 是"游戏里已知正确"的一对,
// 先渲染它确认管线没问题, 再渲染新模型。
//
// 用法: node render_bedrock_4view.cjs <geo.json> <tex.png> <out.png> [每单位像素=10] [uvScale=0=自动]
//
// 视图约定(正交投影, 相机永远看向模型):
//   前(front) = 相机在 -Z 看 +Z; 屏幕右 = -X; 深度 = +Z
//   右(right) = 相机在 +X 看 -X; 屏幕右 = -Z; 深度 = -X
//   后(back)  = 相机在 +Z 看 -Z; 屏幕右 = +X; 深度 = -Z
//   左(left)  = 相机在 -X 看 +X; 屏幕右 = +Z; 深度 = +X
//   竖直: 屏幕下 = -Y
//
// 箱子 UV(minecraft 的 box-UV, 与 Java ModelPart.Cube 同一套; 已用 64x64 玩家皮肤布局核对):
//   一排四个: west | south(有脸的那面) | east | north    各宽 d, w, d, w
//   上面一行: up 在 south 正上方, down 在 east 正上方          各高 d
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const argv = process.argv.slice(2);
const geoPath = argv[0], texPath = argv[1], out = argv[2];
const pxArg = parseFloat(argv[3]);
const uvScaleArg = parseFloat(argv[4]);
const px = isNaN(pxArg) ? 10 : pxArg;
if (!geoPath || !texPath || !out) {
  console.error('用法: node render_bedrock_4view.cjs <geo.json> <tex.png> <out.png> [每单位像素=10] [uvScale=0自动]');
  process.exit(2);
}

const geo = JSON.parse(fs.readFileSync(geoPath, 'utf8'));
const def = (geo['minecraft:geometry'] || [])[0];
if (!def) throw new Error('没有 minecraft:geometry[0]');
const desc = def.description || {};
const tex = decodePNG(texPath);
/* 几何声明的 UV 空间 vs 贴图实际像素: 128 空间 + 256 贴图 => 每个 UV 单位 = 2 像素 */
const uvSpaceW = desc.texture_width || 64;
const autoScale = tex.w / uvSpaceW;
/* uvScale < 0 = **纯几何模式**: 不采贴图, 每根骨骼一个颜色(用来核对"形状/位置对不对", 不受贴图歧义干扰) */
const FLAT = uvScaleArg < 0;
const PALETTE = [
  [232, 92, 92], [236, 155, 76], [226, 206, 92], [130, 200, 118], [104, 190, 210],
  [122, 146, 232], [186, 128, 226], [232, 130, 190], [170, 170, 178], [208, 176, 140]
];
let boneIdx = new Map();
const uvScale = isNaN(uvScaleArg) || uvScaleArg === 0 ? autoScale : uvScaleArg;

/* ---------- 1. 收集方块(含骨骼层级变换) ---------- */
const bones = new Map();
for (const b of (def.bones || [])) bones.set(b.name, b);

function rotMat(deg) {
  const [rx, ry, rz] = deg.map(d => d * Math.PI / 180);
  const cx = Math.cos(rx), sx = Math.sin(rx), cy = Math.cos(ry), sy = Math.sin(ry), cz = Math.cos(rz), sz = Math.sin(rz);
  // R = Rz * Ry * Rx (先绕 X, 再 Y, 再 Z —— Bedrock/GeckoLib 常用次序; 本模型只用到 Z)
  return [
    [cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx],
    [sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx],
    [-sy, cy * sx, cy * cx]
  ];
}
function mul(a, b) {                       // 3x3 * 3x3
  const r = [[0, 0, 0], [0, 0, 0], [0, 0, 0]];
  for (let i = 0; i < 3; i++) for (let j = 0; j < 3; j++) for (let k = 0; k < 3; k++) r[i][j] += a[i][k] * b[k][j];
  return r;
}
function apply(m, v) { return [m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2], m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2], m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]]; }

/** 骨骼链累计变换: 返回 {R, T} 使 p_world = R * (p_local - pivotChainAdjust) ... 用"先平移到 pivot, 旋转, 再平移回去"的链式叠加 */
function chainOf(boneName) {
  const st = [];
  let cur = bones.get(boneName);
  while (cur) { st.push(cur); cur = cur.parent ? bones.get(cur.parent) : null; }
  st.reverse();                            // root -> ... -> bone
  let R = [[1, 0, 0], [0, 1, 0], [0, 0, 1]];
  let before = [0, 0, 0], after = [0, 0, 0];   // world = R*(p + before) + after
  for (const b of st) {
    const piv = b.pivot || [0, 0, 0];
    const rot = b.rotation || [0, 0, 0];
    // 先做本骨骼: p -> Rb*(p - piv) + piv
    before = apply(R, before);
    after = apply(R, after);
    if (rot[0] || rot[1] || rot[2]) {
      const Rb = rotMat(rot);
      R = mul(R, Rb);
      before = apply(Rb, before);
      before = [before[0] - piv[0], before[1] - piv[1], before[2] - piv[2]];
      after = [after[0] + piv[0], after[1] + piv[1], after[2] + piv[2]];
    }
  }
  return { R, before, after };
}
function xform(c, p) { const q = apply(c.R, [p[0] + c.before[0], p[1] + c.before[1], p[2] + c.before[2]]); return [q[0] + c.after[0], q[1] + c.after[1], q[2] + c.after[2]]; }

/** 每个方块: 8 角 + 6 面的 (4 个模型空间角点, 各自 UV 角) */
const FACES = [];
let cubeCount = 0;
/* 骨骼配色**在构建期就定死**(按骨骼出现次序), 不然颜色会随视图的深度排序变化 */
for (const b of (def.bones || [])) if (b.cubes && b.cubes.length && !boneIdx.has(b.name)) boneIdx.set(b.name, boneIdx.size);
for (const b of (def.bones || [])) {
  const ch = chainOf(b.name);
  for (const cube of (b.cubes || [])) {
    cubeCount++;
    const [x0, y0, z0] = cube.origin;
    const [w, h, d] = cube.size;
    const x1 = x0 + w, y1 = y0 + h, z1 = z0 + d;
    const [u0, v0] = cube.uv || [0, 0];
    const inflate = cube.inflate || 0;
    // 箱子 UV 矩形(TL,BR), 先按 UV 空间算, 采样时再乘 uvScale
    const R = {
      west: [u0, v0 + d, u0 + d, v0 + d + h],
      south: [u0 + d, v0 + d, u0 + d + w, v0 + d + h],       // 有脸的那面
      east: [u0 + d + w, v0 + d, u0 + 2 * d + w, v0 + d + h],
      north: [u0 + 2 * d + w, v0 + d, u0 + 2 * d + 2 * w, v0 + d + h],
      up: [u0 + d, v0, u0 + d + w, v0 + d],
      down: [u0 + d + w, v0, u0 + 2 * d + w, v0 + d]
    };
    const mirror = !!cube.mirror;
    const P = (x, y, z) => xform(ch, [x, y, z]);
    /* 每面: [TL, TR, BR, BL](以"从外面看这个面"为准, 贴图 u 向右 / v 向下) */
    const defs = {
      // 从 +Z 外面看 south: 屏幕右 = +X ⇒ TL=(x0,y1,z1)
      south: [[x0, y1, z1], [x1, y1, z1], [x1, y0, z1], [x0, y0, z1]],
      // 从 -Z 外面看 north: 屏幕右 = -X ⇒ TL=(x1,y1,z0)
      north: [[x1, y1, z0], [x0, y1, z0], [x0, y0, z0], [x1, y0, z0]],
      // 从 +X 外面看 east: 屏幕右 = -Z ⇒ TL=(x1,y1,z1)
      east: [[x1, y1, z1], [x1, y1, z0], [x1, y0, z0], [x1, y0, z1]],
      // 从 -X 外面看 west: 屏幕右 = +Z ⇒ TL=(x0,y1,z0)
      west: [[x0, y1, z0], [x0, y1, z1], [x0, y0, z1], [x0, y0, z0]],
      // 俯视(屏幕右 = +X, 屏幕下 = +Z): TL=(x0,y1,z0)
      up: [[x0, y1, z0], [x1, y1, z0], [x1, y1, z1], [x0, y1, z1]],
      // 仰视
      down: [[x0, y0, z1], [x1, y0, z1], [x1, y0, z0], [x0, y0, z0]]
    };
    const NORMAL = { south: [0, 0, 1], north: [0, 0, -1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };
    for (const f of Object.keys(defs)) {
      let corners = defs[f].map(p => { const q = [p[0] + inflate * NORMAL[f][0], p[1] + inflate * NORMAL[f][1], p[2] + inflate * NORMAL[f][2]]; return P(q[0], q[1], q[2]); });
      let [ua, va, ub, vb] = R[f];
      if (mirror) { const t = ua; ua = ub; ub = t; }
      FACES.push({
        key: f, cube: cubeCount, bone: b.name, pts: corners,
        uv: [[ua, va], [ub, va], [ub, vb], [ua, vb]],       // TL,TR,BR,BL
        normal: apply(ch.R, NORMAL[f]),
        shaded: f === 'up' ? 1.0 : f === 'down' ? 0.55 : (f === 'east' || f === 'west') ? 0.80 : 0.90
      });
    }
  }
}

/* ---------- 2. 视图 ---------- */
const VIEWS = [
  { name: 'front', tag: 'F', f: [0, 0, 1], sxv: [-1, 0, 0] },
  { name: 'right', tag: 'R', f: [-1, 0, 0], sxv: [0, 0, -1] },
  { name: 'back', tag: 'B', f: [0, 0, -1], sxv: [1, 0, 0] },
  { name: 'left', tag: 'L', f: [1, 0, 0], sxv: [0, 0, 1] }
];

/* 5x7 点阵(只需 F/R/B/L/U/D), 用来在图角标出是哪个视图 —— 免得靠"数点数"认 */
const GLYPH = {
  F: ['11111', '10000', '10000', '11110', '10000', '10000', '10000'],
  R: ['11110', '10001', '10001', '11110', '10100', '10010', '10001'],
  B: ['11110', '10001', '10001', '11110', '10001', '10001', '11110'],
  L: ['10000', '10000', '10000', '10000', '10000', '10000', '11111'],
  U: ['10001', '10001', '10001', '10001', '10001', '10001', '01110'],
  D: ['11110', '10001', '10001', '10001', '10001', '10001', '11110']
};
function drawTag(cv, x, y, tag, k = 3) {
  const g = GLYPH[tag];
  if (!g) return;
  for (let r = 0; r < g.length; r++) for (let c = 0; c < g[r].length; c++) {
    if (g[r][c] === '1') cv.rect(x + c * k, y + r * k, k, k, 250, 210, 80, 1);
  }
}

/* 世界包围盒(决定缩放与居中) */
let bb = [[1e9, 1e9, 1e9], [-1e9, -1e9, -1e9]];
for (const fc of FACES) for (const p of fc.pts) for (let i = 0; i < 3; i++) { bb[0][i] = Math.min(bb[0][i], p[i]); bb[1][i] = Math.max(bb[1][i], p[i]); }
const spanX = bb[1][0] - bb[0][0], spanY = bb[1][1] - bb[0][1], spanZ = bb[1][2] - bb[0][2];
const W = Math.max(spanX, spanZ) + 6, H = spanY + 6;
const panel = { w: Math.round(W * px), h: Math.round(H * px) };
const PAD = 8;
const cv = new Canvas(panel.w * 2 + PAD * 3, panel.h * 2 + PAD * 3);
cv.rect(0, 0, cv.w, cv.h, 22, 23, 27, 1);
const cyModel = (bb[0][1] + bb[1][1]) / 2;

function sample(u, v) {
  const x = Math.max(0, Math.min(tex.w - 1, Math.floor(u * uvScale)));
  const y = Math.max(0, Math.min(tex.h - 1, Math.floor(v * uvScale)));
  const i = (y * tex.w + x) * 4;
  return [tex.data[i], tex.data[i + 1], tex.data[i + 2], tex.data[i + 3]];
}
/** 逐面: 投影 -> 背面剔除 -> 按深度远到近 -> 双三角仿射贴图 */
VIEWS.forEach((view, vi) => {
  const ox = PAD + (vi % 2) * (panel.w + PAD), oy = PAD + Math.floor(vi / 2) * (panel.h + PAD);
  const cx = ox + panel.w / 2;
  const proj = p => [
    cx + (view.sxv[0] * p[0] + view.sxv[1] * p[1] + view.sxv[2] * p[2]) * px,
    oy + panel.h / 2 - (p[1] - cyModel) * px
  ];
  const depth = p => -(view.f[0] * p[0] + view.f[1] * p[1] + view.f[2] * p[2]);
  const list = FACES
    .filter(fc => (fc.normal[0] * view.f[0] + fc.normal[1] * view.f[1] + fc.normal[2] * view.f[2]) < -1e-6)   // 只画朝向相机的面
    .map(fc => ({ fc, d: fc.pts.reduce((s, p) => s + depth(p), 0) / 4 }))
    .sort((a, b) => a.d - b.d);

  for (const { fc } of list) {
    const sp = fc.pts.map(proj);
    for (const tri of [[0, 1, 2], [0, 2, 3]]) {
      const i0 = tri[0], i1 = tri[1], i2 = tri[2];
      const p0 = sp[i0], p1 = sp[i1], p2 = sp[i2];
      const u0 = fc.uv[i0], u1 = fc.uv[i1], u2 = fc.uv[i2];
      const minX = Math.max(ox, Math.floor(Math.min(p0[0], p1[0], p2[0])));
      const maxX = Math.min(ox + panel.w - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0])));
      const minY = Math.max(oy, Math.floor(Math.min(p0[1], p1[1], p2[1])));
      const maxY = Math.min(oy + panel.h - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1])));
      const det = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1]);
      if (Math.abs(det) < 1e-9) continue;
      for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
        const px0 = x + 0.5, py0 = y + 0.5;
        const a0 = ((p1[1] - p2[1]) * (px0 - p2[0]) + (p2[0] - p1[0]) * (py0 - p2[1])) / det;
        const a1 = ((p2[1] - p0[1]) * (px0 - p2[0]) + (p0[0] - p2[0]) * (py0 - p2[1])) / det;
        const a2 = 1 - a0 - a1;
        if (a0 < -1e-6 || a1 < -1e-6 || a2 < -1e-6) continue;
        const u = u0[0] * a0 + u1[0] * a1 + u2[0] * a2;
        const v = u0[1] * a0 + u1[1] * a1 + u2[1] * a2;
        let c;
        if (FLAT) {
          const col = PALETTE[boneIdx.get(fc.bone) % PALETTE.length];
          c = [col[0], col[1], col[2], 255];
        } else {
          c = sample(u, v);
        }
        if (c[3] < 8) continue;                    // 透明像素露出后面的面
        const k = fc.shaded;
        cv.set(x, y, c[0] * k, c[1] * k, c[2] * k, c[3] / 255);
      }
    }
  }
  /* 地面线 + 视图角标(5x7 点阵大写字母) */
  cv.hline(ox, ox + panel.w - 1, Math.round(oy + panel.h / 2 + (cyModel - bb[0][1]) * px), 70, 90, 70, 1);
  drawTag(cv, ox + 4, oy + 4, view.tag);
});
cv.save(out);
console.log(`已写出 ${out}  (正/右/背/左 四视图; 方块 ${cubeCount} 个, 面 ${FACES.length} 个; ` +
  `贴图 ${path.basename(texPath)} ${tex.w}x${tex.h}; UV 空间 ${uvSpaceW} -> uvScale ${uvScale}; 每单位 ${px}px)`);
