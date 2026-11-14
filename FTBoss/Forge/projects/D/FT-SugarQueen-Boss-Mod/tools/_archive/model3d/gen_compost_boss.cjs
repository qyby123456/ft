/* 炫彩答辩超人 —— 正式模型 v1(2026-09-12)
 *  在块面定稿 v2 基础上做三处改进:
 *   ① 答辩头 5 层 -> **7 层**, 逐层收窄/错位/微旋, 垂直方向也轻微歪斜
 *   ② 彩虹披风 -> 四段**错位垂坠**(上层压下层) + 每段三段渐变(洋红/青/黄), 有"流动感"
 *   ③ 下摆收窄 10%, 稻草腿外移 -> 正视能看清"站在杆上的稻草人"
 *  贴图 **512x512** + **发光膜 glowmask**(彩虹油膜带 / 胸口嫩芽 / 炮口黄铜 发光)
 * 用法: node gen_compost_boss.cjs <输出目录>
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
    poop: [112, 78, 46], poopDark: [80, 52, 30], poopLight: [146, 106, 64], poopDeep: [62, 40, 22],
    burlap: [152, 126, 88], burlapDark: [118, 96, 64], straw: [228, 198, 98], strawDark: [188, 154, 66],
    patchA: [188, 98, 74], patchB: [98, 142, 152], wood: [150, 108, 64], woodDark: [110, 76, 44],
    metal: [178, 184, 192], metalDark: [118, 124, 134], brass: [216, 172, 86],
    rainbowA: [232, 74, 190], rainbowB: [86, 226, 232], rainbowC: [246, 222, 96],
    tomato: [214, 58, 48], eyeWhite: [246, 246, 240], mouth: [52, 30, 22], fly: [46, 42, 50]
};
const GLOW = { rainbowA: 1, rainbowB: 1, rainbowC: 1, straw: 0.35, brass: 0.5, tomato: 0.15, eyeWhite: 0.3 };
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    const flat = /^(rainbow|straw|brass|metal|tomato|eyeWhite|mouth|fly)/.test(mat);
    const n = flat ? 0 : Math.round(nz(x, y, 7) * 22 - 11);
    cv.set(px, py, Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)), Math.max(0, Math.min(255, c[2] + n)), 1);
    const g = GLOW[mat] || 0;
    if (g > 0) gl.set(px, py, Math.round(255 * g), Math.round(255 * g), Math.round(255 * g), 1);
}
let penX = 1, penY = 1, rowH = 0;
const C = [];
function alloc(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > W) { penX = 1; penY += rowH + 2; rowH = 0; }
    if (penY + fh > H) throw new Error('贴图放不下');
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

/* ============ 身体: 披风稻草人(下摆收窄 10%, 腿外移) ============ */
cube('pole', [-1.1, 0, 2.4], [2.2, 28.0, 2.2], 'woodDark');
for (let i = -1; i <= 1; i++) cube('pole', [i * 2.6 - 0.9, 27.4, 2.0], [1.8, 1.8, 2.6], 'straw', [0, 0, i * 20]);
cube('pole', [-0.9, 28.8, 2.2], [1.8, 2.6, 2.2], 'straw');
cube('crossbar', [-13.0, 21.0, -0.4], [26.0, 2.0, 2.0], 'wood');
cube('torso', [-6.6, 13.0, -3.6], [13.2, 6.6, 7.2], 'burlap');
cube('torso', [-7.2, 7.0, -3.9], [14.4, 6.2, 7.8], 'burlapDark');
cube('torso', [-7.4, 2.6, -4.1], [14.8, 4.6, 8.2], 'burlap');   // 下摆比 v2 窄 10%
cube('torso', [-5.2, 14.6, -4.1], [3.0, 2.4, 0.6], 'patchA');
cube('torso', [2.0, 8.2, -4.4], [3.2, 2.6, 0.6], 'patchB');
for (let i = 0; i < 8; i++) { const a = Math.PI * 2 * i / 8; cube('torso', [Math.cos(a) * 6.2 - 0.9, 19.0, Math.sin(a) * 3.9 - 0.9], [1.8, 1.6, 1.8], 'straw'); }
for (let i = -3; i <= 3; i++) {
    cube('torso', [i * 2.2 - 0.9, 1.2, -3.4], [1.8, 2.8, 1.8], 'straw', [0, 0, i * 12]);
    cube('torso', [i * 2.2 - 0.9, 1.4, 3.0], [1.8, 2.8, 1.8], 'straw', [0, 0, -i * 12]);
}
/* 稻草腿(外移) + 土脚 */
cube('leg_l', [-8.2, 0, -2.4], [4.2, 3.2, 4.6], 'woodDark');
cube('leg_r', [4.0, 0, -2.4], [4.2, 3.2, 4.6], 'woodDark');
cube('leg_l', [-7.8, 2.8, -2.0], [3.4, 4.6, 3.8], 'straw');
cube('leg_r', [4.4, 2.8, -2.0], [3.4, 4.6, 3.8], 'straw');
/* 彩虹披风: 四段错位垂坠, 每段三色渐变 */
const CAPE = [[-7.0, 20.2, 3.4, 14.0, 5.0], [-8.4, 15.6, 4.3, 16.8, 4.6], [-10.0, 10.8, 5.2, 19.6, 4.2], [-11.6, 5.6, 6.1, 22.8, 5.2]];
CAPE.forEach((c, i) => {
    const cols = [['rainbowA', 'rainbowB', 'rainbowC'], ['rainbowB', 'rainbowC', 'rainbowA'], ['rainbowC', 'rainbowA', 'rainbowB']][i % 3];
    cube('cape' + (i + 1), [c[0], c[1], c[2]], [c[3], c[4], 0.9], cols[0]);
    cube('cape' + (i + 1), [c[0], c[1] + c[4] * 0.5, c[2] + 0.05], [c[3] * 0.98, c[4] * 0.5, 0.9], cols[1]);
    cube('cape' + (i + 1), [c[0] + 0.4, c[1] - 1.8, c[2] + 0.1], [c[3] * 0.94, 1.8, 0.9], cols[2]);
});

