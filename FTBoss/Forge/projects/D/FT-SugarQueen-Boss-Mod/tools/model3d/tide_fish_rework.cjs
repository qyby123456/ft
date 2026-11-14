const fs = require('node:fs');
const path = require('node:path');

// 2026-09-17 第一轮：动画时长 -35%，关键帧同步服务端前摇（swing 落在伤害帧上）。
// 2026-09-17 第二轮（用户口径：前摇再短一半）：前摇 -50%，整段关键帧按同一差值前移，
// 动画总时长同步缩短，保证"动画起手 / 落点"仍然对齐服务端伤害帧。
//   durations = 与 util/TideFishCombatRules.java 完全一致的动画时长（检验脚本会逐项比对）
//   oldTicks  = 下面关键帧数字所在的旧时间轴；shifts = 该技能整体前移量
const durations = { sweep: [19, 29], volley: [22, 31], dash: [25, 44], summon: [25, 25],
  wave: [31, 47], maelstrom: [31, 41], geyser: [30, 41] };
const oldTicks = { sweep: [22, 32], volley: [26, 35], dash: [29, 48], summon: [31, 31],
  wave: [36, 52], maelstrom: [39, 49], geyser: [36, 47] };
const shifts = { sweep: 3, volley: 4, dash: 4, summon: 6, wave: 5, maelstrom: 8, geyser: 6 };
const zero = [0, 0, 0];
let shift = 0;
const frame = (tick, value) => [Math.max(0, tick - shift) / 20, value];

function track(frames, duration) {
  const sorted = frames.slice().sort((left, right) => left[0] - right[0]);
  const times = new Set(sorted.map(([time]) => time));
  for (let index = 0; index <= duration * 24; index++) times.add(Math.min(duration, index / 24));
  const result = {};
  for (const time of [...times].sort((left, right) => left - right)) {
    let upper = sorted.findIndex(([stamp]) => stamp >= time);
    if (upper < 0) upper = sorted.length - 1;
    const lower = Math.max(0, upper - 1);
    const span = sorted[upper][0] - sorted[lower][0];
    const fraction = span ? Math.max(0, Math.min(1, (time - sorted[lower][0]) / span)) : 0;
    const eased = fraction * fraction * (3 - 2 * fraction);
    result[time.toFixed(4)] = sorted[lower][1].map((value, axis) =>
      Number((value + (sorted[upper][1][axis] - value) * eased).toFixed(4)));
  }
  return result;
}

