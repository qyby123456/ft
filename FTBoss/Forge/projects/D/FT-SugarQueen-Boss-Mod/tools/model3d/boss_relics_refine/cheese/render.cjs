const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { Canvas, decodePNG, encodePNG } = require('../../png.cjs');
const { outline, makeCanvas, writeSafe } = require('../../lib_itemtex.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const output = path.join(root, 'art/boss_relics/refine/cheese');
const modelPath = path.join(assets, 'models/item/boss_relics/cheese_moon_charm.json');
const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const textures = Object.fromEntries(Object.entries(model.textures).map(([key, resource]) => [key, decodePNG(path.join(assets, 'textures', resource.split(':')[1] + '.png'))]));
const normals = { north: [0, 0, -1], south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };
const vertices = element => {
  const [left, low, front] = element.from;
  const [right, high, back] = element.to;
  return {
    north: [[right, high, front], [left, high, front], [left, low, front], [right, low, front]],
    south: [[left, high, back], [right, high, back], [right, low, back], [left, low, back]],
    east: [[right, high, back], [right, high, front], [right, low, front], [right, low, back]],
    west: [[left, high, front], [left, high, back], [left, low, back], [left, low, front]],
    up: [[left, high, back], [right, high, back], [right, high, front], [left, high, front]],
    down: [[left, low, front], [right, low, front], [right, low, back], [left, low, back]]
  };
};
function rotate(point, axis, angle) {
  const radians = angle * Math.PI / 180;
  const cosine = Math.cos(radians);
  const sine = Math.sin(radians);
  const [horizontal, vertical, depth] = point;
  if (axis === 'x') return [horizontal, vertical * cosine - depth * sine, vertical * sine + depth * cosine];
  if (axis === 'y') return [horizontal * cosine + depth * sine, vertical, -horizontal * sine + depth * cosine];
  return [horizontal * cosine - vertical * sine, horizontal * sine + vertical * cosine, depth];
}
function elementPoint(point, element) {
  if (!element.rotation) return point;
  const rotation = element.rotation;
  const relative = point.map((value, axis) => value - rotation.origin[axis]);
  const transformed = rotate(relative, rotation.axis, rotation.angle);
  if (rotation.rescale) {
    const multiplier = 1 / Math.cos(rotation.angle * Math.PI / 180);
    for (const [index, axis] of ['x', 'y', 'z'].entries()) if (axis !== rotation.axis) transformed[index] *= multiplier;
  }
  return transformed.map((value, axis) => value + rotation.origin[axis]);
}
function camera(point, yaw, pitch) {
  const radians = yaw * Math.PI / 180;
  const horizontal = -point[0] * Math.cos(radians) + point[2] * Math.sin(radians);
  const depth = -point[0] * Math.sin(radians) - point[2] * Math.cos(radians);
  const tilt = pitch * Math.PI / 180;
  return [horizontal, point[1] * Math.cos(tilt) - depth * Math.sin(tilt), point[1] * Math.sin(tilt) + depth * Math.cos(tilt)];
}
function render({ size = 480, yaw = 0, pitch = 0, scale = 28, transparent = false, unlit = false } = {}) {
  const image = new Canvas(size, size);
  if (!transparent) {
    for (let row = 0; row < size; row++) {
      const light = 1 - Math.abs(row / size - .35);
      image.rect(0, row, size, 1, 24 + light * 8, 29 + light * 8, 38 + light * 9);
    }
  }
  const zbuffer = new Float64Array(size * size).fill(-Infinity);
  function triangle(points, textureUV, texture, shade) {
    const [first, second, third] = points;
    const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(denominator) < 1e-8) return;
    const minimumX = Math.max(0, Math.floor(Math.min(...points.map(point => point[0]))));
    const maximumX = Math.min(size - 1, Math.ceil(Math.max(...points.map(point => point[0]))));
    const minimumY = Math.max(0, Math.floor(Math.min(...points.map(point => point[1]))));
    const maximumY = Math.min(size - 1, Math.ceil(Math.max(...points.map(point => point[1]))));
    for (let pixelY = minimumY; pixelY <= maximumY; pixelY++) for (let pixelX = minimumX; pixelX <= maximumX; pixelX++) {
      const horizontal = pixelX + .5;
      const vertical = pixelY + .5;
      const firstWeight = ((second[1] - third[1]) * (horizontal - third[0]) + (third[0] - second[0]) * (vertical - third[1])) / denominator;
      const secondWeight = ((third[1] - first[1]) * (horizontal - third[0]) + (first[0] - third[0]) * (vertical - third[1])) / denominator;
      const thirdWeight = 1 - firstWeight - secondWeight;
      if (firstWeight < -1e-7 || secondWeight < -1e-7 || thirdWeight < -1e-7) continue;
      const depth = firstWeight * first[2] + secondWeight * second[2] + thirdWeight * third[2];
      const offset = pixelY * size + pixelX;
      if (depth <= zbuffer[offset] + 1e-8) continue;
      const sample = [0, 1].map(axis => firstWeight * textureUV[0][axis] + secondWeight * textureUV[1][axis] + thirdWeight * textureUV[2][axis]);
      const textureX = Math.max(0, Math.min(texture.w - 1, Math.floor(sample[0] * texture.w / 16)));
      const textureY = Math.max(0, Math.min(texture.h - 1, Math.floor(sample[1] * texture.h / 16)));
      const textureOffset = (textureY * texture.w + textureX) * 4;
      const alpha = texture.data[textureOffset + 3];
      if (alpha < 128) continue;
      zbuffer[offset] = depth;
      image.rect(pixelX, pixelY, 1, 1, ...[0, 1, 2].map(channel => Math.min(255, texture.data[textureOffset + channel] * shade)));
    }
  }
  for (const element of model.elements) {
    const localFaces = vertices(element);
    for (const [face, settings] of Object.entries(element.faces)) {
      const normal = element.rotation ? rotate(normals[face], element.rotation.axis, element.rotation.angle) : normals[face];
      const projectedNormal = camera(normal, yaw, pitch);
      if (projectedNormal[2] < 1e-7) continue;
      const points = localFaces[face].map(point => {
        const transformed = camera(elementPoint(point, element).map(value => value - 8), yaw, pitch);
        return [size / 2 + transformed[0] * scale, size / 2 - transformed[1] * scale, transformed[2]];
      });
      const [left, top, right, bottom] = settings.uv;
      let textureUV = [[left, top], [right, top], [right, bottom], [left, bottom]];
      const faceRotation = (settings.rotation || 0) / 90;
      textureUV = textureUV.map((_, index) => textureUV[(index + faceRotation) % 4]);
      const texture = textures[settings.texture.slice(1)];
      const illumination = Math.max(0, projectedNormal[0] * -.28 + projectedNormal[1] * .55 + projectedNormal[2] * .79);
      const shade = unlit ? 1 : .69 + illumination * .31;
      triangle([points[0], points[1], points[2]], [textureUV[0], textureUV[1], textureUV[2]], texture, shade);
      triangle([points[0], points[2], points[3]], [textureUV[0], textureUV[2], textureUV[3]], texture, shade);
    }
  }
  return image;
}
const font = {
  A: ['010','101','111','101','101'], B: ['110','101','110','101','110'], C: ['011','100','100','100','011'], D: ['110','101','101','101','110'],
  E: ['111','100','110','100','111'], F: ['111','100','110','100','100'], G: ['011','100','101','101','011'], H: ['101','101','111','101','101'],
  I: ['111','010','010','010','111'], J: ['001','001','001','101','010'], K: ['101','101','110','101','101'], L: ['100','100','100','100','111'],
  M: ['101','111','111','101','101'], N: ['101','111','111','111','101'], O: ['010','101','101','101','010'], P: ['110','101','110','100','100'],
  Q: ['010','101','101','111','011'], R: ['110','101','110','101','101'], S: ['011','100','010','001','110'], T: ['111','010','010','010','010'],
  U: ['101','101','101','101','111'], V: ['101','101','101','101','010'], W: ['101','101','111','111','101'], X: ['101','101','010','101','101'],
  Y: ['101','101','010','010','010'], Z: ['111','001','010','100','111'], '0': ['111','101','101','101','111'], '1': ['010','110','010','010','111'],
  '2': ['110','001','010','100','111'], '3': ['110','001','010','001','110'], '4': ['101','101','111','001','001'], '5': ['111','100','110','001','110'],
  '6': ['011','100','111','101','111'], '7': ['111','001','010','010','010'], '8': ['111','101','111','101','111'], '9': ['111','101','111','001','110']
};
function label(image, text, left, top, scale = 3, color = [223, 219, 199]) {
  for (const [index, letter] of [...text.toUpperCase()].entries()) {
    const glyph = font[letter];
    if (!glyph) continue;
    glyph.forEach((row, vertical) => [...row].forEach((pixel, horizontal) => {
      if (pixel === '1') image.rect(left + (index * 4 + horizontal) * scale, top + vertical * scale, scale, scale, ...color);
    }));
  }
}
function paste(destination, source, left, top, scale = 1) {
  for (let pixelY = 0; pixelY < source.h; pixelY++) for (let pixelX = 0; pixelX < source.w; pixelX++) {
    const offset = (pixelY * source.w + pixelX) * 4;
    const data = source.d || source.data;
    if (!data[offset + 3]) continue;
    destination.rect(left + pixelX * scale, top + pixelY * scale, scale, scale, ...data.slice(offset, offset + 3), data[offset + 3] / 255);
  }
}
fs.mkdirSync(output, { recursive: true });
const directions = [['FRONT', 0], ['BACK', 180], ['LEFT', -90], ['RIGHT', 90]];
const sheet = new Canvas(960, 1020);
sheet.rect(0, 0, sheet.w, sheet.h, 24, 29, 38);
label(sheet, 'CHEESE MOON CHARM', 32, 22, 4, [244, 201, 102]);
for (const [index, [name, yaw]] of directions.entries()) {
  const image = render({ size: 480, yaw, scale: 28 });
  image.save(path.join(output, name.toLowerCase() + '.png'));
  const left = index % 2 * 480;
  const top = 60 + Math.floor(index / 2) * 480;
  paste(sheet, image, left, top);
  label(sheet, name, left + 24, top + 18);
}
sheet.save(path.join(output, 'four_views.png'));
const oblique = render({ size: 640, yaw: -24, pitch: 9, scale: 37 });
oblique.save(path.join(output, 'oblique_detail.png'));
const rearOblique = render({ size: 640, yaw: 150, pitch: 8, scale: 37 });
rearOblique.save(path.join(output, 'rear_oblique_detail.png'));
const icon = render({ size: 32, yaw: -18, pitch: 12, scale: 1.85, transparent: true, unlit: true });
const iconCanvas = makeCanvas(32, 32);
iconCanvas.buf.set(icon.d);
outline(iconCanvas, [94, 59, 30]);
const iconPath = path.join(assets, 'textures/item/cheese_moon_charm.png');
iconCanvas.save(iconPath);
const iconImage = decodePNG(iconPath);
const comparison = new Canvas(1024, 704);
comparison.rect(0, 0, comparison.w, comparison.h, 25, 30, 39);
label(comparison, 'CHEESE MOON CHARM', 30, 24, 4, [248, 205, 110]);
paste(comparison, oblique, 0, 64);
label(comparison, 'RUNTIME MODEL', 34, 657, 3);
label(comparison, 'ICON 32 X 32', 674, 88, 3);
paste(comparison, iconImage, 674, 126);
label(comparison, 'ICON 8X', 674, 194, 3);
paste(comparison, iconImage, 672, 232, 8);
label(comparison, 'SAME MATERIAL', 674, 534, 3);
label(comparison, 'GOLD AND AMBER', 674, 567, 3);
label(comparison, 'INLAY AND BAIL', 674, 600, 3);
comparison.save(path.join(output, 'model_icon_comparison.png'));
const relation = new Canvas(1120, 780);
relation.rect(0, 0, relation.w, relation.h, 25, 30, 39);
label(relation, 'RAT BOSS MATERIAL TO RELIC', 30, 24, 4, [248, 205, 110]);
const materialOblique = decodePNG(path.join(root, 'art/boss_materials/refine/cheese/oblique_detail.png'));
const materialIcon = decodePNG(path.join(assets, 'textures/item/cheese_material.png'));
for (let row = 0; row < 520; row++) for (let column = 0; column < 520; column++) {
  const sourceX = Math.floor(column * materialOblique.w / 520);
  const sourceY = Math.floor(row * materialOblique.h / 520);
  const offset = (sourceY * materialOblique.w + sourceX) * 4;
  relation.rect(20 + column, 110 + row, 1, 1, ...materialOblique.data.slice(offset, offset + 3));
  const relicOffset = (Math.floor(row * oblique.h / 520) * oblique.w + Math.floor(column * oblique.w / 520)) * 4;
  relation.rect(580 + column, 110 + row, 1, 1, ...oblique.d.slice(relicOffset, relicOffset + 3));
}
label(relation, 'AGED CRESCENT MATERIAL', 34, 82, 3);
label(relation, 'FINISHED MOON CHARM', 594, 82, 3);
paste(relation, materialIcon, 58, 664, 2);
paste(relation, iconImage, 618, 664, 2);
label(relation, 'RIND AND RAW CELLS', 155, 685, 3);
label(relation, 'BEZEL BAIL AMBER', 715, 685, 3);
relation.save(path.join(output, 'material_relic_relation.png'));
const atlas = new Canvas(512, 512);
for (let pixelY = 0; pixelY < 512; pixelY += 16) for (let pixelX = 0; pixelX < 512; pixelX += 16) atlas.rect(pixelX, pixelY, 16, 16, ...((pixelX / 16 + pixelY / 16) % 2 ? [53, 59, 69] : [42, 47, 57]));
paste(atlas, textures['0'], 0, 0, 2);
atlas.save(path.join(output, 'uv_atlas_2x.png'));
const hash = filename => crypto.createHash('sha256').update(fs.readFileSync(filename)).digest('hex');
writeSafe(path.join(output, 'render_manifest.json'), JSON.stringify({
  source: modelPath, modelSha256: hash(modelPath), textureSha256: hash(path.join(assets, 'textures/item/boss_relics/cheese_moon_charm.png')),
  iconSha256: hash(iconPath), directions, elementRotation: true, rescale: true, faceUV: true, uvRotation: true, nearestSampling: true, alphaCutout: true, depthBuffer: true, backfaceCulling: true
}, null, 2) + '\n');
console.log('Rendered final runtime resources: 4 views, 2 obliques, icon comparison, UV atlas');
module.exports = { render, camera, elementPoint, vertices };
