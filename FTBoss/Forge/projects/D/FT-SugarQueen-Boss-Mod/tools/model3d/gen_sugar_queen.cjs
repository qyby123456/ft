'use strict';

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { Canvas } = require('./png.cjs');
const output = path.resolve(process.argv[2] || 'art/sugar-queen-v3');
const atlasSize = 256;
const canvas = new Canvas(atlasSize, atlasSize);
const bones = [], parts = [];
const rounded = value => Math.round(value * 10000) / 10000;
const palette = {
    cream: [249, 237, 211], icing: [255, 248, 232], vanilla: [226, 208, 197],
    skin: [250, 215, 199], blush: [227, 140, 151], rose: [198, 92, 133],
    petal: [234, 162, 180], jam: [109, 40, 77], velvet: [134, 47, 87],
    gold: [226, 178, 99], crystal: [163, 223, 221], glint: [226, 255, 243], ruby: [196, 63, 113],
};
function bone(name, parent, pivot) {
    const result = { name, pivot, cubes: [] };
    if (parent) result.parent = parent;
    bones.push(result);
    return name;
}
function centered(owner, name, center, size, material, rotation, pattern = '') {
    const result = { origin: center.map((value, axis) => rounded(value - size[axis] / 2)), size: size.map(rounded) };
    if (rotation) { result.rotation = rotation; result.pivot = center.map(rounded); }
    bones.find(entry => entry.name === owner).cubes.push(result);
    parts.push({ owner, name, result, material, pattern });
    return result;
}
function gem(owner, name, center, size, material = 'crystal') {
    centered(owner, name + '_setting', center, [size * 1.25, size * 1.25, size * 0.65], 'gold', [0, 0, 45]);
    centered(owner, name, [center[0], center[1], center[2] - size * 0.27], [size, size, size * 0.55], material, [0, 0, 45]);
}
bone('root', null, [0, 0, 0]);
bone('body', 'root', [0, 21, 0]);
bone('waist', 'body', [0, 22, 0]);
bone('chest', 'waist', [0, 26, 0]);
centered('waist', 'corset', [0, 24, 0], [7.4, 6, 5.2], 'rose', null, 'corset');
centered('chest', 'bodice', [0, 28, 0], [9.2, 4.4, 5.8], 'cream', null, 'bodice');
centered('waist', 'waist_belt', [0, 22.1, -0.1], [8.1, 1.2, 5.7], 'gold');
centered('waist', 'belt_ribbon', [0, 22.2, -3.05], [5, 1.65, 0.6], 'jam');
gem('waist', 'belt_jewel', [0, 22.15, -3.5], 1.5, 'ruby');
centered('chest', 'neck', [0, 31, 0], [3.3, 3.8, 3.4], 'skin');
for (const side of [-1, 1]) {
    centered('chest', 'ruff_' + side, [side * 3.4, 30.25, 0.6], [4.6, 2.2, 6.3], 'icing', [0, 0, side * 20]);
    centered('chest', 'lapel_' + side, [side * 2.2, 28.2, -3.15], [3.7, 1.1, 0.65], 'gold', [0, 0, side * -28]);
}
gem('chest', 'sugar_heart', [0, 28.2, -3.55], 1.9, 'ruby');
bone('head', 'chest', [0, 32, 0]);
centered('head', 'face', [0, 35.2, -0.15], [7.6, 7.4, 6.1], 'skin');
centered('head', 'hair_cap', [0, 38.55, 0.2], [8.5, 2.5, 7.1], 'icing', null, 'hair');
centered('head', 'hair_back', [0, 35.9, 2.8], [8.2, 6.7, 2], 'vanilla', null, 'hair');
for (const side of [-1, 1]) {
    centered('head', 'fringe_' + side, [side * 2.1, 37.6, -3.25], [3.65, 2.3, 1.1], 'cream', [0, 0, side * 13], 'hair');
    centered('head', 'brow_' + side, [side * 2.1, 36.25, -3.29], [2, 0.45, 0.45], 'jam', [0, 0, side * -10]);
    centered('head', 'eye_' + side, [side * 2, 35.25, -3.33], [1.7, 1.25, 0.5], 'jam');
    centered('head', 'iris_' + side, [side * 1.85, 35.22, -3.64], [0.75, 0.95, 0.25], 'ruby');
    centered('head', 'eye_glint_' + side, [side * 1.85 - 0.17, 35.55, -3.83], [0.36, 0.36, 0.18], 'icing');
    centered('head', 'cheek_' + side, [side * 2.9, 34.1, -3.3], [1, 0.55, 0.35], 'blush');
    const suffix = side < 0 ? 'l' : 'r';
    bone('curl_' + suffix, 'head', [side * 4, 37.3, 0.2]);
    centered('curl_' + suffix, 'temple', [side * 4.1, 35.8, 0.15], [2.3, 4.7, 5.6], 'cream', [0, 0, side * 6], 'hair');
    bone('curl_tip_' + suffix, 'curl_' + suffix, [side * 4.4, 33.6, 0.5]);
    centered('curl_tip_' + suffix, 'frosting_curl', [side * 4.4, 32.2, 0.7], [2.5, 3.6, 3.8], 'icing', [0, 0, side * -12], 'hair');
    centered('curl_tip_' + suffix, 'curl_rose', [side * 4.4, 32.3, -1.43], [1.7, 1.7, 0.65], 'petal', [0, 0, 45]);
    centered('curl_tip_' + suffix, 'curl_pearl', [side * 4.4, 32.3, -1.87], [0.8, 0.8, 0.4], 'gold');
}
centered('head', 'mouth', [0, 33.1, -3.38], [1.35, 0.45, 0.4], 'rose');
bone('crown', 'head', [0, 39.2, 0]);
centered('crown', 'crown_band', [0, 39.4, 0], [8.3, 1.1, 6.6], 'gold', null, 'crown');
centered('crown', 'crown_inlay', [0, 39.7, -3.47], [7, 0.55, 0.4], 'jam');
for (let index = -2; index <= 2; index++) {
    const height = 2.2 + (2 - Math.abs(index)) * 0.8;
    const horizontal = index * 1.75;
    centered('crown', 'sugar_prong_' + index, [horizontal, 39.6 + height / 2, -2.35], [0.8, height, 1.05], 'gold', [0, 0, -index * 7]);
    centered('crown', 'crown_crystal_' + index, [horizontal * 1.08, 39.9 + height, -2.5], [1.05, 1.55, 1.05], 'crystal', [0, 0, -index * 9]);
}
gem('crown', 'royal_ruby', [0, 40.45, -3.8], 1.1, 'ruby');
bone('skirt', 'body', [0, 22, 0]);
const tiers = [
    { name: 'upper', bottom: 15, top: 22.1, radius: 5.4, depth: 4.4, material: 'petal' },
    { name: 'middle', bottom: 8.5, top: 15.4, radius: 7.2, depth: 5.65, material: 'rose' },
    { name: 'lower', bottom: 3, top: 8.9, radius: 9.1, depth: 7.0, material: 'jam' },
];
for (const [tierIndex, tier] of tiers.entries()) {
    const owner = bone('skirt_' + tier.name, tierIndex === 0 ? 'skirt' : 'skirt_' + tiers[tierIndex - 1].name, [0, tier.top, 0]);
    const height = tier.top - tier.bottom;
    centered(owner, tier.name + '_core', [0, tier.bottom + height / 2, 0], [tier.radius * 1.42, height - 0.4, tier.depth * 1.42], tier.material, null, 'skirt');
    for (let panel = 0; panel < 8; panel++) {
        const angle = panel * Math.PI / 4, rotation = [0, -panel * 45, 0];
        const radial = [Math.sin(angle) * tier.radius * 0.75, -Math.cos(angle) * tier.depth * 0.75];
        const width = Math.hypot(tier.radius * Math.cos(angle), tier.depth * Math.sin(angle)) * 0.83;
        const front = panel === 0;
        centered(owner, 'panel_' + panel, [radial[0], tier.bottom + height / 2 + panel * 0.025, radial[1]], [width, height, 2.4], front ? 'cream' : tier.material, rotation, front ? 'filigree' : 'skirt');
        const trim = [Math.sin(angle) * (tier.radius * 0.75 + 0.35), tier.bottom + 0.55, -Math.cos(angle) * (tier.depth * 0.75 + 0.35)];
        centered(owner, 'hem_' + panel, [trim[0], trim[1] + panel * 0.025, trim[2]], [width + 0.3, 1, 2.6], 'icing', rotation);
        for (const offset of [-0.3, 0.3]) {
            const tangent = offset * width;
            centered(owner, 'piped_icing_' + panel + '_' + offset, [trim[0] + Math.cos(angle) * tangent, tier.bottom + 1.4, trim[2] + Math.sin(angle) * tangent], [1.1, 1.25 + (panel % 2) * 0.35, 2.8], 'cream', rotation);
        }
        if (tierIndex !== 0 && panel % 2 === 0) {
            centered(owner, 'candied_pearl_' + panel, [Math.sin(angle) * (tier.radius * 0.75 + 1.45), tier.bottom + height * 0.62, -Math.cos(angle) * (tier.depth * 0.75 + 1.45)], [1, 1, 0.7], front ? 'ruby' : 'gold', rotation);
        }
    }
}
for (const side of [-1, 1]) {
    const suffix = side < 0 ? 'l' : 'r';
    bone('leg_' + suffix, 'root', [side * 2.6, 12, 0]);
    centered('leg_' + suffix, 'stocking', [side * 2.6, 8.5, 0], [2.7, 8.1, 2.8], 'cream');
    bone('shin_' + suffix, 'leg_' + suffix, [side * 2.6, 6, 0]);
    centered('shin_' + suffix, 'boot', [side * 2.6, 3.1, 0], [2.85, 5.4, 3], 'rose');
    bone('foot_' + suffix, 'shin_' + suffix, [side * 2.6, 1.5, 0]);
    centered('foot_' + suffix, 'slipper', [side * 2.6, 0.85, -1], [3.2, 1.7, 5.2], 'jam');
    centered('foot_' + suffix, 'slipper_buckle', [side * 2.6, 1.85, -2.4], [1.6, 0.7, 1.5], 'gold');
    bone('arm_' + suffix, 'chest', [side * 6.1, 29.1, 0]);
    centered('arm_' + suffix, 'puff_sleeve', [side * 6.35, 27.75, 0], [4.7, 4.4, 5.8], 'petal', [0, 0, -side * 10], 'sleeve');
    centered('arm_' + suffix, 'upper_arm', [side * 6.7, 25.45, 0], [2.75, 4.1, 3], 'skin');
    centered('arm_' + suffix, 'sleeve_trim', [side * 6.7, 25.4, 0], [3.4, 1, 3.65], 'icing');
    bone('forearm_' + suffix, 'arm_' + suffix, [side * 6.7, 24.2, 0]);
    centered('forearm_' + suffix, 'glove', [side * 6.7, 21.7, -0.1], [2.6, 5.3, 2.8], 'cream', null, 'glove');
    centered('forearm_' + suffix, 'cuff', [side * 6.7, 23.8, -0.1], [3.2, 1, 3.4], 'gold');
    bone('hand_' + suffix, 'forearm_' + suffix, [side * 6.7, 19.8, -0.1]);
    centered('hand_' + suffix, 'hand', [side * 6.7, 19.35, -0.1], [2.75, 2, 2.95], 'icing');
    centered('hand_' + suffix, 'thumb', [side * 5.3, 19.65, -0.8], [0.8, 1.3, 1.45], 'cream');
    gem('forearm_' + suffix, 'wrist_jewel', [side * 6.7, 22.9, -1.85], 0.8);
}
bone('scepter', 'hand_r', [6.7, 19.35, -0.1]);
centered('scepter', 'candy_shaft', [6.7, 19.5, -0.7], [1.25, 30, 1.25], 'icing', null, 'candy');
for (const height of [5.3, 17.5, 21, 30.5, 33.1]) centered('scepter', 'scepter_band_' + height, [6.7, height, -0.7], [1.8, 0.85, 1.8], 'gold');
centered('scepter', 'pommel', [6.7, 4.1, -0.7], [1.6, 1.6, 1.6], 'ruby', [0, 0, 45]);
bone('scepter_head', 'scepter', [6.7, 34, -0.7]);
centered('scepter_head', 'sugar_orb', [6.7, 35.3, -0.7], [3.7, 4.5, 3.7], 'crystal', [0, 0, 45]);
centered('scepter_head', 'orb_inner', [6.7, 35.3, -2.9], [1.7, 2.2, 0.7], 'glint', [0, 0, 45]);
for (const side of [-1, 1]) {
    centered('scepter_head', 'cage_' + side, [6.7 + side * 2.7, 34.9, -0.7], [0.85, 5.4, 1.3], 'gold', [0, 0, side * -18]);
    centered('scepter_head', 'cage_tip_' + side, [6.7 + side * 1.9, 37.4, -0.7], [1.15, 1.9, 1.5], 'cream', [0, 0, side * 35]);
    centered('scepter', 'bow_' + side, [6.7 + side * 1.8, 31.1, -1.1], [3.4, 1.9, 1], 'rose', [0, 0, side * 22]);
}
gem('scepter', 'bow_knot', [6.7, 31.1, -1.8], 1.0, 'ruby');
bone('cape', 'chest', [0, 29, 3.2]);
centered('cape', 'mantle', [0, 27, 3.8], [10.8, 5.7, 1.4], 'velvet', null, 'cape');
centered('cape', 'mantle_fur', [0, 29, 3.95], [11.2, 1.7, 1.9], 'icing');
for (const side of [-1, 1]) {
    const suffix = side < 0 ? 'l' : 'r';
    bone('cape_' + suffix, 'cape', [side * 2.9, 25, 4.1]);
    centered('cape_' + suffix, 'cape_middle', [side * 3.2, 20.9, 4.8], [5.7, 9.1, 1.2], 'velvet', [-7, 0, side * 2], 'cape');
    bone('cape_tip_' + suffix, 'cape_' + suffix, [side * 3.6, 17, 5.3]);
    centered('cape_tip_' + suffix, 'cape_tail', [side * 4.1, 12.7, 6.1], [5.6, 9.7, 1.2], 'jam', [-8, 0, side * 4], 'cape');
    centered('cape_tip_' + suffix, 'tail_hem', [side * 4.45, 8.6, 6.7], [5.9, 1.1, 1.6], 'gold', [-8, 0, side * 4]);
}

