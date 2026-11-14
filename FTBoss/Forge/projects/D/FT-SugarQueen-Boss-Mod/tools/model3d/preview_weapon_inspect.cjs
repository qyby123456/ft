const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG } = require('./png.cjs');
const space = require('./cube_space.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const samples = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const output = path.resolve(process.argv[3]);
const manor = process.argv.includes('--manor');
const newWeapons = process.argv.includes('--new-weapons');
const gui = process.argv.includes('--gui');
const gourmet = process.argv.includes('--gourmet');
const qingyiDora = process.argv.includes('--qingyi-dora');
const cases = gourmet ? [
  ['gourmet_blade', 'gourmet_blank_blade', 'geo/gourmet_blank_blade.geo.json', 'textures/item/gourmet_blank_blade.png']
] : qingyiDora ? [
  ['qingyi_confluence', 'qingyi_confluence_trident', 'geo/qingyi_confluence_trident.geo.json', 'textures/item/qingyi_confluence_trident.png'],
  ['launcher', 'dora_nova_launcher'],
  ['launcher', 'dora_pulse_gun']
] : newWeapons ? [
  ['tide_trident', 'tide_trident'],
  ['launcher', 'dora_nova_launcher'],
  ['lance', 'dora_lance']
] : manor ? [
  ['scythe', 'dandan_scythe'],
  ['launcher', 'dora_nova_launcher_v1'],
  ['rifle', 'xuanlan_calibrum_v2'],
  ['chakram', 'xuanlan_crescendum_v2'],
  ['utensil', 'lemoncat/kitchen_shovel'],
  ['rolling_pin', 'xueli/rolling_pin'],
  ['feather', 'maque_blessing_feather'],
  ['figurine', 'statue_youzi']
] : [
  ['blade', 'shadow_sword', 'geo/shadow_sword.geo.json', 'textures/item/shadow_sword.png'],
  ['cleaver', 'black_cleaver', 'geo/black_cleaver.geo.json', 'textures/item/black_cleaver_geo.png'],
  ['heavy', 'cheese_wheel_mace'],
  ['club', 'wine_bottle_mace'],
  ['pan', 'lemoncat_skillet'],
  ['crossbow', 'barrel_crossbow'],
  ['relic', 'thorn_crown']
];
const faceIndices = {
  north: [6, 4, 0, 2], south: [3, 1, 5, 7],
  west: [2, 0, 1, 3], east: [7, 5, 4, 6],
  up: [3, 7, 6, 2], down: [0, 4, 5, 1]
};
const shades = { north: 0.95, south: 0.90, west: 0.73, east: 0.82, up: 1.10, down: 0.64 };
const width = 400;
const height = 240;
const header = 22;
const fractions = gui ? [0] : [0, 0.23, 0.38, 0.57, 0.76, 1];
const sheet = new Canvas(width * fractions.length, (height + header) * cases.length);
const textures = new Map();
const read = relative => JSON.parse(fs.readFileSync(path.join(assets, relative), 'utf8'));

function modelFor(identifier) {
  const model = read(`models/${identifier}.json`);
  if (model.parent?.startsWith('ftboss:')) {
    const parent = modelFor(model.parent.slice(7));
    return { ...parent, ...model, display: { ...parent.display, ...model.display }, textures: { ...parent.textures, ...model.textures } };
  }
  return model;
}

function textureFor(identifier) {
  const relative = identifier.startsWith('textures/') ? identifier : `textures/${identifier.replace('ftboss:', '')}.png`;
  if (!textures.has(relative)) textures.set(relative, decodePNG(path.join(assets, relative)));
  return textures.get(relative);
}

function geometryFor(entry) {
  const [, item, geoPath, texturePath] = entry;
  const model = modelFor(`item/${item}`);
  const faces = [];
  if (geoPath) {
    const geo = space.loadGeo(path.join(assets, geoPath), { geckolib: true });
    const texture = textureFor(texturePath);
    for (const info of space.listCubes(geo)) {
      if (info.bone.includes('glow')) continue;
      const corners = space.cubeCorners(geo, info).map(space.geoToItemBlocks);
      for (const [name, indices] of Object.entries(faceIndices)) {
        const uv = faceUV(info.cube, name);
        if (!uv) continue;
        const [startU, startV] = uv.uv;
        const [sizeU, sizeV] = uv.uv_size;
        const rectangle = [startU / geo.textureWidth, startV / geo.textureHeight,
          (startU + sizeU) / geo.textureWidth, (startV + sizeV) / geo.textureHeight];
        faces.push({ points: indices.map(index => corners[index]), rectangle, texture, shade: shades[name] });
      }
    }
  } else {
    const geometry = { geckolib: false, bones: [{ name: 'root' }], byName: new Map([['root', { name: 'root' }]]) };
    for (const element of model.elements || []) {
      const cube = { origin: element.from, size: element.to.map((value, axis) => value - element.from[axis]) };
      if (element.rotation) {
        cube.pivot = element.rotation.origin;
        cube.rotation = ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0);
      }
      const corners = space.cubeCorners(geometry, { name: item, bone: 'root', cube }).map(point => point.map(value => value / 16 - 0.5));
      for (const [name, face] of Object.entries(element.faces)) {
        if (!face.uv) continue;
        let identifier = face.texture;
        while (identifier.startsWith('#')) identifier = model.textures[identifier.slice(1)];
        faces.push({ points: faceIndices[name].map(index => corners[index]), rectangle: face.uv.map(value => value / 16),
          texture: textureFor(identifier), shade: shades[name], rotation: face.rotation || 0 });
      }
    }
  }
  return { faces, display: model.display[gui ? 'gui' : 'firstperson_righthand'] };
}

