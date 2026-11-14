'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { createCanvas, GlobalFonts } = require(path.join(process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules', '@napi-rs/canvas'));
const { loadGeo, listCubes, cubeCorners, rotateXYZ } = require('../cube_space.cjs');
const { decodePNG } = require('../png.cjs');
const directory = path.resolve(process.argv[2]);
const resources = path.join(directory, 'assets/ftboss');
const model = loadGeo(path.join(resources, 'geo/defect_spire.geo.json'));
const animations = JSON.parse(fs.readFileSync(path.join(resources, 'animations/defect_spire.animation.json'))).animations;
for (const bone of model.bones) {
  if (bone.rotation) bone.rotation = bone.rotation.map((value, axis) => axis === 1 ? value : -value);
  for (const cube of bone.cubes || []) if (cube.rotation) cube.rotation = cube.rotation.map((value, axis) => axis === 1 ? value : -value);
}
const cubes = listCubes(model);
const weapon = JSON.parse(fs.readFileSync(path.join(resources, 'models/item/defect_claw.json')));
const textures = Object.fromEntries(['glass', 'lightning', 'frost', 'ion', 'dark'].map(name => [name, decodePNG(path.join(resources, 'textures/entity/defect_spire_' + name + '.png'))]));
const weaponTexture = decodePNG(path.join(resources, 'textures/item/defect_claw.png'));
GlobalFonts.registerFromPath('C:/Windows/Fonts/msyh.ttc', 'Yahei');
const indices = { north: [6, 4, 0, 2], south: [3, 1, 5, 7], east: [7, 5, 4, 6], west: [2, 0, 1, 3], up: [2, 3, 7, 6], down: [1, 0, 4, 5] };

