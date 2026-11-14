/* ============================================================================
 *  v3_barbs.cjs —— 加回两个主要倒钩（带独立 UV 岛 + 画法, 幂等）
 *
 *  为什么用"后处理"而不是生成器的 put(): 生成器的 put() 会自动装箱分配 UV, 但**没有对应的画法**,
 *  上次就是这么出的怪色块 ✗。这次直接写进 geo 并指定**独立 UV 岛**(实测空闲区), 再自己画。
 *
 *  倒钩(geo 空间; Java 坐标 = geo + [8, 4.5, 8] -> x 9.55..10.55, y 14.35..15.65 / 18.35..19.65):
 *      origin [1.55, 9.85, -0.65] size [1.0, 1.3, 1.3] rotation [0,0,22.5] pivot [1.6, 10.5, 0]
 *      origin [1.55, 13.85, -0.65] size [1.0, 1.3, 1.3] rotation [0,0,22.5] pivot [1.6, 14.5, 0]
 *  岛位: 200,104  32x24  (画成"钢制倒钩": 中暗底 + 外侧冷色受光棱 + 一道暗紫裂纹)
 *  用法: node tools/model3d/v3_barbs.cjs       （必须在贴图重新生成之后跑）
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
const V = { x: 200, y: 104, w: 32, h: 24 };
const uv = Object.fromEntries(FACES.map(f => [f, { uv: [V.x, V.y], uv_size: [V.w, V.h] }]));

/* ---------- ① 写进 geo（幂等） ---------- */
const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'));
const g = geo['minecraft:geometry'][0];
const blade = g.bones.find(b => b.name === 'blade');
const has = blade.cubes.some(c => c.uv && c.uv.north && c.uv.north.uv[0] === 200 && c.uv.north.uv[1] === 104);   // 判据改成 UV 岛(不给 cube 写 name: 渲染器严格校验字段)
if (!has) {
    blade.cubes.push({
        origin: [1.55, 9.85, -0.65], size: [1.0, 1.3, 1.3],
        rotation: [0, 0, 22.5], pivot: [1.6, 10.5, 0], uv,
    });
    blade.cubes.push({
        origin: [1.55, 13.85, -0.65], size: [1.0, 1.3, 1.3],
        rotation: [0, 0, 22.5], pivot: [1.6, 14.5, 0], uv,
    });
    try { fs.chmodSync(GEO, 0o666); } catch (e) { }
    fs.writeFileSync(GEO, JSON.stringify(geo, null, 2) + '\n');
    console.log('① geo: 已加入 barb_lo / barb_hi（blade 骨骼 cube ' + (blade.cubes.length - 2) + ' -> ' + blade.cubes.length + '）');
} else {
    console.log('① geo: 倒钩已存在, 跳过（幂等）');
}

/* ---------- ② 画岛 ---------- */
const tex = decodePNG(PNG);
const W = tex.w, H = tex.h, d = Buffer.from(tex.data);
const put = (x, y, c) => { const o = (y * W + x) * 4; d[o] = c[0]; d[o + 1] = c[1]; d[o + 2] = c[2]; d[o + 3] = 255; };
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const C1 = [16, 14, 24], C2 = [34, 29, 44], C3 = [80, 71, 89], C4 = [86, 51, 116], C5 = [169, 108, 220], C6 = [234, 213, 245];
for (let y = 0; y < V.h; y++) for (let x = 0; x < V.w; x++) {
    let c = mix(C2, C1, y / V.h);                       // 上亮下暗的斜面
    if (x < 2) c = C3;                                   // 外侧改用冷灰(#504759) —— 原来的 #EAD5F5 在 22.5° 斜视下就是两个白点(用户已点名)
    else if (x < 4) c = mix(C2, C3, 0.45);
    else if (x > V.w - 3) c = C1;                        // 内侧贴剑脊 -> 压暗
    if (y === 0) c = mix(c, C3, 0.5);                    // 上沿一条受光
    if (x > 6 && x < 9 && y > 4 && y < V.h - 4) c = mix(c, C4, 0.45);  // 一道暗紫裂纹
    put(V.x + x, V.y + y, c);
}
const crc32 = b => { let c, crc = 0xffffffff; for (const q of b) { crc ^= q; for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0); } return (crc ^ 0xffffffff) >>> 0; };
const chunk = (t, dd) => { const l = Buffer.alloc(4); l.writeUInt32BE(dd.length); const td = Buffer.concat([Buffer.from(t, 'ascii'), dd]); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(td)); return Buffer.concat([l, td, c]); };
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; d.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4); }
try { fs.chmodSync(PNG, 0o666); } catch (e) { }
fs.writeFileSync(PNG, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]));
console.log('② 贴图: 已画倒钩岛 ' + V.x + ',' + V.y + ' ' + V.w + 'x' + V.h + '（外侧受光棱 + 内侧压暗 + 暗紫裂纹）');
