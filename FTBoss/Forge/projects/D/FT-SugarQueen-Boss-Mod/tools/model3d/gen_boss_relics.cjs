const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const previews = path.join(root, 'build/verification/boss-relics');
fs.mkdirSync(previews, { recursive: true });
const relics = [
  ['frost_crown_charm', [114, 216, 240]],
  ['butcher_fang_charm', [192, 59, 76]],
  ['tea_heart_pendant', [107, 186, 108]],
  ['aged_wine_seal', [168, 76, 147]],
  ['cheese_moon_charm', [255, 203, 88]],
  ['charged_core_pendant', [73, 204, 221]],
  ['rainbow_spore_charm', [195, 124, 223]],
  ['golden_apple_brooch', [245, 186, 59]],
  ['shepherd_whistle_charm', [210, 217, 227]],
  ['tide_pearl_pendant', [87, 181, 229]]
];
const gold = [226, 181, 100];
const pale = [253, 243, 215];
const dark = [54, 40, 61];
const sheet = new Canvas(5 * 112, 2 * 112);
sheet.rect(0, 0, sheet.w, sheet.h, 25, 30, 43);
function circle(canvas, centerX, centerY, radius, color) {
  for (let pixelY = -radius; pixelY <= radius; pixelY++) {
    for (let pixelX = -radius; pixelX <= radius; pixelX++) {
      if (pixelX * pixelX + pixelY * pixelY <= radius * radius) canvas.set(centerX + pixelX, centerY + pixelY, ...color);
    }
  }
}
for (const [index, [name, color]] of relics.entries()) {
  const canvas = new Canvas(32, 32);
  canvas.line(9, 4, 22, 4, ...gold);
  canvas.line(9, 4, 6, 10, ...gold);
  canvas.line(22, 4, 25, 10, ...gold);
  canvas.line(6, 10, 14, 16, ...gold);
  canvas.line(25, 10, 17, 16, ...gold);
  if (index === 0) {
    canvas.rect(7, 17, 19, 10, ...dark);
    canvas.rect(8, 18, 17, 8, ...gold);
    for (const [offset, height] of [[0, 6], [7, 9], [14, 6]]) {
      canvas.rect(8 + offset, 18 - height, 3, height + 1, ...color);
      canvas.set(9 + offset, 18 - height, ...pale);
    }
    canvas.rect(10, 21, 13, 3, ...color);
  } else if (index === 1) {
    circle(canvas, 16, 18, 7, dark);
    circle(canvas, 16, 18, 6, color);
    canvas.tri([12, 17], [19, 17], [15, 28], ...pale);
    canvas.line(12, 17, 18, 17, ...gold);
  } else if (index === 2) {
    canvas.tri([8, 22], [14, 11], [24, 16], ...dark);
    canvas.tri([9, 22], [15, 12], [23, 16], ...color);
    canvas.tri([9, 22], [23, 16], [18, 27], ...color);
    canvas.line(11, 24, 21, 15, ...pale);
    canvas.line(14, 22, 14, 17, ...gold);
  } else if (index === 3) {
    canvas.rect(9, 14, 15, 12, ...dark);
    canvas.rect(10, 15, 13, 10, ...color);
    canvas.rect(10, 16, 13, 2, ...gold);
    canvas.rect(10, 22, 13, 2, ...gold);
    canvas.rect(14, 13, 5, 3, ...pale);
    canvas.rect(14, 19, 5, 2, 106, 48, 93);
  } else if (index === 4) {
    canvas.tri([8, 26], [13, 11], [26, 24], ...dark);
    canvas.tri([10, 24], [14, 13], [24, 23], ...color);
    for (const [centerX, centerY] of [[14, 21], [17, 17], [20, 22]]) circle(canvas, centerX, centerY, 1, [177, 112, 39]);
    canvas.line(10, 25, 24, 24, ...gold);
  } else if (index === 5) {
    canvas.rect(8, 13, 17, 15, ...dark);
    canvas.rect(9, 14, 15, 13, 112, 125, 142);
    canvas.rect(11, 16, 11, 9, ...color);
    canvas.line(17, 15, 14, 21, ...pale);
    canvas.line(14, 21, 19, 20, ...pale);
    canvas.line(19, 20, 16, 26, ...pale);
  } else if (index === 6) {
    canvas.rect(14, 19, 5, 9, ...gold);
    circle(canvas, 16, 19, 8, dark);
    circle(canvas, 16, 18, 7, color);
    canvas.rect(8, 20, 17, 3, 103, 195, 191);
    for (const [centerX, centerY] of [[12, 16], [18, 13], [20, 18]]) circle(canvas, centerX, centerY, 1, pale);
  } else if (index === 7) {
    circle(canvas, 13, 20, 6, dark);
    circle(canvas, 20, 20, 6, dark);
    circle(canvas, 13, 20, 5, color);
    circle(canvas, 20, 20, 5, color);
    canvas.rect(13, 19, 8, 8, ...color);
    canvas.line(17, 15, 18, 10, 116, 77, 43);
    canvas.tri([18, 12], [23, 9], [24, 13], 114, 179, 91);
    canvas.rect(10, 18, 2, 4, ...pale);
  } else if (index === 8) {
    canvas.rect(8, 17, 16, 10, ...dark);
    canvas.rect(9, 18, 14, 8, ...color);
    canvas.rect(10, 16, 5, 3, ...gold);
    circle(canvas, 17, 21, 3, dark);
    circle(canvas, 17, 21, 1, gold);
    canvas.rect(22, 18, 5, 4, ...gold);
  } else {
    canvas.tri([7, 25], [9, 15], [16, 27], ...color);
    canvas.tri([25, 25], [23, 15], [16, 27], ...color);
    circle(canvas, 16, 19, 7, dark);
    circle(canvas, 16, 19, 6, color);
    circle(canvas, 15, 18, 4, [194, 229, 249]);
    canvas.rect(13, 15, 2, 2, ...pale);
  }
  canvas.save(path.join(assets, `textures/item/${name}.png`));
  fs.writeFileSync(path.join(assets, `models/item/${name}.json`), JSON.stringify({
    parent: 'minecraft:item/generated', textures: { layer0: `ftboss:item/${name}` }
  }, null, 2) + '\n');
  const enlarged = canvas.scaled(3);
  for (let pixelY = 0; pixelY < enlarged.h; pixelY++) {
    for (let pixelX = 0; pixelX < enlarged.w; pixelX++) {
      const offset = (pixelY * enlarged.w + pixelX) * 4;
      if (enlarged.d[offset + 3]) sheet.set(index % 5 * 112 + pixelX + 8,
        Math.floor(index / 5) * 112 + pixelY + 8, ...enlarged.d.slice(offset, offset + 3));
    }
  }
}
sheet.save(path.join(previews, 'relics.png'));

