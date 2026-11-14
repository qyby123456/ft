/* 炫彩答辩超人 —— 三视图用块面定稿(2026-09-12)
 * 造型: 堆肥身体 + 番茄眼 + 彩虹油膜带/披风 + 胸口发光嫩芽 + 左手草叉 + 右手铁铲
 *       + 右肩扛【机械动力土豆加农炮】(发射大便) + 头边环绕苍蝇
 * 输出: geo json + 128x128 手绘风贴图(褐色堆肥 + 彩虹条纹 + 番茄红 + 嫩芽绿 + 金属灰)
 * 用法: node gen_compost.cjs <输出目录>
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_compost_sketch';
const W = 128, H = 128;
const cv = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) cv.set(x, y, 0, 0, 0, 0);
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);

/* ================= 配色 ================= */
const M = {
    compost: [104, 72, 44], compostDark: [72, 48, 28], compostLight: [138, 100, 62],
    soil: [86, 62, 40], rainbowA: [232, 74, 190], rainbowB: [86, 226, 232], rainbowC: [246, 222, 96],
    tomato: [214, 58, 48], tomatoLeaf: [96, 168, 72], sprout: [126, 226, 118],
    metal: [176, 182, 190], metalDark: [116, 122, 132], wood: [148, 106, 62],
    fly: [46, 42, 50], eyeWhite: [246, 246, 240], mouth: [58, 34, 26]
};
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    const n = nz(x, y, 7) * 22 - 11;
    if (mat === 'rainbowA' || mat === 'rainbowB' || mat === 'rainbowC' || mat === 'sprout') {
        cv.set(px, py, c[0], c[1], c[2], 1);            // 彩虹/嫩芽: 纯色不噪, 保持"油膜"感
    } else {
        cv.set(px, py, Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)), Math.max(0, Math.min(255, c[2] + n)), 1);
    }
}
/* ================= UV 分配 ================= */
let penX = 1, penY = 1, rowH = 0;
const C = [];
function alloc(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > W) { penX = 1; penY += rowH + 2; rowH = 0; }
    if (penY + fh > H) throw new Error('贴图放不下: ' + fw + 'x' + fh);
    const u = penX, v = penY;
    penX += fw + 2; rowH = Math.max(rowH, fh);
    return [u, v, fw, fh];
}
function cube(bone, o, s, mat) { const uv = alloc(s[0], s[1], s[2]); C.push({ bone, o, s, uv, mat }); }

/* ================= 造型(方块) =================
 * 坐标系: 脚底 y=0, 身高约 30(约 2.6 格 = 41.6px, 这里按 1格=8px 的 4 倍缩放便于搭建) */
/* --- 腿脚(土块) --- */
cube('leg_l', [-5.2, 0, -2.4], [4.4, 6.0, 4.8], 'soil');
cube('leg_r', [0.8, 0, -2.4], [4.4, 6.0, 4.8], 'soil');
/* --- 躯干: 三层堆肥, 越上越窄 --- */
cube('torso', [-7.6, 5.6, -4.2], [15.2, 6.4, 8.4], 'compost');
cube('torso', [-6.8, 11.6, -3.8], [13.6, 6.0, 7.6], 'compostDark');
cube('torso', [-6.0, 17.2, -3.4], [12.0, 5.0, 6.8], 'compostLight');
/* 彩虹油膜: 每层边缘一圈细条, 三色交替 */
for (let i = 0; i < 3; i++) {
    const y = 6.2 + i * 5.8, w = 15.4 - i * 1.6;
    cube('torso', [-w / 2, y, 4.0], [w, 0.7, 0.5], i === 0 ? 'rainbowA' : i === 1 ? 'rainbowB' : 'rainbowC');
    cube('torso', [-w / 2, y, -4.5], [w, 0.7, 0.5], i === 1 ? 'rainbowA' : i === 2 ? 'rainbowB' : 'rainbowC');
}
/* --- 胸口发光嫩芽(核心) --- */
cube('chest', [-0.9, 15.6, -4.2], [1.8, 5.0, 1.8], 'sprout');
cube('chest', [-3.0, 19.4, -3.7], [3.0, 1.3, 1.3], 'sprout');
cube('chest', [0.2, 20.6, -3.7], [3.2, 1.3, 1.3], 'sprout');
/* --- 头: 木桶+粪堆 --- */
cube('head', [-5.6, 22.0, -4.0], [11.2, 7.6, 8.0], 'compost');
cube('head', [-6.2, 28.6, -4.4], [12.4, 1.6, 8.8], 'wood');            // 桶沿
cube('head', [-2.4, 29.6, -2.4], [4.8, 2.6, 4.8], 'compostDark');       // 桶顶粪尖
/* 番茄眼 + 微笑 */
cube('head', [-4.0, 25.4, -4.6], [2.6, 2.6, 0.9], 'tomato');
cube('head', [1.4, 25.4, -4.6], [2.6, 2.6, 0.9], 'tomato');
cube('head', [-3.4, 26.0, -5.0], [1.0, 1.0, 0.5], 'eyeWhite');
cube('head', [2.0, 26.0, -5.0], [1.0, 1.0, 0.5], 'eyeWhite');
cube('head', [-2.6, 22.9, -4.6], [5.2, 0.9, 0.7], 'mouth');
/* 彩虹油膜头带(三色短条) */
for (let i = 0; i < 3; i++) cube('head', [-5.8 + i * 3.9, 27.4, -4.7], [3.6, 1.0, 0.6], i === 0 ? 'rainbowA' : i === 1 ? 'rainbowB' : 'rainbowC');
/* --- 左臂 + 草叉 --- */
cube('arm_l', [-11.6, 14.0, -2.0], [4.2, 4.2, 4.2], 'compost');
cube('arm_l', [-11.2, 9.0, -1.8], [3.6, 5.4, 3.8], 'compostDark');
cube('fork', [-10.2, 4.0, -1.6], [2.2, 14.0, 2.2], 'wood');             // 叉柄
for (let i = -1; i <= 1; i++) cube('fork', [-10.4 + i * 1.8, -2.6, -1.5], [1.0, 6.6, 1.2], 'metal');  // 三齿
/* --- 右臂 + 铁铲 --- */
cube('arm_r', [7.4, 14.0, -2.0], [4.2, 4.2, 4.2], 'compost');
cube('arm_r', [7.6, 9.0, -1.8], [3.6, 5.4, 3.8], 'compostDark');
cube('shovel', [8.4, 4.4, -1.6], [2.0, 12.6, 2.0], 'wood');
cube('shovel', [7.6, -2.2, -2.2], [3.6, 7.0, 3.2], 'metalDark');
/* --- 彩虹油膜披风(四段, 越下越宽) --- */
const cape = [[-6.4, 21.6, 4.6, 12.8, 4.4, 'rainbowA'], [-7.4, 17.4, 5.0, 14.8, 4.2, 'rainbowB'],
              [-8.4, 13.0, 5.4, 16.8, 4.0, 'rainbowC'], [-9.6, 8.2, 5.8, 19.2, 4.6, 'rainbowA']];
