'use strict';

const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const spec = require('./youzi_statue_spec.cjs');
const space = require('./cube_space.cjs');
const { decodePNG } = require('./png.cjs');

const assetsRoot = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const sourceRoot = path.resolve(process.argv[3] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const model = JSON.parse(fs.readFileSync(path.join(assetsRoot, 'models/item/statue_youzi.json'), 'utf8'));
const texture = decodePNG(path.join(assetsRoot, 'textures/block/statue_youzi.png'));
const source = decodePNG(path.join(sourceRoot, 'textures/entity/easter/youzi.png'));
assert.deepEqual(model, spec.buildModel(spec.statueTexture(source)), 'Generated model differs from its source specification');
assert.deepEqual(texture.data, spec.statueTexture(source).data, 'Generated texture differs from its source specification');
assert.deepEqual(fs.readFileSync(path.join(assetsRoot, 'textures/item/statue_youzi.png')), fs.readFileSync(path.join(assetsRoot, 'textures/block/statue_youzi.png')));
assert.equal(model.elements.length, 12);
assert.equal(model.parent, 'minecraft:block/block');
assert.equal(model.ambientocclusion, false);
assert.equal(Object.keys(model.display).length, 8);
let faces = 0;
let overlayPixels = 0;
for (const part of spec.PARTS) {
    for (const rectangle of Object.values(spec.faceRects(part, false))) {
        const [left, top, right, bottom] = rectangle;
        for (let row = Math.min(top, bottom); row < Math.max(top, bottom); row++) {
            for (let column = Math.min(left, right); column < Math.max(left, right); column++) {
                const offset = (row * source.w + column) * 4;
                assert.deepEqual(texture.data.subarray(offset, offset + 4), source.data.subarray(offset, offset + 4), `${part.name}: base skin changed`);
            }
        }
    }
}
for (const element of model.elements) {
    for (const [axis, minimum] of element.from.entries()) {
        assert(Number.isFinite(minimum) && minimum < element.to[axis]);
        assert(minimum >= -16 && element.to[axis] <= 32);
    }
    for (const face of Object.values(element.faces)) {
        faces++;
        assert.equal(face.texture, '#0');
        assert(face.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 16));
        const [left, top, right, bottom] = face.uv.map(value => value * 4);
        let opaque = 0;
        for (let row = Math.min(top, bottom); row < Math.max(top, bottom); row++) {
            for (let column = Math.min(left, right); column < Math.max(left, right); column++) {
                if (texture.data[(row * texture.w + column) * 4 + 3] >= 128) opaque++;
            }
        }
        assert(opaque > 0, `${element.name}: blank face`);
        if (element.name.endsWith('_overlay')) overlayPixels += opaque;
    }
}
const stationary = model.elements.filter(element => !element.rotation);
for (let firstIndex = 0; firstIndex < stationary.length; firstIndex++) {
    for (const second of stationary.slice(firstIndex + 1)) {
        const first = stationary[firstIndex];
        for (const axis of [0, 1, 2]) {
            const otherAxes = [0, 1, 2].filter(value => value !== axis);
            const overlaps = otherAxes.every(value => Math.min(first.to[value], second.to[value]) - Math.max(first.from[value], second.from[value]) > 1e-6);
            if (!overlaps) continue;
            for (const boundary of ['from', 'to']) assert(Math.abs(first[boundary][axis] - second[boundary][axis]) > 1e-6, `${first.name}/${second.name}: coplanar surfaces`);
        }
    }
}
for (const slot of ['thirdperson_righthand', 'thirdperson_lefthand']) {
    const grip = space.applyDisplayTransform(spec.GRIP.map(value => (value - 8) / 16), model.display[slot]);
    assert(grip.every(value => Math.abs(value) < 1e-7), `${slot}: grip is not centered in display space`);
}
assert(overlayPixels > 0);
assert(texture.data.some((value, index) => index % 4 === 3 && value === 0), 'Overlay transparency was not restored');
console.log(`PASS: ${model.elements.length} parts, ${faces} textured faces, base skin unchanged, overlays transparent, no stationary coplanar overlaps, both display-space grip anchors centered.`);
console.log('Hand-space grip checks do not replace an in-game screenshot calibration.');
