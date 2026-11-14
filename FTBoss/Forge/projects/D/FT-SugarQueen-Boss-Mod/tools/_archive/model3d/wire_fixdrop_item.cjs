/*
 * 2026-09-12
 *  ① 去掉炫彩肥料掉落(移除 die() 里的 dropFertilizer 调用; 物品本身保留)
 *  ② 修召唤物贴图: compost_summon / rainbow_fertilizer 之前**没有物品模型与贴图** -> 现补 16x16 程序生成贴图 + item/generated 模型
 *  ③ 大便小怪贴图图集溢出: 64x64 装不下 11 个方块 -> 改 128x128 并重生成
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = 'src/main/resources/assets/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
const { Canvas } = require(path.join(ROOT, 'tools/model3d/png.cjs'));

/* ① 去肥料掉落 */
{
    const rel = `${J}/entity/CompostBossEntity.java`;
    let s = read(rel);
    if (s.includes('this.dropFertilizer();')) {
        s = s.replace('        this.dropFertilizer();', '        // 2026-09-12 按用户要求: 去除炫彩肥料掉落(物品保留, 只是不再掉)');
        write(rel, s);
        console.log('  ① 已移除炫彩肥料掉落调用');
    } else console.log('  ① 未找到掉落调用(可能已移除)');
}

/* ② 两个物品的贴图 + 模型 */
function icon(draw) {
    const cv = new Canvas(16, 16);
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) cv.set(x, y, 0, 0, 0, 0);
    draw(cv);
    return cv;
}
const px = (cv, x, y, c) => cv.set(x, y, c[0], c[1], c[2], 1);
/* 炫彩堆肥桶: 灰桶 + 褐色内容 + 绿芽 + 彩虹条 */
const bucket = icon(cv => {
    for (let y = 4; y < 15; y++) for (let x = 4; x < 12; x++) px(cv, x, y, y < 6 ? [168, 172, 180] : [150, 108, 62]);
    for (let x = 4; x < 12; x++) { px(cv, x, 6, [96, 66, 38]); px(cv, x, 7, [212, 180, 104]); }
    for (let x = 5; x < 11; x++) { px(cv, x, 5, [122, 214, 110]); px(cv, x, 4, [150, 234, 130]); }
    for (let y = 9; y < 12; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [232, 74, 190]);
    for (let y = 12; y < 15; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [86, 226, 232]);
    for (let y = 4; y < 15; y++) { px(cv, 3, y, [110, 116, 126]); px(cv, 12, y, [110, 116, 126]); }
    for (let x = 3; x < 13; x++) { px(cv, x, 3, [176, 182, 190]); px(cv, x, 15, [110, 116, 126]); }
});
/* 炫彩肥料: 麻袋 + 绿顶 + 彩虹条 */
const fert = icon(cv => {
    for (let y = 5; y < 15; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [150, 124, 86]);
    for (let x = 5; x < 11; x++) { px(cv, x, 4, [122, 214, 110]); px(cv, x, 5, [96, 178, 88]); }
    for (let y = 8; y < 10; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [246, 222, 96]);
    for (let y = 10; y < 12; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [86, 226, 232]);
    for (let y = 12; y < 15; y++) for (let x = 4; x < 12; x++) px(cv, x, y, [232, 74, 190]);
    px(cv, 6, 7, [80, 52, 30]); px(cv, 7, 7, [80, 52, 30]); px(cv, 8, 7, [80, 52, 30]); px(cv, 9, 7, [80, 52, 30]);
});
bucket.save(path.join(ROOT, A, 'textures/item/compost_summon.png'));
fert.save(path.join(ROOT, A, 'textures/item/rainbow_fertilizer.png'));
for (const [id, tex] of [['compost_summon', 'compost_summon'], ['rainbow_fertilizer', 'rainbow_fertilizer']]) {
    write(`${A}/models/item/${id}.json`, JSON.stringify({ parent: 'item/generated', textures: { layer0: `ftboss:item/${tex}` } }, null, 2));
}
console.log('  ② 已生成 compost_summon / rainbow_fertilizer 的 16x16 贴图 + item/generated 模型');

/* ③ 大便小怪图集 64 -> 128 */
{
    const rel = 'tools/model3d/gen_poop_minion.cjs';
    let s = read(rel);
    if (s.includes('const W=64,H=64;')) {
        s = s.replace('const W=64,H=64;', 'const W=128,H=128;   // 2026-09-12: 64 装不下 11 个方块 -> 128');
        write(rel, s);
        console.log('  ③ 小怪图集改为 128x128');
    } else console.log('  ③ 图集尺寸未命中(可能已改)');
    const r = spawnSync('node', ['gen_poop_minion.cjs', 'out_poop_minion'], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log('     重生成退出码: ' + r.status);
    for (const [a, b] of [['poop_minion.geo.json', 'geo/poop_minion.geo.json'], ['poop_minion.png', 'textures/entity/poop_minion.png'], ['poop_minion.animation.json', 'animations/poop_minion.animation.json']]) {
        const src = path.join(ROOT, 'tools/model3d/out_poop_minion', a);
        if (fs.existsSync(src)) { fs.copyFileSync(src, path.join(ROOT, A, b)); }
    }
    console.log('     已同步 geo/贴图/动画');
}
console.log('完成');
