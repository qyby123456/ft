// 茶芽小妖(山茶花芽) v2 生成器: 一份规格同时产出 geo JSON + 贴图 PNG
// 用法: node gen_camellia.cjs <输出目录>
//
// 相比 v1 的改进:
//   1) 叶片不再是"1 单位厚的平板"——每片叶子由 3 段轴对齐方块逐级抬升拼出自然上翘弧度;
//   2) 叶子由 3 片补到 4 片(leaf_l/leaf_r/leaf_b + 新增 leaf_f), 侧影不再缺一角;
//   3) 花从"花苞"升级为山茶花: 4 主瓣(内+外两段)+ 4 角瓣 + 黄色花蕊 + 绿萼;
//   4) 茎分两节并各带一圈"节", 侧枝加一个真正的小花苞(bud);
//   5) 土堆加了两条须根, 落地不再像悬空;
//   6) 不用 cube rotation(纯轴对齐方块): 预览工具与游戏渲染结果一致, 避免旋转顺序差异导致走样。
//
// 约束: 骨骼名保持 root/stem1/stem2/leaf_l/leaf_r/leaf_b/flower 不变(6 段既有动画继续可用),
//       新增 leaf_f / bud 两个骨骼; bones 必须是扁平列表 + parent(Bedrock 规范)。
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

/* ================= 参数 ================= */
const TEX_W = 64, TEX_H = 64;
const SOIL_H = 2.6;        // 土堆高
const STEM1_Y = 2.0, STEM1_H = 5.6;
const STEM2_Y = STEM1_Y + STEM1_H;    // 7.6
const STEM2_H = 5.4;
const FLOWER_Y = 13.0;                 // 花心高度(根 bone 枢轴)

const bones = [];
const cubes = [];
function bone(name, parent, pivot) { const b = { name, parent, pivot, cubes: [] }; bones.push(b); return b; }
function cube(b, origin, size, mat) {
  if (size[0] <= 0 || size[1] <= 0 || size[2] <= 0) throw new Error('非法尺寸 ' + b.name + ' ' + size);
  cubes.push({ bone: b.name, origin, size, mat });
}

/** 一片上翘的叶子: 3 段轴对齐方块, 逐级变窄 + 逐级抬高, 拼出叶片的自然弧度 */
function leaf(b, dir, baseY, reach, mats) {
  const [matBase, matMid, matTip] = mats;
  const segs = [
    { out: 0.5, len: reach * 0.40, w: 3.4, th: 0.8, rise: 0.0, mat: matBase },
    { out: 0.5 + reach * 0.40, len: reach * 0.34, w: 3.0, th: 0.7, rise: 1.0, mat: matMid },
    { out: 0.5 + reach * 0.74, len: reach * 0.26, w: 2.2, th: 0.6, rise: 2.0, mat: matTip },
  ];
  for (const s of segs) {
    const y = baseY + s.rise;
    if (dir === 'x-') cube(b, [-(s.out + s.len), y, -s.w / 2], [s.len, s.th, s.w], s.mat);
    else if (dir === 'x+') cube(b, [s.out, y, -s.w / 2], [s.len, s.th, s.w], s.mat);
    else if (dir === 'z-') cube(b, [-s.w / 2, y, -(s.out + s.len)], [s.w, s.th, s.len], s.mat);
    else cube(b, [-s.w / 2, y, s.out], [s.w, s.th, s.len], s.mat);
  }
}

/* ================= 骨骼 ================= */
const root = bone('root', null, [0, 0, 0]);
const stem1 = bone('stem1', 'root', [0, STEM1_Y, 0]);
const stem2 = bone('stem2', 'stem1', [0, STEM2_Y, 0]);
const leafL = bone('leaf_l', 'stem1', [0, 5.0, 0]);
const leafR = bone('leaf_r', 'stem1', [0, 6.4, 0]);
const leafF = bone('leaf_f', 'stem1', [0, 5.8, 0]);
const leafB = bone('leaf_b', 'stem1', [0, 7.4, 0]);
const flower = bone('flower', 'stem2', [0, FLOWER_Y, 0]);
const bud = bone('bud', 'stem2', [1.0, 9.7, 0]);

/* ---- 土堆 + 须根 ---- */
cube(root, [-6.0, 0, -6.0], [12, SOIL_H, 12], 'soil');
cube(root, [-3.6, SOIL_H, -3.6], [7.2, 1.0, 7.2], 'soil_dark');
cube(root, [-7.4, 0, -1.0], [1.8, 1.0, 1.8], 'root');     // 左须根
cube(root, [5.6, 0, -1.1], [2.0, 1.0, 2.0], 'root');      // 右须根
cube(root, [-1.0, 0, 5.7], [1.8, 1.0, 1.8], 'root');      // 后须根

