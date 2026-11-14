/* 物品模型预览器: 应用 display 变换后做正交投影, 画出玩家实际看到的样子
   用法: node preview_item2.cjs <modelJson> <texturePng> <slot> <outPng>                     */
const fs = require('fs');
const { Canvas, decodePNG } = require('./png.cjs');

const [modelPath, texPath, slot, outPath] = process.argv.slice(2);
const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const tex = decodePNG(texPath);
const disp = (model.display && model.display[slot]) || { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] };
const rot = disp.rotation || [0, 0, 0];
const tra = disp.translation || [0, 0, 0];
const sca = disp.scale || [1, 1, 1];

const rad = d => d * Math.PI / 180;
function mat(axis, deg) {
  const c = Math.cos(rad(deg)), s = Math.sin(rad(deg));
  if (axis === 'x') return [1, 0, 0, 0, c, -s, 0, s, c];
  if (axis === 'y') return [c, 0, s, 0, 1, 0, -s, 0, c];
  return [c, -s, 0, s, c, 0, 0, 0, 1];
}
const mul = (A, B) => { const C = new Array(9).fill(0); for (let i = 0; i < 3; i++) for (let k = 0; k < 3; k++) for (let q = 0; q < 3; q++) C[i * 3 + q] += A[i * 3 + k] * B[k * 3 + q]; return C; };
const ap = (M, v) => [M[0] * v[0] + M[1] * v[1] + M[2] * v[2], M[3] * v[0] + M[4] * v[1] + M[5] * v[2], M[6] * v[0] + M[7] * v[1] + M[8] * v[2]];

/* 把模型坐标(0..16) -> 变换后坐标(单位: 格) */
function xform(p) {
  let v = [p[0] / 16 - 0.5, p[1] / 16 - 0.5, p[2] / 16 - 0.5];
  if (p.rot) {                                   // 元素自身旋转(绕 origin)
    const o = [p.rot.origin[0] / 16 - 0.5, p.rot.origin[1] / 16 - 0.5, p.rot.origin[2] / 16 - 0.5];
    const M = mat(p.rot.axis, p.rot.angle);
    const r = ap(M, [v[0] - o[0], v[1] - o[1], v[2] - o[2]]);
    v = [r[0] + o[0], r[1] + o[1], r[2] + o[2]];
  }
  const M = mul(mul(mat('x', rot[0]), mat('y', rot[1])), mat('z', rot[2]));
  const q = ap(M, v);
  return [q[0] * sca[0] + tra[0] / 16, q[1] * sca[1] + tra[1] / 16, q[2] * sca[2] + tra[2] / 16];
}

/* 每个面的 4 个角(模型坐标) */
const FACE = {
  north: (f, t) => [[f[0], t[1], f[2]], [t[0], t[1], f[2]], [t[0], f[1], f[2]], [f[0], f[1], f[2]]],
  south: (f, t) => [[f[0], f[1], t[2]], [t[0], f[1], t[2]], [t[0], t[1], t[2]], [f[0], t[1], t[2]]],
  west: (f, t) => [[f[0], f[1], f[2]], [f[0], f[1], t[2]], [f[0], t[1], t[2]], [f[0], t[1], f[2]]],
  east: (f, t) => [[t[0], f[1], f[2]], [t[0], f[1], t[2]], [t[0], t[1], t[2]], [t[0], t[1], f[2]]],
  up: (f, t) => [[f[0], t[1], f[2]], [f[0], t[1], t[2]], [t[0], t[1], t[2]], [t[0], t[1], f[2]]],
  down: (f, t) => [[f[0], f[1], f[2]], [f[0], f[1], t[2]], [t[0], f[1], t[2]], [t[0], f[1], f[2]]],
};
const FACE_N = { north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };

