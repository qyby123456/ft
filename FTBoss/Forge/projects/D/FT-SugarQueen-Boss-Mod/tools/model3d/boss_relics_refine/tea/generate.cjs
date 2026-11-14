const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const { decodePNG } = require('../../png.cjs');
const { root, render, outline, save } = require('../../boss_materials_refine/tea/render.cjs');
const item = 'tea_heart_pendant';
const resource = path.join(root, 'src/main/resources/assets/ftboss');
const modelFile = path.join(resource, 'models/item/boss_relics/' + item + '.json');
const atlasFile = path.join(resource, 'textures/item/boss_relics/' + item + '.png');
const parts = [];
const round = value => +value.toFixed(5);
const rotation = (angle, origin) => ({ axis: 'z', angle, origin, rescale: false });

function cube(name, from, to, material, turned) {
  parts.push({ name, from: from.map(round), to: to.map(round), material, ...(turned ? { rotation: turned } : {}) });
}

function rod(name, start, end, width, front, back, material) {
  if (end[1] < start[1]) [start, end] = [end, start];
  const deltaX = end[0] - start[0];
  const deltaY = end[1] - start[1];
  const angle = Math.round(Math.atan2(-deltaX, deltaY) * 180 / Math.PI * 2) / 2;
  if (![-45, -22.5, 0, 22.5, 45].includes(angle)) throw new Error('Illegal rod angle: ' + name + ' ' + angle);
  const length = Math.hypot(deltaX, deltaY);
  const centerX = (start[0] + end[0]) / 2;
  const centerY = (start[1] + end[1]) / 2;
  cube(name, [centerX - width / 2, centerY - length / 2 - 0.055, front], [centerX + width / 2, centerY + length / 2 + 0.055, back], material, angle ? rotation(angle, [centerX, centerY, (front + back) / 2]) : null);
}

const upperY = 7.2 + 1.25 / Math.tan(Math.PI / 8);
const crownY = upperY + 1.35;
const perimeter = [[8, 1.8], [5.4, 4.4], [5.4, 7.2], [6.65, upperY], [8, crownY], [9.35, upperY], [10.6, 7.2], [10.6, 4.4]];
for (let index = 0; index < perimeter.length; index++) rod('gilded_leaf_rim_' + index, perimeter[index], perimeter[(index + 1) % perimeter.length], 0.38, 7.12 + index * 0.008, 8.65 - index * 0.008, 'gold');

function leafWidth(height) {
  if (height < 4.4) return (height - 1.8) * 2;
  if (height < 7.2) return 5.2;
  if (height < upperY) return 5.2 - (height - 7.2) * Math.tan(Math.PI / 8) * 2;
  return (crownY - height) * 2;
}

const leafBottom = 2.15;
const leafTop = crownY - 0.27;
const steps = 14;
const stride = (leafTop - leafBottom) / steps;
for (let index = 0; index < steps; index++) {
  const bottom = leafBottom + index * stride;
  const top = bottom + stride;
  const width = Math.max(0.35, Math.min(leafWidth(bottom), leafWidth(top)) + 0.07 - Math.abs((bottom + top) / 2 - 6.7) * 0.012);
  const dome = Math.max(0, 1 - Math.abs((bottom + top) / 2 - 6.7) / 5) * 0.18;
  cube('carved_jade_leaf_' + index, [8 - width / 2, bottom - 0.035, 7.42 - dome], [8 + width / 2, top + 0.035, 8.45 + dome * 0.4], 'jade');
}

rod('front_midrib_gold', [8, 2.05], [8, crownY - 0.2], 0.19, 7.02, 7.5, 'gold');
rod('rear_midrib_engraving', [8, 2.05], [8, crownY - 0.2], 0.18, 8.34, 8.62, 'rear_gold');
for (let index = 0; index < 4; index++) {
  const centerY = 3.3 + index * 1.48;
  const length = index === 0 ? 1.1 : index === 3 ? 1.2 : 2.1;
  for (const side of [-1, 1]) {
    rod('front_leaf_vein_' + index + '_' + side, [8, centerY], [8 + side * length, centerY + length], 0.14, 7.05 + side * 0.008, 7.55 + side * 0.008, 'gold');
    rod('rear_leaf_vein_' + index + '_' + side, [8, centerY], [8 + side * length, centerY + length], 0.12, 8.35 + side * 0.008, 8.6 + side * 0.008, 'rear_gold');
  }
}

