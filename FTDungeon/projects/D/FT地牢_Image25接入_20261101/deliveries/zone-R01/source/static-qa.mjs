import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification, groundRoute, stairRoute, upperRoute } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const hash = digest(bytes)
const nbt = NbtFile.read(bytes).root
const structure = Structure.fromNbt(nbt)
const blocks = structure.getBlocks()
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const keyOf = position => position.join(',')
const fullBlock = position => /minecraft:(?:stone_bricks|mossy_stone_bricks|cracked_stone_bricks|andesite|coarse_dirt|spruce_planks|stripped_spruce_log|moss_block)$/.test(nameAt(position))
assert.equal(digest(await fs.readFile(specification.reference)), specification.reference_sha256)
const batchRoot = path.resolve(path.dirname(specification.reference), '../..')
const batchEvidence = []
for (const file of ['README.md', 'docs/manifest.json', 'docs/verify.json']) {
  const evidence = await fs.readFile(path.join(batchRoot, file))
  batchEvidence.push({ file, sha256: digest(evidence) })
  if (file.endsWith('.json')) {
    const document = JSON.parse(evidence)
    const record = (document.records ?? document.files).find(record => record.file.endsWith('R01-ruins-collapsed-watchtower-base.png'))
    assert.equal(record.sha256, specification.reference_sha256)
    assert.deepEqual(record.size, [1536, 1024])
    assert.equal(record.bytes, 2520714)
  }
}
assert.deepEqual(structure.getSize(), specification.size)
assert.equal(nbt.getNumber('DataVersion'), 3465)
assert.equal(nbt.getList('entities').length, 0)
assert.equal(nbt.has('palettes'), false)
const volume = specification.size.reduce((product, value) => product * value, 1)
assert.equal(blocks.length, volume)
assert.equal(new Set(blocks.map(block => keyOf(block.pos))).size, volume)
const histogram = {}
const blockEntities = []
const supports = []
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|lava|water|fire|cactus|magma_block|powder_snow|cobweb|sand|gravel|anvil|dragon_egg|concrete_powder)$/
for (const block of blocks) {
  const name = block.state.getName().toString()
  assert.ok(structure.isInside(block.pos))
  assert.ok(name.startsWith('minecraft:') && !forbidden.test(name), `Forbidden ${name}`)
  histogram[name] = (histogram[name] ?? 0) + 1
  const [blockX, blockY, blockZ] = block.pos
  if (block.nbt?.size) {
    assert.ok(['minecraft:barrel', 'minecraft:red_wall_banner'].includes(name))
    assert.equal(block.nbt.has('LootTable'), false)
    assert.equal(block.nbt.has('Items') ? block.nbt.getList('Items').length : 0, 0)
    blockEntities.push({ pos: block.pos, block: name, nbt: block.nbt.toJson() })
  }
  if (name === 'minecraft:lantern') {
    const hanging = block.state.getProperty('hanging') === 'true'
    const anchor = [blockX, hanging ? blockY + 1 : blockY - 1, blockZ]
    assert.ok(hanging ? nameAt(anchor) === 'minecraft:chain' : fullBlock(anchor), `Unsupported lamp ${block.pos}`)
    supports.push({ kind: hanging ? 'hanging_lantern' : 'standing_lantern', pos: block.pos, anchor })
  }
  if (name === 'minecraft:chain') assert.ok(['minecraft:chain', 'minecraft:stripped_spruce_log'].includes(nameAt([blockX, blockY + 1, blockZ])))
  if (name === 'minecraft:red_wall_banner') assert.ok(fullBlock([blockX, blockY, blockZ - 1]))
  if (/minecraft:(?:red_carpet|oak_leaves|stone_brick_slab|spruce_slab)$/.test(name)) assert.ok(fullBlock([blockX, blockY - 1, blockZ]), `Unsupported surface decoration ${block.pos}`)
  if (name === 'minecraft:stone_brick_stairs') assert.ok(fullBlock([blockX, blockY - 1, blockZ]))
}
assert.equal(histogram['minecraft:barrel'], 4)
assert.equal(histogram['minecraft:red_wall_banner'], 2)
assert.equal(histogram['minecraft:lantern'], 6)
assert.equal(histogram['minecraft:stone_brick_stairs'], 27)
for (let blockX = 0; blockX < 25; blockX++) {
  for (let blockZ = 0; blockZ < 25; blockZ++) assert.ok(fullBlock([blockX, 0, blockZ]) && fullBlock([blockX, 1, blockZ]))
}
const route = groundRoute()
for (const [centerX, feetY, centerZ] of route) {
  const blockX = Math.floor(centerX)
  const blockZ = Math.floor(centerZ)
  assert.ok(fullBlock([blockX, feetY - 1, blockZ]))
  for (let blockY = feetY; blockY < feetY + 3; blockY++) assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air', `Route obstruction ${blockX},${blockY},${blockZ}`)
}
for (const [centerX, feetY, centerZ] of upperRoute()) {
  const blockX = Math.floor(centerX)
  const blockZ = Math.floor(centerZ)
  assert.ok(fullBlock([blockX, feetY - 1, blockZ]))
  for (let blockY = feetY; blockY < feetY + 3; blockY++) assert.equal(nameAt([blockX, blockY, blockZ]), 'minecraft:air')
}
for (let blockZ = 5; blockZ <= 8; blockZ++) {
  for (let blockX = 9; blockX <= 14; blockX++) assert.equal(nameAt([blockX, 10, blockZ]), 'minecraft:air')
  for (const blockX of [8, 15]) assert.equal(nameAt([blockX, 11, blockZ]), 'minecraft:spruce_fence')
}
for (let blockX = 8; blockX <= 15; blockX++) {
  for (const blockZ of [4, 9]) assert.equal(nameAt([blockX, 11, blockZ]), 'minecraft:spruce_fence')
}
for (const columnX of [5, 17]) {
  for (let blockY = 0; blockY <= 8; blockY++) assert.ok(fullBlock([columnX, blockY, 10]), 'Floating primary support')
}
const solid = new Set(blocks.filter(block => !['minecraft:air', 'minecraft:red_wall_banner'].includes(block.state.getName().toString())).map(block => keyOf(block.pos)))
const connected = new Set()
const queue = []
for (const block of blocks) if (block.pos[1] === 0) { connected.add(keyOf(block.pos)); queue.push(block.pos) }
for (let cursor = 0; cursor < queue.length; cursor++) {
  const position = queue[cursor]
  for (const offset of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const neighbor = position.map((coordinate, axis) => coordinate + offset[axis])
    const key = keyOf(neighbor)
    if (solid.has(key) && !connected.has(key)) { connected.add(key); queue.push(neighbor) }
  }
}
assert.equal(connected.size, solid.size, 'Detached block assembly')
const deviations = [
  '概念图无精确尺度；落地为25×17×25，北侧中央增加3宽4高开放接口及5×5安全落点。',
  '保留真实露天残塔和24格残层缺口；缺口、残层前缘、楼梯两侧加护栏。',
  '右侧断损石阶改为三格宽、实心砌体承托的九级完整石阶，连接上下3×3落点。',
  '两根独立落地双格石柱承托木梁；碎石逐层承托。细碎斜梁、残布和垂藤分别简化为落梁、旗帜/地毯、苔块/持久树叶。',
  'NBT保留四周完整残墙；内视副本剖切。独立废墟采用真实露天残顶，与旧九间密闭房间完整屋顶契约有差异，后续接入需按露天废墟审查。',
]
const metadata = {
  ...specification, facing: 'north', sha256: hash, minecraft: '1.20.1', data_version: 3465,
  generator: 'Minecraft Builder Skill / Lodestone 0.6.0', category: 'ruins', reference_size: [1536, 1024],
  independent_id: true, existing_R1_reused: false, entrance_clearance: [3, 4],
  ports: [{ side: 'north', position: specification.entrance, bounds: [[11, 2, 0], [13, 5, 1]], clearance: [3, 4] }],
  floor_y: 1, ground_route: { spine: [[11, 2, 0], [13, 2, 22]], east_spur: [[11, 2, 20], [21, 2, 22]], dry: true, width: 3 },
  safe_landing: { center: specification.landing, bounds: [[10, 2, 10], [14, 2, 14]], clearance_height: 3 },
  stair: { width: 3, steps: 9, support: 'solid stone wedge to floor', lower_landing: [[19, 2, 20], [21, 2, 22]], upper_landing: [[19, 11, 8], [21, 11, 10]], guardrails: true },
  collapse: { open_roof: true, mezzanine_hole: [[9, 10, 5], [14, 10, 8]], guarded: true, unsupported_assemblies: 0 },
  roof: { complete: false, intentional_ruins: true, not_a_missing_delivery_layer: true },
  records: blocks.length, explicit_air: histogram['minecraft:air'], block_entities: blockEntities.length, entities: 0,
  empty_barrels: 4, banner_patterns: 'two red wall banners with white rhombus', lighting: { lanterns: 6, suspended: 4, standing: 2 },
  deployment: 'independent candidate only; no production files, saves, integration catalog or connection routes modified',
  placement_policy: 'Preflight entire 25×17×25 volume including explicit air; reject occupancy/entities/fluids. No unconditional world clearing.',
  deviations,
}
const report = {
  status: 'PASS', id: specification.id, sha256: hash, reference_sha256: specification.reference_sha256,
  generator: metadata.generator, size: specification.size, records: blocks.length, explicit_air: histogram['minecraft:air'],
  full_unique_explicit_volume: true, foundation_cells: 1250, ground_route_cells: route.length, upper_route_cells: upperRoute().length,
  stair_collision_samples: stairRoute().length, connected_solid_blocks: solid.size, supported_lanterns: supports,
  empty_barrels: 4, wall_banners: 2, stone_stairs: 27, guarded_collapse_cells: 24,
  native_collision_and_support: 'see qa/forge-native.json; static checks do not substitute native checks',
  batch_evidence: batchEvidence, palette_histogram: histogram, block_entities: blockEntities, deviations,
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.mkdir(path.join(root, 'reference'), { recursive: true })
await fs.copyFile(specification.reference, path.join(root, 'reference/R01-ruins-collapsed-watchtower-base.png'))
await fs.writeFile(path.join(root, 'reference/batch-evidence.json'), JSON.stringify({ reference: specification.reference, sha256: specification.reference_sha256, batch_evidence: batchEvidence }, null, 2) + '\n')
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/fixture.json'), JSON.stringify({ sha256: hash, size: specification.size, ground_route: route, upper_route: upperRoute(), stair_route: stairRoute() }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: hash, size: specification.size, records: blocks.length, ground_route_cells: route.length, solid_components: 1 }))
