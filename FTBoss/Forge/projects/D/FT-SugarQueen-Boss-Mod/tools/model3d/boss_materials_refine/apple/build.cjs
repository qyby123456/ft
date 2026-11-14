const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const { encodePNG } = require('../../png.cjs');
const root = path.resolve(__dirname, '../../../..');
const art = path.join(root, 'art/boss_materials/refine/apple');
const resources = path.join(root, 'src/main/resources/assets/ftboss');
const atlasSize = 128;
const density = 1.75;
const parts = [];
const round = value => Math.round(value * 10000) / 10000;

function box(name, center, size, material, rotation) {
  const part = { name, from: center.map((value, index) => round(value - size[index] / 2)), to: center.map((value, index) => round(value + size[index] / 2)), material };
  if (rotation) part.rotation = { origin: center.map(round), axis: rotation[0], angle: rotation[1], rescale: false };
  parts.push(part);
  return part;
}

box('pith_heart', [8, 7.95, 8], [4.15, 6.55, 2.65], 'pith');
box('upper_cut_flesh', [8, 10.9, 8], [5.9, 1.55, 4.65], 'flesh');
box('lower_cut_flesh', [8, 4.65, 8], [5.35, 1.5, 4.3], 'flesh');
for (const side of [-1, 1]) {
  const label = side < 0 ? 'left' : 'right';
  box(`waist_${label}`, [8 + side * 2.08, 7.85, 8], [0.9, 1.85, 3.95], 'flesh');
  box(`upper_taper_${label}`, [8 + side * 2.45, 9.55, 8], [1.1, 3.5, 4.2], 'flesh', ['z', -side * 22.5]);
  box(`lower_taper_${label}`, [8 + side * 2.28, 5.9, 8], [1.12, 3.15, 4.05], 'flesh', ['z', side * 22.5]);
  box(`upper_rind_cut_${label}`, [8 + side * 3.12, 10.95, 8], [0.54, 1.9, 3.65], 'rind', ['z', -side * 22.5]);
  box(`lower_rind_cut_${label}`, [8 + side * 2.95, 4.55, 8], [0.5, 1.55, 3.4], 'rind', ['z', side * 22.5]);
  box(`upper_gold_lobe_${label}`, [8 + side * 1.94, 11.82, 8], [3.15, 1.48, 4.35], 'gold');
  box(`upper_beveled_shoulder_${label}`, [8 + side * 3.28, 11.55, 8], [0.85, 1.38, 3.55], 'gold', ['z', -side * 22.5]);
  box(`upper_highlight_step_${label}`, [8 + side * 1.92, 12.56, 8], [2.22, 0.32, 3.25], 'goldLight');
  box(`lower_gold_lobe_${label}`, [8 + side * 1.68, 3.77, 8], [2.72, 1.22, 4.15], 'gold');
  box(`lower_beveled_heel_${label}`, [8 + side * 2.79, 4.0, 8], [0.82, 1.16, 3.25], 'gold', ['z', side * 22.5]);
  box(`lower_rind_under_${label}`, [8 + side * 1.68, 3.15, 8], [1.96, 0.34, 2.92], 'rind');
}

for (const end of [-1, 1]) {
  const label = end < 0 ? 'front' : 'back';
  box(`carpel_septum_${label}`, [8, 7.88, 8 + end * 2.17], [0.5, 5.55, 0.45], 'pith');
  for (const side of [-1, 1]) {
    const sideLabel = side < 0 ? 'left' : 'right';
    box(`seed_bay_${label}_${sideLabel}`, [8 + side * 1.05, 7.94, 8 + end * 1.38], [1.52, 3.4, 0.62], 'cavity');
    box(`carpel_lip_${label}_${sideLabel}`, [8 + side * 1.74, 7.92, 8 + end * 1.94], [0.34, 2.5, 0.4], 'pith');
    box(`upper_carpel_arch_${label}_${sideLabel}`, [8 + side * 0.95, 9.85, 8 + end * 1.97], [1.58, 0.42, 0.49], 'pith', ['z', -side * 22.5]);
    box(`lower_carpel_arch_${label}_${sideLabel}`, [8 + side * 0.94, 6.02, 8 + end * 1.93], [1.48, 0.4, 0.44], 'pith', ['z', side * 22.5]);
    const seedY = 8.02 + side * end * 0.22;
    box(`seed_${label}_${sideLabel}`, [8 + side * 1.03, seedY, 8 + end * 1.78], [0.88, 1.28, 0.59], 'seed', ['z', -side * 22.5]);
    box(`seed_tip_${label}_${sideLabel}`, [8 + side * 1.29, seedY + 0.64, 8 + end * 1.74], [0.45, 0.55, 0.47], 'seed', ['z', -side * 22.5]);
  }
  box(`gold_upper_cut_rim_${label}`, [8, 11.38, 8 + end * 2.52], [5.88, 0.56, 0.42], 'gold');
  box(`gold_lower_cut_rim_${label}`, [8, 4.04, 8 + end * 2.29], [4.88, 0.48, 0.38], 'gold');
}

