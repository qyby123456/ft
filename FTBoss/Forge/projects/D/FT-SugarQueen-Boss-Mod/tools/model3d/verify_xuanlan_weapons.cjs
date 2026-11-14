const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { decodePNG } = require('./png.cjs');

const root = path.resolve(__dirname, '../..');
const assets = path.resolve(process.argv[2] || path.join(root, 'src/main/resources/assets/ftboss'));
const reference = process.argv[3] && path.resolve(process.argv[3]);
const files = [
    'models/item/wenxuanlan_flywheel.json', 'models/item/xuanlan_calibrum_v2.json',
    'models/item/xuanlan_crescendum_v2.json', 'textures/item/xuanlan_green_white_v2.png',
    'textures/item/xuanlan_particle_v2.png'
];
const read = file => JSON.parse(fs.readFileSync(file, 'utf8'));
const atlasBytes = fs.readFileSync(path.join(assets, files[3]));
const atlas = decodePNG(path.join(assets, files[3]));
assert.deepEqual([atlas.w, atlas.h], [128, 128]);
const reports = [];
for (const [identifier, sourceFile, count] of [
    ['xuanlan_calibrum_v2', 'Xuanlan_Calibrum_Rifle.bbmodel', 155],
    ['xuanlan_crescendum_v2', 'Xuanlan_Crescendum_Chakram.bbmodel', 192]
]) {
    const model = read(path.join(assets, `models/item/${identifier}.json`));
    const source = read(path.join(root, 'art/xuanlan_weapons', sourceFile));
    assert.equal(model.parent, undefined);
    assert.equal(model.elements.length, count);
    assert.equal(source.elements.length, count);
    assert.deepEqual(model.display, source.display);
    assert.equal(Object.keys(model.display).length, 8);
    assert.deepEqual(Buffer.from(source.textures[0].source.split(',')[1], 'base64'), atlasBytes);
    const names = new Set();
    for (const [index, element] of model.elements.entries()) {
        assert(!names.has(element.name));
        names.add(element.name);
        assert.deepEqual(element.from, source.elements[index].from);
        assert.deepEqual(element.to, source.elements[index].to);
        assert(element.from.every((value, axis) => Number.isFinite(value) && value >= -16 && element.to[axis] <= 32 && element.to[axis] > value));
        assert.deepEqual(['x', 'y', 'z'].map(axis => element.rotation?.axis === axis ? element.rotation.angle : 0), source.elements[index].rotation);
        if (element.rotation) {
            assert([-45, -22.5, 22.5, 45].includes(element.rotation.angle));
            assert.deepEqual(element.rotation.origin, source.elements[index].origin);
        }
        for (const [side, face] of Object.entries(element.faces)) {
            assert.equal(face.texture, '#atlas');
            assert.deepEqual(face.uv.map(value => value * 8), source.elements[index].faces[side].uv);
            const [minU, minV, maxU, maxV] = face.uv.map(value => value * 8);
            assert(minU >= 0 && minV >= 0 && maxU <= 128 && maxV <= 128 && minU < maxU && minV < maxV);
            for (let row = minV; row < maxV; row++) for (let column = minU; column < maxU; column++) {
                assert.equal(atlas.data[(row * atlas.w + column) * 4 + 3], 255, element.name + ' transparent face');
            }
        }
    }
    assert.deepEqual(source.outliner.flatMap(group => group.children).sort(), source.elements.map(element => element.uuid).sort());
    if (identifier.includes('crescendum')) {
        assert.deepEqual(model.display.ground.translation, [0, 0, 0]);
        assert.deepEqual(model.display.ground.rotation, [0, 0, 0]);
        assert.deepEqual(model.display.ground.scale, [0.5, 0.5, 0.5]);
    }
    reports.push({ identifier, cubes: count, faces: count * 6, groups: source.outliner.length });
}
const router = read(path.join(assets, files[0]));
assert.equal(router.loader, 'forge:separate_transforms');
assert.equal(router.base.parent, 'ftboss:item/xuanlan_calibrum_v2');
assert.deepEqual(router.perspectives, { ground: { parent: 'ftboss:item/xuanlan_crescendum_v2' } });
assert.deepEqual(router.overrides, [{ predicate: { 'ftboss:xuanlan_white': 1 }, model: 'ftboss:item/xuanlan_crescendum_v2' }]);
const particle = decodePNG(path.join(assets, files[4]));
assert.deepEqual([particle.w, particle.h], [16, 16]);
const hashes = {};
for (const relative of files) {
    const bytes = fs.readFileSync(path.join(assets, relative));
    assert(bytes.length > 0);
    if (reference && relative !== files[0]) assert.deepEqual(bytes, fs.readFileSync(path.join(reference, relative)), relative + ' differs from approved v2');
    hashes[relative] = crypto.createHash('sha256').update(bytes).digest('hex');
}
const entity = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java'), 'utf8');
const equipment = entity.match(/public void equipWeapon\(\)[\s\S]*?String id = WEAPONS\[this.getVariant\(\)\];([\s\S]*?)if \(id == null/)[1];
assert(/getVariant\(\) == 6[\s\S]*?id = "ftboss:wenxuanlan_flywheel"/.test(equipment));
const mapping = entity.match(/String\[\] WEAPONS = \{([\s\S]*?)\};/)[1].replace(/\/\/[^\n]*/g, '');
const ids = [...mapping.matchAll(/"([^"]*)"/g)].map(match => match[1]);
assert.equal(ids[6], 'create:flywheel', 'Legacy generic mapping remains untouched; player weapon uses XuanlanDrops');
assert(/readAdditionalSaveData\(CompoundTag tag\)[\s\S]*?this.setVariant\(tag.getInt\("variant"\)\)/.test(entity));
assert(/tickCount == 1\) \{ this.equipWeapon\(\)/.test(entity));
const renderer = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/client/render/FlywheelRenderer.java'), 'utf8');
assert(renderer.includes('ItemDisplayContext.GROUND'));
assert(renderer.includes('(entity.tickCount + partialTick) * 25.0F'));
console.log(JSON.stringify({ passed: true, assets, approvedV2GeometryAndTexturesByteIdentical: Boolean(reference), models: reports,
    equipped: 'ftboss:wenxuanlan_flywheel', legacyMapping: ids[6], unchangedBossSpin: '25 degrees/tick', hashes }, null, 2));
