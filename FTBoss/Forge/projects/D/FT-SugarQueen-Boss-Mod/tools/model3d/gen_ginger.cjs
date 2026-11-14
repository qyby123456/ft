// 姜饼人国王(糖霜女皇) v2 生成器: 重写几何+贴图, **保留全部骨骼名与枢轴**(7 组动画继续可用)
// 规格取 Bedrock 空间(y 向上, 脚底 y=0, 前端 -Z); 枢轴沿用原文件
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out';
fs.mkdirSync(OUT, { recursive: true });

/* ================= 规格 ================= */
const SPEC = [
  { name: 'root', parent: null, pivot: [0, 0, 0] },
  { name: 'body', parent: 'root', pivot: [0, 13, 0] },
  { name: 'head', parent: 'body', pivot: [0, 28.6, 0] },
  { name: 'arm_l', parent: 'body', pivot: [-7.8, 26, 0] },
  { name: 'arm_r', parent: 'body', pivot: [7.8, 26, 0] },
  { name: 'scepter', parent: 'arm_r', pivot: [9.75, 16.9, 0] },
  { name: 'cape', parent: 'body', pivot: [0, 28.6, 3.9] },
  { name: 'leg_l', parent: 'root', pivot: [-3.9, 13, 0] },
  { name: 'leg_r', parent: 'root', pivot: [3.9, 13, 0] }
];
const C = [];   // {bone, o:[x,y,z], s:[w,h,d], mat}
const cube = (bone, o, s, mat) => C.push({ bone, o, s, mat });

/* ---- body: 姜饼躯干 + 糖霜下摆 + 腰带 + 三颗糖扣 ---- */
cube('body', [-8, 13, -4], [16, 15, 8], 'dough');            // 躯干
cube('body', [-8.6, 12.2, -4.6], [17.2, 2.6, 9.2], 'icing'); // 下摆糖霜
cube('body', [-8.2, 18.6, -4.3], [16.4, 1.6, 8.6], 'icing_dark'); // 腰带
for (const y of [20.4, 23.4, 26.4]) cube('body', [-1.3, y, -5.0], [2.6, 2.6, 1.2], 'candy');
cube('body', [-8.4, 27.4, -4.4], [16.8, 1.2, 8.8], 'icing');  // 领口糖霜

/* ---- head: 姜饼头 + 糖霜刘海 + 金冠(环 + 5 尖) ---- */
cube('head', [-5.5, 28.6, -5.5], [11, 11, 11], 'dough_head');
cube('head', [-5.9, 38.4, -5.9], [11.8, 1.6, 11.8], 'icing');     // 刘海
cube('head', [-6.4, 39.8, -6.4], [12.8, 2.4, 12.8], 'gold');      // 冠环
cube('head', [-6.4, 42.0, -6.4], [2.4, 3.0, 2.4], 'gold');
cube('head', [-1.2, 42.0, -6.4], [2.4, 3.0, 2.4], 'gold');
cube('head', [4.0, 42.0, -6.4], [2.4, 3.0, 2.4], 'gold');
cube('head', [-6.4, 42.0, 4.0], [2.4, 3.0, 2.4], 'gold');
cube('head', [4.0, 42.0, 4.0], [2.4, 3.0, 2.4], 'gold');

/* ---- 手臂 + 糖霜袖口 ---- */
for (const b of ['arm_l', 'arm_r']) {
  const right = (b === 'arm_r');
  const px = 7.8 * (right ? 1 : -1);
  // 手臂朝**外侧**长: 左臂 x -12.0..-8.0, 右臂 x 8.0..12.0 (躯干外沿 ±8, 刚好贴住不穿模)
  const ax = right ? px + 0.2 : px - 4.2;
  const cx = right ? px : px - 4.6;
  cube(b, [ax, 15.6, -2.9], [4.0, 12, 5.8], 'dough');
  cube(b, [cx, 14.0, -3.2], [4.6, 2.6, 6.4], 'icing');
}