cape.forEach((c, i) => cube('cape' + (i + 1), [c[0], c[1], c[2]], [c[3], c[4], 0.8], c[5]));
/* --- 右肩: 机械动力土豆加农炮 --- */
cube('cannon', [4.8, 21.0, -1.2], [5.4, 4.6, 9.0], 'metal');           // 炮身
cube('cannon', [3.6, 20.4, -3.4], [8.0, 5.6, 3.2], 'metalDark');       // 炮尾/装填口
cube('cannon', [5.8, 23.6, 2.4], [4.0, 3.2, 4.0], 'metalDark');        // 料斗
cube('cannon', [6.2, 24.6, 3.0], [3.2, 2.2, 3.2], 'compost');          // 料斗里的"弹药"
/* --- 头边苍蝇(x2) --- */
cube('fly1', [-9.0, 30.0, -6.0], [1.6, 1.6, 1.6], 'fly');
cube('fly1', [-9.8, 31.0, -6.4], [1.8, 0.5, 1.2], 'fly');
cube('fly2', [9.4, 27.4, -6.4], [1.6, 1.6, 1.6], 'fly');
cube('fly2', [10.2, 28.4, -6.8], [1.8, 0.5, 1.2], 'fly');

/* ================= 画贴图 ================= */
for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}
/* ================= 输出 ================= */
const PIV = {
    root: [0, 0, 0], leg_l: [-3.0, 6.0, 0], leg_r: [3.0, 6.0, 0], torso: [0, 6.0, 0], chest: [0, 15.6, 0],
    head: [0, 22.0, 0], arm_l: [-9.5, 16.0, 0], arm_r: [9.5, 16.0, 0], fork: [-10.2, 14.0, 0], shovel: [8.4, 14.0, 0],
    cape1: [0, 24.0, 4.6], cape2: [0, 19.4, 5.0], cape3: [0, 15.0, 5.4], cape4: [0, 10.0, 5.8],
    cannon: [4.8, 22.0, 0], fly1: [-9.0, 30.0, -6.0], fly2: [9.4, 27.4, -6.4]
};
const PAR = { leg_l: 'root', leg_r: 'root', torso: 'root', chest: 'torso', head: 'torso',
    arm_l: 'torso', arm_r: 'torso', fork: 'arm_l', shovel: 'arm_r',
    cape1: 'torso', cape2: 'cape1', cape3: 'cape2', cape4: 'cape3', cannon: 'torso', fly1: 'head', fly2: 'head' };
const bones = Object.keys(PIV).map(name => {
    const cs = C.filter(c => c.bone === name).map(c => ({ origin: c.o, size: c.s, uv: [c.uv[0], c.uv[1]] }));
    const b = { name, pivot: PIV[name] };
    if (cs.length) b.cubes = cs;
    if (PAR[name]) b.parent = PAR[name];
    return b;
});
const geo = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.compost_superman', texture_width: W, texture_height: H, visible_bounds_width: 3, visible_bounds_height: 4, visible_bounds_offset: [0, 1.5, 0] }, bones }] };
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, 'compost_superman.geo.json'), JSON.stringify(geo, null, 1));
cv.save(path.join(OUT, 'compost_superman.png'));
console.log(`已生成块面定稿: cube=${C.length} 骨骼=${bones.length} 贴图=${W}x${H} -> ${OUT}`);
