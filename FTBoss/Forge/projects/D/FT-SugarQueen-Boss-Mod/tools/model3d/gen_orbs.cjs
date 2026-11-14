/* ============================================================================
 * 五种充能球 生成器 v5 —— 2026-09-11 (回到方块, 尽量精细)
 *
 * 思路: 用 **3×3×3 排布去掉 8 个角**(19 个 cube)近似球体 —— 这是方块模型里最"圆"的做法;
 *       再按球种叠加细节: 金属箍 / 阶梯放电尖刺 / 8 颗角部碎晶 / 双环 / 裂纹带 + 环绕碎片。
 *       每个 cube 4 单位, 整球约 12~14 单位(≈0.8 格)。
 *
 * 贴图: box-UV, 256×256。**按 cube 在球面上的位置(经纬)取色**, 所以花纹能绕着球连续过渡;
 *       发光遮罩只覆盖"该发光的材质", 避免把颜色洗白(旧版踩过的坑)。
 *
 * 产出: <type>_orb.geo.json / .animation.json / .png / _glowmask.png
 * ========================================================================== */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out_orbs';
const TEX = 256;
const U = 2;                        // 单元尺寸(单位), 3×3×3 => 6 单位 ≈ 0.375 格(小而精细)
/** 装饰的最大半径(单位): 整球控制在 ~9 单位 ≈ 0.56 格, 与 0.5 的碰撞箱贴合 */
const MAX_R = 4.4;

/* ---------------- 配色 ---------------- */
const PALETTES = {
    glass: { body: [96, 214, 248], hi: [226, 250, 255], deep: [42, 132, 186], accent: [150, 92, 44] },
    lightning: { body: [255, 226, 74], hi: [255, 252, 206], deep: [196, 138, 12], accent: [255, 246, 170] },
    frost: { body: [176, 232, 255], hi: [244, 253, 255], deep: [104, 168, 214], accent: [206, 236, 250] },
    ion: { body: [255, 118, 226], hi: [255, 226, 250], deep: [186, 44, 168], accent: [255, 196, 92] },
    dark: { body: [56, 30, 84], hi: [138, 76, 208], deep: [26, 12, 42], accent: [196, 110, 255] }
};
const M = { BODY: 0, HI: 1, DEEP: 2, ACCENT: 3 };
const rnd = (x, y, s) => (((x * 41 + y * 29 + s * 13) % 19) / 19);
const mix = (a, b, t) => [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t];

/** 球面花纹: 输入经纬(0..1 / 0..1), 返回该点颜色(不含材质语义, 只给色) */
function sphereColor(type, P, lon, lat, mat) {
    const n = rnd(Math.floor(lon * 64), Math.floor(lat * 64), 1) * 14 - 7;
    const add = c => [Math.max(0, Math.min(255, c[0] + n)), Math.max(0, Math.min(255, c[1] + n)), Math.max(0, Math.min(255, c[2] + n))];
    switch (mat) {
        case M.HI:
            return add(P.hi);
        case M.DEEP:
            return add(mix(P.deep, P.body, 0.25));
        case M.ACCENT:
            return add(P.accent);
        default: {
            switch (type) {
                case 'glass': {                     // 刻面: 经纬格交替明暗
                    const facet = (Math.floor(lon * 9) + Math.floor(lat * 7)) % 2 === 0;
                    return add(facet ? P.body : mix(P.body, P.deep, 0.5));
                }
                case 'lightning': {                 // 锯齿放电 + 赤道白热
                    const zig = Math.abs(((lon * 6) % 1) - 0.5) < 0.18;
                    if (zig || Math.abs(lat - 0.5) < 0.1) return add(P.hi);
                    return add(mix(P.deep, P.body, 0.3 + 0.5 * Math.abs(Math.sin(lat * Math.PI * 3))));
                }
                case 'frost': {                     // 冰晶斜纹 + 极冠
                    const lat2 = (Math.floor(lon * 11 + lat * 9) % 3 === 0);
                    if (lat < 0.16 || lat > 0.84) return add(P.hi);
                    return add(lat2 ? P.deep : P.body);
                }
                case 'ion': {                       // 同心亮带
                    const band = Math.abs(Math.sin(lat * Math.PI * 4)) > 0.86;
                    return add(band ? P.hi : mix(P.body, P.deep, 0.35));
                }
                default: {                          // 黑球: 亮紫裂纹
                    const crack = Math.abs(Math.sin(lon * Math.PI * 6 + lat * Math.PI * 8)) > 0.93;
                    return add(crack ? P.accent : mix(P.body, P.deep, 0.6));
                }
            }
        }
    }
}

/** 该材质是否自发光(着色器层用) */
function isGlow(type, mat) {
    if (mat === M.ACCENT) return true;
    if (mat === M.HI) return type !== 'dark';
    if (mat === M.BODY) return type === 'lightning';
    return false;
}

