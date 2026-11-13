import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import assert from 'node:assert/strict'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const hash = async filename => digest(await fs.readFile(path.join(root, filename)))
const write = async (filename, value) => fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + String.fromCharCode(10))
const metadata = await read('room.json')
const staticReport = await read('qa/static.json')
const native = await read('qa/native.json')
const render = await read('qa/render.json')
const review = await read('qa/four-view-review.json')
const fingerprint = await hash('room.nbt')
for (const report of [metadata, staticReport, native, render, review]) {
  assert.equal(report.sha256 ?? report.structure_sha256, fingerprint, 'Stale QA evidence')
}
for (const report of [staticReport, native, render, review]) assert.equal(report.status, 'PASS')
assert.equal(native.stabilization_ticks, 200)
assert.equal(native.after_100_ticks.length, 4)
assert.equal(native.after_200_ticks.length, 4)
for (const result of [...native.immediate_placements, ...native.after_100_ticks, ...native.after_200_ticks]) {
  assert.equal(result.status, 'PASS')
  assert.equal(result.records_checked, 1584)
  assert.equal(result.unexpected_entities, 0)
}
const required = ['preview.png', 'preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png', 'preview/shell.png', 'preview/roof.png', 'preview/four-view-sheet.png', 'preview/native-none.png', 'preview/native-clockwise_90.png', 'preview/native-clockwise_180.png', 'preview/native-counterclockwise_90.png', 'preview/native-placement-sheet.png']
for (const filename of required) {
  const receipt = render.views.find(view => view.file === filename)
  assert(receipt, 'Missing rendered evidence ' + filename)
  assert.equal(await hash(filename), receipt.sha256)
  const image = await fs.readFile(path.join(root, filename))
  assert.equal(image.readUInt32BE(16), 1600)
  assert.equal(image.readUInt32BE(20), 1000)
  if (receipt.source === '/room.nbt') assert.equal(receipt.render_source_sha256, fingerprint)
}
for (const result of native.after_200_ticks) {
  const filename = result.world_capture.split(path.sep).join('/')
  assert.equal(await hash(filename), result.world_capture_sha256)
  const receipt = render.views.find(view => view.source === '/' + filename)
  assert(receipt, 'Missing native world-capture preview')
  assert.equal(receipt.render_source_sha256, result.world_capture_sha256)
}
for (const [filename, evidence] of Object.entries(review.image_fingerprints)) assert.equal(await hash(filename), evidence)
const sourceHash = digest(await fs.readFile(metadata.reference))
assert.equal(sourceHash, metadata.reference_sha256)
const report = {
  id: metadata.id, status: 'PASS', structure_sha256: fingerprint, reference_sha256: sourceHash,
  size: metadata.size, length_blocks: 16, length_chunks: 1,
  checks: { static: { status: staticReport.status, evidence: 'qa/static.json' }, four_view_visual_review: { status: review.status, evidence: 'qa/four-view-review.json' }, forge_native: { status: native.status, evidence: 'qa/native.json', minecraft: native.minecraft, forge: native.forge, rotations: 4, stabilization_ticks: 200 }, preview_integrity: { status: 'PASS', evidence: 'qa/render.json', checked_pngs: required.length }, native_capture_render_integrity: { status: 'PASS', capture_count: 4 } },
  ready_for_unified_review: true, integrated: false, deployed: false, production_world_modified: false,
  limitations: ['Independent candidate only; coordinator still performs unified acceptance and unique slot allocation.', 'Not full modpack-client, multiplayer or FPS acceptance.'],
  artifacts: { template: 'room.nbt', metadata: 'room.json', preview: 'preview.png', four_views: 'preview/four-view-sheet.png', full_shell: 'preview/shell.png', roof: 'preview/roof.png', forge_captures: 'preview/native-placement-sheet.png' },
}
await write('qa.json', report)
const handoff = {
  id: metadata.id, source_id: metadata.source_id, source_thread_id: '01a19f19-57ba-7392-873f-eb90c8824083', source_image: metadata.reference, reference_sha256: sourceHash,
  delivery_directory: root, structure_sha256: fingerprint, independent_qa: 'PASS', ready_for_unified_review: true,
  entrance: metadata.entrance, interior: metadata.interior, exit: metadata.exit, size: metadata.size,
  expected_receiver_thread_id: '01a19c8f-6747-7572-a77a-6e90084bbd34', receiver_messaged: false,
  boundaries: { deployed: false, production_save_changed: false, core_code_changed: false, world_slot_allocated: false },
  evidence: report.artifacts,
}
await write('handoff.json', handoff)
const files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'recipe.mjs', 'materials.json', 'handoff.json', 'qa/static.json', 'qa/native.json', 'qa/render.json', 'qa/four-view-review.json', 'qa/native-roundtrip.nbt', 'qa/forge-20261103-s02-final.log', ...required.filter(filename => filename !== 'preview.png'), ...native.after_200_ticks.map(result => result.world_capture.split(path.sep).join('/'))]
const manifest = { id: metadata.id, structure_sha256: fingerprint, reference_sha256: sourceHash, files: {} }
for (const filename of [...new Set(files)]) manifest.files[filename] = { sha256: await hash(filename), bytes: (await fs.stat(path.join(root, filename))).size }
await write('manifest.json', manifest)
console.log(JSON.stringify({ id: metadata.id, status: report.status, sha256: fingerprint, pngs: required.length, rotations: 4, stabilization_ticks: 200, deployed: false }))
