'use strict';
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { decodePNG, encodePNG } = require('../../png.cjs');
const { createCanvas, GlobalFonts, loadImage } = require(path.join(process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules', '@napi-rs/canvas'));
const root = path.resolve(__dirname, '../../../..');
const output = path.join(root, 'art/boss_materials/refine/capacitor');
GlobalFonts.registerFromPath('C:/Windows/Fonts/msyh.ttc', 'Yahei');

function rotate(point, axis, angle, origin = [0, 0, 0], rescale = false) {
  const radians = angle * Math.PI / 180, cosine = Math.cos(radians), sine = Math.sin(radians);
  const local = point.map((coordinate, index) => coordinate - origin[index]);
  const pair = axis === 'x' ? [1, 2] : axis === 'y' ? [2, 0] : [0, 1];
  const first = local[pair[0]], second = local[pair[1]];
  local[pair[0]] = first * cosine - second * sine;
  local[pair[1]] = first * sine + second * cosine;
  if (rescale) for (const index of pair) local[index] /= cosine;
  return local.map((coordinate, index) => coordinate + origin[index]);
}

function corners(element) {
  const points = [];
  for (const horizontal of [element.from[0], element.to[0]]) for (const vertical of [element.from[1], element.to[1]]) for (const depth of [element.from[2], element.to[2]]) {
    const point = [horizontal, vertical, depth];
    points.push(element.rotation ? rotate(point, element.rotation.axis, element.rotation.angle, element.rotation.origin, element.rotation.rescale) : point);
  }
  return points;
}

const faceIndices = {
  north: [2, 6, 4, 0], south: [7, 3, 1, 5], east: [6, 7, 5, 4],
  west: [3, 2, 0, 1], up: [3, 7, 6, 2], down: [0, 4, 5, 1]
};
const javaIndices = {
  north: [6, 4, 0, 2], south: [3, 1, 5, 7], east: [7, 5, 4, 6],
  west: [2, 0, 1, 3], up: [2, 3, 7, 6], down: [1, 0, 4, 5]
};
function cross(first, second) {
  return [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
}
function parts(model, texture) {
  return model.elements.map(element => ({ points: corners(element), shade: element.shade !== false,
    faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => [side, {
      uv: face.uv.map((coordinate, index) => coordinate * (index % 2 ? texture.h : texture.w) / 16), rotation: face.rotation || 0
    }])) }));
}
function raster(entries, texture, options = {}) {
  const width = options.width || 640, height = options.height || width;
  const center = options.center || [8, 8, 8], zoom = options.zoom || width / 17;
  const pixels = Buffer.alloc(width * height * 4), depth = new Float64Array(width * height).fill(Infinity);
  const camera = point => rotate(rotate(point.map((coordinate, index) => coordinate - center[index]), 'y', options.yaw || 0), 'x', options.pitch || 0);
  function triangle(vertices, coordinates, brightness) {
    const [first, second, third] = vertices;
    const determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(determinant) < 1e-8) return;
    const minimumX = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[0]))));
    const maximumX = Math.min(width - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[0]))));
    const minimumY = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[1]))));
    const maximumY = Math.min(height - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[1]))));
    for (let pixelY = minimumY; pixelY <= maximumY; pixelY++) for (let pixelX = minimumX; pixelX <= maximumX; pixelX++) {
      const sampleX = pixelX + 0.5, sampleY = pixelY + 0.5;
      const firstWeight = ((second[1] - third[1]) * (sampleX - third[0]) + (third[0] - second[0]) * (sampleY - third[1])) / determinant;
      const secondWeight = ((third[1] - first[1]) * (sampleX - third[0]) + (first[0] - third[0]) * (sampleY - third[1])) / determinant;
      const weights = [firstWeight, secondWeight, 1 - firstWeight - secondWeight];
      if (Math.min(...weights) < -1e-8) continue;
      const distance = vertices.reduce((sum, vertex, index) => sum + vertex[2] * weights[index], 0), pixelIndex = pixelY * width + pixelX;
      if (distance >= depth[pixelIndex] - 1e-7) continue;
      const sampleU = coordinates.reduce((sum, coordinate, index) => sum + coordinate[0] * weights[index], 0);
      const sampleV = coordinates.reduce((sum, coordinate, index) => sum + coordinate[1] * weights[index], 0);
      const textureX = Math.max(0, Math.min(texture.w - 1, Math.floor(sampleU))), textureY = Math.max(0, Math.min(texture.h - 1, Math.floor(sampleV)));
      const source = (textureY * texture.w + textureX) * 4, destination = pixelIndex * 4;
      if (texture.data[source + 3] < 128) continue;
      for (let channel = 0; channel < 3; channel++) pixels[destination + channel] = Math.round(Math.min(255, texture.data[source + channel] * brightness));
      pixels[destination + 3] = texture.data[source + 3]; depth[pixelIndex] = distance;
    }
  }
  for (const entry of entries) {
    const cameraPoints = entry.points.map(camera);
    for (const [side, face] of Object.entries(entry.faces)) {
      if (!faceIndices[side]) continue;
      const vertices = faceIndices[side].map(index => cameraPoints[index]);
      const normal = cross(vertices[1].map((coordinate, index) => coordinate - vertices[0][index]), vertices[2].map((coordinate, index) => coordinate - vertices[1][index]));
      const length = Math.hypot(...normal);
      if (length < 1e-8 || normal[2] >= -1e-8) continue;
      const brightness = entry.shade === false ? 1 : 0.73 + 0.27 * Math.max(0, (-0.25 * normal[0] + 0.7 * normal[1] - 0.65 * normal[2]) / length);
      const [minimumU, minimumV, maximumU, maximumV] = face.uv;
      const javaUV = [[minimumU, minimumV], [minimumU, maximumV], [maximumU, maximumV], [maximumU, minimumV]];
      const coordinates = faceIndices[side].map(vertex => javaUV[(javaIndices[side].indexOf(vertex) + (face.rotation || 0) / 90) % 4]);
      const projected = vertices.map(vertex => [width / 2 + vertex[0] * zoom, height / 2 - vertex[1] * zoom, vertex[2]]);
      triangle([projected[0], projected[1], projected[2]], [coordinates[0], coordinates[1], coordinates[2]], brightness);
      triangle([projected[0], projected[2], projected[3]], [coordinates[0], coordinates[2], coordinates[3]], brightness);
    }
  }
  return { width, height, pixels, png: encodePNG(width, height, pixels) };
}

