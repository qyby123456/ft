/* gen_black_cleaver_rebuild.cjs —— 黑色切割者「重建版」生成器（预览用，独立路径）
 *
 * 来源：用户提供的**两份** python 生成器
 *   (22) code (22).markdown —— 原型：分段 cube 拼 150° 主弧 / 140° 副弧、四翼骨骼、UV 图集、动画
 *   (23) code (23).markdown —— v2 精修：四层厚度（厚根/主刃面/过渡刃面/窄外刃）、
 *                              六个刃口 locator、idle 不再控制四翼、按新几何重绘三张遮罩
 * 两份都**不完整**：(22) 缺 radial_strip/cube/bone/shade 的函数体与骨架、握柄、UV 打包前半段；
 * (23) 第 525-527 行的 `sheet = Image.new(...)` 括号都没闭合就断了，而且它是**读 (22) 输出的
 * 精修脚本**（不能独立跑）。本机没有 python / Pillow，工程规矩是"一切生成器写成 Node +
 * tools/model3d/png.cjs"。所以这里把两份**合并移植**：几何算法与全部数值取 (22)+(23)，
 * 命名与四层结构取 (23)，缺失部分按现有正式生成器 gen_black_cleaver.cjs 的输出约定补齐。
 *
 * ★★ 本轮硬性约束（用户指定）★★
 *   · 只写 art/rebuild/，绝不碰 src/main/resources 下任何文件；
 *   · 不构建、不部署；不跑 tools/deploy_jar.ps1。
 *
 * 用法：node tools/model3d/gen_black_cleaver_rebuild.cjs
 *
 * ============================================================================
 * §0 附件缺失部分的补齐假设（逐条，便于复核）
 * ----------------------------------------------------------------------------
 * A1. 骨架 / 握柄 / 中央斧眼在 (22) 里不可见（在更早那条被截断的消息里）。
 *     → 按正式生成器 gen_black_cleaver.cjs 的骨骼名与父子关系重建（名称、父级**完全一致**，
 *       既有动画与 Java 侧 CleaverBloodVisualState 的骨骼引用不会失效），尺寸按本轮
 *       造型目标（"短柄、厚身"）重定。
 * A2. radial_strip(...) 的函数体两份都没给。→ 按调用点唯一合理的解释实现，见 §2。
 * A3. (22) 用 `vertical = sin(angle)` 与 ±0.42 分档，把每条弧切成 upper/lower **两片**
 *     旋转翼；(23) 的六个 locator（123.333 / 190 / 256.667 与 60 / 0 / -60）反推出来是
 *     **九段等分、每三段落在一根骨骼上**。这里采用 (23) 的三段划分
 *     （upper / static / lower 各 3 段）—— 它同时满足本工程的关节安全律。
 * A4. 两份都没给四片翼的 pivot。→ 按用户不变式三"枢轴贴着自己朝内那一侧的边缘
 *     （约留 0.1 单位）"计算，见 §3 pivotAtEdge()。
 * A5. "补缝背板"两份都没给（(22) 只在 known_review_items 里提了一句）。→ 按正式生成器
 *     wing_*_backing 的做法补，判据写成可证明的形式，见 §4 与不变式二。
 * A6. (23) 的 `sheet` 并排图写到一半断了。→ 按它的意图补一张 texture_layers.png。
 * ============================================================================
 * ★ 移植中发现并修正的缺陷（D1~D3），以及开发中被复核/自查推翻的两处假设（D4~D5）
 * ----------------------------------------------------------------------------
 * D1. **附件原样的两条弧会横穿握柄。** (22) 的 main 弧 115°→265°（圆心 y=10）最低点
 *     在 y=0.24、x≈-1.08，而柄杆/柄尾就在 x∈[-1.25,1.25]、y<3 那一带 →
 *     刃尖与握柄实体重叠。副弧 -70°→70°（圆心 (0.65,10)）同理（最低点 (1.66,4.46)
 *     正好落在柄杆上）。本生成器**保留附件给的半径 9.8 / 5.9 与跨度 150° / 140°**，
 *     只把两条弧的**圆心平移**（主弧 y: 10→12；副弧 (0.65,10)→(1.50,8.00)）——
 *     弧长与形状不变，但两条弧都退到柄的两侧。
 *     ★ 用户原话"150° 只是造型参考，不是字面圆弧"，所以保留弧长、调整摆放是允许的。
 * D2. (22) 的层间只有 0.60 径向搭接、厚度 0.48（(23) 改成外刃 0.26 宽 / 0.48 厚），
 *     四层之间基本是**面贴面**。本生成器把四个径向波段做成**真正互相重叠**
 *     （每对 ≥0.40 径向重叠），厚度 4.40→3.60→2.60→1.00 递减，每层再往 z 里缩 0.30，
 *     做出"厚根→主刃面→过渡面→窄外刃"的阶梯。
 * D3. (22) 的"±0.04 厚度交替"幅度太小，且是**奇偶两档**。见 D4。
 * D4. **（开发中自查 + 上级独立复核，结论：真缺陷）** 第一版把"厚度交替"写成
 *     `seg.index % 2 ? -j : +j`（奇偶两档），仍然只有两个厚度值 → 同层内"所有偶数段 z 面
 *     完全相同"。而 150° 新月会回折，索引 0 与 2（同奇偶、**不相邻**）的面内 AABB 会互相
 *     咬进去（切向宽度放大 1.15 倍之后咬得更深）→ 产生**精确共面且面内重叠**的一对
 *     = 真 z-fighting。把索引 3 与 5 的厚度改成相等后实测复现（"差 0.000"）。
 *     现改为**逐段**步进 zStep（主弧 0.045、副弧 0.035），9 段 9 个不同值。
 *     ⚠ 踩过的两个坑：① 跨距必须小于全局跨距 —— 第一版用 (4-index)×0.06 时跨距 0.48 > 0.45，
 *       首尾撞成同厚度，违规数反而从 29 顶到 114；② 步进必须明显大于 `toFixed(3)` 的舍入
 *       噪声 —— 用 0.04 时实测相邻段差被舍入成 0.017，又掉到 0.02 阈值以下，仍有 57 项。
 * D5. **（上级独立复核，结论：我上一版理解有误）** 不变式二原来被我写成
 *     "补缝背板外端 == 刃翼外端"。复核指出正式生成器里 blade 的背板 x∈[-9.00,-1.50]，
 *     而 blade_upper_wing 的 x∈[-8.20,-1.40]，**并不相等**。真正起作用的是
 *     "背板 z 比刃体窄、x/y 收在刃体内 → 合拢态被完全遮住"。而且我原来那句
 *     `x0=min(innerEdge, outer)` 在 `刃翼 x 极值 < innerEdge` 时会**反向探出刃体**
 *     （副刃下翼实测探出 1.397）。现已改成可证明的判据：
 *     **背板 (x,y) 包络 ⊆ 刃翼包络，且 |z| < 刃翼刃口层 |z|**（包含比相等更强）。
 * ============================================================================
 */
'use strict';

const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const OUT = path.join(ROOT, 'art', 'rebuild');        // ★ 唯一输出目录（独立预览）
fs.mkdirSync(OUT, { recursive: true });

const NAME = 'black_cleaver_rebuild';
const DENSITY = 2;                                     // 纹素密度（对齐 --target-density 2）
const PAD = 2;
const ATLAS_CANDIDATES = [256, 512];
const MAX_WING_ANGLE = 11;                             // 血怒最大展开角
const COPLANAR_MIN = 0.05;                             // 工程 §4：相邻方块重叠 0.05，不要严丝合缝
const COPLANAR_EPS = 0.02;                             // "完全共面"判定阈值（不变式四）

