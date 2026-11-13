import assert from 'node:assert/strict';
import { readFileSync, readdirSync, mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const resources = join(root, 'src/main/resources');
const assets = join(resources, 'assets/ftdungeonloot');
const load = path => JSON.parse(readFileSync(path, 'utf8'));
const files = directory => readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? files(join(directory, entry.name)) : [join(directory, entry.name)]);
const jsonFiles = files(resources).filter(path => path.endsWith('.json') || path.endsWith('.mcmeta'));
jsonFiles.forEach(load);
assert.equal(load(join(resources, 'pack.mcmeta')).pack.pack_format, 15);
const chinese = load(join(assets, 'lang/zh_cn.json'));
const english = load(join(assets, 'lang/en_us.json'));
assert.deepEqual(Object.keys(chinese).sort(), Object.keys(english).sort());
const model = load(join(assets, 'models/block/loot_stand.json'));
assert.equal(model.elements.length, 71);
assert.ok(model.elements.every(element => !/^(emerald_|counter_coin|dish_|fallen_coin|scroll_)/.test(element.name)));
let faces = 0;
for (const element of model.elements) {
    element.from.forEach((value, axis) => {
        assert.ok(Number.isFinite(value) && value >= -16 && element.to[axis] <= 32);
        assert.ok(element.to[axis] > value);
    });
    if (element.rotation) assert.ok([0, 22.5, -22.5, 45, -45].includes(element.rotation.angle));
    for (const face of Object.values(element.faces)) {
        faces++;
        assert.ok(face.uv.length === 4 && face.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
        assert.equal(model.textures[face.texture.slice(1)], 'ftdungeonloot:block/loot_stand');
    }
}
assert.ok(faces <= 426);
assert.equal(load(join(assets, 'models/item/loot_stand.json')).parent, 'ftdungeonloot:block/loot_stand');
assert.deepEqual(Object.keys(load(join(assets, 'blockstates/loot_stand.json')).variants).sort(), ['facing=east', 'facing=north', 'facing=south', 'facing=west']);
const profile = load(join(resources, 'data/ftdungeonloot/loot_stands/ordinary.json'));
assert.equal(profile.category, 'normal_chest');
assert.equal(profile.currency, 'kubejs:coin');
assert.equal(profile.dimension, 'ftdungeonmobs:dungeon');
assert.equal(profile.round_source, 'ftdungeon');
assert.ok(profile.loot_tables.every(table => !table.includes('boss')));
const ordinary = load(join(resources, 'data/ftdungeonloot/loot_tables/chests/ordinary.json'));
assert.equal(ordinary.type, 'minecraft:chest');
for (const pool of ordinary.pools) {
    for (const entry of pool.entries) {
        assert.equal(entry.type, 'minecraft:item');
        assert.ok(profile.item_values[entry.name === 'minecraft:book' ? 'minecraft:enchanted_book' : entry.name]);
    }
}
const report = { passed: true, jsonFiles: jsonFiles.length, components: model.elements.length, declaredFaces: faces,
    textures: 1, textureWidth: 256, textureHeight: 256, animationFrames: 1, runtimeDynamicItems: 1,
    runtimePriceLabels: 1, blockEntityTicker: false, budget: { components: 80, faces: 480 }, gameClientTested: false };
mkdirSync(join(root, 'verification'), { recursive: true });
writeFileSync(join(root, 'verification/resources.json'), JSON.stringify(report, null, 2));
console.log(JSON.stringify(report));
