const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const assetRoot = path.resolve(process.argv[2]);
const animationFile = path.resolve(process.argv[3]);
const libraryRoot = process.env.FT_MODEL_TOOLS || __dirname;
const { Canvas, decodePNG } = require(path.join(libraryRoot, 'png.cjs'));
const { loadGeo, listCubes, cubeCorners } = require(path.join(libraryRoot, 'cube_space.cjs'));
const model = loadGeo(path.join(assetRoot, 'apple_boss.geo.json'));
const animations = JSON.parse(fs.readFileSync(animationFile, 'utf8')).animations;
const faceCorners = { north: [2, 6, 0], south: [7, 3, 5], east: [6, 7, 4], west: [3, 2, 1], up: [3, 7, 2], down: [0, 4, 1] };
const cubes = listCubes(model);
const textures = [decodePNG(path.join(assetRoot, 'apple_boss.png')), decodePNG(path.join(assetRoot, 'apple_boss_p2.png'))];

function sample(track, time) {
    if (Array.isArray(track)) return track;
    const frames = Object.entries(track).map(([key, value]) => [Number(key), value]).sort((first, second) => first[0] - second[0]);
    if (time <= frames[0][0]) return frames[0][1];
    for (let index = 1; index < frames.length; index++) {
        if (time > frames[index][0]) continue;
        const [before, start] = frames[index - 1], [after, end] = frames[index];
        const amount = (time - before) / (after - before);
        return start.map((value, axis) => value + (end[axis] - value) * amount);
    }
    return frames[frames.length - 1][1];
}
function poseAt(name, time) {
    const animation = animations['animation.apple_boss.' + name];
    return Object.fromEntries(Object.entries(animation.bones).map(([bone, channels]) => [bone,
        Object.fromEntries(Object.entries(channels).map(([channel, track]) => [channel, sample(track, time)]))]));
}
function render(pose, secondPhase, yawDegrees, pitchDegrees, width = 560, height = 610) {
    const canvas = new Canvas(width, height), depth = new Float64Array(width * height).fill(-Infinity);
    canvas.rect(0, 0, width, height, 24, 30, 34, 1);
    const texture = textures[secondPhase ? 1 : 0];
    const yaw = yawDegrees * Math.PI / 180, pitch = pitchDegrees * Math.PI / 180;
    const viewPoint = point => {
        const horizontal = -point[0] * Math.cos(yaw) - point[2] * Math.sin(yaw);
        const toward = point[0] * Math.sin(yaw) - point[2] * Math.cos(yaw);
        return [horizontal, point[1] * Math.cos(pitch) - toward * Math.sin(pitch),
            toward * Math.cos(pitch) + point[1] * Math.sin(pitch)];
    };
    const transformed = cubes.map(cubeInfo => cubeCorners(model, cubeInfo, pose).map(viewPoint));
    const visible = transformed.filter(points => points.some(point => point.some((value, axis) => Math.abs(value - points[0][axis]) > 0.01))).flat();
    const minimum = [0, 1].map(axis => Math.min(...visible.map(point => point[axis])));
    const maximum = [0, 1].map(axis => Math.max(...visible.map(point => point[axis])));
    const scale = Math.min(10.5, (width - 64) / (maximum[0] - minimum[0]), (height - 145) / (maximum[1] - minimum[1]));
    const baseline = height - 80 + minimum[1] * scale;
    const offsetX = width / 2 - (minimum[0] + maximum[0]) / 2 * scale;
    const project = point => [offsetX + point[0] * scale, baseline - point[1] * scale, point[2]];
    for (let pixelY = baseline - 20; pixelY < baseline + 24; pixelY++) for (let pixelX = 90; pixelX < width - 90; pixelX++) {
        const radius = ((pixelX - width / 2) / 160) ** 2 + ((pixelY - baseline) / 22) ** 2;
        if (radius < 1) canvas.set(pixelX, pixelY, 10, 15, 18, (1 - radius) * 0.6);
    }
    for (const [cubeIndex, cubeInfo] of cubes.entries()) {
        const points = transformed[cubeIndex].map(project);
        for (const [face, indices] of Object.entries(faceCorners)) {
            const uv = cubeInfo.cube.uv[face];
            const [origin, right, bottom] = indices.map(index => points[index]);
            const across = right.map((value, axis) => value - origin[axis]);
            const down = bottom.map((value, axis) => value - origin[axis]);
            const determinant = across[0] * down[1] - across[1] * down[0];
            if (Math.abs(determinant) < 1e-8) continue;
            const fourth = origin.map((value, axis) => value + across[axis] + down[axis]);
            const corners = [origin, right, bottom, fourth];
            const left = Math.max(0, Math.floor(Math.min(...corners.map(point => point[0]))));
            const top = Math.max(0, Math.floor(Math.min(...corners.map(point => point[1]))));
            const endX = Math.min(width - 1, Math.ceil(Math.max(...corners.map(point => point[0]))));
            const endY = Math.min(height - 1, Math.ceil(Math.max(...corners.map(point => point[1]))));
            const light = { north: 1, south: 0.9, east: 0.86, west: 0.76, up: 1.1, down: 0.64 }[face];
            for (let pixelY = top; pixelY <= endY; pixelY++) for (let pixelX = left; pixelX <= endX; pixelX++) {
                const offsetX = pixelX + 0.5 - origin[0], offsetY = pixelY + 0.5 - origin[1];
                const along = (offsetX * down[1] - offsetY * down[0]) / determinant;
                const vertical = (across[0] * offsetY - across[1] * offsetX) / determinant;
                if (along < 0 || along > 1 || vertical < 0 || vertical > 1) continue;
                const distance = origin[2] + along * across[2] + vertical * down[2];
                const pixel = pixelY * width + pixelX;
                if (distance <= depth[pixel]) continue;
                const texX = Math.min(texture.w - 1, Math.floor(uv.uv[0] + along * uv.uv_size[0]));
                const texY = Math.min(texture.h - 1, Math.floor(uv.uv[1] + vertical * uv.uv_size[1]));
                const offset = (texY * texture.w + texX) * 4;
                if (texture.data[offset + 3] < 128) continue;
                canvas.set(pixelX, pixelY, texture.data[offset] * light, texture.data[offset + 1] * light, texture.data[offset + 2] * light, 1);
                depth[pixel] = distance;
            }
        }
    }
    return canvas;
}
const poses = [['idle', 0.5], ['walk', 0.225], ['attack', 0.38], ['roll_charge', 0.8], ['transform', 2.4], ['death', 1.9]];
for (const [name, time] of poses) render(poseAt(name, time), name === 'transform', 26, 12).save(path.join(assetRoot, 'preview_' + name + '.png'));
const phaseTwo = { ...poseAt('idle', 0.5), ...poseAt('lid_open', 0.06) };
render(phaseTwo, true, 26, 12).save(path.join(assetRoot, 'preview_phase2.png'));
const panels = [render({}, false, 26, 12), render({}, false, 0, 0), render({}, false, 180, 0), render(phaseTwo, true, 26, 12)];
const sheet = new Canvas(1120, 1220);
for (const [index, panel] of panels.entries()) {
    const left = index % 2 * panel.w, top = Math.floor(index / 2) * panel.h;
    for (let row = 0; row < panel.h; row++) sheet.d.set(panel.d.subarray(row * panel.w * 4, (row + 1) * panel.w * 4), ((top + row) * sheet.w + left) * 4);
}
sheet.save(path.join(assetRoot, 'apple_boss_preview.png'));