/* ---------------------------------------------------------------- 调色板 / 光照 */
const COLORS = {
  root: [32, 29, 36],      // 厚刃根（最深）
  iron: [42, 37, 43],      // 主刃面（黑铁）
  bevel: [76, 76, 87],     // 过渡刃面（中等）
  edge: [172, 36, 42],     // 最外刃口（红亮边 —— 用户手绘稿）
  band: [75, 65, 67],      // 包箍 / 轴盖
  leather: [65, 40, 34],   // 皮革缠带
  dark: [27, 23, 29],      // 最深结构件
  blood: [94, 22, 30],     // 刃根红缝（合拢时不可见）
  ember: [137, 29, 35],    // 中央嵌芯（自发光）
};
const LIGHT = { north: 1.06, south: 0.94, west: 0.83, east: 0.98, up: 1.12, down: 0.76 };
const shade = (rgb, v) => rgb.map(c => Math.max(0, Math.min(255, Math.round(c * v))));
const BLOOD_DARK = [0x65, 0x13, 0x21], BLOOD_MAIN = [0xC7, 0x2D, 0x3D], BLOOD_HOT = [0xFF, 0x71, 0x5E];

/* ------------------------------------------------------------------- 骨架 */
const bones = [], byName = new Map(), jobs = [];
let cubeTotal = 0;

function bone(name, parent, pivot = [0, 0, 0]) {
  if (byName.has(name)) throw new Error('重复骨骼名：' + name);
  if (parent && !byName.has(parent)) throw new Error(`父级 ${parent} 尚未建立（必须父级优先）：${name}`);
  const b = { name, pivot: [...pivot] };
  if (parent) b.parent = parent;
  bones.push(b); byName.set(name, b);
  return b;
}
const FACE_DIM = ([x, y, z]) => ({ north: [x, y], south: [x, y], west: [z, y], east: [z, y], up: [x, z], down: [x, z] });

function addBox(b, label, origin, size, material, crack = false) {
  for (const v of size) if (!(v > 0)) throw new Error(`size 必须为正：${label} ${JSON.stringify(size)}`);
  const c = { origin: [...origin], size: [...size], uv: {}, label };
  (b.cubes = b.cubes || []).push(c);
  cubeTotal++;
  for (const [dir, [w, h]] of Object.entries(FACE_DIM(size))) {
    jobs.push({ cube: c, label, bone: b.name, direction: dir, w: Math.max(2, Math.ceil(w * DENSITY)), h: Math.max(2, Math.ceil(h * DENSITY)), material, crack });
  }
  return c;
}

/* ============================================================================
 * §1 骨架（补齐 A1：名称/父级与正式版完全一致）
 * ==========================================================================*/
bone('root');
bone('grip_anchor', 'root');
const weapon = bone('weapon', 'grip_anchor');
const handle = bone('handle', 'weapon', [0, -4, 0]);
const haft = bone('haft', 'weapon', [0, 2.2, 0]);
const axeHead = bone('axe_head', 'weapon', [0, 12, 0]);
const blade = bone('blade', 'axe_head', [-1.5, 12, 0]);              // 主刃（x<0）
const bladeRight = bone('blade_right', 'axe_head', [1.5, 12, 0]);    // 副刃（x>0）
const rearHook = bone('rear_hook', 'axe_head', [1.4, 12, 0]);        // 空骨骼（保留旧引用）
const fittings = bone('fittings', 'axe_head', [0, 12, 0]);
const core = bone('ember_core', 'axe_head', [0, 12, 0]);
const pommel = bone('pommel', 'weapon', [0, -10, 0]);
bone('impact_socket', 'axe_head', [-8.0, 12, 0]);
const wingLU = bone('blade_upper_wing', 'blade', [0, 0, 0]);
const wingLL = bone('blade_lower_wing', 'blade', [0, 0, 0]);
const wingRU = bone('blade_right_upper_wing', 'blade_right', [0, 0, 0]);
const wingRL = bone('blade_right_lower_wing', 'blade_right', [0, 0, 0]);

/* ============================================================================
 * §2 radial_strip 的 Node 实现（补齐 A2）
 *
 * 一条带 = "绕 Z 轴转 angle 度的轴对齐盒子"：
 *   径向 r0..r1（自圆心 center 量）→ 盒子 x 尺寸
 *   切向 tangent_width，居中在 angle 中线上 → 盒子 y 尺寸
 *   厚度 thickness（模型 z），再整体平移 zOffset → 盒子 z 尺寸
 * Bedrock 的 cube 不能斜放，只能靠 cube.rotation 近似弧线 —— 这就是附件说的
 * "用分段立方体近似曲线，不是假装任意曲面"。
 * ==========================================================================*/
const D2R = Math.PI / 180;
function radialStrip(target, label, center, angle, r0, r1, tangentWidth, thickness, zOffset, material, crack) {
  const a = angle * D2R, ca = Math.cos(a), sa = Math.sin(a);
  const rm = (r0 + r1) / 2;
  const lx = center[0] + rm * ca, ly = center[1] + rm * sa;
  const halfW = tangentWidth / 2;
  const ox = lx - (r1 - r0) / 2 * ca + halfW * sa;
  const oy = ly - (r1 - r0) / 2 * sa - halfW * ca;
  const c = addBox(target, label, [ox, oy, -thickness / 2 + zOffset], [r1 - r0, tangentWidth, thickness], material, crack);
  c.rotation = [0, 0, +angle.toFixed(6)];
  c.pivot = c.origin.map((coordinate, axis) => coordinate + c.size[axis] / 2);
  return c;
}

/* ============================================================================
 * §3 弧刃规范
 *   数值来源：(22) 的 make_arc（半径 9.8 / 5.9、跨度 150° / 140°、九段）
 *             + (23) 的四层厚度与 locator 角度
 *   摆放来源：本生成器修正 D1（只平移圆心，不改半径与跨度）
 * ==========================================================================*/
const ARCS = [
  {
    side: 'main', boneName: 'blade', center: [-0.65, 12.00], radius: 9.8,
    start: 120, sweep: 120, parts: [3, 3, 3],          // 上翼/静止中刃/下翼 各 3 段（九段等分 13.333°）
    /* D9：跨度 150° → 120°，并把起始角 104° → 120°，让弧**对称居中在 180°**（上下对称）。
       原来跨 150° 时两片刃各绕自己圆心包了近半圈，加上中央体把中间填满 → 整体是闭合圆盘。
       收到 120° 后，弧内缘在 180° 处 x = -7.05，中央体左界 -4.90 → 左侧露出 2.15 单位
       的透镜形负空间；弧两端内缘仍落在中央体内（120° 处 (-3.85, 17.54)），父子几何照样咬住。 */
    inner: 6.40,
    layers: [                                           // [名字, r0, r1, 厚度, z 偏移]
      /* D7 加厚：原来最厚 4.40 → 侧视只有 4.4 单位，对比 16.7 的宽度几乎是一条线。
         整体加厚约 40%，仍保持 root→edge 逐层变薄（向刃口渐薄）。
         D8 外推：root 内缘 4.90 → 6.40。原来环带从 4.90 一路填到 9.80（占半径的 50%），
         加上中央体又把中间填满 → 整个头读成实心圆盘（渲染已证实）。
         外推后 180° 处弧内缘 x = -7.05，中央体左界 -4.90 → 露出 2.15 单位负空间；
         弧两端的根层仍咬住中央体（104° 处内缘点 (-2.20, 18.21) 落在中央体内）。 */
      ['root', 6.40, 8.00, 6.20, 0.00],
      ['body', 7.60, 9.00, 5.00, -0.42],
      ['bevel', 8.60, 9.45, 3.30, -0.84],
      ['edge', 9.20, 9.80, 1.10, -1.26],
    ],
    zPat: 0.06,                                         // 逐段 z 偏移步进，周期 3（见 D6）
  },
  {
    side: 'secondary', boneName: 'blade_right', center: [1.50, 8.00], radius: 5.9,
    start: 49.4, sweep: 110, parts: [3, 3, 3],          // 九段等分 12.222°（D9：140° → 110°）
    inner: 3.60,
    layers: [
      /* D8 外推：内缘 2.95 → 3.60，副刃右侧露出约 1.15 单位负空间。
         ★ 枢轴半径随之 2.85 → 3.50，49.4° 处枢轴 x = 3.778 —— 所以中央体右界也放宽到 3.95。 */
      ['root', 3.60, 4.60, 4.40, 0.00],
      ['body', 4.30, 5.42, 3.50, -0.32],
      ['bevel', 5.18, 5.69, 2.40, -0.64],
      ['edge', 5.54, 5.90, 1.00, -0.96],
    ],
    zPat: 0.06,
  },
];

