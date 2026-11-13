import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createHash } from 'node:crypto'
import { metadata } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const indexed = new Map(blocks.map(block => [block.pos.join(','), block]))
const nameAt = (east, up, south) => indexed.get([east, up, south].join(','))?.state.getName().toString() ?? 'MISSING'
const checks = {}
const check = (name, condition) => { checks[name] = condition; if (!condition) throw new Error(name) }
const materials = {}
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('size_33x15x31', JSON.stringify(structure.getSize()) === JSON.stringify(metadata.size))
check('full_volume_unique_records', blocks.length === 15345 && indexed.size === 15345)
check('reference_hash', hash(await fs.readFile(metadata.reference)) === metadata.reference_sha256)
for (const [filename, field] of [['manifest.json', 'files'], ['verify.json', 'records']]) {
  const batch = JSON.parse(await fs.readFile(path.join(path.dirname(metadata.reference), '../../docs', filename), 'utf8'))
  const entry = batch[field].find(item => item.file.endsWith('N02-hub-minecart-station.png'))
  check(filename + '_matches', entry?.sha256 === metadata.reference_sha256 && entry.ok === true && entry.bytes === 2130390)
}
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|tnt|respawn_anchor|lava|water|fire|magma_block|cactus|powder_snow|cobweb|powered_rail|detector_rail|activator_rail)$/
for (const block of blocks) {
  const name = block.state.getName().toString()
  check('vanilla_only', name.startsWith('minecraft:'))
  check('no_hazards_commands_power_or_spawners', !forbidden.test(name))
  check('no_block_entity_inventory_loot', !block.nbt)
  check('bounds', block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < metadata.size[axis]))
  materials[name] = (materials[name] ?? 0) + 1
}
const routeCells = new Map()
for (let east = 14; east <= 18; east++) for (let south = 0; south <= 30; south++) routeCells.set([east, 2, south].join(','), [east, 2, south])
for (let east = 16; east <= 32; east++) for (let south = 21; south <= 25; south++) routeCells.set([east, 2, south].join(','), [east, 2, south])
for (const position of routeCells.values()) {
  const [east, up, south] = position
  for (let height = 0; height < 3; height++) check('five_wide_three_high_public_routes', nameAt(east, up + height, south) === 'minecraft:air')
  check('level_supported_public_floor', !['minecraft:air', 'MISSING'].includes(nameAt(east, 1, south)))
}
for (const port of metadata.walking_ports) {
  const [east, up, south] = port.feet
  for (let width = -2; width <= 2; width++) for (let height = 0; height < port.height; height++) check('declared_five_wide_four_high_ports', port.facing === 'east' ? nameAt(east, up + height, south + width) === 'minecraft:air' : nameAt(east + width, up + height, south) === 'minecraft:air')
}
for (let east = 0; east < 33; east++) for (let south = 0; south < 31; south++) check('complete_roof', nameAt(east, 13, south) === 'minecraft:stone_bricks')
const tracks = metadata.rail_networks.flatMap(network => network.cells)
check('exact_rail_count', materials['minecraft:rail'] === tracks.length && tracks.length === 73)
for (const [east, up, south] of tracks) {
  check('rails_and_full_block_support', nameAt(east, up, south) === 'minecraft:rail' && nameAt(east, 1, south) === 'minecraft:polished_andesite')
  check('rails_not_on_public_routes', !routeCells.has([east, up, south].join(',')))
  check('rail_two_block_headroom', nameAt(east, 3, south) === 'minecraft:air' && nameAt(east, 4, south) === 'minecraft:air')
}
for (const [east, up, south] of metadata.lamps) {
  check('lamp_record', nameAt(east, up, south) === 'minecraft:lantern')
  for (let height = up + 1; height <= 12; height++) check('continuous_chain_to_roof', nameAt(east, height, south) === 'minecraft:chain')
}
check('single_palette_no_entities', file.root.getList('entities').length === 0 && !file.root.has('palettes'))
const output = { ...metadata, schema: 'ft-independent-zone-room-v1', minecraft: '1.20.1', data_version: 3465, sha256: hash(bytes), records: blocks.length, explicit_air: materials['minecraft:air'], materials, deployment: 'independent-delivery-not-integrated', integration_requirements: ['Inspect entire rotated volume, including air, entity/fluid/world-border occupancy before placement.', 'Rotate local feet coordinates and block states together using native StructureTemplate rotation.', 'Maintain generation markers; never overwrite generated rooms or player changes.', 'Walk connections attach at feet y=2 with a full floor at y=1; rail continuation attaches at rail y=2 with full support y=1.', 'Three independent fixed tracks; no automatic switching, cart spawning, transport logic or powered runtime promised.'] }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(output, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ id: 'N02', status: 'PASS', sha256: hash(bytes), checks, records: blocks.length, public_route_cells: routeCells.size, rails: tracks.length, lamps: metadata.lamps.length, zero_entities: true, zero_block_entities: true }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: output.sha256, size: metadata.size, records: blocks.length, routes: routeCells.size, rails: tracks.length }))
