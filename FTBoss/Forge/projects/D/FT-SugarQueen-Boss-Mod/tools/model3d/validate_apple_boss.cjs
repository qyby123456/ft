const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const assetRoot = path.resolve(process.argv[2]);
const baselineRoot = path.resolve(process.argv[3]);
const { decodePNG } = require(path.join(process.env.FT_MODEL_TOOLS || __dirname, 'png.cjs'));
const read = name => JSON.parse(fs.readFileSync(path.join(assetRoot, name), 'utf8'));
const model = read('apple_boss.geo.json')['minecraft:geometry'][0];
const oldModel = JSON.parse(fs.readFileSync(path.join(baselineRoot, 'apple_boss.geo.json'), 'utf8'))['minecraft:geometry'][0];
const rig = bones => bones.map(({ name, parent, pivot }) => ({ name, parent, pivot }));
assert.deepEqual(rig(model.bones), rig(oldModel.bones), 'Existing animation rig must remain unchanged');
const manifest = read('apple_boss_manifest.json');
assert.equal(manifest.cubes, model.bones.reduce((count, bone) => count + (bone.cubes || []).length, 0));
assert.equal(manifest.faces, manifest.cubes * 6);
const occupied = new Uint16Array(256 * 256);
const textures = ['apple_boss.png', 'apple_boss_p2.png'].map(name => decodePNG(path.join(assetRoot, name)));
for (const texture of textures) { assert.equal(texture.w, 256); assert.equal(texture.h, 256); }
for (const island of manifest.islands) {
    assert(island.left >= 1 && island.top >= 1 && island.left + island.width < 256 && island.top + island.height < 256);
    for (let row = island.top - 1; row <= island.top + island.height; row++) {
        for (let column = island.left - 1; column <= island.left + island.width; column++) {
            const pixel = row * 256 + column;
            assert.equal(occupied[pixel], 0, 'Overlapping UV island or gutter: ' + island.part);
            occupied[pixel]++;
            for (const texture of textures) assert.equal(texture.data[pixel * 4 + 3], 255, 'Transparent UV pixel: ' + island.part);
        }
    }
}
const coplanar = [];
for (let first = 0; first < manifest.parts.length; first++) for (let second = first + 1; second < manifest.parts.length; second++) {
    const one = manifest.parts[first], two = manifest.parts[second];
    if (one.rotation || two.rotation) continue;
    for (let axis = 0; axis < 3; axis++) {
        const overlap = [0, 1, 2].filter(value => value !== axis).map(value =>
            Math.min(one.origin[value] + one.size[value], two.origin[value] + two.size[value]) - Math.max(one.origin[value], two.origin[value]));
        if (overlap.some(value => value <= 0.01)) continue;
        for (const end of [0, 1]) if (Math.abs(one.origin[axis] + one.size[axis] * end - two.origin[axis] - two.size[axis] * end) < 0.001) {
            coplanar.push({ first: one.label, second: two.label, axis, end });
        }
    }
}
assert.deepEqual(coplanar, [], 'Coplanar overlapping rest-pose faces');
const project = read('apple_boss.bbmodel');
assert.equal(project.elements.length, manifest.cubes);
assert.equal(project.animations.length, 16);
const groupIds = new Set();
function walk(nodes) {
    for (const node of nodes) if (typeof node !== 'string') { groupIds.add(node.uuid); walk(node.children); }
}
walk(project.outliner);
assert.equal(groupIds.size, 37);
for (const animation of project.animations) for (const [groupId, animator] of Object.entries(animation.animators)) {
    assert(groupIds.has(groupId), 'Blockbench animator must reference a bone UUID');
    for (const frame of animator.keyframes) {
        assert(Number.isFinite(frame.time) && frame.time >= 0 && frame.time <= animation.length);
        assert.equal(frame.data_points.length, 1);
        assert(['x', 'y', 'z'].every(axis => Number.isFinite(Number(frame.data_points[0][axis]))));
    }
}
const report = { result: 'PASS', bones: 37, cubes: manifest.cubes, uvFaces: manifest.faces, uvGutters: 'opaque, non-overlapping',
    coplanarRestFaces: coplanar.length, animationsPreserved: 16, rigUnchanged: true, textures: '256x256, both phases',
    limitations: ['Rest-pose coplanarity check excludes rotated cubes.', 'Offline rendering does not replace in-game testing.',
        'Existing idle animation has 10 end-key warnings; animation file is not modified.'] };
fs.writeFileSync(path.join(assetRoot, 'validation_report.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
