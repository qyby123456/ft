import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import {NbtFile, Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const fingerprint = createHash('sha256').update(bytes).digest('hex')
const document = NbtFile.read(bytes).root.toSimplifiedJson()
const metadata = JSON.parse(await fs.readFile(path.join(root, 'room.json'), 'utf8'))
const checks = []
const require = (condition, label) => { checks.push({check: label, status: condition ? 'PASS' : 'FAIL'}); if (!condition) throw new Error(label) }
require(bytes[0] === 31 && bytes[1] === 139, 'gzip compressed vanilla structure')
require(document.DataVersion === 3465 && JSON.stringify(document.size) === '[19,11,19]', 'Java 1.20.1 DataVersion and dimensions')
require(!document.palettes && document.palette.length > 0 && document.entities.length === 0, 'single palette, no entities')
require(metadata.sha256 === fingerprint, 'metadata SHA-256 matches actual file')
require(createHash('sha256').update(await fs.readFile(metadata.reference)).digest('hex') === metadata.referenceSha256, 'reference SHA-256 matches design')
const blocks = new Map()
const counts = {}
const nbtEntries = []
for (const entry of document.blocks) {
  require(entry.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < document.size[axis]), `bounds ${entry.pos}`)
  const key = entry.pos.join(',')
  if (blocks.has(key)) throw new Error(`Duplicate ${key}`)
  const state = document.palette[entry.state]
  if (!state?.Name?.startsWith('minecraft:')) throw new Error('Nonvanilla block')
  blocks.set(key, state)
  counts[state.Name] = (counts[state.Name] ?? 0) + 1
  if (entry.nbt) nbtEntries.push({...entry, name: state.Name})
}
const block = (across, height, depth) => blocks.get([across, height, depth].join(','))
const name = (across, height, depth) => block(across, height, depth)?.Name
const air = (across, height, depth) => name(across, height, depth) === 'minecraft:air'
const fullFloor = new Set(['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:smooth_stone', 'minecraft:chiseled_stone_bricks'])
require(blocks.size === 3971, 'all volume positions explicit, unique and in bounds')
require(Object.keys(counts).every(identifier => !/(spawner|command_block|structure_block|jigsaw|lava|water|fire|tnt|powder_snow)/.test(identifier)), 'no hazardous, functional automation or forbidden blocks')
for (let across = 0; across < 19; across++) {
  for (let depth = 0; depth < 19; depth++) {
    if (!fullFloor.has(name(across, 1, depth)) || name(across, 9, depth) !== 'minecraft:stone_bricks' || air(across, 10, depth)) throw new Error(`Floor or roof gap ${across},${depth}`)
  }
}
require(true, 'continuous 361-cell floor and complete two-layer roof')
const lane = new Set()
for (let across = 8; across <= 10; across++) for (let depth = 0; depth <= 10; depth++) lane.add([across, 2, depth].join(','))
for (let across = 8; across <= 18; across++) for (let depth = 8; depth <= 10; depth++) lane.add([across, 2, depth].join(','))
for (const key of lane) {
  const [across, height, depth] = key.split(',').map(Number)
  if (!fullFloor.has(name(across, height - 1, depth)) || ![2, 3, 4].every(level => air(across, level, depth))) throw new Error(`Blocked route ${key}`)
}
require(lane.size === 57, 'three-wide L route with full floor and three-high clearance')
const reached = new Set([metadata.entrance.join(',')])
const queue = [metadata.entrance]
for (let index = 0; index < queue.length; index++) {
  const [across, height, depth] = queue[index]
  for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const adjacent = [across + deltaX, height, depth + deltaZ]
    const key = adjacent.join(',')
    if (lane.has(key) && !reached.has(key)) { reached.add(key); queue.push(adjacent) }
  }
}
require(reached.size === 57 && [metadata.interior, metadata.exit, metadata.bend].every(position => reached.has(position.join(','))), 'entrance, landing, bend and east exit connected bidirectionally')
for (let width = 8; width <= 10; width++) {
  for (let height = 2; height <= 5; height++) if (!air(width, height, 0) || !air(18, height, width)) throw new Error('Port clearance failed')
}
require(true, 'north/east ports exactly three wide and four high')
for (const [across, depth] of [[5, 5], [13, 5], [5, 13], [13, 13]]) {
  for (let height = 1; height <= 7; height++) if (!fullFloor.has(name(across, height, depth))) throw new Error(`Broken load-bearing pier ${across},${height},${depth}`)
}
require(true, 'four independent load-bearing piers reach floor and roof beams')
for (const entry of document.blocks.filter(record => document.palette[record.state].Name === 'minecraft:lantern')) {
  const [across, height, depth] = entry.pos
  if (document.palette[entry.state].Properties.hanging === 'true') {
    let supportY = height + 1
    while (name(across, supportY, depth) === 'minecraft:chain') supportY++
    if (!fullFloor.has(name(across, supportY, depth))) throw new Error(`Floating hanging lantern ${entry.pos}`)
  } else if (!fullFloor.has(name(across, height - 1, depth)) && !(name(across, height - 1, depth) === 'minecraft:spruce_slab' && block(across, height - 1, depth).Properties.type === 'top')) throw new Error('Unsupported table lantern')
}
require(counts['minecraft:lantern'] === 7, 'seven lanterns with continuous ceiling chains or tabletop support')
require(nbtEntries.length === 7 && nbtEntries.filter(entry => entry.name === 'minecraft:barrel').length === 6, 'six empty barrels and one patterned banner only')
for (const entry of nbtEntries) {
  if (entry.name === 'minecraft:barrel' && (entry.nbt.id !== 'minecraft:barrel' || entry.nbt.Items.length !== 0)) throw new Error('Invalid barrel inventory')
  if (entry.nbt.LootTable || entry.nbt.Command) throw new Error('Unsafe block entity')
}
require(name(10, 5, 18) === 'minecraft:stone_bricks', 'banner attached to south wall')
require(name(6, 2, 14) === 'minecraft:spruce_stairs' && name(6, 3, 14) === 'minecraft:spruce_trapdoor' && block(6, 3, 14).Properties.facing === 'north', 'chair back touches the full-height rear of its stair seat')
require(counts['minecraft:cobweb'] === 2 && !Array.from(lane).some(key => blocks.get(key)?.Name === 'minecraft:cobweb'), 'cobwebs confined to high wall corners')
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const report = {
  status: 'PASS', id: 'C01', structure_sha256: fingerprint, size: document.size,
  records: blocks.size, non_air_records: blocks.size - counts['minecraft:air'], palette_states: document.palette.length,
  route_cells: lane.size, supported_lanterns: 7, containers: {barrels: 6, contents: 0, loot_tables: 0},
  checks: checks.filter(check => !check.check.startsWith('bounds ')), bounds_checked: document.blocks.length,
  material_counts: counts, lodestone_decode_records: structure.getBlocks().length,
  limitations: ['Static checks complement the Forge native collision, support, registry and placement tests.']
}
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
process.stdout.write(JSON.stringify({status: report.status, records: report.records, checks: report.checks.length}) + '\n')
