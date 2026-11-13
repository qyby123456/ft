import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import assert from 'node:assert/strict'
import {fileURLToPath} from 'node:url'
import {NbtFile, Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M13-overgrown-ruins-overgrown.png'
const referenceHash = '1fdf0434a35e4261f88d3de7b228deaba30f1b0af06ad467d723c4509fba9e66'
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = crypto.createHash('sha256').update(bytes).digest('hex')
assert.equal(crypto.createHash('sha256').update(await fs.readFile(reference)).digest('hex'), referenceHash)
const data = NbtFile.read(bytes).root
const structure = Structure.fromNbt(data)
const size = structure.getSize()
const blocks = structure.getBlocks()
const nameAt = position => structure.getBlock(position)?.state.getName().toString()
const forbidden = new Set(['spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'barrier', 'lava', 'water', 'fire', 'soul_fire', 'powder_snow', 'cobweb', 'tnt'])
const positions = new Set()
const materials = {}
let air = 0
let vines = 0
let lanterns = 0
let moss = 0
for (const block of blocks) {
  const identity = block.state.getName().toString()
  assert.ok(identity.startsWith('minecraft:'))
  assert.ok(!forbidden.has(identity.slice(10)))
  assert.ok(block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < size[axis]))
  assert.ok(!positions.has(block.pos.join(',')))
  positions.add(block.pos.join(','))
  assert.ok(!block.nbt || block.nbt.size === 0, 'Unexpected block entity')
  materials[identity] = (materials[identity] ?? 0) + 1
  if (identity === 'minecraft:air') air++
  if (identity === 'minecraft:vine') {
    vines++
    const properties = block.state.getProperties()
    const supportX = block.pos[0] + (properties.west === 'true' ? -1 : 1)
    assert.ok(!['minecraft:air', 'minecraft:vine'].includes(nameAt([supportX, block.pos[1], block.pos[2]])))
  }
  if (identity.includes('moss')) moss++
  if (identity === 'minecraft:lantern') {
    lanterns++
    assert.equal(block.pos[1], 6)
    for (let blockY = 7; blockY < 10; blockY++) assert.equal(nameAt([block.pos[0], blockY, block.pos[2]]), 'minecraft:chain')
    assert.notEqual(nameAt([block.pos[0], 10, block.pos[2]]), 'minecraft:air')
  }
  if (identity === 'minecraft:moss_carpet') assert.notEqual(nameAt([block.pos[0], block.pos[1] - 1, block.pos[2]]), 'minecraft:air')
}
assert.deepEqual(size, [13, 11, 40])
assert.equal(data.getNumber('DataVersion'), 3465)
assert.equal(data.getList('entities').length, 0)
assert.equal(blocks.length, size.reduce((product, value) => product * value, 1))
let clearPositions = 0
for (let blockZ = 0; blockZ < 40; blockZ++) {
  for (let blockX = 5; blockX <= 7; blockX++) {
    assert.ok(!['minecraft:air', 'minecraft:vine', 'minecraft:moss_carpet'].includes(nameAt([blockX, 0, blockZ])))
    for (let blockY = 1; blockY <= 3; blockY++) {
      assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air')
      clearPositions++
    }
  }
  for (let blockX = 0; blockX < 13; blockX++) assert.notEqual(nameAt([blockX, 10, blockZ]), 'minecraft:air')
  for (let blockY = 1; blockY < 10; blockY++) {
    assert.notEqual(nameAt([0, blockY, blockZ]), 'minecraft:air')
    assert.notEqual(nameAt([12, blockY, blockZ]), 'minecraft:air')
  }
}
assert.equal(lanterns, 5)
assert.ok(vines >= 180 && moss >= 500)
const metadata = {
  id: 'M13', name: '苔覆遗迹断拱长廊', type: 'corridor', kind: 'medium', size,
  entrance: [6, 1, 0], interior: [6, 1, 4], exit: [6, 1, 39], facing: 'north',
  axis: 'north-south', length_blocks: 40, length_chunks: 2.5,
  ports: [
    {name: 'north', feet: [6, 1, 0], outward: 'north', clear_width: 3, clear_height: 3},
    {name: 'south', feet: [6, 1, 39], outward: 'south', clear_width: 3, clear_height: 3}
  ],
  clear_lane: {x: [5, 7], feet_y: 1, z: [0, 39], height: 3, step_free: true},
  sha256, data_version: 3465, minecraft_version: '1.20.1', compression: 'gzip',
  reference, reference_sha256: referenceHash,
  reference_index: {theme: 'overgrown-ruins', condition: 'overgrown', light: 'lanterns', feature: 'broken-arches', palette: 'gray-green stone, green vines, moss, dark wood'},
  arch_stations_z: [0, 8, 16, 24, 32, 39], hanging_lanterns: [[6, 6, 4], [6, 6, 12], [6, 6, 20], [6, 6, 28], [6, 6, 36]],
  placement: {ignore_entities: true, keep_liquids: false, known_shape: true, block_flags: 18, explicit_air_is_not_permission_to_clear: true},
  integration_status: 'independent_delivery_not_deployed', live_growth: 'Vines retain vanilla random-tick growth; persistent leaves do not decay.',
  scope: 'Only M13 delivery assets; no production mods, registration, deployment or player saves changed.'
}
const report = {
  status: 'PASS', id: 'M13', sha256, reference_sha256: referenceHash, size,
  data_version: 3465, compression: 'gzip', block_records: blocks.length,
  non_air_blocks: blocks.length - air, explicit_air_blocks: air,
  palette_states: new Set(blocks.map(block => block.state.toString())).size,
  entities: 0, block_entities: 0, bounds_and_unique_coordinates: 'PASS',
  complete_volume_and_roof: 'PASS', closed_sidewalls_no_branches: 'PASS',
  straight_lane_clear_cells: clearPositions, lane_width: 3, lane_height: 3,
  entire_length_step_free: 'PASS', north_and_south_3x3_ports: 'PASS',
  vines, moss_family_blocks: moss, hanging_lanterns: lanterns,
  chain_ceiling_anchors: 'PASS', vine_wall_anchors: 'PASS', carpet_support: 'PASS',
  unsafe_blocks_or_fluids: 0, inventory_or_rewards: 0,
  registry_validation: 'See qa/native.json; static names alone do not establish native registry validity.',
  materials
}
await fs.mkdir(path.join(root, 'qa'), {recursive: true})
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({status: report.status, size, sha256, nonAir: report.non_air_blocks, vines, lanterns}, null, 2))