function paint(island) {
    for (let vertical = -1; vertical <= island.height; vertical++) {
        for (let horizontal = -1; horizontal <= island.width; horizontal++) {
            const pixelX = Math.max(0, Math.min(island.width - 1, horizontal));
            const pixelY = Math.max(0, Math.min(island.height - 1, vertical));
            const normalizedX = pixelX / Math.max(1, island.width - 1);
            const normalizedY = pixelY / Math.max(1, island.height - 1);
            const edge = pixelX === 0 || pixelY === island.height - 1 || pixelX === island.width - 1;
            let color = palette[island.material];
            let shade = 1 - normalizedY * 0.07 + (normalizedX < 0.3 ? 0.035 : -0.015);
            if (edge && island.width > 3 && island.height > 3) shade -= 0.09;
            if (island.face === 'up') shade += 0.06;
            if (island.face === 'down') shade -= 0.13;
            if (island.pattern === 'hair' && pixelX % 4 === 0) shade -= 0.08;
            if (island.pattern === 'skirt' && pixelX % 6 === 0) shade -= 0.13;
            if (island.pattern === 'cape') {
                if (pixelX === 1 || pixelX === island.width - 2 || pixelY === island.height - 2) color = palette.gold;
                else if ((pixelX + 2 * pixelY) % 13 === 0 && pixelY % 3 === 0) shade += 0.13;
            }
            if (island.pattern === 'filigree') {
                const diamond = Math.abs(pixelX - (island.width - 1) / 2) + Math.abs(pixelY - (island.height - 1) / 2);
                if (pixelX === 1 || pixelX === island.width - 2) color = palette.gold;
                else if (diamond < 2) color = palette.rose;
                else if (Math.abs(diamond - 3.5) < 0.55) color = palette.gold;
            }
            if (island.pattern === 'corset') {
                if (Math.abs(pixelX - (island.width - 1) / 2) < 0.8) color = palette.gold;
                if (Math.abs(pixelX - (island.width - 1) / 2) < 1.8 && pixelY % 3 === 0) color = palette.cream;
            }
            if (island.pattern === 'sleeve' && pixelX % 5 === 0) color = palette.cream;
            if (island.pattern === 'candy') color = ((pixelY + pixelX) % 7 < 3) ? palette.ruby : palette.icing;
            if (island.material === 'crystal' || island.material === 'ruby') {
                shade += pixelX < island.width / 2 ? 0.13 : -0.12;
                if (pixelY === 1 && pixelX < island.width / 2) color = palette.glint;
            }
            const channels = color.map(channel => Math.max(0, Math.min(255, Math.round(channel * shade))));
            canvas.set(island.origin[0] + horizontal, island.origin[1] + vertical, ...channels, 1);
        }
    }
}
const islands = new Map();
for (const part of parts) {
    const [width, height, depth] = part.result.size;
    part.result.uv = {};
    for (const [face, dimensions] of Object.entries({ north: [width, height], south: [width, height], east: [depth, height], west: [depth, height], up: [width, depth], down: [width, depth] })) {
        const pixelWidth = Math.max(1, Math.ceil(dimensions[0] * 1.5));
        const pixelHeight = Math.max(1, Math.ceil(dimensions[1] * 1.5));
        const pattern = ['north', 'south'].includes(face) ? part.pattern : '';
        const key = [part.material, pattern, pixelWidth, pixelHeight, face === 'up' ? 'top' : face === 'down' ? 'bottom' : 'side'].join(':');
        if (!islands.has(key)) islands.set(key, { key, width: pixelWidth, height: pixelHeight, material: part.material, pattern, face, users: [] });
        islands.get(key).users.push({ result: part.result, face });
    }
}
let cursorX = 1, cursorY = 1, rowHeight = 0;
for (const island of [...islands.values()].sort((left, right) => right.height - left.height || right.width - left.width || left.key.localeCompare(right.key))) {
    if (cursorX + island.width + 1 > atlasSize) { cursorX = 1; cursorY += rowHeight + 2; rowHeight = 0; }
    if (cursorY + island.height + 1 > atlasSize) throw new Error('Texture atlas overflow');
    island.origin = [cursorX, cursorY];
    for (const user of island.users) user.result.uv[user.face] = { uv: island.origin.slice(), uv_size: [island.width, island.height] };
    paint(island);
    cursorX += island.width + 2;
    rowHeight = Math.max(rowHeight, island.height);
}

