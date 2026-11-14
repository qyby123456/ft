// Boss 石像 v3: **直接拿 Boss 自己的模型 + 自己的贴图**做成静态方块雕像(2026-09-10 新增)
//
// 前两版的问题:
//   v1 白膜 + BlockColor 乘色  -> 一整块纯色
//   v2 手绘"石材质感"贴图       -> 有花纹了, 但每个面都是"整张贴图铺满", 只是一块带花纹的石头, 不像 Boss
// 本版: 把 Bedrock geo 的 **box-UV** 按标准布局换算成 Java 方块模型的**逐面 UV**, 再等比缩放、水平居中、
//       按 y=16 切成上下半格(切面上的 UV 按比例切), 于是石像 = Boss 本人的静态缩小版(连脸上五官都在)。
//
// 输出(与已有接线完全兼容, Java 侧一行都不用改):
//   models/block/<id>_{lower,upper}.json         白膜(uv 0..16 铺满, 用 statue_white)
//   models/block/<id>_{lower,upper}_painted.json 上色 = **Boss 原贴图 + 真实 UV**
//   textures/block/<id>.png                      Boss 贴图的**副本**(方块贴图必须走 block 路径)
//   textures/item/<id>.png                       32x32 物品图标(由模型正面视图 + 真实贴图渲染)
//
// ★ 为什么贴图要复制一份: 直接让**方块模型**去引用 `ftboss:entity/xxx` 在本整合包里会变成紫黑 missing
//   (实例装了 ETF/EMF(entity_texture_features/entity_model_features) + ModernFix 动态资源 + Lightspeed,
//    这几家都会接管 `textures/entity/**`)。方块模型引用 `textures/block/**` 才是标准做法, 复制一份即可。
//
// box-UV 布局(尺寸 w×h×d, box-UV 原点 u,v):
//         [up w×d][down w×d]          行1:  x: u+d .. u+d+w , y: v .. v+d
//   [east][north w×h][west][south]    行2:  x: u    .. u+2d+2w, y: v+d .. v+d+h
//   逐面矩形: up   [u+d,      v,   u+d+w,   v+d]
//             down [u+d+w,    v,   u+d+2w,  v+d]
//             east [u,        v+d, u+d,     v+d+h]
//             north[u+d,      v+d, u+d+w,   v+d+h]
//             west [u+d+w,    v+d, u+d+2w? , ...] 实际 west 宽 d: [u+d+w, v+d, u+d+w+d, v+d+h]
//             south[u+d+w+d,  v+d, u+2d+2w, v+d+h]
// 用法: node gen_statue_real.cjs <outDir> [assetsDir] [geoDir]
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');

const OUT = process.argv[2] || 'out_statues_real';
const ASSETS = process.argv[3] || 'D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss';
const GEO = process.argv[4] || path.join(ASSETS, 'geo');
if (JSON.parse(fs.readFileSync(path.join(ASSETS, 'models/block/statue_sugar_queen_lower.json'), 'utf8')).loader === 'forge:obj') {
  throw new Error('Legacy statue generation is retired. Use gen_boss_statues.cjs <outputAssets> [id...] to preserve current geometry and UVs.');
}
for (const d of ['models/block', 'models/item', 'blockstates', 'textures/item', 'textures/block']) fs.mkdirSync(path.join(OUT, d), { recursive: true });

/* ================= 取形来源 =================
 * texW/texH 必须与贴图实际尺寸一致(逐面 UV 要按它换算成 Java 的 0..16 单位)。
 * 沙耶用**玩家模型**(她是 Alex 人形 + 自己的皮肤), 所以手写标准皮肤 box-UV 的 6 个 cube。 */