box('stem_socket', [8, 12.15, 8], [1.3, 0.88, 1.4], 'rind');
box('stem_socket_pith', [8, 12.57, 8], [0.89, 0.36, 0.99], 'pith');
box('woody_stem', [8.2, 13.5, 8], [0.66, 2.35, 0.72], 'wood', ['z', -22.5]);
box('stem_cut_end', [8.65, 14.59, 8], [0.62, 0.3, 0.67], 'woodCut', ['z', -22.5]);
box('leaf_petiole', [9.02, 13.75, 8], [1.5, 0.28, 0.3], 'wood', ['z', 22.5]);
const leafOrigin = [9.1, 13.78, 8];
for (const [name, from, to] of [
  ['leaf_root', [9.1, 13.52, 7.8], [9.85, 14.04, 8.2]],
  ['leaf_blade', [9.8, 12.93, 7.75], [11.75, 14.63, 8.25]],
  ['leaf_taper', [11.7, 13.23, 7.79], [12.65, 14.33, 8.21]],
  ['leaf_point', [12.6, 13.59, 7.84], [13.17, 13.97, 8.16]]
]) {
  const center = from.map((value, index) => (value + to[index]) / 2);
  const leaf = box(name, center, from.map((value, index) => to[index] - value), 'leaf', ['z', 22.5]);
  leaf.rotation.origin = leafOrigin;
}
box('calyx_hub', [8, 3.12, 8], [1.04, 0.66, 1.05], 'wood');
box('calyx_left', [7.27, 2.98, 8], [1.4, 0.34, 0.55], 'wood', ['z', -22.5]);
box('calyx_right', [8.73, 2.98, 8], [1.4, 0.34, 0.55], 'wood', ['z', 22.5]);
box('calyx_front', [8, 2.99, 7.32], [0.46, 0.3, 1.15], 'rind', ['x', 22.5]);
box('calyx_back', [8, 2.99, 8.68], [0.46, 0.3, 1.15], 'rind', ['x', -22.5]);

const palettes = {
  gold: ['5b361f', '93602a', 'c38c38', 'e8ba57', 'ffdf83', 'fff1b4'],
  goldLight: ['875724', 'b67b31', 'd9a94f', 'efd17f', 'ffebad', 'fff5c9'],
  rind: ['4f3023', '795028', 'a06d30', 'bf8e3e', 'dcb35d', 'f2d889'],
  flesh: ['ae844d', 'cfb07a', 'e4cd9d', 'f5e7c2', 'fff4db', 'fffbea'],
  pith: ['b1915c', 'd3be8e', 'e9dbb5', 'f7efda', 'fffaec', 'fffff5'],
  cavity: ['4b2b21', '6d3d26', '92552d', 'ad7339', 'ce9d59', 'e3be80'],
  seed: ['251b20', '3b2424', '563129', '7b4a31', 'a87543', 'd0a268'],
  wood: ['302624', '4f3429', '704b32', '90683e', 'b99461', 'dab98a'],
  woodCut: ['4f3428', '775236', 'a1774c', 'c6a270', 'dcca96', 'efe0b8'],
  leaf: ['12382d', '23533c', '3d7751', '6a9a63', 'a3bd7c', 'd5dca0']
};
const colors = Object.fromEntries(Object.entries(palettes).map(([name, ramp]) => [name, ramp.map(color => [0, 2, 4].map(index => parseInt(color.slice(index, index + 2), 16)))]));
const directions = ['north', 'south', 'east', 'west', 'up', 'down'];
const faceDims = (part, face) => {
  const size = part.to.map((value, index) => value - part.from[index]);
  return ['east', 'west'].includes(face) ? [size[2], size[1]] : ['up', 'down'].includes(face) ? [size[0], size[2]] : [size[0], size[1]];
};
const islands = [];
parts.forEach((part, partIndex) => directions.forEach(face => {
  const dimensions = faceDims(part, face);
  islands.push({ partIndex, face, dimensions, width: dimensions[0] * density, height: dimensions[1] * density });
}));
let free = [{ x: 0, y: 0, w: atlasSize, h: atlasSize }];
for (const island of islands.slice().sort((first, second) => Math.max(second.width, second.height) - Math.max(first.width, first.height) || second.width * second.height - first.width * first.height)) {
  const width = Math.ceil(island.width) + 2;
  const height = Math.ceil(island.height) + 2;
  const candidates = free.filter(rect => rect.w >= width && rect.h >= height).sort((first, second) => Math.min(first.w - width, first.h - height) - Math.min(second.w - width, second.h - height) || first.w * first.h - second.w * second.h);
  if (!candidates.length) throw new Error(`Atlas exhausted at ${parts[island.partIndex].name}.${island.face}`);
  const chosen = candidates[0];
  const used = { x: chosen.x, y: chosen.y, w: width, h: height };
  island.x = used.x + 1;
  island.y = used.y + 1;
  const next = [];
  for (const rectangle of free) {
    if (used.x >= rectangle.x + rectangle.w || used.x + used.w <= rectangle.x || used.y >= rectangle.y + rectangle.h || used.y + used.h <= rectangle.y) {
      next.push(rectangle);
      continue;
    }
    if (used.x > rectangle.x) next.push({ x: rectangle.x, y: rectangle.y, w: used.x - rectangle.x, h: rectangle.h });
    if (used.x + used.w < rectangle.x + rectangle.w) next.push({ x: used.x + used.w, y: rectangle.y, w: rectangle.x + rectangle.w - used.x - used.w, h: rectangle.h });
    if (used.y > rectangle.y) next.push({ x: rectangle.x, y: rectangle.y, w: rectangle.w, h: used.y - rectangle.y });
    if (used.y + used.h < rectangle.y + rectangle.h) next.push({ x: rectangle.x, y: used.y + used.h, w: rectangle.w, h: rectangle.y + rectangle.h - used.y - used.h });
  }
  free = next.filter((rectangle, index) => !next.some((other, otherIndex) => index !== otherIndex && rectangle.x >= other.x && rectangle.y >= other.y && rectangle.x + rectangle.w <= other.x + other.w && rectangle.y + rectangle.h <= other.y + other.h && (rectangle.x !== other.x || rectangle.y !== other.y || rectangle.w !== other.w || rectangle.h !== other.h || index > otherIndex)));
}

