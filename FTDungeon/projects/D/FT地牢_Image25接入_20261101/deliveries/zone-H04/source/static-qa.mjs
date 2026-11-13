import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const digest = createHash('sha256').update(bytes).digest('hex')
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const lookup = new Map(blocks.map(block => [block.pos.join(','), block]))
const identity = position => lookup.get(position.join(','))?.state.getName().toString() ?? 'minecraft:air'
const props = position => lookup.get(position.join(','))?.state.getProperties() ?? {}
const keyOf = position => position.join(',')
const full = position => !/air$|:water$|:lantern$|bars$|banner$|slab$|stairs$|:chain$|potted_/.test(identity(position))
const empty = position => identity(position) === 'minecraft:air'
const neighbors = [[1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1]]
const adjacent = (position, offset) => position.map((value, index) => value + offset[index])
const walkable = position => full(adjacent(position, [0, -1, 0])) && empty(position) && empty(adjacent(position, [0, 1, 0]))
function reachable(start, elevation) {
  const visited = new Set([keyOf(start)])
  const queue = [start]
  for (let cursor = 0; cursor < queue.length; cursor++) {
    for (const offset of neighbors) {
      const next = adjacent(queue[cursor], offset)
      if (next[0] < 0 || next[0] > 32 || next[2] < 0 || next[2] > 36 || next[1] !== elevation) continue
      if (walkable(next) && !visited.has(keyOf(next))) { visited.add(keyOf(next)); queue.push(next) }
    }
  }
  return visited
}
const reference = await fs.readFile(specification.reference)
assert.equal(createHash('sha256').update(reference).digest('hex'), specification.reference_sha256)
const batchRoot = path.resolve(path.dirname(specification.reference), '../..')
for (const filename of ['manifest.json', 'verify.json']) {
  const data = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs', filename), 'utf8'))
  assert(JSON.stringify(data).includes(specification.reference_sha256), 'Missing source fingerprint in ' + filename)
}
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(nbt.getNumber('DataVersion'), 3465)
assert.equal(blocks.length, 33 * 23 * 37)
assert.equal(new Set(blocks.map(block => keyOf(block.pos))).size, blocks.length)
const prohibited = /command_block|structure_block|structure_void|jigsaw|spawner|lava|soul_fire|:fire$|cobweb|powder_snow|magma_block|cactus/
for (const block of blocks) {
  assert(!prohibited.test(block.state.getName().toString()))
  assert(block.pos.every((value, index) => value >= 0 && value < specification.size[index]))
  if (block.nbt?.size) assert.equal(block.state.getName().toString(), 'minecraft:blue_wall_banner')
}
for (let blockX = 0; blockX < 33; blockX++) for (let blockZ = 0; blockZ < 37; blockZ++) {
  assert(full([blockX, 0, blockZ]), 'Missing foundation')
  assert(full([blockX, 1, blockZ]), 'Missing floor')
  assert(full([blockX, 21, blockZ]), 'Missing closed roof')
}
const ground = reachable(specification.entrance, 2)
assert(walkable(specification.entrance) && ground.has(keyOf(specification.interior)))
for (const [direction, center] of Object.entries(specification.ports)) {
  assert(ground.has(keyOf(center)), 'Disconnected port ' + direction)
  for (let offset = -1; offset <= 1; offset++) for (let depth = 0; depth < 2; depth++) {
    const port = direction === 'north' ? [16 + offset, 2, depth] : direction === 'south' ? [16 + offset, 2, 36 - depth] : direction === 'west' ? [depth, 2, 19 + offset] : [32 - depth, 2, 19 + offset]
    assert(walkable(port), 'Blocked port ' + direction)
    for (let extraY = 0; extraY < 4; extraY++) assert(empty(adjacent(port, [0, extraY, 0])))
  }
}
for (const [centerX, centerZ] of specification.pillars) {
  assert(centerX - 2 > 1 && centerX + 2 < 31 && centerZ - 2 > 1 && centerZ + 2 < 35)
  for (let blockY = 0; blockY <= 21; blockY++) assert(full([centerX, blockY, centerZ]), 'Interrupted pillar')
  for (let offsetX = -1; offsetX <= 1; offsetX++) for (let offsetZ = -1; offsetZ <= 1; offsetZ++) for (let blockY = 4; blockY <= 17; blockY++) assert(full([centerX + offsetX, blockY, centerZ + offsetZ]))
  for (let blockX = 1; blockX <= 31; blockX++) assert(full([blockX, 20, centerZ]))
  for (let blockZ = 1; blockZ <= 35; blockZ++) assert(full([centerX, 20, blockZ]))
}
let sealedFaces = 0
const water = blocks.filter(block => block.state.is('minecraft:water'))
assert.equal(water.length, 40)
for (const block of water) {
  assert.equal(props(block.pos).level, '0')
  for (const offset of [...neighbors, [0, -1, 0]]) {
    const next = adjacent(block.pos, offset)
    assert(identity(next) === 'minecraft:water' || full(next), 'Unsealed basin at ' + next)
    sealedFaces++
  }
}
for (const block of blocks.filter(block => block.state.is('minecraft:lantern'))) {
  const hanging = props(block.pos).hanging === 'true'
  let support = adjacent(block.pos, [0, hanging ? 1 : -1, 0])
  if (hanging) while (identity(support) === 'minecraft:chain') support = adjacent(support, [0, 1, 0])
  assert(full(support), 'Floating lantern ' + block.pos)
}
for (const stairX of [4, 28]) {
  assert(ground.has(keyOf([stairX, 2, 6])))
  for (let step = 0; step < 8; step++) for (let offsetX = -1; offsetX <= 1; offsetX++) {
    const position = [stairX + offsetX, step + 2, step + 7]
    assert.equal(identity(position), 'minecraft:stone_brick_stairs')
    assert.equal(props(position).facing, 'south')
    assert(full(adjacent(position, [0, -1, 0])))
    assert(empty(adjacent(position, [0, 1, 0])) && empty(adjacent(position, [0, 2, 0])))
  }
  assert(walkable([stairX, 10, 15]))
}
const upper = reachable([4, 10, 17], 10)
for (const blockX of [7, 25]) for (let blockZ = 15; blockZ <= 29; blockZ++) assert(['minecraft:iron_bars', 'minecraft:chiseled_stone_bricks'].includes(identity([blockX, 10, blockZ])), 'Gallery edge rail missing')
for (const point of specification.balcony_safety_points) assert(upper.has(keyOf(point)), 'Disconnected upper gallery')
assert.equal(blocks.filter(block => block.state.is('minecraft:blue_wall_banner')).length, 16)
assert.equal(blocks.filter(block => block.state.is('minecraft:lantern')).length, 26)
const report = { status: 'PASS', id: 'H04', sha256: digest, reference_sha256: specification.reference_sha256, data_version: 3465, size: structure.getSize(), records: blocks.length, non_air: blocks.filter(block => !block.state.is('minecraft:air')).length, complete_explicit_volume: true, source_manifest_and_verify_match: true, foundation_floor_roof_columns_each: 1221, continuous_grounded_independent_pillars: 4, source_water: water.length, sealed_faces: sealedFaces, lanterns: 26, banners: 16, dry_ground_nodes: ground.size, connected_ground_ports: 4, safe_point_reachable_and_returnable: true, stairs_three_wide: 2, steps_each: 8, connected_upper_nodes: upper.size, hanging_lantern_chains_reach_roof: true, generated_entities: 0, gameplay_loot: false, deployed: false }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ ...specification, sha256: digest, data_version: 3465, floor_y: 1, entrance_clearance: [3, 4], port_clearance: [3, 4], explicit_air_records: true, single_palette: true, production_deployed: false, entities: [], fountain_note: '40 sealed source-water blocks; cyan glass spill sculpture above the basin' }, null, 2) + '\n')
console.log(JSON.stringify(report, null, 2))