function faceUV(cube, name) {
  if (!Array.isArray(cube.uv)) return cube.uv?.[name];
  const [startU, startV] = cube.uv;
  const [width, height, depth] = cube.size;
  const rectangles = {
    up: [startU + depth, startV, width, depth],
    down: [startU + depth + width, startV, width, depth],
    east: [startU, startV + depth, depth, height],
    west: [startU + depth + width, startV + depth, depth, height],
    north: [startU + depth, startV + depth, width, height],
    south: [startU + depth + width + depth, startV + depth, width, height]
  };
  const rectangle = rectangles[name];
  return { uv: rectangle.slice(0, 2), uv_size: rectangle.slice(2) };
}

function rotatePose(point, rotation) {
  let result = space.rotateXYZ(point, [0, 0, rotation[2]]);
  result = space.rotateXYZ(result, [0, rotation[1], 0]);
  return space.rotateXYZ(result, [rotation[0], 0, 0]);
}

function triangle(canvas, depth, vertices, uv, texture, shade) {
  if (vertices.some(vertex => vertex[2] < 0.055)) return;
  const [first, second, third] = vertices;
  const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
  if (Math.abs(denominator) < 1e-6) return;
  const left = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[0]))));
  const right = Math.min(width - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[0]))));
  const top = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[1]))));
  const bottom = Math.min(height - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[1]))));
  for (let row = top; row <= bottom; row++) {
    for (let column = left; column <= right; column++) {
      const weightA = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / denominator;
      const weightB = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / denominator;
      const weightC = 1 - weightA - weightB;
      if (Math.min(weightA, weightB, weightC) < 0) continue;
      const weights = [weightA / first[2], weightB / second[2], weightC / third[2]];
      const reciprocal = weights.reduce((sum, value) => sum + value, 0);
      const distance = 1 / reciprocal;
      const pixel = row * width + column;
      if (distance >= depth[pixel]) continue;
      const texU = weights.reduce((sum, value, index) => sum + value * uv[index][0], 0) / reciprocal;
      const texV = weights.reduce((sum, value, index) => sum + value * uv[index][1], 0) / reciprocal;
      const texX = Math.max(0, Math.min(texture.w - 1, Math.floor(texU * texture.w)));
      const texY = Math.max(0, Math.min(texture.h - 1, Math.floor(texV * texture.h)));
      const texOffset = (texY * texture.w + texX) * 4;
      if (texture.data[texOffset + 3] < 128) continue;
      depth[pixel] = distance;
      canvas.set(column, row, texture.data[texOffset] * shade, texture.data[texOffset + 1] * shade, texture.data[texOffset + 2] * shade);
    }
  }
}

