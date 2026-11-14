'use strict';

const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { loadGeo } = require('./cube_space.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const statues = [
    { id: 'sugar_queen', geo: 'sugar_queen_v3', texture: 'sugar_queen_v3', badge: ['10101', '11111', '01110'] },
    { id: 'butcher', texture: 'butcher_v3', badge: ['01111', '01111', '01000'] },
    { id: 'saya', texture: 'tea_alex', badge: ['00110', '01100', '11000'] },
    { id: 'wine_barrel', geo: 'wine_barrel_boss', texture: 'wine_barrel_boss_v2', badge: ['01110', '10101', '01110'] },
    { id: 'rat_swarm', geo: 'big_rat', texture: 'big_rat', badge: ['10101', '00000', '01110'],
        pose: { body: { rotation: [40, 0, 0] }, head: { rotation: [-20, 0, 0] },
            tail1: { rotation: [0, 65, 0] }, tail2: { rotation: [0, 35, 0] }, tail3: { rotation: [0, 25, 0] } } },
    { id: 'defect', geo: 'defect_spire', texture: 'defect_spire_lightning', badge: ['00110', '01100', '00100'] },
    { id: 'compost', geo: 'compost_superman', texture: 'compost_superman', badge: ['00100', '01110', '11111'] },
    { id: 'apple', geo: 'apple_boss', texture: 'apple_boss', badge: ['00110', '11111', '01110'] },
    { id: 'tide_fish', geo: 'tide_fish_boss', texture: 'tide_fish_boss', badge: ['10101', '11111', '00100'] }
];

function sourceModel(statue, scratch) {
    if (statue.geo) return loadGeo(path.join(assets, `geo/${statue.geo}.geo.json`));
    fs.mkdirSync(scratch, { recursive: true });
    if (statue.id === 'butcher') {
        const output = path.join(scratch, 'butcher');
        const generated = spawnSync(process.execPath, [path.join(__dirname, 'gen_butcher.cjs'), output], { encoding: 'utf8' });
        if (generated.status !== 0) throw new Error(generated.stderr || generated.stdout);
        for (const [relative, original] of [
            ['ButcherBossModel.java', path.join(root, 'src/main/java/net/farmingtales/ftboss/client/model/ButcherBossModel.java')],
            ['butcher_v3.png', path.join(assets, 'textures/entity/butcher_v3.png')]
        ]) {
            if (!fs.readFileSync(path.join(output, relative)).equals(fs.readFileSync(original))) {
                throw new Error(`Butcher source generator differs from the runtime asset: ${relative}`);
            }
        }
        return loadGeo(path.join(output, 'butcher_preview.geo.json'));
    }
    const parts = [
        ['head', [-4, 24, -4], [8, 8, 8], [0, 0], [32, 0], 0.5],
        ['body', [-4, 12, -2], [8, 12, 4], [16, 16], [16, 32], 0.25],
        ['arm_r', [-7, 12, -2], [3, 12, 4], [40, 16], [40, 32], 0.25],
        ['arm_l', [4, 12, -2], [3, 12, 4], [32, 48], [48, 48], 0.25],
        ['leg_r', [-4, 0, -2], [4, 12, 4], [0, 16], [0, 32], 0.25],
        ['leg_l', [0, 0, -2], [4, 12, 4], [16, 48], [0, 48], 0.25]
    ];
    const geometry = { format_version: '1.12.0', 'minecraft:geometry': [{
        description: { identifier: 'geometry.statue_saya', texture_width: 64, texture_height: 64 },
        bones: parts.map(([name, origin, size, uv, overlay, inflate]) => ({ name, pivot: [0, 0, 0],
            cubes: [{ origin, size, uv }, { origin, size, uv: overlay, inflate }] }))
    }] };
    const file = path.join(scratch, 'saya.geo.json');
    fs.writeFileSync(file, JSON.stringify(geometry));
    return loadGeo(file);
}

function faceMapping(cube) {
    if (!Array.isArray(cube.uv)) return cube.uv;
    const [left, top] = cube.uv;
    const [width, height, depth] = cube.size;
    const mapping = {
        north: { uv: [left + depth, top + depth], uv_size: [width, height] },
        south: { uv: [left + depth * 2 + width, top + depth], uv_size: [width, height] },
        east: { uv: [left, top + depth], uv_size: [depth, height] },
        west: { uv: [left + depth + width, top + depth], uv_size: [depth, height] },
        up: { uv: [left + depth, top], uv_size: [width, depth] },
        down: { uv: [left + depth + width, top + depth], uv_size: [width, -depth] }
    };
    if (cube.mirror) {
        [mapping.east, mapping.west] = [mapping.west, mapping.east];
        for (const region of Object.values(mapping)) {
            region.uv[0] += region.uv_size[0];
            region.uv_size[0] *= -1;
        }
    }
    return mapping;
}

module.exports = { statues, sourceModel, faceMapping, assets };
