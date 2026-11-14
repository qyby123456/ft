// geo 骨骼速查: 打印骨骼树/立方体/材质与包围盒, 并给出模型 vs 碰撞箱比值
// 用法: node inspect.cjs <geo.json> [hitboxW_blocks] [hitboxH_blocks]
const fs = require('fs');
const geoFile = process.argv[2];
const hbW = parseFloat(process.argv[3] || '0');
const hbH = parseFloat(process.argv[4] || '0');
const g = JSON.parse(fs.readFileSync(geoFile, 'utf8'));
const geo = g['minecraft:geometry'][0];
console.log('identifier  =', geo.description.identifier);
console.log('texture     =', geo.description.texture ? JSON.stringify(geo.description.texture) : '(none)');
console.log('visible_bounds =', geo.description.visible_bounds_width, 'x', geo.description.visible_bounds_height);
console.log('bones =', geo.bones.length);

let cubes = 0;
const min = [1e9, 1e9, 1e9], max = [-1e9, -1e9, -1e9];
const mats = new Map();
for (const b of geo.bones) {
  for (const c of (b.cubes || [])) {
    cubes++;
    for (let i = 0; i < 3; i++) {
      min[i] = Math.min(min[i], c.origin[i]);
      max[i] = Math.max(max[i], c.origin[i] + c.size[i]);
    }
    // 统计 box-uv 材质宽高
    if (Array.isArray(c.uv)) {
      const key = c.uv.join(',') + '|' + c.size.join(',');
      mats.set(key, (mats.get(key) || 0) + 1);
    }
  }
}
const size = [max[0] - min[0], max[1] - min[1], max[2] - min[2]];
console.log('cubes =', cubes);
console.log('bbox  = X ' + min[0] + '..' + max[0] + '  Y ' + min[1] + '..' + max[1] + '  Z ' + min[2] + '..' + max[2]);
console.log('size  = ' + size.map(v => v.toFixed(2)).join(' x ') + ' (模型单位, 16=1格)');
console.log('blocks= ' + size.map(v => (v / 16).toFixed(2)).join(' x '));
if (hbW > 0 && hbH > 0) {
  console.log('hitbox= ' + hbW + ' x ' + hbH + ' (格)');
  console.log('ratio = 宽 ' + (size[0] / 16 / hbW).toFixed(2) + '  高 ' + (size[1] / 16 / hbH).toFixed(2) + '  (1.00 为刚好)');
}
console.log('\n--- 骨骼 ---');
for (const b of geo.bones) {
  const cs = (b.cubes || []).map(c =>
    `origin=[${c.origin.join(',')}] size=[${c.size.join(',')}] uv=${JSON.stringify(c.uv)}${c.inflate ? ' inflate=' + c.inflate : ''}`);
  console.log(`${b.name.padEnd(14)} parent=${String(b.parent || '-').padEnd(12)} pivot=[${(b.pivot || []).join(',')}]` +
    (b.rotation ? ` rot=[${b.rotation.join(',')}]` : '') + ` cubes=${(b.cubes || []).length}`);
  for (const s of cs) console.log('      ' + s);
}
console.log('\n--- 立方体材质 UV 分组(检查共用/串脸风险) ---');
for (const [k, n] of mats) console.log(`  ${n} 个 cube 共用 uv|size = ${k}`);
