// 酒桶王 v2 生成器: 输出 geo JSON + 贴图 PNG
// 约束: 骨骼名/层级保持不变(7 个动画继续可用), 只重塑几何与贴图
// 设计: 橡木桶身(八角化圆柱近似) + 三道黄铜箍 + 木塞与塞柄 + 铁提环 + 四条铁掌腿
//       + 黄铜龙头(阀) + 桶头开窗露出内部发光酒液
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

/* ================= 参数 ================= */
const CY = 26;            // 桶中心高度(Bedrock: y 向上, 0=脚底)
const R_END = 17.0;       // 端部半径(半宽)
const R_MID = 20.0;       // 中部半径 -> 直径 40 = 2.5 格; 含提环 48 = 3.0 格
const CH = 6.0;           // 八角切角
const ZF = -20, ZB = 20;  // 桶身前后端(总长 40 = 2.5 格)
const TEX_W = 256, TEX_H = 256;

const bones = [];
const cubes = [];          // {bone, origin, size, mat}
function bone(name, parent, pivot) { const b = { name, parent, pivot, cubes: [] }; bones.push(b); return b; }
function cube(b, origin, size, mat) { cubes.push({ bone: b.name, origin, size, mat }); }

/* ---- 八角柱: 5 块拼成一个切角正方形截面 ---- */
function octagon(b, y, r, z0, z1, mat) {
  const c = Math.min(CH, r * 0.55), len = z1 - z0;
  // 芯
  cube(b, [-r + c, y - r + c, z0], [2 * (r - c), 2 * (r - c), len], mat);
  // 上/下
  cube(b, [-r + c, y + r - c, z0], [2 * (r - c), c, len], mat);
  cube(b, [-r + c, y - r, z0], [2 * (r - c), c, len], mat);
  // 左/右
  cube(b, [-r, y - r + c, z0], [c, 2 * (r - c), len], mat);
  cube(b, [r - c, y - r + c, z0], [c, 2 * (r - c), len], mat);
}
/** 八角环(带厚度 t, 略微外凸 proud) —— 箍用 */
function octaRing(b, y, r, z0, z1, proud, mat) {
  const R = r + proud, c = Math.min(CH, R * 0.55), len = z1 - z0;
  cube(b, [-R + c, y - R + c, z0], [2 * (R - c), 2 * (R - c), len], mat);
  cube(b, [-R + c, y + R - c, z0], [2 * (R - c), c, len], mat);
  cube(b, [-R + c, y - R, z0], [2 * (R - c), c, len], mat);
  cube(b, [-R, y - R + c, z0], [c, 2 * (R - c), len], mat);
  cube(b, [R - c, y - R + c, z0], [c, 2 * (R - c), len], mat);
}

/* ================= 骨骼(名字与层级保持原样) ================= */
const root = bone('root', null, [0, CY, 0]);
const body = bone('body', 'root', [0, CY, 0]);
const bmain = bone('barrel_main', 'body', [0, CY, 0]);
const hFront = bone('hoop_front', 'barrel_main', [0, CY, -17]);
const hBack = bone('hoop_back', 'barrel_main', [0, CY, 17]);
const hMid = bone('hoop_middle', 'barrel_main', [0, CY, 0]);
const cork = bone('cork', 'barrel_main', [0, 37, -22]);
const corkH = bone('cork_handle', 'cork', [0, 37, -25]);
const handL = bone('handle_left', 'barrel_main', [-22, CY, 0]);
const handR = bone('handle_right', 'barrel_main', [22, CY, 0]);
const legFL = bone('leg_front_left', 'root', [-11, 16, -13.5]);
const legFR = bone('leg_front_right', 'root', [11, 16, -13.5]);
const legBL = bone('leg_back_left', 'root', [-11, 16, 13.5]);
const legBR = bone('leg_back_right', 'root', [11, 16, 13.5]);
const wine = bone('wine_liquid', 'barrel_main', [0, CY, 0]);
const glow = bone('wine_glow', 'wine_liquid', [0, CY, 0]);
const spout = bone('spout', 'barrel_main', [0, 14, -20]);

/* ---- 桶身: 三段鼓形(端细中粗) ---- */
octagon(body, CY, R_END, ZF, -9, 'plank');          // 前段
octagon(body, CY, R_MID, -9, 9, 'plank_mid');       // 中段(最粗)
octagon(body, CY, R_END, 9, ZB, 'plank');           // 后段

