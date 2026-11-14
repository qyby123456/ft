'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { encodePNG, decodePNG } = require('../../png.cjs');
const { makeCanvas, outline, checks } = require('../../lib_itemtex.cjs');
const { root, output, raster, parts } = require('./render.cjs');
const base = path.join(root, 'src/main/resources/assets/ftboss');
const work = path.join(root, '_work/boss-materials-refine-20261021/capacitor');
const elements = [], materials = new Map();
const precision = value => Math.round(value * 1000000) / 1000000;
const palette = {
  brass: ['#473d27', '#8c6b37', '#c49a51', '#efcc80'],
  copper: ['#4e3026', '#955239', '#c67e50', '#f1bd80'],
  iron: ['#162931', '#334852', '#5c737a', '#9aadb0'],
  dark: ['#0e2028', '#1a353d', '#294951', '#46666a'],
  ceramic: ['#425862', '#8fa7ab', '#c4d9cf', '#f0ead3'],
  energy: ['#073c4d', '#087082', '#25b9c4', '#a5f6dd'],
  jade: ['#153c39', '#256257', '#45907c', '#8fbea0']
};
const faces = ['north', 'south', 'east', 'west', 'up', 'down'];
function add(group, name, from, to, material, options = {}) {
  const element = { name, from: from.map(precision), to: to.map(precision), faces: {} };
  if (options.rotation) element.rotation = options.rotation;
  if (material === 'energy') element.shade = false;
  const dimensions = to.map((coordinate, index) => coordinate - from[index]);
  for (const side of faces) {
    if (options.omit?.includes(side)) continue;
    const measure = side === 'north' || side === 'south' ? [dimensions[0], dimensions[1]] : side === 'east' || side === 'west' ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    const surface = options.surface?.[side] || options.pattern || 'satin';
    const faceMaterial = options.faceMaterial?.[side] || material;
    const width = precision(measure[0] * 2), height = precision(measure[1] * 2);
    const key = [faceMaterial, surface, side, width, height].join('/');
    if (!materials.has(key)) materials.set(key, { key, material: faceMaterial, pattern: surface, side, width, height, boxWidth: Math.ceil(width) + 2, boxHeight: Math.ceil(height) + 2 });
    element.faces[side] = { texture: '#atlas', key };
  }
  elements.push({ element, group });
}
function cuboid(group, name, center, size, material, options = {}) {
  add(group, name, center.map((coordinate, index) => coordinate - size[index] / 2), center.map((coordinate, index) => coordinate + size[index] / 2), material, options);
}
function octagon(group, name, radius, bottom, height, thickness, material, pattern = 'satin') {
  const tangent = radius * 2 * Math.tan(Math.PI / 8) - 0.04;
  for (let index = 0; index < 8; index++) {
    const angle = index * 45, radians = angle * Math.PI / 180;
    const seamOffset = group === 'core' ? 0 : (index % 2) * 0.012;
    const center = [8 + Math.sin(radians) * radius, bottom + height / 2 + seamOffset, 8 - Math.cos(radians) * radius];
    const side = index % 2 === 0;
    const size = index === 2 || index === 6 ? [thickness, height, tangent] : [tangent, height, thickness];
    const rotation = side ? undefined : { origin: center.map(precision), axis: 'y', angle: index === 1 || index === 5 ? -45 : 45, rescale: false };
    cuboid(group, name + '_' + index, center, size, material, { rotation, pattern });
  }
}

