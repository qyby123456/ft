/* 「黑色切割者·赤痕」生成器 —— 移植自用户提供的 python 脚本(逐字保留其数值)
   产物: geo/black_cleaver.geo.json, animations/black_cleaver.animation.json,
         textures/item/black_cleaver.png(+_emissive), textures/effect/cleaver_slam_*.png,
         art/black_cleaver_uv_preview.png, ref/black_cleaver_manifest.json
   用法: node gen_black_cleaver.cjs                                                            */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const ROOT = path.resolve(__dirname, '..', '..');
const RES = path.join(ROOT, 'src/main/resources/assets/ftboss');
const OUT_ART = path.join(ROOT, 'art');
const OUT_REF = path.join(ROOT, 'ref');
for (const d of [path.join(RES, 'geo'), path.join(RES, 'animations'), path.join(RES, 'textures/item'),
                 path.join(RES, 'textures/effect'), OUT_ART, OUT_REF]) fs.mkdirSync(d, { recursive: true });

const SIZE = 256, DENSITY = 2, PAD = 2;
const atlas = new Canvas(SIZE, SIZE);
const COLORS = {
  iron: [42, 37, 43],
  /* ★ 2026-09-16 用户手绘参考图标：**刃口是红色亮边**（不是冷灰）。
     之前规格写"冷灰弧形刃口"，但用户手绘稿是「黑铁身 + 红刃口」—— 以用户手绘为准。
     只改这一处即可：paintFace 的 edge 分支亮线用 shade(base,1.38)，会自动变成更亮的红。 */
  edge: [172, 36, 42], leather: [65, 40, 34],
  band: [75, 65, 67], dark: [27, 23, 29], ember: [137, 29, 35],
  /* ★ 2026-09-16 新增：**刃根红缝**专用材质（规格「展开后露出窄红色接缝」）。
     比 ember 更深更暗 —— 它只在刃翼张开时从缝里透出来，合拢时完全不可见，
     所以不能画亮，否则一开一合像在闪红点。 */
  blood: [94, 22, 30],
};
const LIGHT = { north: 1.08, south: 0.96, west: 1.05, east: 0.83, up: 1.17, down: 0.72 };
const shade = (rgb, v) => rgb.map(c => Math.max(0, Math.min(255, Math.round(c * v))));
for (let y = 0; y < SIZE; y++) for (let x = 0; x < SIZE; x++) atlas.set(x, y, 22, 19, 23, 1);

const bones = [], jobs = [];
let cubeTotal = 0;
const makeBone = (name, parent, pivot = [0, 0, 0]) => { const b = { name, pivot }; if (parent) b.parent = parent; bones.push(b); return b; };
const addBox = (bone, label, origin, size, material, cracks = false) => {
  for (const v of size) if (!(v > 0)) throw new Error('size 必须为正: ' + label);
  const c = { origin: [...origin], size: [...size], uv: {} };
  (bone.cubes = bone.cubes || []).push(c); cubeTotal++;
  const [x, y, z] = size;
  const dims = { north: [x, y], south: [x, y], west: [z, y], east: [z, y], up: [x, z], down: [x, z] };
  for (const [dir, [w, h]] of Object.entries(dims)) {
    jobs.push({ cube: c, label, direction: dir, w: Math.max(2, Math.ceil(w * DENSITY)), h: Math.max(2, Math.ceil(h * DENSITY)), material, cracks });
  }
};

/* ---------- 骨架(与脚本一致) ---------- */
makeBone('root');
makeBone('grip_anchor', 'root');
const weapon = makeBone('weapon', 'grip_anchor');
const handle = makeBone('handle', 'weapon');
const haft = makeBone('haft', 'weapon', [0, 6, 0]);
const head = makeBone('axe_head', 'weapon', [0, 15, 0]);
const blade = makeBone('blade', 'axe_head', [-1.5, 15, 0]);
const rear = makeBone('rear_hook', 'axe_head', [1.4, 15, 0]);
const fittings = makeBone('fittings', 'axe_head', [0, 15, 0]);
const core = makeBone('ember_core', 'axe_head', [0, 15, 0]);
const pommel = makeBone('pommel', 'weapon', [0, -6, 0]);
makeBone('impact_socket', 'axe_head', [-7.8, 14.7, 0]);
const upperWing = makeBone('blade_upper_wing', 'blade', [-1.55, 17.9, 0]);
const lowerWing = makeBone('blade_lower_wing', 'blade', [-1.55, 12.2, 0]);

/* ---------- 2026-09-15 v5：改成**双刃战斧**（用户：黑切是双头斧） ----------
   方向纠正：原来做成"一侧宽刃 + 另一侧短钩"，现在右侧也是完整宽斧刃。
   规格（用户转来的 make_black_cleaver_double.py）要求：
     · 移除右侧短钩的**几何**，但保留空的 rear_hook 骨骼（不破坏旧引用）；
     · 镜像左侧斧身 / 刃口 / 强化剑脊 / 轴盖，形成完整右刃；
     · 新增右侧上下刃翼；
     · blood_pose 扩成四翼对称展开。
   ★ 镜像不需要动 UV：本生成器**每个面各自分一块图集矩形**（不是 box-UV 共享），
     所以右侧各面会各自画一份，不需要像 python 版那样靠"翻转 u + 交换 east/west"复用左侧贴图。
     代价是左右裂纹走向不是逐像素镜像（各自的随机种子）—— 属于自然差异，不是缺陷。 */
