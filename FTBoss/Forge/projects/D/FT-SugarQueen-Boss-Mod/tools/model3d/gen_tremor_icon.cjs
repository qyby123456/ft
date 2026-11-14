/* 【震颤】状态效果图标(18x18): 地震裂纹。用法: node tools/model3d/gen_tremor_icon.cjs <outPng> */
const { Canvas } = require('./png.cjs');
const W = 18, H = 18;
const cv = new Canvas(W, H);
const DARK = [58, 40, 12];
const BODY = [150, 112, 40];
const HI = [226, 190, 96];

/* 底色: 一块土黄的地面 */
for (let y = 8; y < H; y++) for (let x = 0; x < W; x++) {
  const f = 0.92 + ((x * 7 + y * 13) % 11) / 60;
  const c = (y > H - 4) ? [BODY[0] * 0.8, BODY[1] * 0.8, BODY[2] * 0.8] : BODY;
  cv.set(x, y, c[0] * f, c[1] * f, c[2] * f, 1);
}
/* 裂纹: 一条之字形闪电缝 + 两条支缝 */
const zig = [[3, 8], [5, 10], [4, 12], [7, 14], [6, 16], [9, 17]];
for (const [x, y] of zig) { for (let w = 0; w < 2; w++) cv.set(x + w, y, DARK[0], DARK[1], DARK[2], 1); }
for (const [x, y] of [[11, 10], [12, 12], [13, 14]]) { cv.set(x, y, DARK[0], DARK[1], DARK[2], 1); cv.set(x + 1, y + 1, DARK[0], DARK[1], DARK[2], 1); }
/* 高光: 裂缝上沿的亮边 */
for (const [x, y] of zig) cv.set(x, y - 1, HI[0], HI[1], HI[2], 1);
/* 顶上两条"震动"弧线 */
for (const [x, y] of [[2, 3], [4, 1], [8, 1], [12, 1], [15, 3], [16, 6]]) cv.set(x, y, HI[0], HI[1], HI[2], 1);

cv.save(process.argv[2]);
console.log('wrote ' + process.argv[2]);
