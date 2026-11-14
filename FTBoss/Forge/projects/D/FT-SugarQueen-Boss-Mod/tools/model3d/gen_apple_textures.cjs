/* 苹果爵士系列贴图(2026-09-12): 复用现有 geo/anim, 只重画贴图
 *   1) 果虫 apple_worm     <- maggot_minion.png  (苹果果肉色: 奶白+淡青)
 *   2) 果蛾 apple_moth     <- fly_minion.png     (奶油色翅膀+棕褐身体)
 *   3) 烂苹果 rotten_apple <- poop_minion.png    (腐烂红褐+暗斑)
 * 规则: 保留原图的形状/明暗/透明度, 只把颜色按亮度映射到新色带(和之前岩浆怪/史莱姆换皮同一套做法)
 * 用法: node gen_apple_textures.cjs <resourcesRoot>
 */
const fs = require('fs');
const path = require('path');
const { decodePNG, Canvas } = require('./png.cjs');
const RES = process.argv[2];
const TEX = path.join(RES, 'assets/ftboss/textures/entity');

/** 按亮度把 src 重新映射到 [dark, light] 色带; alpha 原样保留 */
function recolor(src, out, dark, light, span) {
    const img = decodePNG(src);
    const cv = new Canvas(img.w, img.h);
    const LO = span ? span[0] : 0, HI = span ? span[1] : 255;
    for (let y = 0; y < img.h; y++) {
        for (let x = 0; x < img.w; x++) {
            const i = (y * img.w + x) * 4;
            const a = img.data[i + 3];
            if (a === 0) continue;
            const bright = Math.max(img.data[i], img.data[i + 1], img.data[i + 2]);
            const t = Math.max(0, Math.min(1, (bright - LO) / Math.max(1, HI - LO)));
            cv.set(x, y,
                Math.round(dark[0] + (light[0] - dark[0]) * t),
                Math.round(dark[1] + (light[1] - dark[1]) * t),
                Math.round(dark[2] + (light[2] - dark[2]) * t), a / 255);
        }
    }
    cv.save(out);
    return `${path.basename(out)}  ${img.w}x${img.h}`;
}

const jobs = [
    ['maggot_minion.png', 'apple_worm.png', [138, 112, 74], [252, 248, 226], [40, 220]],      // 果肉奶白
    ['fly_minion.png', 'apple_moth.png', [64, 48, 36], [236, 220, 186], [24, 200]],            // 奶油蛾
    ['poop_minion.png', 'rotten_apple.png', [72, 40, 28], [182, 100, 62], [30, 190]],          // 腐烂红褐
    ['poop_minion.png', 'green_apple.png', [46, 84, 34], [176, 220, 112], [30, 190]],          // 青苹果(2026-09-12)
    ['gingerbread_minion.png', 'coin_man.png', [118, 84, 18], [252, 228, 124], [24, 200]],      // 金币人(2026-09-12)
];
for (const [s, o, d, l, span] of jobs) {
    const src = path.join(TEX, s);
    if (!fs.existsSync(src)) { console.log('  skip ' + s + ' (不存在)'); continue; }
    console.log('  ' + recolor(src, path.join(TEX, o), d, l, span));
}
console.log('苹果系列召唤物贴图完成');
