const directions = ['north', 'south', 'east', 'west', 'up', 'down'];

function modelFor(spec) {
  const elements = [];
  const materials = Object.keys(spec.palette);
  function box(name, from, to, material = 'b') {
    const spans = to.map((coordinate, index) => coordinate - from[index]);
    const faces = Object.fromEntries(directions.map(direction => {
      const color = typeof material === 'string' ? material : material[direction] || material.side;
      const index = materials.indexOf(color);
      if (index < 0) throw new Error('Missing palette material: ' + color);
      const horizontal = ['east', 'west'].includes(direction) ? spans[2] : spans[0];
      const vertical = ['up', 'down'].includes(direction) ? spans[2] : spans[1];
      const left = (index % 4 * 16 + 0.5) / 4;
      const top = (Math.floor(index / 4) * 16 + 0.5) / 4;
      return [direction, { uv: [left, top, left + horizontal / 4, top + vertical / 4], texture: '#atlas' }];
    }));
    elements.push({ name, from, to, faces });
  }
  const surface = { side: 'b', north: 'm', up: 'l', down: 'd' };
  function ingot() {
    box('ingot_heel', [3, 3.8, 5], [13, 5, 11], 'd');
    box('ingot_body', [2.5, 5, 4.5], [13.5, 7.2, 11.5], surface);
    box('ingot_top', [3.5, 7.2, 5.2], [12.5, 8.5, 10.8], { side: 'm', up: 'l', down: 'b' });
  }
  function inlay(name, left, bottom, width, height, material, relief = 0) {
    box(name + '_front', [left, bottom, 4.28 - relief], [left + width, bottom + height, 4.58 - relief], material);
    box(name + '_back', [left, bottom, 11.42 + relief], [left + width, bottom + height, 11.72 + relief], material);
  }
  if (spec.id === 'frost') {
    ingot();
    box('sugar_ribbon', [6.7, 4.7, 4.22], [8.4, 7.55, 11.78], 'a');
    box('sugar_ribbon_top', [6.7, 8.35, 5.1], [8.4, 8.75, 10.9], 'A');
    for (const [center, height, depth] of [[4.7, 11.1, 7], [8, 13, 8.1], [11.3, 10.9, 8]]) {
      box('crystal_base', [center - 1.15, 8.4, depth - 1.1], [center + 1.15, height - 1.4, depth + 1.1], 'm');
      box('sugar_crystal', [center - 0.7, height - 1.6, depth - 0.7], [center + 0.7, height - 0.5, depth + 0.7], 'A');
      box('crystal_tip', [center - 0.35, height - 0.65, depth - 0.35], [center + 0.35, height, depth + 0.35], 'h');
    }
    inlay('frost_cut', 10.2, 5.4, 0.5, 1.2, 'h');
  }
  if (spec.id === 'butcher') {
    box('jaw_base', [4, 3, 6], [11.8, 6.5, 10], surface);
    box('bone_root', [3, 6.2, 6.3], [8, 9.7, 9.7], 'm');
    box('long_fang', [3.3, 9.4, 6.7], [6.5, 12.2, 9.3], 'l');
    box('long_fang_tip', [3.5, 12, 7.2], [5, 14, 8.8], 'h');
    box('second_fang', [9.4, 6.1, 6.4], [12.5, 9.1, 9.6], 'm');
    box('second_fang_tip', [10.5, 8.8, 7], [12.1, 11.3, 9], 'l');
    box('broken_point', [5.8, 1.8, 6.7], [8, 3.4, 9.3], 'l');
    box('blood_binding', [6.3, 4.6, 5.7], [8.2, 7.2, 10.3], 'a');
    box('blood_ridge', [7.8, 5.1, 5.45], [9, 6.7, 10.55], 'A');
    box('marrow_front', [9.4, 3.5, 5.77], [10.7, 4.5, 6.08], 'g');
    box('marrow_back', [9.4, 3.5, 9.92], [10.7, 4.5, 10.23], 'g');
  }
  if (spec.id === 'tea') {
    for (const [left, bottom, depth] of [[3.6, 2.5, 6.5], [6, 3.5, 8.2], [8.4, 5.6, 6]]) {
      box('fiber_stalk', [left, bottom, depth], [left + 1.8, bottom + 6, depth + 1.8], 'b');
      box('fiber_vein', [left + 0.25, bottom + 0.2, depth - 0.22], [left + 0.75, bottom + 5.8, depth + 0.05], 'l');
      box('fiber_tip', [left + 0.4, bottom + 5.8, depth + 0.3], [left + 1.4, bottom + 7.2, depth + 1.4], 'm');
    }
    box('leaf_left', [3, 9.6, 6.1], [6.4, 11.3, 8.8], 'm');
    box('leaf_left_tip', [3.8, 11.2, 6.5], [5.8, 12.3, 8.5], 'l');
    box('leaf_right', [8.25, 11, 6.4], [11.8, 12.3, 8.6], 'm');
    box('leaf_right_tip', [10.1, 12.1, 6.8], [11.5, 13.5, 8.3], 'h');
    box('fiber_lashing', [3.4, 6.2, 5.7], [10.5, 7.5, 10.3], 'a');
    box('fiber_lashing_edge', [3.2, 6.75, 5.5], [10.7, 7.15, 10.5], 'A');
  }
  if (spec.id === 'wine') {
    ingot();
    for (const center of [4.5, 11.5]) {
      box('bronze_seal_band', [center - 0.45, 4.75, 4.22], [center + 0.45, 7.4, 11.78], 'a');
      box('bronze_seal_band_top', [center - 0.45, 8.35, 5.1], [center + 0.45, 8.8, 10.9], 'A');
    }
    box('wax_block', [6.1, 8.35, 6.2], [9.9, 9.25, 9.8], 'g');
    box('wax_center', [6.9, 9.1, 7], [9.1, 9.6, 9], 'a');
    inlay('wine_seal', 6.3, 5.1, 3.4, 1.8, 'g');
    inlay('wine_seal_core', 7.5, 5.5, 1, 1, 'A', 0.22);
  }
  if (spec.id === 'cheese') {
    box('rind_bottom', [4, 2.7, 5.1], [11.5, 4.2, 10.9], 'd');
    box('cheese_bottom', [3, 4, 4.8], [12.5, 6.1, 11.2], surface);
    box('cheese_middle', [2.5, 6, 5], [6.6, 9.6, 11], surface);
    box('cheese_upper', [3.2, 9.5, 5.4], [6.2, 12, 10.6], 'm');
    box('crescent_tip_top', [4.7, 11.8, 6], [7.4, 13.1, 10], 'l');
    box('crescent_tip_lower', [11.6, 5.9, 5.4], [13.4, 8, 10.6], 'm');
    for (const [left, bottom, width] of [[3.2, 7.2, 1.4], [6.2, 4.5, 1.7], [9.8, 4.6, 1.1]]) {
      box('pore_front', [left, bottom, 4.7], [left + width, bottom + 0.8, 5.1], 'a');
      box('pore_back', [left, bottom, 10.9], [left + width, bottom + 0.8, 11.3], 'a');
    }
    box('top_pore', [4, 9.48, 7], [5.2, 9.7, 8.2], 'a');
  }
  if (spec.id === 'capacitor') {
    box('steel_heel', [4.4, 2.5, 5.4], [11.6, 3.8, 10.6], 'd');
    box('power_cell', [4, 3.6, 5], [12, 10.6, 11], { side: 'b', up: 'l', north: 'm', down: 'd' });
    for (const left of [3.6, 11.4]) box('metal_guard', [left, 3.2, 4.7], [left + 1, 10.9, 11.3], 'g');
    box('top_cap', [3.8, 10.5, 4.8], [12.2, 11.5, 11.2], 'g');
    for (const left of [5, 9.1]) {
      box('copper_terminal', [left, 11.4, 6.7], [left + 1.8, 13.3, 9.3], 'a');
      box('copper_terminal_light', [left + 0.15, 13.1, 6.85], [left + 1.65, 13.55, 9.15], 'A');
    }
    for (const depth of [4.68, 10.98]) {
      box('energy_channel', [6.1, 4.8, depth], [9.9, 9.65, depth + 0.34], 'd');
      for (const [left, bottom] of [[8, 8.4], [7, 7.3], [7.6, 6.2], [6.7, 5.1]]) {
        box('energy_trace', [left, bottom, depth - 0.12], [left + 1.1, bottom + 1.1, depth + 0.46], 'h');
      }
    }
  }
  if (spec.id === 'spore') {
    box('mycelium_base', [4, 2.7, 5], [12, 4.3, 11], 'a');
    box('mycelium_core', [5, 4.1, 5.8], [11, 5.5, 10.2], 'A');
    for (const [center, bottom, width, height, depth] of [[8, 4.2, 5.4, 7.5, 8.3], [4.5, 3.8, 4.1, 4.8, 7], [11.5, 4, 4.3, 5.5, 8.8]]) {
      box('mushroom_stem', [center - 0.65, bottom, depth - 0.7], [center + 0.65, bottom + height - 1.1, depth + 0.7], 'l');
      box('mushroom_cap', [center - width / 2, bottom + height - 2, depth - 1.8], [center + width / 2, bottom + height - 0.4, depth + 1.8], surface);
      box('cap_dome', [center - width / 2 + 0.7, bottom + height - 0.5, depth - 1.25], [center + width / 2 - 0.7, bottom + height + 0.45, depth + 1.25], 'm');
      box('spore_spot', [center - 0.4, bottom + height + 0.3, depth - 0.4], [center + 0.4, bottom + height + 0.65, depth + 0.4], 'h');
    }
    box('amber_spore', [5.3, 3.5, 4.7], [6.3, 4.7, 5.4], 'g');
  }
  if (spec.id === 'apple') {
    ingot();
    box('golden_kernel', [5.9, 8.3, 6.2], [10.1, 10.5, 9.8], 'm');
    box('kernel_top', [6.7, 10.3, 6.7], [9.3, 11.3, 9.3], 'l');
    box('apple_stem', [7.5, 11.1, 7.6], [8.5, 12.8, 8.6], 'g');
    box('apple_leaf', [8.3, 12.1, 7.4], [11.2, 13.1, 9], 'a');
    box('apple_leaf_tip', [10.2, 12.8, 7.7], [11.7, 13.6, 8.7], 'A');
    for (const left of [6.2, 9.2]) inlay('apple_seed', left, 5.5, 0.8, 1.1, 'g');
    box('kernel_shine', [6.2, 9, 5.99], [6.8, 10.35, 6.32], 'h');
  }
  if (spec.id === 'shepherd') {
    for (const [index, [left, bottom, size]] of [[2.3, 10, 3], [4, 8.3, 3], [6, 6.3, 3.2], [8.1, 4.3, 3], [10, 2.5, 3.1]].entries()) {
      box('bone_segment', [left, bottom, 6.5 + index * 0.12], [left + size, bottom + size, 9.5 - index * 0.12], { side: 'b', up: 'l', north: 'm', down: 'd' });
    }
    box('upper_bone_knuckle', [1.9, 10.4, 6.1], [5.6, 13.7, 9.9], 'l');
    box('lower_bone_knuckle', [10.1, 2.1, 6.1], [13.8, 5.85, 9.9], 'l');
    box('pasture_lashing', [5.85, 6.6, 6.17], [10.2, 8.4, 9.83], 'a');
    box('pasture_lashing_light', [6.8, 6.15, 5.95], [8.1, 9.1, 10.05], 'A');
    box('brass_whistle_front', [7.4, 7.1, 5.74], [8.8, 8.1, 6.06], 'g');
    box('brass_whistle_back', [7.4, 7.1, 9.94], [8.8, 8.1, 10.26], 'g');
  }
  if (spec.id === 'tide') {
    box('reef_base', [4.1, 2.5, 5], [11.9, 4.4, 11], 'd');
    box('reef_shoulder', [3, 4.2, 5.7], [13, 6, 10.3], 'b');
    for (const [left, bottom, top, depth, material] of [[3.4, 5.5, 10.6, 7, 'm'], [6.9, 5.3, 13.6, 8.5, 'l'], [10.1, 5.2, 11.4, 7.1, 'A']]) {
      box('tide_crystal', [left, bottom, depth - 1], [left + 2.2, top - 1.2, depth + 1], material);
      box('crystal_cut', [left + 0.35, top - 1.3, depth - 0.7], [left + 1.85, top - 0.45, depth + 0.7], material === 'A' ? 'l' : 'h');
      box('crystal_point', [left + 0.7, top - 0.55, depth - 0.35], [left + 1.5, top, depth + 0.35], 'h');
    }
    box('pearl_mount', [6, 4.4, 4.7], [10, 7.7, 7], 'm');
    box('pearl_center', [6.6, 5.1, 4.35], [9.4, 7.3, 6], 'l');
    box('pearl_highlight', [7, 6.1, 4.1], [8.1, 7.1, 4.6], 'h');
  }
  return {
    ambientocclusion: false, gui_light: 'side',
    textures: { atlas: 'ftboss:item/boss_materials/' + spec.id, particle: 'ftboss:item/' + spec.id + '_material' },
    elements,
    display: {
      gui: { rotation: [25, -35, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
      ground: { rotation: [0, 0, 0], translation: [0, 2.5, 0], scale: [0.5, 0.5, 0.5] },
      fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
      head: { rotation: [0, 180, 0], translation: [0, 10, 0], scale: [1, 1, 1] },
      thirdperson_righthand: { rotation: [0, -90, 0], translation: [0, 1, -1], scale: [0.65, 0.65, 0.65] },
      thirdperson_lefthand: { rotation: [0, 90, 0], translation: [0, 1, -1], scale: [0.65, 0.65, 0.65] },
      firstperson_righthand: { rotation: [0, -25, 0], translation: [1.5, 1, 0], scale: [0.75, 0.75, 0.75] },
      firstperson_lefthand: { rotation: [0, 25, 0], translation: [1.5, 1, 0], scale: [0.75, 0.75, 0.75] }
    }
  };
}
module.exports = { modelFor };
