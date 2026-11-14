/* 羊屎蛋贴图(2026-09-12): 用**原版岩浆怪** magmacube.png 的形状与明暗, 换成暗棕黑(羊粪蛋)。
   原版色域很窄(红通道 16~51 的暗红), 所以先把亮度按 16~51 归一化再映射到棕黑渐变,
   否则整颗会糊成一团黑。alpha 原样保留。
   用法: node gen_sheep_dung_tex.cjs <vanillaMagmacubePng> <outPng> */
const { decodePNG, Canvas } = require('./png.cjs');

const img = decodePNG(process.argv[2]);
const out = process.argv[3];
const cv = new Canvas(img.w, img.h);

const LO = 16, HI = 51;                  // 原版岩浆怪的亮度区间
const DARK = [30, 24, 19];               // 阴影
const LIGHT = [122, 98, 64];             // 高光(偏干的草料黄棕)

for (let y = 0; y < img.h; y++) {
  for (let x = 0; x < img.w; x++) {
    const i = (y * img.w + x) * 4;
    const a = img.data[i + 3];
    if (a === 0) continue;
    const bright = Math.max(img.data[i], img.data[i + 1], img.data[i + 2]);
    const t = Math.max(0, Math.min(1, (bright - LO) / (HI - LO)));
    cv.set(x, y,
      Math.round(DARK[0] + (LIGHT[0] - DARK[0]) * t),
      Math.round(DARK[1] + (LIGHT[1] - DARK[1]) * t),
      Math.round(DARK[2] + (LIGHT[2] - DARK[2]) * t), a / 255);
  }
}
cv.save(out);
console.log('羊屎蛋贴图已生成: ' + out + ' (' + img.w + 'x' + img.h + ')');