/* ---- 茎: 两节, 各带一圈节 ---- */
cube(stem1, [-1.0, STEM1_Y, -1.0], [2.0, STEM1_H, 2.0], 'stem');
cube(stem1, [-1.4, STEM1_Y + STEM1_H - 1.2, -1.4], [2.8, 1.0, 2.8], 'stem_node');
cube(stem2, [-0.75, STEM2_Y, -0.75], [1.5, STEM2_H, 1.5], 'stem');
cube(stem2, [-1.05, STEM2_Y + STEM2_H - 1.4, -1.05], [2.1, 0.8, 2.1], 'stem_node');

/* ---- 四片叶: ±X 两片较低, -Z 片中, +Z 片最高(错落) ---- */
leaf(leafL, 'x-', 5.0, 6.0, ['leaf_deep', 'leaf_mid', 'leaf_tip']);
leaf(leafR, 'x+', 6.4, 6.0, ['leaf_deep', 'leaf_mid', 'leaf_tip']);
leaf(leafF, 'z-', 5.8, 5.8, ['leaf_deep', 'leaf_mid', 'leaf_tip']);
leaf(leafB, 'z+', 7.4, 5.8, ['leaf_deep', 'leaf_mid', 'leaf_tip']);

/* ---- 侧芽(闭合的小花苞): 挂在茎节外侧偏下, 避开顶部花冠 ---- */
cube(bud, [0.9, 9.7, -0.8], [1.7, 1.2, 1.6], 'calyx');
cube(bud, [1.15, 10.8, -0.55], [1.1, 1.7, 1.1], 'petal_deep');
cube(bud, [1.35, 12.4, -0.35], [0.7, 1.0, 0.7], 'petal');

/* ---- 花: 绿萼 + 4 主瓣(内外两段) + 4 角瓣 + 花蕊 ---- */
cube(flower, [-1.5, FLOWER_Y - 0.4, -1.5], [3.0, 1.2, 3.0], 'calyx');
cube(flower, [-1.2, FLOWER_Y + 3.2, -1.2], [2.4, 1.2, 2.4], 'stamen');      // 花蕊芯
// 4 主瓣: 沿 ±X / ±Z 向外, 内段贴芯(深粉) + 外段抬高一格(浅粉)
const PET = [
  ['x+', 1.3, 15.9, -1.6, 3.0], ['x-', 1.3, 15.9, -1.6, 3.0],
  ['z+', 1.3, 15.9, -1.6, 3.0], ['z-', 1.3, 15.9, -1.6, 3.0],
];
for (const [dir] of PET) {
  const yIn = FLOWER_Y + 2.2, yOut = FLOWER_Y + 3.1;
  if (dir === 'x+' || dir === 'x-') {
    const s = dir === 'x+' ? 1 : -1;
    cube(flower, [s > 0 ? 1.2 : -3.4, yIn, -1.5], [2.2, 0.9, 3.0], 'petal_deep');
    cube(flower, [s > 0 ? 3.2 : -5.2, yOut, -1.2], [2.0, 0.8, 2.4], 'petal');
  } else {
    const s = dir === 'z+' ? 1 : -1;
    cube(flower, [-1.5, yIn, s > 0 ? 1.2 : -3.4], [3.0, 0.9, 2.2], 'petal_deep');
    cube(flower, [-1.2, yOut, s > 0 ? 3.2 : -5.2], [2.4, 0.8, 2.0], 'petal');
  }
}
// 4 角瓣(斜向, 低一格, 把方形轮廓打散)
for (const [sx, sz] of [[1, 1], [1, -1], [-1, 1], [-1, -1]]) {
  cube(flower, [sx > 0 ? 1.4 : -3.0, FLOWER_Y + 1.7, sz > 0 ? 1.4 : -3.0], [1.6, 0.8, 1.6], 'petal_deep');
  cube(flower, [sx > 0 ? 2.9 : -4.7, FLOWER_Y + 2.5, sz > 0 ? 2.9 : -4.7], [1.8, 0.7, 1.8], 'petal');
}
// 花蕊(3 根小须)
cube(flower, [-0.35, FLOWER_Y + 4.4, -0.35], [0.7, 1.1, 0.7], 'stamen');
cube(flower, [0.85, FLOWER_Y + 4.1, 0.2], [0.7, 1.0, 0.7], 'stamen');
cube(flower, [-0.9, FLOWER_Y + 4.0, 0.6], [0.7, 1.0, 0.7], 'stamen');

