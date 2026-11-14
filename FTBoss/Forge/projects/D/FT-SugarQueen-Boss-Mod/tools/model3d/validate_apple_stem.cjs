'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const [assetRoot, libraryRoot] = process.argv.slice(2).map(value => path.resolve(value));
const { decodePNG } = require(path.join(libraryRoot, 'png.cjs'));
const { rotateXYZ } = require(path.join(libraryRoot, 'cube_space.cjs'));
const { auditSurfaces, elementFaces } = require(path.join(libraryRoot, 'audit_item_surfaces.cjs'));
const model = JSON.parse(fs.readFileSync(path.join(assetRoot, 'models/item/apple_stem.json'), 'utf8'));
const editable = JSON.parse(fs.readFileSync(path.join(assetRoot, 'apple_stem_micui.bbmodel'), 'utf8'));
const textureBytes = fs.readFileSync(path.join(assetRoot, 'textures/item/apple_stem_micui.png'));
const texture = decodePNG(path.join(assetRoot, 'textures/item/apple_stem_micui.png'));
assert.equal(model.parent, undefined);
assert.equal(Object.keys(model.display).length, 8);
assert.equal(editable.elements.length, model.elements.length);
assert.equal(editable.resolution.width, texture.w);
assert.deepEqual(Buffer.from(editable.textures[0].source.split(',')[1], 'base64'), textureBytes);
const ids = editable.elements.map(element => element.uuid);
assert.equal(new Set(ids).size, ids.length);
assert.deepEqual(editable.outliner.flatMap(group => group.children).sort(), ids.slice().sort());
let faceCount = 0;
for (let index = 0; index < model.elements.length; index++) {
  const element = model.elements[index];
  const source = editable.elements[index];
  assert.deepEqual(element.from, source.from);
  assert.deepEqual(element.to, source.to);
  for (const [direction, face] of Object.entries(element.faces)) {
    faceCount++;
    assert.deepEqual(face.uv.map(value => value * texture.w / 16), source.faces[direction].uv);
    const bounds = [Math.min(face.uv[0], face.uv[2]), Math.min(face.uv[1], face.uv[3]),
      Math.max(face.uv[0], face.uv[2]), Math.max(face.uv[1], face.uv[3])].map(value => value * texture.w / 16);
    for (let pixelY = Math.floor(bounds[1]); pixelY < bounds[3]; pixelY++) {
      for (let pixelX = Math.floor(bounds[0]); pixelX < bounds[2]; pixelX++) {
        assert.equal(texture.data[(pixelY * texture.w + pixelX) * 4 + 3], 255, `${element.name}/${direction}: transparent texel`);
      }
    }
  }
}
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1],
  first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const boxes = model.elements.map(element => {
  const rotation = [0, 0, 0];
  if (element.rotation) rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
  const center = element.from.map((value, axis) => (value + element.to[axis]) / 2);
  return { name: element.name,
    center: rotateXYZ(center, rotation, element.rotation?.origin || [0, 0, 0]),
    half: element.to.map((value, axis) => (value - element.from[axis]) / 2),
    axes: [[1, 0, 0], [0, 1, 0], [0, 0, 1]].map(axis => rotateXYZ(axis, rotation)) };
});
function intersects(first, second) {
  const displacement = second.center.map((value, axis) => value - first.center[axis]);
  const axes = [...first.axes, ...second.axes, ...first.axes.flatMap(axis => second.axes.map(other => cross(axis, other)))];
  return axes.every(axis => {
    if (dot(axis, axis) < 1e-10) return true;
    const extent = box => box.axes.reduce((sum, local, index) => sum + box.half[index] * Math.abs(dot(axis, local)), 0);
    return Math.abs(dot(axis, displacement)) < extent(first) + extent(second) - 0.000001;
  });
}
const visited = new Set();
const components = [];
for (let start = 0; start < boxes.length; start++) {
  if (visited.has(start)) continue;
  const pending = [start];
  const component = [];
  visited.add(start);
  while (pending.length) {
    const current = pending.pop();
    component.push(boxes[current].name);
    boxes.forEach((box, index) => {
      if (!visited.has(index) && intersects(boxes[current], box)) {
        visited.add(index);
        pending.push(index);
      }
    });
  }
  components.push(component);
}
assert.equal(components.length, 1, `Detached geometry: ${JSON.stringify(components)}`);
const surfaces = auditSurfaces(model);
assert.deepEqual(surfaces.coplanar, [], 'Coplanar overlapping faces');
assert.deepEqual(surfaces.nearCoplanar, [], 'Near-coplanar overlapping faces');
let omittedFaces = 0;
for (const element of model.elements) {
  const source = editable.elements.find(candidate => candidate.name === element.name);
  for (const direction of ['north', 'south', 'east', 'west', 'up', 'down']) {
    if (element.faces[direction]) continue;
    omittedFaces++;
    assert.equal(source.faces[direction].texture, null, 'Blockbench must not restore omitted faces');
    const face = elementFaces({ ...element, faces: { [direction]: {} } })[0];
    for (let row = 0; row <= 16; row++) {
      for (let column = 0; column <= 16; column++) {
        const point = face.vertices[0].map((value, axis) => value +
          (face.vertices[1][axis] - value) * column / 16 + (face.vertices[3][axis] - value) * row / 16);
        const covered = boxes.some(box => box.name !== element.name && box.axes.every((axis, index) =>
          Math.abs(dot(point.map((value, coordinate) => value - box.center[coordinate]), axis)) <= box.half[index] + 0.00001));
        assert.ok(covered, `Omitted face is exposed: ${element.name}/${direction} at ${point}`);
      }
    }
  }
}
const bodySlices = model.elements.filter(element => element.name.startsWith('leaf_blade_'));
assert.ok(bodySlices.length > 0);
for (let index = 1; index < bodySlices.length; index++) {
  const lower = bodySlices[index - 1];
  const upper = bodySlices[index];
  assert.equal(lower.to[1], upper.from[1], `Gap or coplanar overlap at ${upper.name}`);
  assert.equal(lower.from[2], upper.from[2], `Stepped front surface at ${upper.name}`);
  assert.equal(lower.to[2], upper.to[2], `Stepped rear surface at ${upper.name}`);
  for (const direction of ['north', 'south']) {
    assert.ok(Math.abs(lower.faces[direction].uv[1] - upper.faces[direction].uv[3]) < 0.00001,
      `Discontinuous blade UV at ${upper.name}/${direction}`);
  }
}
const cuttingEdges = model.elements.filter(element => element.name.startsWith('cutting_edge_'));
assert.deepEqual(cuttingEdges.map(element => element.rotation?.angle), [22.5, -22.5, -45]);
for (let index = 1; index < cuttingEdges.length; index++) {
  assert.ok(intersects(boxes.find(box => box.name === cuttingEdges[index - 1].name),
    boxes.find(box => box.name === cuttingEdges[index].name)), 'Disconnected diagonal edge');
}
if (process.argv[4]) {
  const baseline = JSON.parse(fs.readFileSync(path.join(path.resolve(process.argv[4]), 'apple_stem_micui.bbmodel'), 'utf8'));
  assert.deepEqual(editable.display, baseline.display, 'Display transforms changed');
  const repairedInternalParts = new Set(['grip_wood', 'heart_socket']);
  const unchangedIds = baseline.outliner.filter(group => group.name !== 'blade').flatMap(group => group.children)
    .filter(identity => !repairedInternalParts.has(baseline.elements.find(element => element.uuid === identity).name));
  for (const identity of unchangedIds) {
    const previous = baseline.elements.find(element => element.uuid === identity);
    const current = editable.elements.find(element => element.uuid === identity);
    for (const key of ['name', 'from', 'to', 'origin', 'rotation']) {
      assert.deepEqual(current?.[key], previous[key], `Unrelated geometry changed: ${previous.name}/${key}`);
    }
  }
  console.log(`PASS: ${unchangedIds.length} handle/guard/apple elements and all display transforms unchanged.`);
}
console.log(`PASS: ${cuttingEdges.length} connected diagonal edges; ${bodySlices.length} coplanar body slices share continuous UVs.`);
console.log(`PASS: no coplanar/near-coplanar overlaps; ${omittedFaces} omitted faces pass 17x17 occlusion samples and stay disabled in Blockbench.`);
console.log(`PASS: ${model.elements.length} connected cubes, ${faceCount} opaque UV faces, 8 display slots, exact Blockbench geometry/UV/texture round-trip.`);