const PLAYER_BOXES = [
  // o = 原点(绝对模型坐标), s = 尺寸, uv = box-UV 原点(经典皮肤布局, 64x64)
  // 2026-09-10 修: 手臂 3 宽 + 起点 ±5 会与躯干(x ±4)之间留 1 单位**缝**(雕像上看着像手臂断开),
  // 改成经典 4 宽、起点 ±4 —— 与躯干正好相接。
  { name: 'head', o: [-4, 24, -4], s: [8, 8, 8], uv: [0, 0] },
  { name: 'body', o: [-4, 12, -2], s: [8, 12, 4], uv: [16, 16] },
  { name: 'arm_r', o: [-8, 12, -2], s: [4, 12, 4], uv: [40, 16] },
  { name: 'arm_l', o: [4, 12, -2], s: [4, 12, 4], uv: [32, 48] },
  { name: 'leg_r', o: [-4, 0, -2], s: [4, 12, 4], uv: [0, 16] },
  { name: 'leg_l', o: [0, 0, -2], s: [4, 12, 4], uv: [16, 48] }
];
const BOSSES = [
  { id: 'statue_compost', label: 'Compost Statue', geo: 'compost_superman.geo.json', tex: 'entity/compost_superman', texW: 512, texH: 512 },
  { id: 'statue_apple', label: '苹果爵士石像', geo: 'apple_boss.geo.json', tex: 'entity/apple_boss', texW: 256, texH: 256 },
  { id: 'statue_defect', label: '故障机器人石像', geo: 'defect_core.geo.json', tex: 'entity/defect_core', texW: 512, texH: 512 },   // 2026-09-12
  { id: 'statue_sugar_queen', label: '姜饼王石像', geo: 'gingerbread_king.geo.json', tex: 'entity/gingerbread_king_v2', texW: 128, texH: 128 },
  { id: 'statue_butcher', label: '屠夫石像', geo: '__BUTCHER__', tex: 'entity/butcher_v2', texW: 128, texH: 128 },
  { id: 'statue_saya', label: '沙耶石像', geo: '__PLAYER__', tex: 'entity/tea_alex', texW: 64, texH: 64 },
  { id: 'statue_wine_barrel', label: '酒桶王石像', geo: 'wine_barrel_boss.geo.json', tex: 'entity/wine_barrel_boss_v2', texW: 256, texH: 256 },
  // 大鼠的**尾巴占了整体长度的一大半**(tail1~3), 带着尾巴按 1x1 格缩放会把身子缩成一条细线,
  // 所以石像去掉尾巴 —— 身子+头+四条腿已经足够认出是鼠群首领。
  { id: 'statue_rat_swarm', label: '鼠群石像', geo: 'big_rat.geo.json', tex: 'entity/big_rat', texW: 64, texH: 64, skip: /^tail/ }
];

/* ================= 读 geo: 展开父子变换, **保留贴图 UV** =================
 * 两种 UV 写法都要支持(本项目两种都有):
 *   ① box-UV:  cube.uv = [u,v]              -> 按标准布局算出 6 个面的矩形
 *   ② 逐面 UV: cube.uv = {north:{uv,uv_size}, ...}(big_rat 就是这种) -> 直接取矩形
 * 统一产出 rects = {north:[x1,y1,x2,y2], east:..., ...}, 单位=贴图像素。 */
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
/** 展开成绝对坐标 cube 列表。
 *
 * ★★ 2026-09-10 修掉一个把石像"扭歪"的 bug: 子骨骼原来继承的是 `pm·translate(pivot)`(少了最后那次 translate 回去),
 *    于是每深一层就多吃一次祖先枢轴的平移, 越深的部件偏得越远 —— 表现就是**权杖飘在头旁边、披风/手臂错位、穿模**。
 *    正确做法: 骨骼对子级要传**完整的"绕枢轴旋转"矩阵**(translate(piv)·rot·translate(-piv)),
 *    也就是和它自己 cube 用的同一个矩阵(satur 静止姿势下 = 父矩阵, 即"cube 的 origin 是绝对模型坐标")。
 */
function loadBoxes(file, skip) {
  const g = JSON.parse(fs.readFileSync(file, 'utf8'));
  const out = [];
  (function walk(bones, pm) {
    for (const b of bones) {
      if (skip && skip.test(b.name)) continue;
      const piv = b.pivot || [0, 0, 0];
      let m = mul(pm, tr(piv[0], piv[1], piv[2]));
      if (b.rotation && (b.rotation[0] || b.rotation[1] || b.rotation[2])) {
        // Bedrock: 依次绕 Z / Y / X 旋转(与 render_tex.cjs 一致)
        m = mul(m, rZ(b.rotation[2] || 0));
        m = mul(m, rY(b.rotation[1] || 0));
        m = mul(m, rX(b.rotation[0] || 0));
      }
      m = mul(m, tr(-piv[0], -piv[1], -piv[2]));   // ← 完整矩阵: 子级与自己的 cube 都用它
      for (const c of (b.cubes || [])) {
        const inf = c.inflate || 0;
        const o = [c.origin[0] - inf, c.origin[1] - inf, c.origin[2] - inf];
        const s = [c.size[0] + inf * 2, c.size[1] + inf * 2, c.size[2] + inf * 2];
        const p0 = xf(m, o);
        const p1 = xf(m, [o[0] + s[0], o[1] + s[1], o[2] + s[2]]);
        out.push({
          o: [Math.min(p0[0], p1[0]), Math.min(p0[1], p1[1]), Math.min(p0[2], p1[2])],
          s: [Math.abs(p1[0] - p0[0]), Math.abs(p1[1] - p0[1]), Math.abs(p1[2] - p0[2])],
          rects: rectsOf(c),          // UV 仍按**原始**尺寸算(与 render_tex.cjs / GeckoLib 一致)
          bone: b.name
        });
      }
      if (b.children) walk(b.children, m);
    }
  })(toTree(g['minecraft:geometry'][0].bones), I());
  return out;
}

