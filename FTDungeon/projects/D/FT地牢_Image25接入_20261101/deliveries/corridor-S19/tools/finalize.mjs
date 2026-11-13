import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async filename => JSON.parse(await fs.readFile(path.join(root, filename), 'utf8'))
const writeJson = async (filename, value) => fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + '\n')
const digest = async filename => createHash('sha256').update(await fs.readFile(path.join(root, filename))).digest('hex')
const hash = await digest('room.nbt')
const metadata = await readJson('room.json')
const staticQa = await readJson('qa/static.json')
const native = await readJson('qa/native.json')
const rendering = await readJson('qa/render.json')
const visual = await readJson('qa/visual-review.json')
const required = ['room.nbt', 'room.json', 'preview.png', 'qa/static.json', 'qa/native.json', 'qa/render.json', 'qa/visual-review.json', 'qa/materials.json', 'qa/forge-run.log', 'qa/forge-attempt1-occupied-refused.log', 'qa/four-views.png', 'qa/north.png', 'qa/south.png', 'qa/west.png', 'qa/east.png', 'qa/exterior.png', 'qa/floorplan.png', 'qa/native-roundtrip.nbt', 'qa/native-placed-none.nbt', 'qa/native-placed-clockwise_90.nbt', 'qa/native-placed-clockwise_180.nbt', 'qa/native-placed-counterclockwise_90.nbt', 'README.md']
assert.equal(metadata.sha256, hash)
for (const report of [staticQa, native, rendering, visual]) {
  assert.equal(report.status, 'PASS')
  assert.equal(report.nbt_sha256, hash)
}
assert.equal(native.rotations, 4)
assert.equal(native.ticks, 400)
assert.equal(native.initial.length, 4)
assert.equal(native.after_400_ticks.length, 4)
for (const rotation of native.after_400_ticks) {
  assert.equal(rotation.records, 2640)
  assert.equal(rotation.anchored_cages, 4)
  assert.equal(rotation.anchored_end_rods, 20)
  assert.equal(rotation.through_route_3x3, true)
}
const runLog = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
assert(runLog.includes('S19_NATIVE_GAME_TEST_PASS rotations=4/4 records=10560 ticks=400 sha256=' + hash))
assert(runLog.includes('BUILD SUCCESSFUL'))
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/package.json')
const sharp = require('sharp')
for (const filename of required.filter(filename => filename.endsWith('.png'))) {
  const image = await sharp(path.join(root, filename)).metadata()
  assert(image.width >= 1000 && image.height >= 700, filename)
}
for (const view of rendering.views) assert.equal(await digest(path.relative(root, view.path)), view.sha256)
const timestamp = new Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Shanghai', dateStyle: 'short', timeStyle: 'medium' }).format(new Date()) + ' Asia/Shanghai'
await writeJson('qa.json', {
  id: metadata.id, design_id: 'S19', name: metadata.name, status: 'PASS', integration_status: 'READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED',
  timestamp, nbt_sha256: hash, reference_sha256: metadata.reference_sha256, size: metadata.size,
  static: staticQa, native, render: rendering, four_view_visual_review: visual,
  attempts: [
    { attempt: 1, status: 'EXPECTED_REFUSAL', reason: 'Y=100 isolated fixture was occupied; volume preflight refused placement without clearing', log: 'qa/forge-attempt1-occupied-refused.log' },
    { attempt: 2, status: 'PASS', reason: 'Y=240 isolated empty fixture; all rotations and 400-tick recheck passed', log: 'qa/forge-run.log' }
  ],
  scope: { writes_only: root, deployed: false, slot_assigned: false, production_save_used: false, core_modified: false, other_deliveries_modified: false, shared_incoming_modified: false, full_modpack_playtest: false, image_api_invoked: false },
  limitations: ['Software previews are not Minecraft client screenshots', 'No full modpack, multiplayer or performance acceptance', 'No live hub connectivity or production placement was performed', 'No world clearing is authorized by explicit template air']
})
required.push('qa.json')
const files = {}
for (const filename of required) files[filename] = { bytes: (await fs.stat(path.join(root, filename))).size, sha256: await digest(filename) }
await writeJson('handoff.json', {
  id: metadata.id, design_id: 'S19', status: 'READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED', timestamp, directory: root,
  reference: metadata.reference, reference_sha256: metadata.reference_sha256, nbt_sha256: hash,
  suggested_resource_id: metadata.suggested_resource_id, size: metadata.size, entrance: metadata.entrance, interior: metadata.interior, facing: metadata.facing,
  exit: metadata.exit, exit_facing: metadata.exit_facing, files, deployed: false, slot_assigned: false,
  qa: { static: 'PASS', four_view_visual_review: 'PASS', isolated_forge_rotations: '4/4 PASS', stability_ticks: 400 },
  restrictions: ['Only this dedicated delivery directory is modified', 'Coordinator assigns unique ID/slot and validates the whole occupied volume before placement', 'Do not deploy the Forge QA fixture or open production saves', 'Preserve generated-room markers and player changes']
})
console.log(JSON.stringify({ status: 'PASS', id: metadata.id, nbt_sha256: hash, four_piece_hashes: Object.fromEntries(['room.nbt', 'room.json', 'preview.png', 'qa.json'].map(filename => [filename, files[filename]])), handoff: path.join(root, 'handoff.json') }, null, 2))
