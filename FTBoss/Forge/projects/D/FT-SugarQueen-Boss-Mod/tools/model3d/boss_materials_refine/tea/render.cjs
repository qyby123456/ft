const fs = require('fs');
const path = require('path');
const { encodePNG, decodePNG } = require('../../png.cjs');

const root = path.resolve(__dirname, '../../../..');
const art = path.join(root, 'art/boss_materials/refine/tea');
const modelFile = path.join(root, 'src/main/resources/assets/ftboss/models/item/boss_materials/tea_material.json');
const atlasFile = path.join(root, 'src/main/resources/assets/ftboss/textures/item/boss_materials/tea.png');
const dot = (first, second) => first.reduce((sum, value, index) => sum + value * second[index], 0);
const subtract = (first, second) => first.map((value, index) => value - second[index]);
const normalized = vector => vector.map(value => value / Math.hypot(...vector));

function transform(point, rotation) {
  if (!rotation) return [...point];
  const axis = { x: 0, y: 1, z: 2 }[rotation.axis];
  const first = (axis + 1) % 3;
  const second = (axis + 2) % 3;
  const angle = rotation.angle * Math.PI / 180;
  const moved = subtract(point, rotation.origin);
  if (rotation.rescale) {
    moved[first] /= Math.cos(angle);
    moved[second] /= Math.cos(angle);
  }
  const rotated = [...moved];
  rotated[first] = moved[first] * Math.cos(angle) - moved[second] * Math.sin(angle);
  rotated[second] = moved[first] * Math.sin(angle) + moved[second] * Math.cos(angle);
  return rotated.map((value, index) => value + rotation.origin[index]);
}

function faceGeometry(element) {
  const [left, bottom, front] = element.from;
  const [right, top, back] = element.to;
  return {
    north: { normal: [0, 0, -1], vertices: [[right, top, front], [left, top, front], [left, bottom, front], [right, bottom, front]] },
    south: { normal: [0, 0, 1], vertices: [[left, top, back], [right, top, back], [right, bottom, back], [left, bottom, back]] },
    east: { normal: [1, 0, 0], vertices: [[right, top, back], [right, top, front], [right, bottom, front], [right, bottom, back]] },
    west: { normal: [-1, 0, 0], vertices: [[left, top, front], [left, top, back], [left, bottom, back], [left, bottom, front]] },
    up: { normal: [0, 1, 0], vertices: [[left, top, front], [right, top, front], [right, top, back], [left, top, back]] },
    down: { normal: [0, -1, 0], vertices: [[left, bottom, back], [right, bottom, back], [right, bottom, front], [left, bottom, front]] }
  };
}

function mesh(model) {
  const faces = [];
  for (const element of model.elements) {
    const geometry = faceGeometry(element);
    for (const [direction, face] of Object.entries(element.faces)) {
      const shape = geometry[direction];
      const normalRotation = element.rotation ? { ...element.rotation, origin: [0, 0, 0], rescale: false } : null;
      const [umin, vmin, umax, vmax] = face.uv;
      let coordinates = [[umin, vmin], [umax, vmin], [umax, vmax], [umin, vmax]];
      const steps = (face.rotation || 0) / 90;
      coordinates = coordinates.map((coordinate, index) => coordinates[(index + steps) % 4]);
      faces.push({ name: element.name, normal: transform(shape.normal, normalRotation), vertices: shape.vertices.map(vertex => transform(vertex, element.rotation)), uv: coordinates });
    }
  }
  return faces;
}

