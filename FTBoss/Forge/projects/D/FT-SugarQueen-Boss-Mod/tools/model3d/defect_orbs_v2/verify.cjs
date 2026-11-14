'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { spawnSync } = require('node:child_process');
const { geometry, animations, types } = require('./build.cjs');
const { decodePNG } = require('../png.cjs');
const { loadGeo, listCubes, cubeCorners } = require('../cube_space.cjs');
const directory = path.resolve(process.argv[2]);
const project = path.resolve(__dirname, '../../..');
const report = { models: [], checks: [] };
const durations = { idle: 3.2, summon: 0.5, focus: 0.6, dash: 0.4, return: 0.8, beam: 0.8, drain: 0.3, burst: 0.3 };

for (const type of Object.keys(types)) {
  const filename = path.join(directory, 'assets/ftboss/geo/orb_link_' + type + '.geo.json');
  const model = loadGeo(filename);
  const generated = geometry(type);
  assert.deepEqual(model.json, generated.geometry);
  const base = decodePNG(path.join(directory, 'assets/ftboss/textures/entity/orb_link_' + type + '.png'));
  const glow = decodePNG(path.join(directory, 'assets/ftboss/textures/entity/orb_link_' + type + '_glowmask.png'));
  assert.equal(base.w, 256); assert.equal(base.h, 256);
  assert.equal(glow.w, 256); assert.equal(glow.h, 256);
  const pixels = base.data || base.px || base.rgba;
  const glowing = glow.data || glow.px || glow.rgba;
  assert(pixels && glowing);
  assert(glowing.some((value, index) => index % 4 === 3 && value > 0));
  const cubes = listCubes(model);
  for (const { cube, bone } of cubes) {
    assert(cube.size.every(value => value > 0), bone);
    assert.equal(Object.keys(cube.uv).length, 6);
    for (const face of Object.values(cube.uv)) {
      const [left, top] = face.uv, [width, height] = face.uv_size;
      assert(left >= 1 && top >= 1 && left + width < 256 && top + height < 256);
      for (let row = top; row < top + height; row++) for (let column = left; column < left + width; column++)
        assert.equal(pixels[(row * 256 + column) * 4 + 3], 255, type + ' invisible face');
    }
  }
  const clips = JSON.parse(fs.readFileSync(path.join(directory, 'assets/ftboss/animations/orb_link_' + type + '.animation.json')));
  assert.deepEqual(clips, animations(generated.bones, type));
  for (const [name, duration] of Object.entries(durations)) {
    const animation = clips.animations['animation.orb.' + name];
    assert.equal(animation.animation_length, duration);
    for (const [bone, channels] of Object.entries(animation.bones)) {
      assert(model.byName.has(bone));
      for (const [channel, frames] of Object.entries(channels)) {
        const entries = Object.entries(frames);
        for (let index = 1; index < entries.length; index++)
          assert(Number(entries[index][0]) > Number(entries[index - 1][0]), type + ':' + name + ' unordered GeckoLib time keys');
        assert.equal(Number(entries[0][0]), 0); assert.equal(Number(entries.at(-1)[0]), duration);
        for (const [stamp, values] of entries) {
          assert(Number(stamp) >= 0 && Number(stamp) <= duration);
          assert(values.length === 3 && values.every(Number.isFinite));
          if (channel === 'scale') assert(values.every(value => value >= 0.019 && value <= 1.25));
        }
        if (animation.loop === true) assert.deepEqual(entries[0][1], entries.at(-1)[1], type + ':' + name + ':' + bone + ':' + channel);
      }
    }
  }
  for (const bone of model.bones) {
    bone.rotation = bone.rotation.map((value, axis) => axis === 1 ? value : -value);
    for (const cube of bone.cubes) cube.rotation = cube.rotation.map((value, axis) => axis === 1 ? value : -value);
  }
  const points = cubes.flatMap(entry => cubeCorners(model, entry));
  const bounds = [0, 1, 2].map(axis => [Math.min(...points.map(point => point[axis])), Math.max(...points.map(point => point[axis]))]);
  for (const [min, max] of bounds) assert(max - min < 11, type + ' excessive size');
  report.models.push({ type, bones: model.bones.length, cubes: cubes.length, animations: 8, bounds });
}
report.checks.push('5 geometries / 40 animations / 10 textures', 'Per-face UV pixels opaque and in bounds', 'Loop endpoint continuity / positive scales / finite keys / compact bounds');
const javaRoot = path.join(project, 'src/main/java/net/farmingtales/ftboss');
const orb = fs.readFileSync(path.join(javaRoot, 'entity/ChargedOrbEntity.java'), 'utf8');
const controller = fs.readFileSync(path.join(javaRoot, 'boss/ai/DefectOrbs.java'), 'utf8');
assert.match(orb, /if \(this\.gone\) return;\s+String state/);
assert.match(orb, /!this\.maturedRequested/);
assert.match(orb, /if \(--this\.fadeTicks <= 0\) this\.discard\(\)/);
assert.match(orb, /this\.reservedForCast = true/);
assert.match(controller, /this\.castBacklog\.size\(\) >= SLOTS_MAX/);
assert.match(controller, /this\.castBacklog\.removeIf/);
assert.match(controller, /selected\.releaseDark\(c, this\.orbDamageMul/);
assert.match(controller, /orb\.isReservedForCast\(\) \|\| orb\.isSpentProjectile\(\)/);
const bossAnimations = JSON.parse(fs.readFileSync(path.join(project, 'src/main/resources/assets/ftboss/animations/defect_spire.animation.json'))).animations;
const dark = bossAnimations['animation.defect_core.cast_dark'];
assert.equal(dark.animation_length, 1.3);
assert(Object.keys(dark.bones.arm_r.rotation).some(stamp => Number(stamp) === 0.8));
report.checks.push('Reservation / bounded queue / destruction cancellation / same dark projectile / six-tick fade source contracts');
const testOutput = path.join(directory, 'java-check'); fs.mkdirSync(testOutput, { recursive: true });
function method(signature) {
  const start = controller.indexOf(signature);
  assert(start >= 0, signature);
  let balance = 1;
  const opening = controller.indexOf('{', start);
  let end = opening + 1;
  while (balance && end < controller.length) {
    if (controller[end] === '{') balance++;
    if (controller[end] === '}') balance--;
    end++;
  }
  assert.equal(balance, 0);
  return controller.slice(start, end);
}
const template = fs.readFileSync(path.join(__dirname, 'OrbCastQueueTest.java.template'), 'utf8');
const queueTest = path.join(testOutput, 'OrbCastQueueTest.java');
fs.writeFileSync(queueTest, template.replace('PRODUCTION_METHODS', [method('private void requestOrbCast('),
  method('public void onOrbDestroyed(ChargedOrbEntity orb)')].join('\n')));
const javaHome = process.env.JAVA_HOME || 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot';
function run(binary, argumentsList) {
  const result = spawnSync(path.join(javaHome, 'bin', binary + '.exe'), argumentsList, { encoding: 'utf8' });
  assert.equal(result.status, 0, result.stdout + result.stderr);
  process.stdout.write(result.stdout);
}
run('javac', ['-encoding', 'UTF-8', '-d', testOutput, path.join(javaRoot, 'util/OrbLinkMath.java'), path.join(__dirname, 'OrbLinkMathTest.java'), queueTest]);
run('java', ['-ea', '-cp', testOutput, 'OrbLinkMathTest']);
run('java', ['-ea', '-cp', testOutput, 'OrbCastQueueTest']);
report.checks.push('Compiled Java interpolation and release-timing assertions');
report.checks.push('Production queue method harness: 100 duplicate requests, 11-slot limit, destruction cancellation, ownership and spent filtering');
fs.writeFileSync(path.join(directory, 'verification.json'), JSON.stringify(report, null, 2) + '\n');
console.log(JSON.stringify(report, null, 2));
