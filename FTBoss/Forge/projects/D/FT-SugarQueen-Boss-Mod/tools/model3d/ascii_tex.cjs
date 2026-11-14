// 把一张小贴图打成 ASCII 图, 用来肉眼核对"图标到底画成了什么"(改完美术出图自查用)
// 用法: node ascii_tex.cjs <in.png> [每字符覆盖几像素=1]
const { decodePNG } = require('./png.cjs');

const file = process.argv[2];
const img = decodePNG(file);
const W = img.w || img.width, H = img.h || img.height, data = img.data || img.pixels;
console.log(`${file}  ${W}x${H}`);
const ch = ' .:-=+*#%@';
for (let y = 0; y < H; y++) {
  let line = '';
  for (let x = 0; x < W; x++) {
    const i = (y * W + x) * 4;
    const a = data[i + 3];
    if (a < 16) { line += ' '; continue; }
    const lum = (0.299 * data[i] + 0.587 * data[i + 1] + 0.114 * data[i + 2]) / 255;
    line += ch[Math.max(0, Math.min(ch.length - 1, Math.round(lum * (ch.length - 1))))];
  }
  console.log(String(y).padStart(2) + '|' + line + '|');
}
