import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const load = async filename => Structure.fromNbt(NbtFile.read(await fs.readFile(path.join(root, filename))).root)
const source = await load('room.nbt')
const sourceHash = createHash('sha256').update(await fs.readFile(path.join(root, 'room.nbt'))).digest('hex')
const directions = ['north', 'east', 'south', 'west']
const canonical = (block, turns) => {
  const properties = {}
  for (const [key, value] of Object.entries(block.state.getProperties())) {
    const rotatedKey = directions.includes(key) ? directions[(directions.indexOf(key) + turns) % 4] : key
    properties[rotatedKey] = key === 'facing' && directions.includes(value) ? directions[(directions.indexOf(value) + turns) % 4] : key === 'axis' && turns % 2 && value !== 'y' ? (value === 'x' ? 'z' : 'x') : value
  }
  return block.state.getName().toString() + JSON.stringify(Object.fromEntries(Object.entries(properties).sort(([first], [second]) => first.localeCompare(second))))
}
const cases = [
  { file: 'qa/native-roundtrip.nbt', turns: 0, size: [15, 13, 40], transform: ([across, height, depth]) => [across, height, depth] },
  { file: 'qa/native-placed-none.nbt', turns: 0, size: [15, 13, 40], transform: ([across, height, depth]) => [across, height, depth] },
  { file: 'qa/native-placed-clockwise_90.nbt', turns: 1, size: [40, 13, 15], transform: ([across, height, depth]) => [39 - depth, height, across] },
  { file: 'qa/native-placed-clockwise_180.nbt', turns: 2, size: [15, 13, 40], transform: ([across, height, depth]) => [14 - across, height, 39 - depth] },
  { file: 'qa/native-placed-counterclockwise_90.nbt', turns: 3, size: [40, 13, 15], transform: ([across, height, depth]) => [depth, height, 14 - across] },
]
const reports = []
for (const specification of cases) {
  const snapshot = await load(specification.file)
  if (JSON.stringify(snapshot.getSize()) !== JSON.stringify(specification.size) || snapshot.getBlocks().length !== 7800) throw new Error('Snapshot dimensions or records: ' + specification.file)
  for (const block of source.getBlocks()) {
    const position = specification.transform(block.pos)
    const actual = snapshot.getBlock(position)
    if (!actual || canonical(actual, 0) !== canonical(block, specification.turns)) throw new Error('Snapshot differs: ' + specification.file + ' at ' + position + ' expected=' + canonical(block, specification.turns) + ' actual=' + (actual && canonical(actual, 0)))
  }
  reports.push({ file: specification.file, status: 'PASS', size: specification.size, matched_records: 7800, sha256: createHash('sha256').update(await fs.readFile(path.join(root, specification.file))).digest('hex') })
}
const report = { status: 'PASS', source_sha256: sourceHash, independent_lodestone_reread: true, native_roundtrip: 1, settled_world_rotations: 4, matched_records: 39000, snapshots: reports }
await fs.writeFile(path.join(root, 'qa/native-snapshots.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify(report, null, 2))
