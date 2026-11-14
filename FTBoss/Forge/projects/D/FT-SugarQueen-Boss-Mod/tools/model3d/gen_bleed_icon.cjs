/* 【流血】状态效果图标(18x18): 一滴血 + 下方细流。用法: node tools/model3d/gen_bleed_icon.cjs <outPng> */
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const cv = new Canvas(W, H);
const EDGE = [70, 8, 12];
const BODY = [168, 26, 32];
const SHINE = [236, 128, 132];

/* 血滴: 上尖下圆的水滴形 */
for (let y = 1; y < 14; y++) {
    const t = (y - 1) / 12;
    const half = 1.0 + t * 4.4;                 // 越往下越宽
    const cx = 9;
    for (let x = Math.floor(cx - half); x <= Math.ceil(cx + half); x++) {
        if (x < 0 || x >= W) continue;
        const d = Math.abs(x + 0.5 - cx);
        let c = d > half - 1.0 ? EDGE : BODY;
        if (d < half * 0.35 && y > 4) c = SHINE;   // 左上高光
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}
/* 底下两滴小的(血在滴) */
cv.set(7, 15, BODY[0], BODY[1], BODY[2], 1);
cv.set(8, 15, SHINE[0], SHINE[1], SHINE[2], 1);
cv.set(11, 16, BODY[0], BODY[1], BODY[2], 1);

const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out);
