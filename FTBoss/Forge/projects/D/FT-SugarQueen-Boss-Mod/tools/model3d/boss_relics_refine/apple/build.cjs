const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const { encodePNG } = require('../../png.cjs');
const root = path.resolve(__dirname, '../../../..');
const art = path.join(root, 'art/boss_relics/refine/apple');
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

for (const [index, [left, right, bottom, top]] of [
  [5.6, 10.4, 4.4, 5.1], [4.85, 11.15, 5.1, 6.4], [4.3, 11.7, 6.4, 7.8],
  [3.9, 12.1, 7.8, 9.5], [4.15, 11.85, 9.5, 10.35], [4.72, 11.28, 10.35, 10.95]
].entries()) {
  box(`pressed_backplate_tier_${index}`, [(left + right) / 2, (bottom + top) / 2, 7.85], [right - left, top - bottom, 1.8], 'brass');
}
for (const side of [-1, 1]) {
  const label = side < 0 ? 'left' : 'right';
  box(`backplate_shoulder_${label}`, [8 + side * 1.75, 11.15, 7.84], [2.3, 0.46, 1.74], 'brass');
  box(`backplate_heel_${label}`, [8 + side * 1.05, 4.3, 7.83], [1.7, 0.48, 1.7], 'brass');
  box(`top_frame_lobe_${label}`, [8 + side * 2.1, 11.05, 7.3], [2.4, 0.62, 1.65], 'gold');
  box(`cleft_frame_${label}`, [8 + side * 0.58, 10.77, 7.31 + side * 0.025], [1.45, 0.5, 1.6], 'gold', ['z', side * 22.5]);
  box(`upper_shoulder_frame_${label}`, [8 + side * 3.42, 10.27, 7.29], [0.65, 2.0, 1.62], 'gold', ['z', side * 22.5]);
  box(`side_frame_${label}`, [8 + side * 3.87, 8.6, 7.28], [0.62, 2.0, 1.5], 'gold');
  box(`lower_side_frame_${label}`, [8 + side * 3.2, 6.48, 7.27], [0.65, 2.8, 1.56], 'gold', ['z', -side * 22.5]);
  box(`lower_curve_frame_${label}`, [8 + side * 1.85, 4.96, 7.26], [2.2, 0.64, 1.52], 'gold', ['z', -side * 22.5]);
  box(`bottom_frame_${label}`, [8 + side * 1.05, 4.45, 7.25], [1.45, 0.58, 1.48], 'gold');
  box(`shoulder_inner_facet_${label}`, [8 + side * 2.1, 10.12, 6.6], [2.32, 1.0, 0.64], 'goldLight');
  box(`upper_cleft_facet_${label}`, [8 + side * 0.57, 9.96, 6.52 + side * 0.025], [1.4, 0.4, 0.72], 'goldLight', ['z', side * 22.5]);
  box(`cheek_side_facet_${label}`, [8 + side * 2.98, 7.4, 6.47], [0.7, 2.8, 0.7], 'gold', ['z', -side * 22.5]);
  box(`lower_face_facet_${label}`, [8 + side * 1.95, 5.85, 6.55], [2.05, 0.64, 0.6], 'gold', ['z', -side * 22.5]);
}

