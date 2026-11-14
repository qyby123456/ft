const fs = require('node:fs');
const path = require('node:path');
const { summons, discs, effects } = require('./spec.cjs');
const { drawSummon, drawDisc } = require('./icons.cjs');
const { drawEffect } = require('./effects.cjs');
const { modelFor, makeAtlas } = require('./models.cjs');
const project = path.resolve(__dirname, '../../..');
function expectedAssets(groups = ['summons', 'discs', 'effects']) {
  const output = new Map();
  const add = (relative, value) => output.set(`assets/ftboss/${relative}`, Buffer.isBuffer(value) ? value : Buffer.from(JSON.stringify(value, null, 2) + '\n'));
  for (const group of groups) if (!['summons', 'discs', 'effects'].includes(group)) throw new Error(`Unknown group: ${group}`);
  if (groups.includes('summons')) {
    add('textures/item/summons_redraw/atlas.png', makeAtlas().buffer());
    for (const spec of summons) {
      add(`textures/item/${spec.id}.png`, drawSummon(spec).buffer());
      add(`models/item/summons_redraw/${spec.id}.json`, modelFor(spec));
      add(`models/item/summons_redraw/${spec.id}_gui.json`, {
        ambientocclusion: false, gui_light: 'front', textures: { icon: `ftboss:item/${spec.id}`, particle: `ftboss:item/${spec.id}` },
        elements: [{ from: [0, 0, 8], to: [16, 16, 8], shade: false, faces: {
          south: { uv: [0, 0, 16, 16], texture: '#icon' }, north: { uv: [16, 0, 0, 16], texture: '#icon' }
        } }], display: { gui: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] } }
      });
      add(`models/item/${spec.id}.json`, {
        loader: 'forge:separate_transforms', ambientocclusion: false, gui_light: 'front',
        textures: { particle: `ftboss:item/${spec.id}` }, base: { parent: `ftboss:item/summons_redraw/${spec.id}` },
        perspectives: { gui: { parent: `ftboss:item/summons_redraw/${spec.id}_gui` } }
      });
    }
  }
  if (groups.includes('discs')) for (const spec of discs) {
    add(`textures/item/${spec.id}.png`, drawDisc(spec).buffer());
    add(`models/item/${spec.id}.json`, { parent: 'minecraft:item/generated', textures: { layer0: `ftboss:item/${spec.id}` } });
  }
  if (groups.includes('effects')) for (const spec of effects) add(`textures/mob_effect/${spec.id}.png`, drawEffect(spec).buffer());
  return output;
}
function generate(groups, destination = path.join(project, 'src/main/resources')) {
  const output = expectedAssets(groups);
  for (const [relative, bytes] of output) {
    const filename = path.join(destination, relative);
    fs.mkdirSync(path.dirname(filename), { recursive: true }); fs.writeFileSync(filename, bytes);
  }
  console.log(`FTBoss summon/disc/effect assets: ${output.size} files -> ${destination}`);
  return output;
}
if (require.main === module) {
  const args = process.argv.slice(2);
  const groups = args.filter(argument => argument.startsWith('--group=')).map(argument => argument.slice(8));
  const destination = args.find(argument => !argument.startsWith('--'));
  generate(groups.length ? groups : undefined, destination ? path.resolve(destination) : undefined);
}
module.exports = { expectedAssets, generate };