/* ================= 雕像微调: 把"插进身体"的部位挪出来 =================
 * 实体模型里"头/手插进躯干"是常态(屠夫的头嵌进躯干 4~6.5 单位, 手臂内侧嵌进躯干 1 单位),
 * 实体渲染按固定顺序画, 看不出来; 但**小尺寸雕像**上这些互穿会变成明显的穿模。
 * 这里按骨骼名微调(单位=源模型单位, 缩放前), 只影响石像, 不动真正的 Boss 模型。
 * 数值 = 实测重叠量 + 0.3 余量(见 tools/model3d/overlap_measure.cjs)。
 */
const TWEAK = {
  // 屠夫: 头 y 22.5..36 而躯干顶 27(主躯干)/29(颈部) -> 抬高 5 让头坐在肩上; 两臂内侧嵌进躯干 1 -> 各外移 1.2
  statue_butcher: { head: { dy: 5 }, arm_l: { dx: 1.2 }, arm_r: { dx: -1.2 } }
};
function applyTweaks(boxes, id) {
  const t = TWEAK[id];
  if (!t) return boxes;
  return boxes.map(b => {
    const key = Object.keys(t).find(n => b.bone === n);
    if (!key) return b;
    const d = t[key];
    return Object.assign({}, b, {
      o: [b.o[0] + (d.dx || 0), b.o[1] + (d.dy || 0), b.o[2] + (d.dz || 0)]
    });
  });
}

/* ================= 等比缩放 + 居中 + 落到 y=0; 同时限制 1x1 格footprint ================= */
const FOOT = 20.0;    // 水平方向最多 20 单位(=1.25 格) —— 四足模型(鼠群)太扁, 卡死在 1 格会让它变成一条细线
const HMAX = 30.0;    // 竖直最多 30 单位(方块内 2 格高留一点头)
function normalize(boxes) {
  let mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
  for (const b of boxes) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], b.o[i]); mx[i] = Math.max(mx[i], b.o[i] + b.s[i]); }
  const sc = Math.min(HMAX / (mx[1] - mn[1]), FOOT / (mx[0] - mn[0]), FOOT / (mx[2] - mn[2]));
  const cx = (mn[0] + mx[0]) / 2, cz = (mn[2] + mx[2]) / 2;
  return boxes.map(b => ({
    o: [(b.o[0] - cx) * sc + 8, (b.o[1] - mn[1]) * sc, (b.o[2] - cz) * sc + 8],
    s: [b.s[0] * sc, b.s[1] * sc, b.s[2] * sc],
    rects: b.rects
  }));
}

/* ================= box-UV -> 6 面矩形(贴图像素) ================= */
function boxRects(uv, d3) {
  const [u, v] = uv, [w, h, d] = d3;
  return {
    up: [u + d, v, u + d + w, v + d],
    down: [u + d + w, v, u + d + 2 * w, v + d],
    east: [u, v + d, u + d, v + d + h],
    north: [u + d, v + d, u + d + w, v + d + h],
    west: [u + d + w, v + d, u + d + w + d, v + d + h],
    south: [u + d + w + d, v + d, u + 2 * d + 2 * w, v + d + h]
  };
}
/** 一个 cube 的 6 面矩形: 兼容 box-UV 数组 与 逐面 UV 对象 */
function rectsOf(c) {
  if (Array.isArray(c.uv)) return boxRects(c.uv, c.size);
  const out = {};
  if (c.uv && typeof c.uv === 'object') {
    for (const k of ['north', 'east', 'south', 'west', 'up', 'down']) {
      const f = c.uv[k];
      if (!f) continue;
      const [x, y] = f.uv, [w, h] = f.uv_size || c.size;
      out[k] = [Math.min(x, x + w), Math.min(y, y + h), Math.max(x, x + w), Math.max(y, y + h)];
    }
  }
  if (!out.north) out.north = [0, 0, c.size[0], c.size[1]];
  return out;
}
const r3 = x => Math.round(x * 1000) / 1000;

