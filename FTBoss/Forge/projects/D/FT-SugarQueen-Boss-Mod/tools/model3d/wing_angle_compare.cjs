/* wing_angle_compare.cjs —— 把多张 render_tex 四视图的"正面"面板裁出来横向拼成一张对照图
 *
 * 用途：刃翼展开角度要调的时候，一次看清 0/8/20/35° 的差别，别靠脑补。
 * 只读 + 写一张拼图，不改模型。
 *
 * 用法:
 *   node tools/model3d/wing_angle_compare.cjs <out.png> <panelPx> <in1.png> <label1> <in2.png> <label2> ...
 */
'use strict';

const fs = require('fs');
const { decodePNG, encodePNG } = require('./png.cjs');

const [out, panelArg, ...rest] = process.argv.slice(2);
const PANEL = Number(panelArg);
if (!out || !Number.isFinite(PANEL)) {
  console.error('用法: node wing_angle_compare.cjs <out.png> <panelPx> <in.png> <label> ...');
  process.exit(2);
}

const items = [];
for (let i = 0; i < rest.length; i += 2) items.push({ file: rest[i], label: rest[i + 1] || '' });

/* 5x7 点阵，只够画数字和 ° */
const GLYPH = {
  '0': ['01110', '10001', '10001', '10001', '01110'],
  '1': ['00100', '01100', '00100', '00100', '01110'],
  '2': ['11110', '00001', '01110', '10000', '11111'],
  '3': ['11110', '00001', '01110', '00001', '11110'],
  '4': ['10010', '10010', '11111', '00010', '00010'],
  '5': ['11111', '10000', '11110', '00001', '11110'],
  '6': ['01110', '10000', '11110', '10001', '01110'],
  '7': ['11111', '00001', '00010', '00100', '00100'],
  '8': ['01110', '10001', '01110', '10001', '01110'],
  '9': ['01110', '10001', '01111', '00001', '01110'],
  '°': ['11000', '11000', '00000', '00000', '00000'],
  ' ': ['00000', '00000', '00000', '00000', '00000']
};

const LABEL_H = 34;
const W = PANEL * items.length;
const H = PANEL + LABEL_H;
const outBuf = new Uint8Array(W * H * 4);
for (let i = 0; i < W * H; i++) { outBuf[i * 4] = 26; outBuf[i * 4 + 1] = 28; outBuf[i * 4 + 2] = 32; outBuf[i * 4 + 3] = 255; }

const put = (x, y, r, g, b) => {
  if (x < 0 || y < 0 || x >= W || y >= H) return;
  const i = (y * W + x) * 4;
  outBuf[i] = r; outBuf[i + 1] = g; outBuf[i + 2] = b; outBuf[i + 3] = 255;
};

items.forEach((it, idx) => {
  const { w, h, data } = decodePNG(it.file);
  const ox = idx * PANEL;
  /* render_tex 的正面视图在左上角 */
  for (let y = 0; y < PANEL && y < h; y++) {
    for (let x = 0; x < PANEL && x < w; x++) {
      const si = (y * w + x) * 4;
      put(ox + x, y, data[si], data[si + 1], data[si + 2]);
    }
  }
  /* 标签：白字 + 底部分隔线 */
  let lx = ox + 8;
  for (const ch of it.label) {
    const g = GLYPH[ch] || GLYPH[' '];
    for (let gy = 0; gy < g.length; gy++) {
      for (let gx = 0; gx < g[0].length; gx++) {
        if (g[gy][gx] === '1') {
          for (let sy = 0; sy < 3; sy++) for (let sx = 0; sx < 3; sx++) put(lx + gx * 3 + sx, PANEL + 10 + gy * 3 + sy, 240, 240, 240);
        }
      }
    }
    lx += 6 * 3;
  }
  for (let x = ox; x < ox + PANEL; x++) put(x, PANEL + 2, 90, 90, 100);
});

fs.writeFileSync(out, encodePNG(W, H, outBuf));
console.log('写出 ' + out + '  ' + W + 'x' + H + '（' + items.length + ' 个角度并排）');
