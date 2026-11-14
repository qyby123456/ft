const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { makeCanvas, makePacker, writeSafe, safeCopy } = require('../../lib_itemtex.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const art = path.join(root, 'art/boss_materials/refine/cheese');
const work = path.join(root, '_work/boss-materials-refine-20261021/cheese');
const targets = [
  'textures/item/cheese_material.png',
  'textures/item/boss_materials/cheese.png',
  'models/item/cheese_material.json',
  'models/item/boss_materials/cheese_material.json',
  'models/item/boss_materials/cheese_material_gui.json'
];
const bbPath = path.join(root, 'art/boss_materials/blockbench/cheese_material.bbmodel');
for (const directory of [art, work, path.join(work, 'backup')]) fs.mkdirSync(directory, { recursive: true });
for (const file of [...targets.map(entry => path.join(assets, entry)), bbPath]) {
  const destination = path.join(work, 'backup', path.relative(root, file));
  if (fs.existsSync(file) && !fs.existsSync(destination)) {
    fs.mkdirSync(path.dirname(destination), { recursive: true });
    safeCopy(file, destination);
  }
}
const round = value => +value.toFixed(4);
const mix = (first, second, amount) => first.map((value, channel) => Math.round(value + (second[channel] - value) * Math.max(0, Math.min(1, amount))));
const regular = (centerX, centerY, radius, count) => Array.from({ length: count }, (_, index) => {
  const angle = (index + .5) * Math.PI * 2 / count;
  return [centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle)];
});
const outer = regular(8, 8, 6.4, 16);
const inner = regular(4.65, 8.35, 5.65, 16);
const inside = (point, polygon) => {
  let result = false;
  for (let index = 0, previous = polygon.length - 1; index < polygon.length; previous = index++) {
    const first = polygon[index];
    const second = polygon[previous];
    if ((first[1] > point[1]) !== (second[1] > point[1]) && point[0] < (second[0] - first[0]) * (point[1] - first[1]) / (second[1] - first[1]) + first[0]) result = !result;
  }
  return result;
};
const cross = (first, second) => first[0] * second[1] - first[1] * second[0];
function splitEdges(polygon, other, keepInside, material) {
  const edges = [];
  for (let index = 0; index < polygon.length; index++) {
    const first = polygon[index];
    const second = polygon[(index + 1) % polygon.length];
    const direction = [second[0] - first[0], second[1] - first[1]];
    const cuts = [0, 1];
    for (let otherIndex = 0; otherIndex < other.length; otherIndex++) {
      const third = other[otherIndex];
      const fourth = other[(otherIndex + 1) % other.length];
      const otherDirection = [fourth[0] - third[0], fourth[1] - third[1]];
      const offset = [third[0] - first[0], third[1] - first[1]];
      const determinant = cross(direction, otherDirection);
      if (Math.abs(determinant) < 1e-9) continue;
      const along = cross(offset, otherDirection) / determinant;
      const across = cross(offset, direction) / determinant;
      if (along > 1e-7 && along < 1 - 1e-7 && across >= 0 && across <= 1) cuts.push(along);
    }
    cuts.sort((firstCut, secondCut) => firstCut - secondCut);
    for (let cut = 0; cut < cuts.length - 1; cut++) {
      const start = cuts[cut];
      const end = cuts[cut + 1];
      const middle = (start + end) / 2;
      if (inside([first[0] + direction[0] * middle, first[1] + direction[1] * middle], other) !== keepInside) continue;
      const edge = [start, end].map(amount => [first[0] + direction[0] * amount, first[1] + direction[1] * amount]);
      edges.push({ points: keepInside ? edge.reverse() : edge, material });
    }
  }
  return edges;
}
const edges = [...splitEdges(outer, inner, false, 'rind'), ...splitEdges(inner, outer, true, 'cut')];
const cheeseAt = point => inside(point, outer) && !inside(point, inner);
const holeSpecs = [
  { name: 'through_eye', center: [11.55, 7.5], radius: .83, depth: 4.4, side: 'both' },
  { name: 'upper_cut_cell', center: [10.7, 10.7], radius: .92, depth: 1.12, side: 'front' },
  { name: 'lower_cut_cell', center: [8.7, 3.65], radius: .66, depth: .86, side: 'front' },
  { name: 'rear_cell', center: [11.3, 9.45], radius: .73, depth: .9, side: 'back' }
].map(hole => ({ ...hole, polygon: regular(...hole.center, hole.radius, 8) }));
for (const hole of holeSpecs) if (hole.polygon.some(point => !cheeseAt(point))) throw new Error('Hole outside cheese: ' + hole.name);
const atlasSize = 128;
const density = 2;
const canvas = makeCanvas(atlasSize, atlasSize);
const packer = makePacker([{ x: 0, w: atlasSize - 1, top: 0, bottom: atlasSize - 1 }]);
const elements = [];
const islands = [];
function textureIsland(name, modelWidth, modelHeight, painter, pixelDensity = density) {
  const width = Math.max(1, Math.round(modelWidth * pixelDensity));
  const height = Math.max(1, Math.round(modelHeight * pixelDensity));
  const allocation = packer.alloc(name, width + 3, height + 3);
  const left = Math.ceil(allocation[0]) + 1;
  const top = Math.ceil(allocation[1]) + 1;
  for (let pixelY = 0; pixelY < height; pixelY++) for (let pixelX = 0; pixelX < width; pixelX++) {
    const color = painter((pixelX + .5) / width, (pixelY + .5) / height, pixelX, pixelY);
    if (color) canvas.put(left + pixelX, top + pixelY, color);
  }
  for (let pixelY = -1; pixelY <= height; pixelY++) for (let pixelX = -1; pixelX <= width; pixelX++) {
    if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) continue;
    const sourceX = left + Math.max(0, Math.min(width - 1, pixelX));
    const sourceY = top + Math.max(0, Math.min(height - 1, pixelY));
    const offset = (sourceY * atlasSize + sourceX) * 4;
    const color = [...canvas.buf.slice(offset, offset + 4)];
    if (color[3]) canvas.put(left + pixelX, top + pixelY, color);
  }
  const uv = [left, top, left + width, top + height].map(value => round(value * 16 / atlasSize));
  islands.push({ name, pixels: [left, top, width, height], modelSize: [modelWidth, modelHeight], uv });
  return uv;
}
const distanceSegment = (point, first, second) => {
  const direction = [second[0] - first[0], second[1] - first[1]];
  const amount = Math.max(0, Math.min(1, ((point[0] - first[0]) * direction[0] + (point[1] - first[1]) * direction[1]) / (direction[0] ** 2 + direction[1] ** 2)));
  return Math.hypot(point[0] - first[0] - direction[0] * amount, point[1] - first[1] - direction[1] * amount);
};
const rindDistance = point => Math.min(...edges.filter(edge => edge.material === 'rind').map(edge => distanceSegment(point, ...edge.points)));
const cutDistance = point => Math.min(...edges.filter(edge => edge.material === 'cut').map(edge => distanceSegment(point, ...edge.points)));
function capColor(point, side) {
  if (!cheeseAt(point)) return null;
  for (const hole of holeSpecs) if ((hole.side === side || hole.side === 'both') && inside(point, regular(...hole.center, hole.radius + .22, 8))) return null;
  const rind = rindDistance(point);
  const cut = cutDistance(point);
  const light = .28 + point[1] / 27 + Math.sin(point[0] * .61 + point[1] * .45) * .1;
  let color = mix([222, 155, 45], [255, 231, 140], light);
  if (side === 'back') color = mix(color, [199, 137, 52], .12);
  if (rind < .52) color = mix([158, 83, 30], [239, 175, 63], Math.min(1, rind / .45));
  if (rind >= .52 && rind < .82) color = mix(color, [255, 229, 148], .55);
  if (cut < .24) color = mix(color, [255, 241, 173], .85);
  const cellX = Math.floor(point[0] * 2);
  const cellY = Math.floor(point[1] * 2);
  const grain = Math.sin(cellX * 1.71 + cellY * 2.41) * Math.cos(cellX * .37 - cellY * .85);
  if (grain > .82 && rind > .75) color = mix(color, [196, 143, 56], .25);
  if (grain < -.85) color = mix(color, [255, 235, 155], .35);
  for (const hole of holeSpecs) {
    if (hole.side !== side && hole.side !== 'both') continue;
    const distance = Math.hypot(point[0] - hole.center[0], point[1] - hole.center[1]);
    if (distance > hole.radius * .85 && distance < hole.radius + .36) color = mix(color, point[1] < hole.center[1] ? [255, 237, 168] : [192, 121, 33], .58);
  }
  return color;
}
function capTiles(side, depth) {
  const gridLeft = 1.5;
  const gridBottom = 1.5;
  const tilePixels = 6;
  for (let tileY = 0; tileY < 26; tileY += tilePixels) for (let tileX = 0; tileX < 26; tileX += tilePixels) {
    const originalWidth = Math.min(tilePixels, 26 - tileX);
    const originalHeight = Math.min(tilePixels, 26 - tileY);
    const opaque = [];
    for (let row = 0; row < originalHeight; row++) for (let column = 0; column < originalWidth; column++) {
      if (capColor([gridLeft + (tileX + column + .5) / density, gridBottom + (tileY + row + .5) / density], side)) opaque.push([column, row]);
    }
    if (!opaque.length) continue;
    const minimumX = Math.min(...opaque.map(point => point[0]));
    const maximumX = Math.max(...opaque.map(point => point[0]));
    const minimumY = Math.min(...opaque.map(point => point[1]));
    const maximumY = Math.max(...opaque.map(point => point[1]));
    const lowerX = gridLeft + (tileX + minimumX) / density;
    const lowerY = gridBottom + (tileY + minimumY) / density;
    const width = (maximumX - minimumX + 1) / density;
    const height = (maximumY - minimumY + 1) / density;
    const name = side + '_cut_' + tileX + '_' + tileY;
    const uv = textureIsland(name, width, height, (horizontal, vertical) => capColor([lowerX + horizontal * width, lowerY + (1 - vertical) * height], side));
    const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
    elements.push({ name, from: [lowerX, lowerY, depth], to: [lowerX + width, lowerY + height, depth], faces: { [side === 'front' ? 'north' : 'south']: { uv: oriented, texture: '#0' } } });
  }
}
capTiles('front', 5.8);
capTiles('back', 10.2);
function wall(name, first, second, startDepth, endDepth, material, inward = false) {
  const center = [(first[0] + second[0]) / 2, (first[1] + second[1]) / 2];
  const length = Math.hypot(second[0] - first[0], second[1] - first[1]);
  let angle = Math.atan2(second[1] - first[1], second[0] - first[0]) * 180 / Math.PI;
  while (angle > 90) angle -= 180;
  while (angle < -90) angle += 180;
  const horizontal = Math.abs(angle) <= 45.01;
  const rotation = round(horizontal ? angle : angle > 0 ? angle - 90 : angle + 90);
  const accepted = [0, 22.5, -22.5, 45, -45];
  const snapped = accepted.find(value => Math.abs(value - rotation) < .02);
  if (snapped === undefined) throw new Error('Illegal profile angle: ' + rotation);
  const outward = [second[1] - first[1], first[0] - second[0]];
  if (inward) { outward[0] *= -1; outward[1] *= -1; }
  const radians = snapped * Math.PI / 180;
  const candidate = horizontal ? [-Math.sin(radians), Math.cos(radians)] : [Math.cos(radians), Math.sin(radians)];
  const positive = outward[0] * candidate[0] + outward[1] * candidate[1] > 0;
  const face = horizontal ? positive ? 'up' : 'down' : positive ? 'east' : 'west';
  const from = horizontal ? [center[0] - length / 2, center[1], startDepth] : [center[0], center[1] - length / 2, startDepth];
  const to = horizontal ? [center[0] + length / 2, center[1], endDepth] : [center[0], center[1] + length / 2, endDepth];
  const width = horizontal ? length : endDepth - startDepth;
  const height = horizontal ? endDepth - startDepth : length;
  const uv = textureIsland(name, width, height, (horizontalUV, verticalUV, pixelX, pixelY) => {
    const along = horizontal ? horizontalUV : 1 - verticalUV;
    const across = horizontal ? verticalUV : horizontalUV;
    if (material === 'pit') {
      const level = .25 + .4 * along + .15 * Math.sin(across * Math.PI);
      return mix([127, 73, 25], [224, 164, 61], level);
    }
    if (material === 'cut') return mix([229, 177, 66], [255, 230, 142], .22 + .38 * Math.sin(across * Math.PI) + .12 * Math.sin(along * 8));
    let color = mix([148, 79, 31], [220, 151, 48], .34 + .28 * Math.sin(across * Math.PI) + .16 * Math.sin(along * 6));
    if ((pixelX + pixelY * 3) % 13 === 0) color = mix(color, [240, 187, 78], .26);
    if (across < .18 || across > .82) color = mix(color, [112, 65, 31], .26);
    return color;
  });
  const element = { name, from: from.map(round), to: to.map(round), faces: { [face]: { uv, texture: '#0' } } };
  if (snapped) element.rotation = { origin: [...center.map(round), 8], axis: 'z', angle: snapped, rescale: false };
  elements.push(element);
}
for (const [index, edge] of edges.entries()) {
  const bands = edge.material === 'rind' ? [[5.8, 6.25], [6.25, 9.75], [9.75, 10.2]] : [[5.8, 10.2]];
  for (const [band, depths] of bands.entries()) wall(edge.material + '_' + index + '_band_' + band, ...edge.points, ...depths, edge.material);
}
function holeRing(hole, radius, firstDepth, secondDepth, suffix) {
  const polygon = regular(...hole.center, radius, 8);
  for (let index = 0; index < polygon.length; index++) wall(hole.name + '_' + suffix + '_' + index, polygon[index], polygon[(index + 1) % polygon.length], firstDepth, secondDepth, 'pit', true);
}
function holeDisk(hole, radius, depth, side, ring) {
  const name = hole.name + '_' + (ring ? 'shelf' : 'floor');
  const polygon = regular(...hole.center, radius, 8);
  const smaller = regular(...hole.center, radius - .19, 8);
  const size = radius * 2;
  const uv = textureIsland(name, size, size, (horizontal, vertical) => {
    const point = [hole.center[0] + (horizontal - .5) * size, hole.center[1] + (.5 - vertical) * size];
    if (ring && (!inside(point, polygon) || inside(point, smaller))) return null;
    return ring ? [235, 181, 83] : mix([150, 88, 30], [218, 153, 52], .36 + vertical * .32);
  });
  const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
  elements.push({ name, from: [hole.center[0] - radius, hole.center[1] - radius, depth].map(round), to: [hole.center[0] + radius, hole.center[1] + radius, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: oriented } } });
}
for (const hole of holeSpecs) {
  if (hole.side === 'both') {
    holeRing(hole, hole.radius, 5.8, 10.2, 'tunnel');
    continue;
  }
  const front = hole.side === 'front';
  const start = front ? 5.8 : 10.2;
  const shelf = start + (front ? .24 : -.24);
  const floor = start + (front ? hole.depth : -hole.depth);
  holeRing(hole, hole.radius, Math.min(start, shelf), Math.max(start, shelf), 'lip');
  holeRing(hole, hole.radius - .19, Math.min(shelf, floor), Math.max(shelf, floor), 'cup');
  for (let index = 0; index < hole.polygon.length; index++) {
    const first = hole.polygon[index];
    const second = hole.polygon[(index + 1) % hole.polygon.length];
    rail(hole.name + '_shelf_' + index, first, second, .19, shelf, hole.side, true, 'shelf');
  }
  holeDisk(hole, hole.radius - .19, floor, hole.side, false);
}
function rail(name, first, second, width, depth, side, inward, material) {
  const direction = [second[0] - first[0], second[1] - first[1]];
  const length = Math.hypot(...direction);
  const normal = [direction[1] / length, -direction[0] / length];
  const sign = inward ? -1 : 1;
  const center = [(first[0] + second[0]) / 2 + normal[0] * width / 2 * sign, (first[1] + second[1]) / 2 + normal[1] * width / 2 * sign];
  let angle = Math.atan2(direction[1], direction[0]) * 180 / Math.PI;
  while (angle > 90) angle -= 180;
  while (angle < -90) angle += 180;
  const horizontal = Math.abs(angle) <= 45.01;
  const rotation = round(horizontal ? angle : angle > 0 ? angle - 90 : angle + 90);
  const dimensions = horizontal ? [length, width] : [width, length];
  const uv = textureIsland(name, ...dimensions, (horizontalUV, verticalUV) => {
    if (material === 'rind') return mix([181, 112, 34], [246, 193, 85], .48 + .2 * Math.sin((horizontal ? horizontalUV : verticalUV) * Math.PI));
    if (material === 'shelf') return [213, 155, 62];
    return mix([229, 176, 67], [255, 235, 153], .55 + .2 * Math.sin(horizontalUV * Math.PI));
  });
  const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
  const element = { name, from: [center[0] - dimensions[0] / 2, center[1] - dimensions[1] / 2, depth].map(round), to: [center[0] + dimensions[0] / 2, center[1] + dimensions[1] / 2, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: oriented } } };
  if (rotation) element.rotation = { origin: [...center.map(round), depth], axis: 'z', angle: rotation, rescale: false };
  elements.push(element);
}
for (const side of ['front', 'back']) {
  const depth = side === 'front' ? 5.77 : 10.23;
  for (const [index, edge] of edges.entries()) rail(side + '_' + edge.material + '_lip_' + index, ...edge.points, .46, depth, side, true, edge.material);
  for (const hole of holeSpecs) {
    if (hole.side !== side && hole.side !== 'both') continue;
    const radius = hole.radius + .64;
    const polygon = regular(...hole.center, radius, 8);
    const size = radius * 2;
    const uv = textureIsland(side + '_' + hole.name + '_collar', size, size, (horizontal, vertical) => {
      const point = [hole.center[0] + (horizontal - .5) * size, hole.center[1] + (.5 - vertical) * size];
      if (inside(point, hole.polygon) || !cheeseAt(point)) return null;
      const color = capColor(point, side);
      const distance = Math.hypot(point[0] - hole.center[0], point[1] - hole.center[1]);
      return distance < hole.radius + .33 ? mix([224, 163, 54], [255, 232, 146], .45 + vertical * .3) : color;
    }, 4);
    const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
    elements.push({ name: side + '_' + hole.name + '_collar', from: [hole.center[0] - radius, hole.center[1] - radius, depth].map(round), to: [hole.center[0] + radius, hole.center[1] + radius, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: oriented } } });
  }
}
const display = {
  thirdperson_righthand: { rotation: [0, -80, 5], translation: [0, 2.1, .7], scale: [.48, .48, .48] },
  thirdperson_lefthand: { rotation: [0, 80, -5], translation: [0, 2.1, .7], scale: [.48, .48, .48] },
  firstperson_righthand: { rotation: [0, -20, 5], translation: [1.5, 1, .8], scale: [.62, .62, .62] },
  firstperson_lefthand: { rotation: [0, 20, -5], translation: [1.5, 1, .8], scale: [.62, .62, .62] },
  ground: { rotation: [0, 0, -12], translation: [0, 2.2, 0], scale: [.5, .5, .5] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [.75, .75, .75] },
  head: { rotation: [0, 180, 0], translation: [0, 4, 0], scale: [.8, .8, .8] }
};
const model = { ambientocclusion: false, gui_light: 'front', textures: { '0': 'ftboss:item/boss_materials/cheese', particle: 'ftboss:item/cheese_material' }, elements, display };
const json = value => JSON.stringify(value, null, 2) + '\n';
writeSafe(path.join(assets, 'models/item/boss_materials/cheese_material.json'), json(model));
canvas.save(path.join(assets, 'textures/item/boss_materials/cheese.png'));
const wrapper = { loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/cheese_material' }, base: { parent: 'ftboss:item/boss_materials/cheese_material' }, perspectives: { gui: { parent: 'ftboss:item/boss_materials/cheese_material_gui' } } };
writeSafe(path.join(assets, 'models/item/cheese_material.json'), json(wrapper));
writeSafe(path.join(assets, 'models/item/boss_materials/cheese_material_gui.json'), json({ parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/cheese_material' } }));
const uuid = name => {
  const hash = crypto.createHash('sha256').update('cheese-refine-' + name).digest('hex');
  return hash.slice(0, 8) + '-' + hash.slice(8, 12) + '-4' + hash.slice(13, 16) + '-a' + hash.slice(17, 20) + '-' + hash.slice(20, 32);
};
const bbElements = elements.map(element => ({
  name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to,
  origin: element.rotation ? element.rotation.origin : [8, 8, 8],
  rotation: element.rotation ? ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0) : [0, 0, 0],
  rescale: false, autouv: 0, color: element.name.startsWith('rind') ? 2 : element.name.includes('cell') ? 1 : 0,
  faces: Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(face => [face, element.faces[face] ? { uv: element.faces[face].uv.map(value => round(value * atlasSize / 16)), texture: 0 } : { uv: [0, 0, 0, 0], texture: null }]))
}));
const groups = ['front', 'back', 'rind', 'cut', 'through_eye', 'upper_cut_cell', 'lower_cut_cell', 'rear_cell'];
const bb = {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '熟成月牙酪 · 鼠群材料精修', model_identifier: 'cheese_material',
  resolution: { width: atlasSize, height: atlasSize }, ambientocclusion: false, front_gui_light: true,
  elements: bbElements, outliner: groups.map(group => ({ name: group, uuid: uuid('group_' + group), origin: [8, 8, 8], children: bbElements.filter(element => element.name.startsWith(group)).map(element => element.uuid) })),
  textures: [{ name: 'cheese.png', id: '0', uuid: uuid('texture'), relative_path: '../../../src/main/resources/assets/ftboss/textures/item/boss_materials/cheese.png', path: path.join(assets, 'textures/item/boss_materials/cheese.png'), width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, source: 'data:image/png;base64,' + fs.readFileSync(path.join(assets, 'textures/item/boss_materials/cheese.png')).toString('base64') }], display
};
writeSafe(bbPath, json(bb));
writeSafe(path.join(art, 'design.json'), json({ profile: edges, holes: holeSpecs, atlasSize, density, thickness: 4.4, elements: elements.length, rotated: elements.filter(element => element.rotation).length, islands }));
console.log(json({ elements: elements.length, rotated: elements.filter(element => element.rotation).length, islands: islands.length, atlasSize, output: art }));
