/* rebuild_joint_gap.cjs —— 刃翼展开"关节裂缝"检查（**用 geo 里的真实 pivot**）
 *
 * 为什么需要它：工程自带的 tools/model3d/wing_joint_check.cjs 把正式版 black_cleaver 的
 * 枢轴**写死**在文件里：
 *     upper: pivot [-1.55, 17.9]   lower: pivot [-1.55, 12.2]
 *     upper_right: [1.55,17.9]     lower_right: [1.55,12.2]
 * 它还按"上翼为负、下翼为正"的固定符号旋转。重建版的四个枢轴是
 *     [-1.811, 16.657] / [-1.973, 7.386] / [3.355, 10.164] / [-1.312, 7.535]
 * 符号也不是那套 —— 所以直接拿它跑重建版，等于**绕错误的点旋转别人的几何**，
 * 报出来的"7.693 单位裂缝"没有意义，它也顺便找不到背板（背板识别还依赖正式版的尺寸特征）。
 *
 * 本脚本照抄它的判据（侧视投影上"上下都有几何、中间却空着"的 y 区间 = 洞），
 * 但 **pivot 从 geo 读、符号从生成的 blood_pose 约定读**，所以对重建版是可信的。
 *
 * 用法: node tools/model3d/rebuild_joint_gap.cjs <geo> [--angles 6,8,11] [--json]
 */
'use strict';
const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
const geoFile = args.find(a => !a.startsWith('--')) || path.resolve(__dirname, '../../art/rebuild/black_cleaver_rebuild.geo.json');
const ai = args.indexOf('--angles');
const ANG = (ai >= 0 ? args[ai + 1] : '6,8,11').split(',').map(Number);

/* 与 wing_pose_bake4.cjs / blood_pose 一致的符号：左·上负 / 左·下正 / 右·上正 / 右·下负 */
const WINDS = {
  blade_upper_wing: { staticBone: 'blade', sign: -1, kind: 'upper', label: '左·上翼' },
  blade_lower_wing: { staticBone: 'blade', sign: +1, kind: 'lower', label: '左·下翼' },
  blade_right_upper_wing: { staticBone: 'blade_right', sign: +1, kind: 'upper', label: '右·上翼' },
  blade_right_lower_wing: { staticBone: 'blade_right', sign: -1, kind: 'lower', label: '右·下翼' },
};

const model = JSON.parse(fs.readFileSync(geoFile, 'utf8'))['minecraft:geometry'][0];
const byName = new Map(model.bones.map(b => [b.name, b]));
const kidsOf = n => model.bones.filter(b => b.parent === n).map(b => b.name);

function cubesOf(name, acc = []) {
  const b = byName.get(name);
  if (b) for (const c of b.cubes || []) acc.push({ bone: name, origin: c.origin, size: c.size, rot: c.rotation ? c.rotation[2] : 0 });
  for (const k of kidsOf(name)) cubesOf(k, acc);
  return acc;
}
const rotZ = (p, piv, deg) => {
  const r = deg * Math.PI / 180, c = Math.cos(r), s = Math.sin(r);
  const dx = p[0] - piv[0], dy = p[1] - piv[1];
  return [piv[0] + dx * c - dy * s, piv[1] + dx * s + dy * c];
};
/** cube 的 4 个 x-y 角点（cube 自身 Z 旋转 + 骨骼 Z 旋转都算上） */
function projCorners(c, piv, deg) {
  const [ox, oy] = c.origin, [sx, sy] = c.size;
  const cr = (c.rot || 0) * Math.PI / 180, cc = Math.cos(cr), cs = Math.sin(cr);
  const cx = ox + sx / 2, cy = oy + sy / 2;
  const pts = [];
  for (const dx of [-sx / 2, sx / 2]) for (const dy of [-sy / 2, sy / 2]) {
    pts.push([cx + dx * cc - dy * cs, cy + dx * cs + dy * cc]);
  }
  return deg === 0 ? pts : pts.map(p => rotZ(p, piv, deg));
}
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
const merge = spans => {
  const ss = spans.filter(Boolean).slice().sort((a, b) => a[0] - b[0]), out = [];
  for (const s of ss) { const l = out[out.length - 1]; if (l && s[0] <= l[1] + 1e-9) l[1] = Math.max(l[1], s[1]); else out.push([s[0], s[1]]); }
  return out;
};

