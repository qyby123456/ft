// 小姜饼人(糖霜女皇召唤物) 生成器 —— 一份规格同时产出 **geo + 贴图 + 5 段动画**
// Bedrock 空间: y 向上, 脚底 y=0, 正面朝 -Z。骨骼/动画名必须与 GingerbreadMinionEntity 对齐:
//   bones: root/body/head/arm_l/arm_r/leg_l/leg_r
//   anims: animation.gingerbread_minion.{idle,walk,attack,hurt,death}
// 尺寸目标: 高 17.6 单位 = 1.10 格, 宽 13 单位 = 0.8125 格 → 碰撞箱 0.8 x 1.1 (宽比 1.02 / 高比 1.00)
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out';
fs.mkdirSync(OUT, { recursive: true });

const BONE = 'gingerbread_minion';

/* ================= 骨架 ================= */
// pivot 是**绝对模型坐标**(Bedrock 约定), 不是相对父骨骼的
const SPEC = [
  { name: 'root', parent: null, pivot: [0, 0, 0] },
  { name: 'body', parent: 'root', pivot: [0, 5, 0] },
  { name: 'head', parent: 'body', pivot: [0, 11.6, 0] },
  { name: 'arm_l', parent: 'body', pivot: [-5.2, 11, 0] },
  { name: 'arm_r', parent: 'body', pivot: [5.2, 11, 0] },
  { name: 'leg_l', parent: 'root', pivot: [-2, 5, 0] },
  { name: 'leg_r', parent: 'root', pivot: [2, 5, 0] }
];

const C = [];
const cube = (bone, o, s, mat) => C.push({ bone, o, s, mat });

/* ---- 躯干: 姜饼身 + 糖霜裙边/领口 + 三颗软糖扣 ---- */
cube('body', [-4, 5, -2], [8, 7, 4], 'dough');                 // 躯干 y5..12
cube('body', [-5, 4, -3], [10, 2, 6], 'icing');                // 糖霜裙边(比躯干大一圈, 不共面) y4..6
// 领口: z 只用 5 宽(-2.5..2.5) —— 头骨的正面在 z=-3, 领口若也到 -3 会与头骨正面**共面闪烁**
cube('body', [-5, 11, -2.5], [10, 1, 5], 'icing');            // 领口糖霜 y11..12
cube('body', [-0.7, 6.3, -3], [1.4, 1.4, 1], 'candy_r');       // 扣1(凸出正面 1 单位)
cube('body', [-0.7, 7.8, -3], [1.4, 1.4, 1], 'candy_g');       // 扣2
cube('body', [-0.7, 9.3, -3], [1.4, 1.4, 1], 'candy_y');       // 扣3(顶端 10.7, 让开领口 y=11)

/* ---- 头: 姜饼头 + 糖霜"头发"(刘海层) ---- */
cube('head', [-3, 11.6, -3], [6, 5, 6], 'dough_head');         // 头 y11.6..16.6(下沿埋进领口, 避免与躯干共面)
cube('head', [-4, 15.6, -4], [8, 2, 8], 'icing');              // 糖霜头发 y15.6..17.6

/* ---- 手臂 + 糖霜袖口 ---- */
for (const b of ['arm_l', 'arm_r']) {
  const right = (b === 'arm_r');
  const ax = right ? 4.2 : -6.2;      // 与躯干外沿(x=±4)留 0.2 缝, 不共面
  const cx = right ? 3.5 : -6.5;      // 袖口比手臂宽 1, 向外多出 0.3
  cube(b, [ax, 6, -1], [2, 5, 2], 'dough');       // 手臂 y6..11
  cube(b, [cx, 5, -1.5], [3, 2, 3], 'icing');     // 袖口 y5..7
}

/* ---- 腿 + 糖霜靴 ---- */
for (const b of ['leg_l', 'leg_r']) {
  const right = (b === 'leg_r');
  const lx = right ? 0.5 : -3.5;      // 两腿中间留 1 单位缝
  const bx = right ? 0.2 : -3.8;
  cube(b, [lx, 0, -2], [3, 5.6, 4], 'dough');     // 腿顶埋进躯干 0.6, 不共面
  cube(b, [bx, 0, -2.3], [3.6, 2, 4.6], 'icing'); // 靴 y0..2
}

