// 贴图放大自检: node zoom_tex.cjs <in.png> <out.png> [倍数]
// 用最近邻放大, 用来**肉眼核对** box-UV 各材质的实际像素(尤其五官画在哪个面)。
const { Canvas, decodePNG } = require('./png.cjs');
const img = decodePNG(process.argv[2]);
const S = parseInt(process.argv[4] || '8', 10);
const out = new Canvas(img.w * S, img.h * S);
for (let y = 0; y < img.h; y++) for (let x = 0; x < img.w; x++) {
  const i = (y * img.w + x) * 4;
  out.rect(x * S, y * S, S, S, img.data[i], img.data[i + 1], img.data[i + 2], img.data[i + 3] / 255);
}
out.save(process.argv[3]);
console.log(`已写出 ${process.argv[3]}  ${img.w}x${img.h} -> ${img.w * S}x${img.h * S}`);
