'use strict';

const assert = require('assert/strict');
const fs = require('fs');
const path = require('path');
const { decodePNG } = require('./png.cjs');
const { loadGeo, listCubes, cubeCorners } = require('./cube_space.cjs');
const directory = path.resolve(process.argv[2]);
const model = loadGeo(path.join(directory, 'sugar_queen_v3.geo.json'));
const texturePath = path.join(directory, 'sugar_queen_v3.png');
const texture = decodePNG(texturePath);
const animation = JSON.parse(fs.readFileSync(path.join(directory, 'sugar_queen_v3.animation.json'), 'utf8')).animations;
const blockbench = JSON.parse(fs.readFileSync(path.join(directory, 'sugar_queen_v3.bbmodel'), 'utf8'));
const cubes = listCubes(model);
let faces = 0, frames = 0;
for (const entry of cubes) for (const area of Object.values(entry.cube.uv)) {
    assert(area.uv_size.every(value => value > 0));
    for (let vertical = area.uv[1]; vertical < area.uv[1] + area.uv_size[1]; vertical++) {
        for (let horizontal = area.uv[0]; horizontal < area.uv[0] + area.uv_size[0]; horizontal++) {
            assert.equal(texture.data[(vertical * texture.w + horizontal) * 4 + 3], 255, entry.name + ' has unpainted UV pixels');
        }
    }
    faces++;
}
for (const [name, entry] of Object.entries(animation)) for (const [owner, tracks] of Object.entries(entry.bones)) {
    assert(model.byName.has(owner), name + ': missing bone ' + owner);
    for (const track of Object.values(tracks)) {
        const keys = Object.keys(track), times = keys.map(Number);
        assert(times.every((time, index) => time >= 0 && time <= entry.animation_length + 0.00001 && (index === 0 || time > times[index - 1])));
        assert(Object.values(track).every(vector => vector.length === 3 && vector.every(Number.isFinite)));
        if (entry.loop === true) assert.deepEqual(track[keys[0]], track[keys[keys.length - 1]], name + ': loop discontinuity');
        frames += times.length;
    }
}
assert.equal(blockbench.elements.length, cubes.length);
assert.equal(blockbench.animations.length, Object.keys(animation).length);
assert.deepEqual(Buffer.from(blockbench.textures[0].source.split(',')[1], 'base64'), fs.readFileSync(texturePath));
const groupIds = new Set();
const elementIds = [];
function collect(nodes) {
    for (const node of nodes) {
        if (typeof node === 'string') elementIds.push(node);
        else { groupIds.add(node.uuid); collect(node.children); }
    }
}
collect(blockbench.outliner);
assert.equal(groupIds.size, model.bones.length);
assert.equal(new Set(elementIds).size, cubes.length);
for (const entry of blockbench.animations) for (const [identifier, animator] of Object.entries(entry.animators)) {
    assert(groupIds.has(identifier));
    assert(animator.keyframes.every(frame => typeof frame.time === 'number' && frame.data_points.length === 1));
}
const floorBounds = {};
for (const suffix of ['idle', 'walk', 'scepter_raise', 'scepter_slam', 'hurt', 'death']) {
    const entry = animation['animation.king.' + suffix];
    const stamps = [...new Set(Object.values(entry.bones).flatMap(tracks => Object.values(tracks).flatMap(track => Object.keys(track))))];
    let floor = Infinity;
    for (const stamp of stamps) {
        const pose = Object.fromEntries(Object.entries(entry.bones).map(([owner, tracks]) => [owner, Object.fromEntries(Object.entries(tracks).map(([channel, track]) => [channel, track[stamp] || track[Object.keys(track)[0]]]))]));
        const minimum = Math.min(...cubes.flatMap(cube => cubeCorners(model, cube, pose).map(point => point[1])));
        floor = Math.min(floor, minimum);
    }
    floorBounds[suffix] = Math.round(floor * 10000) / 10000;
    assert(floor >= -0.025, suffix + ': model passes through the floor');
}
const source = fs.readFileSync(path.resolve(__dirname, '../../src/main/java/net/farmingtales/ftboss/entity/SugarQueenEntity.java'), 'utf8');
for (const match of source.matchAll(/"(animation\.king\.[a-z_]+)"/g)) assert(animation[match[1]], 'Controller references missing animation ' + match[1]);
const report = { bones: model.bones.length, cubes: cubes.length, paintedFaces: faces, animationTracksKeyframes: frames, embeddedTextureMatches: true, blockbenchHierarchyValid: true, loopEndpointsMatch: true, controllerAnimationsResolve: true, minimumHeightByAnimation: floorBounds, warning: 'Legacy blank animation intentionally empty; unused legacy aliases retained. Offline checks do not replace game testing.' };
fs.writeFileSync(path.join(directory, 'validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
