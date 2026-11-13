import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const sha256 = async filename => createHash('sha256').update(await fs.readFile(path.join(root, filename))).digest('hex')
const room = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const native = await readJson('qa/forge-native.json')
const views = await readJson('qa/views.json')
const composites = await readJson('qa/composites.json')
const digest = await sha256('room.nbt')
for (const evidence of [room, staticReport, native, views, composites]) assert.equal(evidence.sha256, digest)
for (const evidence of [staticReport, native, views, composites]) assert.equal(evidence.status, 'PASS')
assert.equal(native.rotations, 4)
assert.equal(native.stability_ticks, 400)
assert.equal(native.records_per_phase, 27608)
assert.equal(native.negative_probes_rejected.length, 6)
let jumpCount = 0
let climbCount = 0
for (const phase of [native.initial, native.after_400_ticks]) {
  assert.equal(phase.length, 4)
  assert.equal(new Set(phase.map(entry => entry.rotation)).size, 4)
  for (const fixture of phase) {
    assert.equal(fixture.records, 6902)
    assert.equal(fixture.water_cells, 304)
    assert.equal(fixture.recovery.reachable_wading_cells, 304)
    assert.equal(fixture.recovery.climb_sweeps.length, 2)
    assert.equal(fixture.rooted_pillars, 5)
    assert.equal(fixture.forward_jumps.length, 6)
    assert.equal(fixture.reverse_jumps.length, 6)
    jumpCount += fixture.forward_jumps.length + fixture.reverse_jumps.length
    climbCount += fixture.recovery.climb_sweeps.length
  }
}
assert.equal(jumpCount, 96)
assert.equal(climbCount, 16)
assert.equal(views.views.length, 11)
for (const view of views.views) assert.equal(await sha256(view.file), view.sha256)
for (const composite of composites.files) assert.equal(await sha256(composite.file), composite.sha256)
for (const direction of ['north', 'east', 'south', 'west']) {
  for (const kind of ['interior', 'exterior']) {
    const view = views.views.find(entry => entry.name === `${kind}-${direction}`)
    assert.equal(view.kind, 'full')
    assert.equal(view.original_records, view.displayed_records)
  }
}
const finalLog = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert.ok(finalLog.includes('P05_NATIVE_GAME_TEST_PASS: rotations=4/4'))
assert.ok(finalLog.includes('BUILD SUCCESSFUL'))
const report = {
  id: 'P05', name: '石柱跃厅', status: 'PASS', sha256: digest,
  reference_sha256: room.reference_sha256, size: room.size, minecraft: '1.20.1', forge: '47.4.10', data_version: 3465,
  static: 'PASS', reference_batch_manifest_and_verify: 'PASS', pillars_continuously_supported: 5,
  parkour: { status: 'PASS', landing_size: [2, 2], empty_gap_blocks: 2, vertical_change: 0, legs_per_direction: 6, rotations: 4, phases: 2, native_swept_jumps: jumpCount, native_climb_sweeps: climbCount, forward_and_reverse: true },
  recovery: { status: 'PASS', water_depth: 1, connected_fluid_cells: 304, north_bank_ladders: 2, ladder_support: 'PASS', south_bank_recovery_ladder: false },
  original_nbt_four_interior_and_four_exterior_views: 'PASS', view_count: 11,
  visual_review: { status: 'PASS', method: 'Agent inspection of actual NBT overview, four interior, four exterior and top plan', entrance_exit_markers: true, five_pillars_readable: true, rooted_pillars: true, no_visible_floating_fixtures_in_full_views: true, complete_roof: true, cutaway_display_only: true, previews_are_software_renders: true },
  native_placement_rotations: 4, records_per_phase: 27608, stability_ticks: 400,
  unchanged_after_neighbor_and_fluid_ticks: true, native_can_survive: 'PASS', water_seal: 'PASS',
  negative_probes_rejected: native.negative_probes_rejected,
  qa_fixture_revisions: [
    { file: 'qa/forge-run-20261103-124238-098.log', result: 'REJECTED', issue: 'Y=100 test boundary overlapped random terrain', correction: 'Moved isolated fixtures to Y=300; NBT unchanged' },
    { file: 'qa/forge-run-20261103-125052-997.log', result: 'REJECTED', issue: 'Rotated grounded AABB exact contact produced floating-point collision recheck', correction: 'Deflate only recheck AABB by 1e-7; native movement retains full 0.6x1.8 dimensions; NBT unchanged' },
  ],
  entities: 0, block_entities: 0, loot_tables: 0, deployment: 'NOT_DEPLOYED',
  production_save_used: false, production_files_modified: false, full_modpack_client_playtest: false,
  limits: [native.route_limits, 'Software previews are distinct from client screenshots.', 'Full-volume occupation rejection and persistent generation markers are required before future integration.'],
  evidence: { static: 'qa/static.json', native: 'qa/forge-native.json', native_log: 'qa/forge-run.log', views: 'qa/views.json', composites: 'qa/composites.json', interior: 'preview/four-interior.png', exterior: 'preview/four-exterior.png', gallery: 'preview/index.html', hashes: 'qa/SHA256SUMS.txt' },
  delivery_hashes: { 'room.nbt': digest, 'room.json': await sha256('room.json'), 'preview.png': await sha256('preview.png') },
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const entries = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']
async function collect(relative) {
  for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
    const filename = `${relative}/${entry.name}`
    if (entry.isDirectory()) await collect(filename)
    else if (!filename.endsWith('SHA256SUMS.txt')) entries.push(filename)
  }
}
for (const relative of ['source', 'preview', 'qa', 'forge/src', 'forge/gradle']) await collect(relative)
entries.push('forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties')
const sums = []
for (const entry of entries.sort()) sums.push(`${await sha256(entry)}  ${entry}`)
await fs.writeFile(path.join(root, 'qa/SHA256SUMS.txt'), sums.join('\n') + '\n')
console.log(JSON.stringify({ id: report.id, status: report.status, records_per_phase: report.records_per_phase, jump_count: jumpCount, delivery_hashes: { ...report.delivery_hashes, 'qa.json': await sha256('qa.json') }, hashed_files: entries.length }, null, 2))
