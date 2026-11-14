/* ============================================================================
 *  render_geo.cjs —— geo 直读渲染器（光栅化 + 逐像素贴图采样），用于 §三 校准
 *
 *  特点（与旧的 geo_to_preview 的根本区别）:
 *    · 直接吃 .geo.json, 不经过 Java elements
 *    · 完整骨骼链变换（子先作用→逐级回根, 已在 geo_transform.cjs 校准通过）
 *    · cube 自身 pivot+rotation、inflate 全部应用
 *    · 逐面 UV **保留 uv_size 符号**（负值 = 镜像, 采样时方向翻转）
 *    · 正交投影 + 画家算法(按面深度排序), 无 z-buffer（预览用, 已说明局限）
 *    · 自写 PNG 编码(zlib stored/deflate), 不依赖第三方
 *
 *  用法:
 *    node tools/model3d/render_geo.cjs <geo.json> <tex.png> <out.png> [yaw1,yaw2,...] [pitch] [scale]
 *    node tools/model3d/render_geo.cjs --selftest        # 生成 6 面异色+箭头 的测试模型并自检
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..');
const { decodePNG } = require('./png.cjs');

/* ---------- PNG 编码 ---------- */
function crc32(buf) {
    let c, crc = 0xffffffff;
    for (let n = 0; n < buf.length; n++) {
        c = (crc ^ buf[n]) & 0xff;
        for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
        crc = (crc >>> 8) ^ c;
    }
    return (crc ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
    const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
    const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
    const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td));
    return Buffer.concat([len, td, crc]);
}
function writePNG(file, w, h, rgba) {
    const ihdr = Buffer.alloc(13);
    ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4);
    ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
    const raw = Buffer.alloc((w * 4 + 1) * h);
    for (let y = 0; y < h; y++) {
        raw[y * (w * 4 + 1)] = 0;
        rgba.copy(raw, y * (w * 4 + 1) + 1, y * w * 4, (y + 1) * w * 4);
    }
    fs.writeFileSync(file, Buffer.concat([
        Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
        chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0)),
    ]));
}

/* ---------- 变换（与 geo_transform.cjs 校准通过的实现一致） ---------- */
const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
const add = (a, b) => [a[0] + b[0], a[1] + b[1], a[2] + b[2]];
const rad = d => d * Math.PI / 180;
function rotXYZ(p, r, piv) {
    let q = sub(p, piv);
    let c = Math.cos(rad(r[0])), s = Math.sin(rad(r[0])); q = [q[0], q[1] * c - q[2] * s, q[1] * s + q[2] * c];
    c = Math.cos(rad(r[1])); s = Math.sin(rad(r[1])); q = [q[0] * c + q[2] * s, q[1], -q[0] * s + q[2] * c];
    c = Math.cos(rad(r[2])); s = Math.sin(rad(r[2])); q = [q[0] * c - q[1] * s, q[0] * s + q[1] * c, q[2]];
    return add(q, piv);
}
const scaleAbout = (p, sc, piv) => add(sub(p, piv).map((v, i) => v * sc[i]), piv);
function boneTransform(byName, name, pose) {
    const chain = [];
    for (let cur = name; cur; cur = byName[cur].parent) chain.unshift(byName[cur]);
    const Ms = chain.map(b => {
        const p = pose[b.name] || {};
        const rot = [0, 1, 2].map(i => (b.rotation ? b.rotation[i] : 0) + (p.rotation ? p.rotation[i] : 0));
        const sc = p.scale || [1, 1, 1], pos = p.position || [0, 0, 0], piv = b.pivot || [0, 0, 0];
        return v => add(rotXYZ(scaleAbout(v, sc, piv), rot, piv), pos);
    });
    return v => Ms.reduceRight((acc, f) => f(acc), v);
}
/* 角点枚举顺序: idx = 4*x + 2*y + z (x,y,z ∈ {0,1} 对应 cube 的 from/to 两端)
   -> 六个面必须取"同一坐标分量固定"的四个角点, 且按环绕顺序(否则四边形折叠成蝴蝶结, UV 插值全错) */
const FACE_IDX = {
    north: [0, 2, 6, 4], south: [1, 3, 7, 5], west: [0, 1, 3, 2],
    east: [4, 5, 7, 6], up: [2, 3, 7, 6], down: [0, 1, 5, 4],
};
function faceCorners(byName, boneName, cube, pose) {
    const inf = typeof cube.inflate === 'number' ? cube.inflate : 0;
    const [ox, oy, oz] = cube.origin, [sx, sy, sz] = cube.size;
    const T = boneTransform(byName, boneName, pose);
    const pts = [];
    for (const x of [ox - inf, ox + sx + inf]) for (const y of [oy - inf, oy + sy + inf]) for (const z of [oz - inf, oz + sz + inf]) {
        let p = [x, y, z];
        if (cube.rotation) p = rotXYZ(p, cube.rotation, cube.pivot || [0, 0, 0]);
        pts.push(T(p));
    }
    const out = {};
    for (const [f, idx] of Object.entries(FACE_IDX)) out[f] = idx.map(i => pts[i]);
    return out;
}

