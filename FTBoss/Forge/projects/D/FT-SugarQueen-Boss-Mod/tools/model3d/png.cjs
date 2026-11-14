// 极简 PNG 编解码(Node 内置 zlib, 无外部依赖) + 2D 画布
const zlib = require('zlib');
const fs = require('fs');

const CRC_TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1; t[n] = c; }
  return t;
})();
function crc32(buf) {
  let c = -1;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
  return (c ^ -1) >>> 0;
}
function chunk(type, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length);
  const td = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td));
  return Buffer.concat([len, td, crc]);
}
/** rgba: Uint8Array(w*h*4) */
function encodePNG(w, h, rgba) {
  const raw = Buffer.alloc(h * (w * 4 + 1));
  for (let y = 0; y < h; y++) {
    raw[y * (w * 4 + 1)] = 0;
    Buffer.from(rgba.buffer, rgba.byteOffset + y * w * 4, w * 4).copy(raw, y * (w * 4 + 1) + 1);
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8; ihdr[9] = 6; ihdr[10] = 0; ihdr[11] = 0; ihdr[12] = 0;
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0))
  ]);
}
/** 读 8bit RGB/RGBA 非隔行 PNG(仅用于自检) */
function decodePNG(file) {
  const b = fs.readFileSync(file);
  let pos = 8, w = 0, h = 0, ct = 0, idat = [];
  while (pos < b.length) {
    const len = b.readUInt32BE(pos), type = b.toString('ascii', pos + 4, pos + 8);
    const data = b.subarray(pos + 8, pos + 8 + len);
    if (type === 'IHDR') { w = data.readUInt32BE(0); h = data.readUInt32BE(4); ct = data[9]; }
    else if (type === 'IDAT') idat.push(data);
    else if (type === 'IEND') break;
    pos += 12 + len;
  }
  const raw = zlib.inflateSync(Buffer.concat(idat));
  const ch = ct === 6 ? 4 : ct === 2 ? 3 : 0;
  if (!ch) throw new Error('unsupported colorType ' + ct);
  const stride = w * ch;
  const out = new Uint8Array(w * h * 4);
  let prev = Buffer.alloc(stride);
  for (let y = 0; y < h; y++) {
    const ft = raw[y * (stride + 1)];
    const line = Buffer.from(raw.subarray(y * (stride + 1) + 1, y * (stride + 1) + 1 + stride));
    for (let i = 0; i < stride; i++) {
      const a = i >= ch ? line[i - ch] : 0, bb = prev[i], c = i >= ch ? prev[i - ch] : 0;
      let v = line[i];
      if (ft === 1) v = (v + a) & 255;
      else if (ft === 2) v = (v + bb) & 255;
      else if (ft === 3) v = (v + ((a + bb) >> 1)) & 255;
      else if (ft === 4) { const p = a + bb - c, pa = Math.abs(p - a), pb = Math.abs(p - bb), pc = Math.abs(p - c); v = (v + (pa <= pb && pa <= pc ? a : pb <= pc ? bb : c)) & 255; }
      line[i] = v;
    }
    for (let x = 0; x < w; x++) {
      out[(y * w + x) * 4] = line[x * ch];
      out[(y * w + x) * 4 + 1] = line[x * ch + 1];
      out[(y * w + x) * 4 + 2] = line[x * ch + 2];
      out[(y * w + x) * 4 + 3] = ch === 4 ? line[x * ch + 3] : 255;
    }
    prev = line;
  }
  return { w, h, data: out };
}

/** 简单画布: RGBA 缓冲 + 基础图元 */
class Canvas {
  constructor(w, h) { this.w = w; this.h = h; this.d = new Uint8Array(w * h * 4); }
  set(x, y, r, g, b, a) {
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) return;
    const i = (y * this.w + x) * 4;
    const al = a === undefined ? 1 : a;
    // 必须夹到 0..255: Uint8Array 会**静默截断**取模, 比如 257 -> 1(近黑),
    // 而 r/g/b 在不同亮度上越界时就会写出五颜六色的杂点(实测把亮白写成青色斑点)。
    const cr = Math.max(0, Math.min(255, r)), cg = Math.max(0, Math.min(255, g)), cb = Math.max(0, Math.min(255, b));
    this.d[i] = Math.round(this.d[i] * (1 - al) + cr * al);
    this.d[i + 1] = Math.round(this.d[i + 1] * (1 - al) + cg * al);
    this.d[i + 2] = Math.round(this.d[i + 2] * (1 - al) + cb * al);
    this.d[i + 3] = Math.max(this.d[i + 3], Math.round(255 * al));
  }
  rect(x, y, w, h, r, g, b, a) { for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) this.set(x + i, y + j, r, g, b, a); }
  hline(x0, x1, y, r, g, b, a) { for (let x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) this.set(x, y, r, g, b, a); }
  vline(x, y0, y1, r, g, b, a) { for (let y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) this.set(x, y, r, g, b, a); }
  line(x0, y0, x1, y1, r, g, b, a) {
    const dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
    let err = dx - dy;
    for (;;) { this.set(x0, y0, r, g, b, a); if (x0 === x1 && y0 === y1) break; const e2 = 2 * err; if (e2 > -dy) { err -= dy; x0 += sx; } if (e2 < dx) { err += dx; y0 += sy; } }
  }
  /** 实心三角形(用于 3D 面) —— 两种绕序都接受(投影后绕序会翻转) */
  tri(p0, p1, p2, r, g, b, a) {
    const minX = Math.max(0, Math.floor(Math.min(p0[0], p1[0], p2[0]))), maxX = Math.min(this.w - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0])));
    const minY = Math.max(0, Math.floor(Math.min(p0[1], p1[1], p2[1]))), maxY = Math.min(this.h - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1])));
    const d = (p1[1] - p2[1]) * (p0[0] - p2[0]) + (p2[0] - p1[0]) * (p0[1] - p2[1]);
    if (Math.abs(d) < 1e-9) return;
    for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
      const a0 = ((p1[1] - p2[1]) * (x - p2[0]) + (p2[0] - p1[0]) * (y - p2[1])) / d;
      const a1 = ((p2[1] - p0[1]) * (x - p2[0]) + (p0[0] - p2[0]) * (y - p2[1])) / d;
      const a2 = 1 - a0 - a1;
      const inside = (a0 >= -1e-6 && a1 >= -1e-6 && a2 >= -1e-6) || (a0 <= 1e-6 && a1 <= 1e-6 && a2 <= 1e-6);
      if (inside) this.set(x, y, r, g, b, a);
    }
  }
  quad(pts, r, g, b, a) { this.tri(pts[0], pts[1], pts[2], r, g, b, a); this.tri(pts[0], pts[2], pts[3], r, g, b, a); }
  save(file) { fs.writeFileSync(file, encodePNG(this.w, this.h, this.d)); }
  /** 放大整数倍并加最近邻 */
  scaled(k) { const c = new Canvas(this.w * k, this.h * k); for (let y = 0; y < this.h; y++) for (let x = 0; x < this.w; x++) { const i = (y * this.w + x) * 4; c.rect(x * k, y * k, k, k, this.d[i], this.d[i + 1], this.d[i + 2], this.d[i + 3] / 255); } return c; }
}
module.exports = { encodePNG, decodePNG, Canvas, crc32 };
