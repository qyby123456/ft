const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { decodePNG } = require('./png.cjs');
const root = path.resolve(__dirname, '../..');
const resources = path.join(root, 'src/main/resources');
const assets = path.join(resources, 'assets/ftboss');
const java = path.join(root, 'src/main/java/net/farmingtales/ftboss');
const json = file => JSON.parse(fs.readFileSync(file, 'utf8'));
let checks = 0;
function check(condition, message) { assert(condition, message); checks++; }
function run(script, ...args) {
  const result = spawnSync(process.execPath, [path.join(__dirname, script), ...args], { encoding: 'utf8' });
  process.stdout.write(result.stdout);
  process.stderr.write(result.stderr);
  check(result.status === 0, `${script} failed`);
}
for (const name of ['tide_fish_boss', 'reef_fish', 'brine_puffer']) {
  const geoFile = path.join(assets, `geo/${name}.geo.json`);
  const textureFile = path.join(assets, `textures/entity/${name}.png`);
  const model = json(geoFile)['minecraft:geometry'][0];
  const texture = decodePNG(textureFile);
  check(model.description.texture_width === texture.w && model.description.texture_height === texture.h, `${name} texture size`);
  run('validate.cjs', geoFile);
  run('validate_anim.cjs', geoFile, path.join(assets,
    name === 'tide_fish_boss' ? 'animations/tide_fish_boss.animation.json' : 'animations/reef_minion.animation.json'));
  for (const bone of model.bones) for (const cube of bone.cubes || []) for (const mapping of Object.values(cube.uv)) {
    const [left, top] = mapping.uv;
    const [width, height] = mapping.uv_size;
    for (let pixelY = top; pixelY < top + height; pixelY++) for (let pixelX = left; pixelX < left + width; pixelX++) {
      check(texture.data ? texture.data[(pixelY * texture.w + pixelX) * 4 + 3] > 0
        : texture.d[(pixelY * texture.w + pixelX) * 4 + 3] > 0, `${name} transparent UV`);
    }
  }
}
const entity = fs.readFileSync(path.join(java, 'entity/TideFishBossEntity.java'), 'utf8');
const animations = json(path.join(assets, 'animations/tide_fish_boss.animation.json')).animations;
const lengths = { idle: 60, walk: 24, phase: 40, death: 20 };
for (const [name, durations] of Object.entries(require('./tide_fish_rework.cjs').durations)) {
  lengths[name] = durations[0];
  lengths[name + '_p2'] = durations[1];
}
for (const [name, ticks] of Object.entries(lengths)) {
  check(Math.abs(animations[`animation.tide_fish.${name}`].animation_length * 20 - ticks) < 0.001, `Animation length ${name}`);
  check(entity.includes(`"${name.replace(/_p2$/, '')}"`) || name === 'idle', `Animation action referenced: ${name}`);
}
for (const token of ['BossDamageGate.tick(this)', 'BossRage.tickBoss(this)', 'TrapBreaker.tick(this)',
  'BossBars.track(this', 'BossBars.untrack(this', 'BossMusic.request(server, this)', 'BossMusic.release(server, this)',
  'server.noCollision(this, getBoundingBox().expandTowards(motion))', 'BossTargets.valid(target)']) {
  check(entity.includes(token), `Missing lifecycle/combat hook: ${token}`);
}
for (const name of ['tide_trident']) {
  const model = path.join(assets, `models/item/${name}.json`);
  const texture = path.join(assets, `textures/${name.startsWith('statue') ? 'block' : 'item'}/${name}.png`);
  check(!json(model).parent, '3D item must not use generated parent');
  run('lint_java_model.cjs', model);
  run('check_uv_coverage.cjs', model, texture);
}
const variants = json(path.join(assets, 'blockstates/statue_tide_fish.json')).variants;
check(Object.keys(variants).length === 8, 'Both statue halves and both skins required');
for (const [key, variant] of Object.entries(variants)) {
  const file = path.join(assets, 'models', variant.model.replace('ftboss:', '') + '.json');
  const model = json(file);
  for (const reference of Object.values(model.textures)) {
    check(!reference.includes(':entity/'), 'Statue must not use entity atlas');
    if (reference.startsWith('minecraft:')) continue;
    check(fs.existsSync(path.join(assets, 'textures', reference.replace('ftboss:', '') + '.png')), 'Missing statue texture');
  }
  check(model.loader === 'forge:obj', `Rotation-preserving statue mesh: ${key}`);
}
run('../verification/check_boss_statues.cjs', assets, 'tide_fish');
const recipe = json(path.join(resources, 'data/ftboss/recipes/tide_banquet.json'));
check(recipe.key['#'].item === 'farmingtales:gold_bucket', 'Summon recipe center must be the gold bucket');
check(new Set(Object.values(recipe.key).map(value => value.item)).size === 9, 'Eight different outer ingredients');
const loot = json(path.join(resources, 'data/ftboss/loot_tables/entities/tide_fish_boss.json'));
check(!JSON.stringify(loot).includes('tide_trident') && !JSON.stringify(loot).includes('statue_tide_fish'), 'Do not duplicate event-based guaranteed rewards');
for (const locale of ['zh_cn', 'en_us']) {
  const values = json(path.join(assets, `lang/${locale}.json`));
  for (const key of ['entity.ftboss.tide_fish_boss', 'entity.ftboss.reef_fish', 'entity.ftboss.brine_puffer',
    'item.ftboss.tide_banquet', 'item.ftboss.tide_trident', 'item.ftboss.tide_pearl', 'block.ftboss.statue_tide_fish']) {
    check(typeof values[key] === 'string' && values[key].length > 0, `Missing language key: ${locale}/${key}`);
  }
}
console.log(`Tide fish asset/wiring verification passed: ${checks} assertions. Runtime combat remains an in-game check.`);
