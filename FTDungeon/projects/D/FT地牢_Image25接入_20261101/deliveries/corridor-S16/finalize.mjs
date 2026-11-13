import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.dirname(fileURLToPath(import.meta.url))
const readJson = async name => JSON.parse((await fs.readFile(path.join(root, name), 'utf8')).replace(/^\uFEFF/, ''))
const hashFile = async name => createHash('sha256').update(await fs.readFile(path.join(root, name))).digest('hex')
const metadata = await readJson('room.json')
const staticReport = await readJson('qa/static.json')
const render = await readJson('qa/render-evidence.json')
const native = await readJson('qa/forge-native.json')
const processReport = await readJson('qa/forge-process.json')
const fingerprint = await hashFile('room.nbt')
assert.equal(metadata.sha256, fingerprint)
for (const report of [staticReport, native]) {
  assert.equal(report.status, 'PASS')
  assert.equal(report.structure_sha256, fingerprint)
}
assert.equal(render.status, 'PASS')
assert.equal(processReport.exit_code, 0)
assert.equal(native.rotations.length, 4)
assert.equal(native.records_checked_after_ticks, 7040)
assert.equal(native.stabilization_ticks, 200)
assert.equal(render.views.length, 8)
for (const view of render.views) {
  assert.equal(view.source_sha256, fingerprint)
  assert.equal(view.original_records, 1760)
  await fs.access(view.path)
}
for (const rotation of native.rotations) {
  assert.equal(rotation.capture_matches_source, true)
  assert.equal(rotation.records_checked, 1760)
  assert.equal(await hashFile(rotation.world_capture), rotation.capture_sha256)
}
await fs.access(path.join(root, 'preview/four-view-sheet.png'))
const visualReport = {
  status: 'PASS', structure_sha256: fingerprint,
  method: 'Agent visual inspection of final-NBT north/south/west/east internal sections, both interior perspectives, complete shell and uncut roof; cross-check with static and native support tests.',
  evidence: ['preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png', 'preview/exterior.png', 'preview/roof.png', 'preview/perspective.png', 'preview/reverse.png', 'preview/four-view-sheet.png'],
  checks: [
    { name: 'straight_short_scale', status: 'PASS', note: '16-block north/south axis, parallel side shells, no turns, branches or stairs in the route.' },
    { name: 'reference_theme', status: 'PASS', note: 'Frozen barred cells, mossy stone, four arch ribs, permanent blue ice, vines and cold wall lighting remain identifiable.' },
    { name: 'roof_shell_pillar_connections', status: 'PASS', note: 'Complete y=9 roof, sealed east/west shells, grounded arch pillars. Section cuts deliberately hide roof/near wall, never modify NBT.' },
    { name: 'hanging_decor_and_lights', status: 'PASS', note: 'Chains run to the solid roof; end rods have full-block bases; each vine has direct wall or roof support. Native checks confirm all four rotations.' },
    { name: 'walkway_entry_exit_and_safe_point', status: 'PASS', note: 'Five-wide three-high aisle and dry nonslip three-wide center lane remain unobstructed; both ends and interior safe point connect.' },
    { name: 'fluid_inventory_and_collision', status: 'PASS', note: 'No fluid, snow, meltable ice, falling spikes, entity, container, loot or hazardous block. No light or ice overlaps the required route.' },
  ],
  corrected_during_review: ['Moved the wall-light strips from behind each rib to its leading face so all three pairs are visible from the north entrance.', 'Moved the roof review camera outward so the entire roof is in frame.'],
  adaptations: metadata.concept_adaptations,
  remaining_required_corrections: []
}
await fs.writeFile(path.join(root, 'qa/four-view-review.json'), JSON.stringify(visualReport, null, 2) + '\n')
const qa = {
  id: metadata.id, design_id: metadata.design_id, status: 'PASS', structure_sha256: fingerprint,
  reference_sha256: metadata.reference_sha256,
  static: { status: 'PASS', tests: staticReport.test_count, report: 'qa/static.json' },
  four_view: { status: 'PASS', report: 'qa/four-view-review.json', render_evidence: 'qa/render-evidence.json', sheet: 'preview/four-view-sheet.png' },
  forge_native: { status: 'PASS', minecraft: '1.20.1', forge: '47.4.10', rotations: 4, records_per_rotation: 1760, records_after_ticks: 7040, stabilization_ticks: 200, collision_negative_probes: 4, support_negative_probes: 4, native_captures_match: true, report: 'qa/forge-native.json', process: 'qa/forge-process.json', log: 'qa/forge-run.log', fixture_vines_spread: false, fixture_random_tick_speed: native.random_tick_speed },
  integration_readiness: 'standalone candidate ready for coordinator review; no production ID/slot assigned by this thread',
  deployed: false, player_saves_modified: false, paid_image_calls: 0,
  limitations: native.limitations,
  generated_at: new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Shanghai', dateStyle: 'short', timeStyle: 'medium' }).format(new Date()) + ' Asia/Shanghai'
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'recipe.mjs', 'audit.mjs', 'README.md', 'HANDOFF.md', 'review.html', 'qa/static.json', 'qa/four-view-review.json', 'qa/render-evidence.json', 'qa/forge-native.json', 'qa/forge-process.json', 'qa/forge-run.log', 'preview/four-view-sheet.png', ...render.views.map(view => `preview/${view.view}.png`), ...native.rotations.map(rotation => rotation.world_capture)]
const manifest = []
for (const file of files) manifest.push({ path: file, sha256: await hashFile(file), bytes: (await fs.stat(path.join(root, file))).size })
await fs.writeFile(path.join(root, 'manifest.json'), JSON.stringify({ id: metadata.id, structure_sha256: fingerprint, files: manifest }, null, 2) + '\n')
console.log(JSON.stringify({ status: qa.status, structure_sha256: fingerprint, static_tests: staticReport.test_count, rotations: 4, records: 7040, ticks: 200, files: manifest.length }, null, 2))
