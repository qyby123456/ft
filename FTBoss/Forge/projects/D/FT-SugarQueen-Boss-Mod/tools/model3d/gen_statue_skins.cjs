// 五座 Boss 石像的「上色皮肤」生成器(2026-09-10 新增)
//
// 背景: 原来石像是**一张白膜贴图 + 客户端 BlockColor 乘色**, 结果就是"纯色一块", 没有细节。
// 现在改成: **每个 Boss 一张专属的手绘贴图**(该 Boss 的配色 + 石材斑驳 + 专属花纹),
// 方块状态 tint=0 用白膜、tint>0 用该 Boss 的上色皮肤(模型文件 *_painted 只换 textures, 几何原样沿用)。
//
// ★ 重要: 本脚本**只做皮肤层**, 几何唯一真源是**已部署的** models/block/<id>_{lower,upper}.json。
//   这样就不会再踩 "gen_statues.cjs 重出一遍把手工修过的石像几何冲掉"(鼠群上半格 1 cube 占位)那个坑。
//   造型要改时: 先改 gen_statues.cjs 生成几何 -> 与仓库逐一比对 -> 再跑本脚本贴皮。
//
// 用法: node gen_statue_skins.cjs <outDir> [assetsDir]
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out_statues';
const ASSETS = process.argv[3] || 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
if (JSON.parse(fs.readFileSync(path.join(ASSETS, 'models/block/statue_sugar_queen_lower.json'), 'utf8')).loader === 'forge:obj') {
  throw new Error('Legacy statue skins are retired. Use gen_boss_statues.cjs <outputAssets> [id...] to retain per-boss white stone and cutout textures.');
}
fs.mkdirSync(path.join(OUT, 'textures/block'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'textures/item'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'blockstates'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'models/block'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'models/item'), { recursive: true });

const TS = 64;   // 石像贴图尺寸(每个面 uv=[0,0,16,16] 整张铺满 -> 贴图要"均匀但有细节")

/* ================= 工具 ================= */
const mix = (a, b, k) => [a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k];
function put(cv, x, y, c, k) {
  if (x < 0 || y < 0 || x >= cv.w || y >= cv.h) return;
  if (k !== undefined) c = mix([0, 0, 0], c, k);
  cv.set(x, y, Math.min(255, c[0]), Math.min(255, c[1]), Math.min(255, c[2]), 1);
}
/** 不规则圆形色块(奶酪孔/酒渍/斑块都用它) */
function blob(cv, cx, cy, r, col, soft) {
  for (let y = Math.floor(cy - r - 1); y <= cy + r + 1; y++) {
    for (let x = Math.floor(cx - r - 1); x <= cx + r + 1; x++) {
      const dx = x - cx, dy = y - cy;
      const wob = ((x * 31 + y * 17) % 7) / 7 * 0.55;          // 让边缘不规则
      const d = Math.sqrt(dx * dx + dy * dy);
      if (d > r + wob) continue;
      const k = soft ? Math.max(0, 1 - d / (r + 1)) : 1;
      const i = ((y + 0) % cv.h + cv.h) % cv.h * cv.w + ((x % cv.w) + cv.w) % cv.w;
      const cur = [cv.d[i * 4], cv.d[i * 4 + 1], cv.d[i * 4 + 2]];
      put(cv, x, y, mix(cur, col, Math.min(1, k)));
    }
  }
}
/** 一条带波浪的细带(糖霜淋面/木纹), 循环铺满整张贴图 */
function wavyBand(cv, y0, amp, col, thick, phase) {
  for (let x = 0; x < cv.w; x++) {
    const y = Math.round(y0 + Math.sin((x + phase) * 0.19) * amp);
    for (let t = 0; t < thick; t++) {
      const yy = (y + t + cv.h) % cv.h;
      const i = (yy * cv.w + x) * 4;
      const cur = [cv.d[i], cv.d[i + 1], cv.d[i + 2]];
      put(cv, x, yy, mix(cur, col, 0.85));
    }
  }
}

