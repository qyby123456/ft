// 五把 Boss 主题武器的物品贴图绘制(32x32, 逐像素程序化)
//   主题对应: 姜饼王=甜点 / 屠夫=肉 / 沙耶=茶 / 酒桶王=酒 / 鼠群=奶酪
//
// 用法: node gen_weapons.cjs <输出目录>
//   写出 <输出目录>/textures/item/<id>.png    (32x32 RGBA) —— 5 张
//   写出 <输出目录>/models/item/<id>.json     (与仓库现有文件逐字一致, 内容不变)
//   写出 <输出目录>/weapons_row.png           (32x32 k=8 放大总览)
//   写出 桌面 FT-模型检查/武器图标预览.png      (32x32@8x 与 16x16@16x 并排)
//
// 绘制思路:
//   1) 先铺"物体色"(各部件基础色, 同时作为外描边的取色源)
//   2) 再按材质上明暗 —— 光源固定在左上: light(0.7071,-0.7071)
//      · shadeRibbon  沿骨架扫掠的带状体(杆/刀身/柄): 圆柱体积感 + 可选金属高光
//      · shadeSphere  球体(糖霜球)      · shadeEllipse 竖立圆面(奶酪轮端面)
//      · shadeHBody   水平圆柱(酒瓶桶身): 逐条竖带 + 桶板缝
//   3) 叠材质细节(木纹/金属铆钉/叶脉/绳缠/奶酪孔/血渍/高光/反光)
//   4) 最后外描边: 接边处已有描边色, 其余由内向外取色压暗, 保证轮廓清晰不糊
//
// 注意: 像素质子缓冲下标必须是整数 —— 所有写入最终都经 Sprite.put 的 Math.round。
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const OUT = process.argv[2] || 'out_weapons';
const SIZE = 32;
const DESKTOP = 'C:\\Users\\YOUR_USERNAME\\Desktop\\FT-模型检查\\武器图标预览.png';
fs.mkdirSync(path.join(OUT, 'textures/item'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'models/item'), { recursive: true });

/* ==================== 基础工具 ==================== */
const clamp = (v, a, b) => v < a ? a : v > b ? b : v;
const rnd = Math.round;
const TAU = Math.PI * 2;
function hash(x, y) { const s = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return s - Math.floor(s); }
function lerp3(a, b, t) { return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t]; }
function mul3(c, k) { return [c[0] * k, c[1] * k, c[2] * k]; }
function mix3(a, b, t) { return lerp3(a, b, clamp(t, 0, 1)); }
const rgbs = c => [c[0], c[1], c[2]];

class Sprite {
  constructor(w, h) { this.w = w; this.h = h; this.cv = new Canvas(w, h); this.mc = new Canvas(w, h); this.reg = new Map(); }
  put(x, y, c, force) {
    if (!c) return;
    x = rnd(x); y = rnd(y);
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) return;
    const k = y * this.w + x;
    if (!force && this.reg.has(k)) return;
    this.reg.set(k, rgbs(c));
    this.cv.set(x, y, c[0], c[1], c[2], 1);
    this.mc.set(x, y, 255, 255, 255, 1);
  }
  has(x, y) { x = rnd(x); y = rnd(y); return x >= 0 && y >= 0 && x < this.w && y < this.h && this.reg.has(y * this.w + x); }
  get(x, y) { return this.reg.get(rnd(y) * this.w + rnd(x)) || null; }
  spanRow(y, xa, xb, cf) {
    y = rnd(y);
    if (y < 0 || y >= this.h) return;
    let x0 = rnd(Math.ceil(Math.min(xa, xb))), x1 = rnd(Math.floor(Math.max(xa, xb)));
    if (x1 < x0) { const t = x0; x0 = x1; x1 = t; }
    for (let x = x0; x <= x1; x++) this.put(x, y, cf(x, y));
  }
  /** 多边形填充(nonzero)。cf 返回数组=着色, 返回 falsy=只登记不画(用于做轮廓掩码) */
  fillPoly(pts, cf) {
    const H = this.h, W = this.w;
    for (let y = 0; y < H; y++) {
      const xs = [], dirs = [], yc = y + 0.5;
      for (let i = 0; i < pts.length; i++) {
        const p = pts[i], q = pts[(i + 1) % pts.length];
        if ((p[1] <= yc && q[1] > yc) || (q[1] <= yc && p[1] > yc)) {
          xs.push(p[0] + (yc - p[1]) / (q[1] - p[1]) * (q[0] - p[0]));
          dirs.push(q[1] > p[1] ? 1 : -1);
        }
      }
      const order = xs.map((v, i) => i).sort((a, b) => xs[a] - xs[b]);
      let wind = 0;
      for (let i = 0; i < order.length; i++) {
        const was = wind; wind += dirs[order[i]];
        if (was === 0 && wind !== 0 && i + 1 < order.length) {
          const xa = Math.ceil(xs[order[i]] - 0.5), xb = Math.floor(xs[order[i + 1]] - 0.5);
          for (let x = xa; x <= xb; x++) {
            if (x < 0 || x >= W) continue;
            const c = cf(x, y);
            if (c) this.put(x, y, c);
          }
        }
      }
    }
  }
  shapeEllipse(cx, cy, rx, ry, cf) {
    for (let y = Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
      const t = (y + 0.5 - cy) / ry;
      if (Math.abs(t) >= 1) continue;
      const hw = rx * Math.sqrt(1 - t * t);
      this.spanRow(y, cx - hw, cx + hw, cf);
    }
  }
  disc(cx, cy, r, cf, ok) {
    for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++) {
      const t = (y + 0.5 - cy) / r;
      if (Math.abs(t) >= 1) continue;
      const hw = r * Math.sqrt(1 - t * t);
      this.spanRow(y, cx - hw, cx + hw, (x, yy) => {
        const c = cf(t, (x - cx) / r, x, yy);
        if (ok && !ok(x, yy)) return null;
        return c;
      });
    }
  }
  arc(cx, cy, r, a0, w, cf, a1) {
    const a = a0 === undefined ? 0 : a0, b = a1 === undefined ? TAU : a1;
    const n = Math.max(8, Math.ceil(Math.abs(b - a) * r * 3));
    const pts = [];
    for (let i = 0; i <= n; i++) { const t = a + (b - a) * i / n; pts.push([cx + Math.cos(t) * r, cy + Math.sin(t) * r]); }
    this.strokePath(pts, () => w, cf);
  }
  /** 先取位置再判掩码, 掩码不过就不落笔(避免在轮廓外留下杂点) */
  paint(x, y, ok, c) {
    x = rnd(x); y = rnd(y);
    if (x < 0 || y < 0 || x >= this.w || y >= this.h) return;
    if (ok && !ok(x, y)) return;
    this.put(x, y, c);
  }
  /** 定宽折线笔画; w 为半宽; ok(x,y) 为可选的掩码 */
  strokePath(pts, wf, cf, ok) {
    for (let i = 0; i + 1 < pts.length; i++) {
      const p = pts[i], q = pts[i + 1];
      const dx = q[0] - p[0], dy = q[1] - p[1], L = Math.hypot(dx, dy);
      if (L < 1e-6) continue;
      const nx = -dy / L, ny = dx / L, wid = i / Math.max(pts.length - 1, 1), w = wf(wid);
      const steps = Math.max(1, Math.ceil(L * 3));
      for (let s = 0; s <= steps; s++) {
        const t = s / steps, x = p[0] + dx * t, y = p[1] + dy * t;
        const sub = Math.max(2, Math.ceil(w * 2) + 1);
        for (let j = 0; j <= sub; j++) {
          const o = -w + 2 * w * j / sub;
          this.paint(x + nx * o, y + ny * o, ok, cf(wid, o / Math.max(w, 1e-6), x, y));
        }
      }
    }
  }
  /** 圆头带状体(端部补圆盘) */
  ribbon(pts, wf, cf, ok) {
    this.strokePath(pts, wf, cf, ok);
    for (const e of [[pts[0], wf(0)], [pts[pts.length - 1], wf(1)]]) {
      const c = e[0], w = e[1];
      this.disc(c[0], c[1], w, (t, o, x, y) => cf(0, o, x, y), ok);
    }
  }
  /** 左侧受光: 返回漫反射系数 0..1 */
  light(nx, ny) { return clamp(0.5 + 0.52 * (nx * 0.7071 - ny * 0.7071), 0, 1); }

  /** 外描边 */
  outline(dark) {
    const d = this.mc.d, W = this.w, H = this.h;
    const at = (x, y) => x < 0 || y < 0 || x >= W || y >= H ? 0 : d[(y * W + x) * 4 + 3];
    const list = new Map();
    for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
      if (!at(x, y)) continue;
      let ox = 0, oy = 0, hit = false;
      for (let dy = -1; dy <= 1; dy++) for (let dx = -1; dx <= 1; dx++) {
        if (!dx && !dy) continue;
        if (!at(x + dx, y + dy)) { ox += dx; oy += dy; hit = true; }
      }
      if (!hit) continue;
      const L = Math.hypot(ox, oy) || 1; ox /= L; oy /= L;
      let sx = 0, sy = 0;
      for (let t = 1; t <= 5; t++) { const px = rnd(x + ox * t), py = rnd(y + oy * t); if (at(px, py)) { sx = px; sy = py; break; } }
      if (!sx && !sy) { sx = x; sy = y; }
      const src = this.reg.get(sy * W + sx) || [80, 80, 80];
      for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) {
        if (!dx && !dy) continue;
        const px = x + dx, py = y + dy;
        if (px < 0 || py < 0 || px >= W || py >= H) continue;
        if (at(px, py)) continue;
        const k = py * W + px;
        if (!list.has(k)) {
          const lum = 0.299 * src[0] + 0.587 * src[1] + 0.114 * src[2];
          const grounded = lum > 140
            ? [src[0] * 0.30 + 20, src[1] * 0.30 + 18, src[2] * 0.30 + 22]
            : [src[0] * 0.52 + 6, src[1] * 0.52 + 6, src[2] * 0.52 + 8];
          list.set(k, mix3(grounded, dark, 0.7));
        }
      }
    }
    for (const [k, c] of list) {
      const x = k % W, y = (k - x) / W;
      this.cv.set(x, y, c[0], c[1], c[2], 1);
      this.mc.set(x, y, 255, 255, 255, 1);
    }
  }
  /** 吸附到有限调色板, 更像手工像素画 */
  quantize(pal) {
    const d = this.cv.d;
    for (let y = 0; y < this.h; y++) for (let x = 0; x < this.w; x++) {
      const i = (y * this.w + x) * 4;
      if (!d[i + 3]) continue;
      let best = null, bd = 1e9;
      for (const p of pal) {
        const dd = (p[0] - d[i]) ** 2 + (p[1] - d[i + 1]) ** 2 + (p[2] - d[i + 2]) ** 2;
        if (dd < bd) { bd = dd; best = p; }
      }
      d[i] = best[0]; d[i + 1] = best[1]; d[i + 2] = best[2];
    }
  }
}

