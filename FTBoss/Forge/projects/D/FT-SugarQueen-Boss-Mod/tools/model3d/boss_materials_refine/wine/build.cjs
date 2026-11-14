const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const ROOT = path.resolve(__dirname, '../../../..');
const ASSET = path.join(ROOT, 'src/main/resources/assets/ftboss');
const ART = path.join(ROOT, 'art/boss_materials/refine/wine');
const WORK = path.join(ROOT, '_work/boss-materials-refine-20261021/wine');
const MODEL = path.join(ASSET, 'models/item/boss_materials/wine_material.json');
const ATLAS = path.join(ASSET, 'textures/item/boss_materials/wine.png');
const BB = path.join(ROOT, 'art/boss_materials/blockbench/wine_material.bbmodel');
for (const directory of [ART, WORK]) fs.mkdirSync(directory, { recursive: true });
const elements = [];
const metadata = [];
const clean = value => Number(value.toFixed(6));
function cube(name, center, size, material, axis, angle, group = 'crystal') {
  const element = {
    name,
    from: center.map((value, index) => clean(value - size[index] / 2)),
    to: center.map((value, index) => clean(value + size[index] / 2))
  };
  if (angle) element.rotation = { origin: center, axis, angle, rescale: false };
  element.faces = {};
  elements.push(element);
  metadata.push({ material, group });
  return element;
}
const crystalCenter = [8, 7.5, 8];
cube('wine_long_front_back', crystalCenter, [10.7, 3.4, 5.2], 'wine');
cube('wine_upper_lower_table', crystalCenter, [10.4, 5.2, 3.4], 'wine');
for (const signY of [-1, 1]) {
  for (const signZ of [-1, 1]) {
    const angle = signY === signZ ? 45 : -45;
    cube('wine_cut_' + signY + '_' + signZ,
      [8, 7.5 + signY * 1.66, 8 + signZ * 1.66],
      [10.2, 1.4, 1.29], 'facet', 'x', angle);
  }
}
cube('oak_cradle_main', [8, 4.76, 8], [10.45, 0.42, 2.75], 'oak', null, 0, 'cradle');
cube('oak_cradle_left_end', [3.04, 5.06, 8], [0.48, 0.42, 3.1], 'oak', 'z', -22.5, 'cradle');
cube('oak_cradle_right_end', [12.96, 5.06, 8], [0.48, 0.42, 3.1], 'oak', 'z', 22.5, 'cradle');
function ring(prefix, centerX, width, material, half = 2.7, thick = 0.24) {
  const corner = half - 0.46;
  cube(prefix + '_top', [centerX, 7.5 + half, 8], [width, thick, 3.48], material, null, 0, 'bindings');
  cube(prefix + '_bottom', [centerX, 7.5 - half, 8], [width, thick, 3.48], material, null, 0, 'bindings');
  cube(prefix + '_front', [centerX, 7.5, 8 - half], [width, 3.48, thick], material, null, 0, 'bindings');
  cube(prefix + '_back', [centerX, 7.5, 8 + half], [width, 3.48, thick], material, null, 0, 'bindings');
  for (const signY of [-1, 1]) {
    for (const signZ of [-1, 1]) {
      cube(prefix + '_bevel_' + signY + '_' + signZ,
        [centerX, 7.5 + signY * corner, 8 + signZ * corner],
        [width, thick, 1.39], material, 'x', signY === signZ ? 45 : -45, 'bindings');
    }
  }
}
for (const [label, centerX] of [['left', 4.55], ['right', 11.45]]) {
  ring('iron_' + label, centerX, 0.83, 'iron');
  for (const sign of [-1, 1]) ring('copper_' + label + '_' + sign, centerX + sign * 0.356, 0.14, 'copper', 2.728, 0.27);
  for (const [side, centerZ] of [['front', 5.1], ['back', 10.9]]) {
    for (const centerY of [6.3, 8.7]) {
      cube('rivet_' + label + '_' + side + '_' + centerY, [centerX, centerY, centerZ], [0.36, 0.36, 0.19], 'copper', 'z', 45, 'hardware');
    }
  }
  cube('clasp_' + label, [centerX, 10.38, 8], [1.13, 0.25, 1.18], 'iron', null, 0, 'hardware');
  cube('clasp_pin_' + label, [centerX, 10.545, 8], [0.28, 0.12, 0.78], 'copper', null, 0, 'hardware');
}
cube('wine_raw_crown', [7.6, 10.05, 8.5], [2.1, 2.1, 1.75], 'facet', 'z', 45);
cube('wine_raw_side_shard', [9.0, 10.12, 8.75], [1.28, 2.04, 1.26], 'wine', 'z', -22.5);
cube('wine_raw_front_shard', [6.7, 9.74, 7.42], [1.2, 1.4, 1.16], 'facet', 'z', 22.5);
cube('wax_pool_on_table', [8.0, 10.17, 7.45], [1.36, 0.24, 1.94], 'wax', null, 0, 'wax');
cube('wax_over_bevel', [8.0, 9.74, 6.03], [0.93, 0.28, 1.6], 'wax', 'x', -45, 'wax');
cube('wax_down_front', [8.0, 8.85, 5.23], [0.91, 1.39, 0.32], 'wax', null, 0, 'wax');
cube('wax_seal_middle', [8, 7.42, 4.99], [3.08, 1.65, 0.65], 'wax', null, 0, 'wax');
cube('wax_seal_top', [8, 8.60, 4.97], [1.65, 0.73, 0.66], 'wax', null, 0, 'wax');
cube('wax_seal_bottom', [8, 6.24, 4.97], [1.65, 0.73, 0.66], 'wax', null, 0, 'wax');
for (const signX of [-1, 1]) {
  for (const signY of [-1, 1]) {
    cube('wax_seal_bevel_' + signX + '_' + signY,
      [8 + signX * 0.99, 7.42 + signY * 0.99, 4.97],
      [0.53, 1.04, 0.63], 'wax', 'z', signX === signY ? 45 : -45, 'wax');
  }
}
cube('wax_droplet', [8.53, 5.96, 5.17], [0.42, 0.7, 0.29], 'wax', 'z', 22.5, 'wax');
cube('seal_recessed_table', [8, 7.43, 4.598], [1.83, 1.96, 0.15], 'waxDark', null, 0, 'wax');
const berries = [[-0.4,0.32],[0.07,0.32],[0.52,0.32],[-0.18,-0.13],[0.3,-0.13],[0.08,-0.56]];
for (const [index, berry] of berries.entries()) {
  cube('grape_emboss_' + index, [8 + berry[0], 7.44 + berry[1], 4.456], [0.34, 0.34, 0.18], 'emboss', 'z', 45, 'imprint');
}
cube('grape_vine_stem', [8.02, 8.18, 4.46], [0.17, 0.53, 0.18], 'emboss', 'z', -22.5, 'imprint');
cube('grape_vine_leaf', [7.62, 8.17, 4.46], [0.46, 0.3, 0.18], 'emboss', 'z', -22.5, 'imprint');
cube('rear_vintage_plate', [8, 7.5, 10.7], [2.52, 1.48, 0.30], 'vintage', null, 0, 'hardware');
for (const centerX of [7.46, 8, 8.54]) {
  cube('vintage_iii_' + centerX, [centerX, 7.52, 10.89], [0.13, 0.79, 0.13], 'iron', null, 0, 'hardware');
}
const faceDimensions = (element, face) => {
  const size = element.to.map((value, index) => value - element.from[index]);
  if (face === 'north' || face === 'south') return [size[0], size[1]];
  if (face === 'east' || face === 'west') return [size[2], size[1]];
  return [size[0], size[2]];
};
const faces = [];
for (const [index, element] of elements.entries()) {
  for (const face of ['north', 'south', 'east', 'west', 'up', 'down']) {
    const [width, height] = faceDimensions(element, face);
    faces.push({ index, face, width: width * 2, height: height * 2, material: metadata[index].material });
  }
}
function pack(side) {
  let positionX = 1, positionY = 1, rowHeight = 0;
  for (const item of [...faces].sort((left, right) => right.height - left.height || right.width - left.width)) {
    const width = Math.ceil(item.width) + 2, height = Math.ceil(item.height) + 2;
    if (positionX + width >= side) { positionX = 1; positionY += rowHeight; rowHeight = 0; }
    if (positionY + height >= side) return false;
    item.left = positionX + 1; item.top = positionY + 1;
    positionX += width; rowHeight = Math.max(rowHeight, height);
  }
  return true;
}
let atlasSize = 128;
if (!pack(atlasSize)) { atlasSize = 256; if (!pack(atlasSize)) throw new Error('Atlas overflow'); }
const canvas = makeCanvas(atlasSize, atlasSize);
const palettes = {
  wine: [[43,18,33],[99,30,50],[152,52,69],[224,121,115]],
  facet: [[54,20,40],[117,37,61],[180,70,88],[241,154,140]],
  iron: [[29,31,40],[54,59,68],[89,99,111],[148,157,163]],
  copper: [[69,38,30],[130,79,42],[191,136,70],[232,195,127]],
  emboss: [[93,51,36],[164,109,53],[213,164,89],[248,217,145]],
  wax: [[75,19,32],[147,39,49],[190,63,63],[225,111,88]],
  waxDark: [[64,18,30],[108,26,40],[143,41,49],[180,69,61]],
  oak: [[40,26,26],[82,47,29],[115,72,37],[157,111,59]],
  vintage: [[69,41,29],[123,77,41],[172,119,62],[210,167,107]]
};
function paint(item, positionX, positionY) {
  const width = item.width, height = item.height;
  const localX = Math.min(width - 0.001, Math.max(0.001, positionX + 0.5));
  const localY = Math.min(height - 0.001, Math.max(0.001, positionY + 0.5));
  const across = localX / width, down = localY / height;
  const palette = palettes[item.material];
  const edge = Math.min(localX, localY, width - localX, height - localY);
  const faceBias = { up: 0.13, north: 0.03, south: -0.1, west: -0.04, east: 0.01, down: -0.13 }[item.face];
  let tone = 0.46 + faceBias + (0.5 - down) * 0.16;
  if (item.material === 'wine' || item.material === 'facet') {
    const diagonal = across * 0.75 + down * 0.5;
    tone += diagonal < 0.45 ? 0.17 : diagonal > 0.87 ? -0.11 : 0.02;
    const crack = Math.abs(down - (0.67 - across * 0.38 + (item.index % 3) * 0.035));
    if (width > 8 && height > 4 && crack < 0.065) tone = 0.24;
    if (width > 8 && height > 4 && crack >= 0.065 && crack < 0.115) tone = 0.74;
    if (edge < 0.52 && (across < 0.22 || down < 0.26)) tone = 0.9;
  } else if (item.material === 'iron' || item.material === 'copper' || item.material === 'emboss') {
    tone += [0.1, -0.04, 0.08, -0.08][Math.floor(across * 4) % 4];
    if (edge < 0.63) tone = down < 0.5 ? 0.84 : 0.2;
    if (Math.floor(localY + item.index) % 7 === 0 && Math.floor(localX) % 3 === 1) tone -= 0.13;
  } else if (item.material === 'oak') {
    const grain = (Math.floor(localY) * 3 + Math.floor(localX / 5)) % 6;
    tone += grain === 0 ? -0.2 : grain === 2 ? 0.13 : 0;
    if (edge < 0.6) tone -= 0.14;
  } else if (item.material.startsWith('wax')) {
    tone += Math.floor(across * 7 + Math.sin(down * 6) * 0.5) % 3 === 0 ? 0.09 : -0.03;
    if (edge < 0.65) tone = down < 0.45 ? 0.72 : 0.15;
  } else {
    if (edge < 0.7) tone = down < 0.5 ? 0.83 : 0.15;
    if (down > 0.72 && across > 0.18 && across < 0.82) tone = 0.25;
  }
  tone = Math.max(0, Math.min(0.999, tone));
  return palette[Math.min(3, Math.floor(tone * 4))];
}
for (const item of faces) {
  for (let positionY = -1; positionY <= Math.ceil(item.height); positionY++) {
    for (let positionX = -1; positionX <= Math.ceil(item.width); positionX++) {
      canvas.put(item.left + positionX, item.top + positionY, paint(item, positionX, positionY));
    }
  }
  let coordinates = [item.left, item.top, item.left + item.width, item.top + item.height].map(value => clean(value * 16 / atlasSize));
  if (item.face === 'north' || item.face === 'east') coordinates = [coordinates[2], coordinates[1], coordinates[0], coordinates[3]];
  elements[item.index].faces[item.face] = { uv: coordinates, texture: '#atlas' };
}
const display = {
  thirdperson_righthand: { rotation: [0, -45, 0], translation: [0, 1, 0], scale: [0.65, 0.65, 0.65] },
  thirdperson_lefthand: { rotation: [0, 45, 0], translation: [0, 1, 0], scale: [0.65, 0.65, 0.65] },
  firstperson_righthand: { rotation: [0, -30, 0], translation: [0, 1, 0], scale: [0.75, 0.75, 0.75] },
  firstperson_lefthand: { rotation: [0, 30, 0], translation: [0, 1, 0], scale: [0.75, 0.75, 0.75] },
  ground: { rotation: [0, 0, 0], translation: [0, 1.5, 0], scale: [0.52, 0.52, 0.52] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
  head: { rotation: [0, 0, 0], translation: [0, 8, 0], scale: [0.7, 0.7, 0.7] },
  gui: { rotation: [28, 225, 0], translation: [0, 0, 0], scale: [0.9, 0.9, 0.9] }
};
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_materials/wine', particle: 'ftboss:item/wine_material' }, elements, display };
canvas.save(ATLAS);
writeSafe(MODEL, JSON.stringify(model, null, 2) + '\n');
const uuid = label => {
  const value = crypto.createHash('sha256').update('ftboss:wine_refine:' + label).digest('hex');
  return value.slice(0,8) + '-' + value.slice(8,12) + '-4' + value.slice(13,16) + '-a' + value.slice(17,20) + '-' + value.slice(20,32);
};
const bbElements = elements.map((element, index) => {
  const rotation = [0,0,0];
  if (element.rotation) rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
  const result = { name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to,
    origin: element.rotation ? element.rotation.origin : [8,7.5,8], rotation,
    rescale: !!element.rotation?.rescale, shade: true, box_uv: false, autouv: 0, color: index % 8, faces: {} };
  for (const [face, data] of Object.entries(element.faces)) result.faces[face] = { uv: data.uv.map(value => clean(value * atlasSize / 16)), texture: 0 };
  return result;
});
const groups = [...new Set(metadata.map(item=>item.group))].map(group => ({ name: group, uuid: uuid('group_' + group), origin: [8,7.5,8], rotation: [0,0,0], children: bbElements.filter((element,index)=>metadata[index].group===group).map(element=>element.uuid) }));
const bbmodel = { meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: 'Wine aged crystal and wax seal', model_identifier: 'wine_material', visible_box: [1,1,0], resolution: { width: atlasSize, height: atlasSize }, ambientocclusion: false, front_gui_light: false, elements: bbElements, outliner: groups, textures: [{ path: ATLAS, name: 'wine.png', folder: 'item/boss_materials', namespace: 'ftboss', id: 'atlas', uuid: uuid('texture'), width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, source: 'data:image/png;base64,' + fs.readFileSync(ATLAS).toString('base64') }], display };
writeSafe(BB, JSON.stringify(bbmodel, null, 2) + '\n');
const manifest = { item: 'ftboss:wine_material', displayName: '陈酿酒封配件', design: 'Faceted aged-wine crystal, closed iron hoops with copper edge, embossed grape wax seal, oak cradle', elements: elements.length, rotated: elements.filter(element=>element.rotation).length, faces: faces.length, atlas: [atlasSize,atlasSize], density: 2, groups: metadata, uvIslands: faces.map(({index,face,width,height,left,top,material})=>({index,face,width,height,left,top,material})) };
writeSafe(path.join(WORK, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
console.log(JSON.stringify({ elements: elements.length, rotated: manifest.rotated, faces: faces.length, atlas: atlasSize, density: 2 }, null, 2));
