const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { Canvas, decodePNG, encodePNG } = require('./png.cjs');
const assets = path.resolve(process.argv[2]);
const output = path.resolve(process.argv[3]);
fs.mkdirSync(output, { recursive: true });
const skin = decodePNG(path.join(assets, 'textures/entity/easter/qizai.png'));
const staff = decodePNG(path.join(assets, 'textures/item/qizai_prayer_staff.png'));
if (skin.w !== 64 || skin.h !== 64) throw new Error('Expected original 64x64 skin');
const atlas = new Uint8Array(128 * 64 * 4);
for (const [texture, offset] of [[skin, 0], [staff, 64]]) {
    for (let row = 0; row < 64; row++) for (let column = 0; column < 64; column++) {
        const from = (row * 64 + column) * 4;
        atlas.set(texture.data ? texture.data.subarray(from, from + 4) : texture.rgba.subarray(from, from + 4), (row * 128 + column + offset) * 4);
    }
}
const bones = [{ name: 'player', pivot: [0, 0, 0], cubes: [] }];
function playerPreviewUv(size, uv) {
    const [width, height, depth] = size;
    const [left, top] = uv;
    return {
        up: { uv: [left + depth, top], uv_size: [width, depth] },
        down: { uv: [left + depth + width, top], uv_size: [width, depth] },
        east: { uv: [left, top + depth], uv_size: [depth, -height] },
        west: { uv: [left + depth + width, top + depth], uv_size: [depth, -height] },
        north: { uv: [left + depth, top + depth], uv_size: [width, -height] },
        south: { uv: [left + depth * 2 + width, top + depth], uv_size: [width, -height] }
    };
}
function body(name, origin, size, uv, overlay) {
    const cubes = [{ origin, size, uv: playerPreviewUv(size, uv) }];
    if (overlay) cubes.push({ origin, size, uv: playerPreviewUv(size, overlay), inflate: 0.25 });
    bones.push({ name, parent: 'player', pivot: [0, 0, 0], cubes });
}
body('head', [-4, 24, -4], [8, 8, 8], [0, 0], [32, 0]);
body('body', [-4, 12, -2], [8, 12, 4], [16, 16], [16, 32]);
body('right_arm', [-7, 12, -2], [3, 12, 4], [40, 16], [40, 32]);
body('left_arm', [4, 12, -2], [3, 12, 4], [32, 48], [48, 48]);
body('right_leg', [-4, 0, -2], [4, 12, 4], [0, 16], [0, 32]);
body('left_leg', [0, 0, -2], [4, 12, 4], [16, 48], [0, 48]);
const geometry = JSON.parse(fs.readFileSync(path.join(assets, 'geo/qizai_prayer_staff.geo.json')));
for (const original of geometry['minecraft:geometry'][0].bones) {
    const bone = structuredClone(original);
    bone.name = 'staff_' + bone.name;
    if (bone.parent) bone.parent = 'staff_' + bone.parent;
    else bone.position = [-11, 12, -0.5];
    bone.pivot = bone.pivot.map(value => value * 0.75);
    for (const cube of bone.cubes) {
        cube.origin = cube.origin.map(value => value * 0.75);
        cube.size = cube.size.map(value => value * 0.75);
        if (cube.pivot) cube.pivot = cube.pivot.map(value => value * 0.75);
        for (const face of Object.values(cube.uv)) face.uv[0] += 64;
    }
    bones.push(bone);
}
const preview = { format_version: '1.12.0', 'minecraft:geometry': [{ description: {
    identifier: 'geometry.qizai_asset_preview', texture_width: 128, texture_height: 64
}, bones }] };
const geoFile = path.join(output, 'qizai-preview.geo.json');
const textureFile = path.join(output, 'qizai-preview-atlas.png');
fs.writeFileSync(geoFile, JSON.stringify(preview, null, 2));
fs.writeFileSync(textureFile, encodePNG(128, 64, atlas));
const image = path.join(output, 'qizai-preview.png');
const result = spawnSync(process.execPath, [path.join(__dirname, 'render_tex.cjs'), geoFile, textureFile, image], { encoding: 'utf8' });
process.stdout.write(result.stdout); process.stderr.write(result.stderr);
if (result.status !== 0) process.exit(result.status);
console.log('Preview uses original skin and generated staff, no substitute artwork.');
