const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const { decodePNG } = require('../../png.cjs');
const { root, art, modelFile, atlasFile, render, save, outline, canvas, blit, text } = require('./render.cjs');

const parts = [];
const round = value => +value.toFixed(5);
function cube(name, from, to, material, rotation) {
  parts.push({ name, from: from.map(round), to: to.map(round), material, ...(rotation ? { rotation } : {}) });
}
const rotate = (axis, angle, origin) => ({ axis, angle, origin, rescale: false });

const outerBud = [[5.65, 6.95, 2.15, 7.15, 9.1], [6.9, 8.05, 3.5, 6.75, 9.3], [8, 9.35, 4.05, 6.6, 9.4], [9.3, 10.5, 3.35, 6.8, 9.25], [10.45, 11.5, 2.15, 7.05, 9], [11.45, 12.05, 0.8, 7.35, 8.75]];
outerBud.forEach(([bottom, top, width, front, back], index) => cube('heart_bast_layer_' + index, [8.2 - width / 2, bottom, front], [8.2 + width / 2, top, back], 'husk'));
const innerBud = [[6.45, 7.4, 1.4], [7.35, 8.5, 2.5], [8.45, 9.5, 2.65], [9.45, 10.45, 1.8], [10.4, 10.95, 0.7]];
innerBud.forEach(([bottom, top, width], index) => cube('inner_tea_heart_' + index, [8.2 - width / 2, bottom, 6.25 + index * 0.012], [8.2 + width / 2, top, 7.22 + index * 0.012], 'heart'));
cube('heart_inner_splice', [7.8, 6.8, 6.14], [8.4, 9.7, 6.52], 'sap', rotate('z', -22.5, [8.1, 8.25, 6.35]));
cube('heart_sap_ridge', [8.5, 7.2, 6.13], [8.85, 8.65, 6.5], 'sap');

function leaf(name, origin, angle, length, widths, depth, material) {
  const stride = length / widths.length;
  const rotation = rotate('z', angle, origin);
  widths.forEach((width, index) => {
    const lower = origin[1] + index * stride;
    const curvedDepth = depth + index * 0.025;
    cube(name + '_lamina_' + (index + 1), [origin[0] - width / 2, lower - 0.045, curvedDepth], [origin[0] + width / 2, lower + stride + 0.045, curvedDepth + 0.57], material, rotation);
    parts[parts.length - 1].leaf = { origin, length, lower, width, stride };
    if (index > 0 && index < widths.length - 2) {
      for (const side of [-1, 1]) {
        const edgeX = origin[0] + side * (width / 2 - 0.11);
        cube(name + '_rolled_edge_' + index + '_' + side, [edgeX - 0.14, lower - 0.05, curvedDepth - 0.16], [edgeX + 0.14, lower + stride + 0.05, curvedDepth + 0.2], 'backleaf', rotation);
      }
    }
  });
  cube(name + '_midrib', [origin[0] - 0.11, origin[1] - 0.2, depth - 0.13], [origin[0] + 0.11, origin[1] + length - 0.65, depth + 0.2], 'vein', rotation);
}

leaf('left_guard_leaf', [7.6, 5.8, 7], 45, 7.4, [1, 2.8, 3.2, 2.35, 0.7], 6.5, 'leaf');
leaf('right_guard_leaf', [9.25, 5.35, 8], -22.5, 8, [1, 2.65, 3.15, 2.2, 0.7], 7.25, 'leaf');
leaf('rear_sheath_left', [7.8, 5.45, 8.6], 22.5, 7, [1, 2.45, 2.8, 1.95, 0.65], 9.05, 'backleaf');
leaf('rear_sheath_right', [9.2, 5.65, 8.6], -45, 6.3, [1, 2.25, 2.65, 1.8, 0.65], 9.1, 'backleaf');
leaf('folded_front_collar', [7.8, 4.2, 7], 22.5, 3.3, [0.8, 1.8, 1.3, 0.4], 6.05, 'leaf');
cube('folded_leaf_lip', [10.9, 10.3, 7.15], [11.65, 12.5, 7.8], 'backleaf', rotate('y', 22.5, [11.2, 11.3, 7.4]));

