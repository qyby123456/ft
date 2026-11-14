/* 从 geo 里抽出指定骨骼, 生成"局部放大"用的临时 geo(用于细看脸/配饰有没有穿模)
 * 用法: node geo_subset.cjs <geo.json> <out.geo.json> bone1,bone2,...
 * 说明: 被抽出的骨骼一律挂到 root 下(去掉 parent), 但它们自己的 pivot 保留 -> 位置不变 */
const fs = require('fs');
const [src, out, list] = [process.argv[2], process.argv[3], (process.argv[4] || '').split(',').map((s) => s.trim()).filter(Boolean)];
const g = JSON.parse(fs.readFileSync(src, 'utf8'));
const def = g['minecraft:geometry'][0];
const want = new Set(list);
const bones = def.bones.filter((b) => want.has(b.name)).map((b) => {
    const c = JSON.parse(JSON.stringify(b));
    delete c.parent;
    return c;
});
// root 一定要在(否则 GeckoLib/预览器可能不认), 没抽到就补一个空的
if (!bones.some((b) => b.name === 'root')) bones.unshift({ name: 'root', pivot: [0, 0, 0] });
def.bones = bones;
fs.writeFileSync(out, JSON.stringify(g, null, 1));
let cubes = 0;
for (const b of bones) cubes += (b.cubes || []).length;
console.log(`局部 geo: ${bones.map((b) => b.name).join(', ')}  (${bones.length} 骨骼 / ${cubes} 方块)`);
