const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');
const root = path.resolve(process.argv[2] || path.join(__dirname, '../..'));
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const data = path.join(root, 'src/main/resources/data/ftboss');
const palette = {
  skin: [34, 139, 151], skinDark: [24, 78, 100], belly: [237, 215, 162],
  fin: [226, 116, 74], gold: [235, 177, 62], lemon: [246, 209, 85],
  herb: [59, 107, 75], eye: [252, 235, 180], pupil: [22, 34, 49],
  porcelain: [192, 220, 226], plate: [51, 87, 115], flesh: [227, 142, 113],
  pearl: [138, 222, 220], steel: [134, 193, 198], wood: [70, 78, 79]
};
const phasePalette = { ...palette, skin: [150, 65, 67], skinDark: [62, 46, 80],
  belly: [214, 157, 107], fin: [236, 120, 54], pearl: [164, 141, 242], flesh: [238, 121, 80] };

function writeJson(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(value, null, 2) + '\n');
}

function createModel(name, textureSize) {
  const bones = [];
  const faces = [];
  const canvas = new Canvas(textureSize, textureSize);
  const rows = [];
  const bone = (name, pivot, parent) => {
    const entry = { name, pivot, ...(parent ? { parent } : {}), cubes: [] };
    bones.push(entry);
    return entry;
  };
  const allocate = (width, height, material, faceName) => {
    const pixelWidth = Math.max(1, Math.ceil(width));
    const pixelHeight = Math.max(1, Math.ceil(height));
    let row = rows.filter(candidate => candidate.height >= pixelHeight && candidate.cursor + pixelWidth + 1 <= textureSize)
      .sort((left, right) => left.height - right.height)[0];
    if (!row) {
      const top = rows.reduce((height, candidate) => Math.max(height, candidate.top + candidate.height + 2), 1);
      if (top + pixelHeight + 1 > textureSize) throw new Error(`Atlas overflow: ${name}`);
      row = { top, height: pixelHeight, cursor: 1 };
      rows.push(row);
    }
    const rect = { left: row.cursor, top: row.top, width: pixelWidth, height: pixelHeight, material, faceName };
    faces.push(rect);
    row.cursor += pixelWidth + 2;
    return { uv: [rect.left, rect.top], uv_size: [pixelWidth, pixelHeight] };
  };
  const cube = (parent, origin, size, material) => {
    const [width, height, depth] = size;
    const uv = {};
    for (const face of ['north', 'south', 'east', 'west', 'up', 'down']) {
      const dimensions = face === 'up' || face === 'down' ? [width, depth]
        : face === 'east' || face === 'west' ? [depth, height] : [width, height];
      uv[face] = allocate(...dimensions, material, face);
    }
    parent.cubes.push({ origin, size, uv });
  };
  const paint = colors => {
    for (const rect of faces) {
      for (let drawY = -1; drawY <= rect.height; drawY++) {
        for (let drawX = -1; drawX <= rect.width; drawX++) {
          const pixelY = Math.max(0, Math.min(rect.height - 1, drawY));
          const pixelX = Math.max(0, Math.min(rect.width - 1, drawX));
          let shade = rect.faceName === 'up' ? 1.12 : rect.faceName === 'down' ? 0.73 : rect.faceName === 'east' ? 0.87 : 1;
          const border = pixelX === 0 || pixelY === 0 || pixelX === rect.width - 1 || pixelY === rect.height - 1;
          if (border) shade *= pixelY === 0 ? 1.12 : 0.81;
          if (rect.material === 'skin' || rect.material === 'skinDark') {
            if ((pixelY % 4 === 3) && ((pixelX + Math.floor(pixelY / 4) * 2) % 5 < 3)) shade *= 0.76;
            if ((pixelY % 4 === 0) && ((pixelX + Math.floor(pixelY / 4) * 2) % 5 === 1)) shade *= 1.15;
          }
          if (rect.material === 'flesh') {
            if ((pixelX + pixelY * 2) % 9 < 2) shade *= 1.24;
            if (pixelX % 8 === 5) shade *= 0.64;
          }
          if (rect.material === 'fin' && pixelX % 4 === 0) shade *= 0.72;
          if (rect.material === 'belly') shade *= 0.96 + Math.floor(pixelY / 4) % 2 * 0.06;
          if (rect.material === 'gold' || rect.material === 'steel') shade *= pixelX < rect.width / 3 ? 1.1 : 0.92;
          if (rect.material === 'lemon' && (pixelX + pixelY) % 5 === 0) shade *= 1.12;
          const color = colors[rect.material].map(channel => Math.max(0, Math.min(255, Math.round(channel * shade))));
          canvas.set(rect.left + drawX, rect.top + drawY, ...color, 1);
        }
      }
    }
    return canvas;
  };
  const geo = () => ({ format_version: '1.12.0', 'minecraft:geometry': [{
    description: { identifier: `geometry.${name}`, texture_width: textureSize, texture_height: textureSize,
      visible_bounds_width: 5, visible_bounds_height: 5, visible_bounds_offset: [0, 1.7, 0] }, bones
  }] });
  return { bone, cube, paint, geo, bones, textureSize };
}

