import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync, mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const resources = join(root, 'src/main/resources');
const namespace = 'ftdungeontrading';
const assets = join(resources, 'assets', namespace);
const load = path => JSON.parse(readFileSync(path, 'utf8'));
const files = directory => readdirSync(directory, { withFileTypes: true }).flatMap(entry => entry.isDirectory()
    ? files(join(directory, entry.name)) : [join(directory, entry.name)]);
const jsonFiles = files(resources).filter(path => path.endsWith('.json') || path.endsWith('.mcmeta'));
jsonFiles.forEach(load);
assert.equal(load(join(resources, 'pack.mcmeta')).pack.pack_format, 15);

const chinese = load(join(assets, 'lang/zh_cn.json'));
const english = load(join(assets, 'lang/en_us.json'));
assert.deepEqual(Object.keys(chinese).sort(), Object.keys(english).sort());
const javaSources = files(join(root, 'src/main/java')).filter(path => path.endsWith('.java'));
for (const path of javaSources) {
    const source = readFileSync(path, 'utf8');
    for (const match of source.matchAll(/Component\.translatable\("((?:block|itemGroup|label|message|trade)\.ftdungeontrading\.[a-z_]+)"/g)) {
        if (!match[1].endsWith('.')) assert.ok(chinese[match[1]], `Missing translation ${match[1]}`);
    }
}

const model = load(join(assets, 'models/block/trading_table.json'));
const item = load(join(assets, 'models/item/trading_table.json'));
assert.equal(item.parent, `${namespace}:block/trading_table`);
const states = load(join(assets, 'blockstates/trading_table.json')).variants;
assert.deepEqual(Object.keys(states).sort(), ['east', 'north', 'south', 'west'].flatMap(facing => ['left', 'right'].map(part => `facing=${facing},part=${part}`)).sort());
for (const [state, variant] of Object.entries(states)) assert.equal(variant.model, `${namespace}:block/trading_table_${state.split('part=')[1]}`);
assert.ok(model.elements.length <= 64);
assert.equal(model.render_type, 'minecraft:cutout');
const cutout = new Set(load(join(root, 'model_sources/model-report.json')).cutout_components);
let faceCount = 0;
for (const element of model.elements) {
    assert.equal(element.from.length, 3);
    assert.equal(element.to.length, 3);
    element.from.forEach((value, axis) => {
        assert.ok(Number.isFinite(value) && value >= (axis === 0 ? -16 : 0) && element.to[axis] <= (axis === 0 ? 32 : 16));
        assert.ok(element.to[axis] >= value, `Negative cube ${element.name}`);
        if (!cutout.has(element.name)) assert.ok(element.to[axis] > value, `Unapproved plane ${element.name}`);
    });
    if (element.rotation) {
        assert.ok(['x', 'y', 'z'].includes(element.rotation.axis));
        assert.ok([-45, -22.5, 0, 22.5, 45].includes(element.rotation.angle));
    }
    for (const face of Object.values(element.faces)) {
        faceCount++;
        assert.ok(face.uv.length === 4 && face.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
        assert.ok(face.texture.startsWith('#') && model.textures[face.texture.slice(1)]);
    }
}
assert.ok(faceCount <= 352);
assert.ok(!model.elements.some(element => /^(Coin pile|Emerald|Old key)\s*\//.test(element.name)), 'Removed decorations must not return');
assert.equal(cutout.size, 2);
const slots = ['gui', 'ground', 'fixed', 'thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand'];
for (const slot of slots) assert.ok(model.display[slot], `Missing display ${slot}`);
for (const texture of Object.values(model.textures)) {
    if (texture.startsWith('#')) continue;
    const [name, path] = texture.split(':');
    const png = readFileSync(join(resources, 'assets', name, 'textures', `${path}.png`));
    assert.equal(png.subarray(0, 8).toString('hex'), '89504e470d0a1a0a');
    assert.equal(png.readUInt32BE(16), 128);
    assert.equal(png.readUInt32BE(20), 128);
}

const report = load(join(root, 'model_sources/model-report.json'));
assert.equal(report.components, model.elements.length);
assert.equal(report.declared_faces, faceCount);
let assembledComponents = 0;
let assembledFaces = 0;
for (const part of ['left', 'right']) {
    const half = load(join(assets, `models/block/trading_table_${part}.json`));
    assert.equal(half.render_type, 'minecraft:cutout');
    assert.equal(half.elements.length, report.parts[part].components);
    assert.equal(half.elements.reduce((total, element) => total + Object.keys(element.faces).length, 0), report.parts[part].faces);
    for (const element of half.elements) {
        assert.ok(element.from.every((value, axis) => value >= -16 && element.to[axis] <= 32 && value <= element.to[axis]));
    }
    assembledComponents += half.elements.length;
    assembledFaces += report.parts[part].faces;
}
assert.equal(assembledComponents, report.assembled_block_components);
assert.equal(assembledFaces, report.assembled_block_faces);
assert.ok(assembledComponents <= 80 && assembledFaces <= 432);
const project = load(join(root, 'model_sources/地牢交易桌.bbmodel'));
assert.equal(project.meta.model_format, 'java_block');
assert.equal(project.elements.filter(element => !element.type || element.type === 'cube').length, model.elements.length);
assert.equal(project.textures.length, 1);
assert.ok(project.textures[0].source.startsWith('data:image/png;base64,'));

for (const path of files(join(resources, 'data', namespace, 'ft_trades'))) {
    const offer = load(path);
    assert.ok(offer.costs.length >= 1 && offer.costs.length <= 8);
    assert.ok(offer.rewards.length >= 1 && offer.rewards.length <= 8);
    for (const cost of offer.costs) assert.notEqual(Boolean(cost.item), Boolean(cost.tag));
    for (const reward of offer.rewards) assert.ok(reward.item);
    for (const entry of [...offer.costs, ...offer.rewards]) assert.ok(Number.isInteger(entry.count ?? 1) && (entry.count ?? 1) >= 1 && (entry.count ?? 1) <= 4096);
}

const nativePath = join(root, 'model_sources/qa/blockbench-native-export.json');
const native = load(nativePath);
assert.equal(native.elements.length, model.elements.length);
const normalize = (element, parent) => ({
    from: element.from.map(value => Number(value.toFixed(6))), to: element.to.map(value => Number(value.toFixed(6))),
    rotation: element.rotation?.angle ? { origin: element.rotation.origin.map(value => Number(value.toFixed(6))), axis: element.rotation.axis, angle: element.rotation.angle, rescale: element.rotation.rescale ?? false } : null,
    shade: element.shade ?? true,
    faces: Object.fromEntries(Object.keys(element.faces).sort().map(side => {
        const face = element.faces[side];
        return [side, { uv: face.uv.map(value => Number(value.toFixed(6))), rotation: face.rotation ?? 0, texture: parent.textures[face.texture.slice(1)] }];
    }))
});
const nativeByName = new Map(native.elements.map(element => [element.name, element]));
assert.equal(nativeByName.size, model.elements.length);
model.elements.forEach(element => assert.deepEqual(normalize(nativeByName.get(element.name), native), normalize(element, model), `Native Blockbench mismatch: ${element.name}`));
for (const slot of slots) {
    for (const key of ['rotation', 'translation', 'scale']) {
        const fallback = key === 'scale' ? [1, 1, 1] : [0, 0, 0];
        const normalizeVector = vector => key === 'rotation' ? vector.map(value => ((value % 360) + 360) % 360) : vector;
        assert.deepEqual(normalizeVector(native.display[slot]?.[key] ?? fallback), normalizeVector(model.display[slot]?.[key] ?? fallback), `Native display mismatch: ${slot}/${key}`);
    }
}

const result = { passed: true, json_files: jsonFiles.length, translations: Object.keys(chinese).length,
    components: model.elements.length, declared_faces: faceCount, assembled_components: assembledComponents, assembled_faces: assembledFaces, blockstate_variants: 8, display_slots: slots.length, native_export_geometry_uv_display_equal: true,
    comparison_normalization: 'Coordinates and UV rounded to six decimals; component names used across native group reordering; defaults and equivalent angle wraps normalized. Not a byte comparison.',
    limitations: ['Does not replace the Minecraft parser, Forge GameTest, Blockbench visual review, or an actual client test.'] };
const coreBaseline = load(join(root, 'model_sources/transaction-core-baseline.json'));
const intentionallyModified = new Set(['FTDungeonTrading.java', 'TradeService.java', 'TradeTableAccess.java', 'TradeTableBlockEntity.java']);
let unchangedFiles = 0;
result.intentionally_modified_runtime_files = [];
for (const [path, expected] of Object.entries(coreBaseline)) {
    if (intentionallyModified.has(path.split('/').at(-1))) {
        result.intentionally_modified_runtime_files.push(path);
        continue;
    }
    assert.equal(createHash('sha256').update(readFileSync(join(root, path))).digest('hex'), expected, `Transaction core unexpectedly changed: ${path}`);
    unchangedFiles++;
}
assert.equal(result.intentionally_modified_runtime_files.length, 4);
result.unchanged_transaction_core_files = unchangedFiles;
mkdirSync(join(root, 'model_sources/qa'), { recursive: true });
writeFileSync(join(root, 'model_sources/qa/resource-check.json'), `${JSON.stringify(result, null, 2)}\n`);
console.log(JSON.stringify(result));