function buildAnimations() {
  const animations = {};
  function add(name, ticks, poses, loop = false) {
    const bones = {};
    for (const [bone, channels] of Object.entries(poses)) {
      bones[bone] = {};
      for (const [channel, frames] of Object.entries(channels)) bones[bone][channel] = track(frames, ticks / 20);
    }
    animations[`animation.tide_fish.${name}`] = { animation_length: ticks / 20, loop, bones };
  }
  const rotate = entries => ({ rotation: entries.map(([tick, value]) => frame(tick, value)) });
  add('idle', 60, {
    body: { position: [frame(0, zero), frame(30, [0, 0.35, 0]), frame(60, zero)] },
    head: rotate([[0, zero], [30, [-2, 0, 0]], [60, zero]]),
    jaw: rotate([[0, [2, 0, 0]], [30, [5, 0, 0]], [60, [2, 0, 0]]]),
    fin_l: rotate([[0, [0, 0, -5]], [30, [0, 3, 3]], [60, [0, 0, -5]]]),
    fin_r: rotate([[0, [0, 0, 5]], [30, [0, -3, -3]], [60, [0, 0, 5]]]),
    tail: rotate([[0, [0, -10, 0]], [30, [0, 10, 0]], [60, [0, -10, 0]]]),
    tail_tip: rotate([[0, [0, 7, 0]], [30, [0, -7, 0]], [60, [0, 7, 0]]]),
    dorsal: rotate([[0, zero], [30, [-3, 0, 0]], [60, zero]])
  }, true);
  add('walk', 24, {
    body: rotate([[0, [3, 0, -2]], [6, [4, 0, 0]], [12, [3, 0, 2]], [18, [4, 0, 0]], [24, [3, 0, -2]]]),
    leg_l: rotate([[0, [17, 0, 0]], [12, [-17, 0, 0]], [24, [17, 0, 0]]]),
    leg_r: rotate([[0, [-17, 0, 0]], [12, [17, 0, 0]], [24, [-17, 0, 0]]]),
    foot_l: rotate([[0, [-13, 0, 0]], [12, [13, 0, 0]], [24, [-13, 0, 0]]]),
    foot_r: rotate([[0, [13, 0, 0]], [12, [-13, 0, 0]], [24, [13, 0, 0]]]),
    fin_l: rotate([[0, [-10, 0, -5]], [12, [10, 0, -5]], [24, [-10, 0, -5]]]),
    fin_r: rotate([[0, [8, 0, 5]], [12, [-8, 0, 5]], [24, [8, 0, 5]]]),
    tail: rotate([[0, [0, -16, 0]], [12, [0, 16, 0]], [24, [0, -16, 0]]]),
    tail_tip: rotate([[0, [0, 10, 0]], [12, [0, -10, 0]], [24, [0, 10, 0]]])
  }, true);
  for (const [name, lengths] of Object.entries(durations)) {
    for (const phase of [0, 1]) {
      shift = shifts[name];
      const ticks = oldTicks[name][phase];
      const length = lengths[phase];
      const poses = {};
      const pose = (bone, points) => { poses[bone] = rotate([[0, zero], ...points, [ticks, zero]]); };
      if (name === 'sweep') {
        pose('body', [[4, [0, -24, 0]], [7, [0, 28, 0]], ...(phase ? [[15, [0, 30, 0]], [18, [0, -32, 0]]] : [])]);
        pose('tail', [[4, [0, -35, 0]], [7, [0, 48, 0]], ...(phase ? [[15, [0, 36, 0]], [18, [0, -52, 0]]] : [])]);
        pose('tail_tip', [[5, [0, -18, 0]], [8, [0, 24, 0]], ...(phase ? [[16, [0, 20, 0]], [19, [0, -26, 0]]] : [])]);
        pose('fin_r', [[4, [-12, 0, -30]], [7, [18, 0, -15]], ...(phase ? [[18, [-18, 0, -35]]] : [])]);
        pose('head', [[4, [0, 14, 0]], [7, [0, -16, 0]], ...(phase ? [[18, [0, 18, 0]]] : [])]);
      } else if (name === 'volley') {
        const releases = phase ? [9, 16, 23] : [9];
        pose('head', releases.flatMap(tick => [[tick - 3, [-10, 0, 0]], [tick, [12, 0, 0]], [tick + 3, zero]]));
        pose('jaw', releases.flatMap(tick => [[tick - 3, [28, 0, 0]], [tick, [10, 0, 0]], [tick + 3, zero]]));
        pose('body', [[7, [-5, 0, 0]], ...releases.map(tick => [tick, [5, 0, 0]])]);
        pose('fin_l', [[7, [0, 0, 34]], [ticks - 6, [0, 0, 22]]]);
        pose('fin_r', [[7, [0, 0, -26]], [ticks - 6, [0, 0, -16]]]);
      } else if (name === 'dash') {
        pose('body', [[7, [-8, 0, 0]], [9, [22, 0, 0]], [21, [22, 0, 0]],
          ...(phase ? [[33, [-12, 0, 0]], [35, [24, 0, 0]], [47, [24, 0, 0]]] : [])]);
        pose('fin_l', [[7, [0, 0, 20]], [9, [15, 0, 8]], [21, [15, 0, 8]]]);
        pose('fin_r', [[7, [-10, 0, -15]], [9, [-55, 0, -4]], [21, [-55, 0, -4]],
          ...(phase ? [[33, [-10, 0, -15]], [35, [-55, 0, -4]], [47, [-55, 0, -4]]] : [])]);
        pose('tail', [[7, [0, -12, 0]], [9, [0, 16, 0]], [15, [0, -16, 0]], [21, [0, 16, 0]]]);
        pose('leg_l', [[9, [15, 0, 0]], [15, [-15, 0, 0]], [21, [15, 0, 0]]]);
        pose('leg_r', [[9, [-15, 0, 0]], [15, [15, 0, 0]], [21, [-15, 0, 0]]]);
      } else {
        const releases = name === 'summon' ? [12] : name === 'wave' ? phase ? [11, 30] : [11]
          : name === 'maelstrom' ? phase ? [16, 23, 30] : [16] : phase ? [13, 18, 23, 28, 33] : [13, 18, 23];
        const lift = name === 'summon' ? 64 : name === 'wave' ? 42 : name === 'maelstrom' ? 76 : 32;
        pose('fin_l', [[releases[0] - 8, [-12, 0, lift]], ...releases.map(tick => [tick, [18, 0, lift * 0.5]])]);
        pose('fin_r', [[releases[0] - 8, [-25, 0, -lift]], ...releases.flatMap(tick =>
          [[tick - 3, [-36, 0, -lift]], [tick, [18, 0, -lift * 0.6]]])]);
        pose('body', [[releases[0] - 8, [-6, 0, 0]], ...releases.flatMap(tick => [[tick - 2, [-5, 0, 0]], [tick, [7, 0, 0]]])]);
        pose('head', [[releases[0] - 8, [-10, 0, 0]], [releases.at(-1), [6, 0, 0]]]);
        pose('jaw', [[releases[0] - 8, [18, 0, 0]], [releases.at(-1), [8, 0, 0]]]);
        pose('dorsal', [[releases[0] - 8, [-12, 0, 0]], [releases.at(-1), [8, 0, 0]]]);
        pose('tail_tip', [[releases[0] - 8, [0, phase ? 24 : 12, 0]], [releases.at(-1), [0, -18, 0]]]);
      }
      add(name + (phase ? '_p2' : ''), length, poses);
    }
  }
  shift = 0;
  add('phase', 40, {
    body: rotate([[0, zero], [12, [7, 0, 0]], [20, [-7, 0, 0]], [32, [-7, 0, 0]], [40, zero]]),
    fin_l: rotate([[0, zero], [18, [0, 0, 78]], [32, [0, 0, 78]], [40, zero]]),
    fin_r: rotate([[0, zero], [18, [0, 0, -70]], [32, [0, 0, -70]], [40, zero]]),
    jaw: rotate([[0, zero], [18, [30, 0, 0]], [32, [24, 0, 0]], [40, zero]]),
    dorsal: rotate([[0, zero], [20, [-16, 0, 0]], [40, zero]]),
    head: rotate([[0, zero], [18, [-16, 0, 0]], [32, [-16, 0, 0]], [40, zero]])
  });
  add('death', 20, { body: rotate([[0, zero], [20, [16, 0, -18]]]),
    head: rotate([[0, zero], [20, [18, 0, 0]]]), jaw: rotate([[0, zero], [20, [12, 0, 0]]]),
    fin_l: rotate([[0, zero], [20, [0, 0, 32]]]), fin_r: rotate([[0, zero], [20, [0, 0, -32]]]) }, 'hold_on_last_frame');
  return { format_version: '1.8.0', animations };
}

