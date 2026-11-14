/* 苹果爵士·蜜脆 (Sir Honeycrisp) —— 第 8 位 Boss 的块面定稿 + 手绘贴图(2026-09-12 · v5 精细骨骼)
 *
 * v5 变化(2026-09-12 按用户要求"骨骼做精细一点方便模型活动"):
 *   · 果身**分成两节**: body(下半 y8.5~18.2) -> upper(上半/脸/肩 y17.8~25), 腰在 y18 可以弯/扭
 *   · 四条根腿**各拆大腿/小腿**(带膝), 走路能屈膝
 *   · 手臂加**手掌**(hand_l/hand_r), 叶刃能独立翻转
 *   · 叶斗篷拆**3 节链式**(cape1->cape2->cape3), 甩尾有布料感
 *   · 金腰带(belt) + 苹果徽章扣(buckle) —— 2026-09-12 按用户要求把金链改成"现实苹果包装带" *   · **去掉眼睛**, 只留墨镜后的两个眼窝(打飞墨镜才露出来)
 *   · **眉毛/嘴独立**骨骼(表情), **墨镜/礼帽/蜡盖**各自独立
 *   · 胸口虫洞内新增**2 节虫子**(worm1->worm2), 一阶段藏果身里, 二阶段钻出来
 *   · 头顶 3 颗小苹果**各自独立**(orbit1/2/3)
 * 输出: geo json + 256x256 手绘贴图
 * 用法: node gen_apple_boss.cjs <输出目录>
 */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const OUT = process.argv[2] || 'out_apple_boss';
const W = 256, H = 256;
const cv = new Canvas(W, H);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) cv.set(x, y, 0, 0, 0, 0);
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);

const M = {
    apple: [198, 54, 52], appleLight: [236, 104, 88], appleDark: [142, 32, 38],
    appleWax: [232, 208, 168],
    stem: [116, 78, 46], stemDark: [82, 54, 32],
    leaf: [92, 168, 66], leafDark: [58, 118, 46], leafLight: [140, 206, 96],
    bark: [126, 90, 58], barkDark: [88, 60, 38],
    hole: [26, 20, 18], holeEdge: [56, 34, 30],
    worm: [216, 232, 170], wormDark: [162, 190, 116], wormMouth: [198, 88, 96],   // 2026-09-12: 改成淡黄绿, 在褐色腐烂果身上更显眼
    eyeWhite: [246, 246, 240], pupil: [28, 22, 20], brow: [74, 24, 26],
    mouth: [58, 30, 28],
    hat: [40, 38, 44], hatDark: [24, 22, 28],
    gold: [228, 184, 66], goldDark: [166, 124, 36], goldLight: [252, 222, 122],
    lens: [34, 38, 48], lensGlint: [186, 206, 226]
};
function paint(mat, px, py, x, y) {
    const c = M[mat] || [255, 0, 255];
    if (mat === 'appleWax' || mat === 'eyeWhite' || mat === 'worm' || mat === 'goldLight') {
        cv.set(px, py, c[0], c[1], c[2], 1);
    } else {
        const n = nz(x, y, 7) * 22 - 11;
        cv.set(px, py, Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)),
            Math.max(0, Math.min(255, c[2] + n)), 1);
    }
}
let penX = 1, penY = 1, rowH = 0;
/* 二阶段「虫蛀形态」: 只把果皮/叶/蜡换成腐烂色, 礼帽/墨镜/金腰带保持不变 */
const SKIN = (process.argv[3] || '').toLowerCase();
if (SKIN === 'p2') {
    M.apple = [96, 66, 40]; M.appleLight = [140, 104, 56]; M.appleDark = [56, 38, 24];   // 2026-09-12: 加深枯褐, 让二阶段一眼能看出来
    M.appleWax = [178, 162, 126];
    M.leaf = [104, 118, 54]; M.leafDark = [66, 82, 38]; M.leafLight = [148, 152, 78];
    M.stem = [96, 66, 40]; M.stemDark = [68, 46, 28];
}
const C = [];
function alloc(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > W) { penX = 1; penY += rowH + 2; rowH = 0; }
    if (penY + fh > H) throw new Error('贴图放不下: ' + fw + 'x' + fh + ' @' + penX + ',' + penY);
    const u = penX, v = penY;
    penX += fw + 2; rowH = Math.max(rowH, fh);
    return [u, v];
}
function cube(bone, o, s, mat, rot) { const uv = alloc(s[0], s[1], s[2]); C.push({ bone, o, s, uv, mat, rot }); }