/* ================= UV 打包(box UV: 每个 cube 需要 (2(d+w)) x (d+h)) ================= */
const MATS = {};
for (const c of cubes) {
  const w = c.size[0], h = c.size[1], d = c.size[2];
  const need = [Math.ceil(2 * (d + w)) + 2, Math.ceil(d + h) + 2];
  const m = MATS[c.mat] || (MATS[c.mat] = { w: 0, h: 0 });
  m.w = Math.max(m.w, need[0]); m.h = Math.max(m.h, need[1]);
}
const PAD = 1;
let px = PAD, py = PAD, shelfH = 0;
for (const k of Object.keys(MATS)) {
  const m = MATS[k];
  m.w = Math.ceil(m.w); m.h = Math.ceil(m.h);
  if (px + m.w + PAD > TEX_W) { px = PAD; py += shelfH + PAD; shelfH = 0; }
  m.u = Math.round(px); m.v = Math.round(py);
  px += m.w + PAD; shelfH = Math.max(shelfH, m.h);
}
const usedH = py + shelfH + PAD;
console.log(`UV 打包: ${Object.keys(MATS).length} 种材质, 占用 ${TEX_W}x${usedH}`);
for (const [k, m] of Object.entries(MATS)) console.log(`  ${k.padEnd(12)} u=${m.u} v=${m.v} w=${m.w} h=${m.h}`);
if (usedH > TEX_H) { console.error(`!! 贴图高度不够: 需要 ${usedH} > ${TEX_H}`); process.exit(1); }

/* ================= 写 geo ================= */
function toFlat() {
  return bones.map(b => {
    const o = { name: b.name, pivot: b.pivot };
    if (b.parent) o.parent = b.parent;
    const bs = cubes.filter(c => c.bone === b.name).map(c => {
      const m = MATS[c.mat];
      return { origin: c.origin, size: c.size, uv: [m.u, m.v] };
    });
    if (bs.length) o.cubes = bs;
    return o;
  });
}
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: 'geometry.camellia_sprout',
      texture_width: TEX_W, texture_height: TEX_H,
      visible_bounds_width: 1.5, visible_bounds_height: 1.5, visible_bounds_offset: [0, 0.6, 0]
    },
    bones: toFlat()
  }]
};
const outDir = process.argv[2] || 'out';
fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, 'camellia_sprout.geo.json'), JSON.stringify(geo, null, 1));

/* ================= 画贴图 ================= */
const cv = new Canvas(TEX_W, TEX_H);
cv.rect(0, 0, TEX_W, TEX_H, 0, 0, 0, 0);
let seed = 20260913;
const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
function fill(m, fn) {
  for (let y = 0; y < m.h; y++) for (let x = 0; x < m.w; x++) {
    const c = fn(x, y, m);
    if (c) cv.set(m.u + x, m.v + y, c[0], c[1], c[2], c.length > 3 ? c[3] : 1);
  }
}
const clamp255 = v => Math.max(0, Math.min(255, v));
/** 通用噪点系数 */
const noise = (x, y, k) => 0.88 + (((x * 37 + y * 91) % k) / k) * 0.24;

