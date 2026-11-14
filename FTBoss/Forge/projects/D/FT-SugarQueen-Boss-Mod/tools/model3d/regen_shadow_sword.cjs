/* ============================================================================
 *  噬影之刃 · 裂瞳（v16）—— 按用户提供的 GPT 设计稿在**上一条生成脚本**上直接改：
 *   ① 大轮廓更干净(减少细碎凸起, 只留不对称锯齿/两处刻口)  ② 刃口=灰紫冷色亮边, 与黑剑身分开
 *   ③ 剑脊立体凸起(厚度分层: 薄刃 0.55 -> 刃面 0.90 -> 剑面 1.25 -> 剑脊 2.00)
 *   ④ 裂纹改"不规则分叉 + 局部亮点", 去掉连续霓虹灯带  ⑤ 护手 = 双侧暗影钩爪 + 中央竖瞳魔眼(正反都看得见)
 *   ⑥ 握柄=深色 + 斜向缠带 + 上下金属箍  ⑦ 柄首=多层黑底座包紫晶核
 *   颜色表用设计稿给的那 7 个(最深处 #100E18 保留紫灰, 不写纯黑)。
 *
 *  说明: 设计稿是"逐像素方块 + 之后合并同色共面"的做法; 本脚本直接就是**合并后的方块**(31 件),
 *        面数已经精简; 亮色不是真实自发光, 另出 `shadow_sword_e.png` 发光层(OptiFine/Iris `_e` 约定)。
 *  用法: node tools/model3d/regen_shadow_sword.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const { makeCanvas, makePacker, outline, checks, writeSafe, safeCopy } = require('./lib_itemtex.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const RES = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const MODEL = path.join(RES, 'models', 'item', 'shadow_sword.json');
const TEX = path.join(RES, 'textures', 'item', 'shadow_sword.png');
const EMISSIVE = path.join(RES, 'textures', 'item', 'shadow_sword_e.png');
const REF = path.join(ROOT, 'ref');

if (fs.existsSync(MODEL) && JSON.parse(fs.readFileSync(MODEL, 'utf8')).parent === 'builtin/entity') {
    throw new Error('暗影剑已经使用 GeckoLib 骨骼模型，拒绝用旧静态生成器覆盖。V22 请用 rebuild_shadow_sword_v22.cjs，详见 docs/SHADOW_SWORD_V22_REBUILD.md。');
}

const TEXW = 256, PPU = TEXW / 16;
const K = 0.62, V0 = 15.5, UVM = 0.09, OVER = 0.05;
const uOf = x => 8 + (x - 8) * K, vOf = y => V0 - y * K;
const X = x => uOf(x) * PPU, Y = y => vOf(y) * PPU;
const r4 = v => +(+v).toFixed(4);

/* ---------- 设计稿颜色表 ---------- */
const H = h => [parseInt(h.slice(1, 3), 16), parseInt(h.slice(3, 5), 16), parseInt(h.slice(5, 7), 16)];
const C1 = H('#100E18'), C2 = H('#221D2C'), C3 = H('#504759'), C4 = H('#563374'),
    C5 = H('#A96CDC'), C6 = H('#EAD5F5'), C7 = H('#55424E');
const A = (c, a) => [c[0], c[1], c[2], a === undefined ? 255 : a];

/* ==================== 1) 部件表 ====================
   [名字, x0, x1, y0, y1, 厚度z, 角色, inflate, 旋转, z中心(默认8)] */
const P = [];
const put = (n, x0, x1, y0, y1, d, role, inflate, rot, zc) => P.push({
    n, x0: r4(x0), x1: r4(x1), y0: r4(y0), y1: r4(y1), d: r4(d), role, inflate: inflate || 0, rot: rot || null, zc: zc === undefined ? 8 : zc,
});