function render(model, texture, options = {}) {
  const width = options.width || 512;
  const height = options.height || width;
  const image = { w: width, h: height, data: Buffer.alloc(width * height * 4) };
  const depthBuffer = new Float64Array(width * height).fill(-Infinity);
  const yaw = (options.yaw || 0) * Math.PI / 180;
  const pitch = (options.pitch || 0) * Math.PI / 180;
  const direction = [Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw) * Math.cos(pitch)];
  const horizontal = [-Math.cos(yaw), 0, -Math.sin(yaw)];
  const vertical = [-Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch), Math.cos(yaw) * Math.sin(pitch)];
  const light = normalized([-0.4, 0.8, -0.5]);
  const scale = options.scale || Math.min(width, height) / 17.6;
  const center = options.center || [8, 7.7, 8];
  const edge = (first, second, point) => (point[0] - first[0]) * (second[1] - first[1]) - (point[1] - first[1]) * (second[0] - first[0]);
  for (const face of mesh(model)) {
    if (dot(face.normal, direction) <= 0.00001) continue;
    const shade = 0.66 + Math.max(0, dot(face.normal, light)) * 0.34;
    const points = face.vertices.map(vertex => {
      const relative = subtract(vertex, center);
      return [width / 2 + dot(relative, horizontal) * scale, height / 2 - dot(relative, vertical) * scale, dot(relative, direction)];
    });
    for (const indices of [[0, 1, 2], [0, 2, 3]]) {
      const triangle = indices.map(index => points[index]);
      const denominator = edge(triangle[0], triangle[1], triangle[2]);
      if (Math.abs(denominator) < 0.000001) continue;
      const minX = Math.max(0, Math.floor(Math.min(...triangle.map(vertex => vertex[0]))));
      const maxX = Math.min(width - 1, Math.ceil(Math.max(...triangle.map(vertex => vertex[0]))));
      const minY = Math.max(0, Math.floor(Math.min(...triangle.map(vertex => vertex[1]))));
      const maxY = Math.min(height - 1, Math.ceil(Math.max(...triangle.map(vertex => vertex[1]))));
      for (let pixelY = minY; pixelY <= maxY; pixelY++) for (let pixelX = minX; pixelX <= maxX; pixelX++) {
        const point = [pixelX + 0.5, pixelY + 0.5];
        const weights = [edge(triangle[1], triangle[2], point), edge(triangle[2], triangle[0], point), edge(triangle[0], triangle[1], point)].map(value => value / denominator);
        if (weights.some(weight => weight < -0.000001)) continue;
        const depth = weights.reduce((sum, weight, index) => sum + weight * triangle[index][2], 0);
        const pixel = pixelY * width + pixelX;
        if (depth <= depthBuffer[pixel]) continue;
        const textureU = weights.reduce((sum, weight, index) => sum + weight * face.uv[indices[index]][0], 0);
        const textureV = weights.reduce((sum, weight, index) => sum + weight * face.uv[indices[index]][1], 0);
        const sampleX = Math.max(0, Math.min(texture.w - 1, Math.floor(textureU / 16 * texture.w)));
        const sampleY = Math.max(0, Math.min(texture.h - 1, Math.floor(textureV / 16 * texture.h)));
        const source = (sampleY * texture.w + sampleX) * 4;
        if (texture.data[source + 3] < 128) continue;
        const destination = pixel * 4;
        for (let channel = 0; channel < 3; channel++) image.data[destination + channel] = Math.round(texture.data[source + channel] * shade);
        image.data[destination + 3] = texture.data[source + 3];
        depthBuffer[pixel] = depth;
      }
    }
  }
  return image;
}

function save(file, image) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, encodePNG(image.w, image.h, image.data));
}

function canvas(width, height, color = [20, 30, 29, 255]) {
  const image = { w: width, h: height, data: Buffer.alloc(width * height * 4) };
  for (let offset = 0; offset < image.data.length; offset += 4) image.data.set(color, offset);
  return image;
}

function blit(destination, source, left, top, multiplier = 1) {
  for (let sourceY = 0; sourceY < source.h; sourceY++) for (let sourceX = 0; sourceX < source.w; sourceX++) {
    const sourceOffset = (sourceY * source.w + sourceX) * 4;
    if (!source.data[sourceOffset + 3]) continue;
    for (let subY = 0; subY < multiplier; subY++) for (let subX = 0; subX < multiplier; subX++) {
      const targetX = left + sourceX * multiplier + subX;
      const targetY = top + sourceY * multiplier + subY;
      if (targetX < 0 || targetY < 0 || targetX >= destination.w || targetY >= destination.h) continue;
      destination.data.set(source.data.subarray(sourceOffset, sourceOffset + 4), (targetY * destination.w + targetX) * 4);
    }
  }
}

const font = {
  A: '0111010001111111000110001', B: '1111010001111101000111110', C: '0111110000100001000001111',
  D: '1111010001100011000111110', E: '1111110000111101000011111', F: '1111110000111101000010000',
  G: '0111110000101111000101111', H: '1000110001111111000110001', I: '1111100100001000010011111',
  J: '0011100010000101001001100', K: '1000110010111001001010001', L: '1000010000100001000011111',
  M: '1000111011101011000110001', N: '1000111001101011001110001', O: '0111010001100011000101110',
  P: '1111010001111101000010000', Q: '0111010001100011001101111', R: '1111010001111101001010001',
  S: '0111110000011100000111110', T: '1111100100001000010000100', U: '1000110001100011000101110',
  V: '1000110001100010101000100', W: '1000110001101011101110001', X: '1000101010001000101010001',
  Y: '1000101010001000010000100', Z: '1111100010001000100011111', '0': '0111010011101011100101110',
  '1': '0010001100001000010001110', '2': '1111000001011101000011111', '3': '1111000001011100000111110',
  '4': '1001010010111110001000010', '5': '1111110000111100000111110', '6': '0111110000111101000101110',
  '7': '1111100010001000100001000', '8': '0111010001011101000101110', '9': '0111010001011110000101110',
  '-': '0000000000111110000000000', '/': '0000100010001000100010000', ':': '0000000100000000010000000',
  '.': '0000000000000000000000100', '+': '0000000100011100010000000'
};

