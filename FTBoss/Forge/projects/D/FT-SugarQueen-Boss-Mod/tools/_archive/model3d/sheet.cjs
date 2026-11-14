// 贴图拼版自检: node sheet.cjs <out.png> <每格像素> <in1.png> <in2.png> ...
// 把多张贴图(方块/物品图标)最近邻放大后排成一行/多行, 便于一次看清整套外观。
const { Canvas, decodePNG } = require('./png.cjs');
const out = process.argv[2];
const CELL = parseInt(process.argv[3] || '128', 10);
const files = process.argv.slice(4);
const PAD = 6;
const cols = Math.min(files.length, 5);
const rows = Math.ceil(files.length / cols);
const cv = new Canvas(cols * (CELL + PAD) + PAD, rows * (CELL + PAD) + PAD);
cv.rect(0, 0, cv.w, cv.h, 26, 26, 30, 1);
files.forEach((f, i) => {
  const img = decodePNG(f);
  const k = Math.max(1, Math.floor(CELL / Math.max(img.w, img.h)));
  const ox = PAD + (i % cols) * (CELL + PAD), oy = PAD + Math.floor(i / cols) * (CELL + PAD);
  // 棋盘底(看透明区域)
  for (let y = 0; y < CELL; y += 8) for (let x = 0; x < CELL; x += 8) {
    const c = ((x / 8 + y / 8) % 2) ? 46 : 34;
    cv.rect(ox + x, oy + y, 8, 8, c, c, c + 4, 1);
  }
  for (let y = 0; y < img.h; y++) for (let x = 0; x < img.w; x++) {
    const i4 = (y * img.w + x) * 4;
    if (img.data[i4 + 3] === 0) continue;
    cv.rect(ox + x * k, oy + y * k, k, k, img.data[i4], img.data[i4 + 1], img.data[i4 + 2], img.data[i4 + 3] / 255);
  }
});
cv.save(out);
console.log(`已写出 ${out}  ${cols}x${rows} 格, 每格 ${CELL}px, 共 ${files.length} 张`);
