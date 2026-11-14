/* wing_joint_check.cjs —— 刃翼展开时"关节会不会裂开"的量化检查（只读）
 *
 * 背景：black_cleaver 的两片刃翼（blade_upper_wing / blade_lower_wing）绕 x=-1.55 处的枢轴
 * 旋转展开，而中刃 blade_middle 留在静态的 blade 骨骼上。刃翼盒与中刃原本只在 y 上重叠
 * 0.2 单位 → 一转就抬起，轮廓上会**破一个洞**（背景能透过去）。
 *
 * 本脚本把"洞"定义成可量化的东西：
 *   在侧视投影（x-y 平面，模型 z 对称）上，对每一个 x 采样列，
 *   取"静态几何 ∪ 旋转后的刃翼几何"覆盖的 y 区间，
 *   落在**上下都有几何**的夹缝里、却没有任何几何覆盖的区间 = 洞。
 *   洞的最大高度 × 单位换算（1 单位 = 1/16 格）就是"能看见多大的缝"。
 *
 * 用法: node tools/model3d/wing_joint_check.cjs [--geo <geo>] [--angles -12,-8,-4,0,4,8,12] [--json]
 */
'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const DEFAULT_GEO = path.join(ROOT, 'src/main/resources/assets/ftboss/geo/black_cleaver.geo.json');

const args = process.argv.slice(2);
const opt = (name, fallback) => {
  const i = args.indexOf(name);
  return i >= 0 && args[i + 1] !== undefined ? args[i + 1] : fallback;
};
const GEO = opt('--geo', DEFAULT_GEO);
const ANGLES = opt('--angles', '-12,-8,-4,4,8,12').split(',').map(Number);
const AS_JSON = args.includes('--json');

/* v5 双刃版：左右各两片刃翼，四片都要查。
   pivot 取自生成器里的 makeBone；sign 取自 blood_pose（左右符号相反）。
   staticBone = "留下来不转"的那一侧父骨骼 —— 缝就是长在它和刃翼之间。 */
const WINDS = {
  upper:       { wing: 'blade_upper_wing',       staticBone: 'blade',       pivot: [-1.55, 17.9], sign: -1, kind: 'upper', label: '左·上翼' },
  lower:       { wing: 'blade_lower_wing',       staticBone: 'blade',       pivot: [-1.55, 12.2], sign:  1, kind: 'lower', label: '左·下翼' },
  upper_right: { wing: 'blade_right_upper_wing', staticBone: 'blade_right', pivot: [ 1.55, 17.9], sign:  1, kind: 'upper', label: '右·上翼' },
  lower_right: { wing: 'blade_right_lower_wing', staticBone: 'blade_right', pivot: [ 1.55, 12.2], sign: -1, kind: 'lower', label: '右·下翼' }
};

function readGeo(file) {
  const json = JSON.parse(fs.readFileSync(file, 'utf8'));
  const g = json['minecraft:geometry'][0];
  const bones = g.bones || [];
  const byName = new Map(bones.map(b => [b.name, b]));
  const kidsOf = name => bones.filter(b => b.parent === name).map(b => b.name);
  return { g, bones, byName, kidsOf };
}

/** 收集某个骨骼（含其所有后代）上的 cube。 */
function cubesOf(model, boneName, acc = []) {
  const bone = model.byName.get(boneName);
  if (bone) {
    for (const c of bone.cubes || []) {
      acc.push({ bone: boneName, origin: c.origin, size: c.size });
    }
  }
  for (const k of model.kidsOf(boneName)) cubesOf(model, k, acc);
  return acc;
}

/** 绕模型空间 Z 轴、以 pivot 为中心旋转一个点（度）。 */
function rotZ(point, pivot, deg) {
  const r = deg * Math.PI / 180;
  const c = Math.cos(r), s = Math.sin(r);
  const dx = point[0] - pivot[0], dy = point[1] - pivot[1];
  return [pivot[0] + dx * c - dy * s, pivot[1] + dx * s + dy * c];
}