cuboid('core', 'dielectric_internal_spine', [8, 7.05, 8], [4.2, 6.9, 4.2], 'dark');
octagon('core', 'faceted_dielectric_cell', 2.2, 3.65, 6.85, 0.9, 'energy', 'charged');
cuboid('shell', 'lower_pressure_plate', [8, 3.15, 8], [4.75, 0.75, 4.75], 'iron', { pattern: 'plate' });
cuboid('shell', 'upper_pressure_plate', [8, 10.9, 8], [4.75, 0.75, 4.75], 'iron', { pattern: 'plate' });
octagon('shell', 'lower_bevel_rim', 3.05, 2.85, 0.65, 0.75, 'iron', 'machined');
octagon('shell', 'lower_brass_gasket', 3.0, 3.5, 0.25, 0.65, 'brass', 'machined');
octagon('shell', 'upper_bevel_rim', 3.05, 10.5, 0.7, 0.75, 'brass', 'machined');
octagon('shell', 'upper_dark_gasket', 2.95, 10.25, 0.25, 0.55, 'dark', 'rubber');
for (const signX of [-1, 1]) for (const signZ of [-1, 1]) {
  const center = [8 + signX * 2.16, 7, 8 + signZ * 2.16];
  const rotation = { origin: center, axis: 'y', angle: signX === signZ ? -45 : 45, rescale: false };
  cuboid('shell', 'brass_corner_stanchion_' + signX + '_' + signZ, center, [0.8, 7.0, 0.65], 'brass', { rotation, pattern: 'rail' });
}
for (const sign of [-1, 1]) {
  cuboid('shell', 'side_armor_' + sign, [8 + sign * 3.04, 7.05, 8], [0.65, 6.2, 2.25], 'brass', { pattern: 'panel' });
  cuboid('shell', 'jade_side_inlay_' + sign, [8 + sign * 3.4, 8.5, 8], [0.18, 2, 1.15], 'jade', { pattern: 'panel' });
  cuboid('shell', 'lower_side_insulator_' + sign, [8 + sign * 3.39, 4.8, 8], [0.18, 1.2, 1.15], 'dark', { pattern: 'rubber' });
  for (const vertical of [4.15, 9.9]) {
    cuboid('service', 'side_captive_fastener_' + sign + '_' + vertical, [8 + sign * 3.5, vertical, 8], [0.25, 0.55, 0.55], 'iron', { pattern: 'bolt' });
  }
}
for (const [index, vertical] of [4.2, 5.2, 6.2, 7.2].entries()) {
  octagon('winding', 'copper_turn_' + index, 2.75, vertical, 0.45, 0.4, 'copper', 'drawn_copper');
}
cuboid('winding', 'right_copper_busbar', [10.8, 6.0, 8.65], [0.35, 4.3, 0.4], 'copper', { pattern: 'drawn_copper' });
cuboid('winding', 'busbar_insulating_mount', [10.75, 8.05, 8.65], [0.75, 0.55, 0.8], 'ceramic', { pattern: 'ceramic' });
cuboid('winding', 'positive_feed_riser', [9.7, 9.4, 9.55], [0.4, 3.0, 0.45], 'copper', { pattern: 'drawn_copper' });
cuboid('winding', 'positive_bus_bridge', [10.2, 8.0, 9.1], [1.3, 0.4, 1.25], 'copper', { pattern: 'drawn_copper' });

cuboid('service', 'rear_recess_gasket', [8, 7.7, 10.63], [2.8, 4.4, 0.32], 'dark', { pattern: 'rubber' });
cuboid('service', 'rear_bronze_service_cover', [8, 7.8, 10.87], [2.5, 4.0, 0.28], 'iron', { pattern: 'panel', surface: { south: 'service' } });
for (let index = 0; index < 4; index++) cuboid('service', 'rear_cooling_louvre_' + index, [8, 6.4 + index * 0.65, 11.08], [1.75, 0.28, 0.3], 'iron', { pattern: 'machined' });
for (const horizontal of [7.1, 8.9]) for (const vertical of [6.1, 9.5]) cuboid('service', 'rear_screw_' + horizontal + '_' + vertical, [horizontal, vertical, 11.08], [0.4, 0.4, 0.18], 'brass', { pattern: 'bolt' });
cuboid('service', 'front_energy_window_seal', [8, 8.9, 5.16], [2.2, 2.55, 0.6], 'dark', { pattern: 'rubber' });
cuboid('core', 'front_charge_indicator', [8, 8.9, 4.86], [1.8, 2.15, 0.22], 'energy', { pattern: 'charge_gauge' });
for (const sign of [-1, 1]) cuboid('service', 'front_gauge_clip_' + sign, [8 + sign * 1.08, 8.9, 4.81], [0.32, 2.7, 0.3], 'brass', { pattern: 'rail' });
cuboid('service', 'front_warning_plate', [8, 3.92, 4.92], [1.4, 0.45, 0.2], 'ceramic', { pattern: 'serial' });

