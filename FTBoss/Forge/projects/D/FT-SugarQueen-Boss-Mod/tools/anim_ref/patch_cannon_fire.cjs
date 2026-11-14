/* Rebuild animation.compost_superman.cannon_fire using the keyframe language
   observed in FatalMortar ATTACK4 / HoveringHurricane tornadoShoot /
   IgnitedBerserker X_BLADE_SHOT:
     anticipation -> held aim -> per-shot snap recoil (decaying) ->
     damped overshoot settle -> offset follow-through on cloth bones.
   Shot times mirror the Java side: burstGap=16 ticks (0.80s), then 3 ticks apart.
   Usage: node patch_cannon_fire.cjs <path-to-animation.json> */
const fs = require('fs');
const file = process.argv[2];
const j = JSON.parse(fs.readFileSync(file, 'utf8'));

const SHOTS = [0.80, 0.95, 1.10, 1.25, 1.40];
const f = (n) => String(Math.round(n * 1000) / 1000);
// keys: { time: [x,y,z] }
const rot = (pairs) => { const o = {}; for (const p of pairs) o[f(p[0])] = p[1]; return o; };
const pos = rot;

/* per-shot kick: decay = [1.00, 0.86, 0.74, 0.62, 0.52] */
const DECAY = [1.00, 0.86, 0.74, 0.62, 0.52];

function burstPairs(aim, kick, settle, gap) {
  const out = [];
  for (let i = 0; i < SHOTS.length; i++) {
    const s = SHOTS[i], d = DECAY[i];
    out.push([s, aim.map((a, k) => a + kick[k] * d)]);
    out.push([s + gap, aim.map((a, k) => a + settle[k] * d)]);
  }
  return out;
}
function lin(a, b, t) { return a.map((v, k) => v + (b[k] - v) * t); }

const bones = {};

/* --- cannon: muzzle. negative x = raised --- */
bones.cannon = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.10, [-6, 0, 0]],
    [0.30, [-18, 0, 0]],
    [0.55, [-24, 0, 0]],
    [0.72, [-23, 0, 0]],
    ...burstPairs([-21, 0, 0], [-15, 0, 0], [2, 0, 0], 0.07),
    [1.52, [4, 0, 0]],
    [1.57, [-1.5, 0, 0]],
    [1.60, [0, 0, 0]],
  ]),
  position: pos([
    [0.00, [0, 0, 0]],
    [0.30, [0, 0.5, 0.5]],
    [0.55, [0, 0.6, 0.75]],
    [0.72, [0, 0.6, 0.70]],
    ...burstPairs([0, 0.60, 0.68], [0, 0.02, -0.48], [0, -0.02, 0.10], 0.07),
    [1.52, [0, 0.25, 0.30]],
    [1.57, [0, 0.05, 0.05]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- arm_r: holds the cannon, absorbs recoil --- */
bones.arm_r = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.10, [-18, 0, 4]],
    [0.30, [-55, 0, 11]],
    [0.55, [-68, 0, 14]],
    [0.72, [-70, 0, 14]],
    ...burstPairs([-70, 0, 14], [12, 0, -1], [-2, 0, 0.5], 0.07),
    [1.53, [10, 0, -3]],
    [1.57, [-3, 0, 1]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- arm_l: support hand braces the barrel (mirrored z) --- */
bones.arm_l = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.30, [-16, 0, -10]],
    [0.55, [-24, 0, -16]],
    [0.72, [-25, 0, -16]],
    ...burstPairs([-25, 0, -16], [5, 0, 1.5], [-1, 0, -0.5], 0.07),
    [1.53, [6, 0, 4]],
    [1.57, [-2, 0, -1]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- torso: lean back to aim, snap forward on each shot --- */
bones.torso = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.15, [-3, 0, -1]],
    [0.35, [-8, 0, -2.5]],
    [0.55, [-9, 0, -3]],
    [0.72, [-8.5, 0, -2.8]],
    ...burstPairs([-8, 0, -2.6], [11, 0, 4], [0, 0, 0], 0.07),
    [1.52, [2.2, 0, 0.6]],
    [1.56, [-0.8, 0, -0.3]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- head: sights the target, whips opposite on release --- */
bones.head = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.20, [-6, 0, 0]],
    [0.45, [-11, 0, 0]],
    [0.72, [-10, 0, 0]],
    ...burstPairs([-3.5, 0, 0], [16, 0, 0], [0, 0, 0], 0.07),
    [1.53, [3, 0, 0]],
    [1.57, [-1, 0, 0]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- root: body weight, dips on recoil --- */
bones.root = {
  position: pos([
    [0.00, [0, 0, 0]],
    [0.20, [0, -0.12, 0]],
    [0.45, [0, 0.18, 0.10]],
    [0.72, [0, 0.22, 0.12]],
    ...burstPairs([0, 0.20, 0.12], [0, -0.18, 0.18], [0, -0.02, -0.02], 0.07),
    [1.52, [0, -0.06, 0.02]],
    [1.56, [0, 0.03, 0]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- legs: planting / bracing against the burst --- */
bones.leg_l = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.35, [-6, 0, -3]],
    [0.72, [-8, 0, -4]],
    ...burstPairs([-8, 0, -4], [4, 0, 0.4], [-0.5, 0, -0.2], 0.07),
    [1.54, [4, 0, 1.5]],
    [1.58, [-1, 0, -0.5]],
    [1.60, [0, 0, 0]],
  ]),
};
bones.leg_r = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.35, [-6, 0, 3]],
    [0.72, [-8, 0, 4]],
    ...burstPairs([-8, 0, 4], [4, 0, -0.4], [-0.5, 0, 0.2], 0.07),
    [1.54, [4, 0, -1.5]],
    [1.58, [-1, 0, 0.5]],
    [1.60, [0, 0, 0]],
  ]),
};