for (let index = 0; index < 6; index++) {
  const positionX = 6.8 + index * 0.55;
  const positionZ = (index % 2 ? 8.2 : 7.35) + index * 0.018;
  cube('bast_bundle_' + (index + 1), [positionX, 1.55 + index % 3 * 0.24, positionZ], [positionX + 0.48, 6.3, positionZ + 0.56], 'fiber', rotate('z', index < 3 ? -22.5 : 22.5, [positionX + 0.24, 4.15, positionZ + 0.28]));
}
cube('cut_fiber_rear_left', [6.55, 2.15, 8.65], [7.15, 6.25, 9.2], 'fiber', rotate('z', -22.5, [6.85, 4.6, 8.9]));
cube('cut_fiber_rear_right', [8.9, 1.9, 8.7], [9.5, 6.35, 9.2], 'fiber', rotate('z', 22.5, [9.2, 4.6, 8.9]));

for (let index = 0; index < 2; index++) {
  const height = 4.05 + index * 0.65;
  cube('binding_front_' + (index + 1), [6.35, height, 6.7 - index * 0.025], [10.05, height + 0.46, 7.17 - index * 0.025], 'binding', rotate('z', index ? -22.5 : 22.5, [8.2, height + 0.23, 6.93]));
  cube('binding_back_' + (index + 1), [6.35, height, 9.25 + index * 0.025], [10.05, height + 0.46, 9.68 + index * 0.025], 'binding', rotate('z', index ? 22.5 : -22.5, [8.2, height + 0.23, 9.46]));
  cube('binding_left_turn_' + (index + 1), [6.25 - index * 0.025, height - 0.15, 7.02 - index * 0.025], [6.75 - index * 0.025, height + 0.55, 9.48 + index * 0.025], 'binding');
  cube('binding_right_turn_' + (index + 1), [9.62 + index * 0.025, height - 0.15, 7.02 - index * 0.025], [10.12 + index * 0.025, height + 0.55, 9.48 + index * 0.025], 'binding');
}
cube('binding_knot_base', [7.9, 4.1, 6.3], [8.85, 5.1, 6.95], 'binding', rotate('z', 45, [8.37, 4.6, 6.6]));
cube('binding_knot_cross', [8, 4.25, 6.1], [8.75, 4.85, 6.8], 'binding', rotate('z', -22.5, [8.37, 4.55, 6.4]));
cube('binding_tail_left', [7.4, 2.7, 6.25], [7.8, 4.5, 6.65], 'binding', rotate('z', -22.5, [7.6, 4.25, 6.45]));
cube('binding_tail_right', [8.65, 3.15, 6.4], [9.05, 4.7, 6.82], 'binding', rotate('z', 22.5, [8.85, 4.45, 6.6]));

const colors = {
  leaf: ['263f36', '3d6a44', '668c52', '95b66f', 'c7d59a'],
  backleaf: ['223f37', '3a6045', '628257', '89a36a', 'bcc893'],
  vein: ['4c6945', '72935b', '91ac70', 'b5cb92', 'd2ddb0'],
  husk: ['3d553b', '6f8853', '9eb176', 'c0c995', 'dbdfb1'],
  heart: ['647b50', '91a66b', 'b6cb91', 'dce3b0', 'f3eccf'],
  sap: ['617b50', '91ad70', 'bed6a0', 'e3ebc3', 'f4efd9'],
  fiber: ['455443', '697858', '94a47b', 'c0c7a0', 'dedcba'],
  binding: ['564c37', '857547', 'af995c', 'd1be83', 'e2d1a1']
};
for (const palette of Object.values(colors)) palette.forEach((color, index) => { palette[index] = color.match(/../g).map(channel => parseInt(channel, 16)); });