/* ---- 桶头(桶盖): 前盖开窗露出酒液, 后盖实心 ---- */
{
  const c = CH, r = R_END, z0 = ZF - 3, z1 = ZF;    // 前盖 3 厚
  const wx = 10, wy0 = CY - 10, wy1 = CY + 4;       // 窗口
  // 前盖: 围绕窗口的四块 + 四个切角 (左/右各宽 r-wx, 上/下夹住窗口)
  cube(bmain, [-r, CY - r + c, z0], [r - wx, 2 * (r - c), z1 - z0], 'plank_end');
  cube(bmain, [wx, CY - r + c, z0], [r - wx, 2 * (r - c), z1 - z0], 'plank_end');
  cube(bmain, [-wx, wy1, z0], [2 * wx, r - (wy1 - CY), z1 - z0], 'plank_end');
  cube(bmain, [-wx, CY - r, z0], [2 * wx, (wy0 - (CY - r)), z1 - z0], 'plank_end');
  cube(bmain, [-r + c, CY + r - c, z0], [2 * (r - c), c, z1 - z0], 'plank_end');
  cube(bmain, [-r + c, CY - r, z0], [2 * (r - c), c, z1 - z0], 'plank_end');
  cube(bmain, [-r, CY - r + c, z0], [c, 2 * (r - c), z1 - z0], 'plank_end');
  cube(bmain, [r - c, CY - r + c, z0], [c, 2 * (r - c), z1 - z0], 'plank_end');
  // 后盖实心
  octagon(bmain, CY, R_END, ZB, ZB + 3, 'plank_end');
  // 注意: 这里原本还有两道"桶头铁箍环", 但 octaRing() 生成的是**实心八角板**(芯块+四边),
  // 放在桶头会变成一块实心铁板把桶盖和酒液窗口整个糊住(实测桶身因此渲染成铁灰色)。
  // 桶身已有三道黄铜箍提供横向箍带, 所以直接去掉, 不再补。
}

/* ---- 三道黄铜箍 ---- */
octaRing(hFront, CY, R_END, -16, -10, 0.9, 'brass');
octaRing(hMid, CY, R_MID, -4, 4, 0.9, 'brass');
octaRing(hBack, CY, R_END, 10, 16, 0.9, 'brass');

/* ---- 木塞(桶头窗口上方) + 塞柄 ---- */
cube(cork, [-5, 32, -25.5], [10, 11, 7], 'cork');
cube(corkH, [-1.5, 41.5, -26.5], [3, 3, 9], 'cork');
cube(corkH, [-8.5, 41.5, -24.0], [17, 3, 3], 'cork');
cube(corkH, [-1.5, 41.5, -21.5], [3, 3, 9], 'cork');

/* ---- 黄铜龙头(阀): 自桶头下方出酒(控制在桶前 4 格内, 免得整体过长) ---- */
cube(spout, [-4, CY - 16, ZF - 4], [8, 8, 6], 'brass');       // 阀体
cube(spout, [-2.5, CY - 15, ZF - 8], [5, 5, 5], 'brass');     // 出酒管(仅外伸 5)
cube(spout, [-8, CY - 14.5, ZF - 6.5], [16, 2, 2], 'iron');   // 阀柄

/* ---- 两侧铁提环 ---- */
cube(handL, [-24, CY - 7, -4], [3, 14, 8], 'iron');
cube(handR, [21, CY - 7, -4], [3, 14, 8], 'iron');

/* ---- 四条腿(y 0..16, 枢轴在桶底接合处) ----
 * 2026-09-10 修: 原来木腿只到 y=5, 而桶身下缘在内侧是 y=9、外侧(八角切角处)是 y=15,
 * 于是四个"脚底板"和桶身之间是**断开**的(实测就是用户看到的"没链接上")。
 * 现在改成: 矮木腿(0..8) + 上宽下窄的托架(8..16, 宽 11 以便插进桶身下缘与侧壁) + 铁掌(0..2),
 * 托架顶端 16 > 侧壁起点 15, 保证与桶身有 1 单位实打实的重叠。 */
for (const [b, x, z] of [[legFL, -11, -13.5], [legFR, 11, -13.5], [legBL, -11, 13.5], [legBR, 11, 13.5]]) {
  cube(b, [x - 4.5, 0, z - 4.5], [9, 8, 9], 'plank_dark');      // 木腿
  cube(b, [x - 5.5, 8, z - 5.5], [11, 8, 11], 'plank_dark');    // 托架(接进桶身)
  cube(b, [x - 5, 0, z - 5], [10, 2, 10], 'iron');             // 铁掌
}

