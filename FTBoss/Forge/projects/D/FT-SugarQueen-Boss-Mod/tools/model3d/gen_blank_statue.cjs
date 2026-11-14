'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { Canvas } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const resources = path.join(root, 'src/main/resources');
const assets = path.join(resources, 'assets/ftboss');
const triangles = [];
const surfaces = [];
const tiles = {};
const texture = new Canvas(128, 128);
texture.rect(0, 0, 128, 128, 25, 32, 46, 1);
const subtract = (first, second) => first.map((value, axis) => value - second[axis]);
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const normalize = vector => vector.map(value => value / Math.hypot(...vector));
const mix = (first, second, amount) => first.map((value, axis) => value + (second[axis] - value) * Math.max(0, Math.min(1, amount)));

function paint(name, x, y, width, height, painter) {
  tiles[name] = { x, y, width, height };
  for (let row = 0; row < height; row++) {
    for (let column = 0; column < width; column++) texture.set(x + column, y + row, ...painter(column, row, width, height), 1);
  }
}

const stone = [75, 85, 110];
const shadow = [39, 44, 68];
const gold = [161, 124, 72];
const goldLight = [224, 202, 144];
const goldDark = [74, 63, 61];
for (let side = 0; side < 8; side++) {
  paint('stone' + side, side * 14, 0, 12, 5, (column, row, width, height) => {
    if (row === 0) return [128, 140, 161];
    if (row === height - 1 || column === 0 || column === width - 1) return shadow;
    if ((side % 3 === 0 && column === 4 && row > 1) || (side % 3 === 1 && column === 8 && row === 3)) return [55, 62, 87];
    return mix(stone, shadow, row * 0.06);
  });
  paint('carved' + side, side * 14, 8, 8, 7, (column, row, width, height) => {
    if (column === 0 || column === width - 1 || row === height - 1) return shadow;
    if (row === 0) return [113, 125, 145];
    if (column === 2 && row >= 2 || column === 5 && row >= 2) return [50, 56, 79];
    if (column === 3 && row >= 2 || column === 6 && row >= 2) return [99, 110, 134];
    if (row === 1 && column === 4) return [129, 135, 151];
    return stone;
  });
  paint('crystal' + side, side * 14, 32, 6, 16, (column, row, width, height) => {
    const palette = side % 3 === 0 ? [[38, 73, 119], [113, 220, 228]] : side % 3 === 1 ? [[35, 106, 144], [146, 247, 242]] : [[60, 90, 159], [145, 207, 243]];
    let color = mix(palette[0], palette[1], 0.27 + 0.5 * (1 - column / width) + 0.12 * (1 - row / height));
    if (column === 0) color = mix(color, [219, 254, 255], 0.55);
    if (column === width - 1) color = mix(color, [32, 57, 106], 0.42);
    if (row > 6 && row < 13 && column === (side % 2 === 0 ? 2 : 3)) color = mix(color, [199, 246, 247], 0.2);
    if (row === 10 - column && side % 2 === 0) color = mix(color, [192, 233, 243], 0.33);
    return color;
  });
  paint('rune' + side, side * 14, 18, 8, 8, (column, row, width, height) => {
    if (column === 0 || column === width - 1 || row === 0 || row === height - 1) return goldDark;
    if (row === 1 || column === 1) return goldLight;
    const symbol = side % 4;
    const lit = symbol === 0 ? (column === 4 || row === 4 && column >= 2 && column <= 5)
      : symbol === 1 ? (Math.abs(column - 4) + Math.abs(row - 4) === 2)
      : symbol === 2 ? (column === row && row >= 2 && row <= 5 || column + row === 7 && row >= 2 && row <= 5)
      : (row === 3 && column >= 3 && column <= 5 || column === 3 && row >= 3 && row <= 5);
    return lit ? [115, 220, 226] : [43, 64, 84];
  });
}
paint('gold', 0, 52, 8, 5, (column, row, width, height) => row === 0 ? goldLight : row === height - 1 ? goldDark : mix(gold, goldLight, column === 1 ? 0.4 : 0));
paint('goldEdge', 12, 52, 4, 4, (column, row) => row === 0 || column === 0 ? goldLight : gold);
paint('darkMetal', 20, 52, 8, 8, (column, row, width, height) => row === 0 || column === 0 ? [114, 132, 150] : row === height - 1 ? [36, 48, 66] : [58, 76, 98]);
paint('support', 32, 52, 4, 12, (column, row) => column === 0 ? goldLight : column === 3 ? goldDark : row % 6 === 1 ? [124, 100, 71] : gold);
paint('lens', 42, 52, 12, 12, (column, row) => {
  const distance = Math.hypot(column - 5.5, row - 5.5);
  if (distance > 5) return goldDark;
  if (distance > 4) return goldLight;
  if (distance > 3) return [66, 153, 179];
  if (distance > 2) return [134, 236, 236];
  return [228, 255, 250];
});
paint('glow', 58, 52, 4, 8, (column, row) => column === 0 || column === 3 ? [75, 163, 195] : [179 + row * 4, 245, 249]);
paint('plate', 68, 52, 12, 12, (column, row) => {
  const distance = Math.max(Math.abs(column - 5.5), Math.abs(row - 5.5));
  if (distance > 4.5) return gold;
  if (distance > 3.5) return goldDark;
  if (distance > 2.5) return [108, 132, 149];
  return [47, 64, 87];
});
paint('shard', 84, 52, 4, 10, (column, row) => mix([46, 82, 146], [159, 228, 246], 0.8 - column * 0.19 + row * 0.006));
paint('inlay', 94, 52, 8, 2, (column, row) => column % 3 === 0 ? gold : row === 0 ? [183, 235, 232] : [53, 135, 161]);
paint('medallion', 0, 68, 12, 12, (column, row) => {
  const distance = Math.hypot(column - 5.5, row - 5.5);
  if (distance > 4.7) return goldDark;
  if (distance > 3.7) return goldLight;
  if (Math.abs(column - 5.5) + Math.abs(row - 5.5) < 3.5) return [126, 225, 229];
  return [41, 61, 89];
});

