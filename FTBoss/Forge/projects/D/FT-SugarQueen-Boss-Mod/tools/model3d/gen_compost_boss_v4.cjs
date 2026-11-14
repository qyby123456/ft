/*
 * 炫彩答辩超人 —— 生成器 v4 (2026-09-12 重写, 干净版)
 *   · 不含加农炮方块: cannon 骨骼保留为空骨骼(动画兼容); 武器由代码手持 create:potato_cannon
 *   · 字符串字面量全部 ASCII(避免编码问题), 中文只在注释里
 *   · 一次生成两个变体:
 *       HEAD_MODE=brown   -> 一阶段: 褐色答辩头   (compost_superman.geo.json / .png)
 *       HEAD_MODE=rainbow -> 二阶段: **彩色答辩头**(每层彩虹渐变 + 强发光) (compost_superman_p2.geo.json / _p2.png)
 *   · 造型沿用 v3: 厚头 9 层 / 双手草束 / 无披风 / 三段长腿 / 精细麻布衣
 * 用法: node gen_compost_boss_v4.cjs <输出目录> [brown|rainbow]
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_compost_boss';
const MODE = (process.argv[3] || 'brown').toLowerCase();
const RAINBOW = MODE === 'rainbow';
const SUF = RAINBOW ? '_p2' : '';
const W = 512, H = 512;
const cv = new Canvas(W, H), gl = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) { cv.set(x, y, 0, 0, 0, 0); gl.set(x, y, 0, 0, 0, 0); }
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);
const M = {
    poop: [112, 78, 46], poopDark: [78, 50, 30], poopLight: [148, 108, 66], poopDeep: [58, 36, 20],
    /* 二阶段彩色答辩: 每层一个色相 */
    rb1: [232, 74, 190], rb2: [86, 226, 232], rb3: [246, 222, 96], rb4: [150, 110, 240],
    rb5: [96, 236, 150], rb6: [246, 150, 86],
    burlap: [156, 130, 92], burlapDark: [120, 98, 66], burlapLight: [178, 152, 112],
    straw: [230, 200, 100], strawDark: [190, 156, 68], strawLight: [246, 224, 132], rope: [186, 152, 92],
    patchA: [188, 98, 74], patchB: [98, 142, 152], patchC: [140, 108, 168], stitch: [86, 66, 44],
    wood: [150, 108, 64], woodDark: [110, 76, 44],
    metal: [178, 184, 192], metalDark: [118, 124, 134], brass: [216, 172, 86],
    tomato: [214, 58, 48], tomatoDark: [166, 40, 34], eyeWhite: [246, 246, 240], mouth: [52, 30, 22], fly: [46, 42, 50]
};
const GLOW = RAINBOW
    ? { straw: 0.28, brass: 0.5, tomato: 0.12, eyeWhite: 0.28, rb1: 1, rb2: 1, rb3: 1, rb4: 1, rb5: 1, rb6: 1 }
    : { straw: 0.28, brass: 0.5, tomato: 0.12, eyeWhite: 0.28 };
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    const flat = /^(straw|brass|metal|rope|tomato|eyeWhite|mouth|fly|stitch|rb)/.test(mat);
    const n = flat ? 0 : Math.round(nz(x, y, 7) * 24 - 12);
    cv.set(px, py, Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)), Math.max(0, Math.min(255, c[2] + n)), 1);
    const g = GLOW[mat] || 0;
    if (g > 0) gl.set(px, py, Math.round(255 * g), Math.round(255 * g), Math.round(255 * g), 1);
}
let penX = 1, penY = 1, rowH = 0;
const C = [];
function alloc(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > W) { penX = 1; penY += rowH + 2; rowH = 0; }
    if (penY + fh > H) throw new Error('atlas overflow');
    const u = penX, v = penY;
    penX += fw + 2; rowH = Math.max(rowH, fh);
    return [u, v, fw, fh];
}
function cube(bone, o, s, mat, rot) {
    const uv = alloc(s[0], s[1], s[2]);
    const c = { bone, o, s, uv, mat };
    if (rot) { c.rot = rot; c.pivot = [o[0] + s[0] / 2, o[1] + s[1] / 2, o[2] + s[2] / 2]; }
    C.push(c);
}
const B = 15.0, HB = B + 17.0;

