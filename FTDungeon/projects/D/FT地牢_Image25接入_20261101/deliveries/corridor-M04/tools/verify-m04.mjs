import fs from 'node:fs/promises'
import crypto from 'node:crypto'
import path from 'node:path'
import { pathToFileURL, fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(`${skill}/node_modules/@mattzh72/lodestone/lib/index.js`).href)
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M04-end-flooded.png'
const expectedReferenceHash = '537f369f2598d7c3f603011fca9b64e69f2793fe458285bc44da1afdeee10eee'
const digest = payload => crypto.createHash('sha256').update(payload).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(new Uint8Array(bytes)).root
const structure = Structure.fromNbt(nbt)
const state = position => structure.getBlock(position)?.state
const name = position => state(position)?.getName().toString()
const assert = (condition, description) => { if (!condition) throw new Error(description) }
const size = structure.getSize()
const records = structure.getBlocks()
const sha256 = digest(bytes)
const referenceHash = digest(await fs.readFile(reference))
assert(referenceHash === expectedReferenceHash, 'Reference image hash mismatch')
assert(JSON.stringify(size) === '[13,12,41]', 'Wrong dimensions')
assert(nbt.getNumber('DataVersion') === 3465, 'Not a 1.20.1 structure')
assert(records.length === 13 * 12 * 41, 'Incomplete explicit volume')
assert(nbt.getList('entities').length === 0, 'Unexpected entities')
const forbidden = new Set(['command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'spawner', 'end_portal', 'nether_portal', 'end_gateway', 'lava', 'fire', 'soul_fire', 'cobweb', 'powder_snow'])
const inventory = {}
let fluidCount = 0
let explicitAir = 0
let blockEntities = 0
for (const record of records) {
  const identifier = record.state.getName().toString()
  inventory[identifier] = (inventory[identifier] ?? 0) + 1
  assert(identifier.startsWith('minecraft:'), 'Non-vanilla palette')
  assert(!forbidden.has(identifier.slice(10)), `Forbidden block: ${identifier}`)
  assert(!identifier.endsWith('_stairs'), 'Stair block in straight level corridor')
  if (record.nbt) blockEntities += 1
  if (identifier === 'minecraft:air') explicitAir += 1
  if (identifier === 'minecraft:water') {
    fluidCount += 1
    const [width, height, depth] = record.pos
    assert(height === 1 && depth >= 2 && depth <= 38 && (width >= 2 && width <= 4 || width >= 8 && width <= 10), 'Fluid outside basin')
    assert(record.state.getProperties().level === '0', 'Non-source water')
    for (const [stepX, stepY, stepZ] of [[0, -1, 0], [1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1]]) {
      const neighbor = name([width + stepX, height + stepY, depth + stepZ])
      assert(neighbor && neighbor !== 'minecraft:air', 'Uncontained fluid edge')
    }
  }
}
assert(fluidCount === 222 && blockEntities === 0, 'Unexpected water or block entities')
let routeCells = 0
for (let depth = 0; depth <= 40; depth += 1) {
  for (let width = 5; width <= 7; width += 1) {
    for (let height = 2; height <= 4; height += 1) assert(name([width, height, depth]) === 'minecraft:air', 'Three-high dry passage obstructed')
    assert(['minecraft:purpur_block', 'minecraft:end_stone_bricks', 'minecraft:sea_lantern'].includes(name([width, 1, depth])), 'Unsafe dry route floor')
    routeCells += 1
  }
  for (let width = 0; width <= 12; width += 1) assert(name([width, 11, depth]) !== 'minecraft:air', 'Open roof')
  for (const side of [0, 12]) {
    for (let height = 1; height <= 10; height += 1) assert(name([side, height, depth]) !== 'minecraft:air', 'Side branch or missing wall')
  }
}
for (const depth of [0, 40]) {
  for (let width = 0; width <= 12; width += 1) assert((name([width, 2, depth]) === 'minecraft:air') === (width >= 5 && width <= 7), 'End face has noncanonical exit')
}
const metadata = {
  id: 'corridor-M04', source_id: 'M04', name: '末地淹水吊笼长廊', kind: 'medium',
  minecraft: '1.20.1', data_version: 3465, size,
  entrance: [6, 2, 0], interior: [6, 2, 2], exit: [6, 2, 40], facing: 'north',
  ports: [
    { side: 'north', center: [6, 2, 0], width: 3, height: 4, floor_y: 1 },
    { side: 'south', center: [6, 2, 40], width: 3, height: 4, floor_y: 1 },
  ],
  corridor: { axis: 'z', length_blocks: 41, length_chunks: 41 / 16, straight: true, turns: 0, branches: 0, stair_transitions: 0, dry_path: { x: [5, 7], feet_y: 2, z: [0, 40], clear_height: 3, width: 3 } },
  sha256, reference, reference_sha256: referenceHash,
  reference_batch: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103', palette: inventory,
  fluids: { water_sources: fluidCount, basin_depth_blocks: 1, covered: false, contained: true, pools: [{ min: [2, 1, 2], max: [4, 1, 38] }, { min: [8, 1, 2], max: [10, 1, 38] }] },
  design_changes: ['Shallow flooding is kept in two 3-wide basins; a 3-wide level dry central causeway replaces the unsafe fully flooded main route.', 'The pictured dark end becomes a second open 3x4 portal; no door, branch, turn or staircase is introduced.', 'Cages are empty block-only decorations: no captive entities, spawners, loot or active portals.'],
  preview: { file: 'preview.png', source: 'Actual final room.nbt, Lodestone default resource pack software WebGL rendering; not a Minecraft client screenshot or AI image.' },
  deployment: { deployed: false, player_saves_opened: false, integration_performed: false },
}
const report = {
  status: 'PASS', id: metadata.id, structure_sha256: sha256, reference_sha256: referenceHash,
  minecraft: '1.20.1', size, explicit_records: records.length, explicit_air_records: explicitAir,
  palette: inventory, block_entities: blockEntities, entities: 0,
  checks: { reference_sha256: true, gzip_nbt: bytes[0] === 31 && bytes[1] === 139, data_version: true, full_explicit_volume: true, duplicate_positions: new Set(records.map(record => record.pos.join(','))).size === records.length, vanilla_names_only: true, no_forbidden_blocks: true, no_stairs: true, bounded_water: true, complete_roof: true, parallel_closed_sidewalls: true, north_south_only_openings: true, level_dry_three_wide_three_high_route: true },
  dry_route: { cells: routeCells, entrance: metadata.entrance, exit: metadata.exit, floor_y: 1, feet_y: 2 },
  source_water_blocks: fluidCount,
  scope: 'Static NBT geometry and safety checks, not native Forge placement or client playtesting.',
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), `${JSON.stringify(metadata, null, 2)}\n`)
await fs.writeFile(path.join(root, 'qa/static-qa.json'), `${JSON.stringify(report, null, 2)}\n`)
console.log(JSON.stringify({ status: report.status, sha256, records: records.length, routeCells, fluidCount, size }))
