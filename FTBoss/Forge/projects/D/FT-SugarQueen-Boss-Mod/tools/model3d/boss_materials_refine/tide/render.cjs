const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG, encodePNG } = require('../../png.cjs');

const sub = (first, second) => first.map((value, axis) => value - second[axis]);
const dot = (first, second) => first.reduce((total, value, axis) => total + value * second[axis], 0);
const cross = (first, second) => [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
const norm = vector => vector.map(value => value / Math.hypot(...vector));

function transform(point, element) {
  if (!element.rotation || !element.rotation.angle) return point;
  const rotation = element.rotation;
  const axis = 'xyz'.indexOf(rotation.axis);
  const angle = rotation.angle * Math.PI / 180;
  const cosine = Math.cos(angle);
  const sine = Math.sin(angle);
  const local = sub(point, rotation.origin);
  if (rotation.rescale) for (let index = 0; index < 3; index++) if (index !== axis) local[index] /= Math.cos(Math.abs(angle));
  let result;
  if (axis === 0) result = [local[0], local[1] * cosine - local[2] * sine, local[1] * sine + local[2] * cosine];
  if (axis === 1) result = [local[0] * cosine + local[2] * sine, local[1], -local[0] * sine + local[2] * cosine];
  if (axis === 2) result = [local[0] * cosine - local[1] * sine, local[0] * sine + local[1] * cosine, local[2]];
  return result.map((value, index) => value + rotation.origin[index]);
}

function corners(element, face) {
  const [left, bottom, near] = element.from;
  const [right, top, far] = element.to;
  const directions = {
    north: [[right, top, near], [right, bottom, near], [left, bottom, near], [left, top, near]],
    south: [[left, top, far], [left, bottom, far], [right, bottom, far], [right, top, far]],
    west: [[left, top, near], [left, bottom, near], [left, bottom, far], [left, top, far]],
    east: [[right, top, far], [right, bottom, far], [right, bottom, near], [right, top, near]],
    up: [[left, top, near], [left, top, far], [right, top, far], [right, top, near]],
    down: [[left, bottom, far], [left, bottom, near], [right, bottom, near], [right, bottom, far]]
  };
  return directions[face].map(point => transform(point, element));
}

function render(model, texture, options = {}) {
  const size = options.size || 480;
  const yaw = (options.yaw || 0) * Math.PI / 180;
  const pitch = (options.pitch || 0) * Math.PI / 180;
  const right = [Math.cos(yaw), 0, -Math.sin(yaw)];
  const up = [Math.sin(yaw) * Math.sin(pitch), Math.cos(pitch), Math.cos(yaw) * Math.sin(pitch)];
  const toward = [-Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw) * Math.cos(pitch)];
  const scale = options.scale || (size - 56) / 16;
  const center = options.center || [8, 8, 8];
  const output = new Canvas(size, size);
  if (!options.transparent) output.rect(0, 0, size, size, 20, 30, 42);
  const depth = new Float64Array(size * size).fill(-Infinity);
  const projection = point => {
    const local = sub(point, center);
    return [size / 2 + dot(local, right) * scale, size / 2 - dot(local, up) * scale, dot(local, toward)];
  };
  const sun = norm([-0.4, 0.85, -0.5]);
  for (const element of model.elements) for (const [faceName, face] of Object.entries(element.faces)) {
    const vertices = corners(element, faceName);
    const normal = norm(cross(sub(vertices[1], vertices[0]), sub(vertices[2], vertices[0])));
    if (dot(normal, toward) <= 0.000001) continue;
    const projected = vertices.map(projection);
    const [startU, startV, endU, endV] = face.uv;
    const coordinates = [[startU, startV], [startU, endV], [endU, endV], [endU, startV]];
    const uv = projected.map((unused, index) => coordinates[(index + (face.rotation || 0) / 90) % 4]);
    const light = options.unlit ? 1 : 0.82 + Math.max(0, dot(normal, sun)) * 0.18;
    for (const indices of [[0, 1, 2], [0, 2, 3]]) {
      const triangle = indices.map(index => projected[index]);
      const mapped = indices.map(index => uv[index]);
      const minX = Math.max(0, Math.floor(Math.min(...triangle.map(point => point[0]))));
      const maxX = Math.min(size - 1, Math.ceil(Math.max(...triangle.map(point => point[0]))));
      const minY = Math.max(0, Math.floor(Math.min(...triangle.map(point => point[1]))));
      const maxY = Math.min(size - 1, Math.ceil(Math.max(...triangle.map(point => point[1]))));
      const determinant = (triangle[1][1] - triangle[2][1]) * (triangle[0][0] - triangle[2][0]) + (triangle[2][0] - triangle[1][0]) * (triangle[0][1] - triangle[2][1]);
      if (Math.abs(determinant) < 0.000001) continue;
      for (let screenY = minY; screenY <= maxY; screenY++) for (let screenX = minX; screenX <= maxX; screenX++) {
        const first = ((triangle[1][1] - triangle[2][1]) * (screenX + 0.5 - triangle[2][0]) + (triangle[2][0] - triangle[1][0]) * (screenY + 0.5 - triangle[2][1])) / determinant;
        const second = ((triangle[2][1] - triangle[0][1]) * (screenX + 0.5 - triangle[2][0]) + (triangle[0][0] - triangle[2][0]) * (screenY + 0.5 - triangle[2][1])) / determinant;
        const third = 1 - first - second;
        if (Math.min(first, second, third) < -0.000001) continue;
        const currentDepth = first * triangle[0][2] + second * triangle[1][2] + third * triangle[2][2];
        const screenIndex = screenY * size + screenX;
        if (currentDepth <= depth[screenIndex]) continue;
        const sampleU = first * mapped[0][0] + second * mapped[1][0] + third * mapped[2][0];
        const sampleV = first * mapped[0][1] + second * mapped[1][1] + third * mapped[2][1];
        const pixelX = Math.max(0, Math.min(texture.w - 1, Math.floor(sampleU * texture.w / 16)));
        const pixelY = Math.max(0, Math.min(texture.h - 1, Math.floor(sampleV * texture.h / 16)));
        const textureIndex = (pixelY * texture.w + pixelX) * 4;
        if (texture.data[textureIndex + 3] < 128) continue;
        depth[screenIndex] = currentDepth;
        const outputIndex = screenIndex * 4;
        for (let channel = 0; channel < 3; channel++) output.d[outputIndex + channel] = Math.round(texture.data[textureIndex + channel] * light);
        output.d[outputIndex + 3] = texture.data[textureIndex + 3];
      }
    }
  }
  return output;
}

