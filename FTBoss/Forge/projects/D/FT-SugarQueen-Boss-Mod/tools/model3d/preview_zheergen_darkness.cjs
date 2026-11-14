const fs = require('node:fs');
const path = require('node:path');
const { decodePNG, encodePNG } = require('./png.cjs');
const assets = path.resolve(__dirname, '../../src/main/resources/assets/ftboss');
const frame = decodePNG(path.join(assets, 'textures/gui/overlays/zheergen_dark_frame.png'));
const sigils = decodePNG(path.join(assets, 'textures/gui/overlays/zheergen_dark_sigils.png'));
if (!process.argv[2] || !process.argv[3]) throw new Error('Usage: screenshot.png output-directory');
const background = decodePNG(process.argv[2]);
const output = path.resolve(process.argv[3]);
fs.mkdirSync(output, { recursive: true });
function preview(width, height, destination, checker = false, strength = 1) {
  const pixels = Buffer.alloc(width * height * 4);
  for (let pixelY = 0; pixelY < height; pixelY++) for (let pixelX = 0; pixelX < width; pixelX++) {
    const offset = (pixelY * width + pixelX) * 4;
    const source = (Math.floor(pixelY / height * background.h) * background.w + Math.floor(pixelX / width * background.w)) * 4;
    const texture = (Math.floor(pixelY / height * frame.h) * frame.w + Math.floor(pixelX / width * frame.w)) * 4;
    const checkerColor = 145 + ((Math.floor(pixelX / 32) + Math.floor(pixelY / 32)) % 2) * 30;
    for (let channel = 0; channel < 3; channel++) {
      let color = checker ? checkerColor : background.data[source + channel];
      color = color * (1 - 64 / 255 * strength) + [9, 4, 17][channel] * 64 / 255 * strength;
      const frameAlpha = frame.data[texture + 3] / 255 * 0.9175 * strength;
      color = color * (1 - frameAlpha) + frame.data[texture + channel] * frameAlpha;
      const sigilAlpha = sigils.data[texture + 3] / 255 * 0.5 * strength;
      pixels[offset + channel] = Math.round(color * (1 - sigilAlpha) + sigils.data[texture + channel] * sigilAlpha);
    }
    pixels[offset + 3] = 255;
  }
  fs.writeFileSync(path.join(output, destination), encodePNG(width, height, pixels));
}
preview(1280, 720, 'zheergen-darkness-preview.png');
preview(960, 720, 'zheergen-darkness-4x3.png');
preview(1680, 720, 'zheergen-darkness-21x9.png');
preview(1280, 720, 'zheergen-darkness-checker.png', true);
preview(1280, 720, 'zheergen-darkness-half.png', false, 0.5);
console.log('Offline screen composites, not in-game screenshots: ' + output);