box('apple_face_belly', [8, 7.55, 6.72], [6.35, 3.4, 0.58], 'honey');
box('apple_cheek_left', [6.48, 8.05, 6.24], [3.15, 3.14, 0.85], 'honey', ['y', -22.5]);
box('apple_cheek_right', [9.51, 8.02, 6.22], [3.15, 3.14, 0.85], 'honey', ['y', 22.5]);
box('apple_face_upper', [8, 9.43, 6.675], [5.9, 1.42, 0.76], 'honey');
box('apple_face_lower', [8, 5.76, 6.68], [4.7, 1.05, 0.64], 'honey');
box('apple_face_heel', [8, 5.0, 6.69], [3.1, 0.7, 0.46], 'gold');
box('right_cheek_sheen', [10.45, 8.96, 6.07], [0.21, 0.85, 0.18], 'goldLight');
for (const side of [-1, 1]) {
  const label = side < 0 ? 'left' : 'right';
  box(`engraved_seed_${label}`, [8 + side * 0.53, 6.6, 6.075], [0.24, 0.76, 0.13], 'engraving', ['z', side * 22.5]);
  box(`frame_rivet_upper_${label}`, [8 + side * 3.6, 9.75, 6.38], [0.35, 0.35, 0.26], 'goldLight', ['z', 45]);
  box(`frame_rivet_lower_${label}`, [8 + side * 2.65, 5.35, 6.37], [0.35, 0.35, 0.26], 'goldLight', ['z', 45]);
}
box('calyx_jewel_bezel', [8, 4.78, 6.44], [0.75, 0.75, 0.34], 'goldLight', ['z', 45]);
box('calyx_ruby', [8, 4.78, 6.2], [0.45, 0.45, 0.25], 'ruby', ['z', 45]);
box('gilt_stem_socket', [8.0, 10.75, 7.33], [0.95, 0.66, 1.1], 'rind');
box('gilt_stem', [8.21, 11.72, 7.47], [0.55, 2.0, 0.8], 'rind', ['z', -22.5]);
box('stem_cap', [8.58, 12.61, 7.47], [0.54, 0.25, 0.73], 'goldLight', ['z', -22.5]);
box('leaf_attachment', [8.87, 12.22, 7.65], [1.23, 0.4, 0.62], 'gold', ['z', 22.5]);
const leafOrigin = [8.7, 12.1, 7.6];
for (const [index, [left, right, bottom, top]] of [[8.85, 9.6, 11.86, 12.7], [9.5, 11.1, 11.45, 13.1], [11.0, 11.95, 11.67, 12.88], [11.85, 12.45, 11.99, 12.56]].entries()) {
  const frame = box(`leaf_gold_frame_${index}`, [(left + right) / 2, (bottom + top) / 2, 7.55 + index * 0.025], [right - left, top - bottom, 0.74], 'gold', ['z', 22.5]);
  frame.rotation.origin = leafOrigin;
  const enamel = box(`leaf_enamel_${index}`, [(left + right) / 2, (bottom + top) / 2, 7.12], [right - left - 0.14, top - bottom - 0.22, 0.31], 'leaf', ['z', 22.5]);
  enamel.rotation.origin = leafOrigin;
}
const vein = box('leaf_gold_midrib', [10.49, 12.26, 6.94], [3.1, 0.16, 0.16], 'goldLight', ['z', 22.5]);
vein.rotation.origin = leafOrigin;

box('hinge_mount', [5.55, 8.05, 8.96], [1.1, 1.48, 0.56], 'brass');
box('hinge_upper_lug', [5.55, 8.52, 9.38], [0.72, 0.46, 0.66], 'gold');
box('hinge_lower_lug', [5.55, 7.58, 9.38], [0.72, 0.46, 0.66], 'gold');
box('hinge_axle', [5.55, 8.05, 9.48], [0.37, 1.25, 0.39], 'pin');
box('hinge_screw_top', [5.55, 8.6, 9.76], [0.32, 0.26, 0.16], 'screw');
box('hinge_screw_bottom', [5.55, 7.5, 9.76], [0.32, 0.26, 0.16], 'screw');
box('steel_pin_shank', [7.97, 8.05, 9.51], [5.0, 0.26, 0.28], 'pin');
box('steel_pin_point', [10.52, 8.05, 9.51], [0.64, 0.15, 0.18], 'pin');
box('catch_mount', [10.5, 8.05, 8.98], [1.1, 1.12, 0.6], 'brass');
box('catch_lower_jaw', [10.5, 7.62, 9.4], [1.06, 0.3, 0.78], 'gold');
box('catch_upper_jaw', [10.5, 8.48, 9.4], [1.06, 0.3, 0.78], 'gold');
box('catch_protective_hood', [10.95, 8.05, 9.4], [0.27, 0.86, 0.82], 'gold');
box('back_hallmark_frame', [8, 6.0, 8.86], [1.65, 1.22, 0.24], 'rind');
box('back_hallmark_inset', [8, 6.0, 9.02], [1.28, 0.87, 0.18], 'brass');
box('back_hallmark_core', [8, 6.0, 9.13], [0.19, 0.65, 0.12], 'goldLight');
for (const side of [-1, 1]) box(`back_hallmark_seed_${side < 0 ? 'left' : 'right'}`, [8 + side * 0.36, 6.0, 9.13], [0.18, 0.45, 0.12], 'engraving', ['z', side * 22.5]);