/* 柄首: 多层黑底座 + 双侧凸出的紫晶核 */
put('pommel_base', 6.7, 9.3, 0.0, 1.0, 2.7, 'pommel');           // ① 下层宽底座
put('pommel_crystal', 7.0, 9.0, 1.05, 1.75, 2.9, 'crystal', 0.03);  // ② 宝石环: 下沿 1.05 -> 与下层底座(顶 1.0)重叠 0.05 直连
put('pommel_base2', 7.3, 8.7, 0.95, 2.45, 1.8, 'pommel');         // ③ 通芯: 下沿 0.95 与底座顶面(1.0)重叠 0.05 -> 底座↔基座直连, 环浮动也不会断开
/* 握柄: 深色 + 上下金属箍 + 两道斜缠带 */
put('band_b', 7.05, 8.95, 1.85, 2.35, 2.05, 'metal', 0.02);
put('handle', 7.3, 8.7, 2.3, 7.45, 1.75, 'handle');
put('wrap_a1', 7.1, 8.9, 3.15, 3.70, 1.9, 'wrap', 0, [22.5, 'z', [8, 3.42, 8]]);
put('wrap_a2', 7.1, 8.9, 3.65, 4.20, 1.9, 'wrap', 0, [-22.5, 'z', [8, 3.92, 8]]);
put('band_a', 7.05, 8.95, 7.35, 7.85, 2.1, 'metal', 0.02);
/* 护手: 主体 + 双侧暗影钩爪(±22.5° 旋转) + 中央竖瞳魔眼(厚度 3.0~3.5, 正反两面都凸出) */
put('guard', 5.7, 10.3, 7.75, 9.6, 2.6, 'guard', 0.02);
put('claw_l', 2.9, 5.8, 7.85, 9.25, 1.5, 'claw', 0, [22.5, 'z', [5.8, 8.55, 8]]);
put('claw_r', 10.2, 13.1, 7.85, 9.25, 1.5, 'claw', 0, [-22.5, 'z', [10.2, 8.55, 8]]);
put('eye_iris', 6.45, 9.55, 7.90, 9.20, 3.15, 'eyeIris', 0.02);   // 眼窝: 上沿 9.20(配合刀身旋转收敛, 留 ~0.6 余量)
put('eye_pupil', 7.75, 8.25, 7.90, 9.20, 3.5, 'eyePupil', 0.02);
/* 剑身: 每段 **10 条** = 4 级刃口楔形 + 剑面 + 圆背三级 + **中间一道血槽(樋, 更薄=凹进去)**
   + 每段再插 2 片 ±22.5° 旋转切面片(斜插半埋, 只露斜面) -> 横截面是多面楔形, 不是平直段 */
const SEG = [[9.6, 13.05, 0, 0], [13.05, 16.50, 0.06, 0.22], [16.50, 19.95, 0.13, 0], [19.95, 23.40, 0.20, 0.22]];
/* V3: 8 条三级厚度(薄刃口 -> 剑面 -> 略厚剑脊 + 一道血槽)。宽度合计仍 4.0 -> 轮廓/剑长不变。
   旧的 10 条里有 4 条 0.14~0.20 的细碎切片, 正是用户说的"细碎锯齿", 已删除。 */
