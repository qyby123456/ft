const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { writeSafe } = require('./lib_itemtex.cjs');

const itemName = 'maque_blessing_feather';
const artRoot = path.resolve(__dirname, '../../art/maque_feather');
const sourcePath = path.join(artRoot, 'Maque_Blessing_Feather.bbmodel');
const defaultAssets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const particleName = 'maque_feather_particle';

function buildModel(source) {
  assert.equal(source.meta.model_format, 'java_block');
  assert.equal(source.model_identifier, itemName);
  assert.equal(source.textures.length, 1);
  const { width, height } = source.resolution;
  assert.deepEqual([width, height], [64, 64]);
  const elements = source.elements.map(cube => {
    assert.equal(cube.type, 'cube');
    assert(!cube.inflate, 'Bake inflate before exporting');
    for (const axis of [0, 1, 2]) {
      assert(Number.isFinite(cube.from[axis]) && Number.isFinite(cube.to[axis]));
      assert(cube.from[axis] >= -16 && cube.to[axis] <= 32 && cube.from[axis] < cube.to[axis]);
    }
    assert(cube.rotation.length === 3 && cube.rotation.every(Number.isFinite));
    const rotatedAxes = cube.rotation.map((angle, axis) => ({ angle, axis })).filter(value => value.angle !== 0);
    assert(rotatedAxes.length <= 1, 'Java cubes support one rotation axis');
    const faces = {};
    assert.equal(Object.keys(cube.faces).length, 6);
    for (const [side, face] of Object.entries(cube.faces)) {
      assert(['north', 'south', 'west', 'east', 'up', 'down'].includes(side));
      assert.equal(face.texture, 0);
      const uv = face.uv.map((value, index) => value * 16 / (index % 2 === 0 ? width : height));
      assert(uv.length === 4 && uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
      faces[side] = { uv, texture: '#atlas' };
      if (face.rotation) faces[side].rotation = face.rotation;
    }
    const element = { name: cube.name, from: cube.from, to: cube.to, faces };
    if (rotatedAxes.length) {
      const { angle, axis } = rotatedAxes[0];
      assert([-45, -22.5, 22.5, 45].includes(angle));
      assert(cube.origin.length === 3 && cube.origin.every(Number.isFinite));
      element.rotation = { axis: ['x', 'y', 'z'][axis], angle, origin: cube.origin, rescale: Boolean(cube.rescale) };
    }
    return element;
  });
  for (const group of source.outliner) {
    assert(group.rotation.every(angle => angle === 0), 'Bake group rotations before exporting');
    assert(group.children.every(child => typeof child === 'string'), 'Expected flat groups');
  }
  assert.deepEqual(source.outliner.flatMap(group => group.children).sort(), source.elements.map(cube => cube.uuid).sort());
  assert.equal(new Set(source.elements.map(cube => cube.uuid)).size, elements.length);
  assert(source.textures[0].source.startsWith('data:image/png;base64,'));
  const texture = Buffer.from(source.textures[0].source.split(',')[1], 'base64');
  assert.equal(texture.subarray(0, 8).toString('hex'), '89504e470d0a1a0a');
  assert.equal(texture.readUInt32BE(16), width);
  assert.equal(texture.readUInt32BE(20), height);
  const model = {
    credit: 'Maque / 雀翎·回春 — feather-only revision',
    texture_size: [width, height],
    ambientocclusion: source.ambientocclusion,
    gui_light: source.front_gui_light ? 'front' : 'side',
    textures: { atlas: 'ftboss:item/' + itemName, particle: 'ftboss:item/' + particleName },
    elements,
    display: source.display
  };
  return { model, texture };
}

function generate(assetsRoot = defaultAssets) {
  const source = JSON.parse(fs.readFileSync(sourcePath, 'utf8'));
  const result = buildModel(source);
  const files = [
    ['models/item/' + itemName + '.json', JSON.stringify(result.model, null, 2) + '\n'],
    ['textures/item/' + itemName + '.png', result.texture],
    ['textures/item/' + particleName + '.png', fs.readFileSync(path.join(artRoot, particleName + '.png'))]
  ];
  for (const [relative, bytes] of files) {
    const target = path.join(assetsRoot, relative);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    writeSafe(target, bytes);
  }
  console.log('Maque feather: ' + result.model.elements.length + ' cubes -> ' + assetsRoot);
  return result.model;
}

if (require.main === module) generate(process.argv[2]);
module.exports = { buildModel, generate, sourcePath, defaultAssets, itemName, particleName, artRoot };
