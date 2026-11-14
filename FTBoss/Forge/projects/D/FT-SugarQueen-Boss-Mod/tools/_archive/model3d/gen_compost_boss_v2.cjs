/* 炫彩答辩超人 —— 精细版 v2(2026-09-12 按用户要求)
 *  ① 腿加长: 由"土脚+稻草"8 单位 -> **大腿+小腿+土脚三段共 15 单位**(骨骼名不变, 动画无需重做)
 *  ② 衣服精细化: 领口 + 麻绳腰带(含结) + 5 块补丁 + 缝线 + 褶皱 + 肩甲 + 加长锯齿下摆 + 更多稻草
 *  ③ 头部精细化: 答辩 7 层 -> **9 层**(更平滑收窄) + 每层暗色边圈 + 6 处表面凸起 + 5 条彩虹螺旋带 + 眼睑/眉毛/大牙
 *  ④ 加农炮细节: 扳机 + 两个齿轮 + 黄铜导管 + 准星
 * 用法: node gen_compost_boss_v2.cjs <输出目录>
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_compost_boss';
const W = 512, H = 512;
const cv = new Canvas(W, H), gl = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) { cv.set(x, y, 0, 0, 0, 0); gl.set(x, y, 0, 0, 0, 0); }
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);
const M = {
    poop: [112, 78, 46], poopDark: [78, 50, 30], poopLight: [148, 108, 66], poopDeep: [58, 36, 20],
    burlap: [156, 130, 92], burlapDark: [120, 98, 66], burlapLight: [178, 152, 112],
    straw: [230, 200, 100], strawDark: [190, 156, 68], rope: [186, 152, 92],
    patchA: [188, 98, 74], patchB: [98, 142, 152], patchC: [140, 108, 168], stitch: [86, 66, 44],
    wood: [150, 108, 64], woodDark: [110, 76, 44],
    metal: [178, 184, 192], metalDark: [118, 124, 134], brass: [216, 172, 86],
    rainbowA: [232, 74, 190], rainbowB: [86, 226, 232], rainbowC: [246, 222, 96],
    tomato: [214, 58, 48], tomatoDark: [166, 40, 34], eyeWhite: [246, 246, 240], mouth: [52, 30, 22], fly: [46, 42, 50]
};
const GLOW = { rainbowA: 1, rainbowB: 1, rainbowC: 1, straw: 0.3, brass: 0.5, tomato: 0.12, eyeWhite: 0.28 };
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    const flat = /^(rainbow|straw|brass|metal|rope|tomato|eyeWhite|mouth|fly|stitch)/.test(mat);
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
    if (penY + fh > H) throw new Error('贴图放不下: ' + fw + 'x' + fh + ' @' + penY);
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

/* ==================== ① 腿: 三段共 15 单位(加长) ==================== */
/* 土脚(0~2.4) + 小腿稻草(2.4~8.4) + 大腿稻草(8.4~15.0) */
for (const [bone, sx] of [['leg_l', -1], ['leg_r', 1]]) {
    const cx = sx * 5.6;
    cube(bone, [cx - 2.6, 0, -2.8], [5.2, 2.4, 5.6], 'woodDark');                       // 土脚
    cube(bone, [cx - 2.2, 0.6, -3.4], [4.4, 1.4, 1.2], 'woodDark');                     // 脚前掌
    cube(bone, [cx - 2.0, 2.4, -2.2], [4.0, 6.0, 4.4], 'straw');                        // 小腿
    for (let i = 0; i < 3; i++) cube(bone, [cx - 2.4, 3.4 + i * 1.7, -2.6 + i * 0.1], [4.8, 0.7, 0.7], 'strawDark');  // 稻草束扎痕
    cube(bone, [cx - 2.1, 8.4, -2.3], [4.2, 6.6, 4.6], 'straw');                        // 大腿
    for (let i = 0; i < 3; i++) cube(bone, [cx - 2.5, 9.6 + i * 1.8, -2.7 + i * 0.1], [5.0, 0.7, 0.7], 'strawDark');
}
/* ==================== ② 身体: 精细麻布衣(腰线抬高到 15) ==================== */
const B = 15.0;                                    // 身体基准
cube('pole', [-1.1, 0, 3.0], [2.2, B + 15.0, 2.2], 'woodDark');                        // 背杆加长
for (let i = -1; i <= 1; i++) cube('pole', [i * 2.6 - 0.9, B + 14.6, 2.6], [1.8, 1.8, 2.6], 'straw', [0, 0, i * 20]);
cube('pole', [-0.9, B + 16.0, 2.8], [1.8, 2.8, 2.2], 'straw');
cube('crossbar', [-13.4, B + 6.2, -0.4], [26.8, 2.2, 2.2], 'wood');                    // 肩横杆
cube('crossbar', [-13.8, B + 5.4, -0.9], [2.6, 3.8, 3.2], 'woodDark');                 // 横杆左端
cube('crossbar', [11.2, B + 5.4, -0.9], [2.6, 3.8, 3.2], 'woodDark');                  // 横杆右端
/* 衣身: 5 段(胸/上腹/腰/下摆/锯齿边) */
cube('torso', [-7.0, B + 8.6, -3.8], [14.0, 6.4, 7.6], 'burlapLight');                 // 胸
cube('torso', [-7.2, B + 4.4, -3.9], [14.4, 4.6, 7.8], 'burlap');                      // 上腹
cube('torso', [-7.6, B + 1.2, -4.1], [15.2, 3.4, 8.2], 'burlapDark');                  // 腰
cube('torso', [-8.0, B - 3.6, -4.3], [16.0, 5.0, 8.6], 'burlap');                      // 下摆
for (let i = -3; i <= 3; i++) cube('torso', [i * 2.3 - 0.9, B - 6.4, -4.3], [1.8, 3.0, 1.9], 'burlapDark', [0, 0, i * 14]);  // 锯齿边
/* 麻绳腰带 + 结 */
cube('torso', [-8.2, B + 0.6, -4.4], [16.4, 1.3, 8.8], 'rope');
cube('torso', [-1.0, B - 0.2, -4.7], [2.4, 2.4, 1.0], 'rope');                          // 结
cube('torso', [-1.6, B - 2.4, -4.7], [1.1, 2.6, 0.9], 'rope', [0, 0, 16]);              // 垂带
cube('torso', [0.6, B - 2.6, -4.7], [1.1, 2.8, 0.9], 'rope', [0, 0, -14]);
/* 补丁 5 块 + 缝线 */
cube('torso', [-5.6, B + 9.4, -4.3], [3.2, 2.6, 0.6], 'patchA');
cube('torso', [2.4, B + 5.6, -4.6], [3.4, 2.8, 0.6], 'patchB');
cube('torso', [-4.4, B - 1.6, -4.8], [2.8, 2.4, 0.6], 'patchC');
cube('torso', [3.6, B + 10.0, -4.4], [2.4, 2.0, 0.6], 'patchC');
cube('torso', [-1.2, B + 3.6, -4.7], [2.6, 2.2, 0.6], 'patchA');
for (let i = 0; i < 3; i++) cube('torso', [-6.8 + i * 5.6, B + 7.4, -4.4], [4.6, 0.5, 0.4], 'stitch');
/* 褶皱(斜细条) + 肩甲 */
cube('torso', [-6.4, B + 2.2, -4.5], [0.8, 4.4, 0.4], 'stitch', [0, 0, 12]);
cube('torso', [4.6, B + 1.8, -4.5], [0.8, 4.8, 0.4], 'stitch', [0, 0, -12]);
cube('torso', [-9.0, B + 7.2, -3.4], [4.6, 3.4, 6.6], 'burlapDark');                   // 左肩甲
cube('torso', [4.4, B + 7.2, -3.4], [4.6, 3.4, 6.6], 'burlapDark');                    // 右肩甲
/* 领口 + 颈部稻草 */
cube('torso', [-5.0, B + 14.4, -3.4], [10.0, 1.6, 6.8], 'burlapLight');
for (let i = 0; i < 10; i++) { const a = Math.PI * 2 * i / 10; cube('torso', [Math.cos(a) * 5.6 - 0.8, B + 12.6, Math.sin(a) * 3.6 - 0.8], [1.7, 1.7, 1.7], i % 2 ? 'straw' : 'strawDark'); }
/* 下摆外散稻草 */
for (let i = -4; i <= 4; i++) {
    cube('torso', [i * 2.1 - 0.8, B - 8.6, -3.6], [1.7, 3.2, 1.7], i % 2 ? 'straw' : 'strawDark', [0, 0, i * 12]);
    cube('torso', [i * 2.1 - 0.8, B - 8.4, 3.2], [1.7, 3.2, 1.7], i % 2 ? 'strawDark' : 'straw', [0, 0, -i * 12]);
}
/* 彩虹披风(四段, 每段上/中/下三色 + 边缘) */
const CAPE = [[-7.2, B + 6.0, 3.6, 14.4, 5.0], [-8.6, B + 1.2, 4.5, 17.2, 4.6], [-10.2, B - 4.0, 5.4, 20.0, 4.2], [-11.8, B - 9.4, 6.3, 23.2, 5.2]];
CAPE.forEach((c, i) => {
    const cs = [['rainbowA', 'rainbowB', 'rainbowC'], ['rainbowB', 'rainbowC', 'rainbowA'], ['rainbowC', 'rainbowA', 'rainbowB']][i % 3];
    cube('cape' + (i + 1), [c[0], c[1], c[2]], [c[3], c[4] * 0.42, 0.9], cs[0]);
    cube('cape' + (i + 1), [c[0] + 0.2, c[1] + c[4] * 0.42, c[2] + 0.05], [c[3] - 0.4, c[4] * 0.34, 0.9], cs[1]);
    cube('cape' + (i + 1), [c[0] + 0.5, c[1] + c[4] * 0.76, c[2] + 0.1], [c[3] - 1.0, c[4] * 0.24, 0.9], cs[2]);
    cube('cape' + (i + 1), [c[0] - 0.3, c[1] + 0.2, c[2] - 0.15], [0.6, c[4] * 0.9, 0.5], 'rainbowB');   // 侧缘亮边
});