function polygon(points, material, outward, group, emissive = false) {
  if (dot(cross(subtract(points[1], points[0]), subtract(points[2], points[0])), outward) < 0) points = [...points].reverse();
  const normal = normalize(cross(subtract(points[1], points[0]), subtract(points[2], points[0])));
  const tile = tiles[material];
  const uv = points.length === 3 ? [[0, 1], [1, 1], [0.5, 0]] : [[0, 1], [1, 1], [1, 0], [0, 0]];
  const coordinates = uv.map(pair => [(tile.x + 0.5 + pair[0] * (tile.width - 1)) / 128, (tile.y + 0.5 + pair[1] * (tile.height - 1)) / 128]);
  surfaces.push({ group, material, emissive, vertices: points, uv: coordinates });
  for (let corner = 1; corner + 1 < points.length; corner++) triangles.push({ group, material: emissive ? 'glow' : 'body',
    vertices: [0, corner, corner + 1].map(index => ({ position: points[index], uv: coordinates[index], normal })) });
}

function ring(radius, height, sides = 8, phase = Math.PI / 8, center = [0.5, 0.5]) {
  return Array.from({ length: sides }, (unused, index) => [center[0] + Math.sin(index * Math.PI * 2 / sides + phase) * radius,
    height, center[1] - Math.cos(index * Math.PI * 2 / sides + phase) * radius]);
}

function band(radius, height, upperRadius, upperHeight, material, group, sides = 8, cap = false, center = [0.5, 0.5], emissive = false) {
  const lower = ring(radius, height, sides, Math.PI / sides, center);
  const upper = ring(upperRadius, upperHeight, sides, Math.PI / sides, center);
  for (let side = 0; side < sides; side++) {
    const following = (side + 1) % sides;
    polygon([lower[side], lower[following], upper[following], upper[side]], typeof material === 'function' ? material(side) : material,
      [lower[side][0] - center[0], 0, lower[side][2] - center[1]], group, emissive);
  }
  if (cap) {
    for (let side = 0; side < sides; side++) {
      const following = (side + 1) % sides;
      polygon([[center[0], height, center[1]], lower[following], lower[side]], 'plate', [0, -1, 0], group);
      polygon([[center[0], upperHeight, center[1]], upper[side], upper[following]], 'plate', [0, 1, 0], group);
    }
  }
}

