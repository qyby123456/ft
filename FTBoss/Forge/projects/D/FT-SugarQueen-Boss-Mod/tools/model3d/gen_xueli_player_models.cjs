const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { decodePNG, encodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const output = path.resolve(process.argv[2] || assets);
function write(relative, bytes) {
  const target = path.join(output, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, bytes);
}
function json(relative, value) { write(relative, JSON.stringify(value, null, 2) + '\n'); }
const report = [];
for (const [item, identifier] of [['skillet', 'xueli_skillet_v1'], ['rolling_pin', 'xueli_rolling_pin_v1']]) {
  const model = JSON.parse(fs.readFileSync(path.join(assets, `models/item/xueli/${item}.json`), 'utf8'));
  const source = decodePNG(path.join(assets, `textures/item/${identifier}.png`));
  const mask = Buffer.alloc(source.w * source.h * 4);
  const selected = model.elements.filter(element => /蝴蝶结|玫瑰花|玫纹菱花|缝线|银齿|铜纹章框/.test(element.name));
  assert(selected.length > 0);
  for (const element of selected) {
    for (const face of Object.values(element.faces)) {
      const [left, top, right, bottom] = face.uv.map(value => Math.round(value * source.w / 16));
      for (let vertical = top; vertical < bottom; vertical++) for (let horizontal = left; horizontal < right; horizontal++) {
        const offset = (vertical * source.w + horizontal) * 4;
        const gold = /银齿|铜纹章框|花心|结扣/.test(element.name);
        const brightness = source.data[offset] / 255;
        mask.set(gold ? [255, 207, 120, Math.round(110 + brightness * 85)]
          : [255, 130, 186, Math.round(110 + brightness * 85)], offset);
      }
    }
  }
  const glowElements = selected.map(element => ({
    ...element,
    from: element.from.map(value => Math.round((value - 0.014) * 100000) / 100000),
    to: element.to.map(value => Math.round((value + 0.014) * 100000) / 100000)
  }));
  const glow = {
    ambientocclusion: false, gui_light: 'front',
    textures: { atlas: `ftboss:item/${identifier}_glow`, particle: model.textures.particle },
    elements: glowElements
  };
  json(`models/item/xueli/${item}_glow.json`, glow);
  json(`models/item/xueli_${item}.json`, {
    parent: 'builtin/entity', gui_light: 'front', textures: { particle: model.textures.particle }, display: model.display
  });
  write(`textures/item/${identifier}_glow.png`, encodePNG(source.w, source.h, mask));
  report.push({ item: `ftboss:xueli_${item}`, glowElements: glowElements.length, renderer: 'BEWLR / full-bright translucent emissive' });
}
console.log(JSON.stringify({ output, models: report }, null, 2));