function stamp(target, source, left, top, scale = 1) {
  for (let pixelY = 0; pixelY < source.h; pixelY++) for (let pixelX = 0; pixelX < source.w; pixelX++) {
    const index = (pixelY * source.w + pixelX) * 4;
    if (!source.data[index + 3]) continue;
    target.rect(left + pixelX * scale, top + pixelY * scale, scale, scale, source.data[index], source.data[index + 1], source.data[index + 2], source.data[index + 3] / 255);
  }
}

const font = {
  A:['010','101','111','101','101'], B:['110','101','110','101','110'], C:['011','100','100','100','011'], D:['110','101','101','101','110'], E:['111','100','110','100','111'], F:['111','100','110','100','100'], G:['011','100','101','101','011'], H:['101','101','111','101','101'], I:['111','010','010','010','111'], J:['001','001','001','101','010'], K:['101','101','110','101','101'], L:['100','100','100','100','111'], M:['101','111','111','101','101'], N:['101','111','111','111','101'], O:['010','101','101','101','010'], P:['110','101','110','100','100'], R:['110','101','110','101','101'], S:['011','100','010','001','110'], T:['111','010','010','010','010'], U:['101','101','101','101','111'], V:['101','101','101','101','010'], W:['101','101','111','111','101'], X:['101','101','010','101','101'], Y:['101','101','010','010','010'], Z:['111','001','010','100','111'], '0':['111','101','101','101','111'], '1':['010','110','010','010','111'], '2':['110','001','010','100','111'], '3':['110','001','010','001','110'], '4':['101','101','111','001','001'], '5':['111','100','110','001','110'], '6':['011','100','111','101','111'], '7':['111','001','010','010','010'], '8':['111','101','111','101','111'], '9':['111','101','111','001','110']
};
function label(canvas, text, left, top, scale = 2) {
  for (let index = 0; index < text.length; index++) {
    const letter = font[text[index]];
    if (!letter) continue;
    for (let row = 0; row < 5; row++) for (let column = 0; column < 3; column++) if (letter[row][column] === '1') canvas.rect(left + index * 4 * scale + column * scale, top + row * scale, scale, scale, 171, 210, 220);
  }
}

