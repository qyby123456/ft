const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const space = require('./cube_space.cjs');
const { itemPoint } = require('./puffer_bat_hand.cjs');
const { Canvas, decodePNG } = require('./png.cjs');

const assets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const cases = [
  { id: 'dog_beating_stick', grip: '握柄皮底', features: ['琥珀眼_-1', '琥珀眼_1', '黑鼻'], roll: -22 },
  { id: 'puffer_bat', grip: '深紫握柄', features: ['黑眼珠_-1', '黑眼珠_1', '张口'], roll: -25 },
  { id: 'compost_crown', grip: 'grip_core', features: ['seal_slit_left', 'seal_slit_right', 'seal_mouth'], roll: -15 },
  { id: 'defect_claw', grip: 'grip', features: ['capacitor_glass_vertical', 'capacitor_glass_horizontal'], roll: -12 }
];
const indices = {
  north: [6, 4, 0, 2], south: [3, 1, 5, 7], west: [2, 0, 1, 3],
  east: [7, 5, 4, 6], up: [3, 7, 6, 2], down: [0, 4, 5, 1]
};
const geometry = { geckolib: false, bones: [{ name: 'root' }], byName: new Map([['root', { name: 'root' }]]) };
const center = element => element.from.map((value, axis) => (value + element.to[axis]) / 2);
const slot = left => left ? 'firstperson_lefthand' : 'firstperson_righthand';

function cameraPoint(point, display, left) {
  return itemPoint(point, display, left).map((value, axis) => value / 16 + [left ? -0.56 : 0.56, -0.52, -0.72][axis]);
}

function facingDisplay(model, entry) {
  const original = model.display.firstperson_righthand;
  const grip = center(model.elements.find(element => element.name === entry.grip));
  const result = { rotation: [0, 145, entry.roll], translation: [0, 0, 0], scale: [...original.scale] };
  const target = itemPoint(grip, original, false);
  const rotated = itemPoint(grip, result, false);
  result.translation = space.sub(target, rotated).map(value => Number(value.toFixed(6)));
  return result;
}

function corners(element) {
  const cube = { origin: element.from, size: space.sub(element.to, element.from) };
  if (element.rotation) {
    cube.pivot = element.rotation.origin;
    cube.rotation = ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0);
  }
  let points = space.cubeCorners(geometry, { name: element.name, bone: 'root', cube });
  if (element.rotation?.rescale) {
    const axisIndex = ['x', 'y', 'z'].indexOf(element.rotation.axis);
    const factor = 1 / Math.cos(element.rotation.angle * Math.PI / 180);
    points = points.map(point => point.map((value, axis) => axis === axisIndex ? value
      : element.rotation.origin[axis] + (value - element.rotation.origin[axis]) * factor));
  }
  return points;
}