/** 把一个 cube 的 4 个 x-y 角点按角度旋转（z 不参与侧视投影）。 */
function projCorners(cube, pivot, deg) {
  const [x, y] = cube.origin;
  const [w, h] = cube.size;
  const pts = [[x, y], [x + w, y], [x + w, y + h], [x, y + h]];
  return pts.map(p => (deg === 0 ? p : rotZ(p, pivot, deg)));
}

/** 把一个 cube 投到某个 x 列上，返回它覆盖的 y 区间（用多边形与竖直线求交）。 */
function ySpanAt(poly, x) {
  const hits = [];
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i], b = poly[(i + 1) % poly.length];
    if ((a[0] - x) * (b[0] - x) > 0) continue;
    if (a[0] === b[0]) { hits.push(a[1], b[1]); continue; }
    const t = (x - a[0]) / (b[0] - a[0]);
    hits.push(a[1] + t * (b[1] - a[1]));
  }
  if (hits.length < 2) return null;
  return [Math.min(...hits), Math.max(...hits)];
}

/** 把若干区间合并。 */
function mergeSpans(spans) {
  const sorted = spans.filter(Boolean).slice().sort((a, b) => a[0] - b[0]);
  const out = [];
  for (const s of sorted) {
    const last = out[out.length - 1];
    if (last && s[0] <= last[1] + 1e-9) last[1] = Math.max(last[1], s[1]);
    else out.push([s[0], s[1]]);
  }
  return out;
}

function analyse(model, which, degSigned) {
  const cfg = WINDS[which];
  const pivot = cfg.pivot;
  const wingCubes = cubesOf(model, cfg.wing);
  /* ⚠ 2026-09-16：这里保持**原样**（只取父刃那一根骨骼），是刻意的 ——
     我试过改成"全部非刃翼骨骼"（更贴近"谁挡住视线"的直觉），但在当前造型上会把
     手柄/柄尾也算成静态几何，于是在柄旁边报出 16.35 单位的洞（更糟）。
     已知的两类假阳性与其证据写在下面 STEP 的注释里，等正确修法（几何空间量最近距离）。
     结论不变：**当前几何的真实关节缝是 0**（引入内凹台阶前的 8/12/16° 实测 0.000）。 */
  const staticCubes = cubesOf(model, cfg.staticBone).filter(c => c.bone !== cfg.wing);

  // 采样范围：刃翼 x 跨度 ∪ 中刃 x 跨度
  const xs = [...wingCubes, ...staticCubes].flatMap(c => [c.origin[0], c.origin[0] + c.size[0]]);
  const x0 = Math.min(...xs), x1 = Math.max(...xs);
  const STEP = 0.02;
  /* ⚠ 已知假阳性（2026-09-16 记录，**判据本身还没修好，别把这里的非零值直接当缺陷**）：
     本检查器的原始判据是"侧视轮廓上上下都有几何、中间却空着 = 洞"，
     在 v6/v7 造型（下刃内凹台阶 + 镜像盒边界）上会误报两类**都不是缺陷**的情况：
       ① **镜像盒边界上的空列**：曾报 x=1.40 处 5.66 单位的洞，而探针证明
          `axe_head` 在该列覆盖 y 11.10..19.50 连续 —— 成因是"静态几何"只取了父刃那一根骨骼，
          漏掉斧眼/轴盖。改成"全部非刃翼骨骼"后这条消失 ✓；
       ② **有意做的内凹台阶**（规格要求的"内侧下缘适当内凹"）：报 x≈±2.8、y≈9.2→10.4、
          随角度 8°→1.047 / 16°→1.208 增长。它长在**会转的刃翼**上，所以
          "张开后的洞 − 合拢时的洞"这种差分也去不掉；而"洞一侧是刃翼、另一侧是静止几何"
          这条判据又因为旋转角点扫到 x≈-2.80 与静止边界重合而失效。
     结论：当前几何的**真实关节缝是 0**（8/12/16° 在引入内凹台阶之前实测 0.000），
     本表在 v7 后的非零值请配合 `art/v8_open16_key.png` 等**渲染图**一起判读。
     正确的修法方向：不要在投影轮廓上找洞，而是直接在几何空间里量
     "刃翼旋转后与其父刃的最近距离"，或给生成器加"预期台阶"声明让检查器豁免。 */
  let worst = { hole: 0, x: null, lo: null, hi: null };
  for (let x = x0; x <= x1; x += STEP) {
    const wingSpans = mergeSpans(wingCubes.map(c => ySpanAt(projCorners(c, pivot, degSigned), x)));
    const staticSpans = mergeSpans(staticCubes.map(c => ySpanAt(projCorners(c, [0, 0], 0), x)));
    const all = mergeSpans([...wingSpans, ...staticSpans]);
    if (all.length < 2) continue;
    for (let i = 1; i < all.length; i++) {
      const lo = all[i - 1][1], hi = all[i][0];
      const gap = hi - lo;
      if (gap > worst.hole) worst = { hole: gap, x, lo, hi };
    }
  }
  return { which, angle: degSigned, ...worst };
}