const boss = createModel('tide_fish_boss', 512);
const base = boss.bone('root', [0, 0, 0]);
const platter = boss.bone('platter', [0, 1, 0], 'root');
boss.cube(platter, [-15, 0, -11], [30, 2, 22], 'plate');
boss.cube(platter, [-17, 1.2, -9], [34, 2, 18], 'porcelain');
boss.cube(platter, [-13, 1.5, -13], [26, 2, 26], 'porcelain');
boss.cube(platter, [-13, 3.1, -11], [26, 1, 22], 'plate');
const body = boss.bone('body', [0, 16, 0], 'root');
boss.cube(body, [-10, 13, -7], [20, 25, 14], 'skin');
boss.cube(body, [-8, 14, -9], [16, 23, 4], 'belly');
boss.cube(body, [-12, 19, -5], [24, 16, 10], 'skin');
boss.cube(body, [-7, 21, -10], [14, 12, 2], 'flesh');
boss.cube(body, [-9, 13.3, -8.4], [18, 3, 16], 'gold');
boss.cube(body, [-3, 13.7, -9.2], [6, 3, 2], 'pearl');
const head = boss.bone('head', [0, 36, 0], 'body');
boss.cube(head, [-12, 35, -10], [24, 13, 18], 'skin');
boss.cube(head, [-10, 37, -12], [20, 9, 4], 'skinDark');
boss.cube(head, [-7, 39, -14], [14, 5, 4], 'skin');
boss.cube(head, [-5, 39.5, -15], [10, 3, 2], 'skinDark');
boss.cube(head, [-8, 34, -13], [16, 5, 7], 'belly');
boss.cube(head, [-6, 38, -13.3], [12, 2, 2], 'pupil');
boss.cube(head, [-5, 37.3, -14], [10, 1, 3], 'fin');
for (const side of [-1, 1]) {
  boss.cube(head, [side < 0 ? -11 : 5, 41, -12.8], [6, 5, 2], 'eye');
  boss.cube(head, [side < 0 ? -9.7 : 6.7, 41.7, -13.5], [3, 3, 1], 'pupil');
  boss.cube(head, [side < 0 ? -9.3 : 7.1, 43.4, -14.1], [1, 1, 1], 'porcelain');
  for (let index = 0; index < 3; index++) {
    boss.cube(head, [side < 0 ? -12.8 : 11.5, 37.5 + index * 2.5, -7.5], [1.3, 1, 9], 'skinDark');
  }
  const suffix = side < 0 ? 'r' : 'l';
  const shoulder = boss.bone(`fin_${suffix}`, [side * 10, 30, 0], 'body');
  const tip = boss.bone(`fin_tip_${suffix}`, [side * 16, 22, 0], `fin_${suffix}`);
  boss.cube(shoulder, [side < 0 ? -16 : 9, 24, -3], [7, 9, 6], 'skinDark');
  boss.cube(shoulder, [side < 0 ? -19 : 14, 19, -2], [5, 10, 4], 'fin');
  boss.cube(shoulder, [side < 0 ? -20.5 : 17.5, 22, -1.3], [3, 6, 3], 'fin');
  boss.cube(tip, [side < 0 ? -20 : 15, 15, -1.3], [5, 9, 3], 'fin');
  boss.cube(tip, [side < 0 ? -20.4 : 18.4, 16, -1.7], [2, 7, 4], 'gold');
  const leg = boss.bone(`leg_${suffix}`, [side * 6, 15, 0], 'body');
  const foot = boss.bone(`foot_${suffix}`, [side * 6, 7, 0], `leg_${suffix}`);
  boss.cube(leg, [side * 6 - 2.5, 6, -3], [5, 10, 6], 'skinDark');
  boss.cube(foot, [side * 6 - 4, 3.7, -8], [8, 4, 12], 'fin');
  const herb = boss.bone(`herb_${suffix}`, [side * 9, 34, 5], 'body');
  for (let index = 0; index < 3; index++) {
    boss.cube(herb, [side < 0 ? -11 + index : 7 - index, 21 - index, 5.4 + index], [4, 16 - index * 2, 1], 'herb');
  }
}
const dorsal = boss.bone('dorsal', [0, 32, 6], 'body');
boss.cube(dorsal, [-2, 20, 6], [4, 28, 4], 'fin');
boss.cube(dorsal, [-1, 25, 9], [2, 26, 4], 'fin');
boss.cube(dorsal, [-0.7, 32, 12], [1.4, 14, 3], 'gold');
const tail = boss.bone('tail', [0, 17, 6], 'body');
boss.cube(tail, [-2.5, 14, 5.5], [5, 6, 8], 'skinDark');
boss.cube(tail, [-4, 12, 12], [8, 10, 2], 'fin');
boss.cube(tail, [-5, 11, 13], [10, 3, 2], 'fin');
boss.cube(tail, [-5, 20, 13], [10, 3, 2], 'fin');
const crown = boss.bone('crown', [0, 47, 0], 'head');
boss.cube(crown, [-8, 47.5, -6], [16, 2, 12], 'gold');
for (let index = -1; index <= 1; index++) {
  boss.cube(crown, [index * 6 - 2, 49, -4], [4, index === 0 ? 7 : 5, 4], 'lemon');
  boss.cube(crown, [index * 6 - 1, 50, -4.5], [2, index === 0 ? 4 : 2, 1], 'belly');
}
for (const side of [-1, 1]) {
  boss.cube(platter, [side < 0 ? -12 : 7, 3.6, -10], [5, 3, 4], 'lemon');
  boss.cube(platter, [side < 0 ? -11.3 : 7.7, 4.2, -10.4], [3, 1, 1], 'belly');
  boss.cube(platter, [side < 0 ? -14 : 9, 3.8, 5], [5, 2, 4], 'herb');
}
const fork = boss.bone('royal_fork', [-18, 20, 0], 'fin_tip_r');
boss.cube(fork, [-23, 6, -3], [2, 32, 2], 'wood');
boss.cube(fork, [-23.3, 18, -3.3], [2.6, 3, 2.6], 'gold');
boss.cube(fork, [-24, 18.5, -2.5], [6, 2, 3], 'skinDark');
boss.cube(fork, [-26, 35, -3.5], [8, 3, 3], 'steel');
for (const horizontal of [-26, -23, -20]) {
  boss.cube(fork, [horizontal, 37, -3], [2, horizontal === -23 ? 9 : 6, 2], 'steel');
}

