/* ============================================================================
 *  v3_tip_bevel2.cjs —— 刀刃顶部 45° 斜切（正确实现：**纯 geo 后处理**）
 *
 *  为什么重写: 上一版走生成器的 put() + rotation 枢轴空间不一致 -> 整块被甩到侧边 7.28 单位外 ✗
 *  （用户看到"侧边悬浮方块"）。倒钩用 geo 后处理一直是对的 -> 斜切照同一套写。
 *
 *  geo 参数（本模型 geo = Java − [8, 4.5, 8]）:
 *     枢轴 = 斜切下沿与剑脊的交点 (2.20, 19.35, 0)（= 剑尖 tip_b 的脊角）
 *     薄板 = origin [1.10, 18.95, -0.475]  size [1.10, 0.40, 0.95]  rotation [0,0,-45]
 *     -> 旋转后 AABB x 1.14..2.20（恰好贴脊不凸出）, y 19.07..20.13（顶端 20.15 之下, 不超高）
 *     厚度 0.95 与 tip_b 一致 -> 面齐平, 不是半埋薄片
 *  写入前**自检**: 与 blade 其它 cube 的最大重叠体积必须 ≥ 0.2（不满足就拒绝写入）。
 *  UV 岛: 208,176 32×24（实测空闲区）; 画冷灰受光斜面。
 *  用法: node tools/model3d/v3_tip_bevel2.cjs      （必须在贴图重新生成之后跑; 幂等）
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const PNG = path.join(AS, 'textures', 'item', 'shadow_sword.png');
const { decodePNG } = require('./png.cjs');
const FACES = ['north', 'south', 'east', 'west', 'up', 'down'];

const CUBE = {
    origin: [1.10, 18.95, -0.475], size: [1.10, 0.40, 0.95],
    rotation: [0, 0, -45], pivot: [2.20, 19.35, 0],
};
const ISLE = { x: 208, y: 176, w: 32, h: 24 };

/* ---------- 自检工具（与复核脚本同一套数学） ---------- */
const rad = d => d * Math.PI / 180;
function aabb(c) {
    const inf = c.inflate || 0;
    const x0 = c.origin[0] - inf, y0 = c.origin[1] - inf, z0 = c.origin[2] - inf;
    const x1 = c.origin[0] + c.size[0] + inf, y1 = c.origin[1] + c.size[1] + inf, z1 = c.origin[2] + c.size[2] + inf;
    const pts = [];
    for (const x of [x0, x1]) for (const y of [y0, y1]) for (const z of [z0, z1]) {
        let p = [x, y, z];
        if (c.rotation) {
            const piv = c.pivot || [0, 0, 0];
            let q = [p[0] - piv[0], p[1] - piv[1], p[2] - piv[2]];
            const [a, b, d2] = c.rotation;
            let co = Math.cos(rad(a)), si = Math.sin(rad(a)); q = [q[0], q[1] * co - q[2] * si, q[1] * si + q[2] * co];
            co = Math.cos(rad(b)); si = Math.sin(rad(b)); q = [q[0] * co + q[2] * si, q[1], -q[0] * si + q[2] * co];
            co = Math.cos(rad(d2)); si = Math.sin(rad(d2)); q = [q[0] * co - q[1] * si, q[0] * si + q[1] * co, q[2]];
            p = [q[0] + piv[0], q[1] + piv[1], q[2] + piv[2]];
        }
        pts.push(p);
    }
    return [Math.min(...pts.map(p => p[0])), Math.min(...pts.map(p => p[1])), Math.min(...pts.map(p => p[2])),
            Math.max(...pts.map(p => p[0])), Math.max(...pts.map(p => p[1])), Math.max(...pts.map(p => p[2]))];
}
const vol = (a, b) => { const o = [0, 1, 2].map(i => Math.min(a[i + 3], b[i + 3]) - Math.max(a[i], b[i])); return (o[0] > 0 && o[1] > 0 && o[2] > 0) ? o[0] * o[1] * o[2] : 0; };

