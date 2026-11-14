const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const art = path.join(root, 'art/lemoncat_weapons');
const read = file => JSON.parse(fs.readFileSync(file, 'utf8'));
const models = [];
for (const [identifier, name] of [['skillet', 'LemonCat_Citrus_Skillet.bbmodel'], ['kitchen_shovel', 'LemonCat_Paw_Spatula.bbmodel']]) {
  const source = read(path.join(art, name));
  const model = read(path.join(assets, `models/item/lemoncat/${identifier}.json`));
  const texturePath = path.join(assets, `textures/item/lemoncat/${identifier}.png`);
  const texture = decodePNG(texturePath);
  assert.deepEqual(fs.readFileSync(texturePath), Buffer.from(source.textures[0].source.split(',')[1], 'base64'));
  assert.deepEqual(model.texture_size, [texture.w, texture.h]);
  assert.deepEqual(source.display, model.display);
  assert.equal(model.elements.length, source.elements.length);
  assert(!model.parent);
  for (const group of source.outliner) {
    assert(group.rotation.every(value => value === 0));
    assert(group.children.every(child => typeof child === 'string'));
  }
  assert.deepEqual(source.outliner.flatMap(group => group.children).sort(), source.elements.map(element => element.uuid).sort());
  for (const [index, element] of model.elements.entries()) {
    const editable = source.elements[index];
    assert.deepEqual(editable.from, element.from);
    assert.deepEqual(editable.to, element.to);
    assert.equal(element.name, editable.name);
    assert.deepEqual(editable.rotation, ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0));
    if (element.rotation) assert.deepEqual(element.rotation.origin, editable.origin);
    for (const [face, value] of Object.entries(element.faces)) {
      assert.deepEqual(value.uv, editable.faces[face].uv.map((coordinate, axis) => coordinate * 16 / (axis % 2 ? texture.h : texture.w)));
      assert.equal(value.texture, '#atlas');
    }
  }
  models.push({ identifier, elements: model.elements.length, groups: source.outliner.length, texture: [texture.w, texture.h] });
}
const entity = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java'), 'utf8');
const renderer = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/client/model/LemonCatWeaponModel.java'), 'utf8');
assert(entity.includes('LEMONCAT_WEAPON_MODEL_DATA = 160416'));
assert(entity.includes('LEMONCAT_VARIANT = 4'));
assert(entity.includes('"kaleidoscope_cookery:kitchen_shovel"'));
assert(entity.includes('st.getOrCreateTag().putInt("CustomModelData", LEMONCAT_WEAPON_MODEL_DATA)'));
assert(entity.includes('off.getOrCreateTag().putInt("CustomModelData", LEMONCAT_WEAPON_MODEL_DATA)'));
assert(entity.includes('if (tag.contains("variant")) this.setVariant(tag.getInt("variant"));'));
assert(renderer.includes('== EasterBossEntity.LEMONCAT_WEAPON_MODEL_DATA'));
assert(renderer.includes('original.getOverrides().resolve(original, stack, level, entity, seed)'));
assert(renderer.includes('value = Dist.CLIENT'));
for (const name of ['farmersdelight/models/item/skillet.json', 'kaleidoscope_cookery/models/item/kitchen_shovel.json']) {
  assert(!fs.existsSync(path.resolve(assets, '..', name)), 'Foreign model must not be shadowed by the built-in version');
}
const report = { status: 'passed', models, customModelData: 160416, clientOnlySelector: true, delegatesOriginalItemStates: true, foreignModelJsonOverrides: false, saveLoadReequips: true, inGameTested: false };
fs.writeFileSync(path.join(art, 'integration-validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
