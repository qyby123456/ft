const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const root = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(root, 'src/main/resources/assets/ftboss'));
const id = 'qingyi_confluence_trident';
const width = 256;
const height = 128;
const canvas = new Canvas(width, height);
const glow = new Canvas(width, height);
const materials = {
  shaft: [36, 62, 75], steel: [94, 151, 167], edge: [184, 222, 221],
  gold: [178, 145, 84], wrap: [30, 44, 58], storm: [58, 220, 239],
  desecrator: [216, 67, 111], deep: [107, 94, 214], stellar: [249, 209, 108]
};
const luminous = new Set(['storm', 'desecrator', 'deep', 'stellar']);
const bones = [];
let packX = 2;
let packY = 2;
let rowHeight = 0;
let cubeCount = 0;

function bone(name, parent, pivot) {
  const result = { name, pivot, cubes: [] };
  if (parent) result.parent = parent;
  bones.push(result);
  return result;
}

function paintFace(originX, originY, faceWidth, faceHeight, material, faceIndex) {
  const color = materials[material];
  for (let pixelY = 0; pixelY < faceHeight; pixelY++) {
    for (let pixelX = 0; pixelX < faceWidth; pixelX++) {
      const edge = pixelX === 0 || pixelY === 0 || pixelX === faceWidth - 1 || pixelY === faceHeight - 1;
      let shade = [1.15, 0.66, 0.77, 1.06, 0.85, 0.92][faceIndex];
      if (edge && faceWidth > 2 && faceHeight > 2) shade *= pixelY === 0 ? 1.15 : 0.68;
      if (material === 'wrap') shade *= (pixelY + pixelX) % 4 < 2 ? 1.22 : 0.8;
      if (material === 'shaft' && pixelX === Math.floor(faceWidth / 2)) shade *= 1.24;
      if (luminous.has(material)) shade *= pixelX === Math.floor(faceWidth / 2) ? 1.28 : 0.94;
      const rgb = color.map(channel => Math.max(0, Math.min(255, Math.round(channel * shade))));
      canvas.set(originX + pixelX, originY + pixelY, ...rgb, 1);
      if (luminous.has(material)) glow.set(originX + pixelX, originY + pixelY, ...rgb, 1);
    }
  }
}

function cube(parent, origin, size, material, rotation, pivot) {
  const [sizeX, sizeY, sizeZ] = size;
  const boxWidth = 2 * (sizeX + sizeZ);
  const boxHeight = sizeY + sizeZ;
  if (packX + boxWidth + 2 > width) {
    packX = 2;
    packY += rowHeight + 2;
    rowHeight = 0;
  }
  if (packY + boxHeight + 2 > height) throw new Error('Texture atlas overflow');
  const shape = { origin, size, uv: [packX, packY], inflate: 0.015 };
  if (rotation) { shape.rotation = rotation; shape.pivot = pivot || origin.map((value, axis) => value + size[axis] / 2); }
  parent.cubes.push(shape);
  const faces = [
    [sizeZ, 0, sizeX, sizeZ], [sizeZ + sizeX, 0, sizeX, sizeZ],
    [0, sizeZ, sizeZ, sizeY], [sizeZ, sizeZ, sizeX, sizeY],
    [sizeZ + sizeX, sizeZ, sizeZ, sizeY], [2 * sizeZ + sizeX, sizeZ, sizeX, sizeY]
  ];
  faces.forEach(([offsetX, offsetY, faceWidth, faceHeight], faceIndex) =>
    paintFace(packX + offsetX, packY + offsetY, faceWidth, faceHeight, material, faceIndex));
  packX += boxWidth + 2;
  rowHeight = Math.max(rowHeight, boxHeight);
  cubeCount++;
}

