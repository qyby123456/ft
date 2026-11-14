const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { makeCanvas, makePacker, writeSafe, safeCopy } = require('../../lib_itemtex.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const art = path.join(root, 'art/boss_relics/refine/cheese');
const work = path.join(root, '_work/boss-relics-refine-20261021/cheese');
const id = 'cheese_moon_charm';
const targetPaths = ['models/item/' + id + '.json', 'models/item/boss_relics/' + id + '.json', 'models/item/boss_relics/' + id + '_gui.json', 'textures/item/' + id + '.png', 'textures/item/boss_relics/' + id + '.png'];
const bbPath = path.join(root, 'art/boss_relics/blockbench/' + id + '.bbmodel');
for (const directory of [art, work, path.join(work, 'backup'), path.join(assets, 'textures/item/boss_relics'), path.join(assets, 'models/item/boss_relics')]) fs.mkdirSync(directory, { recursive: true });
for (const file of [...targetPaths.map(entry => path.join(assets, entry)), bbPath]) {
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
const outer = regular(8.1, 6.6, 4.5, 16);
const inner = regular(5.6, 6.9, 4.05, 16);
const edges = [...splitEdges(outer, inner, false, 'outer'), ...splitEdges(inner, outer, true, 'inner')];
const bodyAt = point => inside(point, outer) && !inside(point, inner);
const cells = [
  { name: 'pierced_cell', center: [10.65, 6.25], radius: .52, depth: 1.6, through: true },
  { name: 'carved_cell', center: [10.2, 8.8], radius: .48, depth: .6, through: false }
].map(cell => ({ ...cell, polygon: regular(...cell.center, cell.radius, 8) }));
for (const cell of cells) if (cell.polygon.some(point => !bodyAt(point))) throw new Error('Cell outside body: ' + cell.name);
const atlasSize = 256;
const atlasHeight = 128;
const canvas = makeCanvas(atlasSize, atlasHeight);
const packer = makePacker([{ x: 0, w: atlasSize - 1, top: 0, bottom: atlasHeight - 1 }]);
const elements = [];
const islands = [];
function island(name, width, height, painter, density = 2) {
  const pixelsWide = Math.max(1, Math.round(width * density));
  const pixelsHigh = Math.max(1, Math.round(height * density));
  const allocation = packer.alloc(name, pixelsWide + 3, pixelsHigh + 3);
  const left = Math.ceil(allocation[0]) + 1;
  const top = Math.ceil(allocation[1]) + 1;
  for (let row = 0; row < pixelsHigh; row++) for (let column = 0; column < pixelsWide; column++) {
    const color = painter((column + .5) / pixelsWide, (row + .5) / pixelsHigh, column, row);
    if (color) canvas.put(left + column, top + row, color);
  }
  for (let row = -1; row <= pixelsHigh; row++) for (let column = -1; column <= pixelsWide; column++) {
    if (column >= 0 && column < pixelsWide && row >= 0 && row < pixelsHigh) continue;
    const sourceX = left + Math.max(0, Math.min(pixelsWide - 1, column));
    const sourceY = top + Math.max(0, Math.min(pixelsHigh - 1, row));
    const offset = (sourceY * atlasSize + sourceX) * 4;
    const color = [...canvas.buf.slice(offset, offset + 4)];
    if (color[3]) canvas.put(left + column, top + row, color);
  }
  const uv = [left, top, left + pixelsWide, top + pixelsHigh].map((value, index) => round(value * 16 / (index % 2 ? atlasHeight : atlasSize)));
  islands.push({ name, pixels: [left, top, pixelsWide, pixelsHigh], modelSize: [width, height], density, uv });
  return uv;
}
const metal = (horizontal, vertical) => {
  const band = Math.min(horizontal, 1 - horizontal, vertical, 1 - vertical);
  if (band < .15) return [136, 89, 35];
  return mix([189, 133, 49], [255, 232, 155], .2 + .62 * (1 - vertical));
};
const darkMetal = (horizontal, vertical) => mix([61, 44, 51], [123, 93, 75], .2 + vertical * .5);
const gem = (horizontal, vertical) => mix([131, 55, 30], [255, 186, 79], .2 + .6 * (1 - vertical) + .12 * Math.cos(horizontal * 6));
function box(name, from, to, painter, rotation) {
  const dimensions = to.map((value, axis) => value - from[axis]);
  const faces = {};
  for (const face of ['north', 'south', 'east', 'west', 'up', 'down']) {
    const size = ['north', 'south'].includes(face) ? [dimensions[0], dimensions[1]] : ['east', 'west'].includes(face) ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    const uv = island(name + '_' + face, ...size, painter);
    faces[face] = { texture: '#0', uv: face === 'north' ? [uv[2], uv[1], uv[0], uv[3]] : uv };
  }
  const element = { name, from: from.map(round), to: to.map(round), faces };
  if (rotation && rotation.angle) element.rotation = { ...rotation, origin: rotation.origin.map(round), rescale: false };
  elements.push(element);
}
function rail(name, first, second, width, front, back, painter, shift = 0) {
  if (name.startsWith('chain_link_') || name.startsWith('bail_')) {
    const depthOffset = Number(name.split('_').at(-1)) * .001;
    front -= depthOffset;
    back += depthOffset;
  }
  const direction = [second[0] - first[0], second[1] - first[1]];
  const length = Math.hypot(...direction);
  const normal = [direction[1] / length, -direction[0] / length];
  const center = [(first[0] + second[0]) / 2 + normal[0] * shift, (first[1] + second[1]) / 2 + normal[1] * shift, (front + back) / 2];
  let angle = Math.atan2(direction[1], direction[0]) * 180 / Math.PI;
  while (angle > 90) angle -= 180;
  while (angle < -90) angle += 180;
  const horizontal = Math.abs(angle) <= 45.01;
  const rotation = round(horizontal ? angle : angle > 0 ? angle - 90 : angle + 90);
  const accepted = [-45, -22.5, 0, 22.5, 45].find(value => Math.abs(value - rotation) < .02);
  if (accepted === undefined) throw new Error('Illegal rail angle: ' + name);
  const dimensions = horizontal ? [length + .025, width] : [width, length + .025];
  box(name, [center[0] - dimensions[0] / 2, center[1] - dimensions[1] / 2, front], [center[0] + dimensions[0] / 2, center[1] + dimensions[1] / 2, back], painter, { origin: center, axis: 'z', angle: accepted });
}
function capColor(point, side) {
  if (!bodyAt(point)) return null;
  for (const cell of cells) if ((side === 'front' || cell.through) && inside(point, regular(...cell.center, cell.radius + .18, 8))) return null;
  const sheen = .37 + point[1] / 24 + Math.sin(point[0] * .7) * .05;
  let color = mix([224, 170, 58], [255, 234, 153], sheen);
  if (side === 'back') color = mix([181, 132, 50], [238, 198, 104], .38 + point[1] / 24);
  const vein = (Math.floor(point[0] * 2) + Math.floor(point[1] * 2) * 3) % 17;
  if (vein === 3) color = mix(color, [199, 146, 56], .13);
  return color;
}
for (const side of ['front', 'back']) {
  const depth = side === 'front' ? 7.16 : 8.84;
  for (let tileY = 0; tileY < 20; tileY += 5) for (let tileX = 0; tileX < 20; tileX += 5) {
    const visible = [];
    for (let row = 0; row < 5; row++) for (let column = 0; column < 5; column++) if (capColor([3.1 + (tileX + column + .5) / 2, 1.6 + (tileY + row + .5) / 2], side)) visible.push([column, row]);
    if (!visible.length) continue;
    const minimumX = Math.min(...visible.map(point => point[0]));
    const maximumX = Math.max(...visible.map(point => point[0]));
    const minimumY = Math.min(...visible.map(point => point[1]));
    const maximumY = Math.max(...visible.map(point => point[1]));
    const left = 3.1 + (tileX + minimumX) / 2;
    const bottom = 1.6 + (tileY + minimumY) / 2;
    const width = (maximumX - minimumX + 1) / 2;
    const height = (maximumY - minimumY + 1) / 2;
    const name = side + '_inlay_' + tileX + '_' + tileY;
    const uv = island(name, width, height, (horizontal, vertical) => capColor([left + horizontal * width, bottom + (1 - vertical) * height], side));
    const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
    const coverage = visible.length / ((maximumX - minimumX + 1) * (maximumY - minimumY + 1));
    if (coverage >= .55) {
      elements.push({ name, from: [left, bottom, depth].map(round), to: [left + width, bottom + height, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: oriented } } });
    } else {
      for (const [pixelIndex, pixel] of visible.entries()) {
        const pixelX = 3.1 + (tileX + pixel[0]) / 2;
        const pixelY = 1.6 + (tileY + pixel[1]) / 2;
        const pixelUV = island(name + '_pixel_' + pixelIndex, .5, .5, () => capColor([pixelX + .25, pixelY + .25], side));
        elements.push({ name: name + '_pixel_' + pixelIndex, from: [pixelX, pixelY, depth].map(round), to: [pixelX + .5, pixelY + .5, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: pixelUV } } });
      }
    }
  }
}
for (const [index, edge] of edges.entries()) {
  rail('body_shank_' + index, ...edge.points, .24, 7.2, 8.8, darkMetal, -.1);
  rail('front_gold_bezel_' + index, ...edge.points, .32, 6.98 + index * .0005, 7.24, metal, -.09);
  rail('back_gold_bezel_' + index, ...edge.points, .29, 8.76, 9.01 + index * .0005, metal, -.09);
}
function cellWall(cell, front, back) {
  for (let index = 0; index < cell.polygon.length; index++) {
    const first = cell.polygon[index];
    const second = cell.polygon[(index + 1) % cell.polygon.length];
    rail(cell.name + '_lining_' + index, first, second, .13, front, back, (horizontal, vertical) => mix([136, 88, 35], [231, 180, 72], .24 + vertical * .43), .065);
  }
}
for (const cell of cells) {
  cellWall(cell, 7.12, cell.through ? 8.88 : 7.16 + cell.depth);
  const radius = cell.radius + .5;
  const size = radius * 2;
  for (const side of cell.through ? ['front', 'back'] : ['front']) {
    const uv = island(side + '_' + cell.name + '_collar', size, size, (horizontal, vertical) => {
      const point = [cell.center[0] + (horizontal - .5) * size, cell.center[1] + (.5 - vertical) * size];
      if (!bodyAt(point) || inside(point, cell.polygon)) return null;
      const distance = Math.hypot(point[0] - cell.center[0], point[1] - cell.center[1]);
      return distance < cell.radius + .18 ? mix([156, 107, 38], [245, 210, 116], vertical * .7) : capColor(point, side);
    }, 4);
    const oriented = side === 'front' ? [uv[2], uv[1], uv[0], uv[3]] : uv;
    const depth = side === 'front' ? 7.1 : 8.9;
    elements.push({ name: side + '_' + cell.name + '_collar', from: [cell.center[0] - radius, cell.center[1] - radius, depth].map(round), to: [cell.center[0] + radius, cell.center[1] + radius, depth].map(round), faces: { [side === 'front' ? 'north' : 'south']: { texture: '#0', uv: oriented } } });
  }
  if (!cell.through) {
    const size = cell.radius * 2;
    const uv = island(cell.name + '_floor', size, size, (horizontal, vertical) => mix([165, 112, 39], [230, 183, 76], .35 + vertical * .3));
    elements.push({ name: cell.name + '_floor', from: [cell.center[0] - cell.radius, cell.center[1] - cell.radius, 7.16 + cell.depth], to: [cell.center[0] + cell.radius, cell.center[1] + cell.radius, 7.16 + cell.depth], faces: { north: { texture: '#0', uv: [uv[2], uv[1], uv[0], uv[3]] } } });
  }
}
const chain = [[6.1, 12.2], [5.4, 12.9], [5.4, 14.3], [6.1, 15], [9.9, 15], [10.6, 14.3], [10.6, 12.9], [9.9, 12.2]];
for (let index = 0; index < chain.length; index++) rail('chain_link_' + index, chain[index], chain[(index + 1) % chain.length], .27, 7.8, 8.18, metal);
const bail = [[7.75, 10.65], [7.4, 11], [7.4, 11.95], [7.75, 12.3], [8.25, 12.3], [8.6, 11.95], [8.6, 11], [8.25, 10.65]];
for (let index = 0; index < bail.length; index++) rail('bail_' + index, bail[index], bail[(index + 1) % bail.length], .28, 7.5, 8.5, metal);
box('necklace_clasp', [7.55, 14.77, 7.62], [8.45, 15.23, 8.38], darkMetal);
box('clasp_gold_plate', [7.65, 14.82, 7.53], [8.35, 15.18, 7.66], metal);
box('mount_bridge', [7.75, 10.25, 7.32], [8.65, 10.88, 8.64], darkMetal);
box('mount_cap', [7.89, 10.34, 7.08], [8.51, 10.77, 7.37], metal);
box('amber_setting', [8.04, 1.97, 6.89], [8.96, 2.89, 7.5], darkMetal, { origin: [8.5, 2.43, 7.2], axis: 'z', angle: 45 });
box('amber_drop', [8.19, 2.12, 6.72], [8.81, 2.74, 6.99], gem, { origin: [8.5, 2.43, 6.86], axis: 'z', angle: 45 });
box('amber_glint', [8.39, 2.53, 6.7], [8.56, 2.7, 6.73], () => [255, 229, 156]);
for (const [index, point] of [[11.97, 6.1], [11.45, 8.65], [10.15, 10.3]].entries()) box('setting_rivet_' + index, [point[0] - .1, point[1] - .1, 6.86], [point[0] + .1, point[1] + .1, 7.25], () => [255, 229, 155]);
const display = {
  thirdperson_righthand: { rotation: [0, -75, 0], translation: [0, 1.8, .8], scale: [.47, .47, .47] },
  thirdperson_lefthand: { rotation: [0, 75, 0], translation: [0, 1.8, .8], scale: [.47, .47, .47] },
  firstperson_righthand: { rotation: [0, -15, 0], translation: [1, .7, .8], scale: [.57, .57, .57] },
  firstperson_lefthand: { rotation: [0, 15, 0], translation: [1, .7, .8], scale: [.57, .57, .57] },
  ground: { rotation: [0, 0, -10], translation: [0, 2, 0], scale: [.45, .45, .45] },
  fixed: { rotation: [0, 180, 0], translation: [0, -.5, 0], scale: [.68, .68, .68] },
  head: { rotation: [0, 180, 0], translation: [0, 4, 0], scale: [.7, .7, .7] }
};
const model = { ambientocclusion: false, gui_light: 'front', textures: { '0': 'ftboss:item/boss_relics/' + id, particle: 'ftboss:item/' + id }, elements, display };
const json = value => JSON.stringify(value, null, 2) + '\n';
writeSafe(path.join(assets, 'models/item/boss_relics/' + id + '.json'), json(model));
canvas.save(path.join(assets, 'textures/item/boss_relics/' + id + '.png'));
writeSafe(path.join(assets, 'models/item/' + id + '.json'), json({ loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/' + id }, base: { parent: 'ftboss:item/boss_relics/' + id }, perspectives: { gui: { parent: 'ftboss:item/boss_relics/' + id + '_gui' } } }));
writeSafe(path.join(assets, 'models/item/boss_relics/' + id + '_gui.json'), json({ parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/' + id } }));
const uuid = name => {
  const hash = crypto.createHash('sha256').update('cheese-relic-' + name).digest('hex');
  return hash.slice(0, 8) + '-' + hash.slice(8, 12) + '-4' + hash.slice(13, 16) + '-a' + hash.slice(17, 20) + '-' + hash.slice(20, 32);
};
const bbElements = elements.map(element => ({
  name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to,
  origin: element.rotation ? element.rotation.origin : [8, 8, 8], rotation: element.rotation ? ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0) : [0, 0, 0],
  rescale: false, autouv: 0, color: element.name.includes('gold') ? 2 : 0,
  faces: Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(face => [face, element.faces[face] ? { uv: element.faces[face].uv.map((value, index) => round(value * (index % 2 ? atlasHeight : atlasSize) / 16)), texture: 0 } : { uv: [0, 0, 0, 0], texture: null }]))
}));
const groups = ['front', 'back', 'body', 'pierced', 'carved', 'chain', 'bail', 'necklace', 'clasp', 'mount', 'amber', 'setting'];
const bb = {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '奶酪月牙坠 · 鼠群遗物精修', model_identifier: id,
  resolution: { width: atlasSize, height: atlasHeight }, ambientocclusion: false, front_gui_light: true, elements: bbElements,
  outliner: groups.map(group => ({ name: group, uuid: uuid('group_' + group), origin: [8, 8, 8], children: bbElements.filter(element => element.name.startsWith(group)).map(element => element.uuid) })),
  textures: [{ name: id + '.png', id: '0', uuid: uuid('texture'), path: path.join(assets, 'textures/item/boss_relics/' + id + '.png'), relative_path: '../../../src/main/resources/assets/ftboss/textures/item/boss_relics/' + id + '.png', width: atlasSize, height: atlasHeight, uv_width: atlasSize, uv_height: atlasHeight, source: 'data:image/png;base64,' + fs.readFileSync(path.join(assets, 'textures/item/boss_relics/' + id + '.png')).toString('base64') }], display
};
writeSafe(bbPath, json(bb));
writeSafe(path.join(art, 'design.json'), json({ bodyProfile: edges, cells, islands, elements: elements.length, rotated: elements.filter(element => element.rotation).length, bodyThickness: 1.6, atlasSize, design: 'Polished cheese inlay, double gold bezel, octagonal suspension links, clasp, honey-amber setting' }));
console.log(json({ elements: elements.length, faces: elements.reduce((sum, element) => sum + Object.keys(element.faces).length, 0), rotated: elements.filter(element => element.rotation).length, atlasSize, output: art }));
