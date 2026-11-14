const fs = require('node:fs');
const path = require('node:path');
const { Canvas } = require('./png.cjs');
const output = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const palette = [[250, 239, 236], [165, 185, 207], [229, 111, 163], [115, 51, 91], [255, 188, 225], [253, 238, 249], [231, 193, 156], [255, 248, 255]];
const texture = new Canvas(64, 64);
const glow = new Canvas(64, 64);
for (let swatch = 0; swatch < palette.length; swatch++) {
    const originX = swatch % 4 * 16;
    const originY = Math.floor(swatch / 4) * 16;
    for (let pixelY = 0; pixelY < 16; pixelY++) {
        for (let pixelX = 0; pixelX < 16; pixelX++) {
            const shade = pixelX === 0 || pixelY === 15 ? -18 : pixelX === 15 || pixelY === 0 ? 8 : ((pixelX + pixelY * 3) % 7 === 0 ? -5 : 0);
            const color = palette[swatch].map(value => Math.max(0, Math.min(255, value + shade)));
            texture.set(originX + pixelX, originY + pixelY, ...color, 1);
            if (swatch === 4 || swatch === 7) glow.set(originX + pixelX, originY + pixelY, ...color, 1);
        }
    }
}
for (let row = 3; row < 14; row += 3) {
    texture.rect(19, 16 + row, row % 2 ? 9 : 6, 1, 177, 94, 147, 1);
}
const bones = [];
function bone(name, parent, pivot = [0, 0, 0]) {
    const result = { name, pivot, cubes: [] };
    if (parent) result.parent = parent;
    bones.push(result);
    return result;
}
function cube(part, origin, size, swatch, rotation, pivot) {
    const uv = Object.fromEntries(['north', 'south', 'east', 'west', 'up', 'down'].map(face => [face,
        { uv: [swatch % 4 * 16 + 1, Math.floor(swatch / 4) * 16 + 1], uv_size: [14, 14] }]));
    const value = { origin, size, uv };
    if (rotation) { value.rotation = rotation; value.pivot = pivot; }
    part.cubes.push(value);
}
const root = bone('root');
const shaft = bone('shaft', 'root');
cube(shaft, [-0.65, -14, -0.65], [1.3, 25, 1.3], 0);
cube(shaft, [-0.8, -3.5, -0.8], [1.6, 6, 1.6], 3);
for (const height of [-13.8, -4, 2.3, 8.2, 10]) cube(shaft, [-0.92, height, -0.92], [1.84, 0.55, 1.84], 1);
cube(shaft, [-0.32, -13, -0.71], [0.64, 8, 0.16], 2);
cube(shaft, [-0.33, 3, -0.73], [0.66, 4.5, 0.18], 7);
cube(shaft, [-1, -15, -1], [2, 2, 2], 4, [0, 45, 0], [0, -14, 0]);
const collar = bone('collar', 'root', [0, 10, 0]);
for (const sign of [-1, 1]) cube(collar, [sign * 1.4 - 0.5, 9.8, -0.7], [1, 3.2, 1.4], 1, [0, 0, -sign * 28], [0, 10, 0]);
const ring = bone('star_ring', 'root', [0, 16, 0]);
for (let segment = 0; segment < 12; segment++) {
    if (segment === 8 || segment === 9) continue;
    const angle = segment * Math.PI / 6;
    const centerX = Math.cos(angle) * 4.15;
    const centerY = 16 + Math.sin(angle) * 4.15;
    cube(ring, [centerX - 1.08, centerY - 0.32, -0.55], [2.16, 0.64, 1.1], 1,
        [0, 0, segment * 30 + 90], [centerX, centerY, 0]);
}
for (const sign of [-1, 1]) {
    cube(ring, [sign * 4.5 - 0.65, 15.35, -0.45], [1.3, 1.3, 0.9], 2, [0, 0, 45], [sign * 4.5, 16, 0]);
}
const crystal = bone('heart_crystal', 'root', [0, 16, 0]);
cube(crystal, [-1.4, 14.6, -1.4], [2.8, 2.8, 2.8], 4, [0, 45, 45], [0, 16, 0]);
cube(crystal, [-0.35, 13.1, -0.35], [0.7, 5.8, 0.7], 7);
const ribbon = bone('ribbon', 'root', [0, 10.5, 0]);
for (const sign of [-1, 1]) cube(ribbon, [sign * 1.6 - 0.65, 7.1, 0.6], [1.3, 3.5, 0.25], 2, [0, 0, sign * 18], [0, 10.5, 0]);
bone('page_orbit', 'root', [0, 16, 0]);
for (let page = 0; page < 3; page++) {
    const angle = page * Math.PI * 2 / 3;
    const center = [Math.cos(angle) * 6.4, 16 + page * 0.5, Math.sin(angle) * 6.4];
    const leaf = bone('page_' + page, 'page_orbit', center);
    cube(leaf, [center[0] - 1, center[1] - 1.4, center[2] - 0.13], [2, 2.8, 0.26], 5, [0, -page * 120, -12 + page * 12], center);
}
const geometry = { format_version: '1.12.0', 'minecraft:geometry': [{ description: {
    identifier: 'geometry.qizai_prayer_staff', texture_width: 64, texture_height: 64,
    visible_bounds_width: 3, visible_bounds_height: 4, visible_bounds_offset: [0, 0.3, 0]
}, bones }] };
const keyframes = (start, middle, end = start) => ({ '0.0': start, '2.0': middle, '4.0': end });
const animations = { format_version: '1.8.0', animations: { 'animation.qizai_staff.idle': {
    loop: true, animation_length: 4, bones: {
        heart_crystal: { position: keyframes([0, -0.35, 0], [0, 0.55, 0]), rotation: keyframes([0, 0, 0], [0, 180, 0], [0, 360, 0]) },
        star_ring: { rotation: keyframes([0, 0, -6], [0, 0, 6]) },
        ribbon: { rotation: keyframes([6, 0, -4], [-6, 0, 4]) },
        page_orbit: { rotation: keyframes([0, 0, 0], [0, 180, 0], [0, 360, 0]) },
        page_0: { rotation: keyframes([0, 0, -8], [0, 0, 8]) },
        page_1: { rotation: keyframes([0, 0, 8], [0, 0, -8]) },
        page_2: { position: keyframes([0, -0.3, 0], [0, 0.3, 0]) }
    }
} } };
animations.animations['animation.qizai_staff.charge'] = {
    loop: false, animation_length: 0.6, bones: {
        page_orbit: { scale: { '0.0': [1, 1, 1], '0.6': [1.55, 1.55, 1.55] }, rotation: { '0.0': [0, 0, 0], '0.6': [0, 70, 0] } },
        heart_crystal: { position: { '0.0': [0, 0, 0], '0.6': [0, 0.6, 0] }, scale: { '0.0': [1, 1, 1], '0.6': [1.15, 1.15, 1.15] } },
        star_ring: { rotation: { '0.0': [0, 0, 0], '0.6': [0, 0, 24] } },
        ribbon: { rotation: { '0.0': [0, 0, 0], '0.6': [25, 0, 0] } }
    }
};
animations.animations['animation.qizai_staff.release'] = {
    loop: false, animation_length: 1.2, bones: {
        page_orbit: { scale: { '0.0': [1.55, 1.55, 1.55], '1.2': [1, 1, 1] }, rotation: { '0.0': [0, 70, 0], '1.2': [0, 360, 0] } },
        heart_crystal: { position: { '0.0': [0, 0.6, 0], '1.2': [0, 0, 0] }, scale: { '0.0': [1.15, 1.15, 1.15], '1.2': [1, 1, 1] } },
        star_ring: { rotation: { '0.0': [0, 0, 24], '1.2': [0, 0, 0] } },
        ribbon: { rotation: { '0.0': [25, 0, 0], '1.2': [0, 0, 0] } }
    }
};
const transform = (rotation, translation, scale) => ({ rotation, translation, scale: [scale, scale, scale] });
const model = { parent: 'builtin/entity', gui_light: 'front', textures: { particle: 'ftboss:item/qizai_prayer_staff' }, display: {
    thirdperson_righthand: transform([0, 90, 0], [0, 2, 0], 0.8),
    thirdperson_lefthand: transform([0, -90, 0], [0, 2, 0], 0.8),
    firstperson_righthand: transform([0, -90, 8], [2, 0, -1], 0.65),
    firstperson_lefthand: transform([0, 90, -8], [2, 0, -1], 0.65),
    gui: transform([8, 25, -25], [0, -0.5, 0], 0.38),
    ground: transform([0, 0, 70], [0, 2, 0], 0.35),
    fixed: transform([0, 0, -20], [0, 0, 0], 0.4)
} };
function write(relative, data) {
    const file = path.join(output, relative);
    fs.mkdirSync(path.dirname(file), { recursive: true });
    fs.writeFileSync(file, JSON.stringify(data, null, 2) + '\n');
}
write('geo/qizai_prayer_staff.geo.json', geometry);
write('animations/qizai_prayer_staff.animation.json', animations);
write('models/item/qizai_prayer_staff.json', model);
write('models/item/easter_egg_qizai.json', { parent: 'minecraft:item/template_spawn_egg' });
fs.mkdirSync(path.join(output, 'textures/item'), { recursive: true });
texture.save(path.join(output, 'textures/item/qizai_prayer_staff.png'));
glow.save(path.join(output, 'textures/item/qizai_prayer_staff_glowmask.png'));
console.log('Qizai staff: ' + bones.length + ' bones, ' + bones.reduce((total, entry) => total + entry.cubes.length, 0) + ' cubes');
