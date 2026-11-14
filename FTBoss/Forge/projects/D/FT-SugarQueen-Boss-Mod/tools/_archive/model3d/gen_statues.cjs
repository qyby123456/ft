// 五个 Boss 的「白膜石像」生成器
//   输出: blockstates / models(block lower+upper, item) / item 图标 / 共用白膜贴图
//   形式: 复用现有 CandyStatueBlock(1x1 宽 2 格高双半格), 每个 Boss 一个独立方块
//   白膜: 纯白偏灰的未上色树脂质感, 走一张 16x16 共用贴图
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const OUT = process.argv[2] || 'out';
const GEO = process.argv[3] || 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss/geo';
const ASSETS = process.argv[4] || 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
fs.mkdirSync(OUT, { recursive: true });

/* ================= 各 Boss 的取形来源 ================= */
const BOSSES = [
  { id: 'statue_sugar_queen', name: '姜饼王石像', src: 'gingerbread_king.geo.json' },
  { id: 'statue_butcher', name: '屠夫石像', src: '__BUTCHER__' },
  { id: 'statue_saya', name: '沙耶石像', src: '__SAYA__' },
  { id: 'statue_wine_barrel', name: '酒桶王石像', src: 'wine_barrel_boss.geo.json' },
  { id: 'statue_rat_swarm', name: '鼠群石像', src: 'big_rat.geo.json' }
];

/* 沙耶没有 geo(她是玩家模型) -> 手写一棵茶树造型 */
const SAYA = [
  { o: [-3, 0, -3], s: [6, 22, 6] },        // 树干
  { o: [-11, 22, -11], s: [22, 10, 22] },   // 下层树冠
  { o: [-7.5, 32, -7.5], s: [15, 7, 15] },  // 上层树冠
  { o: [-14, 24, -2.5], s: [9, 3.5, 5] },   // 左枝
  { o: [5, 24, -2.5], s: [9, 3.5, 5] },     // 右枝
  { o: [-9, 39, -4], s: [4, 4, 4] },        // 茶芽
  { o: [3, 40.5, 2], s: [3.5, 3.5, 3.5] },
  { o: [-2, 41.5, -8], s: [3, 3, 3] },
  { o: [-7, 0, -7], s: [4, 3, 4] },         // 根
  { o: [3, 0, -7], s: [4, 3, 4] },
  { o: [-7, 0, 3], s: [4, 3, 4] },
  { o: [3, 0, 3], s: [4, 3, 4] }
];

/* ================= 读取一个 geo 的全部 cube(绝对 Bedrock 坐标, 展开父子变换) ================= */
const I = () => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
const mul = (a, b) => { const o = new Array(16); for (let r = 0; r < 4; r++) for (let c = 0; c < 4; c++) { let s = 0; for (let k = 0; k < 4; k++) s += a[r * 4 + k] * b[k * 4 + c]; o[r * 4 + c] = s; } return o; };
const tr = (x, y, z) => [1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z, 0, 0, 0, 1];
const rX = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [1, 0, 0, 0, 0, c, -s, 0, 0, s, c, 0, 0, 0, 0, 1]; };
const rY = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, 0, s, 0, 0, 1, 0, 0, -s, 0, c, 0, 0, 0, 0, 1]; };
const rZ = d => { const r = d * Math.PI / 180, c = Math.cos(r), s = Math.sin(r); return [c, -s, 0, 0, s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]; };
const xf = (m, p) => [m[0] * p[0] + m[1] * p[1] + m[2] * p[2] + m[3], m[4] * p[0] + m[5] * p[1] + m[6] * p[2] + m[7], m[8] * p[0] + m[9] * p[1] + m[10] * p[2] + m[11]];
function toTree(bones) {
  if (bones.some(b => b.children)) return bones;
  const map = {}; for (const b of bones) map[b.name] = Object.assign({}, b, { children: [] });
  const roots = []; for (const b of bones) { if (b.parent && map[b.parent]) map[b.parent].children.push(map[b.name]); else roots.push(map[b.name]); }
  return roots;
}
function loadBoxes(file) {
  const g = JSON.parse(fs.readFileSync(file, 'utf8'));
  const out = [];
  (function walk(bones, pm) {
    for (const b of bones) {
      const piv = b.pivot || [0, 0, 0];
      let m = mul(pm, tr(piv[0], piv[1], piv[2]));
      if (b.rotation) { m = mul(m, rZ(b.rotation[2] || 0)); m = mul(m, rY(b.rotation[1] || 0)); m = mul(m, rX(b.rotation[0] || 0)); }
      m = mul(m, tr(-piv[0], -piv[1], -piv[2]));
      for (const c of (b.cubes || [])) {
        const p0 = xf(m, c.origin);
        const p1 = xf(m, [c.origin[0] + c.size[0], c.origin[1] + c.size[1], c.origin[2] + c.size[2]]);
        out.push({
          o: [Math.min(p0[0], p1[0]), Math.min(p0[1], p1[1]), Math.min(p0[2], p1[2])],
          s: [Math.abs(p1[0] - p0[0]), Math.abs(p1[1] - p0[1]), Math.abs(p1[2] - p0[2])]
        });
      }
      if (b.children) walk(b.children, m2);   // ★ 子级必须继承**完整**矩阵(2026-09-10 修: 否则父枢轴平移逐级累加, 部件错位/穿模)
    }
  })(toTree(g['minecraft:geometry'][0].bones), I());
  return out;
}

