import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const load = async filename => Structure.fromNbt(NbtFile.read(await fs.readFile(path.join(root, filename))).root)
const digest = async filename => createHash('sha256').update(await fs.readFile(path.join(root, filename))).digest('hex')
const source = await load('room.nbt')
const directions = ['north', 'east', 'south', 'west']
const canonical = (block, turns) => {
  const properties = {}
  for (const [key, value] of Object.entries(block.state.getProperties())) {
    properties[key] = key === 'facing' && directions.includes(value) ? directions[(directions.indexOf(value) + turns) % 4] : key === 'axis' && turns % 2 && value !== 'y' ? (value === 'x' ? 'z' : 'x') : value
  }
  return block.state.getName().toString() + JSON.stringify(Object.fromEntries(Object.entries(properties).sort(([first], [second]) => first.localeCompare(second))))
}
const cases = [
  { file: 'qa/native-roundtrip.nbt', turns: 0, transform: ([across, height, depth]) => [across, height, depth] },
  { file: 'qa/native-placed-none.nbt', turns: 0, transform: ([across, height, depth]) => [across, height, depth] },
  { file: 'qa/native-placed-clockwise_90.nbt', turns: 1, transform: ([across, height, depth]) => [20 - depth, height, across] },
  { file: 'qa/native-placed-clockwise_180.nbt', turns: 2, transform: ([across, height, depth]) => [20 - across, height, 20 - depth] },
  { file: 'qa/native-placed-counterclockwise_90.nbt', turns: 3, transform: ([across, height, depth]) => [depth, height, 20 - across] },
]
const reports = []
for (const specification of cases) {
  const snapshot = await load(specification.file)
  if (JSON.stringify(snapshot.getSize()) !== '[21,11,21]' || snapshot.getBlocks().length !== 4851) throw new Error('Snapshot dimensions or records: ' + specification.file)
  let containers = 0
  for (const block of source.getBlocks()) {
    const position = specification.transform(block.pos)
    const actual = snapshot.getBlock(position)
    if (!actual || canonical(actual, 0) !== canonical(block, specification.turns)) throw new Error('Snapshot differs: ' + specification.file + ' at ' + position)
    if (block.nbt) {
      if (!actual.nbt || actual.nbt.getString('id') !== block.nbt.getString('id') || actual.nbt.getList('Items').length || actual.nbt.has('LootTable') || actual.nbt.has('LootTableSeed') || actual.nbt.getString('Lock')) throw new Error('Snapshot inventory differs at ' + position)
      containers++
    } else if (actual.nbt) throw new Error('Unexpected snapshot block entity')
  }
  if (containers !== 23) throw new Error('Snapshot lost containers')
  reports.push({ file: specification.file, status: 'PASS', matched_records: 4851, empty_containers: containers, sha256: await digest(specification.file) })
}
const report = { status: 'PASS', source_sha256: await digest('room.nbt'), independent_lodestone_reread: true, native_roundtrip: 1, settled_world_rotations: 4, matched_records: 24255, empty_container_comparisons: 115, snapshots: reports }
await fs.writeFile(path.join(root, 'qa/native-snapshots.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify(report, null, 2))