bone('root', null, [0, 0, 0]);
const shaft = bone('shaft', 'root', [0, 0, 0]);
cube(shaft, [-1, -16, -1], [2, 38, 2], 'shaft');
for (const bandY of [-12, -8, -4, 0, 4]) cube(shaft, [-1.5, bandY, -1.5], [3, 2, 3], 'wrap');
for (const bandY of [-15, 8, 18]) cube(shaft, [-2, bandY, -2], [4, 2, 4], 'gold');
cube(shaft, [-0.5, 11, -1.6], [1, 5, 1], 'deep');
const pommel = bone('pommel', 'shaft', [0, -16, 0]);
cube(pommel, [-1.5, -18, -1.5], [3, 3, 3], 'steel');
cube(pommel, [-0.5, -21, -0.5], [1, 4, 1], 'edge');
const head = bone('head', 'shaft', [0, 19, 0]);
cube(head, [-3, 20, -2], [6, 7, 4], 'steel');
cube(head, [-7, 24, -1.5], [14, 3, 3], 'gold');
cube(head, [-2, 19, -2.5], [4, 2, 5], 'gold');
const spine = bone('stellar_blade', 'head', [0, 25, 0]);
cube(spine, [-2, 26, -1], [4, 12, 2], 'steel');
cube(spine, [-1, 36, -1.25], [2, 9, 2], 'edge');
cube(spine, [-0.5, 43, -0.5], [1, 4, 1], 'stellar');
cube(spine, [-0.5, 29, -1.7], [1, 10, 1], 'stellar');
for (const side of [-1, 1]) {
  const suffix = side < 0 ? 'l' : 'r';
  const material = side < 0 ? 'storm' : 'desecrator';
  const prong = bone(`prong_${suffix}`, 'head', [side * 5, 25, 0]);
  cube(prong, [side < 0 ? -9 : 5, 25, -1.5], [4, 8, 3], 'steel');
  cube(prong, [side < 0 ? -9 : 7, 31, -1], [2, 8, 2], 'edge');
  cube(prong, [side < 0 ? -8.5 : 7.5, 37, -0.5], [1, 5, 1], material);
  cube(prong, [side < 0 ? -7.5 : 6.5, 28, -2.1], [1, 7, 1], material);
  const fin = bone(`fin_${suffix}`, 'prong_' + suffix, [side * 6, 26, 0]);
  cube(fin, [side < 0 ? -10 : 6, 23, -1], [4, 2, 2], 'steel', [0, 0, side * 25]);
  cube(fin, [side < 0 ? -11 : 9, 22, -0.5], [2, 2, 1], material, [0, 0, side * 25]);
  const socket = bone(`socket_${suffix}`, 'head', [side * 4.5, 26, -2]);
  cube(socket, [side * 4.5 - 1.5, 24.5, -3], [3, 3, 2], 'wrap');
  const core = bone(`${material}_core`, `socket_${suffix}`, [side * 4.5, 26, -3]);
  cube(core, [side * 4.5 - 1, 25, -3.6], [2, 2, 1], material, [0, 0, 45]);
}
const ring = bone('astrolabe', 'head', [0, 24, -3]);
for (let segment = 0; segment < 8; segment++) {
  const angle = segment * Math.PI / 4;
  cube(ring, [Math.cos(angle) * 3.4 - 0.5, 24 + Math.sin(angle) * 3.4 - 0.5, -3.1],
    [1, 2, 1], 'gold', [0, 0, segment * 45], [Math.cos(angle) * 3.4, 24 + Math.sin(angle) * 3.4, -2.6]);
}
const stellar = bone('stellar_core', 'head', [0, 24, -3.5]);
cube(stellar, [-1, 23, -3.9], [2, 2, 1], 'stellar', [0, 0, 45]);
const deep = bone('deep_core', 'head', [0, 20, -3]);
cube(deep, [-1, 19, -3.4], [2, 2, 1], 'deep', [0, 0, 45]);
const conduit = bone('conduit', 'head', [0, 21, 0]);
cube(conduit, [-0.5, 21, -2.8], [1, 5, 1], 'deep');

const geometry = {
  format_version: '1.12.0',
  'minecraft:geometry': [{
    description: { identifier: `geometry.ftboss.${id}`, texture_width: width, texture_height: height,
      visible_bounds_width: 5, visible_bounds_height: 6, visible_bounds_offset: [0, 1, 0] },
    bones
  }]
};

function keys(entries) {
  const result = {};
  for (let index = 0; index < entries.length - 1; index++) {
    const [start, startValue] = entries[index];
    const [end, endValue] = entries[index + 1];
    for (let sample = 0; sample < 4; sample++) {
      const progress = sample / 4;
      const blend = progress * progress * (3 - 2 * progress);
      result[(start + (end - start) * progress).toFixed(4)] = startValue.map((value, axis) =>
        Number((value + (endValue[axis] - value) * blend).toFixed(4)));
    }
  }
  const [last, value] = entries[entries.length - 1];
  result[last.toFixed(4)] = value;
  return result;
}

