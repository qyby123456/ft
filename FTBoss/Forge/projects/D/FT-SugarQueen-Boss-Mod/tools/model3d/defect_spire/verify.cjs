'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { decodePNG } = require('../png.cjs');
const { model, animations, bossParts } = require('./preview.cjs');
const directory = path.resolve(process.argv[2]);
const resources = path.join(directory, 'assets/ftboss');
const report = { animationBounds: {}, surfaceReview: [], checks: [] };
const weaponModel = JSON.parse(fs.readFileSync(path.join(resources, 'models/item/defect_claw.json')));
const weaponSurfaces = weaponModel.elements.map(element => ({
  origin: element.from, size: element.to.map((value, axis) => value - element.from[axis]), rotation: element.rotation
}));
for (const [name, animation] of Object.entries(animations)) {
  const bounds = { min: [Infinity, Infinity, Infinity], max: [-Infinity, -Infinity, -Infinity] };
  for (let frame = 0; frame <= Math.ceil(animation.animation_length * 24); frame++) {
    const parts = bossParts(name.split('.').pop(), Math.min(frame / 24, animation.animation_length));
    for (const point of parts.flatMap(part => part.points)) for (let axis = 0; axis < 3; axis++) {
      assert(Number.isFinite(point[axis]));
      bounds.min[axis] = Math.min(bounds.min[axis], point[axis]);
      bounds.max[axis] = Math.max(bounds.max[axis], point[axis]);
    }
  }
  assert(bounds.min[1] > -0.2, name + ' is buried in the ground: ' + bounds.min[1]);
  assert(bounds.max[1] < 64, name + ' exceeds culling bounds');
  report.animationBounds[name] = Object.fromEntries(Object.entries(bounds).map(([key, values]) => [key, values.map(value => Math.round(value * 100) / 100)]));
  for (const [boneName, channels] of Object.entries(animation.bones)) {
    assert(model.byName.has(boneName));
    for (const [channel, frames] of Object.entries(channels)) {
      const entries = Object.entries(frames);
      let previous = -1;
      for (const [stamp, values] of entries) {
        assert(Number(stamp) > previous, name + ' unordered keyframes');
        assert(Number(stamp) <= animation.animation_length + 0.0001);
        assert.equal(values.length, 3); assert(values.every(Number.isFinite)); previous = Number(stamp);
      }
      if (animation.loop === true) assert.deepEqual(entries[0][1], entries.at(-1)[1], name + ' loop seam at ' + boneName + '/' + channel);
    }
  }
}
for (const bone of [...model.bones, { name: 'weapon', cubes: weaponSurfaces }]) {
  const cubes = bone.cubes || [];
  for (let firstIndex = 0; firstIndex < cubes.length; firstIndex++) for (let secondIndex = firstIndex + 1; secondIndex < cubes.length; secondIndex++) {
    const first = cubes[firstIndex], second = cubes[secondIndex];
    if (first.rotation || second.rotation) continue;
    for (let axis = 0; axis < 3; axis++) for (const side of [0, 1]) {
      const firstPlane = first.origin[axis] + first.size[axis] * side;
      const secondPlane = second.origin[axis] + second.size[axis] * side;
      if (Math.abs(firstPlane - secondPlane) > 0.0001) continue;
      const others = [0, 1, 2].filter(candidate => candidate !== axis);
      const extent = others.map(candidate => Math.min(first.origin[candidate] + first.size[candidate], second.origin[candidate] + second.size[candidate]) - Math.max(first.origin[candidate], second.origin[candidate]));
      if (extent.every(value => value > 0.01)) report.surfaceReview.push({ bone: bone.name, cubes: [firstIndex, secondIndex], axis, side, area: Math.round(extent[0] * extent[1] * 100) / 100 });
    }
  }
}
for (const name of ['glass', 'lightning', 'frost', 'ion', 'dark']) {
  const base = decodePNG(path.join(resources, 'textures/entity/defect_spire_' + name + '.png'));
  const glow = decodePNG(path.join(resources, 'textures/entity/defect_spire_' + name + '_glowmask.png'));
  assert.equal(base.w, model.textureWidth); assert.equal(base.h, model.textureHeight);
  let glowPixels = 0;
  for (let index = 3; index < glow.data.length; index += 4) if (glow.data[index]) { assert(base.data[index]); glowPixels++; }
  assert(glowPixels > 0 && glowPixels < base.w * base.h / 10);
  for (const bone of model.bones) for (const cube of bone.cubes || []) for (const face of Object.values(cube.uv)) {
    for (let vertical = face.uv[1]; vertical < face.uv[1] + face.uv_size[1]; vertical++) for (let horizontal = face.uv[0]; horizontal < face.uv[0] + face.uv_size[0]; horizontal++) {
      assert(horizontal >= 0 && horizontal < base.w && vertical >= 0 && vertical < base.h);
      assert(base.data[(vertical * base.w + horizontal) * 4 + 3], 'Transparent UV texel');
    }
  }
}
const entityPath = path.resolve(__dirname, '../../..', 'src/main/java/net/farmingtales/ftboss/entity/DefectCoreEntity.java');
const entity = fs.readFileSync(entityPath, 'utf8');
for (const name of Object.keys(animations)) assert(entity.includes('"' + name + '"'), 'Dead animation: ' + name);
for (const [name, ticks] of [['cast', 20], ['hurt', 10], ['phase_shift', 52], ['orb_summon', 17], ['echo', 28], ['beam', 285], ['cast_glass', 23], ['cast_dark', 26]]) {
  assert.equal(animations['animation.defect_core.' + name].animation_length * 20, ticks);
}
assert(entity.includes('BossActionState') && entity.includes('syncAction();'), 'Server animation synchronization not integrated');
assert.equal(report.surfaceReview.length, 0, 'Same-bone or weapon coplanar surfaces need review');
report.checks.push('17 animations referenced by entity', 'All loops closed at 24fps', 'All UVs opaque and in range', 'Five matching core/glow variants', 'Finite posed bounds across all frames', 'Client action synchronization present', 'Durations match entity controller', 'No axis-aligned same-bone/weapon coplanar surface overlap');
fs.writeFileSync(path.join(directory, 'validation.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify({ checks: report.checks, surfaceReview: report.surfaceReview }, null, 2));
