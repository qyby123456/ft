const fs = require('node:fs');
const path = require('node:path');
const { makeCanvas } = require('./lib_itemtex.cjs');
const WIDTH = 1024;
const HEIGHT = 576;
const clamp = value => Math.max(0, Math.min(1, value));
const smooth = value => { const amount = clamp(value); return amount * amount * (3 - 2 * amount); };

function stroke(canvas, points, width, color, alpha) {
  for (let index = 1; index < points.length; index++) {
    const [startX, startY] = points[index - 1];
    const [endX, endY] = points[index];
    const deltaX = endX - startX;
    const deltaY = endY - startY;
    const lengthSquared = deltaX * deltaX + deltaY * deltaY;
    const minX = Math.max(0, Math.floor(Math.min(startX, endX) - width - 2));
    const maxX = Math.min(WIDTH - 1, Math.ceil(Math.max(startX, endX) + width + 2));
    const minY = Math.max(0, Math.floor(Math.min(startY, endY) - width - 2));
    const maxY = Math.min(HEIGHT - 1, Math.ceil(Math.max(startY, endY) + width + 2));
    for (let pixelY = minY; pixelY <= maxY; pixelY++) for (let pixelX = minX; pixelX <= maxX; pixelX++) {
      const along = lengthSquared === 0 ? 0 : clamp(((pixelX - startX) * deltaX + (pixelY - startY) * deltaY) / lengthSquared);
      const distance = Math.hypot(pixelX - startX - along * deltaX, pixelY - startY - along * deltaY);
      canvas.put(pixelX, pixelY, color, smooth((width + 1.1 - distance) / 1.6) * alpha);
    }
  }
}

function generate(assets) {
  const frame = makeCanvas(WIDTH, HEIGHT);
  const sigils = makeCanvas(WIDTH, HEIGHT);
  for (let pixelY = 0; pixelY < HEIGHT; pixelY++) for (let pixelX = 0; pixelX < WIDTH; pixelX++) {
    const horizontal = (pixelX / (WIDTH - 1) - 0.5) * 2;
    const vertical = (pixelY / (HEIGHT - 1) - 0.5) * 2;
    const angle = Math.atan2(vertical, horizontal);
    const radius = Math.hypot(horizontal * 0.88, vertical);
    const fold = Math.sin(angle * 9 + Math.sin(angle * 3) * 1.5) * 0.045 + Math.sin(angle * 17 + 0.8) * 0.023;
    const outer = smooth((radius + fold - 0.66) / 0.44);
    const ribbon = Math.pow(0.5 + 0.5 * Math.sin(radius * 38 + angle * 4 + Math.sin(angle * 13) * 1.5), 7);
    const mist = smooth((radius + fold - 0.45) / 0.5) * 0.16;
    const centerMask = smooth((Math.max(Math.abs(horizontal) / 0.52, Math.abs(vertical) / 0.48) - 1) / 0.5);
    const alpha = (outer * 231 + mist * 110) * centerMask;
    const rim = Math.exp(-Math.pow((radius + fold - 0.84) / 0.13, 2));
    frame.put(pixelX, pixelY, [5 + rim * 17 + ribbon * 8, 3 + rim * 7 + ribbon * 3,
      12 + rim * 32 + ribbon * 18], alpha);
    if (rim * ribbon > 0.15) sigils.put(pixelX, pixelY, [100, 71, 160], rim * ribbon * 42 * centerMask);
  }
  const fractures = [
    [[0, 63], [65, 92], [83, 138], [125, 153], [142, 194]],
    [[0, 454], [63, 418], [83, 376], [133, 369], [163, 334]],
    [[1023, 80], [965, 105], [942, 155], [905, 162], [886, 214]],
    [[1023, 468], [956, 431], [945, 392], [900, 373], [865, 346]],
    [[221, 0], [249, 46], [287, 54], [315, 86], [353, 101]],
    [[761, 0], [731, 34], [718, 72], [675, 81], [654, 102]],
    [[215, 575], [262, 548], [269, 511], [319, 493], [336, 471]],
    [[809, 575], [772, 534], [731, 528], [707, 493], [667, 485]]
  ];
  for (const [index, points] of fractures.entries()) {
    for (let segment = 1; segment < points.length; segment++) {
      const trail = points.slice(segment - 1, segment + 1);
      const taper = 1 - (segment - 1) / points.length;
      stroke(frame, trail, 6 * taper, [5, 2, 12], 135 * taper);
      stroke(sigils, trail, 3 * taper, [89, 45, 144], 35 * taper);
      stroke(sigils, trail, 0.9 * taper, [164, 126, 213], 150 * taper);
    }
    const middle = points[2];
    const side = index % 2 === 0 ? -1 : 1;
    stroke(sigils, [middle, [middle[0] + side * 19, middle[1] + 11], [middle[0] + side * 27, middle[1] + 39]], 0.8, [116, 80, 184], 140);
  }
  for (const [centerX, centerY, mirror] of [[91, 285, 1], [931, 281, -1]]) {
    const point = (across, down) => [centerX + across * mirror, centerY + down];
    const eye = [point(-48, 0), point(-17, -21), point(4, -28), point(42, 0), point(9, 21), point(-19, 18), point(-48, 0)];
    stroke(sigils, eye, 6, [70, 40, 126], 45);
    stroke(sigils, eye, 1.2, [176, 137, 223], 160);
    stroke(sigils, [point(0, -17), point(9, 0), point(0, 17), point(-7, 0), point(0, -17)], 1.8, [192, 168, 231], 180);
    stroke(sigils, [point(-8, -37), point(7, -63), point(18, -39)], 1.1, [154, 115, 204], 155);
    stroke(sigils, [point(-8, 37), point(7, 65), point(18, 40)], 1.1, [154, 115, 204], 155);
    for (let rune = 0; rune < 5; rune++) {
      const distance = 82 + rune * 26;
      const offset = Math.sin(rune * 1.7) * 14;
      for (const side of [-1, 1]) {
        stroke(sigils, [point(offset - 8, distance * side), point(offset, (distance - 8) * side),
          point(offset + 8, distance * side), point(offset, (distance + 8) * side)], 0.9, [121, 87, 186], 140 - rune * 16);
      }
    }
  }
  for (const canvas of [frame, sigils]) {
    for (let pixelY = 0; pixelY < HEIGHT; pixelY++) for (let pixelX = 0; pixelX < WIDTH; pixelX++) {
      const horizontal = Math.abs(pixelX / (WIDTH - 1) - 0.5) * 2;
      const vertical = Math.abs(pixelY / (HEIGHT - 1) - 0.5) * 2;
      const mask = smooth((Math.max(horizontal / 0.52, vertical / 0.48) - 1) / 0.28);
      canvas.buf[(pixelY * WIDTH + pixelX) * 4 + 3] = Math.round(canvas.buf[(pixelY * WIDTH + pixelX) * 4 + 3] * mask);
    }
  }
  const destination = path.join(assets, 'textures/gui/overlays');
  fs.mkdirSync(destination, { recursive: true });
  frame.save(path.join(destination, 'zheergen_dark_frame.png'));
  sigils.save(path.join(destination, 'zheergen_dark_sigils.png'));
  console.log('Generated Zheergen darkness: two transparent 1024x576 textures, protected center');
}

if (require.main === module) {
  if (!process.argv[2]) throw new Error('Provide an explicit staging assets directory');
  generate(path.resolve(process.argv[2]));
}
module.exports = { generate, WIDTH, HEIGHT };
