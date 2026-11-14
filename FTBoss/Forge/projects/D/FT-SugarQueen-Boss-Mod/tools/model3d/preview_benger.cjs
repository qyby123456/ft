'use strict';

const fs = require('fs');
const path = require('path');
const { loadGeo, listCubes, cubeCorners } = require('./cube_space.cjs');
const { decodePNG } = require('./png.cjs');
const { createCanvas, GlobalFonts } = require(path.join(process.env.FT_NODE_MODULES, '@napi-rs/canvas'));

const faceCorners = {
    north: [2, 6, 4, 0], south: [7, 3, 1, 5],
    east: [6, 7, 5, 4], west: [3, 2, 0, 1],
    up: [3, 7, 6, 2], down: [0, 4, 5, 1]
};
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);

function samplePose(animation, time) {
    const pose = {};
    if (!animation) return pose;
    for (const [bone, channels] of Object.entries(animation.bones)) {
        pose[bone] = {};
        for (const [channel, keys] of Object.entries(channels)) {
            const frames = Object.entries(keys).map(([at, values]) => [Number(at), values]).sort((first, second) => first[0] - second[0]);
            let lower = frames[0];
            let upper = frames[frames.length - 1];
            for (const frame of frames) {
                if (frame[0] <= time) lower = frame;
                if (frame[0] >= time) { upper = frame; break; }
            }
            const amount = Math.max(0, Math.min(1, (time - lower[0]) / (upper[0] - lower[0] || 1)));
            pose[bone][channel] = lower[1].map((value, axis) => value + (upper[1][axis] - value) * amount);
        }
    }
    return pose;
}

function render(model, texture, width, height, options = {}) {
    const canvas = createCanvas(width, height);
    const context = canvas.getContext('2d');
    const image = context.createImageData(width, height);
    const depthBuffer = new Float32Array(width * height).fill(-Infinity);
    const yaw = (options.yaw || 0) * Math.PI / 180;
    const elevation = (options.elevation || 0) * Math.PI / 180;
    const camera = [Math.sin(yaw) * Math.cos(elevation), Math.sin(elevation), -Math.cos(yaw) * Math.cos(elevation)];
    const right = [-Math.cos(yaw), 0, -Math.sin(yaw)];
    const upward = [-Math.sin(elevation) * Math.sin(yaw), Math.cos(elevation), Math.sin(elevation) * Math.cos(yaw)];
    const center = options.center || [0, 27, 3];
    const scale = options.scale || 7;
    const pose = options.pose || {};
    const project = point => {
        const relative = point.map((value, axis) => value - center[axis]);
        return [width / 2 + dot(relative, right) * scale, height / 2 - dot(relative, upward) * scale, dot(relative, camera)];
    };
    function triangle(points, coordinates, shade) {
        const [first, second, third] = points;
        const determinant = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
        if (Math.abs(determinant) < 1e-7) return;
        const minColumn = Math.max(0, Math.floor(Math.min(...points.map(point => point[0]))));
        const maxColumn = Math.min(width - 1, Math.ceil(Math.max(...points.map(point => point[0]))));
        const minRow = Math.max(0, Math.floor(Math.min(...points.map(point => point[1]))));
        const maxRow = Math.min(height - 1, Math.ceil(Math.max(...points.map(point => point[1]))));
        for (let row = minRow; row <= maxRow; row++) {
            for (let column = minColumn; column <= maxColumn; column++) {
                const weightFirst = ((second[1] - third[1]) * (column + 0.5 - third[0]) + (third[0] - second[0]) * (row + 0.5 - third[1])) / determinant;
                const weightSecond = ((third[1] - first[1]) * (column + 0.5 - third[0]) + (first[0] - third[0]) * (row + 0.5 - third[1])) / determinant;
                const weightThird = 1 - weightFirst - weightSecond;
                if (Math.min(weightFirst, weightSecond, weightThird) < -1e-7) continue;
                const weights = [weightFirst, weightSecond, weightThird];
                const depth = dot(weights, points.map(point => point[2]));
                const pixel = row * width + column;
                if (depth < depthBuffer[pixel]) continue;
                const textureU = Math.max(0, Math.min(texture.w - 1, Math.floor(dot(weights, coordinates.map(uv => uv[0])))));
                const textureV = Math.max(0, Math.min(texture.h - 1, Math.floor(dot(weights, coordinates.map(uv => uv[1])))));
                const texel = (textureV * texture.w + textureU) * 4;
                if (texture.data[texel + 3] < 128) continue;
                depthBuffer[pixel] = depth;
                for (let channel = 0; channel < 3; channel++) image.data[pixel * 4 + channel] = Math.min(255, texture.data[texel + channel] * shade);
                image.data[pixel * 4 + 3] = 255;
            }
        }
    }
    for (const entry of listCubes(model)) {
        if (options.headOnly && !['head', 'jaw', 'ear_l', 'ear_r', 'neck'].includes(entry.bone)) continue;
        const corners = cubeCorners(model, entry, pose);
        const cubeCenter = [0, 1, 2].map(axis => corners.reduce((sum, point) => sum + point[axis], 0) / 8);
        for (const [face, indices] of Object.entries(faceCorners)) {
            const vertices = indices.map(index => corners[index]);
            const faceCenter = [0, 1, 2].map(axis => vertices.reduce((sum, point) => sum + point[axis], 0) / 4);
            const outward = faceCenter.map((value, axis) => value - cubeCenter[axis]);
            const normal = outward.map(value => value / Math.hypot(...outward));
            if (dot(normal, camera) <= 0) continue;
            const shade = 0.76 + Math.max(0, dot(normal, [-0.35, 0.8, -0.48])) * 0.27;
            const mapping = entry.cube.uv[face];
            const [startU, startV] = mapping.uv;
            const [spanU, spanV] = mapping.uv_size;
            const coordinates = [[startU, startV], [startU + spanU, startV], [startU + spanU, startV + spanV], [startU, startV + spanV]];
            const projected = vertices.map(project);
            for (const indices of [[0, 1, 2], [0, 2, 3]]) triangle(indices.map(index => projected[index]), indices.map(index => coordinates[index]), shade);
        }
    }
    context.putImageData(image, 0, 0);
    return canvas;
}