/* ================= UV 打包(box UV, 货架式) ================= */
const mats = {};
for (const c of C) {
  const [w, h, d] = c.s;
  const m = mats[c.mat] || (mats[c.mat] = { w: 0, h: 0 });
  m.w = Math.max(m.w, Math.ceil(2 * (d + w)) + 2);
  m.h = Math.max(m.h, Math.ceil(d + h) + 2);
}
let TEX_W = 0, TEX_H = 0, usedH = 0;
for (const [W, H] of [[64, 64], [64, 128], [128, 64], [128, 128]]) {
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
const nz = (x, y, a, b) => ((x * a + y * b) % 23) / 23;
function fillMat(m, base, opt) {
  opt = opt || {};
  for (let y = 0; y < m.h; y++) for (let x = 0; x < m.w; x++) {
    let [r, g, b] = base;
    const f = 0.9 + nz(x, y, 31, 79) * 0.2;
    r *= f; g *= f; b *= f;
    if (opt.crust && ((x % 9 === 0) || (y % 7 === 0))) { r = base[0] * 0.68; g = base[1] * 0.68; b = base[2] * 0.68; }
    if (opt.sugar && nz(x, y, 53, 19) > 0.84) { r = 252; g = 250; b = 244; }
    cv.set(m.u + x, m.v + y, Math.min(255, r), Math.min(255, g), Math.min(255, b), 1);
  }
}
fillMat(mats.dough, [176, 116, 62], { crust: true });        // 姜饼面团(焦边)
fillMat(mats.dough_head, [176, 116, 62], { crust: true });   // 头面团(单独材质, 只有它画五官)
fillMat(mats.icing, [246, 242, 232], { sugar: true });       // 糖霜
fillMat(mats.candy_r, [206, 62, 70], {});
fillMat(mats.candy_g, [94, 168, 74], {});
fillMat(mats.candy_y, [226, 186, 66], {});

// 软糖扣高光(每颗左上一小格亮色, 出立体感)
for (const k of ['candy_r', 'candy_g', 'candy_y']) {
  const m = mats[k];
  cv.rect(m.u + 1, m.v + 1, 1, 1, 255, 255, 255, 1);
}
// 五官: 画在头部材质的 -Z(north)面 —— box-UV 里 north 面位于 (u+d, v+d), 尺寸 (w,h)
{
  const m = mats.dough_head;
  const hb = C.find(c => c.bone === 'head' && c.mat === 'dough_head');
  const fx = m.u + hb.s[2], fy = m.v + hb.s[2];   // d = 6
  // 先把整张脸铺成干净的面团色(否则焦边条纹会盖过五官)
  cv.rect(fx, fy, hb.s[0], hb.s[1], 190, 132, 74, 1);
  for (let i = 0; i < hb.s[0]; i++) { cv.set(fx + i, fy, 176, 120, 66, 1); }          // 额头压暗一行
  cv.rect(fx + 1, fy + 2, 1, 1, 48, 28, 14, 1);   // 左眼
  cv.rect(fx + 4, fy + 2, 1, 1, 48, 28, 14, 1);   // 右眼
  cv.rect(fx + 1, fy + 1, 1, 1, 214, 168, 116, 1); // 眼上高光
  cv.rect(fx + 4, fy + 1, 1, 1, 214, 168, 116, 1);
  cv.rect(fx + 2, fy + 4, 2, 1, 246, 242, 232, 1); // 糖霜笑嘴(U 形)
  cv.rect(fx + 1, fy + 3, 1, 1, 246, 242, 232, 1);
  cv.rect(fx + 4, fy + 3, 1, 1, 246, 242, 232, 1);
  cv.rect(fx + 0, fy + 3, 1, 1, 206, 62, 70, 1);   // 腮红糖粒
  cv.rect(fx + 5, fy + 3, 1, 1, 206, 62, 70, 1);
}
cv.save(path.join(OUT, `${BONE}.png`));
console.log(`已写出 ${BONE}.png`);

/* ================= 写 geo(扁平 bones + parent) ================= */
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: `geometry.${BONE}`,
      texture_width: TEX_W, texture_height: TEX_H,
      visible_bounds_width: 3, visible_bounds_height: 3, visible_bounds_offset: [0, 1, 0]
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
fs.writeFileSync(path.join(OUT, `${BONE}.geo.json`), JSON.stringify(geo, null, 1));
console.log(`已写出 ${BONE}.geo.json`);

/* ================= 写动画 =================
   Java 侧: idle/walk 循环; attack 0.5s(10t) / hurt 0.4s(8t) 一次性并被 getAnimationTick 卡住;
   death 用 hold_on_last_frame 保持倒地姿势直到尸体被移除(20t)。
   注意 position 单位是 1/16 格(模型单位)。 */
const A = `animation.${BONE}.`;
const anims = {
  [A + 'idle']: {
    loop: true,
    animation_length: 2.0,
    bones: {
      root: { position: { '0.0': [0, 0, 0], '1.0': [0, 0.25, 0], '2.0': [0, 0, 0] } },
      body: { rotation: { '0.0': [0, 0, 0], '0.6': [1.4, 0, -1.2], '1.4': [1.0, 0, 1.2], '2.0': [0, 0, 0] } },
      head: { rotation: { '0.0': [0, 0, 0], '0.7': [0.8, 5, 0], '1.3': [-0.6, -5, 0], '2.0': [0, 0, 0] } },
      arm_l: { rotation: { '0.0': [0, 0, 2], '1.0': [0, 0, 6], '2.0': [0, 0, 2] } },
      arm_r: { rotation: { '0.0': [0, 0, -2], '1.0': [0, 0, -6], '2.0': [0, 0, -2] } },
      leg_l: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, 1.5], '2.0': [0, 0, 0] } },
      leg_r: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, -1.5], '2.0': [0, 0, 0] } }
    }
  },
  [A + 'walk']: {
    loop: true,
    animation_length: 1.0,
    bones: {
      root: {
        position: { '0.0': [0, 0, 0], '0.25': [0, 0.5, 0], '0.5': [0, 0, 0], '0.75': [0, 0.5, 0], '1.0': [0, 0, 0] },
        rotation: { '0.0': [0, 0, 0], '0.25': [0, 0, -2], '0.5': [0, 0, 0], '0.75': [0, 0, 2], '1.0': [0, 0, 0] }
      },
      body: { rotation: { '0.0': [3, 0, 0], '0.5': [3, 0, 0], '1.0': [3, 0, 0] } },
      head: { rotation: { '0.0': [-2, 0, 0], '0.5': [-2, 0, 0], '1.0': [-2, 0, 0] } },
      leg_l: { rotation: { '0.0': [-26, 0, 0], '0.25': [0, 0, 0], '0.5': [26, 0, 0], '0.75': [0, 0, 0], '1.0': [-26, 0, 0] } },
      leg_r: { rotation: { '0.0': [26, 0, 0], '0.25': [0, 0, 0], '0.5': [-26, 0, 0], '0.75': [0, 0, 0], '1.0': [26, 0, 0] } },
      arm_l: { rotation: { '0.0': [16, 0, 4], '0.5': [-16, 0, 4], '1.0': [16, 0, 4] } },
      arm_r: { rotation: { '0.0': [-16, 0, -4], '0.5': [16, 0, -4], '1.0': [-16, 0, -4] } }
    }
  },
  [A + 'attack']: {
    loop: 'hold_on_last_frame',
    animation_length: 0.5,
    bones: {
      root: { position: { '0.0': [0, 0, 0], '0.15': [0, 0, 0.4], '0.3': [0, 0, -1.2], '0.5': [0, 0, 0] } },
      body: { rotation: { '0.0': [0, 0, 0], '0.15': [-6, -14, 0], '0.3': [8, 16, 0], '0.5': [0, 0, 0] } },
      head: { rotation: { '0.0': [0, 0, 0], '0.15': [-4, -10, 0], '0.3': [6, 12, 0], '0.5': [0, 0, 0] } },
      arm_r: { rotation: { '0.0': [0, 0, 0], '0.15': [-78, 0, -12], '0.3': [62, 0, 6], '0.5': [0, 0, 0] } },
      arm_l: { rotation: { '0.0': [0, 0, 0], '0.15': [18, 0, 14], '0.3': [-16, 0, 8], '0.5': [0, 0, 0] } },
      leg_l: { rotation: { '0.0': [0, 0, 0], '0.3': [-10, 0, 0], '0.5': [0, 0, 0] } },
      leg_r: { rotation: { '0.0': [0, 0, 0], '0.3': [12, 0, 0], '0.5': [0, 0, 0] } }
    }
  },
  [A + 'hurt']: {
    loop: 'hold_on_last_frame',
    animation_length: 0.4,
    bones: {
      root: { position: { '0.0': [0, 0, 0], '0.1': [0, 0, 1.4], '0.25': [0, 0, -0.3], '0.4': [0, 0, 0] } },
      body: { rotation: { '0.0': [0, 0, 0], '0.1': [-15, 0, 4], '0.25': [6, 0, -2], '0.4': [0, 0, 0] } },
      head: { rotation: { '0.0': [0, 0, 0], '0.1': [-20, 0, 6], '0.25': [8, 0, -3], '0.4': [0, 0, 0] } },
      arm_l: { rotation: { '0.0': [0, 0, 2], '0.1': [-22, 0, 26], '0.4': [0, 0, 2] } },
      arm_r: { rotation: { '0.0': [0, 0, -2], '0.1': [-22, 0, -26], '0.4': [0, 0, -2] } },
      leg_l: { rotation: { '0.0': [0, 0, 0], '0.1': [8, 0, 0], '0.4': [0, 0, 0] } },
      leg_r: { rotation: { '0.0': [0, 0, 0], '0.1': [-8, 0, 0], '0.4': [0, 0, 0] } }
    }
  },
  [A + 'death']: {
    loop: 'hold_on_last_frame',
    animation_length: 1.0,
    bones: {
      // 绕脚底(root 原点)后倒 90°, 变成"饼干片躺在地上"。
      // 注意: 绕 x 轴转 90° 后, 模型的 y' 范围 = 原来的 **z 范围**(-4..4 单位),
      // 所以最后必须把 root 抬起 **4 单位**(0.25 格), 否则整片饼干会陷进地里一半。
      root: {
        rotation: { '0.0': [0, 0, 0], '0.2': [-12, 0, 0], '0.45': [86, 0, 0], '0.62': [93, 0, 0], '0.8': [90, 0, 0], '1.0': [90, 0, 0] },
        position: { '0.0': [0, 0, 0], '0.2': [0, 0.6, -0.6], '0.45': [0, 4.0, -1.0], '0.62': [0, 4.3, 0], '0.8': [0, 4.0, 0], '1.0': [0, 4.0, 0] }
      },
      body: { rotation: { '0.0': [0, 0, 0], '0.3': [10, 0, 0], '0.8': [0, 0, 0], '1.0': [0, 0, 0] } },
      head: { rotation: { '0.0': [0, 0, 0], '0.25': [-14, 0, 0], '0.6': [10, 0, 0], '1.0': [0, 0, 0] } },
      arm_l: { rotation: { '0.0': [0, 0, 2], '0.35': [-30, 0, 40], '0.8': [-6, 0, 34], '1.0': [-6, 0, 34] } },
      arm_r: { rotation: { '0.0': [0, 0, -2], '0.35': [-30, 0, -40], '0.8': [-6, 0, -34], '1.0': [-6, 0, -34] } },
      leg_l: { rotation: { '0.0': [0, 0, 0], '0.35': [16, 0, 0], '0.8': [6, 0, 0], '1.0': [6, 0, 0] } },
      leg_r: { rotation: { '0.0': [0, 0, 0], '0.35': [-14, 0, 0], '0.8': [-5, 0, 0], '1.0': [-5, 0, 0] } }
    }
  }
};
fs.writeFileSync(path.join(OUT, `${BONE}.animation.json`),
  JSON.stringify({ format_version: '1.8.0', animations: anims }, null, 1));
console.log(`已写出 ${BONE}.animation.json (${Object.keys(anims).length} 段动画)`);

/* ================= 自检输出 ================= */
let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
for (const c of C) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], c.o[i]); mx[i] = Math.max(mx[i], c.o[i] + c.s[i]); }
console.log(`包围盒 X ${mn[0].toFixed(1)}..${mx[0].toFixed(1)} (${((mx[0] - mn[0]) / 16).toFixed(2)}格)  Y ${mn[1].toFixed(1)}..${mx[1].toFixed(1)} (${((mx[1] - mn[1]) / 16).toFixed(2)}格)  Z ${mn[2].toFixed(1)}..${mx[2].toFixed(1)} (${((mx[2] - mn[2]) / 16).toFixed(2)}格)`);
console.log(`实体碰撞箱 0.8 x 1.1 格 -> 宽比 ${((mx[0] - mn[0]) / 16 / 0.8).toFixed(2)}  高比 ${((mx[1] - mn[1]) / 16 / 1.1).toFixed(2)}`);