/* ============ 头: 答辩 7 层螺旋 ============ */
const LAYERS = [
    [-6.4, 22.4, -5.4, 12.8, 3.4, 11.0, 'poop', 8, -0.3],
    [-6.0, 25.0, -5.1, 12.0, 3.2, 10.4, 'poopDark', -12, 0.4],
    [-5.4, 27.4, -4.8, 10.8, 3.0, 9.6, 'poop', 16, -0.4],
    [-4.8, 29.6, -4.4, 9.6, 2.8, 8.6, 'poopLight', -18, 0.5],
    [-4.0, 31.6, -3.8, 8.0, 2.6, 7.4, 'poop', 20, -0.5],
    [-3.0, 33.4, -3.0, 6.0, 2.4, 5.8, 'poopDark', -22, 0.4],
    [-1.8, 35.0, -2.0, 3.6, 3.0, 3.6, 'poop', 26, -0.3]
];
LAYERS.forEach((L, i) => cube('head', [L[0] + L[8], L[1], L[2]], [L[3], L[4], L[5]], L[6], [0, L[7], 0]));
/* 彩虹螺旋油膜带(缠在 1/3/5 层缝上 + 两侧竖条) */
cube('head', [-6.6, 24.6, -5.6], [13.2, 0.9, 0.7], 'rainbowA', [0, 8, 0]);
cube('head', [-5.6, 28.6, -5.0], [11.2, 0.9, 0.7], 'rainbowB', [0, 16, 0]);
cube('head', [-4.2, 32.0, -4.0], [8.4, 0.9, 0.7], 'rainbowC', [0, 20, 0]);
cube('head', [5.8, 26.0, -4.8], [0.7, 0.9, 3.6], 'rainbowA', [0, -12, 0]);
cube('head', [-6.6, 30.0, -3.4], [0.7, 0.9, 3.8], 'rainbowB', [0, 18, 0]);
/* 脸 */
cube('head', [-3.8, 27.8, -5.4], [2.6, 2.6, 0.8], 'tomato');
cube('head', [1.2, 27.8, -5.4], [2.6, 2.6, 0.8], 'tomato');
cube('head', [-3.2, 28.4, -5.8], [1.0, 1.0, 0.4], 'eyeWhite');
cube('head', [1.8, 28.4, -5.8], [1.0, 1.0, 0.4], 'eyeWhite');
cube('head', [-2.6, 25.2, -5.5], [5.2, 1.0, 0.7], 'mouth');
cube('head', [-1.6, 26.0, -5.5], [3.2, 0.7, 0.7], 'mouth');
cube('head', [-1.0, 24.8, -5.6], [0.9, 1.1, 0.6], 'eyeWhite');
cube('head', [0.2, 24.8, -5.6], [0.9, 1.1, 0.6], 'eyeWhite');

