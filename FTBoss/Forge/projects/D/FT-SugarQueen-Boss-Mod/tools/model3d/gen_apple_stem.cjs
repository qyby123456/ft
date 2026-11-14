'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function generateAppleStem(outputRoot, libraryRoot = __dirname) {
  const { makeCanvas, writeSafe } = require(path.join(libraryRoot, 'lib_itemtex.cjs'));
  const { auditSurfaces } = require(path.join(libraryRoot, 'audit_item_surfaces.cjs'));
  const textureSize = 128;
  const density = 2;
  const parts = [];
  const round = value => Math.round(value * 10000) / 10000;
  const palettes = {
    bark: ['#352a2c', '#604237', '#946443', '#b88955'],
    wrap: ['#3e3033', '#78463c', '#b9664c', '#dfa878'],
    brass: ['#5b4739', '#99744b', '#c3a16a', '#ead39c'],
    leaf: ['#243d35', '#386748', '#659652', '#aec875'],
    blade: ['#24453d', '#3c785a', '#72aa77', '#b8d9a0'],
    edge: ['#527c6c', '#91b89b', '#cae1b3', '#edf1cd'],
    vein: ['#4b6941', '#7d9c54', '#b8c977', '#d7d99a'],
    apple: ['#602c37', '#a53943', '#d85b50', '#f4966f'],
    glint: ['#a54442', '#e88368', '#ffd2a1', '#fff0ca']
  };
  const colors = Object.fromEntries(Object.entries(palettes).map(([key, values]) =>
    [key, values.map(value => [1, 3, 5].map(offset => parseInt(value.slice(offset, offset + 2), 16)))]));
  function part(name, group, from, to, material, rotation) {
    const element = { name, group, from, to, material };
    if (rotation) element.rotation = { origin: rotation[0], axis: rotation[1], angle: rotation[2] };
    parts.push(element);
  }
  function slab(name, group, left, right, bottom, top, depth, material, rotation) {
    part(name, group, [left, bottom, 8 - depth / 2], [right, top, 8 + depth / 2], material, rotation);
  }
  slab('root_pommel', 'handle', 7.23, 8.77, 0.2, 1.15, 1.5, 'brass');
  slab('root_end', 'handle', 7.55, 8.45, -0.15, 0.35, 1.0, 'bark');
  slab('grip_wood', 'handle', 7.4, 8.6, 0.95, 5.04, 1.3, 'bark');
  slab('branch_neck', 'handle', 7.45, 8.85, 4.65, 6.7, 1.45, 'bark');
  for (let band = 0; band < 4; band++) {
    const center = 1.55 + band * 0.78;
    slab(`grip_binding_${band}`, 'handle', 7.22, 8.78, center - 0.24, center + 0.24,
      1.65, 'wrap', [[8, center, 8], 'z', -22.5]);
  }
  slab('grip_collar', 'handle', 7.16, 8.96, 4.9, 5.42, 1.85, 'brass');
  slab('guard_branch_left', 'guard', 4.95, 8.1, 5.72, 6.46, 1.05, 'bark', [[8, 6.1, 8], 'z', -22.5]);
  slab('guard_branch_right', 'guard', 8.0, 11.5, 5.8, 6.54, 1.02, 'bark', [[8, 6.1, 8], 'z', 22.5]);
  slab('guard_leaf_left', 'guard', 4.4, 6.95, 6.03, 6.88, 0.62, 'leaf', [[8, 6.1, 8], 'z', -22.5]);
  slab('guard_leaf_left_tip', 'guard', 3.45, 4.65, 6.17, 6.7, 0.4, 'leaf', [[8, 6.1, 8], 'z', -22.5]);
  slab('guard_leaf_right', 'guard', 9.6, 12.0, 6.04, 6.9, 0.64, 'leaf', [[8, 6.1, 8], 'z', 22.5]);
  slab('guard_leaf_right_tip', 'guard', 11.75, 12.8, 6.2, 6.7, 0.4, 'leaf', [[8, 6.1, 8], 'z', 22.5]);
  slab('heart_socket', 'apple_heart', 6.43, 9.57, 5.87, 8.95, 1.9, 'brass');
  slab('apple_center', 'apple_heart', 6.38, 9.62, 6.43, 8.65, 2.72, 'apple');
  slab('apple_bottom', 'apple_heart', 6.93, 9.07, 5.94, 6.64, 2.06, 'apple');
  slab('apple_left_lobe', 'apple_heart', 5.97, 7.87, 7.47, 9.15, 2.22, 'apple');
  slab('apple_right_lobe', 'apple_heart', 8.14, 10.03, 7.47, 9.15, 2.24, 'apple');
  slab('apple_stalk', 'apple_heart', 7.83, 8.31, 8.75, 9.99, 0.6, 'bark', [[8.1, 8.9, 8], 'z', -22.5]);
  part('apple_highlight_front', 'apple_heart', [6.69, 7.78, 6.59], [7.3, 8.37, 6.68], 'glint');
  part('apple_highlight_back', 'apple_heart', [6.69, 7.78, 9.32], [7.3, 8.37, 9.41], 'glint');
  slab('blade_branch_lower', 'blade', 7.82, 8.7, 8.57, 12.1, 1.17, 'bark');
  slab('blade_branch_upper', 'blade', 8.16, 9.03, 11.85, 15.65, 1.02, 'bark');
  const diagonalSlope = Math.tan(Math.PI / 8);
  const cuttingPath = [
    [5.5 + (14 - 9.15) * diagonalSlope, 9.15],
    [5.5, 14],
    [5.5 + 5 * diagonalSlope, 19],
    [5.5 + 5 * diagonalSlope + 3.6, 22.6]
  ];
  const bladeTip = cuttingPath[3];
  const spineBase = bladeTip[0] - (bladeTip[1] - 18.2) * diagonalSlope;
  const spinePath = [[spineBase, 9.15], [spineBase, 18.2], bladeTip];
  function contourAt(points, height) {
    const endIndex = points.findIndex((point, index) => index > 0 && height <= point[1] + 0.00001);
    const end = points[endIndex < 0 ? points.length - 1 : endIndex];
    const start = points[endIndex < 0 ? points.length - 2 : endIndex - 1];
    return start[0] + (end[0] - start[0]) * (height - start[1]) / (end[1] - start[1]);
  }
  const bodyDepth = 0.8;
  const slices = Math.ceil((bladeTip[1] - cuttingPath[0][1]) / 0.5);
  for (let slice = 0; slice < slices; slice++) {
    const bottom = round(cuttingPath[0][1] + slice * 0.5);
    const top = Math.min(bladeTip[1], round(bottom + 0.5));
    const left = Math.max(contourAt(cuttingPath, bottom), contourAt(cuttingPath, top)) - 0.18;
    const right = Math.min(contourAt(spinePath, bottom), contourAt(spinePath, top)) + 0.12;
    slab(`leaf_blade_${slice}`, 'blade', left, right, slice === 0 ? bottom - 0.06 : bottom, top, bodyDepth, 'blade');
    parts[parts.length - 1].hiddenFaces = [
      ...(slice > 0 ? ['down'] : []),
      ...(slice < slices - 1 ? ['up'] : [])
    ];
  }
  function diagonalStrip(name, start, end, width, depth, material, angle, startPadding = 0.04, endPadding = 0.04) {
    const length = Math.hypot(end[0] - start[0], end[1] - start[1]);
    slab(name, 'blade', start[0] - width / 2, start[0] + width / 2,
      start[1] - startPadding, start[1] + length + endPadding, depth, material,
      angle ? [[start[0], start[1], 8], 'z', angle] : undefined);
  }
  const edgeAngles = [22.5, -22.5, -45];
  const jointPadding = (first, second) => 0.3 * Math.tan(Math.abs(first - second) * Math.PI / 360) + 0.005;
  for (let segment = 0; segment < cuttingPath.length - 1; segment++) {
    diagonalStrip(`cutting_edge_${segment}`, cuttingPath[segment], cuttingPath[segment + 1],
      0.6, 0.88 + segment * 0.16, 'edge', edgeAngles[segment],
      segment ? jointPadding(edgeAngles[segment - 1], edgeAngles[segment]) : 0.04,
      segment < edgeAngles.length - 1 ? jointPadding(edgeAngles[segment], edgeAngles[segment + 1]) : 0.04);
  }
  for (let segment = 0; segment < spinePath.length - 1; segment++) {
    diagonalStrip(`leaf_spine_${segment}`, spinePath[segment], spinePath[segment + 1],
      0.42, 1.12 + segment * 0.16, 'vein', [0, -22.5][segment]);
  }
  slab('leaf_tip', 'blade', bladeTip[0] - 0.17, bladeTip[0] + 0.17,
    bladeTip[1] - 0.17, bladeTip[1] + 0.17, 0.45, 'edge', [[...bladeTip, 8], 'z', -45]);
  for (const face of ['front', 'back']) {
    const front = face === 'front';
    for (let vein = 0; vein < 4; vein++) {
      const bottom = 12.0 + vein * 2.1;
      const left = contourAt(cuttingPath, bottom + 0.6) + 0.48;
      const right = contourAt(spinePath, bottom) - 0.32;
      const surface = 8 + (front ? -1 : 1) * (bodyDepth / 2 + 0.08);
      part(`vein_${face}_${vein}`, 'blade', [left, bottom, surface - 0.13],
        [right, bottom + 0.2, surface + 0.13], 'vein', [[right - 0.1, bottom, 8], 'z', -22.5]);
    }
  }
  const atlas = makeCanvas(textureSize, textureSize);
  const islands = [];
  const tiles = new Map();
  let cursorX = 1;
  let cursorY = 1;
  let rowHeight = 0;
  function paint(material, pixelX, pixelY, width, height) {
    const palette = colors[material];
    const horizontal = (pixelX + 0.5) / width;
    const vertical = (pixelY + 0.5) / height;
    let tone = horizontal < 0.25 ? 2 : horizontal < 0.72 ? 1 : 0;
    if (vertical < 0.16) tone = Math.min(3, tone + 1);
    if (material === 'apple') {
      tone = horizontal < 0.25 ? 2 : horizontal < 0.74 ? 1 : 0;
      if (vertical < 0.3 && horizontal < 0.45) tone = 3;
      if (vertical > 0.8) tone = Math.max(0, tone - 1);
    } else if (material === 'blade' || material === 'leaf') {
      tone = horizontal < 0.35 ? 2 : horizontal < 0.85 ? 1 : 0;
      if (height > 3 && width > 3 && pixelY === Math.floor(height * 0.56 - pixelX * 0.4)) tone = 2;
    } else if (material === 'bark') {
      if (width >= 3 && (pixelX + Math.floor(pixelY / 4)) % 4 === 0) tone = Math.max(0, tone - 1);
    } else if (material === 'edge') {
      tone = horizontal < 0.5 ? 3 : 2;
    } else if (material === 'wrap') {
      tone = (pixelX + pixelY) % 4 === 0 ? 0 : Math.min(3, tone + 1);
    } else if (material === 'glint') tone = pixelX === 0 ? 3 : 2;
    return palette[tone];
  }
  function allocate(material, modelWidth, modelHeight) {
    const width = Math.max(1, Math.round(modelWidth * density));
    const height = Math.max(1, Math.round(modelHeight * density));
    const key = `${material}:${width}:${height}`;
    if (tiles.has(key)) return tiles.get(key);
    if (cursorX + width + 1 > textureSize) {
      cursorX = 1;
      cursorY += rowHeight + 2;
      rowHeight = 0;
    }
    if (cursorY + height + 1 > textureSize) throw new Error(`Texture atlas overflow: ${key}`);
    for (let pixelY = -1; pixelY <= height; pixelY++) {
      for (let pixelX = -1; pixelX <= width; pixelX++) {
        atlas.put(cursorX + pixelX, cursorY + pixelY, paint(material,
          Math.max(0, Math.min(width - 1, pixelX)), Math.max(0, Math.min(height - 1, pixelY)), width, height));
      }
    }
    const rectangle = [cursorX, cursorY, cursorX + width, cursorY + height];
    islands.push({ material, rectangle, width, height });
    const uv = rectangle.map(value => round(value * 16 / textureSize));
    tiles.set(key, uv);
    cursorX += width + 2;
    rowHeight = Math.max(rowHeight, height);
    return uv;
  }
  const bladeSheet = allocate('blade', 7, 14);
  const densityRows = [];
  const elements = parts.map(element => {
    const [width, height, depth] = element.to.map((value, axis) => value - element.from[axis]);
    const faces = {};
    for (const [direction, faceWidth, faceHeight] of [
      ['north', width, height], ['south', width, height], ['east', depth, height],
      ['west', depth, height], ['up', width, depth], ['down', width, depth]
    ]) {
      if (element.hiddenFaces?.includes(direction)) continue;
      const rectangle = element.name.startsWith('leaf_blade_') && ['north', 'south'].includes(direction)
        ? [bladeSheet[0] + (element.from[0] - 5) * density * 16 / textureSize,
          bladeSheet[1] + (23 - element.to[1]) * density * 16 / textureSize,
          bladeSheet[0] + (element.to[0] - 5) * density * 16 / textureSize,
          bladeSheet[1] + (23 - element.from[1]) * density * 16 / textureSize].map(round)
        : allocate(element.material, faceWidth, faceHeight);
      const uv = direction === 'north' ? [rectangle[2], rectangle[1], rectangle[0], rectangle[3]] : rectangle.slice();
      faces[direction] = { uv, texture: '#surface' };
      densityRows.push({ name: element.name, direction, area: faceWidth * faceHeight,
        horizontal: round(Math.abs(uv[2] - uv[0]) * textureSize / 16 / faceWidth),
        vertical: round(Math.abs(uv[3] - uv[1]) * textureSize / 16 / faceHeight) });
    }
    const result = { name: element.name, from: element.from.map(round), to: element.to.map(round), faces };
    if (element.rotation) result.rotation = element.rotation;
    return result;
  });
  const display = {
    thirdperson_righthand: { rotation: [0, -90, 55], translation: [0, 4, 0.5], scale: [0.85, 0.85, 0.85] },
    thirdperson_lefthand: { rotation: [0, 90, -55], translation: [0, 4, 0.5], scale: [0.85, 0.85, 0.85] },
    firstperson_righthand: { rotation: [0, -90, 25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
    firstperson_lefthand: { rotation: [0, 90, -25], translation: [1.13, 3.2, 1.13], scale: [0.68, 0.68, 0.68] },
    gui: { rotation: [0, -25, -35], translation: [-0.5, -2.5, 0], scale: [0.66, 0.66, 0.66] },
    ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.5, 0.5, 0.5] },
    fixed: { rotation: [0, 180, 0], translation: [0, -2.5, 0], scale: [0.68, 0.68, 0.68] },
    head: { rotation: [0, 180, 0], translation: [0, 13, 7], scale: [1, 1, 1] }
  };
  const model = { ambientocclusion: false, gui_light: 'front',
    textures: { surface: 'ftboss:item/apple_stem_micui', particle: 'ftboss:item/apple_stem_micui' }, display, elements };
  const surfaceAudit = auditSurfaces(model);
  if (surfaceAudit.coplanar.length || surfaceAudit.nearCoplanar.length) {
    throw new Error(`Overlapping surface regression: ${JSON.stringify(surfaceAudit)}`);
  }
  const modelPath = path.join(outputRoot, 'models/item/apple_stem.json');
  const texturePath = path.join(outputRoot, 'textures/item/apple_stem_micui.png');
  fs.mkdirSync(path.dirname(modelPath), { recursive: true });
  fs.mkdirSync(path.dirname(texturePath), { recursive: true });
  writeSafe(modelPath, JSON.stringify(model, null, 2) + '\n');
  atlas.save(texturePath);
  writeSafe(path.join(outputRoot, 'apple_stem_surface_audit.json'), JSON.stringify(surfaceAudit, null, 2) + '\n');
  const stableUuid = label => {
    const digest = crypto.createHash('sha256').update(`ftboss:apple_stem:micui:${label}`).digest('hex');
    return `${digest.slice(0, 8)}-${digest.slice(8, 12)}-4${digest.slice(13, 16)}-a${digest.slice(17, 20)}-${digest.slice(20, 32)}`;
  };
  const groups = [...new Set(parts.map(element => element.group))];
  const editable = {
    meta: { format_version: '4.10', model_format: 'java_block', box_uv: false },
    name: '苹果爵士的果梗 · 蜜脆青枝', model_identifier: 'apple_stem',
    resolution: { width: textureSize, height: textureSize }, ambientocclusion: false, front_gui_light: true,
    elements: elements.map(element => {
      const converted = { name: element.name, type: 'cube', uuid: stableUuid(element.name),
        from: element.from, to: element.to, origin: element.rotation?.origin || [8, 8, 8],
        rotation: [0, 0, 0], rescale: false, shade: true, box_uv: false, faces: {} };
      if (element.rotation) converted.rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
      for (const [direction, face] of Object.entries(element.faces)) {
        converted.faces[direction] = { uv: face.uv.map(value => round(value * textureSize / 16)), texture: 0 };
      }
      for (const direction of ['north', 'south', 'east', 'west', 'up', 'down']) {
        if (!converted.faces[direction]) converted.faces[direction] = { uv: [0, 0, 0, 0], texture: null };
      }
      return converted;
    }),
    outliner: groups.map(name => ({ name, uuid: stableUuid(name), origin: [8, 8, 8],
      rotation: [0, 0, 0], export: true, isOpen: true, visibility: true,
      children: parts.filter(element => element.group === name).map(element => stableUuid(element.name)) })),
    textures: [{ path: '', name: 'apple_stem_micui.png', folder: 'item', namespace: 'ftboss',
      id: 'surface', uuid: stableUuid('texture'), mode: 'bitmap', render_mode: 'default',
      saved: false, width: textureSize, height: textureSize, uv_width: textureSize, uv_height: textureSize,
      source: 'data:image/png;base64,' + fs.readFileSync(texturePath).toString('base64') }], display
  };
  writeSafe(path.join(outputRoot, 'apple_stem_micui.bbmodel'), JSON.stringify(editable, null, 2) + '\n');
  const broadFaces = densityRows.filter(face => face.area >= 1 && !/vein_|highlight/.test(face.name));
  const report = { id: 'ftboss:apple_stem', concept: '蜜脆青枝', elements: elements.length,
    rotatedElements: elements.filter(element => element.rotation).length, textureSize, targetDensity: density,
    atlasIslands: islands.length, atlasHeight: cursorY + rowHeight + 1,
    bladeOutline: { revision: 'separated_surfaces_v3', cuttingPath, spinePath, bodyDepth, slices, continuousSurfaceUV: true },
    omittedInternalFaces: parts.reduce((count, element) => count + (element.hiddenFaces?.length || 0), 0),
    broadFaceDensity: { min: Math.min(...broadFaces.flatMap(face => [face.horizontal, face.vertical])),
      max: Math.max(...broadFaces.flatMap(face => [face.horizontal, face.vertical])) },
    densityExceptions: densityRows.filter(face => Math.max(face.horizontal, face.vertical) > 3),
    dimensions: [0, 1, 2].map(axis => round(Math.max(...elements.map(element => element.to[axis])) -
      Math.min(...elements.map(element => element.from[axis])))),
    runtime: 'Static Java item model; no animation, emissive layer, or gameplay changes.',
    handTransforms: 'Preserved from previous apple_stem; in-game calibration still required.', islands };
  writeSafe(path.join(outputRoot, 'apple_stem_report.json'), JSON.stringify(report, null, 2) + '\n');
  console.log(JSON.stringify({ modelPath, texturePath, elements: report.elements,
    textureSize, broadFaceDensity: report.broadFaceDensity, atlasHeight: report.atlasHeight }));
  return { model, texturePath, report };
}

module.exports = { generateAppleStem };
if (require.main === module) {
  if (!process.argv[2]) throw new Error('Usage: node gen_apple_stem.cjs <output-assets-directory> [model3d-library-directory]');
  generateAppleStem(path.resolve(process.argv[2]), process.argv[3] ? path.resolve(process.argv[3]) : __dirname);
}
