import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const load = async file => JSON.parse(await fs.readFile(path.join(root, file), 'utf8'))
const metadata = await load('room.json')
const fingerprint = digest(await fs.readFile(path.join(root, 'room.nbt')))
if (metadata.sha256 !== fingerprint) throw new Error('Metadata fingerprint mismatch')
const evidenceFiles = ['qa/static.json', 'qa/forge-native.json', 'qa/capture-comparison.json', 'qa/views.json', 'qa/image-decode.json', 'qa/visual-review.json']
const evidence = []
for (const file of evidenceFiles) {
  const report = await load(file)
  if (report.status !== 'PASS') throw new Error('Evidence did not pass: ' + file)
  if (report.source_nbt_sha256 && report.source_nbt_sha256 !== fingerprint) throw new Error('Stale evidence: ' + file)
  evidence.push({ file, status: report.status, sha256: digest(await fs.readFile(path.join(root, file))) })
}
const native = await load('qa/forge-native.json')
const views = await load('qa/views.json')
for (const record of [...native.world_captures, ...views.captures]) {
  if (digest(await fs.readFile(path.join(root, record.file))) !== record.sha256) throw new Error('Evidence artifact changed: ' + record.file)
}
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
if (!log.includes('BUILD SUCCESSFUL') || !log.includes('C03_NATIVE_GAME_TEST_PASS rotations=4/4 records=17328 ticks=240 sha256=' + fingerprint)) throw new Error('Missing successful Forge log marker')
if (!Buffer.from(await fs.readFile(path.join(root, 'preview.png'))).equals(await fs.readFile(path.join(root, 'views/interior-north.png')))) throw new Error('Preview source mismatch')
const report = {
  id: 'C03', name: metadata.name, status: 'PASS', source_nbt_sha256: fingerprint,
  reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  primary_deliverables: ['room.nbt', 'room.json', 'preview.png', 'qa.json'], size: metadata.size,
  static_checks_passed: (await load('qa/static.json')).checks.length,
  route: { entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit, width: 5, clear_height: 3, main_route_cells: 95, static_conservative_air_only_reachable_cells: 143, native_collision_reachable_cells: 162 },
  stability: { rotations_passed: 4, snapshots_ticks: [0, 200, 240], records_per_rotation: 4332, total_records_after_ticks: 17328, native_captures_match_source: true, vines_per_rotation: 58, hanging_lanterns_per_rotation: 4, empty_barrels_per_rotation: 3, decorative_banners_per_rotation: 1, negative_probes_passed: 16, stable_between_200_and_240: true },
  visuals: { original_screenshots: 11, interior_views: 4, exterior_views: 4, eye_views: 2, shrine_detail: 1, sheets: ['views/interior-sheet.png', 'views/exterior-sheet.png'], renderer: views.renderer, actual_nbt: true, cutaway_preview_only: true },
  evidence, initial_attempt: { file: 'qa/attempt-1-native.json', status: 'FAIL', cause: 'The canSurvive-only negative vine probe permits support inherited from a vine above. The probe now directly validates every enabled vine face against a full solid backing block. The NBT did not change.' },
  scope: { independent_delivery_only: true, deployed: false, production_instance_modified: false, core_modified: false, player_worlds_opened: false, other_deliveries_modified: false },
  limitations: ['Lodestone renders are software previews, not Minecraft client screenshots.', 'Isolated Forge 47.4.10 GameTest is not full-modpack client, multiplayer or FPS acceptance.', 'Random tick speed was zero to separate natural vine growth from placement/support stability; scheduled and neighbor ticks ran.', 'Main-hall integration, return portals, production occupancy rejection and persistence remain integration-owner responsibilities.', 'Static reachability requires explicit air and is conservative; native collision checks additionally allow noncolliding decoration.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(report, null, 2) + '\n')
const collect = async directory => {
  const files = []
  for (const entry of await fs.readdir(path.join(root, directory), { withFileTypes: true })) {
    const relative = path.posix.join(directory, entry.name)
    if (entry.isDirectory()) files.push(...await collect(relative))
    else files.push(relative)
  }
  return files
}
const artifacts = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json', ...await collect('tools'), ...await collect('views'), ...await collect('qa'), ...await collect('forge/src'), 'forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties']
const lines = []
for (const file of artifacts.sort()) lines.push(digest(await fs.readFile(path.join(root, file))) + '  ' + file)
await fs.writeFile(path.join(root, 'SHA256SUMS.txt'), lines.join('\n') + '\n')
console.log(JSON.stringify({ status: 'PASS', id: 'C03', sha256: fingerprint, evidence_reports: evidence.length, hashed_artifacts: lines.length, primary_deliverables: report.primary_deliverables }))

