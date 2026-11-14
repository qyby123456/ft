/* 炫彩答辩超人 —— 块面定稿 v2(2026-09-12 按用户要求改版)
 *  头 = **答辩**(经典 💩 螺旋堆叠造型, 5 层逐级收窄 + 左右错位 + 彩虹螺旋油膜带, 脸上有番茄眼与大笑)
 *  身 = **披风稻草人**(背木杆 + 肩十字横杆 + 麻布拼补丁衣 + 袖口/下摆/颈部/杆顶**稻草外露** + 彩虹披风)
 *  右手铁铲 / 左手草叉 / 右肩【机械动力土豆加农炮】(炮口朝前, 带黄铜箍)
 * 用法: node gen_compost2.cjs <输出目录>
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_compost_sketch';
const W = 256, H = 256;
const cv = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) cv.set(x, y, 0, 0, 0, 0);
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);

const M = {
    /* 答辩(头部) */
    poop: [112, 78, 46], poopDark: [80, 52, 30], poopLight: [146, 106, 64],
    /* 稻草人 */
    burlap: [152, 126, 88], burlapDark: [118, 96, 64], straw: [228, 198, 98], strawDark: [188, 154, 66],
    patchA: [188, 98, 74], patchB: [98, 142, 152], wood: [150, 108, 64], woodDark: [110, 76, 44],
    /* 金属/彩虹 */
    metal: [178, 184, 192], metalDark: [118, 124, 134], brass: [216, 172, 86],
    rainbowA: [232, 74, 190], rainbowB: [86, 226, 232], rainbowC: [246, 222, 96],
    tomato: [214, 58, 48], eyeWhite: [246, 246, 240], mouth: [52, 30, 22], fly: [46, 42, 50]
};
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    const flat = /^(rainbow|straw|brass|metal|tomato|eyeWhite|mouth|fly)/.test(mat);
    const n = flat ? 0 : nz(x, y, 7) * 22 - 11;
    cv.set(px, py, Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)), Math.max(0, Math.min(255, c[2] + n)), 1);
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
/** cube(骨骼, 起点, 尺寸, 材质, 可选旋转[deg]) */
function cube(bone, o, s, mat, rot) {
    const uv = alloc(s[0], s[1], s[2]);
    const c = { bone, o, s, uv, mat };
    if (rot) { c.rot = rot; c.pivot = [o[0] + s[0] / 2, o[1] + s[1] / 2, o[2] + s[2] / 2]; }
    C.push(c);
}

/* ================= ① 身体: 披风稻草人 ================= */
/* 背木杆(脊) + 杆顶稻草 */
cube('pole', [-1.1, 0, 2.2], [2.2, 27.0, 2.2], 'woodDark');
for (let i = -1; i <= 1; i++) cube('pole', [i * 2.6 - 0.9, 26.4, 1.8], [1.8, 1.6, 2.6], 'straw', [0, 0, i * 18]);
cube('pole', [-0.9, 27.6, 2.0], [1.8, 2.4, 2.2], 'straw');
/* 肩十字横杆 */
cube('crossbar', [-13.0, 20.4, -0.4], [26.0, 2.0, 2.0], 'wood');
/* 麻布衣(三层) + 补丁 */
cube('torso', [-6.8, 12.4, -3.6], [13.6, 6.4, 7.2], 'burlap');
cube('torso', [-7.4, 6.6, -3.9], [14.8, 6.0, 7.8], 'burlapDark');
cube('torso', [-8.0, 2.2, -4.2], [16.0, 4.6, 8.4], 'burlap');
cube('torso', [-5.4, 14.0, -4.1], [3.0, 2.4, 0.6], 'patchA');
cube('torso', [2.0, 7.8, -4.4], [3.2, 2.6, 0.6], 'patchB');
/* 颈部稻草圈 */
for (let i = 0; i < 8; i++) {
    const a = Math.PI * 2 * i / 8;
    cube('torso', [Math.cos(a) * 6.4 - 0.9, 18.4, Math.sin(a) * 4.0 - 0.9], [1.8, 1.6, 1.8], 'straw');
}
/* 下摆稻草(向外下方散开) */
for (let i = -3; i <= 3; i++) {
    cube('torso', [i * 2.4 - 0.9, 1.0, -3.6], [1.8, 2.6, 1.8], 'straw', [0, 0, i * 12]);
    cube('torso', [i * 2.4 - 0.9, 1.2, 3.2], [1.8, 2.6, 1.8], 'straw', [0, 0, -i * 12]);
}
/* 彩虹披风(四段, 错位下垂) */
const cape = [[-7.0, 19.6, 3.6, 14.0, 4.6, 'rainbowA'], [-8.2, 15.4, 4.4, 16.4, 4.2, 'rainbowB'],
              [-9.4, 11.0, 5.2, 18.8, 4.0, 'rainbowC'], [-10.8, 6.2, 6.0, 21.6, 4.8, 'rainbowA']];