function saveEntity(model, name) {
  writeJson(path.join(assets, `geo/${name}.geo.json`), model.geo());
  fs.mkdirSync(path.join(assets, 'textures/entity'), { recursive: true });
  model.paint(palette).save(path.join(assets, `textures/entity/${name}.png`));
}
saveEntity(boss, 'tide_fish_boss');
boss.paint(phasePalette).save(path.join(assets, 'textures/entity/tide_fish_boss_p2.png'));

for (const puffer of [false, true]) {
  const name = puffer ? 'brine_puffer' : 'reef_fish';
  const model = createModel(name, 128);
  model.bone('root', [0, 0, 0]);
  const body = model.bone('body', [0, 6, 0], 'root');
  const tail = model.bone('tail', [0, 6, 4], 'body');
  if (puffer) {
    model.cube(body, [-4, 2, -4], [8, 8, 8], 'lemon');
    model.cube(body, [-5, 3, -3], [10, 6, 6], 'gold');
    model.cube(body, [-3, 3, -5], [6, 6, 10], 'belly');
    for (const side of [-1, 1]) {
      for (const height of [3, 7]) model.cube(body, [side < 0 ? -6 : 4, height, -1], [2, 1, 2], 'pupil');
    }
    for (const horizontal of [-3, 2]) model.cube(body, [horizontal, 9, -1], [1, 3, 1], 'gold');
  } else {
    model.cube(body, [-3, 2, -5], [6, 6, 10], 'skin');
    model.cube(body, [-2, 2.5, -6], [4, 4, 2], 'belly');
    model.cube(body, [-0.5, 7, -2], [1, 4, 6], 'fin');
    model.cube(tail, [-1.5, 3, 3], [3, 5, 4], 'skinDark');
    model.cube(tail, [-3, 1, 6], [6, 9, 2], 'fin');
  }
  for (const side of [-1, 1]) {
    model.cube(body, [side < 0 ? -2.7 : 0.7, 5.5, -6.2], [2, 2, 1], 'eye');
    model.cube(body, [side < 0 ? -2.2 : 1.2, 6, -6.6], [1, 1, 1], 'pupil');
    const fin = model.bone(side < 0 ? 'fin_r' : 'fin_l', [side * 3, 4, 0], 'body');
    model.cube(fin, [side < 0 ? -6 : 2, 3, -1], [4, 1, 4], 'fin');
  }
  saveEntity(model, name);
}

