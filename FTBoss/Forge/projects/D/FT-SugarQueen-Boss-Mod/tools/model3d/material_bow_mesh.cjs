const fs = require('node:fs');
const { rotateXYZ } = require('./cube_space.cjs');

function elementMesh(element) {
  const [left, bottom, front] = element.from;
  const [right, top, back] = element.to;
  const corners = {
    north: [[right, top, front], [right, bottom, front], [left, bottom, front], [left, top, front]],
    south: [[left, top, back], [left, bottom, back], [right, bottom, back], [right, top, back]],
    west: [[left, top, front], [left, bottom, front], [left, bottom, back], [left, top, back]],
    east: [[right, top, back], [right, bottom, back], [right, bottom, front], [right, top, front]],
    up: [[left, top, front], [left, top, back], [right, top, back], [right, top, front]],
    down: [[left, bottom, back], [left, bottom, front], [right, bottom, front], [right, bottom, back]]
  };
  const rotation = [0, 0, 0];
  if (element.rotation) {
    if (element.rotation.rescale) throw new Error('Rescaled item rotations are not supported');
    rotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
  }
  return Object.entries(element.faces).map(([face, definition]) => {
    const [firstU, firstV, lastU, lastV] = definition.uv;
    const uv = [[firstU, firstV], [firstU, lastV], [lastU, lastV], [lastU, firstV]];
    const turns = (definition.rotation || 0) / 90;
    return { name: element.name, face, vertices: corners[face].map(point => rotateXYZ(point, rotation, element.rotation?.origin)),
      uv: uv.map((_, index) => uv[(index + turns) % 4]) };
  });
}

function overlapDepth(first, second) {
  const firstVertices = elementMesh(first).flatMap(face => face.vertices);
  const secondVertices = elementMesh(second).flatMap(face => face.vertices);
  const angles = [0, first.rotation?.angle || 0, second.rotation?.angle || 0];
  const axes = [[0, 0, 1]];
  for (const angle of angles) {
    const radians = angle * Math.PI / 180;
    axes.push([Math.cos(radians), Math.sin(radians), 0], [-Math.sin(radians), Math.cos(radians), 0]);
  }
  return Math.min(...axes.map(axis => {
    const firstProjection = firstVertices.map(point => point.reduce((sum, value, index) => sum + value * axis[index], 0));
    const secondProjection = secondVertices.map(point => point.reduce((sum, value, index) => sum + value * axis[index], 0));
    return Math.min(Math.max(...firstProjection) - Math.min(...secondProjection), Math.max(...secondProjection) - Math.min(...firstProjection));
  }));
}

function touching(first, second) {
  return overlapDepth(first, second) >= -0.0003;
}

if (require.main === module) {
  const model = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
  process.stdout.write(JSON.stringify(model.elements.flatMap(elementMesh)));
}

module.exports = { elementMesh, touching, overlapDepth };