for (const [index, horizontal] of [6.4, 9.6].entries()) {
  cuboid('terminals', 'terminal_gasket_' + index, [horizontal, 11.38, 8], [1.75, 0.45, 1.75], 'dark', { pattern: 'rubber' });
  cuboid('terminals', 'ceramic_terminal_base_' + index, [horizontal, 11.8, 8], [1.35, 0.9, 1.35], 'ceramic', { pattern: 'ceramic' });
  for (let rib = 0; rib < 2; rib++) cuboid('terminals', 'ceramic_rib_' + index + '_' + rib, [horizontal, 11.65 + rib * 0.5, 8], [1.6, 0.22, 1.6], 'ceramic', { pattern: 'ceramic' });
  cuboid('terminals', 'copper_contact_' + index, [horizontal, 12.7 + index * 0.15, 8], [0.9, 1.5 + index * 0.3, 0.9], 'copper', { pattern: 'contact' });
  cuboid('terminals', 'contact_crown_' + index, [horizontal, 13.48 + index * 0.3, 8], [1.25, 0.25, 1.25], 'brass', { pattern: index ? 'positive' : 'negative' });
  cuboid('terminals', 'terminal_throat_' + index, [horizontal, 12.35, 8], [1.15, 0.25, 1.15], 'iron', { pattern: 'machined' });
}
for (const horizontal of [6.7, 8.0, 9.3]) cuboid('terminals', 'bottom_contact_' + horizontal, [horizontal, 2.73, 8], [0.7, 0.28, 2.3], 'copper', { pattern: 'contact' });

