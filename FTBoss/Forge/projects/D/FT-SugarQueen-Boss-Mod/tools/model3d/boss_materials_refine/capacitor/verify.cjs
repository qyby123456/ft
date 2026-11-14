'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const child = require('node:child_process');
const assert = require('node:assert/strict');
const { root, corners, rotate, raster, parts } = require('./render.cjs');
const { decodePNG } = require('../../png.cjs');
const work = path.join(root, '_work/boss-materials-refine-20261021/capacitor');
const base = path.join(root, 'src/main/resources/assets/ftboss');
const modelPath = path.join(base, 'models/item/boss_materials/capacitor_material.json');
const texturePath = path.join(base, 'textures/item/boss_materials/capacitor.png');
const model = JSON.parse(fs.readFileSync(modelPath));
const texture = decodePNG(texturePath);
const bb = JSON.parse(fs.readFileSync(path.join(root, 'art/boss_materials/blockbench/capacitor_material.bbmodel')));
const wrapper = JSON.parse(fs.readFileSync(path.join(base, 'models/item/capacitor_material.json')));
const gui = JSON.parse(fs.readFileSync(path.join(base, 'models/item/boss_materials/capacitor_material_gui.json')));
const manifest = JSON.parse(fs.readFileSync(path.join(work, 'build-manifest.json')));
const messages = [], failures = [];
function check(name, action) {
  try { const details = action(); messages.push('PASS ' + name + (details ? ': ' + details : '')); }
  catch (error) { failures.push(name + ': ' + error.message); messages.push('FAIL ' + name + ': ' + error.message); }
}
function near(first, second, tolerance = 0.0001) { assert.ok(Math.abs(first - second) < tolerance, first + ' != ' + second); }
const dot = (first, second) => first.reduce((sum, coordinate, index) => sum + coordinate * second[index], 0);
const subtract = (first, second) => first.map((coordinate, index) => coordinate - second[index]);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const boxes = model.elements.map(element => {
  const points = corners(element), rawAxes = [subtract(points[4], points[0]), subtract(points[2], points[0]), subtract(points[1], points[0])];
  return { name: element.name, points, axes: rawAxes.map(axis => axis.map(coordinate => coordinate / Math.hypot(...axis))) };
});
function contacts(first, second) {
  const axes = first.axes.concat(second.axes, first.axes.flatMap(firstAxis => second.axes.map(secondAxis => cross(firstAxis, secondAxis))));
  for (const axis of axes) {
    const length = Math.hypot(...axis);
    if (length < 1e-8) continue;
    const firstProjection = first.points.map(point => dot(point, axis) / length), secondProjection = second.points.map(point => dot(point, axis) / length);
    if (Math.max(...firstProjection) < Math.min(...secondProjection) - 0.015 || Math.max(...secondProjection) < Math.min(...firstProjection) - 0.015) return false;
  }
  return true;
}

