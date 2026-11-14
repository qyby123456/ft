/* Generate the status-effect icon for 【屎？！！】 (18x18, vanilla mob_effect size).
   A three-lobe poop mound with an outline, a left highlight and two eyes.
   Usage: node gen_effect_icon.cjs <outPng> */
const path = require('path');
const { Canvas } = require('./png.cjs');

const W = 18, H = 18;
const cv = new Canvas(W, H);
const BODY = [139, 90, 43];
const LIGHT = [176, 122, 70];
const DARK = [62, 36, 16];
const WHITE = [245, 245, 240];
const PUPIL = [30, 20, 12];

/* three stacked lobes */
const LOBES = [
  { cx: 8.5, cy: 13.2, rx: 6.6, ry: 3.5 },
  { cx: 8.5, cy: 9.2, rx: 5.1, ry: 3.0 },
  { cx: 8.5, cy: 5.5, rx: 3.4, ry: 2.7 },
];
const body = new Uint8Array(W * H);
for (const L of LOBES) {
  for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
    const dx = (x + 0.5 - L.cx) / L.rx, dy = (y + 0.5 - L.cy) / L.ry;
    if (dx * dx + dy * dy <= 1.0) body[y * W + x] = 1;
  }
}

/* body + left highlight */
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  if (!body[y * W + x]) continue;
  const c = x < 8 ? LIGHT : BODY;
  cv.set(x, y, c[0], c[1], c[2], 1);
}
/* outline: body pixel touching transparent */
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  if (!body[y * W + x]) continue;
  const n = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => {
    const nx = x + dx, ny = y + dy;
    return nx < 0 || ny < 0 || nx >= W || ny >= H || !body[ny * W + nx];
  });
  if (n) cv.set(x, y, DARK[0], DARK[1], DARK[2], 1);
}
/* lobe separators: give it the classic stacked swirl instead of one blob */
for (const [y, x0, x1] of [[7, 5, 12], [11, 3, 13]]) {
  for (let x = x0; x <= x1; x++) {
    if (!body[y * W + x]) continue;
    cv.set(x, y, DARK[0], DARK[1], DARK[2], 1);
  }
}
/* eyes on the middle lobe */
for (const ex of [6, 10]) {
  cv.rect(ex, 8, 2, 2, WHITE[0], WHITE[1], WHITE[2], 1);
}
cv.set(6, 9, PUPIL[0], PUPIL[1], PUPIL[2], 1);
cv.set(11, 9, PUPIL[0], PUPIL[1], PUPIL[2], 1);

const out = process.argv[2];
cv.save(out);
console.log('wrote ' + out + ' (' + W + 'x' + H + ')  -> ' + path.basename(out));
