'use strict';

const fs = require('fs');
const path = require('path');
const { rotateXYZ } = require('./cube_space.cjs');

const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const subtract = (first, second) => first.map((value, axis) => value - second[axis]);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1],
  first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const side = (start, end, point) => (end[0] - start[0]) * (point[1] - start[1]) -
  (end[1] - start[1]) * (point[0] - start[0]);

function signedArea(polygon) {
  return polygon.reduce((sum, point, index) => {
    const next = polygon[(index + 1) % polygon.length];
    return sum + point[0] * next[1] - point[1] * next[0];
  }, 0) / 2;
}

function clipPolygon(subject, clip) {
  let polygon = subject;
  const boundary = signedArea(clip) >= 0 ? clip : clip.slice().reverse();
  for (let index = 0; index < boundary.length && polygon.length; index++) {
    const start = boundary[index];
    const end = boundary[(index + 1) % boundary.length];
    const input = polygon;
    polygon = [];
    for (let vertex = 0; vertex < input.length; vertex++) {
      const current = input[vertex];
      const previous = input[(vertex + input.length - 1) % input.length];
      const currentSide = side(start, end, current);
      const previousSide = side(start, end, previous);
      if ((currentSide >= 0) !== (previousSide >= 0)) {
        const fraction = previousSide / (previousSide - currentSide);
        polygon.push(previous.map((value, axis) => value + fraction * (current[axis] - value)));
      }
      if (currentSide >= 0) polygon.push(current);
    }
  }
  return polygon;
}

function elementFaces(element) {
  const [left, bottom, front] = element.from;
  const [right, top, back] = element.to;
  const coordinates = {
    north: [[right, top, front], [left, top, front], [left, bottom, front], [right, bottom, front]],
    south: [[left, top, back], [right, top, back], [right, bottom, back], [left, bottom, back]],
    west: [[left, top, front], [left, top, back], [left, bottom, back], [left, bottom, front]],
    east: [[right, top, back], [right, top, front], [right, bottom, front], [right, bottom, back]],
    up: [[left, top, front], [right, top, front], [right, top, back], [left, top, back]],
    down: [[left, bottom, back], [right, bottom, back], [right, bottom, front], [left, bottom, front]]
  };
  const rotation = [0, 0, 0];
  if (element.rotation) rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
  return Object.entries(element.faces).map(([direction]) => {
    const vertices = coordinates[direction].map(point => {
      let result = point;
      if (element.rotation?.rescale) {
        const pivot = element.rotation.origin;
        const axisIndex = 'xyz'.indexOf(element.rotation.axis);
        const scale = 1 / Math.cos(element.rotation.angle * Math.PI / 180);
        result = result.map((value, axis) => axis === axisIndex ? value : pivot[axis] + (value - pivot[axis]) * scale);
      }
      return rotateXYZ(result, rotation, element.rotation?.origin || [0, 0, 0]);
    });
    const normal = cross(subtract(vertices[1], vertices[0]), subtract(vertices[2], vertices[0]));
    const magnitude = Math.hypot(...normal);
    if (magnitude < 1e-10) return null;
    return { name: element.name, direction, vertices, normal: normal.map(value => value / magnitude) };
  }).filter(Boolean);
}

function auditSurfaces(model, minimumSeparation = 0.015) {
  const faces = model.elements.flatMap(elementFaces);
  const coplanar = [];
  const nearCoplanar = [];
  for (let firstIndex = 0; firstIndex < faces.length; firstIndex++) {
    const first = faces[firstIndex];
    for (let secondIndex = firstIndex + 1; secondIndex < faces.length; secondIndex++) {
      const second = faces[secondIndex];
      if (first.name === second.name || Math.abs(dot(first.normal, second.normal)) < 1 - 1e-8) continue;
      const separation = Math.abs(dot(first.normal, subtract(second.vertices[0], first.vertices[0])));
      if (separation > minimumSeparation) continue;
      const dominant = first.normal.map(Math.abs).indexOf(Math.max(...first.normal.map(Math.abs)));
      const project = point => point.filter((value, axis) => axis !== dominant);
      const intersection = clipPolygon(first.vertices.map(project), second.vertices.map(project));
      if (intersection.length < 3) continue;
      const area = Math.abs(signedArea(intersection)) / Math.abs(first.normal[dominant]);
      if (area < 0.00001) continue;
      const pair = { first: `${first.name}/${first.direction}`, second: `${second.name}/${second.direction}`,
        area: Number(area.toFixed(6)), separation: Number(separation.toFixed(6)) };
      (separation < 0.00001 ? coplanar : nearCoplanar).push(pair);
    }
  }
  return { faces: faces.length, minimumSeparation, coplanar, nearCoplanar };
}

module.exports = { auditSurfaces, elementFaces };
if (require.main === module) {
  const modelPath = process.argv[2];
  if (!modelPath) throw new Error('Usage: node audit_item_surfaces.cjs <model.json> [report.json]');
  const result = auditSurfaces(JSON.parse(fs.readFileSync(modelPath, 'utf8')));
  console.log(`${path.basename(modelPath)}: ${result.faces} faces; ${result.coplanar.length} coplanar pairs; ${result.nearCoplanar.length} near-coplanar pairs.`);
  for (const pair of [...result.coplanar, ...result.nearCoplanar]) console.log(JSON.stringify(pair));
  if (process.argv[3]) fs.writeFileSync(process.argv[3], JSON.stringify(result, null, 2) + '\n');
  if (result.coplanar.length || result.nearCoplanar.length) process.exitCode = 1;
}
