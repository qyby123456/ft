/* 【暗影笼罩】状态效果图标(18x18): 一只被暗影吞掉的眼睛。用法: node tools/model3d/gen_shadow_blind_icon.cjs <outPng> */
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const cv = new Canvas(W, H);
const RIM = [92, 60, 132];
const DARK = [18, 10, 28];
const GLOW = [150, 90, 210];

/* 眼睛外形: 上下两条弧夹出杏仁形 */
for (let x = 2; x < 16; x++) {
    const t = (x - 2) / 13;                       // 0..1
    const half = Math.sin(t * Math.PI) * 5.0;     // 中间最开
    const cy = 9;
    for (let y = Math.floor(cy - half); y <= Math.ceil(cy + half); y++) {
        if (y < 0 || y >= H) continue;
        const d = Math.abs(y + 0.5 - cy);
        const edge = d > half - 1.2;
        const c = edge ? RIM : DARK;
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}
/* 瞳孔: 被暗影糊掉的一小团(带一点紫光) */
for (let y = 7; y <= 11; y++) {
    for (let x = 7; x <= 10; x++) {
        const d = Math.hypot(x - 8.5, y - 9);
        if (d > 2.4) continue;
        const c = d < 1.2 ? GLOW : DARK;
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}
const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out);