const segmentsOf = arc => {
  const n = arc.parts.reduce((s, v) => s + v, 0);
  const step = arc.sweep / n;
  const out = []; let idx = 0;
  for (let pi = 0; pi < arc.parts.length; pi++) {
    for (let k = 0; k < arc.parts[pi]; k++, idx++) {
      const a0 = arc.start + idx * step, a1 = a0 + step;
      out.push({ index: idx, a0, a1, angle: (a0 + a1) / 2, step, group: ['upper', 'static', 'lower'][pi] });
    }
  }
  if (out.length !== n) throw new Error(`分段数不符：${arc.side} 期望 ${n} 实得 ${out.length}`);
  return out;
};
const pivotAtEdge = (arc, seg, atStart) => {
  /* 不变式三：枢轴贴着自己朝内那一侧的边缘，约留 0.1 单位。
     "朝内那一侧" = 该翼与静止中刃的**接缝面**（radial 界面），也就是该翼第一段/最后一段
     的外边界角；枢轴半径 = 弧内缘半径 - 0.1。 */
  const edgeAngle = (atStart ? seg.a0 : seg.a1) * D2R;
  const rPiv = arc.inner - 0.10;
  /* ★ 保留 6 位小数：早先 toFixed(3) 会把半径抖动到 4.6e-4（超出 1e-4 判定），
     于是"枢轴贴内缘 0.1"这条被自己的舍入判成失败。geo 的 pivot 允许小数。 */
  return [+(arc.center[0] + rPiv * Math.cos(edgeAngle)).toFixed(6), +(arc.center[1] + rPiv * Math.sin(edgeAngle)).toFixed(6)];
};

const arcSpecs = [];
function buildArc(arc) {
  const segs = segmentsOf(arc);
  const crackSet = new Set(arc.side === 'main' ? [3, 4] : [3]);   // (23)：主副刃裂纹分布不同，不镜像
  arcSpecs.push({ side: arc.side, center: arc.center, radius: arc.radius, sweep: arc.sweep, start: arc.start, segments: segs.length, layers: arc.layers.map(l => l[0]), part_split: arc.parts });

  for (const seg of segs) {
    for (const [lname, r0, r1, baseT, zOff] of arc.layers) {
      const thickness = +baseT.toFixed(3);
      /* D6：面间距 = 厚度差/2 + z 偏移差。只改厚度时两片 z 面**各移动一半** ——
         实测 0.035 步进只得到 0.0175 面间距，够不到 0.05，于是被迫把总跨距推到 0.8。
         改成平移 **z 偏移**（两个 z 面同时同量移动）：周期 3 模式 [-p, 0, +p]
         让相邻段面间距正好 = p，而总跨距只有 2p。 */
      const zOffSeg = +(zOff + arc.zPat * ((seg.index % 3) - 1)).toFixed(3);
      /* 切向宽度 = 相邻条带中心弦长 × 1.15（附件原式是 ×1.04 + 0.04，实测不够：
         4% 时相邻两段的 AABB 刚好在角上**接触**，会生成一对同向共面四边形）。 */
      const tangentWidth = +(2 * r1 * Math.tan((seg.step / 2) * D2R) * 1.15).toFixed(3);
      const target = seg.group === 'static' ? byName.get(arc.boneName)
        : (arc.side === 'main' ? (seg.group === 'upper' ? wingLU : wingLL)
          : (seg.group === 'upper' ? wingRU : wingRL));
      radialStrip(target, `${arc.side}_${lname}_${String(seg.index).padStart(2, '0')}`,
        arc.center, seg.angle, r0, r1, tangentWidth, thickness, zOffSeg,
        lname === 'root' ? 'root' : lname === 'edge' ? 'edge' : lname === 'bevel' ? 'bevel' : 'iron',
        lname === 'body' && crackSet.has(seg.index));
    }
  }
  /* 枢轴取该翼**最外那一段**的外边界角 */
  return {
    segs,
    pivotUpper: pivotAtEdge(arc, segs[0], true),
    pivotLower: pivotAtEdge(arc, segs[segs.length - 1], false),
  };
}
const arcData = ARCS.map(buildArc);
wingLU.pivot = [...arcData[0].pivotUpper, 0];
wingLL.pivot = [...arcData[0].pivotLower, 0];
wingRU.pivot = [...arcData[1].pivotUpper, 0];
wingRL.pivot = [...arcData[1].pivotLower, 0];

/* ============================================================================
 * §4 中央厚身 / 轴座 / 短粗握柄 / 轴盖 / 嵌芯
 *
 * 中央体要**同时**满足（否则 cleaver_check 或不变式三会失败）：
 *   ① 咬住两条弧的根层波段：主弧根层内缘到 x=-3.95、副弧根层内缘从 x=3.27 起；
 *   ② 包住**四个枢轴**（x∈[-1.973, 3.355]、y∈[7.386, 16.657]）；
 *   ③ 别把新月吃掉 —— 这一条是**看渲染图才发现的**：第一版中央体开到 x0=-6.50，
 *      而主弧根层内缘只到 -3.95，多出来的 2.55 单位整块盖在刃面上，
 *      于是 45° 灰模里"月牙"完全看不出来，读成一个方块头。
 *      现在收到 -4.90：左侧新月从 -10.55 到 -4.90，**留出 5.65 单位可见弧身**；
 *      右侧副刃从 3.62 到 6.34，留出 2.72 单位。
 * ==========================================================================*/
const HEAD = { x0: -4.90, x1: 3.95, y0: 3.00, y1: 19.00, z: 1.80 };
addBox(axeHead, 'head_body', [HEAD.x0, HEAD.y0, -HEAD.z],
  [HEAD.x1 - HEAD.x0, HEAD.y1 - HEAD.y0, HEAD.z * 2], 'root', true);

/* 轴座：把头 → 柄做成两级收细，避免"柄杆直接怼进大方块"的跳变。
   ★ 柄杆上端留在主弧下刃尖（外角 ≈ y 0.87、x ≈ -1.06）之内，所以杆顶取 y=2.40。 */
addBox(haft, 'collar', [-2.90, 2.20, -1.75], [5.60, 1.60, 3.50], 'band');
addBox(haft, 'shaft', [-1.35, -1.30, -1.20], [2.70, 3.70, 2.40], 'root');

/* 握柄：短而粗（截面 3.4 × 2.9，握柄本体 10.2 单位） */
addBox(handle, 'grip', [-1.70, -11.50, -1.45], [3.40, 10.20, 2.90], 'leather');
addBox(handle, 'grip_band_up', [-1.95, -1.30, -1.70], [3.90, 1.00, 3.40], 'band');
addBox(handle, 'grip_band_dn', [-1.95, -12.10, -1.70], [3.90, 0.90, 3.40], 'band');
addBox(pommel, 'pommel_body', [-1.50, -13.60, -1.45], [3.00, 1.60, 2.90], 'iron');
addBox(pommel, 'pommel_tip', [-0.95, -14.50, -0.95], [1.90, 0.90, 1.90], 'dark');

/* 轴盖：只覆盖铰接位置（四片翼枢轴的 y），不做成大护板 */
addBox(fittings, 'cap_upper', [-3.45, 15.85, -1.95], [2.70, 1.05, 3.90], 'band');
addBox(fittings, 'cap_lower', [-3.35, 6.35, -1.95], [2.70, 1.05, 3.90], 'band');
addBox(fittings, 'cap_right_upper', [2.55, 9.70, -1.70], [1.60, 0.95, 3.40], 'band');
addBox(fittings, 'cap_right_lower', [-1.55, 7.00, -1.70], [1.60, 0.95, 3.40], 'band');
addBox(fittings, 'top_cap', [-1.15, 19.00, -1.15], [2.30, 0.85, 2.30], 'dark');

