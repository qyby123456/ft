/* ============================================================================
 *  v3_tip_bevel.cjs —— 刀刃顶部 45° 斜切（用户选定方案 B）
 *
 *  设计: 保留一级小平顶（左侧）+ **一个 45° 斜切块**切掉剑脊侧上角, 形成"斜尖"。
 *  关键防坑: 斜切块**与相邻台阶同厚（1.10）** ⇒ 面齐平, 不是"半埋薄片"(那会从侧面露小块, 已踩三次)。
 *
 *  ★ 先数值自检再写入: 在候选 (L,H) 里搜一个满足下面三条的解, 全不满足就**拒绝写入**并报错:
 *      ① 旋转后 AABB 的 max x ≤ 10.25（不许凸出剑脊）
 *      ② max y ≤ 24.78（不许比原顶点更高）
 *      ③ 与 tip_b(y 23.85..24.25) 的重叠体积 ≥ 0.2（保证相连, 不能飘）
 *  另外: 给斜切块一个**独立 UV 岛**(实测空闲区 208,176 32×24)并画上去(冷灰受光斜面)。
 *  用法: node tools/model3d/v3_tip_bevel.cjs        （幂等; 必须在贴图重新生成之后跑）
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..');
const GEN = path.join(ROOT, 'tools', 'model3d', 'regen_shadow_sword.cjs');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');

/* ---------- ① 数值搜索安全解 ---------- */
const rad = d => d * Math.PI / 180;
function rotZ(p, deg, piv) {
    const c = Math.cos(rad(deg)), s = Math.sin(rad(deg));
    const x = p[0] - piv[0], y = p[1] - piv[1];
    return [x * c - y * s + piv[0], x * s + y * c + piv[1]];
}
const PIVOT = [10.20, 23.60];
const tipB = [6.75, 23.85, 10.20, 24.25];              // x0,y0,x1,y1
const overlap = (a, b) => Math.max(0, Math.min(a[2], b[2]) - Math.max(a[0], b[0])) * Math.max(0, Math.min(a[3], b[3]) - Math.max(a[1], b[1]));
let chosen = null;
for (const L of [1.35, 1.20, 1.05, 1.50]) for (const H of [0.55, 0.45, 0.65, 0.35]) {
    // 薄板放在枢轴**下方**、绕枢轴转 -45° -> 斜带朝左上延伸(切削右上角), max x 恰好 = 枢轴 x
    const x0 = PIVOT[0] - L, y0 = PIVOT[1] - H, x1 = PIVOT[0], y1 = PIVOT[1];
    const pts = [[x0, y0], [x1, y0], [x1, y1], [x0, y1]].map(p => rotZ(p, -45, PIVOT));
    const A = [Math.min(...pts.map(p => p[0])), Math.min(...pts.map(p => p[1])), Math.max(...pts.map(p => p[0])), Math.max(...pts.map(p => p[1]))];
    if (A[2] > 10.25 + 1e-6) continue;
    if (A[3] > 24.78 + 1e-6) continue;
    if (overlap(A, tipB) < 0.2) continue;
    if (A[3] <= 24.15) continue;                       // ★ 必须朝上(上一版就是漏了这条, 选到朝下的解)
    chosen = { L, H, box: A };
    break;
}
if (!chosen) { console.error('✗ 没有满足约束的 45° 斜切解 —— 拒绝写入（不猜几何）'); process.exit(1); }
console.log('① 斜切解: L=' + chosen.L + ' H=' + chosen.H + '  旋转后 AABB x ' + chosen.box[0].toFixed(2) + '..' + chosen.box[2].toFixed(2) + '  y ' + chosen.box[1].toFixed(2) + '..' + chosen.box[3].toFixed(2) + '  (未凸出脊 10.25 / 未超高 24.78 / 与上一级重叠 ≥0.2 ✓)');