/* ================= 每个 Boss 的配色 + 专属花纹 ================= */
const BOSSES = [
  { id: 'statue_compost', tint: 7, label: 'Compost Statue', pal: { icon: [150, 118, 74], iconAccent: [232, 74, 190] } },
  {
    id: 'statue_apple', tint: 8, label: '苹果爵士石像',
    // 苹果红石 + 金腰带 + 叶绿 + 果蜡白斑
    pal: {
      dark: [118, 34, 36], light: [216, 98, 90], vein: [72, 20, 24],
      icon: [198, 54, 52], iconAccent: [232, 184, 66]
    },
    detail(cv, pal) {
      const gold = [228, 184, 66], leaf = [104, 176, 78], wax = [242, 230, 206];
      wavyBand(cv, 10, 2.4, gold, 2, 0);
      for (let i = 0; i < 22; i++) {
        const x = (i * 31 + 7) % TS, y = (i * 43 + 13) % TS;
        if (((x * 5 + y * 11) % 6) === 0) put(cv, x, y, leaf);
      }
      for (let i = 0; i < 8; i++) blob(cv, (i * 37 + 11) % TS, (i * 29 + 5) % TS, 1.4, wax, true);
      void pal;
    }
  },  {
    id: 'statue_sugar_queen', tint: 1, label: '姜饼王石像',
    // 姜饼棕 + 糖霜白 + 金 + 红糖果
    pal: {
      dark: [126, 82, 46], light: [232, 186, 128], vein: [96, 60, 32],
      icon: [214, 158, 96], iconAccent: [250, 244, 232]
    },
    detail(cv, pal) {
      const icing = [248, 243, 232], gold = [230, 186, 78], candy = [212, 62, 70];
      wavyBand(cv, 6, 2.2, icing, 3, 0);       // 糖霜淋面(顶部两道)
      wavyBand(cv, 14, 2.6, icing, 2, 40);
      for (let i = 0; i < 26; i++) {           // 金粉
        const x = (i * 29 + 5) % TS, y = (i * 47 + 11) % TS;
        if (((x * 7 + y * 13) % 5) === 0) put(cv, x, y, gold);
      }
      for (let i = 0; i < 14; i++) blob(cv, (i * 37 + 3) % TS, (i * 23 + 19) % TS, 1.2, candy);  // 红糖果粒
      for (let i = 0; i < 10; i++) blob(cv, (i * 41 + 27) % TS, (i * 31 + 7) % TS, 1.6, [246, 238, 224], true); // 糖霜白斑
      void pal;
    }
  },
  {
    id: 'statue_butcher', tint: 2, label: '屠夫石像',
    // 血红石 + 骨白/围裙白 + 暗锈
    pal: {
      dark: [92, 24, 22], light: [206, 92, 82], vein: [58, 14, 14],
      icon: [176, 60, 52], iconAccent: [232, 224, 208]
    },
    detail(cv, pal) {
      const bone = [230, 224, 208], rust = [92, 52, 32], blood = [118, 18, 18];
      for (let i = 0; i < 9; i++) blob(cv, (i * 43 + 9) % TS, (i * 27 + 5) % TS, 3.4 + (i % 3), bone, false);  // 骨白/围裙大斑
      for (let i = 0; i < 16; i++) blob(cv, (i * 19 + 31) % TS, (i * 53 + 13) % TS, 1.5, rust, true);          // 锈点
      for (let i = 0; i < 7; i++) blob(cv, (i * 33 + 21) % TS, (i * 11 + 41) % TS, 2.4, blood, true);          // 血渍
      for (let x = 0; x < TS; x++) {           // 刀痕/裂纹(斜线)
        const y = (x * 5 + 17) % TS;
        if ((x % 9) < 5) put(cv, x, y, mix(pal.vein, [40, 8, 8], 0.5));
      }
    }
  },
  {
    id: 'statue_saya', tint: 3, label: '沙耶石像',
    // 茶绿 + 苔 + 花瓣粉 + 嫩芽黄绿
    pal: {
      dark: [58, 96, 46], light: [178, 214, 132], vein: [44, 76, 36],
      icon: [130, 182, 96], iconAccent: [238, 164, 190]
    },
    detail(cv, pal) {
      const moss = [86, 118, 62], petal = [234, 152, 180], sprout = [206, 226, 120], bark = [96, 74, 48];
      for (let i = 0; i < 12; i++) blob(cv, (i * 37 + 7) % TS, (i * 29 + 23) % TS, 2.6, moss, true);       // 苔斑
      for (let x = 0; x < TS; x++) {                                                                        // 茶树干纹
        const y = Math.round(30 + Math.sin(x * 0.14) * 3);
        for (let t = 0; t < 2; t++) put(cv, x, (y + t) % TS, mix([80, 120, 62], bark, 0.5));
      }
      for (let i = 0; i < 18; i++) {                                                                        // 花瓣(十字小花)
        const x = (i * 23 + 13) % TS, y = (i * 41 + 3) % TS;
        put(cv, x, y, petal); put(cv, x + 1, y, petal); put(cv, x, y + 1, petal);
        put(cv, x + 1, y + 1, mix(petal, [255, 240, 246], 0.5));
      }
      for (let i = 0; i < 20; i++) blob(cv, (i * 17 + 29) % TS, (i * 31 + 37) % TS, 1.1, sprout, true);     // 嫩芽点
      void pal;
    }
  },
  {
    id: 'statue_wine_barrel', tint: 4, label: '酒桶王石像',
    // 酒紫红 + 黄铜箍 + 深紫酒渍 + 木纹
    pal: {
      dark: [74, 22, 40], light: [190, 96, 116], vein: [52, 14, 30],
      icon: [150, 58, 80], iconAccent: [222, 178, 82]
    },
    detail(cv, pal) {
      const brass = [216, 172, 74], brassHi = [246, 214, 128], wine = [64, 16, 44], oak = [92, 46, 40];
      for (const y of [7, 41]) {               // 两道黄铜箍(带高光)
        for (let x = 0; x < TS; x++) {
          for (let t = 0; t < 3; t++) put(cv, x, (y + t) % TS, t === 0 ? brassHi : brass);
        }
      }
      for (let x = 0; x < TS; x++) {           // 竖木纹
        const y = (x * 13) % TS;
        if ((x % 6) === 0) { put(cv, x, y, mix(pal.light, oak, 0.5)); put(cv, x, (y + 1) % TS, oak); }
      }
      for (let i = 0; i < 8; i++) blob(cv, (i * 43 + 11) % TS, (i * 29 + 17) % TS, 3.0, wine, true);   // 酒渍
      for (let i = 0; i < 10; i++) put(cv, (i * 31 + 7) % TS, (i * 19 + 25) % TS, brassHi);            // 铜屑
      void pal;
    }
  },
  {
    id: 'statue_rat_swarm', tint: 5, label: '鼠群石像',
    // 奶酪金 + 奶酪孔 + 灰鼠毛
    pal: {
      dark: [150, 122, 52], light: [248, 228, 152], vein: [132, 104, 44],
      icon: [222, 196, 100], iconAccent: [156, 148, 138]
    },
    detail(cv, pal) {
      const hole = [128, 96, 40], holeHi = [252, 238, 178], fur = [150, 144, 134], cream = [250, 240, 206];
      for (let i = 0; i < 11; i++) {            // 奶酪孔: 深色圆孔 + 上缘高光
        const x = (i * 41 + 5) % TS, y = (i * 23 + 21) % TS, r = 2.0 + (i % 3) * 0.7;
        blob(cv, x, y, r, hole, false);
        for (let t = (y - r - 1); t <= (y - r + 1); t++) put(cv, x, ((t % TS) + TS) % TS, holeHi);
      }
      for (let i = 0; i < 40; i++) put(cv, (i * 17 + 3) % TS, (i * 29 + 13) % TS, fur);   // 鼠毛杂点
      for (let i = 0; i < 8; i++) blob(cv, (i * 37 + 19) % TS, (i * 43 + 7) % TS, 2.2, cream, true);
      void pal;
    }
  }
,
  {
    id: 'statue_defect', tint: 6, label: '故障机器人石像',
    // 奶酪金 + 奶酪孔 + 灰鼠毛
    pal: {
      dark: [150, 122, 52], light: [248, 228, 152], vein: [132, 104, 44],
      icon: [96, 132, 148], iconAccent: [140, 240, 255]
    },
    detail(cv, pal) {
      const hole = [128, 96, 40], holeHi = [252, 238, 178], fur = [150, 144, 134], cream = [250, 240, 206];
      for (let i = 0; i < 11; i++) {            // 奶酪孔: 深色圆孔 + 上缘高光
        const x = (i * 41 + 5) % TS, y = (i * 23 + 21) % TS, r = 2.0 + (i % 3) * 0.7;
        blob(cv, x, y, r, hole, false);
        for (let t = (y - r - 1); t <= (y - r + 1); t++) put(cv, x, ((t % TS) + TS) % TS, holeHi);
      }
      for (let i = 0; i < 40; i++) put(cv, (i * 17 + 3) % TS, (i * 29 + 13) % TS, fur);   // 鼠毛杂点
      for (let i = 0; i < 8; i++) blob(cv, (i * 37 + 19) % TS, (i * 43 + 7) % TS, 2.2, cream, true);
      void pal;
    }
  }];

