const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { writeSafe, safeCopy } = require('./lib_itemtex.cjs');

const projectRoot = path.resolve(__dirname, '../..');
const assetsRoot = path.resolve(process.argv[2] || path.join(projectRoot, 'src/main/resources/assets/ftboss'));
const artRoot = path.resolve(process.argv[3] || path.join(projectRoot, 'art/dora_nova_launcher'));
const source = JSON.parse(fs.readFileSync(path.join(artRoot, 'Dora_Nova_Launcher.bbmodel'), 'utf8'));
assert.equal(source.meta.model_format, 'java_block');
assert.equal(source.textures.length, 1);
assert.equal(source.resolution.width, 256);
assert.equal(source.resolution.height, 256);
assert(source.textures[0].source.startsWith('data:image/png;base64,'));
const elements = source.elements.filter(element => element.export !== false).map(element => {
  assert.equal(element.type, 'cube');
  assert.equal(element.rescale, false);
  const faces = Object.fromEntries(Object.entries(element.faces).map(([side, face]) => {
    assert.equal(face.texture, 0);
    assert.equal(face.rotation || 0, 0);
    return [side, { uv: face.uv.map((value, index) => value * 16 / (index % 2 ? source.resolution.height : source.resolution.width)), texture: '#atlas' }];
  }));
  const result = { name: element.name, from: element.from, to: element.to, faces };
  const axes = element.rotation.map((angle, index) => ({ angle, index })).filter(rotation => rotation.angle !== 0);
  assert(axes.length <= 1, element.name + ': Java only permits one rotation axis');
  if (axes.length) {
    assert([-45, -22.5, 22.5, 45].includes(axes[0].angle));
    result.rotation = { rescale: false, axis: ['x', 'y', 'z'][axes[0].index], angle: axes[0].angle, origin: element.origin };
  }
  return result;
});
const grouped = source.outliner.flatMap(group => {
  assert.deepEqual(group.rotation, [0, 0, 0], 'Rotated groups require baking before Java export');
  assert.equal(group.export, true);
  assert(group.children.every(child => typeof child === 'string'), 'Only flat editable groups are supported');
  return group.children;
});
assert.deepEqual([...grouped].sort(), source.elements.map(element => element.uuid).sort());
const model = {
  credit: 'Dora / 地脉新星 — original Minecraft launcher inspired by the requested Mental Omega weapon',
  texture_size: [source.resolution.width, source.resolution.height],
  ambientocclusion: source.ambientocclusion,
  gui_light: source.front_gui_light ? 'front' : 'side',
  textures: { atlas: 'ftboss:item/dora_nova_launcher_v1', particle: 'ftboss:item/dora_nova_particle_v1' },
  elements,
  display: source.display
};
const modelDirectory = path.join(assetsRoot, 'models/item');
const textureDirectory = path.join(assetsRoot, 'textures/item');
fs.mkdirSync(modelDirectory, { recursive: true });
fs.mkdirSync(textureDirectory, { recursive: true });
writeSafe(path.join(modelDirectory, 'dora_nova_launcher_v1.json'), JSON.stringify(model, null, 2) + '\n');
writeSafe(path.join(textureDirectory, 'dora_nova_launcher_v1.png'), Buffer.from(source.textures[0].source.split(',')[1], 'base64'));
assert(safeCopy(path.join(artRoot, 'dora_nova_particle_v1.png'), path.join(textureDirectory, 'dora_nova_particle_v1.png')) !== false);
console.log(JSON.stringify({ assetsRoot, source: artRoot, cubes: elements.length, groups: source.outliner.length, resources: 3 }, null, 2));
