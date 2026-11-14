const fs = require('node:fs');
const path = require('node:path');
const { Canvas } = require('./png.cjs');

const project = path.resolve(__dirname, '../..');
const root = path.resolve(process.argv[2] || path.join(project, 'src/main/resources'));
const assets = path.join(root, 'assets/ftboss');
const specs = [
  { id: 'wooden_bow', colors: ['51311f', '80512c', 'b47c3d', 'e3b86c'], trim: ['604128', '94683c', 'bf955b', 'ebcd8b'], depth: 1.2, width: 0.66, limbs: [[1.85, -22.5], [1.9, -45], [1.45, -22.5], [1.05, 22.5]] },
  { id: 'stone_bow', colors: ['353b3c', '596364', '889391', 'b9c0b5'], trim: ['4a4136', '746452', 'aa9474', 'd5c29d'], depth: 1.8, width: 1.0, limbs: [[1.65, -22.5], [1.85, -45], [1.5, 0], [1.0, 22.5]] },
  { id: 'iron_bow', colors: ['354349', '65797d', 'a7b9b8', 'e2eeeb'], trim: ['262d31', '465255', '748887', 'c6d2c8'], depth: 1.55, width: 0.82, limbs: [[1.6, -45], [1.85, -22.5], [1.55, 0], [1.0, 45]] },
  { id: 'golden_bow', colors: ['704019', 'ac6c20', 'e9b93e', 'fff0a0'], trim: ['552228', '943b35', 'd9674d', 'ffb27b'], depth: 1.45, width: 0.68, limbs: [[1.5, -45], [1.9, -22.5], [1.65, 0], [1.0, 45]] },
  { id: 'diamond_bow', colors: ['125457', '198e94', '40d7d0', 'bcfff1'], trim: ['103d47', '176876', '38b2b5', '88ebde'], depth: 1.65, width: 0.76, limbs: [[1.8, -22.5], [1.6, -45], [1.4, 0], [1.25, 45]] },
  { id: 'netherite_bow', colors: ['27252d', '423c47', '695866', 'a18b96'], trim: ['38293d', '63445d', '986886', 'd6a2ba'], depth: 2.05, width: 1.05, limbs: [[1.55, -45], [1.65, -22.5], [1.4, -22.5], [1.05, 45]] }
];
const display = {
  gui: { rotation: [0, 0, -35], translation: [0, 0, 0], scale: [0.9, 0.9, 0.9] },
  ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.5, 0.5, 0.5] },
  head: { rotation: [0, 180, 0], translation: [0, 13, 7], scale: [1, 1, 1] },
  fixed: { rotation: [0, 180, -35], translation: [0, 0, 0], scale: [0.85, 0.85, 0.85] },
  thirdperson_righthand: { rotation: [-80, 260, -40], translation: [-1, -2, 2.5], scale: [0.9, 0.9, 0.9] },
  thirdperson_lefthand: { rotation: [-80, -280, 40], translation: [-1, -2, 2.5], scale: [0.9, 0.9, 0.9] },
  firstperson_righthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.9, 0.9, 0.9] },
  firstperson_lefthand: { rotation: [0, 90, -25], translation: [1.13, 3.2, 1.13], scale: [0.9, 0.9, 0.9] }
};
const round = value => Math.round(value * 10000) / 10000;
const rgb = hex => hex.match(/../g).map(value => parseInt(value, 16));
const write = (relative, value) => {
  const target = path.join(assets, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, JSON.stringify(value, null, 2) + '\n');
};

