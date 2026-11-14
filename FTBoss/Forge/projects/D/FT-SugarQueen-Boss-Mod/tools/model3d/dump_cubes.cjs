'use strict';
/* dump 一个 bedrock geo 的骨骼/cube 结构，用于人工对照。
 * 用法: node tools/model3d/dump_cubes.cjs <geo路径> [骨骼名过滤] */
const fs = require('fs');
const p = process.argv[2];
const filt = process.argv[3];
const j = JSON.parse(fs.readFileSync(p, 'utf8'));
const g = j['minecraft:geometry'][0];
const f = n => (+n).toFixed(2).padStart(7);
console.log(`${p}\nid=${g.description.identifier} tex=${g.description.texture_width}x${g.description.texture_height} bones=${g.bones.length}`);
for (const b of g.bones) {
  if (filt && !b.name.includes(filt)) continue;
  const cs = b.cubes || [];
  console.log(`\n[${b.name}] parent=${b.parent ?? '-'} pivot=${JSON.stringify(b.pivot ?? null)} cubes=${cs.length}`);
  cs.forEach((c, i) => {
    const [ox, oy, oz] = c.origin, [sx, sy, sz] = c.size;
    const r = c.rotation ? ` rot=${JSON.stringify(c.rotation)}` : '';
    const uvm = typeof c.uv === 'object' ? Object.keys(c.uv).length + 'f' : `box(${c.uv})`;
    console.log(`  c${String(i).padStart(2)} o=[${f(ox)},${f(oy)},${f(oz)}] s=[${f(sx)},${f(sy)},${f(sz)}]${r} ${uvm}${c.inflate ? ' inflate=' + c.inflate : ''}`);
  });
}
