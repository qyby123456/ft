const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../..');
const parts = ['gun_body', 'gun_stock', 'gun_upper', 'gun_lower', 'gun_core', 'wheel_hub', 'wheel_upper', 'wheel_lower', 'wheel_locks'];
function classify(name, white) {
  if (white) {
    if (name.startsWith('上月')) return 'wheel_upper';
    if (name.startsWith('下月')) return 'wheel_lower';
    return name.includes('锁扣') ? 'wheel_locks' : 'wheel_hub';
  }
  if (name.includes('上枪轨') || name.includes('上轨翠甲')) return 'gun_upper';
  if (name.includes('下枪轨') || name.includes('下轨翠甲')) return 'gun_lower';
  if (name.includes('枪托')) return 'gun_stock';
  if (name.includes('能量轨') || name.includes('枪膛') || name.includes('枪口')) return 'gun_core';
  return 'gun_body';
}
function generate(output) {
  assert(output, 'Generate to a staging directory, not directly over approved resources');
  const report = [];
  for (const [identifier, white] of [['xuanlan_calibrum_v2', false], ['xuanlan_crescendum_v2', true]]) {
    const model = JSON.parse(fs.readFileSync(path.join(root, `src/main/resources/assets/ftboss/models/item/${identifier}.json`), 'utf8'));
    for (const part of parts.filter(name => name.startsWith(white ? 'wheel_' : 'gun_'))) {
      const elements = model.elements.filter(element => classify(element.name, white) === part);
      assert(elements.length > 0, part);
      const target = path.join(output, 'models/item/xuanlan_rig', part + '.json');
      fs.mkdirSync(path.dirname(target), { recursive: true });
      fs.writeFileSync(target, JSON.stringify({ ambientocclusion: false, textures: model.textures, elements }, null, 2) + '\n');
      report.push({ part, cubes: elements.length });
    }
  }
  return report;
}
if (require.main === module) console.log(generate(process.argv[2]));
module.exports = { classify, generate, parts };
