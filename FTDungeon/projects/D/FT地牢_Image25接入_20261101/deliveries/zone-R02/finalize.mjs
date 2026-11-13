import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.dirname(fileURLToPath(import.meta.url))
const readJson = async file => JSON.parse((await fs.readFile(path.join(root, file), 'utf8')).replace(/^\uFEFF/, ''))
const hashBytes = bytes => createHash('sha256').update(bytes).digest('hex')
const hashFile = async file => hashBytes(await fs.readFile(path.resolve(root, file)))
const metadata = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const render = await readJson('qa/render-evidence.json')
const native = await readJson('qa/forge-native.json')
const processReport = await readJson('qa/forge-process.json')
const fingerprint = await hashFile('room.nbt')
assert.equal(metadata.id, 'zone-R02')
assert.equal(metadata.sha256, fingerprint)
assert.equal(hashBytes(await fs.readFile(metadata.reference)), metadata.reference_sha256)
for (const report of [staticReport, native]) {
  assert.equal(report.status, 'PASS')
  assert.equal(report.structure_sha256, fingerprint)
}
assert.equal(render.status, 'PASS')
assert.deepEqual(render.browser_errors, [])
assert.equal(processReport.exit_code, 0)
assert.equal(native.rotations.length, 4)
assert.equal(new Set(native.rotations.map(rotation => rotation.rotation)).size, 4)
assert.equal(native.records_checked_after_ticks, 19404)
assert.equal(native.stabilization_ticks, 200)
assert(native.world_ticks_elapsed >= 200)
assert(native.forced_fixture_chunks > 0)
assert.equal(native.all_fixture_chunks_forced_throughout, true)
assert.equal(native.negative_collision_probes, 4)
assert.equal(native.negative_banner_support_probes, 4)
assert.equal(native.negative_table_lamp_support_probes, 4)
assert.equal(render.views.length, 11)
for (const view of render.views) {
  assert.equal(view.source_sha256, fingerprint)
  assert.equal(view.original_records, 4851)
  await fs.access(view.path)
}
for (const rotation of native.rotations) {
  assert.equal(rotation.capture_matches_source, true)
  assert.equal(rotation.records_checked, 4851)
  assert.equal(rotation.reachable_foot_positions, 195)
  assert.equal(rotation.empty_or_decorative_block_entities, 8)
  assert.equal(rotation.roof_anchored_chain_blocks, 12)
  assert.equal(rotation.supported_lanterns, 5)
  assert.equal(await hashFile(rotation.world_capture), rotation.capture_sha256)
}
const forgeLog = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert(forgeLog.includes('All 1 required tests passed') && forgeLog.includes('BUILD SUCCESSFUL'))
const historyFiles = (await fs.readdir(path.join(root, 'qa'))).filter(file => /^previous-.*forge-(native\.json|process\.json|run\.log)$/.test(file)).sort().map(file => `qa/${file}`)
const revisions = []
for (const file of historyFiles.filter(file => file.endsWith('forge-native.json'))) {
  const previous = await readJson(file)
  revisions.push({ report: file, status: previous.status, structure_sha256: previous.structure_sha256, error: previous.error ?? null, matching_room_unchanged: previous.structure_sha256 === fingerprint })
}
await fs.writeFile(path.join(root, 'qa/fixture-revisions.json'), JSON.stringify({ status: 'RESOLVED', final_status: native.status, structure_sha256: fingerprint, fixture_only_corrections: ['Explicitly forced every fixture/air-halo chunk for actual world tick stability.', 'Created fresh isolated worlds before rerun; archived previous worlds inside zone-R02 only.', 'Moved fixtures to absolute Y=280 after low terrain/cave preparation produced environmental torch/seed drops.', 'Did not remove found entities or disable block drops; require empty sky fixtures before placement and zero entities after stabilization.'], final_forced_chunks: native.forced_fixture_chunks, final_world_ticks_elapsed: native.world_ticks_elapsed, previous_results: revisions, preserved_logs_and_reports: historyFiles, isolated_world_archive: 'qa/fixture-history/' }, null, 2) + '\n')
const referenceEvidence = {
  status: 'PASS', reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  expected_image: { bytes: 1972870, width: 1536, height: 1024, mode: 'RGB' },
  source_documents: await Promise.all([metadata.reference_manifest, metadata.reference_verify].map(async file => ({ path: file, sha256: hashBytes(await fs.readFile(file)) }))),
  matched_records: staticReport.checks.filter(check => check.test.startsWith('reference_')),
}
await fs.writeFile(path.join(root, 'qa/reference.json'), JSON.stringify(referenceEvidence, null, 2) + '\n')
const visualReport = {
  status: 'PASS', structure_sha256: fingerprint,
  method: 'Agent inspected final-NBT four-direction internal and complete-shell contact sheet, overview/roof/entry sheet, and full-size east/internal entry images; cross-checked with static and Forge support/collision evidence.',
  evidence: ['preview/four-view-sheet.png', 'preview/overview-roof-entrance.png', ...render.views.map(view => `preview/${view.view}.png`)],
  checks: [
    { name: 'reference_theme_and_furnishing', status: 'PASS', note: 'Mossy cracked masonry, grounded red-banded pillars, flags, command table and chairs, empty chest/crates, boarded opening, weapon rack, cold brazier, carpet and edge rubble remain identifiable.' },
    { name: 'walls_pillars_beams_and_roof', status: 'PASS', note: 'Four grounded pillars meet beam grid and full double stone roof. All four shell views are uncut. Interior sections hide roof and near wall only in render copies.' },
    { name: 'wall_and_hanging_support', status: 'PASS', note: 'Two wall banners have direct masonry backing; four lanterns hang on twelve chain blocks connected to roof, fifth stands on full upper slab tabletop. Native survival checks and eight negative support probes confirm support.' },
    { name: 'grounded_furniture', status: 'PASS', note: 'Command table rests on four floor-mounted fence legs; both chairs, crates, weapon-rack frame and cold brazier stand on complete floor or full block base. Native collision and table support checks pass.' },
    { name: 'entry_safe_point_and_circulation', status: 'PASS', note: 'North-central three-wide three-high entry reaches [10,1,6] and clear table-side ring routes; 195 reachable foot positions in all four rotations. Side rubble, carpet and high webs do not obstruct required lanes.' },
    { name: 'separate_decorative_openings', status: 'PASS', note: 'East boarded niche remains backed by closed wood shell; south window remains barred with supported stone sill. These are decorative, not connection exits.' },
    { name: 'safe_brazier_inventory_and_liquids', status: 'PASS', note: 'Brazier is unlit. Eight block entities are empty or banners; no loot table, water, lava, dropped entity or active fire.' },
  ],
  software_renderer_limitations: ['Lodestone flag texture is not authoritative for native banner pattern display; Forge checks preserve the exact Patterns NBT.', 'Screenshots are software NBT renders, not a complete modpack client play test.'],
  render_only_corrections: ['Changed overview camera to southeast so flags and supply/weapon corner are visible.', 'Added explicit empty favicon to remove browser 404 from render evidence.'],
  adaptations: metadata.concept_adaptations, remaining_required_corrections: [],
}
await fs.writeFile(path.join(root, 'qa/four-view-review.json'), JSON.stringify(visualReport, null, 2) + '\n')
const qa = {
  id: metadata.id, design_id: metadata.design_id, status: 'PASS', structure_sha256: fingerprint, reference_sha256: metadata.reference_sha256,
  static: { status: 'PASS', tests: staticReport.test_count, report: 'qa/static.json', explicit_records: 4851, non_air_blocks: 2800, reachable_foot_positions: 195 },
  reference: { status: 'PASS', report: 'qa/reference.json', manifest_and_verify_matched: true },
  four_view: { status: 'PASS', internal_directions: 4, full_shell_directions: 4, total_render_views: 11, roof_and_entry_reviewed: true, report: 'qa/four-view-review.json', render_evidence: 'qa/render-evidence.json', sheet: 'preview/four-view-sheet.png', overview_roof_entrance: 'preview/overview-roof-entrance.png' },
  forge_native: { status: 'PASS', minecraft: native.minecraft, forge: native.forge, rotations: 4, records_per_rotation: 4851, records_after_ticks: 19404, stabilization_ticks: 200, world_ticks_elapsed: native.world_ticks_elapsed, forced_fixture_chunks: native.forced_fixture_chunks, all_fixture_chunks_forced_throughout: true, collision_negative_probes: 4, banner_support_negative_probes: 4, table_lamp_support_negative_probes: 4, native_captures_match: true, empty_or_decorative_block_entities: 8, random_tick_speed: native.random_tick_speed, report: 'qa/forge-native.json', process: 'qa/forge-process.json', log: 'qa/forge-run.log' },
  fixture_revision_history: 'qa/fixture-revisions.json',
  integration_readiness: 'Standalone candidate ready for coordinator review; no FT slot or route assigned by this thread',
  deployed: false, production_instance_modified: false, player_saves_modified: false, other_delivery_folders_modified: false, paid_image_calls: 0,
  limitations: native.limitations,
  generated_at: new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Shanghai', dateStyle: 'short', timeStyle: 'medium' }).format(new Date()) + ' Asia/Shanghai',
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const files = [
  'INTEGRATION.md',
  'room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'summary.json', 'recipe.mjs', 'audit.mjs', 'finalize.mjs', 'verify.mjs', 'preview-server.mjs', 'capture-preview.mjs', 'make-sheet.py', 'run-native.ps1', 'README.md', 'HANDOFF.md', 'review.html', 'viewer/index.html', 'viewer/main.mjs',
  'qa/reference.json', 'qa/static.json', 'qa/four-view-review.json', 'qa/render-evidence.json', 'qa/forge-native.json', 'qa/forge-process.json', 'qa/forge-run.log', 'qa/fixture-revisions.json', 'preview/four-view-sheet.png', 'preview/overview-roof-entrance.png',
  'native/build.gradle', 'native/settings.gradle', 'native/gradle.properties', 'native/src/main/java/net/farmingtales/zoner02qa/VerificationMod.java', 'native/src/main/java/net/farmingtales/zoner02qa/RoomGameTests.java', 'native/src/main/resources/META-INF/mods.toml', 'native/src/main/resources/pack.mcmeta', 'native/src/main/resources/data/zoner02qa/structures/empty.nbt',
  ...render.views.map(view => `preview/${view.view}.png`), ...native.rotations.map(rotation => rotation.world_capture.replaceAll('\\', '/')), ...historyFiles,
]
const manifest = []
for (const file of files) manifest.push({ path: file, sha256: await hashFile(file), bytes: (await fs.stat(path.join(root, file))).size })
await fs.writeFile(path.join(root, 'manifest.json'), JSON.stringify({ id: metadata.id, structure_sha256: fingerprint, reference_sha256: metadata.reference_sha256, files: manifest }, null, 2) + '\n')
console.log(JSON.stringify({ status: qa.status, structure_sha256: fingerprint, static_tests: staticReport.test_count, rotations: 4, records: 19404, ticks: 200, hashed_files: manifest.length }, null, 2))