/* ---------- ① 读 geo + 自检 ---------- */
const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'));
const g = geo['minecraft:geometry'][0];
const blade = g.bones.find(b => b.name === 'blade');
if (blade.cubes.some(c => c.rotation && c.rotation[2] === -45)) {
    console.log('① 斜切已存在, 跳过（幂等）');
} else {
    const bb = aabb(CUBE);
    const best = Math.max(...blade.cubes.map(c => vol(bb, aabb(c))));
    console.log('① 自检: 旋转后 AABB x ' + bb[0].toFixed(2) + '..' + bb[3].toFixed(2) +
        '  y ' + bb[1].toFixed(2) + '..' + bb[4].toFixed(2) + '  z ' + bb[2].toFixed(2) + '..' + bb[5].toFixed(2));
    console.log('   与 blade 其它 cube 的最大重叠体积 = ' + best.toFixed(3) + (best >= 0.2 ? '  ✓ 贴合' : '  ✗ 悬空'));
    if (best < 0.2) { console.error('✗ 自检未通过 —— 拒绝写入（绝不把悬空块写进模型）'); process.exit(1); }
    if (bb[3] > 2.21) { console.error('✗ 凸出剑脊(>2.21) —— 拒绝写入'); process.exit(1); }
    if (bb[4] > 20.16) { console.error('✗ 高过刀尖(>20.16) —— 拒绝写入'); process.exit(1); }
    CUBE.uv = Object.fromEntries(FACES.map(f => [f, { uv: [ISLE.x, ISLE.y], uv_size: [ISLE.w, ISLE.h] }]));
    blade.cubes.push(CUBE);
    fs.chmodSync(GEO, 0o666);
    fs.writeFileSync(GEO, JSON.stringify(geo, null, 2) + '\n');
    console.log('② geo: 已加入斜切（blade cube ' + (blade.cubes.length - 1) + ' -> ' + blade.cubes.length + '）, UV 岛 ' + ISLE.x + ',' + ISLE.y);
}

/* ---------- ③ 画 UV 岛 ---------- */
const tex = decodePNG(PNG);
const W = tex.w, H = tex.h, d = Buffer.from(tex.data);
const put = (x, y, c) => { const o = (y * W + x) * 4; d[o] = c[0]; d[o + 1] = c[1]; d[o + 2] = c[2]; d[o + 3] = 255; };
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const C1 = [16, 14, 24], C2 = [34, 29, 44], C3 = [80, 71, 89], C4 = [86, 51, 116], C6 = [234, 213, 245];
for (let y = 0; y < ISLE.h; y++) for (let x = 0; x < ISLE.w; x++) {
    let c = mix(C3, C2, y / ISLE.h);
    if (y < 2) c = mix(C3, C6, 0.22);                    // 上沿一道细亮边
    if (y > ISLE.h - 3) c = C1;                          // 下沿压暗接缝
    if (x < 2) c = C1;
    if (Math.abs(x - ISLE.w / 2) < 2 && y > 6 && y < ISLE.h - 6) c = mix(c, C4, 0.4);
    put(ISLE.x + x, ISLE.y + y, c);
}
const crc32 = b => { let c, crc = 0xffffffff; for (const q of b) { crc ^= q; for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0); } return (crc ^ 0xffffffff) >>> 0; };
const chunk = (t, dd) => { const l = Buffer.alloc(4); l.writeUInt32BE(dd.length); const td = Buffer.concat([Buffer.from(t, 'ascii'), dd]); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(td)); return Buffer.concat([l, td, c]); };
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; d.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4); }
fs.chmodSync(PNG, 0o666);
fs.writeFileSync(PNG, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]));
console.log('③ 贴图: 已画斜切岛（冷灰受光斜面 + 上沿细亮边 + 下沿接缝暗 + 中间暗紫）');