const bladeRight = makeBone('blade_right', 'axe_head', [1.5, 15, 0]);
const upperWingRight = makeBone('blade_right_upper_wing', 'blade_right', [1.55, 17.9, 0]);
const lowerWingRight = makeBone('blade_right_lower_wing', 'blade_right', [1.55, 12.2, 0]);

/* Bedrock 的镜像规矩（AGENTS.md 的 cubePair）：右件 origin.x = -(x + w)，y/z 与尺寸不变。 */
const mirrorX = (origin, size) => [-(origin[0] + size[0]), origin[1], origin[2]];
/** 同时给左右两套骨骼加一对镜像 box（label 加 _r 后缀便于报错定位）。 */
const addPairBox = (boneL, boneR, label, origin, size, material, cracks = false) => {
  addBox(boneL, label, origin, size, material, cracks);
  addBox(boneR, label + '_r', mirrorX(origin, size), size, material, cracks);
};

/* ---------- 几何(逐字照搬) ---------- */
addBox(handle, 'grip', [-1.00, -1.60, -1.00], [2.00, 7.00, 2.00], 'leather');
addBox(handle, 'lower_band', [-1.25, -1.85, -1.25], [2.50, 0.8, 2.50], 'band');
addBox(handle, 'upper_band', [-1.25, 4.9, -1.25], [2.50, 0.8, 2.50], 'band');
addBox(haft, 'shaft', [-0.85, 5.3, -0.85], [1.70, 9.0, 1.70], 'iron');
addBox(haft, 'shaft_collar', [-1.15, 8.8, -1.15], [2.30, 0.8, 2.30], 'band');

/* ★ 2026-09-16 用户「斧柄的接口过渡要做好」：柄杆 1.70 直接接到斧眼 3.20 是**一处跳变** ✗
   这里补两级颈，形成 1.70 -> 2.40 -> 3.80 -> 斧眼 的连续过渡；
   颈二比斧眼(z ±1.80)略厚，所以它读作"轴座包箍"而不是被埋掉。 */
addBox(haft, 'neck_lower', [-1.20, 9.80, -1.20], [2.40, 1.40, 2.40], 'band');
addBox(haft, 'neck_upper', [-1.90, 11.00, -1.90], [3.80, 1.00, 3.80], 'band');
/* ---------- 中央斧眼 / 包箍 / 顶盖：**按规格收薄收小**（2026-09-16 v6）----------
   用户原话："当前斧头中间太厚、两侧斧刃偏窄，外轮廓又接近矩形，所以更像长柄锤"。
   所以中央这一坨的 z（厚度）从 ±1.35/±1.15/±1.04 收到 ±1.05/±0.95/±0.95，
   顶盖也缩小；让**两侧刃面**成为轮廓与体积的主体。 */
addBox(head, 'head_body', [-1.6, 11.1, -1.80], [3.2, 8.4, 3.60], 'iron', true);
/* 轴盖**只覆盖铰接位置**，不做成大护板（规格：中央结构收紧） */
addBox(fittings, 'head_lower_band', [-1.45, 11.6, -1.70], [2.9, 0.7, 3.40], 'band');
addBox(fittings, 'head_upper_band', [-1.45, 18.0, -1.70], [2.9, 0.7, 3.40], 'band');
addBox(fittings, 'top_cap', [-0.55, 19.1, -0.95], [1.1, 0.9, 1.9], 'dark');
/* ---------- v6：两侧改成**外凸弧形刃口**（每侧 7 段轮廓）----------
   规格要求：上端向中央收 → 中段向外鼓 → 下端再收，上下端形成短尖角（不是长钩）；
   轮廓必须由**实际几何**体现，不能只在矩形面上画弧；
   厚度从根部向外递减：中央 ±0.95 → 刃面 ±0.88 → 中间过渡 ±0.60 → 刃口 ±0.38 → 尖端 ±0.34。
   活动刃翼（blade_upper/lower）各承担弧的上下两段，中央固定刃（blade）承担中段鼓出。
   ★ 注意：这里全部走 addPairBox（左右各一份），**不能再单独 addBox 左侧**，否则重复 cube。 */
/* ★★★ 2026-09-16 v11：**非对称双刃**（规格 code(18)）★★★
   主刃方向已核实：render_tex 的 front 投影是 [x, -y]，所以**正面左手边 = 模型 x<0** = bone `blade`
   （伸出 1.40→10.20 = 8.80）= 主刃；副刃 = x>0 的 `blade_right`，本次打破镜像缩到 78%
   （伸出 6.86 → 外沿 x=8.26），高度取主刃约 87%。
   下面这些 `addPairBox(... 'xxx', ...)` 后面紧跟的那行 `addBox(xxxRight, ...)` 就是**显式右侧**，
   覆盖掉镜像出来的那一份 —— 中央连接结构（spine / spine_tip）仍保持左右一致，
   因为规格要求"轴盖、刃根和连接结构不应一起变小"。 */
/* 上翼：**收短**（规格：「上端收得短一些」）。
   下翼：**略长**（「下端稍微延长，形成向下切削的轮廓」），并且靠近柄杆的下缘**内凹**一格
   —— 拆成"外长内短"两块，做出那个台阶，而不是一根等长的条（规格第 1 条）。
   硬约束不变：枢轴贴着自己上边缘（上翼 18.0→枢轴 17.9、下翼 12.3→枢轴 12.2，dy=0.1），
   旋转时角点不会甩出刃翼外端，所以 16° 仍不露缝。 */