/* 中央嵌芯：唯一的常规自发光体块。
   ★ 原来做成"正面一片 + 背面一片"两个盒子，它们 x/y 范围完全相同、只差 z，
     于是四个侧面**精确共面且真实重叠**（实测共面违规 4/4 全在这里）。
     合成**一整块**（z 穿过整个头部）后侧面对消失，且外观不变（正反面照样露出红芯）。 */
addBox(core, 'ember_core', [-0.45, 8.00, -2.40], [0.90, 7.00, 4.30], 'ember');
/* ---- 补缝背板（补齐 A5 / 修正 D5） ----
 * 目的：刃翼绕枢轴转开时"刃翼 ↔ 静止中刃"的接缝会露缝，用一片挂在**静止骨骼**上的
 * 薄板挡住；合拢时必须完全藏在刃翼体内（轮廓零变化）。
 * 判据（可证明，比"外端相等"更强）：背板 (x,y) 包络 ⊆ 刃翼 (x,y) 包络，
 * 且 |z| < 刃翼最外层刃口的 |z|。 */
const BACKING = [];
function backing(side, which, wingBone, staticBone, arc, seamDeg) {
  /* D5 修正：原来拿"刃翼 (x,y) 包围盒的整矩形"当背板 —— 主刃那块是 23.4×22.3 的大平板，
     z 缩进只有 0.4，合拢态必然从新月开口里露出来（渲染图已证实是一大片红板）。
     改成**贴着接缝的一片窄条**：沿用弧身的径向分段，切向只占一段宽、居中在接缝角上。
     合拢时左右各一半分别被刃翼与静止中刃盖住 → 零外露；刃翼转开 11° 时缝里才露血光。
     z 取"刃翼 ∪ 静止段"所有 cube 的 z 交集，再各缩 0.10（满足 ≥0.05 的禁共面门槛）。 */
  let zLo = -Infinity, zHi = Infinity;
  for (const b of [wingBone, staticBone]) for (const c of b.cubes || []) {
    if (/_backing$/.test(String(c.label || ''))) continue;   // ★ 排除已加的背板，否则 z 交集逐次收缩
    zLo = Math.max(zLo, c.origin[2]);
    zHi = Math.min(zHi, c.origin[2] + c.size[2]);
  }
  zLo = +(zLo + 0.10).toFixed(3); zHi = +(zHi - 0.10).toFixed(3);
  if (!(zHi - zLo > 0.2)) throw new Error(`背板 z 交集不足：${side}_${which} [${zLo},${zHi}]`);
  const r0 = Math.min(...arc.layers.map(l => l[1]));
  const r1 = Math.max(...arc.layers.map(l => l[2]));
  const step = arc.sweep / arc.parts.reduce((s, v) => s + v, 0);
  /* 切向半宽上限：相邻那一段的盒子只朝接缝方向伸出
       (相邻中心弦距 − 该段半宽) = 2·r1·sin(step/2) − 1.15·r1·tan(step/2)
     超出这个值就会有露出相邻段之外的部分。取 0.92 安全系数。
     同时这个宽度还必须大于展开 11° 时缝口在刃尖处的张开量 (r1 − r_piv)·sin(11°)。 */
  const chord = 2 * r1 * Math.sin((step / 2) * D2R);
  const segHalf = 1.15 * r1 * Math.tan((step / 2) * D2R);
  const maxHalf = +((chord - segHalf) * 0.92).toFixed(3);
  const gapAtTip = +((r1 - (arc.inner - 0.10)) * Math.sin(11 * D2R)).toFixed(3);
  const tangentWidth = +(2 * maxHalf).toFixed(3);
  const c = radialStrip(staticBone, `wing_${side}_${which}_backing`, arc.center, seamDeg,
    r0, r1, tangentWidth, +(zHi - zLo).toFixed(3), +((zLo + zHi) / 2).toFixed(3), 'blood');
  BACKING.push({
    label: `wing_${side}_${which}_backing`, side, which,
    wingBone: wingBone.name, staticBone: staticBone.name,
    seamDeg: +seamDeg.toFixed(3), r: [r0, r1], tangentWidth, z: [zLo, zHi],
    halfW: +(tangentWidth / 2).toFixed(3), maxHalf, gapAtTip,
    zKey: [+(zLo - 0.10).toFixed(3), +(zHi + 0.10).toFixed(3)],   // 未缩之前的 z 交集
  });
  return c;
}
function cornersXY(c) {
  const [ox, oy] = c.origin, [sx, sy] = c.size;
  const rz = (c.rotation ? c.rotation[2] : 0) * D2R, ca = Math.cos(rz), sa = Math.sin(rz);
  const cx = ox + sx / 2, cy = oy + sy / 2, out = [];
  for (const dx of [-sx / 2, sx / 2]) for (const dy of [-sy / 2, sy / 2]) out.push([cx + dx * ca - dy * sa, cy + dx * sa + dy * ca]);
  return out;
}
const mainStep = ARCS[0].sweep / ARCS[0].parts.reduce((s, v) => s + v, 0);
const secStep = ARCS[1].sweep / ARCS[1].parts.reduce((s, v) => s + v, 0);
backing('main', 'upper', wingLU, blade, ARCS[0], ARCS[0].start + 3 * mainStep);
backing('main', 'lower', wingLL, blade, ARCS[0], ARCS[0].start + 6 * mainStep);
backing('right', 'upper', wingRU, bladeRight, ARCS[1], ARCS[1].start + 3 * secStep);
backing('right', 'lower', wingRL, bladeRight, ARCS[1], ARCS[1].start + 6 * secStep);

if (rearHook.cubes && !rearHook.cubes.length) delete rearHook.cubes;

/* ============================================================================
 * §4b 显式刃口 locator（(23) 的六个，名称与角度原样照搬）
 *   ★ locator 用**模型坐标**；对应骨骼运动后，渲染器必须读取该骨骼的最终变换。
 *   ★ 角度是附件给的绝对值（123.333/190/256.667、60/0/-60），圆心平移后位置随之改变。
 * ==========================================================================*/
/* locator 角度改为**跟随新弧的实际段中点**（上翼/静止中刃/下翼各自的中心角），
 * 不再照抄附件那组硬算角度 —— 那组是按旧圆心旧跨度算的，弧一改就会悬在实体外
 * （子代理报告里 secondary_edge_middle 落在实体外 1.06 单位就是这个原因）。 */
const locAngles = arc => {
  const n = arc.parts.reduce((s, v) => s + v, 0), step = arc.sweep / n;
  const [a, b, c] = arc.parts;
  return [arc.start + (a / 2) * step, arc.start + (a + b / 2) * step, arc.start + (a + b + c / 2) * step];
};
const LA0 = locAngles(ARCS[0]), LA1 = locAngles(ARCS[1]);
const LOCATORS = [
  ['blade_upper_wing', 'main_edge_upper', ARCS[0], LA0[0]],
  ['blade', 'main_edge_middle', ARCS[0], LA0[1]],
  ['blade_lower_wing', 'main_edge_lower', ARCS[0], LA0[2]],
  ['blade_right_upper_wing', 'secondary_edge_upper', ARCS[1], LA1[0]],
  ['blade_right', 'secondary_edge_middle', ARCS[1], LA1[1]],
  ['blade_right_lower_wing', 'secondary_edge_lower', ARCS[1], LA1[2]],
];
for (const [bn, ln, arc, deg] of LOCATORS) {
  const b = byName.get(bn);
  (b.locators = b.locators || {})[ln] = [
    +(arc.center[0] + arc.radius * Math.cos(deg * D2R)).toFixed(6),
    +(arc.center[1] + arc.radius * Math.sin(deg * D2R)).toFixed(6), 0,
  ];
}

/* ============================================================================
 * §5 UV 打包 / 逐面绘制 / 三张遮罩
 * ==========================================================================*/
