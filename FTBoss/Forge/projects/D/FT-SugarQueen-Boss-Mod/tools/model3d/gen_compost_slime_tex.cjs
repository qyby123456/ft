/* 棕黑色史莱姆贴图(2026-09-12): 拿原版 slime.png 的**形状/明暗/透明度**, 把色相换成棕黑。
   规则: 保留每个像素的 alpha 与亮度, 只把颜色映射到棕黑渐变(深 40,32,26 -> 亮 122,98,74)。
   用法: node gen_compost_slime_tex.cjs <vanillaSlimePng> <outPng> */
const fs = require('fs');
const { decodePNG, Canvas } = require('./png.cjs');

const src = process.argv[2];
const out = process.argv[3];
const img = decodePNG(src);
const cv = new Canvas(img.w, img.h);

/* 棕黑渐变锚点(按亮度 0..255 线性插值) */
const DARK = [34, 27, 22];
const LIGHT = [126, 102, 78];

for (let y = 0; y < img.h; y++) {
  for (let x = 0; x < img.w; x++) {
    const i = (y * img.w + x) * 4;
    const a = img.data[i + 3];
    if (a === 0) continue;
    const r = img.data[i], g = img.data[i + 1], b = img.data[i + 2];
    const luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255;      // 0..1
    const t = Math.max(0, Math.min(1, luma));
    cv.set(x, y,
      Math.round(DARK[0] + (LIGHT[0] - DARK[0]) * t),
      Math.round(DARK[1] + (LIGHT[1] - DARK[1]) * t),
      Math.round(DARK[2] + (LIGHT[2] - DARK[2]) * t), a / 255);
  }
}
cv.save(out);
console.log('棕黑史莱姆贴图已生成: ' + out + ' (' + img.w + 'x' + img.h + ')');