/* 每段厚度递增 4.5% -> 侧面看出"一段一段"(宽度与总长不变, 只动厚度) */
const SEGF = [1.00, 1.045, 1.09, 1.14];
const STRIP_W = [0.34, 0.52, 1.05, 0.26, 0.24, 0.30, 0.24, 1.05];
const STRIP_D = [0.42, 0.62, 1.15, 1.75, 2.00, 1.45, 2.00, 1.75];
const STRIP_R = ['edge', 'edge3', 'plate', 'spine2', 'spine', 'groove', 'spine', 'spine2'];
for (const [i, [y0, y1, dx, notch]] of SEG.entries()) {
    const a = y0 - (i ? OVER : 0), b = y1 + OVER;
    let x = 6.0 + dx + notch;
    for (let j = 0; j < STRIP_W.length; j++) {
        put('blade' + i + '_' + j, x, x + STRIP_W[j] + (j < STRIP_W.length - 1 ? OVER : 0), a, b, +(STRIP_D[j] * SEGF[i]).toFixed(3), STRIP_R[j], j === 5 ? 0.02 : 0);
        x += STRIP_W[j];
    }
    /* 刃口切面片已删(2026-09-15): 深 0.80 < 刀身板 1.10~1.25 -> 完全埋在里面看不见, 却白吃 4 cube,
       而且"绕 y 转的薄片"是悬浮小片的头号嫌疑。刃口层次改由贴图 + 十段几何承担。 */
}
/* 45° 倒角片: **薄片斜插半埋**, 只露出一个斜面 -> 削掉直角(块状模型里唯一能"倒角"的加法做法) */
/* 四片 45° 倒角已按用户反馈去掉(斜插半埋的薄片会露"悬浮小块") */
const CHAMFERS = [];
/* 剑尖: 三级收薄 + **22.5° 旋转刀尖**(真斜尖, 不是台阶) */
/* V3 倒钩: 背侧两个主要倒钩(斜后掠 22.5°), 替代原来那些细碎小凸块 */
put('tip_a', 6.20, 10.20, 23.40, 23.85, 1.10, 'face');
put('tip_b', 6.75, 10.20, 23.85, 24.25, 0.95, 'face');
put('tip_c', 7.35, 9.35, 24.25, 24.65, 0.80, 'face');    // 小平顶(右侧让位给 45° 斜切)
/* 顶部 45° 斜切: 与相邻台阶同厚(1.10) -> 面齐平, 不是半埋薄片 */
/* tip_point 已删(2026-09-15 体检): 22.5° 旋转后与剑尖不再接触 -> 全时段"悬浮", 就是用户看到的小片。
   剑尖保留三级阶梯收薄(tip_a/b/c), 层次交给贴图。 */

/* ==================== 2) 侧/盖面装箱(尺寸乘 k! 否则大部件会溢出 0..16) ==================== */
const PACK = makePacker([{ x: 0.05, w: 6.35 }, { x: 9.55, w: 6.40 }]);
const sideName = p => (p.role === 'plate' || p.role === 'face' ? 'SIDE_DARK' : 'S_' + p.n);
const capName = p => 'C_' + p.role;
{
    const ws = {}, wc = {};
    P.forEach(p => { const s = sideName(p), w = p.d * K, h = (p.y1 - p.y0) * K, c = ws[s]; if (!c || w * h > c[0] * c[1]) ws[s] = [w, h]; });
    P.forEach(p => { const w = (p.x1 - p.x0) * K, h = p.d * K, c = wc[capName(p)]; if (!c || w * h > c[0] * c[1]) wc[capName(p)] = [w, h]; });
    Object.entries(ws).sort((a, b) => b[1][1] - a[1][1]).forEach(([n, [w, h]]) => PACK.alloc(n, w, h));
    PACK.alloc('SIDE_DARK', 0.3, 0.3);
    Object.entries(wc).forEach(([n, [w, h]]) => PACK.alloc(n, w, h));
}
const REG = PACK.rects;