function render(model, left, fieldOfView = 70) {
  const width = 640;
  const height = 400;
  const canvas = new Canvas(width, height);
  canvas.rect(0, 0, width, height, 43, 58, 72);
  canvas.rect(0, height / 2, width, height / 2, 66, 73, 65);
  const depth = new Float64Array(width * height).fill(Infinity);
  const owners = new Array(width * height);
  const textures = new Map();
  const focal = height / (2 * Math.tan(fieldOfView * Math.PI / 360));
  const display = model.display[slot(left)];
  function triangle(vertices, coordinates, texture, owner, shade) {
    if (vertices.some(vertex => vertex[2] <= 0.05)) return;
    const [first, second, third] = vertices;
    const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(denominator) < 1e-8) return;
    const minX = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[0]))));
    const maxX = Math.min(width - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[0]))));
    const minY = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[1]))));
    const maxY = Math.min(height - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[1]))));
    for (let row = minY; row <= maxY; row++) for (let column = minX; column <= maxX; column++) {
      const weightA = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / denominator;
      const weightB = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / denominator;
      const weights = [weightA, weightB, 1 - weightA - weightB];
      if (Math.min(...weights) < -1e-8) continue;
      const inverseDepth = weights.reduce((total, weight, index) => total + weight / vertices[index][2], 0);
      const distance = 1 / inverseDepth;
      const offset = row * width + column;
      if (distance >= depth[offset]) continue;
      const uv = [0, 1].map(axis => weights.reduce((total, weight, index) => total + weight * coordinates[index][axis] / vertices[index][2], 0) / inverseDepth);
      const texX = Math.max(0, Math.min(texture.w - 1, Math.floor(uv[0] * texture.w / 16)));
      const texY = Math.max(0, Math.min(texture.h - 1, Math.floor(uv[1] * texture.h / 16)));
      const texOffset = (texY * texture.w + texX) * 4;
      if (texture.data[texOffset + 3] < 128) continue;
      depth[offset] = distance;
      owners[offset] = owner;
      canvas.set(column, row, ...[0, 1, 2].map(channel => texture.data[texOffset + channel] * shade));
    }
  }
  for (const element of model.elements) {
    const points = corners(element).map(point => {
      const camera = cameraPoint(point, display, left);
      return [width / 2 + camera[0] * focal / -camera[2], height / 2 - camera[1] * focal / -camera[2], -camera[2]];
    });
    for (const [side, face] of Object.entries(element.faces)) {
      let identifier = face.texture;
      while (identifier.startsWith('#')) identifier = model.textures[identifier.slice(1)];
      if (!textures.has(identifier)) textures.set(identifier, decodePNG(path.join(assets, 'textures', identifier.replace('ftboss:', '') + '.png')));
      const [leftU, topV, rightU, bottomV] = face.uv;
      const uv = [[leftU, topV], [leftU, bottomV], [rightU, bottomV], [rightU, topV]];
      for (let step = 0; step < (face.rotation || 0) / 90; step++) uv.unshift(uv.pop());
      const vertices = indices[side].map(index => points[index]);
      const shade = { north: 1, south: 0.88, up: 1.05, down: 0.65, east: 0.87, west: 0.8 }[side];
      for (const order of [[0, 1, 2], [0, 2, 3]]) triangle(order.map(index => vertices[index]), order.map(index => uv[index]), textures.get(identifier), element.name, shade);
    }
  }
  const visible = {};
  for (const owner of owners) if (owner) visible[owner] = (visible[owner] || 0) + 1;
  canvas.rect(width / 2 - 4, height / 2, 9, 1, 180, 190, 200);
  canvas.rect(width / 2, height / 2 - 4, 1, 9, 180, 190, 200);
  return { canvas, visible };
}

function audit(model, entry, baseline) {
  assert.deepEqual(model.display.firstperson_lefthand, model.display.firstperson_righthand, entry.id + ': Minecraft already mirrors left-hand yaw/roll/translation');
  if (baseline) {
    const withoutHands = value => {
      const result = structuredClone(value);
      delete result.display.firstperson_righthand;
      delete result.display.firstperson_lefthand;
      return result;
    };
    assert.deepEqual(withoutHands(model), withoutHands(baseline), entry.id + ': unrelated model changes');
    const grip = center(model.elements.find(element => element.name === entry.grip));
    assert(Math.hypot(...space.sub(itemPoint(grip, model.display.firstperson_righthand, false), itemPoint(grip, baseline.display.firstperson_righthand, false))) < 0.00001, entry.id + ': moved grip');
  }
  return [false, true].flatMap(left => [60, 70, 90].map(fieldOfView => {
    const { visible } = render(model, left, fieldOfView);
    const features = Object.fromEntries(entry.features.map(name => [name, visible[name] || 0]));
    assert(Object.values(features).every(pixels => pixels >= 2), `${entry.id} ${slot(left)} FOV ${fieldOfView}: hidden detail ${JSON.stringify(features)}`);
    return { slot: slot(left), fieldOfView, visiblePixels: features };
  }));
}

if (require.main === module) {
  const baselineRoot = process.argv[2];
  const output = path.resolve(process.argv[3] || path.join(__dirname, '../../art/weapon_facing'));
  fs.mkdirSync(output, { recursive: true });
  const report = {};
  for (const entry of cases) {
    const model = JSON.parse(fs.readFileSync(path.join(assets, 'models/item', entry.id + '.json'), 'utf8'));
    const baseline = baselineRoot ? JSON.parse(fs.readFileSync(path.join(baselineRoot, entry.id + '.json'), 'utf8')) : null;
    report[entry.id] = audit(model, entry, baseline);
    for (const left of [false, true]) {
      render(model, left).canvas.save(path.join(output, `${entry.id}-${left ? 'left' : 'right'}-after.png`));
      if (baseline) render(baseline, left).canvas.save(path.join(output, `${entry.id}-${left ? 'left' : 'right'}-before.png`));
    }
  }
  fs.writeFileSync(path.join(output, 'audit.json'), JSON.stringify(report, null, 2) + '\n');
  console.log('PASS: four weapons, both hands, three FOVs, visible face details and unchanged right-hand grip.');
}

module.exports = { cases, center, cameraPoint, facingDisplay, render, audit };