function text(image, message, left, top, scale = 3, color = [202, 218, 184, 255]) {
  for (const character of message.toUpperCase()) {
    const glyph = font[character];
    if (glyph) for (let index = 0; index < 25; index++) if (glyph[index] === '1') blit(image, canvas(scale, scale, color), left + index % 5 * scale, top + Math.floor(index / 5) * scale);
    left += 6 * scale;
  }
}

function outline(image) {
  const original = Buffer.from(image.data);
  for (let pixelY = 0; pixelY < image.h; pixelY++) for (let pixelX = 0; pixelX < image.w; pixelX++) {
    const offset = (pixelY * image.w + pixelX) * 4;
    if (original[offset + 3]) continue;
    if ([[0, 1], [0, -1], [1, 0], [-1, 0]].some(([offsetX, offsetY]) => {
      const neighborX = pixelX + offsetX;
      const neighborY = pixelY + offsetY;
      return neighborX >= 0 && neighborY >= 0 && neighborX < image.w && neighborY < image.h && original[(neighborY * image.w + neighborX) * 4 + 3];
    })) image.data.set([23, 39, 31, 255], offset);
  }
  return image;
}

function previews() {
  const model = JSON.parse(fs.readFileSync(modelFile));
  const atlas = decodePNG(atlasFile);
  const views = [['FRONT', 0], ['BACK', 180], ['LEFT', 270], ['RIGHT', 90]];
  const sheet = canvas(1600, 620);
  text(sheet, 'SAYA / TEA HEART FIBER', 36, 27, 5);
  text(sheet, 'FINAL RUNTIME JSON + UV ATLAS / ORTHOGRAPHIC', 36, 72, 3, [136, 164, 145, 255]);
  for (let index = 0; index < views.length; index++) {
    const [label, yaw] = views[index];
    const image = render(model, atlas, { width: 380, height: 450, yaw, scale: 26 });
    save(path.join(art, label.toLowerCase() + '.png'), image);
    blit(sheet, image, 10 + index * 400, 113);
    text(sheet, label, 135 + index * 400, 578, 3);
  }
  save(path.join(art, 'four_views.png'), sheet);
  const angled = render(model, atlas, { width: 700, height: 750, yaw: -28, pitch: 17, scale: 43 });
  save(path.join(art, 'angled_detail.png'), angled);
  const icon = decodePNG(path.join(root, 'src/main/resources/assets/ftboss/textures/item/tea_material.png'));
  const board = canvas(1280, 900);
  text(board, 'TEA HEART FIBER', 40, 34, 6);
  text(board, 'SAYA / FTBOSS:TEA_MATERIAL', 42, 85, 3, [144, 170, 147, 255]);
  blit(board, angled, 0, 125);
  text(board, 'SAME MATERIAL', 754, 160, 4);
  text(board, '32 PX / 8X PIXEL VIEW', 754, 204, 3);
  blit(board, icon, 804, 253, 8);
  blit(board, icon, 1152, 477);
  text(board, 'LAYERED TEA HEART', 740, 595, 3);
  text(board, 'VEINED LEAF SHEATHS', 740, 637, 3);
  text(board, 'BOUND BAST FIBERS', 740, 679, 3);
  text(board, 'EDITABLE BLOCKBENCH', 740, 763, 3, [144, 170, 147, 255]);
  text(board, 'NO ENTITY OR GAMEPLAY EDITS', 40, 854, 3, [144, 170, 147, 255]);
  save(path.join(art, 'presentation.png'), board);
  const icons = canvas(650, 440);
  text(icons, 'INVENTORY / 32 PX + 10X', 25, 25, 3);
  blit(icons, icon, 50, 80, 10);
  blit(icons, icon, 450, 130);
  blit(icons, icon, 450, 210, 2);
  text(icons, '1X', 500, 139, 2);
  text(icons, '2X', 535, 240, 2);
  save(path.join(art, 'icon_comparison.png'), icons);
  const enlargedAtlas = canvas(atlas.w * 4, atlas.h * 4);
  blit(enlargedAtlas, atlas, 0, 0, 4);
  save(path.join(art, 'atlas_4x.png'), enlargedAtlas);
  console.log('Rendered final runtime front/back/left/right, angle and icon sheets.');
}

if (require.main === module) previews();
module.exports = { root, art, modelFile, atlasFile, transform, mesh, render, save, canvas, blit, text, outline, previews };