/* ==================== ③ 头: 答辩 9 层 + 细节 ==================== */
const HEAD_B = B + 17.0;
const LAYERS = [
    [13.4, 3.2, 11.6, 'poop', 6, -0.4], [12.8, 3.1, 11.0, 'poopDark', -10, 0.5],
    [12.0, 3.0, 10.4, 'poop', 14, -0.5], [11.0, 2.9, 9.6, 'poopLight', -16, 0.5],
    [9.8, 2.8, 8.6, 'poop', 18, -0.5], [8.4, 2.6, 7.5, 'poopDark', -20, 0.4],
    [6.8, 2.5, 6.2, 'poop', 22, -0.4], [4.8, 2.4, 4.6, 'poopLight', -24, 0.3],
    [3.0, 2.8, 3.0, 'poop', 26, -0.2]
];
let hy = HEAD_B;
LAYERS.forEach((L, i) => {
    const w = L[0], h = L[1], d = L[2], off = L[5];
    cube('head', [-w / 2 + off, hy, -d / 2], [w, h, d], L[3], [0, L[4], 0]);
    /* 每层暗色边圈(前沿 + 两侧) */
    cube('head', [-w / 2 + off - 0.2, hy + h - 0.5, -d / 2 - 0.15], [w + 0.4, 0.6, 0.5], 'poopDeep');
    hy += h * 0.78;
});
/* 表面凸起(6 处) */
[[-4.6, HEAD_B + 6.0, -5.6], [3.8, HEAD_B + 9.6, -5.0], [-2.4, HEAD_B + 13.0, -4.2],
 [5.0, HEAD_B + 3.4, -5.2], [-5.2, HEAD_B + 11.0, -4.6], [1.6, HEAD_B + 15.6, -3.0]]
    .forEach(p => cube('head', p, [1.8, 1.5, 1.5], 'poopLight', [0, 22, 0]));