function enhance(assets) {
  const geoPath = path.join(assets, 'geo/tide_fish_boss.geo.json');
  const geometry = JSON.parse(fs.readFileSync(geoPath, 'utf8'));
  const model = geometry['minecraft:geometry'][0];
  if (model.bones.some(bone => bone.name === 'platter')) {
    model.bones = model.bones.filter(bone => bone.name !== 'platter');
    for (const bone of model.bones) {
      if (bone.name !== 'root') bone.pivot[1] = Number((bone.pivot[1] - 3.7).toFixed(4));
      for (const cube of bone.cubes || []) cube.origin[1] = Number((cube.origin[1] - 3.7).toFixed(4));
    }
    const head = model.bones.find(bone => bone.name === 'head');
    const jawCubes = head.cubes.filter(cube => Math.abs(cube.origin[0]) <= 8 && cube.origin[1] < 35.3);
    head.cubes = head.cubes.filter(cube => !jawCubes.includes(cube));
    model.bones.splice(model.bones.indexOf(head) + 1, 0, { name: 'jaw', parent: 'head', pivot: [0, 35.3, -8], cubes: jawCubes });
    const tail = model.bones.find(bone => bone.name === 'tail');
    model.bones.splice(model.bones.indexOf(tail) + 1, 0, { name: 'tail_tip', parent: 'tail', pivot: [0, 13.3, 12], cubes: tail.cubes.splice(1) });
  }
  fs.writeFileSync(geoPath, JSON.stringify(geometry, null, 2) + '\n');
  fs.writeFileSync(path.join(assets, 'animations/tide_fish_boss.animation.json'), JSON.stringify(buildAnimations(), null, 2) + '\n');
  console.log('Tide fish: removed platter, grounded feet, articulated jaw/tail, 18 animation clips.');
}

module.exports = { enhance, buildAnimations, durations, oldTicks, shifts };
if (require.main === module) enhance(path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss')));