addBox(upperWing, 'blade_upper', [-8.20, 17.55, -1.20], [6.80, 2.25, 2.40], 'iron', true);
addBox(upperWingRight, 'blade_upper_r', [1.40, 17.70, -1.20], [1.70, 1.15, 2.40], 'iron', true);
addBox(upperWing, 'edge_upper', [-9.00, 17.65, -0.30], [0.80, 2.05, 0.60], 'edge');
addBox(upperWingRight, 'edge_upper_r', [3.10, 17.75, -0.30], [0.60, 1.05, 0.60], 'edge');
addBox(lowerWing, 'blade_lower', [-8.20, 9.6, -1.20], [4.60, 2.7, 2.40], 'iron', true);
addBox(lowerWingRight, 'blade_lower_r', [1.70, 11.30, -1.20], [1.40, 1.15, 2.40], 'iron', true);
addBox(lowerWing, 'blade_lower_inner', [-3.60, 11.2, -1.20], [2.20, 1.1, 2.40], 'iron', true);
addBox(lowerWingRight, 'blade_lower_inner_r', [1.40, 11.95, -1.20], [0.30, 0.50, 2.40], 'iron', true);
addBox(lowerWing, 'edge_lower', [-9.00, 9.7, -0.30], [0.80, 2.5, 0.60], 'edge');
addBox(lowerWingRight, 'edge_lower_r', [3.10, 11.40, -0.30], [0.60, 0.95, 0.60], 'edge');
/* 中央固定刃：**弧线只体现在外缘**（规格第 3 条：「分段只影响外缘」「刃面应是一整片」）。
   所以刃面 = **一整块** blade_middle，外面只挂几段**收薄的边缘段**去完善轮廓；
   不再做 blade_bulge / blade_step_* 那种"每段都是一个凸起金属块"的做法 ——
   45° 视角下会露出一排台阶厚度，侧看像拼装积木（规格明确点出的问题）。
   轮廓（每侧 x 绝对值，自上而下）：-7.05(翼) → -9.4 → **-10.2**(中段最鼓) → -9.4 → -7.05(翼) */
addBox(blade, 'blade_middle', [-9.60, 12.1, -1.40], [8.20, 5.9, 2.80], 'iron', true);
addBox(bladeRight, 'blade_middle_r', [1.40, 12.55, -1.40], [2.90, 5.00, 2.80], 'iron', true);
addBox(blade, 'edge_middle', [-11.90, 14.0, -0.60], [2.30, 2.0, 1.20], 'edge');
addBox(bladeRight, 'edge_middle_r', [4.30, 13.65, -0.60], [1.40, 2.60, 1.20], 'edge');
addBox(blade, 'edge_middle_up', [-10.60, 15.8, -0.55], [1.00, 1.5, 1.10], 'edge');
addBox(bladeRight, 'edge_middle_up_r', [3.90, 16.25, -0.55], [1.10, 1.30, 1.10], 'edge');
addBox(blade, 'edge_middle_dn', [-10.60, 12.7, -0.55], [1.00, 1.5, 1.10], 'edge');
addBox(bladeRight, 'edge_middle_dn_r', [3.90, 12.55, -0.55], [1.10, 1.20, 1.10], 'edge');
/* 强化脊：贴着斧根、向外收窄（规格：中央结构收紧 + 向外逐渐收窄），不再是等宽大方块 */
/* ★ 2026-09-16 用户实测报「静态无展开下斧中心有贴图叠加」= **共面 z-fighting**：
   中脊原来和斧眼 head_body 同为 z ±0.95 → 两个面完全重合，必然闪。
   现在中脊略微**凸出**到 ±1.02（读起来是凸起的加强肋），嵌芯再往外挪 0.1，
   三层之间都拉开 ≥0.05 的间距。 */
addPairBox(blade, bladeRight, 'reinforced_spine', [-2.85, 12.9, -1.90], [1.35, 4.4, 3.80], 'dark', true);
addPairBox(blade, bladeRight, 'reinforced_spine_tip', [-4.15, 13.6, -1.85], [1.30, 3.0, 3.70], 'dark');
/* 刃翼"静态背板"（2026-09-15 补，为修 cleaver_check / wing_joint_check 报的关节裂缝）：
   两片刃翼绕 x=-1.55 的枢轴转开时，它们与静止 blade_middle 原本只在 y 上重叠 0.2 单位 →
   外侧会抬起（8° 抬 0.55 单位、12° 抬 0.92 单位），侧视轮廓上**破一条能透背景的缝**
   （实测最宽处在 x≈-6.9，也就是刃尖那一头）。
   修法 = 规格里说的"轴盖"，但做成**合拢时完全隐形**的背板：
     · 挂在静止的 blade 骨骼上（跟着转的零件填不了缝，这一点试过 —— 铰链舌会一起转走）；
     · z ±0.22，比刃翼刃口(±0.34)与刃翼(±0.78)都窄 → 合拢时被完全挡住，轮廓零变化；
     · y 覆盖到 19.3 / 10.8，比最大抬起量还多留余量 → 12° 全开也补得住；
     · x 收到 -1.50 / -6.95（都在 blade_middle 与 edge_middle 体内），不产生共面。
   展开时它是"退后 0.56 单位的凹槽"而不是"洞"，读起来就是铰链座。
   ★ v6：刃翼高度加到 3.0（合拢时背板依旧完全藏在刃翼/刃口/中刃体内），
     所以背板也相应加高到 3.0，覆盖抬起量（16° 时 1.49）绰绰有余。 */
