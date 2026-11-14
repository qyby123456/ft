const fs = require('fs');
const path = require('path');
const { Canvas } = require(path.join(process.env.FT_MODEL_TOOLS || __dirname, 'png.cjs'));
const output = process.argv[2] || 'out_apple_boss';
const textureSize = 256;
const parts = [];
const islands = [];
const rig = {
    root: [null, [0, 0, 0]], body: ['root', [0, 8.5, 0]], upper: ['body', [0, 18, 0]],
    leg_fl_up: ['body', [-4.8, 8.2, -4.8]], leg_fl_lo: ['leg_fl_up', [-4.8, 4.2, -4.8]],
    leg_fr_up: ['body', [4.8, 8.2, -4.8]], leg_fr_lo: ['leg_fr_up', [4.8, 4.2, -4.8]],
    leg_bl_up: ['body', [-4.8, 8.2, 4.8]], leg_bl_lo: ['leg_bl_up', [-4.8, 4.2, 4.8]],
    leg_br_up: ['body', [4.8, 8.2, 4.8]], leg_br_lo: ['leg_br_up', [4.8, 4.2, 4.8]],
    lid: ['body', [0, 8.6, -9.4]],
    worm1: ['body', [0, 11.5, -9.8]], worm2: ['worm1', [0, 14.7, -9.8]], worm3: ['worm2', [0, 17.7, -9.9]],
    stem: ['upper', [0, 26.2, 2.2]], leaf: ['stem', [0, 31, 2]], leaf2: ['leaf', [5, 30.4, 2.6]],
    cape1: ['upper', [0, 25, 8.6]], cape2: ['cape1', [0, 19.8, 9]], cape3: ['cape2', [0, 14.2, 9.4]],
    brow_l: ['upper', [-4, 25.9, -10.9]], brow_r: ['upper', [4, 25.9, -10.9]],
    mouth: ['upper', [0, 19, -10.6]], glasses: ['upper', [0, 22, -11]], hat: ['upper', [0, 26.4, 0]],
    belt: ['body', [0, 15.8, 0]], buckle: ['belt', [0, 15.8, -11.5]],
    arm_l: ['upper', [-13, 22.5, 0]], fore_l: ['arm_l', [-13.8, 17.6, 0]], hand_l: ['fore_l', [-14.4, 11.5, 0]],
    arm_r: ['upper', [13, 22.5, 0]], fore_r: ['arm_r', [13.8, 17.6, 0]], hand_r: ['fore_r', [14.4, 11.5, 0]],
    orbit1: ['upper', [0, 34, 0]], orbit2: ['upper', [0, 34, 0]], orbit3: ['upper', [0, 34, 0]]
};
function cube(bone, label, origin, size, material, rotation) {
    const part = { bone, label, origin, size, material, uv: {} };
    if (rotation) { part.rotation = rotation; part.pivot = origin.map((value, axis) => value + size[axis] / 2); }
    parts.push(part);
}
function pair(bones, label, origin, size, material, rotation) {
    cube(bones[0], label + '_l', origin, size, material, rotation);
    cube(bones[1], label + '_r', [-origin[0] - size[0], origin[1], origin[2]], size, material,
        rotation && [rotation[0], -rotation[1], -rotation[2]]);
}
function fruitLayer(bone, label, bottom, height, width, depth, corner, material = 'apple') {
    cube(bone, label + '_heart', [-width / 2 + corner, bottom, -depth / 2], [width - corner * 2, height, depth], material);
    pair([bone, bone], label + '_cheek', [-width / 2, bottom + 0.2, -depth / 2 + corner],
        [corner + 0.6, height - 0.4, depth - corner * 2], material);
}
fruitLayer('body', 'calyx', 7.3, 3.4, 14.8, 11.8, 2.8, 'appleShade');
fruitLayer('body', 'lower', 9.4, 5.1, 21, 16, 3);
fruitLayer('body', 'waist', 13.8, 5.1, 24.4, 18.4, 3);
fruitLayer('upper', 'shoulder', 18.3, 5.7, 25, 19, 3.2);
fruitLayer('upper', 'crown', 23.4, 2.9, 21.8, 16.4, 3.4);
pair(['upper', 'upper'], 'fruit_lobe', [-8.1, 25.3, -6.2], [7.5, 2.1, 11.8], 'apple');
cube('upper', 'stem_dimple', [-2.4, 25.8, -2], [4.8, 0.7, 5.6], 'appleShade');
for (const [leg, centerX, centerZ] of [['fl', -4.8, -4.8], ['fr', 4.8, -4.8], ['bl', -4.8, 4.8], ['br', 4.8, 4.8]]) {
    const upperLeg = 'leg_' + leg + '_up', lowerLeg = 'leg_' + leg + '_lo';
    cube(upperLeg, leg + '_thigh', [centerX - 1.6, 3.7, centerZ - 1.6], [3.2, 5.1, 3.2], 'bark');
    cube(lowerLeg, leg + '_knee', [centerX - 1.85, 3.1, centerZ - 1.85], [3.7, 2.1, 3.7], 'barkDark');
    cube(lowerLeg, leg + '_ankle', [centerX - 1.35, 0.8, centerZ - 1.35], [2.7, 3.3, 2.7], 'bark');
    cube(lowerLeg, leg + '_pad', [centerX - 2.2, 0, centerZ - 2.7], [4.4, 1.4, 5.4], 'barkDark');
    for (const offset of [-1.35, 1.05]) cube(lowerLeg, leg + '_toe_' + offset, [centerX + offset - 0.65, 0.2, centerZ - 3.3], [1.3, 1.1, 2.4], 'bark');
}
pair(['arm_l', 'arm_r'], 'socket', [-14.6, 20.8, -2.4], [4.4, 4, 4.8], 'barkDark');
pair(['arm_l', 'arm_r'], 'upper_branch', [-15.1, 16.6, -1.8], [3, 6.7, 3.6], 'bark');
pair(['fore_l', 'fore_r'], 'elbow', [-15.55, 16.1, -2.05], [3.5, 2.8, 4.1], 'barkDark');
pair(['fore_l', 'fore_r'], 'fore_branch', [-15.25, 10.7, -1.55], [2.9, 6.9, 3.1], 'bark');
pair(['fore_l', 'fore_r'], 'cuff', [-15.5, 11.6, -1.8], [3.4, 1.7, 3.6], 'leafDark');
pair(['hand_l', 'hand_r'], 'palm', [-16, 9.2, -2.15], [3.6, 3.4, 3.9], 'barkDark');
pair(['hand_l', 'hand_r'], 'thumb', [-12.9, 9.4, -2.8], [1.4, 2.1, 2.4], 'bark', [0, 0, -16]);
pair(['hand_l', 'hand_r'], 'blade_base', [-17.1, 7.9, -2.9], [3.8, 4.5, 1.3], 'blade', [0, 0, -12]);
pair(['hand_l', 'hand_r'], 'blade_mid', [-17.8, 5.1, -2.65], [3.3, 4.1, 1.1], 'blade', [0, 0, -12]);
pair(['hand_l', 'hand_r'], 'blade_tip', [-18.1, 3.4, -2.4], [1.9, 2.8, 0.8], 'blade', [0, 0, -18]);
pair(['upper', 'upper'], 'epaulette', [-14.5, 23.2, -3.3], [6.5, 1.3, 7.1], 'leaf', [0, 0, -18]);
pair(['upper', 'upper'], 'lapel', [-8.3, 24.6, -6], [4.4, 1.5, 10.5], 'leafDark', [-10, 0, -12]);
for (const [bone, top, width, back] of [['cape1', 25.2, 16.2, 9.3], ['cape2', 20.5, 14.2, 10.2], ['cape3', 15.1, 11.4, 10.9]]) {
    cube(bone, bone + '_cloth', [-width / 2, top - 6.5, back], [width, 6.5, 1.2], 'cape');
    cube(bone, bone + '_spine', [-0.5, top - 6.3, back + 0.95], [1, 6.2, 0.6], 'vein');
    pair([bone, bone], bone + '_side', [-width / 2 - 1.3, top - 5.3, back + 0.3], [2.2, 4.8, 0.8], 'leaf', [0, 0, -13]);
}
cube('cape3', 'cape_tail', [-2.9, 6.4, 11.15], [5.8, 3.3, 0.9], 'cape');
cube('belt', 'ribbon_front', [-9.6, 14.4, -9.95], [19.2, 2.1, 0.8], 'ribbon');
cube('belt', 'ribbon_back', [-9.4, 14.6, 9.2], [18.8, 1.7, 0.7], 'ribbon');
pair(['belt', 'belt'], 'ribbon_side', [-12.5, 14.7, -6.45], [0.6, 1.6, 12.9], 'ribbon');
pair(['belt', 'belt'], 'ribbon_corner', [-12.35, 14.8, -8.25], [4.3, 1.5, 0.7], 'ribbon', [0, 45, 0]);
pair(['belt', 'belt'], 'ribbon_corner_back', [-12.2, 14.85, 7.15], [4.2, 1.4, 0.7], 'ribbon', [0, -45, 0]);
cube('buckle', 'seal_frame', [-2.6, 13.6, -10.95], [5.2, 3.8, 1.2], 'gold');
cube('buckle', 'seal_inset', [-2, 14.1, -11.4], [4, 2.8, 0.6], 'ivory');
cube('buckle', 'seal_apple', [-1.1, 14.45, -11.85], [2.2, 1.8, 0.55], 'apple');
cube('buckle', 'seal_leaf', [0.15, 15.95, -11.95], [1.3, 0.7, 0.5], 'leaf', [0, 0, 18]);
pair(['upper', 'upper'], 'eye_socket', [-7.5, 21.1, -9.8], [5.5, 3.3, 0.65], 'hole');
pair(['brow_l', 'brow_r'], 'brow', [-7.6, 25, -9.2], [5.4, 1.1, 0.9], 'appleShade', [0, 0, -12]);
pair(['glasses', 'glasses'], 'frame', [-8.2, 20.6, -10.9], [6.8, 4.7, 1.2], 'goldDark');
pair(['glasses', 'glasses'], 'lens', [-7.8, 21, -11.2], [6, 3.9, 0.7], 'lens');
cube('glasses', 'bridge', [-1.75, 22.8, -10.95], [3.5, 0.9, 0.8], 'gold');
pair(['glasses', 'glasses'], 'temple', [-9.55, 23, -10.1], [1.9, 0.9, 6.6], 'hat');
cube('mouth', 'smirk', [-3.3, 18.85, -9.95], [6.6, 0.85, 0.7], 'hole');
cube('mouth', 'smirk_corner', [-4.2, 19.25, -9.9], [1.3, 1.15, 0.6], 'hole', [0, 0, -16]);
cube('body', 'wound_rim', [-3.65, 8.5, -8.9], [7.3, 5.7, 0.9], 'rot');
cube('body', 'wound_cavity', [-2.95, 9, -9.25], [5.9, 4.7, 0.6], 'hole');
cube('lid', 'wax_cover', [-3.4, 8.4, -10.1], [6.8, 5.9, 1.6], 'wax');
cube('lid', 'wax_seal', [-1.3, 10, -10.55], [2.6, 2.4, 0.65], 'goldDark');
for (const [bone, base, width, height] of [['worm1', 9.2, 4.4, 4.6], ['worm2', 12.6, 4, 4.2], ['worm3', 15.8, 3.7, 3.8]]) {
    const taper = Number(bone.slice(-1)) - 1;
    cube(bone, bone + '_grub', [-width / 2, base, -8.55 + taper * 0.25], [width, height, 3.5 - taper * 0.5], 'worm');
    cube(bone, bone + '_ring', [-width / 2 - 0.35, base + 1, -8.75 + taper * 0.25], [width + 0.7, 0.7, 3.9 - taper * 0.5], 'wormRing');
}
pair(['worm3', 'worm3'], 'grub_eye', [-1.4, 17.7, -8.55], [0.9, 1, 0.8], 'hole');
cube('worm3', 'grub_mouth', [-0.8, 16.3, -8.45], [1.6, 0.7, 0.7], 'rot');
cube('hat', 'brim', [-7.7, 26.9, -6.3], [15.4, 1.2, 12.6], 'hat');
pair(['hat', 'hat'], 'brim_wing', [-9.4, 27.1, -4.6], [2.3, 0.8, 9.2], 'hat');
cube('hat', 'hat_crown', [-5.8, 27.7, -4.9], [11.6, 8.5, 9.8], 'hat');
cube('hat', 'hat_top', [-6.3, 35.5, -5.4], [12.6, 1.3, 10.8], 'hat');
cube('hat', 'hat_ribbon', [-6.1, 28.5, -5.2], [12.2, 2, 10.4], 'ribbon');
cube('hat', 'hat_pin', [-4.3, 28.8, -5.65], [1.8, 1.5, 0.65], 'gold');
cube('stem', 'stem_base', [3.55, 25.8, 1.8], [2.5, 4.8, 2.5], 'bark');
cube('stem', 'stem_tip', [3.65, 29.7, 2.15], [1.8, 3.3, 1.8], 'barkDark');
cube('leaf', 'leaf_root', [3.2, 30.6, 1.7], [5.8, 0.9, 4.8], 'leaf', [12, 0, -18]);
cube('leaf2', 'leaf_mid', [6.8, 30.2, 2.4], [5.5, 0.8, 4.1], 'leaf', [12, 0, -24]);
cube('leaf2', 'leaf_tip', [10.8, 28.7, 3.2], [2.8, 0.6, 2.3], 'leaf', [12, 0, -30]);
for (const [index, degrees] of [0, 120, 240].entries()) {
    const angle = degrees * Math.PI / 180, centerX = Math.cos(angle) * 15, centerZ = Math.sin(angle) * 15;
    const bone = 'orbit' + (index + 1);
    cube(bone, bone + '_fruit', [centerX - 2.3, 32.1, centerZ - 1.8], [4.6, 4.1, 3.6], 'apple');
    cube(bone, bone + '_cheek', [centerX - 1.8, 32.5, centerZ - 2.3], [3.6, 3.3, 4.6], 'apple');
    cube(bone, bone + '_stem', [centerX - 0.45, 35.8, centerZ - 0.45], [0.9, 2, 0.9], 'bark');
    cube(bone, bone + '_leaf', [centerX + 0.1, 36.7, centerZ - 0.5], [2.3, 0.55, 1.6], 'leaf', [0, 0, 18]);
}
const palette = {
    apple: ['862e42', 'b63d4d', 'd7595a', 'ef8570'], appleShade: ['542937', '772d3e', 'a33b48', 'c25752'],
    bark: ['49363b', '705043', '9c7151', 'c09965'], barkDark: ['352e35', '533d38', '7a5441', 'a4774e'],
    leaf: ['213e3c', '2e6555', '458775', '84b284'], leafDark: ['1c3436', '2a514a', '3a7460', '67956d'],
    blade: ['24504a', '387e68', '6da389', 'bfcea0'], cape: ['1e383a', '2b5b50', '417c63', '799975'],
    vein: ['335b4d', '688260', '98a773', 'c5c394'], ribbon: ['826339', 'b59650', 'ddc276', 'f1deaa'],
    gold: ['71503b', 'b18143', 'd9af63', 'ffe0a2'], goldDark: ['433835', '75543c', 'b0884b', 'd8b970'],
    hat: ['1b202b', '29303b', '3b4551', '56606b'], lens: ['172633', '253d4d', '456371', 'a6cfce'],
    ivory: ['947c62', 'baa383', 'dbc9a4', 'f5e7c3'], wax: ['9f795b', 'c49c70', 'e3c58b', 'f7dda6'],
    hole: ['201f2b', '272331', '30283a', '433143'], rot: ['392b38', '5b393e', '815045', 'a8734f'],
    worm: ['6a7957', 'a8b774', 'd2d796', 'eff0bd'], wormRing: ['697257', '90985f', 'b7bd7a', 'd3d793']
};
const rottenPalette = {
    apple: ['46313a', '70503f', '96714b', 'bea36a'], appleShade: ['302d34', '4d3d38', '725244', '977652'],
    leaf: ['303c38', '4b5943', '728060', 'a6aa70'], leafDark: ['293632', '414b39', '666748', '8c9260'],
    cape: ['293b36', '455541', '69734f', '8e9360'], blade: ['394940', '607052', '8f9868', 'bec08e']
};
const faceSizes = size => ({ north: [size[0], size[1]], south: [size[0], size[1]], east: [size[2], size[1]], west: [size[2], size[1]], up: [size[0], size[2]], down: [size[0], size[2]] });
for (const part of parts) for (const [face, dimensions] of Object.entries(faceSizes(part.size))) {
    islands.push({ part, face, width: Math.max(1, Math.ceil(dimensions[0])), height: Math.max(1, Math.ceil(dimensions[1])), dimensions });
}
const shelves = [];
let usedHeight = 1;
for (const island of islands.slice().sort((first, second) => second.height - first.height || second.width - first.width)) {
    let shelf = shelves.find(candidate => candidate.height >= island.height + 2 && candidate.used + island.width + 2 < textureSize);
    if (!shelf) { shelf = { top: usedHeight, used: 1, height: island.height + 2 }; shelves.push(shelf); usedHeight += shelf.height; }
    if (usedHeight >= textureSize) throw new Error('Atlas overflow: ' + usedHeight);
    island.left = shelf.used + 1; island.top = shelf.top + 1; shelf.used += island.width + 2;
    island.part.uv[island.face] = { uv: [island.left, island.top], uv_size: [island.width, island.height] };
}
function drawTexture(secondPhase) {
    const canvas = new Canvas(textureSize, textureSize);
    for (const island of islands) {
        const { part, face, width, height, left, top } = island;
        const colors = ((secondPhase && rottenPalette[part.material]) || palette[part.material]).map(hex => [0, 2, 4].map(offset => parseInt(hex.slice(offset, offset + 2), 16)));
        for (let pixelY = 0; pixelY < height; pixelY++) for (let pixelX = 0; pixelX < width; pixelX++) {
            const horizontal = pixelX / Math.max(1, width - 1), vertical = pixelY / Math.max(1, height - 1);
            const edge = pixelX === 0 || pixelY === 0 || pixelX === width - 1 || pixelY === height - 1;
            let tone = vertical < 0.23 ? 2 : 1;
            if (face === 'up') tone = 2;
            if (face === 'down') tone = 0;
            if (part.material.startsWith('apple')) {
                const wholeFruit = part.bone === 'body' || part.bone === 'upper';
                const surfaceX = wholeFruit ? (part.origin[0] + horizontal * part.size[0] + 12.5) / 25 : horizontal;
                const surfaceY = wholeFruit ? (27 - part.origin[1] - (1 - vertical) * part.size[1]) / 20 : vertical;
                tone = surfaceX < 0.46 && surfaceY < 0.58 ? 2 : 1;
                if (surfaceX > 0.88 || surfaceY > 0.9) tone = 0;
                if (surfaceX > 0.16 && surfaceX < 0.29 && surfaceY > 0.15 && surfaceY < 0.35) tone = 3;
                if (width > 8 && height > 4 && (pixelX * 7 + pixelY * 13 + Math.floor(part.origin[1]) * 3) % 71 === 0) tone = Math.min(3, tone + 1);
                if (secondPhase && width > 4 && height > 3 && (Math.floor(pixelX / 3) * 11 + Math.floor(pixelY / 2) * 7 + Math.floor(part.origin[0])) % 9 === 0) tone = 0;
            } else if (/leaf|cape|blade/.test(part.material)) {
                tone = horizontal < 0.5 ? 2 : 1;
                if (edge) tone = 0;
                if (pixelX === Math.floor(width / 2) || (height > 4 && (pixelY + Math.abs(pixelX - Math.floor(width / 2))) % 5 === 0)) tone = 3;
            } else if (part.material.startsWith('bark')) {
                tone = Math.floor(horizontal * 4) === 1 ? 2 : 1;
                if ((pixelX + Math.floor(pixelY / 4)) % 5 === 0) tone = 0;
                if (face === 'up' || face === 'down') tone = edge ? 0 : 2;
            } else if (part.material === 'lens') {
                tone = vertical > 0.65 ? 0 : 1;
                if (pixelY === 1 && pixelX > 0 && pixelX < width - 2) tone = 2;
                if (pixelY === 1 && pixelX === 1) tone = 3;
            } else if (/gold|ribbon|ivory|wax/.test(part.material)) {
                tone = edge ? 1 : 2;
                if (pixelY === 0) tone = 3;
                if (pixelY === height - 1) tone = 0;
                if (part.material === 'wax' && face === 'north' && pixelX === Math.floor(width / 2)) tone = 1;
            } else if (part.material === 'hat') {
                tone = horizontal < 0.25 ? 2 : 1;
                if (edge) tone = 0;
                if (pixelY === 0 && width > 4) tone = 2;
            }
            canvas.set(left + pixelX, top + pixelY, ...colors[tone], 1);
        }
        for (let padY = -1; padY <= height; padY++) for (let padX = -1; padX <= width; padX++) {
            if (padX >= 0 && padX < width && padY >= 0 && padY < height) continue;
            const offset = ((top + Math.max(0, Math.min(height - 1, padY))) * textureSize + left + Math.max(0, Math.min(width - 1, padX))) * 4;
            canvas.set(left + padX, top + padY, canvas.d[offset], canvas.d[offset + 1], canvas.d[offset + 2], 1);
        }
    }
    return canvas;
}
const bones = Object.entries(rig).map(([name, [parent, pivot]]) => {
    const bone = { name, pivot };
    if (parent) bone.parent = parent;
    const cubes = parts.filter(part => part.bone === name).map(part => {
        const result = { origin: part.origin, size: part.size, uv: part.uv };
        if (part.rotation) { result.rotation = part.rotation; result.pivot = part.pivot; }
        return result;
    });
    if (cubes.length) bone.cubes = cubes;
    return bone;
});
const geometry = { format_version: '1.12.0', 'minecraft:geometry': [{
    description: { identifier: 'geometry.apple_boss', texture_width: textureSize, texture_height: textureSize,
        visible_bounds_width: 5, visible_bounds_height: 5, visible_bounds_offset: [0, 1.4, 0] }, bones
}] };
fs.mkdirSync(output, { recursive: true });
if (process.argv[3] !== 'p2') {
    fs.writeFileSync(path.join(output, 'apple_boss.geo.json'), JSON.stringify(geometry, null, 2) + '\n');
    drawTexture(false).save(path.join(output, 'apple_boss.png'));
    fs.writeFileSync(path.join(output, 'apple_boss_manifest.json'), JSON.stringify({
        name: '苹果爵士 · 蜜脆庄园', textureSize, cubes: parts.length, bones: bones.length, atlasHeight: usedHeight,
        faces: islands.length, parts: parts.map(({ uv, ...part }) => part),
        islands: islands.map(({ part, ...island }) => ({ ...island, part: part.label, material: part.material }))
    }, null, 2) + '\n');
}
drawTexture(true).save(path.join(output, 'apple_boss_p2.png'));
console.log(JSON.stringify({ cubes: parts.length, bones: bones.length, textureSize, atlasHeight: usedHeight, output }));
