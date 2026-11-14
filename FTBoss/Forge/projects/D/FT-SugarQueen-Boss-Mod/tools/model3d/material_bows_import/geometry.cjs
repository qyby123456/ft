const readline = require('node:readline');
const { rotateXYZ } = require('../cube_space.cjs');
const { elementMesh } = require('../material_bow_mesh.cjs');

const input = readline.createInterface({ input: process.stdin });
input.on('line', line => {
  const request = JSON.parse(line);
  let result;
  if (request.type === 'rotate') {
    result = rotateXYZ(request.point, [request.angle, 0, 0], request.origin);
  } else if (request.type === 'corners') {
    const faces = Object.fromEntries(['north', 'south', 'west', 'east', 'up', 'down']
      .map(face => [face, { uv: [0, 0, 16, 16] }]));
    result = Object.fromEntries(elementMesh({ from: request.from, to: request.to, faces })
      .map(face => [face.face, face.vertices]));
  } else {
    throw new Error('Unknown geometry operation');
  }
  process.stdout.write(JSON.stringify(result) + '\n');
});
