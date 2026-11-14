const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const art = path.join(root, 'art/xueli_weapons');
const output = path.resolve(process.argv[2] || path.join(root, 'src/main/resources/assets/ftboss'));
const assets = [['xueli_skillet_v1', 'skillet', 86], ['xueli_rolling_pin_v1', 'rolling_pin', 46]];
const report = [];
function write(relative, bytes) {
  const target = path.join(output, relative);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, bytes);
}
for (const [identifier, item, count] of assets) {
  const editable = JSON.parse(fs.readFileSync(path.join(art, `${identifier}.bbmodel`), 'utf8'));
  assert.equal(editable.meta.model_format, 'java_block');
  assert.equal(editable.elements.length, count);
  const width = editable.resolution.width;
  const height = editable.resolution.height;
  assert.equal(width, 128);
  assert.equal(height, 128);
  const elements = editable.elements.map(element => {
    const result = { name: element.name, from: element.from, to: element.to, faces: {} };
    for (const [side, face] of Object.entries(element.faces)) {
      assert.equal(face.texture, 0);
      assert(face.uv.every(value => Number.isFinite(value) && value >= 0 && value <= 128));
      result.faces[side] = { uv: face.uv.map(value => value / 8), texture: '#atlas' };
    }
    assert.equal(Object.keys(result.faces).length, 6);
    const axes = element.rotation.map((angle, index) => ({ angle, axis: ['x', 'y', 'z'][index] })).filter(value => value.angle !== 0);
    assert(axes.length <= 1);
    if (axes.length) {
      assert([-45, -22.5, 22.5, 45].includes(axes[0].angle));
      result.rotation = { rescale: false, ...axes[0], origin: element.origin };
    }
    return result;
  });
  const model = {
    ambientocclusion: false, gui_light: 'front',
    textures: { atlas: `ftboss:item/${identifier}`, particle: `ftboss:item/${identifier}_particle` },
    elements, display: editable.display
  };
  const texture = Buffer.from(editable.textures[0].source.split(',')[1], 'base64');
  assert(texture.length > 100);
  assert.equal(texture.subarray(1, 4).toString('ascii'), 'PNG');
  assert.equal(texture.readUInt32BE(16), width);
  assert.equal(texture.readUInt32BE(20), height);
  const particle = fs.readFileSync(path.join(art, `${identifier}_particle.png`));
  assert.equal(particle.readUInt32BE(16), 16);
  assert.equal(particle.readUInt32BE(20), 16);
  write(`models/item/xueli/${item}.json`, JSON.stringify(model, null, 2) + '\n');
  write(`textures/item/${identifier}.png`, texture);
  write(`textures/item/${identifier}_particle.png`, particle);
  report.push({ model: `ftboss:item/xueli/${item}`, cubes: elements.length, faces: elements.length * 6, texture: [width, height] });
}
console.log(JSON.stringify({ output, assets: report }, null, 2));