const keys = (duration, values) => Object.fromEntries(values.map((value, index) => [
  (duration * index / (values.length - 1)).toFixed(3), value
]));
const rotation = (duration, values) => ({ rotation: keys(duration, values) });
const animations = {};
function animation(name, ticks, bones, loop = false) {
  animations[`animation.tide_fish.${name}`] = { animation_length: ticks / 20, loop, bones };
}
animation('idle', 60, {
  body: { position: keys(3, [[0, 0, 0], [0, 0.5, 0], [0, 0, 0]]) },
  fin_l: rotation(3, [[0, 0, -4], [0, 5, 6], [0, 0, -4]]),
  fin_r: rotation(3, [[0, 0, 4], [0, -5, -6], [0, 0, 4]]),
  dorsal: rotation(3, [[0, -3, 0], [0, 3, 0], [0, -3, 0]]),
  tail: rotation(3, [[0, -12, 0], [0, 12, 0], [0, -12, 0]])
}, true);
animation('walk', 20, {
  body: rotation(1, [[0, 0, -3], [0, 0, 3], [0, 0, -3]]),
  leg_l: rotation(1, [[20, 0, 0], [-20, 0, 0], [20, 0, 0]]),
  leg_r: rotation(1, [[-20, 0, 0], [20, 0, 0], [-20, 0, 0]]),
  foot_l: rotation(1, [[-10, 0, 0], [10, 0, 0], [-10, 0, 0]]),
  foot_r: rotation(1, [[10, 0, 0], [-10, 0, 0], [10, 0, 0]]),
  fin_l: rotation(1, [[-12, 0, -8], [12, 0, 8], [-12, 0, -8]]),
  fin_r: rotation(1, [[12, 0, 8], [-12, 0, -8], [12, 0, 8]]),
  tail: rotation(1, [[0, -18, 0], [0, 18, 0], [0, -18, 0]])
}, true);
animation('sweep', 30, {
  body: { rotation: { '0.0': [0, 0, 0], '0.55': [0, -20, 0], '0.8': [0, 24, 0], '1.5': [0, 0, 0] } },
  fin_r: { rotation: { '0.0': [0, 0, 0], '0.55': [0, 0, -70], '0.8': [0, 0, -35], '1.5': [0, 0, 0] } },
  tail: rotation(1.5, [[0, 0, 0], [0, -50, 0], [0, 0, 0]])
});
animation('volley', 40, {
  head: { rotation: { '0.0': [0, 0, 0], '0.75': [-12, 0, 0], '1.0': [15, 0, 0], '2.0': [0, 0, 0] } },
  fin_l: rotation(2, [[0, 0, 0], [0, 0, 35], [0, 0, 0]]),
  fin_r: rotation(2, [[0, 0, 0], [0, 0, -35], [0, 0, 0]])
});
animation('dash', 44, {
  body: { rotation: { '0.0': [0, 0, 0], '0.8': [-8, 0, 0], '1.0': [20, 0, 0], '1.7': [20, 0, 0], '2.2': [0, 0, 0] } },
  fin_l: rotation(2.2, [[0, 0, 0], [0, 0, 22], [0, 0, 0]]),
  fin_r: rotation(2.2, [[0, 0, 0], [0, 0, -22], [0, 0, 0]])
});
for (const [name, ticks, release] of [['summon', 48, 1.3], ['wave', 56, 1.2], ['maelstrom', 60, 1.8], ['phase', 40, 1]]) {
  const duration = ticks / 20;
  animation(name, ticks, {
    fin_l: { rotation: { '0.0': [0, 0, 0], [String(release - 0.3)]: [0, 0, 85],
      [String(release)]: [0, 0, 55], [String(duration)]: [0, 0, 0] } },
    fin_r: { rotation: { '0.0': [0, 0, 0], [String(release - 0.3)]: [0, 0, -85],
      [String(release)]: [0, 0, -55], [String(duration)]: [0, 0, 0] } },
    head: rotation(duration, [[0, 0, 0], [-8, 0, 0], [0, 0, 0]]),
    dorsal: rotation(duration, [[0, 0, 0], [-12, 0, 0], [0, 0, 0]])
  });
}
animation('death', 20, {
  body: { position: keys(1, [[0, 0, 0], [0, -3, 0]]) },
  leg_l: { position: keys(1, [[0, 0, 0], [0, 3, 0]]) },
  leg_r: { position: keys(1, [[0, 0, 0], [0, 3, 0]]) },
  head: rotation(1, [[0, 0, 0], [15, 0, 0]]),
  fin_l: rotation(1, [[0, 0, 0], [0, 0, 45]]),
  fin_r: rotation(1, [[0, 0, 0], [0, 0, -45]])
}, 'hold_on_last_frame');
function smoothTrack(track, duration) {
  const source = Object.entries(track).map(([time, value]) => [Number(time), value]).sort((left, right) => left[0] - right[0]);
  const times = new Set(source.map(entry => entry[0]));
  for (let frame = 0; frame <= Math.round(duration * 24); frame++) times.add(Number((frame / 24).toFixed(3)));
  const result = {};
  for (const time of [...times].sort((left, right) => left - right)) {
    if (time > duration) continue;
    let high = source.findIndex(entry => entry[0] >= time);
    if (high < 0) high = source.length - 1;
    const low = Math.max(0, high - 1);
    const span = source[high][0] - source[low][0];
    const fraction = span ? Math.max(0, Math.min(1, (time - source[low][0]) / span)) : 0;
    const eased = fraction * fraction * (3 - 2 * fraction);
    result[time.toFixed(3)] = source[low][1].map((value, axis) => Number((value + (source[high][1][axis] - value) * eased).toFixed(4)));
  }
  return result;
}
for (const animation of Object.values(animations)) {
  if (animation.bones.fin_r?.rotation) {
    animation.bones.royal_fork = { rotation: Object.fromEntries(Object.entries(animation.bones.fin_r.rotation)
      .map(([time, value]) => [time, value.map(angle => -angle)])) };
  }
  for (const bone of Object.values(animation.bones)) {
    for (const property of Object.keys(bone)) bone[property] = smoothTrack(bone[property], animation.animation_length);
  }
}
writeJson(path.join(assets, 'animations/tide_fish_boss.animation.json'), { format_version: '1.8.0', animations });
writeJson(path.join(assets, 'animations/reef_minion.animation.json'), { format_version: '1.8.0', animations: {
  'animation.reef_minion.swim': { loop: true, animation_length: 0.8, bones: {
    body: rotation(0.8, [[0, -6, 0], [0, 6, 0], [0, -6, 0]]),
    tail: rotation(0.8, [[0, 20, 0], [0, -20, 0], [0, 20, 0]]),
    fin_l: rotation(0.8, [[0, 0, -20], [0, 0, 20], [0, 0, -20]]),
    fin_r: rotation(0.8, [[0, 0, 20], [0, 0, -20], [0, 0, 20]])
  } }
} });