/* ================= 归一化: 等比缩放到 <=30 高, 水平居中到方块中心(8,8), 脚底 y=0 ================= */
function normalize(boxes, maxH) {
  let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
  for (const b of boxes) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], b.o[i]); mx[i] = Math.max(mx[i], b.o[i] + b.s[i]); }
  const scale = Math.min(maxH / (mx[1] - mn[1]), 1e9);
  const cx = (mn[0] + mx[0]) / 2, cz = (mn[2] + mx[2]) / 2;
  return boxes.map(b => ({
    o: [(b.o[0] - cx) * scale + 8, (b.o[1] - mn[1]) * scale, (b.o[2] - cz) * scale + 8],
    s: [b.s[0] * scale, b.s[1] * scale, b.s[2] * scale]
  }));
}

/* ================= 切成 lower / upper(以 y=16 为界, 跨界的切开) ================= */
function splitY(boxes, half) {
  const lo = half === 'lower' ? 0 : 16, hi = half === 'lower' ? 16 : 32;
  const off = half === 'lower' ? 0 : -16;
  const out = [];
  for (const b of boxes) {
    const y0 = b.o[1], y1 = b.o[1] + b.s[1];
    const a = Math.max(y0, lo), c = Math.min(y1, hi);
    if (c - a <= 0.01) continue;
    out.push({
      o: [b.o[0], a + off, b.o[2]],
      s: [b.s[0], c - a, b.s[2]]
    });
  }
  return out;
}
const r2 = v => Math.round(v * 1000) / 1000;
function elementModel(boxes, tex) {
  return {
    textures: { '0': tex, particle: tex },
    elements: boxes.map(b => {
      const from = b.o.map(r2), to = b.o.map((v, i) => r2(v + b.s[i]));
      const f = {};
      // tintindex:0 —— 客户端 BlockColor 按 tint 状态给这个面染色(白膜=tint 0 不染)
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) f[face] = { uv: [0, 0, 16, 16], texture: '#0', tintindex: 0 };
      return { from, to, faces: f };
    })
  };
}

/* ================= 白膜贴图(64x64, 石像质感) =================
 * 2026-09-10 重做: 原来是 16x16 里 13 档几乎一样的近白(231~251), 染色后整块死白、没有体积感。
 * 现在做 64x64 的"浅灰大理石/树脂": 两级噪声斑驳 + 轻微顶光渐变 + 几条斜向石纹 + 零星凿痕,
 * 亮度压在 180~250 之间 —— 留给染色相乘的空间(染出来的颜色才有明暗层次)。
 * 注意: 石像模型每个面都是 uv=[0,0,16,16](整张图铺满一面), 所以贴图本身要"均匀但有细节"。
 */
const TS = 64;
const white = new Canvas(TS, TS);
{
  for (let y = 0; y < TS; y++) for (let x = 0; x < TS; x++) {
    const a = ((x * 37 + y * 91) % 23) / 23;
    const b = ((Math.floor(x / 4) * 53 + Math.floor(y / 4) * 29) % 17) / 17;
    const c = ((Math.floor(x / 13) * 11 + Math.floor(y / 13) * 7) % 11) / 11;
    let v = 216 + (0.55 * a + 0.30 * b + 0.15 * c) * 32;   // 216..248
    v += (1 - y / TS) * 6;                                  // 顶部略亮(像被顶光照到)
    const vein = Math.abs(((x * 3 + y * 5) % 47) - 23);      // 斜向石纹
    if (vein < 1) v -= 26; else if (vein < 2) v -= 12;
    if (((x * 17 + y * 31) % 211) === 0) v -= 30;           // 零星凿痕
    v = Math.max(182, Math.min(250, v));
    white.set(x, y, v, v - 2, v - 8, 1);                    // 轻微偏暖的白
  }
}
fs.mkdirSync(path.join(OUT, 'textures/block'), { recursive: true });
fs.mkdirSync(path.join(OUT, 'textures/item'), { recursive: true });
white.save(path.join(OUT, 'textures/block/statue_white.png'));
console.log(`已写出 textures/block/statue_white.png (${TS}x${TS} 白膜·石像质感)`);

