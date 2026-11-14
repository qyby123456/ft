/* 糖果拐杖物品贴图(2026-09-12): 16x16 平面手持图标
 * 背景: 原来的 candy_cane 是个**坏掉的 3D 模型**(弯钩方块散到杆子两侧) + 一张 32x32 的"模型图集",
 *       在手里/物品栏都是错的。改成标准 item/handheld 平面图标, 和原版剑一致。
 * 画法: 先按掩码填出拐杖形状 -> 按 (x+y) 上对角条纹(红/奶油) -> 自动描边(邻接透明的像素变深红)
 * 用法: node gen_candy_cane_icon.cjs <outPng>
 */
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2];

const MASK = [
    '................',
    '.....MMMMM......',
    '....MMMMMMM.....',
    '...MMM...MMM....',
    '...MM.....MM....',
    '...MM.....MM....',
    '...MMM...MMM....',
    '....MMMMMMM.....',
    '......MMMMM.....',
    '.......MMMM.....',
    '.......MMMM.....',
    '.......MMMM.....',
    '.......MMMM.....',
    '.......MMMM.....',
    '.......MMMM.....',
    '........MM......',
];
const W = 16, H = 16;
const RED = [228, 62, 62], CREAM = [247, 241, 228];
const filled = (x, y) => y >= 0 && y < H && x >= 0 && x < W && MASK[y][x] === 'M';

const cv = new Canvas(W, H);
for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
        if (!filled(x, y)) continue;
        /* 对角条纹: 红/奶油交替 */
        const base = ((x + y) % 4 < 2) ? RED : CREAM;
        /* 右下相邻为空的像素压暗一档(代替描边, 免得 4px 宽的拐杖被描边吃掉花纹) */
        const shade = !filled(x + 1, y) || !filled(x, y + 1);
        const k = shade ? 0.74 : 1;
        cv.set(x, y, Math.round(base[0] * k), Math.round(base[1] * k), Math.round(base[2] * k), 1);
    }
}
cv.save(OUT);
console.log(`糖果拐杖图标已生成: ${OUT} (${W}x${H}, 对角条纹+右下阴影)`);
