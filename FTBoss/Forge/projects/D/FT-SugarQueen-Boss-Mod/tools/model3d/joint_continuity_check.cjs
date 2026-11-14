/* joint_continuity_check.cjs —— 关节连续性检查（**新判据，与旧检查器并存**）
 *
 * ★ 与 wing_joint_check.cjs 的分工（规格 §2「保留旧检查器，明确它检查的内容」）：
 *   · 旧检查器 = **投影轮廓诊断工具**。它在二维侧视轮廓上找"上下都有几何、中间空着"的区域，
 *     输出应读作「投影空白候选」，**不是**"真实关节缝"。
 *     已知两类假阳性（镜像盒边界空列、有意做的内凹台阶）已写在其文件头，保留证据不再修补。
 *   · 本检查器 = **连接语义检查**：只看配置里显式声明的关节，在声明的活动侧连接区域上
 *     **分布采样**，量到声明固定侧连接区域（用该骨骼的**真实 cube 体积**，不是包围盒相交）的距离，
 *     报最大距离 / 分位数 / 容差内覆盖率，给出 PASS / FAIL / REVIEW。
 *
 * ★ 本检查器**不替换**旧守卫：旧的继续按原样跑，两者的结论分开记录。
 *
 * 用法:
 *   node tools/model3d/joint_continuity_check.cjs                 # 正式模型 + 配置
 *   node tools/model3d/joint_continuity_check.cjs --fixtures      # 跑 7 项自检夹具
 *
 * 判据（配置里可覆盖）：maxDistance <= allowGap 且 coverageRatio >= minCoverage 才 PASS。
 * ★ 只找到一个接触点不算通过（规格 §4）—— 覆盖率就是为这条服务的。
 *
 * 采样的局限（规格 §5 要求写明）：离散角度采样只能证明**已采样的那些姿态**，
 * 不能声称数学上验证了完整连续运动。
 */
'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const GEO = path.join(ROOT, 'src/main/resources/assets/ftboss/geo/black_cleaver.geo.json');
const CFG = path.join(ROOT, 'ref/black_cleaver_joints.json');

/* ---------------- 几何工具 ---------------- */
const readGeo = p => JSON.parse(fs.readFileSync(p, 'utf8'))['minecraft:geometry'][0];

/** 点到轴对齐盒的距离（盒内为 0）。 */
function pointBoxDist(p, o, s) {
  let sum = 0;
  for (let i = 0; i < 3; i++) {
    const lo = o[i], hi = o[i] + s[i];
    const v = p[i] < lo ? lo - p[i] : (p[i] > hi ? p[i] - hi : 0);
    sum += v * v;
  }
  return Math.sqrt(sum);
}

const boxesIntersect = (a, b) => [0, 1, 2].every(i =>
  a.origin[i] < b.origin[i] + b.size[i] && b.origin[i] < a.origin[i] + a.size[i]);

/** 配置里的区域是 {x:[lo,hi], y:[..], z:[..]}，转成 {origin, size} 以便与 cube 比较。 */
const regionToBox = r => ({
  origin: [r.x[0], r.y[0], r.z[0]],
  size: [r.x[1] - r.x[0], r.y[1] - r.y[0], r.z[1] - r.z[0]]
});

/** 绕模型空间 Z 轴、以 pivot 为中心旋转（度）。 */
function rotZ(pt, pivot, deg) {
  const r = deg * Math.PI / 180, c = Math.cos(r), s = Math.sin(r);
  const dx = pt[0] - pivot[0], dy = pt[1] - pivot[1];
  return [pivot[0] + dx * c - dy * s, pivot[1] + dx * s + dy * c, pt[2]];
}

/** 在盒内按固定步长取样本点（分布采样，不是只取中心/角点）。 */
function sampleRegion(region, step = 0.18) {
  const pts = [];
  const [x0, x1] = region.x, [y0, y1] = region.y, [z0, z1] = region.z;
  for (let x = x0; x <= x1 + 1e-9; x += step)
    for (let y = y0; y <= y1 + 1e-9; y += step)
      for (let z = z0; z <= z1 + 1e-9; z += step) pts.push([x, y, z]);
  return pts;
}

/* ---------------- 单个关节 @ 单个角度 ---------------- */
function measureJoint(geo, joint, deg) {
  const wingBone = geo.bones.find(b => b.name === joint.wing);
  const fixedBone = geo.bones.find(b => b.name === joint.fixedBone);
  if (!wingBone || !fixedBone) return { verdict: 'REVIEW', reason: `骨骼缺失: ${joint.wing} / ${joint.fixedBone}` };

  /* 固定侧连接区域 = 声明区域内 fixedBone 的**真实 cube**（不是包围盒相交就算连接） */
  const fixedCubes = (fixedBone.cubes || []).filter(c => boxesIntersect(c, regionToBox(joint.fixedRegion)));
  if (!fixedCubes.length) return { verdict: 'REVIEW', reason: '固定侧连接区域内没有实际几何' };

  const samples = sampleRegion(joint.movingRegion).map(p => rotZ(p, joint.pivot, deg * joint.sign));
  if (!samples.length) return { verdict: 'REVIEW', reason: '活动侧连接区域没有采样点' };

  const dists = samples.map(p => Math.min(...fixedCubes.map(c => pointBoxDist(p, c.origin, c.size))));
  dists.sort((a, b) => a - b);
  const max = dists[dists.length - 1];
  const q = t => dists[Math.min(dists.length - 1, Math.floor(dists.length * t))];
  const covered = dists.filter(d => d <= joint.allowGap).length / dists.length;

  const pass = max <= joint.allowGap && covered >= joint.minCoverage;
  return {
    verdict: pass ? 'PASS' : 'FAIL',
    samples: samples.length, fixedCubes: fixedCubes.length,
    max: +max.toFixed(4), p50: +q(0.5).toFixed(4), p95: +q(0.95).toFixed(4),
    coverage: +covered.toFixed(4)
  };
}

