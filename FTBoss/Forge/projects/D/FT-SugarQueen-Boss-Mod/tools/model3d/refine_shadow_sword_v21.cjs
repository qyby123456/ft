'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');

const input = process.argv[2];
const output = process.argv[3];
assert(input && output, 'Usage: node refine_shadow_sword_v21.cjs <input.geo.json> <output.geo.json>');
assert.notEqual(path.resolve(input), path.resolve(output), 'Use a separate output; keep the source as a backup.');

const original = JSON.parse(fs.readFileSync(input, 'utf8'));
const model = structuredClone(original);
const bones = model['minecraft:geometry'][0].bones;
const findBone = name => {
    const bone = bones.find(entry => entry.name === name);
    assert(bone, `Missing bone: ${name}`);
    return bone;
};
const round = value => Number(value.toFixed(6));

function cropCube(originalCube, origin, size) {
    const cube = structuredClone(originalCube);
    cube.origin = origin;
    cube.size = size;
    const offset = origin.map((value, axis) => value - originalCube.origin[axis]);
    const fraction = offset.map((value, axis) => value / originalCube.size[axis]);
    const extent = size.map((value, axis) => value / originalCube.size[axis]);
    assert(fraction.every((value, axis) => value >= -1e-6 && value + extent[axis] <= 1 + 1e-6), 'Crop must stay inside its existing UV island.');
    const horizontal = {
        north: [1 - fraction[0] - extent[0], extent[0]],
        south: [fraction[0], extent[0]],
        east: [fraction[2], extent[2]],
        west: [1 - fraction[2] - extent[2], extent[2]]
    };
    for (const [face, [start, length]] of Object.entries(horizontal)) {
        const source = originalCube.uv[face];
        cube.uv[face].uv = [
            round(source.uv[0] + source.uv_size[0] * start),
            round(source.uv[1] + source.uv_size[1] * (1 - fraction[1] - extent[1]))
        ];
        cube.uv[face].uv_size = [round(source.uv_size[0] * length), round(source.uv_size[1] * extent[1])];
    }
    return cube;
}

const handle = findBone('handle');
for (const index of [2, 3]) {
    const cube = handle.cubes[index];
    assert(cube.rotation && Math.abs(cube.rotation[2]) === 22.5, 'Unexpected wrap geometry.');
    cube.pivot = cube.origin.map((value, axis) => round(value + cube.size[axis] / 2));
}

const blade = findBone('blade');
assert.equal(blade.cubes.length, 37, 'Expected the v4 blade baseline.');
const tips = [
    { index: 32, origin: [-1.35, 18.9, -0.55], size: [3.55, 0.45, 1.1] },
    { index: 33, origin: [-0.2, 19.30, -0.475], size: [2.4, 0.45, 0.95] },
    { index: 34, origin: [0.7, 19.70, -0.4], size: [0.65, 0.45, 0.8] }
];
for (const tip of tips) {
    const source = blade.cubes[tip.index];
    const cropped = cropCube(source, [tip.origin[0], source.origin[1], tip.origin[2]], [tip.size[0], source.size[1], tip.size[2]]);
    cropped.origin = tip.origin;
    cropped.size = tip.size;
    blade.cubes[tip.index] = cropped;
}

const pommel = findBone('pommel');
assert.equal(pommel.cubes.length, 2, 'Expected the v4 pommel baseline.');
const base = pommel.cubes[0];
const tiers = [
    { origin: [-1.0, -4.5, -1.05], size: [2.0, 0.3, 2.1] },
    { origin: [-1.3, -4.25, -1.35], size: [2.6, 0.5, 2.7] },
    { origin: [-1.15, -3.8, -1.2], size: [2.3, 0.3, 2.4] }
];
pommel.cubes = [cropCube(base, tiers[1].origin, tiers[1].size), pommel.cubes[1],
    cropCube(base, tiers[0].origin, tiers[0].size), cropCube(base, tiers[2].origin, tiers[2].size)];

for (const [index, bone] of bones.entries()) {
    const baseline = original['minecraft:geometry'][0].bones[index];
    for (const key of ['name', 'parent', 'pivot', 'rotation']) assert.deepEqual(bone[key], baseline[key]);
    for (const cube of bone.cubes || []) {
        assert(cube.size.every(value => Number.isFinite(value) && value > 0));
        for (const face of Object.values(cube.uv)) {
            for (const axis of [0, 1]) {
                const start = face.uv[axis];
                const end = start + face.uv_size[axis];
                assert(Math.min(start, end) >= 0 && Math.max(start, end) <= 256, 'UV outside atlas.');
            }
        }
    }
}

fs.mkdirSync(path.dirname(path.resolve(output)), { recursive: true });
fs.writeFileSync(output, JSON.stringify(model, null, 2) + '\n', 'utf8');
console.log(`Refined ${bones.length} bones / ${bones.reduce((count, bone) => count + (bone.cubes || []).length, 0)} cubes -> ${output}`);
