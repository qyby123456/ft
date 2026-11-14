/* Rebuild animation.compost_superman.gas_charge:
   long swell/tremble build-up -> counter-pose hitch -> BOOM snap (extreme held)
   -> damped overshoot settle, with cloth follow-through.
   The release key sits at t=2.0s so it lines up with gasFuse=40 ticks in Java.
   Usage: node patch_gas_charge.cjs <path-to-animation.json> */
const fs = require('fs');
const file = process.argv[2];
const j = JSON.parse(fs.readFileSync(file, 'utf8'));
const f = (n) => String(Math.round(n * 1000) / 1000);
const rot = (pairs) => { const o = {}; for (const p of pairs) o[f(p[0])] = p[1]; return o; };

const bones = {};

bones.root = {
  position: rot([
    [0.00, [0, 0, 0]],
    [0.30, [0, -0.25, 0]],
    [0.70, [0, -0.75, 0]],
    [1.00, [0.04, -1.00, 0.00]],
    [1.15, [-0.05, -1.02, 0.04]],
    [1.30, [0.05, -1.05, -0.04]],
    [1.45, [-0.04, -1.08, 0.03]],
    [1.60, [0.03, -1.10, -0.02]],
    [1.75, [-0.03, -1.18, 0.02]],
    [1.85, [0, -1.25, 0]],
    [1.94, [0, -1.05, 0]],
    [2.00, [0, 0.65, 0]],
    [2.10, [0, 0.80, 0]],
    [2.30, [0, -0.12, 0]],
    [2.46, [0, 0.05, 0]],
    [2.60, [0, 0, 0]],
  ]),
};

bones.torso = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.40, [6, 0, 0]],
    [0.80, [10, 0, 0]],
    [1.35, [12, 0, 0]],
    [1.60, [12.5, 0, 0]],
    [1.85, [14, 0, 0]],
    [1.94, [11, 0, 0]],
    [2.00, [-14, 0, 0]],
    [2.10, [-17, 0, 0]],
    [2.28, [4, 0, 0]],
    [2.44, [-1.5, 0, 0]],
    [2.60, [0, 0, 0]],
  ]),
  scale: rot([
    [0.00, [1, 1, 1]],
    [0.70, [1.06, 1.04, 1.06]],
    [1.35, [1.12, 1.08, 1.12]],
    [1.60, [1.16, 1.10, 1.16]],
    [1.85, [1.24, 1.16, 1.24]],
    [1.96, [1.30, 1.20, 1.30]],
    [2.00, [1.02, 1.00, 1.02]],
    [2.12, [1.06, 1.02, 1.06]],
    [2.32, [0.97, 0.98, 0.97]],
    [2.48, [1.005, 1.00, 1.005]],
    [2.60, [1, 1, 1]],
  ]),
};

bones.head = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [0.45, [10, 0, 0]],
    [1.35, [15, 0, 0]],
    [1.60, [16, 0, 0]],
    [1.85, [19, 0, 0]],
    [1.94, [14, 0, 0]],
    [2.00, [-16, 0, 0]],
    [2.12, [-20, 0, 0]],
    [2.30, [5, 0, 0]],
    [2.46, [-2, 0, 0]],
    [2.60, [0, 0, 0]],
  ]),
};

const ARM = [
  [0.00, [0, 0, 0]],
  [0.60, [10, 0, 8]],
  [1.30, [18, 0, 14]],
  [1.60, [22, 0, 17]],
  [1.85, [26, 0, 21]],
  [1.94, [20, 0, 16]],
  [2.00, [-22, 0, -14]],
  [2.10, [-26, 0, -17]],
  [2.28, [7, 0, 5]],
  [2.46, [-2, 0, -1.5]],
  [2.60, [0, 0, 0]],
];
const mirrorZ = (pairs) => pairs.map((p) => [p[0], [p[1][0], p[1][1], -p[1][2]]]);
bones.arm_l = { rotation: rot(ARM) };
bones.arm_r = { rotation: rot(mirrorZ(ARM)) };

bones.cannon = {
  rotation: rot([
    [0.00, [0, 0, 0]],
    [1.20, [22, 0, 0]],
    [1.60, [25, 0, 0]],
    [1.85, [28, 0, 0]],
    [1.94, [24, 0, 0]],
    [2.00, [-13, 0, 0]],
    [2.10, [-16, 0, 0]],
    [2.28, [5, 0, 0]],
    [2.46, [-1.5, 0, 0]],
    [2.60, [0, 0, 0]],
  ]),
};

/* cloth: swirl inward while charging, whip outward on the boom, settle with overshoot */
const CAPE = [
  { a: -6, b: -18, c: -22, whip: 12, reb: -5, set: 2, t0: 0.60, t1: 1.55, t2: 1.92, t3: 2.08 },
  { a: -9, b: -26, c: -31, whip: 17, reb: -7, set: 3, t0: 0.70, t1: 1.60, t2: 1.94, t3: 2.11 },
  { a: -12, b: -34, c: -40, whip: 22, reb: -9, set: 4, t0: 0.80, t1: 1.65, t2: 1.96, t3: 2.14 },
  { a: -15, b: -42, c: -49, whip: 27, reb: -11, set: 5, t0: 0.90, t1: 1.70, t2: 1.98, t3: 2.17 },
];
CAPE.forEach((c, i) => {
  bones['cape' + (i + 1)] = {
    rotation: rot([
      [0.00, [0, 0, 0]],
      [c.t0, [0, 0, c.a]],
      [c.t1, [0, 0, c.b]],
      [c.t2, [0, 0, c.c]],
      [c.t3, [0, 0, c.whip]],
      [c.t3 + 0.18, [0, 0, c.reb]],
      [c.t3 + 0.35, [0, 0, c.set]],
      [2.60, [0, 0, 0]],
    ]),
  };
});

const LEG = [
  [0.00, [0, 0, 0]],
  [0.70, [-5, 0, -2]],
  [1.60, [-10, 0, -4]],
  [1.90, [-12, 0, -4.5]],
  [2.00, [7, 0, 3]],
  [2.12, [9, 0, 3.5]],
  [2.30, [-2, 0, -1]],
  [2.46, [0.5, 0, 0.5]],
  [2.60, [0, 0, 0]],
];
bones.leg_l = { rotation: rot(LEG) };
bones.leg_r = { rotation: rot(mirrorZ(LEG)) };

j.animations['animation.compost_superman.gas_charge'] = {
  loop: false,
  animation_length: 2.6,
  bones,
};

fs.writeFileSync(file, JSON.stringify(j, null, 2) + '\n');
const a = j.animations['animation.compost_superman.gas_charge'];
let n = 0;
for (const b in a.bones) for (const c in a.bones[b]) n += Object.keys(a.bones[b][c]).length;
console.log('gas_charge rebuilt: bones=' + Object.keys(a.bones).length + ' keys=' + n);
