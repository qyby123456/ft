const fs = require('node:fs');
const path = require('node:path');
const { Canvas } = require('../model3d/png.cjs');
const root = path.resolve(__dirname, '../..');
const canvas = new Canvas(18, 18);
const gold = [245, 190, 69, 1];
const light = [255, 243, 166, 1];
const dark = [74, 43, 25, 1];
canvas.rect(2, 2, 14, 14, ...dark);
canvas.line(3, 2, 14, 2, ...gold);
canvas.line(3, 15, 14, 15, ...gold);
canvas.line(2, 3, 2, 14, ...gold);
canvas.line(15, 3, 15, 14, ...gold);
const pulse = [[3, 10], [6, 10], [8, 5], [10, 13], [12, 8], [14, 8]];
for (let index = 1; index < pulse.length; index++) {
  canvas.line(...pulse[index - 1], ...pulse[index], ...light);
}
canvas.rect(4, 4, 2, 3, ...gold);
canvas.rect(12, 11, 2, 3, ...gold);
canvas.save(path.join(root, 'src/main/resources/assets/ftboss/textures/mob_effect/lethal_tempo.png'));
const preview = path.join(root, 'build/dora-tempo');
fs.mkdirSync(preview, { recursive: true });
canvas.scaled(12).save(path.join(preview, 'lethal-tempo-preview.png'));
console.log('Generated Lethal Tempo icon: 18x18');
