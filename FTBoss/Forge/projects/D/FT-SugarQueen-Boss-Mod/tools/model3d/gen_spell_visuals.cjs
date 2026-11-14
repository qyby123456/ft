const fs = require('node:fs');
const path = require('node:path');
const { encodePNG, decodePNG } = require('./png.cjs');

const root = path.resolve(__dirname, '../..');
const definitions = fs.readFileSync(path.join(root, 'src/main/java/net/farmingtales/ftboss/util/SpellVisualRules.java'), 'utf8');
const spells = [...definitions.matchAll(/\w+\("([a-z_]+)", Form\.([A-Z_]+), 0x([0-9A-F]+), ([\d.]+), (\d+), (true|false)\)/g)]
  .map(match => ({ id: match[1], form: match[2], color: parseInt(match[3], 16), radius: Number(match[4]) }));
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const columns = Number(definitions.match(/ATLAS_COLUMNS = (\d+)/)[1]);
const atlasSize = columns * 128;
const solidTile = columns * columns - 1;

function generate(destination) {
  if (!spells.length || spells.length > solidTile || new Set(spells.map(spell => spell.id)).size !== spells.length) throw new Error('Profiles must be unique and fit before the reserved solid tile');
  fs.mkdirSync(destination, { recursive: true });
  const atlas = new Uint8Array(atlasSize * atlasSize * 4);
  const sheetHeight = Math.ceil(spells.length / 6) * 128;
  const sheet = new Uint8Array(768 * sheetHeight * 4);
  spells.forEach((spell, index) => {
    const icon = decodePNG(path.join(assets, 'textures/gui/spell_icons', spell.id + '.png'));
    const image = new Uint8Array(128 * 128 * 4);
    const tint = [spell.color >> 16 & 255, spell.color >> 8 & 255, spell.color & 255];
    const teeth = 8 + index % 5 * 2;
    for (let pixelY = 0; pixelY < 128; pixelY++) for (let pixelX = 0; pixelX < 128; pixelX++) {
      const across = pixelX - 63.5;
      const depth = pixelY - 63.5;
      const radius = Math.hypot(across, depth);
      const angle = Math.atan2(depth, across);
      const stroke = distance => Math.max(0, Math.min(1, 1.5 - Math.abs(distance)));
      let alpha = Math.max(stroke(radius - 56), stroke(radius - 47) * 0.6,
        stroke(radius - 51.5) * (Math.cos(angle * teeth) > 0.45 ? 0.8 : 0),
        stroke(radius - (39 + Math.sin(angle * (3 + index % 4)) * 3)) * 0.45);
      if (radius > 47 && radius < 57 && Math.abs(Math.sin(angle * teeth / 2)) < 0.04) alpha = 1;
      let channels = tint;
      if (radius < 31) {
        const sourceX = Math.max(0, Math.min(icon.w - 1, Math.round(32 + across * 0.7)));
        const sourceY = Math.max(0, Math.min(icon.h - 1, Math.round(32 + depth * 0.7)));
        const offset = (sourceY * icon.w + sourceX) * 4;
        const brightness = Math.max(icon.data[offset], icon.data[offset + 1], icon.data[offset + 2]);
        const mask = Math.max(0, Math.min(1, (brightness - 65) / 90)) * icon.data[offset + 3] / 255;
        if (mask > alpha) {
          alpha = mask;
          channels = [0, 1, 2].map(channel => Math.min(255, icon.data[offset + channel] * 1.15 + 15));
        }
      }
      const offset = (pixelY * 128 + pixelX) * 4;
      image.set([...channels.map(Math.round), Math.round(alpha * 230)], offset);
      const atlasOffset = ((Math.floor(index / columns) * 128 + pixelY) * atlasSize + index % columns * 128 + pixelX) * 4;
      atlas.set(image.subarray(offset, offset + 4), atlasOffset);
      const sheetOffset = ((Math.floor(index / 6) * 128 + pixelY) * 768 + index % 6 * 128 + pixelX) * 4;
      const blend = image[offset + 3] / 255;
      sheet.set([...channels.map(channel => Math.round(12 * (1 - blend) + channel * blend)), 255], sheetOffset);
    }
    fs.writeFileSync(path.join(destination, spell.id + '.png'), encodePNG(128, 128, image));
  });
  for (let pixelY = atlasSize - 96; pixelY < atlasSize - 32; pixelY++) for (let pixelX = atlasSize - 96; pixelX < atlasSize - 32; pixelX++) {
    atlas.set([255, 255, 255, 255], (pixelY * atlasSize + pixelX) * 4);
  }
  fs.writeFileSync(path.join(destination, 'spell_atlas.png'), encodePNG(atlasSize, atlasSize, atlas));
  fs.writeFileSync(path.join(destination, 'sigil-preview.png'), encodePNG(768, sheetHeight, sheet));
  console.log(`Generated ${spells.length} spell sigils and one shared ${atlasSize}x${atlasSize} runtime atlas`);
}

if (require.main === module) {
  if (!process.argv[2]) throw new Error('Provide an output directory; production installation is explicit');
  generate(path.resolve(process.argv[2]));
}
module.exports = { generate, spells };