/* ==================== 材质/明暗 ==================== */
function shadeRibbon(s, pts, wf, ramp, opt) {
  opt = opt || {};
  const metal = opt.metal || 0, grain = opt.grain || 0;
  for (let i = 0; i + 1 < pts.length; i++) {
    const p = pts[i], q = pts[i + 1];
    const dx = q[0] - p[0], dy = q[1] - p[1], L = Math.hypot(dx, dy);
    if (L < 1e-6) continue;
    const tx = dx / L, ty = dy / L, nx = -ty, ny = tx;
    const w = wf(i / Math.max(pts.length - 1, 1));
    const steps = Math.max(1, Math.ceil(L * 3));
    for (let st = 0; st <= steps; st++) {
      const t = st / steps, cx = p[0] + dx * t, cy = p[1] + dy * t;
      const sub = Math.max(2, Math.ceil(w * 2) + 1);
      for (let j = 0; j <= sub; j++) {
        const o = (-w + 2 * w * j / sub) / w;
        const px = cx + nx * o * w, py = cy + ny * o * w;
        const l = s.light(nx * Math.sign(o || 1), ny * Math.sign(o || 1));
        let c = mix3(ramp[1], ramp[0], 0.55 + 0.45 * l);
        if (grain) c = mul3(c, 1 - grain * 0.45 * (1 - Math.abs(o)) * hash(rnd(px) * 3.1, rnd(py) * 7.7));
        if (metal) c = mix3(c, [255, 255, 255], metal * Math.pow(Math.max(0, 1 - Math.abs(o - 0.5) * 2.6), 3) * 0.9);
        s.put(px, py, c);
      }
    }
  }
  for (const e of [[pts[0], wf(0)], [pts[pts.length - 1], wf(1)]]) {
    const c = e[0], w = e[1];
    s.disc(c[0], c[1], w, (t, o, x, y) => {
      const l = s.light(Math.sign(o || 1), 0);
      return mix3(ramp[1], ramp[0], 0.55 + 0.45 * l);
    });
  }
}

function shadeSphere(s, cx, cy, r, ramp) {
  for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++) {
    const ty = (y + 0.5 - cy) / r;
    if (Math.abs(ty) >= 1) continue;
    const hw = r * Math.sqrt(1 - ty * ty);
    s.spanRow(y, cx - hw, cx + hw, (x) => {
      const tx = (x + 0.5 - cx) / r;
      const nx = tx, ny = ty, nz = Math.sqrt(Math.max(0, 1 - tx * tx - ty * ty));
      const nl = 0.62 * nx - 0.62 * ny + 0.48 * nz;
      const l = clamp(nl, 0, 1);
      let c = mix3(ramp[2], ramp[0], Math.pow(l, 0.85));
      c = mix3(c, ramp[1], clamp((l - 0.5) * 1.5, 0, 1) * 0.55);
      c = mix3(c, [255, 255, 255], Math.pow(clamp((nl - 0.85) / 0.5, 0, 1), 1.6) * 0.9);
      return c;
    });
  }
}