/* ================= 物品图标: 正面剪影 + 立体明暗 + 描边(2026-09-10 由 16 提升到 32) ================= */
function icon(boxes, file, size) {
  const cv = new Canvas(size, size);
  const SC = (size - 6) / 34;                 // 34 单位视野, 四周留边
  const OX = size / 2, OY = size - 3;
  const ymax = Math.max(1, ...boxes.map(b => b.o[1] + b.s[1]));
  const faces = [];
  for (const b of boxes) {
    const [x0, y0, z0] = b.o, [w, h, dd] = b.s;
    if (z0 + dd < 8) continue;                // 只画朝向观察者(-Z)的那半边
    const x1 = x0 + w, y1 = y0 + h;
    const t = (y0 + h / 2) / ymax;            // 越靠上越亮 -> 自带顶光明暗
    faces.push({
      pts: [[x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0]],
      d: -z0, shade: 0.80 + t * 0.34
    });
  }
  faces.sort((a, b) => a.d - b.d);
  for (const f of faces) {
    const sp = f.pts.map(p => [OX + (p[0] - 8) * SC, OY - p[1] * SC]);
    fillPoly(cv, sp, f.shade);
  }
  outlineIcon(cv, size);
  cv.save(file);
}
function fillPoly(cv, pts, shade) {
  const minY = Math.max(0, Math.floor(Math.min(...pts.map(p => p[1])))), maxY = Math.min(cv.h - 1, Math.ceil(Math.max(...pts.map(p => p[1]))));
  const minX = Math.max(0, Math.floor(Math.min(...pts.map(p => p[0])))), maxX = Math.min(cv.w - 1, Math.ceil(Math.max(...pts.map(p => p[0]))));
  const inside = (x, y) => { let c = false; for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) { const [xi, yi] = pts[i], [xj, yj] = pts[j]; if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) c = !c; } return c; };
  for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
    if (!inside(x, y)) continue;
    // 同一面内再加一点细微噪点, 避免大片死平。**必须夹到 255**: shade 会 >1, 不夹就会被
    // Uint8Array 取模回绕成杂色(实测亮白变青色斑点)。
    const n = ((x * 13 + y * 7) % 9) / 9;
    const k = shade * (0.96 + n * 0.08);
    cv.set(x, y, Math.min(255, 236 * k), Math.min(255, 226 * k), Math.min(255, 214 * k), 1);
  }
}
/** 外轮廓描边: 把紧挨透明像素的不透明像素压暗(深棕), 让图标在任何背景上都清楚 */
function outlineIcon(cv, size) {
  const src = cv.d.slice();
  const at = (x, y) => (x < 0 || y < 0 || x >= size || y >= size) ? 0 : src[(y * size + x) * 4 + 3];
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) {
    if (at(x, y) === 0) continue;
    if (at(x - 1, y) > 0 && at(x + 1, y) > 0 && at(x, y - 1) > 0 && at(x, y + 1) > 0) continue;
    const i = (y * size + x) * 4;
    const r = src[i], g = src[i + 1], b = src[i + 2];
    cv.set(x, y, r * 0.55 + 36 * 0.45, g * 0.55 + 28 * 0.45, b * 0.55 + 20 * 0.45, 1);
  }
}

/* ================= 主流程 ================= */
const report = [];
for (const boss of BOSSES) {
  let boxes;
  if (boss.src === '__SAYA__') boxes = SAYA.map(b => ({ o: b.o, s: b.s }));
  else if (boss.src === '__BUTCHER__') boxes = loadBoxes(path.join(__dirname, 'out', 'butcher_preview.geo.json'));
  else boxes = loadBoxes(path.join(GEO, boss.src));
  if (!boxes.length) { console.error(`!! ${boss.id} 没有取到任何 cube`); continue; }
  const norm = normalize(boxes, 30);
  const lower = splitY(norm, 'lower'), upper = splitY(norm, 'upper');
  const tex = 'ftboss:block/statue_white';
  const dirs = ['blockstates', 'models/block', 'models/item'];
  for (const d of dirs) fs.mkdirSync(path.join(OUT, d), { recursive: true });
  fs.writeFileSync(path.join(OUT, `blockstates/${boss.id}.json`), JSON.stringify({
    variants: {
      'half=lower': { model: `ftboss:block/${boss.id}_lower` },
      'half=upper': { model: `ftboss:block/${boss.id}_upper` }
    }
  }, null, 2));
  fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_lower.json`), JSON.stringify(elementModel(lower, tex), null, 1));
  fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_upper.json`), JSON.stringify(elementModel(upper, tex), null, 1));
  fs.writeFileSync(path.join(OUT, `models/item/${boss.id}.json`), JSON.stringify({
    parent: 'minecraft:item/generated',
    // 物品图标也走 tintindex:0 -> 客户端 ItemColor 按"各 Boss 配色"染色(白膜图标本身是灰度剪影)
    textures: { layer0: { texture: `ftboss:item/${boss.id}`, tintindex: 0 } }
  }, null, 2));
  icon(norm, path.join(OUT, `textures/item/${boss.id}.png`), 32);
  report.push(`${boss.id.padEnd(22)} ${boss.name}  cube ${boxes.length} -> lower ${lower.length} / upper ${upper.length}`);
}
console.log('\n生成结果:');
for (const r of report) console.log('  ' + r);