/* ---------- ② 写进生成器（幂等） ---------- */
let s = fs.readFileSync(GEN, 'utf8');
if (s.includes("put('tip_bevel'")) {
    console.log('② 生成器: 斜切已存在, 跳过（幂等）');
} else {
    const anchor = `put('tip_c', 7.35, 9.35, 24.25, 24.65, 0.80, 'face');`;   // 当前生成器里就是收窄过的这行
    const n = s.split(anchor).length - 1;
    if (n !== 1) { console.error('✗ 找不到 tip_c 那行（命中 ' + n + '）—— 拒绝写入'); process.exit(1); }
    const L = chosen.L, H = chosen.H;
    s = s.replace(anchor,
        `put('tip_c', 7.35, 9.35, 24.25, 24.65, 0.80, 'face');    // 小平顶(右侧让位给 45° 斜切)\n` +
        `/* 顶部 45° 斜切: 与相邻台阶同厚(1.10) -> 面齐平, 不是半埋薄片 */\n` +
        `put('tip_bevel', ${(10.20 - L).toFixed(2)}, 10.20, ${(23.60 - H).toFixed(2)}, 23.60, 1.10, 'face', 0, [-45, 'z', [10.20, 23.60, 8]]);`);
    fs.writeFileSync(GEN, s);
    console.log('② 生成器: 已加入 tip_bevel（tip_c 右侧收窄到 x 9.35 让位）');
}

/* ---------- ③ 独立 UV 岛 + 画法 ---------- */
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const PNG = path.join(AS, 'textures', 'item', 'shadow_sword.png');
const { decodePNG } = require('./png.cjs');
const FACES = ['north', 'south', 'east', 'west', 'up', 'down'];
const ISLE = { x: 208, y: 176, w: 32, h: 24 };
const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'));
const bladeBone = geo['minecraft:geometry'][0].bones.find(b => b.name === 'blade');
const bevel = bladeBone.cubes.find(c => c.rotation && c.rotation[2] === -45);   // geo 空间: 只认 -45, 别再按 Java 的 origin.y 去匹配
if (bevel) {
    bevel.uv = Object.fromEntries(FACES.map(f => [f, { uv: [ISLE.x, ISLE.y], uv_size: [ISLE.w, ISLE.h] }]));
    fs.chmodSync(GEO, 0o666);
    fs.writeFileSync(GEO, JSON.stringify(geo, null, 2) + '\n');
    console.log('③ geo: 斜切块用独立 UV 岛 ' + ISLE.x + ',' + ISLE.y + ' ' + ISLE.w + '×' + ISLE.h);
} else {
    console.log('③ geo: 未找到斜切 cube（可能生成器未生效）');
}
const tex = decodePNG(PNG);
const W = tex.w, H2 = tex.h, d = Buffer.from(tex.data);
const put = (x, y, c) => { const o = (y * W + x) * 4; d[o] = c[0]; d[o + 1] = c[1]; d[o + 2] = c[2]; d[o + 3] = 255; };
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const C1 = [16, 14, 24], C2 = [34, 29, 44], C3 = [80, 71, 89], C4 = [86, 51, 116], C6 = [234, 213, 245];
for (let y = 0; y < ISLE.h; y++) for (let x = 0; x < ISLE.w; x++) {
    let c = mix(C3, C2, y / ISLE.h);                 // 受光斜面: 上亮下暗
    if (y < 2) c = mix(C3, C6, 0.25);                // 斜面上沿一道细亮边(不刺眼)
    if (y > ISLE.h - 3) c = C1;                      // 下沿压暗, 与刀身接缝
    if (x < 2) c = C1;                               // 左侧贴台阶 -> 暗
    if (Math.abs(x - ISLE.w / 2) < 2 && y > 6 && y < ISLE.h - 6) c = mix(c, C4, 0.4);   // 中间一道暗紫(与全剑呼应)
    put(ISLE.x + x, ISLE.y + y, c);
}
const crc32 = b => { let c, crc = 0xffffffff; for (const q of b) { crc ^= q; for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0); } return (crc ^ 0xffffffff) >>> 0; };
const chunk = (t, dd) => { const l = Buffer.alloc(4); l.writeUInt32BE(dd.length); const td = Buffer.concat([Buffer.from(t, 'ascii'), dd]); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(td)); return Buffer.concat([l, td, c]); };
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H2, 4); ihdr[8] = 8; ihdr[9] = 6;
const raw = Buffer.alloc((W * 4 + 1) * H2);
for (let y = 0; y < H2; y++) { raw[y * (W * 4 + 1)] = 0; d.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4); }
fs.chmodSync(PNG, 0o666);
fs.writeFileSync(PNG, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]));
console.log('④ 贴图: 已画斜切岛');
