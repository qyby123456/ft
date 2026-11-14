const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { Canvas, decodePNG, encodePNG } = require('./png.cjs');

const assets = path.resolve(process.argv[2] || '');
const output = path.resolve(process.argv[3] || '');
const names = process.argv.slice(4);
if (!process.argv[2] || !process.argv[3] || !names.length) throw new Error('Pass assets/ftboss, output, and explicit generated item IDs');
const letters = {
  A: ['010','101','111','101','101'], B: ['110','101','110','101','110'], C: ['011','100','100','100','011'],
  D: ['110','101','101','101','110'], E: ['111','100','110','100','111'], F: ['111','100','110','100','100'],
  G: ['011','100','101','101','011'], H: ['101','101','111','101','101'], I: ['111','010','010','010','111'],
  K: ['101','101','110','101','101'], L: ['100','100','100','100','111'], N: ['101','111','111','111','101'], O: ['010','101','101','101','010'],
  P: ['110','101','110','100','100'], R: ['110','101','110','101','101'], S: ['011','100','010','001','110'],
  T: ['111','010','010','010','010'], U: ['101','101','101','101','111'], W: ['101','101','111','111','101'],
  X: ['101','101','010','101','101'], Y: ['101','101','010','010','010']
};
const label = (canvas, text, originX, originY) => {
  for (let index = 0; index < text.length; index++) {
    const letter = letters[text[index]];
    if (!letter) continue;
    for (let row = 0; row < 5; row++) for (let column = 0; column < 3; column++) {
      if (letter[row][column] === '1') canvas.rect(originX + index * 8 + column * 2, originY + row * 2, 2, 2, 210, 218, 235);
    }
  }
};
const hash = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const cell = 176;
const scale = 8;
const views = ['FRONT', 'BACK', 'LEFT', 'RIGHT'];
const report = [];
fs.mkdirSync(output, { recursive: true });
for (let group = 0; group * 4 < names.length; group++) {
  const selected = names.slice(group * 4, group * 4 + 4);
  const sheet = new Canvas(cell * 4, cell * selected.length);
  sheet.rect(0, 0, sheet.w, sheet.h, 24, 27, 37);
  for (let row = 0; row < selected.length; row++) {
    const name = selected[row];
    const modelPath = path.join(assets, 'models/item', name + '.json');
    const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
    if (model.parent !== 'minecraft:item/generated' || Object.keys(model.textures).length !== 1) throw new Error('Unsupported item geometry: ' + name);
    const texturePath = path.join(assets, 'textures', model.textures.layer0.replace(/^ftboss:/, '') + '.png');
    const texture = decodePNG(texturePath);
    if (texture.w !== 16 || texture.h !== 16) throw new Error('Expected final 16x16 sprite: ' + name);
    const sample = (pixelX, pixelY) => [...texture.data.slice((pixelY * texture.w + pixelX) * 4, (pixelY * texture.w + pixelX) * 4 + 4)];
    for (let view = 0; view < views.length; view++) {
      const originX = view * cell;
      const originY = row * cell;
      label(sheet, views[view], originX + 14, originY + 8);
      label(sheet, name.replace('_material', '').toUpperCase(), originX + 14, originY + 154);
      for (let pixelY = 0; pixelY < 16; pixelY++) {
        const opaque = Array.from({ length: 16 }, (_, pixelX) => pixelX).filter(pixelX => sample(pixelX, pixelY)[3] !== 0);
        if (!opaque.length) continue;
        if (view < 2) {
          for (const pixelX of opaque) {
            const rgba = sample(pixelX, pixelY);
            const screenX = view === 0 ? pixelX : 15 - pixelX;
            sheet.rect(originX + 24 + screenX * scale, originY + 22 + pixelY * scale, scale, scale, ...rgba.slice(0, 3), rgba[3] / 255);
          }
        } else {
          const rgba = sample(view === 2 ? opaque[opaque.length - 1] : opaque[0], pixelY);
          sheet.rect(originX + 84, originY + 22 + pixelY * scale, scale, scale, ...rgba.slice(0, 3), rgba[3] / 255);
        }
      }
    }
    report.push({ item: name, modelSha256: hash(modelPath), textureSha256: hash(texturePath), directions: views, generatedThicknessModelUnits: 1 });
  }
  fs.writeFileSync(path.join(output, 'boss_materials_4view_' + (group + 1) + '.png'), encodePNG(sheet.w, sheet.h, sheet.d));
}
fs.writeFileSync(path.join(output, 'generated-item-four-view-manifest.json'), JSON.stringify({ assets, items: report }, null, 2) + '\n');
console.log(JSON.stringify({ items: report.length, viewsPerItem: views.length, output }));