function shadeEllipse(s, cx, cy, rx, ry, ramp) {
  for (let y = Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
    const ty = (y + 0.5 - cy) / ry;
    if (Math.abs(ty) >= 1) continue;
    const hw = rx * Math.sqrt(1 - ty * ty);
    s.spanRow(y, cx - hw, cx + hw, (x) => {
      const tx = (x + 0.5 - cx) / rx;
      const l = clamp(0.72 - 0.42 * tx - 0.34 * ty, 0, 1);
      return mix3(ramp[1], ramp[0], l);
    });
  }
}

/** 水平圆柱(桶身/瓶身): 逐条竖带明暗; 用 mask 限制在轮廓内 */
function shadeHBody(s, x0, x1, y0f, y1f, ramp, mask, opt) {
  opt = opt || {};
  const W = s.w;
  let minX = W, maxX = -1, minY = s.h, maxY = -1;
  for (const k of mask) {
    const x = k % W, y = (k - x) / W;
    if (x < minX) minX = x; if (x > maxX) maxX = x;
    if (y < minY) minY = y; if (y > maxY) maxY = y;
  }
  for (let x = Math.max(minX, Math.floor(x0)); x <= Math.min(maxX, Math.ceil(x1)); x++) {
    const u = clamp((x + 0.5 - x0) / (x1 - x0), 0, 1);
    let l = s.light(-Math.cos(u * Math.PI), 0);
    if (opt.stave) l = l * 0.72 + 0.28 * (0.5 + 0.5 * Math.sin(u * Math.PI * 8 + 1.2));
    let col = mix3(ramp[1], ramp[0], 0.5 + 0.5 * l);
    if (opt.whiteout) col = mix3(col, [255, 255, 255], opt.whiteout * Math.pow(Math.max(0, 1 - Math.abs(u - 0.28) * 4.4), 2) * 0.9);
    const y0 = Math.max(minY, rnd(y0f(u))), y1 = Math.min(maxY, rnd(y1f(u)));
    for (let y = y0; y <= y1; y++) {
      if (!mask.has(y * W + x)) continue;
      let c = col;
      if (opt.cell) c = mul3(c, 1 + opt.cell * (hash(x, y) - 0.5));
      s.put(x, y, c, true);
    }
  }
}

/** 只覆盖已画过像素 */
function repaint(s, cf) {
  for (const k of [...s.reg.keys()]) {
    const x = k % s.w, y = (k - x) / s.w;
    s.put(x, y, cf(x, y, s.reg.get(k)), true);
  }
}

/* ==================== 五把武器 ==================== */
const WEAPONS = [];

/* ---------- 1. 糖霜权杖: 直杆 + 45 度红白条纹 + 顶端糖霜球(尖顶/高光/阴影/红丝) ---------- */
WEAPONS.push({
  id: 'sugar_scepter', cn: '糖霜权杖', en: 'Icing Scepter', boss: '姜饼王/糖霜女皇(甜点)',
  outline: [36, 28, 20],
  pal: [
    [255, 253, 251], [248, 244, 242], [236, 228, 228], [222, 212, 214], [206, 194, 198],
    [226, 234, 244], [198, 210, 226], [170, 184, 206], [224, 84, 92], [214, 62, 72],
    [186, 40, 52], [150, 26, 40], [118, 16, 30], [236, 202, 130], [198, 152, 66], [150, 106, 40],
    [36, 28, 20], [255, 255, 255]
  ],
  draw(s) {
    // 直杆骨架(自下而上), 顶端向左弯成拐杖钩
    // 竖直杆(自下而上): 直杆 + 顶端向左的小卷曲
    const spine = [[17.6, 28.0], [17.6, 25], [17.5, 21], [17.5, 17], [17.4, 13.6],
      [17.2, 11.4], [16.6, 10.2], [15.6, 10.0], [14.8, 10.8]];
    const wf = t => 4.3 - 0.7 * Math.pow(t, 2.5);
    let arcLen = 0; const cum = [0];
    for (let i = 0; i + 1 < spine.length; i++) { arcLen += Math.hypot(spine[i + 1][0] - spine[i][0], spine[i + 1][1] - spine[i][1]); cum.push(arcLen); }
    const at = (u) => {
      const target = u * arcLen;
      let i = 0; while (i < cum.length - 2 && cum[i + 1] < target) i++;
      const t = (target - cum[i]) / Math.max(cum[i + 1] - cum[i], 1e-6);
      const p = spine[i], q = spine[i + 1];
      return [p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t, Math.atan2(q[1] - p[1], q[0] - p[0]), target];
    };
    // 杆身: 左受光面 -> 右侧阴影
    for (let u = 0; u <= 1; u += 0.0028) {
      const [x, y, ang] = at(u);
      const nx = -Math.sin(ang), ny = Math.cos(ang), w = wf(u);
      const sub = Math.max(3, Math.ceil(w * 2) + 1);
      for (let j = 0; j <= sub; j++) {
        const o = (-w + 2 * w * j / sub) / w;
        const l = s.light(nx * Math.sign(o || 1), ny * Math.sign(o || 1));
        s.put(x + nx * o * w, y + ny * o * w, mix3([198, 182, 188], [252, 248, 246], 0.5 + 0.5 * l));
      }
    }
    // 粗段红白条纹(理发店旋转柱式): 段宽 ~2.2px, 缩到 16x16 后仍有 1px 红白交替
    for (let u = 0; u <= 1; u += 0.0016) {
      const [x, y, ang] = at(u);
      const nx = -Math.sin(ang), ny = Math.cos(ang), w = wf(u);
      const sub = Math.max(5, Math.ceil(w * 3.2) + 1);
      for (let j = 0; j <= sub; j++) {
        const o = (-w + 2 * w * j / sub) / w;
        const px = x + nx * o * w, py = y + ny * o * w;
        const band = Math.floor((rnd(px) + rnd(py)) / 4.4);
        const red = ((band % 2) + 2) % 2 === 0;
        const l = s.light(nx * Math.sign(o || 1), ny * Math.sign(o || 1));
        // 每段只做很轻的明暗, 保证缩图后红白分明
        const base = red ? [200, 46, 58] : [226, 216, 216];
        const lit = red ? [226, 84, 92] : [255, 255, 255];
        s.put(px, py, mix3(base, lit, 0.2 + 0.55 * l), true);
      }
    }
    // 杆上竖向高光 + 底部杆尾
    for (let y = 13; y <= 27; y++) s.put(16.0, y, [255, 255, 255], true);
    s.ribbon([[15.0, 26.8], [20.2, 26.8]], () => 2.4, (t, o, x, y) =>
      mix3([150, 106, 40], [236, 202, 130], 0.45 + 0.5 * s.light(0, -1)));
    // 顶端糖霜球
    const BX = 17.6, BY = 7.5, BR = 6.0;
    shadeSphere(s, BX, BY, BR, [[255, 255, 255], [226, 234, 244], [186, 198, 216]]);
    // 顶部小尖(糖霜尖)
    s.fillPoly([[16.8, 2.8], [18.1, 6.2], [15.4, 6.4]],
      (x, y) => mix3([196, 208, 224], [255, 255, 255], 0.5 + 0.4 * s.light(-0.6, -0.8)));
    // 缠绕的红色糖霜细线
    for (let k = 0; k < 3; k++) {
      const a0 = 0.35 + k * 1.05;
      s.arc(BX, BY, BR - 0.55, a0, 0.42, (t, o, x, y) => {
        const ang = Math.atan2(y - BY, x - BX);
        return mix3([150, 26, 40], [238, 112, 118], 0.45 + 0.45 * Math.cos(ang - 0.9));
      }, a0 + 1.85);
    }
    // 高光 + 底部阴影 + 与杆相接的金色托盘
    s.disc(BX - 1.6, BY - 1.9, 1.25, () => [255, 255, 255]);
    s.disc(BX + 1.5, BY + 2.4, 0.7, () => [246, 246, 250]);
    s.ribbon([[BX - 3.6, BY + 4.4], [BX + 3.4, BY + 4.4]], () => 1.1, (t, o, x, y) =>
      mix3([150, 106, 40], [240, 210, 140], 0.42 + 0.5 * s.light(0, -1)));
  }
});

