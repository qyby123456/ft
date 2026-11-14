'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const root = path.resolve(process.argv[2] || path.join(__dirname, '../..'));
const { rotateXYZ } = require(path.join(root, 'tools/model3d/cube_space.cjs'));
const { Canvas, decodePNG } = require(path.join(root, 'tools/model3d/png.cjs'));
const models = path.join(root, 'src/main/resources/assets/ftboss/models/item');
const texture = decodePNG(path.join(root, 'src/main/resources/assets/ftboss/textures/item/barrel_crossbow.png'));
const names = ['body', 'left', 'right', 'latch', 'string_left', 'string_right'];
const parts = Object.fromEntries(names.map(name => [name,
  JSON.parse(fs.readFileSync(path.join(models, 'barrel_crossbow_parts/' + name + '.json'), 'utf8')).elements]));
const java = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/client/model/BarrelCrossbowModel.java'), 'utf8');
const constant = name => Number(java.match(new RegExp('float ' + name + ' = ([\\d.-]+)F;'))[1]);
const pivotX = constant('PIVOT_X');
const pivotZ = constant('PIVOT_Z');
const limbAngle = constant('LIMB_ANGLE');
const travel = constant('LATCH_TRAVEL');
const mix = (start, end, amount) => start + (end - start) * amount;
const smooth = progress => progress * progress * (3 - 2 * progress);
const near = (actual, expected) => assert(Math.abs(actual - expected) < 1e-5, `${actual} != ${expected}`);

function limb(point, side, progress) {
  return rotateXYZ(point, [0, side * limbAngle * progress, 0], [side > 0 ? pivotX : 16 - pivotX, 0, pivotZ]);
}

function transform(point, part, progress) {
  if (part === 'left' || part === 'right') return limb(point, part === 'left' ? 1 : -1, progress);
  if (part === 'latch') return [point[0], point[1], point[2] + travel * progress];
  if (!part.startsWith('string_')) return point;
  const left = part === 'string_left';
  const tip = limb([left ? -4 : 20, 8.5, -1], left ? 1 : -1, progress);
  const along = left ? (point[0] + 4) / 12 : (20 - point[0]) / 12;
  return [mix(tip[0], 8, along), point[1], point[2] + mix(tip[2] + 1, travel * progress, along)];
}

const original = JSON.parse(fs.readFileSync(path.join(root, 'ref/barrel_crossbow_draw_original.json'), 'utf8'));
assert.equal(parts.body.length, 27);
assert.equal(parts.left.length, 4);
assert.equal(parts.right.length, 4);
assert.deepEqual(parts.latch[0], original.elements[31]);
for (const elements of Object.values(parts)) {
  for (const element of elements) {
    for (const face of Object.values(element.faces)) assert(face.uv.every(value => value >= 0 && value <= 16));
  }
}
for (let step = 0; step <= 100; step++) {
  const progress = smooth(step / 100);
  const leftTip = transform([-4, 8.5, -1], 'string_left', progress);
  const rightTip = transform([20, 8.5, -1], 'string_right', progress);
  near(leftTip[0] + rightTip[0], 16);
  near(leftTip[2], rightTip[2]);
  const attachedTip = limb([-4, 8.5, -1], 1, progress);
  leftTip.forEach((value, axis) => near(value, attachedTip[axis]));
  const leftCenter = transform([8, 8.5, -1], 'string_left', progress);
  const rightCenter = transform([8, 8.5, -1], 'string_right', progress);
  leftCenter.forEach((value, axis) => near(value, rightCenter[axis]));
  near(leftCenter[2], -1 + travel * progress);
  assert(leftCenter[0] >= 5 && leftCenter[0] <= 11);
  for (const [part, elements] of Object.entries(parts)) {
    for (const element of elements) {
      for (const point of [element.from, element.to]) {
        assert(transform(point, part, progress).every(Number.isFinite));
        if (step === 0) transform(point, part, progress).forEach((value, axis) => near(value, point[axis]));
      }
    }
  }
}
console.log('PASS: 101 poses, mirrored limb tips, joined string center, latch travel, finite coordinates, unchanged rest pose, valid UVs');

