/* 【庄园·鏖战】唱片贴图(16x16): 深色黑胶 + 绯红金标签。
   用法: node tools/model3d/gen_disc_manor_guignol.cjs <outPng> */
const { Canvas } = require('./png.cjs');

const S = 16;
const cv = new Canvas(S, S);
const cx = 7.5, cy = 7.5;

const VINYL_D = [34, 30, 34];
const VINYL_L = [58, 52, 58];
const LABEL = [176, 40, 40];
const LABEL_HI = [214, 92, 62];
const HOLE = [12, 10, 12];

for (let y = 0; y < S; y++) {
    for (let x = 0; x < S; x++) {
        const d = Math.hypot(x - cx, y - cy);
        if (d > 7.4) continue;                       // 圆外的透明
        let c;
        if (d > 7.0) c = [22, 20, 24];                // 外圈描边
        else if (d > 4.2) {
            /* 纹路: 两圈一亮一暗 */
            const ring = Math.floor(d * 1.6) % 2 === 0;
            c = ring ? VINYL_L : VINYL_D;
        } else if (d > 1.6) c = ((x + y) % 3 === 0) ? LABEL_HI : LABEL;   // 标签
        else c = HOLE;                                 // 中心孔
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}
/* 左上一点高光, 让它看得出是"片"而不是"饼" */
cv.set(4, 3, 210, 200, 200, 1);
cv.set(5, 3, 150, 142, 146, 1);
cv.set(3, 4, 150, 142, 146, 1);

const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out);