const atlas = new Canvas(128, 128);
const elements = [];
let cursorX = 1;
let cursorY = 1;
let rowHeight = 0;
function faceTexture(width, height, color, pattern) {
  width = Math.max(1, Math.ceil(width));
  height = Math.max(1, Math.ceil(height));
  if (cursorX + width + 1 > atlas.w) { cursorX = 1; cursorY += rowHeight + 2; rowHeight = 0; }
  if (cursorY + height >= atlas.h) throw new Error('Staff atlas overflow');
  for (let pixelY = -1; pixelY <= height; pixelY++) {
    for (let pixelX = -1; pixelX <= width; pixelX++) {
      const sampleX = Math.max(0, Math.min(width - 1, pixelX));
      const sampleY = Math.max(0, Math.min(height - 1, pixelY));
      const edge = sampleX === 0 || sampleY === height - 1;
      const shade = edge ? 0.73 : sampleY === 0 ? 1.13 : 1;
      const base = pattern && (sampleY + sampleX) % 7 < 2 ? [217, 121, 168] : color;
      atlas.set(cursorX + pixelX, cursorY + pixelY, ...base.map(channel => Math.round(channel * shade)));
    }
  }
  const uv = [cursorX, cursorY, cursorX + width, cursorY + height].map(value => value / 8);
  cursorX += width + 2;
  rowHeight = Math.max(rowHeight, height);
  return { uv, texture: '#staff' };
}
function cube(from, to, color, pattern = false) {
  const [width, height, depth] = to.map((value, axis) => value - from[axis]);
  const faces = {};
  for (const face of ['north', 'south']) faces[face] = faceTexture(width, height, color, pattern);
  for (const face of ['west', 'east']) faces[face] = faceTexture(depth, height, color, pattern);
  for (const face of ['up', 'down']) faces[face] = faceTexture(width, depth, color, pattern);
  elements.push({ from, to, faces });
}
cube([7, -6, 7], [9, 21, 9], [221, 235, 239], true);
cube([6.5, -6.5, 6.5], [9.5, -4, 9.5], gold);
cube([6.5, 2, 6.5], [9.5, 10, 9.5], [101, 62, 99]);
for (const height of [2, 9, 17]) cube([6.3, height, 6.3], [9.7, height + 1, 9.7], gold);
cube([3, 20, 6], [13, 22, 10], gold);
cube([2, 21, 6.5], [4, 27, 9.5], [222, 232, 242]);
cube([12, 21, 6.5], [14, 27, 9.5], [222, 232, 242]);
cube([4, 21, 7], [6, 25, 9], gold);
cube([10, 21, 7], [12, 25, 9], gold);
cube([6, 22, 6], [10, 29, 10], [107, 207, 230]);
cube([7, 28.8, 7], [9, 31, 9], [209, 245, 249]);
cube([6.8, 24, 5.8], [8, 27.5, 6.1], [234, 255, 255]);
cube([2, 26.8, 6.5], [4, 29, 9.5], [230, 150, 187]);
cube([12, 26.8, 6.5], [14, 29, 9.5], [230, 150, 187]);
cube([6, 19, 5.8], [10, 21.2, 6.2], [230, 150, 187]);
const model = {
  ambientocclusion: false,
  gui_light: 'front',
  textures: { staff: 'ftboss:item/frost_queen_staff', particle: 'ftboss:item/frost_queen_staff' },
  display: {
    thirdperson_righthand: { rotation: [0, 0, 8], translation: [0, 3, 0], scale: [0.8, 0.8, 0.8] },
    thirdperson_lefthand: { rotation: [0, 0, -8], translation: [0, 3, 0], scale: [0.8, 0.8, 0.8] },
    firstperson_righthand: { rotation: [0, -15, 12], translation: [1, 2, 0], scale: [0.65, 0.65, 0.65] },
    firstperson_lefthand: { rotation: [0, 15, -12], translation: [1, 2, 0], scale: [0.65, 0.65, 0.65] },
    gui: { rotation: [12, -25, -30], translation: [0, -1.4, 0], scale: [0.4, 0.4, 0.4] },
    ground: { rotation: [0, 0, 0], translation: [0, 4, 0], scale: [0.3, 0.3, 0.3] },
    fixed: { rotation: [0, 180, 0], translation: [0, -1, 0], scale: [0.4, 0.4, 0.4] },
    head: { rotation: [0, 0, 0], translation: [0, 12, 0], scale: [0.5, 0.5, 0.5] }
  },
  elements
};
fs.writeFileSync(path.join(assets, 'models/item/frost_queen_staff.json'), JSON.stringify(model, null, 2) + '\n');
atlas.save(path.join(assets, 'textures/item/frost_queen_staff.png'));
console.log(`Generated ${relics.length} relic icons and ${elements.length}-part staff`);
