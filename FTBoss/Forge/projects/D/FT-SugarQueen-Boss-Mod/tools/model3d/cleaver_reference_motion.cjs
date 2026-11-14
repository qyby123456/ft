'use strict';

const { geckoRotation } = require('./cube_space.cjs');
const AUTHOR_WINGS = { blade_upper_wing: -1, blade_lower_wing: 1, blade_right_upper_wing: 1, blade_right_lower_wing: -1 };
const WINGS = Object.fromEntries(Object.entries(AUTHOR_WINGS).map(([name,sign]) => [name,-sign]));
const FPS = 30;
const PREFIX = 'animation.black_cleaver.';
const CLIPS = {
  idle: { duration: 3, loop: true },
  charge: { duration: 0.4 },
  slam: { duration: 0.65 },
  recover: { duration: 0.3 },
  ready: { duration: 0.45 },
  blood_pose: { duration: 1.4 },
  blood_enter: { duration: 1.4 },
  blood_idle: { duration: 2.4, loop: true },
  blood_slam: { duration: 0.65 },
  blood_recover: { duration: 0.3 },
  blood_exit: { duration: 1.4 },
  blood_showcase: { duration: 9.75, loop: true }
};
const SHOWCASE = [
  { clip: null, duration: 0.6, label: 'NORMAL / CLOSED' },
  { clip: 'blood_enter', duration: 1.4, label: 'AWAKEN / UNFOLD' },
  { clip: 'blood_idle', duration: 2.4, label: 'BLOOD RAGE / DOUBLE HEARTBEAT' },
  { clip: 'blood_slam', duration: 0.65, label: 'HEAVY STRIKE / RECOIL' },
  { clip: 'blood_recover', duration: 0.3, label: 'RECOVER / SETTLE' },
  { clip: 'blood_idle', duration: 2.4, label: 'BLOOD RAGE / SUSTAIN' },
  { clip: 'blood_exit', duration: 1.4, label: 'COOL DOWN / CLOSE' },
  { clip: null, duration: 0.6, label: 'NORMAL / CLOSED' }
];
const clamp = value => Math.max(0, Math.min(1, value));
const smooth = value => { const amount = clamp(value); return amount * amount * (3 - 2 * amount); };
const round = value => Number(value.toFixed(6));
const keyTime = value => value.toFixed(6);

function curve(keys, time) {
  if (time <= keys[0][0]) return keys[0][1];
  for (let index = 1; index < keys.length; index++) {
    if (time > keys[index][0]) continue;
    const [start, from] = keys[index - 1];
    const [end, to] = keys[index];
    const amount = smooth((time - start) / (end - start));
    return Array.isArray(from) ? from.map((value, axis) => value + (to[axis] - value) * amount) : from + (to - from) * amount;
  }
  return keys[keys.length - 1][1];
}

function heartbeat(time) {
  const beat = (center, width) => Math.abs(time - center) < width ? Math.cos((time - center) / width * Math.PI / 2) ** 2 : 0;
  return beat(0.30, 0.20) + 0.62 * beat(0.69, 0.18);
}

function showcaseAt(time) {
  let start = 0;
  for (const [index, segment] of SHOWCASE.entries()) {
    if (time < start + segment.duration - 1e-8 || index === SHOWCASE.length - 1) {
      return { ...segment, start, time: Math.max(0, Math.min(segment.duration, time - start)) };
    }
    start += segment.duration;
  }
}

function closedPose() {
  return { weapon: { rotation: [0, 0, 0] }, ember_core: { rotation: [0, 0, 0], scale: [1, 1, 1] },
    ...Object.fromEntries(Object.keys(WINGS).map(name => [name, { rotation: [0, 0, 0] }])) };
}

