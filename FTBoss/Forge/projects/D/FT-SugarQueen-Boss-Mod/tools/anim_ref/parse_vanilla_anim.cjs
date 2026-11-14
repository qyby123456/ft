/* Parse javap -c -p -constants output of a vanilla AnimationDefinition class
   into a readable keyframe table.
   Usage: node parse_vanilla_anim.cjs <javap.txt> [regexForDetail] */
const fs = require('fs');
const file = process.argv[2];
const detailRe = process.argv[3] ? new RegExp(process.argv[3], 'i') : null;
const raw = fs.readFileSync(file, 'utf8').split(/\r?\n/);

const TARGET = {
  'f_232250_': 'POS',
  'f_232251_': 'ROT',
  'f_232252_': 'SCALE',
};
const INTERP = {
  'f_232229_': 'linear',
  'f_232230_': 'catmull',
  'f_232231_': 'catmull',
};

let floats = [];
let lastString = null;
let pendingBone = null;
let curTarget = null;
let cur = null;
let pendingKf = null;
const anims = [];

const push = (v) => { floats.push(v); if (floats.length > 32) floats.shift(); };
const last4 = () => floats.slice(-4);

for (let i = 0; i < raw.length; i++) {
  const line = raw[i];
  const code = line.replace(/\/\/.*$/, '');
  const fm = line.match(/\/\/\s*float\s+(-?[\d.]+)f/);
  const fc = code.match(/\bfconst_([012])\b/);
  if (fc) push(parseInt(fc[1], 10));
  else if (fm) push(parseFloat(fm[1]));

  const sm = line.match(/\/\/\s*String\s+(.+?)\s*$/);
  if (sm && !/class |Method |Field /.test(line)) lastString = sm[1].trim();

  if (/class net\/minecraft\/client\/animation\/AnimationChannel$/.test(line)) pendingBone = lastString;

  const tm = line.match(/AnimationChannel\$Targets\.(f_\d+_)/);
  if (tm) curTarget = TARGET[tm[1]] || tm[1];

  if (/KeyframeAnimations\.(m_\d+_)/.test(line)) {
    const [t, x, y, z] = last4();
    pendingKf = { t, v: [x, y, z], i: '?' };
  }
  const im = line.match(/Interpolations\.(f_\d+_)/);
  if (im && pendingKf) pendingKf.i = INTERP[im[1]] || im[1];

  if (/Keyframe\."<init>"/.test(line) && pendingKf) {
    if (cur) {
      const key = pendingBone + '|' + curTarget;
      if (!cur.ch[key]) cur.ch[key] = [];
      cur.ch[key].push(pendingKf);
    }
    pendingKf = null;
  }

  if (/AnimationDefinition\$Builder\.m_\d+_:\(F\)/.test(line)) {
    const len = floats.length ? floats[floats.length - 1] : -1;
    if (cur) { cur.name = cur.name || '?'; anims.push(cur); }
    cur = { name: '?', len, ch: {} };
  }
  const pm = line.match(/putstatic\s+#\d+\s+\/\/\s*Field (\w+):/);
  if (pm) {
    if (cur) { cur.name = pm[1]; anims.push(cur); cur = null; }
    else anims.push({ name: pm[1], len: -1, ch: {} });
  }
}

console.log('animations: ' + anims.length);
for (const a of anims) {
  const keys = Object.keys(a.ch);
  const nkf = keys.reduce((s, k) => s + a.ch[k].length, 0);
  console.log(`  ${a.name.padEnd(22)} len=${a.len}s  channels=${keys.length}  keyframes=${nkf}`);
}

if (detailRe) {
  for (const a of anims) {
    if (!detailRe.test(a.name)) continue;
    console.log('\n=== ' + a.name + '  (len ' + a.len + 's) ===');
    const bones = [];
    for (const k of Object.keys(a.ch)) {
      const [b, tg] = k.split('|');
      if (!bones.includes(b)) bones.push(b);
    }
    for (const b of bones) {
      for (const tg of ['ROT', 'POS', 'SCALE']) {
        const list = a.ch[b + '|' + tg];
        if (!list) continue;
        console.log(`  ${b} ${tg}`);
        for (const kf of list) {
          const v = kf.v.map((n) => (n === undefined ? '?' : n)).join(', ');
          console.log(`     t=${String(kf.t).padStart(7)}  [${v}]  ${kf.i}`);
        }
      }
    }
  }
}
