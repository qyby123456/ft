const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const art = path.join(root, 'art/xueli_weapons');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const read = filename => JSON.parse(fs.readFileSync(filename, 'utf8'));
const models = [];
for (const [identifier, item, count] of [['xueli_skillet_v1', 'skillet', 86], ['xueli_rolling_pin_v1', 'rolling_pin', 46]]) {
  const editable = read(path.join(art, `${identifier}.bbmodel`));
  const model = read(path.join(assets, `models/item/xueli/${item}.json`));
  const texturePath = path.join(assets, `textures/item/${identifier}.png`);
  const texture = decodePNG(texturePath);
  assert.deepEqual(fs.readFileSync(texturePath), Buffer.from(editable.textures[0].source.split(',')[1], 'base64'));
  assert.deepEqual([texture.w, texture.h], [128, 128]);
  assert.equal(model.elements.length, count);
  assert.equal(model.parent, undefined);
  assert.equal(model.overrides, undefined);
  assert.deepEqual(model.display, editable.display);
  assert.equal(Object.keys(model.display).length, 8);
  assert.deepEqual(editable.outliner.flatMap(group => group.children).sort(), editable.elements.map(element => element.uuid).sort());
  for (const [index, element] of model.elements.entries()) {
    const original = editable.elements[index];
    assert.deepEqual(element.from, original.from);
    assert.deepEqual(element.to, original.to);
    assert.equal(element.name, original.name);
    assert.deepEqual(original.rotation, ['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0));
    if (element.rotation) assert.deepEqual(element.rotation.origin, original.origin);
    for (const [side, face] of Object.entries(element.faces)) {
      assert.deepEqual(face.uv, original.faces[side].uv.map(value => value / 8));
      assert.equal(face.texture, '#atlas');
      const [left, top, right, bottom] = face.uv.map(value => value * 8);
      assert(left >= 0 && top >= 0 && right <= 128 && bottom <= 128 && right > left && bottom > top);
      for (let vertical = top; vertical < bottom; vertical++) for (let horizontal = left; horizontal < right; horizontal++) {
        assert.equal(texture.data[(vertical * texture.w + horizontal) * 4 + 3], 255, `${element.name}:${side} transparent texel`);
      }
    }
  }
  for (const reference of Object.values(model.textures)) {
    const [namespace, relative] = reference.split(':');
    assert.equal(namespace, 'ftboss');
    assert(fs.existsSync(path.join(assets, `textures/${relative}.png`)));
  }
  const particle = decodePNG(path.join(assets, `textures/item/${identifier}_particle.png`));
  assert.deepEqual([particle.w, particle.h], [16, 16]);
  models.push({ model: `ftboss:item/xueli/${item}`, elements: count, faces: count * 6, groups: editable.outliner.length });
}
const selector = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/client/model/XueliWeaponModel.java'), 'utf8');
const entity = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java'), 'utf8');
assert(selector.includes('value = Dist.CLIENT'));
assert(selector.includes('entity instanceof EasterBossEntity boss && boss.getVariant() == EasterBossEntity.XUELI_VARIANT'));
assert(selector.includes('MODEL_DATA = 160417'));
assert(selector.includes('original.getOverrides().resolve(original, stack, level, entity, seed)'));
assert(selector.includes('new ModelResourceLocation("farmersdelight", "skillet", "inventory")'));
assert(selector.includes('new ModelResourceLocation("bakery", "rolling_pin", "inventory")'));
assert(selector.includes('event.register(SKILLET)') && selector.includes('event.register(ROLLING_PIN)'));
assert(entity.includes('this.entityData.get(DATA_VARIANT)'));
for (const foreign of ['farmersdelight/models/item/skillet.json', 'bakery/models/item/rolling_pin.json']) {
  assert(!fs.existsSync(path.join(assets, '..', foreign)), 'Built-in must not override foreign item JSON');
}
const report = { status: 'PASS', models, contextSelectsXueli: true, previewModelData: 160417, delegatesOriginalOverrides: true, checks: 'static source and artifact validation', inGameTested: false };
fs.writeFileSync(path.join(art, 'builtin-validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