/* ---------- 2. 屠夫剁骨刀: 宽矩形刀身(厚刀背/弧刃/吊孔) + 短木柄 2 铆钉 ---------- */
WEAPONS.push({
  id: 'butcher_cleaver', cn: '屠夫剁骨刀', en: "Butcher's Cleaver", boss: '屠夫(肉)',
  outline: [36, 28, 20],
  pal: [
    [232, 236, 239], [208, 214, 220], [168, 176, 184], [140, 148, 158], [106, 112, 122], [90, 96, 104],
    [169, 116, 63], [122, 79, 40], [74, 47, 23], [246, 238, 216], [186, 160, 116], [136, 106, 68],
    [186, 44, 48], [140, 26, 34], [100, 16, 24], [36, 28, 20], [200, 44, 56]
  ],
  draw(s) {
    // 宽矩形刀身(约 21 宽 x 15 高), 左下为刃口, 右侧钝刀尖
    const blade = [[4.6, 4.4], [9, 3.4], [14, 3.2], [19, 3.4], [23.6, 4.2], [25.8, 6.0], [26.2, 8.8],
      [25.8, 11.8], [24.0, 14.6], [20.0, 16.4], [15.0, 16.8], [10.0, 16.4], [6.0, 15.2], [4.6, 12.2], [4.6, 4.4]];
    s.fillPoly(blade, (x, y) => {
      const u = clamp((x - 4.0) / 22.4, 0, 1);       // 0=刀根 1=刀尖
      const v = clamp((y - 2.8) / 14.0, 0, 1);       // 0=刀背 1=刃口
      // 钢刃三级: 亮(左/上受光) -> 中 -> 暗(右下)
      let c;
      if (v < 0.30) c = mix3([168, 176, 184], [232, 236, 239], 0.72 - 0.25 * u);
      else if (v < 0.74) c = mix3([140, 148, 158], [208, 214, 220], 0.75 - 0.55 * u);
      else c = mix3([106, 112, 122], [168, 176, 184], 0.55 - 0.35 * u);
      // 刀背厚边(亮线)
      if (v < 0.10) c = mix3(c, [246, 250, 253], 0.78);
      // 中段冷高光斜带
      const hl = Math.max(0, 1 - Math.abs((x * 0.5 - y) + 0.6) / 1.4);
      c = mix3(c, [255, 255, 255], hl * 0.45);
      if (hash(x * 2.3, y * 5.1) > 0.9) c = mul3(c, 0.95);
      return c;
    });
    // 下缘刃口: 亮银白 1px + 其下深灰刀身厚度
    s.ribbon([[5.6, 15.1], [10.2, 16.3], [15.2, 16.7], [20.2, 16.3], [24.2, 14.5], [26.0, 11.6]], () => 1.15,
      (t, o, x, y) => mix3([120, 126, 136], [162, 170, 180], 0.45 + 0.4 * s.light(0, 1)));
    s.ribbon([[5.8, 15.9], [10.3, 17.1], [15.2, 17.5], [20.2, 17.1], [24.4, 15.2], [26.3, 12.2]], () => 0.55,
      (t, o, x, y) => mix3([52, 58, 68], [104, 110, 120], 0.5 + 0.4 * s.light(0, -1)));
    s.ribbon([[5.4, 14.4], [10.1, 15.6], [15.2, 16.0], [20.1, 15.6], [23.8, 13.8], [25.6, 11.0]], () => 0.42,
      () => [246, 251, 254]);
    // 吊孔(屠刀标志)
    s.disc(8.4, 8.4, 1.95, (t, o, x, y) => mix3([90, 96, 104], [168, 176, 184], 0.5 + 0.4 * s.light(-1, -1)));
    s.disc(8.4, 8.4, 1.1, () => [36, 28, 20]);
    s.arc(8.4, 8.4, 1.55, Math.PI * 0.15, 0.3, () => [232, 236, 239], Math.PI * 1.15);
    // 血渍(沿刃口)
    const blood = [[200, 44, 56], [152, 30, 38], [100, 16, 24]];
    s.ribbon([[24.6, 13.8], [21.8, 15.6], [18.2, 16.1]], () => 0.55,
      (t, o, x, y) => blood[hash(x * 2.1, y * 3.3) > 0.5 ? 1 : 0]);
    s.disc(23.4, 12.4, 0.75, () => [152, 30, 38]);
    s.disc(25.2, 9.4, 0.6, () => [100, 16, 24]);
    // 短木柄(自刀身右下伸出, 2 颗铆钉)
    const handle = [[17.4, 15.4], [22.8, 15.0], [24.6, 18.4], [23.6, 22.4], [21.8, 26.0], [19.2, 29.4],
      [15.4, 29.4], [16.6, 25.4], [17.6, 21.4], [17.2, 17.8]];
    s.fillPoly(handle, (x, y) => {
      const l = clamp(0.62 - 0.105 * (x - 15.4) + 0.06 * (y - 15) / 14, 0, 1);
      let c = mix3([74, 47, 23], [169, 116, 63], l);
      const g = hash(Math.floor(x * 0.8) * 1.7, Math.floor(y * 0.7) * 3.3);
      if (g > 0.58) c = mul3(c, 0.88);
      if (g < 0.14) c = mul3(c, 1.08);
      return c;
    });
    // 柄首铁箍 + 铆钉
    s.ribbon([[17.2, 16.0], [23.0, 15.6]], () => 0.65, (t, o, x, y) =>
      mix3([52, 58, 68], [168, 176, 184], 0.4 + 0.5 * s.light(-1, -0.5)));
    for (const [bx, by] of [[20.0, 19.2], [19.4, 23.8]]) {
      s.disc(bx, by, 1.0, (t, o, x, y) => mix3([74, 47, 23], [230, 228, 220], 0.35 + 0.65 * s.light(-0.7, -0.7)));
      s.put(bx - 0.4, by - 0.4, [255, 255, 255]);
    }
  }
});