/* ---- 权杖: 杖身 + 宝石 + 金环 ---- */
cube('scepter', [9.2, 10.4, -0.55], [1.1, 17, 1.1], 'gold');
cube('scepter', [8.2, 27.0, -1.55], [3.1, 3.1, 3.1], 'gem');
cube('scepter', [7.7, 26.5, -2.05], [4.1, 0.9, 4.1], 'gold');
cube('scepter', [7.7, 29.7, -2.05], [4.1, 0.9, 4.1], 'gold');

/* ---- 披风: 红呢 + 金边 ---- */
cube('cape', [-8.6, 9.6, 4.0], [17.2, 19.8, 1.4], 'cape');
cube('cape', [-8.8, 9.2, 3.9], [17.6, 1.8, 1.6], 'gold');

/* ---- 腿 + 糖霜靴 ---- */
for (const b of ['leg_l', 'leg_r']) {
  const right = (b === 'leg_r');
  const px = 3.9 * (right ? 1 : -1);
  // 与原始文件一致: 腿在枢轴内侧 → 左腿 x -6.5..-1.3, 右腿 x 1.3..6.5 (中间留 2.6 空隙)
  cube(b, [px - 2.6, 0, -3.3], [5.2, 13, 6.6], 'dough');
  cube(b, [px - 2.9, 0, -3.6], [5.8, 2.8, 7.2], 'icing');
}

/* ================= UV 打包(box UV) ================= */
const mats = {};
for (const c of C) {
  const [w, h, d] = c.s;
  const need = [Math.ceil(2 * (d + w)) + 2, Math.ceil(d + h) + 2];
  const m = mats[c.mat] || (mats[c.mat] = { w: 0, h: 0 });
  m.w = Math.max(m.w, need[0]); m.h = Math.max(m.h, need[1]);
}
let TEX_W = 0, TEX_H = 0, usedH = 0;
for (const [W, H] of [[128, 64], [128, 128], [256, 128]]) {
  const PAD = 1; let px = PAD, py = PAD, shelf = 0;
  for (const k of Object.keys(mats)) {
    const m = mats[k];
    if (px + m.w + PAD > W) { px = PAD; py += shelf + PAD; shelf = 0; }
    m.u = px; m.v = py; px += m.w + PAD; shelf = Math.max(shelf, m.h);
  }
  const h = py + shelf + PAD;
  if (h <= H) { TEX_W = W; TEX_H = H; usedH = h; break; }
}
if (!TEX_W) { console.error('贴图放不下'); process.exit(1); }
console.log(`贴图 ${TEX_W}x${TEX_H} (占用到 y=${usedH}); 材质 ${Object.keys(mats).length} 种`);
for (const [k, m] of Object.entries(mats)) console.log(`  ${k.padEnd(11)} u=${m.u} v=${m.v} w=${m.w} h=${m.h}`);