function poseAt(name, time) {
  if (name === 'blood_showcase') {
    const segment = showcaseAt(time);
    return segment.clip ? poseAt(segment.clip, segment.time) : closedPose();
  }
  const pose = closedPose();
  let core = 1;
  let twist = 0;
  if (name === 'idle') core = 1 + 0.008 * (1 - Math.cos(time / 3 * Math.PI * 2));
  if (name === 'charge') {
    pose.weapon.rotation = curve([[0,[0,0,0]],[0.18,[-2,0,-1.5]],[0.4,[0,0,0]]],time);
    core = curve([[0,1],[0.16,0.94],[0.3,1.08],[0.4,1]],time);
  }
  if (name === 'ready') core = curve([[0,1],[0.10,0.97],[0.22,1.09],[0.45,1]],time);
  if (name === 'slam' || name === 'blood_slam') {
    const blood = name === 'blood_slam';
    pose.weapon.rotation = curve(blood ? [
      [0,[0,0,0]],[0.12,[-9,2,-5]],[0.28,[-26,6,-16]],
      [0.30,[-26,6,-16]],[0.38,[34,-3,9]],[0.42,[38,-3,10]],[0.49,[28,-1,6]],[0.65,[12,0,3]]
    ] : [
      [0,[0,0,0]],[0.13,[-7,0,-3]],[0.28,[-22,2,-10]],
      [0.30,[-22,2,-10]],[0.38,[28,-2,7]],[0.43,[32,-2,8]],[0.65,[12,0,3]]
    ],time);
    core = curve([[0,1],[0.26,blood ? 0.93 : 0.97],[0.38,blood ? 1.13 : 1.06],[0.46,1.03],[0.65,1]],time);
  }
  if (name === 'recover' || name === 'blood_recover') {
    pose.weapon.rotation = curve([[0,[12,0,3]],[0.23,[-0.9,0,-0.25]],[0.3,[0,0,0]]],time);
  }
  if (name === 'blood_enter') {
    core = curve([[0,1],[0.16,0.94],[0.38,1.10],[0.76,1.025],[1.4,1]],time);
    twist = curve([[0,0],[0.16,-3],[0.38,4],[0.9,0.6],[1.4,0]],time);
    pose.weapon.rotation = curve([[0,[0,0,0]],[0.32,[-0.8,0,-0.8]],[0.85,[0,0,0]]],time);
  }
  if (name === 'blood_idle') {
    core = 1 + 0.055 * heartbeat(time);
    twist = 1.8 * heartbeat(time);
  }
  if (name === 'blood_exit') {
    core = curve([[0,1],[0.7,0.97],[1.2,0.99],[1.4,1]],time);
    twist = curve([[0,0],[0.6,-1.5],[1.4,0]],time);
  }
  pose.ember_core = { scale: [core,core,1], rotation: [0,0,twist] };
  if (name.startsWith('blood_')) {
    const windows = [[0.16,1.12],[0.34,1.28],[0.25,1.20],[0.43,1.36]];
    for (const [index,[boneName,sign]] of Object.entries(AUTHOR_WINGS).entries()) {
      let angle = 11;
      if (name === 'blood_pose') angle = 11 * clamp(time / 1.4);
      if (name === 'blood_enter' || name === 'blood_exit') {
        const unfolded = name === 'blood_exit' ? 1.4 - time : time;
        const [start,end] = windows[index];
        angle = 11 * smooth((unfolded - start) / (end - start));
      }
      if (name === 'blood_idle') angle = 11 - 0.48 * heartbeat(Math.max(0,time - index * 0.025));
      if (name === 'blood_slam') angle = curve([[0,11],[0.27,8.6],[0.38,11],[0.47,10.35],[0.65,11]],time);
      pose[boneName] = { rotation: [0,0,sign * angle] };
    }
  } else {
    for (const boneName of Object.keys(WINGS)) delete pose[boneName];
    if (name === 'idle' || name === 'ready') delete pose.weapon;
  }
  return pose;
}

function effectsAt(name, time) {
  const resting = { core: 0.08, rune: 0, edge: 0, seam: 0 };
  if (name === 'blood_showcase') {
    const segment = showcaseAt(time);
    return segment.clip ? effectsAt(segment.clip,segment.time) : resting;
  }
  if (name === 'blood_enter' || name === 'blood_pose') {
    return {
      core: curve([[0,0.08],[0.18,0.38],[0.38,1.0],[0.82,0.60],[1.4,0.48]],time),
      seam: curve([[0,0],[0.18,0.65],[0.64,0.82],[1.4,0.55]],time),
      rune: curve([[0,0],[0.32,0],[0.85,0.58],[1.4,0.42]],time),
      edge: curve([[0,0],[0.60,0],[1.10,0.50],[1.4,0.28]],time)
    };
  }
  if (name === 'blood_exit') {
    return { core: curve([[0,0.48],[0.8,0.20],[1.4,0.08]],time),
      rune: curve([[0,0.42],[0.85,0]],time), edge: curve([[0,0.28],[0.6,0]],time),
      seam: curve([[0,0.55],[0.75,0.38],[1.30,0.04],[1.4,0]],time) };
  }
  if (name === 'blood_idle') {
    const pulse = heartbeat(time);
    return { core: 0.48 + 0.48*pulse, rune: 0.42 + 0.30*pulse, edge: 0.28 + 0.21*pulse, seam: 0.55 + 0.30*pulse };
  }
  if (name === 'blood_slam') {
    const energy = curve([[0,0],[0.26,-0.14],[0.38,0.55],[0.48,0.10],[0.65,0]],time);
    return { core: 0.48+energy, rune: 0.42+energy, edge: 0.28+energy, seam: 0.55+energy };
  }
  if (name === 'blood_recover') return { core: 0.48, rune: 0.42, edge: 0.28, seam: 0.55 };
  return resting;
}