addBox(blade, 'wing_upper_backing', [-9.00, 16.60, -0.20], [7.50, 3.00, 0.40], 'blood');
addBox(bladeRight, 'wing_upper_backing_r', [1.40, 16.85, -0.20], [2.30, 1.95, 0.40], 'blood');
addBox(blade, 'wing_lower_backing', [-9.00, 10.40, -0.20], [7.50, 3.00, 0.40], 'blood');
addBox(bladeRight, 'wing_lower_backing_r', [1.40, 11.45, -0.20], [2.30, 1.85, 0.40], 'blood');
/* ★ v5：右侧短钩的**几何**已按规格移除（改成完整右刃），但**骨骼保留**（空的 rear_hook），
   避免任何按名字找它的旧引用失效。 */
if (rear.cubes) delete rear.cubes;
addBox(core, 'core_front', [-0.36, 13.5, -2.00], [0.72, 3.0, 0.42], 'ember');
addBox(core, 'core_back', [-0.36, 13.5, 1.58], [0.72, 3.0, 0.42], 'ember');
addBox(pommel, 'pommel_body', [-1.15, -2.80, -1.15], [2.3, 1.35, 2.3], 'iron');
addBox(pommel, 'pommel_tip', [-0.8, -3.65, -0.8], [1.6, 1.05, 1.6], 'dark');

/* ---------- 简易绘图工具 ---------- */
const line = (cv, x0, y0, x1, y1, c) => {
  x0 = Math.round(x0); y0 = Math.round(y0); x1 = Math.round(x1); y1 = Math.round(y1);
  const dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
  const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  let err = dx - dy;
  for (;;) { cv.set(x0, y0, c[0], c[1], c[2], (c[3] === undefined ? 255 : c[3]) / 255);
    if (x0 === x1 && y0 === y1) break;
    const e2 = 2 * err; if (e2 > -dy) { err -= dy; x0 += sx; } if (e2 < dx) { err += dx; y0 += sy; } }
};
const oval = (cv, cx, cy, rx, ry, c, steps = 256) => {
  let px = cx + rx, py = cy;
  for (let i = 1; i <= steps; i++) { const a = i / steps * Math.PI * 2;
    const x = cx + Math.cos(a) * rx, y = cy + Math.sin(a) * ry; line(cv, px, py, x, y, c); px = x; py = y; }
};
const rectOutline = (cv, x0, y0, x1, y1, c) => { line(cv, x0, y0, x1, y0, c); line(cv, x1, y0, x1, y1, c); line(cv, x1, y1, x0, y1, c); line(cv, x0, y1, x0, y0, c); };

/* ---------- 逐面绘制 ---------- */
let seed = 3100;
function mulberry(s) { return () => { s |= 0; s = s + 0x6D2B79F5 | 0; let t = Math.imul(s ^ s >>> 15, 1 | s); t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t; return ((t ^ t >>> 14) >>> 0) / 4294967296; }; }
function paintFace(job) {
  const w = job.w, h = job.h, base = shade(COLORS[job.material], LIGHT[job.direction]);
  const cv = new Canvas(w, h);
  for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) cv.set(x, y, base[0], base[1], base[2], 1);
  const rnd = mulberry(seed++);
  /* ★ 2026-09-16 §4 贴图精修：**大块暗面 + 少量磨损**，不再满面随机噪点。
     原来这里对非专用材质撒了 w*h/110 条 3 像素亮线 —— 规格点名"减少细碎噪声和孤立像素"，
     而且纹素密度从 3 降到 2 之后这些小点更容易变成雪花。现在只放 2~3 处**明确的磨损块**。 */
  const mirrored = String(job.label || '').endsWith('_r');
  if (job.material === 'leather') {
    /* 皮革缠带：条纹周期 8 → **12**，做出"清楚的大块节奏"（规格 §4.5） */
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
      const stripe = (x + y) % 12;
      if (stripe === 0) cv.set(x, y, ...shade(base, 1.30), 1);
      else if (stripe <= 2) cv.set(x, y, ...shade(base, 0.70), 1);
    }
  } else if (job.material === 'edge') {
    /* 刃口：外缘一条连续冷灰亮边 + 一道暗坡（规格"弧形刃口保持连续亮边"）。
       亮边放在**靠外那一列**：镜像件的 u 轴是反的，所以要跟着翻。 */
    const bright = mirrored ? w - 2 : 1;
    const bevel = mirrored ? w - 3 : 2;
    if (w > 2) line(cv, bright, 0, bright, h - 1, shade(base, 1.38));
    if (w > 4) line(cv, bevel, 0, bevel, h - 1, shade(base, 0.74));
    /* 局部一两处缺口（规格 §4 材质表："局部一两处缺口"）*/
    if (w > 4 && h > 6) {
      const gy = 2 + Math.floor(rnd() * Math.max(1, h - 5));
      cv.set(bright, gy, ...shade(base, 0.62), 1);
      if (h > 10) cv.set(bright, (gy + Math.floor(h / 2)) % h, ...shade(base, 0.70), 1);
    }
  } else if (job.material === 'ember') {
    if (w > 2 && h > 2) {
      for (let y = 1; y <= h - 2; y++) for (let x = 1; x <= w - 2; x++) cv.set(x, y, 179, 38, 38, 1);
      line(cv, Math.floor(w / 2), 1, Math.floor(w / 2), h - 2, [241, 109, 64, 255]);
      cv.set(Math.floor(w / 2), 1, 255, 188, 109, 1);
    }
  } else if (job.material === 'blood') {
    /* 刃根缝：整片暗红 + 缝口一列略亮（合拢时看不见，张开才透出来）*/
    line(cv, mirrored ? w - 1 : 0, 0, mirrored ? w - 1 : 0, h - 1, shade(base, 1.25));
  } else {
    /* 黑铁 / 深色：大块暗面 + **2~3 处明确磨损**（2×2 的块，不是 1 像素噪点）*/
    const wear = 2 + Math.floor(rnd() * 2);
    for (let i = 0; i < wear; i++) {
      const bx = 1 + Math.floor(rnd() * Math.max(1, w - 3));
      const by = 1 + Math.floor(rnd() * Math.max(1, h - 3));
      const light = rnd() < 0.45;
      const c = shade(base, light ? 1.20 : 0.78);
      for (let yy = 0; yy < 2 && by + yy < h; yy++)
        for (let xx = 0; xx < 2 && bx + xx < w; xx++) cv.set(bx + xx, by + yy, c[0], c[1], c[2], 1);
    }
    /* 上下缘仍保留一道受光/背光的边，保证体块感 */
    line(cv, 0, 0, w - 1, 0, shade(base, 1.16));
  }
  /* 裂纹：**从刃根向外**延伸（规格 §4.3），且只走约 2/3 的长度 —— 不铺满整个刃面。
     刃根在哪一侧取决于镜像（右刃的 u 轴是反的），所以起点跟着翻。 */
  if (job.cracks && (job.direction === 'north' || job.direction === 'south')) {
    const rootX = mirrored ? w - 2 : 1;
    const dirX = mirrored ? -1 : 1;
    const span = Math.max(3, Math.floor(w * 0.66));
    const pts = [];
    let x = rootX;
    const stepX = Math.max(1, Math.floor(span / 6));
    for (let i = 0; i <= span; i += stepX) {
      const d = [rnd(), rnd(), rnd()];
      const dy = d[0] < 0.34 ? -1 : (d[0] < 0.67 ? 0 : 1);
      const y = Math.max(1, Math.min(h - 2, Math.floor(h / 2) + dy * (1 + Math.floor(rnd() * 2))));
      pts.push([x, y]);
      x = Math.max(1, Math.min(w - 2, x + dirX * stepX));
    }
    for (let i = 1; i < pts.length; i++) line(cv, pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1], [129, 29, 34, 255]);
    /* 越靠刃根越亮（规格 §5「亮度集中在根部与嵌芯」），向外逐渐变暗 */
    for (let i = 1; i < pts.length; i++) {
      const t = i / pts.length;
      const [px, py] = pts[i];
      if (t < 0.5) cv.set(px, py, 197, 57, 42, 1);
      else if (t < 0.8) cv.set(px, py, 150, 40, 40, 1);
      else cv.set(px, py, 108, 30, 34, 1);
    }
  }
  return cv;
}

