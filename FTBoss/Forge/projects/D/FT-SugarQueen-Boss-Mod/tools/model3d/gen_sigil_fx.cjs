/* ============================================================================
 *  gen_sigil_fx.cjs —— 「暗影镇印」的两张特效贴图（程序化生成, 原创, 不复刻任何模组资产）
 *   ① shadow_sigil.png  256×256  地面魔法阵: 双层断续外环(⌀2.8 视觉) + 四方向尖角 + 内环 + 8 个原创符文
 *   ② shadow_pillar.png  32×128  暗影能量柱: 上浓下淡的竖直渐变 + 两侧亮边
 *  配色（按规格）: 暗影底色 #100E18 / 暗紫 #563374 / 主紫 #A96CDC (亮紫只用于轮廓与冲击)
 *  规格要求: 不填半透明圆盘（"别做成发亮的饼"）✓ 主体黑紫、只有轮廓亮 ✓ 背景透明 ✓
 *  只写 assets/ftboss/textures/fx/ 与新文件; 不动任何现有资源。
 *  用法: node tools/model3d/gen_sigil_fx.cjs
 * ==========================================================================*/
'use strict';
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const ROOT = path.resolve(__dirname, '..', '..');
const OUT = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'textures', 'fx');
const ART = path.join(ROOT, 'art');

const crc32 = b => { let c, crc = 0xffffffff; for (const q of b) { crc ^= q; for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0); } return (crc ^ 0xffffffff) >>> 0; };
const chunk = (t, dd) => { const l = Buffer.alloc(4); l.writeUInt32BE(dd.length); const td = Buffer.concat([Buffer.from(t, 'ascii'), dd]); const c = Buffer.alloc(4); c.writeUInt32BE(crc32(td)); return Buffer.concat([l, td, c]); };
function savePNG(file, W, H, d) {
    const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(W, 0); ihdr.writeUInt32BE(H, 4); ihdr[8] = 8; ihdr[9] = 6;
    const raw = Buffer.alloc((W * 4 + 1) * H);
    for (let y = 0; y < H; y++) { raw[y * (W * 4 + 1)] = 0; d.copy(raw, y * (W * 4 + 1) + 1, y * W * 4, (y + 1) * W * 4); }
    fs.mkdirSync(path.dirname(file), { recursive: true });
    try { fs.chmodSync(file, 0o666); } catch (e) { }
    fs.writeFileSync(file, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]));
}
const C_DEEP = [16, 14, 24], C_DARK = [86, 51, 116], C_MAIN = [169, 108, 220];

/* ---------------- ① 魔法阵 256×256 ---------------- */
const S = 256;
const sig = Buffer.alloc(S * S * 4, 0);
const put = (x, y, c, a) => {
    x = Math.round(x); y = Math.round(y);
    if (x < 0 || y < 0 || x >= S || y >= S || a <= 0) return;
    const o = (y * S + x) * 4;
    const old = sig[o + 3] / 255, na = a / 255;
    const out = na + old * (1 - na);
    if (out <= 0) return;
    for (let i = 0; i < 3; i++) sig[o + i] = Math.round((c[i] * na + sig[o + i] * old * (1 - na)) / out);
    sig[o + 3] = Math.round(out * 255);
};
/** 断续圆环: seg 段, 每段占 frac, 双层厚度 */
function ring(cx, cy, r, thick, seg, frac, c, a) {
    for (let i = 0; i < seg; i++) {
        const t0 = (i / seg) * Math.PI * 2, t1 = t0 + (Math.PI * 2 / seg) * frac;
        for (let t = t0; t < t1; t += 0.004) {
            for (let d = -thick; d <= thick; d += 0.5) {
                put(cx + Math.cos(t) * (r + d), cy + Math.sin(t) * (r + d), c, a);
            }
        }
    }
}
/** 8 个原创符文: 每个是"干线 + 1~2 条支线"的组合, 各不相同(用下标决定形状) */
function rune(cx, cy, ang, size, idx, c, a) {
    const px = cx + Math.cos(ang) * 80, py = cy + Math.sin(ang) * 80;
    const line = (x0, y0, x1, y1, w) => {
        const n = 40;
        for (let i = 0; i <= n; i++) {
            const x = x0 + (x1 - x0) * i / n, y = y0 + (y1 - y0) * i / n;
            for (let dx = -w; dx <= w; dx += 0.5) for (let dy = -w; dy <= w; dy += 0.5) put(x + dx, y + dy, c, a);
        }
    };
    const ux = Math.cos(ang + Math.PI / 2), uy = Math.sin(ang + Math.PI / 2);   // 切向
    const rx = Math.cos(ang), ry = Math.sin(ang);                               // 径向
    const P = (t, r) => [px + ux * t + rx * r, py + uy * t + ry * r];
    let A = P(-size, 0), B = P(size, 0);
    line(A[0], A[1], B[0], B[1], 1.1);                                          // 主干(切向)
    if (idx % 4 === 0) { const C = P(0, -size * 0.9); line(A[0], A[1], C[0], C[1], 1.0); line(B[0], B[1], C[0], C[1], 1.0); }
    else if (idx % 4 === 1) { const C = P(0, size * 0.9); line(A[0], A[1], C[0], C[1], 1.0); line(B[0], B[1], C[0], C[1], 1.0); }
    else if (idx % 4 === 2) { const C = P(0, -size * 0.9), D = P(0, size * 0.9); line(A[0], A[1], C[0], C[1], 1.0); line(B[0], B[1], D[0], D[1], 1.0); }
    else { line(A[0], A[1], B[0], B[1], 2.0); const C = P(0, -size * 0.55); line(C[0], C[1], P(size, size * 0.6)[0], P(size, size * 0.6)[1], 0.9); }
}
const cx = S / 2 - 0.5, cy = S / 2 - 0.5;
ring(cx, cy, 118, 1.6, 24, 0.62, C_DARK, 210);        // 外环(断续)
ring(cx, cy, 108, 0.9, 24, 0.62, C_MAIN, 150);        // 外环内衬(细)
for (let k = 0; k < 4; k++) {                          // 四个方向尖角
    const a = k * Math.PI / 2 + Math.PI / 4;
    for (let r = 96; r < 126; r += 0.5) {
        const w = (126 - r) / 8;
        for (let d = -w; d <= w; d += 0.5) put(cx + Math.cos(a) * r + Math.cos(a + Math.PI / 2) * d, cy + Math.sin(a) * r + Math.sin(a + Math.PI / 2) * d, C_MAIN, 190);
    }
}
ring(cx, cy, 80, 1.2, 1, 1, C_DARK, 200);              // 内环
for (let k = 0; k < 8; k++) rune(cx, cy, k * Math.PI / 4 + Math.PI / 8, 11, k, C_MAIN, 205);
for (let r = 0; r < 46; r += 0.6) {                    // 阵心: 淡暗紫雾(很淡, 不做成"发亮的饼")
    const a = 36 * (1 - r / 46);
    for (let t = 0; t < Math.PI * 2; t += 0.02) put(cx + Math.cos(t) * r, cy + Math.sin(t) * r, C_DEEP, a);
}
savePNG(path.join(OUT, 'shadow_sigil.png'), S, S, sig);