/* ================= ★ 关键: Bedrock 的 box-UV 矩形不能原样抄给 Java =================
 * 两边的"贴图角 -> 立方体角"对应关系**不一样**, 原样抄会让贴图整个错乱(镜像/上下翻)。
 * 下面两张表都是从字节码里挖出来的权威表(z 轴: -Z = 最小端, +Z = 最大端):
 *
 * Java(FaceInfo + BlockFaceUV, rotation=0): 顶点 i 依次拿贴图角 TL, BL, BR, TR
 *   DOWN  v0(-X,-Y,+Z) v1(-X,-Y,-Z) v2(+X,-Y,-Z) v3(+X,-Y,+Z)
 *   UP    v0(-X,+Y,-Z) v1(-X,+Y,+Z) v2(+X,+Y,+Z) v3(+X,+Y,-Z)
 *   NORTH v0(+X,+Y,-Z) v1(+X,-Y,-Z) v2(-X,-Y,-Z) v3(-X,+Y,-Z)
 *   SOUTH v0(-X,+Y,+Z) v1(-X,-Y,+Z) v2(+X,-Y,+Z) v3(+X,+Y,+Z)
 *   WEST  v0(-X,+Y,-Z) v1(-X,-Y,-Z) v2(-X,-Y,+Z) v3(-X,+Y,+Z)
 *   EAST  v0(+X,+Y,+Z) v1(+X,-Y,+Z) v2(+X,-Y,-Z) v3(+X,+Y,-Z)
 *
 * Bedrock(GeckoLib VertexSet.quadX): 4 个顶点依次拿贴图角 TL, TR, BR, BL
 *   north (-X,+Y,-Z)(+X,+Y,-Z)(+X,-Y,-Z)(-X,-Y,-Z)   south (+X,+Y,+Z)(-X,+Y,+Z)(-X,-Y,+Z)(+X,-Y,+Z)
 *   east  (+X,+Y,-Z)(+X,+Y,+Z)(+X,-Y,+Z)(+X,-Y,-Z)   west  (-X,+Y,+Z)(-X,+Y,-Z)(-X,-Y,-Z)(-X,-Y,+Z)
 *   up    (-X,+Y,+Z)(+X,+Y,+Z)(+X,+Y,-Z)(-X,+Y,-Z)   down  (-X,-Y,-Z)(+X,-Y,-Z)(+X,-Y,+Z)(-X,-Y,+Z)
 *
 * 把两张表对齐(要让"Bedrock 放在某个角上的贴图角"落到 Java 的同一个几何角) 得到的结论非常简单:
 *   4 个侧面(north/south/east/west): 发出 [u2, v1, u1, v2]  (u 与 v 都要交换)
 *   顶面/底面(up/down):            发出 [u1, v2, u2, v1]  (只交换 v)
 * Java 允许 u1 > u2 / v1 > v2(即镜像), 所以这是合法的写法。
 */
const ORIENT = {
  north: 'swapUV', south: 'swapUV', east: 'swapUV', west: 'swapUV',
  up: 'swapV', down: 'swapV'
};
function orientRect(rect, face) {
  const [x1, y1, x2, y2] = rect;
  return ORIENT[face] === 'swapUV' ? [x2, y1, x1, y2] : [x1, y2, x2, y1];
}

/**
 * 切半格: 以 y=16 为界切开 cube。
 * 4 个竖直面(north/east/south/west)的贴图 v 范围要**按比例切**(box-UV 里 v 向下增大 = 对应 y 向下减小);
 * 顶面/底面只在各自那一半里出现, 不需要切。
 */
