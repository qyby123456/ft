import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../', import.meta.url));
const assets = resolve(root, 'src/main/resources/assets/ftdungeonmobs');
const mappings = [
  ['skeleton_warrior', '骷髅战士final.bbmodel'],
  ['skeleton_archer', '骷髅弓手final.bbmodel'],
  ['skeleton_mage', '骷髅法师final.bbmodel'],
  ['skeleton_king', '骷髅王final.bbmodel'],
];
const readJson = filename => JSON.parse(readFileSync(filename, 'utf8'));
const sha256 = contents => createHash('sha256').update(contents).digest('hex');
const results = [];
for (const [modelId, filename] of mappings) {
  const sourceBuffer = readFileSync(resolve(root, 'model_sources', filename));
  const source = JSON.parse(sourceBuffer.toString('utf8'));
  const geometry = readJson(resolve(assets, 'geo', `${modelId}.geo.json`));
  const animations = readJson(resolve(assets, 'animations', `${modelId}.animation.json`));
  const model = geometry['minecraft:geometry'][0];
  assert.equal(geometry.format_version, '1.12.0');
  assert.equal(model.description.identifier, `geometry.${modelId}`);
  assert.equal(model.description.texture_width, source.resolution.width);
  assert.equal(model.description.texture_height, source.resolution.height);
  const boneNames = new Set(model.bones.map(bone => bone.name));
  assert.equal(boneNames.size, model.bones.length);
  assert.equal(model.bones.length, source.groups.length);
  for (const bone of model.bones) {
    if (bone.parent) assert.ok(boneNames.has(bone.parent));
  }
  const cubeCount = model.bones.reduce((count, bone) => count + (bone.cubes?.length ?? 0), 0);
  assert.equal(cubeCount, source.elements.length);
  assert.equal(Object.keys(animations.animations).length, source.animations.length);
  for (const animation of source.animations) {
    const imported = animations.animations[`animation.${modelId}.${animation.name}`];
    assert.ok(imported, `Missing ${modelId}:${animation.name}`);
    assert.ok(Math.abs(imported.animation_length - animation.length) < 0.00011);
    assert.equal(imported.loop === true, animation.loop === 'loop');
    for (const boneName of Object.keys(imported.bones ?? {})) {
      assert.ok(boneNames.has(boneName), `Orphan animation bone: ${modelId}:${boneName}`);
    }
  }
  const texture = readFileSync(resolve(assets, 'textures/entity', `${modelId}.png`));
  const originalTexture = Buffer.from(source.textures[0].source.split(',')[1], 'base64');
  assert.deepEqual(texture, originalTexture);
  assert.equal(texture.readUInt32BE(16), source.resolution.width);
  assert.equal(texture.readUInt32BE(20), source.resolution.height);
  for (const locale of ['zh_cn', 'en_us']) {
    const language = readJson(resolve(assets, 'lang', `${locale}.json`));
    assert.ok(language['itemGroup.ftdungeonmobs']);
    assert.ok(language[`entity.ftdungeonmobs.${modelId}`]);
    assert.ok(language[`item.ftdungeonmobs.${modelId}_spawn_egg`]);
  }
  const eggModel = readJson(resolve(assets, 'models/item', `${modelId}_spawn_egg.json`));
  assert.equal(eggModel.parent, 'minecraft:item/template_spawn_egg');
  results.push({ modelId, filename, spawnEgg: `ftdungeonmobs:${modelId}_spawn_egg`, cubes: cubeCount, bones: boneNames.size,
    animations: source.animations.map(animation => animation.name),
    originalSha256: sha256(sourceBuffer), textureSha256: sha256(texture) });
}
const report = { models: results.length, spawnEggs: results.length,
  cubes: results.reduce((count, model) => count + model.cubes, 0),
  bones: results.reduce((count, model) => count + model.bones, 0),
  animations: results.reduce((count, model) => count + model.animations.length, 0), results };
assert.equal(report.models, 4);
assert.equal(report.cubes, 655);
assert.equal(report.bones, 218);
assert.equal(report.animations, 17);
mkdirSync(resolve(root, 'build/reports'), { recursive: true });
writeFileSync(resolve(root, 'build/reports/imported-assets.json'), `${JSON.stringify(report, null, 2)}\n`);
console.log(JSON.stringify(report, null, 2));
