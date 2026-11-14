'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const crypto = require('crypto');
const { decodePNG } = require('./png.cjs');
const { loadGeo, listCubes, cubeCorners } = require('./cube_space.cjs');
const { samplePose } = require('./preview_benger.cjs');
const { bones, parts } = require('./benger_spec.cjs');

const directory = path.resolve(process.argv[2] || 'art/benger_reference');
const read = name => JSON.parse(fs.readFileSync(path.join(directory, name), 'utf8'));
const model = loadGeo(path.join(directory, 'benger.geo.json'));
const texture = decodePNG(path.join(directory, 'benger.png'));
const animations = read('benger.animation.json');
const referenceAnimations = require('./benger_animation_spec.json');
const blockbench = read('Benger_Reference_Rework.bbmodel');
const allocations = read('uv-layout.json');
const cubes = listCubes(model);
assert.deepEqual(animations, referenceAnimations);
assert.equal(Object.keys(animations.animations).length, 9);
assert.equal(model.bones.length, 27);
assert.equal(cubes.length, parts.length);
assert.equal(blockbench.elements.length, cubes.length);
assert.equal(blockbench.animations.length, 9);
assert.equal(texture.w, model.textureWidth);
assert.equal(texture.h, model.textureHeight);
assert.deepEqual(Buffer.from(blockbench.textures[0].source.split(',')[1], 'base64'), fs.readFileSync(path.join(directory, 'benger.png')));
assert.deepEqual(model.bones.map(({ name, parent, pivot }) => ({ name, ...(parent ? { parent } : {}), pivot })), bones);

let opaqueSamples = 0;
const occupancy = new Uint8Array(texture.w * texture.h);
for (const allocation of allocations) {
    for (let row = -1; row <= allocation.size[1]; row++) {
        for (let column = -1; column <= allocation.size[0]; column++) {
            const horizontal = allocation.uv[0] + column;
            const vertical = allocation.uv[1] + row;
            assert(horizontal >= 0 && horizontal < texture.w && vertical >= 0 && vertical < texture.h);
            const pixel = vertical * texture.w + horizontal;
            assert.equal(occupancy[pixel], 0, `UV overlap: ${allocation.part}/${allocation.face}`);
            occupancy[pixel] = 1;
            assert.equal(texture.data[pixel * 4 + 3], 255, `Transparent UV: ${allocation.part}/${allocation.face}`);
            opaqueSamples++;
        }
    }
}

function bounds(points) {
    return {
        min: [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis]))),
        max: [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis])))
    };
}
const restBounds = bounds(cubes.flatMap(entry => cubeCorners(model, entry)));
assert.equal(restBounds.min[1], 0);
assert(restBounds.max[1] < 57);
assert(restBounds.max[0] - restBounds.min[0] <= 1.7 * 16);
const animationBounds = {};
const jointWarnings = [];
const connected = bones.filter(bone => bone.name.startsWith('shin_') || bone.name.startsWith('paw_') || /^tail[2-5]$/.test(bone.name));
let posesChecked = 0;
for (const [name, animation] of Object.entries(animations.animations)) {
    const sampledPoints = [];
    const frames = Math.ceil(animation.animation_length * 24);
    for (let frame = 0; frame <= frames; frame++) {
        const pose = samplePose(animation, frame / frames * animation.animation_length);
        const byBone = new Map();
        for (const entry of cubes) {
            const points = cubeCorners(model, entry, pose);
            assert(points.flat().every(Number.isFinite));
            sampledPoints.push(...points);
            byBone.set(entry.bone, [...(byBone.get(entry.bone) || []), ...points]);
        }
        for (const bone of connected) {
            const child = bounds(byBone.get(bone.name));
            const parent = bounds(byBone.get(bone.parent));
            const gap = Math.max(...child.min.map((value, axis) => Math.max(value - parent.max[axis], parent.min[axis] - child.max[axis])));
            if (gap > 0.5) jointWarnings.push({ name, frame, bone: bone.name, gap });
        }
        posesChecked++;
    }
    animationBounds[name] = bounds(sampledPoints);
}
assert.equal(jointWarnings.length, 0, JSON.stringify(jointWarnings.slice(0, 8)));
const report = {
    bones: bones.length, cubes: cubes.length, uvFaces: allocations.length, opaqueSamples,
    animations: Object.keys(animations.animations).length, posesChecked,
    animationValuesPreserved: true, jointAabbSeparationWarnings: jointWarnings,
    restBounds, animationBounds,
    sha256: Object.fromEntries(['benger.geo.json', 'benger.png', 'benger.animation.json'].map(name => [name,
        crypto.createHash('sha256').update(fs.readFileSync(path.join(directory, name))).digest('hex')])),
    limitations: ['Offline geometry/UV/animation checks, not an in-game acceptance test.',
        'Joint AABB contact is a coarse check, not a surface intersection proof.',
        'Four existing idle shin tracks contain a single constant key; animation values are preserved.']
};
fs.writeFileSync(path.join(directory, 'validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify({ bones: report.bones, cubes: report.cubes, uvFaces: report.uvFaces,
    opaqueSamples, posesChecked, animationValuesPreserved: true, jointAabbSeparationWarnings: jointWarnings.length, restBounds }, null, 2));