const palettes = {
  honey: ['785027', 'b08034', 'd7a54b', 'edc66c', 'ffe19a', 'fff3c9'],
  brass: ['513b2b', '785c3b', 'a18550', 'bca36a', 'd3bd87', 'e8d9aa'],
  engraving: ['493125', '64432a', '906037', 'b48447', 'd9b270', 'ead198'],
  ruby: ['4d2531', '7b3040', 'ad4451', 'd26769', 'ef9590', 'ffd1ba'],
  pin: ['29363e', '4b5d66', '778a91', 'a2b6ba', 'ced9d5', 'f1f1df'],
  screw: ['39424a', '55646b', '82979a', 'a8bdbb', 'd9e1d4', 'f3f2dd'],
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
  if (material.startsWith('gold') || material === 'rind' || material === 'honey' || material === 'brass') {
    if (vertical < 0.22) tone += 0.8;
    if (vertical > 0.76) tone -= 1.05;
    if (horizontal > 0.55 && horizontal < 0.72) tone -= 0.65;
    if (material === 'honey' && part.name.includes('cheek')) {
      tone += part.name.endsWith('left') ? 0.25 : -0.15;
      if (vertical < 0.2 && horizontal > 0.18 && horizontal < 0.44) tone = 5;
      if (vertical > 0.73) tone -= 0.6;
    }
    if ((pixelX + island.partIndex * 3) % 9 === 1 && pixelY === Math.max(1, Math.floor(height * 0.68))) tone -= 1;
    if (pixelY === 0 && width > 2) tone += 0.6;
    if (material === 'brass' && part.name.startsWith('pressed_backplate')) {
      const worldY = part.to[1] - vertical * (part.to[1] - part.from[1]);
      tone = 2.5 + (worldY > 9.7 ? 0.8 : worldY < 5.3 ? -0.4 : 0) + (horizontal < 0.23 ? 0.3 : horizontal > 0.82 ? -0.3 : 0);
      if (island.face === 'up') tone = 4;
      if (island.face === 'down') tone = 2;
    }
  } else if (material === 'pin' || material === 'screw') {
    tone = horizontal < 0.35 ? 4 : horizontal < 0.7 ? 2 : 1;
    if (island.face === 'up') tone = 5;
    if (material === 'screw' && pixelY === Math.floor(height / 2)) tone = 0;
  } else if (material === 'ruby') {
    tone = horizontal < 0.3 ? 4 : vertical > 0.6 ? 1 : 2;
    if (pixelX === 0 && pixelY === 0) tone = 5;
  } else if (material === 'engraving') {
    tone = island.face === 'north' ? 1 : 2;
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
  elements[island.partIndex].faces[island.face] = { uv: [island.x, island.y, island.x + island.width, island.y + island.height].map(value => Number((value / atlasSize * 16).toFixed(5))), texture: '#atlas' };
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
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_relics/golden_apple_brooch', particle: 'ftboss:item/golden_apple_brooch' }, elements, display };
const wrapper = { loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/golden_apple_brooch' }, base: { parent: 'ftboss:item/boss_relics/golden_apple_brooch' }, perspectives: { gui: { parent: 'ftboss:item/boss_relics/golden_apple_brooch_gui' } } };
const gui = { parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/golden_apple_brooch' } };
const atlas = encodePNG(atlasSize, atlasSize, canvas.buf);
const uuid = label => {
  const digest = crypto.createHash('sha256').update(`ftboss:golden_apple_brooch:${label}`).digest('hex');
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
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '金苹果胸针 · 蜜脆金工', model_identifier: 'ftboss:golden_apple_brooch',
  visible_box: [1.5, 1.5, 0.5], resolution: { width: atlasSize, height: atlasSize }, elements: bbElements,
  outliner: [{ name: 'golden_apple_brooch', uuid: uuid('group'), origin: [8, 8, 8], rotation: [0, 0, 0], children: bbElements.map(element => element.uuid) }], display,
  textures: [{ name: 'golden_apple_brooch.png', id: '0', uuid: uuid('atlas'), mode: 'bitmap', source: `data:image/png;base64,${atlas.toString('base64')}`, path: '', width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, particle: true }]
};
const json = value => JSON.stringify(value, null, 2) + '\n';
fs.mkdirSync(art, { recursive: true });
writeSafe(path.join(resources, 'textures/item/boss_relics/golden_apple_brooch.png'), atlas);
writeSafe(path.join(resources, 'models/item/boss_relics/golden_apple_brooch.json'), json(model));
writeSafe(path.join(resources, 'models/item/boss_relics/golden_apple_brooch_gui.json'), json(gui));
writeSafe(path.join(resources, 'models/item/golden_apple_brooch.json'), json(wrapper));
writeSafe(path.join(root, 'art/boss_relics/blockbench/golden_apple_brooch.bbmodel'), json(bb));
writeSafe(path.join(art, 'construction.json'), json({ density, atlasSize, parts, islands }));
console.log(`GOLDEN APPLE BROOCH ${elements.length} elements; ${islands.length} unique face islands; ${atlasSize}x${atlasSize}; ${density} px/unit`);