/* ---- legs: three segments, 15 units ---- */
for (const [bone, sx] of [['leg_l', -1], ['leg_r', 1]]) {
    const cx = sx * 5.2;   // 2026-09-12: legs back closer together
    cube(bone, [cx - 2.6, 0, -2.8], [5.2, 2.4, 5.6], 'woodDark');
    cube(bone, [cx - 2.2, 0.6, -3.4], [4.4, 1.4, 1.2], 'woodDark');
    cube(bone, [cx - 1.8, 2.4, -2.2], [3.6, 6.0, 4.2], 'straw');
    cube(bone, [cx - 1.9, 8.4, -2.3], [3.8, 6.6, 4.4], 'straw');
    for (let i = 0; i < 4; i++) cube(bone, [cx - 2.4, 3.2 + i * 2.6, -2.7], [4.8, 0.7, 0.7], 'strawDark');
}
/* ---- scarecrow body ---- */
cube('pole', [-1.1, 0, 3.0], [2.2, B + 24.0, 2.2], 'woodDark');
for (let i = -1; i <= 1; i++) cube('pole', [i * 2.6 - 0.9, B + 23.4, 2.6], [1.8, 1.8, 2.6], 'straw', [0, 0, i * 20]);
cube('pole', [-0.9, B + 24.6, 2.8], [1.8, 2.8, 2.2], 'straw');
cube('crossbar', [-13.4, B + 6.2, -0.4], [26.8, 2.2, 2.2], 'wood');
cube('crossbar', [-13.8, B + 5.4, -0.9], [2.6, 3.8, 3.2], 'woodDark');
cube('crossbar', [11.2, B + 5.4, -0.9], [2.6, 3.8, 3.2], 'woodDark');
cube('torso', [-7.0, B + 8.6, -3.8], [14.0, 6.4, 7.6], 'burlapLight');
cube('torso', [-7.2, B + 4.4, -3.9], [14.4, 4.6, 7.8], 'burlap');
cube('torso', [-7.6, B + 1.2, -4.1], [15.2, 3.4, 8.2], 'burlapDark');
cube('torso', [-7.6, B - 2.4, -4.1], [15.2, 4.2, 8.2], 'burlap');
for (let i = -3; i <= 3; i++) cube('torso', [i * 2.3 - 0.9, B - 5.2, -4.1], [1.8, 2.6, 1.9], 'burlapDark', [0, 0, i * 14]);
cube('torso', [-8.2, B + 0.6, -4.4], [16.4, 1.3, 8.8], 'rope');
cube('torso', [-1.0, B - 0.2, -4.7], [2.4, 2.4, 1.0], 'rope');
cube('torso', [-1.6, B - 2.4, -4.7], [1.1, 2.6, 0.9], 'rope', [0, 0, 16]);
cube('torso', [0.6, B - 2.6, -4.7], [1.1, 2.8, 0.9], 'rope', [0, 0, -14]);
cube('torso', [-5.6, B + 9.4, -4.3], [3.2, 2.6, 0.6], 'patchA');
cube('torso', [2.4, B + 5.6, -4.6], [3.4, 2.8, 0.6], 'patchB');
cube('torso', [-4.4, B - 1.6, -4.8], [2.8, 2.4, 0.6], 'patchC');
cube('torso', [3.6, B + 10.0, -4.4], [2.4, 2.0, 0.6], 'patchC');
cube('torso', [-1.2, B + 3.6, -4.7], [2.6, 2.2, 0.6], 'patchA');
for (let i = 0; i < 3; i++) cube('torso', [-6.8 + i * 5.6, B + 7.4, -4.4], [4.6, 0.5, 0.4], 'stitch');
cube('torso', [-6.4, B + 2.2, -4.5], [0.8, 4.4, 0.4], 'stitch', [0, 0, 12]);
cube('torso', [4.6, B + 1.8, -4.5], [0.8, 4.8, 0.4], 'stitch', [0, 0, -12]);
cube('torso', [-9.0, B + 7.2, -3.4], [4.6, 3.4, 6.6], 'burlapDark');
cube('torso', [4.4, B + 7.2, -3.4], [4.6, 3.4, 6.6], 'burlapDark');
cube('torso', [-5.0, B + 14.4, -3.4], [10.0, 1.6, 6.8], 'burlapLight');
for (let i = 0; i < 10; i++) { const a = Math.PI * 2 * i / 10; cube('torso', [Math.cos(a) * 5.6 - 0.8, B + 12.6, Math.sin(a) * 3.6 - 0.8], [1.7, 1.7, 1.7], i % 2 ? 'straw' : 'strawDark'); }
for (let i = -3; i <= 3; i++) {
    cube('torso', [i * 2.0 - 0.8, B - 6.2, -3.6], [1.6, 2.6, 1.6], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 10]);
    cube('torso', [i * 2.0 - 0.8, B - 6.0, 3.2], [1.6, 2.6, 1.6], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 10]);
}
/* ---- head: 9 thick layers; rainbow variant colors each layer ---- */
const RB = ['rb1', 'rb2', 'rb3', 'rb4', 'rb5', 'rb6'];
/* 2026-09-12 修正: 原来每层绕 Y 旋转到 ±24 度, 上彩虹色后看着像"只剩半边"的螺旋台阶。
 * 现在改为**微旋(±6 度以内)** + 每层**加厚** + 层间统一**暗边**, 变成一坨饱满的答辩。 */
