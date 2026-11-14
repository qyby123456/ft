'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { makeCanvas, outline, checks } = require('../../lib_itemtex.cjs');
const { encodePNG, decodePNG } = require('../../png.cjs');
const { root, raster, parts, corners } = require('../../boss_materials_refine/capacitor/render.cjs');
const { specification } = require('./spec.cjs');
const work = '_work/boss-relics-refine-20261021/capacitor';
const resources = 'src/main/resources/assets/ftboss';
const item = 'charged_core_pendant';
const precision = value => Math.round(value * 1000000) / 1000000;
const entries = specification();
const islands = new Map();
const palettes = {
  brass: ['#423924', '#876632', '#c39c52', '#f0d18c'],
  iron: ['#142830', '#344d58', '#68838a', '#acbec0'],
  dark: ['#101f29', '#1c3540', '#314a53', '#547079'],
  copper: ['#4d2c25', '#965336', '#cf8554', '#f3c38c'],
  ceramic: ['#536b72', '#93afa9', '#cfdfcc', '#f2eed5'],
  energy: ['#084653', '#087b89', '#29c0c9', '#86e4db'],
  jade: ['#163e3e', '#24645c', '#4a9784', '#94c7a8'],
  ivory: ['#78968c', '#b4c8b1', '#edebc6', '#fff8db']
};
const faceNames = ['north', 'south', 'east', 'west', 'up', 'down'];
const modelElements = entries.map(entry => {
  const element = { name: entry.name, from: entry.from.map(precision), to: entry.to.map(precision), faces: {} };
  if (entry.rotation) element.rotation = { ...entry.rotation, origin: entry.rotation.origin.map(precision) };
  if (entry.material === 'energy' || entry.material === 'ivory') element.shade = false;
  const dimensions = element.to.map((coordinate, index) => coordinate - element.from[index]);
  for (const side of faceNames) {
    const measure = side === 'north' || side === 'south' ? [dimensions[0], dimensions[1]] : side === 'east' || side === 'west' ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    const width = precision(measure[0] * 2), height = precision(measure[1] * 2);
    const pattern = entry.pattern || 'satin';
    const continuous = entry.material === 'energy' && (side === 'north' || side === 'south');
    const key = [entry.material, pattern, side, width, height, continuous ? entry.name : ''].join('/');
    const points = continuous ? corners(element) : undefined;
    const indices = side === 'north' ? [6, 2, 4] : [3, 7, 1];
    const projection = continuous ? indices.map(index => points[index]) : undefined;
    if (!islands.has(key)) islands.set(key, { key, material: entry.material, pattern, side, width, height, projection, boxWidth: Math.ceil(width) + 2, boxHeight: Math.ceil(height) + 2 });
    element.faces[side] = { key, texture: '#atlas' };
  }
  return element;
});
const sorted = [...islands.values()].sort((first, second) => second.boxHeight - first.boxHeight || second.boxWidth - first.boxWidth || first.key.localeCompare(second.key));
function pack(size) {
  const rectangles = [{ left: 0, top: 0, width: size, height: size }];
  for (const island of sorted) {
    let bestIndex = -1, score = Infinity;
    for (const [index, rectangle] of rectangles.entries()) {
      if (island.boxWidth > rectangle.width || island.boxHeight > rectangle.height) continue;
      const value = Math.min(rectangle.width - island.boxWidth, rectangle.height - island.boxHeight) * size + rectangle.top;
      if (value < score) { score = value; bestIndex = index; }
    }
    if (bestIndex < 0) return false;
    const chosen = rectangles.splice(bestIndex, 1)[0]; island.left = chosen.left + 1; island.top = chosen.top + 1;
    if (chosen.width > island.boxWidth) rectangles.push({ left: chosen.left + island.boxWidth, top: chosen.top, width: chosen.width - island.boxWidth, height: island.boxHeight });
    if (chosen.height > island.boxHeight) rectangles.push({ left: chosen.left, top: chosen.top + island.boxHeight, width: chosen.width, height: chosen.height - island.boxHeight });
  }
  return true;
}
let size = 128;
if (!pack(size)) { size = 256; if (!pack(size)) throw new Error('Relic UV atlas full'); }
const canvas = makeCanvas(size, size);
const color = hex => [1, 3, 5].map(start => parseInt(hex.slice(start, start + 2), 16));
for (const island of sorted) {
  const colors = palettes[island.material].map(color), width = Math.ceil(island.width), height = Math.ceil(island.height);
  const local = [];
  for (let vertical = 0; vertical < height; vertical++) for (let horizontal = 0; horizontal < width; horizontal++) {
    const border = horizontal === 0 || vertical === 0 || horizontal === width - 1 || vertical === height - 1;
    let level = border ? (vertical === 0 || horizontal === 0 ? 3 : 0) : 2;
    if (!border && (vertical === height - 2 || horizontal === width - 2)) level = 1;
    if (island.material === 'energy') {
      level = horizontal + vertical < (width + height) * 0.35 ? 3 : horizontal + vertical > (width + height) * 0.75 ? 1 : 2;
      if (border) level = horizontal === 0 || vertical === 0 ? 2 : 0;
      if (island.pattern === 'cut_facet') level = vertical < height / 2 ? 2 : 1;
      if (!border && width > 4 && height > 4 && vertical === 2 && horizontal < width - 2) level = 3;
    }
    if (island.material === 'jade') {
      if (!border && width > 4 && height > 4 && (horizontal === 2 || vertical === 2)) level = 3;
      if (!border && horizontal === width - 3) level = 1;
    }
    if (island.material === 'iron' && !border && height > 4 && vertical % 5 === 3) level = 1;
    if (island.material === 'ceramic') level = border ? (vertical === 0 ? 3 : 1) : horizontal < width / 3 ? 3 : 2;
    if (island.material === 'dark') level = border ? 0 : 1;
    if (island.pattern === 'screw') {
      level = border ? 1 : 3;
      if (horizontal === Math.floor(width / 2)) level = 0;
    }
    if (island.pattern === 'milled' && !border && height > 3 && horizontal % 5 === 3) level = 1;
    if (width === 1 || height === 1) level = island.side === 'up' ? 3 : island.side === 'down' ? 0 : island.side === 'east' || island.side === 'south' ? 1 : 2;
    if (island.projection) {
      const [origin, across, down] = island.projection;
      const amountX = Math.min(1, (horizontal + 0.5) / island.width), amountY = Math.min(1, (vertical + 0.5) / island.height);
      const point = origin.map((coordinate, index) => coordinate + (across[index] - coordinate) * amountX + (down[index] - coordinate) * amountY);
      const localX = point[0] - 8, localY = point[1] - 7.1;
      const radial = Math.hypot(localX, localY);
      level = localY > 0.65 + localX * 0.45 ? 3 : localY < -0.55 + localX * 0.4 ? 1 : 2;
      if (radial > 2.35) level = 1;
      if (radial > 2.7) level = 0;
      if (Math.abs(localY - 1.55) < 0.13 && localX > -1.25 && localX < 0.75) level = 3;
    }
    local.push(colors[level]);
  }
  for (let vertical = -1; vertical <= height; vertical++) for (let horizontal = -1; horizontal <= width; horizontal++) {
    const selected = local[Math.max(0, Math.min(height - 1, vertical)) * width + Math.max(0, Math.min(width - 1, horizontal))];
    canvas.put(island.left + horizontal, island.top + vertical, selected);
  }
}
for (const element of modelElements) for (const face of Object.values(element.faces)) {
  const island = islands.get(face.key);
  face.uv = [island.left, island.top, island.left + island.width, island.top + island.height].map(coordinate => precision(coordinate * 16 / size)); delete face.key;
}
if (checks.goldArtifacts(canvas)) throw new Error('Unexpected saturated gold artifacts');
const display = {
  gui: { rotation: [16, -28, 0], translation: [0, 0, 0], scale: [0.95, 0.95, 0.95] },
  ground: { rotation: [0, 0, 0], translation: [0, 2.5, 0], scale: [0.45, 0.45, 0.45] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.78, 0.78, 0.78] },
  head: { rotation: [0, 180, 0], translation: [0, 9, 0], scale: [0.7, 0.7, 0.7] },
  thirdperson_righthand: { rotation: [0, -70, 0], translation: [0, 1, -0.5], scale: [0.6, 0.6, 0.6] },
  thirdperson_lefthand: { rotation: [0, 70, 0], translation: [0, 1, -0.5], scale: [0.6, 0.6, 0.6] },
  firstperson_righthand: { rotation: [0, -25, 0], translation: [1.5, 1, 0], scale: [0.68, 0.68, 0.68] },
  firstperson_lefthand: { rotation: [0, 25, 0], translation: [1.5, 1, 0], scale: [0.68, 0.68, 0.68] }
};
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_relics/' + item, particle: 'ftboss:item/' + item }, elements: modelElements, display };
function write(relative, data) {
  const filename = path.join(root, relative); fs.mkdirSync(path.dirname(filename), { recursive: true });
  if (fs.existsSync(filename)) fs.chmodSync(filename, 0o666); fs.writeFileSync(filename, data);
}
function json(relative, data) { write(relative, JSON.stringify(data, null, 2) + '\n'); }
const atlas = encodePNG(size, size, Buffer.from(canvas.buf));
write(resources + '/textures/item/boss_relics/' + item + '.png', atlas);
json(resources + '/models/item/boss_relics/' + item + '.json', model);
json(resources + '/models/item/' + item + '.json', {
  loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/' + item },
  base: { parent: 'ftboss:item/boss_relics/' + item }, perspectives: { gui: { parent: 'ftboss:item/boss_relics/' + item + '_gui' } }
});
json(resources + '/models/item/boss_relics/' + item + '_gui.json', { parent: 'minecraft:item/generated', gui_light: 'front', textures: { layer0: 'ftboss:item/' + item } });
const actual = JSON.parse(fs.readFileSync(path.join(root, resources, 'models/item/boss_relics/' + item + '.json')));
const texture = decodePNG(path.join(root, resources, 'textures/item/boss_relics/' + item + '.png'));
const icon = raster(parts(actual, texture), texture, { width: 32, height: 32, center: [8, 8.5, 8], yaw: -20, pitch: -10, zoom: 2.18 });
const iconCanvas = makeCanvas(32, 32); iconCanvas.buf.set(icon.pixels); outline(iconCanvas, [12, 24, 28, 255]);
write(resources + '/textures/item/' + item + '.png', encodePNG(32, 32, Buffer.from(iconCanvas.buf)));
function uuid(name) {
  const hash = crypto.createHash('sha256').update('charged-core-pendant-refine/' + name).digest('hex').slice(0, 32);
  return [hash.slice(0, 8), hash.slice(8, 12), hash.slice(12, 16), hash.slice(16, 20), hash.slice(20)].join('-');
}
const groupNames = [...new Set(entries.map(entry => entry.group))];
json('art/boss_relics/blockbench/' + item + '.bbmodel', {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '蓄能核心坠 · 八角镶芯电荷护坠', model_identifier: item,
  visible_box: [1, 1, 0], resolution: { width: size, height: size }, elements: modelElements.map(element => ({
    name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to, origin: element.rotation?.origin || [8, 8, 8],
    rotation: ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0), rescale: false, shade: element.shade !== false, autouv: 0, color: 0,
    faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => [side, { uv: face.uv.map(coordinate => precision(coordinate * size / 16)), texture: 0 }]))
  })),
  outliner: groupNames.map(group => ({ name: group, uuid: uuid('group/' + group), origin: [8, 8, 8], children: entries.filter(entry => entry.group === group).map(entry => uuid(entry.name)) })),
  textures: [{ name: item + '.png', id: '0', uuid: uuid('atlas'), mode: 'bitmap', path: '', source: 'data:image/png;base64,' + atlas.toString('base64'), width: size, height: size, uv_width: size, uv_height: size, particle: true }], display
});
write('art/boss_relics/refine/capacitor/atlas-raw.png', atlas);
json(work + '/build-manifest.json', {
  item: 'ftboss:' + item, elements: modelElements.length, rotated: modelElements.filter(element => element.rotation).length, faces: modelElements.length * 6,
  atlas: [size, size], pixelsPerUnit: 2, groups: Object.fromEntries(groupNames.map(group => [group, entries.filter(entry => entry.group === group).length])), islands: sorted,
  pixelAudit: { goldArtifacts: checks.goldArtifacts(canvas), warmPixels: checks.warmPixels(canvas) }
});
console.log('Built only charged_core_pendant:', modelElements.length, 'elements,', sorted.length, 'proportional islands,', size + 'px atlas.');