/* ★ 2026-09-16 用户「放大后重做贴图来适配」：**全局等比放大**（真·巨斧体量）。
   放在 UV 打包**之前**，所以图集会按放大后的每个面重算尺寸 —— 纹素密度自动保持，
   这正是"重做贴图来适配"；不需要手工补偿，也不会出现"面变大了贴图还是旧的"那种拉伸。
   所有已验几何约束都是尺度无关的（关节抬起量 ≤ 翼高、共面间距、密度比、宽高比），放大后依然成立。
   握持原点 grip_anchor、父级链、骨骼名与 pivot 相对位置一律不变。 */
const GIANT = 1.35;
for (const b of bones) {
  if (b.pivot) b.pivot = b.pivot.map(v => +(v * GIANT).toFixed(3));
  for (const c of b.cubes || []) {
    c.origin = c.origin.map(v => +(v * GIANT).toFixed(3));
    c.size = c.size.map(v => +(v * GIANT).toFixed(3));
  }
}
/* ---------- UV 打包 ---------- */
jobs.sort((a, b) => (b.h - a.h) || (b.w - a.w));
let cx = 0, cy = 0, rowH = 0;
const emissiveRegions = [];
for (const job of jobs) {
  const pw = job.w + PAD * 2, ph = job.h + PAD * 2;
  if (cx + pw > SIZE) { cx = 0; cy += rowH; rowH = 0; }
  if (cy + ph > SIZE) throw new Error('UV 图集空间不足');
  const tile = paintFace(job);
  for (let y = 0; y < ph; y++) for (let x = 0; x < pw; x++) {
    const sx = Math.max(0, Math.min(job.w - 1, x - PAD)), sy = Math.max(0, Math.min(job.h - 1, y - PAD));
    const i = (sy * job.w + sx) * 4;
    atlas.set(cx + x, cy + y, tile.d[i], tile.d[i + 1], tile.d[i + 2], tile.d[i + 3] / 255);
  }
  const left = cx + PAD, top = cy + PAD;
  job.cube.uv[job.direction] = { uv: [left, top], uv_size: [job.w, job.h] };
  if (job.material === 'ember') emissiveRegions.push([left, top, job.w, job.h]);
  cx += pw; rowH = Math.max(rowH, ph);
}
atlas.save(path.join(RES, 'textures/item/black_cleaver_geo.png'));
const emissive = new Canvas(SIZE, SIZE);
for (const [x, y, w, h] of emissiveRegions) for (let yy = 0; yy < h; yy++) for (let xx = 0; xx < w; xx++) {
  const i = ((y + yy) * SIZE + (x + xx)) * 4;
  emissive.set(x + xx, y + yy, atlas.d[i], atlas.d[i + 1], atlas.d[i + 2], 1);
}
emissive.save(path.join(RES, 'textures/item/black_cleaver_emissive.png'));
const big = new Canvas(SIZE * 3, SIZE * 3);
for (let y = 0; y < SIZE * 3; y++) for (let x = 0; x < SIZE * 3; x++) { const i = ((Math.floor(y / 3)) * SIZE + Math.floor(x / 3)) * 4; big.set(x, y, atlas.d[i], atlas.d[i + 1], atlas.d[i + 2], 1); }
big.save(path.join(OUT_ART, 'black_cleaver_uv_preview.png'));

