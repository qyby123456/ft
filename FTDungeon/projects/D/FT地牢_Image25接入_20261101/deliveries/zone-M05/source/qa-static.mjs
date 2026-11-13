import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { specification } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const hash = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const cells = new Map(blocks.map(block => [block.pos.join(','), block]))
const nameAt = (east, up, south) => cells.get([east, up, south].join(','))?.state.getName().path ?? 'air'
const stateAt = (east, up, south) => cells.get([east, up, south].join(','))?.state
const checks = {}
const check = (label, condition) => { if (!condition) throw new Error(label); checks[label] = true }
const isFull = name => !['air', 'water', 'rail', 'chain', 'lantern', 'ladder', 'amethyst_cluster', 'large_amethyst_bud'].includes(name) && !name.endsWith('_slab') && !name.endsWith('_stairs')
const fluids = blocks.filter(block => block.state.getName().path === 'water')
const fixtures = []
const inventory = {}
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('source_reference_hash', hash(await fs.readFile(specification.reference)) === specification.referenceSha256)
check('size', JSON.stringify(structure.getSize()) === JSON.stringify(specification.size))
check('full_explicit_volume_unique_coordinates', blocks.length === 10935 && cells.size === 10935)
check('no_entities', file.root.getList('entities').length === 0)
check('java_1_20_1_data_version', file.root.getNumber('DataVersion') === 3465)
const forbidden = new Set(['command_block', 'repeating_command_block', 'chain_command_block', 'structure_block', 'structure_void', 'jigsaw', 'spawner', 'tnt', 'respawn_anchor', 'lava', 'fire', 'soul_fire', 'cobweb', 'powder_snow'])
for (const block of blocks) {
  const identity = block.state.getName()
  const [east, up, south] = block.pos
  check('coordinates_in_bounds', block.pos.every((coordinate, axis) => coordinate >= 0 && coordinate < specification.size[axis]))
  check('vanilla_palette_no_forbidden_ids', identity.namespace === 'minecraft' && !forbidden.has(identity.path))
  check('no_block_entities_loot_or_items', !block.nbt)
  inventory[identity.toString()] = (inventory[identity.toString()] ?? 0) + 1
  if (identity.path === 'rail' || identity.path === 'lantern' && block.state.getProperties().hanging === 'false' || identity.path.endsWith('_anvil')) check('floor_props_supported', isFull(nameAt(east, up - 1, south)))
  if (['amethyst_cluster', 'large_amethyst_bud'].includes(identity.path)) {
    const facing = block.state.getProperties().facing
    const offset = { up: [0, -1, 0], down: [0, 1, 0], north: [0, 0, 1], south: [0, 0, -1], east: [-1, 0, 0], west: [1, 0, 0] }[facing]
    check('amethyst_attached_to_solid', isFull(nameAt(east + offset[0], up + offset[1], south + offset[2])))
    fixtures.push({ pos: block.pos, facing })
  }
  if (identity.path === 'water') {
    check('water_has_solid_floor', isFull(nameAt(east, up - 1, south)))
    for (const [offsetEast, offsetSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) check('water_laterally_contained', nameAt(east + offsetEast, up, south + offsetSouth) === 'water' || isFull(nameAt(east + offsetEast, up, south + offsetSouth)))
  }
}
for (let east = 0; east < 27; east++) for (let south = 0; south < 27; south++) {
  check('complete_foundation', isFull(nameAt(east, 0, south)))
  check('complete_roof', isFull(nameAt(east, 14, south)))
}
for (const [east, south] of specification.pillars) for (let offsetEast = 0; offsetEast < 2; offsetEast++) for (let offsetSouth = 0; offsetSouth < 2; offsetSouth++) for (let up = 1; up <= 14; up++) check('four_continuous_two_by_two_pillars', isFull(nameAt(east + offsetEast, up, south + offsetSouth)))
for (const [east, up, south] of specification.hangingLamps) {
  check('hanging_lantern_state', nameAt(east, up, south) === 'lantern' && stateAt(east, up, south).getProperties().hanging === 'true')
  for (let height = up + 1; height <= 11; height++) check('continuous_lamp_chains', nameAt(east, height, south) === 'chain')
  check('lamp_chain_fixed_to_ceiling_beam', isFull(nameAt(east, 12, south)))
}
for (const [east, up, south] of specification.blueCrystals) {
  for (let height = 1; height <= up + 3; height++) check('blue_glass_crystal_spines_continuously_grounded', isFull(nameAt(east, height, south)))
  for (const [offsetEast, offsetSouth] of [[-1, 0], [0, 1], [1, 0]]) {
    if (east + offsetEast > 24 || south + offsetSouth > 24) continue
    for (let height = 1; height <= up + 1; height++) check('blue_glass_crystal_branches_continuously_grounded', isFull(nameAt(east + offsetEast, height, south + offsetSouth)))
  }
}
const walkable = (east, south) => east >= 0 && east < 27 && south >= 0 && south < 27 && nameAt(east, 2, south) === 'air' && nameAt(east, 3, south) === 'air' && isFull(nameAt(east, 1, south))
const reached = new Set(['13,0'])
const pending = [[13, 0]]
while (pending.length) {
  const [east, south] = pending.shift()
  for (const [offsetEast, offsetSouth] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const nextEast = east + offsetEast
    const nextSouth = south + offsetSouth
    const key = [nextEast, nextSouth].join(',')
    if (!reached.has(key) && walkable(nextEast, nextSouth)) { reached.add(key); pending.push([nextEast, nextSouth]) }
  }
}
check('interior_safety_point_reachable', reached.has('13,5'))
for (let east = 11; east <= 15; east++) for (let south = 0; south <= 24; south++) {
  check('five_wide_main_route_connected', reached.has([east, south].join(',')))
  for (let up = 2; up <= 5; up++) check('four_clear_blocks_above_main_route', nameAt(east, up, south) === 'air')
}
const roomHash = hash(bytes)
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'materials.json'), JSON.stringify(inventory, null, 2) + '\n')
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ id: specification.id, name: specification.name, category: 'mines', size: specification.size, entrance: specification.entrance, interior: specification.interior, facing: 'north', entrance_width: 5, entrance_height: 5, sha256: roomHash, reference: specification.reference, reference_sha256: specification.referenceSha256, template_id: 'ftdungeon:zone_m05_crystal_mine_grotto', deployment: 'independent_delivery_only', pillars: specification.pillars, main_route: { from: [11, 2, 0], to: [15, 2, 24], dry: true }, water: { source_cells: fluids.length, depth_blocks: 1, contained: true }, notes: ['独立分区 zone-M05；不是 M05 或 corridor-M05', '完整屋顶；四向剖切仅作用于内存预览', '原版固定方块破损矿车道具，无实体、容器或奖励'] }, null, 2) + '\n')
const report = { status: 'PASS', source_sha256: roomHash, reference_sha256: specification.referenceSha256, checks, dimensions: structure.getSize(), records: blocks.length, solid_roof_cells: 729, support_pillars: 4, hanging_lanterns: specification.hangingLamps.length, actual_amethyst_fixtures: fixtures.length, water_sources: fluids.length, reachable_dry_foot_positions: reached.size, central_route_foot_positions: 125, inventory, actual_amethyst_supports: fixtures }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, records: report.records, reachable: reached.size, water: fluids.length, amethyst: fixtures.length, sha256: roomHash }))
