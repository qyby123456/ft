import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification, reservedRoute } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hashBytes = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const digest = hashBytes(bytes)
const source = NbtFile.read(bytes).root
const structure = Structure.fromNbt(source)
const blocks = structure.getBlocks()
const keyOf = position => position.join(',')
const nameAt = (model, position) => model.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const batchRoot = path.resolve(path.dirname(specification.reference), '../..')
const referenceBytes = await fs.readFile(specification.reference)
assert.equal(hashBytes(referenceBytes), specification.reference_sha256)
const lineage = []
for (const file of ['README.md', 'docs/manifest.json', 'docs/verify.json']) {
  const data = await fs.readFile(path.join(batchRoot, file))
  lineage.push({ file: path.join(batchRoot, file), sha256: hashBytes(data) })
  if (file.endsWith('.json')) {
    const index = JSON.parse(data)
    const entry = Object.values(index).filter(Array.isArray).flat().find(entry => entry.file?.endsWith('N01-hub-four-way-junction.png'))
    assert.equal(entry?.sha256, specification.reference_sha256)
    assert.deepEqual(entry.size, [1536, 1024])
  }
}
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(source.getNumber('DataVersion'), 3465)
assert.equal(blocks.length, 7500)
assert.equal(new Set(blocks.map(block => keyOf(block.pos))).size, 7500)
assert.equal(source.getList('entities').length, 0)
const histogram = {}
const signs = []
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|water|lava|fire|cactus|magma_block|powder_snow|cobweb|tnt)$/
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert.ok(structure.isInside(block.pos), 'Out of bounds')
  assert.ok(name.startsWith('minecraft:') && !forbidden.test(name), `Forbidden ${name}`)
  histogram[name] = (histogram[name] ?? 0) + 1
  if (name === 'minecraft:spruce_wall_sign') {
    assert.equal(block.nbt.getString('id'), 'minecraft:sign')
    assert.equal(block.nbt.getBoolean('is_waxed'), true)
    const messages = block.nbt.getCompound('front_text').getList('messages')
    assert.equal(messages.length, 4)
    for (let line = 0; line < messages.length; line++) {
      const component = JSON.parse(messages.getString(line))
      assert.deepEqual(Object.keys(component), ['text'])
    }
    const delta = { north: [0, 0, 1], east: [-1, 0, 0], south: [0, 0, -1], west: [1, 0, 0] }[block.state.getProperty('facing')]
    const support = block.pos.map((coordinate, axis) => coordinate + delta[axis])
    assert.notEqual(nameAt(structure, support), 'minecraft:air')
    signs.push({ position: block.pos, facing: block.state.getProperty('facing') })
  } else {
    assert.ok(!block.nbt || block.nbt.size === 0, `Unexpected block entity ${name}`)
  }
}
assert.equal(signs.length, 12)
assert.equal(histogram['minecraft:lantern'], 13)
assert.equal(histogram['minecraft:chain'], 36)
assert.equal(histogram['minecraft:rail'], 4)

