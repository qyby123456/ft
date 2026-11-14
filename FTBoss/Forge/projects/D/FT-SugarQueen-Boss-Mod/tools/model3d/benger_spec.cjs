'use strict';

const bones = [
    ['root', null, [0, 0, 0]],
    ['body', 'root', [0, 28, -8]],
    ['spine', 'body', [0, 28, 2]],
    ['waist', 'spine', [0, 28, 10]],
    ['neck', 'body', [0, 34, -12]],
    ['head', 'neck', [0, 43, -16]],
    ['jaw', 'head', [0, 37.5, -24.5]],
    ['ear_l', 'head', [-5.5, 48.5, -20]],
    ['ear_r', 'head', [5.5, 48.5, -20]],
    ['stick', 'head', [0, 35.8, -31]],
    ['tail1', 'waist', [0, 32, 15]],
    ['tail2', 'tail1', [0, 26, 21]],
    ['tail3', 'tail2', [0, 21, 27]],
    ['tail4', 'tail3', [0, 16, 33]],
    ['tail5', 'tail4', [0, 12, 38]]
].map(([name, parent, pivot]) => ({ name, ...(parent ? { parent } : {}), pivot }));

for (const [suffix, lateral, depth, parent] of [
    ['fl', -7, -9, 'body'], ['fr', 7, -9, 'body'],
    ['bl', -7, 12, 'waist'], ['br', 7, 12, 'waist']
]) {
    bones.push({ name: `leg_${suffix}`, parent, pivot: [lateral, 24, depth] });
    bones.push({ name: `shin_${suffix}`, parent: `leg_${suffix}`, pivot: [lateral, 14, depth] });
    bones.push({ name: `paw_${suffix}`, parent: `shin_${suffix}`, pivot: [lateral, 6.5, depth] });
}

const parts = [];
function cube(name, bone, origin, size, material, rotation) {
    parts.push({ name, bone, origin, size, material, ...(rotation ? { rotation } : {}) });
}
function pair(name, bone, origin, size, material, rotation) {
    cube(`${name}_l`, bone, origin, size, material, rotation);
    cube(`${name}_r`, bone, [-origin[0] - size[0], origin[1], origin[2]], size,
        material, rotation ? [rotation[0], -rotation[1], -rotation[2]] : undefined);
}

cube('chest_core', 'body', [-8.7, 22.3, -13.7], [17.4, 16.6, 15.2], 'black');
cube('withers', 'body', [-7.6, 37.2, -12.5], [15.2, 3.9, 12.8], 'black');
pair('shoulder_bevel', 'body', [-10.1, 24.8, -12.6], [4.2, 12.8, 13.4], 'black', [0, 0, -10]);
cube('belly', 'body', [-7.7, 20.8, -10.6], [15.4, 5.1, 17.2], 'belly');
cube('middle_back', 'spine', [-9.1, 22.6, -0.4], [18.2, 16.1, 10.5], 'black');
cube('saddle', 'spine', [-7.7, 36.8, 0.1], [15.4, 3.1, 10.1], 'black');
cube('haunch', 'waist', [-8.4, 21.6, 8.7], [16.8, 16.1, 9.3], 'black');
cube('rump_bevel', 'waist', [-7.1, 34.1, 10.2], [14.2, 4.2, 8.5], 'black', [-7, 0, 0]);
pair('haunch_feather', 'waist', [-10, 19.9, 10.4], [3.8, 11.7, 7.8], 'black_soft', [-9, 0, -9]);

