const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const space = require('./cube_space.cjs');

function gripCenter(model) {
  const grip = model.elements.find(element => element.name === '深紫握柄');
  assert(grip, 'Missing grip geometry');
  return grip.from.map((value, axis) => (value + grip.to[axis]) / 2);
}

function itemPoint(point, display, leftHand) {
  const side = leftHand ? -1 : 1;
  let result = space.mul(space.sub(point, [8, 8, 8]), display.scale);
  for (const axis of [2, 1, 0]) {
    const rotation = [0, 0, 0];
    rotation[axis] = display.rotation[axis] * (axis === 0 ? 1 : side);
    result = space.rotateXYZ(result, rotation, [0, 0, 0]);
  }
  return space.add(result, [side * display.translation[0], display.translation[1], display.translation[2]]);
}

function armPoint(point, display, leftHand, slim = false) {
  const side = leftHand ? -1 : 1;
  let result = space.add(itemPoint(point, display, leftHand), [side, 2, -10]);
  result = space.rotateXYZ(result, [0, 180, 0], [0, 0, 0]);
  result = space.rotateXYZ(result, [-90, 0, 0], [0, 0, 0]);
  if (slim) result[0] += side * 0.5;
  return result;
}

function audit(model) {
  const center = gripCenter(model);
  return [false, true].flatMap(leftHand => {
    const slot = leftHand ? 'thirdperson_lefthand' : 'thirdperson_righthand';
    const display = model.display[slot];
    const untranslated = itemPoint(center, { ...display, translation: [0, 0, 0] }, leftHand);
    const suggestedTranslation = space.sub([0, -2, 2], untranslated).map((value, axis) => Number((value * (leftHand && axis === 0 ? -1 : 1)).toFixed(4)));
    return [false, true].map(slim => {
      const handCenter = [(leftHand ? 1 : -1) * (slim ? 0.5 : 1), 8, 0];
      const gripInArm = armPoint(center, display, leftHand, slim);
      const distance = Math.hypot(...space.sub(gripInArm, handCenter));
      return { slot, slim, modelGrip: center, armGrip: gripInArm.map(value => Number(value.toFixed(5))), handCenter, errorModelUnits: Number(distance.toFixed(6)), suggestedTranslation };
    });
  });
}

if (require.main === module) {
  const modelPath = process.argv[2] || path.resolve(__dirname, '../../src/main/resources/assets/ftboss/models/item/puffer_bat.json');
  const result = audit(JSON.parse(fs.readFileSync(modelPath, 'utf8')));
  console.log(JSON.stringify(result, null, 2));
  if (!process.argv.includes('--allow-offset')) assert(result.every(entry => entry.errorModelUnits < 0.001), 'Grip is outside hand-center tolerance');
}

module.exports = { gripCenter, itemPoint, armPoint, audit };