const LAY = [
    [15.6, 4.6, 13.6, 3, -0.3], [15.0, 4.4, 13.0, -4, 0.4], [14.2, 4.2, 12.2, 5, -0.4],
    [13.2, 4.0, 11.2, -5, 0.4], [12.0, 3.8, 10.0, 6, -0.4], [10.6, 3.6, 8.8, -6, 0.3],
    [9.0, 3.4, 7.4, 5, -0.3], [7.0, 3.2, 5.6, -4, 0.2], [4.6, 3.2, 3.8, 3, -0.2]
];
let hy = HB;
LAY.forEach((L, i) => {
    const solid = RAINBOW ? RB[i % RB.length] : (i % 3 === 0 ? 'poop' : (i % 3 === 1 ? 'poopDark' : 'poopLight'));
    const deep = 'poopDeep';   // 层间暗边统一(彩虹模式下也用它, 让整颗头读起来是实心的一坨)
    cube('head', [-L[0] / 2 + L[4], hy, -L[2] / 2], [L[0], L[1], L[2]], solid, [0, L[3], 0]);
    cube('head', [-L[0] / 2 + L[4] - 0.2, hy + L[1] - 0.6, -L[2] / 2 - 0.30], [L[0] + 0.4, 0.8, 0.5], deep);
    hy += L[1] * 0.98;   // no layer overlap -> kills the z-fighting flicker
});
[[-5.4, HB + 7.0, -6.6], [4.6, HB + 11.0, -5.8], [-2.8, HB + 15.4, -5.0], [6.0, HB + 4.0, -6.0], [-6.0, HB + 13.0, -5.4]]
    .forEach((p, i) => cube('head', p, [2.0, 1.7, 1.6], RAINBOW ? RB[(i + 1) % RB.length] : 'poopLight', [0, 22, 0]));
