const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const ROOT = path.resolve(__dirname, '../../../..');
const ASSET = path.join(ROOT, 'src/main/resources/assets/ftboss');
const ART = path.join(ROOT, 'art/boss_relics/refine/wine');
const WORK = path.join(ROOT, '_work/boss-relics-refine-20261021/wine');
const MODEL = path.join(ASSET, 'models/item/boss_relics/aged_wine_seal.json');
const ATLAS = path.join(ASSET, 'textures/item/boss_relics/aged_wine_seal.png');
const BB = path.join(ROOT, 'art/boss_relics/blockbench/aged_wine_seal.bbmodel');
for (const directory of [ART, WORK]) fs.mkdirSync(directory, { recursive: true });
const elements = [];
const metadata = [];
const clean = value => Number(value.toFixed(6));
function cube(name, center, size, material, axis, angle, group = 'seal') {
  const element = {
    name,
    from: center.map((value, index) => clean(value - size[index] / 2)),
    to: center.map((value, index) => clean(value + size[index] / 2))
  };
  if (angle) element.rotation = { origin: center.map(clean), axis, angle, rescale: false };
  element.faces = {};
  elements.push(element);
  metadata.push({ material, group });
  return element;
}
function plate(prefix, center, radius, depth, material, group) {
  const flat = radius * (Math.SQRT2 - 1);
  cube(prefix + '_east_west', center, [radius * 2, flat * 2, depth], material, null, 0, group);
  cube(prefix + '_north_south', center, [flat * 2, radius * 2, depth + 0.035], material, null, 0, group);
  const radial = radius * 0.39;
  const offset = (radius + flat - radial * Math.SQRT1_2) / 2;
  const tangent = (radius - flat) * Math.SQRT2;
  for (const signX of [-1, 1]) {
    for (const signY of [-1, 1]) {
      cube(prefix + '_facet_' + signX + '_' + signY,
        [center[0] + signX * offset, center[1] + signY * offset, center[2] + 0.012],
        [radial, tangent, depth - 0.035], material, 'z', signX === signY ? 45 : -45, group);
    }
  }
}
function ring(prefix, center, radius, thickness, depth, material, group) {
  const middle = radius - thickness / 2;
  const flat = middle * (Math.SQRT2 - 1);
  const long = 2 * flat + 0.07;
  let layer = 0;
  const segmentDepth = () => clean(depth + (++layer) * 0.006);
  for (const sign of [-1, 1]) {
    cube(prefix + '_horizontal_' + sign, [center[0], center[1] + sign * middle, center[2]], [long, thickness, segmentDepth()], material, null, 0, group);
    cube(prefix + '_vertical_' + sign, [center[0] + sign * middle, center[1], center[2]], [thickness, long, segmentDepth()], material, null, 0, group);
  }
  for (const signX of [-1, 1]) {
    for (const signY of [-1, 1]) {
      const corner = (middle + flat) / 2;
      cube(prefix + '_diagonal_' + signX + '_' + signY,
        [center[0] + signX * corner, center[1] + signY * corner, center[2]],
        [thickness, (middle - flat) * Math.SQRT2 + 0.07, segmentDepth()], material,
        'z', signX === signY ? 45 : -45, group);
    }
  }
}
plate('forged_seal_back', [8, 6.9, 8], 3.42, 1.3, 'iron', 'body');
ring('cast_copper_frame', [8, 6.9, 7.63], 3.7, 0.42, 1.24, 'copper', 'frame');
ring('dark_outer_edge', [8, 6.9, 7.76], 3.8, 0.17, 0.95, 'iron', 'frame');
plate('wax_octagonal_seal', [8, 6.9, 7.19], 3.15, 0.47, 'wax', 'wax');
ring('polished_inner_lip', [8, 6.9, 7.02], 3.25, 0.19, 0.53, 'emboss', 'frame');
plate('wine_crystal_intaglio', [8, 6.9, 6.885], 2.48, 0.25, 'wine', 'inlay');
ring('wax_impression_rim', [8, 6.9, 6.91], 2.55, 0.13, 0.27, 'waxDark', 'wax');
for (const [label, centerX, centerY] of [
  ['north', 8, 10.39], ['south', 8, 3.41], ['east', 11.49, 6.9], ['west', 4.51, 6.9]
]) {
  cube('bezel_stud_' + label, [centerX, centerY, 6.99], [0.38, 0.38, 0.24], 'emboss', 'z', 45, 'hardware');
}
const berries = [[-0.72, 0.53], [0, 0.53], [0.72, 0.53], [-0.37, -0.15], [0.37, -0.15], [0, -0.84]];
for (const [index, berry] of berries.entries()) {
  cube('grape_relief_base_' + index, [8 + berry[0], 6.9 + berry[1], 6.65], [0.58, 0.58, 0.31 + index * 0.006], 'emboss', 'z', 45, 'grape_relief');
  cube('grape_relief_cut_' + index, [8 + berry[0], 6.9 + berry[1], 6.46], [0.37, 0.37, 0.16], 'copper', 'z', 45, 'grape_relief');
}
cube('vine_stalk', [8.06, 8.22, 6.61], [0.2, 1.0, 0.25], 'emboss', 'z', -22.5, 'grape_relief');
cube('vine_leaf_left', [7.51, 8.36, 6.65], [0.76, 0.36, 0.4], 'copper', 'z', 22.5, 'grape_relief');
cube('vine_leaf_right', [8.53, 8.46, 6.61], [0.69, 0.32, 0.29], 'emboss', 'z', -22.5, 'grape_relief');
for (const sign of [-1, 1]) {
  cube('vine_curl_' + sign, [8 + sign * 1.48, 6.44, 6.69], [0.14, 0.76, 0.16], 'emboss', 'z', sign * 22.5, 'grape_relief');
  cube('vine_curl_tip_' + sign, [8 + sign * 1.33, 6.05, 6.69], [0.44, 0.15, 0.19], 'copper', null, 0, 'grape_relief');
}
cube('wax_low_run', [8.47, 3.98, 7.07], [0.62, 1.46, 0.38], 'wax', null, 0, 'wax');
cube('wax_rounded_drop', [8.47, 3.15, 7.03], [0.56, 0.56, 0.42], 'wax', 'z', 45, 'wax');
plate('back_hammered_recess', [8, 6.9, 8.71], 2.9, 0.29, 'iron', 'back');
ring('rear_copper_inlay', [8, 6.9, 8.75], 3.17, 0.15, 0.42, 'copper', 'back');
cube('vintage_plate', [8, 6.92, 8.98], [2.58, 1.48, 0.34], 'vintage', null, 0, 'back');
for (const centerX of [7.45, 8, 8.55]) {
  cube('vintage_iii_' + centerX, [centerX, 6.98, 9.18], [0.16, 0.75, 0.16], 'emboss', null, 0, 'back');
}
for (const centerX of [5.89, 10.11]) {
  cube('rear_rivet_' + centerX, [centerX, 6.91, 8.91], [0.31, 0.31, 0.17], 'copper', 'z', 45, 'back');
}
cube('rear_cellar_rule_upper', [8, 8.19, 8.93], [1.78, 0.13, 0.16], 'copper', null, 0, 'back');
cube('rear_cellar_rule_lower', [8, 5.63, 8.93], [1.78, 0.13, 0.16], 'copper', null, 0, 'back');
cube('bail_foot', [8, 10.64, 7.98], [1.58, 0.78, 1.0], 'iron', null, 0, 'suspension');
cube('cork_gasket', [8, 10.88, 7.96], [1.1, 0.64, 0.78], 'oak', null, 0, 'suspension');
for (const centerX of [7.42, 8.58]) {
  cube('bail_cheek_' + centerX, [centerX, 11.24, 7.97], [0.34, 0.99, 0.78], 'copper', null, 0, 'suspension');
}
cube('hinge_cross_pin', [8, 11.12, 7.55], [1.67, 0.28, 0.36], 'emboss', null, 0, 'suspension');
ring('closed_wearing_loop', [8, 13.18, 7.99], 1.79, 0.23, 0.35, 'copper', 'suspension');
for (const sign of [-1, 1]) {
  cube('loop_solder_' + sign, [8 + sign * 1.67, 13.18, 8.0], [0.29, 0.63, 0.43], 'iron', null, 0, 'suspension');
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
  const element = elements[item.index];
  const coherentFace = ['north', 'south'].includes(item.face) &&
    ['wine_crystal_intaglio', 'wax_octagonal_seal', 'forged_seal_back', 'back_hammered_recess'].some(prefix => element.name.startsWith(prefix));
  if (coherentFace) {
    let modelX = element.from[0] + across * (element.to[0] - element.from[0]);
    let modelY = element.to[1] - down * (element.to[1] - element.from[1]);
    if (element.rotation) {
      const radians = element.rotation.angle * Math.PI / 180;
      const offsetX = modelX - element.rotation.origin[0];
      const offsetY = modelY - element.rotation.origin[1];
      modelX = element.rotation.origin[0] + offsetX * Math.cos(radians) - offsetY * Math.sin(radians);
      modelY = element.rotation.origin[1] + offsetX * Math.sin(radians) + offsetY * Math.cos(radians);
    }
    const offsetX = modelX - 8, offsetY = modelY - 6.9;
    let coherentTone;
    if (item.material === 'wine') {
      coherentTone = 0.44 + offsetY * 0.066 - offsetX * 0.025;
      if (offsetX + offsetY * 0.5 < -0.85) coherentTone += 0.18;
      if (offsetX - offsetY * 0.4 > 1.05) coherentTone -= 0.12;
      const vein = Math.abs(offsetY + offsetX * 0.57 + 0.91);
      if (vein < 0.11) coherentTone = 0.2;
      if (vein >= 0.11 && vein < 0.27) coherentTone = 0.74;
    } else if (item.material === 'wax') {
      coherentTone = 0.42 + offsetY * 0.052 - offsetX * 0.018;
      const radius = Math.max(Math.abs(offsetX), Math.abs(offsetY), (Math.abs(offsetX) + Math.abs(offsetY)) / Math.SQRT2);
      if (radius > 2.82) coherentTone = offsetY > 0 ? 0.79 : 0.24;
    } else {
      coherentTone = 0.34 + offsetY * 0.04;
      if (Math.abs(offsetY - 1.1) < 0.13 || Math.abs(offsetY + 1.1) < 0.13) coherentTone = 0.61;
      if (Math.floor((modelX + modelY * 0.4) * 3) % 11 === 0) coherentTone += 0.09;
    }
    return palette[Math.max(0, Math.min(3, Math.floor(coherentTone * 4)))];
  }
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
  thirdperson_righthand: { rotation: [0, -45, 0], translation: [0, 1, 0], scale: [0.72, 0.72, 0.72] },
  thirdperson_lefthand: { rotation: [0, 45, 0], translation: [0, 1, 0], scale: [0.72, 0.72, 0.72] },
  firstperson_righthand: { rotation: [0, -30, 0], translation: [0, 1, 0], scale: [0.75, 0.75, 0.75] },
  firstperson_lefthand: { rotation: [0, 30, 0], translation: [0, 1, 0], scale: [0.75, 0.75, 0.75] },
  ground: { rotation: [0, 0, 0], translation: [0, 1.5, 0], scale: [0.48, 0.48, 0.48] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
  head: { rotation: [0, 0, 0], translation: [0, 8, 0], scale: [0.7, 0.7, 0.7] },
  gui: { rotation: [28, 225, 0], translation: [0, 0, 0], scale: [0.9, 0.9, 0.9] }
};
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_relics/aged_wine_seal', particle: 'ftboss:item/aged_wine_seal' }, elements, display };
canvas.save(ATLAS);
writeSafe(MODEL, JSON.stringify(model, null, 2) + '\n');
const uuid = label => {
  const value = crypto.createHash('sha256').update('ftboss:aged_wine_seal_refine:' + label).digest('hex');
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
const bbmodel = { meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: 'Aged Wine Seal / octagonal wax relic', model_identifier: 'aged_wine_seal', visible_box: [1,1,0], resolution: { width: atlasSize, height: atlasSize }, ambientocclusion: false, front_gui_light: false, elements: bbElements, outliner: groups, textures: [{ path: ATLAS, name: 'aged_wine_seal.png', folder: 'item/boss_relics', namespace: 'ftboss', id: 'atlas', uuid: uuid('texture'), width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, source: 'data:image/png;base64,' + fs.readFileSync(ATLAS).toString('base64') }], display };
writeSafe(BB, JSON.stringify(bbmodel, null, 2) + '\n');
const manifest = { item: 'ftboss:aged_wine_seal', displayName: '陈酿酒封', design: 'Finished octagonal wine seal pendant, cast copper bezel, ruby crystal intaglio, raised grape stamp, wax run, hinged closed wearing loop', elements: elements.length, rotated: elements.filter(element=>element.rotation).length, faces: faces.length, atlas: [atlasSize,atlasSize], density: 2, groups: metadata, uvIslands: faces.map(({index,face,width,height,left,top,material})=>({index,face,width,height,left,top,material})) };
writeSafe(path.join(WORK, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
console.log(JSON.stringify({ elements: elements.length, rotated: manifest.rotated, faces: faces.length, atlas: atlasSize, density: 2 }, null, 2));


const wrapper = { loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/aged_wine_seal' }, base: { parent: 'ftboss:item/boss_relics/aged_wine_seal' }, perspectives: { gui: { parent: 'ftboss:item/boss_relics/aged_wine_seal_gui' } } };
const gui = { parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/aged_wine_seal' } };
writeSafe(path.join(ASSET, 'models/item/aged_wine_seal.json'), JSON.stringify(wrapper, null, 2) + '\n');
writeSafe(path.join(ASSET, 'models/item/boss_relics/aged_wine_seal_gui.json'), JSON.stringify(gui, null, 2) + '\n');