for (const [k, m] of Object.entries(MATS)) {
  if (k === 'soil') fill(m, (x, y) => {
    const n = noise(x, y, 13);
    const grain = ((x * 7 + y * 11) % 17) === 0;
    const base = grain ? [126, 98, 64] : [92, 70, 46];
    return [clamp255(base[0] * n), clamp255(base[1] * n), clamp255(base[2] * n)];
  });
  else if (k === 'soil_dark') fill(m, (x, y) => {
    const n = noise(x, y, 11);
    return [clamp255(68 * n), clamp255(50 * n), clamp255(32 * n)];
  });
  else if (k === 'root') fill(m, (x, y) => {
    const n = noise(x, y, 9);
    return [clamp255(150 * n), clamp255(134 * n), clamp255(100 * n)];
  });
  else if (k === 'stem') fill(m, (x, y) => {
    const fib = (x % 3 === 0) ? 0.82 : 1.0;          // 竖向纤维
    const n = noise(x, y, 15) * fib;
    return [clamp255(88 * n), clamp255(126 * n), clamp255(58 * n)];
  });
  else if (k === 'stem_node') fill(m, (x, y) => {
    const ring = 0.86 + 0.3 * Math.sin(y * 1.4);
    const n = noise(x, y, 12) * ring;
    return [clamp255(70 * n), clamp255(104 * n), clamp255(48 * n)];
  });
  else if (k === 'calyx') fill(m, (x, y) => {
    const n = noise(x, y, 10);
    return [clamp255(60 * n), clamp255(94 * n), clamp255(44 * n)];
  });
  else if (k === 'leaf_deep') fill(m, (x, y) => {
    const n = noise(x, y, 14);
    return [clamp255(58 * n), clamp255(98 * n), clamp255(44 * n)];
  });
  else if (k === 'leaf_mid') fill(m, (x, y) => {
    const n = noise(x, y, 14);
    const vein = (y % 5 === 0) ? 1.14 : 1.0;          // 叶脉
    return [clamp255(78 * n * vein), clamp255(124 * n * vein), clamp255(54 * n)];
  });
  else if (k === 'leaf_tip') fill(m, (x, y) => {
    const n = noise(x, y, 12);
    return [clamp255(112 * n), clamp255(152 * n), clamp255(66 * n)];
  });
  else if (k === 'petal') fill(m, (x, y) => {
    const g = 1.0 - y / (m.h * 1.6);                   // 花瓣外缘更浅
    const n = noise(x, y, 16);
    return [clamp255(228 * g * n), clamp255(150 * g * n), clamp255(182 * g * n)];
  });
  else if (k === 'petal_deep') fill(m, (x, y) => {
    const n = noise(x, y, 16);
    return [clamp255(196 * n), clamp255(104 * n), clamp255(146 * n)];
  });
  else if (k === 'stamen') fill(m, (x, y) => {
    const n = noise(x, y, 9);
    const speck = ((x * 5 + y * 3) % 7) === 0;
    return speck ? [clamp255(198 * n), clamp255(140 * n), clamp255(42 * n)]
                 : [clamp255(234 * n), clamp255(198 * n), clamp255(74 * n)];
  });
}
cv.save(path.join(outDir, 'camellia_sprout.png'));
console.log('已写出: camellia_sprout.geo.json + camellia_sprout.png');

/* ---- 调试贴图: 每种材质纯色 + 描边, 一眼看出 UV 映射 ---- */
{
  const dbg = new Canvas(TEX_W, TEX_H);
  const COLORS = [[255, 0, 0], [0, 255, 0], [0, 0, 255], [255, 255, 0], [255, 0, 255], [0, 255, 255],
    [255, 128, 0], [128, 0, 255], [255, 255, 255], [0, 128, 0], [128, 64, 0], [64, 64, 64]];
  let i = 0;
  for (const [name, m] of Object.entries(MATS)) {
    const c = COLORS[i++ % COLORS.length];
    dbg.rect(m.u, m.v, m.w, m.h, c[0], c[1], c[2], 1);
    for (let x = 0; x < m.w; x++) { dbg.set(m.u + x, m.v, 16, 16, 16, 1); dbg.set(m.u + x, m.v + m.h - 1, 16, 16, 16, 1); }
    for (let y = 0; y < m.h; y++) { dbg.set(m.u, m.v + y, 16, 16, 16, 1); dbg.set(m.u + m.w - 1, m.v + y, 16, 16, 16, 1); }
    console.log(`  [debug] ${name.padEnd(12)} -> rgb(${c.join(',')})`);
  }
  dbg.save(path.join(outDir, 'camellia_debug.png'));
}

/* ---- 结构自检: 骨骼/立方体/包围盒 ---- */
{
  const min = [1e9, 1e9, 1e9], max = [-1e9, -1e9, -1e9];
  for (const c of cubes) for (let i = 0; i < 3; i++) {
    min[i] = Math.min(min[i], c.origin[i]);
    max[i] = Math.max(max[i], c.origin[i] + c.size[i]);
  }
  const size = [max[0] - min[0], max[1] - min[1], max[2] - min[2]];
  console.log(`骨骼 ${bones.length} / cube ${cubes.length}`);
  console.log(`包围盒 X ${min[0]}..${max[0]}  Y ${min[1]}..${max[1]}  Z ${min[2]}..${max[2]}`);
  console.log(`尺寸 ${size.map(v => v.toFixed(2)).join(' x ')} 单位 = ${size.map(v => (v / 16).toFixed(2)).join(' x ')} 格`);
  console.log(`碰撞箱 0.8 x 1.3 格 -> 宽比 ${(size[0] / 16 / 0.8).toFixed(2)}  高比 ${(size[1] / 16 / 1.3).toFixed(2)}`);
}