const idle = { loop: true, animation_length: 2.4, bones: {
  astrolabe: { rotation: keys([[0, [0, 0, -7]], [1.2, [0, 0, 7]], [2.4, [0, 0, -7]]]) },
  stellar_core: { rotation: keys([[0, [0, -8, 0]], [1.2, [0, 8, 0]], [2.4, [0, -8, 0]]]) },
  deep_core: { position: keys([[0, [0, 0, 0]], [1.2, [0, 0.35, -0.15]], [2.4, [0, 0, 0]]]) }
} };
const attack = { loop: false, animation_length: 0.5, bones: {
  root: { rotation: keys([[0, [0, 0, 0]], [0.12, [-12, 5, -15]], [0.25, [18, -5, 23]], [0.5, [0, 0, 0]]]) },
  stellar_blade: { position: keys([[0, [0, 0, 0]], [0.25, [0, 1.6, 0]], [0.5, [0, 0, 0]]]) },
  astrolabe: { rotation: keys([[0, [0, 0, -7]], [0.25, [0, 0, 40]], [0.5, [0, 0, -7]]]) }
} };
const charge = { loop: 'hold_on_last_frame', animation_length: 1.6, bones: {
  root: { rotation: keys([[0, [0, 0, 0]], [0.5, [-6, 0, 0]], [1.6, [-6, 0, 0]]]) },
  astrolabe: { rotation: keys([[0, [0, 0, -7]], [0.8, [0, 0, 90]], [1.6, [0, 0, 270]]]) },
  stellar_blade: { position: keys([[0, [0, 0, 0]], [1.2, [0, -2, 0]], [1.6, [0, -2, 0]]]) },
  stellar_core: { position: keys([[0, [0, 0, 0]], [1.6, [0, 0, -1]]]) },
  deep_core: { position: keys([[0, [0, 0, 0]], [1.6, [0, 1.2, -0.6]]]) }
} };
const release = { loop: false, animation_length: 0.8, bones: {
  root: { rotation: keys([[0, [-6, 0, 0]], [0.2, [14, 0, 0]], [0.38, [-4, 0, 0]], [0.8, [0, 0, 0]]]) },
  stellar_blade: { position: keys([[0, [0, -2, 0]], [0.2, [0, 3, 0]], [0.5, [0, 0, 0]], [0.8, [0, 0, 0]]]) },
  astrolabe: { rotation: keys([[0, [0, 0, 270]], [0.2, [0, 0, 353]], [0.8, [0, 0, 353]]]) },
  stellar_core: { position: keys([[0, [0, 0, -1]], [0.2, [0, 0, -2]], [0.8, [0, 0, 0]]]) },
  deep_core: { position: keys([[0, [0, 1.2, -0.6]], [0.2, [0, -1, -1.6]], [0.8, [0, 0, 0]]]) }
} };
for (const side of [-1, 1]) {
  const suffix = side < 0 ? 'l' : 'r';
  const core = side < 0 ? 'storm_core' : 'desecrator_core';
  idle.bones[`fin_${suffix}`] = { rotation: keys([[0, [0, side * -3, 0]], [1.2, [0, side * 3, 0]], [2.4, [0, side * -3, 0]]]) };
  attack.bones[`prong_${suffix}`] = { rotation: keys([[0, [0, 0, 0]], [0.25, [0, 0, -side * 8]], [0.5, [0, 0, 0]]]) };
  charge.bones[`prong_${suffix}`] = { rotation: keys([[0, [0, 0, 0]], [0.8, [0, 0, -side * 22]], [1.6, [0, 0, -side * 22]]]) };
  charge.bones[`fin_${suffix}`] = { rotation: keys([[0, [0, side * -3, 0]], [1.6, [0, side * 35, -side * 15]]]) };
  charge.bones[core] = { position: keys([[0, [0, 0, 0]], [1.6, [-side * 1.4, 0, -1]]]) };
  release.bones[`prong_${suffix}`] = { rotation: keys([[0, [0, 0, -side * 22]], [0.2, [0, 0, side * 5]], [0.5, [0, 0, -side * 8]], [0.8, [0, 0, 0]]]) };
  release.bones[`fin_${suffix}`] = { rotation: keys([[0, [0, side * 35, -side * 15]], [0.2, [0, side * 55, -side * 30]], [0.8, [0, side * -3, 0]]]) };
  release.bones[core] = { position: keys([[0, [-side * 1.4, 0, -1]], [0.2, [side, 0, -2]], [0.8, [0, 0, 0]]]) };
}
const animations = { format_version: '1.8.0', animations: {} };
for (const [name, data] of Object.entries({ idle, attack, charge, release })) {
  animations.animations[`animation.qingyi_confluence.${name}`] = data;
}

function write(relative, value) {
  const destination = path.join(output, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, JSON.stringify(value, null, 2) + '\n');
}
write(`geo/${id}.geo.json`, geometry);
write(`animations/${id}.animation.json`, animations);
write(`models/item/${id}.json`, {
  parent: 'builtin/entity', gui_light: 'front', textures: { particle: `ftboss:item/${id}` },
  display: {
    thirdperson_righthand: { rotation: [0, 90, 15], translation: [0, 0, 1], scale: [0.65, 0.65, 0.65] },
    thirdperson_lefthand: { rotation: [0, -90, -15], translation: [0, 0, 1], scale: [0.65, 0.65, 0.65] },
    firstperson_righthand: { rotation: [0, -90, 10], translation: [1, 1, -1], scale: [0.6, 0.6, 0.6] },
    firstperson_lefthand: { rotation: [0, 90, -10], translation: [1, 1, -1], scale: [0.6, 0.6, 0.6] },
    gui: { rotation: [12, 25, -35], translation: [0, -2.2, 0], scale: [0.22, 0.22, 0.22] },
    ground: { rotation: [0, 0, 90], translation: [0, 2, 0], scale: [0.24, 0.24, 0.24] },
    fixed: { rotation: [0, 0, -35], translation: [0, -2, -1], scale: [0.23, 0.23, 0.23] },
    head: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] }
  }
});
fs.mkdirSync(path.join(output, 'textures/item'), { recursive: true });
canvas.save(path.join(output, `textures/item/${id}.png`));
glow.save(path.join(output, `textures/item/${id}_glowmask.png`));
console.log(`${id}: ${bones.length} bones, ${cubeCount} cubes, four animations; atlas ${width}x${height}, used ${packY + rowHeight}px high`);
