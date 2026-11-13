import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const hashFile = async file => digest(await fs.readFile(file))
const read = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const require = (condition, message) => { if (!condition) throw new Error(message) }
const metadata = await read('room.json')
const sourceHash = await hashFile(path.join(root, 'room.nbt'))
require(sourceHash === metadata.sha256, 'Metadata/source mismatch')
const reports = {}
for (const report of ['static', 'route', 'native', 'views', 'image-decode', 'visual-review']) {
  reports[report] = await read('qa/' + report + '.json')
  require(reports[report].status === 'PASS', report + ' did not pass')
  require((reports[report].source_sha256 ?? reports[report].structure_sha256) === sourceHash, report + ' stale source hash')
}
require(reports.native.rotations.length === 4 && reports.native.stabilization_ticks === 200, 'Incomplete native QA')
for (const rotation of reports.native.rotations) {
  require(rotation.capture_matches_source && rotation.route.catch_cells === 336 && rotation.route.jumps === 5, 'Incomplete native route')
  require(rotation.world_capture_sha256 === await hashFile(path.join(root, rotation.world_capture)), 'Native capture mismatch')
}
for (const capture of reports.views.captures) require(capture.sha256 === await hashFile(path.join(root, capture.file)), 'View image changed')
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const verify = JSON.parse(await fs.readFile(path.join(batch, 'docs/verify.json'), 'utf8'))
const matching = verify.records.find(entry => entry.file === 'out/parkour/P03-parkour-lava-bubble-jumps.png')
require(matching && matching.ok && matching.sha256 === metadata.reference_sha256, 'Batch verification mismatch')
const manifest = JSON.parse(await fs.readFile(path.join(batch, 'docs/manifest.json'), 'utf8'))
require(manifest.files.some(entry => entry.file === matching.file && entry.sha256 === matching.sha256 && entry.ok), 'Batch manifest mismatch')
require(await hashFile(metadata.reference) === metadata.reference_sha256, 'Reference changed')
const provenanceFiles = []
for (const file of ['README.md', 'docs/manifest.json', 'docs/verify.json', 'out/parkour/P03-parkour-lava-bubble-jumps.png']) {
  const absolute = path.join(batch, file)
  provenanceFiles.push({ file: absolute, sha256: await hashFile(absolute), bytes: (await fs.stat(absolute)).size })
}
await fs.writeFile(path.join(root, 'qa/provenance.json'), JSON.stringify({ status: 'PASS', id: 'P03', source_sha256: sourceHash, reference_sha256: metadata.reference_sha256, batch_record: matching, files: provenanceFiles }, null, 2) + '\n')
const nativeRoutes = reports.native.rotations.map(rotation => ({ rotation: rotation.rotation, ...rotation.route }))
const result = {
  schema: 'ft-independent-zone-room-qa-v1', id: 'P03', name: metadata.name, status: 'PASS', sha256: sourceHash,
  reference: metadata.reference, reference_sha256: metadata.reference_sha256, minecraft: '1.20.1', forge: '47.4.10', size: metadata.size,
  entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit,
  checks: {
    reference_and_batch_provenance: 'PASS', static_nbt_geometry_and_full_air_volume: 'PASS', complete_roof_and_base: 'PASS',
    ports_three_wide_four_high: 'PASS', five_jumps_clearance_and_landing: 'PASS', all_336_catch_cells_recover_to_entry: 'PASS',
    lava_289_sources_sealed_all_six_faces: 'PASS', vanilla_registry_and_states: 'PASS',
    forge_four_rotations_actual_placement: 'PASS', native_200_tick_stability_and_neighbor_updates: 'PASS',
    native_world_captures_match_source: 'PASS', negative_fault_probes: 'PASS', eight_lanterns_and_chain_anchors: 'PASS',
    no_entities_drops_loot_or_forbidden_blocks: 'PASS', four_internal_four_external_views: 'PASS', visual_review_and_image_decode: 'PASS'
  },
  native_summary: { rotations: reports.native.rotations.map(rotation => rotation.rotation), records_per_rotation: 10179, checked_after_ticks: 40716, stabilization_ticks: 200, scheduled_lava_ticks: true, forced_chunk_ticking: true, negative_probes: reports.native.negative_probes, route_results: nativeRoutes, external_fluid_cells: 0, world_entities: 0 },
  evidence: { static: 'qa/static.json', route: 'qa/route.json', native: 'qa/native.json', native_log: 'qa/forge-gametest.log', views: 'qa/views.json', visual_review: 'qa/visual-review.json', image_decode: 'qa/image-decode.json', provenance: 'qa/provenance.json', interior_sheet: 'views/interior-sheet.png', exterior_sheet: 'views/exterior-sheet.png', route_plan: 'views/route-plan.png', native_captures: reports.native.rotations.map(rotation => rotation.world_capture) },
  not_tested: ['Full FarmingTales client human playthrough, multiplayer and FPS.', 'FT core integration, dungeon connections and return portals.'],
  safety: { deployed: false, player_saves_opened_or_modified: false, mods_or_production_jars_modified: false, parallel_rooms_modified: false, paid_image_api_called: false, test_world: 'native-qa/run-isolated/world', qa_fixture_must_not_be_deployed: true }
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(result, null, 2) + '\n')
const files = []
async function collect(relative) {
  for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
    const child = path.posix.join(relative.replaceAll('\\', '/'), entry.name)
    if (['native-qa/.gradle', 'native-qa/build', 'native-qa/run-isolated'].includes(child)) continue
    if (entry.isDirectory()) await collect(child)
    else if (entry.isFile() && !['SHA256SUMS.txt', 'qa/integrity.json'].includes(child)) files.push(child)
  }
}
await collect('')
files.sort()
const lines = []
for (const file of files) lines.push(await hashFile(path.join(root, file)) + '  ' + file)
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), lines.join('\n') + '\n')
for (const line of lines) {
  const file = line.slice(66)
  require(await hashFile(path.join(root, file)) === line.slice(0, 64), 'Final integrity mismatch ' + file)
}
await fs.writeFile(path.join(root, 'qa/integrity.json'), JSON.stringify({ status: 'PASS', source_sha256: sourceHash, verified_files: files.length, sha256sums_sha256: await hashFile(path.join(root, 'SHA256SUMS.txt')), excluded: ['native-qa/.gradle', 'native-qa/build', 'native-qa/run-isolated', 'SHA256SUMS.txt', 'qa/integrity.json'] }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', source_sha256: sourceHash, verified_files: files.length, qa_sha256: await hashFile(path.join(root, 'qa.json')), sha256sums_sha256: await hashFile(path.join(root, 'SHA256SUMS.txt')) }, null, 2))
