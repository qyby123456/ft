#!/usr/bin/env node
'use strict';

// Builds dedicated signature-spell animations for the seven regular bosses.
// Every key pose is sampled from an existing, already validated animation of the same
// skeleton. This keeps pivots, handedness and body clearance data-driven instead of
// inventing transforms by visual guesswork.
const fs = require('node:fs');
const path = require('node:path');
const { planReorders, serializeOrdered } = require('./anim_key_order.cjs');
const root = path.resolve(__dirname, '../..');
const animDir = path.join(root, 'src/main/resources/assets/ftboss/animations');
const geoDir = path.join(root, 'src/main/resources/assets/ftboss/geo');
const OUT_TIMES = ['0.0000', '0.3000', '0.7200', '1.1000', '1.5000'];
const LENGTH = 1.5;

const specs = [
  {
    file: 'sugar_queen_v3', prefix: 'animation.king',
    attack: [['animation.king.idle', 0], ['animation.king.scepter_raise', 0.5], ['animation.king.scepter_raise', 1.6], ['instant_projectile', 0.15], ['animation.king.idle', 0]],
    support: [['animation.king.idle', 0], ['animation.king.scepter_raise', 0.42], ['self_cast_two_hands', 0.4], ['instant_self', 0.25], ['animation.king.idle', 0]]
  },
  {
    file: 'wine_barrel_boss', prefix: 'animation.barrel_boss',
    attack: [['animation.barrel_boss.idle', 0], ['animation.barrel_boss.windup', 0.5], ['charged_throw', 0.9], ['charge_spit_finish', 0.15], ['animation.barrel_boss.idle', 0]],
    support: [['animation.barrel_boss.idle', 0], ['spell_cast', 0.3], ['self_cast_two_hands', 0.4], ['self_cast_two_hands', 1.0], ['animation.barrel_boss.idle', 0]]
  },
  {
    file: 'defect_core', prefix: 'animation.defect_core',
    attack: [['animation.defect_core.idle', 0], ['animation.defect_core.orb_summon', 0.3], ['animation.defect_core.cast_ion', 0.5], ['animation.defect_core.cast_lightning', 0.72], ['animation.defect_core.idle', 0]],
    support: [['animation.defect_core.idle', 0], ['animation.defect_core.orb_summon', 0.32], ['animation.defect_core.phase_shift', 0.8], ['animation.defect_core.cast_frost', 0.55], ['animation.defect_core.idle', 0]]
  },
  {
    file: 'compost_superman', prefix: 'animation.compost_superman',
    attack: [['animation.compost_superman.idle', 0], ['animation.compost_superman.phase_shift', 0.3], ['animation.compost_superman.gas_charge', 1.0], ['animation.compost_superman.cannon_fire', 1.2], ['animation.compost_superman.idle', 0]],
    support: [['animation.compost_superman.idle', 0], ['animation.compost_superman.transform', 0.4], ['animation.compost_superman.gas_charge', 1.3], ['animation.compost_superman.phase_shift', 1.2], ['animation.compost_superman.idle', 0]]
  },
  {
    file: 'apple_boss', prefix: 'animation.apple_boss',
    attack: [['animation.apple_boss.idle', 0], ['animation.apple_boss.summon', 0.5], ['animation.apple_boss.orbit_shot', 0.7], ['animation.apple_boss.seed_shot', 0.8], ['animation.apple_boss.idle', 0]],
    support: [['animation.apple_boss.idle', 0], ['animation.apple_boss.wax_shield', 0.4], ['animation.apple_boss.summon', 1.0], ['animation.apple_boss.wax_shield', 1.5], ['animation.apple_boss.idle', 0]]
  },
  {
    file: 'tide_fish_boss', prefix: 'animation.tide_fish',
    attack: [['animation.tide_fish.idle', 0], ['animation.tide_fish.summon', 0.3], ['animation.tide_fish.maelstrom', 0.8], ['animation.tide_fish.geyser', 1.0], ['animation.tide_fish.idle', 0]],
    support: [['animation.tide_fish.idle', 0], ['animation.tide_fish.summon', 0.25], ['animation.tide_fish.phase', 0.8], ['animation.tide_fish.summon', 1.0], ['animation.tide_fish.idle', 0]]
  }
];