if (require.main === module) {
    const directory = path.resolve(process.argv[2] || 'art/benger_reference');
    const model = loadGeo(path.join(directory, 'benger.geo.json'));
    const texture = decodePNG(path.join(directory, 'benger.png'));
    const animations = JSON.parse(fs.readFileSync(path.join(directory, 'benger.animation.json'))).animations;
    GlobalFonts.registerFromPath('C:/Windows/Fonts/msyh.ttc', 'Yahei');
    const poster = createCanvas(1600, 1160);
    const context = poster.getContext('2d');
    context.fillStyle = '#e8e6df';
    context.fillRect(0, 0, poster.width, poster.height);
    const text = (content, horizontal, vertical, size = 20, color = '#686a65') => {
        context.font = `${size}px Yahei`;
        context.fillStyle = color;
        context.fillText(content, horizontal, vertical);
    };
    text('FARMING TALES  /  BOSS MODEL STUDY', 56, 56, 19);
    text('BENGER', 52, 130, 62, '#242b2b');
    text('黑白边牧 · 参考图重制', 370, 127, 27, '#444d49');
    context.fillStyle = '#a65348'; context.fillRect(56, 156, 1488, 3);
    text('01  全身造型', 58, 206);
    context.drawImage(render(model, texture, 880, 850, { yaw: -38, elevation: 13, scale: 11.9, center: [0, 27, 1] }), 10, 225);
    text('02  面部特写', 962, 206);
    context.drawImage(render(model, texture, 585, 470, { yaw: -8, elevation: 3, scale: 18, center: [0, 44, -22], headOnly: true }), 935, 220);
    text('03  正面', 952, 718);
    context.drawImage(render(model, texture, 250, 315, { scale: 5, center: [0, 28, 0] }), 923, 744);
    text('04  侧面', 1240, 718);
    context.drawImage(render(model, texture, 343, 315, { yaw: 90, scale: 4, center: [0, 28, 5] }), 1208, 744);
    text(`27 骨骼  /  ${listCubes(model).length} 方块  /  9 段现有动画  /  2 px 每模型单位`, 56, 1110, 20);
    text('离线模型预览 · 非游戏截图', 1230, 1110, 18);
    fs.writeFileSync(path.join(directory, 'benger-preview.png'), poster.toBuffer('image/png'));
    const sheet = createCanvas(1500, 1500);
    const sheetContext = sheet.getContext('2d');
    sheetContext.fillStyle = '#e8e6df'; sheetContext.fillRect(0, 0, 1500, 1500);
    const shots = [['idle', 0.6], ['walk', 0.25], ['run', 0.16], ['bite', 0.16], ['pounce', 0.3], ['roar', 0.45], ['sweep', 0.34], ['hurt', 0.12], ['death', 1.2]];
    shots.forEach(([name, time], index) => {
        const horizontal = index % 3 * 500;
        const vertical = Math.floor(index / 3) * 500;
        sheetContext.font = '22px Yahei'; sheetContext.fillStyle = '#424b47';
        sheetContext.fillText(`${name}  /  ${time}s`, horizontal + 20, vertical + 38);
        const pose = samplePose(animations[`animation.benger.${name}`], time);
        const points = listCubes(model).flatMap(entry => cubeCorners(model, entry, pose));
        const minimum = [0, 1, 2].map(axis => Math.min(...points.map(point => point[axis])));
        const maximum = [0, 1, 2].map(axis => Math.max(...points.map(point => point[axis])));
        const center = minimum.map((value, axis) => (value + maximum[axis]) / 2);
        const scale = Math.min(5.7, 410 / Math.max(...maximum.map((value, axis) => value - minimum[axis])));
        sheetContext.drawImage(render(model, texture, 500, 450, { yaw: -32, elevation: 8, scale, center, pose }), horizontal, vertical + 45);
    });
    fs.writeFileSync(path.join(directory, 'benger-poses.png'), sheet.toBuffer('image/png'));
    console.log('Rendered benger-preview.png and benger-poses.png');
}

module.exports = { render, samplePose };