/* ============ 手臂 / 农具 / 加农炮 / 苍蝇 ============ */
cube('arm_l', [-12.4, 15.6, -2.0], [4.0, 4.0, 4.0], 'burlap');
cube('arm_l', [-11.8, 11.0, -1.8], [3.4, 4.8, 3.6], 'burlapDark');
for (let i = -1; i <= 1; i++) cube('arm_l', [-11.6, 10.0, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');
cube('fork', [-10.6, 4.6, -1.6], [2.0, 12.2, 2.0], 'wood');
for (let i = -1; i <= 1; i++) cube('fork', [-10.8 + i * 1.7, -1.4, -1.5], [0.9, 6.4, 1.1], 'metal');
cube('arm_r', [8.4, 15.6, -2.0], [4.0, 4.0, 4.0], 'burlap');
cube('arm_r', [8.4, 11.0, -1.8], [3.4, 4.8, 3.6], 'burlapDark');
for (let i = -1; i <= 1; i++) cube('arm_r', [9.4, 10.0, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');
cube('shovel', [9.0, 5.0, -1.6], [1.9, 11.6, 1.9], 'wood');
cube('shovel', [8.2, -1.4, -2.0], [3.4, 6.6, 3.0], 'metalDark');
cube('cannon', [5.0, 22.2, -10.6], [4.0, 4.0, 11.6], 'metal');
cube('cannon', [4.6, 21.8, -12.4], [4.8, 4.8, 1.8], 'brass');
cube('cannon', [4.6, 21.8, -4.8], [4.8, 4.8, 1.8], 'brass');
cube('cannon', [4.0, 20.0, -7.0], [6.0, 3.0, 5.2], 'metalDark');
cube('cannon', [5.2, 26.0, -7.4], [3.6, 3.0, 3.6], 'metalDark');
cube('cannon', [5.8, 27.2, -6.8], [2.4, 1.8, 2.4], 'poop');
cube('fly1', [-10.0, 33.4, -7.4], [1.6, 1.6, 1.6], 'fly');
cube('fly1', [-10.8, 34.4, -7.8], [1.8, 0.5, 1.2], 'fly');
cube('fly2', [10.4, 31.2, -7.8], [1.6, 1.6, 1.6], 'fly');
cube('fly2', [11.2, 32.2, -8.2], [1.8, 0.5, 1.2], 'fly');

for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}
const PIV = {
    root: [0, 0, 0], leg_l: [-6.0, 3.0, 0], leg_r: [6.0, 3.0, 0], pole: [0, 0, 2.4], crossbar: [0, 22.0, 0],
    torso: [0, 6.0, 0], head: [0, 22.4, 0], arm_l: [-10.4, 17.6, 0], arm_r: [10.4, 17.6, 0],
    fork: [-10.6, 16.6, 0], shovel: [9.0, 16.6, 0],
    cape1: [0, 22.0, 3.4], cape2: [0, 17.4, 4.3], cape3: [0, 12.6, 5.2], cape4: [0, 7.4, 6.1],
    cannon: [5.0, 24.2, -5.0], fly1: [-10.0, 33.4, -7.4], fly2: [10.4, 31.2, -7.8]
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
const geo = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.compost_superman', texture_width: W, texture_height: H, visible_bounds_width: 3.5, visible_bounds_height: 5, visible_bounds_offset: [0, 2, 0] }, bones }] };
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'compost_superman.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'compost_superman.png'));
gl.save(path.join(OUT, 'compost_superman_glowmask.png'));
console.log(`正式模型已生成: cube=${C.length} 骨骼=${bones.length} 贴图=${W}x${H} + 发光膜 -> ${OUT}`);
