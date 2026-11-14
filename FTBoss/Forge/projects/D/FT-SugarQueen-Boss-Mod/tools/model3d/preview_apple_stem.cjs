'use strict';

const fs = require('fs');
const path = require('path');
const [assetRoot, libraryRoot] = process.argv.slice(2).map(value => path.resolve(value));
const { Canvas, decodePNG } = require(path.join(libraryRoot, 'png.cjs'));
const { rotateXYZ } = require(path.join(libraryRoot, 'cube_space.cjs'));
const model = JSON.parse(fs.readFileSync(path.join(assetRoot, 'models/item/apple_stem.json'), 'utf8'));
const texture = decodePNG(path.join(assetRoot, 'textures/item/apple_stem_micui.png'));

function facePoints(element, direction) {
  const [left, bottom, front] = element.from;
  const [right, top, back] = element.to;
  const definitions = {
    north: [[right, top, front], [left, top, front], [right, bottom, front]],
    south: [[left, top, back], [right, top, back], [left, bottom, back]],
    west: [[left, top, front], [left, top, back], [left, bottom, front]],
    east: [[right, top, back], [right, top, front], [right, bottom, back]],
    up: [[left, top, front], [right, top, front], [left, top, back]],
    down: [[left, bottom, back], [right, bottom, back], [left, bottom, front]]
  };
  return definitions[direction];
}

function render(width, height, rotation, scale, slot) {
  const canvas = new Canvas(width, height);
  const depth = new Float64Array(width * height).fill(-Infinity);
  const transform = point => {
    let result = point.map((value, axis) => value - [8, slot ? 8 : 11.5, 8][axis]);
    if (slot) {
      const display = model.display[slot];
      result = result.map((value, axis) => value * display.scale[axis]);
      result = rotateXYZ(result, display.rotation);
      result = result.map((value, axis) => value + display.translation[axis]);
    }
    result = rotateXYZ(result, rotation);
    return [width / 2 + result[0] * scale, height / 2 - result[1] * scale, result[2]];
  };
  for (const element of model.elements) {
    const elementRotation = [0, 0, 0];
    if (element.rotation) elementRotation['xyz'.indexOf(element.rotation.axis)] = element.rotation.angle;
    for (const [direction, face] of Object.entries(element.faces)) {
      const points = facePoints(element, direction).map(point =>
        transform(rotateXYZ(point, elementRotation, element.rotation?.origin || [0, 0, 0])));
      const origin = points[0];
      const horizontal = points[1].map((value, axis) => value - origin[axis]);
      const vertical = points[2].map((value, axis) => value - origin[axis]);
      const determinant = horizontal[0] * vertical[1] - horizontal[1] * vertical[0];
      if (Math.abs(determinant) < 1e-8) continue;
      const last = origin.map((value, axis) => value + horizontal[axis] + vertical[axis]);
      const corners = [...points, last];
      const left = Math.max(0, Math.floor(Math.min(...corners.map(point => point[0]))));
      const right = Math.min(width - 1, Math.ceil(Math.max(...corners.map(point => point[0]))));
      const top = Math.max(0, Math.floor(Math.min(...corners.map(point => point[1]))));
      const bottom = Math.min(height - 1, Math.ceil(Math.max(...corners.map(point => point[1]))));
      const brightness = { north: 0.94, south: 1, west: 0.72, east: 0.8, up: 1.08, down: 0.62 }[direction];
      for (let pixelY = top; pixelY <= bottom; pixelY++) {
        for (let pixelX = left; pixelX <= right; pixelX++) {
          const offsetX = pixelX + 0.5 - origin[0];
          const offsetY = pixelY + 0.5 - origin[1];
          const along = (offsetX * vertical[1] - offsetY * vertical[0]) / determinant;
          const down = (horizontal[0] * offsetY - horizontal[1] * offsetX) / determinant;
          if (along < 0 || along > 1 || down < 0 || down > 1) continue;
          const distance = origin[2] + along * horizontal[2] + down * vertical[2];
          const pixel = pixelY * width + pixelX;
          if (distance <= depth[pixel]) continue;
          const texX = Math.min(texture.w - 1, Math.max(0, Math.floor((face.uv[0] + along * (face.uv[2] - face.uv[0])) * texture.w / 16)));
          const texY = Math.min(texture.h - 1, Math.max(0, Math.floor((face.uv[1] + down * (face.uv[3] - face.uv[1])) * texture.h / 16)));
          const offset = (texY * texture.w + texX) * 4;
          if (texture.data[offset + 3] < 128) continue;
          depth[pixel] = distance;
          canvas.set(pixelX, pixelY, texture.data[offset] * brightness,
            texture.data[offset + 1] * brightness, texture.data[offset + 2] * brightness, 1);
        }
      }
    }
  }
  return canvas;
}

const board = new Canvas(1200, 790);
board.rect(0, 0, board.w, board.h, 18, 26, 24, 1);
for (let column = 0; column < 3; column++) {
  const left = 24 + column * 390;
  board.rect(left, 95, 370, 622, 27, 39, 34, 1);
  for (let gridY = 111; gridY < 717; gridY += 24) board.hline(left, left + 369, gridY, 34, 48, 41, 1);
  for (let gridX = left + 8; gridX < left + 370; gridX += 24) board.vline(gridX, 95, 716, 34, 48, 41, 1);
}
function blit(destination, source, left, top) {
  for (let pixelY = 0; pixelY < source.h; pixelY++) {
    for (let pixelX = 0; pixelX < source.w; pixelX++) {
      const offset = (pixelY * source.w + pixelX) * 4;
      if (source.d[offset + 3]) destination.set(left + pixelX, top + pixelY,
        source.d[offset], source.d[offset + 1], source.d[offset + 2], source.d[offset + 3] / 255);
    }
  }
}
blit(board, render(370, 590, [0, 0, 0], 23), 24, 111);
blit(board, render(370, 590, [10, -35, -8], 23), 414, 111);
blit(board, render(370, 590, [0, 180, 0], 23), 804, 111);
board.save(path.join(assetRoot, 'apple_stem_views.png'));
const icon = render(64, 64, [0, 0, 0], 3.3, 'gui');
icon.save(path.join(assetRoot, 'apple_stem_icon.png'));
icon.scaled(6).save(path.join(assetRoot, 'apple_stem_icon_zoom.png'));
const side = render(360, 650, [0, 90, 0], 25);
side.save(path.join(assetRoot, 'apple_stem_side.png'));
console.log('Rendered textured front / oblique / back, side and GUI previews. Offline previews are not in-game calibration.');
