const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { Canvas, encodePNG } = require('./png.cjs');
const { specs } = require('./boss_materials/spec.cjs');
const { modelFor } = require('./boss_materials/models.cjs');

const destination = path.resolve(process.argv[2] || '');
const editor = process.argv[3] ? path.resolve(process.argv[3]) : null;
if (!process.argv[2]) throw new Error('Pass an explicit staging assets/ftboss directory');
const rgba = hex => [1, 3, 5].map(offset => parseInt(hex.slice(offset, offset + 2), 16));
function write(relative, data) {
  const filename = path.join(destination, relative);
  fs.mkdirSync(path.dirname(filename), { recursive: true });
  if (fs.existsSync(filename)) fs.chmodSync(filename, 0o666);
  fs.writeFileSync(filename, data);
}
const json = object => JSON.stringify(object, null, 2) + '\n';
for (const spec of specs) {
  const icon = new Canvas(16, 16);
  if (spec.pixels.length !== 16) throw new Error(spec.id + ': expected 16 rows');
  spec.pixels.forEach((row, vertical) => {
    if (row.length !== 16) throw new Error(spec.id + ': malformed row ' + vertical + ' (' + row.length + ')');
    [...row].forEach((pixel, horizontal) => {
      if (pixel === '.') return;
      if (!spec.palette[pixel]) throw new Error(spec.id + ': undefined palette ' + pixel);
      icon.set(horizontal, vertical, ...rgba(spec.palette[pixel]));
    });
  });
  const atlas = new Canvas(64, 64);
  Object.values(spec.palette).forEach((hex, index) => {
    const base = rgba(hex);
    for (let vertical = 0; vertical < 16; vertical++) for (let horizontal = 0; horizontal < 16; horizontal++) {
      const variation = vertical === 0 ? 5 : (horizontal * 13 + vertical * 7 + index * 3) % 41 === 0 ? -5 : 0;
      atlas.set(index % 4 * 16 + horizontal, Math.floor(index / 4) * 16 + vertical,
        ...base.map(channel => Math.max(0, Math.min(255, channel + variation))));
    }
  });
  const item = spec.id + '_material';
  const model = modelFor(spec);
  const atlasBytes = encodePNG(64, 64, atlas.d);
  write('textures/item/' + item + '.png', encodePNG(16, 16, icon.d));
  write('textures/item/boss_materials/' + spec.id + '.png', atlasBytes);
  write('models/item/boss_materials/' + item + '.json', json(model));
  write('models/item/boss_materials/' + item + '_gui.json', json({
    parent: 'minecraft:item/generated', textures: { layer0: 'ftboss:item/' + item }
  }));
  write('models/item/' + item + '.json', json({
    loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front',
    textures: { particle: 'ftboss:item/' + item },
    base: { parent: 'ftboss:item/boss_materials/' + item },
    perspectives: { gui: { parent: 'ftboss:item/boss_materials/' + item + '_gui' } }
  }));
  if (editor) {
    fs.mkdirSync(editor, { recursive: true });
    const cubes = model.elements.map(element => ({
      name: element.name, type: 'cube', uuid: crypto.randomUUID(), from: element.from, to: element.to,
      origin: [8, 8, 8], rotation: [0, 0, 0], rescale: false, autouv: 0, color: 0,
      faces: Object.fromEntries(Object.entries(element.faces).map(([direction, face]) =>
        [direction, { uv: face.uv.map(coordinate => coordinate * 4), texture: 0 }]))
    }));
    const document = {
      meta: { format_version: '4.10', model_format: 'java_block', box_uv: false },
      name: spec.title, model_identifier: item, visible_box: [1, 1, 0], resolution: { width: 64, height: 64 },
      elements: cubes, outliner: cubes.map(cube => cube.uuid), display: model.display,
      textures: [{ name: spec.id + '.png', id: '0', uuid: crypto.randomUUID(), mode: 'bitmap',
        source: 'data:image/png;base64,' + atlasBytes.toString('base64'),
        path: '', width: 64, height: 64, uv_width: 64, uv_height: 64, particle: true }]
    };
    fs.writeFileSync(path.join(editor, item + '.bbmodel'), json(document));
  }
}
console.log(JSON.stringify({ materials: specs.length, files: specs.length * 5, destination, editor }));
