import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const fingerprint = crypto.createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const metadata = await readJson('room.json')
const reports = {
  static: await readJson('qa/static.json'),
  render: await readJson('qa/render.json'),
  forge: await readJson('qa/forge-native.json'),
  visual: await readJson('qa/visual-review.json'),
}
if (metadata.sha256 !== fingerprint) throw new Error('Metadata does not match final NBT')
for (const [name, report] of Object.entries(reports)) {
  if (report.status !== 'PASS') throw new Error(`${name} QA is not PASS`)
  if ((report.source_nbt_sha256 ?? report.nbt_sha256) !== fingerprint) throw new Error(`${name} QA does not match final NBT`)
}
const stateMap = bytes => {
  const structure = Structure.fromNbt(NbtFile.read(bytes).root)
  return new Map(structure.getBlocks().map(block => [block.pos.join(','), { name: block.state.getName().toString(), properties: block.state.getProperties() }]))
}
const original = stateMap(await fs.readFile(path.join(root, 'room.nbt')))
const nativeSnapshots = []
for (const rotation of ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']) {
  const relative = `qa/native-placed-${rotation}.nbt`
  const bytes = await fs.readFile(path.join(root, relative))
  const placed = stateMap(bytes)
  if (placed.size !== original.size) throw new Error(`Incomplete native capture ${rotation}`)
  for (const [position, expected] of original) {
    const actual = placed.get(position)
    if (!actual || actual.name !== expected.name || Object.entries(expected.properties).some(([property, value]) => actual.properties[property] !== value)) throw new Error(`Native capture mismatch ${rotation} ${position}`)
  }
  nativeSnapshots.push({ rotation, path: relative, sha256: crypto.createHash('sha256').update(bytes).digest('hex'), records: placed.size, normalized_states_match_source: true })
}
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
if (!log.includes('BUILD SUCCESSFUL') || !log.includes(`S06_NATIVE_GAME_TEST_PASS rotations=4/4 records=8320 ticks=240 sha256=${fingerprint}`)) throw new Error('Final Forge log does not prove this NBT passed')
const evidence = []
for (const relative of ['room.nbt', 'room.json', 'preview.png', 'recipe.mjs', 'qa/static.json', 'qa/render.json', 'qa/forge-native.json', 'qa/forge-run.log', 'qa/visual-review.json', 'views/four-direction-review.png', ...reports.render.evidence.map(item => item.path), ...nativeSnapshots.map(item => item.path)]) {
  if (evidence.some(item => item.path === relative)) continue
  const bytes = await fs.readFile(path.join(root, relative))
  evidence.push({ path: relative, bytes: bytes.length, sha256: crypto.createHash('sha256').update(bytes).digest('hex') })
}
const report = {
  id: metadata.id, status: 'PASS', ready_for_independent_handoff: true,
  source_nbt_sha256: fingerprint, source_image_sha256: metadata.reference_sha256,
  size: metadata.size, static_checks: reports.static.checks.length,
  four_direction_interior_exterior: 'PASS', final_nbt_software_preview: 'PASS',
  forge: { status: reports.forge.status, minecraft: reports.forge.minecraft, forge: reports.forge.forge, rotations: reports.forge.rotations, stabilized_ticks: reports.forge.stable_snapshot_ticks, records_checked: reports.forge.records_checked_after_ticks, fluid_ticks_enabled: reports.forge.fluid_ticks_enabled },
  native_snapshots: nativeSnapshots, evidence,
  scope: { deployed: false, player_saves_modified: false, shared_core_modified: false, official_instance_modified: false, test_world_only: true },
  limitations: [...reports.forge.limitations, 'Unique integration slot, connection from FT hall, and final coordinator acceptance remain outside this independent corridor delivery.'],
}
await fs.writeFile(path.join(root, 'qa.json'), `${JSON.stringify(report, null, 2)}\n`)
process.stdout.write(`${JSON.stringify({ id: metadata.id, status: report.status, sha256: fingerprint, static_checks: report.static_checks, rotations: report.forge.rotations.length, evidence: evidence.length })}\n`)