const atlasSize = 128;
const density = 2;
const atlas = makeCanvas(atlasSize, atlasSize);
const islands = [];
for (const part of parts) {
  const [width, height, depth] = part.to.map((value, index) => value - part.from[index]);
  part.faces = {};
  for (const [face, dimensions] of Object.entries({ north: [width, height], south: [width, height], east: [depth, height], west: [depth, height], up: [width, depth], down: [width, depth] })) {
    islands.push({ part, face, dimensions, width: Math.max(1, Math.ceil(dimensions[0] * density)), height: Math.max(1, Math.ceil(dimensions[1] * density)) });
  }
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
  if (shelfTop + island.height + 2 >= atlasSize) throw new Error('Atlas overflow at ' + island.part.name);
  island.left = shelfLeft;
  island.top = shelfTop;
  shelfLeft += island.width + 2;
  shelfHeight = Math.max(shelfHeight, island.height);
  const palette = colors[island.part.material];
  const exactWidth = island.dimensions[0] * density;
  const exactHeight = island.dimensions[1] * density;
  for (let pixelY = -1; pixelY <= island.height; pixelY++) for (let pixelX = -1; pixelX <= island.width; pixelX++) {
    const boundedX = Math.min(island.width - 1, Math.max(0, pixelX));
    const boundedY = Math.min(island.height - 1, Math.max(0, pixelY));
    const horizontal = (boundedX + 0.5) / exactWidth;
    const vertical = (boundedY + 0.5) / exactHeight;
    const edge = Math.min(boundedX, boundedY, exactWidth - 1 - boundedX, exactHeight - 1 - boundedY);
    let shade = edge < 0.2 ? 1 : horizontal < 0.32 ? 3 : horizontal < 0.72 ? 2 : 1;
    const material = island.part.material;
    if (material === 'leaf' || material === 'backleaf') {
      shade = horizontal < 0.17 ? 3 : horizontal > 0.85 ? 1 : 2;
      if (island.part.leaf && ['north', 'south'].includes(island.face)) {
        const leaf = island.part.leaf;
        const offsetX = (horizontal - 0.5) * leaf.width;
        const offsetY = island.part.to[1] - (boundedY + 0.5) / density - leaf.origin[1];
        const veinPhase = ((offsetY - Math.abs(offsetX) * 0.8) / 1.25 % 1 + 1) % 1;
        if (veinPhase < 0.23) shade = 3;
        if (Math.abs(offsetX) < 0.18) shade = 3;
        if (Math.abs(offsetX) > leaf.width / 2 - 0.18) shade = 1;
      }
    } else if (material === 'heart' || material === 'husk') {
      const centerX = ['north', 'south'].includes(island.face) ? (island.part.to[0] - (boundedX + 0.5) / density - 8.2) / 4.1 : horizontal - 0.5;
      const centerY = ['north', 'south'].includes(island.face) ? (island.part.to[1] - (boundedY + 0.5) / density - 8.5) / 6.4 : vertical - 0.52;
      const ring = Math.floor(Math.sqrt(centerX * centerX * 1.35 + centerY * centerY) * 13) % 3;
      shade = ring === 0 ? 1 : ring === 1 ? 2 : 3;
      if (horizontal < 0.2 && vertical < 0.7) shade = Math.min(4, shade + 1);
      if (edge < 0.05) shade = 1;
    } else if (material === 'fiber') {
      shade = boundedX % 3 === 0 ? 1 : boundedX % 3 === 1 ? 3 : 2;
      if (['up', 'down'].includes(island.face)) shade = (boundedX + boundedY) % 3 === 0 ? 1 : 3;
      if (boundedY % 7 === 0 && boundedX % 2 === 0) shade = 2;
    } else if (material === 'binding') {
      shade = (boundedX + boundedY * 2) % 4 < 2 ? 2 : 3;
      if (edge < 0.1) shade = 1;
    } else if (material === 'vein' || material === 'sap') {
      shade = horizontal < 0.4 ? 3 : 2;
      if (boundedY % 5 === 0) shade = 4;
    }
    if (island.face === 'down') shade = Math.max(0, shade - 1);
    if (island.face === 'up') shade = Math.min(4, shade + 1);
    atlas.put(island.left + pixelX, island.top + pixelY, palette[shade]);
  }
  let coordinates = [island.left, island.top, island.left + exactWidth, island.top + exactHeight].map(value => round(value / atlasSize * 16));
  if (island.face === 'south') coordinates = [coordinates[2], coordinates[1], coordinates[0], coordinates[3]];
  island.part.faces[island.face] = { uv: coordinates, texture: '#tea' };
}