console.log('=== 重建版 刃翼展开"关节裂缝"检查（枢轴取自 geo）===');
console.log('geo: ' + path.relative(path.resolve(__dirname, '../..'), geoFile));
console.log('判据与工程自带 wing_joint_check 相同：侧视轮廓上上下都有几何、中间空着的 y 区间');
console.log('');
console.log('★ 为什么必须看 **差分**：绝对"缝高"里绝大部分是造型本身的凹口 —— 斧刃是新月形，');
console.log('  侧视轮廓上两条刃臂之间天然就是空的（工程自带工具的文件头把这类误报写在注释里了：');
console.log('  「静态几何」只取了父刃那一根骨骼，漏掉斧眼/轴盖）。真正属于**关节**的缝，');
console.log('  只会随展开角变化的那一部分。所以下面同时给"合拢态基线"与"展开 − 合拢"。');
console.log('');
const results = [], diffs = [];
for (const [wname, cfg] of Object.entries(WINDS)) {
  const bone = byName.get(wname);
  if (!bone) { console.log('  · ' + wname + ' 不存在，跳过'); continue; }
  const piv = bone.pivot;
  const wingCubes = cubesOf(wname);
  const staticCubes = cubesOf(cfg.staticBone).filter(c => c.bone !== wname);
  const xs = [...wingCubes, ...staticCubes].flatMap(c => [c.origin[0], c.origin[0] + c.size[0]]);
  const x0 = Math.min(...xs), x1 = Math.max(...xs);
  const holeAt = deg => {
    let worst = { hole: 0, x: null, lo: null, hi: null };
    for (let x = x0; x <= x1; x += 0.02) {
      const w = merge(wingCubes.map(c => ySpanAt(projCorners(c, piv, deg), x)));
      const s = merge(staticCubes.map(c => ySpanAt(projCorners(c, piv, 0), x)));
      const all = merge([...w, ...s]);
      if (all.length < 2) continue;
      for (let i = 1; i < all.length; i++) {
        const lo = all[i - 1][1], hi = all[i][0], g = hi - lo;
        if (g > worst.hole) worst = { hole: g, x, lo, hi };
      }
    }
    return worst;
  };
  const base = holeAt(0);
  console.log(`--- ${cfg.label} ${wname}（真实枢轴 ${piv[0]}, ${piv[1]}）---`);
  console.log(`     合拢态基线（造型自身凹口）: ${base.hole.toFixed(3)} 单位`);
  for (const mag of [...new Set(ANG.map(Math.abs))].sort((a, b) => a - b)) {
    const deg = cfg.sign * mag;
    const w = holeAt(deg);
    const d = w.hole - base.hole;
    const tag = d <= 1e-6 ? 'OK 关节零新增缝' : (d < 0.25 ? '· 可忽略' : '!! 关节新增可见缝');
    const where = w.x === null ? '' : `  最宽处 x=${w.x.toFixed(2)}  y ${w.lo.toFixed(2)}->${w.hi.toFixed(2)}`;
    console.log(`  ${String(deg).padStart(4)}°  ${tag}  缝高 ${w.hole.toFixed(3)}（较合拢 +${d.toFixed(3)}）${where}`);
    results.push({ wing: wname, angle: deg, hole: +w.hole.toFixed(3), baseline: +base.hole.toFixed(3), delta: +d.toFixed(3), x: w.x });
    diffs.push(d);
  }
}
const worstAll = results.reduce((m, r) => Math.max(m, r.hole), 0);
const worstDelta = diffs.reduce((m, d) => Math.max(m, d), 0);
console.log('');
console.log('全部角度里最大绝对缝高 = ' + worstAll.toFixed(3) + ' 单位（含造型自身凹口）');
console.log('全部角度里最大**关节新增**缝 = ' + worstDelta.toFixed(3) + ' 单位（1 单位 = 1/16 格；>0.25 约 4px 起肉眼可见）');
if (process.argv.includes('--json')) {
  const out = path.resolve(__dirname, '../../art/rebuild/joint_gap.json');
  fs.writeFileSync(out, JSON.stringify({ geo: geoFile, worstAbsolute: +worstAll.toFixed(3), worstJointDelta: +worstDelta.toFixed(3), results }, null, 2));
  console.log('报告：' + path.relative(path.resolve(__dirname, '../..'), out));
}
process.exit(worstDelta > 0.25 ? 1 : 0);
