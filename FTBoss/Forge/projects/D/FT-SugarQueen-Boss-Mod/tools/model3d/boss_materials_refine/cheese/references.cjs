const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG } = require('../../png.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const output = path.join(root, 'art/boss_materials/refine/cheese');
const entries = ['entity/rat_boss', 'entity/big_rat', 'entity/small_rat', 'item/cheese_moon_charm', 'item/cheese_wheel_mace', 'item/blue_cheese', 'item/rat_bait', 'item/cheese_material'];
const sheet = new Canvas(1024, 512);
sheet.rect(0, 0, 1024, 512, 31, 35, 44);
for (const [index, entry] of entries.entries()) {
  const texture = decodePNG(path.join(assets, 'textures', entry + '.png'));
  const scale = Math.max(1, Math.floor(224 / Math.max(texture.w, texture.h)));
  const originX = index % 4 * 256 + 16;
  const originY = Math.floor(index / 4) * 256 + 16;
  for (let pixelY = 0; pixelY < texture.h; pixelY++) for (let pixelX = 0; pixelX < texture.w; pixelX++) {
    const offset = (pixelY * texture.w + pixelX) * 4;
    sheet.rect(originX + pixelX * scale, originY + pixelY * scale, scale, scale, ...texture.data.slice(offset, offset + 3), texture.data[offset + 3] / 255);
  }
}
sheet.save(path.join(output, 'references.png'));
fs.writeFileSync(path.join(output, 'references.json'), JSON.stringify({ order: entries, source: assets }, null, 2) + '\n');
console.log(entries);

