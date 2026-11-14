const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const assert = require('assert/strict');
const output = path.resolve(process.argv[2]);
const root = path.resolve(__dirname, '../..');
const run = (script, args) => {
  const result = spawnSync(process.execPath, [path.join(__dirname, script), ...args], { encoding: 'utf8' });
  process.stdout.write(result.stdout || '');
  assert.equal(result.status, 0, result.stderr);
};
run('gen_butcher.cjs', [output]);
assert.equal(fs.readFileSync(path.join(output, 'ButcherBossModel.java'), 'utf8'),
  fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/client/model/ButcherBossModel.java'), 'utf8'));
const clamp = value => Math.max(0, Math.min(1, value));
for (const [name, age] of [['windup', 6], ['impact', 8], ['recovery', 14]]) {
  const windup = clamp(age / 6);
  const strike = clamp((age - 6) / 2);
  const recovery = 1 - clamp((age - 9) / 9);
  const rotations = {
    arm_r: [(-1.65 * windup + 1.8 * strike) * recovery, -0.25 * windup * (1 - strike) * recovery, 0.07],
    arm_l: [-0.3 * windup * recovery, 0, -0.07],
    body: [0.1 + 0.14 * strike * recovery, 0.18 * (windup - 2 * strike) * recovery, 0]
  };
  const geo = JSON.parse(fs.readFileSync(path.join(output, 'butcher_preview.geo.json'), 'utf8'));
  const walk = bones => {
    for (const bone of bones) {
      if (rotations[bone.name]) {
        const rotation = rotations[bone.name];
        bone.rotation = [-rotation[0], rotation[1], -rotation[2]].map(value => value * 180 / Math.PI);
      }
      walk(bone.children || []);
    }
  };
  walk(geo['minecraft:geometry'][0].bones);
  const geometryPath = path.join(output, `butcher-${name}.geo.json`);
  fs.writeFileSync(geometryPath, JSON.stringify(geo, null, 2) + '\n');
  run('render_tex.cjs', [geometryPath, path.join(output, 'butcher_v2.png'),
    path.join(output, `butcher-${name}.png`), '1.5', '2.6']);
}
