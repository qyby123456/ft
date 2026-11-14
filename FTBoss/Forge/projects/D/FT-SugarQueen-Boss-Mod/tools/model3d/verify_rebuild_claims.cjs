'use strict';
/* 独立复核 art/rebuild 重建版的自我声明 —— 不读它的 report，自己从 geo 算。
 * v2: 修正 —— 正式版里 "wing_*_backing" 是 **cube 标签**(挂在 blade 骨里)，不是骨骼名。
 * 用法: node tools/model3d/verify_rebuild_claims.cjs      只读，不写文件。 */
const fs = require('fs');
const path = require('path');

const ROOT = 'D:/ft工程文件/FT-SugarQueen-Boss-Mod';
const OFF = path.join(ROOT, 'src/main/resources/assets/ftboss/geo/black_cleaver.geo.json');
const REB = path.join(ROOT, 'art/rebuild/black_cleaver_rebuild.geo.json');
const D2R = Math.PI / 180;

function load(p) {
  const j = JSON.parse(fs.readFileSync(p, 'utf8'));
  const g = j['minecraft:geometry'][0];
  const d = g.description || {};
  const map = new Map(), labels = new Map();
  for (const b of g.bones) {
    map.set(b.name, b);
    for (const cube of (b.cubes || [])) {
      const label = cube.label || cube.name;
      if (label) labels.set(label, { bone: b.name, cube });
    }
  }
  return {
    id: d.identifier, fmt: j.format_version, tex: [d.texture_width, d.texture_height],
    bones: g.bones, map, labels,
    cubes: g.bones.reduce((s, b) => s + (b.cubes || []).length, 0),
    faces: g.bones.reduce((s, b) => s + (b.cubes || []).reduce((t, c) => t + Object.keys(c.uv || {}).length, 0), 0),
  };
}
function geom(c) {
  const [ox, oy, oz] = c.origin, [sx, sy, sz] = c.size;
  const rz = (c.rotation ? c.rotation[2] : 0) * D2R, ca = Math.cos(rz), sa = Math.sin(rz);
  const cx = ox + sx / 2, cy = oy + sy / 2;
  let x0 = Infinity, x1 = -Infinity, y0 = Infinity, y1 = -Infinity;
  for (const dx of [-sx / 2, sx / 2]) for (const dy of [-sy / 2, sy / 2]) {
    const X = cx + dx * ca - dy * sa, Y = cy + dx * sa + dy * ca;
    x0 = Math.min(x0, X); x1 = Math.max(x1, X); y0 = Math.min(y0, Y); y1 = Math.max(y1, Y);
  }
  return { x0, x1, y0, y1, z0: oz, z1: oz + sz };
}
const f3 = n => (+n).toFixed(3);
const ov = (a0, a1, b0, b1) => Math.min(a1, b1) - Math.max(a0, b0);
const env = gs => ({
  x: [Math.min(...gs.map(g => g.x0)), Math.max(...gs.map(g => g.x1))],
  y: [Math.min(...gs.map(g => g.y0)), Math.max(...gs.map(g => g.y1))],
});

const off = load(OFF), reb = load(REB);
console.log('=========== 1. 骨骼名 / 父级 / 图集 ===========');
console.log(`official: id=${off.id} tex=${off.tex} bones=${off.bones.length} cubes=${off.cubes} faces=${off.faces}`);
console.log(`rebuild : id=${reb.id} tex=${reb.tex} bones=${reb.bones.length} cubes=${reb.cubes} faces=${reb.faces}`);
const on = [...off.map.keys()], rn = [...reb.map.keys()];
console.log('缺失 =', on.filter(n => !reb.map.has(n)).join(',') || '无 ✓',
  '| 新增 =', rn.filter(n => !off.map.has(n)).join(',') || '无 ✓');
const pd = on.filter(n => reb.map.has(n) && off.map.get(n).parent !== reb.map.get(n).parent)
  .map(n => `${n}:${off.map.get(n).parent}->${reb.map.get(n).parent}`);