function javaElements(model, scale, offset, slice) {
  const elements = [];
  for (const bone of model.bones) for (const cube of bone.cubes) {
    const from = cube.origin.map((value, axis) => value * scale + offset[axis]);
    const to = cube.origin.map((value, axis) => (value + cube.size[axis]) * scale + offset[axis]);
    const originalLow = from[1];
    const originalHigh = to[1];
    if (slice) {
      from[1] = Math.max(from[1], slice[0]);
      to[1] = Math.min(to[1], slice[1]);
      if (from[1] >= to[1]) continue;
    }
    const faces = {};
    for (const [face, mapping] of Object.entries(cube.uv)) {
      const [left, top] = mapping.uv;
      const [width, height] = mapping.uv_size;
      let lowV = top;
      let highV = top + height;
      const vertical = !['up', 'down'].includes(face);
      if (vertical && slice) {
        lowV = top + height * (originalHigh - to[1]) / (originalHigh - originalLow);
        highV = top + height * (originalHigh - from[1]) / (originalHigh - originalLow);
      }
      const rect = vertical ? [left + width, lowV, left, highV] : [left, top + height, left + width, top];
      faces[face] = { uv: rect.map(value => Number((value * 16 / model.textureSize).toFixed(5))), texture: '#body' };
    }
    if (slice) { from[1] -= slice[0]; to[1] -= slice[0]; }
    elements.push({ from: from.map(value => Number((value - 0.015).toFixed(4))),
      to: to.map(value => Number((value + 0.015).toFixed(4))), faces });
  }
  return elements;
}

