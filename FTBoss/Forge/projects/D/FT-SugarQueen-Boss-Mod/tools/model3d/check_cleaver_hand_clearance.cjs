'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const hands = JSON.parse(fs.readFileSync(path.join(OUT,'hand-validation.json'),'utf8'));
const bounds = cube => [0,1,2].map(axis => [Math.min(...cube.corners.map(point => point[axis])),Math.max(...cube.corners.map(point => point[axis]))]);
let pairs = 0;
let poses = 0;
for (const sample of hands.filter(entry => entry.scene)) {
  for (const pose of ['closed','blood_enter:1.4']) {
    poses++;
    for (const body of sample.scene.body.filter(cube => cube.part === '/head' || cube.part === '/body')) {
      for (const cube of sample.scene[pose]) {
        const bodyBounds = bounds(body);
        const itemBounds = bounds(cube);
        const overlap = bodyBounds.map((range,axis) => Math.min(range[1],itemBounds[axis][1])-Math.max(range[0],itemBounds[axis][0]));
        pairs++;
        assert.ok(overlap.some(value => value <= 0.05),JSON.stringify({ model: sample.model,hand: sample.hand,pose,
          part: body.part,bone: cube.bone,index: cube.index,overlap }));
      }
    }
  }
}
assert.equal(poses,8);
const result = { status: 'PASS',neutral_poses: poses,conservative_aabb_pairs: pairs,
  maximum_grip_target_error_model_units: Math.max(...hands.map(sample => sample.palm_error_model_units)),
  scope: 'Native classic/slim player neutral ITEM arm pose, both hands, closed/blood-open head and torso clearance',
  dynamic_character_collision_tested: false,game_tested: false };
fs.writeFileSync(path.join(OUT,'hand-clearance-validation.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result,null,2));
