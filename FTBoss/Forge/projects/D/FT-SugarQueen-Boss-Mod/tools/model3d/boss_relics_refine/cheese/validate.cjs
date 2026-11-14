const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const childProcess = require('node:child_process');
const assert = require('node:assert/strict');
const { decodePNG } = require('../../png.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const work = path.join(root, '_work/boss-relics-refine-20261021/cheese');
const files = ['models/item/cheese_moon_charm.json', 'models/item/boss_relics/cheese_moon_charm.json', 'models/item/boss_relics/cheese_moon_charm_gui.json', 'textures/item/cheese_moon_charm.png', 'textures/item/boss_relics/cheese_moon_charm.png'].map(file => path.join(assets, file));
const bbFile = path.join(root, 'art/boss_relics/blockbench/cheese_moon_charm.bbmodel');
const [wrapper, model, gui] = files.slice(0, 3).map(file => JSON.parse(fs.readFileSync(file, 'utf8')));
const bb = JSON.parse(fs.readFileSync(bbFile, 'utf8'));
const atlas = decodePNG(files[4]);
const icon = decodePNG(files[3]);
assert.equal(wrapper.loader, 'forge:separate_transforms');
assert.equal(wrapper.base.parent, 'ftboss:item/boss_relics/cheese_moon_charm');
assert.equal(wrapper.perspectives.gui.parent, 'ftboss:item/boss_relics/cheese_moon_charm_gui');
assert.equal(gui.parent, 'minecraft:item/generated');
assert.equal(gui.textures.layer0, 'ftboss:item/cheese_moon_charm');
assert.equal(model.parent, undefined);
assert.equal(Object.keys(model.display).length, 7);
assert.equal(icon.w, 32);
assert.equal(icon.h, 32);
assert.equal(atlas.w, 256);
assert.equal(atlas.h, 128);
assert.equal(bb.elements.length, model.elements.length);
assert.deepEqual(Buffer.from(bb.textures[0].source.split(',')[1], 'base64'), fs.readFileSync(files[4]));
let faces = 0;
let rotated = 0;
let maximumAspectQuantization = 0;
for (const [index, element] of model.elements.entries()) {
  assert.equal(bb.elements[index].name, element.name);
  assert.deepEqual(bb.elements[index].from, element.from);
  assert.deepEqual(bb.elements[index].to, element.to);
  assert.deepEqual(bb.elements[index].origin, element.rotation ? element.rotation.origin : [8, 8, 8]);
  assert.deepEqual(bb.elements[index].rotation, element.rotation ? ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0) : [0, 0, 0]);
  for (const point of [element.from, element.to]) assert.ok(point.every(value => Number.isFinite(value) && value >= -16 && value <= 32));
  if (element.rotation) {
    assert.ok(['x', 'y', 'z'].includes(element.rotation.axis));
    assert.ok([-45, -22.5, 0, 22.5, 45].includes(element.rotation.angle));
    assert.equal(element.rotation.rescale, false);
    rotated++;
  }
  const dimensions = element.to.map((value, axis) => value - element.from[axis]);
  for (const [face, settings] of Object.entries(element.faces)) {
    assert.ok(settings.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
    assert.equal(settings.texture, '#0');
    assert.deepEqual(bb.elements[index].faces[face].uv, settings.uv.map((value, axis) => +(value * (axis % 2 ? atlas.h : atlas.w) / 16).toFixed(4)));
    const faceDimensions = ['north', 'south'].includes(face) ? [dimensions[0], dimensions[1]] : ['east', 'west'].includes(face) ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
    assert.ok(faceDimensions.every(value => value > 0));
    const density = element.name.endsWith('_collar') ? 4 : 2;
    const uvSize = [Math.abs(settings.uv[2] - settings.uv[0]) * atlas.w / 16, Math.abs(settings.uv[3] - settings.uv[1]) * atlas.h / 16];
    for (let axis = 0; axis < 2; axis++) {
      const expected = Math.max(1, Math.round(faceDimensions[axis] * density));
      assert.ok(Math.abs(expected - uvSize[axis]) < .002, element.name + ': UV stretch');
      maximumAspectQuantization = Math.max(maximumAspectQuantization, Math.abs(uvSize[axis] - faceDimensions[axis] * density));
    }
    const resource = model.textures[settings.texture.slice(1)];
    assert.ok(fs.existsSync(path.join(assets, 'textures', resource.split(':')[1] + '.png')));
    faces++;
  }
}
const grouped = bb.outliner.flatMap(group => group.children);
assert.equal(grouped.length, bb.elements.length);
assert.equal(new Set(grouped).size, bb.elements.length);
const lint = childProcess.spawnSync(process.execPath, [path.join(root, 'tools/model3d/lint_java_model.cjs'), files[1], files[2]], { encoding: 'utf8' });
const coverage = childProcess.spawnSync(process.execPath, [path.join(root, 'tools/model3d/check_uv_coverage.cjs'), files[1], files[4]], { encoding: 'utf8' });
fs.writeFileSync(path.join(work, 'lint.log'), lint.stdout + lint.stderr);
fs.writeFileSync(path.join(work, 'coverage.log'), coverage.stdout + coverage.stderr);
assert.equal(lint.status, 0, lint.stdout);
assert.equal(coverage.status, 0, coverage.stdout);
const hash = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
const design = JSON.parse(fs.readFileSync(path.join(root, 'art/boss_relics/refine/cheese/design.json'), 'utf8'));
for (const island of design.islands) {
  const [left, top, width, height] = island.pixels;
  assert.ok(left >= 1 && top >= 1 && left + width < atlas.w && top + height < atlas.h, island.name + ': atlas bounds');
}
for (let first = 0; first < design.islands.length; first++) for (let second = first + 1; second < design.islands.length; second++) {
  const firstRect = design.islands[first].pixels;
  const secondRect = design.islands[second].pixels;
  const overlap = firstRect[0] < secondRect[0] + secondRect[2] && firstRect[0] + firstRect[2] > secondRect[0] && firstRect[1] < secondRect[1] + secondRect[3] && firstRect[1] + firstRect[3] > secondRect[1];
  assert.equal(overlap, false, 'UV island overlap');
}
const hashes = Object.fromEntries([...files, bbFile].map(filename => [path.relative(root, filename).replaceAll('\\', '/'), hash(filename)]));
const report = { pass: true, elements: model.elements.length, faces, rotated, texture: [atlas.w, atlas.h], icon: [icon.w, icon.h], forgeWrapper: 'PASS custom contract validator', legalRotations: true, textureReferences: true, perFaceUV: true, quantizationErrorPixelsMax: maximumAspectQuantization, embeddedTextureMatch: true, bbRuntimeGeometryMatch: true, bbOutlinerUniqueAndComplete: true, sharedLint: lint.status === 0, sharedCoverage: coverage.status === 0, hashes };
fs.writeFileSync(path.join(work, 'validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
