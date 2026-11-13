import assert from 'node:assert/strict';
import {readFile, readdir, mkdir, writeFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import {createHash} from 'node:crypto';

const root = fileURLToPath(new URL('../', import.meta.url));
const resources = path.join(root, 'src/main/resources');

async function filesUnder(directory) {
  const entries = await readdir(directory, {withFileTypes: true});
  const results = [];
  for (const entry of entries) {
    const filename = path.join(directory, entry.name);
    if (entry.isDirectory()) results.push(...await filesUnder(filename));
    else results.push(filename);
  }
  return results.sort();
}

const files = await filesUnder(resources);
const hashes = [];
let jsonCount = 0;
for (const filename of files) {
  const bytes = await readFile(filename);
  if (filename.endsWith('.json') || filename.endsWith('.mcmeta')) {
    JSON.parse(bytes.toString('utf8'));
    jsonCount++;
  }
  assert(!/\.(?:png|ogg|bbmodel|class|jar)$/.test(filename), 'Challenge module must not bundle models or external binaries');
  hashes.push({path: path.relative(resources, filename).replaceAll('\\', '/'), sha256: createHash('sha256').update(bytes).digest('hex')});
}

const chinese = JSON.parse(await readFile(path.join(resources, 'assets/ftdungeonchallenges/lang/zh_cn.json'), 'utf8'));
const english = JSON.parse(await readFile(path.join(resources, 'assets/ftdungeonchallenges/lang/en_us.json'), 'utf8'));
assert.deepEqual(Object.keys(chinese).sort(), Object.keys(english).sort(), 'Language key coverage differs');
for (const key of Object.keys(chinese)) {
  assert.equal((chinese[key].match(/%s/g) ?? []).length, (english[key].match(/%s/g) ?? []).length, `Placeholder mismatch: ${key}`);
}
const javaFiles = await filesUnder(path.join(root, 'src/main/java'));
const source = (await Promise.all(javaFiles.map(filename => readFile(filename, 'utf8')))).join('\n');
for (const match of source.matchAll(/"(challenge\.ftdungeonmobs\.[a-z_]+(?:\.[a-z_]+)*)"/g)) {
  const key = match[1];
  assert(key in english || key.endsWith('.reason') || key.endsWith('.error'), `Missing literal translation ${key}`);
}
for (const reason of ['victory', 'cancelled', 'defeat', 'absent', 'wave_timeout', 'spawn_blocked', 'torch_removed',
  'enemy_lost', 'out_of_bounds', 'server_stopped', 'disabled', 'peaceful']) {
  assert(`challenge.ftdungeonmobs.reason.${reason}` in chinese, `Missing end reason ${reason}`);
}
for (const tier of [1, 2, 3, 4, 5]) {
  const loot = JSON.parse(await readFile(path.join(resources, `data/ftdungeonchallenges/loot_tables/challenges/tier_${tier}.json`), 'utf8'));
  assert.equal(loot.type, 'minecraft:gift');
  assert.equal(loot.pools.length, 2);
  for (const pool of loot.pools) {
    assert.equal(pool.rolls, 1);
    for (const entry of pool.entries) {
      assert.equal(entry.type, 'minecraft:item');
      assert(entry.name.startsWith('minecraft:'));
      const count = entry.functions.find(operation => operation.function === 'minecraft:set_count').count;
      assert(Number.isInteger(count.min) && Number.isInteger(count.max) && count.min > 0 && count.max >= count.min);
    }
  }
}
const metadata = await readFile(path.join(resources, 'META-INF/mods.toml'), 'utf8');
assert(metadata.includes('modId="ftdungeonchallenges"'));
assert(metadata.includes('modId="ftdungeonmobs"'));
assert(!metadata.includes('modId="majrusz'));
const report = {status: 'PASS', jsonCount, translations: Object.keys(chinese).length, lootTiers: 5, javaFiles: javaFiles.length, resources: hashes};
await mkdir(path.join(root, 'build/reports'), {recursive: true});
await writeFile(path.join(root, 'build/reports/challenge-resources.json'), `${JSON.stringify(report, null, 2)}\n`);
console.log(`CHALLENGE_RESOURCES_VERIFIED json=${jsonCount} translations=${report.translations} lootTiers=5 files=${files.length}`);
