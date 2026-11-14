/* 【虚空侵蚀】状态效果图标(18x18, 原版 mob_effect 尺寸) —— 2026-09-14 v118。
   一个紫黑色的**漩涡**(虚空侵蚀): 外圈深紫描边 + 螺旋亮紫带 + 中心亮芯。
   用法: node tools/model3d/gen_shadow_erosion_icon.cjs <outPng> */
const path = require('path');
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const cv = new Canvas(W, H);
const OUTER = [52, 16, 78];      // 深紫(盘面)
const RING = [118, 46, 190];     // 主紫
const GLOW = [198, 122, 255];    // 亮紫(旋臂)
const CORE = [242, 226, 255];    // 中心亮芯
const cx = (W - 1) / 2, cy = (H - 1) / 2;

for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
        const dx = x + 0.5 - cx, dy = y + 0.5 - cy;
        const r = Math.hypot(dx, dy);
        if (r > 8.4) continue;                       // 圆外留透明
        if (r > 7.3) { cv.set(x, y, OUTER[0], OUTER[1], OUTER[2], 1); continue; }   // 描边
        if (r <= 1.7) { cv.set(x, y, CORE[0], CORE[1], CORE[2], 1); continue; }     // 亮芯
        const ang = Math.atan2(dy, dx);
        const band = Math.abs(Math.sin(r * 1.35 + ang * 2.2));                       // 旋臂
        const c = band > 0.72 ? GLOW : (band > 0.34 ? RING : OUTER);
        cv.set(x, y, c[0], c[1], c[2], 1);
    }
}

const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out + ' (' + W + 'x' + H + ')  -> ' + path.basename(out));
