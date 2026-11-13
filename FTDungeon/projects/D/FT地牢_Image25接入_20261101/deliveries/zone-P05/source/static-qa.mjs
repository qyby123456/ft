import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const digest = createHash('sha256').update(bytes).digest('hex')
const data = NbtFile.read(bytes).root
const structure = Structure.fromNbt(data)
const blocks = structure.getBlocks()
const keyOf = position => position.join(',')
const byPosition = new Map(blocks.map(block => [keyOf(block.pos), block]))
const blockAt = position => byPosition.get(keyOf(position))
const nameAt = position => blockAt(position)?.state.getName().toString() ?? 'minecraft:air'
const nonSolid = /minecraft:(?:air|water|ladder|wall_torch|chain|lantern|oak_fence|.*slab)$/
const solid = position => !nonSolid.test(nameAt(position))
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|lava|fire|soul_fire|cactus|magma_block|powder_snow|cobweb)$/
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(blocks.length, 6902)
assert.equal(byPosition.size, blocks.length)
assert.equal(data.getNumber('DataVersion'), 3465)
assert.equal(data.getList('entities').length, 0)
assert.equal(data.has('palettes'), false)
const referenceBytes = await fs.readFile(specification.reference)
assert.equal(createHash('sha256').update(referenceBytes).digest('hex'), specification.reference_sha256)
const batchRoot = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103'
const manifest = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/manifest.json'), 'utf8'))
const verify = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/verify.json'), 'utf8'))
const manifestEntry = manifest.files.find(entry => entry.file.endsWith('P05-parkour-pillar-hop-hall.png'))
const verifyEntry = verify.records.find(entry => entry.file.endsWith('P05-parkour-pillar-hop-hall.png'))
assert.equal(manifestEntry.sha256, specification.reference_sha256)
assert.equal(verifyEntry.sha256, specification.reference_sha256)
assert.equal(manifestEntry.bytes, referenceBytes.length)
assert.deepEqual(manifestEntry.size, [1536, 1024])
const histogram = {}
const waterCells = []
let supportedFixtures = 0
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert.ok(name.startsWith('minecraft:') && !forbidden.test(name), `Forbidden block ${name}`)
  assert.ok(!block.nbt || block.nbt.size === 0)
  assert.ok(structure.isInside(block.pos))
  histogram[name] = (histogram[name] ?? 0) + 1
  const [blockX, blockY, blockZ] = block.pos
  if (name === 'minecraft:water' || block.state.getProperty('waterlogged') === 'true') waterCells.push(block.pos)
  if (name === 'minecraft:wall_torch') {
    assert.ok(solid([blockX === 1 ? 0 : 16, blockY, blockZ]))
    supportedFixtures++
  }
  if (name === 'minecraft:ladder') {
    assert.equal(block.state.getProperty('facing'), 'south')
    assert.ok(solid([blockX, blockY, blockZ - 1]))
    supportedFixtures++
  }
  if (name === 'minecraft:lantern') {
    assert.equal(block.state.getProperty('hanging'), 'true')
    assert.equal(nameAt([blockX, blockY + 1, blockZ]), 'minecraft:chain')
    assert.equal(nameAt([blockX, blockY + 2, blockZ]), 'minecraft:chain')
    assert.ok(solid([blockX, blockY + 3, blockZ]))
    supportedFixtures++
  }
  if (name === 'minecraft:oak_fence' || name.endsWith('_slab')) assert.ok(solid([blockX, blockY - 1, blockZ]))
}
assert.equal(waterCells.length, 304)
const fluidKeys = new Set(waterCells.map(keyOf))
for (const [blockX, blockY, blockZ] of waterCells) {
  assert.equal(blockY, 1)
  for (const [deltaX, deltaY, deltaZ] of [[1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1], [0, -1, 0]]) {
    const neighbor = [blockX + deltaX, blockY + deltaY, blockZ + deltaZ]
    assert.ok(fluidKeys.has(keyOf(neighbor)) || solid(neighbor), `Unsealed fluid ${neighbor}`)
  }
  for (let headY = 2; headY <= 3; headY++) assert.ok(['minecraft:air', 'minecraft:ladder'].includes(nameAt([blockX, headY, blockZ])))
}
let pillarSupportColumns = 0
for (const pillar of specification.pillars) {
  for (let blockX = 7; blockX <= 8; blockX++) {
    for (let blockZ = pillar.from[2]; blockZ <= pillar.to[2]; blockZ++) {
      for (let blockY = 0; blockY <= 4; blockY++) assert.ok(solid([blockX, blockY, blockZ]), `Unrooted pillar ${pillar.id}`)
      for (let blockY = 5; blockY <= 8; blockY++) assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air')
      pillarSupportColumns++
    }
  }
}
for (const portZ of [0, 28]) {
  for (let blockX = 7; blockX <= 9; blockX++) {
    assert.ok(solid([blockX, 4, portZ]))
    for (let blockY = 5; blockY <= 7; blockY++) assert.equal(nameAt([blockX, blockY, portZ]), 'minecraft:air')
  }
}
for (let blockX = 0; blockX <= 16; blockX++) {
  for (let blockZ = 0; blockZ <= 28; blockZ++) {
    assert.ok(solid([blockX, 0, blockZ]))
    assert.ok(solid([blockX, 12, blockZ]))
  }
}
const recovery = new Set()
const queue = [[3, 1, 4]]
while (queue.length) {
  const point = queue.shift()
  if (!fluidKeys.has(keyOf(point)) || recovery.has(keyOf(point))) continue
  recovery.add(keyOf(point))
  for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) queue.push([point[0] + deltaX, 1, point[2] + deltaZ])
}
assert.equal(recovery.size, waterCells.length)
for (const ladder of specification.recovery_ladders) {
  for (let blockY = 1; blockY <= 4; blockY++) assert.equal(nameAt([ladder.x, blockY, 4]), 'minecraft:ladder')
  assert.equal(nameAt([ladder.x, 5, 3]), 'minecraft:air')
  assert.equal(nameAt([ladder.x, 6, 3]), 'minecraft:air')
  assert.ok(solid([ladder.x, 4, 3]))
}
const jumps = [3, 7, 11, 15, 19, 23].map((takeoffTopZ, index) => ({
  leg: index + 1, takeoff: [8, 5, takeoffTopZ + 0.6],
  destination_z: index === 5 ? [26, 28] : [6 + index * 4, 7 + index * 4],
  empty_block_gap: 2, vertical_change: 0, target_width: 2,
}))
const metadata = {
  ...specification, sha256: digest, minecraft: '1.20.1', data_version: 3465,
  type: 'independent_parkour_room', generator: 'Minecraft Builder Skill / Lodestone 0.6.0',
  entrance_clearance: [3, 3], ports: [
    { side: 'north', position: specification.entrance, bounds: [[7, 5, 0], [9, 7, 0]], marker: 'yellow_terracotta' },
    { side: 'south', position: specification.exit, bounds: [[7, 5, 28], [9, 7, 28]], marker: 'lime_terracotta' },
  ],
  parkour: { pillars: 5, jumps: 6, direction: 'north to south', reversible: true, feet_y: 5, pillar_top_y: 4, empty_gap: 2, vertical_change: 0, landing_size: [2, 2], legs: jumps },
  recovery: { basin_y: 1, depth: 1, fluid_cells: waterCells.length, return_to: 'north entrance', ladders: specification.recovery_ladders, all_water_cells_connected: true, south_bank_has_no_recovery_ladder: true },
  roof: { complete: true, y: 12, cutaway_preview_only: true },
  records: blocks.length, explicit_air: histogram['minecraft:air'], entities: 0, block_entities: 0,
  deployment: 'NOT_DEPLOYED', production_files_modified: false,
  placement_contract: 'Future integrator must reject occupied full volume before placing explicit air; retain persistent one-time generation markers.',
}
const report = {
  status: 'PASS', sha256: digest, reference_sha256: specification.reference_sha256,
  reference_manifest_match: true, reference_verify_match: true, size: specification.size,
  data_version: 3465, records: blocks.length, unique_coordinates: byPosition.size,
  single_palette: true, palette_histogram: histogram, forbidden_blocks: 0, entities: 0, block_entities: 0,
  rooted_pillar_columns: pillarSupportColumns, jump_legs: jumps, one_block_deep_fluid_cells: waterCells.length,
  sealed_fluid_faces_checked: waterCells.length * 5, recovery_bfs_cells: recovery.size,
  continuous_supported_recovery_ladders: 2, supported_lamps_torches_ladders: supportedFixtures,
  complete_floor_roof_cells: 17 * 29 * 2, ports: '3 wide x 3 high, both open and grounded',
  native_route_validation: 'See qa/forge-native.json for swept player collision, ballistic jumps, recovery and post-tick results.',
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256: digest, records: blocks.length, water_cells: waterCells.length, pillar_columns: pillarSupportColumns }))
