const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const parts = ['handle', 'head', 'blade_base', 'blade_mid', 'blade_tip', 'front_core', 'back_core', 'counter', 'pommel'];

function classify(element) {
  const name = element.name;
  if (/弧刃_|外刃脊_|内刃口_/.test(name)) {
    const horizontal = element.from[0];
    return horizontal < -7 ? 'blade_tip' : horizontal < 0 ? 'blade_mid' : 'blade_base';
  }
  if (name === '前烛白方芯') return 'front_core';
  if (name === '后烛白方芯') return 'back_core';
  if (name.startsWith('短尾')) return 'counter';
  if (name.startsWith('柄尾')) return 'pommel';
  if (/镰首|烛纹|铭纹/.test(name)) return 'head';
  return 'handle';
}

function buildRig(model) {
  const files = new Map();
  for (const part of parts) {
    const elements = model.elements.filter(element => classify(element) === part);
    assert(elements.length, part);
    files.set(`ftboss/models/item/dandan_rig/${part}.json`, JSON.stringify({
      ambientocclusion: false, textures: model.textures, elements
    }, null, 2) + '\n');
  }
  assert.equal([...files.values()].reduce((count, text) => count + JSON.parse(text).elements.length, 0), 111);
  return files;
}

if (require.main === module) {
  const root = path.resolve(__dirname, '../..');
  const output = process.argv[2];
  assert(output, 'Provide a staging assets directory');
  const model = JSON.parse(fs.readFileSync(path.join(root, 'src/main/resources/assets/ftboss/models/item/dandan_scythe.json')));
  for (const [relative, content] of buildRig(model)) {
    const target = path.join(output, relative);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    fs.writeFileSync(target, content);
  }
}
module.exports = { parts, classify, buildRig };