/* 彩虹螺旋带 5 条 */
[[-7.0, HEAD_B + 2.6, -6.0, 13.8, 8], [6.0, HEAD_B + 5.4, -5.4, 12.4, -12], [-5.6, HEAD_B + 8.6, -5.0, 11.0, 16],
 [-6.4, HEAD_B + 12.0, -4.4, 9.0, 20], [4.8, HEAD_B + 14.4, -3.6, 6.0, -18]]
    .forEach((b, i) => cube('head', [b[0], b[1], b[2]], [b[3], 0.9, 0.7], ['rainbowA', 'rainbowB', 'rainbowC'][i % 3], [0, b[4], 0]));
/* 脸: 眼(带眼睑/眉) + 大笑 + 大牙 */
const FY = HEAD_B + 5.2;
cube('head', [-4.2, FY, -6.0], [2.8, 2.8, 0.9], 'tomato');
cube('head', [1.4, FY, -6.0], [2.8, 2.8, 0.9], 'tomato');
cube('head', [-4.2, FY + 1.8, -6.3], [2.8, 0.8, 0.9], 'tomatoDark');            // 眼睑
cube('head', [1.4, FY + 1.8, -6.3], [2.8, 0.8, 0.9], 'tomatoDark');
cube('head', [-3.6, FY + 0.7, -6.5], [1.1, 1.1, 0.5], 'eyeWhite');             // 高光
cube('head', [2.0, FY + 0.7, -6.5], [1.1, 1.1, 0.5], 'eyeWhite');
cube('head', [-4.6, FY + 3.0, -6.2], [3.4, 0.7, 0.8], 'poopDeep', [0, 0, 12]); // 眉
cube('head', [1.6, FY + 3.0, -6.2], [3.4, 0.7, 0.8], 'poopDeep', [0, 0, -12]);
cube('head', [-3.0, FY - 3.0, -6.1], [6.4, 1.2, 0.8], 'mouth');                // 嘴
cube('head', [-2.0, FY - 1.9, -6.1], [4.2, 0.8, 0.8], 'mouth');
for (let i = 0; i < 3; i++) cube('head', [-2.2 + i * 1.7, FY - 2.4, -6.2], [1.1, 1.2, 0.6], 'eyeWhite');  // 上排牙
cube('head', [-1.0, FY - 3.6, -6.2], [1.0, 1.1, 0.6], 'eyeWhite');             // 下门牙