const uuid = key => {
    const hex = crypto.createHash('sha256').update('apple_boss_remodel:' + key).digest('hex');
    return [hex.slice(0, 8), hex.slice(8, 12), '4' + hex.slice(13, 16), 'a' + hex.slice(17, 20), hex.slice(20, 32)].join('-');
};
const groups = new Map(model.bones.map(bone => [bone.name, { name: bone.name, uuid: uuid(bone.name), origin: bone.pivot, children: [] }]));
const outliner = [], elements = [];
for (const cubeInfo of cubes) {
    const cube = cubeInfo.cube;
    const element = { name: cubeInfo.name, uuid: uuid(cubeInfo.name), type: 'cube', box_uv: false,
        from: cube.origin, to: cube.origin.map((value, axis) => value + cube.size[axis]),
        origin: cube.pivot || cube.origin.map((value, axis) => value + cube.size[axis] / 2), rotation: cube.rotation || [0, 0, 0],
        faces: Object.fromEntries(Object.entries(cube.uv).map(([face, uv]) => [face, {
            uv: [...uv.uv, uv.uv[0] + uv.uv_size[0], uv.uv[1] + uv.uv_size[1]], texture: 0
        }])) };
    elements.push(element); groups.get(cubeInfo.bone).children.push(element.uuid);
}
for (const bone of model.bones) {
    const group = groups.get(bone.name);
    if (bone.parent) groups.get(bone.parent).children.push(group); else outliner.push(group);
}
const project = {
    meta: { format_version: '4.10', model_format: 'bedrock', box_uv: false }, name: '苹果爵士 · 蜜脆庄园',
    model_identifier: 'apple_boss', resolution: { width: 256, height: 256 }, elements, outliner,
    textures: ['apple_boss.png', 'apple_boss_p2.png'].map((name, index) => ({ name, id: String(index), uuid: uuid(name),
        namespace: 'ftboss', folder: 'entity', source: 'data:image/png;base64,' + fs.readFileSync(path.join(assetRoot, name)).toString('base64') })),
    animations: Object.entries(animations).map(([name, animation]) => ({ name, uuid: uuid(name),
        loop: animation.loop === true ? 'loop' : animation.loop === 'hold_on_last_frame' ? 'hold' : 'once',
        length: animation.animation_length, snapping: 20,
        animators: Object.fromEntries(Object.entries(animation.bones).map(([bone, channels]) => [groups.get(bone).uuid, {
            name: bone, type: 'bone', keyframes: Object.entries(channels).flatMap(([channel, track]) => Object.entries(track).map(([time, vector]) => ({
                channel, time: Number(time), interpolation: 'linear', uuid: uuid(name + bone + channel + time),
                data_points: [{ x: String(vector[0]), y: String(vector[1]), z: String(vector[2]) }]
            }))).sort((first, second) => first.time - second.time)
        }]))
    }))
};
fs.writeFileSync(path.join(assetRoot, 'apple_boss.bbmodel'), JSON.stringify(project, null, 2) + '\n');
console.log('Rendered 7 poses + four-panel sheet; exported Blockbench model with 16 animations.');