/* ---------------- 装箱器(box-UV) ---------------- */
const maskMat = [];
let penX = 0, penY = 0, rowH = 0;
function allocUV(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > TEX) { penX = 0; penY += rowH + 1; rowH = 0; }
    if (penY + fh > TEX) throw new Error(`贴图放不下(${fw}x${fh})`);
    const u = penX, v = penY;
    penX += fw + 1; rowH = Math.max(rowH, fh);
    return [u, v, fw, fh];
}

/* ---------------- 规格 ---------------- */
function spec(type) {
    const c = [];
    const add = (bone, o, s, mat) => c.push({ bone, o, s, mat });
    const half = 1.5 * U;                       // 中心在 0 => 3×3×3 的边界
    // ① 3×3×3 去掉 8 个角 => 19 个 cube 的"圆球"
    for (let ix = -1; ix <= 1; ix++)
        for (let iy = -1; iy <= 1; iy++)
            for (let iz = -1; iz <= 1; iz++) {
                const corner = Math.abs(ix) === 1 && Math.abs(iy) === 1 && Math.abs(iz) === 1;
                if (corner) continue;
                const center = (ix === 0 && iy === 0 && iz === 0);
                const faceCenter = (Math.abs(ix) + Math.abs(iy) + Math.abs(iz)) === 1;   // 6 个面心
                // 冰球: 6 个面心用高光材质 => 有可自发光的面(否则整球没有发光像素)
                let mat = M.BODY;
                if (type === 'frost' && faceCenter) mat = M.HI;
                else if (type === 'frost' && center) mat = M.DEEP;
                else if (center && type === 'dark') mat = M.HI;
                add('core', [ix * U - U / 2, iy * U - U / 2, iz * U - U / 2], [U, U, U], mat);
            }
    // ② 球种细节
    if (type === 'glass') {
        // 2026-09-11 优化: 8 个角加"切割棱面"(高光材质, 会发光) + 金属框架(上下箍 + 4 根立柱) => 笼中水晶球
        for (const ix of [-1, 1]) for (const iy of [-1, 1]) for (const iz of [-1, 1]) {
            add('facets', [ix * (half + 0.2) - 0.7, iy * (half + 0.2) - 0.7, iz * (half + 0.2) - 0.7],
                    [1.4, 1.4, 1.4], M.HI);
        }
        const w = 3 * U + 0.4;
        add('frame', [-w / 2, half - 0.5, -w / 2], [w, 0.8, w], M.ACCENT);
        add('frame', [-w / 2, -half - 0.3, -w / 2], [w, 0.8, w], M.ACCENT);
        for (const sx of [-1, 1]) for (const sz of [-1, 1]) {
            add('frame', [sx * (half + 0.2) - 0.4, -half, sz * (half + 0.2) - 0.4], [0.8, 3 * U, 0.8], M.ACCENT);
        }
    } else if (type === 'lightning') {
        for (const axis of [0, 1, 2]) {
            for (const sign of [-1, 1]) {
                const b = [0, 0, 0], t = [0, 0, 0];
                b[axis] = sign * (half + 0.2);
                t[axis] = sign * (half + 1.1);
                const bs = [1.6, 1.6, 1.6], ts = [1.0, 1.0, 1.0];
                bs[axis] = 1.4; ts[axis] = 1.2;
                add('spikes', b, bs, M.ACCENT);
                add('spikes', t, ts, M.HI);
            }
        }
    } else if (type === 'frost') {
        for (const ix of [-1, 1]) for (const iy of [-1, 1]) for (const iz of [-1, 1]) {
            add('shards', [ix * (half + 0.7) - 0.7, iy * (half + 0.7) - 0.7, iz * (half + 0.7) - 0.7],
                    [1.4, 1.4, 1.4], M.DEEP);
        }
    } else if (type === 'ion') {
        const r = half + 0.8;
        for (const y of [-1.6, 1.6]) {
            add('ring', [-r, y - 0.4, -0.6], [2 * r, 0.8, 1.2], M.ACCENT);
            add('ring', [-0.6, y - 0.4, -r], [1.2, 0.8, 2 * r], M.ACCENT);
        }
        for (let k = 0; k < 4; k++) {
            const ang = k * Math.PI / 2 + 0.4;
            add('nodes', [Math.cos(ang) * r - 0.7, -0.7, Math.sin(ang) * r - 0.7], [1.4, 1.4, 1.4], M.ACCENT);
        }
    } else {   // dark
        for (const axis of [0, 1, 2]) {
            for (const sign of [-1, 1]) {
                const o = [0, 0, 0], s = [U, U, U];
                o[axis] = sign * (half + 0.15);
                s[axis] = 0.5;
                add('cracks', o, s, M.ACCENT);
            }
        }
        for (let k = 0; k < 4; k++) {
            const ang = k * Math.PI / 2 + 0.7;
            const rr = half + 1.2;
            add('shards', [Math.cos(ang) * rr - 0.7, (k % 2 === 0 ? 1.6 : -1.6) - 0.7, Math.sin(ang) * rr - 0.7],
                    [1.4, 1.4, 1.4], M.ACCENT);
        }
    }
    // 硬性约束: 不许超出 MAX_R(保持"小而精细")
    for (const cube of c) {
        for (let i = 0; i < 3; i++) {
            cube.o[i] = Math.max(-MAX_R, Math.min(MAX_R - cube.s[i], cube.o[i]));
        }
    }
    return c;
}