function poseAt(name, time) {
  const animation = animations['animation.defect_core.' + name];
  const moment = animation.loop === true ? time % animation.animation_length : Math.min(time, animation.animation_length);
  return Object.fromEntries(Object.entries(animation.bones).map(([boneName, channels]) => [boneName,
    Object.fromEntries(Object.entries(channels).map(([channel, frames]) => {
      const entries = Object.entries(frames).map(([stamp, vector]) => [Number(stamp), vector]).sort((left, right) => left[0] - right[0]);
      let first = entries[0], last = entries[entries.length - 1];
      for (let index = 1; index < entries.length; index++) if (moment <= entries[index][0]) { first = entries[index - 1]; last = entries[index]; break; }
      const amount = Math.max(0, Math.min(1, (moment - first[0]) / Math.max(0.00001, last[0] - first[0])));
      return [channel, first[1].map((value, axis) => (value + (last[1][axis] - value) * amount) * (channel === 'rotation' && axis !== 1 ? -1 : 1))];
    }))]));
}
function bossParts(name, time) {
  const pose = poseAt(name, time);
  return cubes.map(entry => ({ owner: entry.bone, points: cubeCorners(model, entry, pose), faces: entry.cube.uv }));
}
function weaponParts() {
  return weapon.elements.map(element => {
    const points = [];
    for (const horizontal of [element.from[0], element.to[0]]) for (const vertical of [element.from[1], element.to[1]]) for (const depth of [element.from[2], element.to[2]]) {
      let point = [horizontal, vertical, depth];
      if (element.rotation) point = rotateXYZ(point, ['x', 'y', 'z'].map(axis => axis === element.rotation.axis ? element.rotation.angle : 0), element.rotation.origin);
      points.push(point);
    }
    return { points, faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => [side, {
      uv: face.uv.slice(0, 2).map(value => value * weaponTexture.w / 16),
      uv_size: [face.uv[2] - face.uv[0], face.uv[3] - face.uv[1]].map(value => value * weaponTexture.w / 16)
    }])) };
  });
}
function render(parts, texture, width, height, options = {}) {
  const canvas = createCanvas(width, height);
  const context = canvas.getContext('2d');
  const image = context.createImageData(width, height);
  const depthBuffer = new Float32Array(width * height).fill(Infinity);
  const center = options.center || [0, 24, 0];
  const zoom = options.zoom || Math.min(width / 36, height / 57);
  const view = point => rotateXYZ(point.map((value, axis) => (value - center[axis]) * (axis === 0 ? -1 : 1)), [options.pitch || 0, options.yaw || 0, options.roll || 0]);
  const project = point => [width / 2 + point[0] * zoom, height / 2 - point[1] * zoom, point[2]];
  function triangle(points, coordinates, brightness) {
    const [first, second, third] = points;
    const denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1]);
    if (Math.abs(denominator) < 0.00001) return;
    const minX = Math.max(0, Math.floor(Math.min(...points.map(point => point[0]))));
    const maxX = Math.min(width - 1, Math.ceil(Math.max(...points.map(point => point[0]))));
    const minY = Math.max(0, Math.floor(Math.min(...points.map(point => point[1]))));
    const maxY = Math.min(height - 1, Math.ceil(Math.max(...points.map(point => point[1]))));
    for (let pixelY = minY; pixelY <= maxY; pixelY++) for (let pixelX = minX; pixelX <= maxX; pixelX++) {
      const sampleX = pixelX + 0.5, sampleY = pixelY + 0.5;
      const weightA = ((second[1] - third[1]) * (sampleX - third[0]) + (third[0] - second[0]) * (sampleY - third[1])) / denominator;
      const weightB = ((third[1] - first[1]) * (sampleX - third[0]) + (first[0] - third[0]) * (sampleY - third[1])) / denominator;
      const weightC = 1 - weightA - weightB;
      if (Math.min(weightA, weightB, weightC) < -0.00001) continue;
      const distance = weightA * first[2] + weightB * second[2] + weightC * third[2];
      const index = pixelY * width + pixelX;
      if (distance >= depthBuffer[index]) continue;
      const horizontal = Math.min(texture.w - 1, Math.max(0, Math.floor(weightA * coordinates[0][0] + weightB * coordinates[1][0] + weightC * coordinates[2][0])));
      const vertical = Math.min(texture.h - 1, Math.max(0, Math.floor(weightA * coordinates[0][1] + weightB * coordinates[1][1] + weightC * coordinates[2][1])));
      const source = (vertical * texture.w + horizontal) * 4;
      if (!texture.data[source + 3]) continue;
      for (let channel = 0; channel < 3; channel++) image.data[index * 4 + channel] = Math.min(255, texture.data[source + channel] * brightness);
      image.data[index * 4 + 3] = 255;
      depthBuffer[index] = distance;
    }
  }
  for (const part of parts) {
    const transformed = part.points.map(view);
    for (const [side, rectangle] of Object.entries(part.faces)) {
      const vertices = indices[side].map(index => transformed[index]);
      const firstEdge = vertices[1].map((value, axis) => value - vertices[0][axis]);
      const secondEdge = vertices[2].map((value, axis) => value - vertices[0][axis]);
      const normal = [firstEdge[1] * secondEdge[2] - firstEdge[2] * secondEdge[1], firstEdge[2] * secondEdge[0] - firstEdge[0] * secondEdge[2], firstEdge[0] * secondEdge[1] - firstEdge[1] * secondEdge[0]].map(value => -value);
      const length = Math.hypot(...normal);
      if (normal[2] >= -0.00001) continue;
      const brightness = 0.76 + 0.24 * Math.max(0, (-normal[0] * 0.32 + normal[1] * 0.7 - normal[2] * 0.65) / length);
      const [left, top] = rectangle.uv, [faceWidth, faceHeight] = rectangle.uv_size;
      const coordinates = [[left, top], [left, top + faceHeight], [left + faceWidth, top + faceHeight], [left + faceWidth, top]];
      const projected = vertices.map(project);
      triangle([projected[0], projected[1], projected[2]], [coordinates[0], coordinates[1], coordinates[2]], brightness);
      triangle([projected[0], projected[2], projected[3]], [coordinates[0], coordinates[2], coordinates[3]], brightness);
    }
  }
  context.putImageData(image, 0, 0);
  return canvas;
}
function text(context, content, horizontal, vertical, size = 22, color = '#b6c3bc') {
  context.fillStyle = color; context.font = size + 'px Yahei'; context.fillText(content, horizontal, vertical);
}
function sheet(width, height) {
  const canvas = createCanvas(width, height), context = canvas.getContext('2d');
  context.fillStyle = '#161e20'; context.fillRect(0, 0, width, height);
  return { canvas, context };
}
function main() {
  const { canvas, context } = sheet(1600, 1060);
  text(context, 'THE DEFECT  /  故障机器人', 50, 68, 38, '#e9d3a0');
  text(context, '黄铜裂壳 · 青蓝复眼 · 分段机械关节 · 破损青绿披风', 52, 110);
  context.drawImage(render(bossParts('idle', 0), textures.glass, 650, 805, { yaw: -28, pitch: 6, zoom: 14 }), 20, 155);
  for (const [index, yaw, label] of [[0, 0, '正面'], [1, 90, '侧面'], [2, 180, '背面']]) {
    context.drawImage(render(bossParts('idle', 0), textures.glass, 285, 440, { yaw, zoom: 8.1 }), 680 + index * 295, 145);
    text(context, label, 792 + index * 295, 610, 19);
  }
  context.drawImage(render(weaponParts(), weaponTexture, 420, 360, { center: [7, 10, 7], yaw: -28, pitch: 14, roll: -18, zoom: 17 }), 680, 645);
  text(context, '掉落武器 / 充能机械爪', 1125, 716, 26, '#e9d3a0');
  text(context, '三指弯爪 + 对握拇指', 1125, 767, 20);
  text(context, '空心腕箍 + 内置握杆', 1125, 804, 20);
  text(context, '独立电容镜片与导电槽', 1125, 841, 20);
  text(context, '游戏模型贴图离线渲染 · 非游戏截图', 50, 1020, 18, '#7d9692');
  fs.writeFileSync(path.join(directory, 'preview.png'), canvas.toBuffer('image/png'));
  const poses = [['cast_glass', 0.55, 'glass', '玻璃 / 双掌推出'], ['cast_lightning', 0.45, 'lightning', '雷电 / 单臂落雷'], ['cast_frost', 0.3, 'frost', '冰霜 / 横向展开'], ['cast_ion', 0.3, 'ion', '离子 / 交替击发'], ['cast_dark', 1.3, 'dark', '黑暗 / 聚能释放'], ['float_move', 0.4, 'glass', '漂移 / 收腿滑行']];
  const contact = sheet(1500, 1050);
  text(contact.context, '独立施法姿势与移动姿态', 42, 55, 30, '#e9d3a0');
  for (const [index, [name, time, tint, label]] of poses.entries()) {
    const horizontal = index % 3 * 500, vertical = Math.floor(index / 3) * 485 + 80;
    contact.context.drawImage(render(bossParts(name, time), textures[tint], 480, 425, { yaw: -20, pitch: 3, zoom: 7.6 }), horizontal + 10, vertical);
    text(contact.context, label, horizontal + 100, vertical + 449, 21);
  }
  fs.writeFileSync(path.join(directory, 'poses.png'), contact.canvas.toBuffer('image/png'));
  if (process.argv.includes('--video')) {
    const frameDirectory = path.join(directory, 'frames'); fs.mkdirSync(frameDirectory, { recursive: true });
    const clips = [['idle', 3.2, 'glass'], ['cast_lightning', 1, 'lightning'], ['cast_frost', 1, 'frost'], ['cast_ion', 1, 'ion'], ['cast_glass', 1.15, 'glass'], ['cast_dark', 1.3, 'dark'], ['float_move', 1.6, 'glass'], ['orb_summon', 0.85, 'glass'], ['phase_shift', 2.6, 'glass'], ['echo', 1.4, 'ion'], ['exhausted', 1.6, 'glass']];
    let frameIndex = 0;
    for (const [name, duration, tint] of clips) for (let frame = 0; frame < Math.round(duration * 15); frame++) {
      const video = sheet(720, 760);
      text(video.context, '故障机器人 / ' + name, 32, 46, 24, '#e9d3a0');
      video.context.drawImage(render(bossParts(name, frame / 15), textures[tint], 720, 670, { yaw: -24, pitch: 4, zoom: 11.3 }), 0, 70);
      fs.writeFileSync(path.join(frameDirectory, String(frameIndex++).padStart(4, '0') + '.png'), video.canvas.toBuffer('image/png'));
    }
  }
  console.log('Rendered preview.png and poses.png');
}
if (require.main === module) main();
module.exports = { model, animations, poseAt, bossParts, weaponParts, render, textures, weaponTexture };
