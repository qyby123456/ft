/* ============================================================================
 *  gen_weapons3d.cjs —— 批量把本工程的武器做成"暗影刀 v13 同款标准"的 3D 物品模型
 *
 *  标准(全部来自前面几轮的教训, 见 docs/MODEL_REFERENCE_NOTES.md / AGENTS.md):
 *   · 每个武器 = **一张部件表 + 一套配色**, 几何/UV/装箱/描边/自检都走公共库 lib_itemtex.cjs
 *   · 宽面: 整件画成 2D 侧视图(按模型坐标对齐), 每个部件取自己那块; north/south **互为镜像**
 *   · 薄侧面/顶底: 按 `(厚度 × 高度)`/`(宽 × 厚)` **逐部件装箱**成比例正确的块 -> 不拉伸
 *   · 相邻部件互相重叠 0.05 防共面闪烁; `inflate` 做分层; ±22.5°/±45° 旋转做斜构件
 *   · 画布全 clamp; 暗部往蓝紫偏(hue shift); 1px 面板缝 + 上缘亮线; 细噪点; 1px 紫黑外描边
 *   · 生成完跑四项自检(侧面比例/正反镜像/浮点噪声/暖色杂点)
 *
 *  用法: node tools/model3d/gen_weapons3d.cjs [武器id ...]   (不带参数 = 全部)
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const { makeCanvas, makePacker, outline, checks, writeSafe, safeCopy } = require('./lib_itemtex.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const RES = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const REF = path.join(ROOT, 'ref');

/* ==================== 通用: 颜色工具 ==================== */
const cl = v => v < 0 ? 0 : v > 255 ? 255 : v;
const mix = (a, b, t0) => {
    const t = Math.max(0, Math.min(1, t0));
    const aa = a[3] === undefined ? 255 : a[3], ba = b[3] === undefined ? 255 : b[3];
    return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t, aa + (ba - aa) * t];
};
const hash2 = (x, y) => { const s = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return s - Math.floor(s); };
const stipple = (fx, fy, c, amt = 10) => {
    const h = hash2(Math.round(fx * 211), Math.round(fy * 197));
    if (h > 0.84) return [cl(c[0] + amt), cl(c[1] + amt), cl(c[2] + amt + 3), c[3]];
    if (h < 0.16) return [cl(c[0] - amt), cl(c[1] - amt), cl(c[2] - amt), c[3]];
    return c;
};
/* 四段圆柱色阶: 高光 -> 亮 -> 中 -> 暗 -> 反光边 */
const cylRamp = (fx, C) => {
    const d = Math.abs(fx - 0.36) * 2;
    if (d < 0.18) return C.hi;
    if (d < 0.46) return mix(C.hi, C.mid, (d - 0.18) / 0.28);
    if (d < 0.82) return mix(C.mid, C.dark, (d - 0.46) / 0.36);
    return mix(C.dark, C.accent || C.mid, (d - 0.82) / 0.18 * 0.6);
};

/* ==================== 通用画法(按角色) ====================
   签名: (fill, C) 其中 fill(fn) 负责在指定矩形里逐像素调用 fn(fx, fy) -> 颜色        */
