import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification, onRoute, onGuard } from './spec.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const hash = digest(bytes)
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const solidNames = new Set(['stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'smooth_stone', 'chiseled_stone_bricks'].map(name => 'minecraft:' + name))
const fullBlock = position => solidNames.has(nameAt(position))
const directions = [[1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1], [0, -1, 0]]
assert.equal(digest(await fs.readFile(specification.reference)), specification.reference_sha256)
const sourceRoot = path.resolve(path.dirname(specification.reference), '../..')
for (const [filename, field] of [['manifest.json', 'files'], ['verify.json', 'records']]) {
  const report = JSON.parse(await fs.readFile(path.join(sourceRoot, 'docs', filename), 'utf8'))
  const entry = report[field].find(entry => entry.file.endsWith('C02-corners-flooded-corner-bend.png'))
  assert.equal(entry.sha256, specification.reference_sha256)
  assert.equal(entry.ok, true)
}
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(nbt.getNumber('DataVersion'), 3465)
assert.equal(nbt.has('palettes'), false)
assert.equal(nbt.getList('entities').length, 0)
assert.equal(blocks.length, 3971)
assert.equal(new Set(blocks.map(block => block.pos.join(','))).size, 3971)
const histogram = {}
const waters = []
const containers = []
const guards = []
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert.ok(name.startsWith('minecraft:'))
  assert.ok(!/(command_block|structure_block|structure_void|jigsaw|spawner|lava|fire|cobweb|powder_snow|magma_block|cactus)$/.test(name))
  assert.ok(structure.isInside(block.pos))
  histogram[name] = (histogram[name] ?? 0) + 1
  if (name === 'minecraft:water') {
    assert.equal(block.state.getProperty('level'), '0')
    assert.equal(block.pos[1], 1)
    waters.push(block.pos)
  }
  if (name === 'minecraft:spruce_fence') {
    assert.equal(block.state.getProperty('waterlogged'), 'false')
    assert.ok(onGuard(block.pos[0], block.pos[2]))
    assert.ok(fullBlock([block.pos[0], block.pos[1] - 1, block.pos[2]]))
    guards.push(block.pos)
  }
  if (name === 'minecraft:barrel') {
    assert.equal(block.nbt.getString('id'), 'minecraft:barrel')
    assert.equal(block.nbt.getList('Items').length, 0)
    assert.ok(!block.nbt.has('LootTable'))
    assert.ok(fullBlock([block.pos[0], block.pos[1] - 1, block.pos[2]]))
    containers.push(block.pos)
  } else assert.ok(!block.nbt || block.nbt.size === 0)
}
let sealFaces = 0
for (const water of waters) {
  for (const direction of directions) {
    const neighbor = water.map((value, axis) => value + direction[axis])
    assert.ok(structure.isInside(neighbor))
    assert.ok(nameAt(neighbor) === 'minecraft:water' || fullBlock(neighbor), 'Unsealed water at ' + neighbor)
    sealFaces++
  }
}
const route = []
for (let blockX = 0; blockX < 19; blockX++) {
  for (let blockZ = 0; blockZ < 19; blockZ++) {
    assert.ok(fullBlock([blockX, 0, blockZ]))
    assert.ok(fullBlock([blockX, 10, blockZ]))
    if (!onRoute(blockX, blockZ)) continue
    assert.ok(fullBlock([blockX, 1, blockZ]))
    for (let blockY = 2; blockY <= 4; blockY++) assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air')
    route.push([blockX, 2, blockZ])
  }
}
const flood = start => {
  const allowed = new Set(route.map(position => position.join(',')))
  const visited = new Set([start.join(',')])
  const queue = [start]
  for (let index = 0; index < queue.length; index++) {
    for (const direction of directions.slice(0, 4)) {
      const next = queue[index].map((value, axis) => value + direction[axis])
      if (allowed.has(next.join(',')) && !visited.has(next.join(','))) {
        visited.add(next.join(','))
        queue.push(next)
      }
    }
  }
  return visited
}
assert.equal(route.length, 57)
assert.ok(flood(specification.entrance).has(specification.exit.join(',')))
assert.ok(flood(specification.exit).has(specification.entrance.join(',')))
assert.equal(flood(specification.entrance).size, 57)
for (const [lightX, lightZ] of specification.hanging_lights) {
  assert.equal(nameAt([lightX, 6, lightZ]), 'minecraft:lantern')
  for (let blockY = 7; blockY <= 9; blockY++) assert.equal(nameAt([lightX, blockY, lightZ]), 'minecraft:chain')
  assert.ok(fullBlock([lightX, 10, lightZ]))
}
assert.equal(containers.length, 2)
assert.ok(fullBlock([16, 1, 16]))
assert.ok(fullBlock([16, 2, 16]))
let closedShellCells = 0
for (let blockX = 0; blockX < 19; blockX++) {
  for (let blockZ = 0; blockZ < 19; blockZ++) {
    if (blockX !== 0 && blockX !== 18 && blockZ !== 0 && blockZ !== 18) continue
    for (let blockY = 1; blockY <= 9; blockY++) {
      const port = blockY >= 2 && blockY <= 5
        && ((blockZ === 0 && blockX >= 8 && blockX <= 10) || (blockX === 18 && blockZ >= 8 && blockZ <= 10))
      if (port) assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air')
      else {
        assert.ok(fullBlock([blockX, blockY, blockZ]))
        closedShellCells++
      }
    }
  }
}
const ports = [
  { side: 'north', feet: specification.entrance, outward: [0, 0, -1], bounds: [[8, 2, 0], [10, 5, 0]], opening: [3, 4] },
  { side: 'east', feet: specification.exit, outward: [1, 0, 0], bounds: [[18, 2, 8], [18, 5, 10]], opening: [3, 4] },
]
const metadata = {
  ...specification, category: 'corner_room', canonical_id: 'zone-C02', minecraft: '1.20.1', data_version: 3465,
  sha256: hash, ports, floor_y: 1,
  walkway: { type: 'north_to_east_90_degree_bend', feet_y: 2, minimum_width: 3, clear_height: 3, cells: route.length, segments: [[[8, 2, 0], [10, 4, 10]], [[8, 2, 8], [18, 4, 10]]], same_level: true, dry: true, guarded_edges: guards.length },
  flooded: { source_blocks: waters.length, y: 1, depth: 1, vertical_sides_and_base_sealed: true, liquid_below_player_feet: true },
  roof: { complete: true, y: 10, preview_cutaways_only: true },
  block_entities: 2, inventory_items: 0, entities: 0, loot_tables: 0,
  builder: { skill: 'minecraft-builder-skill', renderer: 'Lodestone 0.6.0', recipe: 'source/recipe.mjs', explicit_volume: true },
  design_translation: [
    '保留苔裂石柱、铁栅壁龛、暖色链灯、浅水与靠墙空木桶。',
    '参考图大面积淹水地面增加三格宽带栏杆的石质 L 通路；北中央入口转向东中央出口，同高无需跳跃。',
    '原图剖面和开放边界改为完整外墙与完整屋顶；铁栅后保留实体背墙，水仅位于 y=1 的一格深密封池。',
    '省略原图非原版绳圈和箱子贴图，不添加未知模型或模组方块。',
  ],
  integration: { deployed: false, production_world_modified: false, core_modified: false, receiver_review_required: true },
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({
  status: 'PASS', sha256: hash, reference_sha256: specification.reference_sha256,
  size: specification.size, data_version: 3465, records: blocks.length,
  explicit_air_records: histogram['minecraft:air'], source_water_blocks: waters.length,
  sealed_water_neighbor_faces_checked: sealFaces, dry_three_wide_three_high_route_cells: route.length,
  bidirectional_connectivity: true, turn_degrees: 90, ports, complete_roof_cells: 361, sealed_shell_cells: closedShellCells,
  supported_chain_lanterns: 4, supported_table_lanterns: 1, supported_guard_fences: guards.length,
  empty_barrels: 2, inventory_items: 0, loot_tables: 0, entities: 0,
  source_hash_matches_manifest_and_verify: true, palette_histogram: histogram,
  native_state_and_collision_validation: 'qa/forge-native.json',
}, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/fixture.json'), JSON.stringify({ sha256: hash, size: specification.size, route, waters, guards }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash, records: blocks.length, water: waters.length, route: route.length, guards: guards.length }))