jobs.sort((a, b) => (b.h - a.h) || (b.w - a.w) || String(a.label).localeCompare(String(b.label)) || a.direction.localeCompare(b.direction));
function pack(size) {
  const out = []; let x = 0, y = 0, rowH = 0;
  for (const job of jobs) {
    const w = job.w + PAD * 2, h = job.h + PAD * 2;
    if (w > size || h > size) return null;
    if (x + w > size) { x = 0; y += rowH; rowH = 0; }
    if (y + h > size) return null;
    out.push([x + PAD, y + PAD]); x += w; rowH = Math.max(rowH, h);
  }
  return out;
}
let ATLAS = 0, placements = null;
for (const cand of ATLAS_CANDIDATES) { const p = pack(cand); if (p) { ATLAS = cand; placements = p; break; } }
if (!placements) throw new Error('UV 图集打包失败');
jobs.forEach((job, i) => { const [px, py] = placements[i]; job.cube.uv[job.direction] = { uv: [px, py], uv_size: [job.w, job.h] }; });
{
  const rs = jobs.map((j, i) => ({ n: `${j.bone}/${j.label}.${j.direction}`, x: placements[i][0], y: placements[i][1], w: j.w, h: j.h }));
  for (let i = 0; i < rs.length; i++) for (let k = i + 1; k < rs.length; k++) {
    const a = rs[i], b = rs[k];
    const ox = Math.min(a.x + a.w, b.x + b.w) - Math.max(a.x, b.x), oy = Math.min(a.y + a.h, b.y + b.h) - Math.max(a.y, b.y);
    if (ox > 0 && oy > 0) throw new Error(`UV 矩形重叠：${a.n} × ${b.n} ${ox}x${oy}px`);
  }
  const bottom = Math.max(...rs.map(r => r.y + r.h)), right = Math.max(...rs.map(r => r.x + r.w));
  console.log(`[uv] 图集 ${ATLAS}²  面 ${rs.length}  占用 ${right}×${bottom} (${(bottom / ATLAS * 100).toFixed(1)}% 高)`);
}

function mulberry(s) { return () => { s |= 0; s = s + 0x6D2B79F5 | 0; let t = Math.imul(s ^ s >>> 15, 1 | s); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
const BG = [23, 20, 26];
let seed = 7717;
const MIRRORED = new Set(['secondary', 'right']);

/** 逐面绘制（合并 (22) 的 paint_face 与 (23) 的 paint） */
function paint(job) {
  const w = job.w, h = job.h;
  const base = shade(COLORS[job.material], LIGHT[job.direction]);
  const tile = new Canvas(w, h), normal = new Canvas(w, h), blood = new Canvas(w, h);
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) tile.set(x, y, base[0], base[1], base[2], 1);
  const rnd = mulberry(seed++);
  const mirrored = MIRRORED.has(String(job.label).split('_')[0]);

  if (['iron', 'root', 'band'].includes(job.material) && w >= 4 && h >= 4) {
    const y0 = Math.floor(h * 2 / 3);
    for (let y = y0; y < h; y++) for (let x = 0; x < w; x++) tile.set(x, y, ...shade(base, 0.87), 1);
  }
  for (let x = 0; x < w; x++) tile.set(x, h - 1, ...shade(base, 0.76), 1);

  if (job.material === 'leather') {
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
      const b = (y + (x >> 1)) % 8;
      if (b === 0) tile.set(x, y, ...shade(base, 1.23), 1);
      else if (b === 1) tile.set(x, y, ...shade(base, 0.72), 1);
    }
  } else if (job.material === 'bevel') {
    const x0 = mirrored ? 0 : Math.floor(w / 2), x1 = mirrored ? Math.ceil(w / 2) : w;
    for (let y = 0; y < h; y++) for (let x = x0; x < x1; x++) tile.set(x, y, ...shade(base, 1.13), 1);
  } else if (job.material === 'edge') {
    const bright = mirrored ? 1 : w - 2, bevelAt = mirrored ? 2 : w - 3;
    if (w > 2) for (let y = 0; y < h; y++) tile.set(bright, y, 181, 184, 195, 1);
    if (w > 4) for (let y = 0; y < h; y++) tile.set(bevelAt, y, ...shade(base, 0.74), 1);
    if (w > 4 && h > 6) {
      const gy = 2 + Math.floor(rnd() * Math.max(1, h - 5));
      tile.set(bright, gy, ...shade(base, 0.62), 1);
      if (h > 10) tile.set(bright, (gy + (h >> 1)) % h, ...shade(base, 0.70), 1);
    }
  } else if (job.material === 'ember') {
    if (w >= 3 && h >= 3) {
      for (let y = 1; y <= h - 2; y++) for (let x = 1; x <= w - 2; x++) tile.set(x, y, 166, 30, 44, 1);
      for (let y = 1; y <= h - 2; y++) tile.set(w >> 1, y, 230, 83, 64, 1);
      for (let y = 1; y <= h - 2; y++) for (let x = 1; x <= w - 2; x++) normal.set(x, y, 106, 19, 30, 1);
      for (let y = 1; y <= h - 2; y++) for (let x = 1; x <= w - 2; x++) blood.set(x, y, 214, 34, 53, 1);
      for (let y = 1; y <= h - 2; y++) blood.set(w >> 1, y, 255, 127, 84, 1);
    }
  } else if (job.material === 'blood') {
    const col = mirrored ? 0 : w - 1;
    for (let y = 0; y < h; y++) tile.set(col, y, ...shade(base, 1.25), 1);
  } else if (job.material === 'band') {
    for (let x = 0; x < w; x++) tile.set(x, 0, ...shade(base, 1.18), 1);
  } else {
    /* iron / root / dark：2~3 处明确的磨损块（2×2，不撒 1 像素噪点） */
    const wear = 2 + Math.floor(rnd() * 2);
    for (let i = 0; i < wear; i++) {
      const bx = 1 + Math.floor(rnd() * Math.max(1, w - 3)), by = 1 + Math.floor(rnd() * Math.max(1, h - 3));
      const c = shade(base, rnd() < 0.45 ? 1.20 : 0.78);
      for (let yy = 0; yy < 2 && by + yy < h; yy++) for (let xx = 0; xx < 2 && bx + xx < w; xx++) tile.set(bx + xx, by + yy, c[0], c[1], c[2], 1);
    }
    for (let x = 0; x < w; x++) tile.set(x, 0, ...shade(base, 1.16), 1);
  }

  /* 主裂纹：正面/背面，从刃根向外约 2/3 */
  if (job.crack && (job.direction === 'north' || job.direction === 'south') && w >= 4 && h >= 4) {
    const rootX = mirrored ? w - 2 : 1, dirX = mirrored ? -1 : 1;
    const span = Math.max(3, Math.floor(w * 0.66)), pts = [];
    let x = rootX; const stepX = Math.max(1, Math.floor(span / 6));
    for (let i = 0; i <= span; i += stepX) {
      const d = rnd(), dy = d < 0.34 ? -1 : (d < 0.67 ? 0 : 1);
      pts.push([x, Math.max(1, Math.min(h - 2, (h >> 1) + dy * (1 + Math.floor(rnd() * 2))))]);
      x = Math.max(1, Math.min(w - 2, x + dirX * stepX));
    }
    const put = (cv, c, x0, y0, x1, y1) => {
      const dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
      let e = dx - dy, px = x0, py = y0;
      for (;;) { cv.set(px, py, c[0], c[1], c[2], 1); if (px === x1 && py === y1) break; const e2 = 2 * e; if (e2 > -dy) { e -= dy; px += sx; } if (e2 < dx) { e += dx; py += sy; } }
    };
    for (let i = 1; i < pts.length; i++) {
      put(tile, [88, 22, 33], pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1]);
      put(blood, BLOOD_MAIN, pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1]);
    }
    for (let i = 1; i < pts.length; i++) {
      const t = i / pts.length, [px, py] = pts[i];
      tile.set(px, py, ...(t < 0.5 ? [197, 57, 42] : t < 0.8 ? [150, 40, 40] : [108, 30, 34]), 1);
      blood.set(px, py, ...(t < 0.5 ? BLOOD_HOT : BLOOD_MAIN), 1);
    }
  }
  return { tile, normal, blood };
}

