const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { writeSafe } = require('./lib_itemtex.cjs');

const sourcePath = path.resolve(__dirname, '../../art/puffer_bat/Zicai_Puffer_Bat.bbmodel');
const defaultAssets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const textureName = 'zicai_puffer_bat_v1';

function buildModel(source) {
  assert.equal(source.meta.model_format, 'java_block');
  assert.equal(source.model_identifier, 'puffer_bat');
  assert.equal(source.textures.length, 1);
  const { width, height } = source.resolution;
  assert(width > 0 && height > 0);
  const elements = source.elements.map(cube => {
    assert.equal(cube.type, 'cube');
    assert(!cube.inflate, 'Bake inflate before exporting');
    for (const axis of [0, 1, 2]) {
      assert(Number.isFinite(cube.from[axis]) && Number.isFinite(cube.to[axis]));
      assert(cube.from[axis] >= -16 && cube.to[axis] <= 32 && cube.from[axis] < cube.to[axis]);
    }
    const rotatedAxes = cube.rotation.map((angle, axis) => ({ angle, axis })).filter(value => value.angle !== 0);
    assert(rotatedAxes.length <= 1, 'Java cubes support rotation on one axis only');
    const faces = {};
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
      element.rotation = { rescale: Boolean(cube.rescale), axis: ['x', 'y', 'z'][axis], angle, origin: cube.origin };
    }
    return element;
  });
  for (const group of source.outliner) {
    assert(group.rotation.every(angle => angle === 0), 'Bake group rotations before exporting');
    assert(group.children.every(child => typeof child === 'string'), 'Expected flat source groups');
  }
  assert.deepEqual(source.outliner.flatMap(group => group.children).sort(), source.elements.map(cube => cube.uuid).sort());
  assert.equal(new Set(source.elements.map(cube => cube.uuid)).size, elements.length);
  assert(source.textures[0].source.startsWith('data:image/png;base64,'));
  const texture = Buffer.from(source.textures[0].source.split(',')[1], 'base64');
  assert.equal(texture.subarray(0, 8).toString('hex'), '89504e470d0a1a0a');
  assert.equal(texture.readUInt32BE(16), width);
  assert.equal(texture.readUInt32BE(20), height);
  const model = {
    ambientocclusion: source.ambientocclusion,
    gui_light: source.front_gui_light ? 'front' : 'side',
    textures: { atlas: 'ftboss:item/' + textureName, particle: 'ftboss:item/' + textureName },
    elements,
    display: source.display
  };
  return { model, texture };
}

function generate(assetsRoot = defaultAssets) {
  const source = JSON.parse(fs.readFileSync(sourcePath, 'utf8'));
  const result = buildModel(source);
  const modelPath = path.join(assetsRoot, 'models/item/puffer_bat.json');
  const texturePath = path.join(assetsRoot, 'textures/item', textureName + '.png');
  for (const filename of [modelPath, texturePath]) fs.mkdirSync(path.dirname(filename), { recursive: true });
  writeSafe(modelPath, JSON.stringify(result.model, null, 2) + '\n');
  writeSafe(texturePath, result.texture);
  console.log('Puffer bat: ' + result.model.elements.length + ' cubes / ' + source.outliner.length + ' groups -> ' + assetsRoot);
  return result.model;
}

if (require.main === module) generate(process.argv[2]);
module.exports = { buildModel, generate, sourcePath, defaultAssets, textureName };
