'use strict';

const rounded = value => Math.round(value * 10000) / 10000;
const mix = (first, second, amount) => first.map((value, axis) => value + (second[axis] - value) * amount);
const rotation = (pitch = 0, yaw = 0, roll = 0) => ({ rotation: [pitch, yaw, roll] });

function resting() {
  const pose = {
    root: { position: [0, 0.5, 0] }, pelvis: rotation(0, 0, -1), waist: rotation(-2), torso: rotation(-3),
    neck: rotation(3), head: rotation(-2, -6, -4), jaw: rotation(), core: rotation(),
    arm_l: rotation(9, 0, 9), arm_r: rotation(15, 0, -10),
    forearm_l: rotation(16), forearm_r: rotation(24), hand_l: rotation(-8), hand_r: rotation(-12),
    leg_l: rotation(4, 0, 2), leg_r: rotation(-4, 0, -2), shin_l: rotation(-9), shin_r: rotation(-5),
    foot_l: rotation(5), foot_r: rotation(9), thumb_l: rotation(0, -12, -12), thumb_r: rotation(0, 12, 12)
  };
  for (let segment = 1; segment <= 4; segment++) pose['cape_back' + segment] = rotation(-3 - segment);
  for (const [side, sign] of [['l', 1], ['r', -1]]) for (let digit = 1; digit <= 3; digit++) {
    pose['claw_' + side + digit] = rotation(8 + digit * 2, 0, sign * (digit - 2) * 4);
  }
  return pose;
}

function merge(...poses) {
  const result = {};
  for (const pose of poses) for (const [name, tracks] of Object.entries(pose)) result[name] = { ...result[name], ...tracks };
  return result;
}

