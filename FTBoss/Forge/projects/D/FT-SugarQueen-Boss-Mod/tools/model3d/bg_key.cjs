/* bg_key.cjs —— 把预览图里"和模型深色材料撞色"的背景换成高对比色，便于肉眼判读。
 *
 * 为什么需要它：本工程的武器贴图主体是**暗铁**（约 42,37,43），而
 * render_tex.cjs 的预览底色也很暗 —— "轮廓上破了个洞（露出背景）"和"实心暗铁"
 * 在图上几乎同色，**看图根本分不出来**。把背景换成洋红之后，洞就是一眼可见的洋红块。
 *
 * 用法: node tools/model3d/bg_key.cjs <in.png> <out.png> [背景色hex(默认取左上角像素)] [替换色hex(默认FF00FF)]
 */
'use strict';

const fs = require('fs');
const { decodePNG, encodePNG } = require('./png.cjs');

const [inFile, outFile, fromHex, toHex] = process.argv.slice(2);

if (!inFile || !outFile) {
  console.error('用法: node bg_key.cjs <in.png> <out.png> [fromHex] [toHex]');
  process.exit(1);
}

/* png.cjs 的 decodePNG 返回 { w, h, data }（不是 width/height）。 */
const { w: width, h: height, data } = decodePNG(inFile);

const at = (x, y) => {
  const i = (y * width + x) * 4;
  return [data[i], data[i + 1], data[i + 2]];
};

const parseHex = (hex) => {
  const h = hex.replace('#', '');
  return [parseInt(h.slice(0, 2), 16), parseInt(h.slice(2, 4), 16), parseInt(h.slice(4, 6), 16)];
};

/* 默认把左上角像素当作背景色（预览图四角一定是底色）。 */
const from = fromHex ? parseHex(fromHex) : at(0, 0);
const to = toHex ? parseHex(toHex) : [255, 0, 255];

let hit = 0;
for (let i = 0; i < data.length; i += 4) {
  if (data[i] === from[0] && data[i + 1] === from[1] && data[i + 2] === from[2]) {
    data[i] = to[0];
    data[i + 1] = to[1];
    data[i + 2] = to[2];
    hit++;
  }
}

fs.writeFileSync(outFile, encodePNG(width, height, data));
console.log(`[bg_key] ${inFile} -> ${outFile}  背景 ${from.join(',')} -> ${to.join(',')}  替换 ${hit} 像素`);