/* ==================== 3) 组装 ==================== */
const elements = P.map(p => {
    const zc = p.zc, z0 = r4(zc - p.d / 2), z1 = r4(zc + p.d / 2);
    const e = { name: p.n, from: [p.x0, p.y0, z0], to: [p.x1, p.y1, z1] };
    if (p.rot) e.rotation = { angle: p.rot[0], axis: p.rot[1], origin: p.rot[2].map(r4) };
    if (p.inflate) e.inflate = p.inflate;
    const T = '#layer0';
    const drawN = [r4(uOf(p.x1) + UVM), r4(vOf(p.y1) - UVM), r4(uOf(p.x0) - UVM), r4(vOf(p.y0) + UVM)];
    const drawS = [r4(uOf(p.x0) - UVM), r4(vOf(p.y1) - UVM), r4(uOf(p.x1) + UVM), r4(vOf(p.y0) + UVM)];
    const face = r => ({ uv: REG[r].slice(), texture: T });
    const f = { north: { uv: drawN, texture: T }, south: { uv: drawS, texture: T } };
    f.east = face(sideName(p)); f.west = face(sideName(p)); f.up = face(capName(p)); f.down = face(capName(p));
    if (p.role === 'eyePupil' || p.role === 'eyeIris' || p.role === 'crystal') { for (const kk of ['east', 'west']) f[kk] = face(capName(p)); }
    e.faces = f;
    return e;
});
const model = {
    credit: 'FTBoss regen_shadow_sword.cjs v16 噬影之刃·裂瞳 (2026-09-15: 按用户设计稿; 大轮廓干净+刃口分层+剑脊凸起+分叉裂纹+护手钩爪竖瞳魔眼+柄首紫晶核; 颜色表用设计稿 7 色)',
    parent: 'minecraft:item/handheld',
    texture_size: [TEXW, TEXW],
    textures: { layer0: 'ftboss:item/shadow_sword', particle: 'ftboss:item/shadow_sword' },
    display: {
        thirdperson_righthand: { rotation: [0, -90, 55], translation: [0, 4.0, 0.5], scale: [0.85, 0.85, 0.85] },
        thirdperson_lefthand: { rotation: [0, 90, -55], translation: [0, 4.0, 0.5], scale: [0.85, 0.85, 0.85] },
        firstperson_righthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
        firstperson_lefthand: { rotation: [0, 90, -25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
    },
    elements,
};

/* ==================== 4) 画布 ==================== */
const cv = makeCanvas(TEXW, TEXW);
const { mix } = cv;
const hash2 = (x, y) => { const s = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return s - Math.floor(s); };
const stip = (fx, fy, c, amt) => {
    const h = hash2(Math.round(fx * 211), Math.round(fy * 197));
    const a = amt === undefined ? 8 : amt;
    if (h > 0.86) return [cv.buf ? c[0] + a : c[0], c[1] + a, c[2] + a + 2, c[3]];
    if (h < 0.14) return [c[0] - a, c[1] - a, c[2] - a, c[3]];
    return c;
};
function fillModel(x0, y0, x1, y1, fn) {
    const px0 = Math.round(X(x0)), px1 = Math.round(X(x1)), py0 = Math.round(Y(y1)), py1 = Math.round(Y(y0));
    cv.rect(px0, py0, Math.max(1, px1 - px0), Math.max(1, py1 - py0), fn);
}
function fillRegion(key, fn) {
    const r = REG[key];
    cv.rect(Math.round(r[0] * PPU), Math.round(r[1] * PPU), Math.max(1, Math.round((r[2] - r[0]) * PPU)), Math.max(1, Math.round((r[3] - r[1]) * PPU)), fn);
}
const vLine = (x, y0, y1, c) => fillModel(x, y0, x + 1 / PPU / K, y1, () => c);
const hLine = (y, x0, x1, c) => fillModel(x0, y, x1, y + 1 / PPU / K, () => c);

/* ---- 4.1 剑身: 按 10 条走(4 级刃口 + 剑面 + 圆背三级 + 血槽), 条与条之间压 1px AO 缝 ---- */
const stripCol = (role, fx, fy, i, j) => {
    switch (role) {
        case 'edge': return j === 0
            ? (fx < 0.5 ? A(C3, 248) : mix(A(C3, 244), A(C2, 244), (fx - 0.5) / 0.5))
            : mix(A(C3, 244), A(C1, 246), fx);      // 第 2 条就落到最暗 -> 高光只在最外两条(窄冷高光)
        case 'edge3': return mix(A(C3, 240), A(C1, 246), fx ** 0.8);
        case 'plate': return stip(fx, fy + i * 0.41, mix(A(C2, 246), A(C1, 246), fx ** 0.8), 4);
        case 'spine2': return mix(A(C2, 246), A(C1, 248), fx);
        case 'spine': { const d = Math.abs(fx - 0.5) * 2; return d > 0.84 ? A(C1, 248) : mix(A(C2, 248), A(C3, 232), 1 - d); }
        case 'groove': return mix(A(C1, 252), A(C4, 110), fx < 0.22 || fx > 0.78 ? 0.30 : 0.06);   // 血槽: **最深**底色 + 两侧一点暗紫(原来往亮色混是反的)
        case 'facet': return fx < 0.16 ? A(C6, 210) : mix(A(C3, 246), A(C1, 246), 1 - fx);   // 切面: 外侧受光
        default: return stip(fx, fy + i * 0.29, mix(A(C2, 246), A(C1, 246), fx), 4);
    }
};
for (const [i, [y0, y1, dx, notch]] of SEG.entries()) {
    let x = 6.0 + dx + notch;
    for (let j = 0; j < STRIP_W.length; j++) {
        fillModel(x, y0, x + STRIP_W[j], y1, (fx, fy) => stripCol(STRIP_R[j], fx, fy, i, j));
        x += STRIP_W[j];
    }
    /* 最外刃线: **断续**高光(每 0.36 画 0.16) —— §8「窄而有间断, 不要连续白色灯带」 */
    for (let yy = y0 + 0.04; yy < y1 - 0.10; yy += 0.36) {
        vLine(6.0 + dx + notch + 0.02, yy, Math.min(y1, yy + 0.16), A(C6, 238));
    }
    /* 血槽两侧: 内 1px 深色 AO + 外 1px 冷色受光脊 -> 凹槽才有立体感(删掉逐条 AO 缝后靠这两对线把它"立"起来) */
    {
        const gx0 = 6.0 + dx + notch + (STRIP_W[0] + STRIP_W[1] + STRIP_W[2] + STRIP_W[3] + STRIP_W[4]);
        const gx1 = gx0 + STRIP_W[5];
        vLine(gx0 - 0.045, y0, y1, A(C1, 240));
        vLine(gx0 + 0.045, y0, y1, A(C1, 240));
        vLine(gx0 - 0.115, y0, y1, A(C3, 190));
        vLine(gx1 - 0.045, y0, y1, A(C1, 240));
        vLine(gx1 + 0.045, y0, y1, A(C1, 240));
        vLine(gx1 + 0.115, y0, y1, A(C3, 190));
    }
    /* 节带: 段交界上沿的深色带 + 受光脊 + 中间暗紫线; 中间两节加粗、紫线更亮 */
    {
        const jx = 6.0 + dx + notch;
        const strong = (i === 1 || i === 2);
        const bh = strong ? 0.20 : 0.14;
        fillModel(jx, y1 - bh, jx + 4.0, y1, () => A(C1, strong ? 246 : 226));
        hLine(y1 - bh - 0.06, jx, jx + 4.0, A(C3, strong ? 205 : 175));
        hLine(y1 - bh * 0.5, jx + 0.5, jx + 3.5, A(C4, strong ? 170 : 120));
    }
    if (notch > 0) {                                                       // 不对称刻口
        hLine(y0 + 0.05, 6.0 + dx + notch - 0.12, 6.0 + dx + notch + 0.85, A(C1, 250));
        hLine(y0 + 0.13, 6.0 + dx + notch - 0.12, 6.0 + dx + notch + 0.85, A(C3, 195));
    }
}
/* 45° 倒角片: 画成"受光的斜面"(外侧亮一档 + 内侧压暗) */
for (const [nx0, nx1, ny0, ny1, dir] of [[5.95, 6.95, 9.55, 9.71, 1], [9.25, 10.25, 9.55, 9.71, -1], [6.15, 7.15, 20.70, 20.86, -1], [9.05, 10.05, 20.70, 20.86, 1]]) {
    fillModel(nx0, ny0, nx1, ny1, fx => {
        const t = dir > 0 ? fx : 1 - fx;
        return t < 0.14 ? A(C6, 200) : mix(A(C3, 246), A(C1, 246), 1 - t);
    });
}

/* ---- 4.2 分叉裂纹 + 局部亮点(取代连续霓虹灯带) ---- */
const cracks = [
    [11.2, 3.4, 1], [15.4, 3.4, -1], [19.6, 3.4, 1],      // 精简到 3 条, 长度加大以覆盖加长后的刀身
];
for (const [cy, span, dir] of cracks) {
    let x = 8.0, y = cy;
    for (let s = 0; s < 7; s++) {
        const nx = x + dir * (s % 2 ? 0.16 : 0.34), ny = y + (hash2(s * 13 + cy, s * 7) - 0.5) * 0.5;
        const X0 = Math.min(x, nx) - 0.08, X1 = Math.max(x, nx) + 0.08;
        const Y0 = Math.min(y, ny) - 0.07, Y1 = Math.max(y, ny) + 0.07;
        fillModel(X0, Y0, X1, Y1, () => A(C4, 210));                      // 暗紫裂
        if (s % 3 === 1) fillModel(X0 + 0.04, Y0 + 0.04, X1 - 0.04, Y1 - 0.04, () => A(C5, 244));   // 局部亮点
        x = nx; y = ny;
    }
    if (dir > 0) fillModel(8.1, cy - 0.5, 8.45, cy + 0.5, () => A(C4, 150));    // 分叉
    else fillModel(7.55, cy - 0.5, 7.9, cy + 0.5, () => A(C4, 150));
}
for (const [bx, by] of [[7.15, 12.4], [8.6, 15.1], [7.3, 18.2], [8.75, 20.0]]) {   // 极少量高光点
    fillModel(bx, by, bx + 0.22, by + 0.22, () => A(C6, 235));
}

/* ---- 4.3 剑尖: 三级收薄 + 旋转尖 ---- */
fillModel(6.20, 23.40, 10.20, 23.85, fx => mix(A(C3, 242), A(C1, 246), fx));
vLine(6.22, 23.40, 23.85, A(C5, 205));
fillModel(6.75, 23.85, 10.20, 24.25, fx => mix(A(C3, 240), A(C1, 246), fx));
vLine(6.77, 23.85, 24.25, A(C5, 200));
fillModel(7.35, 24.25, 10.20, 24.65, fx => mix(A(C3, 238), A(C1, 246), fx));
vLine(7.37, 24.25, 24.65, A(C5, 195));


/* ---- 4.4 护手: 主体 + 钩爪 + 竖瞳魔眼 ---- */
fillModel(5.70, 7.75, 10.30, 9.60, (fx, fy) => {
    let c = mix(A(C1, 252), A(C1, 252), 0);                          // 护手底色 = 最深, 下面按横向位置微亮
    c = mix(A(C2, 250), c, 0.45 + Math.min(1, Math.abs(fy - 0.45) * 1.4));
    return stip(fx, fy, mix(c, A(C1, 250), Math.abs(fx - 0.5) * 1.1), 7);
});
hLine(9.52, 5.70, 10.30, A(C3, 200));
hLine(7.83, 5.70, 10.30, A(C1, 230));
for (const [x0, x1, dir] of [[2.90, 5.80, -1], [10.20, 13.10, 1]]) {              // 钩爪: 内厚外薄
    fillModel(x0, 7.85, x1, 9.25, fx => {
        const d = dir < 0 ? fx : 1 - fx;               // 0 = 靠内侧
        return mix(A(C2, 250), A(C1, 250), d ** 0.7);
    });
    const tipX = dir < 0 ? x0 : x1;
    vLine(tipX + (dir < 0 ? 0.03 : -0.33), 7.85, 9.25, A(C3, 215));
    fillModel(dir < 0 ? x0 + 0.05 : x1 - 0.45, 8.2, dir < 0 ? x0 + 0.45 : x1 - 0.05, 8.9, () => A(C5, 200));  // 爪尖短促高光
}
fillModel(6.15, 7.80, 9.85, 9.55, () => A(C1, 252));                                // 眼眶
hLine(9.50, 6.15, 9.85, A(C2, 250)); hLine(7.85, 6.15, 9.85, A(C3, 210));
fillModel(6.70, 8.00, 9.30, 9.40, fx => mix(A(C4, 252), A(C5, 252), 1 - Math.abs(fx - 0.5) * 1.6));   // 虹膜
fillModel(7.75, 7.90, 8.25, 9.20, () => A(C1, 255));                                // 竖瞳
fillModel(7.55, 8.05, 7.75, 8.55, () => A(C5, 250));
fillModel(8.30, 8.75, 8.55, 9.30, () => A(C5, 220));
fillModel(6.95, 8.95, 7.35, 9.30, () => A(C6, 250));                                // 眼神光
vLine(7.90, 8.0, 8.6, A(C6, 120));

/* ---- 4.5 握柄/金属箍/缠带 ---- */
fillModel(7.30, 2.30, 8.70, 7.45, fx => mix(A(C7, 252), A(C1, 252), Math.abs(fx - 0.42) * 1.9));   // 缠带色为主 -> 与纯黑护手区分
for (const y0 of [3.15, 3.65]) {
    fillModel(7.10, y0, 8.90, y0 + 0.55, (fx, fy) => {
        const band = Math.floor(((fx * 1.6 + fy) % 1) * 5);
        return mix((band % 2 === 0) ? A(C7, 252) : A(C1, 252), A(C2, 250), Math.abs(fx - 0.4) * 0.9);
    });
    hLine(y0 + 0.55, 7.10, 8.90, A(C1, 250));
}
for (const [y0, y1] of [[1.85, 2.35], [7.35, 7.85]]) {                              // 上下金属箍
    fillModel(7.05, y0, 8.95, y1, (fx, fy) => mix(A(C3, 252), A(C1, 252), Math.abs(fy - 0.45) * 1.4));
    hLine(y1, 7.05, 8.95, A(C3, 235));
    hLine(y0, 7.05, 8.95, A(C1, 240));
}
/* ---- 4.6 柄首: 黑底座 + 紫晶核 ---- */
fillModel(6.70, 0.0, 9.30, 1.0, (fx, fy) => mix(A(C1, 252), A(C2, 252), fy * 0.6));
fillModel(7.00, 1.05, 9.00, 1.75, (fx, fy) => {           // 宝石环: 径向 + 一点高光
    const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
    return d > 0.85 ? A(C4, 250) : d > 0.45 ? mix(A(C4, 250), A(C5, 250), 1 - (d - 0.45) / 0.4) : A(C5, 252);
});
fillModel(7.35, 1.25, 7.75, 1.62, () => A(C6, 248));
fillModel(7.30, 0.95, 8.70, 2.45, fx => mix(A(C2, 252), A(C1, 252), Math.abs(fx - 0.5) * 1.3));   // 通芯(画在环之后会盖住环 -> 下面补一遍)
fillModel(7.00, 1.05, 9.00, 1.75, (fx, fy) => {           // 宝石环(补画在通芯之上)
    const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
    return d > 0.85 ? A(C4, 250) : d > 0.45 ? mix(A(C4, 250), A(C5, 250), 1 - (d - 0.45) / 0.4) : A(C5, 252);
});
fillModel(7.35, 1.25, 7.75, 1.62, () => A(C6, 248));

/* ---- 4.7 侧/盖面(每块比例与面对应) ---- */
const SIDE = {
    edge: fx => mix(A(C3, 240), A(C1, 240), fx), edge2: fx => mix(A(C3, 230), A(C1, 240), fx),
    edge3: fx => mix(A(C3, 236), A(C1, 246), fx ** 0.8),
    spine2: fx => mix(A(C2, 246), A(C1, 248), fx),
    chamfer: fx => (fx < 0.14 ? A(C6, 200) : mix(A(C3, 246), A(C1, 246), 1 - fx)),
    point: fx => mix(A(C6, 244), A(C3, 238), fx ** 0.7),
    spine: fx => { const d = Math.abs(fx - 0.5) * 2; return d > 0.82 ? A(C1, 248) : mix(A(C2, 250), A(C3, 200), 1 - d); },
    handle: fx => mix(A(C2, 250), A(C1, 252), Math.abs(fx - 0.42) * 2.2),
    wrap: (fx, fy) => { const band = Math.floor(((fx * 1.6 + fy) % 1) * 5); return mix((band % 2 === 0) ? A(C7, 252) : A(C1, 252), A(C2, 250), Math.abs(fx - 0.4) * 0.9); },
    metal: (fx, fy) => mix(A(C3, 252), A(C1, 252), Math.abs(fy - 0.45) * 1.4),
    guard: (fx, fy) => mix(A(C2, 250), A(C1, 250), Math.min(1, Math.abs(fy - 0.45) * 1.5)),
    claw: fx => mix(A(C2, 250), A(C1, 250), fx ** 0.7),
    eyeFrame: () => A(C1, 252),
    eyeIris: fx => mix(A(C4, 252), A(C5, 252), 1 - Math.abs(fx - 0.5) * 1.6),
    eyePupil: () => A(C1, 255),
    crystal: (fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.85 ? A(C4, 250) : d > 0.45 ? mix(A(C4, 250), A(C5, 250), 1 - (d - 0.45) / 0.4) : A(C5, 252); },
    pommel: (fx, fy) => mix(A(C1, 252), A(C2, 252), fy * 0.6),
    face: fx => mix(A(C3, 236), A(C1, 246), fx), tiph: fx => mix(A(C3, 230), A(C1, 246), fx), plate: fx => mix(A(C2, 246), A(C1, 246), fx),
};
P.forEach(p => {
    const key = sideName(p), fn = SIDE[p.role] || SIDE.plate;
    fillRegion(key, fn);
    fillRegion(capName(p), fn);
});
fillRegion('SIDE_DARK', SIDE.plate);
outline(cv, A(C1, 250));

/* ==================== 5) 发光层 + 自检 + 写盘 ==================== */
const emissive = makeCanvas(TEXW, TEXW);
for (let i = 0; i < cv.buf.length; i += 4) {
    const r = cv.buf[i], g = cv.buf[i + 1], b = cv.buf[i + 2], a = cv.buf[i + 3];
    if (a > 60 && ((b > 120 && b > g + 45 && r > 90) || (r > 200 && g > 180 && b > 210))) {   // 紫亮面/眼神光
        emissive.buf[i] = r; emissive.buf[i + 1] = g; emissive.buf[i + 2] = b; emissive.buf[i + 3] = a;
    }
}
const res = {
    parts: elements.length,
    aspect: checks.aspect(P, REG, sideName),
    mirror: checks.mirror(elements, /^(eye|pommel_crystal|crystal)/),
    noise: checks.noise(elements),
    gold: checks.goldArtifacts(cv),
    warm: checks.warmPixels(cv),
    rot: elements.filter(e => e.rotation).length,
    inflate: elements.filter(e => e.inflate).length,
};
safeCopy(MODEL, path.join(REF, 'shadow_sword_prev.json.bak'));
safeCopy(TEX, path.join(REF, 'shadow_sword_32.png.bak'));
writeSafe(MODEL, JSON.stringify(model, null, 2) + '\n');
cv.save(TEX);
emissive.save(EMISSIVE);

/* ★★ 2026-09-15 手工优化会被本脚本覆盖，别再踩：
 *   重跑本脚本会按下面的旧调色板重新生成 shadow_sword.png，
 *   把已应用的「明度优化」抹掉（当时测得 66% 像素亮度 <40、亮部仅 0.7%、动态范围 55，
 *   在游戏里就是一团看不清的黑块；优化后暗部 33%、亮部 3.3%、动态范围 67）。
 *   所以：**重跑本脚本之后，必须再跑一次**
 *       node tools/model3d/shadow_sword_tex_fix.cjs
 *   或者把这里的调色板/明度曲线一起改掉（更好的做法）。 */
console.log('⚠ 记得重跑 shadow_sword_tex_fix.cjs —— 本脚本会覆盖贴图的明度优化');
let emN = 0; for (let i = 3; i < emissive.buf.length; i += 4) if (emissive.buf[i] > 8) emN++;
console.log('噬影之刃·裂瞳: 部件 ' + res.parts + ' | 旋转 ' + res.rot + ' | inflate ' + res.inflate + ' | 发光层 ' + emN + ' px');
console.log('  侧面比例 : ' + (res.aspect.length ? '✗ ' + res.aspect.join('; ') : '✓ 一一对应'));
console.log('  正反镜像 : ' + (res.mirror.length ? '✗ ' + res.mirror.join(',') : '✓ 通过'));
console.log('  浮点噪声 : ' + (res.noise.length ? '✗ ' + res.noise.join(',') : '✓ 0 个'));
console.log('  金色杂点 : ' + (res.gold ? '✗ ' + res.gold : '✓ 0 个') + '   (暖色像素 ' + res.warm + ' 仅参考)');
console.log('  贴图 -> ' + path.relative(ROOT, TEX) + '  模型 -> ' + path.relative(ROOT, MODEL));