cube('tea_heart_socket', [6.95, 5.3, 6.85], [9.05, 8.35, 7.58], 'gold');
cube('tea_heart_socket_horizontal', [6.65, 5.85, 6.92], [9.35, 7.8, 7.62], 'gold');
cube('inset_tea_heart', [7.18, 5.5, 6.53], [8.82, 8.15, 7.1], 'heart');
cube('inset_tea_heart_bevel', [6.9, 6, 6.61], [9.1, 7.65, 7.13], 'heart');
cube('inset_center_glaze', [7.43, 5.85, 6.35], [8.57, 7.85, 6.72], 'sap');
cube('inset_tip_glaze', [7.7, 7.6, 6.43], [8.3, 8.35, 6.95], 'sap', rotation(45, [8, 7.98, 6.7]));
for (const [positionX, positionY, angle] of [[7.08, 5.78, -45], [8.92, 5.78, 45], [7.08, 7.83, 45], [8.92, 7.83, -45]]) {
  cube('heart_setting_claw_' + positionX + '_' + positionY, [positionX - 0.14, positionY - 0.4, 6.3], [positionX + 0.14, positionY + 0.4, 7.02], 'gold', rotation(angle, [positionX, positionY, 6.7]));
}

cube('rear_socket_plate', [7.1, 5.6, 8.32], [8.9, 8.2, 8.72], 'rear_gold');
cube('rear_socket_inlay', [7.35, 5.9, 8.62], [8.65, 7.95, 8.84], 'jade_dark');
rod('rear_sprout_stem', [8, 6.15], [8, 7.65], 0.16, 8.78, 8.95, 'rear_gold');
rod('rear_sprout_left', [8, 6.65], [7.5, 7.15], 0.19, 8.77, 8.96, 'rear_gold');
rod('rear_sprout_right', [8, 7.05], [8.5, 7.55], 0.19, 8.79, 8.97, 'rear_gold');

cube('bail_neck', [7.72, crownY - 0.15, 7.3], [8.28, 12.48, 8.4], 'gold');
cube('bail_front_collar', [7.56, 11.65, 7.08], [8.44, 12.05, 7.66], 'gold');
const ring = [[7.2, 12.15], [6.7, 12.65], [6.7, 13.95], [7.2, 14.45], [8.8, 14.45], [9.3, 13.95], [9.3, 12.65], [8.8, 12.15]];
for (let index = 0; index < ring.length; index++) {
  const start = ring[index];
  const end = ring[(index + 1) % ring.length];
  if (start[1] === end[1]) cube('hanging_ring_' + index, [Math.min(start[0], end[0]) - 0.1, start[1] - 0.2, 7.58 + index * 0.008], [Math.max(start[0], end[0]) + 0.1, start[1] + 0.2, 8.25 - index * 0.008], 'gold');
  else rod('hanging_ring_' + index, start, end, 0.4, 7.58 + index * 0.008, 8.25 - index * 0.008, 'gold');
}
for (const positionX of [6.7, 9.3]) cube('ring_joint_' + positionX, [positionX - 0.25, 13.1, 7.49], [positionX + 0.25, 13.47, 8.35], 'rear_gold');

