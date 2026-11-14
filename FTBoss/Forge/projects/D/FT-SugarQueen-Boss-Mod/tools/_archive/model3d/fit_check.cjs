// 屠夫模型「动画姿势穿模」检查: 立方体级 AABB 相交(静态姿势查不出走路/挥砍时的穿模)
// 用法: node fit_check.cjs [姿势名]     不给姿势名则跑全部姿势
//
// 原理: 按 ButcherBossModel.setupAnim 的公式把每条骨骼的旋转/位移写进矩阵链
//       (Java 顺序: translate(offset) -> Rz -> Ry -> Rx, 与 ModelPart.translateAndRotate 一致),
//       把每个 cube 的 8 个角变换到模型空间, 取 AABB; 然后逐 cube 对求交体积。
//       Java 空间: y 向下, 脚底 y=24。规避父-子骨骼对(关节处本来就贴合)。
const { SPEC, POSES } = require('./butcher_spec.cjs');

/* ---- 4x4 矩阵(行主) ---- */
const I = () => [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]];
const mul = (a, b) => {
  const o = I();
  for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) {
    let s = 0; for (let k = 0; k < 4; k++) s += a[r][k] * b[k][c];
    o[r][c] = s;
  }
  return o;
};
const T = (x, y, z) => [[1, 0, 0, x], [0, 1, 0, y], [0, 0, 1, z], [0, 0, 0, 1]];
const Rx = t => { const c = Math.cos(t), s = Math.sin(t); return [[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]]; };
const Ry = t => { const c = Math.cos(t), s = Math.sin(t); return [[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]]; };
const Rz = t => { const c = Math.cos(t), s = Math.sin(t); return [[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]; };
const xf = (m, p) => [
  m[0][0] * p[0] + m[0][1] * p[1] + m[0][2] * p[2] + m[0][3],
  m[1][0] * p[0] + m[1][1] * p[1] + m[1][2] * p[2] + m[1][3],
  m[2][0] * p[0] + m[2][1] * p[1] + m[2][2] * p[2] + m[2][3]
];

const byName = {};
for (const b of SPEC) byName[b.name] = b;
const parentOf = n => byName[n] && byName[n].parent;
function boneMatrix(name, pose) {
  const b = byName[name];
  const rot = (pose && pose[name]) || [0, 0, 0];
  const dy = (pose && pose[name + 'Dy']) || 0;
  const local = mul(T(b.pivot[0], b.pivot[1] + dy, b.pivot[2]),
    mul(Rz(rot[2] || 0), mul(Ry(rot[1] || 0), Rx(rot[0] || 0))));
  return b.parent ? mul(boneMatrix(b.parent, pose), local) : local;
}
/** 某姿势下所有 cube 的模型空间 AABB */
function cubesOf(pose) {
  const out = [];
  for (const b of SPEC) {
    const m = boneMatrix(b.name, pose);
    for (const cb of b.boxes) {
      const lo = [1e9, 1e9, 1e9], hi = [-1e9, -1e9, -1e9];
      for (const i of [0, 1]) for (const j of [0, 1]) for (const k of [0, 1]) {
        const p = xf(m, [cb.x + i * cb.w, cb.y + j * cb.h, cb.z + k * cb.d]);
        for (let a = 0; a < 3; a++) { lo[a] = Math.min(lo[a], p[a]); hi[a] = Math.max(hi[a], p[a]); }
      }
      out.push({ bone: b.name, mat: cb.mat, lo, hi, rest: [cb.x, cb.y, cb.z, cb.w, cb.h, cb.d] });
    }
  }
  return out;
}
const vol = (A, B) => {
  const d = [0, 1, 2].map(a => Math.min(A.hi[a], B.hi[a]) - Math.max(A.lo[a], B.lo[a]));
  if (d.some(v => v <= 0)) return 0;
  return d[0] * d[1] * d[2];
};
const depth = (A, B) => {
  const d = [0, 1, 2].map(a => Math.min(A.hi[a], B.hi[a]) - Math.max(A.lo[a], B.lo[a]));
  if (d.some(v => v <= 0)) return 0;
  return Math.min(...d);   // 最小穿透深度(单位)
};
const bonesAdjacent = (a, b) => parentOf(a) === b || parentOf(b) === a;

function report(poseName, pose, threshold) {
  const cs = cubesOf(pose);
  const pairs = [];
  for (let i = 0; i < cs.length; i++) for (let j = i + 1; j < cs.length; j++) {
    const A = cs[i], B = cs[j];
    if (A.bone === B.bone || bonesAdjacent(A.bone, B.bone)) continue;
    const v = vol(A, B);
    if (v >= threshold) pairs.push({ a: A.bone, b: B.bone, m1: A.mat, m2: B.mat, v, d: depth(A, B) });
  }
  pairs.sort((x, y) => y.v - x.v);
  console.log(`\n===== 姿势 ${poseName} =====`);
  // 逐骨骼包围盒(便于看整体是否合理)
  const per = {};
  for (const c of cs) {
    const p = per[c.bone] || (per[c.bone] = [1e9, 1e9, 1e9, -1e9, -1e9, -1e9]);
    for (let a = 0; a < 3; a++) { p[a] = Math.min(p[a], c.lo[a]); p[a + 3] = Math.max(p[a + 3], c.hi[a]); }
  }
  for (const [n, p] of Object.entries(per)) {
    console.log(`  ${n.padEnd(8)} X ${p[0].toFixed(1)}..${p[3].toFixed(1)}  Y ${p[1].toFixed(1)}..${p[4].toFixed(1)}  Z ${p[2].toFixed(1)}..${p[5].toFixed(1)}`);
  }
  if (!pairs.length) { console.log('  ✓ 无跨骨骼立方体相交'); return 0; }
  console.log(`  ⚠ 跨骨骼相交 ${pairs.length} 对(体积≥${threshold}):`);
  for (const p of pairs.slice(0, 14)) {
    console.log(`    ${p.a}.${p.m1} <-> ${p.b}.${p.m2}   体积 ${p.v.toFixed(1)}  最小穿透 ${p.d.toFixed(2)} 单位 (${(p.d / 16).toFixed(3)} 格)`);
  }
  return pairs.length;
}

const REST = {};
const only = process.argv[2];
const thr = 1.0;
let total = 0;
if (!only || only === 'rest') total += report('rest(静止)', REST, thr);
for (const [name, pose] of Object.entries(POSES)) {
  if (only && only !== name) continue;
  total += report(name, pose, thr);
}
console.log(`\n合计可疑相交 ${total} 处(阈值 ${thr} 单位³; 只报跨骨骼、非父子对)`);

/* ================= 导出"姿势 geo"给 preview/render_tex 渲染验证 =================
   Java 弧度 -> Bedrock 角度: x/z 取负, y 不变(镜像 y 轴导致); pivot 用累加后的绝对值。 */
const fs = require('fs');
const path = require('path');
const J2B = v => 24 - v;
const absPiv = { root: [0, 0, 0] };
for (const b of SPEC) if (b.parent) absPiv[b.name] = absPiv[b.parent].map((v, i) => v + b.pivot[i]);
function poseGeo(poseName, pose, outDir, texW, texH, uvs) {
  const bones = SPEC.filter(s => s.boxes.length || s.name === 'root').map(s => {
    const ap = absPiv[s.name];
    const r = (pose && pose[s.name]) || [0, 0, 0];
    const dy = (pose && pose[s.name + 'Dy']) || 0;
    const o = { name: s.name, pivot: [ap[0], J2B(ap[1] + dy), ap[2]] };
    if (s.parent) o.parent = s.parent;
    const rot = [-(r[0] || 0) * 180 / Math.PI, (r[1] || 0) * 180 / Math.PI, -(r[2] || 0) * 180 / Math.PI];
    if (rot.some(v => Math.abs(v) > 1e-6)) o.rotation = rot;
    o.cubes = s.boxes.map(bx => ({
      origin: [ap[0] + bx.x, J2B(ap[1] + bx.y + bx.h), ap[2] + bx.z],
      size: [bx.w, bx.h, bx.d],
      uv: uvs[bx.mat] || [1, 1]
    }));
    return o;
  });
  const geo = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
      description: { identifier: 'geometry.butcher_' + poseName, texture_width: texW, texture_height: texH, visible_bounds_width: 4, visible_bounds_height: 4, visible_bounds_offset: [0, 1.5, 0] },
      bones
    }]
  };
  fs.writeFileSync(path.join(outDir, `butcher_pose_${poseName}.geo.json`), JSON.stringify(geo, null, 1));
}
const outDir = process.argv[3];
if (outDir) {
  fs.mkdirSync(outDir, { recursive: true });
  // 材质 UV 表: 与 gen_butcher 的打包一致(从贴图实际布局读出) —— 这里用同样规则重算一遍
  const mats = {};
  for (const b of SPEC) for (const bx of b.boxes) {
    const need = [Math.ceil(2 * (bx.d + bx.w)) + 2, Math.ceil(bx.d + bx.h) + 2];
    const m = mats[bx.mat] || (mats[bx.mat] = { w: 0, h: 0 });
    m.w = Math.max(m.w, need[0]); m.h = Math.max(m.h, need[1]);
  }
  let TEX = 0, px, py;
  for (const W of [128, 256, 512]) {
    px = 1; py = 1; let shelf = 0, ok = true;
    for (const k of Object.keys(mats)) {
      const m = mats[k];
      if (px + m.w + 1 > W) { px = 1; py += shelf + 1; shelf = 0; }
      if (py + m.h + 1 > W) { ok = false; break; }
      m.u = px; m.v = py; px += m.w + 1; shelf = Math.max(shelf, m.h);
    }
    if (ok) { TEX = W; break; }
  }
  const uvs = {}; for (const [k, m] of Object.entries(mats)) uvs[k] = [m.u, m.v];
  poseGeo('rest', REST, outDir, TEX, TEX, uvs);
  for (const [n, p] of Object.entries(POSES)) poseGeo(n, p, outDir, TEX, TEX, uvs);
  console.log(`已导出姿势 geo 到 ${outDir} (rest + ${Object.keys(POSES).length} 个姿势), 贴图 ${TEX}x${TEX}`);
}