const atlas = new Canvas(ATLAS, ATLAS), glowNormal = new Canvas(ATLAS, ATLAS), glowBlood = new Canvas(ATLAS, ATLAS);
for (let y = 0; y < ATLAS; y++) for (let x = 0; x < ATLAS; x++) atlas.set(x, y, BG[0], BG[1], BG[2], 1);
/* 每块 UV 独立 padding：边缘像素向外延展一圈，最近邻采样不会串到邻块 */
const pastePadded = (dst, tile, x, y) => {
  for (let py = -PAD; py < tile.h + PAD; py++) for (let px = -PAD; px < tile.w + PAD; px++) {
    const inX = px >= 0 && px < tile.w, inY = py >= 0 && py < tile.h;
    if (!inX && !inY) continue;                            // 四角不填（斜角不属于任何边的延展）
    const sx = Math.max(0, Math.min(tile.w - 1, px)), sy = Math.max(0, Math.min(tile.h - 1, py));
    const i = (sy * tile.w + sx) * 4;
    if (tile.d[i + 3] === 0) continue;
    dst.set(x + px, y + py, tile.d[i], tile.d[i + 1], tile.d[i + 2], 1);
  }
};
const rectOf = (label, dir) => { const j = jobs.find(jj => jj.label === label && jj.direction === dir); return j ? { x: j.cube.uv[dir].uv[0], y: j.cube.uv[dir].uv[1], w: j.w, h: j.h } : null; };
for (const job of jobs) {
  const [x, y] = job.cube.uv[job.direction].uv;
  const { tile, normal, blood } = paint(job);
  pastePadded(atlas, tile, x, y); pastePadded(glowNormal, normal, x, y); pastePadded(glowBlood, blood, x, y);
}
/* 刃根红缝：独立第二张遮罩（展开进度靠前时先亮） */
const seam = new Canvas(ATLAS, ATLAS);
for (const bk of BACKING) for (const dir of ['north', 'south', 'up', 'down']) {
  const r = rectOf(bk.label, dir); if (!r) continue;
  for (let yy = 0; yy < r.h; yy++) for (let xx = 0; xx < r.w; xx++) {
    const hot = (xx === r.w - 1 || yy === 0 || yy === r.h - 1);
    const c = hot ? BLOOD_MAIN : BLOOD_DARK;
    seam.set(r.x + xx, r.y + yy, c[0], c[1], c[2], 1);
  }
}
atlas.save(path.join(OUT, `${NAME}_geo.png`));
glowNormal.save(path.join(OUT, `${NAME}_emissive.png`));
glowBlood.save(path.join(OUT, `${NAME}_blood_emissive.png`));
seam.save(path.join(OUT, `${NAME}_blood_seam_emissive.png`));
atlas.scaled(2).save(path.join(OUT, 'texture_preview.png'));
/* 补齐 A6：(23) 想画的"主贴图 / 普通发光 / 血怒发光"并排图 */
{
  const sheet = new Canvas(ATLAS * 3, ATLAS);
  for (let y = 0; y < ATLAS; y++) for (let x = 0; x < ATLAS * 3; x++) sheet.set(x, y, 30, 28, 34, 1);
  for (const [idx, cv] of [atlas, glowNormal, glowBlood].entries()) {
    for (let y = 0; y < ATLAS; y++) for (let x = 0; x < ATLAS; x++) {
      const i = (y * ATLAS + x) * 4;
      if (cv.d[i + 3] === 0) continue;
      sheet.set(idx * ATLAS + x, y, cv.d[i], cv.d[i + 1], cv.d[i + 2], 1);
    }
  }
  sheet.save(path.join(OUT, 'texture_layers.png'));
}

/* ---- geo / 动画 ---- */
const geo = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: { identifier: `geometry.${NAME}`, texture_width: ATLAS, texture_height: ATLAS, visible_bounds_width: 5, visible_bounds_height: 5, visible_bounds_offset: [0, 0.4, 0] },
    bones,
  }],
};
fs.writeFileSync(path.join(OUT, `${NAME}.geo.json`), JSON.stringify(geo, null, 2));

const wingAngles = { blade_upper_wing: -11, blade_lower_wing: 11, blade_right_upper_wing: 11, blade_right_lower_wing: -11 };
const animations = {
  [`animation.${NAME}.idle`]: {                          // ★ (23)：idle **不再**控制四翼，避免与 Java 冲突
    loop: true, animation_length: 3, bones: {
      weapon: { rotation: [0, 0, 0], position: [0, 0, 0], scale: [1, 1, 1] },
      ember_core: { scale: { '0.0': [1, 1, 1], '1.5': [1.015, 1.02, 1], '3.0': [1, 1, 1] } },
    },
  },
  [`animation.${NAME}.blood_pose`]: {                    // 仅预览用
    loop: false, animation_length: 1,
    bones: Object.fromEntries(Object.entries(wingAngles).map(([n, a]) => [n, { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, a] } }])),
  },
};
fs.writeFileSync(path.join(OUT, `${NAME}.animation.json`), JSON.stringify({ format_version: '1.8.0', animations }, null, 2));

/* ============================================================================
 * §6 结构数据 + 不变式自检（生成器先自算一遍，守门工具再独立算一遍）
 * ==========================================================================*/
const r3 = v => Math.round(v * 1000) / 1000;
const boundsOfBone = b => {
  const pts = []; for (const c of b.cubes || []) pts.push(...cornersXY(c));
  return pts.length ? [0, 1].map(k => [Math.min(...pts.map(p => p[k])), Math.max(...pts.map(p => p[k]))]) : null;
};
const report = { name: NAME, bones: bones.length, cubes: cubeTotal, faces: jobs.length, atlas: ATLAS, arcs: arcSpecs, invariants: {}, geometry: {} };