cube('ruff_back', 'neck', [-7.8, 31.6, -17.1], [15.6, 11.4, 9.1], 'black');
cube('throat', 'neck', [-6.5, 31.1, -20.9], [13, 10.1, 7.6], 'white');
cube('bib_upper', 'body', [-7.6, 28.2, -19.6], [15.2, 8.1, 7.4], 'white');
cube('bib_middle', 'body', [-6.6, 23, -20.1], [13.2, 7.1, 6.7], 'white');
cube('bib_lower', 'body', [-5.2, 19.1, -19.7], [10.4, 5.8, 5.9], 'white');
cube('bib_tip', 'body', [-3, 16.7, -18.7], [6, 5, 4.6], 'white');
pair('bib_feather_high', 'neck', [-9.7, 30.8, -19.6], [4.3, 9.3, 6.1], 'white', [8, -8, -19]);
pair('bib_feather_low', 'body', [-7.9, 21.6, -18.8], [3.5, 8.7, 4.9], 'white', [10, -5, -19]);
for (let layer = 0; layer < 3; layer++) {
    pair(`mane_${layer}`, 'neck', [-10.9 - layer * 0.2, 29.5 + layer * 3.1, -15.8],
        [5.1, 7.8, 7.6], layer === 1 ? 'black_soft' : 'black', [13, -12, -21 + layer * 3]);
}
cube('collar', 'neck', [-6.9, 34.2, -21.1], [13.8, 1.7, 8.3], 'red');
cube('collar_buckle', 'neck', [-1.4, 33.9, -21.4], [2.8, 2, 0.65], 'brass');
cube('pendant', 'neck', [-1.2, 31.7, -21.6], [2.4, 2.6, 0.55], 'brass');

cube('skull', 'head', [-8.5, 39.5, -26], [17, 9.8, 11.6], 'face');
cube('crown', 'head', [-7, 47.7, -25.2], [14, 2.8, 10.2], 'face');
cube('occiput', 'head', [-7.6, 39.8, -17.3], [15.2, 8.7, 4.8], 'black');
pair('temple', 'head', [-9.7, 40.3, -23.4], [3.6, 8.3, 8.8], 'black', [0, -10, -8]);
pair('cheek', 'head', [-8.3, 36.9, -24.9], [4.5, 7.1, 7.3], 'black', [8, -9, -17]);
for (let layer = 0; layer < 3; layer++) {
    pair(`cheek_tuft_${layer}`, 'head', [-10.6 - layer * 0.3, 36.6 + layer * 2.6, -22.1 + layer * 0.5],
        [4.1, 5.7, 5.9], layer === 1 ? 'black_soft' : 'black', [14, -13, -27]);
}
cube('bridge', 'head', [-3.5, 40.1, -29.3], [7, 4.7, 5.8], 'muzzle', [-10, 0, 0]);
cube('upper_muzzle', 'head', [-5.5, 36.8, -31.6], [11, 5.7, 7.1], 'muzzle');
pair('muzzle_cheek', 'head', [-6.1, 36.4, -30.9], [3.7, 4.7, 6.2], 'muzzle', [0, -12, -8]);
cube('snout_tip', 'head', [-4, 37.2, -33.6], [8, 4.3, 4.4], 'muzzle');
cube('nose', 'head', [-3.1, 37.8, -35], [6.2, 3.2, 2.1], 'nose');
cube('nose_lower', 'head', [-2.4, 37.2, -34.8], [4.8, 1.8, 1.8], 'nose');
cube('mouth_line', 'head', [-4.7, 35.9, -32.7], [9.4, 0.8, 7.1], 'mouth');
cube('lower_jaw', 'jaw', [-4.3, 34.3, -32], [8.6, 2, 8.3], 'white');
cube('chin', 'jaw', [-3.4, 34.1, -33], [6.8, 1.8, 4.6], 'white');
pair('canine', 'head', [-4.4, 35.2, -29.2], [0.7, 1.5, 1], 'tooth');
for (const side of [-1, 1]) {
    const center = side * 5.65;
    cube(`eye_${side < 0 ? 'l' : 'r'}`, 'head', [center - 2, 42.8, -26.36], [4, 3.5, 0.5], 'eye');
    cube(`eyelid_${side < 0 ? 'l' : 'r'}`, 'head', [center - 2.2, 45.5, -26.5], [4.4, 0.8, 0.8],
        'black', [0, 0, side * 7]);
    const earBone = side < 0 ? 'ear_l' : 'ear_r';
    const earCenter = side * 6.1;
    cube(`${earBone}_outer`, earBone, [earCenter - 3.3, 48.3, -23.2],
        [6.6, 6.6, 3.3], 'black', [0, 0, side * 43]);
    cube(`${earBone}_inner`, earBone, [earCenter - 2.05, 49.25, -23.37],
        [4.1, 4.5, 0.5], 'ear', [0, 0, side * 43]);
    cube(`${earBone}_base`, earBone, [earCenter - 3.6, 46.8, -23.52],
        [7.2, 3.9, 4.2], 'black');
    cube(`${earBone}_root_fur`, earBone, [side < 0 ? -10.1 : 6.9, 46.7, -21.8],
        [3.2, 5.4, 5.4], 'black', [6, side * 8, side * -16]);
}