/* ================= 画贴图 ================= */
const cv = new Canvas(TEX_W, TEX_H);
cv.rect(0, 0, TEX_W, TEX_H, 0, 0, 0, 0);
let seed = 20260914;
const rnd = () => { seed = (seed * 1103515245 + 12345) & 0x7fffffff; return seed / 0x7fffffff; };
const nz = (x, y, a, b) => ((x * a + y * b) % 23) / 23;
function fillMat(m, base, opt) {
  opt = opt || {};
  for (let y = 0; y < m.h; y++) for (let x = 0; x < m.w; x++) {
    let [r, g, b] = base;
    const f = 0.88 + nz(x, y, 37, 91) * 0.24;
    r *= f; g *= f; b *= f;
    // 姜饼焦边: 每隔一段画一圈深褐
    if (opt.crust && ((x % 13 === 0) || (y % 11 === 0))) { r = base[0] * 0.62; g = base[1] * 0.62; b = base[2] * 0.62; }
    if (opt.sugar) { const sp = nz(x, y, 53, 19); if (sp > 0.82) { r = 250; g = 246; b = 238; } }
    cv.set(m.u + x, m.v + y, Math.min(255, r), Math.min(255, g), Math.min(255, b), 1);
  }
}
fillMat(mats.dough, [166, 106, 56], { crust: true });          // 姜饼面团
fillMat(mats.dough_head, [166, 106, 56], { crust: true });     // 头部面团(单独材质: 只有它能画五官)
fillMat(mats.icing, [244, 240, 230], { sugar: true });         // 白糖霜
fillMat(mats.icing_dark, [206, 150, 196], { sugar: true });    // 深色糖霜(腰带)
fillMat(mats.candy, [214, 62, 70], {});                        // 糖扣
fillMat(mats.gold, [226, 176, 62], {});                        // 金冠
fillMat(mats.gem, [92, 206, 190], {});                         // 杖头宝石
fillMat(mats.cape, [156, 38, 46], { crust: false });           // 披风
// 糖霜五官: 画在头部材质的 -Z(north) 面上 —— box-UV 里 north 面位于 (u+d, v+d), 尺寸 (w,h)
{
  const m = mats.dough_head;
  const hb = C.find(c => c.bone === 'head' && c.mat === 'dough_head');
  const fx = m.u + hb.s[2], fy = m.v + hb.s[2];      // d = 11
  const fw = hb.s[0], fh = hb.s[1];
  // 两眼(糖霜白, 中间留深色瞳孔)
  for (const ex of [fx + 2, fx + 6]) {
    cv.rect(ex, fy + 3, 3, 3, 248, 244, 236, 1);
    cv.rect(ex + 1, fy + 4, 1, 1, 74, 46, 26, 1);
  }
  // 笑嘴(糖霜)
  cv.rect(fx + 3, fy + 7, 5, 1, 248, 244, 236, 1);
  cv.rect(fx + 2, fy + 6, 1, 1, 248, 244, 236, 1);
  cv.rect(fx + 8, fy + 6, 1, 1, 248, 244, 236, 1);
  // 腮红糖粒
  cv.rect(fx + 1, fy + 6, 2, 2, 214, 62, 70, 1);
  cv.rect(fx + 8, fy + 6, 2, 2, 214, 62, 70, 1);
}
cv.save(path.join(OUT, 'gingerbread_king.png'));
console.log('已写出 gingerbread_king.png');

/* ================= 写 geo(扁平 bones + parent) ================= */
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: 'geometry.gingerbread_king',
      texture_width: TEX_W, texture_height: TEX_H,
      visible_bounds_width: 4, visible_bounds_height: 4, visible_bounds_offset: [0, 2, 0]
    },
    bones: SPEC.map(b => {
      const o = { name: b.name, pivot: b.pivot };
      if (b.parent) o.parent = b.parent;
      const bs = C.filter(c => c.bone === b.name).map(c => {
        const m = mats[c.mat];
        return { origin: c.o, size: c.s, uv: [m.u, m.v] };
      });
      if (bs.length) o.cubes = bs;
      return o;
    })
  }]
};
fs.writeFileSync(path.join(OUT, 'gingerbread_king.geo.json'), JSON.stringify(geo, null, 1));
console.log('已写出 gingerbread_king.geo.json');

let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
for (const c of C) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], c.o[i]); mx[i] = Math.max(mx[i], c.o[i] + c.s[i]); }
console.log(`包围盒 X ${mn[0].toFixed(1)}..${mx[0].toFixed(1)} (${((mx[0] - mn[0]) / 16).toFixed(2)}格)  Y ${mn[1].toFixed(1)}..${mx[1].toFixed(1)} (${((mx[1] - mn[1]) / 16).toFixed(2)}格)  Z ${mn[2].toFixed(1)}..${mx[2].toFixed(1)} (${((mx[2] - mn[2]) / 16).toFixed(2)}格)`);
console.log(`实体碰撞箱 1.2 x 2.7 格 -> 宽比 ${((mx[0] - mn[0]) / 16 / 1.2).toFixed(2)}  高比 ${((mx[1] - mn[1]) / 16 / 2.7).toFixed(2)}`);