function build(boneNames) {
  const animations = {};
  function bake(name, length, loop, samples) {
    const bones = {};
    for (const boneName of boneNames) {
      const channels = new Set(samples.flatMap(sample => Object.keys(sample.pose[boneName] || {})));
      if (!channels.size) continue;
      bones[boneName] = {};
      for (const channel of channels) {
        const frames = {};
        for (const sample of samples) {
          const vector = sample.pose[boneName]?.[channel] || (channel === 'scale' ? [1, 1, 1] : [0, 0, 0]);
          frames[sample.time.toFixed(4)] = vector.map((value, axis) => rounded(channel === 'rotation' && axis !== 1 ? -value : value));
        }
        bones[boneName][channel] = frames;
      }
    }
    animations['animation.defect_core.' + name] = { loop, animation_length: length, bones };
  }
  function loop(name, length, transform) {
    const samples = [];
    const steps = Math.round(length * 24);
    for (let frame = 0; frame <= steps; frame++) {
      const time = frame * length / steps;
      samples.push({ time, pose: transform(frame === steps ? 0 : time, length) });
    }
    bake(name, length, true, samples);
  }
  function motion(name, length, keyPoses, hold = false) {
    const points = keyPoses.map(([time, pose]) => ({ time, pose: merge(resting(), pose) }));
    const times = new Set(points.map(point => point.time));
    for (let frame = 0; frame <= Math.ceil(length * 24); frame++) times.add(Math.min(length, frame / 24));
    const samples = [...times].sort((left, right) => left - right).map(time => {
      let left = points[0], right = points[points.length - 1];
      for (let index = 1; index < points.length; index++) if (time <= points[index].time) { left = points[index - 1]; right = points[index]; break; }
      const progress = Math.max(0, Math.min(1, (time - left.time) / Math.max(0.00001, right.time - left.time)));
      const smooth = progress * progress * (3 - 2 * progress);
      const pose = {};
      for (const boneName of new Set([...Object.keys(left.pose), ...Object.keys(right.pose)])) {
        pose[boneName] = {};
        for (const channel of new Set([...Object.keys(left.pose[boneName] || {}), ...Object.keys(right.pose[boneName] || {})])) {
          const fallback = channel === 'scale' ? [1, 1, 1] : [0, 0, 0];
          pose[boneName][channel] = mix(left.pose[boneName]?.[channel] || fallback, right.pose[boneName]?.[channel] || fallback, smooth);
        }
      }
      return { time, pose };
    });
    bake(name, length, hold ? 'hold_on_last_frame' : false, samples);
  }
  function hover(time, length, moving = false) {
    const phase = time / length * Math.PI * 2;
    const pose = merge(resting(), {
      root: { position: [0, 0.5 + Math.sin(phase) * 0.5, 0], rotation: [moving ? -9 : 0, 0, Math.sin(phase) * 1.5] },
      torso: rotation(-3 + Math.sin(phase + 0.4) * 1.4, Math.sin(phase) * 2),
      head: rotation(-2 + Math.sin(phase - 0.6) * 2, -6 + Math.sin(phase) * 4, -4),
      arm_l: rotation(moving ? -12 : 9 + Math.sin(phase + 0.4) * 4, 0, 10),
      arm_r: rotation(moving ? -18 : 15 + Math.sin(phase - 0.4) * 4, 0, -10)
    });
    if (moving) Object.assign(pose, { leg_l: rotation(-17), leg_r: rotation(-26), shin_l: rotation(-30), shin_r: rotation(-23), foot_l: rotation(18), foot_r: rotation(25) });
    for (let segment = 1; segment <= 4; segment++) pose['cape_back' + segment] = rotation((moving ? -10 : -4) + Math.sin(phase - segment * 0.6) * (1.5 + segment), 0, Math.sin(phase - segment * 0.4) * 1.1);
    for (const side of ['l', 'r']) for (let digit = 1; digit <= 3; digit++) pose['claw_' + side + digit].rotation[0] += Math.sin(phase - digit * 0.4) * 4;
    return pose;
  }
  loop('idle', 3.2, (time, length) => hover(time, length));
  loop('float_move', 1.6, (time, length) => hover(time, length, true));
  for (const [name, sign] of [['turn_l', 1], ['turn_r', -1]]) loop(name, 1.2, (time, length) => merge(hover(time, length), {
    root: { position: [0, 0.6, 0], rotation: [0, 0, sign * 5] }, torso: rotation(-3, sign * 10), head: rotation(-2, sign * 18, -sign * 4),
    arm_l: rotation(20, sign * 8, 15), arm_r: rotation(4, sign * 8, -15), cape_back1: rotation(-8, -sign * 10)
  }));
  const palms = (left, right, leftElbow, rightElbow, spread = 14) => ({
    arm_l: rotation(left, 0, spread), arm_r: rotation(right, 0, -spread),
    forearm_l: rotation(leftElbow), forearm_r: rotation(rightElbow),
    hand_l: rotation(-8, -8), hand_r: rotation(-8, 8),
    fingers_l: rotation(-12), fingers_r: rotation(-12)
  });
  const glassCharge = palms(35, 45, 60, 65, 18);
  const glassRelease = palms(78, 83, 10, 8, 13);
  motion('cast_glass', 1.15, [[0, {}], [0.32, glassCharge], [0.55, merge(glassRelease, { torso: rotation(-6), head: rotation(-5) })], [0.73, glassRelease], [1.15, {}]]);
  const lightningCharge = palms(36, 128, 25, 28, 22);
  const lightningRelease = palms(42, 82, 27, 8, 18);
  motion('cast_lightning', 1, [[0, {}], [0.25, lightningCharge], [0.45, merge(lightningRelease, { torso: rotation(-5, -8), head: rotation(-7, -9) })], [0.62, lightningRelease], [1, {}]]);
  const frostCharge = palms(45, 45, 50, 50, 12);
  const frostRelease = palms(42, 42, 15, 15, 60);
  motion('cast_frost', 1, [[0, {}], [0.16, frostCharge], [0.3, merge(frostRelease, { hand_l: rotation(5, -38), hand_r: rotation(5, 38) })], [0.63, frostRelease], [1, {}]]);
  const ionCharge = palms(78, 18, 30, 40, 25);
  const ionRelease = palms(84, 65, 4, 15, 20);
  motion('cast_ion', 1, [[0, {}], [0.16, ionCharge], [0.3, merge(ionRelease, { waist: rotation(-2, 8), torso: rotation(-5, 5) })], [0.5, ionRelease], [1, {}]]);
  const darkCharge = merge(palms(30, 30, 80, 80, 20), { head: rotation(-10), waist: rotation(-5) });
  const darkRelease = merge(palms(85, 85, 4, 4, 12), { torso: rotation(-6), root: { position: [0, 0.5, 0.7] } });
  motion('cast_dark', 1.3, [[0, {}], [0.38, darkCharge], [0.68, darkCharge], [0.8, darkRelease], [1.05, darkRelease], [1.3, {}]]);
  animations['animation.defect_core.cast'] = structuredClone(animations['animation.defect_core.cast_ion']);
  motion('orb_summon', 0.85, [[0, {}], [0.32, palms(82, 65, 55, 45, 40)], [0.53, palms(110, 92, 8, 16, 38)], [0.85, {}]]);
  motion('phase_shift', 2.6, [[0, {}], [0.55, merge(darkCharge, { root: { position: [0, 0, 0] } })],
    [1.2, merge(palms(12, 12, 15, 15, 68), { root: { position: [0, 2, 0] }, head: rotation(10), cape_back1: rotation(-22) })],
    [1.8, merge(palms(20, 20, 22, 22, 50), { root: { position: [0, 1.8, 0] } })], [2.6, {}]]);
  motion('echo', 1.4, [[0, {}], [0.3, merge(darkCharge, { head: rotation(-6, -14) })],
    [0.6, merge(palms(65, 65, 40, 40, 44), { head: rotation(-3, 12), core: { scale: [1.12, 1.12, 1] } })],
    [0.8, merge(palms(68, 62, 40, 40, 44), { head: rotation(-3, -10) })], [1.4, {}]]);
  motion('hurt', 0.5, [[0, {}], [0.1, { torso: rotation(6, 0, -5), head: rotation(12, 0, 7), arm_l: rotation(24, 0, 18), cape_back1: rotation(-14) }], [0.5, {}]]);
  const beamCharge = merge(palms(38, 38, 68, 68, 22), { head: rotation(-9), cape_back1: rotation(-14) });
  const beamRelease = merge(palms(84, 84, 4, 4, 10), { torso: rotation(-6), head: rotation(-8), cape_back1: rotation(-20) });
  motion('beam', 14.25, [[0, {}], [0.7, beamCharge], [7.7, beamCharge], [8, beamRelease], [8.15, merge(beamRelease, { root: { position: [0, 0.6, 0.8] } })], [10.85, beamRelease], [14.25, beamRelease]]);
  loop('exhausted', 1.6, (time, length) => {
    const breath = Math.sin(time / length * Math.PI * 2);
    return merge(resting(), { root: { position: [0, -0.3, 0] }, waist: rotation(-7), torso: rotation(-9 + breath), head: rotation(-12),
      arm_l: rotation(0, 0, 10), arm_r: rotation(0, 0, -10), forearm_l: rotation(5), forearm_r: rotation(5),
      leg_l: rotation(), leg_r: rotation(), shin_l: rotation(), shin_r: rotation(), foot_l: rotation(), foot_r: rotation() });
  });
  motion('death_overload', 1, [[0, {}], [0.16, { torso: rotation(-10), head: rotation(-12), core: { scale: [1.25, 1.25, 1.1] } }],
    [0.48, merge(palms(10, 10, 10, 10, 48), { root: { position: [0, 0.5, 0] }, waist: rotation(-8), torso: rotation(-12) })],
    [0.68, { root: { position: [0, 0.5, 0] }, leg_l: rotation(45, 0, 8), leg_r: rotation(45, 0, -8), shin_l: rotation(-75), shin_r: rotation(-75),
      waist: rotation(-11), torso: rotation(-18), head: rotation(-15), arm_l: rotation(30, 0, 28), arm_r: rotation(30, 0, -28) }],
    [0.9, { root: { position: [0, -5.5, 0] }, leg_l: rotation(80, 0, 12), leg_r: rotation(80, 0, -12), shin_l: rotation(-135), shin_r: rotation(-135),
      waist: rotation(-15), torso: rotation(-24), head: rotation(-20), arm_l: rotation(38, 0, 20), arm_r: rotation(38, 0, -20), cape_back1: rotation(-20), core: { scale: [0.6, 0.6, 0.6] } }],
    [1, { root: { position: [0, -5.5, 0] }, leg_l: rotation(80, 0, 12), leg_r: rotation(80, 0, -12), shin_l: rotation(-135), shin_r: rotation(-135),
      waist: rotation(-15), torso: rotation(-24), head: rotation(-20), arm_l: rotation(38, 0, 20), arm_r: rotation(38, 0, -20), cape_back1: rotation(-20), core: { scale: [0.6, 0.6, 0.6] } }]], true);
  return { format_version: '1.8.0', animations };
}

module.exports = { build };
