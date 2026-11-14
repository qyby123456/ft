'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { Canvas } = require('./png.cjs');
const { bones, parts } = require('./benger_spec.cjs');
const { paint } = require('./benger_paint.cjs');
const { serializeOrdered } = require('./anim_key_order.cjs');

const output = path.resolve(process.argv[2] || 'out');
const textureSize = 512;
const density = 2;
const canvas = new Canvas(textureSize, textureSize);
const allocations = [];
const faces = ['north', 'south', 'east', 'west', 'up', 'down'];
let cursorU = 2;
let cursorV = 2;
let rowHeight = 0;
fs.mkdirSync(output, { recursive: true });

function dimensions(part, face) {
    const [width, height, depth] = part.size;
    return face === 'up' || face === 'down' ? [width, depth]
        : face === 'east' || face === 'west' ? [depth, height] : [width, height];
}

function allocate(width, height) {
    if (cursorU + width + 2 > textureSize) {
        cursorU = 2;
        cursorV += rowHeight + 3;
        rowHeight = 0;
    }
    if (cursorV + height + 2 > textureSize) throw new Error('Benger texture atlas overflow');
    const uv = [cursorU, cursorV];
    cursorU += width + 3;
    rowHeight = Math.max(rowHeight, height);
    return uv;
}

const geometry = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
        description: {
            identifier: 'geometry.benger', texture_width: textureSize, texture_height: textureSize,
            visible_bounds_width: 8, visible_bounds_height: 8, visible_bounds_offset: [0, 2, 0]
        },
        bones: bones.map(bone => ({ ...bone, cubes: [] }))
    }]
};
const byName = new Map(geometry['minecraft:geometry'][0].bones.map(bone => [bone.name, bone]));
const rectangles = parts.flatMap(part => faces.map(face => ({
    key: `${part.name}:${face}`,
    extent: dimensions(part, face).map(value => Math.max(1, Math.round(value * density)))
}))).sort((first, second) => second.extent[1] - first.extent[1] || second.extent[0] - first.extent[0]);
const layout = new Map(rectangles.map(rectangle => [rectangle.key, allocate(...rectangle.extent)]));
for (const [partIndex, part] of parts.entries()) {
    const uv = {};
    for (const face of faces) {
        const extent = dimensions(part, face).map(value => Math.max(1, Math.round(value * density)));
        const start = layout.get(`${part.name}:${face}`);
        uv[face] = { uv: start, uv_size: extent };
        allocations.push({ part: part.name, face, uv: start, size: extent });
        for (let row = -1; row <= extent[1]; row++) {
            for (let column = -1; column <= extent[0]; column++) {
                const color = paint(part, face,
                    Math.max(0, Math.min(extent[0] - 1, column)),
                    Math.max(0, Math.min(extent[1] - 1, row)), extent, partIndex);
                canvas.set(start[0] + column, start[1] + row, ...color, 1);
            }
        }
    }
    const cube = { origin: part.origin, size: part.size, uv };
    if (part.rotation) {
        cube.rotation = part.rotation;
        cube.pivot = part.origin.map((value, axis) => value + part.size[axis] / 2);
    }
    byName.get(part.bone).cubes.push(cube);
}
const animationText = fs.readFileSync(path.join(__dirname, 'benger_animation_spec.json'), 'utf8');
const animations = JSON.parse(animationText).animations;
const keyOrder = new Map();
for (const [animationName, animation] of Object.entries(animations)) {
    for (const [boneName, channels] of Object.entries(animation.bones)) {
        for (const [channel, keys] of Object.entries(channels)) {
            keyOrder.set(`/animations/${animationName}/bones/${boneName}/${channel}`,
                Object.keys(keys).sort((first, second) => Number(first) - Number(second)));
        }
    }
}
canvas.save(path.join(output, 'benger.png'));
fs.writeFileSync(path.join(output, 'benger.geo.json'), JSON.stringify(geometry, null, 2) + '\n');
fs.writeFileSync(path.join(output, 'benger.animation.json'),
    serializeOrdered({ format_version: '1.8.0', animations }, keyOrder, 1));
fs.writeFileSync(path.join(output, 'uv-layout.json'), JSON.stringify(allocations, null, 2) + '\n');

function uuid(name) {
    const value = crypto.createHash('sha256').update(`benger-reference:${name}`).digest('hex');
    return `${value.slice(0, 8)}-${value.slice(8, 12)}-4${value.slice(13, 16)}-a${value.slice(17, 20)}-${value.slice(20, 32)}`;
}
const elements = parts.map(part => {
    const bone = byName.get(part.bone);
    const cubeIndex = parts.filter(candidate => candidate.bone === part.bone).indexOf(part);
    const cube = bone.cubes[cubeIndex];
    return {
        name: part.name, uuid: uuid(part.name), type: 'cube', from: part.origin,
        to: part.origin.map((value, axis) => value + part.size[axis]),
        origin: cube.pivot || bone.pivot, rotation: part.rotation || [0, 0, 0],
        autouv: 0, box_uv: false, rescale: false, locked: false, visibility: true,
        faces: Object.fromEntries(faces.map(face => {
            const rectangle = cube.uv[face];
            return [face, { uv: [...rectangle.uv, rectangle.uv[0] + rectangle.uv_size[0],
                rectangle.uv[1] + rectangle.uv_size[1]], texture: 0 }];
        }))
    };
});
function group(bone) {
    return {
        name: bone.name, uuid: uuid(`bone:${bone.name}`), origin: bone.pivot,
        rotation: [0, 0, 0], export: true, visibility: true, isOpen: false,
        children: [...parts.filter(part => part.bone === bone.name).map(part => uuid(part.name)),
            ...bones.filter(child => child.parent === bone.name).map(group)]
    };
}
const blockbench = {
    meta: { format_version: '4.10', model_format: 'bedrock', box_uv: false },
    name: 'Benger - Border Collie Reference Rework', model_identifier: 'benger',
    resolution: { width: textureSize, height: textureSize }, elements,
    outliner: bones.filter(bone => !bone.parent).map(group),
    textures: [{ name: 'benger.png', id: '0', uuid: uuid('texture'), mode: 'bitmap',
        render_mode: 'default', visible: true, saved: false,
        source: `data:image/png;base64,${fs.readFileSync(path.join(output, 'benger.png')).toString('base64')}` }],
    animations: Object.entries(animations).map(([name, animation]) => ({
        name, uuid: uuid(name), loop: animation.loop === true ? 'loop' : animation.loop === 'hold_on_last_frame' ? 'hold' : 'once',
        length: animation.animation_length, snapping: 24,
        animators: Object.fromEntries(Object.entries(animation.bones).map(([boneName, channels]) => [uuid(`bone:${boneName}`), {
            name: boneName, type: 'bone', keyframes: Object.entries(channels).flatMap(([channel, keys]) =>
                Object.entries(keys).sort(([first], [second]) => Number(first) - Number(second)).map(([time, values]) => ({
                    channel, time: Number(time), interpolation: 'linear',
                    uuid: uuid(`${name}:${boneName}:${channel}:${time}`),
                    data_points: [{ x: String(values[0]), y: String(values[1]), z: String(values[2]) }]
                })))
        }]))
    }))
};
fs.writeFileSync(path.join(output, 'Benger_Reference_Rework.bbmodel'), JSON.stringify(blockbench, null, 2) + '\n');
console.log(JSON.stringify({ output, bones: bones.length, cubes: parts.length,
    atlas: [textureSize, textureSize], occupiedHeight: cursorV + rowHeight,
    density, animations: Object.keys(animations).length }, null, 2));
