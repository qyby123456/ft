import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import assert from 'node:assert/strict'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const read = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const write = async (name, data) => fs.writeFile(path.join(root, name), JSON.stringify(data, null, 2) + '\n')
const metadata = await read('room.json')
const staticQa = await read('qa/static.json')
const nativeQa = await read('qa/native.json')
const render = await read('qa/render.json')
const structureHash = hash(await fs.readFile(path.join(root, 'room.nbt')))
for (const evidence of [metadata.sha256, staticQa.structure_sha256, nativeQa.structure_sha256, render.structure_sha256]) assert.equal(evidence, structureHash)
for (const evidence of [staticQa, nativeQa, render]) assert.equal(evidence.status, 'PASS')
assert(nativeQa.isolated_world.endsWith('20261103-p02-final-2\\world'))
assert.equal(nativeQa.stabilization_ticks, 200)
assert.equal(nativeQa.after_200_ticks.length, 4)
assert.equal(render.views.length, 19)
assert.equal(render.browser_exceptions.length, 0)
for (const view of render.views) {
  const bytes = await fs.readFile(path.join(root, view.file))
  assert.equal(hash(bytes), view.sha256, 'Stale screenshot ' + view.file)
  assert.equal(bytes.subarray(1, 4).toString(), 'PNG')
  assert.equal(bytes.readUInt32BE(16), 1600)
  assert.equal(bytes.readUInt32BE(20), 1000)
  if (view.render_source_sha256 && view.source === '/room.nbt') assert.equal(view.render_source_sha256, structureHash)
}
const nativeSummary = []
for (const placement of nativeQa.after_200_ticks) {
  assert.equal(placement.status, 'PASS')
  assert.equal(placement.records_checked, 7293)
  assert.equal(placement.fluid_cells, 156)
  assert.equal(placement.escaped_fluid_cells, 0)
  assert.equal(placement.native_player_jumps.length, 16)
  assert.equal(placement.recovery_routes.length, 8)
  assert.equal(hash(await fs.readFile(path.join(root, placement.world_capture))), placement.world_capture_sha256)
  assert(render.views.some(view => view.render_source_sha256 === placement.world_capture_sha256))
  for (const recovery of placement.recovery_routes) {
    assert(recovery.native_failure_fall.length > 0 && recovery.native_stair_travel.length > 0)
    assert.equal(recovery.return_to_entrance, 'PASS')
  }
  nativeSummary.push({ rotation: placement.rotation, forward_jumps: 8, reverse_jumps: 8, native_fall_and_stair_routes: 8, stable_block_records: placement.records_checked, stable_fluid_cells: placement.fluid_cells, escaped_fluid_cells: 0, capture_sha256: placement.world_capture_sha256 })
}
const runLog = await fs.readFile(path.join(root, 'qa/forge-20261103-p02-final-2.log'), 'utf8')
assert(runLog.includes('All 1 required tests passed') && runLog.includes('BUILD SUCCESSFUL'))
const visualReview = {
  status: 'PASS', structure_sha256: structureHash,
  method: 'Direct visual inspection of Lodestone renders from the final NBT and Forge world captures.',
  reviewed: ['preview.png', 'preview/four-view-sheet.png', 'preview/four-shell-sheet.png', 'preview/native-placement-sheet.png', 'preview/interior.png', 'preview/roof.png'],
  findings: [
    { area: 'north/south interiors', result: 'PASS', observation: 'Central alternating mossy platforms and the broad midpoint rest deck are visible; north and south ports remain open.' },
    { area: 'west/east interiors', result: 'PASS', observation: 'Stone columns connect to transverse beams; chain lanterns, wooden rails, cyan banners and empty barrels remain supported and outside the jump lane.' },
    { area: 'four exterior directions', result: 'PASS', observation: 'Uncut shell has continuous side walls and roof; centered dry 3x3 north and south openings are the only ports.' },
    { area: 'fluid and recovery', result: 'PASS', observation: 'Two shallow basins each have recovery stairs. The far-end water feature is behind glass beyond the side-gallery stop wall.' },
    { area: 'reference fidelity', result: 'PASS', observation: 'The concept mossy channel, repeating stone arches, warm lanterns, offset stepping stones, wooden rails, barrels and teal accents are retained; scale and roof are adapted for a playable room.' },
    { area: 'world recapture', result: 'PASS', observation: 'Four rotated complete shells render from hash-matched post-stabilization Forge captures.' },
  ],
  limitation: 'Software-rendered evidence, not an in-game modpack client screenshot.',
}
await write('qa/four-view-review.json', visualReview)
await write('qa/route-summary.json', { status: 'PASS', structure_sha256: structureHash, method: 'Forge 47.4.10 / Minecraft 1.20.1 FakePlayer jumpFromGround + baseTick + travel; 0.6x1.8 player collision. Fake players are never added to world entity lists.', jump_parameters: { sprinting: false, initial_horizontal_speed: 0.215, native_jump_impulse: true, measured_ticks_to_land: 12, gap_blocks: 1, sampled_reverse: true, note: 'Each leg starts from a settled platform takeoff; a continuous input-recorded client run is outside this evidence.' }, recovery_parameters: { shallow_water_depth: 1, water_fall_ticks: 8, measured_stair_travel_ticks: 16, step_rises: [0.5, 0.5], eye_air_supply_unchanged: true, no_fall_damage: true, return_lane: 'Continuous native collision-clear sampled walking route to the north starting platform.' }, rotations: nativeSummary })
const qa = {
  id: 'zone-P02', status: 'PASS', size: metadata.size, structure_sha256: structureHash, reference_sha256: metadata.reference_sha256,
  checks: {
    static: { status: 'PASS', evidence: 'qa/static.json', complete_explicit_volume: 7293, eight_gap_checks: 8 },
    source_documents_and_hashes: { status: 'PASS', evidence: 'qa/static.json' },
    four_direction_interior_and_shell_review: { status: 'PASS', evidence: 'qa/four-view-review.json', interior_sheet: 'preview/four-view-sheet.png', shell_sheet: 'preview/four-shell-sheet.png' },
    forge_native: { status: 'PASS', evidence: 'qa/native.json', log: 'qa/forge-20261103-p02-final-2.log', minecraft: '1.20.1', forge: '47.4.10', rotations: 4, stabilization_ticks: 200, unchanged_records_per_rotation: 7293, escaped_fluid_cells: 0, empty_block_entities_per_rotation: 7 },
    route_and_recovery: { status: 'PASS', evidence: 'qa/route-summary.json', after_200_ticks_native_jumps: 64, after_200_ticks_native_fall_stair_recoveries: 32, negative_blocked_port_probes: 4, negative_basin_hole_probes: 4 },
    preview_integrity: { status: 'PASS', evidence: 'qa/render.json', png_count: 19, source_nbt_hash_matched: true, native_capture_hashes_matched: 4 },
  },
  ready_for_unified_review: true, integrated: false, deployed: false, production_world_modified: false,
  artifacts: { template: 'room.nbt', metadata: 'room.json', preview: 'preview.png', qa: 'qa.json', checksums: 'checksums.sha256', interior_four_views: 'preview/four-view-sheet.png', shell_four_views: 'preview/four-shell-sheet.png', native_placements: 'preview/native-placement-sheet.png', recipe: 'recipe.mjs' },
  retained_failed_attempts: [
    { log: 'qa/forge-20261103-p02-initial.log', cause: 'GameTest pack metadata missing; added pack.mcmeta before native acceptance.' },
    { log: 'qa/forge-20261103-p02-native-2.log', cause: 'Generated terrain water entered the test perimeter; moved the isolated fixture to Y=160.' },
    { log: 'qa/forge-20261103-p02-final.log', cause: '512 MiB test JVM exhausted memory before completing GameTest. Gradle returned success despite no fresh report; final runner now requires fresh PASS report and explicit GameTest success.' },
  ],
  limitations: ['Independent candidate awaiting coordinator acceptance, external approach construction and slot allocation.', 'Evidence covers isolated Forge structure placement and native server-side collision/travel probes, not full-modpack client gameplay, multiplayer or FPS.', 'The midpoint is a dry rest platform, not a respawn/checkpoint command. The shallow-water route is intentionally recoverable and does not enforce a score or prevent deliberate swimming.'],
}
await write('qa.json', qa)
const evidenceFiles = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'qa/static.json', 'qa/native.json', 'qa/route-summary.json', 'qa/four-view-review.json', 'qa/render.json', 'qa/forge-20261103-p02-final-2.log', 'recipe.mjs', 'src/main/java/net/farmingtales/p02qa/P02GameTests.java', ...render.views.map(view => view.file), ...nativeQa.after_200_ticks.map(entry => entry.world_capture)]
const checksums = []
for (const file of [...new Set(evidenceFiles)]) checksums.push(hash(await fs.readFile(path.join(root, file))) + '  ' + file.replaceAll('\\', '/'))
await fs.writeFile(path.join(root, 'checksums.sha256'), checksums.join('\n') + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: structureHash, core_artifacts: checksums.slice(0, 4), native_jumps: 64, native_recoveries: 32, pngs: render.views.length }, null, 2))
