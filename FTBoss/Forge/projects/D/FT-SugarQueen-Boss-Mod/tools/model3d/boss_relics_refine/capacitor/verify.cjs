'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const child = require('node:child_process');
const { root, corners } = require('../../boss_materials_refine/capacitor/render.cjs');
const { decodePNG } = require('../../png.cjs');
const base = path.join(root, 'src/main/resources/assets/ftboss');
const work = path.join(root, '_work/boss-relics-refine-20261021/capacitor');
const item = 'charged_core_pendant';
const modelPath = path.join(base, 'models/item/boss_relics/' + item + '.json');
const texturePath = path.join(base, 'textures/item/boss_relics/' + item + '.png');
const read = filename => JSON.parse(fs.readFileSync(filename));
const model = read(modelPath), texture = decodePNG(texturePath);
const bb = read(path.join(root, 'art/boss_relics/blockbench/' + item + '.bbmodel'));
const manifest = read(path.join(work, 'build-manifest.json'));
const messages = [], failures = [];
const near = (first, second) => assert.ok(Math.abs(first - second) < 0.0001, first + ' != ' + second);
function check(name, action) {
  try { const detail = action(); messages.push('PASS ' + name + (detail ? ': ' + detail : '')); }
  catch (error) { failures.push(name + ': ' + error.message); messages.push('FAIL ' + name + ': ' + error.message); }
}
check('Java model lint and texture coverage', () => {
  for (const args of [
    ['tools/model3d/lint_java_model.cjs', modelPath, path.join(base, 'models/item/boss_relics/' + item + '_gui.json')],
    ['tools/model3d/check_uv_coverage.cjs', modelPath, texturePath]
  ]) {
    const result = child.spawnSync(process.execPath, args, { cwd: root, encoding: 'utf8' });
    messages.push(result.stdout.trim()); assert.equal(result.status, 0, result.stderr || result.stdout);
  }
});
check('Forge GUI separation, resource references and display', () => {
  const wrapper = read(path.join(base, 'models/item/' + item + '.json'));
  const gui = read(path.join(base, 'models/item/boss_relics/' + item + '_gui.json'));
  assert.equal(wrapper.loader, 'forge:separate_transforms'); assert.equal(wrapper.base.parent, 'ftboss:item/boss_relics/' + item);
  assert.equal(wrapper.perspectives.gui.parent, 'ftboss:item/boss_relics/' + item + '_gui');
  assert.equal(gui.parent, 'minecraft:item/generated'); assert.equal(gui.textures.layer0, 'ftboss:item/' + item);
  assert.ok(!model.parent); assert.equal(Object.keys(model.display).length, 8);
  for (const reference of Object.values(model.textures).concat(Object.values(gui.textures), Object.values(wrapper.textures))) {
    assert.ok(reference.startsWith('ftboss:')); assert.ok(fs.existsSync(path.join(base, 'textures', reference.split(':')[1] + '.png')));
  }
  return 'ftboss:charged_core_pendant unchanged; independent 2D GUI + actual 3D base';
});
check('Geometry, single-axis legal rotations and transformed bounds', () => {
  const names = new Set(), signatures = new Set();
  for (const element of model.elements) {
    assert.ok(!names.has(element.name), element.name); names.add(element.name);
    const signature = JSON.stringify([element.from, element.to, element.rotation]); assert.ok(!signatures.has(signature), element.name); signatures.add(signature);
    assert.equal(Object.keys(element.faces).length, 6);
    element.from.forEach((value, index) => assert.ok(element.to[index] > value));
    if (element.rotation) { assert.ok(['x', 'y', 'z'].includes(element.rotation.axis)); assert.ok([-45, -22.5, 0, 22.5, 45].includes(element.rotation.angle)); assert.equal(element.rotation.rescale, false); }
    for (const point of corners(element)) for (const value of point) assert.ok(Number.isFinite(value) && value >= 0 && value <= 16, element.name);
  }
  return model.elements.length + ' unique elements / ' + model.elements.filter(element => element.rotation).length + ' rotated / 0..16 bounds';
});
check('Per-face proportions, atlas gutters and dense alpha coverage', () => {
  let faces = 0, densityError = 0;
  for (const element of model.elements) for (const [side, face] of Object.entries(element.faces)) {
    faces++;
    const dimensions = element.to.map((coordinate, index) => coordinate - element.from[index]);
    const expected = side === 'north' || side === 'south' ? [dimensions[0], dimensions[1]] : side === 'east' || side === 'west' ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    const uv = face.uv.map(value => value * texture.w / 16);
    for (const value of face.uv) assert.ok(value >= 0 && value <= 16);
    [(uv[2] - uv[0]) / expected[0], (uv[3] - uv[1]) / expected[1]].forEach(value => { near(value, 2); densityError = Math.max(densityError, Math.abs(value - 2)); });
    for (let vertical = 0; vertical <= 10; vertical++) for (let horizontal = 0; horizontal <= 10; horizontal++) {
      const textureX = Math.floor(uv[0] + (uv[2] - uv[0] - 0.00001) * horizontal / 10);
      const textureY = Math.floor(uv[1] + (uv[3] - uv[1] - 0.00001) * vertical / 10);
      assert.equal(texture.data[(textureY * texture.w + textureX) * 4 + 3], 255, element.name + '.' + side);
    }
  }
  const occupied = new Set();
  for (const island of manifest.islands) for (let vertical = island.top - 1; vertical < island.top - 1 + island.boxHeight; vertical++) for (let horizontal = island.left - 1; horizontal < island.left - 1 + island.boxWidth; horizontal++) {
    const key = horizontal + '/' + vertical; assert.ok(!occupied.has(key)); occupied.add(key);
  }
  assert.equal(manifest.pixelAudit.goldArtifacts, 0);
  return faces + ' faces / 2px per unit / max error ' + densityError.toExponential(2) + ' / 121 alpha samples per face / 1px gutters';
});
const subtract = (first, second) => first.map((value, index) => value - second[index]);
const dot = (first, second) => first.reduce((sum, value, index) => sum + value * second[index], 0);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const boxes = model.elements.map(element => {
  const points = corners(element);
  const axes = [subtract(points[4], points[0]), subtract(points[2], points[0]), subtract(points[1], points[0])].map(axis => axis.map(value => value / Math.hypot(...axis)));
  return { name: element.name, points, axes };
});
function contacts(first, second) {
  const axes = first.axes.concat(second.axes, first.axes.flatMap(firstAxis => second.axes.map(secondAxis => cross(firstAxis, secondAxis))));
  for (const axis of axes) {
    const length = Math.hypot(...axis); if (length < 1e-8) continue;
    const firstProjection = first.points.map(point => dot(point, axis) / length), secondProjection = second.points.map(point => dot(point, axis) / length);
    if (Math.max(...firstProjection) < Math.min(...secondProjection) - 0.02 || Math.max(...secondProjection) < Math.min(...firstProjection) - 0.02) return false;
  }
  return true;
}
check('OBB connection graph for housing, settings, electrodes and suspension', () => {
  const connected = new Set([0]), queue = [0];
  while (queue.length) {
    const current = queue.shift();
    for (let index = 0; index < boxes.length; index++) if (!connected.has(index) && contacts(boxes[current], boxes[index])) { connected.add(index); queue.push(index); }
  }
  assert.equal(connected.size, boxes.length, 'unconnected: ' + boxes.filter((box, index) => !connected.has(index)).map(box => box.name).join(', '));
  return 'all ' + boxes.length + ' parts connected; 15-axis SAT tolerance 0.02 units';
});
check('Editable Blockbench exactly matches final runtime model and atlas', () => {
  assert.equal(bb.elements.length, model.elements.length); assert.equal(bb.meta.box_uv, false); assert.equal(bb.resolution.width, texture.w);
  assert.deepEqual(bb.display, model.display); assert.ok(Buffer.from(bb.textures[0].source.split(',')[1], 'base64').equals(fs.readFileSync(texturePath)));
  assert.equal(new Set(bb.elements.map(element => element.uuid)).size, model.elements.length);
  assert.equal(new Set(bb.outliner.flatMap(group => group.children)).size, model.elements.length);
  for (const [index, element] of model.elements.entries()) {
    const editable = bb.elements[index]; assert.equal(editable.name, element.name); assert.deepEqual(editable.from, element.from); assert.deepEqual(editable.to, element.to);
    assert.deepEqual(editable.origin, element.rotation?.origin || [8, 8, 8]); assert.equal(editable.shade, element.shade !== false);
    assert.deepEqual(editable.rotation, ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0));
    for (const [side, face] of Object.entries(element.faces)) face.uv.forEach((value, coordinate) => near(editable.faces[side].uv[coordinate], value * texture.w / 16));
  }
  return bb.outliner.length + ' editable part groups; embedded texture byte-identical';
});
check('Independent GUI icon framing and alpha', () => {
  const icon = decodePNG(path.join(base, 'textures/item/' + item + '.png')); assert.equal(icon.w, 32); assert.equal(icon.h, 32);
  let hits = 0;
  for (let vertical = 0; vertical < 32; vertical++) for (let horizontal = 0; horizontal < 32; horizontal++) {
    const alpha = icon.data[(vertical * 32 + horizontal) * 4 + 3]; assert.ok(alpha === 0 || alpha === 255);
    if (alpha) { hits++; assert.ok(horizontal > 0 && horizontal < 31 && vertical > 0 && vertical < 31, 'clipped icon'); }
  }
  return '32x32 RGBA / ' + hits + ' opaque pixels / >=1px safe margin';
});
const targets = ['textures/item/' + item + '.png', 'textures/item/boss_relics/' + item + '.png', 'models/item/' + item + '.json', 'models/item/boss_relics/' + item + '.json', 'models/item/boss_relics/' + item + '_gui.json'].map(relative => 'src/main/resources/assets/ftboss/' + relative).concat('art/boss_relics/blockbench/' + item + '.bbmodel');
const hash = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
if (process.argv.includes('--repro')) check('Deterministic relic-only regeneration', () => {
  const before = targets.map(relative => hash(path.join(root, relative)));
  const result = child.spawnSync(process.execPath, [path.join(__dirname, 'build.cjs')], { cwd: root, encoding: 'utf8' }); assert.equal(result.status, 0, result.stderr);
  assert.deepEqual(targets.map(relative => hash(path.join(root, relative))), before); return 'all six assets reproduce byte-for-byte';
});
if (process.argv.includes('--previews')) check('Final source/image hashes and four-view framing', () => {
  const preview = read(path.join(work, 'preview-manifest.json'));
  for (const [relative, expected] of Object.entries(preview.inputSha256)) assert.equal(hash(path.join(base, relative)), expected);
  for (const [filename, expected] of Object.entries(preview.imageSha256)) assert.equal(hash(path.join(root, 'art/boss_relics/refine/capacitor', filename)), expected);
  for (const name of ['front', 'back', 'left', 'right']) {
    const image = decodePNG(path.join(root, 'art/boss_relics/refine/capacitor', name + '.png')); let hits = 0;
    for (let vertical = 0; vertical < image.h; vertical++) for (let horizontal = 0; horizontal < image.w; horizontal++) if (image.data[(vertical * image.w + horizontal) * 4 + 3]) {
      hits++; assert.ok(horizontal > 2 && horizontal < image.w - 3 && vertical > 2 && vertical < image.h - 3, name + ' clips frame');
    }
    assert.ok(hits > 10000, name + ' incomplete');
  }
  return 'final runtime inputs and 10 previews matched; all four views complete and uncropped';
});
fs.writeFileSync(path.join(work, 'asset-sha256.json'), JSON.stringify(Object.fromEntries(targets.map(relative => [relative, hash(path.join(root, relative))])), null, 2) + '\n');
fs.writeFileSync(path.join(work, 'checks.txt'), messages.join('\n') + '\n');
fs.writeFileSync(path.join(work, 'validation.json'), JSON.stringify({ pass: !failures.length, failures, checks: messages.filter(message => message.startsWith('PASS')), elements: model.elements.length, faces: model.elements.length * 6, texture: [texture.w, texture.h] }, null, 2) + '\n');
console.log(messages.join('\n')); process.exitCode = failures.length ? 1 : 0;