/* --- cape1..4: offset follow-through (each lags the previous one more) --- */
const CAPE = [
  { lag: 0.80, up: 7, kick: -8, side: 3, amp: 1.00, tail: 5 },
  { lag: 0.90, up: 9, kick: -11, side: 4, amp: 1.25, tail: 4 },
  { lag: 0.98, up: 11, kick: -13, side: 5, amp: 1.55, tail: 6 },
  { lag: 1.06, up: 13, kick: -15, side: 6, amp: 1.85, tail: 7 },
];
CAPE.forEach((c, i) => {
  const pairs = [
    [0.00, [0, 0, 0]],
    [0.45 + i * 0.06, [c.up * 0.75, 0, -c.side * 0.75]],
    [0.72 + i * 0.04, [c.up * 0.85, 0, -c.side * 0.85]],
  ];
  for (let s = 0; s < SHOTS.length; s++) {
    const d = DECAY[s];
    const t0 = SHOTS[s] + 0.02 + i * 0.015;      // cloth reacts just after the body
    pairs.push([t0, [c.kick * d * c.amp * 0.35, 0, c.side * d * c.amp * 0.35]]);
    pairs.push([t0 + 0.07, [c.kick * d * c.amp * 0.75, 0, c.side * d * c.amp * 0.75]]);
  }
  pairs.push([1.55 + i * 0.012, [c.tail, 0, -c.tail * 0.35]]);   // counter-swing overshoot
  pairs.push([1.60, [0, 0, 0]]);
  bones['cape' + (i + 1)] = { rotation: rot(pairs) };
});

/* --- fly1/fly2: rotating hoops take up the shake with a lag --- */
bones.fly1 = {
  position: pos([
    [0.00, [-10, 33.4, -7.4]],
    [0.45, [-13, 34.4, -9.5]],
    [0.72, [-12, 34.2, -9.2]],
    [0.88, [-4.5, 32.6, -5.2]],
    [1.05, [-7, 33.0, -6.0]],
    [1.22, [-8.5, 33.2, -6.6]],
    [1.40, [-9.5, 33.4, -7.0]],
    [1.52, [-7.5, 33.0, -6.6]],
    [1.58, [-11, 33.6, -7.8]],
    [1.60, [-10, 33.4, -7.4]],
  ]),
};
bones.fly2 = {
  position: pos([
    [0.00, [10.4, 31.2, -7.8]],
    [0.45, [13.4, 32.2, -9.9]],
    [0.72, [12.4, 32.0, -9.6]],
    [0.92, [5, 30.4, -5.6]],
    [1.09, [7.5, 30.8, -6.4]],
    [1.26, [9, 31.0, -7.0]],
    [1.44, [10, 31.2, -7.4]],
    [1.55, [8, 30.8, -7.0]],
    [1.60, [10.4, 31.2, -7.8]],
  ]),
};

j.animations['animation.compost_superman.cannon_fire'] = {
  loop: false,
  animation_length: 1.6,
  bones,
};

fs.writeFileSync(file, JSON.stringify(j, null, 2) + '\n');
const a = j.animations['animation.compost_superman.cannon_fire'];
let n = 0;
for (const b in a.bones) for (const c in a.bones[b]) n += Object.keys(a.bones[b][c]).length;
console.log('cannon_fire rebuilt: bones=' + Object.keys(a.bones).length + ' keys=' + n);