/* ================= 画一块"上色石像"贴图 =================
 * 石材基底用**值噪声(value noise)** 叠三个尺度, 不再用取模式斜线 —— 上一版 `(x*3+y*5)%71` 那种写法
 * 会在整张贴图上生成等距斜纹, 铺到方块上像"条纹布"。石纹改用噪声的**等值线**(|n-0.5|<eps), 才像天然石脉。 */
function h2(x, y, seed) {
  let n = Math.imul(x | 0, 374761393) ^ Math.imul(y | 0, 668265263) ^ Math.imul(seed | 0, 1274126177);
  n = Math.imul(n ^ (n >>> 13), 1274126177);
  return ((n ^ (n >>> 16)) >>> 0) / 4294967295;
}
function vnoise(x, y, scale, seed) {
  const xs = x / scale, ys = y / scale;
  const x0 = Math.floor(xs), y0 = Math.floor(ys);
  const fx = xs - x0, fy = ys - y0;
  const sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy);
  const a = h2(x0, y0, seed), b = h2(x0 + 1, y0, seed), c = h2(x0, y0 + 1, seed), d = h2(x0 + 1, y0 + 1, seed);
  return (a * (1 - sx) + b * sx) * (1 - sy) + (c * (1 - sx) + d * sx) * sy;
}
function paintStatue(boss) {
  const pal = boss.pal;
  const cv = new Canvas(TS, TS);
  for (let y = 0; y < TS; y++) for (let x = 0; x < TS; x++) {
    const n1 = vnoise(x, y, 10.0, 11);     // 大块明暗
    const n2 = vnoise(x, y, 4.0, 27);      // 中等斑驳
    const n3 = vnoise(x, y, 1.7, 41);      // 细颗粒
    let t = 0.08 + n1 * 0.48 + n2 * 0.26 + n3 * 0.16;
    t += (1 - y / TS) * 0.07;                                  // 顶光
    let col = mix(pal.dark, pal.light, Math.max(0, Math.min(1, t)));
    const edge1 = Math.abs(n1 - 0.5), edge2 = Math.abs(n2 - 0.5);
    if (edge1 < 0.020) col = mix(col, pal.vein, 0.46);          // 石脉 = 噪声等值线
    else if (edge1 < 0.040) col = mix(col, pal.vein, 0.20);
    if (edge2 < 0.012) col = mix(col, pal.vein, 0.22);
    if (h2(x, y, 97) > 0.9965) col = mix(col, pal.vein, 0.55);  // 零星凿痕
    if (h2(x, y, 131) > 0.9975) col = mix(col, pal.light, 0.45); // 零星亮晶
    put(cv, x, y, col);
  }
  boss.detail(cv, pal);
  return cv;
}