cape.forEach((c, i) => cube('cape' + (i + 1), [c[0], c[1], c[2] * 0.9], [c[3], c[4], 0.9], c[5]));
/* 腿(稻草) + 土脚 */
cube('leg_l', [-5.0, 0, -2.4], [4.2, 3.4, 4.6], 'soil' in M ? 'soil' : 'woodDark');
cube('leg_r', [0.8, 0, -2.4], [4.2, 3.4, 4.6], 'woodDark');
cube('leg_l', [-4.6, 3.0, -2.0], [3.4, 4.4, 3.8], 'straw');
cube('leg_r', [1.2, 3.0, -2.0], [3.4, 4.4, 3.8], 'straw');

/* ================= ② 头: 答辩(💩 螺旋堆叠) ================= */
/* 5 层: 逐级收窄 + 左右错位 + 各自绕 y 微旋 -> 螺旋感 */
cube('head', [-6.0, 22.2, -5.2], [12.0, 3.6, 10.4], 'poop',   [0, 8, 0]);
cube('head', [-5.4, 25.2, -4.8], [10.8, 3.4, 9.4], 'poopDark', [0, -14, 0]);
cube('head', [-4.4, 28.0, -4.2], [8.8, 3.2, 8.0], 'poop',   [0, 20, 0]);
cube('head', [-3.2, 30.6, -3.2], [6.4, 2.6, 5.8], 'poopLight', [0, -16, 0]);
cube('head', [-1.9, 32.8, -1.9], [3.8, 3.2, 3.6], 'poop',   [0, 24, 0]);
/* 彩虹螺旋油膜带(缠在答辩上) */
cube('head', [-6.2, 23.0, -5.4], [12.4, 0.9, 0.7], 'rainbowA', [0, 8, 0]);
cube('head', [-5.6, 26.0, -5.0], [11.2, 0.9, 0.7], 'rainbowB', [0, -14, 0]);
cube('head', [-4.6, 28.8, -4.4], [9.2, 0.9, 0.7], 'rainbowC', [0, 20, 0]);
cube('head', [5.4, 25.4, -4.6], [0.7, 0.9, 3.4], 'rainbowA', [0, -14, 0]);
cube('head', [-6.4, 27.9, -3.2], [0.7, 0.9, 3.6], 'rainbowB', [0, 20, 0]);
/* 脸(番茄眼 + 大笑 + 门牙) */
cube('head', [-3.8, 26.2, -5.2], [2.6, 2.6, 0.8], 'tomato');
cube('head', [1.2, 26.2, -5.2], [2.6, 2.6, 0.8], 'tomato');
cube('head', [-3.2, 26.8, -5.6], [1.0, 1.0, 0.4], 'eyeWhite');
cube('head', [1.8, 26.8, -5.6], [1.0, 1.0, 0.4], 'eyeWhite');
cube('head', [-2.6, 23.6, -5.3], [5.2, 1.0, 0.7], 'mouth', [0, 0, 0]);       // 嘴
cube('head', [-1.6, 24.4, -5.3], [3.2, 0.7, 0.7], 'mouth');                  // 上扬嘴角
cube('head', [-1.0, 23.2, -5.4], [0.9, 1.1, 0.6], 'eyeWhite');               // 门牙
cube('head', [0.2, 23.2, -5.4], [0.9, 1.1, 0.6], 'eyeWhite');

