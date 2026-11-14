'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { decodePNG, encodePNG } = require('./png.cjs');

const SCALE = 15 / 32;
const FEET = 0.75;
const PARTS = [
    { name: 'head', from: [-4, 24, -4], size: [8, 8, 8], uv: [0, 0], overlay: [32, 0], inflate: 0.5, padding: 0.016 },
    { name: 'body', from: [-4, 12, -2], size: [8, 12, 4], uv: [16, 16], overlay: [16, 32], inflate: 0.3, padding: 0.02 },
    { name: 'arm_r', from: [4, 11.5, -2], size: [3, 12, 4], uv: [40, 16], overlay: [40, 32], pivot: [5, 22.5, 0], angle: 22.5 },
    { name: 'arm_l', from: [-7, 11.5, -2], size: [3, 12, 4], uv: [32, 48], overlay: [48, 48], pivot: [-5, 22.5, 0], angle: -22.5 },
    { name: 'leg_r', from: [0.3, 0, -2], size: [3.7, 12, 4], uvSize: [4, 12, 4], uv: [0, 16], overlay: [0, 32], inflate: 0.2, padding: 0.008 },
    { name: 'leg_l', from: [-4, 0, -2], size: [3.7, 12, 4], uvSize: [4, 12, 4], uv: [16, 48], overlay: [0, 48], inflate: 0.2, padding: 0.008 },
];

const round = value => Math.round(value * 1000000) / 1000000;
const position = point => [8 + point[0] * SCALE, FEET + point[1] * SCALE, 8 + point[2] * SCALE].map(round);
const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
const GRIP = position([0, 13, 0]);
const HAND_SCALE = 0.7;
const DISPLAY = {
    thirdperson_righthand: transform([90, 0, 0], [0, 0, round((8 - GRIP[1]) * HAND_SCALE)], HAND_SCALE),
    thirdperson_lefthand: transform([90, 0, 0], [0, 0, round((8 - GRIP[1]) * HAND_SCALE)], HAND_SCALE),
    firstperson_righthand: transform([0, 155, 0], [0.5, 1.5, 0], 0.8),
    firstperson_lefthand: transform([0, 155, 0], [0.5, 1.5, 0], 0.8),
    gui: transform([10, 165, 0], [0, 0, 0], 0.98),
    ground: transform([0, 0, 0], [0, 2.25, 0], 0.55),
    fixed: transform([0, 180, 0], [0, 0, 0], 0.95),
    head: transform([0, 180, 0], [0, 8, 0], 0.75),
};

function faceRects(part, overlay) {
    const [width, height, depth] = part.uvSize || part.size;
    const [horizontal, vertical] = overlay ? part.overlay : part.uv;
    return {
        up: [horizontal + depth, vertical, horizontal + depth + width, vertical + depth],
        down: [horizontal + depth + width, vertical + depth, horizontal + depth + 2 * width, vertical],
        north: [horizontal + depth, vertical + depth, horizontal + depth + width, vertical + depth + height],
        south: [horizontal + 2 * depth + width, vertical + depth, horizontal + 2 * depth + 2 * width, vertical + depth + height],
        east: [horizontal, vertical + depth, horizontal + depth, vertical + depth + height],
        west: [horizontal + depth + width, vertical + depth, horizontal + 2 * depth + width, vertical + depth + height],
    };
}

function hasPixels(texture, rectangle) {
    const [left, top, right, bottom] = rectangle;
    for (let row = Math.min(top, bottom); row < Math.max(top, bottom); row++) {
        for (let column = Math.min(left, right); column < Math.max(left, right); column++) {
            if (texture.data[(row * texture.w + column) * 4 + 3] > 0) return true;
        }
    }
    return false;
}

function statueTexture(skin) {
    const texture = { w: skin.w, h: skin.h, data: new Uint8Array(skin.data) };
    for (const part of PARTS) {
        for (const rectangle of Object.values(faceRects(part, true))) {
            const [left, top, right, bottom] = rectangle;
            for (let row = Math.min(top, bottom); row < Math.max(top, bottom); row++) {
                for (let column = Math.min(left, right); column < Math.max(left, right); column++) {
                    const offset = (row * texture.w + column) * 4;
                    if (Math.max(...texture.data.subarray(offset, offset + 3)) <= 16) texture.data[offset + 3] = 0;
                }
            }
        }
    }
    return texture;
}

function element(part, overlay, texture) {
    const padding = overlay ? (part.inflate || 0.25) * SCALE : (part.padding || 0.012);
    const minimum = position(part.from);
    const maximum = position(part.from.map((value, axis) => value + part.size[axis]));
    const faces = {};
    for (const [side, rectangle] of Object.entries(faceRects(part, overlay))) {
        if (overlay && !hasPixels(texture, rectangle)) continue;
        faces[side] = { uv: rectangle.map(value => value / 4), texture: '#0' };
    }
    const result = {
        name: part.name + (overlay ? '_overlay' : ''),
        from: minimum.map(value => round(value - padding)),
        to: maximum.map(value => round(value + padding)),
        faces,
    };
    if (part.angle) result.rotation = { origin: position(part.pivot), axis: 'z', angle: part.angle, rescale: false };
    return result;
}

function buildModel(texture) {
    assert.equal(texture.w, 64, 'Youzi requires a 64x64 slim player skin');
    assert.equal(texture.h, 64, 'Youzi requires a 64x64 slim player skin');
    const elements = PARTS.flatMap(part => [element(part, false, texture), element(part, true, texture)])
        .filter(part => Object.keys(part.faces).length > 0);
    return {
        parent: 'minecraft:block/block',
        ambientocclusion: false,
        gui_light: 'front',
        textures: { '0': 'ftboss:block/statue_youzi', particle: 'ftboss:block/statue_youzi' },
        display: DISPLAY,
        elements,
    };
}

function generate(assetsRoot = path.resolve(__dirname, '../../src/main/resources/assets/ftboss'), outputRoot = assetsRoot) {
    const skin = path.join(assetsRoot, 'textures/entity/easter/youzi.png');
    const texture = statueTexture(decodePNG(skin));
    const model = buildModel(texture);
    const relative = 'models/item/statue_youzi.json';
    for (const directory of ['models/item', 'textures/block', 'textures/item']) fs.mkdirSync(path.join(outputRoot, directory), { recursive: true });
    fs.writeFileSync(path.join(outputRoot, relative), JSON.stringify(model, null, 2) + '\n');
    for (const directory of ['block', 'item']) fs.writeFileSync(path.join(outputRoot, `textures/${directory}/statue_youzi.png`), encodePNG(texture.w, texture.h, texture.data));
    console.log(`${relative}: ${model.elements.length} elements, ${Object.keys(DISPLAY).length} display slots; grip=${GRIP.join(',')}`);
    return model;
}

module.exports = { PARTS, DISPLAY, GRIP, SCALE, FEET, faceRects, statueTexture, buildModel, generate };
