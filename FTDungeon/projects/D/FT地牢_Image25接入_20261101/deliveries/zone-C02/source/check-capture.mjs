import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { gunzipSync } from 'node:zlib'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const hash = digest(bytes)
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const native = JSON.parse(await fs.readFile(path.join(root, 'qa/forge-native.json'), 'utf8'))
const compressed = await fs.readFile(path.join(root, 'qa/native-capture.json.gz'))
const capture = JSON.parse(gunzipSync(compressed).toString('utf8'))
assert.equal(native.status, 'PASS')
assert.equal(native.sha256, hash)
assert.equal(native.capture_sha256, digest(compressed))
assert.equal(capture.sha256, hash)
const rotations = ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90']
const horizontal = ['north', 'east', 'south', 'west']
const recordsPerRotation = Object.fromEntries(rotations.map(name => [name, 0]))
const seen = new Set()
for (const record of capture.records) {
  const rotation = rotations.indexOf(record.rotation)
  assert.ok(rotation >= 0)
  const block = structure.getBlock(record.local)
  assert.ok(block)
  const identity = record.rotation + ':' + record.local.join(',')
  assert.equal(seen.has(identity), false)
  seen.add(identity)
  const properties = {}
  for (const [key, value] of Object.entries(block.state.getProperties())) {
    let rotatedKey = key
    let rotatedValue = value
    if (horizontal.includes(key)) rotatedKey = horizontal[(horizontal.indexOf(key) + rotation) % 4]
    if (key === 'facing' && horizontal.includes(value)) rotatedValue = horizontal[(horizontal.indexOf(value) + rotation) % 4]
    if (key === 'axis' && rotation % 2 === 1 && ['x', 'z'].includes(value)) rotatedValue = value === 'x' ? 'z' : 'x'
    properties[rotatedKey] = rotatedValue
  }
  assert.equal(record.name, block.state.getName().toString(), identity)
  assert.deepEqual(record.properties, properties, identity)
  const [blockX, blockY, blockZ] = record.local
  const offsets = [[blockX, blockZ], [-blockZ, blockX], [-blockX, -blockZ], [blockZ, -blockX]][rotation]
  assert.deepEqual(record.world, [2048 + rotation * 80 + offsets[0], 100 + blockY, 2048 + offsets[1]])
  recordsPerRotation[record.rotation]++
}
assert.equal(capture.records.length, 15884)
for (const count of Object.values(recordsPerRotation)) assert.equal(count, 3971)
for (const phase of [native.initial, native.after_400_ticks]) {
  assert.equal(phase.length, 4)
  for (const report of phase) {
    assert.equal(report.records, 3971)
    assert.equal(report.source_water_blocks, 131)
    assert.equal(report.dry_player_positions, 57)
    assert.equal(report.empty_barrels, 2)
    assert.equal(report.unchanged_boundary_air, 1762)
  }
}
const report = { status: 'PASS', sha256: hash, capture_sha256: digest(compressed), compared_records: capture.records.length, records_per_rotation: recordsPerRotation, method: 'Independent Node decoder compares original NBT names, all serialized state properties and manually transformed positions with native post-400-tick ServerLevel snapshots.', mismatches: 0 }
await fs.writeFile(path.join(root, 'qa/capture-comparison.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify(report))