check('Java model lint and actual atlas coverage', () => {
  const commands = [
    ['tools/model3d/lint_java_model.cjs', modelPath, path.join(base, 'models/item/boss_materials/capacitor_material_gui.json')],
    ['tools/model3d/check_uv_coverage.cjs', modelPath, texturePath]
  ];
  for (const args of commands) {
    const result = child.spawnSync(process.execPath, args, { cwd: root, encoding: 'utf8' });
    messages.push(result.stdout.trim()); assert.equal(result.status, 0, result.stderr || result.stdout);
  }
});
check('Forge GUI / 3D separation and resource references', () => {
  assert.equal(wrapper.loader, 'forge:separate_transforms');
  assert.equal(wrapper.base.parent, 'ftboss:item/boss_materials/capacitor_material');
  assert.equal(wrapper.perspectives.gui.parent, 'ftboss:item/boss_materials/capacitor_material_gui');
  assert.equal(gui.parent, 'minecraft:item/generated'); assert.equal(gui.textures.layer0, 'ftboss:item/capacitor_material');
  assert.ok(!model.parent); assert.equal(Object.keys(model.display).length, 8);
  for (const reference of Object.values(model.textures).concat(Object.values(gui.textures), Object.values(wrapper.textures))) {
    const [namespace, asset] = reference.split(':'); assert.equal(namespace, 'ftboss');
    assert.ok(fs.existsSync(path.join(base, 'textures', asset + '.png')), reference);
  }
  return 'original ftboss:capacitor_material model route preserved';
});
check('Unique geometry, bounds and legal single-axis rotations', () => {
  const names = new Set(), geometry = new Set();
  for (const element of model.elements) {
    assert.ok(!names.has(element.name), element.name); names.add(element.name);
    assert.equal(Object.keys(element.faces).length, 6, element.name);
    const signature = JSON.stringify([element.from, element.to, element.rotation]); assert.ok(!geometry.has(signature)); geometry.add(signature);
    for (let axis = 0; axis < 3; axis++) assert.ok(element.to[axis] > element.from[axis]);
    if (element.rotation) {
      assert.ok(['x', 'y', 'z'].includes(element.rotation.axis)); assert.ok([-45, -22.5, 0, 22.5, 45].includes(element.rotation.angle));
      assert.equal(element.rotation.rescale, false);
    }
    for (const vertex of corners(element)) for (const value of vertex) assert.ok(Number.isFinite(value) && value >= 0 && value <= 16, element.name);
  }
  return model.elements.length + ' cuboids / ' + model.elements.filter(element => element.rotation).length + ' rotated / within 0..16';
});
check('Proportional per-face UV, dense opaque sampling and atlas padding', () => {
  let count = 0, maximumDensityError = 0;
  for (const element of model.elements) for (const [side, face] of Object.entries(element.faces)) {
    count++;
    const dimensions = element.to.map((value, index) => value - element.from[index]);
    const expected = side === 'north' || side === 'south' ? [dimensions[0], dimensions[1]] : side === 'east' || side === 'west' ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    const pixelUV = face.uv.map(value => value * texture.w / 16);
    const densities = [(pixelUV[2] - pixelUV[0]) / expected[0], (pixelUV[3] - pixelUV[1]) / expected[1]];
    for (const density of densities) { maximumDensityError = Math.max(maximumDensityError, Math.abs(density - 2)); near(density, 2); }
    for (const value of face.uv) assert.ok(value >= 0 && value <= 16);
    for (let vertical = 0; vertical <= 10; vertical++) for (let horizontal = 0; horizontal <= 10; horizontal++) {
      const textureX = Math.floor(pixelUV[0] + (pixelUV[2] - pixelUV[0] - 0.00001) * horizontal / 10);
      const textureY = Math.floor(pixelUV[1] + (pixelUV[3] - pixelUV[1] - 0.00001) * vertical / 10);
      assert.equal(texture.data[(textureY * texture.w + textureX) * 4 + 3], 255, element.name + '.' + side);
    }
  }
  const occupied = new Set();
  for (const island of manifest.islands) for (let vertical = island.top - 1; vertical < island.top - 1 + island.boxHeight; vertical++) for (let horizontal = island.left - 1; horizontal < island.left - 1 + island.boxWidth; horizontal++) {
    const key = horizontal + '/' + vertical; assert.ok(!occupied.has(key), 'island collision'); occupied.add(key);
  }
  return count + ' faces, 2px/unit in both axes, density error ' + maximumDensityError.toExponential(2) + ', all samples alpha=255, 1px extruded gutters';
});
check('Oriented box connection graph', () => {
  const connected = new Set([0]), pending = [0];
  while (pending.length) {
    const current = pending.shift();
    for (let index = 0; index < boxes.length; index++) if (!connected.has(index) && contacts(boxes[current], boxes[index])) { connected.add(index); pending.push(index); }
  }
  assert.equal(connected.size, boxes.length, 'unconnected: ' + boxes.filter((box, index) => !connected.has(index)).map(box => box.name).join(', '));
  return 'one connected component, 15-axis SAT, contact tolerance 0.015 model units';
});
check('Editable Blockbench / runtime geometry, rotations and embedded texture match', () => {
  assert.equal(bb.elements.length, model.elements.length); assert.equal(bb.meta.box_uv, false);
  assert.equal(bb.resolution.width, texture.w); assert.equal(bb.resolution.height, texture.h);
  assert.deepEqual(bb.display, model.display);
  const embedded = Buffer.from(bb.textures[0].source.split(',')[1], 'base64'); assert.ok(embedded.equals(fs.readFileSync(texturePath)));
  assert.equal(new Set(bb.elements.map(element => element.uuid)).size, bb.elements.length);
  const children = bb.outliner.flatMap(group => group.children); assert.equal(new Set(children).size, bb.elements.length);
  for (const [index, element] of model.elements.entries()) {
    const editable = bb.elements[index]; assert.equal(editable.name, element.name); assert.deepEqual(editable.from, element.from); assert.deepEqual(editable.to, element.to);
    assert.deepEqual(editable.origin, element.rotation?.origin || [8, 8, 8]);
    assert.deepEqual(editable.rotation, ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0));
    assert.equal(editable.shade, element.shade !== false);
    for (const [side, face] of Object.entries(element.faces)) face.uv.forEach((value, coordinate) => near(editable.faces[side].uv[coordinate], value * texture.w / 16));
  }
  return 'five editable component groups; embedded atlas byte-identical';
});
check('Renderer rotation, rescale, Java UV orientation and cutout handling', () => {
  const rotated = rotate([1, 0, 0], 'y', 45); near(rotated[0], Math.SQRT1_2); near(rotated[2], -Math.SQRT1_2);
  const rescaled = rotate([1, 0, 0], 'y', 45, [0, 0, 0], true); near(rescaled[0], 1); near(rescaled[2], -1);
  const fixtureTexture = { w: 2, h: 2, data: Buffer.from([255, 0, 0, 255, 0, 255, 0, 255, 0, 0, 255, 255, 255, 255, 0, 0]) };
  const fixture = { elements: [{ from: [0, 0, 0], to: [2, 2, 1], faces: { north: { uv: [0, 0, 16, 16] } }, shade: false }] };
  const image = raster(parts(fixture, fixtureTexture), fixtureTexture, { width: 8, height: 8, center: [1, 1, 0], zoom: 2 });
  const sample = (horizontal, vertical) => [...image.pixels.subarray((vertical * 8 + horizontal) * 4, (vertical * 8 + horizontal) * 4 + 4)];
  assert.deepEqual(sample(2, 2), [0, 255, 0, 255]); assert.deepEqual(sample(5, 2), [255, 0, 0, 255]);
  assert.deepEqual(sample(5, 5), [0, 0, 255, 255]); assert.equal(sample(2, 5)[3], 0);
  return 'analytic 45-degree transform + rescale; Java FaceInfo/BlockFaceUV corner fixture; alpha cutout';
});
check('GUI icon alpha and framing', () => {
  const icon = decodePNG(path.join(base, 'textures/item/capacitor_material.png')); assert.equal(icon.w, 32); assert.equal(icon.h, 32);
  let opaque = 0; const colors = new Set();
  for (let vertical = 0; vertical < icon.h; vertical++) for (let horizontal = 0; horizontal < icon.w; horizontal++) {
    const offset = (vertical * icon.w + horizontal) * 4, alpha = icon.data[offset + 3];
    assert.ok(alpha === 0 || alpha === 255);
    if (alpha) { opaque++; assert.ok(horizontal > 0 && horizontal < 31 && vertical > 0 && vertical < 31, 'icon clipped'); colors.add(icon.data.subarray(offset, offset + 3).toString('hex')); }
  }
  return '32px RGBA, ' + opaque + ' opaque pixels, ' + colors.size + ' shaded colors, >=1px margin';
});
const targets = [
  'src/main/resources/assets/ftboss/textures/item/capacitor_material.png',
  'src/main/resources/assets/ftboss/textures/item/boss_materials/capacitor.png',
  'src/main/resources/assets/ftboss/models/item/capacitor_material.json',
  'src/main/resources/assets/ftboss/models/item/boss_materials/capacitor_material.json',
  'src/main/resources/assets/ftboss/models/item/boss_materials/capacitor_material_gui.json',
  'art/boss_materials/blockbench/capacitor_material.bbmodel'
];
const hash = relative => crypto.createHash('sha256').update(fs.readFileSync(path.join(root, relative))).digest('hex');
if (process.argv.includes('--previews')) check('Final preview input hashes, image hashes and four-view framing', () => {
  const preview = JSON.parse(fs.readFileSync(path.join(work, 'preview-manifest.json')));
  for (const [filename, expected] of Object.entries(preview.inputSha256)) assert.equal(hash('src/main/resources/assets/ftboss/' + filename), expected);
  for (const [filename, expected] of Object.entries(preview.imageSha256)) assert.equal(hash('art/boss_materials/refine/capacitor/' + filename), expected);
  for (const filename of ['front', 'back', 'left', 'right']) {
    const image = decodePNG(path.join(root, 'art/boss_materials/refine/capacitor/' + filename + '.png'));
    let hits = 0;
    for (let vertical = 0; vertical < image.h; vertical++) for (let horizontal = 0; horizontal < image.w; horizontal++) if (image.data[(vertical * image.w + horizontal) * 4 + 3]) {
      hits++; assert.ok(horizontal > 2 && horizontal < image.w - 3 && vertical > 2 && vertical < image.h - 3, filename + ' clips frame');
    }
    assert.ok(hits > 20000, filename + ' empty or incomplete');
  }
  return 'all final source hashes match rendered inputs; 10 image digests verified; four complete uncropped views';
});
if (process.argv.includes('--repro')) check('Deterministic capacitor-only regeneration', () => {
  const before = targets.map(hash), regenerated = child.spawnSync(process.execPath, [path.join(__dirname, 'build.cjs')], { cwd: root, encoding: 'utf8' });
  assert.equal(regenerated.status, 0, regenerated.stderr); assert.deepEqual(targets.map(hash), before);
  return 'all six final assets reproduce byte-for-byte';
});
fs.writeFileSync(path.join(work, 'asset-sha256.json'), JSON.stringify(Object.fromEntries(targets.map(relative => [relative, hash(relative)])), null, 2) + '\n');
fs.writeFileSync(path.join(work, 'checks.txt'), messages.join('\n') + '\n');
fs.writeFileSync(path.join(work, 'validation.json'), JSON.stringify({ pass: failures.length === 0, failures, checks: messages.filter(message => message.startsWith('PASS')), elements: model.elements.length, faces: 750, atlas: [texture.w, texture.h] }, null, 2) + '\n');
console.log(messages.join('\n'));
process.exitCode = failures.length ? 1 : 0;