/* ---------- 渲染 ---------- */
function render(geoFile, texFile, outFile, yaws, pitchDeg, zoom) {
    const data = JSON.parse(fs.readFileSync(geoFile, 'utf8'));
    const g = data['minecraft:geometry'][0];
    const byName = Object.fromEntries(g.bones.map(b => [b.name, b]));
    const tex = decodePNG(texFile);
    const tw = g.description.texture_width, th = g.description.texture_height;
    const PW = 300, PH = 300;
    const W = PW * yaws.length, H = PH;
    const buf = Buffer.alloc(W * H * 4, 0);
    for (let i = 0; i < W * H; i++) { buf[i * 4 + 3] = 255; buf[i * 4] = 245; buf[i * 4 + 1] = 245; buf[i * 4 + 2] = 247; }

    const faces = [];
    for (const b of g.bones) for (const c of (b.cubes || [])) {
        const fc = faceCorners(byName, b.name, c, {});
        for (const [f, pts] of Object.entries(fc)) {
            const uv = c.uv && c.uv[f];
            if (!uv) continue;
            faces.push({ f, pts, uv });
        }
    }
    /* 自动取景: 所有面角点求包围盒 -> 居中 + 缩放到占满 86% 面板(否则模型会溢出画幅) */
    const allPts = faces.flatMap(f => f.pts);
    const bx = [Math.min(...allPts.map(p => p[0])), Math.max(...allPts.map(p => p[0]))];
    const by = [Math.min(...allPts.map(p => p[1])), Math.max(...allPts.map(p => p[1]))];
    const cxf = (bx[0] + bx[1]) / 2, cyf = (by[0] + by[1]) / 2;
    const zoomF = (PH * 0.86) / Math.max(bx[1] - bx[0], by[1] - by[0], 1);
    const proj = (p, yaw, pit, offX) => {
        const cy = Math.cos(rad(yaw)), sy = Math.sin(rad(yaw));
        let x = p[0] * cy + p[2] * sy, z = -p[0] * sy + p[2] * cy, y = p[1];
        const cp = Math.cos(rad(pit)), sp = Math.sin(rad(pit));
        const y2 = y * cp - z * sp, z2 = y * sp + z * cp;
        return [offX + PW / 2 + (x - cxf) * zoomF, PH / 2 - (y2 - cyf) * zoomF, z2];   // 用包围盒中心 + 自适应缩放
    };
    yaws.forEach((yaw, si) => {
        const list = faces.map(fa => {
            const P = fa.pts.map(p => proj(p, yaw, pitchDeg, si * PW));
            return { ...fa, P, depth: P.reduce((s, p) => s + p[2], 0) / P.length };
        }).sort((a, b) => b.depth - a.depth);   // 由远到近绘制(画家算法); 顺序写反会让背面盖住正面
        for (const fa of list) {
            const [u, v] = fa.uv.uv, [uw, vh] = fa.uv.uv_size;
            const u0 = u / tw, v0 = v / th, u1 = (u + uw) / tw, v1 = (v + vh) / th;
            const [A, B, C, D] = fa.P;
            for (const [p0, p1, p2, s0, t0, s1, t1, s2, t2] of [
                [A, B, C, 0, 0, 1, 0, 1, 1], [A, C, D, 0, 0, 1, 1, 0, 1],
            ]) {
                const minX = Math.max(0, Math.floor(Math.min(p0[0], p1[0], p2[0])));
                const maxX = Math.min(W - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0])));
                const minY = Math.max(0, Math.floor(Math.min(p0[1], p1[1], p2[1])));
                const maxY = Math.min(H - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1])));
                const d = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1]);
                if (Math.abs(d) < 1e-9) continue;
                for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
                    const w0 = ((p1[1] - p2[1]) * (x - p2[0]) + (p2[0] - p1[0]) * (y - p2[1])) / d;
                    const w1 = ((p2[1] - p0[1]) * (x - p2[0]) + (p0[0] - p2[0]) * (y - p2[1])) / d;
                    const w2 = 1 - w0 - w1;
                    if (w0 < -0.002 || w1 < -0.002 || w2 < -0.002) continue;
                    const s = w0 * s0 + w1 * s1 + w2 * s2, t = w0 * t0 + w1 * t1 + w2 * t2;
                    let su = u0 + (u1 - u0) * s, sv = v0 + (v1 - v0) * t;   // 负 uv_size 自动成为反向区间
                    const px = Math.min(tw - 1, Math.max(0, Math.floor(su * tw)));
                    const py = Math.min(th - 1, Math.max(0, Math.floor(sv * th)));
                    const q = (py * tex.w + px) * 4;
                    if (tex.data[q + 3] < 8) continue;
                    const o = (y * W + x) * 4;
                    const a = tex.data[q + 3] / 255;
                    buf[o] = Math.round(tex.data[q] * a + buf[o] * (1 - a));
                    buf[o + 1] = Math.round(tex.data[q + 1] * a + buf[o + 1] * (1 - a));
                    buf[o + 2] = Math.round(tex.data[q + 2] * a + buf[o + 2] * (1 - a));
                }
            }
        }
    });
    writePNG(outFile, W, H, buf);
    return { W, H, buf, faces: faces.length };
}