/* 全模型包围盒 */
{
  const pts = [];
  for (const b of bones) for (const c of b.cubes || []) {
    const [ox, oy, oz] = c.origin, [sx, sy, sz] = c.size;
    const rz = (c.rotation ? c.rotation[2] : 0) * D2R, ca = Math.cos(rz), sa = Math.sin(rz);
    const cx = ox + sx / 2, cy = oy + sy / 2;
    for (const dx of [-sx / 2, sx / 2]) for (const dy of [-sy / 2, sy / 2]) pts.push([cx + dx * ca - dy * sa, cy + dx * sa + dy * ca, oz, oz + sz]);
  }
  const mn = [0, 1, 2].map(i => Math.min(...pts.map(p => i === 2 ? p[2] : p[i])));
  const mx = [0, 1, 2].map(i => Math.max(...pts.map(p => i === 2 ? p[3] : p[i])));
  report.geometry.bounds_min = mn.map(r3); report.geometry.bounds_max = mx.map(r3);
  report.geometry.size = [0, 1, 2].map(i => r3(mx[i] - mn[i]));
}
/* 头 / 刃伸出 / 握柄
 * ★ 度量修正：原来只统计 blade / blade_right 两根骨骼（**不含四片翼**），且把背板也算进去 ——
 *   旧值 副刃伸出 6.068 恰好等于旧背板的 x0，也就是说 58.4% 这个比例是**背板撑出来的假数**。
 *   现在按"这一侧所有刃体骨骼（含翼）"取包围盒，并排除背板（补缝结构，不是刃）。 */
{
  const boundsOf = names => {
    const xs = [], ys = [], zs = [];
    for (const bn of names) for (const c of byName.get(bn).cubes || []) {
      if (/_backing$/.test(String(c.label || ''))) continue;
      for (const p of cornersXY(c)) { xs.push(p[0]); ys.push(p[1]); }
      zs.push(Math.abs(c.origin[2]), Math.abs(c.origin[2] + c.size[2]));
    }
    return { x: [Math.min(...xs), Math.max(...xs)], y: [Math.min(...ys), Math.max(...ys)], z: Math.max(...zs) };
  };
  const M = boundsOf(['blade', 'blade_upper_wing', 'blade_lower_wing']);
  const S = boundsOf(['blade_right', 'blade_right_upper_wing', 'blade_right_lower_wing']);
  const mainReach = r3(-M.x[0]), secReach = r3(S.x[1]);
  report.geometry.main_blade_reach_from_axis = mainReach;
  report.geometry.secondary_blade_reach_from_axis = secReach;
  report.geometry.secondary_over_main = r3(secReach / mainReach);
  report.geometry.head_width_total = r3(Math.max(M.x[1], S.x[1]) - Math.min(M.x[0], S.x[0]));
  report.geometry.head_height = r3(Math.max(M.y[1], S.y[1]) - Math.min(M.y[0], S.y[0]));
  report.geometry.head_z_thickness = r3(2 * Math.max(M.z, S.z));
  report.geometry.main_x = M.x.map(r3); report.geometry.secondary_x = S.x.map(r3);
}
{
  const grip = byName.get('handle').cubes.find(c => c.size[1] > 5);
  report.geometry.grip_length = r3(grip.size[1]);
  report.geometry.grip_section = [r3(grip.size[0]), r3(grip.size[2])];
  const low = Math.min(...[...byName.get('handle').cubes, ...byName.get('pommel').cubes].map(c => c.origin[1]));
  const top = Math.max(...byName.get('haft').cubes.map(c => c.origin[1] + c.size[1]));
  report.geometry.handle_total_length = r3(top - low);
  report.geometry.handle_top_y = r3(top); report.geometry.handle_bottom_y = r3(low);
  report.geometry.head_body = HEAD;
}
/* 不变式一：关节安全律 */
{
  const rows = [];
  for (const [wname, deg] of Object.entries(wingAngles)) {
    const wb = byName.get(wname), piv = wb.pivot, pts = [];
    for (const c of wb.cubes || []) pts.push(...cornersXY(c));
    let farD = -1;
    for (const p of pts) { const d = Math.hypot(p[0] - piv[0], p[1] - piv[1]); if (d > farD) farD = d; }
    const height = r3(Math.max(...pts.map(p => p[1])) - Math.min(...pts.map(p => p[1])));
    const rise = r3(farD * Math.sin(Math.abs(deg) * D2R));
    rows.push({ wing: wname, angle: deg, pivot: piv.slice(0, 2), pivot_to_tip: r3(farD), wing_height: height, rise, margin: r3(height - rise), ok: rise <= height + 1e-9 });
  }
  report.invariants.joint_safety = { rule: '枢轴到刃尖距离 × sin(展开角) ≤ 刃翼高度', max_angle: MAX_WING_ANGLE, rows, all_ok: rows.every(r => r.ok) };
}
/* 不变式二：补缝背板合拢态完全隐形（D5 重做后的可证明判据）
 *   旧判据是"(x,y) 包络 ⊆ 刃翼包络" —— 那是 AABB 包含，**不蕴含视觉遮挡**：
 *   刃翼实际只是一条弧带，而它的包围盒里大半是空的，于是 23×22 的大平板照样露在外面。
 *   新判据直接对"被盖住"这件事下定义：
 *     (a) 切向半宽 ≤ 单段半弦长 → 接缝左右两侧各有一段（刃翼段 / 静止段）盖住它；
 *     (b) z 两个面各缩进"刃翼 ∪ 静止段"的 z 交集 ≥0.05 → 前后两个大面都被包在里面。 */
{
  const rows = BACKING.map(bk => {
    const wb = byName.get(bk.wingBone), sb = byName.get(bk.staticBone);
    let zLo = -Infinity, zHi = Infinity;
    for (const b of [wb, sb]) for (const c of b.cubes || []) {
      if (/_backing$/.test(String(c.label || ''))) continue;
      zLo = Math.max(zLo, c.origin[2]); zHi = Math.min(zHi, c.origin[2] + c.size[2]);
    }
    const insetLo = +(bk.z[0] - zLo).toFixed(4), insetHi = +(zHi - bk.z[1]).toFixed(4);
    const zOk = Math.min(insetLo, insetHi) >= 0.05;
    const wOk = bk.halfW <= bk.maxHalf + 1e-9;
    const coverOk = bk.gapAtTip <= bk.halfW + 1e-9;
    return {
      backing: bk.label, wing: bk.wingBone, static_bone: bk.staticBone, seam_deg: bk.seamDeg,
      backing_z: bk.z.map(r3), z_inset: [insetLo, insetHi],
      half_width: bk.halfW, max_half_width: bk.maxHalf, gap_at_tip: bk.gapAtTip,
      z_ok: zOk, width_ok: wOk, covers_gap_ok: coverOk, ok: zOk && wOk && coverOk,
    };
  });
  report.invariants.backing_hidden_at_rest = {
    rule: '背板贴合接缝：切向半宽 ≤ 相邻段伸入量（合拢被盖住）且 ≥ 展开缝口张开量（展开不漏底），且 z 两面各缩进交集 ≥0.05',
    rows, all_ok: rows.every(r => r.ok),
  };
}
/* 不变式三：枢轴贴朝内边缘 0.1，且落在中央体内 */
{
  const rows = [];
  for (const [wname, arc] of [['blade_upper_wing', ARCS[0]], ['blade_lower_wing', ARCS[0]], ['blade_right_upper_wing', ARCS[1]], ['blade_right_lower_wing', ARCS[1]]]) {
    const wb = byName.get(wname), piv = wb.pivot;
    const rPiv = Math.hypot(piv[0] - arc.center[0], piv[1] - arc.center[1]);
    const inHeadXY = piv[0] > HEAD.x0 && piv[0] < HEAD.x1 && piv[1] > HEAD.y0 && piv[1] < HEAD.y1;
    /* ★ z 也要判：枢轴 pivot 是三元组，早先这里只取了 [0],[1] 两个分量做包含性判断，
       却把第三个分量（=0）也拿去比 HEAD.y1 —— 0 > 18 恒为假，于是四条全部 FAIL。
       这是我自己的检查器写错了，不是几何问题（gap 四条都精确等于 0.1）。 */
    const inHead = inHeadXY && Math.abs(piv[2]) < HEAD.z;
    const gap = arc.inner - rPiv;
    /* 容差 1e-3：pivot 落盘精度是 1e-6，1e-3 足够宽又远小于"0.1 单位"这个量级本身。 */
    const okGap = Math.abs(gap - 0.1) < 1e-3;
    /* ★ 容差必须给到 1e-4，不能是 1e-6：pivot 坐标 toFixed(3) 之后
       Math.hypot 回来的半径是 4.799999999999999，gap 变成 0.10000000000000009，
       用 1e-6 判定会把四条全部判失败（我实测踩过这个假阳性）。 */
    rows.push({ wing: wname, pivot: piv.slice(0, 2), radius_from_arc_center: r3(rPiv), gap_to_inner_edge: r3(gap), inside_head_body: inHead, ok: okGap && inHead });  }
  report.invariants.wing_pivot_at_inner_edge = { rule: '枢轴贴朝内边缘（留 0.1）且落在中央体内', rows, all_ok: rows.every(r => r.ok) };
}
/* 不变式四：共面间距 —— 只抓**真正的 z-fighting**：
   同一骨骼上、两个 cube 的**同向可视面**落在同一平面（差 < COPLANAR_EPS）且在该平面内
   **真实重叠面积** > COPLANAR_EPS²。
   ★ 不能拿 AABB 端面距当判据（我第一版就是这样，52~114 项里绝大多数是假阳性）：
     弧刃的分段是**绕 Z 轴转过角度**的盒子，它的 x/y "端面"根本不是面 —— 真实面是
     垂直于条带轴的斜面。两个相差 16.7° 的条带，AABB 的 y 端面差 0.017，但真实面
     方向差了 16.7°，永远不会共面。所以：
       · z 面：⊥ 旋转轴，**仍然真实轴对齐** → 用"平面差 + 旋转后的四边形真实相交面积"判；
       · x/y 面：只有**未旋转**的 cube 才真实轴对齐 → 只对双方都未旋转的对判。
   ★ 阈值：工程 §4 的共面判据是 **< 0.05**（AGENTS.md 第 611 行），不是 cleaver_check.cjs
     里"每轴错开 ≥0.2"那条（那条只用于**不同骨骼**的相邻段）。 */
{
  /* 旋转矩形（4 角点）裁剪求交面积 —— Sutherland-Hodgman */
  const clipArea = (subject, clip) => {
    const inside = (p, a, b) => (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]) >= -1e-12;
    const cut = (poly, a, b) => {
      const out = [];
      for (let i = 0; i < poly.length; i++) {
        const P = poly[i], Q = poly[(i + 1) % poly.length];
        const ip = inside(P, a, b), iq = inside(Q, a, b);
        if (ip) out.push(P);
        if (ip !== iq) {
          const dx = Q[0] - P[0], dy = Q[1] - P[1];
          const ex = b[0] - a[0], ey = b[1] - a[1];
          const den = dx * ey - dy * ex;
          if (Math.abs(den) > 1e-12) {
            const t = ((a[0] - P[0]) * ey - (a[1] - P[1]) * ex) / den;
            out.push([P[0] + t * dx, P[1] + t * dy]);
          }
        }
      }
      return out;
    };
    let poly = subject.slice();
    for (let i = 0; i < clip.length && poly.length; i++) poly = cut(poly, clip[i], clip[(i + 1) % clip.length]);
    if (poly.length < 3) return 0;
    let s = 0;
    for (let i = 0; i < poly.length; i++) { const a = poly[i], b = poly[(i + 1) % poly.length]; s += a[0] * b[1] - b[0] * a[1]; }
    return Math.abs(s) / 2;
  };
  const bad = [];
  for (const b of bones) {
    const cs = b.cubes || [];
    const info = cs.map(c => {
      const rot = c.rotation ? c.rotation[2] : 0;
      return { c, rot: +rot.toFixed(6), pts: cornersXY(c), z0: c.origin[2], z1: c.origin[2] + c.size[2] };
    });
    for (let i = 0; i < info.length; i++) for (let k = i + 1; k < info.length; k++) {
      const A = info[i], B = info[k];
      /* z 面：永远真实轴对齐 —— 平面差足够小则再算真实重叠面积 */
      for (const [za, la] of [[A.z0, '-'], [A.z1, '+']]) for (const [zb, lb] of [[B.z0, '-'], [B.z1, '+']]) {
        if (la !== lb) continue;
        const dz = Math.abs(za - zb);
        if (dz >= COPLANAR_EPS) continue;
        const area = clipArea(A.pts, B.pts);
        if (area > COPLANAR_EPS * COPLANAR_EPS) {
          bad.push(`${b.name}: cube[${i}]x cube[${k}] z${la} 面共面（差 ${r3(dz)}）真实重叠面积 ${r3(area)}`);
        }
      }
      /* x/y 面：只有两个 cube 都未旋转时才真实轴对齐 */
      if (A.rot !== 0 || B.rot !== 0) continue;
      for (const ax of [0, 1]) {
        const span = (o, a) => a === 0 ? [o.c.origin[0], o.c.origin[0] + o.c.size[0]] : [o.c.origin[1], o.c.origin[1] + o.c.size[1]];
        for (const sgn of [0, 1]) {
          const pa = span(A, ax)[sgn], pb = span(B, ax)[sgn];
          if (Math.abs(pa - pb) >= COPLANAR_EPS) continue;
          const other = ax === 0 ? 1 : 0;
          const sa = span(A, other), sb = span(B, other);
          const o = Math.min(sa[1], sb[1]) - Math.max(sa[0], sb[0]);
          if (o > COPLANAR_EPS) bad.push(`${b.name}: cube[${i}]x cube[${k}] ${'xy'[ax]} 面共面（差 ${r3(Math.abs(pa - pb))}）面内重叠 ${r3(o)}`);
        }
      }
    }
  }
  report.invariants.coplanar_gap = {
    rule: `任何两个同向可视面不得共面（z 面按旋转后真实相交面积判；x/y 面只判未旋转的 cube）`,
    violations: bad, all_ok: bad.length === 0,
  };
}
/* 不变式五：同一骨骼内、**且 x/y 包络互相重叠**的 cube，厚度必须两两不同
   （D4 的回归防线）。加了"重叠"这个前提，是因为同一骨骼上完全分开的两块
   （比如 blade 上的上/下背板）厚度相同并不会产生共面。 */
{
  /* 不变式五（重定义）：同一骨骼内 x/y 重叠的 cube，**两个 z 面都必须错开 ≥0.05**。
     旧版写的是"厚度必须两两不同" —— 那是为"逐段改厚度"那套方案发明的：
     改厚度时 z0=-t/2+off 与 z1=+t/2+off 各只移动一半，所以厚度差 0.035 只换来 0.0175 面间距，
     要凑够 0.05 就得把总跨距推到 0.8。现行方案改 z 偏移（两面同时移动），厚度可以保持恒定，
     所以判据要回到真正要防的东西：**面是否共面**。 */
  const rows = [];
  for (const b of bones) {
    const cs = b.cubes || [];
    const box = cs.map(c => { const p = cornersXY(c); return [Math.min(...p.map(q => q[0])), Math.max(...p.map(q => q[0])), Math.min(...p.map(q => q[1])), Math.max(...p.map(q => q[1]))]; });
    for (let i = 0; i < cs.length; i++) for (let k = i + 1; k < cs.length; k++) {
      const A = box[i], B = box[k];
      const ox = Math.min(A[1], B[1]) - Math.max(A[0], B[0]);
      const oy = Math.min(A[3], B[3]) - Math.max(A[2], B[2]);
      if (ox <= 0.05 || oy <= 0.05) continue;                 // x/y 重叠太小 → 无共面风险
      const dz0 = Math.abs(cs[i].origin[2] - cs[k].origin[2]);
      const dz1 = Math.abs((cs[i].origin[2] + cs[i].size[2]) - (cs[k].origin[2] + cs[k].size[2]));
      if (Math.min(dz0, dz1) < 0.05) rows.push(`${b.name}: cube[${i}]x cube[${k}] z面间距 ${r3(Math.min(dz0, dz1))} < 0.05，x/y 重叠 ${r3(ox)}x${r3(oy)}`);
    }
  }
  report.invariants.z_face_separation = { rule: '同一骨骼内 x/y 重叠 >0.05 的 cube，两个 z 面都必须错开 ≥0.05', violations: rows, all_ok: rows.length === 0 };
}
fs.writeFileSync(path.join(OUT, `${NAME}_report.json`), JSON.stringify(report, null, 2));

