const fs = require('node:fs');
const path = require('node:path');
const { Canvas } = require('../model3d/png.cjs');
const poses = fs.readFileSync(process.argv[2], 'utf8').split(/\r?\n/)
  .filter(line => line.startsWith('POSE_JSON=')).map(line => JSON.parse(line.slice(10)));
if (poses.length !== 12) throw new Error('Expected twelve exported pose frames');
const canvas = new Canvas(960, 1120);
canvas.rect(0, 0, canvas.w, canvas.h, 19, 24, 32, 1);
const faces = [[0, 1, 3, 2], [4, 6, 7, 5], [0, 4, 5, 1], [2, 3, 7, 6], [0, 2, 6, 4], [1, 5, 7, 3]];
const colors = [[191, 175, 165], [97, 110, 127], [192, 64, 78], [125, 135, 151], [78, 88, 102], [78, 88, 102]];
const viewYaw = -0.52;
const viewPitch = 0.12;
for (const [poseIndex, pose] of poses.entries()) {
  const column = poseIndex % 3;
  const row = Math.floor(poseIndex / 3);
  const centerX = column * 320 + 160;
  const baseY = row * 280 + 218;
  canvas.rect(column * 320 + 10, row * 280 + 12, 300, 254, 28, 34, 44, 1);
  canvas.rect(column * 320 + 20, row * 280 + 22, 32 + row * 18, 5, 220, 73, 88, 1);
  canvas.rect(column * 320 + 20, row * 280 + 250, column === 0 ? 120 : column * 42, 3, 111, 139, 158, 1);
  const polygons = [];
  for (const [partIndex, corners] of pose.parts.entries()) {
    const vertices = corners.map(vertex => {
      const horizontal = Math.cos(viewYaw) * vertex[0] + Math.sin(viewYaw) * vertex[2];
      const depth = -Math.sin(viewYaw) * vertex[0] + Math.cos(viewYaw) * vertex[2];
      const vertical = Math.cos(viewPitch) * vertex[1] - Math.sin(viewPitch) * depth;
      return [centerX + horizontal * 94, baseY + (vertical - 1.5) * 94, depth];
    });
    for (const [faceIndex, face] of faces.entries()) {
      const shade = 0.62 + faceIndex * 0.065;
      polygons.push({ points: face.map(index => vertices[index]),
        depth: face.reduce((sum, index) => sum + vertices[index][2], 0) / 4,
        color: colors[partIndex].map(channel => Math.round(channel * shade)) });
    }
  }
  polygons.sort((first, second) => second.depth - first.depth);
  for (const polygon of polygons) {
    canvas.quad(polygon.points, ...polygon.color, 1);
    for (let edge = 0; edge < 4; edge++) {
      const first = polygon.points[edge];
      const second = polygon.points[(edge + 1) % 4];
      canvas.line(Math.round(first[0]), Math.round(first[1]), Math.round(second[0]), Math.round(second[1]), 36, 42, 52, 0.7);
    }
  }
}
const output = path.resolve(process.argv[3]);
canvas.save(output);
console.log(output);