/* ================= 造型(方块) =================
   y 向上, 前方 = -z, 单位 1/16 格; 腰线 y=18 */
const BD = 'body', UP = 'upper';

/* ---------- 四条根腿: 大腿(up) + 小腿(lo, 带根盘) ---------- */
const LEGS = [['leg_fl', -4.8, -4.8], ['leg_fr', 4.8, -4.8], ['leg_bl', -4.8, 4.8], ['leg_br', 4.8, 4.8]];
for (const [nm, ix, iz] of LEGS) {
    cube(nm + '_up', [ix - 1.8, 4.0, iz - 1.8], [3.6, 4.2, 3.6], 'bark');
    cube(nm + '_lo', [ix - 1.8, 0.0, iz - 1.8], [3.6, 4.2, 3.6], 'barkDark');
    cube(nm + '_lo', [ix - 2.6, 0.0, iz - 2.6], [5.2, 2.4, 5.2], 'barkDark');
}
/* ---------- 下半果 (body, y 8.5~18.2) ---------- */
cube(BD, [-10, 8.5, -9], [20, 9.7, 18], 'apple');
for (const sx of [-1, 1]) for (const sz of [-1, 1]) {
    cube(BD, [sx * 9.4 - (sx > 0 ? 0 : 4.6), 9.4, sz * 8.4 - (sz > 0 ? 0 : 4.6)], [4.6, 8.8, 4.6], 'appleLight');
}
cube(BD, [-3.2, 7.4, -3.2], [6.4, 1.6, 6.4], 'appleDark');              // 底部凹陷
cube(BD, [-9.2, 11.0, -9.12], [3.0, 5.0, 0.25], 'appleLight');          // 果皮明暗(用浅红, 不是一块奶白贴纸)
/* ---------- 上半果 (upper, y 17.8~25 + 圆肩) ----------
   2026-09-12 修闪烁: 上半果比下半果**各向外 0.05**(20.1/18.1), 避免两节的侧面**完全共面** z-fighting;
   同时保留 0.4 的重叠, 免得弯腰时露出接缝 */
cube(UP, [-10.05, 17.8, -9.05], [20.1, 7.2, 18.1], 'apple');
cube(UP, [-8.6, 23.4, -7.8], [17.2, 3.4, 15.6], 'apple');               // 圆肩
for (const sx of [-1, 1]) for (const sz of [-1, 1]) {
    cube(UP, [sx * 9.4 - (sx > 0 ? 0 : 4.7), 17.5, sz * 8.4 - (sz > 0 ? 0 : 4.7)], [4.7, 6.0, 4.7], 'appleLight');
}
cube(UP, [-3.5, 25.4, -3.5], [7, 2.2, 7], 'appleDark');                 // 顶部凹陷
/* ---------- 果梗 + 叶片(两节) ---------- */
cube('stem', [-1.2, 26.2, 2.2], [2.4, 7.0, 2.4], 'stem', [14, 0, 6]);
cube('stem', [-1.7, 32.4, 2.8], [3.4, 1.8, 3.4], 'stemDark');
cube('leaf', [-0.8, 31.0, 2.0], [6.0, 0.8, 6.6], 'leaf', [12, 0, -18]);
cube('leaf2', [5.0, 30.4, 2.6], [6.2, 0.7, 5.4], 'leafLight', [12, 0, -26]);
/* ---------- 立领 + 3 节链式斗篷 ---------- */
/* 立领: 比肩块**各向外 0.15**(17.5/15.9), 免得侧面与肩块共面 */
cube(UP, [-8.75, 24.8, -7.95], [17.5, 2.2, 15.9], 'leafDark');
cube(UP, [-7.0, 26.6, -6.4], [14.0, 1.2, 12.8], 'leaf');
for (const sx of [-1, 1]) {
    cube(UP, [sx * 8.2 - (sx > 0 ? 0 : 5.2), 22.6, -3.4], [5.2, 2.0, 7.6], 'leaf', [0, 0, sx * 16]);
}
cube('cape1', [-9.0, 19.0, 8.6], [18, 6.0, 1.3], 'leafDark');
cube('cape2', [-8.0, 13.4, 9.0], [16, 6.4, 1.2], 'leaf');
cube('cape3', [-6.8, 7.8, 9.4], [13.6, 6.0, 1.1], 'leafDark');
/* ---------- 脸: 眼窝(墨镜打飞才露出) + 怒眉 + 嘴 ----------
   2026-09-12 修穿模: 这些件原来整体浮在脸面(z=-9.05)之外 0.55~1.15, 侧面看就是戳在脸上的贴片;
   现在一律只外凸 0.07~0.3, 其余埋进果身里。 */
