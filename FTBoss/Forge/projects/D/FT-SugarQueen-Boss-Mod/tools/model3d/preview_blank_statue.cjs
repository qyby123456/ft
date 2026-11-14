'use strict';

const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const destination = path.resolve(process.argv[2] || path.join(root, 'art/blank-statue/fourviews.png'));
const positions = [];
const coordinates = [];
const normals = [];
const triangles = [];
let material = 'body';
for (const line of fs.readFileSync(path.join(assets, 'models/block/statue_blank.obj'), 'utf8').split('\n')) {
  const [kind, ...values] = line.trim().split(/\s+/);
  if (kind === 'v') positions.push(values.map(Number));
  if (kind === 'vt') coordinates.push(values.map(Number));
  if (kind === 'vn') normals.push(values.map(Number));
  if (kind === 'usemtl') material = values[0];
  if (kind === 'f') triangles.push({ material, vertices: values.map(value => {
    const indices = value.split('/').map(index => Number(index) - 1);
    return { position: positions[indices[0]], uv: coordinates[indices[1]], normal: normals[indices[2]] };
  }) });
}
const texture = decodePNG(path.join(assets, 'textures/block/statue_blank.png'));
const hero = process.argv.includes('--hero');
const panelWidth = hero ? 900 : 450;
const canvas = new Canvas(hero ? 900 : 1800, hero ? 1050 : 620);
canvas.rect(0, 0, canvas.w, canvas.h, 19, 30, 42, 1);
const depth = new Float64Array(canvas.w * canvas.h).fill(-Infinity);
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const views = hero ? [{ name: 'hero', yaw: -32 }] : [{ name: 'front', yaw: 0 }, { name: 'back', yaw: 180 }, { name: 'left', yaw: 90 }, { name: 'right', yaw: -90 }];
for (const [panel, view] of views.entries()) {
  const yaw = view.yaw * Math.PI / 180;
  const elevation = (hero ? 20 : 10) * Math.PI / 180;
  const camera = [Math.sin(yaw) * Math.cos(elevation), Math.sin(elevation), -Math.cos(yaw) * Math.cos(elevation)];
  const right = [-Math.cos(yaw), 0, -Math.sin(yaw)];
  const up = [-Math.sin(elevation) * Math.sin(yaw), Math.cos(elevation), Math.sin(elevation) * Math.cos(yaw)];
  const project = point => {
    const relative = [point[0] - 0.5, point[1] - 0.5, point[2] - 0.5];
    return [panel * panelWidth + panelWidth / 2 + dot(relative, right) * (hero ? 830 : 510),
      (hero ? 505 : 290) - dot(relative, up) * (hero ? 830 : 510), dot(relative, camera)];
  };
  canvas.rect(panel * panelWidth + 40, canvas.h - 35, panelWidth - 80, 2, 89, 183, 199, 1);
  for (const meshFace of triangles) {
    const triangle = meshFace.vertices;
    const normal = triangle[0].normal;
    if (dot(normal, camera) <= 0) continue;
    const shade = meshFace.material === 'glow' ? 1 : Math.min(1.08, 0.62 + Math.max(0, dot(normal, [-0.35, 0.8, -0.48])) * 0.48);
    const projected = triangle.map(vertex => project(vertex.position));
    const [first, second, third] = projected;
    const determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(determinant) < 1e-8) continue;
    const minimumColumn = Math.max(panel * panelWidth, Math.floor(Math.min(...projected.map(point => point[0]))));
    const maximumColumn = Math.min((panel + 1) * panelWidth - 1, Math.ceil(Math.max(...projected.map(point => point[0]))));
    const minimumRow = Math.max(0, Math.floor(Math.min(...projected.map(point => point[1]))));
    const maximumRow = Math.min(canvas.h - 1, Math.ceil(Math.max(...projected.map(point => point[1]))));
    for (let row = minimumRow; row <= maximumRow; row++) {
      for (let column = minimumColumn; column <= maximumColumn; column++) {
        const firstWeight = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / determinant;
        const secondWeight = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / determinant;
        const weights = [firstWeight, secondWeight, 1 - firstWeight - secondWeight];
        if (Math.min(...weights) < -1e-7) continue;
        const distance = weights.reduce((sum, weight, index) => sum + weight * projected[index][2], 0);
        const bufferIndex = row * canvas.w + column;
        if (distance <= depth[bufferIndex]) continue;
        const uv = [0, 1].map(axis => weights.reduce((sum, weight, index) => sum + weight * triangle[index].uv[axis], 0));
        const horizontal = Math.max(0, Math.min(texture.w - 1, Math.floor(uv[0] * texture.w)));
        const vertical = Math.max(0, Math.min(texture.h - 1, Math.floor(uv[1] * texture.h)));
        const texel = (vertical * texture.w + horizontal) * 4;
        canvas.set(column, row, ...Array.from(texture.data.slice(texel, texel + 3)).map(channel => channel * shade), 1);
        depth[bufferIndex] = distance;
      }
    }
  }
}
fs.mkdirSync(path.dirname(destination), { recursive: true });
canvas.save(destination);
fs.writeFileSync(path.join(path.dirname(destination), hero ? 'hero.json' : 'fourviews.json'), JSON.stringify({
  source: 'Final runtime OBJ and PNG', views, triangles: triangles.length,
  bounds: [0, 1, 2].map(axis => [Math.min(...positions.map(point => point[axis])), Math.max(...positions.map(point => point[axis]))])
}, null, 2) + '\n');
console.log(destination);