for (const spec of specs) {
  const texture = new Canvas(64, 64);
  const palettes = [spec.colors, ['332821', '624632', '926542', 'c69963'], spec.colors, spec.trim,
    ['8d8574', 'b9b198', 'e0dcc4', 'fff3d1'], ['57402b', '8e683b', 'c49b5c', 'e7c98b'],
    ['7b7974', 'b3bcb7', 'e3e8dc', 'fff9e7'], [spec.colors[0], spec.colors[0], spec.colors[1], spec.colors[2]]];
  for (let pixelY = 0; pixelY < 64; pixelY++) for (let pixelX = 0; pixelX < 64; pixelX++) {
    const column = pixelX % 16;
    const row = pixelY % 32;
    const tile = Math.floor(pixelX / 16) + 4 * Math.floor(pixelY / 32);
    let shade = column === 1 || row === 1 ? 3 : column === 0 || row === 0 ? 0 : column < 4 ? 2 : 1;
    if (tile === 1) shade = (row + column) % 6 === 0 ? 3 : (row + column) % 6 < 2 ? 0 : 1;
    if (tile === 0 && spec.id === 'wooden_bow' && (column * 5 + row) % 17 === 0) shade = 0;
    if (tile === 0 && spec.id === 'stone_bow' && (row * 3 + column * 7) % 19 < 3) shade = Math.max(0, shade - 1);
    if (tile === 0 && spec.id === 'diamond_bow' && column > 2 && column + row % 10 > 8) shade = 1;
    if (tile === 4) shade = column % 2 === 0 ? 2 : 3;
    texture.set(pixelX, pixelY, ...rgb(palettes[tile][shade]), 1);
  }
  fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
  texture.save(path.join(assets, 'textures/item', spec.id + '.png'));
  for (let state = 0; state < 4; state++) {
    const elements = [];
    const box = (name, center, size, tile = 0, angle = 0) => {
      const from = size.map((value, axis) => center[axis] - value / 2);
      const to = size.map((value, axis) => center[axis] + value / 2);
      const faces = {};
      for (const face of ['north', 'south', 'east', 'west', 'up', 'down']) {
        const width = ['north', 'south', 'up', 'down'].includes(face) ? size[0] : size[2];
        const height = ['up', 'down'].includes(face) ? size[2] : size[1];
        const role = tile === 0 && !['north', 'south'].includes(face) ? 7 : tile;
        const startU = ((role % 4) * 16 + 1) / 4;
        const startV = (Math.floor(role / 4) * 32 + 1) / 4;
        const lastU = round(startU + width / 2);
        const uv = face === 'south' ? [lastU, startV, startU, round(startV + height / 2)] : [startU, startV, lastU, round(startV + height / 2)];
        faces[face] = { uv, texture: '#bow' };
      }
      const element = { name, from: from.map(round), to: to.map(round), faces };
      if (angle) element.rotation = { angle, axis: 'z', origin: center.map(round), rescale: false };
      elements.push(element);
    };
    const beam = (name, start, length, angle, width, depth, tile = 0, side = 1) => {
      const radians = angle * Math.PI / 180;
      const end = [start[0] - Math.sin(radians) * length, start[1] + side * Math.cos(radians) * length];
      box(name, [(start[0] + end[0]) / 2, (start[1] + end[1]) / 2, 8], [width, length + 0.12, depth], tile, angle * side);
      return end;
    };
    const front = 8 - spec.depth / 2;
    const stringZ = front - 0.4;
    box('grip', [4.6, 8, 8], [1.05, 3.3, spec.depth], 1);
    box('arrow_rest', [5.1, 7.73, front - 0.1], [0.55, 0.3, 0.8], 2);
    for (const side of [-1, 1]) {
      box('ferrule_' + side, [4.6, 8 + side * 1.6, 8], [1.45, 0.48, spec.depth + 0.32], 2);
      let point = [4.6, 8 + side * 1.65];
      for (let segment = 0; segment < spec.limbs.length; segment++) {
        let [length, angle] = spec.limbs[segment];
        if (state >= 1 && segment === 3) angle -= 22.5;
        if (state >= 2 && segment === 2) angle -= 22.5;
        if (state >= 3 && segment === 0 && angle === -22.5) angle -= 22.5;
        const radians = angle * Math.PI / 180;
        const next = [point[0] - Math.sin(radians) * length, point[1] + side * Math.cos(radians) * length];
        const center = [(point[0] + next[0]) / 2, (point[1] + next[1]) / 2, 8];
        const thickness = spec.width * [1.15, 1, 0.8, 0.6][segment];
        const depth = spec.depth - (segment + 1) * 0.09;
        box('limb_' + side + '_' + segment, center, [thickness, length + 0.16, depth], 0, angle * side);
        if (spec.id === 'wooden_bow') {
          if (segment === 0 || segment === 3) box('binding_' + side + '_' + segment, center, [thickness + 0.15, 0.38, depth + 0.14], 1, angle * side);
        } else if (spec.id === 'stone_bow') {
          box('stone_plate_' + side + '_' + segment, center, [thickness + 0.3, length * 0.66, depth + 0.24], 0, angle * side);
          box('lashing_' + side + '_' + segment, center, [thickness + 0.42, 0.25, depth + 0.4], 1, angle * side);
        } else {
          box('laminate_' + side + '_' + segment, [center[0], center[1], front - 0.06 + (segment + 1) * 0.045], [thickness * 0.48, length * 0.78, 0.24], spec.id === 'iron_bow' ? 2 : 3, angle * side);
          if (segment === 0 || segment === 2) {
            box('rivet_front_' + side + '_' + segment, [center[0], center[1], front - 0.24 + (segment + 1) * 0.045], [0.3, 0.3, 0.14], 2);
            box('rivet_back_' + side + '_' + segment, [center[0], center[1], 16 - front + 0.1 - (segment + 1) * 0.045], [0.3, 0.3, 0.2], 2);
          }
        }
        if (segment === 1 && ['golden_bow', 'diamond_bow', 'netherite_bow'].includes(spec.id)) {
          const extra = spec.id === 'golden_bow' ? 0.75 : spec.id === 'diamond_bow' ? 1.15 : 1.05;
          const width = spec.id === 'diamond_bow' ? 0.62 : 0.46;
          const anchor = [center[0] - 0.3, center[1] - side * 0.3];
          beam('outer_spur_' + side, anchor, extra, 45, width, depth - 0.26, 2, side);
          if (spec.id === 'diamond_bow') box('crystal_' + side, [center[0], center[1], 8], [1.2, 1.2, depth + 0.35], 0, 45);
        }
        point = next;
      }
      box('nock_' + side, [point[0], point[1], 8], [0.58, 0.5, spec.depth + 0.8], 2);
      const nockX = point[0] + [0, 0.85, 1.85, 3.25][state];
      const steps = Math.ceil(Math.abs(point[1] - 8) * 3);
      for (let step = 0; step < steps; step++) {
        const first = step / steps;
        const last = (step + 1) / steps;
        const firstX = nockX + (point[0] - nockX) * first;
        const lastX = nockX + (point[0] - nockX) * last;
        const firstY = 8 + (point[1] - 8) * first;
        const lastY = 8 + (point[1] - 8) * last;
        box('string_' + side + '_' + step, [(firstX + lastX) / 2, (firstY + lastY) / 2, stringZ],
          [Math.abs(lastX - firstX) + 0.16, Math.abs(lastY - firstY), 0.16], 4);
      }
      if (state && side === 1) {
        const arrowLength = spec.id === 'wooden_bow' ? 7.5 : spec.id === 'stone_bow' ? 6.9 : 6.3;
        const arrowStart = nockX - arrowLength;
        box('arrow_shaft', [(arrowStart + nockX) / 2, 8, stringZ], [arrowLength, 0.22, 0.22], 5);
        box('arrow_head', [arrowStart, 8, stringZ], [0.6, 0.6, 0.45], 2, 45);
        box('arrow_fletching', [nockX - 0.65, 8, stringZ], [0.9, 0.65, 0.2], 6);
        box('arrow_fletching_cross', [nockX - 0.65, 8, stringZ], [0.86, 0.16, 0.65], 6);
      }
    }
    if (spec.id === 'iron_bow') {
      for (const side of [-1, 1]) box('grip_guard_' + side, [5.1, 8 + side * 1.05, 8], [0.55, 1.1, spec.depth + 0.2], 2, 22.5 * side);
    }
    if (['golden_bow', 'diamond_bow', 'netherite_bow'].includes(spec.id)) {
      for (const side of [-1, 1]) {
        const center = [4.6, 8 + side * 1.75, 8];
        box('jewel_mount_' + side, center, [1.15, 1.15, spec.depth + 0.42], 2, 45);
        box('jewel_front_' + side, [center[0], center[1], front - 0.33], [0.6, 0.6, 0.3], 3, 45);
        box('jewel_back_' + side, [center[0], center[1], 16 - front + 0.33], [0.6, 0.6, 0.3], 3, 45);
        if (spec.id === 'netherite_bow') beam('riser_talon_' + side, [4.15, 8 + side * 1.9], 1.35, 45, 0.6, spec.depth - 0.2, 0, side);
      }
    }
    const model = { credit: 'FarmingTales original material recurve bows', texture_size: [64, 64],
      textures: { bow: 'ftboss:item/' + spec.id, particle: 'ftboss:item/' + spec.id }, gui_light: 'front', display, elements };
    if (state === 0) model.overrides = [
      { predicate: { 'minecraft:pulling': 1 }, model: 'ftboss:item/' + spec.id + '_pulling_0' },
      { predicate: { 'minecraft:pulling': 1, 'minecraft:pull': 0.65 }, model: 'ftboss:item/' + spec.id + '_pulling_1' },
      { predicate: { 'minecraft:pulling': 1, 'minecraft:pull': 0.9 }, model: 'ftboss:item/' + spec.id + '_pulling_2' }
    ];
    write('models/item/' + spec.id + (state ? '_pulling_' + (state - 1) : '') + '.json', model);
  }
}
console.log(JSON.stringify({ root, bows: specs.length, models: specs.length * 4, textures: specs.length, resourceOnly: true }));
