/* 苹果爵士掉落物图标(2026-09-12): 16x16 平面图标
 *   1) 苹果核 apple_core  —— 材料
 *   2) 果梗   apple_stem  —— 武器(BossMercy 掉落)
 * 教训沿用: 一律平面图标(item/generated | item/handheld), 不做 3D 模型, 免得再出"图集当图标"的问题。
 * 用法: node gen_apple_drop_icons.cjs <assetsDir>
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const A = process.argv[2];

const PAL = {
    K: [142, 26, 30], R: [206, 58, 54], W: [244, 240, 226], w: [216, 208, 186],
    d: [58, 40, 26], s: [104, 66, 38], t: [126, 82, 48], B: [84, 52, 30],
    l: [96, 168, 74], L: [150, 206, 110], g: [226, 186, 72], G: [252, 224, 140],
    '.': null,
};

const ART = {
    /* 苹果核: 咬过的果核 + 顶部果梗 + 两颗籽 */
    apple_core: [
        '................',
        '.......ss.......',
        '......ss........',
        '.....KKKK.......',
        '....KRRRRK......',
        '....KRWWRK......',
        '....KWWWWK......',
        '...KWdWWdWK.....',
        '...KWWWWWWK.....',
        '...KWWwwWWK.....',
        '....KWWWWK......',
        '....KWwwWK......',
        '....KRRRRK......',
        '.....KKKK.......',
        '................',
        '................',
    ],
    /* 果梗武器: 斜向木质果梗 + 叶片 + 握把金环 */
    apple_stem: [
        '..........tt....',
        '.........ttl....',
        '........ttlLL...',
        '.......ttlLLL...',
        '......ttllL.....',
        '.....ttt........',
        '....ttt.........',
        '...ttt..........',
        '..ttt...........',
        '..tt............',
        '..tt............',
        '..gg............',
        '..gG............',
        '..tt............',
        '..tt............',
        '..BB............',
    ],
};

const TS = 16;
for (const name in ART) {
    const cv = new Canvas(TS, TS);
    const rows = ART[name];
    let n = 0;
    for (let y = 0; y < TS; y++) {
        for (let x = 0; x < TS; x++) {
            const c = PAL[rows[y][x]];
            if (!c) continue;
            cv.set(x, y, c[0], c[1], c[2], 1);
            n++;
        }
    }
    const out = path.join(A, 'textures/item/' + name + '.png');
    cv.save(out);
    console.log(`  ${name}.png  ${TS}x${TS}  ${n} px`);
}
