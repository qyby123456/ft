import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(`${skill}/node_modules/@mattzh72/lodestone/lib/index.js`))
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M08-forge-lava-burnt.png'
const expectedReference = 'ea87fc89e452e478a91bca5ea505acea8b97d2da8bd468a07108db61f7e46050'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = digest(await fs.readFile(reference))
if (referenceHash !== expectedReference) throw new Error('Reference checksum changed')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const blocks = structure.getBlocks()
const size = structure.getSize()
const at = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const assert = (condition, message) => { if (!condition) throw new Error(message) }
const counts = {}
for (const block of blocks) {
  const identity = block.state.getName().toString()
  counts[identity] = (counts[identity] ?? 0) + 1
}
assert(blocks.length === size.reduce((volume, edge) => volume * edge, 1), 'Expected explicit full-volume records')
let safeLaneCells = 0
for (let depth = 0; depth < size[2]; depth += 1) {
  for (let column = 6; column <= 10; column += 1) {
    for (const height of [3, 4, 5]) assert(at([column, height, depth]) === 'minecraft:air', 'Straight route obstructed')
    assert(!['minecraft:lava', 'minecraft:air', 'minecraft:magma_block'].includes(at([column, 2, depth])), 'Unsafe floor')
    safeLaneCells += 1
  }
}
for (let depth = 0; depth < 41; depth += 1) {
  for (let column = 0; column < 17; column += 1) {
    assert(at([column, 12, depth]) !== 'minecraft:air', 'Roof hole')
    assert(at([column, 0, depth]) !== 'minecraft:air', 'Foundation hole')
  }
  for (let height = 3; height <= 10; height += 1) {
    for (const column of [0, 16]) assert(at([column, height, depth]) !== 'minecraft:air', 'Side branch/opening')
  }
}
const room = {
  id: 'corridor-M08', source_id: 'M08', name: '焚烬熔炉直廊', category: 'corridor', length_class: 'medium',
  size, entrance: [8, 3, 0], interior: [8, 3, 3], exit: [8, 3, 40], facing: 'north',
  axis: 'z', length_blocks: 41, length_chunks: 41 / 16, straight: true, branches: 0, stairs: 0,
  clear_path: { x: [6, 10], feet_y: 3, z: [0, 40], width: 5, minimum_clear_height: 3, level_floor: true },
  ports: [
    { name: 'north', feet: [8, 3, 0], facing: 'north', opening_width: 5, minimum_clear_height: 5 },
    { name: 'south', feet: [8, 3, 40], facing: 'south', opening_width: 5, minimum_clear_height: 5 },
  ],
  data_version: 3465, minecraft: '1.20.1', sha256: digest(bytes), reference, reference_sha256: referenceHash,
  reference_features: ['parallel blackstone forge walls', 'eight glazed lava forge windows', 'eight anvils', 'eight empty hanging iron cages', 'covered orange lava floor seams', 'soot and cracked masonry', 'flat straight path with two distant arches'],
  safety: { floor_lava_glazed: true, wall_lava_glazed: true, open_fire: false, flammable_structure: false, loot: false, entities: false, spawners: false, command_blocks: false },
  integration: { deployed: false, player_save_modified: false, modifies_existing_core: false, explicit_air_requires_volume_occupancy_check: true },
  evidence: { preview: 'preview.png', four_direction_views: 'preview/index.html', static_qa: 'qa/static.json', forge_native: 'qa/native.json', aggregate: 'qa.json' },
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), `${JSON.stringify(room, null, 2)}\n`)
const report = {
  status: 'PASS', method: 'Lodestone NBT roundtrip and explicit coordinate assertions', sha256: room.sha256,
  reference_sha256: referenceHash, size, records: blocks.length, explicit_air: counts['minecraft:air'],
  non_air: blocks.length - counts['minecraft:air'], material_counts: counts, straight_safe_lane_cells: safeLaneCells,
  roof_cells: 17 * 41, foundation_cells: 17 * 41, side_branches: 0,
  native_registry_validation: 'See qa/native.json; not claimed by this static check',
}
await fs.writeFile(path.join(root, 'qa/static.json'), `${JSON.stringify(report, null, 2)}\n`)
console.log(JSON.stringify({ sha256: room.sha256, records: blocks.length, size, safeLaneCells, materials: Object.keys(counts).length }))