const statueId = 'statue_tide_fish';
fs.mkdirSync(path.join(assets, 'textures/block'), { recursive: true });
fs.copyFileSync(path.join(assets, 'textures/entity/tide_fish_boss.png'), path.join(assets, `textures/block/${statueId}.png`));
const stone = boss.paint(palette);
for (let index = 0; index < stone.d.length; index += 4) {
  const gray = Math.round(stone.d[index] * 0.25 + stone.d[index + 1] * 0.6 + stone.d[index + 2] * 0.15);
  stone.d[index] = Math.min(255, gray * 0.6 + 84);
  stone.d[index + 1] = Math.min(255, gray * 0.6 + 88);
  stone.d[index + 2] = Math.min(255, gray * 0.6 + 92);
}
stone.save(path.join(assets, `textures/block/${statueId}_white.png`));
const goldStatue = boss.paint(palette);
for (let index = 0; index < goldStatue.d.length; index += 4) {
  const light = (goldStatue.d[index] * 0.2126 + goldStatue.d[index + 1] * 0.7152 + goldStatue.d[index + 2] * 0.0722) / 255;
  const shine = Math.max(0, Math.min(1, Math.pow(light, 0.82)));
  goldStatue.d[index] = Math.round(58 + shine * 197);
  goldStatue.d[index + 1] = Math.round(31 + shine * 176);
  goldStatue.d[index + 2] = Math.round(7 + shine * 74);
}
goldStatue.save(path.join(assets, `textures/block/${statueId}_gold.png`));
const variants = {};
for (const [half, slice] of [['lower', [0, 16]], ['upper', [16, 32]]]) {
  for (const white of [false, true]) {
    const modelName = `${statueId}_${half}${white ? '' : '_painted'}`;
    writeJson(path.join(assets, `models/block/${modelName}.json`), {
      ambientocclusion: false, textures: { body: `ftboss:block/${statueId}${white ? '_white' : ''}`, particle: `ftboss:block/${statueId}` },
      elements: javaElements(boss, 0.48, [8, 0.7, 8], slice)
    });
    variants[`half=${half},white=${white},gold=false`] = { model: `ftboss:block/${modelName}` };
  }
  const goldModel = `${statueId}_${half}_gold`;
  writeJson(path.join(assets, `models/block/${goldModel}.json`), {
    ambientocclusion: false, textures: { body: `ftboss:block/${statueId}_gold`, particle: `ftboss:block/${statueId}_gold` },
    elements: javaElements(boss, 0.48, [8, 0.7, 8], slice)
  });
  variants[`half=${half},white=false,gold=true`] = { model: `ftboss:block/${goldModel}` };
  variants[`half=${half},white=true,gold=true`] = { model: `ftboss:block/${goldModel}` };
}
writeJson(path.join(assets, `blockstates/${statueId}.json`), { variants });
writeJson(path.join(assets, `models/item/${statueId}.json`), {
  gui_light: 'front', ambientocclusion: false, textures: { body: `ftboss:block/${statueId}`, particle: `ftboss:block/${statueId}` },
  elements: javaElements(boss, 0.25, [8, 0, 8]), display: {
    gui: { rotation: [20, 35, 0], translation: [0, 1, 0], scale: [0.85, 0.85, 0.85] },
    ground: { translation: [0, 3, 0], scale: [0.4, 0.4, 0.4] },
    fixed: { scale: [0.7, 0.7, 0.7] },
    thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.4, 0.4, 0.4] },
    thirdperson_lefthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.4, 0.4, 0.4] },
    firstperson_righthand: { rotation: [0, 35, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] },
    firstperson_lefthand: { rotation: [0, -35, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] }
  }
});

const weapon = createModel('tide_trident', 128);
const handle = weapon.bone('root', [8, 8, 8]);
weapon.cube(handle, [7, -5, 7], [2, 25, 2], 'wood');
for (const height of [-5, 4, 14]) weapon.cube(handle, [6.7, height, 6.7], [2.6, 2, 2.6], 'gold');
weapon.cube(handle, [2, 17, 6.5], [12, 3, 3], 'steel');
weapon.cube(handle, [6, 18, 6], [4, 5, 4], 'skinDark');
weapon.cube(handle, [7, 19, 5.4], [2, 3, 1], 'pearl');
for (const horizontal of [2, 7, 12]) {
  weapon.cube(handle, [horizontal, 19, 7], [2, horizontal === 7 ? 10 : 7, 2], 'steel');
  weapon.cube(handle, [horizontal + 0.4, horizontal === 7 ? 28 : 25, 7.4], [1.2, 3, 1.2], 'porcelain');
}
fs.mkdirSync(path.join(assets, 'textures/item'), { recursive: true });
weapon.paint(palette).save(path.join(assets, 'textures/item/tide_trident.png'));
writeJson(path.join(assets, 'models/item/tide_trident.json'), {
  gui_light: 'front', ambientocclusion: false, textures: { body: 'ftboss:item/tide_trident', particle: 'ftboss:item/tide_trident' },
  elements: javaElements(weapon, 1, [0, 0, 0]), display: {
    gui: { rotation: [0, 0, -35], translation: [0, -1, 0], scale: [0.38, 0.38, 0.38] },
    ground: { rotation: [0, 0, 0], translation: [0, 6, 0], scale: [0.35, 0.35, 0.35] },
    fixed: { rotation: [0, 0, -35], translation: [0, -1, 0], scale: [0.5, 0.5, 0.5] },
    thirdperson_righthand: { rotation: [0, -90, 0], translation: [0, 0, 0.5], scale: [0.8, 0.8, 0.8] },
    thirdperson_lefthand: { rotation: [0, 90, 0], translation: [0, 0, 0.5], scale: [0.8, 0.8, 0.8] },
    firstperson_righthand: { rotation: [0, -90, 15], translation: [1.2, -2, 0], scale: [0.65, 0.65, 0.65] },
    firstperson_lefthand: { rotation: [0, 90, -15], translation: [1.2, -2, 0], scale: [0.65, 0.65, 0.65] }
  }
});
writeJson(path.join(__dirname, 'tide_trident.preview.geo.json'), weapon.geo());