for (const sx of [-1, 1]) {
    cube(UP, [sx * 5.4 - (sx > 0 ? 0 : 4.0), 20.8, -9.12], [4.0, 3.2, 0.55], 'hole');
}
cube('brow_l', [-6.4, 25.2, -9.35], [4.8, 1.5, 0.5], 'brow', [0, 0, -16]);
cube('brow_r', [1.6, 25.2, -9.35], [4.8, 1.5, 0.5], 'brow', [0, 0, 16]);
cube('mouth', [-3.0, 18.4, -9.12], [6.0, 1.2, 0.5], 'mouth');
/* ---------- 胸口: 虫洞 + 蜡盖 + 洞里的虫子(3 节, 二阶段钻出来) ---------- */
cube(BD, [-3.8, 8.6, -9.9], [7.6, 6.0, 0.9], 'holeEdge');
cube(BD, [-2.8, 9.2, -10.3], [5.6, 4.8, 0.5], 'hole');
cube('lid', [-4.0, 8.4, -10.9], [8.0, 6.0, 1.2], 'appleWax');
cube('lid', [-1.1, 9.4, -11.5], [2.2, 2.2, 0.7], 'stemDark');
/* 虫子(2026-09-12 加大加亮: 原来 2 节太小, 钻出来也看不清) */
cube('worm1', [-2.3, 9.2, -9.8], [4.6, 4.6, 3.6], 'worm');
cube('worm2', [-2.1, 12.6, -9.8], [4.2, 4.2, 3.4], 'worm');
cube('worm3', [-1.9, 15.8, -9.9], [3.8, 3.8, 3.2], 'worm');
cube('worm3', [-1.5, 16.8, -10.4], [1.0, 1.0, 0.5], 'pupil');
cube('worm3', [0.5, 16.8, -10.4], [1.0, 1.0, 0.5], 'pupil');
cube('worm3', [-1.0, 17.8, -10.5], [2.0, 0.6, 0.4], 'wormMouth');
/* ---------- ★ 三件套: 礼帽 / 金腰带(苹果包装式) / 墨镜 ---------- */
cube('hat', [-7.6, 26.2, -7.6], [15.2, 1.1, 15.2], 'hat');
cube('hat', [-4.9, 27.0, -4.9], [9.8, 11.5, 9.8], 'hat');
cube('hat', [-4.5, 37.9, -4.5], [9.0, 1.0, 9.0], 'hatDark');
cube('hat', [-5.15, 27.3, -5.15], [10.3, 2.2, 10.3], 'gold');
cube('hat', [-1.2, 27.6, -5.6], [2.4, 1.7, 0.6], 'goldLight');
/* 金腰带: 绕果身一圈(前后左右 4 条带) + 上沿高光
   2026-09-12 修闪烁: 带子**往果身里再压 0.35**(内侧面落到果身内部), 不再与果身表面共面 */
