const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { decodePNG } = require('./png.cjs');
const { buildModel, sourcePath, defaultAssets, itemName, particleName, artRoot } = require('./gen_maque_feather.cjs');

const root = path.resolve(__dirname, '../..');
const assets = process.argv[2] ? path.resolve(process.argv[2]) : defaultAssets;
const readJSON = filename => JSON.parse(fs.readFileSync(filename, 'utf8'));
const source = readJSON(sourcePath);
const expected = buildModel(source);
const model = readJSON(path.join(assets, 'models/item/' + itemName + '.json'));
const texturePath = path.join(assets, 'textures/item/' + itemName + '.png');
assert.deepEqual(model, expected.model);
assert.deepEqual(fs.readFileSync(texturePath), expected.texture);
assert(!model.parent, 'Static elements must not inherit generated or builtin/entity');
assert.equal(model.elements.length, 45);
assert.deepEqual(source.outliner.map(group => group.name), ['01_层叠白羽', '02_羽片收尖', '03_浅色羽纹', '04_天然羽轴']);
assert(model.elements.every(element => !/柄|护符|金|玉|雀|箍|带|珠|徽/.test(element.name)));
assert.equal(new Set(model.elements.map(element => element.name)).size, model.elements.length);
const texture = decodePNG(texturePath);
assert.deepEqual([texture.w, texture.h], [64, 64]);
let checkedFaces = 0;
const densityValues = [];
for (const element of model.elements) {
  const dimensions = element.to.map((value, axis) => value - element.from[axis]);
  for (const [side, face] of Object.entries(element.faces)) {
    const rectangle = face.uv.map(value => value * texture.w / 16);
    assert(rectangle.every(Number.isInteger), element.name + ': non-integer texture coordinate');
    const [minU, minV, maxU, maxV] = rectangle;
    assert(minU < maxU && minV < maxV);
    for (let row = minV; row < maxV; row++) for (let column = minU; column < maxU; column++) {
      assert.equal(texture.data[(row * texture.w + column) * 4 + 3], 255, element.name + ': transparent UV');
    }
    const physical = ['up', 'down'].includes(side) ? [dimensions[0], dimensions[2]]
      : ['east', 'west'].includes(side) ? [dimensions[2], dimensions[1]] : [dimensions[0], dimensions[1]];
    const density = [(maxU - minU) / physical[0], (maxV - minV) / physical[1]];
    assert(Math.max(...density) <= 3, element.name + ': excess density');
    if (Math.min(...physical) >= 1) densityValues.push(...density);
    checkedFaces++;
  }
}
assert(densityValues.every(value => value >= 1 - 1e-8 && value <= 2));
assert.equal(Object.keys(model.display).length, 8);
for (const transform of Object.values(model.display)) {
  for (const vector of Object.values(transform)) assert(vector.length === 3 && vector.every(Number.isFinite));
  assert(transform.scale.every(value => value > 0 && value <= 4));
  assert(transform.translation.every(value => Math.abs(value) <= 80));
}
const particlePath = path.join(assets, 'textures/item/' + particleName + '.png');
assert.deepEqual(fs.readFileSync(particlePath), fs.readFileSync(path.join(artRoot, particleName + '.png')));
const particle = decodePNG(particlePath);
assert.deepEqual([particle.w, particle.h], [16, 16]);
for (let offset = 3; offset < particle.data.length; offset += 4) assert.equal(particle.data[offset], 255);
for (const reference of Object.values(model.textures)) {
  assert(reference.startsWith('ftboss:'));
  assert(fs.existsSync(path.join(assets, 'textures', reference.slice('ftboss:'.length) + '.png')));
}
const javaRoot = path.join(root, 'src/main/java/net/farmingtales/ftboss');
const registry = fs.readFileSync(path.join(javaRoot, 'item/FTBossItems.java'), 'utf8');
const tabs = fs.readFileSync(path.join(javaRoot, 'item/FTCreativeTabs.java'), 'utf8');
const entity = fs.readFileSync(path.join(javaRoot, 'entity/EasterBossEntity.java'), 'utf8');
assert(registry.includes('ITEMS.register("maque_blessing_feather", net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem::new)'));
assert(tabs.includes('ManorWeaponCatalog.addTo(out);'));
assert(fs.readFileSync(path.join(javaRoot, 'item/ManorWeaponCatalog.java'), 'utf8')
  .includes('new Entry("ftboss:maque_blessing_feather", Form.DEFAULT)'));
const equip = entity.slice(entity.indexOf('public void equipWeapon()'), entity.indexOf('public void equipWeapon()') + 7000);
assert(equip.includes('String id = WEAPONS[this.getVariant()]'));
assert(equip.includes('st.enchant(e, WEAPON_ENCH_LEVEL)'));
assert(equip.includes('st.getOrCreateTag().putBoolean("Unbreakable", true)'));
assert(/this\.tickCount == 1[^\n]*this\.equipWeapon\(\)/.test(entity));
const weaponTable = entity.match(/String\[\] WEAPONS = \{([\s\S]*?)\};/)[1];
const ids = [...weaponTable.matchAll(/"([a-z0-9_:]+)"/g)].map(match => match[1]);
assert.equal(ids[9], 'ftboss:maque_blessing_feather', 'Sparrow reward must be the usable feather');
const drop = entity.slice(entity.indexOf('public void dropWeapon()'), entity.indexOf('public void dropWeapon()') + 1000);
assert(drop.includes('String id = WEAPONS[this.getVariant()]'));
assert(!drop.includes(itemName));
assert(/protected void dropCustomDeathLoot[\s\S]*MaqueFeatherRules\.shouldDrop[\s\S]*this\.dropWeapon\(\)/.test(entity));
const key = 'item.ftboss.' + itemName;
assert.equal(readJSON(path.join(defaultAssets, 'lang/zh_cn.json'))[key], '雀翎·回春');
assert.equal(readJSON(path.join(defaultAssets, 'lang/en_us.json'))[key], "Maque's Blessing Feather");
if (process.argv[3]) assert.deepEqual(model, readJSON(path.resolve(process.argv[3])), 'Approved preview changed during import');
console.log(JSON.stringify({ valid: true, item: 'ftboss:' + itemName, cubes: model.elements.length, groups: source.outliner.length,
  checkedFaces, texture: [texture.w, texture.h], mainDensityRange: [Math.min(...densityValues), Math.max(...densityValues)],
  sourceMatches: true, equipmentWired: true, oldEntitiesReequipOnFirstTick: true, playerKillDropWired: true,
  approvedPreviewMatches: Boolean(process.argv[3]), requiresResourcePack: false,
  notVerified: ['Minecraft runtime registry and reload', 'actual hand placement and Boss arm pose'] }, null, 2));