function annulus(inner, outer, height, thickness, material, group, sides = 16, emissive = false) {
  const lowerInner = ring(inner, height, sides);
  const lowerOuter = ring(outer, height, sides);
  const upperInner = ring(inner, height + thickness, sides);
  const upperOuter = ring(outer, height + thickness, sides);
  for (let side = 0; side < sides; side++) {
    const following = (side + 1) % sides;
    polygon([upperOuter[side], upperOuter[following], upperInner[following], upperInner[side]], material, [0, 1, 0], group, emissive);
    polygon([lowerOuter[side], lowerOuter[following], upperOuter[following], upperOuter[side]], material,
      [lowerOuter[side][0] - 0.5, 0, lowerOuter[side][2] - 0.5], group, emissive);
    polygon([lowerInner[side], upperInner[side], upperInner[following], lowerInner[following]], material,
      [0.5 - lowerInner[side][0], 0, 0.5 - lowerInner[side][2]], group, emissive);
    polygon([lowerOuter[side], lowerInner[side], lowerInner[following], lowerOuter[following]], material, [0, -1, 0], group, emissive);
  }
}

function box(center, size, angle, material, group, emissive = false) {
  const cosine = Math.cos(angle);
  const sine = Math.sin(angle);
  const transform = point => [center[0] + point[0] * cosine - point[2] * sine, center[1] + point[1], center[2] + point[0] * sine + point[2] * cosine];
  const corners = Array.from({ length: 8 }, (unused, index) => transform([index & 1 ? size[0] / 2 : -size[0] / 2,
    index & 2 ? size[1] / 2 : -size[1] / 2, index & 4 ? size[2] / 2 : -size[2] / 2]));
  for (const indices of [[0, 2, 3, 1], [4, 5, 7, 6], [0, 4, 6, 2], [1, 3, 7, 5], [0, 1, 5, 4], [2, 6, 7, 3]]) {
    const points = indices.map(index => corners[index]);
    const outward = points.reduce((sum, point) => sum.map((value, axis) => value + (point[axis] - center[axis]) / 4), [0, 0, 0]);
    polygon(points, material, outward, group, emissive);
  }
}

function rod(start, end, width, material, group, emissive = false) {
  const direction = normalize(subtract(end, start));
  const right = normalize(cross(direction, Math.abs(direction[1]) > 0.9 ? [1, 0, 0] : [0, 1, 0]));
  const up = normalize(cross(right, direction));
  const section = point => [[-1, -1], [1, -1], [1, 1], [-1, 1]].map(pair => point.map((value, axis) => value + (right[axis] * pair[0] + up[axis] * pair[1]) * width / 2));
  const lower = section(start);
  const upper = section(end);
  for (let side = 0; side < 4; side++) {
    const following = (side + 1) % 4;
    polygon([lower[side], lower[following], upper[following], upper[side]], material,
      subtract(lower[side], start), group, emissive);
  }
  polygon(lower, material, direction.map(value => -value), group, emissive);
  polygon(upper, material, direction, group, emissive);
}

const radial = (angle, radius, height) => [0.5 + Math.sin(angle) * radius, height, 0.5 - Math.cos(angle) * radius];
band(0.405, 0.008, 0.435, 0.039, side => 'stone' + side, '01_plinth_bevel');
band(0.435, 0.039, 0.435, 0.094, side => 'stone' + side, '02_plinth', 8, true);
band(0.436, 0.094, 0.405, 0.12, 'gold', '03_lower_brass_cornice');
band(0.404, 0.12, 0.348, 0.17, side => 'stone' + side, '04_chamfered_shoulder');
band(0.348, 0.17, 0.348, 0.32, side => 'carved' + side, '05_carved_octagonal_drum');
band(0.351, 0.32, 0.387, 0.342, 'gold', '06_upper_cornice');
band(0.387, 0.342, 0.36, 0.369, 'darkMetal', '07_lens_crown', 8, true);
annulus(0.20, 0.321, 0.373, 0.011, 'gold', '08_engraved_focus_ring');
annulus(0.18, 0.197, 0.372, 0.008, 'glow', '09_focus_light', 16, true);
band(0.145, 0.369, 0.12, 0.381, 'lens', '10_focus_lens', 16, true);