cube('belt', [-10.45, 14.0, -10.45], [20.9, 3.6, 1.75], 'gold');
cube('belt', [-10.45, 14.0, 8.7], [20.9, 3.6, 1.75], 'gold');
cube('belt', [-10.45, 14.0, -9.0], [1.75, 3.6, 18.0], 'goldDark');
cube('belt', [8.7, 14.0, -9.0], [1.75, 3.6, 18.0], 'goldDark');
cube('belt', [-10.2, 16.8, -10.6], [20.4, 0.8, 0.6], 'goldLight');
cube('belt', [-10.2, 14.2, -10.6], [20.4, 0.6, 0.6], 'goldLight');
/* 腰带扣: 金扣 + 红苹果徽章 + 小叶片(独立骨骼, 会跟身体轻微晃动) */
cube('buckle', [-3.2, 13.4, -11.5], [6.4, 4.8, 1.4], 'gold');
cube('buckle', [-2.6, 14.0, -12.2], [5.2, 3.6, 0.7], 'goldLight');
cube('buckle', [-1.7, 14.4, -12.8], [3.4, 3.0, 0.6], 'apple');
cube('buckle', [-1.1, 14.9, -13.2], [1.4, 1.2, 0.4], 'appleLight');
cube('buckle', [-0.4, 17.4, -12.9], [0.9, 1.4, 0.6], 'leaf');
/* 墨镜: 上下框 + 镜片(反光) + 镜腿
   2026-09-12 三次修(用户反馈"墨镜穿模"):
   ① 整体再贴近脸面(脸在 z=-9.05, 现在框/镜片只凸出 0.85);
   ② 镜腿前端顶到镜框背面(z=-9.9 起), 不再与框之间留 0.2 的缝;
   ③ 反光点贴在镜片前面 0.05, 不再悬空。 */
cube('glasses', [-7.6, 24.0, -9.9], [15.2, 1.1, 1.0], 'hatDark');
cube('glasses', [-7.6, 19.7, -9.9], [15.2, 1.0, 1.0], 'hatDark');
for (const sx of [-1, 1]) {
    cube('glasses', [sx * 4.0 - (sx > 0 ? 0 : 6.6), 20.6, -9.9], [6.6, 3.4, 0.7], 'lens');
    cube('glasses', [sx * 4.0 - (sx > 0 ? 0 : 2.6), 22.4, -9.95], [2.6, 1.0, 0.15], 'lensGlint');
    cube('glasses', [sx * 9.8 - (sx > 0 ? 0 : 1.0), 20.4, -9.9], [1.0, 1.0, 5.6], 'hatDark');
}
/* ---------- 手臂: 上臂(带树瘤) -> 前臂 -> 手掌(叶刃) ---------- */
for (const sx of [-1, 1]) {
    const up = sx < 0 ? 'arm_l' : 'arm_r', fo = sx < 0 ? 'fore_l' : 'fore_r', hd = sx < 0 ? 'hand_l' : 'hand_r';
    cube(up, [sx * 13.0 - (sx > 0 ? 0 : 3.6), 17.0, -1.8], [3.6, 7.0, 3.6], 'bark', [0, 0, sx * 8]);
    cube(up, [sx * 13.4 - (sx > 0 ? 0 : 3.2), 22.4, -1.8], [3.2, 2.4, 3.2], 'barkDark');
    cube(up, [sx * 14.7 - (sx > 0 ? 0 : 1.3), 19.6, -3.2], [1.3, 1.8, 1.3], 'barkDark');   // 树瘤
    cube(up, [sx * 14.7 - (sx > 0 ? 0 : 1.3), 16.4, 0.6], [1.3, 1.5, 1.3], 'barkDark');   // 树瘤
    cube(fo, [sx * 13.8 - (sx > 0 ? 0 : 2.9), 11.2, -1.6], [2.9, 6.4, 2.9], 'barkDark', [0, 0, -sx * 6]);
    cube(hd, [sx * 15.0 - (sx > 0 ? 0.3 : 0), 5.4, -4.2], [0.9, 9.0, 4.2], 'leaf', [0, 0, -sx * 20]);
    cube(hd, [sx * 15.6 - (sx > 0 ? 0.3 : 0), 4.8, -6.8], [0.8, 6.0, 2.6], 'leafLight', [0, 0, -sx * 26]);
    cube(hd, [sx * 14.4 - (sx > 0 ? 0 : 2.4), 10.0, -1.4], [2.4, 2.0, 2.4], 'bark', [0, 0, -sx * 10]);
}
/* ---------- 左右两侧细节(2026-09-12 按用户要求"左右两边精细一点") ----------
   原来两侧就是一大块红板。加: 侧鳍叶片 ×2/边 + 侧面果蜡高光 + 右侧虫蛀缺口 + 侧根须 */
