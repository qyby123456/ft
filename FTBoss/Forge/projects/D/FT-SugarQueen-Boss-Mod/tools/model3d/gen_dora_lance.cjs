const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { makeCanvas, makePacker, writeSafe } = require('./lib_itemtex.cjs');
const assets = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const size = 128;
const canvas = makeCanvas(size, size);
const packer = makePacker([{ x: 0.125, w: 15.75, top: 0.125, bottom: 15.875 }]);
const palette = {
  shaft: [[24, 48, 55], [75, 112, 113]],
  grip: [[22, 33, 41], [58, 70, 74]],
  gold: [[117, 78, 33], [238, 192, 86]],
  silver: [[92, 132, 146], [220, 239, 228]],
  energy: [[21, 113, 110], [129, 248, 216]]
};
const elements = [];
function cube(name, from, to, material, rotation) {
  const lengths = to.map((value, axis) => value - from[axis]);
  assert(lengths.every(length => length > 0));
  const faces = {};
  for (const side of ['north', 'south', 'west', 'east', 'up', 'down']) {
    const dimensions = side === 'up' || side === 'down' ? [lengths[0], lengths[2]]
      : side === 'east' || side === 'west' ? [lengths[2], lengths[1]] : [lengths[0], lengths[1]];
    const pixels = dimensions.map(length => Math.max(1, Math.round(length * 1.5)));
    const rectangle = packer.alloc(`${name}/${side}`, (pixels[0] + 2) / 8, (pixels[1] + 2) / 8);
    const pixelLeft = Math.round(rectangle[0] * 8) + 1;
    const pixelTop = Math.round(rectangle[1] * 8) + 1;
    const [dark, light] = palette[material];
    for (let row = -1; row <= pixels[1]; row++) {
      for (let column = -1; column <= pixels[0]; column++) {
        const ridge = Math.min(1, Math.max(0, column) / Math.max(1, pixels[0] - 1));
        const stripe = material === 'grip' && row % 5 === 0 ? 0.16 : 0.55 + 0.38 * (1 - ridge);
        canvas.put(pixelLeft + column, pixelTop + row, canvas.mix(dark, light, stripe));
      }
    }
    const rectangleUv = [pixelLeft, pixelTop, pixelLeft + pixels[0], pixelTop + pixels[1]].map(value => value / 8);
    faces[side] = { uv: rectangleUv, texture: '#atlas' };
  }
  elements.push({ name, from, to, faces, ...(rotation ? { rotation } : {}) });
}

cube('dark_alloy_shaft', [7.35, -9.5, 7.35], [8.65, 21, 8.65], 'shaft');
cube('wrapped_grip', [7.05, -7.8, 7.05], [8.95, 4.8, 8.95], 'grip');
cube('lower_counterweight', [6.8, -10.5, 6.8], [9.2, -8.5, 9.2], 'gold');
cube('counterweight_core', [7.25, -11.6, 7.25], [8.75, -10.1, 8.75], 'energy');
for (const height of [-7.9, -1.8, 4.7, 10.9, 16.6]) {
  cube(`collar_${height}`, [6.8, height, 6.8], [9.2, height + 0.7, 9.2], 'gold');
}
cube('upper_energy_front', [7.7, 5.1, 7.02], [8.3, 16.5, 7.52], 'energy');
cube('upper_energy_back', [7.7, 5.1, 8.48], [8.3, 16.5, 8.98], 'energy');
cube('cross_guard', [4.7, 17, 6.6], [11.3, 18.7, 9.4], 'gold');
for (const side of [-1, 1]) {
  cube(`guard_wing_${side}`, [8 + side * 3.8 - 0.65, 16.8, 7.3], [8 + side * 3.8 + 0.65, 21.1, 8.7], 'silver',
    { origin: [8 + side * 3.3, 17.8, 8], axis: 'z', angle: side * -22.5, rescale: false });
}
cube('blade_spine', [7.45, 18.3, 7.1], [8.55, 30.5, 8.9], 'gold');
const widths = [1.1, 1.8, 2.65, 2.5, 2.1, 1.65, 1.15, 0.7, 0.3];
for (const [index, halfWidth] of widths.entries()) {
  const lower = 18.9 + index * 1.4;
  cube(`blade_step_${index}`, [8 - halfWidth, lower, 7.45], [8 + halfWidth, lower + 1.46, 8.55], 'silver');
  if (index < 7) {
    cube(`conduit_front_${index}`, [7.7, lower + 0.1, 6.86], [8.3, lower + 1.32, 7.58], 'energy');
    cube(`conduit_back_${index}`, [7.7, lower + 0.1, 8.42], [8.3, lower + 1.32, 9.14], 'energy');
  }
}
cube('reactor_front', [6.95, 17.2, 6.26], [9.05, 19.65, 6.95], 'energy');
cube('reactor_back', [6.95, 17.2, 9.05], [9.05, 19.65, 9.74], 'energy');
const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
const model = {
  ambientocclusion: false, gui_light: 'front', textures: { atlas: 'ftboss:item/dora_lance', particle: 'ftboss:item/dora_lance' },
  elements,
  display: {
    gui: transform([12, 12, -34], [0, -1.4, 0], 0.35),
    ground: transform([0, 0, 0], [0, 3, 0], 0.25),
    fixed: transform([0, 0, -35], [0, -1.8, 0], 0.35),
    head: transform([0, 0, 0], [0, 10, 0], 0.5),
    thirdperson_righthand: transform([0, -90, 0], [0, 1, 0], 0.7),
    thirdperson_lefthand: transform([0, 90, 0], [0, 1, 0], 0.7),
    firstperson_righthand: transform([0, -90, 15], [1.1, 0.8, 0.1], 0.52),
    firstperson_lefthand: transform([0, 90, -15], [1.1, 0.8, 0.1], 0.52)
  }
};
fs.mkdirSync(path.join(assets, 'models/item'), { recursive: true });
fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
writeSafe(path.join(assets, 'models/item/dora_lance.json'), JSON.stringify(model, null, 2) + '\n');
canvas.save(path.join(assets, 'textures/item/dora_lance.png'));
console.log(`Dora lance: ${elements.length} cubes, ${size}x${size}, ${assets}`);