const faces = {
  north: [[1, 1, 0], [1, 0, 0], [0, 0, 0], [0, 1, 0]],
  south: [[0, 1, 1], [0, 0, 1], [1, 0, 1], [1, 1, 1]],
  west: [[0, 1, 0], [0, 0, 0], [0, 0, 1], [0, 1, 1]],
  east: [[1, 1, 1], [1, 0, 1], [1, 0, 0], [1, 1, 0]],
  up: [[0, 1, 0], [0, 1, 1], [1, 1, 1], [1, 1, 0]],
  down: [[1, 0, 0], [1, 0, 1], [0, 0, 1], [0, 0, 0]]
};
const panel = 400;
const width = panel * 5;
const height = 820;
const canvas = new Canvas(width, height);
canvas.rect(0, 0, width, height, 33, 37, 42);
const depthBuffer = new Float64Array(width * height).fill(-Infinity);

function triangle(vertices, shade) {
  const [first, second, third] = vertices;
  const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(denominator) < 1e-8) return;
  const minX = Math.max(0, Math.floor(Math.min(...vertices.map(point => point[0]))));
  const maxX = Math.min(width - 1, Math.ceil(Math.max(...vertices.map(point => point[0]))));
  const minY = Math.max(0, Math.floor(Math.min(...vertices.map(point => point[1]))));
  const maxY = Math.min(height - 1, Math.ceil(Math.max(...vertices.map(point => point[1]))));
  for (let pixelY = minY; pixelY <= maxY; pixelY++) {
    for (let pixelX = minX; pixelX <= maxX; pixelX++) {
      const weightA = ((second[1] - third[1]) * (pixelX - third[0]) + (third[0] - second[0]) * (pixelY - third[1])) / denominator;
      const weightB = ((third[1] - first[1]) * (pixelX - third[0]) + (first[0] - third[0]) * (pixelY - third[1])) / denominator;
      const weightC = 1 - weightA - weightB;
      if (Math.min(weightA, weightB, weightC) < -1e-6) continue;
      const depth = first[2] * weightA + second[2] * weightB + third[2] * weightC;
      const pixel = pixelY * width + pixelX;
      if (depth < depthBuffer[pixel]) continue;
      const textureX = Math.max(0, Math.min(texture.w - 1, Math.floor((first[3] * weightA + second[3] * weightB + third[3] * weightC) * texture.w / 16)));
      const textureY = Math.max(0, Math.min(texture.h - 1, Math.floor((first[4] * weightA + second[4] * weightB + third[4] * weightC) * texture.h / 16)));
      const source = (textureY * texture.w + textureX) * 4;
      if (texture.data[source + 3] < 128) continue;
      depthBuffer[pixel] = depth;
      canvas.set(pixelX, pixelY, texture.data[source] * shade, texture.data[source + 1] * shade, texture.data[source + 2] * shade);
    }
  }
}

for (let frame = 0; frame < 5; frame++) {
  const progress = smooth(frame / 4);
  canvas.rect(frame * panel + 35, 14, panel - 70, 4, 65, 70, 76);
  canvas.rect(frame * panel + 35, 14, (panel - 70) * frame / 4, 4, 189, 166, 117);
  for (let view = 0; view < 2; view++) {
    for (const [part, elements] of Object.entries(parts)) {
      for (const element of elements) {
        for (const [face, material] of Object.entries(element.faces)) {
          const coordinates = [[material.uv[0], material.uv[1]], [material.uv[0], material.uv[3]],
            [material.uv[2], material.uv[3]], [material.uv[2], material.uv[1]]];
          const vertices = faces[face].map((corner, index) => {
            let point = corner.map((end, axis) => end ? element.to[axis] : element.from[axis]);
            if (element.rotation) {
              const rotation = [0, 0, 0];
              rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
              point = rotateXYZ(point, rotation, element.rotation.origin);
            }
            point = transform(point, part, progress).map((value, axis) => value - [8, 9, 5][axis]);
            point = rotateXYZ(point, view === 0 ? [90, 0, 0] : [55, -25, 0]);
            const coordinate = coordinates[(index + (material.rotation || 0) / 90) % 4];
            return [frame * panel + panel / 2 + point[0] * 9.7, 220 + view * 390 - point[1] * 9.7, point[2], ...coordinate];
          });
          const shade = face === 'up' ? 1 : face === 'north' || face === 'south' ? 0.85 : 0.72;
          triangle([vertices[0], vertices[1], vertices[2]], shade);
          triangle([vertices[0], vertices[2], vertices[3]], shade);
        }
      }
    }
  }
}
const output = process.argv[3] || path.join(root, 'tools/model3d/out/barrel_crossbow_draw.png');
fs.mkdirSync(path.dirname(output), { recursive: true });
canvas.save(output);
console.log('Preview: 0%, 25%, 50%, 75%, 100%; top and oblique views; ' + output);