/* ==================== ④ 手臂 / 农具 / 加农炮 / 苍蝇 ==================== */
cube('arm_l', [-13.0, B + 6.6, -2.2], [4.4, 4.4, 4.4], 'burlap');
cube('arm_l', [-12.4, B + 2.4, -2.0], [3.6, 4.6, 3.8], 'burlapDark');
cube('arm_l', [-12.2, B - 2.2, -1.9], [3.4, 4.8, 3.6], 'burlap');              // 前臂加一段
for (let i = -1; i <= 1; i++) cube('arm_l', [-12.0, B - 3.4, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');
cube('fork', [-11.2, B - 9.0, -1.7], [2.1, 14.0, 2.1], 'wood');
cube('fork', [-11.2, B - 10.4, -1.7], [2.1, 1.6, 2.1], 'woodDark');
for (let i = -1; i <= 1; i++) cube('fork', [-11.4 + i * 1.8, B - 16.0, -1.6], [0.9, 5.8, 1.1], 'metal');
cube('arm_r', [8.6, B + 6.6, -2.2], [4.4, 4.4, 4.4], 'burlap');
cube('arm_r', [8.8, B + 2.4, -2.0], [3.6, 4.6, 3.8], 'burlapDark');
cube('arm_r', [9.0, B - 2.2, -1.9], [3.4, 4.8, 3.6], 'burlap');
for (let i = -1; i <= 1; i++) cube('arm_r', [9.8, B - 3.4, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');
cube('shovel', [9.6, B - 8.6, -1.7], [2.0, 13.4, 2.0], 'wood');
cube('shovel', [8.8, B - 15.6, -2.1], [3.6, 7.0, 3.2], 'metalDark');
cube('shovel', [8.8, B - 16.4, -2.3], [3.6, 1.2, 3.4], 'metal');
/* 加农炮: 炮管 + 前后箍 + 机匣 + 料斗 + 扳机 + 齿轮 + 导管 + 准星 */
const CY = B + 9.4;
cube('cannon', [5.0, CY, -11.4], [4.2, 4.2, 12.4], 'metal');
cube('cannon', [4.6, CY - 0.4, -13.4], [5.0, 5.0, 1.9], 'brass');
cube('cannon', [4.6, CY - 0.4, -5.2], [5.0, 5.0, 1.9], 'brass');
cube('cannon', [4.6, CY - 0.4, -8.4], [5.0, 5.0, 1.4], 'brass');
cube('cannon', [3.8, CY - 2.4, -7.6], [6.4, 3.2, 5.6], 'metalDark');
cube('cannon', [5.2, CY + 4.4, -8.0], [3.8, 3.2, 3.8], 'metalDark');
cube('cannon', [5.8, CY + 5.6, -7.4], [2.6, 2.0, 2.6], 'poop');
cube('cannon', [4.4, CY - 3.4, -6.2], [2.4, 2.0, 2.0], 'metalDark');           // 扳机护圈
cube('cannon', [10.0, CY + 0.6, -7.0], [1.2, 3.0, 3.0], 'brass', [0, 0, 20]);  // 齿轮1
cube('cannon', [10.0, CY - 2.6, -9.4], [1.2, 2.6, 2.6], 'brass', [0, 0, -16]); // 齿轮2
cube('cannon', [2.2, CY + 1.0, -9.0], [1.6, 1.6, 6.0], 'brass');               // 黄铜导管
cube('cannon', [6.4, CY + 2.6, -12.6], [1.4, 1.4, 1.4], 'metal');              // 准星
/* 苍蝇(带腿) */
for (const [bone, bx, by, bz] of [['fly1', -10.6, HEAD_B + 12.0, -8.0], ['fly2', 11.0, HEAD_B + 9.6, -8.4]]) {
    cube(bone, [bx, by, bz], [1.7, 1.7, 1.7], 'fly');
    cube(bone, [bx - 0.2, by + 1.1, bz - 0.3], [1.9, 0.5, 1.3], 'fly');
    for (let i = -1; i <= 1; i++) cube(bone, [bx + i * 0.6, by - 0.5, bz + 0.2], [0.4, 0.9, 0.4], 'fly');
}

for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}
/* 骨骼(名字与旧版一致 -> 动画无需重做; 仅整体抬高) */
const PIV = {
    root: [0, 0, 0], leg_l: [-5.6, 2.4, 0], leg_r: [5.6, 2.4, 0], pole: [0, 0, 3.0], crossbar: [0, B + 7.2, 0],
    torso: [0, B + 1.2, 0], head: [0, HEAD_B, 0], arm_l: [-11.0, B + 8.8, 0], arm_r: [11.0, B + 8.8, 0],
    fork: [-11.2, B + 5.4, 0], shovel: [9.6, B + 5.4, 0],
    cape1: [0, B + 8.6, 3.6], cape2: [0, B + 3.8, 4.5], cape3: [0, B - 1.4, 5.4], cape4: [0, B - 6.8, 6.3],
    cannon: [5.0, B + 11.4, -5.6], fly1: [-10.6, HEAD_B + 12.0, -8.0], fly2: [11.0, HEAD_B + 9.6, -8.4]
};
const PAR = {
    leg_l: 'root', leg_r: 'root', pole: 'root', torso: 'pole', crossbar: 'torso', head: 'torso',
    arm_l: 'crossbar', arm_r: 'crossbar', fork: 'arm_l', shovel: 'arm_r',
    cape1: 'crossbar', cape2: 'cape1', cape3: 'cape2', cape4: 'cape3', cannon: 'crossbar', fly1: 'head', fly2: 'head'
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
const geo = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.compost_superman', texture_width: W, texture_height: H, visible_bounds_width: 4, visible_bounds_height: 7, visible_bounds_offset: [0, 3, 0] }, bones }] };
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'compost_superman.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'compost_superman.png'));
gl.save(path.join(OUT, 'compost_superman_glowmask.png'));
console.log(`精细版已生成: cube=${C.length} 骨骼=${bones.length} 贴图=${W}x${H} 身高≈${(HEAD_B + 26).toFixed(0)}单位(腿 15)`);
