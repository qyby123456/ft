'use strict';

const assert = require('assert/strict');
const fs = require('fs');
const { loadGeo, listCubes, cubeCorners, transformByBoneChain } = require('./cube_space.cjs');
const { geckoModel, geckoPose } = require('./queen_gecko_pose.cjs');

const model = geckoModel(loadGeo(process.argv[2]));
const animations = JSON.parse(fs.readFileSync(process.argv[3], 'utf8')).animations;
const cubes = listCubes(model);
const names = ['animation.king.scepter_raise', 'long_cast', 'spell_cast', 'long_cast_finish', 'instant_self', 'instant_projectile', 'charged_throw', 'charge_spit_finish', 'cast_t_pose', 'self_cast_two_hands', 'throw_item'];
const report = [];
for (const name of names) {
    const animation = animations[name];
    assert(animation, 'Missing spell animation ' + name);
    const directions = { l: { min: Infinity, max: -Infinity }, r: { min: Infinity, max: -Infinity } };
    const stamps = Object.keys(animation.bones.arm_l.rotation);
    for (const stamp of stamps) {
        const rawPose = Object.fromEntries(Object.entries(animation.bones).map(([owner, tracks]) => [owner, Object.fromEntries(Object.entries(tracks).map(([channel, track]) => [channel, track[stamp]]))]));
        const pose = geckoPose(rawPose);
        const origin = transformByBoneChain(model, 'chest', [0, 26, 0], pose);
        const front = transformByBoneChain(model, 'chest', [0, 26, -1], pose).map((value, axis) => value - origin[axis]);
        for (const side of ['l', 'r']) {
            const hand = cubes.find(cube => cube.bone === 'hand_' + side);
            const corners = cubeCorners(model, hand, pose);
            const center = [0, 1, 2].map(axis => corners.reduce((sum, point) => sum + point[axis], 0) / corners.length);
            const shoulder = transformByBoneChain(model, 'chest', model.byName.get('arm_' + side).pivot, pose);
            const forward = center.reduce((sum, value, axis) => sum + (value - shoulder[axis]) * front[axis], 0);
            directions[side].min = Math.min(directions[side].min, forward);
            directions[side].max = Math.max(directions[side].max, forward);
        }
    }
    const result = { animation: name, frames: stamps.length, leftHandForward: directions.l, rightHandForward: directions.r };
    report.push(result);
    assert(directions.l.min > 2 && directions.r.min > 1, name + ': hand is behind shoulder in GeckoLib space: ' + JSON.stringify(result));
}
console.log(JSON.stringify({ status: 'PASS', coordinateBasis: 'GeckoLib 4.8.4; model-space rotation [-x, y, -z]; forward = chest local -Z', animations: report }, null, 2));
