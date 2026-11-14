'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const space = require('./cube_space.cjs');
const { decodePNG, encodePNG } = require('./png.cjs');

const [modelFile, textureFile, outputFile, mode = 'model'] = process.argv.slice(2);
assert(modelFile && textureFile && outputFile, 'Usage: <model.json> <texture.png> <output.png> [model|display]');
const model = JSON.parse(fs.readFileSync(modelFile, 'utf8'));
const texture = decodePNG(textureFile);
const panelWidth = 340;
const height = 520;
const width = panelWidth * 4;
const pixels = Buffer.alloc(width * height * 4);
const depths = new Float64Array(width * height).fill(Infinity);
for (let index = 0; index < pixels.length; index += 4) {
    pixels[index] = 28;
    pixels[index + 1] = 34;
    pixels[index + 2] = 46;
    pixels[index + 3] = 255;
}
const panels = mode === 'display'
    ? ['gui', 'firstperson_righthand', 'thirdperson_righthand', 'thirdperson_lefthand'].map(slot => ({ slot, yaw: 0 }))
    : [0, 35, 90, 180].map(yaw => ({ yaw }));
const coordinates = [[0, 0], [0, 1], [1, 1], [1, 0]];

function corners(element) {
    const [left, bottom, front] = element.from;
    const [right, top, back] = element.to;
    return {
        north: [[right, top, front], [right, bottom, front], [left, bottom, front], [left, top, front]],
        south: [[left, top, back], [left, bottom, back], [right, bottom, back], [right, top, back]],
        west: [[left, top, front], [left, bottom, front], [left, bottom, back], [left, top, back]],
        east: [[right, top, back], [right, bottom, back], [right, bottom, front], [right, top, front]],
        up: [[left, top, back], [right, top, back], [right, top, front], [left, top, front]],
        down: [[left, bottom, front], [right, bottom, front], [right, bottom, back], [left, bottom, back]],
    };
}

function transform(point, element, panel) {
    let result = point;
    if (element.rotation) {
        const angles = [0, 0, 0];
        angles['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
        result = space.rotateXYZ(result, angles, element.rotation.origin);
    }
    result = result.map(value => (value - 8) / 16);
    if (panel.slot) {
        const display = structuredClone(model.display[panel.slot]);
        if (panel.slot.endsWith('lefthand')) {
            display.rotation[1] *= -1;
            display.rotation[2] *= -1;
            display.translation[0] *= -1;
        }
        const scaling = display.scale;
        result = result.map((value, axis) => value * scaling[axis]);
        for (const axis of [2, 1, 0]) {
            const angles = [0, 0, 0];
            angles[axis] = display.rotation[axis];
            result = space.rotateXYZ(result, angles);
        }
        result = result.map((value, axis) => value + display.translation[axis] / 16);
        if (panel.slot.startsWith('thirdperson')) {
            result = space.rotateXYZ(result, [0, 180, 0]);
            result = space.rotateXYZ(result, [-90, 0, 0]);
            result = [-result[0], -result[1], result[2]];
        } else {
            result = space.rotateXYZ(result, [0, 180, 0]);
        }
    }
    result = space.rotateXYZ(result, [0, panel.yaw, 0]);
    result = space.rotateXYZ(result, [mode === 'display' ? 0 : 7, 0, 0]);
    return result;
}

function triangle(vertices, uvs, shade, panelIndex) {
    const [first, second, third] = vertices;
    const determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(determinant) < 1e-8) return;
    const minColumn = Math.max(panelIndex * panelWidth, Math.floor(Math.min(...vertices.map(vertex => vertex[0]))));
    const maxColumn = Math.min((panelIndex + 1) * panelWidth - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[0]))));
    const minRow = Math.max(0, Math.floor(Math.min(...vertices.map(vertex => vertex[1]))));
    const maxRow = Math.min(height - 1, Math.ceil(Math.max(...vertices.map(vertex => vertex[1]))));
    for (let row = minRow; row <= maxRow; row++) {
        for (let column = minColumn; column <= maxColumn; column++) {
            const firstWeight = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / determinant;
            const secondWeight = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / determinant;
            const thirdWeight = 1 - firstWeight - secondWeight;
            if (Math.min(firstWeight, secondWeight, thirdWeight) < -1e-8) continue;
            const weights = [firstWeight, secondWeight, thirdWeight];
            const depth = weights.reduce((sum, weight, index) => sum + weight * vertices[index][2], 0);
            const destination = row * width + column;
            if (depth >= depths[destination]) continue;
            const texel = [0, 1].map(axis => Math.floor(weights.reduce((sum, weight, index) => sum + weight * uvs[index][axis], 0) * texture.w / 16));
            const source = (Math.max(0, Math.min(texture.h - 1, texel[1])) * texture.w + Math.max(0, Math.min(texture.w - 1, texel[0]))) * 4;
            if (texture.data[source + 3] < 128) continue;
            depths[destination] = depth;
            for (const axis of [0, 1, 2]) pixels[destination * 4 + axis] = Math.round(texture.data[source + axis] * shade);
        }
    }
}

for (const [panelIndex, panel] of panels.entries()) {
    for (const element of model.elements) {
        for (const [side, points] of Object.entries(corners(element))) {
            const face = element.faces[side];
            if (!face) continue;
            const vertices = points.map(point => transform(point, element, panel)).map(point => [panelIndex * panelWidth + panelWidth / 2 - point[0] * 420, height * 0.54 - point[1] * 420, point[2]]);
            const uvs = coordinates.map(([horizontal, vertical]) => [face.uv[0] + horizontal * (face.uv[2] - face.uv[0]), face.uv[1] + vertical * (face.uv[3] - face.uv[1])]);
            const shade = side === 'up' ? 1 : side === 'down' ? 0.72 : ['east', 'west'].includes(side) ? 0.86 : 0.98;
            for (const order of [[0, 1, 2], [0, 2, 3]]) triangle(order.map(index => vertices[index]), order.map(index => uvs[index]), shade, panelIndex);
        }
    }
}
fs.mkdirSync(path.dirname(path.resolve(outputFile)), { recursive: true });
fs.writeFileSync(outputFile, encodePNG(width, height, pixels));
console.log(`${outputFile}: ${mode}; display panels omit camera, arm pose and hand translation; not an in-game grip calibration.`);