/* ---------------- 构建 ---------------- */
function build(type) {
    const P = PALETTES[type];
    const cubes = spec(type);
    const cv = new Canvas(TEX, TEX);
    const glowCv = new Canvas(TEX, TEX);
    const mask = new Uint8Array(TEX * TEX).fill(255);
    let glowPx = 0;
    const out = [];
    for (const c of cubes) {
        const [w, h, d] = c.s;
        const uv = allocUV(w, h, d);
        const [u, v, fw, fh] = uv;
        // 用 cube 中心算经纬 => 花纹绕着球连续
        const cx = c.o[0] + w / 2, cy = c.o[1] + h / 2, cz = c.o[2] + d / 2;
        const len = Math.max(0.001, Math.sqrt(cx * cx + cy * cy + cz * cz));
        const lat = 0.5 - Math.asin(Math.max(-1, Math.min(1, cy / len))) / Math.PI;
        const lon = (Math.atan2(cz, cx) / (Math.PI * 2) + 1) % 1;
        const col = sphereColor(type, P, lon, lat, c.mat);
        const glow = isGlow(type, c.mat);
        for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) {
            const nn = rnd(x, y, 2) * 10 - 5;
            const r = Math.max(0, Math.min(255, col[0] + nn)), g = Math.max(0, Math.min(255, col[1] + nn)),
                    b2 = Math.max(0, Math.min(255, col[2] + nn));
            cv.set(u + x, v + y, r, g, b2, 1);
            mask[(v + y) * TEX + (u + x)] = glow ? 1 : 0;
            if (glow) { glowCv.set(u + x, v + y, r, g, b2, 1); glowPx++; }
        }
        out.push({ bone: c.bone, origin: c.o, size: c.s, uv: [u, v] });
    }
    const names = [...new Set(cubes.map(c => c.bone))];
    const bones = [{ name: 'root', pivot: [0, 0, 0] }];
    for (const n of names) bones.push({ name: n, parent: 'root', pivot: [0, 0, 0], cubes: out.filter(c => c.bone === n) });
    const geo = {
        format_version: '1.12.0',
        'minecraft:geometry': [{
            description: {
                identifier: `geometry.${type}_orb`, texture_width: TEX, texture_height: TEX,
                visible_bounds_width: 2, visible_bounds_height: 2, visible_bounds_offset: [0, 0, 0]
            },
            bones
        }]
    };
    const A = 'animation.orb.';
    const spinBone = names[0];
    const anim = {
        format_version: '1.8.0',
        animations: {
            [`${A}idle`]: {
                loop: true, animation_length: 1.4,
                bones: {
                    root: { position: { '0.0': [0, 0, 0], '0.7': [0, -0.9, 0], '1.4': [0, 0, 0] } },
                    [spinBone]: { rotation: { '0.0': [0, 0, 0], '1.4': [0, 360, 0] } },
                    ...(names.includes('ring') ? { ring: { rotation: { '0.0': [0, 0, 0], '1.4': [0, -360, 0] } } } : {}),
                    ...(names.includes('shards') ? { shards: { rotation: { '0.0': [0, 0, 0], '0.7': [0, 180, 0], '1.4': [0, 360, 0] } } } : {}),
                    ...(names.includes('spikes') ? { spikes: { rotation: { '0.0': [0, 0, 0], '1.4': [0, 180, 0] } } } : {})
                }
            },
            [`${A}burst`]: {
                loop: false, animation_length: 0.4,
                bones: { root: { scale: { '0.0': [1, 1, 1], '0.2': [1.6, 1.6, 1.6], '0.4': [2.3, 2.3, 2.3] } } }
            }
        }
    };
    return { geo, anim, cv, glow: glowCv, cubes: out.length, glowPx };
}

fs.mkdirSync(OUT, { recursive: true });
for (const type of Object.keys(PALETTES)) {
    const r = build(type);
    fs.writeFileSync(path.join(OUT, `${type}_orb.geo.json`), JSON.stringify(r.geo, null, 1));
    fs.writeFileSync(path.join(OUT, `${type}_orb.animation.json`), JSON.stringify(r.anim, null, 1));
    r.cv.save(path.join(OUT, `${type}_orb.png`));
    r.glow.save(path.join(OUT, `${type}_orb_glowmask.png`));
    console.log(`${type.padEnd(10)} cube=${String(r.cubes).padStart(3)} 发光像素=${r.glowPx}  基色=rgb(${PALETTES[type].body.join(',')})`);
}
console.log(`五种充能球(方块版 v5)已生成 -> ${OUT}`);