function splitBoxes(box) {
  const y0 = box.o[1], y1 = box.o[1] + box.s[1];
  const lo = Math.max(y0, 0), hi = Math.min(y1, 16);
  const lo2 = Math.max(y0, 16), hi2 = Math.min(y1, 32);
  const parts = [];
  if (hi - lo > 0.001) parts.push({ box, half: 'lower', yFrom: lo, yTo: hi });
  if (hi2 - lo2 > 0.001) parts.push({ box, half: 'upper', yFrom: lo2, yTo: hi2 });
  return parts;
}
function elementsFor(boxes, half, tex16) {
  const off = half === 'lower' ? 0 : -16;
  const els = [];
  for (const b of boxes) {
    for (const part of splitBoxes(b)) {
      if (part.half !== half) continue;
      const yFrom = part.yFrom, yTo = part.yTo;
      const full = b.o[1], fullTop = b.o[1] + b.s[1], H = b.s[1];
      // 该段在整条 cube 里占的 v 比例(box-UV 中 v 顶端对应 cube 顶部)
      const fracTop = (fullTop - yTo) / H;      // 从顶部往下切掉的比例
      const fracBot = (fullTop - yFrom) / H;
      const rects = b.rects;
      const scale = 16 / 1;                      // Java uv 是 0..16 单位
      const conv = (face, vSlice) => {
        // r = 源 box-UV 矩形 [x1,y1,x2,y2](贴图像素) -> 竖向切片 -> **朝向换算** -> 0..16 单位
        let r = orientRect(rects[face], face);
        let [x1, y1, x2, y2] = r;
        if (vSlice) {
          // 注意: 先算好"哪一端是贴图上方"(切片按 v 从小到大), 再做切分
          const vLo = Math.min(y1, y2), vHi = Math.max(y1, y2), dv = vHi - vLo;
          const a = vLo + dv * vSlice[0], b = vLo + dv * vSlice[1];
          if (y1 <= y2) { y1 = a; y2 = b; } else { y1 = b; y2 = a; }
        }
        return [r3(x1 * 16 / tex16.w), r3(y1 * 16 / tex16.h), r3(x2 * 16 / tex16.w), r3(y2 * 16 / tex16.h)];
      };
      const vSlice = [fracTop, fracBot];
      const faces = {
        up: { uv: conv('up'), texture: '#0' },
        down: { uv: conv('down'), texture: '#0' },
        east: { uv: conv('east', vSlice), texture: '#0' },
        west: { uv: conv('west', vSlice), texture: '#0' },
        north: { uv: conv('north', vSlice), texture: '#0' },
        south: { uv: conv('south', vSlice), texture: '#0' }
      };
      // 切出来的那一面(切面)不需要补: 上下半格严丝合缝贴着, 补了反而 z-fighting
      // ★ 让每个 cube 往外**微量膨胀** EPS 单位(0.02 = 1/800 格, 肉眼不可见):
      //   实体模型里相邻部件经常"面贴面"(实测屠夫 31 个 cube 里有 51 对面重叠量 = 0.000),
      //   实体渲染按固定顺序画 → 后画的赢, 看不出问题; 但方块(尤其 Embeddium/Sodium)下共面会 **z-fighting**,
      //   表现就是"头部/手部贴图和身体小叠加"。互相轻微穿插后共面消失, 内部面被正面挡住 → 干净。
      const EPS = 0.02;
      els.push({
        from: [r3(b.o[0] - EPS), r3(yFrom + off - EPS), r3(b.o[2] - EPS)],
        to: [r3(b.o[0] + b.s[0] + EPS), r3(yTo + off + EPS), r3(b.o[2] + b.s[2] + EPS)],
        faces
      });
    }
  }
  return els;
}
/** 白膜版: 几何一样, 每个面 uv 铺满整张(同样按 Java 的朝向写: 侧面 [16,0,0,16], 顶底 [0,16,16,0]) */
function whiteElements(els) {
  const full = { north: [16, 0, 0, 16], south: [16, 0, 0, 16], east: [16, 0, 0, 16], west: [16, 0, 0, 16], up: [0, 16, 16, 0], down: [0, 16, 16, 0] };
  return els.map(e => {
    const f = {};
    for (const k of ['north', 'east', 'south', 'west', 'up', 'down']) f[k] = { uv: full[k].slice(), texture: '#0' };
    return { from: e.from, to: e.to, faces: f };
  });
}

