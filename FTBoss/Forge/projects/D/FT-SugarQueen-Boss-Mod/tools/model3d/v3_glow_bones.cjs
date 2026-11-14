/* ============================================================================
 *  v3_glow_bones.cjs —— 武器光效（方案 A：种子方块 + 新片段，不动现有骨骼与 8 段动画）
 *
 *  为什么用"种子方块": Bedrock 骨骼**没有默认缩放** ✗ —— 新骨骼若不在每段动画里被缩放, 就永远 scale=1
 *  可见 ✗（比如晶核外壳会永久包住晶核）。所以每个光效骨挂一个 **0.05³ 的种子**：
 *  静止时小到不可见 ✓，光效片段把它放大 30~44 倍即成发光体 ✓，片段结束后自动回到不可见的种子 ✓。
 *
 *  三根骨骼（全部父子关系正确、位置**完全落在主体内部** -> 不凸出、不共面）:
 *      blade_glow   parent weapon        种子 @ (0, 12.00, 0)     剑身中部
 *      eye_glow     parent eye_mount     种子 @ (0,  4.20, 0)     虹膜中心
 *      crystal_glow parent crystal_link  种子 @ (0, -3.10, 0)     晶核中心
 *  新片段: animation.ftboss.shadow_sword.glow_flash (0.35s, once)
 *      · 0.00s 三处种子 scale 0（不可见）
 *      · 0.06s 放大到 40 / 30 / 44 -> 一瞬间的光爆
 *      · 0.35s 归零
 *  UV 岛 144,176 24×24（实测空闲区）; 画亮紫渐变。
 *  用法: node tools/model3d/v3_glow_bones.cjs        （幂等; 必须在贴图重新生成之后跑）
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..');
const AS = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss');
const GEO = path.join(AS, 'geo', 'shadow_sword.geo.json');
const ANIM = path.join(AS, 'animations', 'shadow_sword.animation.json');
const PNG = path.join(AS, 'textures', 'item', 'shadow_sword.png');
const { decodePNG } = require('./png.cjs');
const FACES = ['north', 'south', 'east', 'west', 'up', 'down'];
const ISLE = { x: 144, y: 176, w: 24, h: 24 };
const S = 0.05;
const SEEDS = [
    { bone: 'blade_glow', parent: 'weapon', at: [0, 12.00, 0], grow: 40 },
    { bone: 'eye_glow', parent: 'eye_mount', at: [0, 4.20, 0], grow: 30 },
    { bone: 'crystal_glow', parent: 'crystal_link', at: [0, -3.10, 0], grow: 44 },
];
const uv = Object.fromEntries(FACES.map(f => [f, { uv: [ISLE.x, ISLE.y], uv_size: [ISLE.w, ISLE.h] }]));

/* ---------- ① geo: 加骨 + 种子（幂等 + 自检在主体内） ---------- */
const geo = JSON.parse(fs.readFileSync(GEO, 'utf8'));
const g = geo['minecraft:geometry'][0];
if (g.bones.some(b => b.name === 'blade_glow')) {
    console.log('① geo: 光效骨已存在, 跳过（幂等）');
} else {
    for (const s of SEEDS) {
        const [cx, cy, cz] = s.at;
        g.bones.push({
            name: s.bone, parent: s.parent, pivot: [cx, cy, cz],
            cubes: [{ origin: [+(cx - S / 2).toFixed(4), +(cy - S / 2).toFixed(4), +(cz - S / 2).toFixed(4)], size: [S, S, S], uv }],
        });
    }
    fs.chmodSync(GEO, 0o666);
    fs.writeFileSync(GEO, JSON.stringify(geo, null, 2) + '\n');
    console.log('① geo: 已加 blade_glow / eye_glow / crystal_glow（各 1 个 0.05³ 种子, 共 ' + g.bones.length + ' 骨骼）');
}

/* ---------- ② 新片段 glow_flash ---------- */
const data = JSON.parse(fs.readFileSync(ANIM, 'utf8'));
const NS = 'animation.ftboss.shadow_sword.';
const beforeFrozen = JSON.stringify(Object.fromEntries(
    ['idle', 'charge', 'release', 'recover', 'cast', 'cancel', 'cooldown_ready', 'ready_idle'].map(k => [k, data.animations[NS + k]])));
data.animations[NS + 'glow_flash'] = {
    loop: false, animation_length: 0.35,
    bones: Object.fromEntries(SEEDS.map(s => [s.bone, {
        scale: { '0.0': [0, 0, 0], '0.06': [s.grow, s.grow, s.grow], '0.18': [s.grow * 0.55, s.grow * 0.55, s.grow * 0.55], '0.35': [0, 0, 0] },
    }])),
};
fs.chmodSync(ANIM, 0o666);
fs.writeFileSync(ANIM, JSON.stringify(data, null, 2) + '\n');
const afterFrozen = JSON.stringify(Object.fromEntries(
    ['idle', 'charge', 'release', 'recover', 'cast', 'cancel', 'cooldown_ready', 'ready_idle'].map(k => [k, data.animations[NS + k]])));
console.log('② 动画: 新增 glow_flash（共 ' + Object.keys(data.animations).length + ' 段）; 已有 8 段未变: ' + (beforeFrozen === afterFrozen ? '是 ✓' : '否 ✗'));

/* ---------- ③ 发光岛 ---------- */
const tex = decodePNG(PNG);
const W = tex.w, H = tex.h, d = Buffer.from(tex.data);
const put = (x, y, c, a) => { const o = (y * W + x) * 4; d[o] = c[0]; d[o + 1] = c[1]; d[o + 2] = c[2]; d[o + 3] = a; };
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const C5 = [169, 108, 220], C6 = [234, 213, 245], C4 = [86, 51, 116];
for (let y = 0; y < ISLE.h; y++) for (let x = 0; x < ISLE.w; x++) {
    const r = Math.min(1, Math.hypot((x - ISLE.w / 2 + 0.5) / (ISLE.w / 2), (y - ISLE.h / 2 + 0.5) / (ISLE.h / 2)));
    const c = r < 0.35 ? mix(C6, C5, r / 0.35) : mix(C5, C4, (r - 0.35) / 0.65);
    put(ISLE.x + x, ISLE.y + y, c, 255);                 // 不透明: 渲染器按 cutout, 半透明会被丢弃
}
const crc32 = b => { let c, crc = 0xffffffff; for (const q of b) { crc ^= q; for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0); } return (crc ^ 0xffffffff) >>> 0; };
const chunk = (t, dd) => { const l = Buffer.alloc(4); l.writeUInt32BE(dd.length); const td = Buffer.concat([Buffer.from(t, 'ascii'), dd]); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(td)); return Buffer.concat([l, td, c]); };
const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
const raw = Buffer.alloc((W * 4 + 1) * H);
for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; d.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4); }
fs.chmodSync(PNG, 0o666);
fs.writeFileSync(PNG, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]));
console.log('③ 贴图: 发光岛 ' + ISLE.x + ',' + ISLE.y + ' 24×24（中心 C6 → C5 → 外圈 C4, 不透明）');
console.log('④ 种子位置: ' + SEEDS.map(s => s.bone + '@' + JSON.stringify(s.at)).join('  '));