/* ================= ③ 手臂 / 农具 / 加农炮 / 苍蝇 ================= */
cube('arm_l', [-12.4, 15.0, -2.0], [4.0, 4.0, 4.0], 'burlap');
cube('arm_l', [-11.8, 10.4, -1.8], [3.4, 4.8, 3.6], 'burlapDark');
for (let i = -1; i <= 1; i++) cube('arm_l', [-11.6, 9.4, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');   // 袖口稻草
cube('fork', [-10.6, 4.2, -1.6], [2.0, 12.0, 2.0], 'wood');
for (let i = -1; i <= 1; i++) cube('fork', [-10.8 + i * 1.7, -1.6, -1.5], [0.9, 6.2, 1.1], 'metal');
cube('arm_r', [8.4, 15.0, -2.0], [4.0, 4.0, 4.0], 'burlap');
cube('arm_r', [8.4, 10.4, -1.8], [3.4, 4.8, 3.6], 'burlapDark');
for (let i = -1; i <= 1; i++) cube('arm_r', [9.4, 9.4, -1.6 + i * 1.5], [2.0, 1.8, 1.4], 'straw');
cube('shovel', [9.0, 4.6, -1.6], [1.9, 11.4, 1.9], 'wood');
cube('shovel', [8.2, -1.6, -2.0], [3.4, 6.4, 3.0], 'metalDark');
/* 右肩土豆加农炮: 炮口朝前(-z) + 黄铜箍 + 料斗 */
cube('cannon', [5.0, 21.6, -10.0], [4.0, 4.0, 11.0], 'metal');          // 炮管(朝前)
cube('cannon', [4.6, 21.2, -11.6], [4.8, 4.8, 1.6], 'brass');           // 炮口箍
cube('cannon', [4.6, 21.2, -4.4], [4.8, 4.8, 1.6], 'brass');            // 后箍
cube('cannon', [4.0, 19.4, -6.6], [6.0, 3.0, 5.0], 'metalDark');        // 机匣
cube('cannon', [5.2, 25.4, -7.0], [3.6, 3.0, 3.6], 'metalDark');        // 料斗
cube('cannon', [5.8, 26.6, -6.4], [2.4, 1.8, 2.4], 'poop');             // 料斗里的"弹药"
/* 苍蝇 x2 */
cube('fly1', [-10.0, 32.4, -7.0], [1.6, 1.6, 1.6], 'fly');
cube('fly1', [-10.8, 33.4, -7.4], [1.8, 0.5, 1.2], 'fly');
cube('fly2', [10.4, 30.4, -7.4], [1.6, 1.6, 1.6], 'fly');
cube('fly2', [11.2, 31.4, -7.8], [1.8, 0.5, 1.2], 'fly');

/* ================= 画贴图 ================= */
for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}
/* ================= 输出 ================= */
const PIV = {
    root: [0, 0, 0], leg_l: [-3.0, 3.0, 0], leg_r: [3.0, 3.0, 0], pole: [0, 0, 2.2], crossbar: [0, 21.4, 0],
    torso: [0, 6.0, 0], head: [0, 22.2, 0], arm_l: [-10.4, 17.0, 0], arm_r: [10.4, 17.0, 0],
    fork: [-10.6, 16.0, 0], shovel: [9.0, 16.0, 0],
    cape1: [0, 21.6, 3.6], cape2: [0, 17.4, 4.4], cape3: [0, 13.0, 5.2], cape4: [0, 8.0, 6.0],
    cannon: [5.0, 23.6, -5.0], fly1: [-10.0, 32.4, -7.0], fly2: [10.4, 30.4, -7.4]
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
const geo = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.compost_superman', texture_width: W, texture_height: H, visible_bounds_width: 3, visible_bounds_height: 4, visible_bounds_offset: [0, 1.6, 0] }, bones }] };
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'compost_superman.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'compost_superman.png'));
console.log(`已生成块面定稿 v2: cube=${C.length} 骨骼=${bones.length} 贴图=${W}x${H} -> ${OUT}`);