module.exports = { render, transform, corners, stamp, label };

if (require.main === module) {
  const root = path.resolve(__dirname, '../../../..');
  const assets = path.join(root, 'src/main/resources/assets/ftboss');
  const output = path.join(root, 'art/boss_materials/refine/tide');
  const model = JSON.parse(fs.readFileSync(path.join(assets, 'models/item/boss_materials/tide_material.json'), 'utf8'));
  const texture = decodePNG(path.join(assets, 'textures/item/boss_materials/tide.png'));
  fs.mkdirSync(output, { recursive: true });
  const save = (file, canvas) => fs.writeFileSync(path.join(output, file), encodePNG(canvas.w, canvas.h, canvas.d));
  const sheet = new Canvas(1040, 1100);
  sheet.rect(0, 0, sheet.w, sheet.h, 14, 23, 33);
  label(sheet, 'TIDE CRYSTAL  FINAL RUNTIME ASSETS', 32, 22, 3);
  const views = [['FRONT', 0], ['BACK', 180], ['LEFT', 90], ['RIGHT', -90]];
  for (let index = 0; index < views.length; index++) {
    const [name, yaw] = views[index];
    const image = render(model, texture, { size: 480, yaw, pitch: 0 });
    save(name.toLowerCase() + '.png', image);
    const left = 24 + index % 2 * 516;
    const top = 60 + Math.floor(index / 2) * 510;
    stamp(sheet, { w: image.w, h: image.h, data: image.d }, left, top);
    label(sheet, name, left + 16, top + 446, 3);
  }
  save('four_views.png', sheet);
  const detail = new Canvas(1300, 760);
  detail.rect(0, 0, detail.w, detail.h, 14, 23, 33);
  label(detail, 'TIDE CRYSTAL  CUTS  PEARL  SCALE LAYERS', 30, 22, 3);
  for (const [left, yaw, name] of [[18, -28, 'FRONT DETAIL'], [656, 145, 'REAR DETAIL']]) {
    const image = render(model, texture, { size: 620, yaw, pitch: 16, scale: 39 });
    stamp(detail, { w: image.w, h: image.h, data: image.d }, left, 66);
    label(detail, name, left + 24, 700, 3);
  }
  save('oblique_details.png', detail);
  const icon = decodePNG(path.join(assets, 'textures/item/tide_material.png'));
  const iconSheet = new Canvas(1000, 600);
  iconSheet.rect(0, 0, iconSheet.w, iconSheet.h, 14, 23, 33);
  label(iconSheet, 'RUNTIME ICON AND 3D MATERIAL', 32, 24, 3);
  const modelView = render(model, texture, { size: 480, yaw: -16, pitch: 10 });
  stamp(iconSheet, { w: 480, h: 480, data: modelView.d }, 20, 72);
  stamp(iconSheet, icon, 560, 112, 10);
  stamp(iconSheet, icon, 562, 486);
  const inventory = { w:16,h:16,data:new Uint8Array(16*16*4) };
  for(let pixelY=0;pixelY<16;pixelY++) for(let pixelX=0;pixelX<16;pixelX++) {
    const source=((pixelY*2+1)*icon.w+pixelX*2+1)*4;
    inventory.data.set(icon.data.slice(source,source+4),(pixelY*16+pixelX)*4);
  }
  stamp(iconSheet,inventory,884,488);
  label(iconSheet,'16 PX',884,520,2);
  label(iconSheet, '3D MODEL', 64, 552, 3);
  label(iconSheet, '32 PX', 618, 492, 2);
  label(iconSheet, 'ICON 10X', 560, 552, 3);
  save('icon_comparison.png', iconSheet);
  console.log(JSON.stringify({ output, views: views.map(view => view[0]), source: 'final runtime JSON and PNG', rotation: true, rescale: true, uvRotation: true, zBuffer: true, nearestSampling: true }));
}
