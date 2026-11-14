const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { decodePNG } = require('./png.cjs');
const { audit } = require('./puffer_bat_hand.cjs');
const { buildModel, sourcePath, defaultAssets, textureName } = require('./gen_puffer_bat.cjs');

const assetsRoot = path.resolve(process.argv[2] || defaultAssets);
const source = JSON.parse(fs.readFileSync(sourcePath, 'utf8'));
const expected = buildModel(source);
const model = JSON.parse(fs.readFileSync(path.join(assetsRoot, 'models/item/puffer_bat.json'), 'utf8'));
const texturePath = path.join(assetsRoot, 'textures/item', textureName + '.png');
assert.deepEqual(model, expected.model, 'Model differs from approved Blockbench source');
assert.deepEqual(fs.readFileSync(texturePath), expected.texture, 'Texture differs from embedded source');
assert.equal(model.parent, undefined, 'Static item must not use a generated or entity parent');
assert.deepEqual(Object.keys(model.display).sort(), ['firstperson_lefthand', 'firstperson_righthand', 'fixed', 'ground', 'gui', 'head', 'thirdperson_lefthand', 'thirdperson_righthand']);
const texture = decodePNG(texturePath);
let checkedFaces = 0;
for (const element of model.elements) {
  for (const face of Object.values(element.faces)) {
    const rectangle = face.uv.map((value, index) => value / 16 * (index % 2 === 0 ? texture.w : texture.h));
    const minX = Math.floor(Math.min(rectangle[0], rectangle[2]));
    const maxX = Math.ceil(Math.max(rectangle[0], rectangle[2]));
    const minY = Math.floor(Math.min(rectangle[1], rectangle[3]));
    const maxY = Math.ceil(Math.max(rectangle[1], rectangle[3]));
    assert(minX < maxX && minY < maxY);
    for (let row = minY; row < maxY; row++) for (let column = minX; column < maxX; column++) {
      assert.equal(texture.data[(row * texture.w + column) * 4 + 3], 255, element.name + ' transparent UV');
    }
    checkedFaces++;
  }
}
const handChecks = audit(model);
assert(handChecks.every(entry => entry.errorModelUnits < 0.001), 'Third-person grip is not centered in hand');
const regression = JSON.parse(JSON.stringify(model));
for (const slot of ['thirdperson_righthand', 'thirdperson_lefthand']) regression.display[slot].translation = [0, 4, 0.5];
assert(audit(regression).every(entry => entry.errorModelUnits > 3), 'Legacy offset regression was not detected');
console.log(JSON.stringify({ valid: true, cubes: model.elements.length, groups: source.outliner.length, texture: [texture.w, texture.h], checkedFaces, sourceMatches: true, displaySlots: Object.keys(model.display).length, handChecks }, null, 2));
