import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const fingerprint = async relative => createHash('sha256').update(await fs.readFile(path.join(root, relative))).digest('hex')
const metadata = await read('room.json')
const staticReport = await read('qa/static.json')
const native = await read('qa/forge-native.json')
const render = await read('qa/render.json')
const sourceHash = await fingerprint('room.nbt')
for (const report of [staticReport, native, render]) {
  if (report.status !== 'PASS' || (report.structure_sha256 ?? report.source_sha256) !== sourceHash) throw new Error('Missing, failing or stale verification evidence')
}
if (metadata.sha256 !== sourceHash || native.rotations.length !== 4 || native.rotations.some(item => !item.capture_exact_match || item.status !== 'PASS')) throw new Error('Metadata or native rotations invalid')
for (const item of native.rotations) {
  if (await fingerprint(item.capture) !== item.capture_sha256) throw new Error(`Capture fingerprint mismatch: ${item.capture}`)
}
if (!String(await fs.readFile(path.join(root, 'qa/forge-gametest.log'))).includes('BUILD SUCCESSFUL')) throw new Error('Forge process did not finish successfully')
const visual = await read('qa/visual-review.json')
if (visual.status !== 'PASS' || visual.source_sha256 !== sourceHash) throw new Error('Visual review missing or stale')
for (const view of render.views) {
  const reviewed = visual.images.find(image => image.path === view.path)
  if (!reviewed || reviewed.sha256 !== await fingerprint(view.path)) throw new Error(`Visual evidence changed since review: ${view.path}`)
}
const qa = {
  id: metadata.id, batch_id: 'M18', status: 'PASS', scope: 'Independent structure delivery; not production integration or gameplay acceptance',
  source_sha256: sourceHash, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit,
  static: { status: staticReport.status, checks: staticReport.checks.length, block_records: 8640, non_air_blocks: staticReport.solid_and_decorative_blocks, report: 'qa/static.json' },
  forge: { status: native.status, minecraft: native.minecraft, version: native.forge, rotations: 4, stabilization_ticks: native.stabilization_ticks, records_checked_after_ticks: native.records_checked_after_ticks, captures_exact: true, report: 'qa/forge-native.json', log: 'qa/forge-gametest.log' },
  render: { status: render.status, views: render.views.length, four_view: 'preview/four-view.png', complete_exterior: 'preview/complete-exterior.png', report: 'qa/render.json', visual_review: 'qa/visual-review.json', game_screenshots: false, cutaway_only_in_preview: true },
  safety: { no_deployment: true, player_saves_modified: false, core_code_modified: false, paid_image_requests: 0, only_vanilla_1_20_1: true, no_entities_loot_or_block_entities: true, nonmelting_ice_only: true },
  limitations: ['No FT core catalog/gallery route/occupancy/idempotence verification.', 'No full modpack client, multiplayer or performance test.', 'Software previews use approximate candle glow; native light and snow safety verified separately.'],
}
await fs.writeFile(path.join(root, 'qa.json'), `${JSON.stringify(qa, null, 2)}\n`)
const included = [
  'room.nbt', 'room.json', 'preview.png', 'qa.json', 'recipe.mjs', 'summary.json', 'README.md',
  'tools/check.mjs', 'tools/render.mjs', 'tools/run-forge.ps1', 'tools/finalize.mjs', 'tools/record-visual-review.mjs',
  'qa/static.json', 'qa/forge-native.json', 'qa/render.json', 'qa/visual-review.json', 'qa/forge-gametest.log', 'qa/forge-compile-attempt-1.log', 'qa/preview-server.log',
  'preview/four-view.png', ...render.views.map(view => view.path), ...native.rotations.map(item => item.capture),
  'forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties', 'forge/src/main/resources/pack.mcmeta', 'forge/src/main/resources/META-INF/mods.toml',
  'forge/src/main/resources/data/ftcorridorm18qa/structures/empty.nbt', 'forge/src/main/java/net/farmingtales/corridorm18qa/CorridorVerification.java',
]
const files = []
for (const relative of included) files.push({ path: relative.replaceAll('\\', '/'), bytes: (await fs.stat(path.join(root, relative))).size, sha256: await fingerprint(relative) })
const manifest = { id: metadata.id, status: 'PASS', created_at: new Date().toISOString(), user_timezone: 'Asia/Shanghai', source_sha256: sourceHash, files, exclusions: ['Local Gradle cache and build products', 'Isolated qa/forge-run test world; not a player save'], deployed: false }
await fs.writeFile(path.join(root, 'delivery-manifest.json'), `${JSON.stringify(manifest, null, 2)}\n`)
console.log(JSON.stringify({ status: qa.status, size: metadata.size, sha256: sourceHash, manifest_files: files.length, native_rotations: 4 }, null, 2))