/* ---------- 自检用测试模型: 6 面异色 + 箭头(方向可见) ---------- */
function selftest() {
    const CELL = 8, TH = 16, TW = CELL * 6;
    const cols = [[220, 60, 60], [60, 200, 90], [70, 120, 240], [230, 200, 60], [200, 90, 220], [70, 210, 210]];
    const names = ['north', 'south', 'east', 'west', 'up', 'down'];
    const raw = Buffer.alloc(TW * TH * 4);
    names.forEach((n, i) => {
        for (let y = 0; y < TH; y++) for (let x = 0; x < CELL; x++) {
            const o = (y * TW + i * CELL + x) * 4;
            let c = cols[i];
            if (x === 0 || y === 0) c = [30, 30, 30];                       // 左上角标记
            if (x >= 3 && x <= 4 && y >= 3 && y <= 4) c = [255, 255, 255];  // 4x4 白块(位置可核对)
            raw[o] = c[0]; raw[o + 1] = c[1]; raw[o + 2] = c[2]; raw[o + 3] = 255;
        }
    });
    const png = path.join(ROOT, 'art', 'calib_tex.png');
    (function () {   // 直接借 render 的 PNG 写出
        const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(TW, 0); ihdr.writeUInt32BE(TH, 4); ihdr[8] = 8; ihdr[9] = 6;
        const rr = Buffer.alloc((TW * 4 + 1) * TH);
        for (let y = 0; y < TH; y++) { rr[y * (TW * 4 + 1)] = 0; raw.copy(rr, y * (TW * 4 + 1) + 1, y * TW * 4, (y + 1) * TW * 4); }
        fs.writeFileSync(png, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(rr)), chunk('IEND', Buffer.alloc(0))]));
    })();
    const geo = {
        format_version: '1.12.0',
        'minecraft:geometry': [{
            description: { identifier: 'geometry.calib', texture_width: TW, texture_height: TH },
            bones: [{
                name: 'root', pivot: [0, 0, 0], cubes: [{
                    origin: [0, 0, 0], size: [16, 16, 16],
                    uv: Object.fromEntries(names.map((n, i) => [n, { uv: [i * CELL + (n === 'north' ? CELL : 0), 0], uv_size: n === 'north' ? [-CELL, TH] : [CELL, TH] }])),   // 镜像面的 uv 取区域**右**边(负宽向左展开), 之前写成 0 会跑到贴图外
                }],
            }],
        }],
    };
    const geoFile = path.join(ROOT, 'art', 'calib.geo.json');
    fs.writeFileSync(geoFile, JSON.stringify(geo, null, 2));
    const out = path.join(ROOT, 'art', 'calib_faces.png');
    const r = render(geoFile, png, out, [0, 35, 70], 20, 9);
    /* 数值自检: 三个视角面板的中心像素应来自各自朝向面的颜色 */
    const probe = (x, y) => { const o = (y * r.W + x) * 4; return [r.buf[o], r.buf[o + 1], r.buf[o + 2]]; };
    console.log('自检渲染 -> ' + path.relative(ROOT, out) + '  (' + r.W + 'x' + r.H + ', ' + r.faces + ' 个面)');
    console.log('  测试贴图 ' + TW + 'x' + TH + '（6 格异色, 每格左上角深色边 + 中央 4x4 白块, north 面用**负宽度 UV**验证镜像）');
    for (let i = 0; i < 3; i++) console.log('  视角' + i + ' 中心像素 = ' + probe(i * 300 + 150, 150).join(','));
    console.log('  ⚠ 需与 Blockbench 同角度比对后才能确认方向/UV 完全一致; 本图仅用于校准, 不代表正式模型。');
}

const args = process.argv.slice(2);
if (args[0] === '--selftest') selftest();
else if (args.length >= 3) {
    const r = render(args[0], args[1], args[2], (args[3] || '0,35,70').split(',').map(Number), Number(args[4] || 20), Number(args[5] || 9));
    console.log('渲染 -> ' + args[2] + '  (' + r.W + 'x' + r.H + ', ' + r.faces + ' 面)');
} else {
    console.log('用法: --selftest | <geo.json> <tex.png> <out.png> [yaws] [pitch] [zoom]');
}