/* ---------- geo ---------- */
fs.writeFileSync(path.join(RES, 'geo/black_cleaver.geo.json'), JSON.stringify({
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: { identifier: 'geometry.black_cleaver', texture_width: SIZE, texture_height: SIZE, visible_bounds_width: 5, visible_bounds_height: 5, visible_bounds_offset: [0, 0.4, 0] },
    bones,
  }],
}, null, 2));

/* ---------- 动画 ---------- */
const coreScale = keys => Object.fromEntries(keys.map(([t, x, y]) => [String(t), [x, y, 1]]));
const animations = {
  'animation.black_cleaver.idle': { loop: true, animation_length: 3, bones: {
    weapon: { rotation: [0, 0, 0], position: [0, 0, 0], scale: [1, 1, 1] },
    ember_core: { scale: coreScale([[0, 1, 1], [1.5, 1.015, 1.02], [3, 1, 1]]) } } },
  'animation.black_cleaver.charge': { loop: false, animation_length: 0.4, bones: {
    weapon: { rotation: [0, 0, 0], position: [0, 0, 0] },
    ember_core: { scale: coreScale([[0, 1, 1], [0.15, 0.88, 0.95], [0.3, 1.04, 1.04], [0.4, 1, 1]]) } } },
  'animation.black_cleaver.slam': { loop: false, animation_length: 0.65, bones: {
    weapon: { position: [0, 0, 0], rotation: { '0.0': [0, 0, 0], '0.15': [-18, 0, -8], '0.28': [-28, 0, -12], '0.38': [32, 0, 8], '0.46': [38, 0, 10], '0.65': [12, 0, 3] } },
    ember_core: { scale: coreScale([[0, 1, 1], [0.28, 0.92, 0.96], [0.38, 1.06, 1.04], [0.5, 1.02, 1.02], [0.65, 1, 1]]) } } },
  'animation.black_cleaver.recover': { loop: false, animation_length: 0.3, bones: {
    weapon: { position: [0, 0, 0], rotation: { '0.0': [12, 0, 3], '0.3': [0, 0, 0] } },
    ember_core: { scale: [1, 1, 1] } } },
  'animation.black_cleaver.ready': { loop: false, animation_length: 0.45, bones: {
    ember_core: { scale: coreScale([[0, 1, 1], [0.12, 0.92, 0.95], [0.24, 1.06, 1.04], [0.45, 1, 1]]) } } },
  /* 血怒展开姿态（v5 双刃版）：**四片刃翼对称展开**，各 ±8°。
     符号左右相反 —— 展开后轮廓是"左上↖ / 右上↗ / 左下↙ / 右下↘"。
     ★ 仅供 Blockbench 预览，**不注册到游戏控制器**：实机由客户端 CleaverBloodVisualState
     直接 set 四个骨骼的旋转（每次从基础姿态 set、不累加）。 */
  'animation.black_cleaver.blood_pose': { loop: false, animation_length: 1, bones: {
    blade_upper_wing: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, -8] } },
    blade_lower_wing: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, 8] } },
    blade_right_upper_wing: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, 8] } },
    blade_right_lower_wing: { rotation: { '0.0': [0, 0, 0], '1.0': [0, 0, -8] } } } },
};
fs.writeFileSync(path.join(RES, 'animations/black_cleaver.animation.json'), JSON.stringify({ format_version: '1.8.0', animations }, null, 2));

/* ---------- 特效贴图 ---------- */
const FX = 128, C = (FX - 1) / 2;
const polar = (r, a) => [C + Math.cos(a) * r, C + Math.sin(a) * r];
const circ = new Canvas(FX, FX);
for (let s = 0; s < 360; s += 45) for (let a = s + 4; a <= s + 38; a += 2) {
  const p0 = polar(54.5, a * Math.PI / 180), p1 = polar(54.5, (a + 2) * Math.PI / 180); line(circ, p0[0], p0[1], p1[0], p1[1], [182, 37, 47, 225]);
}
oval(circ, C, C, 47.5, 47.5, [114, 27, 37, 190]);
for (let i = 0; i < 128; i++) { const a = i / 128 * Math.PI * 2; line(circ, C + Math.cos(a) * 24.5, C + Math.sin(a) * 24.5, C + Math.cos(a + 0.05) * 24.5, C + Math.sin(a + 0.05) * 24.5, [205, 66, 51, 230]); line(circ, C + Math.cos(a + 0.05) * 24.5, C + Math.sin(a + 0.05) * 24.5, C + Math.cos(a + 0.1) * 24.5, C + Math.sin(a + 0.1) * 24.5, [205, 66, 51, 230]); }
for (let i = 0; i < 4; i++) { const a = i * Math.PI / 2;
  const A = polar(62, a), B = polar(51, a - 0.10), D = polar(55, a), E = polar(51, a + 0.10);
  line(circ, A[0], A[1], B[0], B[1], [228, 82, 57, 240]); line(circ, B[0], B[1], D[0], D[1], [228, 82, 57, 240]); line(circ, D[0], D[1], E[0], E[1], [228, 82, 57, 240]); line(circ, E[0], E[1], A[0], A[1], [228, 82, 57, 240]); }
