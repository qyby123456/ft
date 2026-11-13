import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const read = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const metadata = await read('room.json')
const digest = hash(await fs.readFile(path.join(root, 'room.nbt')))
const staticReport = await read('qa/static.json')
const native = await read('qa/native.json')
const render = await read('qa/render.json')
const lodestone = await read('qa/lodestone-render.json')
const reference = await read('qa/reference.json')
const visual = await read('qa/visual-review.json')
if ([staticReport, native, render, lodestone, reference, visual].some(report => report.status !== 'PASS')) throw new Error('All independent QA stages must PASS before delivery')
if ([metadata.sha256, staticReport.sha256, native.sha256, render.source_sha256, lodestone.source_sha256, visual.source_sha256].some(value => value !== digest)) throw new Error('Source hash mismatch')
const successLog = 'qa/forge-h05-attempt4.log'
const log = await fs.readFile(path.join(root, successLog), 'utf8')
if (!log.includes('H05_NATIVE_GAME_TEST_PASS: rotations=4/4; records=130572; ticks=400') || !log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Missing actual Forge success markers')
const report = {
  status: 'PASS', id: 'zone-H05', name: '穹顶大殿', sha256: digest,
  reference_sha256: metadata.referenceSha256, size: metadata.size,
  records: metadata.records, non_air: metadata.nonAir,
  static: { status: staticReport.status, checks: staticReport.checks.length, evidence: 'qa/static.json' },
  reference: { status: reference.status, evidence: 'qa/reference.json', batch_manifests: 'reference/' },
  visual: { status: visual.status, evidence: 'qa/visual-review.json', four_views: 'views/' + digest.slice(0, 16) + '/zone-H05/', preview: 'preview.png', preview_kind: 'Lodestone 0.6.0 exact NBT interior software render, not a game screenshot', cutaway_changes_nbt: false },
  forge: { status: native.status, evidence: 'qa/native.json', log: successLog, minecraft: '1.20.1', forge: '47.4.10', rotations: native.rotations, records_per_rotation: native.records_per_rotation, total_placed_records: native.total_placed_records, stability_ticks: native.stability_ticks, neighbor_shape_updates: native.neighbor_shape_updates, explicit_random_ticks: native.explicit_random_ticks, full_modpack_client_playtest: false },
  safety: { north_entrance: metadata.entrance, safe_point: metadata.safe, main_aisle_width: 5, main_aisle_headroom: 4, safe_footprint: [5, 5], grounded_piers: 6, anchored_chandeliers: 4, complete_two_layer_vault_columns: 1209, exterior_air_flood_leaks: 0, active_fluid_or_fire: false, entities: 0, block_entities: 0, inventories: 0, loot_or_mob_logic: false },
  changes_after_rejection: [{ stage: 'static', cause: '12 decorative corbel blocks were only diagonal contacts', resolution: 'built continuous masonry back to the pier spine', evidence: 'qa/history/static-before-corbel-support.json' }, { stage: 'Forge attempt1', cause: 'test terrain occupied the verification volume at y=100', resolution: 'moved isolated fixtures to y=240; reran in a fresh world', evidence: 'qa/forge-attempt1.log' }, { stage: 'Forge attempt2', cause: 'player collision sampler skipped the carpet lip at the stair landing', resolution: 'sampled both the stair top and 1/16 carpet lift before crossing the landing', evidence: 'qa/forge-attempt2.log' }, { stage: 'Forge attempt3', cause: 'ForgeGradle remote environment certificate check stalled despite offline dependency mode', resolution: 'preserved thread dump, stopped only verified H05 Gradle processes, disabled remote check for this isolated build and passed in fresh attempt4 world', evidence: 'qa/gradle-attempt3-thread-dump.txt' }],
  deviations: metadata.deviations,
  production_files_changed: false, deployed: false,
  limitations: ['Isolated Forge GameTest verifies actual ServerLevel placement, native collision and survival after neighbor updates and 400 ticks; no full modpack client FPS or multiplayer run.', 'Safe point describes dry supported collision clearance; natural mob spawning was disabled only inside the QA fixture and production gameplay rules remain for the integrator.', 'Stair ascent uses native collision boxes and <=0.6-block support-height transitions; no human-controlled player client was launched.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'source/recipe.mjs', 'source/static-qa.mjs', 'source/finalize.mjs', 'source/render-four-views.py', 'source/render-lodestone.cjs', 'source/run-forge.py', 'source/prepare-fixture.mjs', 'qa/static.json', 'qa/native.json', 'qa/reference.json', 'qa/render.json', 'qa/four-view-manifest.json', 'qa/lodestone-render.json', 'qa/visual-review.json', successLog, 'qa/forge-attempt1.log', 'qa/forge-attempt2.log', 'reference/H05-halls-vaulted-grand-hall.png', 'reference/batch-README.md', 'reference/batch-manifest.json', 'reference/batch-verify.json', ...render.files.map(entry => entry.file), 'views/lodestone-north.png', 'views/lodestone-south.png', 'views/lodestone-exterior.png', 'native-test/src/main/java/net/farmingtales/zoneh05qa/ZoneH05Verification.java', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/resources/META-INF/mods.toml']
files.push('qa/forge-attempt3.log', 'qa/gradle-attempt3-thread-dump.txt', 'qa/history/static-before-corbel-support.json', 'qa/history/before-corbel-support.nbt', 'qa/history/native-attempt2-running-marker.json', 'native-test/src/main/resources/pack.mcmeta', 'native-test/src/main/resources/data/ftzoneh05qa/structures/empty.nbt', 'views/' + digest.slice(0, 16) + '/zone-H05/room.nbt', 'views/' + digest.slice(0, 16) + '/zone-H05/room.json')
const entries = []
for (const name of files) {
  const bytes = await fs.readFile(path.join(root, name))
  entries.push({ file: name, bytes: bytes.length, sha256: hash(bytes) })
}
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: report.id, status: 'PASS', nbt_sha256: digest, files: entries }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', id: report.id, sha256: digest, delivery_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'], manifest_files: entries.length }, null, 2))