/* ================= 物品图标: 模型正面视图 + 真实贴图采样 ================= */
function renderIcon(els, tex, file, size) {
  const cv = new Canvas(size, size);
  const faces = [];
  for (const e of els) {
    const [x0, y0, z0] = e.from, [x1, y1, z1] = e.to;
    const t = (y0 + y1) / 2 / 32;
    faces.push({ pts: [[x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0]], d: -z0, t, uv: e.faces.north.uv });
  }
  faces.sort((a, b) => a.d - b.d);
  const xs = els.flatMap(e => [e.from[0], e.to[0]]), ys = els.flatMap(e => [e.from[1], e.to[1]]);
  const bx0 = Math.min(...xs), bx1 = Math.max(...xs), by0 = Math.min(...ys), by1 = Math.max(...ys);
  const SC = Math.min((size - 4) / Math.max(1, bx1 - bx0), (size - 4) / Math.max(1, by1 - by0));
  const ox = size / 2, oy = size - 2;
  for (const f of faces) {
    // 正面视图: 屏幕右 = -x(Java BlockFaceUV 语义, 贴图 u1 列在屏幕左侧), 这样图标不会左右镜像
    const sp = f.pts.map(p => [ox - (p[0] - (bx0 + bx1) / 2) * SC, oy - p[1] * SC]);
    const minX = Math.max(0, Math.floor(Math.min(...sp.map(p => p[0])))), maxX = Math.min(size - 1, Math.ceil(Math.max(...sp.map(p => p[0]))));
    const minY = Math.max(0, Math.floor(Math.min(...sp.map(p => p[1])))), maxY = Math.min(size - 1, Math.ceil(Math.max(...sp.map(p => p[1]))));
    const sx0 = Math.min(...sp.map(p => p[0])), sx1 = Math.max(...sp.map(p => p[0]));
    const sy0 = Math.min(...sp.map(p => p[1])), sy1 = Math.max(...sp.map(p => p[1]));
    const [u1, v1, u2, v2] = f.uv;
    for (let y = minY; y <= maxY; y++) for (let x = minX; x <= maxX; x++) {
      if (!inPoly(sp, x + 0.5, y + 0.5)) continue;
      const fx = (x + 0.5 - sx0) / Math.max(1e-6, sx1 - sx0), fy = (y + 0.5 - sy0) / Math.max(1e-6, sy1 - sy0);
      // Java uv 是 0..16 单位 -> 贴图像素
      const tu = (u1 + (u2 - u1) * fx) / 16 * tex.w, tv = (v1 + (v2 - v1) * fy) / 16 * tex.h;
      const pxi = Math.max(0, Math.min(tex.w - 1, Math.floor(tu))), pyi = Math.max(0, Math.min(tex.h - 1, Math.floor(tv)));
      const i = (pyi * tex.w + pxi) * 4;
      if (tex.data[i + 3] === 0) continue;
      const k = 0.86 + f.t * 0.26;                 // 顶光
      cv.set(x, y, Math.min(255, tex.data[i] * k), Math.min(255, tex.data[i + 1] * k), Math.min(255, tex.data[i + 2] * k), 1);
    }
    stroke(cv, sp, 0.72);
  }
  outline(cv, size);
  cv.save(file);
}
function inPoly(pts, x, y) {
  let c = false;
  for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
    const [xi, yi] = pts[i], [xj, yj] = pts[j];
    if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) c = !c;
  }
  return c;
}
function stroke(cv, pts, dark) {
  for (let i = 0; i < pts.length; i++) {
    const [ax, ay] = pts[i], [bx, by] = pts[(i + 1) % pts.length];
    const n = Math.ceil(Math.max(Math.abs(bx - ax), Math.abs(by - ay)));
    for (let s = 0; s <= n; s++) {
      const x = Math.round(ax + (bx - ax) * (s / n)), y = Math.round(ay + (by - ay) * (s / n));
      if (x < 0 || y < 0 || x >= cv.w || y >= cv.h) continue;
      const idx = (y * cv.w + x) * 4;
      if (cv.d[idx + 3] === 0) continue;
      cv.set(x, y, cv.d[idx] * dark, cv.d[idx + 1] * dark, cv.d[idx + 2] * dark, 1);
    }
  }
}
function outline(cv, size) {
  const src = cv.d.slice();
  const at = (x, y) => (x < 0 || y < 0 || x >= size || y >= size) ? 0 : src[(y * size + x) * 4 + 3];
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) {
    if (at(x, y) === 0) continue;
    if (at(x - 1, y) > 0 && at(x + 1, y) > 0 && at(x, y - 1) > 0 && at(x, y + 1) > 0) continue;
    const i = (y * size + x) * 4;
    cv.set(x, y, src[i] * 0.5 + 30 * 0.5, src[i + 1] * 0.5 + 24 * 0.5, src[i + 2] * 0.5 + 18 * 0.5, 1);
  }
}