/* 采样某个面 UV 的平均色 */
function avgColor(uv) {
  if (!uv) return [200, 0, 200, 255];
  const sx = tex.w / 16, sy = tex.h / 16;
  const x0 = Math.max(0, Math.round(Math.min(uv[0], uv[2]) * sx)), x1 = Math.min(tex.w, Math.round(Math.max(uv[0], uv[2]) * sx));
  const y0 = Math.max(0, Math.round(Math.min(uv[1], uv[3]) * sy)), y1 = Math.min(tex.h, Math.round(Math.max(uv[1], uv[3]) * sy));
  let r = 0, g = 0, b = 0, a = 0, n = 0;
  for (let y = y0; y < y1; y++) for (let x = x0; x < x1; x++) { const i = (y * tex.w + x) * 4; r += tex.data[i]; g += tex.data[i + 1]; b += tex.data[i + 2]; a += tex.data[i + 3]; n++; }
  if (!n) return [200, 0, 200, 255];
  return [r / n, g / n, b / n, a / n];
}

/* 收集所有面的多边形 + 深度 */
const polys = [];
for (const e of (model.elements || [])) {
  for (const key of Object.keys(e.faces || {})) {
    const face = e.faces[key];
    const pts = FACE[key](e.from, e.to).map(p => xform(e.rotation ? { ...{}, 0: p[0], 1: p[1], 2: p[2], rot: e.rotation } : p));
    const nrm = ap(mul(mul(mat('x', rot[0]), mat('y', rot[1])), mat('z', rot[2])), FACE_N[key]);
    const c = avgColor(face.uv);
    const zavg = pts.reduce((s, p) => s + p[2], 0) / 4;
    polys.push({ pts, nrm, c, zavg, key, cubeIdx: (model.elements || []).indexOf(e) + 1 });
  }
}
/* 相机: 沿 -Z 看(+Z 朝观察者), 屏幕 x=世界x, y=世界y */
const W = 480, H = 480, S = 320;      // 像素/格
const cv = new Canvas(W, H);
/* 背景网格 */
for (let y = 0; y < H; y += 1) for (let x = 0; x < W; x += 1) cv.set(x, y, 235, 235, 240, 1);

polys.sort((a, b) => a.zavg - b.zavg);        // 画家算法: 远的先画
function fill(poly) {
  const pts = poly.pts.map(p => [W / 2 + p[0] * S, H / 2 - p[1] * S]);
  const xs = pts.map(p => p[0]), ys = pts.map(p => p[1]);
  const minx = Math.max(0, Math.floor(Math.min(...xs))), maxx = Math.min(W - 1, Math.ceil(Math.max(...xs)));
  const miny = Math.max(0, Math.floor(Math.min(...ys))), maxy = Math.min(H - 1, Math.ceil(Math.max(...ys)));
  const TINT=[[220,60,60],[240,200,60],[70,200,90],[70,180,240],[220,90,220],[255,140,60],[150,110,240],[80,220,200],[230,230,230]];
  const tint=TINT[(poly.cubeIdx-1)%TINT.length];
  const [r, g, b, a] = tint;
  const shade = 0.72 + 0.28 * Math.max(0, poly.nrm[2]);   // 面向观察者更亮
  for (let y = miny; y <= maxy; y++) for (let x = minx; x <= maxx; x++) {
    let inside = false;
    for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
      const xi = pts[i][0], yi = pts[i][1], xj = pts[j][0], yj = pts[j][1];
      if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) inside = !inside;
    }
    if (inside) cv.set(x, y, r * shade, g * shade, b * shade, 1);
  }
}
for (const p of polys) fill(p);
cv.save(outPath);
console.log('[' + slot + '] 画了 ' + polys.length + ' 个面 -> ' + outPath);
console.log('   刃面/薄面法线朝观察者(z>0)的面数: ' + polys.filter(p => p.nrm[2] > 0.5).length +
            '   最靠观察者的面: cube' + polys[polys.length - 1].cubeIdx + '.' + polys[polys.length - 1].key +
            ' z=' + polys[polys.length - 1].zavg.toFixed(3));