function sampleTimes(duration) {
  const times = Array.from({ length: Math.floor(duration * FPS) + 1 }, (_,index) => index / FPS);
  times.push(duration);
  for (const time of [0.10,0.12,0.13,0.16,0.18,0.22,0.23,0.25,0.26,0.27,0.28,0.30,0.32,0.34,0.38,0.42,0.43,0.45,0.46,0.47,0.49,0.65]) {
    if (time < duration) times.push(time);
  }
  if (duration === CLIPS.blood_showcase.duration) {
    let start = 0;
    for (const segment of SHOWCASE) {
      for (const local of sampleTimes(segment.duration)) times.push(start + local);
      start += segment.duration;
    }
  }
  return [...new Set(times.map(round))].sort((left,right) => left-right);
}

function buildAnimations() {
  return Object.fromEntries(Object.entries(CLIPS).map(([name,definition]) => {
    const bones = {};
    for (const time of sampleTimes(definition.duration)) {
      for (const [boneName,channels] of Object.entries(poseAt(name,time))) {
        bones[boneName] ||= {};
        for (const [channel,values] of Object.entries(channels)) {
          bones[boneName][channel] ||= {};
          bones[boneName][channel][keyTime(time)] = (channel === 'rotation' ? geckoRotation(values) : values).map(round);
        }
      }
    }
    return [PREFIX+name,{ loop: !!definition.loop, animation_length: definition.duration, bones }];
  }));
}

function buildEffects() {
  return { format_version: '1.0', time_unit: 'seconds', integration: 'custom emissive-layer curves; not a GeckoLib animation file',
    textures: { core: 'black_cleaver_core_emissive.png', rune: 'black_cleaver_rune_emissive.png',
      edge: 'black_cleaver_edge_emissive.png', seam: 'black_cleaver_blood_seam_emissive.png' },
    clips: Object.fromEntries(Object.entries(CLIPS).map(([name,definition]) => {
      const tracks = Object.fromEntries(['core','rune','edge','seam'].map(channel => [channel,{}]));
      for (const time of sampleTimes(definition.duration)) {
        for (const [channel,value] of Object.entries(effectsAt(name,time))) tracks[channel][keyTime(time)] = round(value);
      }
      return [PREFIX+name,{ duration: definition.duration, loop: !!definition.loop, tracks }];
    })),
    showcase: SHOWCASE, strike_contact_seconds: 0.38 };
}

function sampleTrack(track, time) {
  if (Array.isArray(track)) return [...track];
  const keys = Object.keys(track).map(Number).sort((left,right) => left-right);
  const at = target => track[Object.keys(track).find(key => Number(key) === target)];
  if (time <= keys[0]) return at(keys[0]);
  for (let index = 1; index < keys.length; index++) {
    if (time > keys[index]) continue;
    const from = at(keys[index-1]);
    const to = at(keys[index]);
    const amount = (time-keys[index-1])/(keys[index]-keys[index-1]);
    return Array.isArray(from) ? from.map((value,axis) => value+(to[axis]-value)*amount) : from+(to-from)*amount;
  }
  return at(keys[keys.length-1]);
}

function sampleAnimation(animation, time) {
  return Object.fromEntries(Object.entries(animation.bones).map(([name,channels]) => [name,
    Object.fromEntries(Object.entries(channels).map(([channel,track]) => [channel,sampleTrack(track,time)]))]));
}

function sampleEffects(clip, time) {
  return Object.fromEntries(Object.entries(clip.tracks).map(([channel,track]) => [channel,sampleTrack(track,time)]));
}

module.exports = { WINGS, FPS, PREFIX, CLIPS, SHOWCASE, buildAnimations, buildEffects, sampleAnimation, sampleEffects, sampleTimes, showcaseAt };
