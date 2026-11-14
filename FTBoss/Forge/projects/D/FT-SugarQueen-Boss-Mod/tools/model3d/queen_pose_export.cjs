'use strict';

const fs = require('fs');
const path = require('path');
const { loadGeo, listCubes, cubeCorners } = require('./cube_space.cjs');
const { geckoModel, geckoPose, samplePose, blendPoses } = require('./queen_gecko_pose.cjs');
const directory = path.resolve(process.argv[2]);
const model = geckoModel(loadGeo(path.join(directory, 'sugar_queen_v3.geo.json')));
const animations = JSON.parse(fs.readFileSync(path.join(directory, 'sugar_queen_v3.animation.json'), 'utf8')).animations;
const cubes = listCubes(model);
function exportPose(rawPose) {
    const pose = geckoPose(rawPose);
    return cubes.map(cube => ({ bone: cube.bone, name: cube.name, corners: cubeCorners(model, cube, pose), uv: cube.cube.uv }));
}
const poses = {};
for (const [name, animation, time] of [['idle', 'idle', 0], ['walk', 'walk', 0.3], ['cast', 'scepter_raise', 0.8], ['slam', 'scepter_slam', 0.38], ['hurt', 'hurt', 0.1], ['death', 'death', 1.8]]) {
    poses[name] = exportPose(samplePose(animations['animation.king.' + animation], time));
}
for (const time of [0, 0.24, 0.5, 0.8, 1.6]) poses['raise_' + time] = exportPose(samplePose(animations['animation.king.scepter_raise'], time));
poses.cast_hold = exportPose(samplePose(animations.long_cast, animations.long_cast.animation_length / 2));
poses.cast_return = exportPose(blendPoses(samplePose(animations.long_cast, animations.long_cast.animation_length / 2), samplePose(animations['animation.king.idle'], 0), 0.5));
const bounds = {};
for (const [name, entries] of Object.entries(poses)) {
    const points = entries.flatMap(entry => entry.corners);
    bounds[name] = { min: [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis]))), max: [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis]))) };
}
fs.writeFileSync(path.join(directory, 'render-poses.json'), JSON.stringify(poses));
fs.writeFileSync(path.join(directory, 'pose-bounds.json'), JSON.stringify(bounds, null, 2) + '\n');
console.log(JSON.stringify(bounds, null, 2));
