/* 噬影之刃 模型/贴图生成器 —— 移植自用户提供的 python 脚本(逐像素方块 + 调色板图集)
   输出: assets/ftboss/models/item/shadow_sword.json  (覆写)
         assets/ftboss/textures/item/shadow_sword.png (覆写, 32x32 调色板)
         art/shadow_sword_reference.png / shadow_sword_preview.png (预览留档)
   用法: node gen_shadow_sword.cjs                                                        */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const MODEL = path.join(ROOT, 'src/main/resources/assets/ftboss/models/item/shadow_sword.json');
const TEXTURE = path.join(ROOT, 'src/main/resources/assets/ftboss/textures/item/shadow_sword.png');
const ART = path.join(ROOT, 'art');

/* 0 表示透明，其余数字代表调色板颜色 */
const COLORS = {
  1: '#100D19', 2: '#211A30', 3: '#393047', 4: '#663A91',
  5: '#B06AE8', 6: '#ECD6FF', 7: '#443447',
};
const W = 16, H = 32;
const pixels = new Map();
const put = (x, y, c) => { if (x >= 0 && x < W && y >= 0 && y < H) pixels.set(x + ',' + y, c); };

/* ---------- 剑刃：由下往上 ---------- */
for (let y = 12; y <= 28; y++) {
  let left = y < 23 ? 5 : 6;
  let right = y < 24 ? 10 : 9;
  if (y === 14 || y === 18 || y === 22) right = 11;
  if (y === 16 || y === 20) left = 4;
  for (let x = left; x <= right; x++) {
    let color;
    if (x === left) color = 3; else if (x === right) color = 1; else color = 2;
    put(x, y, color);
  }
  const vein = 7 + ((Math.floor(y / 3) % 2) ? 1 : 0);
  put(vein, y, y % 5 === 0 ? 5 : 4);
}
/* ---------- 剑尖 ---------- */
for (let x = 6; x < 10; x++) put(x, 29, x === 6 ? 3 : 2);
put(7, 30, 3); put(8, 30, 4); put(8, 31, 5);

/* ---------- 爪形护手 ---------- */
for (const [y, left, right] of [[9, 3, 12], [10, 2, 13], [11, 1, 14]]) {
  for (let x = left; x <= right; x++) put(x, y, y === 11 ? 3 : 2);
}
for (const [x, y, c] of [[1, 12, 3], [2, 12, 2], [1, 13, 4], [0, 14, 5],
                         [13, 12, 2], [14, 12, 3], [14, 13, 4], [15, 14, 5]]) put(x, y, c);
for (let x = 5; x <= 10; x++) put(x, 10, 4);
for (let x = 6; x <= 9; x++) put(x, 10, 5);
put(7, 10, 1); put(8, 10, 6); put(7, 11, 4); put(8, 11, 5);

/* ---------- 剑柄 + 柄首晶核 ---------- */
for (let y = 2; y <= 8; y++) for (let x = 6; x <= 9; x++) {
  let c = (x === 6 || x === 9) ? 1 : 2;
  if (y % 2 === 0 && (x === 7 || x === 8)) c = 7;
  put(x, y, c);
}
for (let x = 5; x <= 10; x++) put(x, 1, 3);
for (let x = 6; x <= 9; x++) put(x, 0, 4);
put(7, 1, 5); put(8, 1, 6);

/* ---------- 32x32 调色板图集(每色一列 4px) ---------- */
const atlas = new Canvas(32, 32);
const hex = h => [parseInt(h.slice(1, 3), 16), parseInt(h.slice(3, 5), 16), parseInt(h.slice(5, 7), 16)];
for (let y = 0; y < 32; y++) for (let x = 0; x < 32; x++) { const c = hex(COLORS[1]); atlas.set(x, y, c[0], c[1], c[2], 1); }
for (const [num, h] of Object.entries(COLORS)) {
  const c = hex(h), sx = (Number(num) - 1) * 4;
  for (let x = sx; x < sx + 4; x++) for (let y = 0; y < 32; y++) atlas.set(x, y, c[0], c[1], c[2], 1);
}
fs.mkdirSync(path.dirname(TEXTURE), { recursive: true });
atlas.save(TEXTURE);