for (let side = 0; side < 8; side++) {
  const angle = side * Math.PI / 4;
  box(radial(angle, 0.327, 0.245), [0.105, 0.105, 0.012], angle, 'rune' + side, '11_rune_cartouches_' + side);
  for (const offset of [-0.075, 0.075]) {
    const center = radial(angle, 0.328, 0.245);
    center[0] += Math.cos(angle) * offset;
    center[2] += Math.sin(angle) * offset;
    box(center, [0.021, 0.124, 0.022], angle, 'darkMetal', '12_fluted_pilasters_' + side);
    box([center[0], 0.178, center[2]], [0.03, 0.015, 0.028], angle, 'goldEdge', '13_pilaster_feet_' + side);
    box([center[0], 0.31, center[2]], [0.03, 0.015, 0.028], angle, 'goldEdge', '14_pilaster_capitals_' + side);
  }
  box(radial(angle, 0.408, 0.084), [0.155, 0.01, 0.004], angle, 'inlay', '15_plinth_inlays_' + side);
  for (const offset of [-0.07, 0.07]) {
    const center = radial(angle, 0.407, 0.062);
    center[0] += Math.cos(angle) * offset;
    center[2] += Math.sin(angle) * offset;
    box(center, [0.012, 0.012, 0.008], angle, 'goldEdge', '16_plinth_rivets_' + side);
  }
  box(radial(angle, 0.263, 0.389), [0.033, 0.008, 0.035], angle, 'rune' + side, '17_focus_marks_' + side);
}

for (let side = 0; side < 4; side++) {
  const angle = Math.PI / 4 + side * Math.PI / 2;
  const foot = radial(angle, 0.314, 0.36);
  const elbow = radial(angle, 0.303, 0.443);
  const claw = radial(angle, 0.194, 0.49);
  rod(foot, elbow, 0.035, 'support', '18_mount_feet_' + side);
  rod(elbow, claw, 0.029, 'support', '19_tapered_claws_' + side);
  rod(radial(angle, 0.30, 0.451), radial(angle, 0.20, 0.494), 0.007, 'glow', '20_claw_inlays_' + side, true);
  const center = radial(angle, 0.284, 0.458);
  band(0.041, 0.447, 0.037, 0.473, 'gold', '21_auxiliary_sockets_' + side, 8, true, [center[0], center[2]]);
  band(0.023, 0.472, 0.034, 0.514, 'shard', '22_auxiliary_crystals_' + side, 4, false, [center[0], center[2]]);
  band(0.034, 0.514, 0.004, 0.601, 'shard', '23_auxiliary_tips_' + side, 4, false, [center[0], center[2]]);
}

band(0.005, 0.423, 0.12, 0.508, side => 'crystal' + side, '24_main_crystal_lower_facets');
band(0.12, 0.508, 0.186, 0.572, side => 'crystal' + side, '25_main_crystal_shoulders');
band(0.186, 0.572, 0.17, 0.804, side => 'crystal' + side, '26_main_crystal_long_facets');
band(0.17, 0.804, 0.067, 0.927, side => 'crystal' + side, '27_main_crystal_crown_facets');
band(0.067, 0.927, 0.002, 0.992, side => 'crystal' + side, '28_main_crystal_apex');
rod([0.5, 0.383, 0.5], [0.5, 0.424, 0.5], 0.017, 'glow', '29_suspension_energy_link', true);
for (let side = 0; side < 8; side++) {
  const angle = Math.PI / 8 + side * Math.PI / 4;
  rod(radial(angle, 0.187, 0.573), radial(angle, 0.171, 0.803), 0.0025, 'glow', '30_crystal_edge_refractions_' + side, true);
}

box([0.5, 0.239, 0.169], [0.074, 0.074, 0.014], 0, 'medallion', '31_front_identity_seal');
box([0.5, 0.2, 0.833], [0.14, 0.045, 0.01], 0, 'darkMetal', '32_rear_service_panel');
for (let column = 0; column < 4; column++) box([0.455 + column * 0.03, 0.2, 0.839], [0.012, 0.023, 0.003], 0, 'gold', '33_rear_vents_' + column);