const PAINT = {
    cyl: (fill, C) => fill(fx => stipple(fx, 0.3, cylRamp(fx, C), 8)),
    wrap: (fill, C) => fill((fx, fy) => {                       // 斜缠绳
        const band = Math.floor(((fx * 1.6 + fy) % 1) * 5);
        let c = (band % 2 === 0) ? C.mid : C.dark;
        if (band === 2 && C.accent) c = C.accent;
        return stipple(fx, fy, mix(c, C.hi, Math.max(0, 0.42 - Math.abs(fx - 0.36)) * 0.7), 8);
    }),
    wood: (fill, C) => fill((fx, fy) => {                        // 木纹
        const grain = hash2(Math.round(fy * 41), Math.round(fx * 7)) > 0.72 ? 1 : 0;
        return stipple(fx, fy, mix(cylRamp(fx, C), grain ? C.hi : C.dark, grain ? 0.25 : 0.12), 8);
    }),
    plate: (fill, C) => fill((fx, fy) => {                       // 刀身板: 横向色阶 + 细噪点
        let c = mix(C.hi, C.dark, fx ** 0.85);
        return stipple(fx, fy, c, 9);
    }),
    edge: (fill, C) => fill(fx => {                              // 刃口: 外侧亮
        return fx < 0.24 ? C.hi : fx < 0.62 ? mix(C.hi, C.mid, (fx - 0.24) / 0.38) : mix(C.mid, C.dark, (fx - 0.62) / 0.38);
    }),
    spine: (fill, C) => fill(fx => mix(C.hi, C.dark, Math.min(1, Math.abs(fx - 0.42) * 2.2))),   // 刀背(中间一条反光)
    metal: (fill, C) => fill((fx, fy) => {                       // 金属件: 上下压暗 + 中间亮
        let c = mix(C.hi, C.mid, Math.min(1, Math.abs(fy - 0.45) * 1.6));
        return stipple(fx, fy, mix(c, C.dark, Math.abs(fx - 0.5) * 1.2), 8);
    }),
    ball: (fill, C) => fill((fx, fy) => {                        // 球体
        const d = Math.hypot((fx - 0.42) * 2, (fy - 0.42) * 2);
        if (d > 0.86) return C.dark;
        if (d > 0.5) return stipple(fx, fy, mix(C.hi, C.mid, (d - 0.5) / 0.36), 8);
        return mix(C.hi, C.accent || [255, 255, 255, 255], (0.5 - d) / 0.5 * 0.45);
    }),
    leaf: (fill, C) => fill((fx, fy) => {                        // 叶片/护手
        const d = Math.abs(fx - 0.5) * 2;
        let c = mix(C.hi, C.dark, d ** 0.8);
        if (Math.abs(fx - 0.5) < 0.06) c = mix(c, C.accent || C.hi, 0.5);   // 叶脉
        return stipple(fx, fy, c, 8);
    }),
    fin: (fill, C) => fill((fx, fy) => mix(C.hi, C.dark, fy)),   // 半透明鳍
    rivet: (fill, C) => fill((fx, fy) => {
        const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
        return d > 0.92 ? C.dark : d > 0.55 ? mix(C.mid, C.dark, (d - 0.55) / 0.37) : mix(C.hi, [255, 255, 255, 255], (0.55 - d) / 0.55 * 0.5);
    }),
    gem: (fill, C) => fill((fx, fy) => {
        const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
        return d > 0.75 ? C.dark : d > 0.4 ? C.mid : (d > 0.15 ? C.hi : [255, 255, 255, 255]);
    }),
    glass: (fill, C) => fill((fx, fy) => {                       // 酒瓶玻璃
        let c = mix(C.mid, C.dark, fx ** 0.7);
        if (fx < 0.2) c = mix(c, C.hi, (0.2 - fx) / 0.2 * 0.8);   // 一条高光
        if (fy > 0.72) c = mix(c, C.accent, 0.5);                 // 瓶底液体
        return stipple(fx, fy, c, 6);
    }),
    cheese: (fill, C) => fill((fx, fy) => {
        const d = Math.hypot((fx - 0.45) * 2, (fy - 0.45) * 2);
        let c = mix(C.hi, C.dark, d ** 0.9);
        const h = hash2(Math.round(fx * 23), Math.round(fy * 19));
        if (h > 0.86 && d < 0.7) c = mix(c, C.accent || C.dark, 0.75);      // 奶酪孔
        return stipple(fx, fy, c, 7);
    }),
    bone: (fill, C) => fill((fx, fy) => cylRamp(fx, C)),
    spike: (fill, C) => fill((fx, fy) => {                       // 尖刺: 中间亮、边缘暗
        const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
        return d > 0.9 ? C.dark : d > 0.45 ? mix(C.hi, C.mid, (d - 0.45) / 0.45) : C.hi;
    }),
    ferrule: (fill, C) => PAINT.metal(fill, C),
    candy: (fill, C) => fill((fx, fy) => {                       // 糖果/拐杖: 斜条纹
        const band = Math.floor(((fx * 0.8 + fy) % 1) * 4);
        return mix((band % 2 === 0) ? C.hi : C.mid, C.dark, Math.abs(fx - 0.45) * 0.9);
    }),
    stem: (fill, C) => PAINT.cyl(fill, C),
    rind: (fill, C) => fill((fx, fy) => mix(C.hi, C.dark, fy * 0.7)),
};

