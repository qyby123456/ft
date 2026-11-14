// 生成"试听唱片"的 16x16 贴图(2026-09-13 v101)
// 画一张黑胶: 深色盘面 + 细纹 + 暗红标签 + 金色中孔(呼应"甜苹果庄园 / 黑暗奇幻")
const fs = require('fs');
const path = require('path');
const { Canvas, encodePNG } = require('../model3d/png.cjs');

const OUT = path.join(__dirname, '..', '..', 'src', 'main', 'resources', 'assets', 'ftboss',
    'textures', 'item', 'music_disc_segment_01.png');

const S = 16;
const cv = new Canvas(S, S);
// 透明底
const px = (x, y, r, g, b, a = 1) => cv.set(x, y, r, g, b, a);

const cx = (S - 1) / 2, cy = (S - 1) / 2;
for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
        const d = Math.hypot(x - cx, y - cy);
        if (d > 7.6) continue;                       // 圆盘外留透明(略带方角, 像素风)
        if (d > 6.9) { px(x, y, 26, 26, 30); continue; }        // 边缘暗环
        if (d <= 1.6) { px(x, y, 232, 186, 74); continue; }     // 金色中孔
        if (d <= 3.6) {                                          // 暗红标签
            const band = Math.sin((x + y) * 0.9) > 0.55 ? 12 : 0;
            px(x, y, 146 + band, 30, 42); continue;
        }
        // 盘面纹路: 每 2 像素一圈深浅
        const ring = Math.floor(d * 2) % 2 === 0;
        // 斜向高光(模拟反光)
        const gloss = (x - y) > 6 && d > 4.2 ? 26 : 0;
        px(x, y, 22 + (ring ? 8 : 0) + gloss, 22 + (ring ? 8 : 0) + gloss, 27 + (ring ? 9 : 0) + gloss);
    }
}
fs.writeFileSync(OUT, encodePNG(S, S, cv.d));
console.log(`已写出 ${OUT}  (16x16)`);