function write(relative, contents) {
  const target = path.join(resources, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, contents);
}
const json = (relative, value) => write(relative, JSON.stringify(value, null, 2) + '\n');
const mesh = ['mtllib ftboss:models/block/statue_blank.mtl'];
let index = 1;
let currentGroup = '';
let currentMaterial = '';
for (const triangle of triangles) {
  if (triangle.group !== currentGroup) mesh.push('g ' + (currentGroup = triangle.group));
  if (triangle.material !== currentMaterial) mesh.push('usemtl ' + (currentMaterial = triangle.material));
  for (const vertex of triangle.vertices) mesh.push('v ' + vertex.position.map(value => value.toFixed(6)).join(' '),
    'vt ' + vertex.uv.map(value => value.toFixed(6)).join(' '), 'vn ' + vertex.normal.map(value => value.toFixed(6)).join(' '));
  mesh.push(`f ${index}/${index}/${index} ${index + 1}/${index + 1}/${index + 1} ${index + 2}/${index + 2}/${index + 2}`);
  index += 3;
}
write('assets/ftboss/models/block/statue_blank.obj', mesh.join('\n') + '\n');
write('assets/ftboss/models/block/statue_blank.mtl', 'newmtl body\nKa 0 0 0\nKd 1 1 1\nd 1\nmap_Kd #body\n\nnewmtl glow\nKa 1 1 1\nKd 1 1 1\nd 1\nmap_Kd #body\n');
texture.save(path.join(assets, 'textures/block/statue_blank.png'));
json('assets/ftboss/models/block/statue_blank.json', { parent: 'minecraft:block/block', loader: 'forge:obj', model: 'ftboss:models/block/statue_blank.obj',
  automatic_culling: false, shade_quads: true, flip_v: false, emissive_ambient: true, ambientocclusion: false, render_type: 'minecraft:cutout',
  textures: { body: 'ftboss:block/statue_blank', particle: 'ftboss:block/statue_blank' } });
json('assets/ftboss/models/item/statue_blank.json', { parent: 'ftboss:block/statue_blank', display: {
  gui: { rotation: [25, 225, 0], translation: [0, 0, 0], scale: [0.88, 0.88, 0.88] },
  thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.375, 0.375, 0.375] },
  ground: { translation: [0, 3, 0], scale: [0.25, 0.25, 0.25] } } });
json('assets/ftboss/blockstates/statue_blank.json', { variants: { '': { model: 'ftboss:block/statue_blank' } } });
json('data/ftboss/loot_tables/blocks/statue_blank.json', { type: 'minecraft:block', pools: [{ rolls: 1,
  entries: [{ type: 'minecraft:item', name: 'ftboss:statue_blank' }], conditions: [{ condition: 'minecraft:survives_explosion' }] }] });
json('data/ftboss/recipes/statue_blank.json', { type: 'minecraft:crafting_shaped', pattern: [' A ', 'AEA', 'SSS'], key: {
  A: { item: 'minecraft:amethyst_shard' }, E: { item: 'minecraft:ender_pearl' }, S: { item: 'minecraft:smooth_stone' } }, result: { item: 'ftboss:statue_blank' } });
const art = path.join(root, 'art/blank-statue/refined');
fs.mkdirSync(art, { recursive: true });
fs.writeFileSync(path.join(art, 'design.json'), JSON.stringify({ name: 'Blank statue — carved projection reliquary',
  triangles: triangles.length, groups: [...new Set(triangles.map(triangle => triangle.group))], atlas: { size: 128, tiles }, surfaces }, null, 2) + '\n');
const elements = [...new Set(surfaces.map(surface => surface.group))].map(group => {
  const vertices = {};
  const faces = {};
  let vertexIndex = 0;
  let faceIndex = 0;
  for (const surface of surfaces.filter(candidate => candidate.group === group)) {
    const keys = surface.vertices.map(position => {
      const key = 'vertex_' + vertexIndex++;
      vertices[key] = position.map(value => value * 16);
      return key;
    });
    faces['face_' + faceIndex++] = { vertices: keys, uv: Object.fromEntries(keys.map((key, index) => [key, surface.uv[index].map(value => value * 128)])), texture: 0 };
  }
  return { name: group, uuid: crypto.randomUUID(), type: 'mesh', origin: [0, 0, 0], rotation: [0, 0, 0], visibility: true, vertices, faces };
});
fs.writeFileSync(path.join(art, 'statue_blank.bbmodel'), JSON.stringify({ meta: { format_version: '4.10', model_format: 'free', box_uv: false },
  name: '空白石像 · 精细投影水晶', resolution: { width: 128, height: 128 }, elements, outliner: elements.map(element => element.uuid),
  textures: [{ name: 'statue_blank.png', id: '0', uuid: crypto.randomUUID(), width: 128, height: 128, uv_width: 128, uv_height: 128,
    source: 'data:image/png;base64,' + fs.readFileSync(path.join(assets, 'textures/block/statue_blank.png')).toString('base64') }] }, null, 2) + '\n');
console.log(`Refined projection crystal: ${triangles.length} triangles, ${new Set(triangles.map(triangle => triangle.group)).size} named parts, ${Object.keys(tiles).length} painted UV islands.`);