const animations = {};
function animation(name, length, loop, channels) {
    const tracks = {};
    for (const [owner, source] of Object.entries(channels)) {
        tracks[owner] = {};
        for (const [channel, stops] of Object.entries(source)) {
            const frames = {};
            for (let index = 0; index <= Math.ceil(length * 24); index++) {
                const time = Math.min(index / 24, length);
                let start = stops[0], end = stops[stops.length - 1];
                for (let segment = 1; segment < stops.length; segment++) {
                    if (time <= stops[segment][0]) { start = stops[segment - 1]; end = stops[segment]; break; }
                }
                const progress = Math.max(0, Math.min(1, (time - start[0]) / Math.max(0.0001, end[0] - start[0])));
                const eased = progress * progress * (3 - 2 * progress);
                frames[time.toFixed(4)] = start[1].map((value, axis) => rounded(value + (end[1][axis] - value) * eased));
            }
            tracks[owner][channel] = frames;
        }
    }
    animations[name] = { loop, animation_length: length, bones: tracks };
}
const rotation = stops => ({ rotation: stops });
const wave = (length, first, middle, last = first) => [[0, first], [length / 2, middle], [length, last]];
const idle = {
    body: rotation(wave(4, [0, 0, -0.8], [0, 0, 0.8])),
    head: rotation(wave(4, [-2, -3, 0], [0, 3, 0])),
    arm_l: rotation(wave(4, [-10, 0, 22], [-12, 0, 24])),
    forearm_l: rotation(wave(4, [-14, 0, -8], [-16, 0, -8])),
    arm_r: rotation(wave(4, [-6, 0, -32], [-8, 0, -34])),
    forearm_r: rotation(wave(4, [-6, 0, 20], [-8, 0, 22])),
    scepter: rotation(wave(4, [12, 0, 12], [16, 0, 12])),
    cape: rotation(wave(4, [2, 0, 0], [-2, 0, 0])),
};
for (const side of ['l', 'r']) {
    idle['curl_' + side] = rotation(wave(4, [0, 0, -1], [0, 0, 1]));
    idle['cape_' + side] = rotation([[0, [3, 0, 0]], [1, [-1, 0, 1]], [3, [5, 0, -1]], [4, [3, 0, 0]]]);
    idle['cape_tip_' + side] = rotation([[0, [1, 0, 0]], [1.5, [-3, 0, 1]], [3.5, [3, 0, -1]], [4, [1, 0, 0]]]);
}
animation('animation.king.idle', 4, true, idle);
animation('animation.king.walk', 1.2, true, {
    body: { position: [[0, [0, 0, 0]], [0.3, [0, 0.45, 0]], [0.6, [0, 0, 0]], [0.9, [0, 0.45, 0]], [1.2, [0, 0, 0]]] },
    head: rotation(wave(1.2, [0, -2, 0], [0, 2, 0])),
    leg_l: rotation(wave(1.2, [18, 0, 0], [-18, 0, 0])), leg_r: rotation(wave(1.2, [-18, 0, 0], [18, 0, 0])),
    shin_l: rotation(wave(1.2, [-5, 0, 0], [-24, 0, 0])), shin_r: rotation(wave(1.2, [-24, 0, 0], [-5, 0, 0])),
    arm_l: rotation(wave(1.2, [-4, 0, 24], [-18, 0, 24])),
    forearm_l: rotation(wave(1.2, [-14, 0, -8], [-10, 0, -8])),
    arm_r: rotation(wave(1.2, [-6, 0, -32], [-10, 0, -34])), forearm_r: rotation(wave(1.2, [-6, 0, 20], [-8, 0, 22])),
    scepter: rotation(wave(1.2, [12, 0, 12], [18, 0, 12])),
    skirt: rotation(wave(1.2, [0, 0, -1], [0, 0, 1])), skirt_lower: rotation(wave(1.2, [1, 0, 1], [-1, 0, -1])),
    cape: rotation(wave(1.2, [-4, 0, -1], [-7, 0, 1])),
    cape_l: rotation(wave(1.2, [-5, 0, 0], [-1, 0, 0])), cape_r: rotation(wave(1.2, [-1, 0, 0], [-5, 0, 0])),
});
animation('long_cast', 1.6, true, {
    body: rotation(wave(1.6, [-2, -3, 0], [-3, 3, 0])), head: rotation(wave(1.6, [-6, -4, 0], [-3, 4, 0])),
    arm_l: rotation(wave(1.6, [-48, 4, 26], [-56, 6, 28])),
    forearm_l: rotation(wave(1.6, [-26, 0, -8], [-30, 0, -10])), hand_l: rotation(wave(1.6, [-6, -12, 0], [-10, -16, 4])),
    arm_r: rotation(wave(1.6, [-22, 0, -30], [-28, 0, -32])), forearm_r: rotation(wave(1.6, [-14, 0, 20], [-17, 0, 22])),
    scepter: rotation(wave(1.6, [36, 0, 10], [45, 0, 10])),
    cape: rotation(wave(1.6, [-7, 0, 0], [-11, 0, 0])),
    cape_l: rotation(wave(1.6, [-7, 0, -2], [-3, 0, -3])), cape_r: rotation(wave(1.6, [-3, 0, 3], [-7, 0, 2])),
    cape_tip_l: rotation(wave(1.6, [-5, 0, 0], [-9, 0, 0])), cape_tip_r: rotation(wave(1.6, [-9, 0, 0], [-5, 0, 0])),
});
const raiseTracks = {};
for (const [owner, tracks] of Object.entries(animations.long_cast.bones)) {
    raiseTracks[owner] = {};
    for (const [channel, frames] of Object.entries(tracks)) {
        const resting = animations['animation.king.idle'].bones[owner]?.[channel]?.['0.0000'] || [0, 0, 0];
        const held = frames['0.0000'];
        const opening = resting.map((value, axis) => rounded(value + (held[axis] - value) * 0.2));
        if (channel === 'rotation' && owner === 'arm_l') opening[2] = 30;
        if (channel === 'rotation' && owner === 'arm_r') opening[2] = -34;
        raiseTracks[owner][channel] = [[0, resting], [0.24, opening], [0.8, held], [1.6, held]];
    }
}
animation('animation.king.scepter_raise', 1.6, false, raiseTracks);
animation('animation.king.scepter_slam', 0.7, false, {
    body: rotation([[0, [0, 0, 0]], [0.23, [-5, -8, 0]], [0.38, [10, 5, 0]], [0.7, [0, 0, 0]]]),
    arm_r: rotation([[0, [-6, 0, -32]], [0.23, [-48, 0, -36]], [0.38, [-18, 0, -30]], [0.7, [-6, 0, -32]]]),
    forearm_r: rotation([[0, [-6, 0, 20]], [0.23, [-25, 0, 24]], [0.38, [-8, 0, 22]], [0.7, [-6, 0, 20]]]),
    scepter: rotation([[0, [12, 0, 12]], [0.23, [73, 0, 12]], [0.38, [26, 0, 8]], [0.7, [12, 0, 12]]]),
    arm_l: rotation([[0, [-10, 0, 22]], [0.23, [-24, 0, 30]], [0.38, [-14, 0, 26]], [0.7, [-10, 0, 22]]]),
    forearm_l: rotation([[0, [-14, 0, -8]], [0.7, [-14, 0, -8]]]),
    cape: rotation([[0, [0, 0, 0]], [0.23, [-9, 0, 0]], [0.5, [5, 0, 0]], [0.7, [0, 0, 0]]]),
});
animation('animation.king.hurt', 0.4, false, {
    body: rotation([[0, [0, 0, 0]], [0.1, [-7, 0, -4]], [0.4, [0, 0, 0]]]),
    head: rotation([[0, [0, 0, 0]], [0.1, [-9, 0, 3]], [0.4, [0, 0, 0]]]),
    arm_l: rotation(wave(0.4, [-10, 0, 22], [-6, 0, 28])), forearm_l: rotation(wave(0.4, [-14, 0, -8], [-18, 0, -8])),
    arm_r: rotation(wave(0.4, [-6, 0, -32], [-2, 0, -38])), forearm_r: rotation(wave(0.4, [-6, 0, 20], [-10, 0, 26])),
    scepter: rotation(wave(0.4, [12, 0, 12], [12, 0, 12])),
    cape: rotation(wave(0.4, [0, 0, 0], [-6, 0, 0])),
});
animation('animation.king.death', 1.8, 'hold_on_last_frame', {
    root: { rotation: [[0, [0, 0, 0]], [0.4, [12, 0, 0]], [1.35, [90, 0, 0]], [1.8, [90, 0, 0]]], position: [[0, [0, 0, 0]], [0.4, [0, 0.5, 0]], [1.35, [0, 7.6, 0]], [1.8, [0, 7.6, 0]]] },
    head: rotation([[0, [0, 0, 0]], [0.45, [10, 0, 5]], [1.8, [0, 0, 6]]]),
    arm_l: rotation([[0, [8, 0, -14]], [0.8, [0, 0, -40]], [1.8, [0, 0, -35]]]),
    arm_r: rotation([[0, [0, 0, 27]], [0.8, [0, 0, 38]], [1.8, [0, 0, 35]]]),
    forearm_r: rotation([[0, [8, 0, -25]], [1.8, [0, 0, -30]]]),
    cape: rotation([[0, [0, 0, 0]], [0.8, [-8, 0, 0]], [1.8, [0, 0, 0]]]),
});
const legacy = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../src/main/resources/assets/ftboss/animations/gingerbread_king.animation.json'), 'utf8')).animations;
const castLoop = animations.long_cast;
delete animations.long_cast;
for (const [name, original] of Object.entries(legacy)) {
    if (animations[name]) continue;
    const alias = structuredClone(castLoop);
    if (name === 'blank') alias.bones = {};
    const ratio = original.animation_length / alias.animation_length;
    for (const tracks of Object.values(alias.bones)) for (const channel of Object.keys(tracks)) {
        tracks[channel] = Object.fromEntries(Object.entries(tracks[channel]).map(([time, values]) => [(Number(time) * ratio).toFixed(4), values]));
    }
    alias.animation_length = original.animation_length;
    alias.loop = original.loop;
    animations[name] = alias;
}
for (const [name, entry] of Object.entries(animations)) {
    if (name === 'animation.king.death') continue;
    for (const [owner, side] of [['arm_l', -1], ['arm_r', 1]]) {
        const tracks = entry.bones[owner];
        if (!tracks?.rotation) continue;
        tracks.position = Object.fromEntries(Object.entries(tracks.rotation).map(([stamp, angles]) => {
            const lift = Math.max(0, Math.min(1, (-angles[0] - 12) / 32));
            return [stamp, [rounded(side * (0.65 + lift * 0.8)), 0, rounded(-0.1 - lift * 0.8)]];
        }));
    }
}

