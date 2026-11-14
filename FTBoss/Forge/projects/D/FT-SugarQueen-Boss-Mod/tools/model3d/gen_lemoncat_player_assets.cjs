const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { encodePNG } = require('./png.cjs');
const { writeSafe } = require('./lib_itemtex.cjs');
const root = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const palette = [[255, 225, 97, 255], [115, 255, 188, 255], [255, 249, 208, 255], [255, 173, 52, 255]];
const pixels = Buffer.alloc(16 * 16 * 4);
for (let row = 0; row < 16; row++) for (let column = 0; column < 16; column++) {
  pixels.set(palette[Math.floor(column / 4)], (row * 16 + column) * 4);
}
function write(relative, bytes) {
  const target = path.join(root, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  writeSafe(target, bytes);
}
write('textures/item/lemoncat/glow_palette.png', encodePNG(16, 16, pixels));
write('textures/fx/lemoncat_white.png', encodePNG(8, 8, Buffer.alloc(8 * 8 * 4, 255)));
const result = [];
for (const [identifier, item, names] of [
  ['skillet', 'lemoncat_skillet', ['柠檬徽章果心', '猫耳内衬_-1', '猫耳内衬_1', '掌心下部', '掌心上部', '肉垫_0', '肉垫_1', '肉垫_2', '肉垫_3', '果心']],
  ['kitchen_shovel', 'lemoncat_spatula', ['柠檬徽章果心', '薄荷猫耳芯_-1', '薄荷猫耳芯_1', '猫眼_-1', '猫眼_1', '小猫鼻']]
]) {
  const original = JSON.parse(fs.readFileSync(path.join(root, `models/item/lemoncat/${identifier}.json`), 'utf8'));
  const elements = original.elements.filter(element => names.includes(element.name)).map(element => {
    const overlay = structuredClone(element);
    overlay.from = overlay.from.map(value => value - 0.025);
    overlay.to = overlay.to.map(value => value + 0.025);
    const color = /猫耳/.test(element.name) ? 1 : /果|鼻/.test(element.name) ? 0 : 2;
    overlay.shade = false;
    for (const face of Object.values(overlay.faces)) {
      face.texture = '#glow';
      face.uv = [color * 4 + 1, 1, color * 4 + 3, 3];
    }
    return overlay;
  });
  assert.equal(elements.length, names.length);
  write(`models/item/lemoncat/${identifier}_glow.json`, JSON.stringify({
    ambientocclusion: false, gui_light: 'front', textures: { glow: 'ftboss:item/lemoncat/glow_palette' }, elements
  }, null, 2) + '\n');
  write(`models/item/${item}.json`, JSON.stringify({ parent: `ftboss:item/lemoncat/${identifier}` }, null, 2) + '\n');
  result.push({ identifier, item, glowCubes: elements.length });
}
console.log(JSON.stringify(result, null, 2));
