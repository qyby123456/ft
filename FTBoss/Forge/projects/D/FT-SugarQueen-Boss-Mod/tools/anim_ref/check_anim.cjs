/* Validate a GeckoLib animation JSON against its geometry:
   - every bone exists in the geo
   - times ascending, finite, within animation_length
   - no null / NaN values
   Usage: node check_anim.cjs <animation.json> <geo.json> */
const fs = require('fs');
const anim = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const geo = JSON.parse(fs.readFileSync(process.argv[3], 'utf8'));

const bones = new Set();
const geoDef = geo['minecraft:geometry'][0];
(function walk(list, parent) {
  for (const b of list || []) {
    bones.add(b.name);
    if (b.name !== parent) (b.children || []).length; // no-op, keep shape explicit
    walk(b.children, b.name);
  }
})(geoDef.bones, null);
// GeckoLib also allows referencing cubes' parent groups; report the set size for sanity
let problems = 0;
const fail = (m) => { problems++; console.log('  FAIL ' + m); };

for (const key in anim.animations) {
  const a = anim.animations[key];
  const len = a.animation_length;
  let keys = 0;
  for (const b in a.bones || {}) {
    if (!bones.has(b)) fail(key + ': unknown bone "' + b + '"');
    for (const ch in a.bones[b]) {
      if (!['rotation', 'position', 'scale'].includes(ch)) fail(key + ': bad channel ' + ch);
      const kf = a.bones[b][ch];
      const times = Object.keys(kf).map(Number).sort((x, y) => x - y);
      if (times.some((t) => !isFinite(t))) fail(key + ' ' + b + '.' + ch + ': non-numeric key');
      if (times[0] < 0) fail(key + ' ' + b + '.' + ch + ': negative key');
      if (times[times.length - 1] > len + 1e-6) fail(key + ' ' + b + '.' + ch + ': key ' + times[times.length - 1] + ' beyond length ' + len);
      for (const t of Object.keys(kf)) {
        const v = kf[t];
        const arr = Array.isArray(v) ? v : (v && v.vector);
        if (!arr) { fail(key + ' ' + b + '.' + ch + ' @' + t + ': malformed keyframe'); continue; }
        for (const n of arr) if (typeof n !== 'number' || !isFinite(n)) fail(key + ' ' + b + '.' + ch + ' @' + t + ': bad value ' + n);
        keys++;
      }
    }
  }
  if (a.loop && key.indexOf('idle') < 0 && key.indexOf('walk') < 0 && key.indexOf('float') < 0 && key.indexOf('turn') < 0)
    console.log('  NOTE ' + key + ' is looping');
  console.log('  ' + key.padEnd(40) + ' len=' + len + ' bones=' + Object.keys(a.bones || {}).length + ' keys=' + keys);
}
console.log('geo bones: ' + bones.size + ' -> ' + [...bones].join(','));
console.log(problems === 0 ? 'OK: no problems' : problems + ' problem(s)');
process.exit(problems === 0 ? 0 : 1);