/* ==================== 构建一个武器 ==================== */
function buildWeapon(spec) {
    const { id, tex = 256, k, v0 = 15.5, parts, pal, outline: outCol = [44, 14, 62, 234], decorate, emissiveFilter } = spec;
    const PPU = tex / 16;
    const uOf = x => 8 + (x - 8) * k, vOf = y => v0 - y * k;
    const X = x => uOf(x) * PPU, Y = y => vOf(y) * PPU;
    const r4 = v => +(+v).toFixed(4);
    const UVM = 0.09, OVER = 0.05;

    /* --- 部件(自动处理重叠: 同名组相邻时向外各扩 OVER/2) --- */
    const P = parts.map(p => ({
        n: p[0], x0: r4(p[1]), x1: r4(p[2]), y0: r4(p[3]), y1: r4(p[4]), d: r4(p[5]),
        role: p[6], inflate: p[7] || 0, rot: p[8] || null,
    }));
    /* --- 侧/盖面装箱 --- */
    const PACK = makePacker([{ x: 0.05, w: 6.35 }, { x: 9.55, w: 6.40 }]);
    const sideName = p => (p.role === 'plate' ? 'SIDE_DARK' : 'S_' + p.n);
    const capName = p => 'C_' + p.role;
    {
        const ws = {}, wc = {};
        P.forEach(p => { const s = sideName(p), w = p.d * k, h = (p.y1 - p.y0) * k, c = ws[s]; if (!c || w * h > c[0] * c[1]) ws[s] = [w, h]; });
        P.forEach(p => { const w = (p.x1 - p.x0) * k, h = p.d * k, c = wc[capName(p)]; if (!c || w * h > c[0] * c[1]) wc[capName(p)] = [w, h]; });
        Object.entries(ws).sort((a, b) => b[1][1] - a[1][1]).forEach(([n, [w, h]]) => PACK.alloc(n, w, h));
        PACK.alloc('SIDE_DARK', 0.3, 0.3);
        Object.entries(wc).forEach(([n, [w, h]]) => PACK.alloc(n, w, h));
    }
    const REG = PACK.rects;
    /* --- 组装 --- */
    const elements = P.map(p => {
        const z0 = r4(8 - p.d / 2), z1 = r4(8 + p.d / 2);
        const e = { name: p.n, from: [p.x0, p.y0, z0], to: [p.x1, p.y1, z1] };
        if (p.rot) e.rotation = { angle: p.rot[0], axis: p.rot[1], origin: p.rot[2].map(r4) };
        if (p.inflate) e.inflate = p.inflate;
        const T = '#layer0';
        const drawN = [r4(uOf(p.x1) + UVM), r4(vOf(p.y1) - UVM), r4(uOf(p.x0) - UVM), r4(vOf(p.y0) + UVM)];
        const drawS = [r4(uOf(p.x0) - UVM), r4(vOf(p.y1) - UVM), r4(uOf(p.x1) + UVM), r4(vOf(p.y0) + UVM)];
        const face = r => ({ uv: REG[r].slice(), texture: T });
        const f = { north: { uv: drawN, texture: T }, south: { uv: drawS, texture: T } };
        f.east = face(sideName(p)); f.west = face(sideName(p)); f.up = face(capName(p)); f.down = face(capName(p));
        if (p.role === 'rivet') { for (const kk of ['north', 'south', 'east', 'west', 'up', 'down']) f[kk] = face('C_rivet'); }
        e.faces = f;
        return e;
    });
    /* --- 画布 --- */
    const cv = makeCanvas(tex, tex);
    const fillModel = (x0, y0, x1, y1) => fn => {
        const px0 = Math.round(X(x0)), px1 = Math.round(X(x1)), py0 = Math.round(Y(y1)), py1 = Math.round(Y(y0));
        cv.rect(px0, py0, Math.max(1, px1 - px0), Math.max(1, py1 - py0), fn);
    };
    const fillRegion = key => fn => {
        const r = REG[key];
        cv.rect(Math.round(r[0] * PPU), Math.round(r[1] * PPU), Math.max(1, Math.round((r[2] - r[0]) * PPU)), Math.max(1, Math.round((r[3] - r[1]) * PPU)), fn);
    };
    P.forEach(p => {
        const C = pal[p.role] || pal.plate;
        const painter = PAINT[p.role] || PAINT.plate;
        painter(fillModel(p.x0, p.y0, p.x1, p.y1), C);                 // 宽面(整件侧视图的一部分)
        if (p.role !== 'rivet') { painter(fillRegion(sideName(p)), C); painter(fillRegion(capName(p)), C); }
        if (p.role === 'rivet') painter(fillRegion('C_rivet'), C);
    });
    if (pal.SIDE_DARK) PAINT.plate(fillRegion('SIDE_DARK'), pal.plate);
    if (decorate) decorate({ cv, fillModel, fillRegion, X, Y, r4, k, PPU, REG, elements, mix, stipple, cl });
    outline(cv, outCol);
    /* --- 发光层(可选) --- */
    let em = null;
    if (emissiveFilter) {
        em = makeCanvas(tex, tex);
        for (let i = 0; i < cv.buf.length; i += 4) {
            const c = [cv.buf[i], cv.buf[i + 1], cv.buf[i + 2], cv.buf[i + 3]];
            if (emissiveFilter(c)) { em.buf[i] = c[0]; em.buf[i + 1] = c[1]; em.buf[i + 2] = c[2]; em.buf[i + 3] = c[3]; }
        }
    }
    /* --- 写盘 --- */
    const MODEL = path.join(RES, 'models', 'item', id + '.json');
    const TEXP = path.join(RES, 'textures', 'item', id + '.png');
    const model = {
        credit: 'FTBoss gen_weapons3d.cjs (' + new Date().toISOString().slice(0, 10) + ': 逐面UV+比例正确的侧盖面块+重叠防闪烁+四段色阶+细噪点+1px描边)',
        parent: 'minecraft:item/handheld',
        texture_size: [tex, tex],
        textures: { layer0: 'ftboss:item/' + id, particle: 'ftboss:item/' + id },
        display: {
            thirdperson_righthand: { rotation: [0, -90, 55], translation: [0, 4.0, 0.5], scale: [0.85, 0.85, 0.85] },
            thirdperson_lefthand: { rotation: [0, 90, -55], translation: [0, 4.0, 0.5], scale: [0.85, 0.85, 0.85] },
            firstperson_righthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
            firstperson_lefthand: { rotation: [0, 90, -25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
        },
        elements,
    };
    fs.mkdirSync(REF, { recursive: true });
    safeCopy(MODEL, path.join(REF, id + '_prev.json.bak'));
    safeCopy(TEXP, path.join(REF, id + '_prev.png.bak'));
    writeSafe(MODEL, JSON.stringify(model, null, 2) + '\n');
    cv.save(TEXP);
    if (em) em.save(path.join(RES, 'textures', 'item', id + '_e.png'));
    /* --- 自检 --- */
    const res = {
        id, parts: elements.length,
        aspect: checks.aspect(P, REG, sideName),
        mirror: checks.mirror(elements, /^(rivet|stud)/),
        noise: checks.noise(elements),
        warm: checks.warmPixels(cv),
        gold: checks.goldArtifacts(cv),
        rot: elements.filter(e => e.rotation).length,
        inflate: elements.filter(e => e.inflate).length,
        em: em ? (() => { let n = 0; for (let i = 3; i < em.buf.length; i += 4) if (em.buf[i] > 8) n++; return n; })() : 0,
    };
    return res;
}

/* ==================== 武器表 ==================== */
const SPECS = {};
function addSpec(s) { SPECS[s.id] = s; }

/* ---- 1. 黑色切割者(断罪斩): **双刃战斧**(2026-09-15 v5 按用户"黑切是双头斧"改)
   原来是"一侧刃口 + 另一侧刀背剑脊(带铆钉)"的单头造型。
   现在把刃口镜像到右侧（edge_r），剑脊从右端收回**中央**当厚重的斧眼，
   铆钉分到左右两片刃根上，宝石挪到斧眼中心，血渍两侧都有。
   刃口 x：左 4.6..5.4 / 右 10.6..11.4（关于握柄中线 x=8.0 对称） ---- */
addSpec({
    id: 'black_cleaver', k: 0.60,
    parts: [
        ['pommel', 7.0, 9.0, 0.0, 1.4, 1.5, 'metal'],
        ['handle', 7.2, 8.8, 1.35, 7.05, 1.2, 'cyl'],
        ['wrap_a1', 7.0, 9.0, 3.0, 3.5, 1.35, 'wrap', 0, [22.5, 'z', [8, 3.25, 8]]],
        ['wrap_a2', 7.0, 9.0, 3.45, 3.95, 1.35, 'wrap', 0, [-22.5, 'z', [8, 3.7, 8]]],
        ['wrap_b1', 7.0, 9.0, 5.0, 5.5, 1.35, 'wrap', 0, [22.5, 'z', [8, 5.25, 8]]],
        ['wrap_b2', 7.0, 9.0, 5.45, 5.95, 1.35, 'wrap', 0, [-22.5, 'z', [8, 5.7, 8]]],
        ['ferrule', 6.6, 9.4, 6.95, 7.85, 1.7, 'metal', 0.03],
        ['blade', 5.4, 10.6, 7.8, 19.4, 1.0, 'plate'],
        ['edge', 4.6, 5.4, 7.8, 19.4, 0.66, 'edge'],
        ['edge_r', 10.6, 11.4, 7.8, 19.4, 0.66, 'edge'],
        ['spine', 7.4, 8.6, 7.8, 19.4, 1.55, 'spine', 0.02],
        ['tip_a', 5.4, 10.6, 19.35, 20.1, 1.15, 'plate'],
        ['tip_b', 6.4, 9.6, 20.1, 20.9, 1.05, 'plate'],
        ['rivet_a', 6.05, 6.5, 10.4, 10.84, 1.85, 'rivet', 0.02],
        ['rivet_b', 6.05, 6.5, 13.4, 13.84, 1.85, 'rivet', 0.02],
        ['rivet_c', 6.05, 6.5, 16.4, 16.84, 1.85, 'rivet', 0.02],
        ['rivet_d', 9.5, 9.95, 10.4, 10.84, 1.85, 'rivet', 0.02],
        ['rivet_e', 9.5, 9.95, 13.4, 13.84, 1.85, 'rivet', 0.02],
        ['rivet_f', 9.5, 9.95, 16.4, 16.84, 1.85, 'rivet', 0.02],
    ],
    pal: {
        metal: { hi: [96, 88, 108, 246], mid: [46, 41, 56, 246], dark: [14, 12, 22, 246], accent: [72, 62, 88, 246] },
        cyl: { hi: [86, 62, 58, 246], mid: [50, 34, 32, 246], dark: [18, 12, 12, 246], accent: [70, 48, 44, 246] },
        wrap: { hi: [120, 92, 84, 244], mid: [52, 34, 30, 244], dark: [22, 14, 13, 244], accent: [168, 44, 48, 244] },
        ferrule: { hi: [96, 88, 108, 246], mid: [46, 41, 56, 246], dark: [14, 12, 22, 246] },
        plate: { hi: [56, 50, 68, 240], mid: [30, 26, 38, 240], dark: [8, 7, 14, 240] },
        edge: { hi: [232, 226, 240, 244], mid: [140, 132, 156, 232], dark: [56, 50, 72, 200] },
        spine: { hi: [150, 142, 168, 226], mid: [34, 30, 42, 240], dark: [6, 5, 12, 240] },
        rivet: { hi: [214, 168, 96, 255], mid: [140, 98, 48, 255], dark: [60, 36, 16, 255] },
    },
    decorate({ fillModel, fillRegion, mix, stipple }) {
        /* 双刃战斧特征: 两面斧刃各 3 道血槽缝 + **斧眼中央**一颗红宝石 + 两侧刃口都有血渍 */
        for (const by of [10.2, 13.2, 16.2]) {
            fillModel(5.55, by, 7.35, by + 0.16)(fx => mix([10, 8, 16, 130], [120, 108, 140, 90], 1 - fx));
            fillModel(8.65, by, 10.5, by + 0.16)(fx => mix([10, 8, 16, 130], [120, 108, 140, 90], 1 - fx));
        }
        /* 宝石挪到中央斧眼上（原来在右端剑脊上） */
        fillModel(7.45, 16.4, 8.55, 17.6)((fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.8 ? [90, 20, 26, 255] : d > 0.45 ? [186, 42, 52, 255] : [236, 120, 130, 255]; });
        /* 刃口血渍：左右两侧都画（关于中线 x=8 镜像） */
        for (const [sy, sx] of [[9.4, 4.9], [12.6, 4.85], [15.6, 4.9]]) {
            const stain = x0 => fillModel(x0, sy, x0 + 0.5, sy + 0.9)((fx, fy) => mix([128, 24, 30, 190], [60, 10, 14, 120], fy));
            stain(sx);              // 左刃口
            stain(15.5 - sx);       // 右刃口（镜像）
        }
        /* 铆钉高光已在 rivet 画法里 */
        if (fillRegion) { /* 侧/盖面按角色画好了 */ }
    },
    emissiveFilter: c => c[3] > 40 && c[0] > 120 && c[0] > c[2] + 40,     // 红宝石/血渍发光
});

/* ---- 2. 河豚棒: 木棍 + 缠绳 + 河豚球 + 尖刺 + 鳍 ---- */
addSpec({
    id: 'puffer_bat', k: 0.62,
    parts: [
        ['stick', 7.2, 8.8, 0.0, 8.6, 1.6, 'wood'],
        ['grip_a', 6.95, 9.05, 1.5, 2.9, 1.95, 'wrap', 0.03],
        ['grip_b', 6.95, 9.05, 4.3, 5.7, 1.95, 'wrap', 0.03],
        ['body', 4.4, 11.6, 8.6, 15.8, 7.2, 'ball'],
        ['spike_t', 7.6, 8.4, 15.7, 17.0, 0.8, 'spike'],
        ['spike_l', 3.3, 4.6, 11.8, 12.6, 0.8, 'spike'],
        ['spike_r', 11.4, 12.7, 11.8, 12.6, 0.8, 'spike'],
        ['spike_bl', 5.0, 5.8, 9.4, 10.3, 0.7, 'spike', 0, [22.5, 'z', [5.4, 9.85, 8]]],
        ['spike_br', 10.2, 11.0, 9.4, 10.3, 0.7, 'spike', 0, [-22.5, 'z', [10.6, 9.85, 8]]],
        ['fin_l', 3.6, 4.9, 12.4, 14.6, 2.8, 'fin'],
        ['fin_r', 11.1, 12.4, 12.4, 14.6, 2.8, 'fin'],
    ],
    pal: {
        wood: { hi: [138, 104, 64, 246], mid: [92, 66, 38, 246], dark: [46, 32, 18, 246], accent: [110, 80, 46, 246] },
        wrap: { hi: [150, 132, 96, 244], mid: [86, 72, 50, 244], dark: [40, 32, 22, 244], accent: [172, 60, 52, 244] },
        ball: { hi: [214, 196, 128, 250], mid: [168, 150, 88, 250], dark: [104, 92, 52, 250], accent: [246, 240, 200, 255] },
        spike: { hi: [232, 220, 168, 250], mid: [172, 156, 104, 250], dark: [92, 82, 50, 250] },
        fin: { hi: [226, 190, 120, 232], mid: [180, 140, 76, 216], dark: [104, 76, 38, 190] },
        plate: { hi: [200, 184, 120, 240], mid: [150, 132, 76, 240], dark: [92, 78, 42, 240] },
    },
    decorate({ cv, fillModel, fillRegion, X, Y, mix }) {
        /* 河豚的脸: 两只眼睛 + 嘴(画在正面画区里) */
        fillModel(5.6, 13.6, 6.6, 14.6)((fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.85 ? [40, 34, 20, 250] : d > 0.45 ? [246, 246, 240, 255] : [24, 20, 16, 255]; });
        fillModel(9.4, 13.6, 10.4, 14.6)((fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.85 ? [40, 34, 20, 250] : d > 0.45 ? [246, 246, 240, 255] : [24, 20, 16, 255]; });
        fillModel(7.1, 11.0, 8.9, 11.9)((fx, fy) => mix([84, 30, 30, 250], [40, 16, 16, 250], fy));
        /* 肚子浅色 */
        fillModel(4.8, 8.7, 11.2, 11.0)((fx, fy) => mix([236, 226, 176, 180], [200, 186, 130, 90], fy));
    },
});



/* ---- 5. 文玄蓝的飞轮: 轴 + 缠绳 + 轮毂 + 8 段轮缘(45° 用旋转) + 8 颗铆钉 + 4 根辐条 ---- */
addSpec({
    id: 'wenxuanlan_flywheel', k: 0.55,
    parts: (() => {
        const out = [
            ['shaft', 7.4, 8.6, 0.0, 5.4, 1.2, 'cyl'],
            ['wrap_a1', 7.15, 8.85, 1.4, 1.95, 1.35, 'wrap', 0, [22.5, 'z', [8, 1.68, 8]]],
            ['wrap_a2', 7.15, 8.85, 1.9, 2.45, 1.35, 'wrap', 0, [-22.5, 'z', [8, 2.18, 8]]],
            ['hub', 6.9, 9.1, 5.4, 7.0, 2.4, 'metal', 0.03],
        ];
        const R = 2.7, CY = 11.5, W = 0.65, H = 0.65;
        for (let i = 0; i < 8; i++) {
            const a = i * 45 * Math.PI / 180;
            const cx = 8 + R * Math.sin(a), cy = CY + R * Math.cos(a);
            const rot = (i % 2 === 0) ? null : [i === 1 || i === 5 ? 45 : -45, 'z', [cx, cy, 8]];
            out.push(['rim' + i, cx - W, cx + W, cy - H, cy + H, 1.7, 'metal', 0.02, rot]);
            out.push(['stud' + i, cx - 0.25, cx + 0.25, cy - 0.25, cy + 0.25, 2.2, 'rivet', 0.03]);
        }
        out.push(['spoke_v', 7.7, 8.3, 7.0, 14.2, 1.2, 'metal']);
        out.push(['spoke_h', 5.3, 10.7, 11.2, 11.8, 1.2, 'metal']);
        out.push(['spoke_d1', 6.2, 9.8, 9.8, 13.2, 1.1, 'metal', 0, [45, 'z', [8, 11.5, 8]]]);
        out.push(['spoke_d2', 6.2, 9.8, 9.8, 13.2, 1.1, 'metal', 0, [-45, 'z', [8, 11.5, 8]]]);
        return out;
    })(),
    pal: {
        cyl: { hi: [150, 170, 210, 246], mid: [86, 104, 148, 246], dark: [30, 40, 66, 246], accent: [110, 132, 176, 246] },
        wrap: { hi: [120, 140, 180, 244], mid: [62, 78, 116, 244], dark: [26, 34, 54, 244], accent: [96, 216, 236, 244] },
        metal: { hi: [168, 186, 224, 246], mid: [96, 114, 158, 246], dark: [34, 44, 70, 246] },
        rivet: { hi: [208, 226, 255, 255], mid: [130, 156, 208, 255], dark: [46, 62, 104, 255] },
    },
    decorate({ fillModel, mix }) {
        fillModel(7.0, 5.5, 9.0, 6.9)((fx, fy) => {                  // 轮毂上的蓝色核心
            const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2);
            return d > 0.85 ? [30, 40, 66, 250] : d > 0.4 ? [70, 150, 210, 250] : [176, 232, 255, 255];
        });
        for (let i = 0; i < 4; i++) fillModel(5.6 + i * 1.2, 10.0, 6.6 + i * 1.2, 13.0)((fx, fy) => mix([150, 190, 230, 60], [60, 90, 140, 30], fy));
    },
    emissiveFilter: c => c[3] > 40 && c[2] > 150 && c[1] > 120 && c[2] > c[0] + 30,
});

/* ---- 6. 绽放的荆棘: 藤茎(3 段错位) + 6 根斜刺(旋转) + 3 朵花 + 3 片叶 + 缠绳柄 ---- */
addSpec({
    id: 'blooming_thorns', k: 0.62,
    parts: [
        ['grip_w1', 7.2, 8.8, 1.4, 1.95, 1.5, 'wrap', 0, [22.5, 'z', [8, 1.68, 8]]],
        ['grip_w2', 7.2, 8.8, 2.9, 3.45, 1.5, 'wrap', 0, [-22.5, 'z', [8, 3.18, 8]]],
        ['stem0', 7.65, 8.35, 0.6, 4.4, 0.75, 'stem'],
        ['stem1', 7.75, 8.45, 4.3, 8.1, 0.75, 'stem'],
        ['stem2', 7.85, 8.55, 8.0, 12.0, 0.75, 'stem'],
        ['thorn0', 8.4, 9.1, 3.4, 4.0, 0.6, 'spike', 0, [45, 'z', [8.45, 3.7, 8]]],
        ['thorn1', 9.0, 9.7, 5.0, 5.6, 0.6, 'spike', 0, [-45, 'z', [9.05, 5.3, 8]]],
        ['thorn2', 8.5, 9.2, 6.6, 7.2, 0.6, 'spike', 0, [45, 'z', [8.55, 6.9, 8]]],
        ['thorn3', 6.8, 7.5, 4.6, 5.2, 0.6, 'spike', 0, [-45, 'z', [7.45, 4.9, 8]]],
        ['thorn4', 6.7, 7.4, 7.4, 8.0, 0.6, 'spike', 0, [45, 'z', [7.35, 7.7, 8]]],
        ['thorn5', 8.6, 9.3, 9.0, 9.6, 0.6, 'spike', 0, [-45, 'z', [8.65, 9.3, 8]]],
        ['leaf_l', 5.9, 7.6, 8.4, 9.3, 0.5, 'leaf', 0, [22.5, 'z', [7.6, 8.85, 8]]],
        ['leaf_r', 8.5, 10.2, 6.4, 7.3, 0.5, 'leaf', 0, [-22.5, 'z', [8.5, 6.85, 8]]],
        ['leaf_t', 8.6, 10.3, 11.0, 11.9, 0.5, 'leaf', 0, [-22.5, 'z', [8.6, 11.45, 8]]],
        ['bloom_a', 5.4, 7.6, 8.9, 11.1, 2.2, 'bloom'],
        ['bloom_b', 8.5, 10.7, 6.9, 9.1, 2.2, 'bloom'],
        ['bloom_c', 7.0, 9.2, 11.8, 14.0, 2.2, 'bloom'],
    ],
    pal: {
        wrap: { hi: [138, 126, 96, 244], mid: [80, 70, 48, 244], dark: [34, 30, 20, 244], accent: [186, 62, 78, 244] },
        stem: { hi: [126, 168, 96, 248], mid: [72, 116, 62, 248], dark: [30, 58, 34, 248], accent: [96, 140, 76, 248] },
        spike: { hi: [206, 216, 168, 246], mid: [138, 150, 96, 246], dark: [60, 72, 40, 246] },
        leaf: { hi: [156, 200, 116, 246], mid: [92, 146, 70, 246], dark: [38, 78, 38, 246], accent: [210, 234, 170, 248] },
        bloom: { hi: [236, 122, 140, 252], mid: [186, 54, 78, 252], dark: [96, 22, 38, 252], accent: [255, 220, 226, 255] },
    },
    decorate({ fillModel, mix }) {
        /* 花心 + 叶脉 + 藤上的小刺点 */
        for (const [bx, by] of [[6.5, 10.0], [9.6, 8.0], [8.1, 12.9]]) {
            fillModel(bx - 0.35, by - 0.35, bx + 0.35, by + 0.35)((fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.7 ? [150, 40, 60, 250] : [250, 232, 150, 255]; });
        }
        fillModel(6.0, 8.5, 7.5, 9.2)(fx => mix([38, 78, 38, 200], [170, 208, 130, 180], 1 - fx));
        fillModel(8.6, 6.5, 10.1, 7.2)(fx => mix([38, 78, 38, 200], [170, 208, 130, 180], fx));
    },
    emissiveFilter: c => c[3] > 40 && c[0] > 170 && c[0] > c[1] + 60,
});

/* ---- 7. 酒瓶锤: 缠绳柄 + 瓶颈/肩/瓶身(玻璃+酒液) + 木塞 ---- */
addSpec({
    id: 'wine_bottle_mace', k: 0.62,
    parts: [
        ['handle', 7.4, 8.6, 0.0, 6.0, 1.3, 'wood'],
        ['wrap_a1', 7.2, 8.8, 2.0, 2.55, 1.45, 'wrap', 0, [22.5, 'z', [8, 2.28, 8]]],
        ['wrap_a2', 7.2, 8.8, 3.4, 3.95, 1.45, 'wrap', 0, [-22.5, 'z', [8, 3.68, 8]]],
        ['neck', 7.6, 8.4, 5.9, 8.4, 0.95, 'glass'],
        ['shoulder', 6.9, 9.1, 8.3, 9.6, 1.7, 'glass'],
        ['body', 5.9, 10.1, 9.5, 17.8, 2.7, 'glass'],
        ['label', 6.2, 9.8, 11.6, 14.4, 2.9, 'plate', 0.02],
        ['cork', 7.7, 8.3, 17.7, 18.8, 0.85, 'wood'],
    ],
    pal: {
        wood: { hi: [138, 104, 64, 246], mid: [92, 66, 38, 246], dark: [46, 32, 18, 246], accent: [110, 80, 46, 246] },
        wrap: { hi: [150, 132, 96, 244], mid: [86, 72, 50, 244], dark: [40, 32, 22, 244], accent: [150, 42, 60, 244] },
        glass: { hi: [150, 190, 150, 232], mid: [56, 96, 66, 230], dark: [22, 46, 30, 236], accent: [128, 22, 44, 238] },
        plate: { hi: [226, 214, 186, 250], mid: [184, 166, 130, 250], dark: [120, 102, 74, 250] },
    },
    decorate({ fillModel, mix }) {
        fillModel(6.3, 12.0, 9.7, 14.0)((fx, fy) => {              // 酒标
            let c = mix([232, 224, 200, 252], [190, 176, 142, 252], fy);
            if (Math.abs(fy - 0.5) < 0.08 || Math.abs(fx - 0.5) < 0.04) c = [150, 30, 48, 252];
            return c;
        });
        fillModel(9.0, 9.8, 10.0, 17.4)(fx => mix([240, 250, 240, 120], [90, 130, 100, 40], fx));   // 玻璃高光
    },
    emissiveFilter: null,
});

/* ---- 8. 奶酪轮锤: 木柄 + 铁环 + 奶酪轮(径向 + 孔) + 外皮 ---- */
addSpec({
    id: 'cheese_wheel_mace', k: 0.62,
    parts: [
        ['handle', 7.4, 8.6, 0.0, 6.2, 1.3, 'wood'],
        ['ferrule', 6.8, 9.2, 6.0, 6.8, 1.6, 'metal', 0.02],
        ['wheel', 5.0, 11.0, 6.8, 12.8, 3.4, 'cheese'],
        ['rind_t', 5.0, 11.0, 12.4, 12.9, 3.6, 'rind', 0.02],
        ['rind_b', 5.0, 11.0, 6.7, 7.2, 3.6, 'rind', 0.02],
    ],
    pal: {
        wood: { hi: [122, 78, 48, 246], mid: [78, 48, 28, 246], dark: [34, 20, 12, 246], accent: [96, 62, 36, 246] },
        metal: { hi: [168, 168, 180, 246], mid: [96, 96, 110, 246], dark: [40, 40, 52, 246] },
        cheese: { hi: [252, 226, 140, 250], mid: [226, 186, 88, 250], dark: [170, 126, 44, 250], accent: [120, 84, 26, 255] },
        rind: { hi: [226, 150, 62, 250], mid: [186, 108, 36, 250], dark: [128, 66, 18, 250] },
    },
    decorate({ fillModel, mix }) {
        for (const [cx, cy] of [[6.4, 9.0], [9.2, 10.6], [7.8, 11.9], [8.6, 8.2]]) {   // 奶酪孔
            fillModel(cx - 0.35, cy - 0.35, cx + 0.35, cy + 0.35)((fx, fy) => { const d = Math.hypot((fx - 0.5) * 2, (fy - 0.5) * 2); return d > 0.75 ? [226, 186, 88, 250] : [126, 88, 28, 255]; });
        }
    },
    emissiveFilter: null,
});


/* ---- 10. 打狗棒: 木棒 + 缠绳 + 更粗的棒头(两道铁箍) + 斜挂的骨头 ---- */
addSpec({
    id: 'dog_beating_stick', k: 0.62,
    parts: [
        ['shaft', 7.4, 8.6, 0.0, 11.5, 1.4, 'wood'],
        ['wrap_a1', 7.2, 8.8, 1.8, 2.35, 1.55, 'wrap', 0, [22.5, 'z', [8, 2.08, 8]]],
        ['wrap_a2', 7.2, 8.8, 3.2, 3.75, 1.55, 'wrap', 0, [-22.5, 'z', [8, 3.48, 8]]],
        ['head', 6.7, 9.3, 11.5, 15.8, 2.3, 'wood'],
        ['band_a', 6.6, 9.4, 11.4, 11.9, 2.45, 'metal', 0.02],
        ['band_b', 6.6, 9.4, 15.4, 15.9, 2.45, 'metal', 0.02],
        ['bone1', 9.3, 10.0, 12.6, 15.0, 0.7, 'bone', 0, [22.5, 'z', [9.65, 13.8, 8]]],
        ['bone2', 6.0, 6.7, 12.6, 15.0, 0.7, 'bone', 0, [-22.5, 'z', [6.35, 13.8, 8]]],
    ],
    pal: {
        wood: { hi: [146, 108, 66, 246], mid: [98, 70, 40, 246], dark: [48, 32, 18, 246], accent: [116, 84, 48, 246] },
        wrap: { hi: [150, 132, 96, 244], mid: [86, 72, 50, 244], dark: [40, 32, 22, 244], accent: [96, 140, 180, 244] },
        metal: { hi: [176, 178, 190, 246], mid: [104, 106, 120, 246], dark: [44, 46, 58, 246] },
        bone: { hi: [244, 240, 222, 250], mid: [206, 198, 172, 250], dark: [140, 130, 104, 250], accent: [220, 214, 190, 250] },
    },
    decorate({ fillModel, mix }) {
        fillModel(6.9, 12.0, 9.1, 15.3)((fx, fy) => {              // 棒头木纹 + 齿痕
            const g = ((fx * 4 + fy * 9) % 1) < 0.18;
            return mix(g ? [72, 48, 26, 250] : [118, 84, 46, 250], [40, 26, 14, 200], Math.abs(fx - 0.5) * 1.1);
        });
        for (const ty of [12.6, 13.6, 14.6]) fillModel(6.6, ty, 9.4, ty + 0.18)(fx => mix([60, 40, 22, 170], [150, 110, 66, 120], fx));
    },
    emissiveFilter: null,
});

addSpec({ id: 'apple_stem' });

/* ==================== 执行 ==================== */
const want = process.argv.slice(2);
const ids = want.length ? want : Object.keys(SPECS);
for (const id of ids) {
    if (id === 'blooming_thorns') {
        console.log('SKIP blooming_thorns: preserve the user-imported thorns model and texture.');
        continue;
    }
    if (id === 'dog_beating_stick') {
        require('./gen_benger_weapon.cjs').generate();
        continue;
    }
    if (id === 'puffer_bat') {
        require('./gen_puffer_bat.cjs').generate();
        continue;
    }
    if (id === 'wine_bottle_mace') {
        require('./gen_wine_bottle_mace.cjs').generate();
        continue;
    }
    if (id === 'wenxuanlan_flywheel') {
        require('./gen_xuanlan_weapons.cjs').generate();
        continue;
    }
    if (id === 'apple_stem') {
        const output = path.join(ROOT, 'art', 'apple_stem_micui');
        require('./gen_apple_stem.cjs').generateAppleStem(output);
        fs.mkdirSync(REF, { recursive: true });
        safeCopy(path.join(RES, 'models/item/apple_stem.json'), path.join(REF, 'apple_stem_prev.json.bak'));
        safeCopy(path.join(RES, 'textures/item/apple_stem_micui.png'), path.join(REF, 'apple_stem_micui_prev.png.bak'));
        safeCopy(path.join(output, 'models/item/apple_stem.json'), path.join(RES, 'models/item/apple_stem.json'));
        safeCopy(path.join(output, 'textures/item/apple_stem_micui.png'), path.join(RES, 'textures/item/apple_stem_micui.png'));
        continue;
    }
    const spec = SPECS[id];
    if (!spec) { console.log('✗ 没有这个武器的配方: ' + id); continue; }
    const r = buildWeapon(spec);
    const ok = !r.aspect.length && !r.mirror.length && !r.noise.length && !r.gold;
    console.log((ok ? '✓ ' : '✗ ') + r.id.padEnd(18) + '部件 ' + String(r.parts).padStart(2) + ' | 旋转 ' + r.rot + ' | inflate ' + r.inflate +
        ' | 发光层 ' + String(r.em).padStart(4) + ' px | 暖色像素 ' + String(r.warm).padStart(5) + '(信息) | ' +
        (ok ? '自检全绿' : '异常: ' + JSON.stringify({ 比例: r.aspect, 镜像: r.mirror, 噪声: r.noise, 金色杂点: r.gold })));
}
