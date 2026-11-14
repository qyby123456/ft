/* 【充能】状态效果图标(18x18): 一道电光青的闪电。用法: node tools/model3d/gen_qingyi_charge_icon.cjs <outPng> */
const path = require('path');
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const cv = new Canvas(W, H);
const EDGE = [12, 62, 92];
const BODY = [47, 216, 230];
const CORE = [225, 255, 255];

/* 闪电: 三段折线, 上宽下尖 */
const pts = [];
for (let y = 1; y < H - 1; y++) {
    const t = y / (H - 1);
    const cx = 9 + (t < 0.45 ? -2.4 : (t < 0.7 ? 2.2 : -1.2));
    const half = 2.6 - t * 1.4;
    pts.push({ y, cx, half });
}
for (const { y, cx, half } of pts) {
    for (let x = Math.floor(cx - half); x <= Math.ceil(cx + half); x++) {
        if (x < 0 || x >= W) continue;
        const d = Math.abs(x + 0.5 - cx);
        const c = d > half - 0.9 ? EDGE : (d < half * 0.4 ? CORE : BODY);
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}
const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out + ' -> ' + path.basename(out));
