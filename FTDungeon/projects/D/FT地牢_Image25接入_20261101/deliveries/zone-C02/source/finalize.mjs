import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const artifact = async filename => {
  const bytes = await fs.readFile(path.join(root, filename))
  return { path: filename, bytes: bytes.length, sha256: digest(bytes) }
}
const readJson = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const room = await readJson('room.json')
const hash = digest(await fs.readFile(path.join(root, 'room.nbt')))
assert.equal(room.sha256, hash)
const evidenceFiles = ['qa/static.json', 'qa/render-manifest.json', 'qa/visual-review.json', 'qa/forge-native.json', 'qa/capture-comparison.json']
const reports = {}
const evidence = {}
for (const filename of evidenceFiles) {
  const report = await readJson(filename)
  assert.equal(report.status, 'PASS', filename)
  assert.equal(report.sha256, hash, filename)
  reports[filename] = report
  evidence[filename] = await artifact(filename)
}
const views = reports['qa/render-manifest.json'].views
assert.equal(views.length, 12)
assert.equal(views.filter(view => view.name.startsWith('interior-')).length, 4)
assert.equal(views.filter(view => view.name.startsWith('exterior-')).length, 4)
assert.equal(reports['qa/visual-review.json'].checked_individual_views.length, 8)
for (const view of views) {
  const bytes = await fs.readFile(path.join(root, view.file))
  assert.equal(digest(bytes), view.sha256)
  assert.equal(bytes.readUInt32BE(16), 1536)
  assert.equal(bytes.readUInt32BE(20), 1024)
}
assert.equal(digest(await fs.readFile(path.join(root, 'preview.png'))), views.find(view => view.name === 'hero').sha256)
const native = reports['qa/forge-native.json']
const capture = await artifact('qa/native-capture.json.gz')
assert.equal(capture.sha256, native.capture_sha256)
evidence[capture.path] = capture
const logs = (await fs.readdir(path.join(root, 'qa'))).filter(filename => /^forge-.*\.log$/.test(filename)).sort().reverse()
let acceptedLog
for (const filename of logs) {
  const bytes = await fs.readFile(path.join(root, 'qa', filename))
  const text = bytes[0] === 255 && bytes[1] === 254 ? bytes.subarray(2).toString('utf16le') : bytes.toString('utf8')
  if (text.includes('C02_NATIVE_GAME_TEST_PASS: rotations=4/4; records=15884; ticks=400; sha256=' + hash) && text.includes('BUILD SUCCESSFUL') && text.includes('All 1 required tests passed')) {
    acceptedLog = 'qa/' + filename
    break
  }
}
assert.ok(acceptedLog, 'No completed successful Forge log for the final hash')
evidence[acceptedLog] = await artifact(acceptedLog)
const qa = {
  id: 'C02', canonical_id: 'zone-C02', status: 'PASS', sha256: hash,
  reference_sha256: room.reference_sha256, size: room.size,
  scope: 'Independent producer delivery; static QA, individually reviewed four interior/four exterior views, isolated Forge four-rotation placement and post-400-tick stability.',
  checks: {
    static: 'PASS', source_fingerprint: 'PASS', north_east_ports: 'PASS', dry_three_wide_turn: 'PASS',
    bidirectional_player_collision_path: 'PASS', complete_roof_and_sealed_shell: 'PASS', source_water_seal: 'PASS',
    eight_individual_views: 'PASS', native_registry_and_properties: 'PASS', native_four_rotations: 'PASS',
    native_400_tick_stability: 'PASS', independent_native_capture_comparison: 'PASS',
  },
  counts: { records_per_rotation: 3971, four_rotation_records: 15884, water_sources: 131, dry_route_cells: 57, guard_fences: 34, hanging_lanterns: 4, table_lanterns: 1, empty_barrels: 2, inventory_items: 0, loot_tables: 0, entities: 0 },
  native: { minecraft: '1.20.1', forge: '47.4.10', rotations: 4, ticks_observed: 400, native_state_comparisons_across_two_phases: 31768, boundary_air_cells_per_rotation: 1762, source_water_seal_faces_per_rotation: 655, neighbor_updates: true, water_ticks_scheduled: true, random_tick_speed: 3, fire_tick_enabled: true, snapshot_mismatches: 0 },
  negative_probes_rejected: native.negative_probes_rejected,
  evidence, software_preview_only: true, preview_cutaways_modify_nbt: false,
  design_deviations: reports['qa/visual-review.json'].reference_deviations,
  revisions: { initial_sha256: '18e6cf4dafa5129fced0bc57c2242b2b961cb0995d44b8240fbd55b6ca027bbd', reason: 'Added solid foundation beneath the side lantern plinth; all final QA and images regenerated.', archived_evidence: 'qa/revisions/initial-18e6cf4d/' },
  ready_for_receiver_review: true, ready_for_unified_integration: false,
  deployed: false, production_save_accessed: false, production_mods_modified: false, full_modpack_client_playtest: false,
  limits: ['Receiver must freeze and independently recheck the final hash before integration.', 'Core road connections, occupancy refusal, idempotence and persistence belong to later unified integration.', 'Isolated native verification does not cover complete modpack/client/multiplayer/FPS gameplay.', 'Explicit air records require a full-volume occupancy check before any future placement.'],
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json', 'forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties']
async function collect(directory) {
  for (const entry of await fs.readdir(path.join(root, directory), { withFileTypes: true })) {
    const filename = directory + '/' + entry.name
    if (entry.isDirectory()) await collect(filename)
    else files.push(filename)
  }
}
for (const directory of ['source', 'views', 'forge/src', 'forge/gradle']) await collect(directory)
for (const filename of await fs.readdir(path.join(root, 'qa'))) {
  if ((await fs.stat(path.join(root, 'qa', filename))).isFile()) files.push('qa/' + filename)
}
const entries = await Promise.all([...new Set(files)].sort().map(artifact))
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: 'C02', canonical_id: 'zone-C02', sha256: hash, required_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'], evidence_scope: 'Final independent candidate only; historical revisions and generated Gradle/world/cache directories excluded.', files: entries }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash, files_hashed: entries.length, forge_log: acceptedLog, deployment: false }))