/* ---- 酒液 + 辉光 ----
 * 注意: octagon() 生成的是**实心**八角块, 所以桶身内部并不存在"空腔"——把酒液放在桶体内部
 * 是看不见的(实测窗口区采到的仍是木头)。这里改成把酒液做成**嵌在桶头窗口里的一层酒面**:
 * 位置落在桶盖厚度之内, 前脸比桶盖前脸缩进 0.2, 于是从正面正好透过窗口看到发光的酒。
 * death 动画里 wine_liquid 会缩放 -> 看起来就是"酒流光"; spray 里的缩放即"喷酒"。 */
cube(wine, [-10.5, 15.5, ZF - 2.8], [21, 15, 2.6], 'wine');
cube(glow, [-9, 17, ZF - 2.9], [18, 12, 1.6], 'wine_glow');

/* ================= UV 打包(box UV: 每个 cube 需要 (2(d+w)) x (d+h)) ================= */
const MATS = {};
for (const c of cubes) {
  const w = c.size[0], h = c.size[1], d = c.size[2];
  const need = [Math.ceil(2 * (d + w)) + 2, Math.ceil(d + h) + 2];
  const m = MATS[c.mat] || (MATS[c.mat] = { w: 0, h: 0 });
  m.w = Math.max(m.w, need[0]); m.h = Math.max(m.h, need[1]);
}
// 货架式打包 —— u/v/w/h 全部取整: 像素缓冲的下标必须是整数, 否则写入会被静默丢弃
const PAD = 1;
let px = PAD, py = PAD, shelfH = 0;
const placed = {};
for (const k of Object.keys(MATS)) {
  const m = MATS[k];
  m.w = Math.ceil(m.w); m.h = Math.ceil(m.h);
  if (px + m.w + PAD > TEX_W) { px = PAD; py += shelfH + PAD; shelfH = 0; }
  m.u = Math.round(px); m.v = Math.round(py); placed[k] = m;
  px += m.w + PAD; shelfH = Math.max(shelfH, m.h);
}
const usedH = py + shelfH + PAD;
console.log(`UV 打包: ${Object.keys(MATS).length} 种材质, 占用 ${TEX_W}x${usedH}`);
for (const [k, m] of Object.entries(MATS)) console.log(`  ${k.padEnd(12)} u=${m.u} v=${m.v} w=${m.w} h=${m.h}`);
if (usedH > TEX_H) { console.error(`!! 贴图高度不够: 需要 ${usedH} > ${TEX_H}`); process.exit(1); }

/* ================= 写 geo ================= */
// 重要: Bedrock 格式的 bones 必须是**扁平列表 + parent 字段**。
// 早先版本误用了嵌套 children 结构 -> GeckoLib 只读到顶层 root(root 无 cube) -> 整个模型不渲染。
function toFlat() {
  return bones.map(b => {
    const o = { name: b.name, pivot: b.pivot };
    if (b.parent) o.parent = b.parent;
    const bs = cubes.filter(c => c.bone === b.name).map(c => {
      const m = MATS[c.mat];
      return { origin: c.origin, size: c.size, uv: [m.u, m.v] };
    });
    if (bs.length) o.cubes = bs;      // 无 cube 的骨骼省略该字段(与原始文件一致)
    return o;
  });
}
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: 'geometry.barrel_boss',
      texture_width: TEX_W, texture_height: TEX_H,
      visible_bounds_width: 5, visible_bounds_height: 4, visible_bounds_offset: [0, 2, 0]
    },
    bones: toFlat()
  }]
};
const outDir = process.argv[2] || 'out';
fs.mkdirSync(outDir, { recursive: true });
fs.writeFileSync(path.join(outDir, 'wine_barrel_boss.geo.json'), JSON.stringify(geo, null, 1));

/* ================= 画贴图 ================= */
const cv = new Canvas(TEX_W, TEX_H);
cv.rect(0, 0, TEX_W, TEX_H, 0, 0, 0, 0);   // 透明底
let seed = 20260912;
const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };

function fill(m, fn) {
  for (let y = 0; y < m.h; y++) for (let x = 0; x < m.w; x++) {
    const c = fn(x, y, m);
    if (c) cv.set(m.u + x, m.v + y, c[0], c[1], c[2], c.length > 3 ? c[3] : 1);
  }
}
/** 橡木竖板: 深缝 + 木纹 */
function plank(y0, y1, base, dark, seamEvery) {
  return (x, y) => {
    const seam = (x % seamEvery === 0);
    let [r, g, b] = base;
    const n = ((x * 37 + y * 91) % 17) / 17;
    const f = 0.86 + n * 0.28;
    r *= f; g *= f; b *= f;
    if (seam) { r = dark[0]; g = dark[1]; b = dark[2]; }
    else if ((x % seamEvery) === 1) { r *= 1.12; g *= 1.12; b *= 1.12; }
    if (y < y0 || y > y1) { r *= 0.88; g *= 0.88; b *= 0.88; }
    return [Math.min(255, r), Math.min(255, g), Math.min(255, b)];
  };
}
for (const [k, m] of Object.entries(MATS)) {
  if (k === 'plank') fill(m, plank(0, m.h, [124, 82, 44], [58, 36, 20], 16));
  else if (k === 'plank_mid') fill(m, plank(0, m.h, [130, 87, 47], [58, 36, 20], 16));
  else if (k === 'plank_end') fill(m, plank(0, m.h, [112, 74, 40], [52, 32, 18], 16));
  else if (k === 'plank_dark') fill(m, plank(0, m.h, [78, 52, 30], [38, 24, 14], 8));
  else if (k === 'brass') fill(m, (x, y) => {
    const band = 0.72 + 0.5 * Math.abs(Math.sin(y * 0.9));            // 金属高光带
    const n = ((x * 13 + y * 7) % 11) / 11;
    const f = band * (0.9 + n * 0.2);
    return [Math.min(255, 214 * f), Math.min(255, 168 * f), Math.min(255, 62 * f)];
  });
  else if (k === 'iron') fill(m, (x, y) => {
    const n = ((x * 29 + y * 53) % 13) / 13;
    const f = 0.62 + n * 0.30;
    return [96 * f, 98 * f, 106 * f];
  });
  else if (k === 'cork') fill(m, (x, y) => {
    const n = ((x * 41 + y * 17) % 19) / 19;
    const hole = ((x * 7 + y * 11) % 23) === 0;
    const f = 0.84 + n * 0.3;
    return hole ? [120, 86, 48] : [188 * f, 146 * f, 88 * f];
  });
  else if (k === 'wine') fill(m, (x, y) => {
    const n = ((x * 61 + y * 23) % 29) / 29;
    const f = 0.8 + n * 0.4;
    return [120 * f, 16 * f, 34 * f];
  });
  else if (k === 'wine_glow') fill(m, (x, y) => {
    const n = ((x * 19 + y * 31) % 17) / 17;
    const f = 0.85 + n * 0.5;
    return [200 * f, 40 * f, 62 * f, 0.85];
  });
}
cv.save(path.join(outDir, 'wine_barrel_boss.png'));
console.log('已写出: wine_barrel_boss.geo.json + wine_barrel_boss.png');

// 调试贴图: 每种材质涂成不同亮色 + 描边框, 用来一眼看出 UV 映射对不对
{
  const dbg = new Canvas(TEX_W, TEX_H);
  const COLORS = [[255,0,0],[0,255,0],[0,0,255],[255,255,0],[255,0,255],[0,255,255],[255,128,0],[128,0,255],[255,255,255],[0,0,0]];
  let k = 0;
  for (const [name, m] of Object.entries(MATS)) {
    const c = COLORS[k++ % COLORS.length];
    dbg.rect(m.u, m.v, m.w, m.h, c[0], c[1], c[2], 1);
    // 描边(便于看清单个材质的边界)
    for (let x = 0; x < m.w; x++) { dbg.set(m.u + x, m.v, 20, 20, 20, 1); dbg.set(m.u + x, m.v + m.h - 1, 20, 20, 20, 1); }
    for (let y = 0; y < m.h; y++) { dbg.set(m.u, m.v + y, 20, 20, 20, 1); dbg.set(m.u + m.w - 1, m.v + y, 20, 20, 20, 1); }
    console.log(`  [debug] ${name.padEnd(12)} -> rgb(${c.join(',')})`);
  }
  dbg.save(path.join(outDir, 'wine_barrel_debug.png'));
  console.log('已写出调试贴图: wine_barrel_debug.png');
}