for (const sx of [-1, 1]) {
    /* 侧鳍叶片: **根部往果身里插 3.0 单位**(带旋转也绝不会离体 —— 之前 1.1 薄片看着像悬空的虫子) */
    cube(BD, [sx * 10.05 - (sx > 0 ? 0 : 3.0), 11.2, -3.0], [3.0, 4.6, 8.0], 'leaf', [0, 0, sx * -16]);
    cube(BD, [sx * 10.05 - (sx > 0 ? 0 : 2.6), 9.4, 1.5], [2.6, 3.2, 5.0], 'leafLight', [0, 0, sx * -22]);
    /* 侧面明暗: 用浅红做果皮渐变, 不再用奶白(否则像外挂一块板) */
    cube(BD, [sx * 10.05 - (sx > 0 ? 0 : 0.5), 10.0, -4.5], [0.5, 6.5, 4.0], 'appleLight');
    cube(BD, [sx * 10.05 - (sx > 0 ? 0 : 0.4), 10.5, 1.5], [0.4, 4.0, 3.0], 'appleLight');
    /* 侧根须(插进果身 2.6) */
    cube(BD, [sx * 10.0 - (sx > 0 ? 0 : 2.6), 8.6, 2.0], [2.6, 1.5, 1.5], 'bark', [0, 0, sx * 14]);
    cube(BD, [sx * 10.0 - (sx > 0 ? 0 : 2.2), 8.9, -3.4], [2.2, 1.3, 1.3], 'barkDark', [0, 0, sx * -10]);
}
/* 右侧"被咬过"的缺口(呼应虫蛀主题, 只外凸 0.25) */
cube(BD, [9.3, 12.4, -2.2], [1.0, 3.2, 3.2], 'appleDark');
cube(BD, [9.3, 13.6, 1.4], [1.0, 2.2, 2.2], 'appleDark');
cube(BD, [9.3, 11.0, 1.0], [1.0, 1.8, 1.8], 'hole');
/* ---------- 头顶 3 颗小苹果(各自独立骨骼) ----------
   注意: 这里**不要**用 [[0,0],[1,120],[2,240]].entries() —— 那样 a 拿到的是数组,
   角度算出 NaN -> 坐标 NaN -> JSON 里成为 null -> GeckoLib JsonNull 直接让加载条卡死 */
const ORBIT_DEG = [0, 120, 240];
ORBIT_DEG.forEach((deg, i) => {
    const r = 15, rad = deg * Math.PI / 180;
    const x = Math.cos(rad) * r, z = Math.sin(rad) * r;
    const nm = 'orbit' + (i + 1);
    cube(nm, [x - 2.1, 32.0, z - 2.1], [4.2, 4.2, 4.2], 'appleLight');
    cube(nm, [x - 0.5, 35.6, z - 0.5], [1.0, 2.4, 1.0], 'stem');
    cube(nm, [x + 0.4, 36.6, z - 1.6], [2.4, 0.6, 2.0], 'leaf');
});

/* ================= 画贴图 ================= */
for (const c of C) {
    const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
    for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) paint(c.mat, c.uv[0] + x, c.uv[1] + y, x, y);
}