function render(model, sample) {
  const canvas = new Canvas(width, height);
  canvas.rect(0, 0, width, height, 38, 53, 68);
  canvas.rect(0, height * 0.53, width, height, 56, 61, 58);
  const depth = new Float64Array(width * height).fill(Infinity);
  const focal = height / (2 * Math.tan(70 * Math.PI / 360));
  const move = sample.slice(1, 4);
  const rotation = sample.slice(4, 7);
  for (const face of model.faces) {
    const points = face.points.map(point => {
      const scaled = point.map((value, axis) => value * model.display.scale[axis]);
      const displayed = rotatePose(scaled, model.display.rotation)
        .map((value, axis) => value + model.display.translation[axis] / 16);
      if (gui) return [width / 2 + displayed[0] * height, height / 2 - displayed[1] * height, 4 - displayed[2] * 0.001];
      const transformed = rotatePose(displayed, rotation);
      const camera = transformed.map((value, axis) => value + move[axis] + [0.56, -0.52, -0.72][axis]);
      return [width / 2 + camera[0] * focal / -camera[2], height / 2 - camera[1] * focal / -camera[2], -camera[2]];
    });
    const [left, top, right, bottom] = face.rectangle;
    const uv = [[left, top], [left, bottom], [right, bottom], [right, top]];
    for (let step = 0; step < (face.rotation || 0) / 90; step++) uv.unshift(uv.pop());
    for (const indices of [[0, 1, 2], [0, 2, 3]]) triangle(canvas, depth, indices.map(index => points[index]), indices.map(index => uv[index]), face.texture, face.shade);
  }
  if (!gui) {
    canvas.rect(width / 2 - 3, height / 2, 7, 1, 160, 176, 187);
    canvas.rect(width / 2, height / 2 - 3, 1, 7, 160, 176, 187);
  }
  return canvas;
}

fs.mkdirSync(output, { recursive: true });
for (const [caseIndex, entry] of cases.entries()) {
  const model = geometryFor(entry);
  const frames = samples[entry[0]];
  if (!model.faces.length || !model.display || !frames?.length) throw new Error(`Missing geometry, display or motion: ${entry[1]}`);
  for (const [column, fraction] of fractions.entries()) {
    const sample = frames[Math.round((frames.length - 1) * fraction)];
    const panel = render(model, sample);
    const originY = caseIndex * (height + header) + header;
    sheet.rect(column * width, originY - header, width, header, 16, 22, 30);
    for (let row = 0; row < height; row++) {
      const offset = ((originY + row) * sheet.w + column * width) * 4;
      sheet.d.set(panel.d.subarray(row * width * 4, (row + 1) * width * 4), offset);
    }
    panel.save(path.join(output, `${qingyiDora ? entry[1] : entry[0]}-${column}.png`));
  }
}
sheet.save(path.join(output, 'inspect-contact.png'));
fs.writeFileSync(path.join(output, 'preview-notes.json'), JSON.stringify({
  rows: cases.map(entry => ({ profile: entry[0], item: entry[1] })), columns: fractions,
  excluded: manor ? ['polearm: Tridents N Stuff renders Java BEWLR geometry, not JSON item geometry; requires in-game verification.'] : [],
  limitation: 'Offline motion/framing approximation, not a Minecraft screenshot. No player arms, item shaders or live GeckoLib effects.'
}, null, 2) + '\n');
console.log(`Saved textured motion reference sheet to ${output}`);
