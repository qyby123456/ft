// 1:1 观感自检: 把交付目录里的 32x32 贴图按最近邻缩到 16x16 并放大, 贴在原图下方
// 用法: node check_1to1.cjs   -> 输出 桌面\FT-模型检查\1比1观感自检.png
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const SRC = path.resolve(__dirname, '..', '..', 'src/main/resources/assets/ftboss/textures/item');
const OUT = 'C:\\Users\\YOUR_USERNAME\\Desktop\\FT-模型检查\\1比1观感自检.png';
const IDS = ['sugar_scepter', 'butcher_cleaver', 'tea_leaf_blade', 'wine_bottle_mace', 'cheese_wheel_mace'];

const K = 12, K16 = 24, PAD = 18, GAP = 14;
const cellW = 32 * K + PAD * 2;
const rowH = 32 * K + GAP + 16 * K16 + PAD + 26;
const cv = new Canvas(cellW * IDS.length + PAD, rowH + PAD);
cv.rect(0, 0, cv.w, cv.h, 24, 26, 32, 1);

function blit(c, ox, oy, k) {
  for (let y = 0; y < c.h * k; y++) for (let x = 0; x < c.w * k; x++) {
    const g = (((x / k) | 0) + ((y / k) | 0)) % 2 ? 62 : 76;
    cv.set(ox + x, oy + y, g, g + 2, g + 6);
  }
  const d = c.data || c.d;
  for (let y = 0; y < c.h; y++) for (let x = 0; x < c.w; x++) {
    const i = (y * c.w + x) * 4;
    if (!d[i + 3]) continue;
    cv.rect(ox + x * k, oy + y * k, k, k, d[i], d[i + 1], d[i + 2], 1);
  }
}

const rows = [];
IDS.forEach((id, j) => {
  const img = decodePNG(path.join(SRC, id + '.png'));
  const small = new Canvas(16, 16);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const i = ((y * 2) * 32 + x * 2) * 4;
    if (img.data[i + 3] > 0) small.set(x, y, img.data[i], img.data[i + 1], img.data[i + 2], 1);
  }
  let op = 0;
  for (let i = 3; i < img.data.length; i += 4) if (img.data[i] > 0) op++;
  rows.push(`  ${id.padEnd(20)} ${img.w}x${img.h}  非透明 ${(op / (img.w * img.h) * 100).toFixed(1)}%`);
  const ox = PAD + j * (cellW + 0);
  blit(img, ox, PAD, K);
  blit(small, ox, PAD + 32 * K + GAP, K16);
});
cv.save(OUT);
console.log('交付贴图自检(1:1 观感):');
rows.forEach(r => console.log(r));
console.log('预览: ' + OUT + '  上=32x32@12x  下=16x16@24x');
