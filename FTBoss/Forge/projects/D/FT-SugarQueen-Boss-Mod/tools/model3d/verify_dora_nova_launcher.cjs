const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const { decodePNG } = require('./png.cjs');
const { writeSafe } = require('./lib_itemtex.cjs');

const projectRoot = path.resolve(__dirname, '../..');
const assetsRoot = path.resolve(process.argv[2] || path.join(projectRoot, 'src/main/resources/assets/ftboss'));
const artRoot = path.join(projectRoot, 'art/dora_nova_launcher');
const readJSON = filename => JSON.parse(fs.readFileSync(filename, 'utf8'));
const source = readJSON(path.join(artRoot, 'Dora_Nova_Launcher.bbmodel'));
const modelFile = path.join(assetsRoot, 'models/item/dora_nova_launcher_v1.json');
const textureFile = path.join(assetsRoot, 'textures/item/dora_nova_launcher_v1.png');
const particleFile = path.join(assetsRoot, 'textures/item/dora_nova_particle_v1.png');
const model = readJSON(modelFile);
const texture = decodePNG(textureFile);
const particle = decodePNG(particleFile);
assert.equal(model.parent, undefined);
assert.equal(model.elements.length, 133);
assert.equal(source.outliner.length, 9);
assert.deepEqual(model.display, source.display);
assert.equal(Object.keys(model.display).length, 8);
assert.equal(model.ambientocclusion, false);
assert.equal(model.gui_light, 'front');
assert.deepEqual([texture.w, texture.h], [256, 256]);
assert.deepEqual(Buffer.from(source.textures[0].source.split(',')[1], 'base64'), fs.readFileSync(textureFile));
assert.deepEqual([particle.w, particle.h], [16, 16]);
for (let offset = 3; offset < particle.data.length; offset += 4) assert.equal(particle.data[offset], 255);
const mainFaceDensities = [];
const thinFaces = [];
for (const [index, element] of model.elements.entries()) {
  const editable = source.elements[index];
  assert.equal(element.name, editable.name);
  assert.deepEqual(element.from, editable.from);
  assert.deepEqual(element.to, editable.to);
  assert.deepEqual(editable.rotation, ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0));
  if (element.rotation) assert.deepEqual(element.rotation.origin, editable.origin);
  const size = element.to.map((value, axis) => value - element.from[axis]);
  for (const axis of [0, 1, 2]) {
    assert(size[axis] > 0);
    assert(element.from[axis] >= -16 && element.to[axis] <= 32);
  }
  for (const [side, face] of Object.entries(element.faces)) {
    assert.equal(face.texture, '#atlas');
    assert(face.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
    const pixels = face.uv.map(value => value * 16);
    assert.deepEqual(pixels, editable.faces[side].uv);
    const lowU = Math.min(pixels[0], pixels[2]);
    const highU = Math.max(pixels[0], pixels[2]);
    const lowV = Math.min(pixels[1], pixels[3]);
    const highV = Math.max(pixels[1], pixels[3]);
    assert(highU > lowU && highV > lowV);
    for (let row = Math.floor(lowV); row < Math.ceil(highV); row++) for (let column = Math.floor(lowU); column < Math.ceil(highU); column++) {
      assert.equal(texture.data[(row * texture.w + column) * 4 + 3], 255, element.name + ': transparent UV');
    }
    const physical = ['up', 'down'].includes(side) ? [size[0], size[2]] : ['east', 'west'].includes(side) ? [size[2], size[1]] : [size[0], size[1]];
    const density = [(highU - lowU) / physical[0], (highV - lowV) / physical[1]];
    if (Math.min(...physical) >= 1) mainFaceDensities.push(...density);
    if (Math.max(...density) > 3) thinFaces.push({ element: element.name, side, density });
  }
}
assert(mainFaceDensities.every(value => value >= 1 && value <= 2));
for (const reference of Object.values(model.textures)) {
  assert(reference.startsWith('ftboss:item/'));
  assert(fs.existsSync(path.join(assetsRoot, 'textures', reference.split(':')[1] + '.png')));
}
const entitySource = fs.readFileSync(path.join(projectRoot, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java'), 'utf8');
const weaponIds = [...entitySource.match(/String\[\] WEAPONS\s*=\s*\{([\s\S]*?)\};/)[1].matchAll(/"([^"]*)"/g)].map(match => match[1]);
assert.equal(weaponIds[2], 'bren:machine_gun');
const hash = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
const report = {
  valid: true, cubes: model.elements.length, groups: source.outliner.length,
  resources: Object.fromEntries([modelFile, textureFile, particleFile].map(filename => [path.relative(assetsRoot, filename).replaceAll('\\', '/'), hash(filename)])),
  scope: 'Dora (EasterBossEntity variant 2) holding bren:machine_gun; all other holders delegate to the original model',
  mainFaceDensity: { min: Math.min(...mainFaceDensities), max: Math.max(...mainFaceDensities) },
  thinFacesAboveThreePixelsPerUnit: thinFaces.length,
  checks: ['Blockbench geometry, rotations, UV, texture and display match', 'All face UVs opaque', 'Main face density 1-2 px/unit', 'Separate opaque particle texture', 'No generated parent', 'Existing Dora weapon ID retained'],
  notVerified: ['Minecraft render, arm pose, first-person behavior and resource reload', 'Interactive Blockbench import']
};
writeSafe(path.join(artRoot, 'builtin-validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