const geometry = { format_version: '1.12.0', 'minecraft:geometry': [{ description: { identifier: 'geometry.sugar_queen_v3', texture_width: atlasSize, texture_height: atlasSize, visible_bounds_width: 5, visible_bounds_height: 5, visible_bounds_offset: [0, 1.8, 0] }, bones }] };
const { listCubes, cubeCorners } = require('./cube_space.cjs');
const model = { bones, byName: new Map(bones.map(entry => [entry.name, entry])) };
const walkBones = animations['animation.king.walk'].bones;
for (const suffix of ['l', 'r']) {
    const legCubes = listCubes(model).filter(entry => ['leg_', 'shin_', 'foot_'].some(prefix => entry.bone === prefix + suffix));
    walkBones['leg_' + suffix].position = {};
    for (const stamp of Object.keys(walkBones['leg_' + suffix].rotation)) {
        const pose = Object.fromEntries(Object.entries(walkBones).map(([owner, tracks]) => [owner, Object.fromEntries(Object.entries(tracks).filter(([channel]) => channel !== 'position').map(([channel, frames]) => [channel, frames[stamp]]))]));
        const floor = Math.min(...legCubes.flatMap(entry => cubeCorners(model, entry, pose).map(point => point[1])));
        walkBones['leg_' + suffix].position[stamp] = [0, rounded(Math.max(0, -floor) + 0.015), 0];
    }
}
const deathBones = animations['animation.king.death'].bones;
for (const stamp of Object.keys(deathBones.root.position)) {
    const pose = Object.fromEntries(Object.entries(deathBones).map(([owner, tracks]) => [owner, Object.fromEntries(Object.entries(tracks).map(([channel, track]) => [channel, track[stamp]]))]));
    const floor = Math.min(...listCubes(model).flatMap(entry => cubeCorners(model, entry, pose).map(point => point[1])));
    deathBones.root.position[stamp][1] = rounded(deathBones.root.position[stamp][1] + Math.max(0, 0.015 - floor));
}
fs.mkdirSync(output, { recursive: true });
const write = (name, data) => fs.writeFileSync(path.join(output, name), JSON.stringify(data, null, 2) + '\n');
canvas.save(path.join(output, 'sugar_queen_v3.png'));
write('sugar_queen_v3.geo.json', geometry);
write('sugar_queen_v3.animation.json', { format_version: '1.8.0', animations });
const uuid = name => {
    const digest = crypto.createHash('sha256').update('sugar-queen-v3:' + name).digest('hex');
    return `${digest.slice(0, 8)}-${digest.slice(8, 12)}-4${digest.slice(13, 16)}-a${digest.slice(17, 20)}-${digest.slice(20, 32)}`;
};
const groups = Object.fromEntries(bones.map(entry => [entry.name, { name: entry.name, uuid: uuid(entry.name), origin: entry.pivot, rotation: [0, 0, 0], children: [], export: true, isOpen: false }]));
const elements = parts.map((part, index) => {
    const result = part.result, identifier = uuid('cube_' + index);
    groups[part.owner].children.push(identifier);
    return { name: part.name, uuid: identifier, type: 'cube', from: result.origin, to: result.origin.map((value, axis) => rounded(value + result.size[axis])), origin: result.pivot || result.origin.map((value, axis) => rounded(value + result.size[axis] / 2)), rotation: result.rotation || [0, 0, 0], box_uv: false, autouv: 0, faces: Object.fromEntries(Object.entries(result.uv).map(([face, area]) => [face, { uv: [area.uv[0], area.uv[1], area.uv[0] + area.uv_size[0], area.uv[1] + area.uv_size[1]], texture: 0 }])) };
});
const outliner = [];
for (const entry of bones) {
    if (entry.parent) groups[entry.parent].children.push(groups[entry.name]);
    else outliner.push(groups[entry.name]);
}
write('sugar_queen_v3.bbmodel', {
    meta: { format_version: '4.10', model_format: 'geckolib_model', box_uv: false }, name: 'Sugar Queen - Patisserie Sovereign', model_identifier: 'sugar_queen_v3',
    resolution: { width: atlasSize, height: atlasSize }, elements, outliner,
    textures: [{ uuid: uuid('texture'), id: '0', name: 'sugar_queen_v3.png', namespace: 'ftboss', folder: 'entity', width: atlasSize, height: atlasSize, uv_width: atlasSize, uv_height: atlasSize, source: 'data:image/png;base64,' + fs.readFileSync(path.join(output, 'sugar_queen_v3.png')).toString('base64') }],
    animations: Object.entries(animations).map(([name, entry]) => ({ uuid: uuid(name), name, loop: entry.loop === 'hold_on_last_frame' ? 'hold' : entry.loop ? 'loop' : 'once', length: entry.animation_length, snapping: 24, animators: Object.fromEntries(Object.entries(entry.bones).map(([owner, tracks]) => [groups[owner].uuid, { name: owner, type: 'bone', keyframes: Object.entries(tracks).flatMap(([channel, frames]) => Object.entries(frames).map(([time, values]) => ({ uuid: uuid(name + owner + channel + time), channel, time: Number(time), interpolation: 'linear', data_points: [{ x: values[0], y: values[1], z: values[2] }] }))) }])) })),
});
write('manifest.json', { generator: 'tools/model3d/gen_sugar_queen.cjs', bones: bones.length, cubes: parts.length, atlas: [atlasSize, atlasSize], atlasRowsUsed: cursorY + rowHeight, animationNames: Object.keys(animations), collisionBox: [1.2, 2.7], textureDensity: '1.5 texels/model-unit; ceil to whole pixels', parts: parts.map(entry => ({ name: entry.name, bone: entry.owner, material: entry.material })) });
console.log(JSON.stringify({ output, bones: bones.length, cubes: parts.length, atlasRowsUsed: cursorY + rowHeight, animations: Object.keys(animations).length }));
