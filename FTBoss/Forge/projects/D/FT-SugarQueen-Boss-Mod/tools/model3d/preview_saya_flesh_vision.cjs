const fs = require('node:fs');
const path = require('node:path');
const { decodePNG, encodePNG } = require('./png.cjs');

const assets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const frame = decodePNG(path.join(assets, 'textures/gui/overlays/saya_flesh_frame.png'));
const veins = decodePNG(path.join(assets, 'textures/gui/overlays/saya_flesh_veins.png'));
if (!process.argv[2] || !process.argv[3]) throw new Error('Usage: node preview_saya_flesh_vision.cjs screenshot.png output-directory');
const background = decodePNG(process.argv[2]);
const output = path.resolve(process.argv[3]);
fs.mkdirSync(output, { recursive: true });

function preview(width, height, heartbeat, destination, lightBackground = false) {
  const pixels = Buffer.alloc(width * height * 4);
  for (let pixelY = 0; pixelY < height; pixelY++) {
    for (let pixelX = 0; pixelX < width; pixelX++) {
      const offset = (pixelY * width + pixelX) * 4;
      const source = (Math.floor(pixelY / height * background.h) * background.w + Math.floor(pixelX / width * background.w)) * 4;
      const texture = (Math.floor(pixelY / height * frame.h) * frame.w + Math.floor(pixelX / width * frame.w)) * 4;
      const checker = (Math.floor(pixelX / 32) + Math.floor(pixelY / 32)) % 2;
      for (let channel = 0; channel < 3; channel++) {
        let color = lightBackground ? 162 + checker * 25 : background.data[source + channel];
        color = color * (1 - 12 / 255) + [104, 22, 38][channel] * 12 / 255;
        const fleshAlpha = frame.data[texture + 3] / 255 * (0.93 + 0.05 * heartbeat);
        color = color * (1 - fleshAlpha) + frame.data[texture + channel] * fleshAlpha;
        const veinAlpha = veins.data[texture + 3] / 255 * (0.48 + 0.24 * heartbeat);
        pixels[offset + channel] = Math.round(color * (1 - veinAlpha) + veins.data[texture + channel] * veinAlpha);
      }
      pixels[offset + 3] = 255;
    }
  }
  fs.writeFileSync(path.join(output, destination), encodePNG(width, height, pixels));
}
preview(1280, 720, 0.6, 'saya-flesh-composite.png');
preview(960, 720, 0.6, 'saya-flesh-4x3.png');
preview(1680, 720, 0.6, 'saya-flesh-21x9.png');
preview(1280, 720, 0.6, 'saya-flesh-light.png', true);
console.log(`Offline composites (not in-game captures): ${output}`);