/* ---------- 参考图(1x 放大 16x), 供肉眼比对 ---------- */
fs.mkdirSync(ART, { recursive: true });
const ref = new Canvas(W, H);
for (const [k, num] of pixels) { const [x, y] = k.split(',').map(Number); const c = hex(COLORS[num]); ref.set(x, H - 1 - y, c[0], c[1], c[2], 1); }
ref.save(path.join(ART, 'shadow_sword_reference.png'));
const big = new Canvas(W * 16, H * 16);
for (let y = 0; y < H * 16; y++) for (let x = 0; x < W * 16; x++) {
  const px = Math.floor(x / 16), py = Math.floor(y / 16);
  const num = pixels.get(px + ',' + (H - 1 - py));
  if (!num) { big.set(x, y, 0, 0, 0, 0); continue; }
  const c = hex(COLORS[num]); big.set(x, y, c[0], c[1], c[2], 1);
}
big.save(path.join(ART, 'shadow_sword_preview.png'));

/* ---------- 逐像素方块 ---------- */
const uvFor = color => { const u = (color - 1) * 2; return [u + 0.25, 0.25, u + 1.75, 1.75]; };
const face = color => ({ uv: uvFor(color), texture: '#0' });
const elements = [];
const entries = [...pixels.entries()].sort((a, b) => {
  const [ax, ay] = a[0].split(',').map(Number), [bx, by] = b[0].split(',').map(Number);
  return ay - by || ax - bx;
});
for (const [k, color] of entries) {
  const [x, y] = k.split(',').map(Number);
  const thickness = (y >= 9 && y <= 11) ? 2.5 : (y < 9 ? 2.0 : 1.5);
  const z0 = 8 - thickness / 2, z1 = 8 + thickness / 2;
  elements.push({
    name: 'pixel_' + x + '_' + y,
    from: [x, y, z0], to: [x + 1, y + 1, z1],
    faces: {
      north: face(color), south: face(color),
      east: face(1), west: face(color === 2 || color === 3 ? 3 : color),
      up: face(color), down: face(1),
    },
  });
}

const model = {
  credit: 'Original Shadow Sword prototype (ported generator)',
  texture_size: [32, 32],
  textures: { 0: 'ftboss:item/shadow_sword', particle: 'ftboss:item/shadow_sword' },
  elements,
  display: {
    thirdperson_righthand: { rotation: [0, -90, 55], translation: [0, 4, 0.5], scale: [0.65, 0.65, 0.65] },
    thirdperson_lefthand: { rotation: [0, 90, -55], translation: [0, 4, 0.5], scale: [0.65, 0.65, 0.65] },
    firstperson_righthand: { rotation: [0, -90, 25], translation: [1.2, 2.5, 1.2], scale: [0.65, 0.65, 0.65] },
    firstperson_lefthand: { rotation: [0, 90, -25], translation: [1.2, 2.5, 1.2], scale: [0.65, 0.65, 0.65] },
    gui: { rotation: [0, 0, -35], translation: [3, -3, 0], scale: [0.52, 0.52, 0.52] },
    ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.3, 0.3, 0.3] },
    fixed: { rotation: [0, 180, 0], translation: [0, -4, 0], scale: [0.5, 0.5, 0.5] },
  },
};
fs.mkdirSync(path.dirname(MODEL), { recursive: true });
fs.writeFileSync(MODEL, JSON.stringify(model, null, 2));

/* ---------- 自检 ---------- */
let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
for (const e of elements) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], e.from[i]); mx[i] = Math.max(mx[i], e.to[i]); }
console.log('[shadow_sword] elements=' + elements.length + '  像素数=' + pixels.size);
console.log('[shadow_sword] bbox ' + mn.map(v => v.toFixed(1)).join(',') + ' -> ' + mx.map(v => v.toFixed(1)).join(','));
const illegal = elements.filter(e => e.rotation || (e.from[0] >= e.to[0]) || (e.from[1] >= e.to[1]) || (e.from[2] >= e.to[2]));
console.log('[shadow_sword] 非法元素(旋转/空厚度): ' + illegal.length);
console.log('[shadow_sword] 输出: ' + MODEL);
console.log('[shadow_sword] 贴图: ' + TEXTURE);