async function sheet(filename, items, columns, cellWidth = 480, cellHeight = 520, heading = '') {
  const canvas = createCanvas(columns * cellWidth, Math.ceil(items.length / columns) * cellHeight + 76), context = canvas.getContext('2d');
  context.fillStyle = '#101c27'; context.fillRect(0, 0, canvas.width, canvas.height);
  context.fillStyle = '#e8e4cf'; context.font = 'bold 26px Yahei'; context.fillText(heading, 24, 38);
  context.fillStyle = '#7ca6b6'; context.font = '14px Yahei'; context.fillText('FTBOSS  /  CAPACITOR MATERIAL  /  Runtime assets · nearest UV sampling', 24, 62);
  for (const [index, item] of items.entries()) {
    const left = index % columns * cellWidth, top = Math.floor(index / columns) * cellHeight + 76;
    context.fillStyle = '#152533'; context.fillRect(left + 8, top + 8, cellWidth - 16, cellHeight - 16);
    const image = await loadImage(item.png || item.path), fit = Math.min((cellWidth - 32) / image.width, (cellHeight - 66) / image.height);
    context.imageSmoothingEnabled = false; context.drawImage(image, left + (cellWidth - image.width * fit) / 2, top + 12, image.width * fit, image.height * fit);
    context.font = 'bold 20px Yahei'; context.fillStyle = '#dfdcc9'; context.fillText(item.label, left + 24, top + cellHeight - 25);
  }
  fs.writeFileSync(path.join(output, filename), canvas.toBuffer('image/png'));
}
async function main() {
  fs.mkdirSync(output, { recursive: true });
  const base = path.join(root, 'src/main/resources/assets/ftboss');
  const model = JSON.parse(fs.readFileSync(path.join(base, 'models/item/boss_materials/capacitor_material.json')));
  const texture = decodePNG(path.join(base, 'textures/item/boss_materials/capacitor.png')), entries = parts(model, texture);
  const views = [['front', 0, '正面 / NORTH'], ['back', 180, '背面 / SOUTH'], ['left', -90, '左侧 / WEST'], ['right', 90, '右侧 / EAST']];
  const images = views.map(([name, yaw, label]) => {
    const image = raster(entries, texture, { width: 480, height: 720, yaw, center: [8, 8.1, 8], zoom: 55 });
    fs.writeFileSync(path.join(output, name + '.png'), image.png); return { png: image.png, label };
  });
  await sheet('four-views.png', images, 4, 410, 620, '蓄能电芯配件 · 最终运行时四视图');
  const oblique = raster(entries, texture, { width: 960, height: 960, yaw: -34, pitch: -19, zoom: 64 });
  const reverse = raster(entries, texture, { width: 960, height: 960, yaw: 145, pitch: -17, zoom: 64 });
  fs.writeFileSync(path.join(output, 'oblique.png'), oblique.png); fs.writeFileSync(path.join(output, 'reverse-oblique.png'), reverse.png);
  await sheet('detail-and-icon.png', [{ png: oblique.png, label: '斜视 · 外壳 / 绕组 / 端子' }, { png: reverse.png, label: '背部 · 散热盖 / 汇流导线' },
    { path: path.join(base, 'textures/item/capacitor_material.png'), label: 'GUI 图标 · 32×32 最近邻放大' }], 3, 520, 640, '蓄能电芯配件 · 结构与图标对应');
  const icon = await loadImage(path.join(base, 'textures/item/capacitor_material.png'));
  const comparison = createCanvas(800, 380), context = comparison.getContext('2d');
  context.fillStyle = '#152533'; context.fillRect(0, 0, 800, 380); context.imageSmoothingEnabled = false;
  context.drawImage(icon, 32, 90, 32, 32); context.drawImage(icon, 105, 90, 16, 16); context.drawImage(icon, 220, 40, 320, 320);
  context.fillStyle = '#e8e4cf'; context.font = '18px Yahei'; context.fillText('32px 原尺寸', 20, 160); context.fillText('16px GUI', 110, 200); context.fillText('10× 放大', 580, 200);
  fs.writeFileSync(path.join(output, 'icon-sizes.png'), comparison.toBuffer('image/png'));
  const backup = path.join(root, '_work/boss-materials-refine-20261021/capacitor/backup-before-refine/src/main/resources/assets/ftboss');
  const previous = JSON.parse(fs.readFileSync(path.join(backup, 'models/item/boss_materials/capacitor_material.json'))), previousTexture = decodePNG(path.join(backup, 'textures/item/boss_materials/capacitor.png'));
  const original = raster(parts(previous, previousTexture), previousTexture, { width: 640, height: 640, yaw: -34, pitch: -19, zoom: 42.666667 });
  await sheet('before-after.png', [{ png: original.png, label: '重制前 · 19 个直角元素' }, { png: oblique.png, label: '重制后 · 可拆装的蓄能材料' }], 2, 600, 680, '同一物品 ID · 轮廓与机械结构重制');
  const sha256 = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
  const inputs = ['models/item/boss_materials/capacitor_material.json', 'textures/item/boss_materials/capacitor.png', 'textures/item/capacitor_material.png'];
  const imageNames = ['front.png', 'back.png', 'left.png', 'right.png', 'four-views.png', 'oblique.png', 'reverse-oblique.png', 'detail-and-icon.png', 'icon-sizes.png', 'before-after.png'];
  fs.writeFileSync(path.join(root, '_work/boss-materials-refine-20261021/capacitor/preview-manifest.json'), JSON.stringify({
    inputSha256: Object.fromEntries(inputs.map(filename => [filename, sha256(path.join(base, filename))])),
    imageSha256: Object.fromEntries(imageNames.map(filename => [filename, sha256(path.join(output, filename))])),
    renderer: 'opaque depth buffer; cutout alpha>=128; nearest texel; Java FaceInfo/BlockFaceUV corner mapping; legal-axis rotation and rescale',
    views: { front: 0, back: 180, left: -90, right: 90 }, center: [8, 8.1, 8]
  }, null, 2) + '\n');
  console.log('Rendered final runtime JSON + atlas: front/back/left/right + oblique/icon comparison.');
}
module.exports = { root, output, raster, parts, corners, rotate, sheet };
if (require.main === module) main().catch(error => { console.error(error); process.exitCode = 1; });