circ.save(path.join(RES, 'textures/effect/cleaver_slam_circle.png'));

const runes = new Canvas(FX, FX);
for (let i = 0; i < 8; i++) { const a = i * Math.PI / 4;
  const p0 = polar(31, a), p1 = polar(43, a), p2 = polar(37, a + 0.10), p3 = polar(37, a - 0.10);
  line(runes, p0[0], p0[1], p1[0], p1[1], [225, 73, 53, 235]);
  line(runes, p3[0], p3[1], p2[0], p2[1], [163, 32, 42, 225]); line(runes, p2[0], p2[1], p1[0], p1[1], [163, 32, 42, 225]); }
line(runes, 49, 64, 64, 56, [236, 96, 63, 245]); line(runes, 64, 56, 79, 64, [236, 96, 63, 245]);
line(runes, 79, 64, 64, 72, [236, 96, 63, 245]); line(runes, 64, 72, 49, 64, [236, 96, 63, 245]);
line(runes, 64, 58, 64, 70, [255, 164, 93, 255]); line(runes, 63, 58, 63, 70, [255, 164, 93, 255]);
runes.save(path.join(RES, 'textures/effect/cleaver_slam_runes.png'));

const wave = new Canvas(FX, FX);
oval(wave, C, C, 55.5, 55.5, [213, 57, 47, 225]);
oval(wave, C, C, 50.5, 50.5, [101, 25, 34, 120]);
wave.save(path.join(RES, 'textures/effect/cleaver_slam_wave.png'));

const column = new Canvas(32, 128);
for (let y = 0; y < 128; y++) {
  const env = Math.min(1, y / 12, (127 - y) / 9);
  const half = Math.max(0, Math.round((5 + (Math.floor(y / 12) % 3)) * env));
  const alpha = Math.round(190 * env);
  line(column, 16 - half, y, 16 + half, y, [87, 18, 30, alpha]);
  if (half >= 2) line(column, 15, y, 17, y, [210, 47, 39, Math.round(235 * env)]);
  if (half >= 4) column.set(16, y, 255, 132, 69, Math.round(250 * env) / 255);
}
column.save(path.join(RES, 'textures/effect/cleaver_slam_column.png'));

/* ---------- 血怒发光遮罩: 中央嵌芯 + 主刃裂纹 + 刃翼连接处红线 ---------- */
/* 依规格: 裂纹暗部 #651321 / 主红光 #C72D3D / 亮点 #FF715E。
   只画三处, 不新增悬浮光壳、不放大主体、不生成悬浮方块。
   ★ UV 方向依据: north/south/up/down 四个面的面内 u 轴都指向 **+x**
     (见 tools/model3d/hand_check.cjs 的 FACE_UV_ORDER: 各面"贴图左上"角点取的都是 x=0 那个角),
     而刃翼 x 范围 [-6.15, -1.40] 里**连接处在 x 最大那一侧**(枢轴 x=-1.55)，
     所以"靠近连接处"= 该面图集矩形的**右缘**（u 最大侧），不会画到刃尖去。 */
const BLOOD_DARK = [0x65, 0x13, 0x21];
const BLOOD_MAIN = [0xC7, 0x2D, 0x3D];
const BLOOD_HOT = [0xFF, 0x71, 0x5E];
const blood = new Canvas(SIZE, SIZE);
/* 1) 中央嵌芯：与常规自发光同一批 ember 面区域，直接照抄图集像素 */
for (const [x, y, w, h] of emissiveRegions) for (let yy = 0; yy < h; yy++) for (let xx = 0; xx < w; xx++) {
  const i = ((y + yy) * SIZE + (x + xx)) * 4;
  blood.set(x + xx, y + yy, atlas.d[i], atlas.d[i + 1], atlas.d[i + 2], 1);
}
/* 面 -> 图集矩形（packing 阶段已把 uv/uv_size 写回 cube） */
const rectOf = (label, dir) => {
  const job = jobs.find(j => j.label === label && j.direction === dir);
  const r = job && job.cube.uv[dir];
  return r ? { x: r.uv[0], y: r.uv[1], w: r.uv_size[0], h: r.uv_size[1] } : null;
};
const rndB = mulberry(90210);
/* 左右两套刃的 label（v5 双刃：右侧镜像件的 label 带 _r 后缀） */
const BLADE_LABELS = ['blade_middle', 'blade_upper', 'blade_lower',
                      'blade_middle_r', 'blade_upper_r', 'blade_lower_r'];
/* 2) 主刃几条裂纹：左右各自的 blade_middle + 两片刃翼的 north/south 上画竖向折线 */
for (const label of BLADE_LABELS) {
  for (const dir of ['north', 'south']) {
    const r = rectOf(label, dir);
    if (!r || r.w < 4 || r.h < 4) continue;
    let px = Math.floor(r.w / 2), prev = null;
    const stepY = Math.max(1, Math.floor(r.h / 5));
    for (let yy = 0; yy < r.h; yy += stepY) {
      const here = [r.x + px, r.y + yy];
      if (prev) line(blood, prev[0], prev[1], here[0], here[1], [BLOOD_DARK[0], BLOOD_DARK[1], BLOOD_DARK[2], 255]);
      blood.set(here[0], here[1], BLOOD_MAIN[0], BLOOD_MAIN[1], BLOOD_MAIN[2], 1);
      prev = here;
      const d = rndB();
      px = Math.max(1, Math.min(r.w - 2, px + (d < 0.34 ? -1 : (d < 0.67 ? 0 : 1))));
    }
    if (prev) blood.set(prev[0], prev[1], BLOOD_HOT[0], BLOOD_HOT[1], BLOOD_HOT[2], 1);
  }
}
/* 3) 刃翼连接处红线：侧面/顶底面取**靠连接处那一列**，连接端盖整片点亮。
   ★ 镜像件的"连接处"在**另一头**：左翼刃 x∈[-6.15,-1.40] 的连接处是 x 最大侧（矩形右缘、east 端盖），
     右翼刃 x∈[1.40,6.15] 的连接处是 x 最小侧（矩形左缘、west 端盖）—— 见上面的 u 轴说明。 */