const canvas = makeCanvas(atlasSize, atlasSize);
function paint(island, localX, localY) {
  const part = parts[island.partIndex];
  const material = part.material;
  const ramp = colors[material];
  const width = island.width;
  const height = island.height;
  const pixelX = Math.max(0, Math.min(Math.ceil(width) - 1, Math.floor(localX)));
  const pixelY = Math.max(0, Math.min(Math.ceil(height) - 1, Math.floor(localY)));
  const horizontal = (pixelX + 0.5) / Math.max(1, Math.ceil(width));
  const vertical = (pixelY + 0.5) / Math.max(1, Math.ceil(height));
  const directional = { up: 1, north: 0, south: -0.25, east: -0.7, west: -0.3, down: -1 }[island.face];
  let tone = 3 + directional + (horizontal < 0.23 ? 0.5 : horizontal > 0.78 ? -0.65 : 0);
  if (material.startsWith('gold') || material === 'rind') {
    if (vertical < 0.22) tone += 0.8;
    if (vertical > 0.76) tone -= 1.05;
    if (horizontal > 0.55 && horizontal < 0.72) tone -= 0.65;
    if ((pixelX + island.partIndex * 3) % 9 === 1 && pixelY === Math.max(1, Math.floor(height * 0.68))) tone -= 1;
    if (pixelY === 0 && width > 2) tone += 0.6;
  } else if (material === 'flesh' || material === 'pith') {
    if (pixelY === 0) tone += 0.8;
    if (vertical > 0.78) tone -= 0.6;
    if ((pixelX + Math.floor(pixelY / 4) + island.partIndex) % 5 === 0 && pixelY % 5 !== 0) tone -= 0.8;
    if ((pixelX - Math.floor(pixelY / 3) + 23) % 11 === 2) tone += 0.7;
  } else if (material === 'cavity') {
    tone = 1 + 1.4 * Math.abs(horizontal - 0.5) * 2 + (vertical > 0.82 ? 0.7 : 0);
    if (pixelX === 0 || pixelX === Math.ceil(width) - 1) tone += 0.8;
    if (pixelX === Math.floor(width / 2) && pixelY > 2) tone -= 0.6;
  } else if (material === 'seed') {
    tone = 1.4 + (horizontal < 0.5 ? 1 : 0) - (vertical > 0.68 ? 0.7 : 0);
    if (pixelX === 0 && pixelY === 1) tone = 4;
    if (pixelX === 0 && pixelY === 0) tone = 3;
  } else if (material.startsWith('wood')) {
    tone = 2.4 + (pixelX % 3 === 0 ? -1 : 0.3) + (vertical < 0.2 ? 0.9 : 0);
    if (material === 'woodCut') tone = pixelX === 0 || pixelY === 0 ? 2 : 4;
  } else if (material === 'leaf') {
    tone = vertical < 0.5 ? 3.1 : 2.2;
    const mid = Math.floor(height / 2);
    if (pixelY === mid) tone = 4;
    else if ((pixelX + Math.abs(pixelY - mid)) % 4 === 1) tone = 3.8;
    if (pixelY === 0 || pixelY === Math.ceil(height) - 1) tone -= 1;
  }
  return ramp[Math.max(0, Math.min(5, Math.round(tone)))];
}
for (const island of islands) {
  for (let localY = -1; localY < Math.ceil(island.height) + 1; localY++) {
    for (let localX = -1; localX < Math.ceil(island.width) + 1; localX++) canvas.put(island.x + localX, island.y + localY, paint(island, localX, localY));
  }
}
const elements = parts.map(part => ({ name: part.name, from: part.from, to: part.to, ...(part.rotation ? { rotation: part.rotation } : {}), faces: {} }));
for (const island of islands) {
  elements[island.partIndex].faces[island.face] = { uv: [island.x, island.y, island.x + island.width, island.y + island.height].map(value => round(value / atlasSize * 16)), texture: '#atlas' };
}
const display = {
  gui: { rotation: [12, -25, 0], translation: [0, 0, 0], scale: [0.88, 0.88, 0.88] },
  ground: { rotation: [0, 0, -10], translation: [0, 2.5, 0], scale: [0.48, 0.48, 0.48] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.75, 0.75, 0.75] },
  head: { rotation: [0, 180, 0], translation: [0, 11, 0], scale: [0.9, 0.9, 0.9] },
  thirdperson_righthand: { rotation: [0, -80, -12], translation: [0, 1.25, -0.6], scale: [0.61, 0.61, 0.61] },
  thirdperson_lefthand: { rotation: [0, 80, 12], translation: [0, 1.25, -0.6], scale: [0.61, 0.61, 0.61] },
  firstperson_righthand: { rotation: [0, -25, -9], translation: [1.4, 1, 0], scale: [0.72, 0.72, 0.72] },
  firstperson_lefthand: { rotation: [0, 25, 9], translation: [1.4, 1, 0], scale: [0.72, 0.72, 0.72] }
};
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_materials/apple', particle: 'ftboss:item/apple_material' }, elements, display };
const wrapper = { loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/apple_material' }, base: { parent: 'ftboss:item/boss_materials/apple_material' }, perspectives: { gui: { parent: 'ftboss:item/boss_materials/apple_material_gui' } } };
const gui = { parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/apple_material' } };
const atlas = encodePNG(atlasSize, atlasSize, canvas.buf);
const uuid = label => {
  const digest = crypto.createHash('sha256').update(`ftboss:apple_material:${label}`).digest('hex');
  return `${digest.slice(0, 8)}-${digest.slice(8, 12)}-4${digest.slice(13, 16)}-a${digest.slice(17, 20)}-${digest.slice(20, 32)}`;
};
const bbElements = elements.map(element => ({
  name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to,
  origin: element.rotation ? element.rotation.origin : [8, 8, 8],
  rotation: ['x', 'y', 'z'].map(axis => element.rotation && element.rotation.axis === axis ? element.rotation.angle : 0),
  rescale: false, autouv: 0, color: ['gold', 'rind'].includes(parts.find(part => part.name === element.name).material) ? 4 : 2,
  faces: Object.fromEntries(Object.entries(element.faces).map(([face, data]) => [face, { uv: data.uv.map(value => round(value / 16 * atlasSize)), texture: 0 }]))
}));
const bb = {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '金苹果核 · 蜜脆原材料', model_identifier: 'ftboss:apple_material',
  visible_box: [1.5, 1.5, 0.5], resolution: { width: atlasSize, height: atlasSize }, elements: bbElements,
  outliner: [{ name: 'golden_apple_core', uuid: uuid('group'), origin: [8, 8, 8], rotation: [0, 0, 0], children: bbElements.map(element => element.uuid) }], display,
  textures: [{ name: 'apple.png', id: '0', uuid: uuid('atlas'), mode: 'bitmap', source: `data:image/png;base64,${atlas.toString('base64')}`, path: '', width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, particle: true }]
};
const json = value => JSON.stringify(value, null, 2) + '\n';
fs.mkdirSync(art, { recursive: true });
writeSafe(path.join(resources, 'textures/item/boss_materials/apple.png'), atlas);
writeSafe(path.join(resources, 'models/item/boss_materials/apple_material.json'), json(model));
writeSafe(path.join(resources, 'models/item/boss_materials/apple_material_gui.json'), json(gui));
writeSafe(path.join(resources, 'models/item/apple_material.json'), json(wrapper));
writeSafe(path.join(root, 'art/boss_materials/blockbench/apple_material.bbmodel'), json(bb));
writeSafe(path.join(art, 'construction.json'), json({ density, atlasSize, parts, islands }));
console.log(`APPLE ${elements.length} elements; ${islands.length} unique face islands; ${atlasSize}x${atlasSize}; ${density} px/unit`);