/* ---------------- ② 能量柱 32×128 ---------------- */
const PW = 32, PH = 128;
const pil = Buffer.alloc(PW * PH * 4, 0);
for (let y = 0; y < PH; y++) {
    const t = y / (PH - 1);                            // 0 = 顶, 1 = 底(触地端)
    for (let x = 0; x < PW; x++) {
        const dx = Math.abs(x - (PW - 1) / 2) / (PW / 2);   // 0 中心, 1 边缘
        let a = 0;
        if (dx < 0.30) a = 235 - 120 * t;              // 芯: 上浓下淡
        else if (dx < 0.62) a = 150 - 95 * t;
        else if (dx < 0.86) a = 70 - 55 * t;
        else a = 26;
        const c = dx < 0.30 ? (t < 0.35 ? C_MAIN : C_DARK) : C_DARK;
        const o = (y * PW + x) * 4;
        pil[o] = c[0]; pil[o + 1] = c[1]; pil[o + 2] = c[2]; pil[o + 3] = Math.max(0, Math.round(a));
    }
}
for (let y = 0; y < PH; y++) {                         // 两侧亮边(轮廓)
    for (const x of [3, PW - 4]) {
        const o = (y * PW + x) * 4;
        pil[o] = C_MAIN[0]; pil[o + 1] = C_MAIN[1]; pil[o + 2] = C_MAIN[2];
        pil[o + 3] = Math.max(pil[o + 3], Math.round(180 - 140 * (y / PH)));
    }
}
savePNG(path.join(OUT, 'shadow_pillar.png'), PW, PH, pil);

/* ---------------- 预览图（放大 + 棋盘底, 方便肉眼看） ---------------- */
function preview(src, dst, scale) {
    const rd = fs.readFileSync(src);
    // 极简 PNG 解码: 复用 render 工具的 decodePNG
    const { decodePNG } = require('./png.cjs');
    const t = decodePNG(src);
    const W = t.w * scale, H = t.h * scale;
    const out = Buffer.alloc(W * H * 4);
    for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
        const sx = Math.floor(x / scale), sy = Math.floor(y / scale);
        const o = (sy * t.w + sx) * 4, q = (y * W + x) * 4;
        const a = t.data[o + 3] / 255;
        const bg = ((Math.floor(x / 8) + Math.floor(y / 8)) % 2) ? 90 : 130;
        out[q] = Math.round(t.data[o] * a + bg * (1 - a));
        out[q + 1] = Math.round(t.data[o + 1] * a + bg * (1 - a));
        out[q + 2] = Math.round(t.data[o + 2] * a + bg * (1 - a));
        out[q + 3] = 255;
    }
    savePNG(dst, W, H, out);
}
preview(path.join(OUT, 'shadow_sigil.png'), path.join(ART, 'fx_sigil_preview.png'), 2);
preview(path.join(OUT, 'shadow_pillar.png'), path.join(ART, 'fx_pillar_preview.png'), 3);
console.log('① ' + path.relative(ROOT, path.join(OUT, 'shadow_sigil.png')) + '  256×256（外环⌀2.8/内环+8符文/4尖角, 透明底 ✓）');
console.log('② ' + path.relative(ROOT, path.join(OUT, 'shadow_pillar.png')) + '  32×128（上浓下淡 + 两侧亮边 ✓）');
console.log('③ 预览: art/fx_sigil_preview.png（2×）/ art/fx_pillar_preview.png（3×, 棋盘底看透明度 ✓）');