/* ---------- 3. 茶刃: 细长直剑身(尖端三角 + 刃口高光) + 茶芽叶护手 + 缠绳木柄 ---------- */
WEAPONS.push({
  id: 'tea_leaf_blade', cn: '茶刃', en: 'Tea Leaf Blade', boss: '沙耶(茶)',
  outline: [36, 28, 20],
  pal: [
    [159, 213, 138], [140, 196, 122], [111, 168, 75], [96, 152, 68], [78, 128, 58],
    [63, 107, 44], [52, 88, 40], [42, 72, 34], [214, 240, 196], [176, 216, 156],
    [204, 180, 132], [170, 138, 92], [130, 102, 60], [92, 68, 40], [58, 42, 26], [36, 28, 20]
  ],
  draw(s) {
    // 垂直摆放: 护手在下, 剑尖朝上(细长直, 带明显尖端三角)
    const GX = 15.0;   // 剑身中轴 x
    // 剑身: 尖端三角 + 等宽直身(左缘受光 -> 右缘背光), 三级青瓷 + 1px 刃口高光
    s.fillPoly([[9.3, 20.4], [9.3, 7.2], [11.6, 7.0], [13.0, 4.0], [15.0, 2.6], [17.0, 4.0],
      [18.4, 7.0], [20.7, 7.2], [20.7, 20.4]], (x, y) => {
      const d = x + 0.5 - GX;
      const u = clamp((y - 2.4) / 18.0, 0, 1);
      // 三个明确色阶(缩到 16x16 后仍是一条亮刃 + 一条中绿 + 一条暗背)
      let c;
      if (d < -3.2) c = [246, 254, 236];
      else if (d < -0.9) c = [160, 214, 138];
      else if (d < 1.4) c = [111, 168, 75];
      else c = [63, 107, 44];
      // 中脉(叶脉): 暗阶里一条稍亮的脉
      if (d >= 1.6 && Math.abs(d - 2.6) < 0.45) c = [96, 152, 68];
      // 细分斜侧脉
      if (u > 0.10) {
        const vph = (((y * 0.9) + (d + 4) * 0.55) % 2.1 + 2.1) % 2.1;
        if (vph < 0.36) c = mix3(c, [96, 152, 68], 0.34);
      }
      return c;
    });
    // 剑尖三角(明确的一个尖)
    s.fillPoly([[12.8, 4.6], [15.0, 2.6], [17.2, 4.6], [15.0, 6.6]], (x, y) =>
      x + 0.5 < GX + 0.4 ? [232, 248, 216] : mix3([111, 168, 75], [159, 213, 138], 0.5));
    // 金箍(护手与剑身之间) + 护手
    s.ribbon([[9.1, 20.6], [20.9, 20.6]], () => 0.65, (t, o, x, y) =>
      mix3([130, 102, 60], [214, 194, 152], 0.4 + 0.5 * s.light(0, -1)));
    s.fillPoly([[4.4, 22.6], [7.6, 18.8], [11.8, 17.6], [16.6, 16.8], [19.2, 19.8],
      [15.0, 22.6], [11.8, 25.6], [7.8, 26.6], [4.6, 24.6]], (x, y) => {
      const l = clamp(0.62 - 0.046 * (x - 4.4) + 0.14 * (1 - Math.abs(y - 21.8) / 4.6), 0, 1);
      let c = mix3([42, 72, 34], [159, 213, 138], l);
      if (Math.abs(y - 21.6 - (x - 6.4) * 0.34) < 0.55) c = mix3(c, [200, 234, 182], 0.68);
      return c;
    });
    s.ribbon([[5.0, 23.8], [9.8, 20.8], [14.6, 18.2], [18.4, 17.6]], () => 0.5,
      (t, o, x, y) => mix3([42, 72, 34], [214, 240, 196], 0.45 + 0.5 * s.light(0, -1)));
    // 柄(深木色 + 缠绳纹)
    s.fillPoly([[11.6, 24.6], [18.4, 24.6], [18.0, 27.2], [17.4, 29.4], [12.6, 29.4],
      [12.0, 27.2]], (x, y) => {
      const l = clamp(0.60 - 0.066 * (x - 11.8) + 0.05 * (1 - (y - 24) / 6), 0, 1);
      let c = mix3([58, 42, 26], [170, 138, 92], l);
      const g = hash(Math.floor(x * 0.9) * 2.1, Math.floor(y * 0.7) * 3.1);
      if (g > 0.58) c = mul3(c, 0.9);
      if (g < 0.14) c = mul3(c, 1.07);
      return c;
    });
    for (let yy = 25.4; yy <= 28.8; yy += 1.45)   // 缠绳
      s.ribbon([[17.8, yy + 0.4], [12.2, yy - 0.4]], () => 0.45,
        (t, o, x, y) => mix3([92, 68, 40], [204, 180, 132], 0.4 + 0.5 * s.light(-1, -0.4)));
    s.fillPoly([[12.0, 28.4], [18.0, 28.4], [18.0, 29.4], [12.0, 29.4]], () => [58, 42, 26]);
  }
});