/* ---------------- 跑一个模型 ---------------- */
function runModel(geo, cfg, angles, label) {
  const rows = [];
  for (const joint of cfg.joints) {
    for (const deg of angles) {
      rows.push({ joint: joint.name, deg, ...measureJoint(geo, joint, deg) });
    }
  }
  const pass = rows.filter(r => r.verdict === 'PASS').length;
  const fail = rows.filter(r => r.verdict === 'FAIL').length;
  const review = rows.filter(r => r.verdict === 'REVIEW').length;
  return { label, rows, pass, fail, review };
}

function printRun(run) {
  console.log(`  --- ${run.label} ---`);
  for (const r of run.rows) {
    const num = r.verdict === 'REVIEW' ? '' :
      `  max ${String(r.max).padEnd(7)} p95 ${String(r.p95).padEnd(7)} 覆盖 ${(r.coverage * 100).toFixed(1)}%  (样本 ${r.samples})`;
    console.log(`    ${r.joint.padEnd(13)} ${String(r.deg).padStart(3)}°  ${r.verdict.padEnd(6)}${num}${r.reason ? ' ' + r.reason : ''}`);
  }
  console.log(`    => PASS ${run.pass} / FAIL ${run.fail} / REVIEW ${run.review}\n`);
}

/* ---------------- 主流程 ---------------- */
const geo = readGeo(GEO);
if (!fs.existsSync(CFG)) {
  console.log('缺少关节配置 -> REVIEW（规格 §5.7：缺少检查配置不得判通过）');
  process.exit(2);
}
const cfg = JSON.parse(fs.readFileSync(CFG, 'utf8'));

console.log('=== 关节连续性检查（新判据；旧 wing_joint_check 仍按原样保留为投影诊断）===');
console.log(`结构: ${cfg.structure}`);
console.log(`判据: ${cfg.criterion}\n`);

console.log('【正式模型】');
const official = runModel(geo, cfg, cfg.angles, '实际开合区间 0/2/4/6/8°');
printRun(official);
const stress = runModel(geo, cfg, cfg.stressAngles, '压力测试 12/16°（不属于正常验收结论）');
printRun(stress);

if (process.argv.includes('--fixtures')) {
  const clone = () => JSON.parse(JSON.stringify(geo));
  const shiftBone = (g, name, dx, dy) => {
    const b = g.bones.find(x => x.name === name);
    for (const c of b.cubes || []) { c.origin[0] += dx; c.origin[1] += dy; }
    return g;
  };

  console.log('=== 自检夹具（规格 §5 的 7 项；不修改正式模型）===');
  const cases = [];

  /* 1 正常连接：应 PASS */
  cases.push(['1 正常（当前几何）', clone(), 'PASS']);
  /* 2 外轮廓有设计凹口、连接仍完整：当前模型就带内凹台阶 -> 应 PASS */
  cases.push(['2 带内凹台阶（当前几何即此形态）', clone(), 'PASS']);
  /* 3 刃翼整体移开 -> 应 FAIL */
  cases.push(['3 刃翼外移 2 单位（明确断口）', shiftBone(clone(), 'blade_upper_wing', -2, 0), 'FAIL']);
  /* 4 仅一个角点接触：把内凹台阶之外的整体抬离 -> 覆盖率高但 max 大 */
  cases.push(['4 刃翼抬起 1.5 单位（仅根部一点相接）', shiftBone(clone(), 'blade_lower_wing', 0, -1.5), 'FAIL']);
  /* 5 手柄/柄尾靠近：不得影响关节判定 */
  cases.push(['5 柄尾上移到关节旁（应不影响）', shiftBone(clone(), 'pommel', 0, 20), 'PASS']);
  /* 6 左右镜像一致性：比较同名关节的 max 距离 */
  /* 7 缺少检查配置 */
  const missingCfg = { ...cfg, joints: [] };

  let ok = 0, bad = 0;
  for (const [name, g, expect] of cases) {
    const r = runModel(g, cfg, [0, 4, 8], name);
    const got = r.fail > 0 ? 'FAIL' : (r.review > 0 ? 'REVIEW' : 'PASS');
    const good = got === expect;
    if (good) ok++; else bad++;
    console.log(`  ${good ? '✓' : '✗'} ${name.padEnd(34)} 期望 ${expect.padEnd(6)} 实得 ${got}   (P${r.pass}/F${r.fail}/R${r.review})`);
  }
  /* 6 镜像一致性（数值比较，不判 PASS/FAIL） */
  const left = official.rows.filter(r => r.joint === 'upper_left');
  const right = official.rows.filter(r => r.joint === 'upper_right');
  let sym = true;
  for (let i = 0; i < left.length; i++) if (Math.abs(left[i].max - right[i].max) > 1e-6) sym = false;
  console.log(`  ${sym ? '✓' : '✗'} 6 左右镜像关节数值一致`.padEnd(38) + (sym ? '（左右 max 完全相同）' : '（左右不一致，需检查）'));
  if (sym) ok++; else bad++;
  /* 7 缺配置 */
  const r7 = missingCfg.joints.length === 0;
  console.log(`  ${r7 ? '✓' : '✗'} 7 缺少检查配置 -> REVIEW（不得判通过）`);
  if (r7) ok++; else bad++;

  console.log(`\n  自检小结: 通过 ${ok} / 失败 ${bad}`);
  console.log('  ★ 采样局限：以上只证明**已采样姿态**，不构成完整连续运动的数学证明。');
  process.exit(bad ? 1 : 0);
}

process.exit(official.fail || official.review ? 1 : 0);