const FY = HB + 6.0;
cube('head', [-4.8, FY, -7.0], [3.2, 3.2, 1.0], 'tomato');
cube('head', [1.6, FY, -7.0], [3.2, 3.2, 1.0], 'tomato');
cube('head', [-4.8, FY + 2.1, -7.3], [3.2, 0.9, 1.0], 'tomatoDark');
cube('head', [1.6, FY + 2.1, -7.3], [3.2, 0.9, 1.0], 'tomatoDark');
cube('head', [-4.1, FY + 0.8, -7.5], [1.2, 1.2, 0.5], 'eyeWhite');
cube('head', [2.3, FY + 0.8, -7.5], [1.2, 1.2, 0.5], 'eyeWhite');
cube('head', [-5.2, FY + 3.4, -7.2], [3.8, 0.8, 0.9], 'poopDeep', [0, 0, 12]);
cube('head', [1.8, FY + 3.4, -7.2], [3.8, 0.8, 0.9], 'poopDeep', [0, 0, -12]);
cube('head', [-3.4, FY - 3.4, -7.1], [7.2, 1.3, 0.9], 'mouth');
cube('head', [-2.2, FY - 2.2, -7.1], [4.8, 0.9, 0.9], 'mouth');
for (let i = 0; i < 4; i++) cube('head', [-2.6 + i * 1.7, FY - 2.7, -7.2], [1.1, 1.3, 0.7], 'eyeWhite');
cube('head', [-1.1, FY - 4.0, -7.2], [1.1, 1.2, 0.7], 'eyeWhite');
/* ---- straw hands (no tools) ---- */
for (const [bone, sx] of [['arm_l', -1], ['arm_r', 1]]) {
    const cx = sx * 11.0;
    cube(bone, [cx - 2.2, B + 6.6, -2.2], [4.4, 4.4, 4.4], 'burlap');
    cube(bone, [cx - 2.0, B + 2.4, -2.0], [4.0, 4.6, 4.0], 'burlapDark');
    cube(bone, [cx - 1.9, B - 2.0, -1.9], [3.8, 4.4, 3.8], 'burlap');
    for (let i = 0; i < 9; i++) {
        const a = Math.PI * 2 * i / 9, r = 2.3;
        cube(bone, [cx - 0.9 + Math.cos(a) * r, B - 6.4, -0.9 + Math.sin(a) * r], [1.8, 4.6, 1.8],
            i % 3 === 0 ? 'strawLight' : (i % 3 === 1 ? 'straw' : 'strawDark'), [sx * 10, 0, Math.cos(a) * 16]);
    }
    cube(bone, [cx - 1.6, B - 5.0, -1.6], [3.2, 2.2, 3.2], 'straw');
}
/* ---- flies ---- */
for (const [bone, bx, by, bz] of [['fly1', -10.0, HB + 16.0, -8.4], ['fly2', 10.4, HB + 13.4, -8.8]]) {
    cube(bone, [bx, by, bz], [1.7, 1.7, 1.7], 'fly');
    cube(bone, [bx - 0.2, by + 1.1, bz - 0.3], [1.9, 0.5, 1.3], 'fly');
    for (let i = -1; i <= 1; i++) cube(bone, [bx + i * 0.6, by - 0.5, bz + 0.2], [0.4, 0.9, 0.4], 'fly');
}
for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}
/* ---- bones (names unchanged; cannon/fork/shovel/cape are empty bones) ---- */
const PIV = {
    root: [0, 0, 0], leg_l: [-5.6, 2.4, 0], leg_r: [5.6, 2.4, 0], pole: [0, 0, 3.0], crossbar: [0, B + 7.2, 0],
    torso: [0, B + 1.2, 0], head: [0, HB, 0], arm_l: [-11.0, B + 8.8, 0], arm_r: [11.0, B + 8.8, 0],
    fork: [-11.0, B - 3.0, 0], shovel: [11.0, B - 3.0, 0],
    hand_r: [11.0, B - 6.2, -0.6],   // 右手稻草束中心(手持物渲染定位用)
    cape1: [0, B + 8.6, 3.6], cape2: [0, B + 3.8, 4.5], cape3: [0, B - 1.4, 5.4], cape4: [0, B - 6.8, 6.3],
    cannon: [11.0, B + 2.0, -7.0], fly1: [-10.0, HB + 16.0, -8.4], fly2: [10.4, HB + 13.4, -8.8]
};
const PAR = {
    leg_l: 'root', leg_r: 'root', pole: 'root', torso: 'pole', crossbar: 'torso', head: 'torso',
    arm_l: 'crossbar', arm_r: 'crossbar', fork: 'arm_l', shovel: 'arm_r',
    cape1: 'crossbar', cape2: 'cape1', cape3: 'cape2', cape4: 'cape3',
    cannon: 'arm_r', fly1: 'head', fly2: 'head', hand_r: 'arm_r'
};
const bones = Object.keys(PIV).map(name => {
    const cs = C.filter(c => c.bone === name).map(c => {
        const o = { origin: c.o, size: c.s, uv: [c.uv[0], c.uv[1]] };
        if (c.rot) { o.rotation = c.rot; o.pivot = c.pivot; }
        return o;
    });
    const b = { name, pivot: PIV[name] };
    if (cs.length) b.cubes = cs;
    if (PAR[name]) b.parent = PAR[name];
    return b;
});
const geo = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.compost_superman', texture_width: W, texture_height: H, visible_bounds_width: 4, visible_bounds_height: 8, visible_bounds_offset: [0, 3.4, 0] }, bones }] };
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'compost_superman' + SUF + '.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'compost_superman' + SUF + '.png'));
gl.save(path.join(OUT, 'compost_superman' + SUF + '_glowmask.png'));
const cubeCount = C.length;
console.log('mode=' + MODE + ' cubes=' + cubeCount + ' bones=' + bones.length + ' -> compost_superman' + SUF);