function spatial(model) {
  const walkable = new Set()
  let reserved = 0
  for (let blockX = 0; blockX < 25; blockX++) {
    for (let blockZ = 0; blockZ < 25; blockZ++) {
      for (const blockY of [0, 1, 11]) assert.notEqual(nameAt(model, [blockX, blockY, blockZ]), 'minecraft:air', 'Floor/roof hole')
      const clear = nameAt(model, [blockX, 2, blockZ]) === 'minecraft:air' && nameAt(model, [blockX, 3, blockZ]) === 'minecraft:air'
      if (clear) walkable.add(keyOf([blockX, 2, blockZ]))
      if (!reservedRoute(blockX, blockZ)) continue
      for (let blockY = 2; blockY <= 6; blockY++) assert.equal(nameAt(model, [blockX, blockY, blockZ]), 'minecraft:air', `Reserved route blocked ${blockX},${blockY},${blockZ}`)
      reserved++
    }
  }
  for (const port of specification.ports) {
    for (let blockX = port.throat.from[0]; blockX <= port.throat.to[0]; blockX++) {
      for (let blockY = 2; blockY <= 6; blockY++) {
        for (let blockZ = port.throat.from[2]; blockZ <= port.throat.to[2]; blockZ++) assert.equal(nameAt(model, [blockX, blockY, blockZ]), 'minecraft:air', `${port.id} throat blocked`)
      }
    }
  }
  const queue = [specification.center_safe]
  const reached = new Set([keyOf(queue[0])])
  for (let cursor = 0; cursor < queue.length; cursor++) {
    for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const next = [queue[cursor][0] + deltaX, 2, queue[cursor][2] + deltaZ]
      const key = keyOf(next)
      if (walkable.has(key) && !reached.has(key)) { reached.add(key); queue.push(next) }
    }
  }
  for (const port of specification.ports) assert.ok(reached.has(keyOf(port.center)), `${port.id} disconnected`)
  return { reserved_route_cells: reserved, reachable_player_cells: reached.size, all_four_ports_reachable: true, ordered_port_pairs: 12, route_width: 3, route_clearance: 5, center_safe: specification.center_safe }
}
const routes = spatial(structure)
for (const pillar of specification.pillars) {
  for (let blockX = pillar.shaft.from[0]; blockX <= pillar.shaft.to[0]; blockX++) {
    for (let blockZ = pillar.shaft.from[2]; blockZ <= pillar.shaft.to[2]; blockZ++) {
      for (let blockY = 1; blockY <= 11; blockY++) assert.notEqual(nameAt(structure, [blockX, blockY, blockZ]), 'minecraft:air', 'Pillar not continuous')
    }
  }
  for (let blockX = pillar.base.from[0]; blockX <= pillar.base.to[0]; blockX++) {
    for (let blockZ = pillar.base.from[2]; blockZ <= pillar.base.to[2]; blockZ++) assert.equal(nameAt(structure, [blockX, 2, blockZ]), 'minecraft:polished_andesite', 'Prop intersects pillar base')
  }
}
for (const [blockX, blockZ] of specification.hanging_lanterns) {
  for (let blockY = 8; blockY <= 10; blockY++) assert.equal(nameAt(structure, [blockX, blockY, blockZ]), 'minecraft:chain')
  assert.notEqual(nameAt(structure, [blockX, 11, blockZ]), 'minecraft:air')
}
const negativeProbes = []
for (const probe of [
  { name: 'missing_floor', position: [12, 1, 5], replacement: 'minecraft:air' },
  { name: 'blocked_port', position: [12, 2, 0], replacement: 'minecraft:stone' },
  { name: 'ring_headroom_loss', position: [9, 6, 12], replacement: 'minecraft:stone' },
  { name: 'unsafe_center', position: [12, 3, 9], replacement: 'minecraft:stone' },
]) {
  const damaged = structure.clone()
  damaged.addBlock(probe.position, probe.replacement)
  assert.throws(() => spatial(damaged), undefined, probe.name)
  negativeProbes.push(probe.name)
}
const report = { status: 'PASS', sha256: digest, reference_sha256: specification.reference_sha256, lineage, size: structure.getSize(), data_version: 3465, records: blocks.length, explicit_air: histogram['minecraft:air'], entities: 0, block_entities: signs.length, histogram, signs, routes, four_floor_to_roof_pillars: true, all_hanging_lanterns_roof_attached: true, negative_probes_rejected: negativeProbes, native_collision_check: 'See qa/forge-native.json', deployment: 'NOT_DEPLOYED' }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify({ ...specification, sha256: digest, record_count: blocks.length, explicit_air: histogram['minecraft:air'], block_entities: signs.length, entities: 0, coordinate_system: '+X east, +Z south; Y=2 feet; all direction labels are template-local', rotation_pivot: [0, 0, 0], adjacency_translation: { N: [0, 0, -25], E: [25, 0, 0], S: [0, 0, 25], W: [-25, 0, 0] }, future_placement_requires_full_volume_occupancy_check: true }, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256: digest, records: blocks.length, routes }, null, 2))
