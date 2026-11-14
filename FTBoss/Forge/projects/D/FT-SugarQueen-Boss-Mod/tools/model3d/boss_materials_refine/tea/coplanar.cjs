const fs = require('fs');
const { mesh } = require('./render.cjs');
const dot = (first, second) => first.reduce((sum, value, index) => sum + value * second[index], 0);
const subtract = (first, second) => first.map((value, index) => value - second[index]);
const signedArea = polygon => polygon.reduce((sum, point, index) => {
  const next = polygon[(index + 1) % polygon.length];
  return sum + point[0] * next[1] - next[0] * point[1];
}, 0) / 2;

function overlapArea(subject, clipping) {
  const orientation = Math.sign(signedArea(clipping));
  const edge = (start, end, point) => orientation * ((end[0] - start[0]) * (point[1] - start[1]) - (end[1] - start[1]) * (point[0] - start[0]));
  let polygon = subject;
  for (let index = 0; index < clipping.length && polygon.length; index++) {
    const start = clipping[index];
    const end = clipping[(index + 1) % clipping.length];
    const input = polygon;
    polygon = [];
    for (let cursor = 0; cursor < input.length; cursor++) {
      const previous = input[(cursor + input.length - 1) % input.length];
      const current = input[cursor];
      const distancePrevious = edge(start, end, previous);
      const distanceCurrent = edge(start, end, current);
      if ((distancePrevious >= 0) !== (distanceCurrent >= 0)) {
        const ratio = distancePrevious / (distancePrevious - distanceCurrent);
        polygon.push(previous.map((value, axis) => value + (current[axis] - value) * ratio));
      }
      if (distanceCurrent >= 0) polygon.push(current);
    }
  }
  return polygon.length ? Math.abs(signedArea(polygon)) : 0;
}

function audit(model) {
  const geometry = mesh(model);
  const overlaps = [];
  for (let first = 0; first < geometry.length; first++) for (let second = first + 1; second < geometry.length; second++) {
    const faceA = geometry[first];
    const faceB = geometry[second];
    if (faceA.name === faceB.name || dot(faceA.normal, faceB.normal) < 0.999999) continue;
    if (Math.abs(dot(subtract(faceA.vertices[0], faceB.vertices[0]), faceA.normal)) > 0.000001) continue;
    const droppedAxis = faceA.normal.map(Math.abs).indexOf(Math.max(...faceA.normal.map(Math.abs)));
    const project = face => face.vertices.map(vertex => vertex.filter((value, axis) => axis !== droppedAxis));
    const area = overlapArea(project(faceA), project(faceB));
    if (area > 0.00001) overlaps.push({ first: faceA.name, second: faceB.name, normal: faceA.normal, projectedArea: area });
  }
  return overlaps;
}

if (require.main === module) console.log(JSON.stringify(audit(JSON.parse(fs.readFileSync(process.argv[2]))), null, 2));
module.exports = { audit };