function place(islands, size) {
  const free = [{ left: 0, top: 0, width: size, height: size }];
  for (const island of islands) {
    let choice = -1, best = Infinity;
    for (const [index, rectangle] of free.entries()) {
      if (island.boxWidth > rectangle.width || island.boxHeight > rectangle.height) continue;
      const score = Math.min(rectangle.width - island.boxWidth, rectangle.height - island.boxHeight) * size + rectangle.top;
      if (score < best) { choice = index; best = score; }
    }
    if (choice < 0) return false;
    const rectangle = free.splice(choice, 1)[0];
    island.left = rectangle.left + 1; island.top = rectangle.top + 1;
    const rightWidth = rectangle.width - island.boxWidth, bottomHeight = rectangle.height - island.boxHeight;
    if (rightWidth > 0) free.push({ left: rectangle.left + island.boxWidth, top: rectangle.top, width: rightWidth, height: island.boxHeight });
    if (bottomHeight > 0) free.push({ left: rectangle.left, top: rectangle.top + island.boxHeight, width: rectangle.width, height: bottomHeight });
  }
  return true;
}
const islands = [...materials.values()].sort((first, second) => second.boxHeight - first.boxHeight || second.boxWidth - first.boxWidth || first.key.localeCompare(second.key));
let size = 128;
if (!place(islands, size)) { size = 256; if (!place(islands, size)) throw new Error('Capacitor atlas full'); }
const canvas = makeCanvas(size, size);
const color = hex => [1, 3, 5].map(start => parseInt(hex.slice(start, start + 2), 16));
function paint(island) {
  const colors = palette[island.material].map(color), width = Math.ceil(island.width), height = Math.ceil(island.height);
  const seed = crypto.createHash('sha256').update(island.key).digest().readUInt32LE();
  const local = [];
  for (let vertical = 0; vertical < height; vertical++) for (let horizontal = 0; horizontal < width; horizontal++) {
    let level = 2;
    const border = horizontal === 0 || vertical === 0 || horizontal === width - 1 || vertical === height - 1;
    if (border) level = vertical === 0 || horizontal === 0 ? 3 : 0;
    else if (horizontal === width - 2 || vertical === height - 2) level = 1;
    if (island.material === 'energy') {
      level = border ? 0 : (horizontal <= width / 3 ? 3 : horizontal >= width * 0.75 ? 1 : 2);
      if (vertical % 7 === 4 && horizontal > 1 && horizontal < width - 2) level = 1;
      if (island.pattern === 'charge_gauge') {
        level = border ? 0 : 1;
        if (vertical % 3 !== 0 && horizontal > 0 && horizontal < width - 1) level = vertical < height * 0.3 ? 3 : 2;
      }
    } else if (island.material === 'copper') {
      if (!border && (island.side === 'up' || island.side === 'down') && vertical % 3 === 1) level = 3;
      if (!border && horizontal % 7 === 5 && height > 3) level = 1;
    } else if (island.material === 'dark') {
      level = border ? 0 : (vertical % 3 === 1 ? 1 : 2);
    } else if (island.material === 'ceramic') {
      level = border ? (vertical === 0 ? 3 : 1) : horizontal < width / 3 ? 3 : 2;
    } else {
      if (!border && height > 5 && vertical === 2 && horizontal > 1 && horizontal < width - 2) level = 3;
      if (!border && width > 5 && horizontal === width - 3) level = 1;
      if (!border && (horizontal * 11 + vertical * 17 + seed) % 53 === 0) level = 3;
    }
    if (island.pattern === 'bolt') {
      level = border ? 1 : 3;
      if (horizontal === Math.floor(width / 2)) level = 0;
    }
    if (island.pattern === 'service' && !border && vertical === height - 4 && horizontal > 1 && horizontal < width - 2) level = 0;
    if (island.pattern === 'serial') level = horizontal % 3 === 1 && !border ? 0 : 2;
    if ((width === 1 || height === 1) && island.material !== 'energy') {
      level = island.side === 'up' ? 3 : island.side === 'down' ? 0 : island.side === 'south' || island.side === 'east' ? 1 : 2;
    }
    if ((island.pattern === 'positive' || island.pattern === 'negative') && island.side === 'up') {
      const middleX = Math.floor(width / 2), middleY = Math.floor(height / 2);
      if (vertical === middleY || (island.pattern === 'positive' && horizontal === middleX)) level = 0;
    }
    local.push(colors[level]);
  }
  for (let vertical = -1; vertical <= height; vertical++) for (let horizontal = -1; horizontal <= width; horizontal++) {
    const sourceX = Math.max(0, Math.min(width - 1, horizontal)), sourceY = Math.max(0, Math.min(height - 1, vertical));
    const selected = local[sourceY * width + sourceX];
    canvas.put(island.left + horizontal, island.top + vertical, selected);
  }
}
for (const island of islands) paint(island);
for (const { element } of elements) for (const face of Object.values(element.faces)) {
  const island = materials.get(face.key);
  face.uv = [island.left, island.top, island.left + island.width, island.top + island.height].map(value => precision(value * 16 / size));
  delete face.key;
}
const display = {
  gui: { rotation: [19, -34, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
  ground: { rotation: [0, 0, 0], translation: [0, 2.2, 0], scale: [0.5, 0.5, 0.5] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.8, 0.8, 0.8] },
  head: { rotation: [0, 180, 0], translation: [0, 9, 0], scale: [0.8, 0.8, 0.8] },
  thirdperson_righthand: { rotation: [0, -70, 0], translation: [0, 1, -0.5], scale: [0.65, 0.65, 0.65] },
  thirdperson_lefthand: { rotation: [0, 70, 0], translation: [0, 1, -0.5], scale: [0.65, 0.65, 0.65] },
  firstperson_righthand: { rotation: [0, -25, 0], translation: [1.5, 1, 0], scale: [0.75, 0.75, 0.75] },
  firstperson_lefthand: { rotation: [0, 25, 0], translation: [1.5, 1, 0], scale: [0.75, 0.75, 0.75] }
};
const model = { ambientocclusion: false, gui_light: 'side', textures: { atlas: 'ftboss:item/boss_materials/capacitor', particle: 'ftboss:item/capacitor_material' }, elements: elements.map(entry => entry.element), display };
function write(relative, data) {
  const target = path.join(root, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  if (fs.existsSync(target)) fs.chmodSync(target, 0o666);
  fs.writeFileSync(target, data);
}
function json(relative, data) { write(relative, JSON.stringify(data, null, 2) + '\n'); }
if (checks.goldArtifacts(canvas) !== 0) throw new Error('Unexpected saturated gold artifacts');
const atlas = encodePNG(size, size, Buffer.from(canvas.buf));
write('src/main/resources/assets/ftboss/textures/item/boss_materials/capacitor.png', atlas);
json('src/main/resources/assets/ftboss/models/item/boss_materials/capacitor_material.json', model);
json('src/main/resources/assets/ftboss/models/item/capacitor_material.json', {
  loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front', textures: { particle: 'ftboss:item/capacitor_material' },
  base: { parent: 'ftboss:item/boss_materials/capacitor_material' }, perspectives: { gui: { parent: 'ftboss:item/boss_materials/capacitor_material_gui' } }
});
json('src/main/resources/assets/ftboss/models/item/boss_materials/capacitor_material_gui.json', {
  parent: 'minecraft:item/generated', gui_light: 'front', textures: { layer0: 'ftboss:item/capacitor_material' }
});
const finalModel = JSON.parse(fs.readFileSync(path.join(base, 'models/item/boss_materials/capacitor_material.json')));
const finalTexture = decodePNG(path.join(base, 'textures/item/boss_materials/capacitor.png'));
const icon = raster(parts(finalModel, finalTexture), finalTexture, { width: 32, height: 32, center: [8, 8.1, 8], yaw: -34, pitch: -19, zoom: 2.15 });
const iconCanvas = makeCanvas(32, 32);
iconCanvas.buf.set(icon.pixels);
outline(iconCanvas, [12, 24, 28, 255]);
write('src/main/resources/assets/ftboss/textures/item/capacitor_material.png', encodePNG(32, 32, Buffer.from(iconCanvas.buf)));
function uuid(name) {
  const hash = crypto.createHash('sha256').update('capacitor-refine/' + name).digest('hex').slice(0, 32);
  return [hash.slice(0, 8), hash.slice(8, 12), hash.slice(12, 16), hash.slice(16, 20), hash.slice(20)].join('-');
}
const groups = ['core', 'shell', 'winding', 'terminals', 'service'];
const bbElements = elements.map(({ element }) => ({
  name: element.name, type: 'cube', uuid: uuid(element.name), from: element.from, to: element.to,
  origin: element.rotation?.origin || [8, 8, 8], rotation: ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0),
  rescale: element.rotation?.rescale || false, shade: element.shade !== false, autouv: 0, color: 0,
  faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => [side, { uv: face.uv.map(value => precision(value * size / 16)), texture: 0 }]))
}));
json('art/boss_materials/blockbench/capacitor_material.bbmodel', {
  meta: { format_version: '4.10', model_format: 'java_block', box_uv: false }, name: '蓄能电芯配件 · 铜绕组拆装电芯', model_identifier: 'capacitor_material',
  visible_box: [1, 1, 0], resolution: { width: size, height: size }, elements: bbElements,
  outliner: groups.map(group => ({ name: group, uuid: uuid('group/' + group), origin: [8, 8, 8], children: elements.filter(entry => entry.group === group).map(entry => uuid(entry.element.name)) })),
  textures: [{ name: 'capacitor.png', id: '0', uuid: uuid('texture'), mode: 'bitmap', path: '', source: 'data:image/png;base64,' + atlas.toString('base64'), width: size, height: size, uv_width: size, uv_height: size, particle: true }], display
});
fs.mkdirSync(work, { recursive: true }); fs.mkdirSync(output, { recursive: true });
write('art/boss_materials/refine/capacitor/atlas-raw.png', atlas);
json('_work/boss-materials-refine-20261021/capacitor/build-manifest.json', {
  item: 'ftboss:capacitor_material', elements: model.elements.length, rotated: model.elements.filter(element => element.rotation).length,
  faces: model.elements.reduce((sum, element) => sum + Object.keys(element.faces).length, 0), texture: [size, size], pixelsPerUnit: 2, islands: islands.length,
  islandArea: islands.reduce((sum, island) => sum + island.boxWidth * island.boxHeight, 0), pixelAudit: { goldArtifacts: checks.goldArtifacts(canvas), warmPixels: checks.warmPixels(canvas) },
  groups: Object.fromEntries(groups.map(group => [group, elements.filter(entry => entry.group === group).length])), islands
});
console.log('Built capacitor only:', model.elements.length, 'elements,', islands.length, 'proportional islands,', size + 'px atlas, 32px GUI icon.');