function vec(value) {
  if (Array.isArray(value)) return value.map(Number);
  if (value && Array.isArray(value.vector)) return value.vector.map(Number);
  if (value && Array.isArray(value.post)) return value.post.map(Number);
  if (value && Array.isArray(value.pre)) return value.pre.map(Number);
  throw new Error('Unsupported keyframe value: ' + JSON.stringify(value));
}
function neutral(channel) { return channel === 'scale' ? [1, 1, 1] : [0, 0, 0]; }
function sampleTrack(track, time, channel) {
  if (Array.isArray(track) || (track && Array.isArray(track.vector))) return vec(track);
  const entries = Object.entries(track || {}).map(([stamp, value]) => [Number(stamp), vec(value)])
    .sort((a, b) => a[0] - b[0]);
  if (!entries.length) return neutral(channel);
  if (time <= entries[0][0]) return entries[0][1].slice();
  for (let i = 1; i < entries.length; i++) {
    const a = entries[i - 1], b = entries[i];
    if (time > b[0]) continue;
    const p = b[0] === a[0] ? 1 : (time - a[0]) / (b[0] - a[0]);
    return a[1].map((value, axis) => value + (b[1][axis] - value) * p);
  }
  return entries.at(-1)[1].slice();
}
function sampleAnimation(animation, time) {
  const pose = {};
  for (const [bone, tracks] of Object.entries(animation.bones || {})) {
    pose[bone] = {};
    for (const [channel, track] of Object.entries(tracks)) pose[bone][channel] = sampleTrack(track, time, channel);
  }
  return pose;
}
function clean(vector) {
  return vector.map(value => {
    const rounded = Math.round(value * 10000) / 10000;
    return Math.abs(rounded) < 0.00005 ? 0 : rounded;
  });
}
function differs(values, channel) {
  const baseline = neutral(channel);
  return values.some(value => value.some((n, axis) => Math.abs(n - baseline[axis]) > 0.0001))
    || values.some((value, index) => index > 0 && value.some((n, axis) => Math.abs(n - values[0][axis]) > 0.0001));
}
function compose(animations, recipe, validBones) {
  if (recipe.length !== OUT_TIMES.length) throw new Error('Recipe/key count mismatch');
  const poses = recipe.map(([name, time]) => {
    if (!animations[name]) throw new Error(`Missing source animation ${name}`);
    return sampleAnimation(animations[name], time);
  });
  const boneOrder = [...validBones].filter(bone => poses.some(pose => pose[bone]));
  const bones = {};
  for (const bone of boneOrder) {
    const channels = [...new Set(poses.flatMap(pose => Object.keys(pose[bone] || {})))];
    const tracks = {};
    for (const channel of channels) {
      const values = poses.map(pose => clean(pose[bone]?.[channel] || neutral(channel)));
      if (!differs(values, channel)) continue;
      tracks[channel] = Object.fromEntries(OUT_TIMES.map((stamp, index) => [stamp, values[index]]));
    }
    if (Object.keys(tracks).length) bones[bone] = tracks;
  }
  return { loop: false, animation_length: LENGTH, bones };
}

for (const spec of specs) {
  const animationPath = path.join(animDir, `${spec.file}.animation.json`);
  const geoPath = path.join(geoDir, `${spec.file}.geo.json`);
  const data = JSON.parse(fs.readFileSync(animationPath, 'utf8'));
  const geo = JSON.parse(fs.readFileSync(geoPath, 'utf8'))['minecraft:geometry'][0];
  const validBones = new Set((geo.bones || []).map(bone => bone.name));
  const attackName = `${spec.prefix}.signature_cast`;
  const supportName = `${spec.prefix}.signature_self_cast`;
  data.animations[attackName] = compose(data.animations, spec.attack, validBones);
  data.animations[supportName] = compose(data.animations, spec.support, validBones);
  // JSON.stringify cannot preserve a numeric timeline such as 0, 0.3, 1, 1.5:
  // JavaScript promotes integer-like keys ahead of decimal keys. Serialize every
  // animation track with an explicit numeric key order so GeckoLib receives the
  // intended chronology for both generated and pre-existing clips.
  const provisional = JSON.stringify(data, null, 2) + '\n';
  const orderFor = new Map();
  for (const entry of planReorders(data.animations, provisional)) {
    orderFor.set(`/animations/${entry.anim}/bones/${entry.bone}/${entry.track}`, entry.keys);
  }
  fs.writeFileSync(animationPath, serializeOrdered(data, orderFor), 'utf8');
  console.log(`${spec.file}: ${attackName} (${Object.keys(data.animations[attackName].bones).length} bones), ${supportName} (${Object.keys(data.animations[supportName].bones).length} bones)`);
}