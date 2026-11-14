const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { writeSafe, safeCopy } = require('./lib_itemtex.cjs');

const root = path.resolve(__dirname, '../..');
const art = path.join(root, 'art/xuanlan_weapons');
const models = [
    { source: 'Xuanlan_Calibrum_Rifle.bbmodel', id: 'xuanlan_calibrum_v2' },
    { source: 'Xuanlan_Crescendum_Chakram.bbmodel', id: 'xuanlan_crescendum_v2' }
];

function generate(assetsRoot = path.join(root, 'src/main/resources/assets/ftboss')) {
    const result = [];
    let atlas;
    function write(relative, content) {
        const target = path.join(assetsRoot, relative);
        fs.mkdirSync(path.dirname(target), { recursive: true });
        writeSafe(target, content);
    }
    for (const spec of models) {
        const source = JSON.parse(fs.readFileSync(path.join(art, spec.source), 'utf8'));
        assert.equal(source.meta.model_format, 'java_block');
        assert.equal(source.textures.length, 1);
        assert.deepEqual(source.resolution, { width: 128, height: 128 });
        assert(source.outliner.every(group => group.rotation.every(value => value === 0)));
        const texture = Buffer.from(source.textures[0].source.split(',')[1], 'base64');
        assert.equal(texture.readUInt32BE(16), 128);
        assert.equal(texture.readUInt32BE(20), 128);
        if (atlas) assert.deepEqual(texture, atlas, 'Both weapons must share the approved atlas');
        atlas = texture;
        const elements = source.elements.map(cube => {
            assert.equal(cube.type, 'cube');
            const element = { name: cube.name, from: cube.from, to: cube.to,
                faces: Object.fromEntries(Object.entries(cube.faces).map(([side, face]) => {
                    assert.equal(face.texture, 0);
                    const uv = face.uv.map(value => value / 8);
                    assert(uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
                    return [side, { uv, texture: '#atlas' }];
                })) };
            assert.equal(Object.keys(element.faces).length, 6);
            assert(cube.from.every((value, axis) => value >= -16 && cube.to[axis] <= 32 && cube.to[axis] > value));
            const axes = cube.rotation.map((angle, index) => ({ angle, axis: ['x', 'y', 'z'][index] })).filter(rotation => rotation.angle !== 0);
            assert(axes.length <= 1, 'Java item cubes support one rotation axis');
            if (axes.length) {
                assert([-45, -22.5, 22.5, 45].includes(axes[0].angle));
                element.rotation = { rescale: false, axis: axes[0].axis, angle: axes[0].angle, origin: cube.origin };
            }
            return element;
        });
        const model = {
            credit: '玄蓝 / 绿白月武 v2 — Aphelios-inspired original voxel geometry',
            texture_size: [128, 128], ambientocclusion: false, gui_light: 'front',
            textures: { atlas: 'ftboss:item/xuanlan_green_white_v2', particle: 'ftboss:item/xuanlan_particle_v2' },
            elements, display: source.display
        };
        write(`models/item/${spec.id}.json`, JSON.stringify(model, null, 2));
        result.push({ id: spec.id, cubes: elements.length, groups: source.outliner.length });
    }
    write('textures/item/xuanlan_green_white_v2.png', atlas);
    safeCopy(path.join(art, 'xuanlan_particle_v2.png'), path.join(assetsRoot, 'textures/item/xuanlan_particle_v2.png'));
    write('models/item/wenxuanlan_flywheel.json', JSON.stringify({
        loader: 'forge:separate_transforms', gui_light: 'front', ambientocclusion: false,
        textures: { particle: 'ftboss:item/xuanlan_particle_v2' },
        base: { parent: 'ftboss:item/xuanlan_calibrum_v2' },
        perspectives: { ground: { parent: 'ftboss:item/xuanlan_crescendum_v2' } },
        overrides: [{ predicate: { 'ftboss:xuanlan_white': 1 }, model: 'ftboss:item/xuanlan_crescendum_v2' }]
    }, null, 2));
    return { assetsRoot, models: result };
}

if (require.main === module) console.log(JSON.stringify(generate(process.argv[2]), null, 2));
module.exports = { generate };
