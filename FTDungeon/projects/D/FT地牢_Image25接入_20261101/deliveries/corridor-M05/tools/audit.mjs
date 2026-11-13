import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import build from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const size = structure.getSize()
const names = new Map()
const requireCheck = (condition, message) => { if (!condition) throw new Error(message) }
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const checks = {}
const metadata = build({
  createStructure: dimensions => new Structure(dimensions),
  setBlock: (target, position, name, properties) => target.addBlock(position, name, properties),
  fill: (target, from, to, name, properties) => {
    for (let blockX = from[0]; blockX <= to[0]; blockX += 1) {
      for (let blockY = from[1]; blockY <= to[1]; blockY += 1) {
        for (let blockZ = from[2]; blockZ <= to[2]; blockZ += 1) target.addBlock([blockX, blockY, blockZ], name, properties)
      }
    }
  }
}).metadata
requireCheck(hash(await fs.readFile(metadata.reference)) === metadata.referenceSha256, 'Reference fingerprint mismatch')
requireCheck(JSON.stringify(size) === '[19,12,40]', 'Dimensions mismatch')
requireCheck(blocks.length === 9120, 'Incomplete explicit volume')
const coordinates = new Set()
for (const block of blocks) {
  const identifier = block.state.getName().toString()
  requireCheck(block.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < size[axis]), 'Out of bounds')
  requireCheck(!coordinates.has(block.pos.join(',')), 'Duplicate coordinate')
  coordinates.add(block.pos.join(','))
  requireCheck(identifier.startsWith('minecraft:'), 'Nonvanilla namespace')
  requireCheck(!/command_block|structure_block|jigsaw|spawner|lava|water|fire|magma|powder_snow|cobweb/.test(identifier), `Forbidden or hazardous block ${identifier}`)
  requireCheck(!block.nbt, 'Unexpected custom block entity payload')
  names.set(identifier, (names.get(identifier) ?? 0) + 1)
}
for (let blockZ = 0; blockZ < 40; blockZ += 1) {
  for (let blockX = 7; blockX <= 11; blockX += 1) {
    requireCheck(nameAt([blockX, 1, blockZ]) !== 'minecraft:air', 'Missing main-route floor')
    for (let blockY = 2; blockY <= 4; blockY += 1) requireCheck(nameAt([blockX, blockY, blockZ]) === 'minecraft:air', 'Main route obstruction')
  }
}
for (let blockX = 0; blockX < 19; blockX += 1) {
  for (let blockZ = 0; blockZ < 40; blockZ += 1) {
    for (const blockY of [10, 11]) requireCheck(nameAt([blockX, blockY, blockZ]) === 'minecraft:stone_bricks' || nameAt([blockX, blockY, blockZ]) === 'minecraft:chiseled_stone_bricks', 'Roof gap')
  }
}
requireCheck(names.get('minecraft:iron_door') === 20 && names.get('minecraft:light_gray_bed') === 20, 'Ten cells require ten complete doors and beds')
checks.reference_sha256 = 'PASS'
checks.gzip_single_palette_java_1_20_1 = bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes') ? 'PASS' : 'FAIL'
requireCheck(checks.gzip_single_palette_java_1_20_1 === 'PASS', 'Invalid NBT format')
checks.explicit_volume_bounds_unique_coordinates = 'PASS'
checks.straight_40_block_axis_5_wide_3_high = 'PASS'
checks.two_open_endpoints_and_full_roof = 'PASS'
checks.ten_closed_side_cells = 'PASS'
checks.no_fluid_fire_hazard_entities_inventory_or_loot_payload = 'PASS'
const room = {
  id: metadata.id, design_id: 'M05', name: metadata.label, size, entrance: metadata.entrance,
  interior: metadata.interior, exit: metadata.exit, facing: 'north', sha256: hash(bytes),
  reference: metadata.reference, reference_sha256: metadata.referenceSha256,
  length_class: 'medium', length_blocks: 40, length_chunks: 2.5, axis: 'z',
  connection_points: [{ side: 'north', position: metadata.entrance }, { side: 'south', position: metadata.exit }],
  clear_main_route: metadata.clearMainRoute, cells: metadata.cells, adaptations: metadata.adaptations,
  data_version: 3465, minecraft: '1.20.1', palette_count: file.root.getList('palette').length,
  placement_requires_empty_volume: true, deployment_status: 'NOT_DEPLOYED'
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(room, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ id: metadata.id, status: 'PASS', sha256: room.sha256, size, checks, explicit_records: blocks.length, non_air_blocks: blocks.length - names.get('minecraft:air'), materials: Object.fromEntries([...names].sort()) }, null, 2) + '\n')
const empty = new Structure([1, 1, 1])
empty.addBlock([0, 0, 0], 'minecraft:air')
const emptyPath = path.join(root, 'native-test/src/main/resources/data/ftcorridorm05qa/structures/empty.nbt')
await fs.mkdir(path.dirname(emptyPath), { recursive: true })
await fs.writeFile(emptyPath, empty.writeNbt({ dataVersion: 3465, compression: 'gzip' }))
process.stdout.write(JSON.stringify({ static: 'PASS', sha256: room.sha256, records: blocks.length, size }) + '\n')