console.log(`[rebuild] 骨骼 ${bones.length}  立方体 ${cubeTotal}  面 ${jobs.length}  图集 ${ATLAS}²`);
console.log(`[rebuild] 包围盒 min [${report.geometry.bounds_min}] max [${report.geometry.bounds_max}] 尺寸 ${report.geometry.size.join(' x ')}`);
console.log(`[rebuild] 主刃伸出 ${report.geometry.main_blade_reach_from_axis}  副刃 ${report.geometry.secondary_blade_reach_from_axis}  比例 ${report.geometry.secondary_over_main}`);
console.log(`[rebuild] 关节安全律 ${report.invariants.joint_safety.all_ok ? 'OK' : 'FAIL'}` +
  `  背板隐形 ${report.invariants.backing_hidden_at_rest.all_ok ? 'OK' : 'FAIL'}` +
  `  枢轴 ${report.invariants.wing_pivot_at_inner_edge.all_ok ? 'OK' : 'FAIL'}` +
  `  共面 ${report.invariants.coplanar_gap.all_ok ? 'OK' : 'FAIL(' + report.invariants.coplanar_gap.violations.length + ')'}` +
  `  z面错开 ${report.invariants.z_face_separation.all_ok ? 'OK' : 'FAIL(' + report.invariants.z_face_separation.violations.length + ')'}`);
console.log('[rebuild] 输出：' + path.relative(ROOT, OUT));
