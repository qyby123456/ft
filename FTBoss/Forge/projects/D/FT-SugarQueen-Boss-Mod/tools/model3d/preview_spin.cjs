/* 物品模型"预览动画"生成器 —— 参考 [食物动画]farmingtales_drink_anim 的检视思路,
   把物品模型按 display 变换后做正交投影, 逐帧绕 Y 轴旋转, 输出一张横向序列图(可直接看动画)。

   用法: node preview_spin.cjs <modelJson> <texturePng> <slot> <outPng> [frames] [totalDeg] [tint]
   例:   node preview_spin.cjs ../../src/.../turkey_legs.json ../../src/.../turkey_legs.png \
             firstperson_righthand %TEMP%/tk_spin.png 12 360 tint                             */
const fs = require('fs');
const { Canvas, decodePNG } = require('./png.cjs');

const [modelPath, texPath, slot, outPath, framesArg, degArg, modeArg] = process.argv.slice(2);
const FRAMES = Math.max(1, parseInt(framesArg || '12', 10));
const TOTAL = parseFloat(degArg || '360');
const TINTED = (modeArg || '') === 'tint';

const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const tex = decodePNG(texPath);
const disp0 = (model.display && model.display[slot]) || { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] };
const tra = disp0.translation || [0, 0, 0];
const sca = disp0.scale || [1, 1, 1];

const rad = d => d * Math.PI / 180;
const mat = (axis, deg) => {
  const c = Math.cos(rad(deg)), s = Math.sin(rad(deg));
  if (axis === 'x') return [1, 0, 0, 0, c, -s, 0, s, c];
  if (axis === 'y') return [c, 0, s, 0, 1, 0, -s, 0, c];
  return [c, -s, 0, s, c, 0, 0, 0, 1];
};
const mul = (A, B) => { const C = new Array(9).fill(0); for (let i = 0; i < 3; i++) for (let k = 0; k < 3; k++) for (let q = 0; q < 3; q++) C[i * 3 + q] += A[i * 3 + k] * B[k * 3 + q]; return C; };
const ap = (M, v) => [M[0] * v[0] + M[1] * v[1] + M[2] * v[2], M[3] * v[0] + M[4] * v[1] + M[5] * v[2], M[6] * v[0] + M[7] * v[1] + M[8] * v[2]];

const FACE = {
  north: (f, t) => [[f[0], t[1], f[2]], [t[0], t[1], f[2]], [t[0], f[1], f[2]], [f[0], f[1], f[2]]],
  south: (f, t) => [[f[0], f[1], t[2]], [t[0], f[1], t[2]], [t[0], t[1], t[2]], [f[0], t[1], t[2]]],
  west: (f, t) => [[f[0], f[1], f[2]], [f[0], f[1], t[2]], [f[0], t[1], t[2]], [f[0], t[1], f[2]]],
  east: (f, t) => [[t[0], f[1], f[2]], [t[0], f[1], t[2]], [t[0], t[1], t[2]], [t[0], t[1], f[2]]],
  up: (f, t) => [[f[0], t[1], f[2]], [f[0], t[1], t[2]], [t[0], t[1], t[2]], [t[0], t[1], f[2]]],
  down: (f, t) => [[f[0], f[1], f[2]], [f[0], f[1], t[2]], [t[0], f[1], t[2]], [t[0], f[1], f[2]]],
};
const FN = { north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };
const TINT = [[220, 60, 60], [240, 200, 60], [70, 200, 90], [70, 180, 240], [220, 90, 220], [255, 140, 60], [150, 110, 240], [80, 220, 200], [120, 120, 120]];

