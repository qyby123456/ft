const fs = require('node:fs');
const path = require('node:path');
const { makeCanvas } = require('./lib_itemtex.cjs');

const WIDTH = 1024;
const HEIGHT = 576;
const FRAME_COUNT = 12;
const FRAME_WIDTH = 512;
const FRAME_HEIGHT = 288;
const ATLAS_COLUMNS = 4;
const clamp = value => Math.max(0, Math.min(1, value));
const smooth = value => { const amount = clamp(value); return amount * amount * (3 - 2 * amount); };
const bump = (position, center, spread) => Math.exp(-(((position - center) / spread) ** 2));
const hash = (column, row) => {
  const value = Math.sin(column * 127.1 + row * 311.7 + 91.17) * 43758.5453;
  return value - Math.floor(value);
};
function noise(horizontal, vertical) {
  const column = Math.floor(horizontal);
  const row = Math.floor(vertical);
  const blendX = smooth(horizontal - column);
  const blendY = smooth(vertical - row);
  const upper = hash(column, row) * (1 - blendX) + hash(column + 1, row) * blendX;
  const lower = hash(column, row + 1) * (1 - blendX) + hash(column + 1, row + 1) * blendX;
  return upper * (1 - blendY) + lower * blendY;
}

function generate(assets) {
  const frame = makeCanvas(WIDTH, HEIGHT);
  const veins = makeCanvas(WIDTH, HEIGHT);
  for (let pixelY = 0; pixelY < HEIGHT; pixelY++) {
    for (let pixelX = 0; pixelX < WIDTH; pixelX++) {
      const horizontal = pixelX / (WIDTH - 1);
      const vertical = pixelY / (HEIGHT - 1);
      const top = 0.065 + 0.09 * bump(horizontal, 0.13, 0.105) + 0.12 * bump(horizontal, 0.76, 0.13)
        + 0.012 * Math.sin(horizontal * 68) + 0.009 * Math.sin(horizontal * 131);
      const bottom = 0.055 + 0.085 * bump(horizontal, 0.23, 0.12) + 0.095 * bump(horizontal, 0.88, 0.12)
        + 0.012 * Math.sin(horizontal * 79 + 1.8);
      const left = 0.045 + 0.07 * bump(vertical, 0.24, 0.21) + 0.075 * bump(vertical, 0.79, 0.18)
        + 0.008 * Math.sin(vertical * 49);
      const right = 0.05 + 0.07 * bump(vertical, 0.13, 0.18) + 0.055 * bump(vertical, 0.68, 0.2)
        + 0.009 * Math.sin(vertical * 57 + 2);
      const depth = Math.max((top - vertical) * HEIGHT, (bottom - 1 + vertical) * HEIGHT,
        (left - horizontal) * WIDTH, (right - 1 + horizontal) * WIDTH);
      if (depth < -12) continue;
      const broad = noise(pixelX / 42, pixelY / 35);
      const tissue = noise(pixelX / 9, pixelY / 12);
      const grain = hash(pixelX, pixelY) - 0.5;
      const folds = Math.sin(depth * 0.22 + broad * 5 + tissue * 0.75);
      const crease = Math.pow(Math.max(0, -folds), 6);
      const lip = Math.exp(-(((depth - 3.5) / 3.4) ** 2));
      const fibers = Math.sin(pixelX * 0.18 + pixelY * 0.32 + broad * 12) * 3;
      const light = 16 * folds + 25 * broad + 12 * tissue - 29 * crease + fibers + grain * 8;
      const shade = Math.min(1, Math.max(0, depth) / 80);
      frame.put(pixelX, pixelY, [102 + light + 49 * lip - 35 * shade,
        29 + light * 0.34 + 38 * lip - 14 * shade, 46 + light * 0.42 + 33 * lip - 15 * shade],
      smooth((depth + 12) / 15) * (242 + 13 * shade));
    }
  }

  function cord(points, thickness) {
    for (let step = 0; step <= 360; step++) {
      const progress = step / 360;
      const inverse = 1 - progress;
      const horizontal = (inverse ** 3 * points[0][0] + 3 * inverse ** 2 * progress * points[1][0]
        + 3 * inverse * progress ** 2 * points[2][0] + progress ** 3 * points[3][0]) * WIDTH;
      const vertical = (inverse ** 3 * points[0][1] + 3 * inverse ** 2 * progress * points[1][1]
        + 3 * inverse * progress ** 2 * points[2][1] + progress ** 3 * points[3][1]) * HEIGHT;
      const radius = thickness * (0.09 + inverse ** 0.85);
      for (let pixelY = Math.floor(vertical - radius - 1); pixelY <= vertical + radius + 1; pixelY++) {
        for (let pixelX = Math.floor(horizontal - radius - 1); pixelX <= horizontal + radius + 1; pixelX++) {
          const distance = Math.hypot(pixelX - horizontal, pixelY - vertical) / radius;
          if (distance >= 1) continue;
          const light = Math.sqrt(1 - distance * distance);
          const ridges = Math.sin(progress * 145) * 5;
          frame.put(pixelX, pixelY, [66 + 115 * light + ridges, 17 + 52 * light, 35 + 53 * light],
            smooth((1 - distance) * radius) * 255);
        }
      }
    }
  }
  cord([[0.04, 0.04], [0.16, 0.11], [0.16, 0.23], [0.21, 0.24]], 15);
  cord([[0.72, -0.02], [0.7, 0.12], [0.64, 0.11], [0.62, 0.225]], 11);
  cord([[0.995, 0.37], [0.85, 0.39], [0.96, 0.53], [0.835, 0.59]], 16);
  cord([[0.08, 1.01], [0.22, 0.98], [0.15, 0.79], [0.24, 0.795]], 14);
  cord([[0.93, 1.01], [0.82, 0.92], [0.81, 0.83], [0.765, 0.81]], 17);

  function vessel(startX, startY, angle, length, radius, seed, generation) {
    let previousX = startX;
    let previousY = startY;
    const steps = Math.ceil(length * 1.5);
    for (let step = 1; step <= steps; step++) {
      const progress = step / steps;
      const bend = Math.sin(progress * 5.6 + seed) * length * 0.075 * progress;
      const horizontal = startX + Math.cos(angle) * length * progress - Math.sin(angle) * bend;
      const vertical = startY + Math.sin(angle) * length * progress + Math.cos(angle) * bend;
      const width = radius * (1 - progress * 0.75);
      for (let pixelY = Math.floor(vertical - width - 2); pixelY <= vertical + width + 2; pixelY++) {
        for (let pixelX = Math.floor(horizontal - width - 2); pixelX <= horizontal + width + 2; pixelX++) {
          if (pixelX < 0 || pixelY < 0 || pixelX >= WIDTH || pixelY >= HEIGHT) continue;
          const mask = frame.alphaAt(pixelX, pixelY) / 255;
          if (mask < 0.3) continue;
          const distance = Math.hypot(pixelX - horizontal, pixelY - vertical);
          const groove = clamp(width + 0.6 - distance) * mask;
          frame.put(pixelX, pixelY, [48, 9, 31], groove * 130);
          const rim = clamp(1 - Math.abs(distance - width - 0.5)) * mask;
          frame.put(pixelX, pixelY, [185, 78, 91], rim * 32);
          veins.put(pixelX, pixelY, [206, 46, 72], clamp(width * 0.6 + 0.4 - distance) * mask * 150);
        }
      }
      if (generation > 0 && (step === Math.floor(steps * 0.43) || step === Math.floor(steps * 0.7))) {
        const side = step < steps * 0.5 ? -1 : 1;
        const tangent = Math.atan2(vertical - previousY, horizontal - previousX);
        vessel(horizontal, vertical, tangent + side * (0.5 + hash(seed, step) * 0.6),
          length * 0.38, radius * 0.56, seed + step, generation - 1);
      }
      previousX = horizontal;
      previousY = vertical;
    }
  }
  for (let index = 0; index < 13; index++) {
    const horizontal = (index + 0.35 + hash(index, 17) * 0.4) / 13 * WIDTH;
    vessel(horizontal, 0, Math.PI * (0.36 + hash(index, 1) * 0.28), 90 + hash(index, 2) * 85, 2.1, index + 1, 2);
    vessel(WIDTH - horizontal, HEIGHT - 1, -Math.PI * (0.35 + hash(index, 3) * 0.3),
      75 + hash(index, 4) * 75, 1.9, index + 21, 2);
  }
  for (let index = 0; index < 7; index++) {
    const vertical = (index + 0.45) / 7 * HEIGHT;
    vessel(0, vertical, hash(index, 7) - 0.5, 135 + hash(index, 9) * 65, 2.4, index + 41, 2);
    vessel(WIDTH - 1, HEIGHT - vertical, Math.PI + hash(index, 8) - 0.5,
      120 + hash(index, 10) * 70, 2.2, index + 61, 2);
  }
  const directory = path.join(assets, 'textures/gui/overlays');
  fs.mkdirSync(directory, { recursive: true });
  frame.save(path.join(directory, 'saya_flesh_frame.png'));
  veins.save(path.join(directory, 'saya_flesh_veins.png'));
  const animation = generateAnimation(frame, veins, directory);
  console.log(`Saya flesh vision: ${WIDTH}x${HEIGHT}, separate tissue and capillary layers -> ${directory}`);
  return { frame, veins, ...animation };
}