/* ================= 输出 geo ================= */
const mk = (bone) => C.filter((c) => c.bone === bone).map((c) => {
    const o = { origin: c.o, size: c.s, uv: c.uv };
    if (c.rot) o.rotation = c.rot;
    return o;
});
/* 骨骼树: 名字 -> [父骨骼, 轴心] */
const TREE = {
    root: [null, [0, 0, 0]],
    body: ['root', [0, 8.5, 0]],
    upper: ['body', [0, 18, 0]],
    /* 腿(挂在下半果上) */
    leg_fl_up: ['body', [-4.8, 8.2, -4.8]], leg_fl_lo: ['leg_fl_up', [-4.8, 4.2, -4.8]],
    leg_fr_up: ['body', [4.8, 8.2, -4.8]], leg_fr_lo: ['leg_fr_up', [4.8, 4.2, -4.8]],
    leg_bl_up: ['body', [-4.8, 8.2, 4.8]], leg_bl_lo: ['leg_bl_up', [-4.8, 4.2, 4.8]],
    leg_br_up: ['body', [4.8, 8.2, 4.8]], leg_br_lo: ['leg_br_up', [4.8, 4.2, 4.8]],
    /* 上半果附属 */
    lid: ['body', [0, 8.6, -9.4]],
    worm1: ['body', [0, 11.5, -9.8]], worm2: ['worm1', [0, 14.7, -9.8]], worm3: ['worm2', [0, 17.7, -9.9]],
    stem: ['upper', [0, 26.2, 2.2]], leaf: ['stem', [0, 31, 2.0]], leaf2: ['leaf', [5.0, 30.4, 2.6]],
    cape1: ['upper', [0, 25, 8.6]], cape2: ['cape1', [0, 19.8, 9.0]], cape3: ['cape2', [0, 14.2, 9.4]],
    brow_l: ['upper', [-4.0, 25.9, -10.9]], brow_r: ['upper', [4.0, 25.9, -10.9]],
    mouth: ['upper', [0, 19.0, -10.6]],
    glasses: ['upper', [0, 22, -11.0]],
    hat: ['upper', [0, 26.4, 0]],
    belt: ['body', [0, 15.8, 0]], buckle: ['belt', [0, 15.8, -11.5]],
    arm_l: ['upper', [-13, 22.5, 0]], fore_l: ['arm_l', [-13.8, 17.6, 0]], hand_l: ['fore_l', [-14.4, 11.5, 0]],
    arm_r: ['upper', [13, 22.5, 0]], fore_r: ['arm_r', [13.8, 17.6, 0]], hand_r: ['fore_r', [14.4, 11.5, 0]],
    orbit1: ['upper', [0, 34, 0]], orbit2: ['upper', [0, 34, 0]], orbit3: ['upper', [0, 34, 0]],
};
const ORDER = Object.keys(TREE);
const bones = ORDER.map((n) => {
    const [parent, pivot] = TREE[n];
    const b = { name: n, pivot };
    if (parent) b.parent = parent;
    const cubes = mk(n);
    if (cubes.length) b.cubes = cubes;
    return b;
});
const geo = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
        description: {
            identifier: 'geometry.apple_boss',
            texture_width: W, texture_height: H,
            visible_bounds_width: 3, visible_bounds_height: 3.5, visible_bounds_offset: [0, 1.4, 0]
        },
        bones
    }]
};
fs.mkdirSync(OUT, { recursive: true });
if (SKIN === 'p2') {
    /* 二阶段: 几何完全一样, 只多一张腐烂贴图 */
    cv.save(path.join(OUT, 'apple_boss_p2.png'));
    console.log('苹果爵士二阶段(虫蛀/腐烂)贴图已生成: apple_boss_p2.png ' + W + 'x' + H);
} else {
    fs.writeFileSync(path.join(OUT, 'apple_boss.geo.json'), JSON.stringify(geo, null, 1));
    cv.save(path.join(OUT, 'apple_boss.png'));
    const empty = bones.filter((b) => !b.cubes).map((b) => b.name);
    console.log('苹果爵士(精细骨骼): cube=' + C.length + ' bones=' + bones.length +
        ' 贴图=' + W + 'x' + H + ' 图集到 y=' + (penY + rowH) + (empty.length ? '  空骨骼: ' + empty.join(',') : ''));
}
