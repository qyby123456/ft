import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const hashFile = async relative => digest(await fs.readFile(path.join(root, relative)))
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const requireCheck = (condition, label) => { if (!condition) throw new Error(label) }
const sourceHash = await hashFile('room.nbt')
const metadata = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const native = await readJson('qa/native.json')
const views = await readJson('qa/views.json')
const images = await readJson('qa/image-decode.json')
const visual = await readJson('qa/visual-review.json')
requireCheck(metadata.id === 'zone-M01' && metadata.batch_type === 'zone' && metadata.sha256 === sourceHash, 'Zone identity/source mismatch')
for (const [label, report, key] of [['static', staticReport, 'sha256'], ['native', native, 'structure_sha256'], ['views', views, 'source_sha256'], ['images', images, 'source_sha256'], ['visual', visual, 'source_sha256']]) {
  requireCheck(report.status === 'PASS' && report[key] === sourceHash, label + ' status/fingerprint mismatch')
}
requireCheck(digest(await fs.readFile(metadata.reference)) === metadata.reference_sha256, 'Reference changed')
for (const source of metadata.source_documents) requireCheck(digest(await fs.readFile(source.file)) === source.sha256, 'Source document changed ' + source.file)
requireCheck(native.rotations.length === 4 && native.rotations_before_ticks.length === 4 && native.stabilization_ticks === 200 && native.records_checked_after_ticks === 27324, 'Native coverage mismatch')
requireCheck(new Set(native.rotations.map(rotation => rotation.rotation)).size === 4, 'Duplicate rotation')
for (const rotation of native.rotations) {
  requireCheck(rotation.status === 'PASS' && rotation.records_checked === 6831 && rotation.capture_matches_source && rotation.rail_cells === 27 && rotation.lanterns_supported === 6 && rotation.grounded_timber_posts === 4 && rotation.empty_barrels === 4 && rotation.roof_cells === 621 && rotation.external_fluid_cells === 0 && rotation.world_entities === 0, 'Native rotation checks incomplete')
  requireCheck(await hashFile(rotation.world_capture) === rotation.world_capture_sha256, 'Capture changed ' + rotation.rotation)
}
for (const value of Object.values(native.negative_probes)) requireCheck(value === 4, 'Negative probe missing')
requireCheck(views.captures.length === 10 && images.decoded_views === 10 && views.console_errors.length === 0 && !views.original_nbt_modified, 'View coverage incomplete')
for (const capture of views.captures) requireCheck(await hashFile(capture.path) === capture.sha256, 'View fingerprint mismatch')
for (const capture of images.views) requireCheck(await hashFile(capture.file) === capture.sha256, 'Image decode fingerprint mismatch')
requireCheck(await hashFile('preview.png') === await hashFile('views/interior-south.png'), 'Preview not from inspected source')
const log = await fs.readFile(path.join(root, 'qa/forge-gametest.log'), 'utf8')
requireCheck(log.includes('ZONE_M01_NATIVE_PASS rotations=4/4 records=27324 ticks=200 sha256=' + sourceHash) && log.includes('BUILD SUCCESSFUL'), 'Native log incomplete')
const report = {
  schema: 'ft-independent-zone-delivery-qa-v1', id: 'zone-M01', batch_id: 'M01', batch_type: 'zone', name: '活跃矿道', status: 'PASS',
  sha256: sourceHash, reference: metadata.reference, reference_sha256: metadata.reference_sha256, size: metadata.size,
  entrance: metadata.entrance, interior: metadata.interior, safe_point: metadata.safe_point, minecraft: '1.20.1', forge: '47.4.10',
  checks: { source_image_and_batch_hashes: 'PASS', static_24_checks: 'PASS', unique_explicit_6831_cell_volume: 'PASS', grounded_timber_supports: 'PASS', continuous_supported_rail: 'PASS', route_ports_and_safe_point: 'PASS', complete_roof_and_ceiling: 'PASS', supported_hanging_lights: 'PASS', empty_barrel_entities_only: 'PASS', native_registry_and_states: 'PASS', native_four_rotations_placement: 'PASS', native_200_tick_stability: 'PASS', native_four_world_captures: 'PASS', five_negative_probes_each_rotation: 'PASS', four_internal_four_external_two_eye_views: 'PASS', visual_inspection: 'PASS' },
  native_summary: { rotations: native.rotations.map(rotation => rotation.rotation), records_per_rotation: 6831, checked_after_ticks: 27324, stabilization_ticks: 200, reachable_foot_positions_per_rotation: native.rotations.map(rotation => rotation.reachable_foot_positions), pedestrian_route_cells: 158, rail_cells: 27, grounded_posts: 4, supported_lanterns: 6, empty_barrels: 4, roof_cells: 621, external_fluid_cells: 0, world_entities: 0, negative_probes: native.negative_probes, forced_chunk_ticking: native.chunks_forced_to_tick },
  evidence: { static: 'qa/static.json', native: 'qa/native.json', native_log: 'qa/forge-gametest.log', views: 'qa/views.json', image_decode: 'qa/image-decode.json', visual_review: 'qa/visual-review.json', interior_sheet: 'views/interior-sheet.png', exterior_sheet: 'views/exterior-sheet.png', native_world_captures: native.rotations.map(rotation => rotation.world_capture), sha256_manifest: 'delivery-manifest.json', sha256_sums: 'SHA256SUMS.txt' },
  adaptations: visual.intentional_adaptations,
  safety: { deployed: false, production_saves_opened_or_modified: false, mods_directory_changed: false, shared_core_modified: false, other_deliveries_overwritten: false, paid_image_api_called: false, isolated_test_world: 'native-qa/run-isolated/world' },
  limitations: ['Software renders are not Minecraft client screenshots.', 'No full FarmingTales client, multiplayer or FPS validation.', 'No FT core or return-portal integration test.', 'Native occupancy negative probe checks this fixture; production full-volume/world-border/entity/fluid preflight and persistent idempotence remain integration responsibilities.', 'Ore blocks are vanilla mineable building materials; no minecart entity or loose tool items.'],
  integration_requirements: ['Use zone-M01 as the distinct persistent identity.', 'Preflight full 27x11x23 volume including explicit air and reject occupied/entity/fluid/world-border conflicts.', 'Rotate ports, feet/safe points and block states with the same native transform.', 'Preserve generated/player-modified rooms and persistent generation markers.', 'Do not deploy the isolated QA harness.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const relatives = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']
for (const directory of ['qa', 'views', 'tools', 'native-qa/src']) {
  for (const relative of await fs.readdir(path.join(root, directory), { recursive: true })) {
    const entry = path.join(directory, relative)
    if ((await fs.stat(path.join(root, entry))).isFile()) relatives.push(entry)
  }
}
relatives.push('native-qa/build.gradle', 'native-qa/settings.gradle', 'native-qa/gradle.properties')
const entries = []
for (const relative of [...new Set(relatives)].sort()) {
  const bytes = await fs.readFile(path.join(root, relative))
  entries.push({ path: relative.split(path.sep).join('/'), bytes: bytes.length, sha256: digest(bytes) })
}
const manifest = { id: 'zone-M01', status: 'PASS', source_sha256: sourceHash, reference_sha256: metadata.reference_sha256, primary_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'], entries, excludes: ['native-qa/.gradle', 'native-qa/build', 'native-qa/run-isolated disposable world/cache', 'delivery-manifest.json and SHA256SUMS.txt self fingerprints'] }
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify(manifest, null, 2) + '\n')
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), entries.map(entry => entry.sha256 + '  ' + entry.path).join('\n') + '\n' + await hashFile('delivery-manifest.json') + '  delivery-manifest.json\n')
console.log(JSON.stringify({ id: 'zone-M01', status: 'PASS', nbt_sha256: sourceHash, evidence_files: entries.length, qa_sha256: await hashFile('qa.json'), manifest_sha256: await hashFile('delivery-manifest.json') }, null, 2))