/* ---------- 4. 酒瓶锤: 竖立酒瓶(细颈/肩/瓶身 + 木塞 + 深红酒液) + 缠绳柄 ---------- */
WEAPONS.push({
  id: 'wine_bottle_mace', cn: '酒瓶锤', en: 'Wine Bottle Mace', boss: '酒桶王(酒)',
  outline: [36, 28, 20],
  pal: [
    [216, 236, 226], [186, 214, 202], [150, 186, 172], [120, 158, 144], [90, 128, 116],
    [70, 106, 94], [54, 86, 76], [40, 66, 58], [196, 48, 60], [162, 30, 44], [124, 20, 34],
    [94, 14, 26], [246, 238, 216], [226, 208, 172], [196, 172, 132], [160, 132, 92],
    [206, 158, 70], [168, 118, 44], [128, 84, 32], [58, 42, 28], [96, 70, 44], [36, 28, 20]
  ],
  draw(s) {
    const BX0 = 5.0, BX1 = 19.2, BY0 = 8.4, BY1 = 21.8;   // 瓶身
    const NX0 = 16.4, NX1 = 20.8, NY0 = 4.6, NY1 = 9.4;   // 瓶颈
    // 瓶颈
    s.fillPoly([[NX0, NY0], [NX1, NY0], [NX1, NY1], [NX0, NY1]], (x, y) => {
      const l = s.light((x - (NX0 + NX1) / 2) / 2, 0);
      let c = mix3([46, 80, 68], [176, 214, 198], 0.5 + 0.5 * l);
      if (x <= NX0 + 0.6) c = mix3(c, [214, 240, 228], 0.7);   // 竖向反光
      return c;
    });
    s.ribbon([[NX0, NY0], [NX1, NY0]], () => 0.7, (t, o, x, y) =>
      mix3([128, 84, 32], [226, 208, 172], 0.4 + 0.5 * s.light(0, -1)));
    // 瓶身(逐列明暗 -> 玻璃的圆柱感)
    const bodyMask = new Set();
    s.fillPoly([[BX0, BY0], [BX0 + 3.4, BY0], [NX0 - 1.4, BY0 - 1.6], [NX0 + 1.0, BY0 - 2.6],
      [NX1, BY0 - 2.8], [BX1 - 1.0, BY0 - 1.0], [BX1, BY0 + 2.0], [BX1, BY1 - 1.4],
      [BX1 - 1.6, BY1], [BX0 + 1.4, BY1], [BX0, BY1 - 1.6]],
      (x, y) => { bodyMask.add(rnd(y) * SIZE + rnd(x)); return null; });
    const inBody = (x, y) => bodyMask.has(y * SIZE + x);
    const SURF = 12.8;   // 液面高度
    shadeHBody(s, BX0, BX1, () => BY0 - 1.4, () => BY1 - 0.2, [[196, 224, 210], [46, 78, 68]],
      bodyMask, { whiteout: 0.5, cell: 0.09 });
    // 深红酒液(只覆盖液面以下)
    repaint(s, (x, y, base) => {
      if (!inBody(x, y)) return base;
      if (y < SURF) return base;
      const u = clamp((x - BX0) / (BX1 - BX0), 0, 1);
      const l = s.light(-Math.cos(u * Math.PI), 0);
      return mix3([92, 14, 26], [168, 32, 46], 0.30 + 0.5 * l);
    });
    // 液面线(亮一条)与上方空瓶玻璃
    repaint(s, (x, y, base) => {
      if (!inBody(x, y)) return base;
      if (Math.abs(y - SURF) > 0.65) return base;
      const u = clamp((x - BX0) / (BX1 - BX0), 0, 1);
      const l = s.light(-Math.cos(u * Math.PI), 0);
      return mix3([110, 20, 32], [196, 88, 96], 0.35 + 0.5 * l);
    });
    // 液面上方的空瓶玻璃
    repaint(s, (x, y, base) => {
      if (!inBody(x, y)) return base;
      if (y > SURF) return base;
      const u = clamp((x - BX0) / (BX1 - BX0), 0, 1);
      const l = s.light(-Math.cos(u * Math.PI), 0);
      return mix3([54, 88, 78], [150, 190, 174], 0.5 + 0.5 * l);
    });
    // 木塞顶部压到 y=3 以上, 避免贴边
    // 玻璃竖向反光(左) + 右侧暗反光
    s.ribbon([[BX0 + 2.2, BY0 + 1.0], [BX0 + 2.2, BY1 - 1.6]], () => 0.45, () => [226, 246, 238]);
    s.ribbon([[BX1 - 1.6, BY0 + 2.6], [BX1 - 1.6, BY1 - 3.0]], () => 0.32, () => [128, 172, 154]);
    // 桶箍 x2(酒桶王风味)
    for (const [hy, yy] of [[BY0 + 3.4, 12.4], [BY1 - 3.2, 18.0]])
      s.ribbon([[BX0 + 0.6, hy], [BX1 - 0.8, hy]], () => 0.5, (t, o, x, y) => {
        if (!inBody(x, y)) return null;
        return mix3([128, 84, 32], [224, 180, 104], 0.42 + 0.5 * s.light(0, 0));
      }, inBody);
    // 标签(羊皮纸 + 暗红酒滴)
    s.fillPoly([[BX0 + 1.2, 15.2], [BX0 + 6.6, 15.2], [BX0 + 6.6, 19.6], [BX0 + 1.2, 19.6]], (x, y) => {
      if (!inBody(x, y)) return null;
      let c = mix3([196, 172, 132], [248, 240, 218], 0.45 + 0.55 * s.light(-0.5, -0.5));
      if (x <= BX0 + 1.4 || x >= BX0 + 6.4 || y <= 15.4 || y >= 19.4) c = mul3(c, 0.7);
      return c;
    });
    s.arc(BX0 + 3.9, 17.4, 1.35, 0.6, 1.3, (t, o, x, y) =>
      mix3([104, 16, 30], [186, 46, 58], 0.4 + 0.4 * s.light(-1, -1)), 0.6 + Math.PI);
    // 木塞(瓶口)
    s.fillPoly([[NX0 + 0.2, 3.4], [NX1 - 0.2, 3.4], [NX1 - 0.4, 6.0], [NX0 + 0.4, 6.0]], (x, y) => {
      let c = mix3([128, 84, 32], [208, 162, 74], 0.4 + 0.6 * s.light(-1, -0.5));
      if (hash(x * 1.7, y * 2.3) > 0.6) c = mul3(c, 0.92);
      return c;
    });
    s.ribbon([[NX0 + 1.0, 3.4], [NX1 - 1.0, 3.4]], () => 0.5, (t, o, x, y) =>
      mix3([92, 62, 26], [226, 190, 120], 0.4 + 0.5 * s.light(0, -1)));
    s.ribbon([[NX0 + 0.4, 5.4], [NX1 - 0.4, 5.4]], () => 0.4, () => [92, 64, 28]);
    // 缠绳柄(瓶身下方)
    s.fillPoly([[11.0, 20.8], [16.6, 20.8], [17.4, 24.0], [16.6, 26.8], [16.0, 29.4],
      [11.6, 29.4], [11.0, 26.8], [10.2, 24.0]], (x, y) => {
      const l = clamp(0.62 - 0.085 * (x - 10.2) + 0.05 * (1 - (y - 21) / 9), 0, 1);
      let c = mix3([58, 42, 28], [160, 132, 92], l);
      const g = hash(Math.floor(x * 0.8) * 2.1, Math.floor(y * 0.7) * 3.1);
      if (g > 0.6) c = mul3(c, 0.9);
      return c;
    });
    for (let yy = 21.8; yy <= 28.6; yy += 1.45)   // 缠绳
      s.ribbon([[16.6, yy + 0.5], [11.6, yy - 0.5]], () => 0.5,
        (t, o, x, y) => mix3([110, 92, 58], [216, 198, 156], 0.35 + 0.5 * s.light(-1, -0.4)));
    s.fillPoly([[11.6, 28.4], [16.0, 28.4], [16.0, 29.4], [11.6, 29.4]], () => [58, 42, 28]);
  }
});

