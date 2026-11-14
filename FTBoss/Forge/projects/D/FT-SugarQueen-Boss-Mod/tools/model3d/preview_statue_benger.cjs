'use strict';

const fs = require('fs');
const path = require('path');
const { Canvas, decodePNG } = require('./png.cjs');
const assets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const destination = path.resolve(process.argv[2] || 'art/benger-statue-preview.png');
const canvas = new Canvas(1440, 700);
canvas.rect(0, 0, canvas.w, canvas.h, 234, 231, 224, 1);
const positions = [];
const coordinates = [];
const normals = [];
const triangles = [];
let material = 'body';
const mesh = fs.readFileSync(path.join(assets, 'models/block/statue_benger_item.obj'), 'utf8');
for (const line of mesh.split(/\r?\n/)) {
    const [kind, ...values] = line.trim().split(/\s+/);
    if (kind === 'v') positions.push(values.map(Number).map((value, axis) => axis === 1 ? value * 32 : (value - 0.5) * 32));
    if (kind === 'vt') coordinates.push(values.map(Number));
    if (kind === 'vn') normals.push(values.map(Number));
    if (kind === 'usemtl') material = values[0];
    if (kind === 'f') triangles.push({ material, vertices: values.map(value => {
        const indices = value.split('/').map(index => Number(index) - 1);
        return { position: positions[indices[0]], uv: coordinates[indices[1]], normal: normals[indices[2]] };
    }) });
}
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const panelWidth = 480;
const depth = new Float64Array(canvas.w * canvas.h).fill(-Infinity);
const stone = { base: [150, 151, 145], trim: [61, 65, 69], badge: [213, 172, 66] };
const height = Math.max(...positions.map(point => point[1]));

for (const [panel, setting] of [{ yaw: -34, white: false }, { yaw: 0, white: false }, { yaw: -34, white: true }].entries()) {
    const yaw = setting.yaw * Math.PI / 180;
    const elevation = 12 * Math.PI / 180;
    const camera = [Math.sin(yaw) * Math.cos(elevation), Math.sin(elevation), -Math.cos(yaw) * Math.cos(elevation)];
    const right = [-Math.cos(yaw), 0, -Math.sin(yaw)];
    const up = [-Math.sin(elevation) * Math.sin(yaw), Math.cos(elevation), Math.sin(elevation) * Math.cos(yaw)];
    const texture = decodePNG(path.join(assets, `textures/block/statue_benger${setting.white ? '_white' : ''}.png`));
    const scale = 21;
    const project = point => {
        const relative = [point[0], point[1] - height / 2, point[2]];
        return [panel * panelWidth + panelWidth / 2 + dot(relative, right) * scale,
            345 - dot(relative, up) * scale, dot(relative, camera)];
    };
    canvas.rect(panel * panelWidth + 24, 656, 432, 4, 154, 127, 83, 1);
    for (const triangle of triangles) {
        const normal = triangle.vertices[0].normal;
        if (dot(normal, camera) <= 0) continue;
        const shade = Math.min(1.08, 0.64 + Math.max(0, dot(normal, [-0.35, 0.8, -0.48])) * 0.45);
        const projected = triangle.vertices.map(vertex => project(vertex.position));
        const [first, second, third] = projected;
        const determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
        if (Math.abs(determinant) < 1e-8) continue;
        const minColumn = Math.max(panel * panelWidth, Math.floor(Math.min(...projected.map(point => point[0]))));
        const maxColumn = Math.min((panel + 1) * panelWidth - 1, Math.ceil(Math.max(...projected.map(point => point[0]))));
        const minRow = Math.max(0, Math.floor(Math.min(...projected.map(point => point[1]))));
        const maxRow = Math.min(canvas.h - 1, Math.ceil(Math.max(...projected.map(point => point[1]))));
        for (let row = minRow; row <= maxRow; row++) {
            for (let column = minColumn; column <= maxColumn; column++) {
                const firstWeight = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / determinant;
                const secondWeight = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / determinant;
                const thirdWeight = 1 - firstWeight - secondWeight;
                if (Math.min(firstWeight, secondWeight, thirdWeight) < -1e-7) continue;
                const weights = [firstWeight, secondWeight, thirdWeight];
                const distance = weights.reduce((sum, weight, index) => sum + weight * projected[index][2], 0);
                const bufferIndex = row * canvas.w + column;
                if (distance <= depth[bufferIndex]) continue;
                let color = stone[triangle.material];
                if (triangle.material === 'body') {
                    const uv = [0, 1].map(axis => weights.reduce((sum, weight, index) => sum + weight * triangle.vertices[index].uv[axis], 0));
                    const horizontal = Math.max(0, Math.min(texture.w - 1, Math.floor(uv[0] * texture.w)));
                    const vertical = Math.max(0, Math.min(texture.h - 1, Math.floor(uv[1] * texture.h)));
                    const texel = (vertical * texture.w + horizontal) * 4;
                    if (texture.data[texel + 3] < 128) continue;
                    color = Array.from(texture.data.slice(texel, texel + 3));
                }
                canvas.set(column, row, ...color.map(value => value * shade), 1);
                depth[bufferIndex] = distance;
            }
        }
    }
}
fs.mkdirSync(path.dirname(destination), { recursive: true });
canvas.save(destination);
console.log(destination);
