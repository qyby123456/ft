const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { writeSafe } = require('./lib_itemtex.cjs');
const { encodePNG } = require('./png.cjs');
const { buildRig } = require('./gen_dandan_rig.cjs');

const artRoot = path.resolve(__dirname, '../../art/dandan_scythe');
const defaultAssets = path.resolve(__dirname, '../../src/main/resources/assets');
const sourcePath = path.join(artRoot, 'Dandan_End_Rod_Scythe.bbmodel');
const customModelData = 91601;
const textureName = 'dandan_end_rod_scythe_v2';
const particleName = 'dandan_end_rod_scythe_particle';

function buildModel(source) {
  assert.equal(source.meta.model_format, 'java_block');
  assert.equal(source.model_identifier, 'dandan_end_rod_scythe');
  assert.equal(source.textures.length, 1);
  const { width, height } = source.resolution;
  assert(width > 0 && height > 0);
  const elements = source.elements.map(cube => {
    assert.equal(cube.type, 'cube');
    assert(!cube.inflate);
    for (const axis of [0, 1, 2]) {
      assert(Number.isFinite(cube.from[axis]) && Number.isFinite(cube.to[axis]));
      assert(cube.from[axis] >= -16 && cube.to[axis] <= 32 && cube.from[axis] < cube.to[axis]);
    }
    const rotated = cube.rotation.map((angle, axis) => ({ angle, axis })).filter(value => value.angle !== 0);
    assert(rotated.length <= 1);
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
    if (rotated.length) {
      const { angle, axis } = rotated[0];
      assert([-45, -22.5, 22.5, 45].includes(angle));
      element.rotation = { rescale: Boolean(cube.rescale), axis: ['x', 'y', 'z'][axis], angle, origin: cube.origin };
    }
    return element;
  });
  for (const group of source.outliner) {
    assert(group.rotation.every(angle => angle === 0));
    assert(group.children.every(child => typeof child === 'string'));
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
    textures: { atlas: 'ftboss:item/' + textureName, particle: 'ftboss:item/' + particleName },
    elements,
    display: source.display
  };
  return { model, texture };
}

function buildRouting(existing = { parent: 'minecraft:block/end_rod' }) {
  assert.equal(existing.parent, 'minecraft:block/end_rod');
  const overrides = existing.overrides || [];
  assert(overrides.every(override =>
    override.predicate?.custom_model_data === customModelData || override.predicate?.custom_model_data === customModelData + 1),
  'Merge unrelated end rod overrides explicitly before exporting');
  return {
    ...existing,
    overrides: [
      { predicate: { custom_model_data: customModelData }, model: 'ftboss:item/dandan_end_rod_scythe' },
      { predicate: { custom_model_data: customModelData + 1 }, model: 'minecraft:block/end_rod' }
    ]
  };
}

function buildWeaponModel(source) {
  const model = buildModel(source).model;
  for (const element of model.elements) {
    if (/烛白内刃口_|前烛白方芯|后烛白方芯|上段烛白嵌条|前淡紫烛纹_/.test(element.name)) {
      element.shade = false;
      for (const face of Object.values(element.faces)) {
        face.forge_data = { block_light: 15, sky_light: 15, ambient_occlusion: false };
      }
    }
  }
  return model;
}

function generate(assetsRoot = defaultAssets) {
  const source = JSON.parse(fs.readFileSync(sourcePath, 'utf8'));
  const result = buildModel(source);
  const particle = fs.readFileSync(path.join(artRoot, particleName + '.png'));
  assert.equal(particle.readUInt32BE(16), 16);
  assert.equal(particle.readUInt32BE(20), 16);
  const routingPath = path.join(assetsRoot, 'minecraft/models/item/end_rod.json');
  const existing = fs.existsSync(routingPath) ? JSON.parse(fs.readFileSync(routingPath, 'utf8')) : undefined;
  const files = new Map([
    ['ftboss/models/item/dandan_end_rod_scythe.json', JSON.stringify(result.model, null, 2) + '\n'],
    ['ftboss/models/item/dandan_scythe.json', JSON.stringify(buildWeaponModel(source), null, 2) + '\n'],
    ['ftboss/textures/item/' + textureName + '.png', result.texture],
    ['ftboss/textures/item/' + particleName + '.png', particle],
    ['minecraft/models/item/end_rod.json', JSON.stringify(buildRouting(existing), null, 2) + '\n'],
    ['ftboss/textures/fx/dandan_candle_light.png', encodePNG(2, 2, new Uint8Array(16).fill(255))]
  ]);
  for (const [relative, bytes] of buildRig(buildWeaponModel(source))) files.set(relative, bytes);
  for (const [relative, bytes] of files) {
    const target = path.join(assetsRoot, relative);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    writeSafe(target, bytes);
  }
  console.log('Dandan scythe: ' + result.model.elements.length + ' cubes / CMD ' + customModelData + ' -> ' + assetsRoot);
  return result;
}

if (require.main === module) generate(process.argv[2]);
module.exports = { buildModel, buildWeaponModel, buildRouting, generate, sourcePath, defaultAssets, customModelData };
