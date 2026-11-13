import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.dirname(fileURLToPath(import.meta.url))
const source = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S16-frozen-prison-overgrown.png'
const sourceHash = '253f2d0d39dd3a642e97a1022ad95c9f14e3fc7a321e18769430ae8c16f39fc4'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes)
const structure = Structure.fromNbt(nbt.root)
const size = structure.getSize()
const blocks = structure.getBlocks()
const nameAt = position => structure.getBlock(position)?.state.getName().toString()
const tests = []
const check = (name, condition, detail) => {
  tests.push({ name, status: condition ? 'PASS' : 'FAIL', detail })
  assert.ok(condition, name)
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
check('reference_fingerprint', sha256(await fs.readFile(source)) === sourceHash, sourceHash)
check('gzip_single_palette_java_1_20_1', bytes[0] === 31 && bytes[1] === 139 && nbt.root.getNumber('DataVersion') === 3465 && nbt.root.has('palette') && !nbt.root.has('palettes'), 'DataVersion=3465')
check('one_chunk_straight_length', size.join(',') === '11,10,16', size)
check('full_explicit_volume_no_duplicates', blocks.length === 1760 && new Set(blocks.map(block => block.pos.join(','))).size === 1760, blocks.length)
check('coordinates_inside_bounds', blocks.every(block => structure.isInside(block.pos)), blocks.length)
const prohibited = new Set(['ice', 'frosted_ice', 'lava', 'water', 'powder_snow', 'snow', 'fire', 'soul_fire', 'magma_block', 'cactus', 'cobweb', 'sweet_berry_bush', 'wither_rose', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'structure_void'])
check('vanilla_nonhazardous_palette', blocks.every(block => block.state.getName().namespace === 'minecraft' && !prohibited.has(block.state.getName().path)), [...new Set(blocks.map(block => block.state.getName().toString()))])
check('no_entities_block_entities_inventory_or_loot', nbt.root.getList('entities').length === 0 && blocks.every(block => !block.nbt), 'No containers, loot, mobs or commands')
check('no_fluids_or_waterlogging', blocks.every(block => block.state.getProperties().waterlogged !== 'true'), 'Only permanent packed/blue ice')
const footPositions = []
for (let depth = 0; depth < 16; depth += 1) {
  for (let horizontal = 3; horizontal <= 7; horizontal += 1) {
    footPositions.push([horizontal, 1, depth])
    for (let vertical = 1; vertical <= 3; vertical += 1) assert.equal(nameAt([horizontal, vertical, depth]), 'minecraft:air', `Aisle obstruction ${horizontal},${vertical},${depth}`)
    assert.notEqual(nameAt([horizontal, 0, depth]), 'minecraft:air')
  }
}
check('five_wide_three_high_level_through_aisle', true, `${footPositions.length} foot positions; north/south open`)
check('dry_nonslip_three_wide_center_lane', footPositions.filter(position => position[0] >= 4 && position[0] <= 6).every(position => !nameAt([position[0], 0, position[2]]).includes('ice')), 'x=4..6, y=1, z=0..15')
const fullRoof = Array.from({ length: 176 }, (_, index) => [index % 11, 9, Math.floor(index / 11)])
check('complete_solid_roof', fullRoof.every(position => nameAt(position).includes('stone_bricks')), fullRoof.length)
check('closed_parallel_side_walls_no_branches', [0, 10].every(horizontal => Array.from({ length: 144 }, (_, index) => [horizontal, 1 + index % 9, Math.floor(index / 9)]).every(position => nameAt(position) !== 'minecraft:air')), 'Both side shells sealed')
const vines = blocks.filter(block => block.state.getName().path === 'vine')
check('vines_have_direct_solid_support', vines.every(block => {
  const properties = block.state.getProperties()
  const [horizontal, vertical, depth] = block.pos
  const support = properties.up === 'true' ? [horizontal, vertical + 1, depth] : properties.west === 'true' ? [horizontal - 1, vertical, depth] : [horizontal + 1, vertical, depth]
  return nameAt(support) !== 'minecraft:air'
}), vines.length)
const chains = blocks.filter(block => block.state.getName().path === 'chain')
check('chains_connected_to_solid_roof', chains.every(block => {
  const [horizontal, vertical, depth] = block.pos
  return vertical < 8 ? nameAt([horizontal, vertical + 1, depth]) === 'minecraft:chain' : nameAt([horizontal, 9, depth]) !== 'minecraft:air'
}), chains.length)
const materials = {}
for (const block of blocks) {
  const identity = block.state.getName().toString()
  materials[identity] = (materials[identity] ?? 0) + 1
}
const metadata = {
  id: 'corridor-S16', design_id: 'S16', name: '冰封牢狱·蔓生短廊', category: 'straight_short_corridor',
  size, entrance: [5, 1, 0], interior: [5, 1, 2], exit: [5, 1, 15], facing: 'north',
  exits: [{ position: [5, 1, 0], facing: 'north' }, { position: [5, 1, 15], facing: 'south' }],
  length_blocks: 16, length_chunks: 1, clear_path: { x: [3, 7], y: [1, 3], z: [0, 15], width: 5, height: 3 },
  dry_center_lane: { x: [4, 6], y: 1, z: [0, 15], width: 3 },
  sha256: sha256(bytes), data_version: 3465, format: 'gzip vanilla Java single palette structure NBT',
  reference: source, reference_sha256: sourceHash,
  builder: 'minecraft-builder-skill / Lodestone 0.6.0 / run-build.mjs',
  features: ['four ribbed stone arches', 'six ice-blocked barred cell panels', 'six roof-anchored frozen chains', 'twelve end rods', 'wall and roof-supported vines', 'permanent packed and blue ice'],
  concept_adaptations: ['North/south arches stay open instead of the concept closed far gate.', 'No meltable ice, snow layers, fluids or falling ice spikes.', 'Cells are sealed ornamental recesses, not additional branches.'],
  placement_policy: 'Coordinator must inspect the full volume including air and reject occupied/fluid/entity overlap before placement; never blindly clear a player world.',
  deployment: 'not integrated; not deployed; no player save opened or modified'
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'materials.json'), JSON.stringify(materials, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', structure_sha256: metadata.sha256, tests, test_count: tests.length, walkable_foot_positions: footPositions.length }, null, 2) + '\n')
console.log(JSON.stringify({ status: 'PASS', sha256: metadata.sha256, tests: tests.length, size, materials }, null, 2))
