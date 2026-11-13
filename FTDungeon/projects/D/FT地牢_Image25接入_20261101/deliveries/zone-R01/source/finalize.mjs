import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const readJson = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const sourceHash = digest(await fs.readFile(path.join(root, 'room.nbt')))
const metadata = await readJson('room.json')
assert.equal(metadata.id, 'zone-R01')
assert.equal(metadata.sha256, sourceHash)
const reports = {}
for (const name of ['static', 'forge-native', 'views', 'composites']) {
  reports[name] = await readJson(`qa/${name}.json`)
  assert.equal(reports[name].status, 'PASS')
  assert.equal(reports[name].sha256, sourceHash)
}
const native = reports['forge-native']
assert.equal(native.rotations, 4)
assert.equal(native.ticks, 400)
assert.equal(native.records_checked_per_snapshot, 42500)
assert.equal(native.negative_probes_rejected.length, 6)
for (const phase of ['initial', 'after_400_ticks']) {
  assert.equal(native[phase].length, 4)
  assert.equal(new Set(native[phase].map(entry => entry.rotation)).size, 4)
  for (const entry of native[phase]) {
    assert.equal(entry.records, 10625)
    assert.equal(entry.native_supported_blocks, 4112)
    assert.equal(entry.player_collision_samples, 184)
    assert.equal(entry.unwanted_entities, 0)
  }
}
const images = [...reports.views.views, ...reports.composites.images]
assert.equal(reports.views.views.length, 11)
for (const image of images) {
  const bytes = await fs.readFile(path.join(root, image.file))
  assert.equal(digest(bytes), image.sha256, `Stale screenshot ${image.file}`)
  assert.equal(bytes.subarray(1, 4).toString(), 'PNG')
  assert.ok(bytes.length > 10000)
  if (image.file !== 'preview.png' && !image.file.includes('four-')) {
    assert.equal(bytes.readUInt32BE(16), 1536)
    assert.equal(bytes.readUInt32BE(20), 1024)
  }
}
for (const kind of ['interior', 'exterior']) {
  const directions = reports.views.views.filter(view => view.name.startsWith(`${kind}-`))
  assert.equal(directions.length, 4)
  assert.equal(new Set(directions.map(view => view.sha256)).size, 4)
  if (kind === 'exterior') for (const view of directions) assert.equal(view.kind, 'full')
}
assert.equal(digest(await fs.readFile(path.join(root, 'reference/R01-ruins-collapsed-watchtower-base.png'))), metadata.reference_sha256)
const forgeLog = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert.ok(forgeLog.includes(`ZONE_R01_NATIVE_GAME_TEST_PASS rotations=4/4 records=42500 ticks=400 sha256=${sourceHash}`))
assert.ok(forgeLog.includes('All 1 required tests passed'))
assert.ok(forgeLog.includes('BUILD SUCCESSFUL'))
const reportIndex = []
for (const [name, report] of Object.entries(reports)) {
  const filename = `qa/${name}.json`
  reportIndex.push({ file: filename, status: report.status, sha256: digest(await fs.readFile(path.join(root, filename))) })
}
const qa = {
  id: 'zone-R01', name: metadata.name, status: 'PASS', sha256: sourceHash, reference_sha256: metadata.reference_sha256,
  size: metadata.size, minecraft: '1.20.1', data_version: 3465, generator: metadata.generator,
  required_four_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'],
  checks: {
    reference_verified_against_manifest_and_verify: true, original_reference_sha256_matches: true,
    full_unique_explicit_volume: 10625, non_air_blocks: 4112, explicit_air: 6513,
    unsupported_assemblies: 0, entrance_clearance: [3, 4], dry_ground_route_cells: 103,
    safe_landing: [12, 2, 12], safe_landing_size: [5, 3, 5], three_wide_supported_stair_steps: 9,
    supported_lanterns: 6, empty_barrels: 4, patterned_wall_banners: 2, entities: 0,
    four_exterior_views_original_nbt: true, four_interior_views_display_cutaway: true, total_views: 11,
    forge_native_actual_rotations: 4, forge_ticks: 400, forge_records_per_snapshot: 42500,
    unchanged_block_states_both_snapshots: true, native_collision_samples_per_rotation: 184,
    native_negative_defects_detected: 6, boundary_air_cells_per_rotation: 3226,
    no_fluids_fire_gravity_blocks_or_scripted_rewards: true,
  },
  report_index: reportIndex,
  visual_review: { status: 'PASS', inspected: ['preview.png', 'preview/four-interior.png', 'preview/four-exterior.png', 'preview/cutaway.png', 'preview/exterior-north.png'], software_preview_only: true, displayed_cutaway_preserves_original_nbt: true, observation_side_attached_banners_hidden_with_removed_wall: true },
  deviations: metadata.deviations,
  fixture_history: { first_attempt: 'qa/forge-run-20261103-153925-230.log', failure: 'natural terrain occupied test boundary at Y99', correction: 'fixture origin raised from Y100 to Y256; room NBT unchanged', final: 'qa/forge-run.log' },
  deployment: { installed: false, production_files_modified: false, player_save_used: false, other_ids_modified: false, existing_R1_reused: false },
  limitations: ['露天残顶与旧九间密闭房间屋顶条款有差异，后续需按露天废墟审查。', '软件渲染、隔离Forge原生碰撞采样，不是整合包客户端真人游玩/FPS/联机验收。', '含显式空气，后续放置必须预检整个体积并拒绝占用。'],
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const evidenceFiles = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']
async function collect(relative) {
  for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
    const child = path.posix.join(relative, entry.name)
    if (entry.isDirectory()) await collect(child)
    else evidenceFiles.push(child)
  }
}
for (const directory of ['source', 'preview', 'reference', 'qa', 'forge/src', 'forge/gradle/wrapper']) await collect(directory)
for (const filename of ['forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties']) evidenceFiles.push(filename)
const evidence = []
for (const filename of [...new Set(evidenceFiles)].sort()) {
  const bytes = await fs.readFile(path.join(root, filename))
  evidence.push({ file: filename, bytes: bytes.length, sha256: digest(bytes) })
}
const manifest = {
  id: 'zone-R01', status: 'PASS', name: metadata.name, room_sha256: sourceHash, reference_sha256: metadata.reference_sha256,
  files: evidence, excludes: ['forge/run-* isolated worlds', 'forge/build generated caches', 'forge/.gradle', '.vite renderer cache', 'delivery-manifest.json self'],
  production_deployment: false,
}
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', id: 'zone-R01', required_four_files: qa.required_four_files, room_sha256: sourceHash, evidence_files: evidence.length, manifest_sha256: digest(await fs.readFile(path.join(root, 'delivery-manifest.json'))) }))
