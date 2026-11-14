/* v119 三个新状态效果的 18x18 图标(原版 mob_effect 尺寸): 禁疗 / 血怒 / 蓝火。
   用法: node tools/model3d/gen_v119_icons.cjs <输出目录> */
const path = require('path');
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const DIR = process.argv[2];

function flame(cv, cols) {
    const cx = (W - 1) / 2;
    for (let y = 0; y < H; y++) {
        const t = y / (H - 1);                       // 0 顶 -> 1 底
        const half = 1.6 + Math.sin(Math.PI * Math.min(1, t * 1.15)) * 5.4;   // 上尖下宽
        for (let x = 0; x < W; x++) {
            const dx = Math.abs(x + 0.5 - cx);
            if (dx > half) continue;
            const edge = dx > half - 1.2;
            const inner = dx < half * 0.42 && t > 0.45;
            const c = edge ? cols.edge : (inner ? cols.core : cols.body);
            cv.set(x, y, c[0], c[1], c[2], 1);
        }
    }
}

function heartBan(cv) {
    const cx = (W - 1) / 2;
    for (let y = 0; y < H; y++) {
        for (let x = 0; x < W; x++) {
            const dx = (x + 0.5 - cx) / 6.6;
            const dy = (y + 0.5 - 7.4) / 6.4;
            /* 心形隐式方程 */
            const f = Math.pow(dx * dx + dy * dy - 1, 3) - dx * dx * dy * dy * dy;
            if (f > 0) continue;
            cv.set(x, y, 168, 26, 40, 1);
        }
    }
    /* 斜杠(禁) */
    for (let i = 0; i < 14; i++) {
        const x = 3 + i, y = 14 - i;
        if (x < 0 || y < 0 || x >= W || y >= H) continue;
        cv.set(x, y, 245, 240, 240, 1);
        if (x + 1 < W) cv.set(x + 1, y, 60, 12, 20, 1);
    }
}

const jobs = [
    ['heal_ban.png', (cv) => heartBan(cv)],
    ['blood_rage.png', (cv) => flame(cv, { edge: [120, 10, 20], body: [196, 32, 40], core: [252, 196, 96] })],
    ['blue_fire.png', (cv) => flame(cv, { edge: [16, 48, 120], body: [40, 120, 230], core: [190, 240, 255] })],
];

for (const [name, draw] of jobs) {
    const cv = new Canvas(W, H);
    draw(cv);
    const out = path.join(DIR, name);
    cv.save(out);
    console.log('wrote ' + out);
}