/* ---------- 5. 奶酪轮锤: 斜视角奶酪轮(椭圆面 5 孔 + 深色外皮) + 木柄 ---------- */
WEAPONS.push({
  id: 'cheese_wheel_mace', cn: '奶酪轮锤', en: 'Cheese Wheel Mace', boss: '鼠群(奶酪)',
  outline: [36, 28, 20],
  pal: [
    [242, 208, 122], [230, 190, 100], [217, 174, 78], [203, 158, 66], [184, 138, 52],
    [168, 127, 46], [150, 110, 38], [130, 94, 32], [112, 80, 28], [94, 66, 24],
    [76, 52, 20], [58, 40, 16], [214, 194, 152], [178, 148, 106], [140, 112, 72], [104, 82, 52]
  ],
  draw(s) {
    const RX = 8.4, RY = 8.0, CX = 15.0, CY = 14.2;
    // 顶面(椭圆上半, 体现斜视角)
    const top = [];
    for (let i = 0; i <= 16; i++) {
      const t = i / 16, a = Math.PI - t * Math.PI;
      top.push([CX + RX * Math.cos(a), CY - RY * Math.sin(a)]);
    }
    top.push([CX + 0.2, CY - RY], [CX - 0.1, CY + 2.2]);
    s.fillPoly(top, (x, y) => {
      const u = clamp((x - (CX - RX)) / (RX * 1.35), 0, 1);
      const l = 0.38 + 0.58 * u - 0.10 * clamp((y - (CY - RY)) / (RY * 1.4), 0, 1);
      let c = mix3([203, 158, 66], [242, 208, 122], clamp(l, 0, 1));
      const h = hash(x, y);
      if (h > 0.84) c = mix3(c, [242, 214, 140], 0.5);
      else if (h < 0.12) c = mul3(c, 0.94);
      return c;
    });
    // 正面椭圆(三级奶酪色)
    shadeEllipse(s, CX, CY, RX, RY, [[242, 208, 122], [150, 110, 38]]);
    // 侧面深色外皮(1~2px 弧带, 右侧与下侧)
    for (let y = 0; y < SIZE; y++) for (let x = 0; x < SIZE; x++) {
      const c = s.get(x, y);
      if (!c) continue;
      const tx = (x + 0.5 - CX) / RX, ty = (y + 0.5 - CY) / RY;
      const d = Math.hypot(tx, ty);
      if (d > 0.84 && tx > -0.55) {
        const k = clamp((d - 0.84) / 0.16, 0, 1);
        s.put(x, y, mix3(c, [112, 80, 28], 0.4 + 0.5 * k), true);
      }
    }
    // 外皮与顶面交界线
    s.ribbon([[CX, CY - RY + 0.7], [CX + RX - 0.6, CY - RY * 0.7], [CX + RX + 0.4, CY - 0.4]], () => 0.5,
      (t, o, x, y) => mix3([94, 66, 24], [203, 158, 66], 0.4 + 0.5 * s.light(1, -0.6)));
    // 孔洞: 椭圆面 4 个(带内部阴影) + 顶面 2 个
    for (const [hx, hy, hrx, hry] of [[20.4, 9.4, 0.9, 0.75], [13.8, 12.0, 1.8, 1.5],
      [18.2, 16.4, 1.2, 1.05], [14.8, 18.6, 0.85, 0.72]]) {
      if (Math.hypot((hx - CX) / RX, (hy - CY) / RY) > 0.68) continue;
      s.shapeEllipse(hx, hy, hrx + 0.5, hry + 0.5, (x, y) => {
        const a = Math.atan2((y + 0.5 - hy) / hry, (x + 0.5 - hx) / hrx);
        return mix3([150, 110, 38], [242, 208, 122], 0.45 + 0.5 * s.light(-Math.cos(a), -Math.sin(a)));
      });
      s.shapeEllipse(hx, hy, Math.max(hrx - 0.25, 0.1), Math.max(hry - 0.25, 0.1), (x, y) =>
        mix3([58, 40, 16], [94, 66, 24], 0.4 + 0.5 * s.light(-1, 1)));
      s.put(hx - hrx * 0.45, hy + hry * 0.55, [203, 158, 66]);
    }
    for (const [hx, hy, hrx, hry] of [[10.6, 10.2, 1.3, 0.95], [14.6, 8.0, 0.85, 0.62]]) {
      s.shapeEllipse(hx, hy, hrx, hry, (x, y) => mix3([150, 110, 38], [217, 174, 78], 0.5 + 0.4 * s.light(-1, 1)));
      s.put(hx, hy + hry * 0.5, [130, 94, 32]);
    }
    // 裂纹
    s.ribbon([[20.8, 8.8], [20.4, 10.6], [21.2, 12.2]], () => 0.32, () => [130, 94, 32]);
    s.ribbon([[21.6, 17.6], [20.2, 19.0]], () => 0.3, () => [130, 94, 32]);
    // 木柄(自轮右下伸出)
    s.fillPoly([[18.6, 19.2], [24.0, 22.8], [23.4, 26.0], [22.8, 29.4], [18.6, 29.4],
      [19.0, 25.4], [17.0, 21.4], [16.2, 19.6]], (x, y) => {
      const l = clamp(0.60 - 0.075 * (x - 17) + 0.05 * (y - 19) / 10, 0, 1);
      let c = mix3([104, 82, 52], [214, 194, 152], l);
      const g = hash(Math.floor(x * 0.9) * 1.3, Math.floor(y * 0.8) * 2.7);
      if (g > 0.58) c = mul3(c, 0.9);
      if (g < 0.14) c = mul3(c, 1.06);
      return c;
    });
    // 柄上铁箍 + 缠绳
    s.ribbon([[18.8, 20.4], [21.8, 22.8]], () => 0.5, (t, o, x, y) =>
      mix3([76, 52, 20], [168, 127, 46], 0.4 + 0.5 * s.light(-1, -0.6)));
    for (let yy = 24.2; yy <= 28.6; yy += 1.5)
      s.ribbon([[22.6, yy + 0.5], [19.0, yy - 0.5]], () => 0.45,
        (t, o, x, y) => mix3([140, 112, 72], [214, 194, 152], 0.35 + 0.5 * s.light(-1, -0.4)));
    s.fillPoly([[18.8, 28.4], [22.8, 28.4], [22.8, 29.4], [18.8, 29.4]], () => [76, 52, 20]);
    // 奶酪碎屑
    for (const [cx2, cy2, r] of [[7.6, 19.6, 0.65], [10.0, 21.8, 0.5], [10.6, 23.6, 0.6], [6.2, 12.4, 0.5]]) {
      s.disc(cx2, cy2, r, (t, o, x, y) => mix3([150, 110, 38], [242, 214, 140], 0.45 + 0.5 * s.light(-1, -1)));
    }
  }
});

/* ==================== 生成 ==================== */
const rendered = [];
for (const w of WEAPONS) {
  const s = new Sprite(SIZE, SIZE);
  w.draw(s);
  s.outline(w.outline);
  s.quantize(w.pal);
  s.cv.save(path.join(OUT, `textures/item/${w.id}.png`));
  fs.writeFileSync(path.join(OUT, `models/item/${w.id}.json`), JSON.stringify({
    parent: 'minecraft:item/handheld',
    textures: { layer0: `ftboss:item/${w.id}` }
  }, null, 2));
  rendered.push({ w, cv: s.cv });
  console.log(`  ${w.id.padEnd(20)} ${w.cn.padEnd(6)} (${w.boss})  32x32 贴图已写出`);
}

