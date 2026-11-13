import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import build from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const requireCondition = (condition, message) => { if (!condition) throw new Error(message) }
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const metadata = build({ createStructure: () => ({}), setBlock() {}, fill() {}, createBlockNbt: value => value }).metadata
requireCondition(JSON.stringify(structure.getSize()) === '[25,13,25]', 'Unexpected dimensions')
requireCondition(nbt.getNumber('DataVersion') === 3465 && !nbt.has('palettes'), 'Wrong version or palette layout')
requireCondition(nbt.getList('entities').length === 0, 'Preplaced entities')
const blocks = structure.getBlocks()
requireCondition(blocks.length === 8125, 'Incomplete volume')
const index = new Map()
const counts = {}
const blockEntities = {}
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|tnt|lava|water|campfire|soul_fire|^minecraft:fire$|magma_block|cobweb|powder_snow|sweet_berry_bush|wither_rose)/
for (const block of blocks) {
  const key = block.pos.join(',')
  requireCondition(!index.has(key), 'Duplicate coordinate')
  requireCondition(block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < structure.getSize()[axis]), 'Out of bounds')
  const name = block.state.getName().toString()
  requireCondition(name.startsWith('minecraft:') && !forbidden.test(name), 'Forbidden palette entry: ' + name)
  requireCondition(block.state.getProperties().waterlogged !== 'true', 'Waterlogged block')
  if (block.nbt) {
    const identifier = block.nbt.getString('id')
    requireCondition(['minecraft:barrel', 'minecraft:banner'].includes(identifier), 'Unexpected block entity')
    requireCondition(!block.nbt.has('LootTable') && !block.nbt.has('Items') && !block.nbt.has('Command'), 'Container payload')
    blockEntities[identifier] = (blockEntities[identifier] ?? 0) + 1
  }
  counts[name] = (counts[name] ?? 0) + 1
  index.set(key, block)
}
const nameAt = (east, up, south) => index.get([east, up, south].join(','))?.state.getName().toString()
const protectedCells = new Set()
for (const bounds of metadata.protectedRoute) {
  for (let east = bounds.min[0]; east <= bounds.max[0]; east++) {
    for (let south = bounds.min[2]; south <= bounds.max[2]; south++) {
      protectedCells.add([east, south].join(','))
      for (let up = 1; up <= 3; up++) requireCondition(nameAt(east, up, south) === 'minecraft:air', 'Protected route obstructed')
      requireCondition(!['minecraft:air'].includes(nameAt(east, 0, south)), 'Route lacks floor')
    }
  }
}
let shellCells = 0
for (const block of blocks) {
  const [east, up, south] = block.pos
  const opening = up >= 1 && up <= 4 && ((south === 0 && east >= 11 && east <= 13) || (east === 24 && south >= 11 && south <= 13))
  if (up === 0 || up === 12 || east === 0 || east === 24 || south === 0 || south === 24) {
    if (!opening) requireCondition(block.state.getName().toString() !== 'minecraft:air', 'Shell gap')
    shellCells++
  }
}
const solid = new Set(blocks.filter(block => block.state.getName().toString() !== 'minecraft:air').map(block => block.pos.join(',')))
const supported = new Set()
const pending = []
for (const key of solid) if (key.split(',')[1] === '0') { pending.push(key); supported.add(key) }
for (let cursor = 0; cursor < pending.length; cursor++) {
  const coordinates = pending[cursor].split(',').map(Number)
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const key = coordinates.map((value, axis) => value + offset[axis]).join(',')
    if (solid.has(key) && !supported.has(key)) { supported.add(key); pending.push(key) }
  }
}
requireCondition(supported.size === solid.size, 'Disconnected floating geometry')
requireCondition(counts['minecraft:lantern'] === 4 && counts['minecraft:chain'] === 12, 'Lighting theme missing')
requireCondition(blockEntities['minecraft:barrel'] === 8 && blockEntities['minecraft:banner'] === 4, 'Furniture theme missing')
const referenceBytes = await fs.readFile(metadata.reference)
requireCondition(hash(referenceBytes) === metadata.referenceSha256, 'Reference hash mismatch')
const referenceRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const manifest = JSON.parse(await fs.readFile(path.join(referenceRoot, 'docs/manifest.json'), 'utf8'))
const verification = JSON.parse(await fs.readFile(path.join(referenceRoot, 'docs/verify.json'), 'utf8'))
const referenceEntry = manifest.files.find(entry => entry.file.includes('C04-'))
const verifiedEntry = verification.records.find(entry => JSON.stringify(entry).includes('C04-'))
requireCondition(referenceEntry?.sha256 === metadata.referenceSha256 && verifiedEntry?.sha256 === metadata.referenceSha256, 'Batch reference mismatch')
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ ...metadata, name: '焦毁塌角', authoring: 'Minecraft Builder / Lodestone 0.6.0', minecraft: '1.20.1', dataVersion: 3465, sha256: hash(bytes), blockRecords: blocks.length, nonAirBlocks: solid.size, entities: 0, deployment: 'independent delivery only' }, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ id: metadata.id, status: 'PASS', sha256: hash(bytes), reference_sha256: metadata.referenceSha256, size: structure.getSize(), dataVersion: 3465, complete_volume_records: blocks.length, non_air_blocks: solid.size, palette_count: nbt.getList('palette').length, duplicate_positions: 0, out_of_bounds: 0, preplaced_entities: 0, hazardous_blocks: 0, protected_floor_positions: protectedCells.size, route_clearance: '3 wide x 3 high; north to east L connection', shell_cells_checked: shellCells, roof_blocks: 625, floor_blocks: 625, connected_to_floor: supported.size, disconnected_non_air: 0, block_entities: blockEntities, material_counts: counts, batch_reference_match: true, limits: ['Static connectivity is a structural attachment check; collision and block survival are checked separately by Forge'] }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash(bytes), nonAir: solid.size, routeCells: protectedCells.size }))