const display = {
  ground: { rotation: [0, 0, 0], translation: [0, 2.1, 0], scale: [0.57, 0.57, 0.57] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
  head: { rotation: [0, 180, 0], translation: [0, 10, 0], scale: [0.85, 0.85, 0.85] },
  thirdperson_righthand: { rotation: [0, -90, 8], translation: [0, 1.5, -1], scale: [0.65, 0.65, 0.65] },
  thirdperson_lefthand: { rotation: [0, 90, -8], translation: [0, 1.5, -1], scale: [0.65, 0.65, 0.65] },
  firstperson_righthand: { rotation: [0, -28, 8], translation: [1.5, 1.2, 0], scale: [0.75, 0.75, 0.75] },
  firstperson_lefthand: { rotation: [0, 28, -8], translation: [1.5, 1.2, 0], scale: [0.75, 0.75, 0.75] }
};
const elements = parts.map(({ material, leaf, ...part }) => part);
const model = { ambientocclusion: false, gui_light: 'front', textures: { tea: 'ftboss:item/boss_materials/tea', particle: 'ftboss:item/tea_material' }, elements, display };
const json = value => JSON.stringify(value, null, 2) + String.fromCharCode(10);
writeSafe(modelFile, json(model));
atlas.save(atlasFile);
const finalModel = JSON.parse(fs.readFileSync(modelFile));
const finalAtlas = decodePNG(atlasFile);
const icon = outline(render(finalModel, finalAtlas, { width: 32, height: 32, yaw: -18, pitch: 12, scale: 1.78 }));
save(path.join(root, 'src/main/resources/assets/ftboss/textures/item/tea_material.png'), icon);

const uuid = name => {
  const hex = crypto.createHash('sha256').update('tea-refine:' + name).digest('hex');
  return hex.slice(0, 8) + '-' + hex.slice(8, 12) + '-4' + hex.slice(13, 16) + '-a' + hex.slice(17, 20) + '-' + hex.slice(20, 32);
};
const textureId = uuid('atlas');
const bbElements = elements.map(element => ({
  name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to, box_uv: false,
  origin: element.rotation ? element.rotation.origin : [8, 8, 8],
  rotation: ['x', 'y', 'z'].map(axis => element.rotation && element.rotation.axis === axis ? element.rotation.angle : 0),
  rescale: element.rotation ? element.rotation.rescale : false,
  faces: Object.fromEntries(Object.entries(element.faces).map(([direction, face]) => [direction, { uv: face.uv.map(value => round(value * atlasSize / 16)), texture: 0 }]))
}));
const groupNames = ['heart', 'leaf', 'fiber', 'binding'];
const groups = groupNames.map(group => ({ name: group, uuid: uuid('group:' + group), origin: [8, 8, 8], children: parts.filter(part => group === 'heart' ? ['husk', 'heart', 'sap'].includes(part.material) : group === 'leaf' ? ['leaf', 'backleaf', 'vein'].includes(part.material) : group === 'fiber' ? part.material === 'fiber' : part.material === 'binding').map(part => uuid(part.name)) }));
const bbmodel = {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '茶心纤维 / Saya tea heart', model_identifier: 'tea_material',
  resolution: { width: atlasSize, height: atlasSize }, visible_box: [2, 2, 0], ambientocclusion: false, front_gui_light: true,
  elements: bbElements, outliner: groups, display,
  textures: [{ name: 'tea.png', id: '0', uuid: textureId, mode: 'bitmap', source: 'data:image/png;base64,' + fs.readFileSync(atlasFile).toString('base64'), path: '', width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, particle: true }]
};
writeSafe(path.join(root, 'art/boss_materials/blockbench/tea_material.bbmodel'), json(bbmodel));
writeSafe(path.join(root, '_work/boss-materials-refine-20261021/tea/design_manifest.json'), json({ elements: parts.length, rotated: parts.filter(part => part.rotation).length, atlas: [atlasSize, atlasSize], density, islands: islands.map(({ part, face, dimensions, left, top, width, height }) => ({ name: part.name, material: part.material, face, dimensions, left, top, width, height })) }));

const references = canvas(1050, 600);
text(references, 'READ-ONLY REFERENCE / ACTUAL SAYA SKIN', 25, 26, 3);
const actualSkin = decodePNG(path.join(root, 'src/main/resources/assets/ftboss/textures/entity/tea_alex.png'));
blit(references, actualSkin, 40, 90, 6);
blit(references, decodePNG(path.join(root, 'src/main/resources/assets/ftboss/textures/item/tea_seed.png')), 510, 120, 9);
const originalPendant = path.join(root, '_work/boss-relics-refine-20261021/tea/backup/src/main/resources/assets/ftboss/textures/item/tea_heart_pendant.png');
blit(references, decodePNG(fs.existsSync(originalPendant) ? originalPendant : path.join(root, 'src/main/resources/assets/ftboss/textures/item/tea_heart_pendant.png')), 755, 120, 9);
text(references, 'TEA_ALEX.PNG / ALEX PLAYER MODEL', 30, 520, 3);
text(references, 'MINT + DEEP GREEN / WARM BAST ACCENTS', 30, 558, 3);
save(path.join(art, 'references.png'), references);
console.log('Generated tea only: ' + parts.length + ' elements, ' + parts.filter(part => part.rotation).length + ' rotated, ' + islands.length + ' per-face UV islands, 128px atlas.');