const colors = {
  gold: ['514635', '82653c', 'b89958', 'dec48a', 'f3e2b1'],
  rear_gold: ['444a36', '6b7049', '9c9c67', 'c3bc84', 'e2d7a5'],
  jade: ['263f36', '3d6a44', '668c52', '95b66f', 'c7d59a'],
  jade_dark: ['1e3b34', '325b43', '527c50', '7d9f6c', 'abc08a'],
  heart: ['647b50', '91a66b', 'b6cb91', 'dce3b0', 'f3eccf'],
  sap: ['617b50', '91ad70', 'bed6a0', 'e3ebc3', 'f4efd9']
};
for (const palette of Object.values(colors)) palette.forEach((color, index) => { palette[index] = color.match(/../g).map(channel => parseInt(channel, 16)); });
const atlasSize = 128;
const density = 2;
const atlas = makeCanvas(atlasSize, atlasSize);
const islands = [];
for (const part of parts) {
  const dimensions = part.to.map((value, axis) => value - part.from[axis]);
  part.faces = {};
  const faceDimensions = { north: [dimensions[0], dimensions[1]], south: [dimensions[0], dimensions[1]], east: [dimensions[2], dimensions[1]], west: [dimensions[2], dimensions[1]], up: [dimensions[0], dimensions[2]], down: [dimensions[0], dimensions[2]] };
  for (const [face, size] of Object.entries(faceDimensions)) islands.push({ part, face, size, width: Math.max(1, Math.ceil(size[0] * density)), height: Math.max(1, Math.ceil(size[1] * density)) });
}
islands.sort((first, second) => second.height - first.height || second.width - first.width);
let shelfLeft = 1;
let shelfTop = 1;
let shelfHeight = 0;
for (const island of islands) {
  if (shelfLeft + island.width + 2 >= atlasSize) {
    shelfTop += shelfHeight + 2;
    shelfLeft = 1;
    shelfHeight = 0;
  }
  if (shelfTop + island.height + 2 >= atlasSize) throw new Error('Atlas overflow: ' + island.part.name);
  const left = shelfLeft;
  const top = shelfTop;
  shelfLeft += island.width + 2;
  shelfHeight = Math.max(shelfHeight, island.height);
  const width = island.size[0] * density;
  const height = island.size[1] * density;
  const material = island.part.material;
  const palette = colors[material];
  for (let pixelY = -1; pixelY <= island.height; pixelY++) for (let pixelX = -1; pixelX <= island.width; pixelX++) {
    const boundedX = Math.max(0, Math.min(island.width - 1, pixelX));
    const boundedY = Math.max(0, Math.min(island.height - 1, pixelY));
    const horizontal = (boundedX + 0.5) / width;
    let shade = horizontal < 0.24 ? 3 : horizontal < 0.57 ? 2 : 1;
    if (material === 'gold' || material === 'rear_gold') {
      if (island.width <= 2) shade = island.face === 'north' ? 3 : 2;
      if (boundedY % 8 === 0 && boundedX % 3 === 0) shade = Math.max(1, shade - 1);
    } else if (material === 'jade' || material === 'jade_dark') {
      if (['north', 'south'].includes(island.face)) {
        const positionX = island.part.to[0] - (boundedX + 0.5) / density - 8;
        const positionY = island.part.to[1] - (boundedY + 0.5) / density;
        shade = positionX < -0.5 ? 3 : positionX < 1 ? 2 : 1;
        const phase = ((positionY - Math.abs(positionX) * 0.8) / 1.48 % 1 + 1) % 1;
        if (phase < 0.18) shade = Math.min(4, shade + 1);
        if (Math.abs(positionX) > leafWidth(positionY) / 2 - 0.4) shade = 1;
      }
    } else {
      const positionY = (boundedY + 0.5) / height;
      const radius = Math.hypot((horizontal - 0.45) * 1.4, positionY - 0.45);
      shade = radius < 0.22 ? 4 : radius < 0.4 ? 3 : 2;
      if (horizontal > 0.8) shade = 1;
    }
    if (island.face === 'up') shade = Math.min(4, shade + 1);
    if (island.face === 'down') shade = Math.max(0, shade - 1);
    atlas.put(left + pixelX, top + pixelY, palette[shade]);
  }
  let uv = [left, top, left + width, top + height].map(value => round(value / atlasSize * 16));
  if (island.face === 'south') uv = [uv[2], uv[1], uv[0], uv[3]];
  island.part.faces[island.face] = { uv, texture: '#tea' };
}
const display = {
  ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.5, 0.5, 0.5] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.78, 0.78, 0.78] },
  head: { rotation: [0, 180, 0], translation: [0, 10, 0], scale: [0.8, 0.8, 0.8] },
  thirdperson_righthand: { rotation: [0, -90, 0], translation: [0, 1, -1], scale: [0.62, 0.62, 0.62] },
  thirdperson_lefthand: { rotation: [0, 90, 0], translation: [0, 1, -1], scale: [0.62, 0.62, 0.62] },
  firstperson_righthand: { rotation: [0, -25, 0], translation: [1.5, 0.8, 0], scale: [0.72, 0.72, 0.72] },
  firstperson_lefthand: { rotation: [0, 25, 0], translation: [1.5, 0.8, 0], scale: [0.72, 0.72, 0.72] }
};
const elements = parts.map(({ material, ...part }) => part);
const model = { ambientocclusion: false, gui_light: 'front', textures: { tea: 'ftboss:item/boss_relics/' + item, particle: 'ftboss:item/' + item }, elements, display };
const json = value => JSON.stringify(value, null, 2) + String.fromCharCode(10);
writeSafe(modelFile, json(model));
atlas.save(atlasFile);
writeSafe(path.join(resource, 'models/item/' + item + '.json'), json({ loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/' + item }, base: { parent: 'ftboss:item/boss_relics/' + item }, perspectives: { gui: { parent: 'ftboss:item/boss_relics/' + item + '_gui' } } }));
writeSafe(path.join(resource, 'models/item/boss_relics/' + item + '_gui.json'), json({ parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/' + item } }));
const icon = outline(render(JSON.parse(fs.readFileSync(modelFile)), decodePNG(atlasFile), { width: 32, height: 32, yaw: -16, pitch: 9, scale: 1.85, center: [8, 8.2, 8] }));
save(path.join(resource, 'textures/item/' + item + '.png'), icon);

function uuid(name) {
  const hex = crypto.createHash('sha256').update('tea-relic-refine:' + name).digest('hex');
  return hex.slice(0, 8) + '-' + hex.slice(8, 12) + '-4' + hex.slice(13, 16) + '-a' + hex.slice(17, 20) + '-' + hex.slice(20, 32);
}
const bbElements = elements.map(element => ({ name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to, box_uv: false,
  origin: element.rotation ? element.rotation.origin : [8, 8, 8], rotation: ['x', 'y', 'z'].map(axis => element.rotation && element.rotation.axis === axis ? element.rotation.angle : 0), rescale: false,
  faces: Object.fromEntries(Object.entries(element.faces).map(([face, value]) => [face, { uv: value.uv.map(coordinate => round(coordinate / 16 * atlasSize)), texture: 0 }])) }));
const categories = { leaf: parts.filter(part => part.name.startsWith('carved')), mounting: parts.filter(part => /rim|bail|ring/.test(part.name)), inlay: parts.filter(part => /heart|inset|setting/.test(part.name)), ornament: parts.filter(part => !part.name.startsWith('carved') && !/rim|bail|ring|heart|inset|setting/.test(part.name)) };
const bb = { meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '茶心护坠 / Saya tea heart pendant', model_identifier: item, resolution: { width: atlasSize, height: atlasSize }, visible_box: [2, 2, 0], ambientocclusion: false, front_gui_light: true, elements: bbElements,
  outliner: Object.entries(categories).map(([name, children]) => ({ name, uuid: uuid('group:' + name), origin: [8, 8, 8], children: children.map(part => uuid(part.name)) })), display,
  textures: [{ name: item + '.png', id: '0', uuid: uuid('atlas'), mode: 'bitmap', source: 'data:image/png;base64,' + fs.readFileSync(atlasFile).toString('base64'), path: '', width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, particle: true }] };
writeSafe(path.join(root, 'art/boss_relics/blockbench/' + item + '.bbmodel'), json(bb));
writeSafe(path.join(root, '_work/boss-relics-refine-20261021/tea/design_manifest.json'), json({ item, elements: parts.length, rotated: parts.filter(part => part.rotation).length, atlas: [atlasSize, atlasSize], density, materials: Object.fromEntries(Object.keys(colors).map(material => [material, parts.filter(part => part.material === material).length])) }));
console.log('Generated only tea_heart_pendant: ' + parts.length + ' elements, ' + parts.filter(part => part.rotation).length + ' rotations, ' + islands.length + ' face islands.');
