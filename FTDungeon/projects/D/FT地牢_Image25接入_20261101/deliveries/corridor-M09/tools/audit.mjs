import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { root, readRoom, sha256, writeJson } from './common.mjs'

const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M09-obsidian-trial-damaged.png'
const referenceHash = '485e717ae64f6bc8b0ba8ba59512a904b2bc43fced70389aa6df650440f70de9'
const room = await readRoom()
const referenceBytes = await fs.readFile(reference)
const blocks = room.structure.getBlocks()
const size = room.structure.getSize()
const nameAt = position => room.structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const key = position => position.join(',')
const requiredFeet = []
const clearance = []
const fullFloor = new Set(['minecraft:deepslate_tiles', 'minecraft:cracked_deepslate_tiles', 'minecraft:polished_blackstone_bricks', 'minecraft:polished_blackstone', 'minecraft:gilded_blackstone', 'minecraft:gold_block'])
const forbidden = new Set(['lava', 'water', 'fire', 'soul_fire', 'magma_block', 'cobweb', 'powder_snow', 'cactus', 'wither_rose', 'sweet_berry_bush', 'campfire', 'soul_campfire', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void', 'light'])
const checks = []
function check(name, callback, details) {
  try {
    callback()
    checks.push({ name, status: 'PASS', details })
  } catch (error) {
    checks.push({ name, status: 'FAIL', error: error.message })
  }
}
check('reference_sha256', () => assert.equal(sha256(referenceBytes), referenceHash))
check('gzip_single_palette_1201', () => {
  assert.equal(room.bytes[0], 0x1f)
  assert.equal(room.bytes[1], 0x8b)
  assert.equal(room.file.root.getNumber('DataVersion'), 3465)
  assert.ok(!room.file.root.has('palettes'))
  assert.ok(room.file.root.has('palette'))
})
check('medium_dimensions_explicit_volume', () => {
  assert.deepEqual(size, [15, 12, 41])
  assert.ok(size[2] >= 32 && size[2] <= 48)
  assert.equal(blocks.length, 15 * 12 * 41)
  assert.equal(new Set(blocks.map(block => key(block.pos))).size, blocks.length)
  for (const block of blocks) assert.ok(block.pos.every((coordinate, index) => coordinate >= 0 && coordinate < size[index]))
}, { size, length_chunks: 41 / 16, explicit_records: blocks.length })
check('no_entities_block_entities_or_hazards', () => {
  assert.equal(room.file.root.getList('entities').length, 0)
  for (const block of blocks) {
    assert.ok(!forbidden.has(block.state.getName().path), block.state.toString())
    assert.equal(block.nbt, undefined)
  }
})
for (let posZ = 0; posZ < 41; posZ++) {
  for (let posX = 6; posX <= 8; posX++) {
    requiredFeet.push([posX, 2, posZ])
    for (let posY = 2; posY <= 4; posY++) clearance.push([posX, posY, posZ])
  }
}
check('straight_three_wide_three_high_route', () => {
  for (const coordinate of clearance) assert.equal(nameAt(coordinate), 'minecraft:air', key(coordinate))
  for (const [posX, feetY, posZ] of requiredFeet) assert.ok(fullFloor.has(nameAt([posX, feetY - 1, posZ])))
}, { clear_cells: clearance.length, safe_feet: requiredFeet.length, y: 2 })
check('north_south_openings_five_wide', () => {
  for (const posZ of [0, 40]) {
    for (let posX = 5; posX <= 9; posX++) {
      assert.ok(fullFloor.has(nameAt([posX, 1, posZ])))
      for (let posY = 2; posY <= 6; posY++) assert.equal(nameAt([posX, posY, posZ]), 'minecraft:air')
    }
  }
})
check('continuous_roof_foundation_and_no_side_branches', () => {
  for (let posX = 0; posX < 15; posX++) {
    for (let posZ = 0; posZ < 41; posZ++) {
      assert.notEqual(nameAt([posX, 0, posZ]), 'minecraft:air')
      assert.notEqual(nameAt([posX, 11, posZ]), 'minecraft:air')
    }
  }
  for (const posX of [0, 14]) {
    for (let posZ = 0; posZ < 41; posZ++) {
      for (let posY = 2; posY <= 10; posY++) assert.notEqual(nameAt([posX, posY, posZ]), 'minecraft:air')
    }
  }
})
check('candles_and_gold_buttons_supported', () => {
  for (const block of blocks) {
    const name = block.state.getName().path
    if (name === 'candle') {
      assert.equal(block.state.getProperties().lit, 'true')
      assert.ok(['minecraft:polished_blackstone_bricks', 'minecraft:cracked_deepslate_bricks'].includes(nameAt([block.pos[0], block.pos[1] - 1, block.pos[2]])))
    }
    if (name === 'polished_blackstone_button') {
      const direction = block.state.getProperties().facing === 'east' ? -1 : 1
      assert.equal(nameAt([block.pos[0] + direction, block.pos[1], block.pos[2]]), 'minecraft:gold_block')
    }
  }
})
check('obsidian_trial_visual_features', () => {
  const count = name => blocks.filter(block => block.state.getName().path === name).length
  assert.ok(count('obsidian') > 1200)
  assert.ok(count('crying_obsidian') > 150)
  assert.equal(count('candle'), 12)
  assert.ok(count('gold_block') >= 20)
  assert.ok(count('shroomlight') >= 80)
  assert.ok(count('deepslate_brick_slab') >= 6)
  for (const posZ of [1, 8, 15, 22, 29, 38]) assert.equal(nameAt([7, 9, posZ]), 'minecraft:gilded_blackstone')
})
const counts = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  counts[name] = (counts[name] ?? 0) + 1
}
const metadata = {
  id: 'corridor-M09', design_id: 'M09', name: '黑曜试炼残拱长廊',
  minecraft_version: '1.20.1', data_version: 3465, size,
  entrance: [7, 2, 0], interior: [7, 2, 3], exit: [7, 2, 40], facing: 'north',
  connectors: [{ side: 'north', feet: [7, 2, 0], width: 5, height: 5 }, { side: 'south', feet: [7, 2, 40], width: 5, height: 5 }],
  corridor: { kind: 'medium', length: 41, length_chunks: 2.5625, axis: 'z', required_clearance: { min: [6, 2, 0], max: [8, 4, 40] }, branches: 0, stairs_on_route: false },
  sha256: room.hash, reference, reference_sha256: referenceHash,
  palette: ['obsidian', 'crying_obsidian', 'deepslate_bricks', 'deepslate_tiles', 'polished_blackstone_bricks', 'gold_block', 'gilded_blackstone', 'orange_stained_glass', 'shroomlight', 'candle'],
  design_translation: ['Six repeated stepped ribs with three missing shoulder fragments; continuous sealed roof.', 'Twelve supported, lit multi-candle groups; gold plates carry dark stone button runes.', 'Orange glass and encased shroomlight safely approximate lava seams; no real lava or magma.', 'Side rubble never enters the straight three-wide, three-high required route.'],
  entities: 0, block_entities: 0, loot: false, deployment: 'not_deployed', player_saves_modified: false,
  integration: 'Independent template only. Core owner must validate whole-volume occupancy, connector placement and idempotence before deployment.',
}
const status = checks.every(result => result.status === 'PASS') ? 'PASS' : 'FAIL'
await writeJson('room.json', metadata)
await writeJson('qa/blueprint.json', { size, required_feet: requiredFeet, required_clearance: clearance, reference_sha256: referenceHash })
await writeJson('qa/materials.json', Object.fromEntries(Object.entries(counts).sort((left, right) => right[1] - left[1])))
await writeJson('qa/static.json', { status, source_nbt_sha256: room.hash, reference_sha256: referenceHash, checks, unique_blocks: Object.keys(counts).length, explicit_records: blocks.length, non_air_blocks: blocks.length - (counts['minecraft:air'] ?? 0), limitations: ['Native registry/state validity and collision/support checks require the separate Forge GameTest report.'] })
console.log(JSON.stringify({ status, checks, sha256: room.hash }, null, 2))
if (status !== 'PASS') process.exitCode = 1
