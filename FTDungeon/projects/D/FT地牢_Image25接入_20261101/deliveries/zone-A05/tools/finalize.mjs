import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

if (!process.argv.includes('--visual-reviewed')) throw new Error('Inspect all actual-NBT views before finalizing')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async name => JSON.parse(await fs.readFile(path.join(root, name), 'utf8'))
const digest = async name => createHash('sha256').update(await fs.readFile(path.join(root, name))).digest('hex')
const sha256 = await digest('room.nbt')
const metadata = await read('room.json')
const staticReport = await read('qa/static.json')
const nativeReport = await read('qa/native.json')
const renderReport = await read('qa/render.json')
const snapshotReport = await read('qa/native-snapshots.json')
const visualReview = await read('qa/visual-review.json')
if (metadata.sha256 !== sha256 || nativeReport.sha256 !== sha256 || [staticReport, renderReport, snapshotReport].some(report => report.source_sha256 !== sha256)) throw new Error('Stale artifact fingerprints')
if ([staticReport, nativeReport, renderReport, snapshotReport, visualReview].some(report => report.status !== 'PASS') || visualReview.source_sha256 !== sha256) throw new Error('Incomplete QA')
if (staticReport.checks.some(check => check.status !== 'PASS') || snapshotReport.matched_records !== 24255 || renderReport.views.length !== 10) throw new Error('Missing QA coverage')
if (nativeReport.rotations !== 4 || nativeReport.total_placed_records !== 19404 || nativeReport.stability_ticks < 400 || nativeReport.after_400_ticks.length !== 4) throw new Error('Incomplete Forge placement/stability evidence')
if ([...nativeReport.initial, ...nativeReport.after_400_ticks].some(rotation => rotation.empty_containers !== 23 || rotation.container_ray_reach_checks !== 23 || rotation.full_roof_cells !== 441 || rotation.reachable_feet !== 277)) throw new Error('Incomplete native content coverage')
const log = await fs.readFile(path.join(root, 'qa/forge-gametest.log'), 'utf8')
if (!log.includes('A05_NATIVE_GAME_TEST_PASS') || !log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Forge success markers absent')
for (const view of renderReport.views) if (await digest(view.file) !== view.sha256) throw new Error('Preview changed: ' + view.file)
for (const snapshot of snapshotReport.snapshots) if (await digest(snapshot.file) !== snapshot.sha256) throw new Error('Snapshot changed: ' + snapshot.file)
const files = ['room.nbt', 'room.json', 'preview.png', 'recipe.mjs', 'summary.json', 'README.md', 'Run-QA.ps1',
  'tools/static-qa.mjs', 'tools/review-native-snapshots.mjs', 'tools/finalize.mjs', 'tools/viewer.html', 'tools/viewer.mjs', 'tools/preview-server.mjs', 'tools/capture-preview.mjs',
  'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/java/net/farmingtales/zonea05/ZoneA05Test.java',
  'native-test/src/main/resources/META-INF/mods.toml', 'native-test/src/main/resources/pack.mcmeta', 'native-test/src/main/resources/data/ftzonea05qa/structures/empty.nbt',
  'reference/A05-concept.png', 'reference/prompt.txt', 'reference/README.md', 'reference/manifest.json', 'reference/verify.json',
  'qa/static.json', 'qa/native.json', 'qa/native-snapshots.json', 'qa/render.json', 'qa/visual-review.json', 'qa/render-capture.log', 'qa/forge-gametest.log',
  'qa/native-roundtrip.nbt', 'preview/four-direction-interior-exterior.png', ...renderReport.views.map(view => view.file), ...snapshotReport.snapshots.map(snapshot => snapshot.file)]
const attemptFiles = (await fs.readdir(path.join(root, 'qa'))).filter(name => name.startsWith('forge-gametest-') && name.endsWith('.log'))
files.push(...attemptFiles.map(name => 'qa/' + name))
const fileHashes = {}
for (const filename of new Set(files)) fileHashes[filename] = await digest(filename)
const report = {
  status: 'PASS', id: metadata.id, name: metadata.name, source_id: metadata.source_id,
  completed_at: new Intl.DateTimeFormat('sv-SE', { dateStyle: 'short', timeStyle: 'medium', timeZone: 'Asia/Shanghai' }).format(new Date()) + ' Asia/Shanghai',
  sha256, reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, facing: metadata.facing,
  static: { status: 'PASS', checks: staticReport.checks.length, records: 4851, evidence: 'qa/static.json' },
  visuals: { status: 'PASS', evidence: 'qa/render.json', directions: ['north', 'south', 'west', 'east'], interior_cutaway_views: 4, complete_exterior_views: 4, eye_level_views: 2, visually_inspected: true, software_render_not_game_screenshot: true, roof_unchanged_in_nbt: true },
  forge: { status: 'PASS', evidence: 'qa/native.json', log: 'qa/forge-gametest.log', minecraft: '1.20.1', forge: '47.4.10', rotations: 4, records_per_rotation: 4851, total_placed_records: 19404, stability_ticks: 400, actual_server_level_placement: true, native_neighbor_shape_updates: true, empty_containers_per_rotation: 23, empty_slots_per_rotation: 621, player_ray_reach_all_containers: true, safe_route_and_return_all_rotations: true, production_world_used: false },
  native_snapshot_reread: { status: 'PASS', evidence: 'qa/native-snapshots.json', native_roundtrips: 1, actual_settled_world_rotations: 4, matched_records: 24255, empty_container_comparisons: 115 },
  safety: { production_deployed: false, production_save_modified: false, other_room_modified: false, paid_image_generation_called: false, entities: 0, block_entities: 23, container_items: 0, loot_tables: 0, live_fluids: 0, real_fire: 0 },
  deviations: [...metadata.deviations, '原提示词要求四柱；按实际成图柱列观感落为六根柱。', '概念图未标尺寸，本次落为21×11×21；检查光照比概念图更亮，未复现暗光、破灯和反射。'],
  limitations: ['软件预览采用检查光照，不是实机截图，不代表整合包着色器、光照或 FPS。', 'Forge 验证运行在本目录独立 GameTestServer；不是完整整合包客户端/多人联机验收。', '未部署或连接正式地牢；接入时仍需检查目标占用、实体、液体、边界、幂等标记和返程。', '显式空气记录不构成覆盖现有建筑的授权。'],
  files: fileHashes,
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256, files: Object.keys(fileHashes).length, size: report.size, forge: report.forge }, null, 2))
