'use strict';

const assert = require('assert/strict');
const fs = require('fs');
const path = require('path');
const { loadGeo, listCubes, cubeCorners } = require('./cube_space.cjs');
const { geckoModel, geckoPose, samplePose, blendPoses } = require('./queen_gecko_pose.cjs');
const directory = path.resolve(process.argv[2]);
const model = geckoModel(loadGeo(path.join(directory, 'sugar_queen_v3.geo.json')));
const animations = JSON.parse(fs.readFileSync(path.join(directory, 'sugar_queen_v3.animation.json'), 'utf8')).animations;
const parts = JSON.parse(fs.readFileSync(path.join(directory, 'manifest.json'), 'utf8')).parts;
const counts = {};
const cubes = listCubes(model).map(cube => {
    const index = counts[cube.bone] || 0;
    counts[cube.bone] = index + 1;
    return { ...cube, part: parts.filter(part => part.bone === cube.bone)[index].name };
});
const limbs = cubes.filter(cube => /^(arm_|forearm_|hand_|scepter)/.test(cube.bone));
const body = cubes.filter(cube => /^(waist|chest|head|curl_|crown|skirt|cape)/.test(cube.bone));
const subtract = (first, second) => first.map((value, axis) => value - second[axis]);
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
function box(cube, pose) {
    const corners = cubeCorners(model, cube, pose);
    const edges = [4, 2, 1].map(index => subtract(corners[index], corners[0]));
    const lengths = edges.map(edge => Math.hypot(...edge));
    return { cube, min: [0, 1, 2].map(axis => Math.min(...corners.map(point => point[axis]))),
        max: [0, 1, 2].map(axis => Math.max(...corners.map(point => point[axis]))),
        center: corners[0].map((value, axis) => (value + corners[7][axis]) / 2),
        half: lengths.map(length => length / 2), axes: edges.map((edge, index) => edge.map(value => value / lengths[index])) };
}
function penetration(first, second) {
    if (first.min.some((value, axis) => value >= second.max[axis] || second.min[axis] >= first.max[axis])) return 0;
    const displacement = subtract(second.center, first.center);
    const axes = [...first.axes, ...second.axes, ...first.axes.flatMap(axis => second.axes.map(other => cross(axis, other)))];
    let depth = Infinity;
    for (const direction of axes) {
        const length = Math.hypot(...direction);
        if (length < 1e-8) continue;
        const axis = direction.map(value => value / length);
        const extent = entry => entry.axes.reduce((sum, local, index) => sum + entry.half[index] * Math.abs(dot(axis, local)), 0);
        const overlap = extent(first) + extent(second) - Math.abs(dot(axis, displacement));
        if (overlap <= 0) return 0;
        depth = Math.min(depth, overlap);
    }
    return depth;
}
assert(penetration(box(limbs[0], {}), box(limbs[0], {})) > 0);
const findings = new Map();
const detached = new Set();
let samples = 0;
function check(label, rawPose) {
    samples++;
    const pose = geckoPose(rawPose);
    const limbBoxes = limbs.map(cube => box(cube, pose)), bodyBoxes = body.map(cube => box(cube, pose));
    for (const sleeve of limbBoxes.filter(entry => entry.cube.part === 'puff_sleeve')) {
        if (!bodyBoxes.some(target => target.cube.bone === 'chest' && penetration(sleeve, target) > 0)) detached.add(sleeve.cube.bone + '@' + label);
    }
    for (const limb of limbBoxes) for (const target of bodyBoxes) {
        if (limb.cube.part === 'puff_sleeve' && target.cube.bone === 'chest'
                && (target.cube.part === 'bodice' || target.cube.part === (limb.cube.bone === 'arm_l' ? 'ruff_-1' : 'ruff_1'))) continue;
        const depth = penetration(limb, target);
        if (depth < 0.02) continue;
        const key = `${limb.cube.bone}/${limb.cube.part} -> ${target.cube.bone}/${target.cube.part}`;
        if ((findings.get(key)?.depth || 0) < depth) findings.set(key, { pair: key, depth, sample: label });
    }
}
const names = Object.keys(animations).filter(name => name !== 'blank' && name !== 'animation.king.death');
for (const name of names) {
    const animation = animations[name];
    const frames = Math.ceil(animation.animation_length * 60);
    for (let frame = 0; frame <= frames; frame++) {
        const time = Math.min(frame / 60, animation.animation_length);
        check(`${name}@${time.toFixed(3)}`, samplePose(animation, time));
    }
}
const transitions = ['animation.king.idle', 'animation.king.walk', 'animation.king.hurt', 'animation.king.scepter_slam', 'animation.king.scepter_raise', 'long_cast'];
for (const sourceName of transitions) {
    for (const sourceFraction of [0, 0.25, 0.5, 0.75, 1]) {
        const source = samplePose(animations[sourceName], animations[sourceName].animation_length * sourceFraction);
        for (const targetName of transitions) {
            for (let frame = 0; frame <= 18; frame++) {
                const progress = frame / 18;
                const target = samplePose(animations[targetName], 0);
                check(`${sourceName}@${sourceFraction} -> ${targetName}@${progress.toFixed(2)}`, blendPoses(source, target, progress));
            }
        }
    }
}
const collisions = [...findings.values()].sort((first, second) => second.depth - first.depth);
const report = { status: collisions.length || detached.size ? 'FAIL' : 'PASS', samples, animations: names.length,
    basis: 'GeckoLib model-space OBB SAT, 60 FPS plus 6-tick controller blend samples',
    excluded: 'Puff sleeve vs bodice/its own shoulder ruff is the intentional attachment; death is outside the casting check.',
    collisions, detachedShoulders: [...detached].slice(0, 10) };
fs.writeFileSync(path.join(directory, 'arm-clearance.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify({ ...report, collisions: collisions.slice(0, 12) }, null, 2));
assert.equal(collisions.length, 0, 'Arm/body intersections; see arm-clearance.json');
assert.equal(detached.size, 0, 'Shoulder sleeves must remain attached to the chest/collar');
assert.deepEqual(samplePose(animations['animation.king.scepter_raise'], 1.6), samplePose(animations.long_cast, 0), 'Raise must join the casting loop without snapping');
assert.deepEqual(samplePose(animations.long_cast, 0), samplePose(animations.long_cast, animations.long_cast.animation_length), 'Casting loop endpoints must match');