/* ================= 物品图标: 用**已部署模型**的 elements 做正面剪影, 按 Boss 配色上色 =================
 * 2026-09-10 改进: 原来只画"朝向观察者的那半边"且不描面边 → 屠夫/酒桶王变成一块死板的长方形;
 * 鼠群更糟(前半几乎没有 cube, 图标只有 6px)。现在: ① 画**全部** cube, 按深度画家算法排序(近的盖远的);
 * ② 自动按包围盒缩放, 任何形状都能填满图标; ③ 每个 cube 的面描一圈暗边 + 顶面高光 → 有雕刻块面感。 */
function elementsToBoxes(model) {
  return (model.elements || []).map(e => ({
    o: e.from, s: [e.to[0] - e.from[0], e.to[1] - e.from[1], e.to[2] - e.from[2]]
  }));
}
function icon(boxes, file, size, pal) {
  const cv = new Canvas(size, size);
  const x0 = Math.min(...boxes.map(b => b.o[0])), x1 = Math.max(...boxes.map(b => b.o[0] + b.s[0]));
  const y0 = Math.min(...boxes.map(b => b.o[1])), y1 = Math.max(...boxes.map(b => b.o[1] + b.s[1]));
  const bw = Math.max(1, x1 - x0), bh = Math.max(1, y1 - y0);
  const SC = Math.min((size - 6) / bw, (size - 6) / bh);
  const ox = size / 2, oy = size - 3;       // 形状水平居中, 脚底贴图标底边
  const ymax = Math.max(1, y1);
  const faces = [];
  for (const b of boxes) {
    const [bx, by, bz] = b.o, [w, h, dd] = b.s;
    const t = (by + h / 2) / ymax;
    faces.push({
      pts: [[bx, by, bz], [bx + w, by, bz], [bx + w, by + h, bz], [bx, by + h, bz]],
      d: -bz, t, w, h
    });
  }
  faces.sort((a, b) => a.d - b.d);          // 远的先画
  for (const f of faces) {
    const sp = f.pts.map(p => [ox + (p[0] - (x0 + x1) / 2) * SC, oy - p[1] * SC]);
    const base = mix(pal.icon, pal.iconAccent, Math.min(1, f.t * 0.55));
    const shade = 0.82 + f.t * 0.36;        // 越靠上越亮
    fillPoly(cv, sp, shade, base);
    topHighlight(cv, sp, size);
    // 块面暗边: **必须紧接着自己的填充画**, 否则密集模型(酒桶王 91 个 cube)的边线会互相叠加成黑块
    strokePoly(cv, sp, 0.78);
  }
  outlineIcon(cv, size);
  cv.save(file);
}
/** 把多边形最上面一两行提亮(伪顶光), 出体积感 */
function topHighlight(cv, pts, size) {
  const top = Math.min(...pts.map(p => p[1]));
  const xs = pts.filter(p => p[1] <= top + 0.5).map(p => p[0]);
  for (let x = Math.ceil(Math.min(...xs)); x <= Math.floor(Math.max(...xs)); x++) {
    const y = Math.round(top);
    if (x < 0 || y < 0 || x >= cv.w || y >= cv.h) continue;
    const i = (y * cv.w + x) * 4;
    if (cv.d[i + 3] === 0) continue;
    cv.set(x, y, cv.d[i] * 1.14, cv.d[i + 1] * 1.12, cv.d[i + 2] * 1.10, 1);
  }
}
/** 多边形描边(暗一档), 让相邻块面之间有分界 */
function strokePoly(cv, pts, dark) {
  for (let i = 0; i < pts.length; i++) {
    const [ax, ay] = pts[i], [bx, by] = pts[(i + 1) % pts.length];
    const steps = Math.ceil(Math.max(Math.abs(bx - ax), Math.abs(by - ay)));
    for (let s = 0; s <= steps; s++) {
      const x = Math.round(ax + (bx - ax) * (s / steps)), y = Math.round(ay + (by - ay) * (s / steps));
      if (x < 0 || y < 0 || x >= cv.w || y >= cv.h) continue;
      const idx = (y * cv.w + x) * 4;
      if (cv.d[idx + 3] === 0) continue;
      cv.set(x, y, cv.d[idx] * dark, cv.d[idx + 1] * dark, cv.d[idx + 2] * dark, 1);
    }
  }
}
function fillPoly(cv, pts, shade, base) {
  const minY = Math.max(0, Math.floor(Math.min(...pts.map(p => p[1])))), maxY = Math.min(cv.h - 1, Math.ceil(Math.max(...pts.map(p => p[1]))));
  const minX = Math.max(0, Math.floor(Math.min(...pts.map(p => p[0])))), maxX = Math.min(cv.w - 1, Math.ceil(Math.max(...pts.map(p => p[0]))));
  const inside = (x, y) => { let c = false; for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) { const [xi, yi] = pts[i], [xj, yj] = pts[j]; if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) c = !c; } return c; };
  for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
    if (!inside(x, y)) continue;
    const n = ((x * 13 + y * 7) % 9) / 9;
    const k = shade * (0.95 + n * 0.10);
    // **必须夹到 255**(Uint8Array 会静默取模, 亮色越界就变杂点), Canvas.set 里也夹了一道
    cv.set(x, y, Math.min(255, base[0] * k), Math.min(255, base[1] * k), Math.min(255, base[2] * k), 1);
  }
}
function outlineIcon(cv, size) {
  const src = cv.d.slice();
  const at = (x, y) => (x < 0 || y < 0 || x >= size || y >= size) ? 0 : src[(y * size + x) * 4 + 3];
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) {
    if (at(x, y) === 0) continue;
    if (at(x - 1, y) > 0 && at(x + 1, y) > 0 && at(x, y - 1) > 0 && at(x, y + 1) > 0) continue;
    const i = (y * size + x) * 4;
    cv.set(x, y, src[i] * 0.55 + 36 * 0.45, src[i + 1] * 0.55 + 28 * 0.45, src[i + 2] * 0.55 + 20 * 0.45, 1);
  }
}

