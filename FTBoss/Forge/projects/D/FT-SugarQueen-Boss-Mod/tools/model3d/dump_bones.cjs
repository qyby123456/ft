// 打印一份 Bedrock geometry 每根骨骼的包围盒 + 面部件在哪一侧, 用来判定"模型朝哪边"
// 用法: node dump_bones.cjs <geo.json> [要细看的骨骼名,逗号分隔]
const fs = require('fs');
const geo = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const def = (geo['minecraft:geometry'] || [])[0];
const only = process.argv[3] ? new Set(process.argv[3].split(',')) : null;
console.log(`identifier=${def.description.identifier} tex=${def.description.texture_width}x${def.description.texture_height} bones=${def.bones.length}`);
console.log('骨骼'.padEnd(16) + 'parent'.padEnd(14) + 'min(x,y,z)'.padEnd(24) + 'max(x,y,z)'.padEnd(24) + 'cubes');
for (const b of def.bones) {
  if (only && !only.has(b.name)) continue;
  if (!b.cubes || !b.cubes.length) { console.log(b.name.padEnd(16) + String(b.parent || '-').padEnd(14) + '(无方块)'); continue; }
  const mn = [1e9, 1e9, 1e9], mx = [-1e9, -1e9, -1e9];
  for (const c of b.cubes) for (let i = 0; i < 3; i++) { mn[i] = Math.min(mn[i], c.origin[i]); mx[i] = Math.max(mx[i], c.origin[i] + c.size[i]); }
  const f = a => a.map(v => (Math.round(v * 10) / 10).toString()).join(',');
  console.log(b.name.padEnd(16) + String(b.parent || '-').padEnd(14) + ('[' + f(mn) + ']').padEnd(24) + ('[' + f(mx) + ']').padEnd(24) + b.cubes.length);
}
