/* Dump a GeckoLib animation block as a compact keyframe list.
   Usage: node dump_json_anim.cjs <animation.json> <animNameOrSuffix> [boneRegex] */
const fs = require('fs');
const file = process.argv[2];
const want = process.argv[3];
const boneRe = process.argv[4] ? new RegExp(process.argv[4], 'i') : null;
const j = JSON.parse(fs.readFileSync(file, 'utf8'));
const all = j.animations || {};
const key = Object.keys(all).find((k) => k === want || k.endsWith('.' + want) || k.includes(want));
if (!key) { console.log('not found: ' + want + '  available: ' + Object.keys(all).join(', ')); process.exit(1); }
const a = all[key];
console.log('### ' + key + '  len=' + a.animation_length + ' loop=' + !!a.loop);
for (const b of Object.keys(a.bones || {})) {
  if (boneRe && !boneRe.test(b)) continue;
  for (const ch of Object.keys(a.bones[b])) {
    console.log('  ' + b + ' ' + ch);
    const kf = a.bones[b][ch];
    for (const t of Object.keys(kf)) {
      const v = kf[t];
      const val = Array.isArray(v) ? v.join(', ') : (v && v.vector ? v.vector.join(', ') : JSON.stringify(v));
      console.log('     t=' + String(t).padStart(6) + '  [' + val + ']');
    }
  }
}
