import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const requireResult = (condition, message) => { if (!condition) throw new Error(message) }
const room = await readJson('room.json')
const staticQa = await readJson('qa/static.json')
const native = await readJson('qa/native.json')
const rendering = await readJson('qa/render.json')
const visual = await readJson('qa/visual-review.json')
const hash = digest(await fs.readFile(path.join(root, 'room.nbt')))
for (const report of [room, staticQa, native, rendering, visual]) {
  requireResult(report.sha256 === hash, 'Evidence fingerprint does not match final room.nbt')
}
for (const report of [staticQa, native, rendering, visual]) requireResult(report.status === 'PASS', 'A required QA check failed')
requireResult(native.rotations === 4 && native.settle_ticks >= 600, 'Native placement stability evidence incomplete')
requireResult(native.initial.length === 4 && native.after_600_ticks.length === 4, 'Four-rotation snapshots missing')
requireResult(native.record_comparisons_per_snapshot === 36244, 'Unexpected native comparison count')
const requiredViews = ['interior', 'exterior'].flatMap(kind => ['north', 'east', 'south', 'west'].map(direction => `preview/${kind}-${direction}.png`))
for (const filename of requiredViews) {
  const record = rendering.records.find(record => record.file === filename)
  requireResult(record && record.sha256 === digest(await fs.readFile(path.join(root, filename))), `View missing or changed: ${filename}`)
}
const logNames = (await fs.readdir(path.join(root, 'qa'))).filter(name => /^gametest-.*\.log$/.test(name)).sort()
const logs = []
for (const name of logNames) {
  const content = await fs.readFile(path.join(root, 'qa', name), 'utf8')
  const success = content.includes(`CORRIDOR_M08_GAME_TEST_PASS: rotations=4/4; records=36244; ticks=600; sha256=${hash}`) && content.includes('BUILD SUCCESSFUL')
  const failure = content.match(/nativeplacementfourrotations failed! ([^\r\n]+)/)?.[1] ?? null
  logs.push({ file: `qa/${name}`, status: success ? 'PASS' : 'FAIL', failure })
}
requireResult(logs.some(log => log.status === 'PASS'), 'Successful Forge process log not found')
const checks = [
  { name: 'reference_sha256', passed: room.reference_sha256 === 'ea87fc89e452e478a91bca5ea505acea8b97d2da8bd468a07108db61f7e46050' },
  { name: 'medium_length_32_to_48', passed: room.size[2] >= 32 && room.size[2] <= 48 },
  { name: 'straight_flat_five_wide_route', passed: staticQa.straight_safe_lane_cells === 205 && room.branches === 0 && room.stairs === 0 },
  { name: 'complete_roof_and_foundation', passed: staticQa.roof_cells === 697 && staticQa.foundation_cells === 697 },
  { name: 'native_java_1_20_1_palette_and_roundtrip', passed: native.palette_states_validated > 0 },
  { name: 'four_rotations_600_ticks_exact_states', passed: native.rotations === 4 && native.settle_ticks >= 600 },
  { name: 'all_lava_fully_enclosed', passed: native.after_600_ticks.every(snapshot => snapshot.lava_cells === 142 && snapshot.fully_enclosed_lava) },
  { name: 'all_eight_cages_anchored', passed: native.after_600_ticks.every(snapshot => snapshot.anchored_empty_cages === 8) },
  { name: 'all_eight_anvils_supported', passed: native.after_600_ticks.every(snapshot => snapshot.supported_anvils === 8) },
  { name: 'both_ports_reachable_and_returnable', passed: native.after_600_ticks.every(snapshot => snapshot.round_trip_both_ports) },
  { name: 'no_drops_or_unexpected_entities', passed: native.after_600_ticks.every(snapshot => snapshot.unexpected_entities === 0) },
  { name: 'four_direction_internal_external_previews', passed: requiredViews.length === 8 },
  { name: 'assistant_visual_review', passed: visual.status === 'PASS' },
  { name: 'no_deployment_or_player_save_edits', passed: !native.production_save_used && !room.integration.deployed && !room.integration.player_save_modified },
]
requireResult(checks.every(check => check.passed), 'Aggregate QA contains a failed check')
const aggregate = {
  id: room.id, status: 'PASS', passed: true, created_at: new Date().toISOString(), sha256: hash,
  size: room.size, reference: room.reference, reference_sha256: room.reference_sha256, checks,
  evidence: { static: 'qa/static.json', native: 'qa/native.json', render: 'qa/render.json', visual: 'qa/visual-review.json', logs, gallery: 'preview/index.html' },
  limitations: [
    'Lodestone software renders are not screenshots from a modpack client.',
    'Forge 47.4.10 isolated GameTestServer verifies native placement, not full-modpack multiplayer, FPS or gameplay.',
    'No production JAR deployment, dungeon-core integration, player-world command or player-save access occurred.',
    'Integration must check the entire explicit-air-inclusive volume before placement and persist an independent generated marker.',
  ],
}
await fs.writeFile(path.join(root, 'qa.json'), `${JSON.stringify(aggregate, null, 2)}\n`)
await fs.writeFile(path.join(root, 'qa/attempts.json'), `${JSON.stringify({
  final_sha256: hash, logs,
  corrections: [
    'The first QA fixture was rejected because default-world terrain occupied y=99; switched to an independent flat void QA world and y=300, without clearing any occupied cells.',
    'The second QA run rejected a flammable material; soot coal blocks were replaced by inert black concrete, without relaxing the flammability check.',
    'The first capture waited for a deliberately hidden loading indicator to be visible; corrected the readiness condition. The following custom-view navigation was served as the default SPA instead, so moved the evidence viewer to its own local Vite root. Failed capture evidence remains in qa/render-failure.json.',
  ],
}, null, 2)}\n`)
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json', 'qa/static.json', 'qa/native.json', 'qa/render.json', 'qa/visual-review.json', 'qa/attempts.json', 'qa/render-failure.json', 'qa/render-failure.png', 'preview/index.html', 'preview/builder-viewer.png', 'preview/four-view-interior.png', 'preview/four-view-exterior.png', ...rendering.records.map(record => record.file), ...logs.map(log => log.file)]
const checksums = {}
async function appendSources(directory) {
  for (const entry of await fs.readdir(path.join(root, directory), { withFileTypes: true })) {
    const filename = `${directory}/${entry.name}`
    if (entry.isDirectory()) {
      if (!['node_modules', '.vite'].includes(entry.name)) await appendSources(filename)
    } else {
      files.push(filename)
    }
  }
}
await appendSources('tools')
files.push('build.gradle', 'settings.gradle', 'gradle.properties')
for (const filename of files) checksums[filename] = digest(await fs.readFile(path.join(root, filename)))
await fs.writeFile(path.join(root, 'checksums.json'), `${JSON.stringify(checksums, null, 2)}\n`)
console.log(JSON.stringify({ status: 'PASS', sha256: hash, checked_files: files.length, checks: checks.length }))