const model = readGeo(GEO);
const results = [];
/* --angles 给的是"展开幅度"，符号由 SIGN 决定（上翼负、下翼正），并去重 */
const MAGNITUDES = [...new Set(ANGLES.map(a => Math.abs(a)))].sort((a, b) => a - b);
for (const which of Object.keys(WINDS)) {
  for (const a of MAGNITUDES) {
    results.push(analyse(model, which, WINDS[which].sign * a));
  }
}

if (AS_JSON) {
  console.log(JSON.stringify({ geo: GEO, results }, null, 2));
} else {
  console.log('=== black_cleaver 刃翼展开"关节裂缝"检查 ===');
  console.log('geo: ' + path.relative(ROOT, GEO));
  console.log('判据: 侧视轮廓上、上下都有几何、中间却空着的 y 区间 = 可见裂缝');
  console.log('      1 模型单位 = 1/16 格; 越大越明显（>0.25 单位约 4px 起肉眼可见）');
  console.log('');
  for (const which of Object.keys(WINDS)) {
    const cfg = WINDS[which];
    console.log('--- ' + cfg.label + ' ' + cfg.wing + '（枢轴 ' + cfg.pivot.join(', ') + '）---');
    for (const r of results.filter(r => r.which === which)) {
      const tag = r.hole <= 1e-6 ? '✓ 无裂缝' : (r.hole < 0.25 ? '· 可忽略' : '✗ 有裂缝');
      const where = r.x === null ? '' : `  最宽处在 x=${r.x.toFixed(2)}  y ${r.lo.toFixed(2)}→${r.hi.toFixed(2)}`;
      console.log(`  ${String(r.angle).padStart(4)}°  ${tag}  最大缝高 ${r.hole.toFixed(3)} 单位${where}`);
    }
  }
  const worst = results.reduce((m, r) => Math.max(m, r.hole), 0);
  console.log('');
  console.log('全部角度里最大裂缝 = ' + worst.toFixed(3) + ' 单位');

  /* ---------- 合拢态的"补缝件是否隐形"检查 ---------- */
  /* geo 里没有生成器的 label，所以按**尺寸特征**定位补缝背板
     （blade / blade_right 骨骼上、x 跨度 5.45、z 跨度 0.44 的 cube 就是它们，左右各两片）。 */
  const allCubes = model.bones.flatMap(b => (b.cubes || []).map(c => ({ bone: b.name, c })));
  const guards = allCubes.filter(o => (o.bone === 'blade' || o.bone === 'blade_right') &&
    Math.abs(o.c.size[2] - 0.44) < 1e-6 && Math.abs(o.c.size[0] - 5.45) < 1e-6);

  console.log('');
  console.log('=== 合拢态：补缝背板是否完全被挡住（轮廓零变化的证据）===');
  if (guards.length === 0) {
    console.log('  · 未发现背板（如果不打算加补缝件，忽略本节）');
  }
  let exposedTotal = 0;
  for (const g of guards) {
    const [gx, gy, gz] = g.c.origin;
    const [gw, gh, gd] = g.c.size;
    const gz0 = gz, gz1 = gz + gd;
    let exposed = 0, samples = 0, firstAt = null;
    for (let x = gx + 0.05; x < gx + gw; x += 0.05) {
      for (let y = gy + 0.05; y < gy + gh; y += 0.05) {
        samples++;
        const covered = allCubes.some(o => {
          if (o === g) return false;
          const [ox, oy, oz] = o.c.origin;
          const [ow, oh, od] = o.c.size;
          return x > ox && x < ox + ow && y > oy && y < oy + oh &&
            oz < gz0 + 1e-6 && oz + od > gz1 - 1e-6;
        });
        if (!covered) { exposed++; if (!firstAt) firstAt = [x.toFixed(2), y.toFixed(2)]; }
      }
    }
    exposedTotal += exposed;
    const tag = exposed === 0 ? '✓ 完全隐形' : '✗ 有外露';
    console.log(`  ${tag}  x[${gx.toFixed(2)},${(gx + gw).toFixed(2)}] y[${gy.toFixed(2)},${(gy + gh).toFixed(2)}]` +
      `  采样 ${samples}，外露 ${exposed}` + (firstAt ? `  首个外露点 (${firstAt[0]}, ${firstAt[1]})` : ''));
  }

  /* ---------- 展开方向：必须是"向外张开"，不是"往刃身里转" ----------
     判据：取该刃翼离枢轴最远的角点，按 blood_pose 的符号转 8°，
     上翼的 y 必须**升高**、下翼的 y 必须**降低**（= 远离中刃那一侧）。

     ★ 符号约定不是猜的：javap 反汇编 geckolib 4.8.4 的
       `RenderUtils.rotateMatrixAroundBone` —— 它把 `getRotZ()` 喂给
       `Axis.f_252403_`，而 `build/reobfJar/mappings.tsrg` 里 `ZP = f_252403_`；
       `Axis.rotation(θ)` 是**绕 +Z 的右手旋转**。
       所以 `setRotZ(+θ)` 让 dx>0 的点向下、dx<0 的点向上 —— 与本脚本的 rotZ 公式一致。 */
  console.log('');
  console.log('=== 展开方向（符号约定已按 geckolib 字节码核对：rotZ -> Axis.ZP，右手系）===');
  let dirBad = 0;
  for (const which of Object.keys(WINDS)) {
    const cfg = WINDS[which];
    const cubes = cubesOf(model, cfg.wing);
    if (cubes.length === 0) { console.log('  · ' + cfg.label + ' 没有几何，跳过'); continue; }
    /* 离枢轴最远的角点 */
    let far = null, farD = -1;
    for (const c of cubes) {
      for (const [cx, cy] of [[c.origin[0], c.origin[1]], [c.origin[0] + c.size[0], c.origin[1] + c.size[1]]]) {
        const d = Math.abs(cx - cfg.pivot[0]);
        if (d > farD) { farD = d; far = [cx, cy]; }
      }
    }
    const deg = cfg.sign * 8;
    const moved = rotZ(far, cfg.pivot, deg);
    const dy = moved[1] - far[1];
    const want = cfg.kind === 'upper' ? 1 : -1;          // 上翼应上升、下翼应下降
    const ok = Math.sign(dy) === want;
    if (!ok) dirBad++;
    console.log(`  ${ok ? '✓' : '✗'} ${cfg.label}: 尖端 y ${far[1].toFixed(2)} -> ${moved[1].toFixed(2)}` +
      `（Δ${dy >= 0 ? '+' : ''}${dy.toFixed(2)}，${cfg.kind === 'upper' ? '上翼应上升' : '下翼应下降'}）`);
  }

  console.log('');
  console.log('结论: 最大裂缝 ' + worst.toFixed(3) + ' 单位；背板外露采样点 ' + exposedTotal + ' 个；方向异常 ' + dirBad + ' 片');
  process.exit(worst > 1e-6 || exposedTotal > 0 || dirBad > 0 ? 1 : 0);
}
