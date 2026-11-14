const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { makeCanvas, makePacker, writeSafe } = require('./lib_itemtex.cjs');
const assets = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const size = 128;
const canvas = makeCanvas(size, size);
const packer = makePacker([{ x: 0.125, w: 15.75, top: 0.125, bottom: 15.875 }]);
const palette = {
  body: [[24, 43, 49], [82, 119, 123]],
  steel: [[43, 54, 62], [146, 168, 175]],
  grip: [[22, 29, 37], [61, 72, 77]],
  gold: [[114, 77, 35], [231, 186, 91]],
  energy: [[18, 103, 99], [127, 246, 211]],
  bore: [[13, 20, 27], [28, 38, 44]]
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
    const left = Math.round(rectangle[0] * 8) + 1;
    const top = Math.round(rectangle[1] * 8) + 1;
    const [dark, light] = palette[material];
    for (let row = -1; row <= pixels[1]; row++) {
      for (let column = -1; column <= pixels[0]; column++) {
        const edge = row <= 0 || column <= 0 || row >= pixels[1] - 1 || column >= pixels[0] - 1;
        const shade = material === 'energy' ? 0.8 : material === 'grip' && row % 3 === 0 ? 0.15
          : edge ? (row <= 0 ? 0.9 : 0.2) : 0.55 + 0.2 * (1 - row / Math.max(1, pixels[1]));
        canvas.put(left + column, top + row, canvas.mix(dark, light, shade));
      }
    }
    faces[side] = { uv: [left, top, left + pixels[0], top + pixels[1]].map(value => value / 8), texture: '#atlas' };
  }
  elements.push({ name, from, to, faces, ...(rotation ? { rotation } : {}) });
}
cube('compact_receiver', [3.6, 8.9, 6.45], [15.5, 12.2, 9.55], 'body');
cube('upper_slide', [4.1, 12.05, 6.7], [15.1, 12.85, 9.3], 'steel');
cube('short_barrel', [15.3, 9.6, 7.25], [18.2, 11.5, 8.75], 'steel');
cube('muzzle_brake', [17.7, 9.3, 6.95], [19.1, 11.8, 9.05], 'grip');
cube('dark_bore', [19.08, 9.95, 7.5], [19.15, 11.15, 8.5], 'bore');
cube('stock_upper_strut', [-1.7, 10.6, 7.55], [4.0, 11.3, 8.45], 'steel');
cube('stock_lower_strut', [-1.7, 8.8, 7.45], [3.9, 9.45, 8.55], 'body');
cube('stock_buttpad', [-2.5, 8.25, 7.05], [-1.3, 11.8, 8.95], 'grip');
cube('pistol_grip', [4.2, 4.7, 6.9], [6.4, 9.2, 9.1], 'grip',
  { origin: [5.3, 8.8, 8], axis: 'z', angle: -22.5, rescale: false });
cube('grip_heel', [3.55, 4.2, 6.65], [6.2, 5.1, 9.35], 'gold');
cube('trigger_guard_floor', [6.0, 7.3, 7.55], [9.15, 7.8, 8.45], 'steel');
cube('trigger_guard_front', [8.6, 7.4, 7.6], [9.1, 9.2, 8.4], 'steel');
cube('trigger', [6.8, 8.0, 7.7], [7.3, 9.1, 8.3], 'gold');
cube('magazine_well', [9.15, 8.2, 6.65], [12.6, 9.3, 9.35], 'body');
cube('straight_magazine', [9.55, 3.4, 7.0], [12.15, 8.45, 9.0], 'grip');
cube('magazine_baseplate', [9.2, 2.9, 6.75], [12.5, 3.65, 9.25], 'gold');
cube('front_handstop', [13.6, 7.6, 7.2], [14.5, 9.15, 8.8], 'grip');
cube('rear_sight_base', [4.3, 12.65, 7.1], [5.8, 13.4, 8.9], 'grip');
cube('front_sight_base', [14.0, 12.65, 7.2], [15.1, 13.35, 8.8], 'grip');
cube('front_sight_dot', [14.25, 13.25, 7.75], [14.8, 13.8, 8.25], 'energy');
for (const side of [-1, 1]) {
  const outer = 8 + side * 1.76;
  cube(`receiver_panel_${side}`, [4.3, 9.55, outer - 0.18], [10.4, 11.7, outer + 0.18], 'steel');
  cube(`leyline_stripe_${side}`, [5.0, 10.5, outer - 0.25], [9.8, 11.0, outer + 0.25], 'energy');
  cube(`magazine_charge_window_${side}`, [10.45, 4.1, 8 + side * 1.07 - 0.14], [11.1, 7.65, 8 + side * 1.07 + 0.14], 'energy');
  for (let index = 0; index < 3; index++) {
    cube(`handguard_vent_${side}_${index}`, [11.2 + index * 1.15, 9.95, outer - 0.13],
      [11.65 + index * 1.15, 11.55, outer + 0.13], 'grip');
  }
}
cube('charging_handle', [5.3, 11.35, 9.4], [6.5, 11.95, 10.5], 'gold');
const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
const model = {
  ambientocclusion: false, gui_light: 'front',
  textures: { atlas: 'ftboss:item/dora_smg_v1', particle: 'ftboss:item/dora_smg_v1' },
  elements,
  display: {
    gui: transform([12, 24, -18], [0, 0, 0], 0.62),
    ground: transform([0, 0, 0], [0, 3, 0], 0.4),
    fixed: transform([0, 0, 0], [0, 0, 0], 0.65),
    head: transform([0, 180, 0], [0, 6, 0], 0.6),
    thirdperson_righthand: transform([0, 90, -12], [0, 1.8, 1.2], 0.7),
    thirdperson_lefthand: transform([0, -90, 12], [0, 1.8, 1.2], 0.7),
    firstperson_righthand: transform([0, 70, -8], [0.2, 0.9, -1.5], 0.63),
    firstperson_lefthand: transform([0, -70, 8], [0.2, 0.9, -1.5], 0.63)
  }
};
fs.mkdirSync(path.join(assets, 'models/item'), { recursive: true });
fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
writeSafe(path.join(assets, 'models/item/dora_smg_v1.json'), JSON.stringify(model, null, 2) + '\n');
canvas.save(path.join(assets, 'textures/item/dora_smg_v1.png'));
console.log(`Dora SMG: ${elements.length} cubes, ${size}x${size}, ${assets}`);