function icon(name, draw) {
  const canvas = new Canvas(32, 32);
  draw(canvas);
  canvas.save(path.join(assets, `textures/item/${name}.png`));
  writeJson(path.join(assets, `models/item/${name}.json`), { parent: 'minecraft:item/generated', textures: { layer0: `ftboss:item/${name}` } });
}
icon('tide_banquet', canvas => {
  canvas.rect(2, 22, 28, 5, ...palette.plate, 1);
  canvas.rect(4, 20, 24, 4, ...palette.porcelain, 1);
  canvas.rect(6, 12, 20, 9, ...palette.skinDark, 1);
  canvas.rect(8, 11, 15, 9, ...palette.skin, 1);
  canvas.rect(9, 16, 13, 4, ...palette.flesh, 1);
  canvas.tri([23, 15], [29, 10], [29, 21], ...palette.fin, 1);
  canvas.rect(7, 12, 3, 3, ...palette.eye, 1);
  canvas.rect(8, 13, 1, 1, ...palette.pupil, 1);
  canvas.rect(13, 7, 9, 4, ...palette.lemon, 1);
  canvas.rect(15, 8, 5, 2, ...palette.belly, 1);
  canvas.rect(11, 22, 10, 2, ...palette.gold, 1);
});
icon('tide_pearl', canvas => {
  canvas.rect(9, 4, 14, 24, ...palette.skinDark, 1);
  canvas.rect(5, 8, 22, 16, ...palette.skinDark, 1);
  canvas.rect(8, 8, 16, 16, ...palette.skin, 1);
  canvas.rect(10, 6, 12, 20, ...palette.pearl, 1);
  canvas.rect(7, 10, 17, 11, ...palette.pearl, 1);
  canvas.rect(11, 8, 5, 5, ...palette.porcelain, 1);
  canvas.rect(19, 16, 3, 7, ...palette.skin, 1);
});

writeJson(path.join(data, 'recipes/tide_banquet.json'), {
  type: 'minecraft:crafting_shaped', pattern: ['ABC', 'D#E', 'FGH'], key: Object.fromEntries([
    ['A', 'minecraft:cooked_cod'], ['B', 'minecraft:cooked_salmon'], ['C', 'minecraft:pufferfish'],
    ['D', 'minecraft:nautilus_shell'], ['#', 'farmingtales:gold_bucket'], ['E', 'minecraft:heart_of_the_sea'],
    ['F', 'minecraft:prismarine_crystals'], ['G', 'minecraft:dried_kelp_block'], ['H', 'minecraft:gold_block']
  ].map(([key, item]) => [key, { item }])), result: { item: 'ftboss:tide_banquet' }
});
writeJson(path.join(data, 'loot_tables/blocks/statue_tide_fish.json'), {
  type: 'minecraft:block', pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'ftboss:statue_tide_fish' }],
    conditions: [{ condition: 'minecraft:survives_explosion' }, { condition: 'minecraft:block_state_property',
      block: 'ftboss:statue_tide_fish', properties: { half: 'lower' } }] }]
});
writeJson(path.join(data, 'loot_tables/entities/tide_fish_boss.json'), {
  type: 'minecraft:entity', pools: [{ rolls: 1, entries: [{ type: 'minecraft:item', name: 'ftboss:tide_pearl',
    functions: [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: 2, max: 4 } }] }] },
  { rolls: 1, entries: [{ type: 'minecraft:item', name: 'minecraft:cooked_salmon',
    functions: [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: 6, max: 10 } }] }] }]
});
for (const name of ['reef_fish', 'brine_puffer']) writeJson(path.join(data, `loot_tables/entities/${name}.json`), { type: 'minecraft:entity', pools: [] });
writeJson(path.join(data, 'recipes/tide_trident_recast.json'), { type: 'minecraft:crafting_shapeless',
  ingredients: [{ item: 'ftboss:tide_pearl' }, { item: 'ftboss:tide_pearl' }, { item: 'ftboss:tide_pearl' },
    { item: 'minecraft:trident' }, { item: 'minecraft:prismarine_crystals' }], result: { item: 'ftboss:tide_trident' } });

