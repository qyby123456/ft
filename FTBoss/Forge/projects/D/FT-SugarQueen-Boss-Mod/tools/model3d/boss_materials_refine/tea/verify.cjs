const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const assert = require('assert/strict');
const { decodePNG } = require('../../png.cjs');
const { root, art, modelFile, atlasFile, transform, mesh, render, outline } = require('./render.cjs');
const { audit } = require('./coplanar.cjs');
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const dot = (first, second) => first.reduce((sum, value, index) => sum + value * second[index], 0);
const minus = (first, second) => first.map((value, index) => value - second[index]);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];

function box(element) {
  const center = transform(element.from.map((value, index) => (value + element.to[index]) / 2), element.rotation);
  const rotation = element.rotation ? { ...element.rotation, origin: [0, 0, 0], rescale: false } : null;
  const axes = [[1, 0, 0], [0, 1, 0], [0, 0, 1]].map(axis => transform(axis, rotation));
  const half = element.to.map((value, index) => {
    const rescale = element.rotation && element.rotation.rescale && ['x', 'y', 'z'][index] !== element.rotation.axis;
    return (value - element.from[index]) / 2 / (rescale ? Math.cos(element.rotation.angle * Math.PI / 180) : 1);
  });
  return { name: element.name, center, axes, half };
}

function touching(first, second, margin = 0.015) {
  const delta = minus(second.center, first.center);
  const candidates = [...first.axes, ...second.axes, ...first.axes.flatMap(axis => second.axes.map(other => cross(axis, other)))];
  return candidates.every(axis => {
    const length = Math.hypot(...axis);
    if (length < 0.000001) return true;
    const radius = shape => shape.axes.reduce((sum, basis, index) => sum + shape.half[index] * Math.abs(dot(basis, axis)), 0);
    return Math.abs(dot(delta, axis)) <= radius(first) + radius(second) + margin * length;
  });
}

