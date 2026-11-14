const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { writeSafe } = require('./lib_itemtex.cjs');
const artRoot = path.resolve(__dirname, '../../art/lemoncat_weapons');
const assetsRoot = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const sources = ['LemonCat_Citrus_Skillet.bbmodel', 'LemonCat_Paw_Spatula.bbmodel'];

for (const source of sources) {
  const editable = JSON.parse(fs.readFileSync(path.join(artRoot, source), 'utf8'));
  assert.equal(editable.meta.model_format, 'java_block');
  assert.equal(editable.textures.length, 1);
  const identifier = editable.model_identifier;
  assert(['skillet', 'kitchen_shovel'].includes(identifier));
  const { width, height } = editable.resolution;
  const model = {
    credit: `Lemon Cat / ${editable.name} / original voxel weapon model`,
    texture_size: [width, height], ambientocclusion: false, gui_light: 'front',
    textures: { atlas: `ftboss:item/lemoncat/${identifier}`, particle: `ftboss:item/lemoncat/${identifier}` },
    elements: editable.elements.map(element => {
      assert.equal(element.type, 'cube');
      const result = {
        name: element.name, from: element.from, to: element.to,
        faces: Object.fromEntries(Object.entries(element.faces).map(([side, face]) => {
          assert.equal(face.texture, 0);
          return [side, { uv: face.uv.map((value, index) => value * 16 / (index % 2 === 0 ? width : height)), texture: '#atlas' }];
        }))
      };
      const rotations = element.rotation.map((angle, axis) => ({ angle, axis })).filter(rotation => rotation.angle !== 0);
      assert(rotations.length <= 1);
      if (rotations.length) {
        const rotation = rotations[0];
        assert([-45, -22.5, 22.5, 45].includes(rotation.angle));
        result.rotation = { rescale: false, origin: element.origin, axis: ['x', 'y', 'z'][rotation.axis], angle: rotation.angle };
      }
      return result;
    }),
    display: editable.display
  };
  const texture = Buffer.from(editable.textures[0].source.split(',')[1], 'base64');
  assert.equal(texture.subarray(1, 4).toString(), 'PNG');
  const modelPath = path.join(assetsRoot, `models/item/lemoncat/${identifier}.json`);
  const texturePath = path.join(assetsRoot, `textures/item/lemoncat/${identifier}.png`);
  for (const file of [modelPath, texturePath]) fs.mkdirSync(path.dirname(file), { recursive: true });
  writeSafe(modelPath, JSON.stringify(model, null, 2) + '\n');
  writeSafe(texturePath, texture);
  console.log(`${identifier}: ${model.elements.length} cubes, ${width}x${height}, ${texture.length} texture bytes`);
}