const languages = {
  zh_cn: {
    'entity.ftboss.tide_fish_boss': '潮宴鱼王·沧鳞', 'entity.ftboss.reef_fish': '香煎飞鱼卫',
    'entity.ftboss.brine_puffer': '盐渍河豚', 'entity.ftboss.tide_bolt': '潮骨水刃',
    'item.ftboss.tide_banquet': '深海御膳', 'item.ftboss.tide_trident': '潮骨三叉戟',
    'item.ftboss.tide_pearl': '潮汐珍珠', 'block.ftboss.statue_tide_fish': '潮宴鱼王石像',
    'tooltip.ftboss.tide_banquet': '主世界右键召唤潮宴鱼王；需要前方有平坦空地，成功后消耗。',
    'tooltip.ftboss.tide_trident': '右键：三重潮刃，每道12点伤害，减速2.5秒；冷却4秒。',
    'tooltip.ftboss.tide_trident_shift': '潜行右键：回潮，5格内16点伤害与牵引，水下呼吸10秒；冷却10秒。',
    'message.ftboss.tide_no_room': '前方空间不足：请找平坦空地，至少留出3×4×3空间；未消耗召唤物。',
    'message.ftboss.tide_sweep': '潮宴鱼王：摆尾分潮！退开或绕到身后！',
    'message.ftboss.tide_volley': '潮宴鱼王：鱼骨齐射！横向闪避！',
    'message.ftboss.tide_dash': '潮宴鱼王：破浪冲盘！离开预警直线！',
    'message.ftboss.tide_summon': '潮宴鱼王：御膳开席！鱼卫与河豚加入战斗！',
    'message.ftboss.tide_wave': '潮宴鱼王：翻海浪环！跳过扩散的水环！',
    'message.ftboss.tide_maelstrom': '潮宴鱼王：沸海涡流！离开脚下预警圈！',
    'message.ftboss.tide_phase': '潮宴鱼王进入二阶段：炙潮盛宴！'
  },
  en_us: {
    'entity.ftboss.tide_fish_boss': 'Tide Banquet Sovereign', 'entity.ftboss.reef_fish': 'Seared Reef Guard',
    'entity.ftboss.brine_puffer': 'Brined Puffer', 'entity.ftboss.tide_bolt': 'Tidebone Bolt',
    'item.ftboss.tide_banquet': 'Abyssal Banquet', 'item.ftboss.tide_trident': 'Tidebone Trident',
    'item.ftboss.tide_pearl': 'Tidal Pearl', 'block.ftboss.statue_tide_fish': 'Tide Sovereign Statue',
    'tooltip.ftboss.tide_banquet': 'Summons the Sovereign in clear Overworld terrain; consumed only on success.',
    'tooltip.ftboss.tide_trident': 'Use: three tide blades, 12 damage each and 2.5s Slowness. Cooldown: 4s.',
    'tooltip.ftboss.tide_trident_shift': 'Sneak-use: 5-block undertow, 16 damage, pull and 10s Water Breathing. Cooldown: 10s.',
    'message.ftboss.tide_no_room': 'Not enough room ahead. Find level ground with 3x4x3 clearance; item not consumed.',
    'message.ftboss.tide_sweep': 'Tail sweep! Retreat or move behind the Sovereign!',
    'message.ftboss.tide_volley': 'Fishbone volley! Dodge sideways!',
    'message.ftboss.tide_dash': 'Breaking-wave charge! Leave the marked line!',
    'message.ftboss.tide_summon': 'The banquet begins! Reef guards and puffers arrive!',
    'message.ftboss.tide_wave': 'Rising tide! Jump over the expanding wave!',
    'message.ftboss.tide_maelstrom': 'Boiling maelstrom! Leave the marked circle!',
    'message.ftboss.tide_phase': 'Phase two: the Searing Banquet!'
  }
};
for (const [language, entries] of Object.entries(languages)) {
  const file = path.join(assets, `lang/${language}.json`);
  const text = fs.readFileSync(file, 'utf8').trimEnd();
  const existing = JSON.parse(text);
  const missing = Object.entries(entries).filter(([key]) => !(key in existing));
  if (missing.length) fs.writeFileSync(file, text.slice(0, text.lastIndexOf('}')).trimEnd()
    + ',\n' + missing.map(([key, value]) => `  ${JSON.stringify(key)}: ${JSON.stringify(value)}`).join(',\n') + '\n}\n');
}
require('./tide_fish_rework.cjs').enhance(assets);
require('./tide_fish_rewards.cjs').update(path.join(root, 'src/main/resources'));
console.log('Generated Tide Sovereign, two minions, 18 boss animations, statue, weapon, summon, recipes and loot.');