function validate(options = {}) {
  const input = { modelFile, atlasFile, item: 'tea_material', bbFile: path.join(root, 'art/boss_materials/blockbench/tea_material.bbmodel'), output: path.join(root, '_work/boss-materials-refine-20261021/tea/checks.json'), ...options };
  const model = JSON.parse(fs.readFileSync(input.modelFile));
  const texture = decodePNG(input.atlasFile);
  const iconFile = path.join(root, 'src/main/resources/assets/ftboss/textures/item/' + input.item + '.png');
  const entryFile = path.join(root, 'src/main/resources/assets/ftboss/models/item/' + input.item + '.json');
  const entry = JSON.parse(fs.readFileSync(entryFile));
  const bb = JSON.parse(fs.readFileSync(input.bbFile));
  assert.equal(entry.loader, 'forge:separate_transforms');
  assert.equal(path.basename(entry.base.parent), input.item);
  const guiPath = path.join(root, 'src/main/resources/assets', entry.perspectives.gui.parent.replace(':', '/models/') + '.json');
  const gui = JSON.parse(fs.readFileSync(guiPath));
  assert.equal(gui.parent, 'minecraft:item/generated');
  assert.equal(gui.textures.layer0, 'ftboss:item/' + input.item);
  assert(!model.parent, 'Geometry must not inherit generated/handheld.');
  assert.equal(model.elements.length, bb.elements.length);
  assert.deepEqual(bb.display, model.display);
  assert(Buffer.from(bb.textures[0].source.split(',')[1], 'base64').equals(fs.readFileSync(input.atlasFile)));
  const legal = new Set([-45, -22.5, 0, 22.5, 45]);
  let faces = 0;
  let samples = 0;
  const densities = [];
  const rectangles = [];
  for (let index = 0; index < model.elements.length; index++) {
    const element = model.elements[index];
    const source = bb.elements[index];
    assert.deepEqual(element.from, source.from);
    assert.deepEqual(element.to, source.to);
    const dimensions = element.to.map((value, axis) => value - element.from[axis]);
    assert(dimensions.every(value => value > 0));
    assert.equal(Object.keys(element.faces).length, 6);
    if (element.rotation) {
      assert(legal.has(element.rotation.angle));
      assert(['x', 'y', 'z'].includes(element.rotation.axis));
      assert.deepEqual(source.origin, element.rotation.origin);
      assert.equal(source.rotation[['x', 'y', 'z'].indexOf(element.rotation.axis)], element.rotation.angle);
    }
    for (const [direction, face] of Object.entries(element.faces)) {
      faces++;
      assert(face.uv.every(value => value >= 0 && value <= 16 && Number.isFinite(value)));
      const textureName = model.textures[face.texture.slice(1)];
      assert(textureName);
      const referenced = path.join(root, 'src/main/resources/assets', textureName.replace(':', '/textures/') + '.png');
      assert.equal(path.resolve(referenced), path.resolve(input.atlasFile));
      const expected = ['north', 'south'].includes(direction) ? [dimensions[0], dimensions[1]] : ['east', 'west'].includes(direction) ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[2]];
      const width = Math.abs(face.uv[2] - face.uv[0]) / 16 * texture.w;
      const height = Math.abs(face.uv[3] - face.uv[1]) / 16 * texture.h;
      const ratioError = Math.abs(width / height / (expected[0] / expected[1]) - 1);
      assert(ratioError < 0.001, element.name + '/' + direction + ' distorted');
      densities.push(width / expected[0], height / expected[1]);
      assert(source.faces[direction].uv.every((value, axis) => Math.abs(value / texture.w * 16 - face.uv[axis]) < 0.00001));
      const minX = Math.min(face.uv[0], face.uv[2]) / 16 * texture.w;
      const minY = Math.min(face.uv[1], face.uv[3]) / 16 * texture.h;
      rectangles.push([minX, minY, minX + width, minY + height]);
      for (let row = 0; row < 7; row++) for (let column = 0; column < 7; column++) {
        const sampleX = Math.floor(minX + width * (column + 0.5) / 7);
        const sampleY = Math.floor(minY + height * (row + 0.5) / 7);
        assert.equal(texture.data[(sampleY * texture.w + sampleX) * 4 + 3], 255, element.name + '/' + direction + ' transparent');
        samples++;
      }
    }
  }
  for (let first = 0; first < rectangles.length; first++) for (let second = first + 1; second < rectangles.length; second++) {
    const rectangleA = rectangles[first];
    const rectangleB = rectangles[second];
    assert(!(Math.min(rectangleA[2], rectangleB[2]) - Math.max(rectangleA[0], rectangleB[0]) > 0.0001 && Math.min(rectangleA[3], rectangleB[3]) - Math.max(rectangleA[1], rectangleB[1]) > 0.0001), 'UV islands overlap');
  }
  const boxes = model.elements.map(box);
  const pending = new Set(boxes.map((shape, index) => index));
  const components = [];
  while (pending.size) {
    const first = pending.values().next().value;
    const connected = [first];
    pending.delete(first);
    for (let cursor = 0; cursor < connected.length; cursor++) for (const candidate of [...pending]) if (touching(boxes[connected[cursor]], boxes[candidate])) {
      connected.push(candidate);
      pending.delete(candidate);
    }
    components.push(connected.map(index => boxes[index].name));
  }
  assert.equal(components.length, 1, 'Detached components: ' + JSON.stringify(components));
  const vertices = mesh(model).flatMap(face => face.vertices);
  const coplanarOverlaps = audit(model);
  assert.equal(coplanarOverlaps.length, 0, 'Coplanar face overlap: ' + JSON.stringify(coplanarOverlaps));
  const bounds = [0, 1, 2].map(axis => [Math.min(...vertices.map(vertex => vertex[axis])), Math.max(...vertices.map(vertex => vertex[axis]))]);
  assert(bounds.every(([minimum, maximum]) => minimum >= 0 && maximum <= 16));
  for (const angle of [-45, -22.5, 22.5, 45]) for (const axis of ['x', 'y', 'z']) {
    const initial = [1.1, 2.3, 3.7];
    const origin = [4, 5, 6];
    const moved = transform(initial, { axis, angle, origin });
    const restored = transform(moved, { axis, angle: -angle, origin });
    assert(restored.every((value, index) => Math.abs(value - initial[index]) < 0.0000001));
  }
  const rescaled = transform([0, 1, 0], { axis: 'x', angle: 45, origin: [0, 0, 0], rescale: true });
  assert(rescaled.every((value, index) => Math.abs(value - [0, 1, 1][index]) < 0.0000001));
  const icon = decodePNG(iconFile);
  const iconOptions = input.iconOptions || { width: 32, height: 32, yaw: -18, pitch: 12, scale: 1.78 };
  assert(Buffer.from(outline(render(model, texture, iconOptions)).data).equals(Buffer.from(icon.data)), 'GUI icon differs from runtime model rendering');
  for (let column = 0; column < icon.w; column++) for (const row of [0, icon.h - 1]) assert.equal(icon.data[(row * icon.w + column) * 4 + 3], 0, 'Icon clipped');
  for (let row = 0; row < icon.h; row++) for (const column of [0, icon.w - 1]) assert.equal(icon.data[(row * icon.w + column) * 4 + 3], 0, 'Icon clipped');
  const report = {
    status: 'PASS', item: 'ftboss:' + input.item, elements: model.elements.length, rotated: model.elements.filter(element => element.rotation).length,
    faces, opaqueSamples: samples, faceUVRatioTolerance: 0.001, density: [Math.min(...densities), Math.max(...densities)], atlas: [texture.w, texture.h], bounds,
    connectedComponents: components.length, coplanarFaceOverlaps: coplanarOverlaps.length, blockbenchGeometryUVAndEmbeddedTexture: 'MATCH', separateTransforms: 'PASS', iconRuntimeConsistency: 'EXACT',
    renderer: 'Textured per-pixel depth-buffer rasterizer; Java single-axis origin/rotation/rescale; face UV/mirroring; alpha cutout.',
    files: [input.modelFile, input.atlasFile, iconFile, entryFile, guiPath, input.bbFile].map(file => ({ path: file, sha256: hash(file) })),
    limits: 'Offline asset acceptance only. No game launch, Gradle build, deployment or Curios behavior test.'
  };
  fs.writeFileSync(input.output, JSON.stringify(report, null, 2) + String.fromCharCode(10));
  console.log(JSON.stringify(report, null, 2));
  return report;
}

if (require.main === module) validate();
module.exports = { validate, touching, box, hash };
