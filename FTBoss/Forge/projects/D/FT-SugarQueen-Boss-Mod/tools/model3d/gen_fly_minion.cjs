/* 苍蝇小怪模型(2026-09-12): 炫彩答辩超人的召唤物。
   胸/腹/头/复眼/左右翅/六足, 64x64 图集; 输出 geo + anim + png 到 out_fly_minion/
   用法: node gen_fly_minion.cjs [outDir] */
const fs = require('fs'), path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_fly_minion';
const W = 64, H = 64;
const cv = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) cv.set(x, y, 0, 0, 0, 0);

const P = {
  chitin: [58, 56, 54],     // 胸节: 灰褐
  sheen: [72, 108, 96],     // 腹节金属绿
  dark: [26, 26, 30],
  band: [34, 46, 42],       // 腹节条纹
  eye: [168, 38, 38],
  wing: [212, 218, 230],
  leg: [34, 34, 40],
};
let px = 1, py = 1, rh = 0;
const C = [];
function alloc(w, h, d) {
  const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
  if (px + fw > W) { px = 1; py += rh + 2; rh = 0; }
  const u = px, v = py;
  px += fw + 2; rh = Math.max(rh, fh);
  return [u, v];
}
function cube(bone, o, s, mat, rot, pivot) {
  const uv = alloc(s[0], s[1], s[2]);
  C.push({ bone, o, s, uv, mat, rot, pivot });
}

/* 身体: 头在前(-Z), 腹在后(+Z) */
cube('body', [-1.6, 0, -1.6], [3.2, 3, 3.2], 'chitin');       // 胸
cube('body', [-2.1, 0.1, 1.5], [4.2, 3.6, 4.4], 'sheen');     // 腹
cube('body', [-1.3, 0.6, 3.5], [2.6, 2.2, 1], 'band');        // 腹尖
cube('head', [-2, 0.5, -3.5], [4, 3.2, 2], 'dark');           // 头
cube('head', [-2.6, 1.1, -3.9], [1.4, 2.4, 1.4], 'eye');      // 左复眼
cube('head', [1.2, 1.1, -3.9], [1.4, 2.4, 1.4], 'eye');       // 右复眼
cube('body', [-0.4, 0.8, -4.1], [0.8, 0.8, 0.9], 'dark');     // 口器
/* 翅膀: 薄片, 向外后方展开(后掠 18°), 转轴在翅根 */
cube('wing_l', [-3.9, 3.1, -1.8], [4.0, 0.3, 3.4], 'wing', [0, -16, 0], [-0.2, 3.1, -1.8]);
cube('wing_r', [0.0, 3.1, -1.8], [4.0, 0.3, 3.4], 'wing', [0, 16, 0], [0.2, 3.1, -1.8]);
/* 六足: 每侧三条, 向外斜撑 */
for (let i = 0; i < 3; i++) {
  const z = -1.6 + i * 1.5;
  cube('body', [-2.9, -2.4, z], [1, 2.8, 0.7], 'leg', [0, 0, 20], [-2.4, -0.2, z]);
  cube('body', [1.9, -2.4, z], [1, 2.8, 0.7], 'leg', [0, 0, -20], [2.4, -0.2, z]);
}

for (const c of C) {
  const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
  const col = P[c.mat];
  for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) {
    /* 腹节按行做条纹, 免得整只苍蝇是死板的纯色; 其余面加一点随机深浅 */
    let base = col;
    if (c.mat === 'sheen' && y % 4 === 0) base = P.band;
    const j = ((x * 7 + y * 13) % 5) - 2;
    cv.set(c.uv[0] + x, c.uv[1] + y,
      Math.max(0, base[0] + j * 3), Math.max(0, base[1] + j * 3), Math.max(0, base[2] + j * 3), 1);
  }
}

const mk = (bone) => C.filter((c) => c.bone === bone).map((c) => {
  const o = { origin: c.o, size: c.s, uv: c.uv };
  if (c.rot) o.rotation = c.rot;
  if (c.pivot) o.pivot = c.pivot;
  return o;
});
const bones = [
  { name: 'root', pivot: [0, 0, 0] },
  { name: 'body', pivot: [0, 1, 0], parent: 'root', cubes: mk('body') },
  { name: 'head', pivot: [0, 1.5, -3], parent: 'body', cubes: mk('head') },
  { name: 'wing_l', pivot: [-1.4, 3, -1], parent: 'body', cubes: mk('wing_l') },
  { name: 'wing_r', pivot: [1.4, 3, -1], parent: 'body', cubes: mk('wing_r') },
];

const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: {
      identifier: 'geometry.fly_minion',
      texture_width: W, texture_height: H,
      visible_bounds_width: 1.5, visible_bounds_height: 1.5, visible_bounds_offset: [0, 0.5, 0],
    },
    bones,
  }],
};

/* 翅膀高频扇动(0.2s 一个来回) + 身体轻微起伏 */
const flap = { '0.0': [0, 0, 26], '0.05': [0, 0, -26], '0.1': [0, 0, 26], '0.15': [0, 0, -26], '0.2': [0, 0, 26] };
const flapR = { '0.0': [0, 0, -26], '0.05': [0, 0, 26], '0.1': [0, 0, -26], '0.15': [0, 0, 26], '0.2': [0, 0, -26] };
const anim = {
  format_version: '1.8.0',
  animations: {
    'animation.fly_minion.fly': {
      loop: true,
      animation_length: 0.6,
      bones: {
        root: { position: { '0.0': [0, 0, 0], '0.2': [0, 0.3, 0], '0.4': [0, 0, 0], '0.6': [0, 0.22, 0] } },
        body: { rotation: { '0.0': [2, 0, 0], '0.3': [-2, 0, 0], '0.6': [2, 0, 0] } },
        wing_l: { rotation: flap },
        wing_r: { rotation: flapR },
      },
    },
  },
};

fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'fly_minion.geo.json'), JSON.stringify(geo, null, 1));
fs.writeFileSync(path.join(OUT, 'fly_minion.animation.json'), JSON.stringify(anim, null, 1));
cv.save(path.join(OUT, 'fly_minion.png'));
console.log('苍蝇模型已生成: cube=' + C.length + ' bones=' + bones.length + ' 图集=' + W + 'x' + H);
