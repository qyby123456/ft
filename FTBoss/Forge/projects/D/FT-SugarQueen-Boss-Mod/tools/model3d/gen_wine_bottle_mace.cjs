const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { makeCanvas, makePacker, writeSafe } = require('./lib_itemtex.cjs');

function generate(output) {
  const assets = path.resolve(output || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
  const canvas = makeCanvas(256, 256);
  const packer = makePacker([{ x: 0.125, w: 15.75, top: 0.125, bottom: 15.875 }]);
  const palette = {
    glass: [[18, 56, 43], [99, 176, 120]],
    glassDark: [[12, 34, 34], [48, 108, 81]],
    shine: [[81, 155, 117], [171, 224, 174]],
    brass: [[86, 57, 38], [224, 179, 88]],
    metal: [[24, 35, 42], [88, 106, 106]],
    leather: [[38, 30, 36], [124, 63, 58]],
    wood: [[46, 32, 31], [129, 93, 60]],
    wine: [[100, 41, 24], [246, 172, 56]],
    wax: [[68, 21, 36], [181, 53, 66]],
    paper: [[122, 102, 70], [237, 217, 155]]
  };
  const elements = [];
  function cube(name, from, to, material) {
    const lengths = to.map((value, axis) => value - from[axis]);
    assert(lengths.every(length => length > 0));
    const faces = {};
    for (const side of ['north', 'south', 'west', 'east', 'up', 'down']) {
      const dimensions = side === 'up' || side === 'down' ? [lengths[0], lengths[2]]
        : side === 'east' || side === 'west' ? [lengths[2], lengths[1]] : [lengths[0], lengths[1]];
      const pixels = dimensions.map(length => Math.max(1, Math.round(length * 1.5)));
      const rectangle = packer.alloc(`${name}/${side}`, (pixels[0] + 2) / 16, (pixels[1] + 2) / 16);
      const left = Math.round(rectangle[0] * 16) + 1;
      const top = Math.round(rectangle[1] * 16) + 1;
      for (let row = -1; row <= pixels[1]; row++) {
        for (let column = -1; column <= pixels[0]; column++) {
          const horizontal = Math.max(0, Math.min(1, column / Math.max(1, pixels[0] - 1)));
          const vertical = Math.max(0, Math.min(1, row / Math.max(1, pixels[1] - 1)));
          const border = row <= 0 || column <= 0 || row >= pixels[1] - 1 || column >= pixels[0] - 1;
          let shade = border ? (row <= 0 || column <= 0 ? 0.82 : 0.10)
            : material.startsWith('glass') ? (horizontal < 0.22 ? 0.85 : horizontal < 0.62 ? 0.50 : 0.2)
              : material === 'leather' ? (row % 4 === 0 ? 0.2 : 0.6)
                : 0.7 - vertical * 0.25;
          let colors = palette[material];
          if (material === 'paper' && (side === 'north' || side === 'south')) {
            const centered = Math.abs(horizontal - 0.5);
            const emblem = vertical > 0.18 && vertical < 0.74 && centered < 0.26;
            const bands = emblem && (vertical < 0.31 || vertical > 0.61);
            if (emblem) { colors = bands ? palette.brass : palette.wax; shade = 0.7; }
          }
          if (name.startsWith('sight_glass') && row % 4 === 0 && column >= pixels[0] - 2) {
            colors = palette.paper;
            shade = 0.8;
          }
          canvas.put(left + column, top + row, canvas.mix(colors[0], colors[1], shade));
        }
      }
      faces[side] = { uv: [left, top, left + pixels[0], top + pixels[1]].map(value => value / 16), texture: '#atlas' };
    }
    elements.push({ name, from, to, faces });
  }
  cube('ashwood_core', [7.05, 0, 7.05], [8.95, 10.3, 8.95], 'wood');
  cube('leather_grip', [6.8, 1.1, 6.8], [9.2, 6.35, 9.2], 'leather');
  cube('brass_pommel', [6.45, -0.6, 6.45], [9.55, 1.0, 9.55], 'brass');
  cube('pommel_inset', [7.0, -0.88, 7.0], [9.0, -0.4, 9.0], 'wax');
  cube('grip_collar', [6.5, 6.15, 6.5], [9.5, 7.35, 9.5], 'brass');
  cube('sealed_bottle_neck', [6.6, 7.15, 6.6], [9.4, 10.65, 9.4], 'glassDark');
  cube('neck_wax_wrap', [6.25, 7.5, 6.25], [9.75, 8.5, 9.75], 'wax');
  cube('bottle_shoulder', [5.15, 9.9, 5.65], [10.85, 12.2, 10.35], 'glass');
  cube('bottle_head', [4.3, 11.75, 5.4], [11.7, 20.8, 10.6], 'glass');
  cube('front_bevel', [4.65, 12.0, 4.9], [11.35, 20.55, 5.75], 'glassDark');
  cube('back_bevel', [4.65, 12.0, 10.25], [11.35, 20.55, 11.1], 'glassDark');
  cube('bottom_armor', [3.9, 20.4, 4.5], [12.1, 22.25, 11.5], 'metal');
  cube('striking_plate', [4.25, 21.95, 4.85], [11.75, 22.85, 11.15], 'brass');
  cube('upper_brass_band', [4.05, 18.8, 4.65], [11.95, 20.0, 11.35], 'brass');
  cube('lower_brass_band', [4.0, 11.15, 4.7], [12.0, 12.55, 11.3], 'brass');
  cube('left_reinforcement', [3.9, 12.35, 6.55], [4.65, 20.65, 9.45], 'metal');
  cube('right_reinforcement', [11.35, 12.35, 6.55], [12.1, 20.65, 9.45], 'metal');
  for (const side of [-1, 1]) {
    const surface = side < 0 ? 4.9 : 11.1;
    const outer = surface + side * 0.24;
    cube(`vintage_label_${side}`, [5.65, 14.0, outer - 0.3], [10.35, 17.75, outer + 0.3], 'paper');
    cube(`red_wax_seal_${side}`, [7.2, 12.85, outer - 0.4], [8.8, 14.4, outer + 0.4], 'wax');
    cube(`wax_drip_${side}`, [7.6, 12.2, outer - 0.24], [8.2, 13.2, outer + 0.24], 'wax');
    cube(`glass_reflection_${side}`, [4.85, 14.3, surface - 0.3], [5.35, 18.4, surface + 0.3], 'shine');
    for (const height of [11.8, 19.4, 21.3]) {
      for (const horizontal of [4.9, 11.1]) {
        const band = height === 11.8 ? (side < 0 ? 4.7 : 11.3) : (side < 0 ? 4.5 : 11.5);
        cube(`rivet_${side}_${height}_${horizontal}`, [horizontal - 0.3, height - 0.3, band - 0.3],
          [horizontal + 0.3, height + 0.3, band + 0.3], 'metal');
      }
    }
  }
  cube('pressure_gauge_frame', [11.7, 13.2, 7.05], [12.75, 18.4, 8.95], 'brass');
  cube('sight_glass_amber', [12.5, 13.6, 7.35], [13.0, 17.95, 8.65], 'wine');
  cube('leather_tie_left', [6.15, 5.0, 6.65], [6.85, 8.4, 7.35], 'leather');
  cube('leather_tie_tip', [5.85, 4.45, 6.4], [6.95, 5.7, 7.6], 'brass');
  const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
  const model = {
    ambientocclusion: false, gui_light: 'front',
    textures: { atlas: 'ftboss:item/wine_bottle_mace', particle: 'ftboss:item/wine_bottle_mace' },
    elements,
    display: {
      gui: transform([12, 30, -28], [0, -1.25, 0], 0.53),
      ground: transform([0, 0, 0], [0, 4, 0], 0.38),
      fixed: transform([0, 0, 0], [0, -1, 0], 0.6),
      head: transform([0, 180, 0], [0, 7, 0], 0.65),
      thirdperson_righthand: transform([0, -90, 55], [0, 4, 0.5], 0.85),
      thirdperson_lefthand: transform([0, 90, -55], [0, 4, 0.5], 0.85),
      firstperson_righthand: transform([0, -65, 16], [1.4, 1.5, -0.8], 0.67),
      firstperson_lefthand: transform([0, 65, -16], [1.4, 1.5, -0.8], 0.67)
    }
  };
  fs.mkdirSync(path.join(assets, 'models/item'), { recursive: true });
  fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
  writeSafe(path.join(assets, 'models/item/wine_bottle_mace.json'), JSON.stringify(model, null, 2) + '\n');
  canvas.save(path.join(assets, 'textures/item/wine_bottle_mace.png'));
  console.log(`Wine bottle warhammer: ${elements.length} cubes, 256x256 atlas, 1.5 pixels/unit; ${assets}`);
  return model;
}

module.exports = { generate };
if (require.main === module) generate(process.argv[2]);