for (const label of ['blade_upper', 'blade_lower', 'blade_upper_r', 'blade_lower_r']) {
  const mirrored = label.endsWith('_r');
  for (const dir of ['north', 'south', 'up', 'down']) {
    const r = rectOf(label, dir);
    if (!r || r.w < 3) continue;
    const colDark = mirrored ? r.x : r.x + r.w - 2;
    const colMain = mirrored ? r.x + 1 : r.x + r.w - 1;
    for (let yy = 0; yy < r.h; yy++) {
      blood.set(colDark, r.y + yy, BLOOD_DARK[0], BLOOD_DARK[1], BLOOD_DARK[2], 1);
      blood.set(colMain, r.y + yy, BLOOD_MAIN[0], BLOOD_MAIN[1], BLOOD_MAIN[2], 1);
    }
  }
  const cap = rectOf(label, mirrored ? 'west' : 'east');
  if (cap) for (let yy = 0; yy < cap.h; yy++) for (let xx = 0; xx < cap.w; xx++) {
    const hot = (xx === cap.w - 1 || yy === 0);
    const c = hot ? BLOOD_HOT : BLOOD_MAIN;
    blood.set(cap.x + xx, cap.y + yy, c[0], c[1], c[2], 1);
  }
}
/* ★ 2026-09-16 分阶段发光（规格 §5「进入血怒：刃根接缝先亮，随后主裂纹增强」）：
   刃根缝写进**独立的第二张遮罩**，渲染器用两个发光层分别驱动 —— 缝的进度窗口靠前(0~0.35)、
   主裂纹靠后(0.30~1.0)，于是展开时先红缝、再裂纹亮起。
   （规格：不支持遮罩阈值就用"少量分区遮罩"，不为此新建 Shader 框架 ✓） */
const seamBlood = new Canvas(SIZE, SIZE);
for (const label of ['wing_upper_backing', 'wing_lower_backing',
                     'wing_upper_backing_r', 'wing_lower_backing_r']) {
  for (const dir of ['north', 'south']) {
    const r = rectOf(label, dir);
    if (!r) continue;
    for (let yy = 0; yy < r.h; yy++) for (let xx = 0; xx < r.w; xx++) {
      /* 靠外那一列/两行用亮色，做出"缝里更深、缝口更亮"的层次 */
      const hot = (xx === r.w - 1 || yy === 0 || yy === r.h - 1);
      const c = hot ? BLOOD_MAIN : BLOOD_DARK;
      seamBlood.set(r.x + xx, r.y + yy, c[0], c[1], c[2], 1);
    }
  }
}
seamBlood.save(path.join(RES, 'textures/item/black_cleaver_blood_seam_emissive.png'));
blood.save(path.join(RES, 'textures/item/black_cleaver_blood_emissive.png'));

/* ---------- manifest ---------- */
fs.writeFileSync(path.join(OUT_REF, 'black_cleaver_manifest.json'), JSON.stringify({
  model: 'ftboss:geo/black_cleaver.geo.json',
  /* ★ 本 geo 的贴图是 256² 逐面 UV 图集 black_cleaver_geo.png。
     textures/item/black_cleaver.png 是 gen_weapons3d.cjs 出的 32×32 **平面物品图标**，
     被 models/item/black_cleaver.json 逐面 UV 引用 —— 任何生成器都不许写它。 */
  texture: 'ftboss:textures/item/black_cleaver_geo.png',
  flat_item_icon: 'ftboss:textures/item/black_cleaver.png',
  emissive_candidate: 'ftboss:textures/item/black_cleaver_emissive.png',
  blood_emissive_candidate: 'ftboss:textures/item/black_cleaver_blood_emissive.png',
  animations: Object.keys(animations),
  bones: bones.map(b => b.name),
  cubes: cubeTotal, quads: cubeTotal * 6, triangles_estimated: cubeTotal * 12,
  effect_design: { duration_ticks: 18, impact_tick: 5, circle_radius_blocks: 1.4, wave_max_radius_blocks: 1.8, column_height_blocks: 2.6 },
  notes: ['此清单是交付说明，不是 Minecraft 原生配置', '特效 PNG 需要客户端渲染代码接入',
          '自发光遮罩需要显式渲染层', 'slam 后衔接 recover，再回 idle', '变换和手持位置需 Blockbench 与游戏校准'],
}, null, 2));

let used = 0; for (let i = 3; i < atlas.d.length; i += 4) if (atlas.d[i] > 0) used++;
console.log('[black_cleaver] 骨骼=' + bones.length + '  立方体=' + cubeTotal + '  面=' + jobs.length);
console.log('[black_cleaver] 图集占用像素=' + used + '/' + (SIZE * SIZE) + '  图集游标 y=' + (cy + rowH));
console.log('[black_cleaver] 自发光区域=' + emissiveRegions.length);
console.log('[black_cleaver] 动画=' + Object.keys(animations).join(', '));
console.log('[black_cleaver] 已写入 geo / animations / item 贴图(+emissive) / 4 张特效贴图');
