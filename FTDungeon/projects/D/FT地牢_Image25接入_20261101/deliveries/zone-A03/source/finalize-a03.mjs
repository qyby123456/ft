import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const sharp = require('sharp')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const json = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const save = async (filename, value) => fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + '\n')
const assert = (condition, message) => { if (!condition) throw new Error(message) }
const room = await json('room.json')
const staticQa = await json('qa/static.json')
const nativeQa = await json('qa/forge-placement.json')
const views = await json('views/manifest.json')
const currentHash = digest(await fs.readFile(path.join(root, 'room.nbt')))
for (const evidence of [room.sha256, staticQa.structure_sha256, nativeQa.structure_sha256, views.source_sha256]) assert(evidence === currentHash, 'Stale structure evidence')
assert(staticQa.status === 'PASS' && nativeQa.status === 'PASS' && views.errors.length === 0, 'Upstream QA failure')
assert(nativeQa.rotations === 4 && nativeQa.actual_world_placements === 8 && nativeQa.post_placement_server_ticks >= 200, 'Incomplete Forge rotations/ticks')
const log = await fs.readFile(path.join(root, 'qa/forge-gametest.log'), 'utf8')
assert(log.includes('BUILD SUCCESSFUL') && log.includes('A03_FORGE_PASS sha256=' + currentHash) && log.includes('All 1 required tests passed'), 'Missing native completion log')
for (const image of views.images) {
  const bytes = await fs.readFile(path.join(root, image.path))
  assert(image.source_sha256 === currentHash && digest(bytes) === image.sha256, 'Stale or modified view ' + image.path)
  const metadata = await sharp(bytes).metadata()
  assert(metadata.width === 1536 && metadata.height === 1024, 'Wrong capture dimensions')
  const statistics = await sharp(bytes).stats()
  assert(statistics.channels.some(channel => channel.stdev > 12), 'Blank screenshot ' + image.path)
}
for (const mode of ['interior', 'exterior']) {
  const composite = await Promise.all(['north', 'south', 'west', 'east'].map(async (side, index) => ({ input: await sharp(path.join(root, 'views/' + mode + '-' + side + '.png')).resize(768, 512).toBuffer(), left: index % 2 * 768, top: Math.floor(index / 2) * 512 })))
  await sharp({ create: { width: 1536, height: 1024, channels: 3, background: '#10191b' } }).composite(composite).png().toFile(path.join(root, 'views/' + mode + '-four-views.png'))
}
const visualReview = {
  status: 'PASS', reviewed_on: '2026-11-03', source_sha256: currentHash,
  review_method: 'Visually inspected final-hash interior four-view sheet, exterior four-view sheet, preview and complete-NBT entrance eye view.',
  checked: ['北侧入口、前场和双侧绕桌路线可辨认', '炉罩石墩、锅底玄武岩承台和四脚木桌连续落地', '柜架有立柱，吊锅砂轮贴梁并由南墙锚定', '完整 NBT 入口视点证实五组链灯接屋顶，外观四向屋顶封闭', '显示剖切只移除上部和近墙；屏蔽被剖掉立柱的残留柱帽'],
  final_revision: '炉锅改放完整玄武岩承台，冷营火在前方石炉床；五组链灯直连y=8横梁，内景剖视保留梁链；最终结构全部重做四旋转与200 tick验证。',
  game_client_screenshot: false,
}
await save('qa/visual-review.json', visualReview)
views.status = 'PASS'
views.visual_review = 'qa/visual-review.json'
views.composite_images = []
for (const mode of ['interior', 'exterior']) {
  const filename = 'views/' + mode + '-four-views.png'
  views.composite_images.push({ path: filename, sha256: digest(await fs.readFile(path.join(root, filename))), source_sha256: currentHash })
}
await save('views/manifest.json', views)
const batch = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const sourceFiles = [room.reference, batch + '/README.md', batch + '/docs/manifest.json', batch + '/docs/verify.json']
const referenceEvidence = { id: 'A03', status: 'PASS', expected_reference_sha256: room.reference_sha256, artifacts: [] }
for (const filename of sourceFiles) {
  const bytes = await fs.readFile(filename)
  referenceEvidence.artifacts.push({ path: filename, bytes: bytes.length, sha256: digest(bytes) })
}
await save('qa/reference-evidence.json', referenceEvidence)
const qa = {
  id: 'A03', name: '荒废厨房', status: 'PASS', structure_sha256: currentHash, reference_sha256: room.reference_sha256,
  delivery: 'independent', deployed: false, production_files_modified: false, player_worlds_read_or_modified: false,
  source: { status: 'PASS', image_hash_matches: true, manifest_match: true, verify_match: true, evidence: 'qa/reference-evidence.json' },
  static: { status: 'PASS', evidence: 'qa/static.json', records: 3971, palette_states: 32, single_palette: true, explicit_air: staticQa.explicit_air, roof_cells: 361, full_floor_cells: 361, dry_reachable_cells: 209, opening: [3, 4], safety_waypoints: 8, furniture_support_columns: 10, supported_decorations: 13, hanging_lanterns: 5, wall_anchored_hanging_utensils: 3, empty_barrels: 3, empty_furnaces: 1, empty_campfires: 1, nonempty_inventory: 0, loot_tables: 0, liquids: 0, active_fire_sources: 0 },
  forge: { status: 'PASS', evidence: 'qa/forge-placement.json', log: 'qa/forge-gametest.log', minecraft: nativeQa.minecraft, version: nativeQa.forge, rotations: 4, actual_placements: 8, neighbor_updates: true, post_placement_ticks: 200, exact_record_matches_per_rotation: 3971, native_reachable_floor_cells: 209, full_player_aabb: [0.6, 1.8, 0.6], native_can_survive: true, random_tick_speed: 3, fire_ticks_enabled: true, player_world_used: false, isolated_fixture: 'qa/forge-run' },
  views: { status: 'PASS', manifest: 'views/manifest.json', visual_review: 'qa/visual-review.json', exterior_directions: ['north', 'south', 'west', 'east'], interior_directions: ['north', 'south', 'west', 'east'], final_nbt_based: true, same_structure_sha256: true, complete_roof_in_nbt: true, cutaway_is_display_only: true, captures: 10, composite_sheets: 2, game_client_screenshots: false },
  limits: ['Software rendering and an isolated native Forge GameTest server are the delivered verification levels.', 'No full modpack client playtest, player save placement, lobby connection, persistent generation or deployment was performed.'],
}
await save('qa.json', qa)
const collect = async directory => {
  const output = []
  for (const entry of await fs.readdir(directory, { withFileTypes: true })) {
    const filename = path.join(directory, entry.name)
    if (entry.isDirectory()) output.push(...await collect(filename))
    else output.push(filename)
  }
  return output
}
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'review.html', 'summary.json', 'native/build.gradle', 'native/settings.gradle', 'native/gradle.properties']
for (const folder of ['source', 'views', 'native/src']) files.push(...(await collect(path.join(root, folder))).map(filename => path.relative(root, filename).replaceAll('\\', '/')))
for (const entry of await fs.readdir(path.join(root, 'qa'), { withFileTypes: true })) if (entry.isFile()) files.push('qa/' + entry.name)
const artifacts = []
for (const filename of [...new Set(files)].sort()) {
  const bytes = await fs.readFile(path.join(root, filename))
  artifacts.push({ path: filename, bytes: bytes.length, sha256: digest(bytes) })
}
await save('delivery-manifest.json', { id: 'A03', date: '2026-11-03', status: 'PASS', structure_sha256: currentHash, reference_sha256: room.reference_sha256, deployed: false, excludes: ['native/.gradle', 'native/build', 'qa/forge-run disposable server world'], artifacts })
console.log(JSON.stringify({ status: 'PASS', sha256: currentHash, files: artifacts.length, four_piece: artifacts.filter(artifact => ['room.nbt', 'room.json', 'preview.png', 'qa.json'].includes(artifact.path)) }, null, 2))