cube('staff_wood', 'stick', [-13.2, 34.85, -32.9], [26.4, 1.9, 2], 'wood');
pair('staff_band', 'stick', [-10, 34.65, -33.1], [1.35, 2.3, 2.4], 'brass');

const tailSegments = [
    ['tail1', [-4.3, 25, 13], [8.6, 9.6, 10.3], 'black'],
    ['tail1', [-4, 21.1, 19.7], [8, 8, 8.9], 'black'],
    ['tail2', [-3.8, 16.9, 24.2], [7.6, 8.1, 9.1], 'black'],
    ['tail3', [-3.4, 12.4, 29.8], [6.8, 8, 8.5], 'black'],
    ['tail4', [-2.9, 8.8, 34.8], [5.8, 6.6, 8.6], 'white'],
    ['tail5', [-2.2, 6.1, 40.3], [4.4, 5.6, 5.4], 'white']
];
tailSegments.forEach(([bone, origin, size, material], index) => {
    cube(`tail_core_${index}`, bone, origin, size, material);
    pair(`tail_feather_${index}`, bone, [origin[0] - 0.8, origin[1] + 1.1, origin[2] + 0.7],
        [2.3, size[1] - 0.6, size[2] - 1.5], material, [-9, 0, -12]);
});

for (const [suffix, lateral, depth] of [['fl', -7, -9], ['fr', 7, -9], ['bl', -7, 12], ['br', 7, 12]]) {
    const front = suffix.startsWith('f');
    cube(`${suffix}_upper`, `leg_${suffix}`, [lateral - 2.6, 13.6, depth - 3.1], [5.2, 12.2, 6.4], 'black');
    cube(`${suffix}_feather`, `leg_${suffix}`, [lateral - 2.3, 14.9, depth - 0.7], [4.6, 9.6, 5.5], 'black_soft', [-10, 0, 0]);
    cube(`${suffix}_shin`, `shin_${suffix}`, [lateral - 2.15, 5.8, depth - 2.7], [4.3, 9.6, 4.7], 'sock');
    cube(`${suffix}_ankle`, `paw_${suffix}`, [lateral - 2.25, 2.1, depth - 2.8], [4.5, 5.2, 4.9], 'white');
    cube(`${suffix}_paw`, `paw_${suffix}`, [lateral - 2.9, 0.2, depth - 5.9], [5.8, 3.7, 8.2], 'white');
    for (let toe = 0; toe < 3; toe++) {
        cube(`${suffix}_toe_${toe}`, `paw_${suffix}`, [lateral - 2.5 + toe * 1.75, 0, depth - 6.8], [1.5, 2.4, 3.8], 'white');
    }
    if (!front) cube(`${suffix}_hock`, `shin_${suffix}`, [lateral - 2, 9.2, depth + 0.4], [4, 4.7, 3], 'black');
}

module.exports = { bones, parts };
