import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

if (!process.argv.includes('--visual-reviewed')) throw new Error('Inspect the actual NBT hero, reverse, and eight directional images before passing --visual-reviewed')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const digest = async name => createHash('sha256').update(await fs.readFile(path.join(root, name))).digest('hex')
const sha256 = await digest('room.nbt')
const metadata = await read('room.json')
const staticReport = await read('qa/static.json')
const nativeReport = await read('qa/native.json')
const renderReport = await read('qa/render.json')
const snapshotReport = await read('qa/native-snapshots.json')
for (const report of [metadata, staticReport, nativeReport]) if (report.sha256 !== sha256) throw new Error('Stale fingerprint')
for (const report of [staticReport, nativeReport, renderReport]) if (report.status !== 'PASS') throw new Error('Incomplete QA')
if (snapshotReport.status !== 'PASS' || snapshotReport.source_sha256 !== sha256 || snapshotReport.matched_records !== 39000) throw new Error('Native snapshot reread failed or stale')
if (renderReport.source_sha256 !== sha256 || renderReport.views.length !== 10) throw new Error('Missing or stale actual-NBT views')
if (nativeReport.rotations !== 4 || nativeReport.total_placed_records !== 31200 || nativeReport.stability_ticks < 400 || nativeReport.after_400_ticks.length !== 4) throw new Error('Incomplete Forge placement/stability proof')
const log = await fs.readFile(path.join(root, 'qa/forge-gametest.log'), 'utf8')
if (!log.includes('M06_NATIVE_GAME_TEST_PASS') || !log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Forge final success markers absent')
for (const view of renderReport.views) if (await digest(view.file) !== view.sha256) throw new Error('Preview changed: ' + view.file)
const files = ['room.nbt', 'room.json', 'preview.png', 'recipe.mjs', 'summary.json', 'README.md', 'Run-QA.ps1', 'tools/static-qa.mjs', 'tools/review-native-snapshots.mjs', 'tools/finalize.mjs', 'tools/viewer.html', 'tools/viewer.mjs', 'tools/preview-server.mjs', 'tools/capture-preview.mjs', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/java/net/farmingtales/corridorm06/CorridorM06Test.java', 'native-test/src/main/resources/META-INF/mods.toml', 'native-test/src/main/resources/pack.mcmeta', 'native-test/src/main/resources/data/ftcorridorm06qa/structures/empty.nbt', 'qa/static.json', 'qa/native.json', 'qa/native-snapshots.json', 'qa/render.json', 'qa/render-capture.log', 'qa/forge-gametest.log', 'qa/forge-gametest-attempt1-missing-pack-metadata.log', 'qa/forge-gametest-attempt2-terrain-occupied.log', 'qa/forge-gametest-attempt3-iron-bar-shape-drift.log', 'qa/attempt3-before-iron-bar-repair.nbt', 'qa/attempt3-before-iron-bar-repair.json', 'qa/native-roundtrip.nbt', 'preview/four-direction-interior-exterior.png', ...renderReport.views.map(view => view.file), ...['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90'].map(rotation => 'qa/native-placed-' + rotation + '.nbt')]
const fileHashes = {}
for (const file of files) fileHashes[file] = await digest(file)
const report = {
  status: 'PASS', id: metadata.id, source_id: metadata.source_id, name: metadata.name,
  completed_at: new Intl.DateTimeFormat('sv-SE', { dateStyle: 'short', timeStyle: 'medium', timeZone: 'Asia/Shanghai' }).format(new Date()) + ' Asia/Shanghai',
  sha256, reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit,
  static: { status: 'PASS', checks: staticReport.checks.length, records: 7800, evidence: 'qa/static.json' },
  visuals: { status: 'PASS', evidence: 'qa/render.json', directions: ['north', 'south', 'west', 'east'], interior_cutaway_views: 4, complete_exterior_views: 4, eye_level_views: 2, visually_inspected: true, software_render_not_game_screenshot: true, roof_unchanged_in_nbt: true },
  forge: { status: 'PASS', evidence: 'qa/native.json', log: 'qa/forge-gametest.log', minecraft: '1.20.1', forge: '47.4.10', rotations: 4, records_per_rotation: 7800, total_placed_records: 31200, stability_ticks: 400, actual_server_level_placement: true, native_neighbor_shape_updates: true, safe_route_and_return_all_rotations: true, production_world_used: false },
  native_snapshot_reread: { status: 'PASS', evidence: 'qa/native-snapshots.json', native_roundtrips: 1, actual_settled_world_rotations: 4, matched_records: 39000 },
  safety: { production_deployed: false, production_save_modified: false, other_room_modified: false, paid_image_generation_called: false, entities: 0, block_entities: 0, live_fluids: 0, real_fire: 0 },
  limitations: ['Not a complete modpack client or multiplayer playtest.', 'Not deployed or linked into the live dungeon; the integrator must validate destination occupancy, entities, fluids, boundaries, persistence and return routes before placement.', 'Explicit air records are not authorization to overwrite existing structures.', 'First Forge attempt failed before structure testing because test-pack metadata was missing; second attempt safely refused naturally occupied test volume at y=100; third attempt exposed generic Lodestone iron-bar connection drift next to candles. Logs and the rejected NBT are retained; final fixtures use y=240 and explicit native-compatible iron-bar states.', 'Software previews do not reproduce client candle particles, dynamic lighting or shaders; review views use brighter inspection lighting.'],
  files: fileHashes,
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256, files: Object.keys(fileHashes).length, forge: report.forge, size: report.size }, null, 2))
