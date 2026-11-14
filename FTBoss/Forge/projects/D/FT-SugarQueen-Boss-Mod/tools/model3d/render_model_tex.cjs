// Java 方块模型「四视图 + 真实贴图采样」预览器(2026-09-10)
//
// 用途: 石像这类用 elements(from/to) 手写的方块模型, 光看贴图看不出"放到世界里长什么样";
// 更重要的是 —— **逐面 UV 有没有接对**(正/背/左/右四个面各自采样哪一块贴图)只能靠出图核对。
//
// 用法: node render_model_tex.cjs <lower.json> [upper.json] <tex.png> <out.png> [每单位像素]
//   给两个模型时, 第二个(upper)会抬高 16 单位, 拼成完整两格石像。
//
// 视图约定(与 Minecraft **Java 方块模型**一致, 见 FaceInfo + BlockFaceUV):
//   Java 的每个面, 顶点 0..3 依次拿贴图角 TL, BL, BR, TR; 各面顶点位置:
//     NORTH v0(+X,+Y,-Z) v1(+X,-Y,-Z) v2(-X,-Y,-Z) v3(-X,+Y,-Z)   ← 所以"屏幕右 = -x"
//     SOUTH v0(-X,+Y,+Z) v1(-X,-Y,+Z) v2(+X,-Y,+Z) v3(+X,+Y,+Z)   ← "屏幕右 = +x"
//     WEST  v0(-X,+Y,-Z) ...  ⇒ 屏幕右 = +z ;  EAST v0(+X,+Y,+Z) ... ⇒ 屏幕右 = -z
//   纵向一律: 屏幕下 = -y; 贴图 v 向下 = 方块 y 向下。
// ★ 别把它"修"成 +x 投影: 那会让预览左右镜像, 从而误判 Bedrock→Java 的 UV 转换写错了。
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const args = process.argv.slice(2);
const pxArg = args.pop();
const out = args.pop();
const texPath = args.pop();
const px = parseFloat(pxArg || '10');
const models = args.map(f => JSON.parse(fs.readFileSync(f, 'utf8')));
const tex = decodePNG(texPath);

// sx(p) = 该视图下"屏幕水平方向相对模型中心(x=8,z=8)的偏移"; 竖直一律 屏幕下 = -y。
// ★ 屏幕右方向必须按"从外面看这个面"来定(Java 的 BlockFaceUV 语义: 贴图 q1 列落在该面屏幕左侧):
//   front(相机 -Z, 视线 +Z): 右 = -x   → 用 8-x 才能让贴图不被左右镜像
//   back (相机 +Z, 视线 -Z): 右 = +x
//   right(相机 +X, 视线 -X): 右 = -z
//   left (相机 -X, 视线 +X): 右 = +z
const VIEWS = [
  { name: 'front', face: 'north', sx: p => 8 - p[0], depth: p => p[2] },
  { name: 'right', face: 'east', sx: p => 8 - p[2], depth: p => p[0] },
  { name: 'back', face: 'south', sx: p => p[0] - 8, depth: p => -p[2] },
  { name: 'left', face: 'west', sx: p => p[2] - 8, depth: p => -p[0] }
];

const quads = [];
for (let mi = 0; mi < models.length; mi++) {
  const yOff = mi === 1 ? 16 : 0;
  for (const el of (models[mi].elements || [])) {
    const [x0, y0, z0] = el.from, [x1, y1, z1] = el.to;
    const P = (x, y, z) => [x, y + yOff, z];
    // 每个面的 4 个角(顺序不重要, 后面按屏幕包围盒插值采样)
    quads.push({ face: 'north', pts: [P(x0, y0, z0), P(x1, y0, z0), P(x1, y1, z0), P(x0, y1, z0)], uv: el.faces.north && el.faces.north.uv });
    quads.push({ face: 'south', pts: [P(x1, y0, z1), P(x0, y0, z1), P(x0, y1, z1), P(x1, y1, z1)], uv: el.faces.south && el.faces.south.uv });
    quads.push({ face: 'east', pts: [P(x1, y0, z0), P(x1, y0, z1), P(x1, y1, z1), P(x1, y1, z0)], uv: el.faces.east && el.faces.east.uv });
    quads.push({ face: 'west', pts: [P(x0, y0, z1), P(x0, y0, z0), P(x0, y1, z0), P(x0, y1, z1)], uv: el.faces.west && el.faces.west.uv });
  }
}

const W = 20, H = 36;                       // 视野(模型单位): 略大于 1x2 格
const panel = { w: Math.round(W * px), h: Math.round(H * px) };
const PAD = 6;
const cv = new Canvas(panel.w * 2 + PAD * 3, panel.h * 2 + PAD * 3);
cv.rect(0, 0, cv.w, cv.h, 24, 24, 28, 1);

function sample(u, v) {
  let x = Math.floor((u / 16) * tex.w), y = Math.floor((v / 16) * tex.h);
  x = Math.max(0, Math.min(tex.w - 1, x)); y = Math.max(0, Math.min(tex.h - 1, y));
  const i = (y * tex.w + x) * 4;
  return [tex.data[i], tex.data[i + 1], tex.data[i + 2], tex.data[i + 3]];
}
function inPoly(pts, x, y) {
  let c = false;
  for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
    const [xi, yi] = pts[i], [xj, yj] = pts[j];
    if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) c = !c;
  }
  return c;
}

VIEWS.forEach((view, vi) => {
  const ox = PAD + (vi % 2) * (panel.w + PAD), oy = PAD + Math.floor(vi / 2) * (panel.h + PAD);
  // 该视图下所有相关面: 按深度**远→近**排序后画
  const list = quads.filter(q => q.face === view.face && q.uv)
    .map(q => ({ q, d: q.pts.reduce((s, p) => Math.max(s, view.depth(p)), -1e9) }))
    .sort((a, b) => b.d - a.d);
  for (const { q } of list) {
    const sp = q.pts.map(p => [ox + panel.w / 2 + view.sx(p) * px, oy + (H - 4 - p[1]) * px]);
    const [u1, v1, u2, v2] = q.uv;
    const sx0 = Math.min(...sp.map(p => p[0])), sx1 = Math.max(...sp.map(p => p[0]));
    const sy0 = Math.min(...sp.map(p => p[1])), sy1 = Math.max(...sp.map(p => p[1]));
    const minX = Math.max(ox, Math.floor(sx0)), maxX = Math.min(ox + panel.w - 1, Math.ceil(sx1));
    const minY = Math.max(oy, Math.floor(sy0)), maxY = Math.min(oy + panel.h - 1, Math.ceil(sy1));
    for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
      if (!inPoly(sp, x + 0.5, y + 0.5)) continue;
      const a = (x + 0.5 - sx0) / Math.max(1e-6, sx1 - sx0);
      const b = (y + 0.5 - sy0) / Math.max(1e-6, sy1 - sy0);
      const c = sample(u1 + (u2 - u1) * a, v1 + (v2 - v1) * b);
      if (c[3] === 0) continue;
      const k = 0.86 + (1 - b) * 0.24;         // 顶光
      cv.set(x, y, Math.min(255, c[0] * k), Math.min(255, c[1] * k), Math.min(255, c[2] * k), 1);
    }
  }
  // 地面线
  cv.hline(ox, ox + panel.w - 1, Math.round(oy + (H - 4) * px), 70, 90, 70, 1);
});
cv.save(out);
console.log(`已写出 ${out}  (四视图 正/右/背/左, ${models.length} 个模型, 贴图 ${path.basename(texPath)} ${tex.w}x${tex.h})`);
