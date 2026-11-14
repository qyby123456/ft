const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { Canvas, decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const output = path.resolve(process.argv[2]);
fs.mkdirSync(output, { recursive: true });
const base = JSON.parse(fs.readFileSync(path.join(assets, 'geo/tide_fish_boss.geo.json'), 'utf8'));
const animations = JSON.parse(fs.readFileSync(path.join(assets, 'animations/tide_fish_boss.animation.json'), 'utf8')).animations;
const releases = { sweep: 16, volley: 20, dash: 20, summon: 26, wave: 24, maelstrom: 36, geyser: 28 };
const sheet = new Canvas(2100, 600);
const indexFile = [];
let column = 0;
for (const [name, tick] of Object.entries(releases)) {
  for (const phase of [0, 1]) {
    const id = name + (phase ? '_p2' : '');
    const geometry = structuredClone(base);
    const bones = geometry['minecraft:geometry'][0].bones;
    for (const [boneName, channels] of Object.entries(animations['animation.tide_fish.' + id].bones)) {
      const bone = bones.find(candidate => candidate.name === boneName);
      for (const [channel, values] of Object.entries(channels)) {
        const keys = Object.keys(values).sort((left, right) => Math.abs(Number(left) - tick / 20) - Math.abs(Number(right) - tick / 20));
        bone[channel] = values[keys[0]];
      }
    }
    const geoFile = path.join(output, id + '.geo.json');
    const imageFile = path.join(output, id + '.png');
    fs.writeFileSync(geoFile, JSON.stringify(geometry));
    const result = spawnSync(process.execPath, [path.join(__dirname, 'render_geo.cjs'), geoFile,
      path.join(assets, 'textures/entity/tide_fish_boss' + (phase ? '_p2' : '') + '.png'), imageFile, '35', '10', '6'], { encoding: 'utf8' });
    if (result.status !== 0) throw new Error(result.stderr || result.stdout);
    const image = decodePNG(imageFile);
    for (let pixelY = 0; pixelY < image.h; pixelY++) {
      const targetOffset = ((phase * 300 + pixelY) * sheet.w + column * 300) * 4;
      sheet.d.set(image.data.subarray(pixelY * image.w * 4, (pixelY + 1) * image.w * 4), targetOffset);
    }
    indexFile.push({ column: column + 1, row: phase + 1, animation: id, tick });
  }
  column++;
}
sheet.save(path.join(output, 'tide-actions.png'));
fs.writeFileSync(path.join(output, 'tide-actions.json'), JSON.stringify(indexFile, null, 2) + '\n');
console.log('Rendered fourteen skill impact poses:', path.join(output, 'tide-actions.png'));
