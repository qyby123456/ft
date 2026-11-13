import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { root, readRoom, sha256, writeJson } from './common.mjs'

const room = await readRoom()
const metadata = JSON.parse(await fs.readFile(path.join(root, 'room.json')))
const reports = {}
for (const stage of ['static', 'forge', 'render']) {
  reports[stage] = JSON.parse(await fs.readFile(path.join(root, `qa/${stage}.json`)))
  assert.equal(reports[stage].status, 'PASS', stage)
  assert.equal(reports[stage].source_nbt_sha256, room.hash, `${stage} report stale`)
}
assert.equal(metadata.sha256, room.hash)
assert.equal(reports.forge.rotations.length, 4)
assert.equal(reports.forge.records_checked_after_ticks, 29520)
assert.equal(reports.forge.stabilization_ticks, 240)
assert.deepEqual(reports.forge.stable_snapshot_ticks, [200, 240])
const sourceRecords = room.structure.getBlocks()
const nativeCaptures = []
for (const rotation of ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']) {
  const relative = `qa/native-placed-${rotation}.nbt`
  const native = await readRoom(relative)
  assert.deepEqual(native.structure.getSize(), room.structure.getSize())
  assert.equal(native.structure.getBlocks().length, sourceRecords.length)
  for (const block of sourceRecords) {
    const actual = native.structure.getBlock(block.pos)
    assert.ok(actual, `${rotation} missing ${block.pos}`)
    assert.equal(actual.state.getName().toString(), block.state.getName().toString(), `${rotation} name ${block.pos}`)
    for (const [property, value] of Object.entries(block.state.getProperties())) {
      assert.equal(actual.state.getProperties()[property], value, `${rotation} ${block.pos} ${property}`)
    }
  }
  nativeCaptures.push({ rotation, file: relative, sha256: native.hash, source_states_match: true, records: sourceRecords.length })
}
const required = ['hero', 'reverse', 'interior-north', 'exterior-north', 'interior-south', 'exterior-south', 'interior-west', 'exterior-west', 'interior-east', 'exterior-east', 'plan', 'native-hero']
assert.equal(reports.render.outputs.length, required.length)
assert.deepEqual([...reports.render.outputs.map(output => output.view + (output.native ? ':native' : ''))].sort(), [...required.map(view => view === 'native-hero' ? 'hero:native' : view)].sort())
for (const output of reports.render.outputs) {
  const bytes = await fs.readFile(path.join(root, output.file))
  assert.equal(sha256(bytes), output.sha256, `Stale PNG ${output.file}`)
  assert.equal(bytes.readUInt32BE(16), 1536)
  assert.equal(bytes.readUInt32BE(20), 1024)
}
const visualReview = {
  status: 'PASS', reviewer: 'Codex visual inspection of rendered images',
  source_nbt_sha256: room.hash,
  inspected: ['preview.png', 'views/four-direction-review.png', 'views/native-hero.png', 'views/plan.png', 'views/interior-west.png'],
  results: [
    'Centered sightline runs straight through both portals, without turns, junctions or route stairs.',
    'Six repeated obsidian/crying-obsidian arches, gold rune plates, warm candle groups and orange seams read clearly.',
    'Wall-side rubble and missing shoulder stones communicate light damage while the central route stays clear.',
    'All four complete exterior views show continuous roof, full floor and closed side walls; only north and south connect.',
    'Native 240-tick capture visually matches the delivered layout; this is an NBT render, not an in-game screenshot.',
  ],
  intentional_differences_from_concept: ['Vanilla materials cannot exactly reproduce illustrated gold glyphs; buttons on gold plates provide readable rune relief.', 'Real lava is replaced by sealed shroomlight/orange-glass seams for placement safety.', 'No custom shaders, texture pack or combat/trial scripting is included.'],
}
await writeJson('qa/visual-review.json', visualReview)
const assetFiles = ['room.nbt', 'room.json', 'preview.png', 'views/four-direction-review.png', 'views/plan.png', 'views/native-hero.png', 'qa/static.json', 'qa/forge.json', 'qa/render.json', 'qa/visual-review.json', 'qa/forge-invocation.json', 'qa/forge-console.log', 'README.md', 'recipe.mjs']
const files = []
for (const relative of assetFiles) {
  const bytes = await fs.readFile(path.join(root, relative))
  files.push({ file: relative, bytes: bytes.length, sha256: sha256(bytes) })
}
const qa = {
  id: metadata.id, design_id: 'M09', status: 'PASS',
  date: '2026-11-03', timezone: 'Asia/Shanghai',
  source_nbt_sha256: room.hash,
  reference: metadata.reference, reference_sha256: metadata.reference_sha256,
  size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit,
  static: { status: 'PASS', checks_passed: reports.static.checks.length, checks: reports.static.checks, explicit_records: sourceRecords.length, non_air_blocks: reports.static.non_air_blocks, report: 'qa/static.json' },
  forge: {
    status: 'PASS', minecraft: '1.20.1', forge: reports.forge.forge, java: reports.forge.java,
    placement_rotations: 4, rotations: reports.forge.rotations,
    stabilization_ticks: 240, stable_snapshot_ticks: [200, 240],
    records_checked_after_ticks: 29520, path_feet_per_rotation: 123,
    negative_blocked_path_probes: 4, negative_unsupported_candle_probes: 4,
    report: 'qa/forge.json', console: 'qa/forge-console.log', native_captures: nativeCaptures,
  },
  views: { status: 'PASS', four_direction_interior: 4, four_direction_exterior: 4, other_views: ['north-south axis', 'south-north axis', 'plan', 'native capture axis'], report: 'qa/render.json', review: 'qa/visual-review.json', contact_sheet: 'views/four-direction-review.png' },
  visual_review: visualReview,
  delivery_files: ['room.nbt', 'room.json', 'preview.png', 'qa.json'],
  files,
  deployment: { status: 'NOT_DEPLOYED', production_mods_changed: false, player_saves_opened: false, player_saves_modified: false, other_deliveries_changed: false },
  limitations: ['Isolated Forge 1.20.1 server test and software-rendered NBT are separate from an actual modpack client playtest.', 'No FT catalog/core integration, live hallway connection, whole-volume occupancy refusal or idempotence test is claimed.', 'All coordinates denote player feet; a future placement integrator must check the whole 15×12×41 volume before writing any block, including explicit air.', 'The optional Forge QA source is a test harness only, not a mod for deployment.'],
  non_blocking_log_messages: ['Forge update-check timeout only; unrelated to validation and BUILD SUCCESSFUL is recorded.', 'Fresh-world Forge default configuration corrections and Gradle deprecation warnings are not template failures.'],
}
await writeJson('qa.json', qa)
console.log(JSON.stringify({ status: 'PASS', files: qa.delivery_files, sha256: room.hash, native_rotations: 4, rendered_views: reports.render.outputs.length }, null, 2))