function avgColor(uv) {
  if (!uv) return [200, 0, 200];
  const sx = tex.w / 16, sy = tex.h / 16;
  const x0 = Math.max(0, Math.round(Math.min(uv[0], uv[2]) * sx)), x1 = Math.min(tex.w, Math.round(Math.max(uv[0], uv[2]) * sx));
  const y0 = Math.max(0, Math.round(Math.min(uv[1], uv[3]) * sy)), y1 = Math.min(tex.h, Math.round(Math.max(uv[1], uv[3]) * sy));
  let r = 0, g = 0, b = 0, a = 0, n = 0;
  for (let y = y0; y < y1; y++) for (let x = x0; x < x1; x++) { const i = (y * tex.w + x) * 4; r += tex.data[i]; g += tex.data[i + 1]; b += tex.data[i + 2]; a += tex.data[i + 3]; n++; }
  if (!n) return [200, 0, 200];
  const al = a / n / 255;
  return [r / n * al + 235 * (1 - al), g / n * al + 235 * (1 - al), b / n * al + 235 * (1 - al)];
}

const FW = 220, FH = 300, S = 190;
const cv = new Canvas(FW * FRAMES, FH);
for (let y = 0; y < FH; y++) for (let x = 0; x < FW * FRAMES; x++) cv.set(x, y, 235, 235, 240, 1);

for (let f = 0; f < FRAMES; f++) {
  const yaw = TOTAL * f / FRAMES;
  const rot = [disp0.rotation?.[0] || 0, (disp0.rotation?.[1] || 0) + yaw, disp0.rotation?.[2] || 0];
  const RM = mul(mul(mat('x', rot[0]), mat('y', rot[1])), mat('z', rot[2]));
  const polys = [];
  for (const e of (model.elements || [])) {
    for (const key of Object.keys(e.faces || {})) {
      const pts = FACE[key](e.from, e.to).map(p => {
        let v = [p[0] / 16 - 0.5, p[1] / 16 - 0.5, p[2] / 16 - 0.5];
        if (e.rotation) {
          const o = [e.rotation.origin[0] / 16 - 0.5, e.rotation.origin[1] / 16 - 0.5, e.rotation.origin[2] / 16 - 0.5];
          const M2 = mat(e.rotation.axis, e.rotation.angle);
          const r2 = ap(M2, [v[0] - o[0], v[1] - o[1], v[2] - o[2]]);
          v = [r2[0] + o[0], r2[1] + o[1], r2[2] + o[2]];
        }
        const q = ap(RM, v);
        return [q[0] * sca[0] + tra[0] / 16, q[1] * sca[1] + tra[1] / 16, q[2] * sca[2] + tra[2] / 16];
      });
      const nrm = ap(RM, FN[key]);
      const col = TINTED ? TINT[((model.elements || []).indexOf(e)) % TINT.length] : avgColor(e.faces[key].uv);
      polys.push({ pts, nrm, col, z: pts.reduce((s, p) => s + p[2], 0) / 4 });
    }
  }
  polys.sort((p, q) => p.z - q.z);
  for (const p of polys) {
    const xoff = f * FW + FW / 2, yoff = FH / 2;
    const pts = p.pts.map(pt => [xoff + pt[0] * S, yoff - pt[1] * S]);
    const xs = pts.map(q => q[0]), ys = pts.map(q => q[1]);
    const minx = Math.max(f * FW, Math.floor(Math.min(...xs))), maxx = Math.min((f + 1) * FW - 1, Math.ceil(Math.max(...xs)));
    const miny = Math.max(0, Math.floor(Math.min(...ys))), maxy = Math.min(FH - 1, Math.ceil(Math.max(...ys)));
    const shade = 0.7 + 0.3 * Math.max(0, p.nrm[2]);
    for (let y = miny; y <= maxy; y++) for (let x = minx; x <= maxx; x++) {
      let inside = false;
      for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
        const xi = pts[i][0], yi = pts[i][1], xj = pts[j][0], yj = pts[j][1];
        if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) inside = !inside;
      }
      if (inside) cv.set(x, y, p.col[0] * shade, p.col[1] * shade, p.col[2] * shade, 1);
    }
  }
  for (let y = 0; y < FH; y++) cv.set(f * FW, y, 200, 200, 205, 1);
}
cv.save(outPath);
console.log('[' + slot + '] ' + FRAMES + ' 帧 / ' + TOTAL + '°  -> ' + outPath + '  (' + cv.w + 'x' + cv.h + ')');