function generateAnimation(frame, veins, directory) {
  const atlasWidth = FRAME_WIDTH * ATLAS_COLUMNS;
  const atlasHeight = FRAME_HEIGHT * Math.ceil(FRAME_COUNT / ATLAS_COLUMNS);
  const fleshAtlas = makeCanvas(atlasWidth, atlasHeight);
  const veinAtlas = makeCanvas(atlasWidth, atlasHeight);
  for (let frameIndex = 0; frameIndex < FRAME_COUNT; frameIndex++) {
    const phase = frameIndex / FRAME_COUNT * Math.PI * 2;
    const originX = frameIndex % ATLAS_COLUMNS * FRAME_WIDTH;
    const originY = Math.floor(frameIndex / ATLAS_COLUMNS) * FRAME_HEIGHT;
    for (let pixelY = 0; pixelY < FRAME_HEIGHT; pixelY++) {
      const vertical = pixelY / (FRAME_HEIGHT - 1);
      for (let pixelX = 0; pixelX < FRAME_WIDTH; pixelX++) {
        const horizontal = pixelX / (FRAME_WIDTH - 1);
        if (horizontal >= 0.25 && horizontal <= 0.75 && vertical >= 0.28 && vertical <= 0.72) continue;
        const waveX = Math.sin(Math.PI * horizontal);
        const waveY = Math.sin(Math.PI * vertical);
        const sourceX = clamp(horizontal + 0.012 * Math.sin(phase + vertical * 9) * waveX
          + 0.008 * Math.sin(phase) * (horizontal - 0.5) * waveX) * (WIDTH - 1);
        const sourceY = clamp(vertical + 0.016 * Math.sin(phase - horizontal * 10) * waveY
          + 0.010 * Math.sin(phase) * (vertical - 0.5) * waveY) * (HEIGHT - 1);
        const lowerX = Math.floor(sourceX);
        const lowerY = Math.floor(sourceY);
        const upperX = Math.min(WIDTH - 1, lowerX + 1);
        const upperY = Math.min(HEIGHT - 1, lowerY + 1);
        const blendX = sourceX - lowerX;
        const blendY = sourceY - lowerY;
        const destination = ((originY + pixelY) * atlasWidth + originX + pixelX) * 4;
        for (const [source, target] of [[frame.buf, fleshAtlas.buf], [veins.buf, veinAtlas.buf]]) {
          for (let channel = 0; channel < 4; channel++) {
            const upper = source[(lowerY * WIDTH + lowerX) * 4 + channel] * (1 - blendX)
              + source[(lowerY * WIDTH + upperX) * 4 + channel] * blendX;
            const lower = source[(upperY * WIDTH + lowerX) * 4 + channel] * (1 - blendX)
              + source[(upperY * WIDTH + upperX) * 4 + channel] * blendX;
            target[destination + channel] = Math.round(upper * (1 - blendY) + lower * blendY);
          }
        }
      }
    }
  }
  fleshAtlas.save(path.join(directory, 'saya_flesh_dynamic.png'));
  veinAtlas.save(path.join(directory, 'saya_veins_dynamic.png'));
  return { fleshAtlas, veinAtlas };
}

if (require.main === module) {
  generate(path.resolve(process.argv[2] || path.join(__dirname, '../_archive/out/saya_flesh_vision/assets/ftboss')));
}
module.exports = { generate, WIDTH, HEIGHT, FRAME_COUNT, FRAME_WIDTH, FRAME_HEIGHT, ATLAS_COLUMNS };
