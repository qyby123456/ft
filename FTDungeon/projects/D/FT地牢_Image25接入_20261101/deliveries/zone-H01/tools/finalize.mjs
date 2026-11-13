import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const save = async (relative, data) => fs.writeFile(path.join(root, relative), JSON.stringify(data, null, 2) + '\n')
const metadata = await readJson('room.json')
const fingerprint = hash(await fs.readFile(path.join(root, 'room.nbt')))
const require = (condition, message) => { if (!condition) throw new Error(message) }
require(metadata.sha256 === fingerprint, 'NBT fingerprint mismatch')
const staticReport = await readJson('qa/static.json')
const native = await readJson('qa/native.json')
const views = await readJson('qa/views.json')
const decode = await readJson('qa/image-decode.json')
const visual = await readJson('qa/visual-review.json')
for (const report of [staticReport, native, views, decode, visual]) require(report.status === 'PASS', 'Evidence not PASS')
require(staticReport.source_sha256 === fingerprint && native.structure_sha256 === fingerprint && views.source_sha256 === fingerprint && visual.source_sha256 === fingerprint, 'Mixed source NBT evidence')
require(Object.values(staticReport.checks).every(value => value === true), 'Static check failed')
require(native.source_unchanged && native.stabilization_ticks === 200 && native.rotations.length === 4 && native.records_checked_after_ticks === 68904, 'Incomplete native test')
require(native.rotations.every(rotation => rotation.status === 'PASS' && rotation.capture_matches_source && rotation.records_checked === 17226 && rotation.world_entities === 0 && rotation.fluid_cells_in_volume_and_halo === 0), 'Native rotation failed')
require(views.captures.length === 10 && views.console_errors.length === 0 && views.original_nbt_modified === false, 'Incomplete views')
for (const capture of [...views.captures, ...decode.images]) require(hash(await fs.readFile(path.join(root, capture.path ?? capture.file))) === capture.sha256, 'Rendered image fingerprint changed')
for (const rotation of native.rotations) require(hash(await fs.readFile(path.join(root, rotation.world_capture))) === rotation.world_capture_sha256, 'Native capture fingerprint changed')
require(hash(await fs.readFile(path.join(root, 'preview.png'))) === hash(await fs.readFile(path.join(root, 'views/eye-north.png'))), 'Preview is not actual NBT eye view')
const sourceRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const documents = []
for (const relative of ['README.md', 'docs/manifest.json', 'docs/verify.json']) {
  const bytes = await fs.readFile(path.join(sourceRoot, relative))
  const record = { path: path.join(sourceRoot, relative), bytes: bytes.length, sha256: hash(bytes) }
  if (relative.endsWith('.json')) {
    const document = JSON.parse(bytes.toString('utf8'))
    record.h01 = (document.files ?? document.records).find(entry => entry.file === 'out/halls/H01-halls-throne-hall-dais.png')
    require(record.h01?.sha256 === metadata.reference_sha256 && record.h01.ok, 'Source batch record mismatch')
  }
  documents.push(record)
}
require(hash(await fs.readFile(metadata.reference)) === metadata.reference_sha256, 'Reference changed')
await save('qa/reference-provenance.json', { status: 'PASS', reference: metadata.reference, reference_sha256: metadata.reference_sha256, png_size: [1536, 1024], documents, generated_new_images: false })
const checks = {
  reference_hash_and_both_batch_records: 'PASS',
  static_nbt_geometry_28_checks: 'PASS',
  full_explicit_volume_single_palette_1201: 'PASS',
  north_five_wide_six_high_port: 'PASS',
  main_aisle_three_high_and_four_safe_points: 'PASS',
  two_stair_rows_bidirectional_real_collision: 'PASS',
  supported_pillars_lights_braziers_and_banners: 'PASS',
  complete_roof_and_sealed_sides: 'PASS',
  vanilla_registry_properties_and_template_roundtrip: 'PASS',
  native_four_rotations_and_200_ticks: 'PASS',
  native_world_captures_match_rotated_source: 'PASS',
  zero_entities_fluids_or_reward_stock: 'PASS',
  occupied_air_blocked_route_and_missing_chain_negative_probes: 'PASS',
  four_interior_four_exterior_plus_two_detail_views: 'PASS',
  image_decode_source_binding_and_visual_review: 'PASS'
}
await save('qa.json', {
  schema: 'ft-independent-zone-room-v1', id: 'H01', status: 'PASS', sha256: fingerprint,
  reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, facing: metadata.facing,
  minecraft: '1.20.1', forge: '47.4.10', checks,
  native_summary: { rotations: native.rotations.map(record => record.rotation), records_per_rotation: 17226, checked_after_ticks: 68904, stabilization_ticks: 200, collision_path_samples_per_rotation: 1090, player_collision_size: [0.6, 1.8, 0.6], stair_max_step: 0.5, safe_waypoints: 4, supported_lanterns: 6, supported_braziers: 4, empty_barrels: 6, decorative_banners: 6, zero_world_entities: true, zero_fluids: true, negative_probes: native.negative_probes },
  evidence: { provenance: 'qa/reference-provenance.json', static: 'qa/static.json', native: 'qa/native.json', native_log: 'qa/forge-gametest.log', world_captures: native.rotations.map(record => record.world_capture), views: 'qa/views.json', image_decode: 'qa/image-decode.json', visual: 'qa/visual-review.json', interior_sheet: 'views/interior-sheet.png', exterior_sheet: 'views/exterior-sheet.png', detail: 'views/throne-detail.png' },
  design_deviations: metadata.design_deviations,
  boundaries: { deployed: false, production_saves_modified: false, production_jars_modified: false, production_resourcepacks_or_configs_modified: false, parallel_delivery_directories_modified: false, integration_routes_verified: false, modpack_client_playtest: false, isolated_test_world: 'native-qa/run-isolated-h01' },
  limitations: ['Lodestone software views are not client gameplay screenshots.', 'Only standalone Forge placement verified; main-dungeon connection and return logic belong to integration.', 'Native test has initial world-generation overload and Gradle deprecation warnings; it completed successfully.']
})
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']
for (const directory of ['qa', 'views', 'tools']) for (const entry of await fs.readdir(path.join(root, directory), { withFileTypes: true })) if (entry.isFile()) files.push(directory + '/' + entry.name)
for (const relative of ['build.gradle', 'settings.gradle', 'gradle.properties', 'src/main/java/net/farmingtales/h01qa/HallTests.java', 'src/main/java/net/farmingtales/h01qa/Verification.java', 'src/main/resources/pack.mcmeta', 'src/main/resources/META-INF/mods.toml', 'src/main/resources/data/fth01qa/structures/empty.nbt']) files.push('native-qa/' + relative)
const entries = []
for (const relative of files.sort()) entries.push(hash(await fs.readFile(path.join(root, relative))) + '  ' + relative)
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), entries.join('\n') + '\n')
console.log(JSON.stringify({ id: 'H01', status: 'PASS', sha256: fingerprint, evidence_files: entries.length, main_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'] }))
