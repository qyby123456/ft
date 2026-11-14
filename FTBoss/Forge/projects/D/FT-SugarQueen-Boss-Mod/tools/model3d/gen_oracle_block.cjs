const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const root = path.resolve(__dirname, '../..');
const resources = path.join(root, 'src/main/resources');
const blockModelPath = path.join(resources, 'assets/ftboss/models/block/oracle.json');
const texturePath = path.join(resources, 'assets/ftboss/textures/block/oracle.png');

const uv = { base: [0, 0, 8, 16], gold: [8, 0, 12, 16], crystal: [12, 0, 16, 16] };
function faces(kind) {
  const value = {};
  for (const face of ['north', 'south', 'east', 'west', 'up', 'down']) {
    value[face] = { uv: uv[kind], texture: '#oracle' };
  }
  return value;
}
function cube(from, to, kind) { return { from, to, faces: faces(kind) }; }
const model = {
  parent: 'minecraft:block/block',
  ambientocclusion: true,
  textures: { particle: 'ftboss:block/oracle', oracle: 'ftboss:block/oracle' },
  elements: [
    cube([1, 0, 1], [15, 3, 15], 'base'),
    cube([2, 3, 2], [14, 4, 14], 'gold'),
    cube([3, 4, 3], [13, 6, 13], 'base'),
    cube([6, 6, 6], [10, 13, 10], 'crystal'),
    cube([7, 13, 7], [9, 15, 9], 'crystal'),
    cube([3, 6, 3], [4, 12, 4], 'gold'),
    cube([12, 6, 3], [13, 12, 4], 'gold'),
    cube([3, 6, 12], [4, 12, 13], 'gold'),
    cube([12, 6, 12], [13, 12, 13], 'gold'),
    cube([2.5, 11, 2.5], [4.5, 13, 4.5], 'base'),
    cube([11.5, 11, 2.5], [13.5, 13, 4.5], 'base'),
    cube([2.5, 11, 11.5], [4.5, 13, 13.5], 'base'),
    cube([11.5, 11, 11.5], [13.5, 13, 13.5], 'base'),
    cube([5, 7, 2], [11, 12, 3], 'gold'),
    cube([6, 8, 1.5], [10, 11, 2], 'crystal'),
    cube([7, 8.75, 1.25], [9, 10.25, 1.5], 'base')
  ],
  display: {
    gui: { rotation: [30, 225, 0], translation: [0, 0, 0], scale: [0.68, 0.68, 0.68] },
    ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.25, 0.25, 0.25] },
    fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] },
    thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.38, 0.38, 0.38] },
    firstperson_righthand: { rotation: [0, 45, 0], translation: [0, 0, 0], scale: [0.4, 0.4, 0.4] }
  }
};
fs.mkdirSync(path.dirname(blockModelPath), { recursive: true });
fs.writeFileSync(blockModelPath, JSON.stringify(model, null, 2) + '\n');

const image = new Canvas(16, 16);
for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
  const noise = ((x * 17 + y * 31 + x * y * 7) % 19) - 9;
  if (x < 8) {
    const seam = ((x + y * 2) % 7 === 0) ? 17 : 0;
    image.set(x, y, 30 + noise + seam, 19 + Math.floor(noise / 2), 42 + noise + seam, 1);
  } else if (x < 12) {
    const shine = ((x + y) % 5 === 0) ? 34 : 0;
    image.set(x, y, 176 + noise + shine, 116 + noise + shine, 27 + Math.floor(noise / 2), 1);
  } else {
    const shine = ((x * 3 + y) % 5 === 0) ? 42 : 0;
    image.set(x, y, 105 + noise + shine, 58 + noise, 168 + noise + shine, 1);
  }
}
fs.mkdirSync(path.dirname(texturePath), { recursive: true });
image.save(texturePath);
console.log(`Generated ${path.relative(root, blockModelPath)} and ${path.relative(root, texturePath)}`);