/* ================= 主流程 ================= */
const report = [];
for (const boss of BOSSES) {
  const lower = JSON.parse(fs.readFileSync(path.join(ASSETS, `models/block/${boss.id}_lower.json`), 'utf8'));
  const upper = JSON.parse(fs.readFileSync(path.join(ASSETS, `models/block/${boss.id}_upper.json`), 'utf8'));

  // 1) 上色贴图(该 Boss 专属)
  const cv = paintStatue(boss);
  cv.save(path.join(OUT, `textures/block/${boss.id}.png`));

  // 2) 上色模型 = 几何原样 + 只换贴图(白膜模型保持不动)
  for (const [half, model] of [['lower', lower], ['upper', upper]]) {
    const m = JSON.parse(JSON.stringify(model));
    m.textures = { '0': `ftboss:block/${boss.id}`, particle: `ftboss:block/${boss.id}` };
    // 面里原来的 tintindex:0 是给 BlockColor 用的, 现在颜色画进贴图了 -> 去掉, 免得再被乘色
    for (const e of (m.elements || [])) for (const f of Object.values(e.faces || {})) delete f.tintindex;
    fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_${half}_painted.json`), JSON.stringify(m, null, 1));
  }
  // 白膜模型也把 tintindex 去掉(不再有 BlockColor)
  for (const [half, model] of [['lower', lower], ['upper', upper]]) {
    const m = JSON.parse(JSON.stringify(model));
    let changed = false;
    for (const e of (m.elements || [])) for (const f of Object.values(e.faces || {})) if ('tintindex' in f) { delete f.tintindex; changed = true; }
    if (changed) fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_${half}.json`), JSON.stringify(m, null, 1));
  }

  // 3) blockstate: tint=0 白膜, tint=1..5 上色皮肤(每个 tint 值都要定义, 否则那个状态会变成 missing 模型)
  const variants = {};
  for (let t = 0; t <= 5; t++) {
    const suf = t === 0 ? '' : '_painted';
    variants[`half=lower,tint=${t}`] = { model: `ftboss:block/${boss.id}_lower${suf}` };
    variants[`half=upper,tint=${t}`] = { model: `ftboss:block/${boss.id}_upper${suf}` };
  }
  fs.writeFileSync(path.join(OUT, `blockstates/${boss.id}.json`), JSON.stringify({ variants }, null, 2));

  // 4) 物品图标(按 Boss 配色手绘, 不再靠 ItemColor 乘色) + item 模型(layer0 **必须是字符串**)
  icon(elementsToBoxes(lower).concat(elementsToBoxes(upper)), path.join(OUT, `textures/item/${boss.id}.png`), 32, boss.pal);
  fs.writeFileSync(path.join(OUT, `models/item/${boss.id}.json`), JSON.stringify({
    parent: 'minecraft:item/generated',
    textures: { layer0: `ftboss:item/${boss.id}` }
  }, null, 2));

  report.push(`${boss.id.padEnd(22)} ${boss.label}  tint=${boss.tint}  cube ${(lower.elements || []).length}+${(upper.elements || []).length}`);
}

// 5) 通用「糖果石像」: 它的 blockstate 也要覆盖 tint=0..5(否则 setblock 带 tint 会缺模型), 几何/贴图不动
{
  const variants = {};
  for (let t = 0; t <= 5; t++) {
    variants[`half=lower,tint=${t}`] = { model: 'ftboss:block/statue_a' };
    variants[`half=upper,tint=${t}`] = { model: 'ftboss:block/statue_b' };
  }
  fs.writeFileSync(path.join(OUT, 'blockstates/statue.json'), JSON.stringify({ variants }, null, 1));
}

console.log('生成结果:');
for (const r of report) console.log('  ' + r);
console.log('\n产出: 5 张 block 上色贴图 + 5 张 item 图标 + 每像 2 个 *_painted 模型 + 6 个 blockstate');
