'use strict';
const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const out = path.join(root, 'src/main/resources/assets/ftboss/textures/item/food_appraisal_monocle.png');
const c = new Canvas(16, 16);
const cx = 7, cy = 6;
// Translucent cyan lens first.
for (let y = 1; y <= 11; y++) for (let x = 2; x <= 12; x++) {
  const d = Math.hypot(x - cx, y - cy);
  if (d < 3.75) {
    const shine = Math.max(0, 1 - Math.hypot(x - 5, y - 4) / 5);
    c.set(x, y, 82 + shine * 92, 150 + shine * 75, 184 + shine * 66, 0.72);
  }
}
// Dark outer silhouette and warm gold rim.
for (let y = 0; y <= 12; y++) for (let x = 1; x <= 13; x++) {
  const d = Math.hypot(x - cx, y - cy);
  if (d >= 4.35 && d < 5.65) c.set(x, y, 73, 42, 14, 1);
  if (d >= 3.75 && d < 4.75) {
    const bright = x + y < 12;
    c.set(x, y, bright ? 244 : 190, bright ? 193 : 120, bright ? 66 : 27, 1);
  }
}
// Lens glint.
c.set(4, 3, 225, 250, 255, 0.95); c.set(5, 3, 188, 232, 247, 0.9);
c.set(4, 4, 178, 227, 246, 0.85); c.set(10, 8, 48, 105, 142, 0.8);
// Hinge and dangling chain: dark outline, then gold center.
for (const [x,y] of [[10,10],[11,11],[11,12],[12,13],[12,14],[13,15]]) {
  c.set(x-1,y,70,39,13,1); c.set(x+1,y,70,39,13,1); c.set(x,y,70,39,13,1);
}
for (const [x,y] of [[10,10],[11,11],[11,12],[12,13],[12,14],[13,15]]) c.set(x,y,224,158,39,1);
c.set(11,10,250,203,73,1);
fs.mkdirSync(path.dirname(out), { recursive: true });
c.save(out);
const image = decodePNG(out);
let opaque = 0;
for (let i = 3; i < image.data.length; i += 4) if (image.data[i] > 0) opaque++;
if (image.w !== 16 || image.h !== 16 || opaque < 65 || opaque > 150) throw new Error('invalid monocle sprite');
console.log('food appraisal monocle:', image.w + 'x' + image.h, opaque + ' visible pixels ->', out);