/* ================= 主流程 ================= */
const report = [];
for (const boss of BOSSES) {
  let boxes;
  if (boss.geo === '__PLAYER__') boxes = PLAYER_BOXES.map(b => ({ o: b.o, s: b.s, rects: boxRects(b.uv, b.s) }));
  else if (boss.geo === '__BUTCHER__') { const bp = path.join(__dirname, 'out', 'butcher_preview.geo.json'); if (!fs.existsSync(bp)) { console.log('  skip butcher (preview missing)'); continue; } boxes = loadBoxes(bp, boss.skip); }
  else boxes = loadBoxes(path.join(GEO, boss.geo), boss.skip);
  if (!boxes.length) { console.error(`!! ${boss.id} 没取到 cube`); continue; }
  const norm = normalize(applyTweaks(boxes, boss.id));
  const elsL = elementsFor(norm, 'lower', { w: boss.texW, h: boss.texH });
  const elsU = elementsFor(norm, 'upper', { w: boss.texW, h: boss.texH });
  const texRef = `ftboss:block/${boss.id}`;
  // Boss 贴图复制到 textures/block/(方块模型的贴图必须走 block 路径, 见文件头说明)
  fs.copyFileSync(path.join(ASSETS, `textures/${boss.tex}.png`), path.join(OUT, `textures/block/${boss.id}.png`));
  // 上色(真实贴图 + 真实 UV)
  for (const [half, els] of [['lower', elsL], ['upper', elsU]]) {
    fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_${half}_painted.json`), JSON.stringify({
      // ambientocclusion:false —— 雕像按"实体"的平光渲染(实体本来就没有方块 AO),
      // 也避免部件互相穿插处方块 AO 算出的脏阴影
      ambientocclusion: false,
      textures: { '0': texRef, particle: texRef }, elements: els
    }, null, 1));
  }
  // 白膜(同一套几何, 面 uv 铺满)
  for (const [half, els] of [['lower', elsL], ['upper', elsU]]) {
    fs.writeFileSync(path.join(OUT, `models/block/${boss.id}_${half}.json`), JSON.stringify({
      ambientocclusion: false,
      textures: { '0': 'ftboss:block/statue_white', particle: 'ftboss:block/statue_white' },
      elements: whiteElements(els)
    }, null, 1));
  }
  // blockstate: 只按 `white` 属性选模型(tint 留成通配符, 只为兼容旧存档)
  //   white=false(默认) = Boss 真实外观    white=true = 白膜
  //   ★ 这样**早期放置的石像**(状态里只有 half/tint)也会自动显示 Boss 外观, 不会"看起来没生效"。
  const variants = {
    'half=lower,white=false': { model: `ftboss:block/${boss.id}_lower_painted` },
    'half=upper,white=false': { model: `ftboss:block/${boss.id}_upper_painted` },
    'half=lower,white=true': { model: `ftboss:block/${boss.id}_lower` },
    'half=upper,white=true': { model: `ftboss:block/${boss.id}_upper` }
  };
  fs.writeFileSync(path.join(OUT, `blockstates/${boss.id}.json`), JSON.stringify({ variants }, null, 2));
  // 物品图标: 用真实模型+贴图渲染(上半格抬高 16 单位拼起来)
  const both = elsL.concat(elsU.map(e => ({
    from: [e.from[0], e.from[1] + 16, e.from[2]], to: [e.to[0], e.to[1] + 16, e.to[2]], faces: e.faces
  })));
  const tex = decodePNG(path.join(ASSETS, `textures/${boss.tex}.png`));
  renderIcon(both, tex, path.join(OUT, `textures/item/${boss.id}.png`), 32);
  // 物品模型(layer0 必须是字符串!)
  fs.writeFileSync(path.join(OUT, `models/item/${boss.id}.json`), JSON.stringify({
    parent: 'minecraft:item/generated', textures: { layer0: `ftboss:item/${boss.id}` }
  }, null, 2));
  // 顺带导出 Java 模型的正面视图, 方便肉眼核对
  report.push(`${boss.id.padEnd(22)} ${boss.label}  cube ${boxes.length} -> lower ${elsL.length} / upper ${elsU.length}  贴图 ${boss.texW}x${boss.texH}`);
}
console.log('生成结果:');
for (const r of report) console.log('  ' + r);