console.log('父级不一致 =', pd.join(' | ') || '无 ✓');

console.log('\n=========== 2. 背板 vs 刃翼 x 范围（它声称正式版「并不相等，差 0.80」）===========');
const WING = { main_upper: 'blade_upper_wing', main_lower: 'blade_lower_wing', right_upper: 'blade_right_upper_wing', right_lower: 'blade_right_lower_wing' };
for (const [tag, m] of [['official', off], ['rebuild', reb]]) {
  for (const k of Object.keys(WING)) {
    const lbl = `wing_${k}_backing`;
    const rec = m.labels.get(lbl), W = m.map.get(WING[k]);
    if (!rec) { console.log(`  [${tag}] ${lbl}: 无此 cube`); continue; }
    if (!W || !(W.cubes || []).length) { console.log(`  [${tag}] ${lbl}: 翼 ${WING[k]} 无 cube`); continue; }
    const b = env([geom(rec.cube)]), w = env((W.cubes || []).map(geom));
    const d = [b.x[0] - w.x[0], b.x[1] - w.x[1]].map(f3);
    console.log(`  [${tag}] ${lbl} (在 ${rec.bone} 骨) x=[${f3(b.x[0])},${f3(b.x[1])}] 翼 x=[${f3(w.x[0])},${f3(w.x[1])}] 差=[${d}] ` +
      `${(d[0] === '0.000' && d[1] === '0.000') ? '严格相等' : '不等'}`);
  }
}

console.log('\n=========== 3. 同骨骼 z 区间重复（共面根因）===========');
function dupScan(m, tag) {
  let exact = 0, near = 0, min = Infinity, worst = '';
  for (const b of m.bones) {
    const cs = (b.cubes || []).map(geom);
    for (let i = 0; i < cs.length; i++) for (let j = i + 1; j < cs.length; j++) {
      const a = cs[i], c = cs[j];
      if (ov(a.x0, a.x1, c.x0, c.x1) <= 0.05 || ov(a.y0, a.y1, c.y0, c.y1) <= 0.05) continue;
      const dz = Math.max(Math.abs(a.z0 - c.z0), Math.abs(a.z1 - c.z1));
      if (dz < 0.001) exact++;
      else if (dz < 0.05) near++;
      if (dz < min) { min = dz; worst = `${b.name} cube[${i}]×cube[${j}]`; }
    }
  }
  console.log(`  ${tag}: 精确共面(dz<0.001) = ${exact}   近共面(0.001≤dz<0.05) = ${near}   最小 dz = ${min === Infinity ? '—' : f3(min)}  (${worst})`);
  return { exact, near, min };
}
const a = dupScan(off, 'official'), c = dupScan(reb, 'rebuild');
console.log(`  判定: 精确共面 ${a.exact}→${c.exact} ${c.exact === 0 ? '✓ 已清零' : '✗'} ; 近共面(旧)${a.near}→(新)${c.near}`);

console.log('\n=========== 4. 重建版：背板包络 ⊆ 刃翼包络 ===========');
for (const k of Object.keys(WING)) {
  const rec = reb.labels.get(`wing_${k}_backing`), W = reb.map.get(WING[k]);
  if (!rec || !W) { console.log(`  wing_${k}_backing: 缺`); continue; }
  const b = env([geom(rec.cube)]), w = env((W.cubes || []).map(geom));
  const o = [Math.max(0, w.x[0] - b.x[0]), Math.max(0, b.x[1] - w.x[1]),
             Math.max(0, w.y[0] - b.y[0]), Math.max(0, b.y[1] - w.y[1])].map(f3);
  console.log(`  wing_${k}_backing 外露(x0,x1,y0,y1)=[${o}] ${o.every(v => v === '0.000') ? '✓' : '✗'}`);
}
console.log('  注意: 只证明 AABB 包含，**不**证明合拢态被真正遮住 → 必须看渲染。');
