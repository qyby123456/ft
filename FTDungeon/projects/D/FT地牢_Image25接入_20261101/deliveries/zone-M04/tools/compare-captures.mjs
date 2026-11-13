import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const sourceBytes = await fs.readFile(path.join(root, 'room.nbt'))
const source = Structure.fromNbt(NbtFile.read(sourceBytes).root)
const structureHash = crypto.createHash('sha256').update(sourceBytes).digest('hex')
const native = JSON.parse(await fs.readFile(path.join(root, 'qa/native.json'), 'utf8'))
if (native.status !== 'PASS' || native.structure_sha256 !== structureHash) throw new Error('Native evidence does not match final NBT')
const directions = ['north', 'east', 'south', 'west']
const facing = (direction, turns) => directions[(directions.indexOf(direction) + turns) % 4]
function rotateProperties(properties, turns) {
  const rotated = {}
  for (const [key, value] of Object.entries(properties ?? {})) {
    const newKey = directions.includes(key) ? facing(key, turns) : key
    if (key === 'facing' && directions.includes(value)) rotated[newKey] = facing(value, turns)
    else if (key === 'axis' && ['x', 'z'].includes(value) && turns % 2) rotated[newKey] = value === 'x' ? 'z' : 'x'
    else if (key === 'shape' && value.includes('_')) {
      const pieces = value.split('_').map(direction => facing(direction, turns))
      rotated[newKey] = pieces.includes('north') && pieces.includes('south') ? 'north_south' : pieces.includes('east') && pieces.includes('west') ? 'east_west' : `${pieces.includes('north') ? 'north' : 'south'}_${pieces.includes('east') ? 'east' : 'west'}`
    } else rotated[newKey] = value
  }
  return rotated
}
const results = []
for (const [turns, rotation] of ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'].entries()) {
  const file = `qa/native-placed-${rotation}.nbt`
  const bytes = await fs.readFile(path.join(root, file))
  const capture = Structure.fromNbt(NbtFile.read(bytes).root)
  if (capture.getBlocks().length !== 8750) throw new Error(`Incomplete capture ${rotation}`)
  const captured = new Map(capture.getBlocks().map(entry => [entry.pos.join(','), entry]))
  let compared = 0
  for (const entry of source.getBlocks()) {
    const [width, height, depth] = entry.pos
    const positions = [[width, height, depth], [24 - depth, height, width], [24 - width, height, 24 - depth], [depth, height, 24 - width]]
    const actual = captured.get(positions[turns].join(','))
    if (!actual || actual.state.getName().toString() !== entry.state.getName().toString()) throw new Error(`Capture block mismatch ${rotation} ${entry.pos}`)
    const properties = rotateProperties(entry.state.getProperties(), turns)
    for (const [key, value] of Object.entries(properties)) {
      if (actual.state.getProperties()[key] !== value) throw new Error(`Capture state mismatch ${rotation} ${entry.pos} ${key}: ${actual.state.getProperties()[key]} != ${value}`)
    }
    compared++
  }
  results.push({ status: 'PASS', rotation, file, sha256: crypto.createHash('sha256').update(bytes).digest('hex'), compared_records: compared, mismatches: 0 })
}
const report = { status: 'PASS', id: 'zone-M04', structure_sha256: structureHash, total_records_independently_compared: results.reduce((sum, entry) => sum + entry.compared_records, 0), captures: results, scope: 'Independent Lodestone re-read of native world captures; coordinates and all declared states checked under each rotation. Native Java separately compared complete BlockState objects including defaults.' }
await fs.writeFile(path.join(root, 'qa/capture-comparison.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, total_records: report.total_records_independently_compared, structure_sha256: structureHash }))
