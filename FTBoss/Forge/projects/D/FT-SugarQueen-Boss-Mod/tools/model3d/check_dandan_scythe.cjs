const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { buildModel, buildWeaponModel, buildRouting, sourcePath, defaultAssets, customModelData } = require('./gen_dandan_scythe.cjs');
const readJSON = filename => JSON.parse(fs.readFileSync(filename, 'utf8'));
const source = readJSON(sourcePath);
const generated = buildModel(source);
const model = readJSON(path.join(defaultAssets, 'ftboss/models/item/dandan_end_rod_scythe.json'));
const weapon = readJSON(path.join(defaultAssets, 'ftboss/models/item/dandan_scythe.json'));
assert.deepEqual(weapon, buildWeaponModel(source));
assert.equal(weapon.elements.length, 111);
const glowing = weapon.elements.filter(element => element.shade === false);
assert(glowing.length > 0 && glowing.length < weapon.elements.length);
for (const element of glowing) for (const face of Object.values(element.faces)) {
  assert.deepEqual(face.forge_data, { block_light: 15, sky_light: 15, ambient_occlusion: false });
}
assert(fs.existsSync(path.join(defaultAssets, 'ftboss/textures/fx/dandan_candle_light.png')));
assert.deepEqual(model, generated.model);
assert.deepEqual(fs.readFileSync(path.join(defaultAssets, 'ftboss/textures/item/dandan_end_rod_scythe_v2.png')), generated.texture);
const routing = readJSON(path.join(defaultAssets, 'minecraft/models/item/end_rod.json'));
assert.deepEqual(routing, buildRouting());
assert.deepEqual(buildRouting(routing), routing);
assert.throws(() => buildRouting({ parent: 'minecraft:block/end_rod', overrides: [{ predicate: { custom_model_data: 42 }, model: 'other:item/custom' }] }));
function resolve(value) {
  let selected = routing.parent;
  for (const override of routing.overrides) if (value >= override.predicate.custom_model_data) selected = override.model;
  return selected;
}
for (const value of [0, 1, 91600, 91602, 1000000]) assert.equal(resolve(value), 'minecraft:block/end_rod');
assert.equal(resolve(customModelData), 'ftboss:item/dandan_end_rod_scythe');
assert.equal(model.elements.length, 111);
assert.equal(source.outliner.length, 8);
assert(!model.parent);
assert.deepEqual(Object.keys(model.display).sort(), ['firstperson_lefthand', 'firstperson_righthand', 'fixed', 'ground', 'gui', 'head', 'thirdperson_lefthand', 'thirdperson_righthand']);
const entity = fs.readFileSync(path.resolve(__dirname, '../../src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java'), 'utf8');
assert(entity.includes('private static final int DANDAN_VARIANT = 1;'));
assert(entity.includes('private static final int DANDAN_WEAPON_MODEL_DATA = ' + customModelData + ';'));
assert(/if \(this\.getVariant\(\) == DANDAN_VARIANT && st\.is\(Items\.END_ROD\)\)\s*\{\s*st\.getOrCreateTag\(\)\.putInt\("CustomModelData", DANDAN_WEAPON_MODEL_DATA\);\s*\}/.test(entity));
let approvedDesignMatches = null;
if (process.argv[2]) {
  const approvedRoot = path.resolve(process.argv[2], 'assets');
  const approved = readJSON(path.join(approvedRoot, 'ftboss/models/item/dandan_end_rod_scythe.json'));
  delete approved.credit;
  delete approved.texture_size;
  assert.deepEqual(model, approved);
  for (const name of ['dandan_end_rod_scythe_v2.png', 'dandan_end_rod_scythe_particle.png']) {
    assert.deepEqual(fs.readFileSync(path.join(approvedRoot, 'ftboss/textures/item', name)), fs.readFileSync(path.join(defaultAssets, 'ftboss/textures/item', name)));
  }
  approvedDesignMatches = true;
}
const report = {
  playerItem: 'ftboss:dandan_scythe', glowingElements: glowing.length,
  valid: true, item: 'minecraft:end_rod', customModelData, cubes: model.elements.length,
  approvedDesignMatches, exporterMatchesSource: true, vanillaFallbackVerified: true,
  dandanOnlyEquipmentGuard: true, unrelatedOverridesProtected: true,
  notVerified: ['Minecraft hand placement', 'Actual game resource loading']
};
fs.writeFileSync(path.resolve(__dirname, '../../art/dandan_scythe/source-check.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