/* ==================== 自检 ==================== */
function nearest16(cv) {
  const out = new Canvas(16, 16);
  for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const i = (y * 2 * cv.w + x * 2) * 4;
    if (cv.d[i + 3] > 0) out.set(x, y, cv.d[i], cv.d[i + 1], cv.d[i + 2], 1);
  }
  return out;
}
console.log('\n== 自检 ==');
let fail = 0;
for (const { w, cv } of rendered) {
  const file = path.join(OUT, `textures/item/${w.id}.png`);
  const img = decodePNG(file);
  const d = img.data;
  let opaque = 0, aMin = 255, aMax = 0, semi = 0, x0 = 1e9, x1 = -1e9, y0 = 1e9, y1 = -1e9;
  for (let y = 0; y < img.h; y++) for (let x = 0; x < img.w; x++) {
    const a = d[(y * img.w + x) * 4 + 3];
    aMin = Math.min(aMin, a); aMax = Math.max(aMax, a);
    if (a > 0 && a < 255) semi++;
    if (a > 0) { opaque++; x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y); }
  }
  const ratio = opaque / (img.w * img.h);
  const ok = img.w === 32 && img.h === 32 && aMin === 0 && aMax === 255 &&
    ratio >= 0.35 && ratio <= 0.85 && x0 >= 1 && y0 >= 1 && x1 <= img.w - 2 && y1 <= img.h - 2;
  if (!ok) fail++;
  console.log(`  ${ok ? 'PASS' : 'FAIL'} ${w.id.padEnd(20)} ${img.w}x${img.h} alpha=[${aMin}..${aMax}] 半透明px=${semi}` +
    ` 非透明=${opaque} (${(ratio * 100).toFixed(1)}%) 包围盒 x${x0}..${x1} y${y0}..${y1}`);
}
if (fail) console.error(`\n!! ${fail} 张自检未通过`);

/* ==================== 预览图 ==================== */
const FONT = {};
for (const [ch, pat] of Object.entries({
  '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'],
  '2': ['111', '001', '111', '100', '111'], '3': ['111', '001', '111', '001', '111'],
  '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
  '6': ['111', '100', '111', '101', '111'], '7': ['111', '001', '010', '010', '010'],
  '8': ['111', '101', '111', '101', '111'], '9': ['111', '101', '111', '001', '111']
})) FONT[ch] = pat;

function drawText(cv, txt, x, y, r, g, b) {
  let cx = x;
  for (const ch of String(txt)) {
    const gl = FONT[ch];
    if (gl) for (let j = 0; j < gl.length; j++) for (let i = 0; i < 3; i++)
      if (gl[j][i] === '1') cv.set(cx + i, y + j, r, g, b, 1);
    cx += 4;
  }
}
function blit(cv, icon, ox, oy, k) {
  for (let y = 0; y < icon.h * k; y++) for (let x = 0; x < icon.w * k; x++) {
    const g = (((x / k) | 0) + ((y / k) | 0)) % 2 ? 60 : 74;
    cv.set(ox + x, oy + y, g, g + 2, g + 6);
  }
  for (let y = 0; y < icon.h; y++) for (let x = 0; x < icon.w; x++) {
    const i = (y * icon.w + x) * 4;
    if (!icon.d[i + 3]) continue;
    cv.rect(ox + x * k, oy + y * k, k, k, icon.d[i], icon.d[i + 1], icon.d[i + 2], 1);
  }
}
{
  const K = 8, K16 = 16, PAD = 26, GAP = 30, TOP = 74;
  const big = SIZE * K;
  const cellW = big + GAP + 16 * K16 + PAD * 2;
  const rowH = big + 58;
  const cv = new Canvas(cellW, TOP + rowH * rendered.length + PAD);
  cv.rect(0, 0, cv.w, cv.h, 24, 26, 32, 1);
  cv.rect(0, 0, cv.w, 48, 15, 17, 22, 1);
  cv.rect(0, 48, cv.w, 1, 60, 64, 74, 1);
  const title = '1/5  2/5  3/5  4/5  5/5     32x32 @8x  |  16x16 @16x';
  drawText(cv, title, 16, 12, 232, 236, 244);
  drawText(cv, '1', 16, 12, 240, 214, 150);
  // 标题里的说明用 5x7 数字拼不出的文字改用色块图例
  cv.rect(16, 26, 10, 10, 60, 62, 70, 1); cv.rect(20, 30, 4, 4, 74, 76, 86, 1);
  drawText(cv, '1 2 3 4 5', 32, 28, 150, 158, 172);
  rendered.forEach(({ w, cv: icon }, i) => {
    const y0 = TOP + i * rowH;
    cv.rect(PAD, y0 + 8, cellW - PAD * 2, rowH - 22, 32, 35, 42, 1);
    cv.rect(PAD, y0 + 8, cellW - PAD * 2, 1, 52, 56, 66, 1);
    drawText(cv, String(i + 1), PAD + 12, y0 + 16, 240, 214, 150);
    blit(cv, icon, PAD + 40, y0 + 30, K);
    const small = nearest16(icon);
    const ox2 = PAD + 40 + big + GAP;
    blit(cv, small, ox2, y0 + 30 + (big - 16 * K16) / 2, K16);
    drawText(cv, '32', PAD + 40, y0 + 34 + big, 150, 158, 172);
    drawText(cv, '16', ox2, y0 + 34 + big, 150, 158, 172);
  });
  cv.save(path.join(OUT, 'weapons_row.png'));
  console.log('\n总览图(32x32 @8x): ' + path.join(OUT, 'weapons_row.png'));
  try {
    fs.mkdirSync(path.dirname(DESKTOP), { recursive: true });
    cv.save(DESKTOP);
    console.log('桌面预览图: ' + DESKTOP);
  } catch (e) { console.error('!! 桌面预览图写出失败: ' + e.message); }
}

/* ==================== 回写仓库贴图(只覆盖这 5 张 PNG, 不碰 JSON) ==================== */
try {
  const SRC = path.resolve(__dirname, '..', '..', 'src/main/resources/assets/ftboss/textures/item');
  if (fs.existsSync(SRC)) {
    for (const { w } of rendered) {
      if (w.id === 'wine_bottle_mace') continue;
      fs.copyFileSync(path.join(OUT, `textures/item/${w.id}.png`), path.join(SRC, `${w.id}.png`));
    }
    console.log('已回写 5 张 32x32 贴图 -> ' + SRC);
  } else {
    console.log('(未找到仓库贴图目录, 跳过回写: ' + SRC + ')');
  }
} catch (e) { console.error('!! 回写仓库贴图失败: ' + e.message); }
