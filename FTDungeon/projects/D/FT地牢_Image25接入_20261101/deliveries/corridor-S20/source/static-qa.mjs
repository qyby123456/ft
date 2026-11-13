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
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const propsAt = position => structure.getBlock(position)?.state.getProperties() ?? {}
const stateAt = position => ({ name: nameAt(position), properties: propsAt(position) })
const keyOf = position => position.join(',')
const reference = await fs.readFile(specification.reference)
assert.equal(createHash('sha256').update(reference).digest('hex'), specification.reference_sha256)
const batchRoot = path.dirname(path.dirname(specification.reference))
const batch = JSON.parse(await fs.readFile(path.join(batchRoot, 'manifest.json'), 'utf8'))
const index = JSON.parse(await fs.readFile(path.join(batchRoot, 'out/corridor_index.json'), 'utf8'))
assert.equal(batch.images.find(entry => entry.id === 'S20').sha256, specification.reference_sha256)
assert.equal(index.find(entry => entry.id === 'S20').theme, 'abandoned-mine')
assert.equal(index.find(entry => entry.id === 'S20').condition, 'flooded')
assert.equal(index.find(entry => entry.id === 'S20').kind, 'short')
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(nbt.getNumber('DataVersion'), 3465)
assert.equal(blocks.length, 1584)
assert.equal(new Set(blocks.map(block => keyOf(block.pos))).size, 1584)
assert.equal(nbt.getList('entities').length, 0)
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|lava|fire|cactus|magma_block|powder_snow|cobweb)$/
const histogram = {}
const waters = []
let torches = 0
let rails = 0
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert.ok(name.startsWith('minecraft:') && !forbidden.test(name), `Forbidden block ${name}`)
  assert.ok(!block.nbt || block.nbt.size === 0, 'Unexpected block entity')
  assert.ok(structure.isInside(block.pos), 'Out of bounds')
  histogram[name] = (histogram[name] ?? 0) + 1
  if (name === 'minecraft:water') {
    assert.equal(block.state.getProperty('level'), '0')
    waters.push(block.pos)
  }
  if (name === 'minecraft:wall_torch') {
    const [blockX, blockY, blockZ] = block.pos
    assert.equal(nameAt([blockX === 2 ? 1 : 9, blockY, blockZ]), 'minecraft:dark_oak_log')
    torches++
  }
  if (name === 'minecraft:rail') {
    assert.equal(block.state.getProperty('shape'), 'north_south')
    assert.equal(block.state.getProperty('waterlogged'), 'false')
    rails++
  }
}
assert.equal(waters.length, 44)
assert.equal(torches, 8)
assert.equal(rails, 16)
const fullBlock = position => !/minecraft:(?:air|water|rail|wall_torch|.*stairs|.*slab)$/.test(nameAt(position))
let sealedFaces = 0
for (const [blockX, blockY, blockZ] of waters) {
  for (const [deltaX, deltaY, deltaZ] of [[1, 0, 0], [-1, 0, 0], [0, 0, 1], [0, 0, -1], [0, -1, 0]]) {
    const neighbor = [blockX + deltaX, blockY + deltaY, blockZ + deltaZ]
    assert.ok(structure.isInside(neighbor))
    assert.ok(nameAt(neighbor) === 'minecraft:water' || fullBlock(neighbor), `Water leak ${neighbor}`)
    sealedFaces++
  }
}
let dryRouteCells = 0
for (let blockZ = 0; blockZ < 16; blockZ++) {
  for (let blockX = 4; blockX <= 6; blockX++) {
    assert.ok(fullBlock([blockX, 1, blockZ]))
    for (let blockY = 2; blockY <= 4; blockY++) {
      assert.ok(['minecraft:air', 'minecraft:rail'].includes(nameAt([blockX, blockY, blockZ])))
    }
    dryRouteCells++
  }
}
for (let blockX = 0; blockX < 11; blockX++) {
  for (let blockZ = 0; blockZ < 16; blockZ++) {
    assert.ok(fullBlock([blockX, 0, blockZ]))
    assert.ok(fullBlock([blockX, 8, blockZ]))
  }
}
for (const blockX of [0, 10]) {
  for (let blockZ = 0; blockZ < 16; blockZ++) {
    for (let blockY = 1; blockY <= 7; blockY++) assert.ok(fullBlock([blockX, blockY, blockZ]))
  }
}
const metadata = {
  ...specification,
  kind: 'short_straight_corridor', facing: 'north', sha256: digest, data_version: 3465,
  minecraft: '1.20.1', generator: 'Minecraft Builder Skill / Lodestone 0.6.0',
  reference_size: [1536, 1024], entrance_clearance: [3, 3],
  ports: [
    { side: 'north', position: specification.entrance, bounds: [[4, 2, 0], [6, 4, 0]], clearance: [3, 3] },
    { side: 'south', position: specification.exit, bounds: [[4, 2, 15], [6, 4, 15]], clearance: [3, 3] },
  ],
  floor_y: 1,
  walkway: { x: [4, 6], y: 2, z: [0, 15], width: 3, height: 3, dry: true, same_level: true },
  flooded: { sources: waters.length, y: 1, depth: 1, open_top: true, sides_and_base_sealed: true },
  roof: { complete: true, y: 8, cutaway_preview_only: true },
  broken_arch_stations: [5, 10], records: blocks.length, block_entities: 0, entities: 0,
  rewards: 'none; ordinary vanilla ore blocks are mineable, no scripted rewards or loot tables',
  deployment: 'independent candidate only; not installed; no save, catalog, road, or production mod changes',
}
const report = {
  status: 'PASS', sha256: digest, reference_sha256: specification.reference_sha256,
  generator: metadata.generator, size: specification.size, records: blocks.length,
  explicit_air: histogram['minecraft:air'], unique_coordinates: 1584, data_version: 3465,
  water_sources: waters.length, sealed_water_neighbor_faces_checked: sealedFaces,
  supported_wall_torches: torches, stable_dry_rail_cells: rails,
  three_wide_three_high_dry_route_cells: dryRouteCells, complete_floor_and_roof_cells: 352,
  no_turns_branches_or_stairs_in_route: true, both_ports_open: true,
  reference_hash_matches_batch_manifest: true, namespace: 'minecraft only', palette_histogram: histogram,
  native_collision_and_stability: 'verified separately by qa/forge-native.json',
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/fixture.json'), JSON.stringify({ sha256: digest, size: specification.size, waters, blocks: blocks.map(block => ({ pos: block.pos, ...stateAt(block.pos) })) }, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256: digest, water: waters.length, rails, torches, records: blocks.length }))
