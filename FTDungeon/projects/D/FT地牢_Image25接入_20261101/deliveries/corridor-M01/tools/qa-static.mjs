import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath, pathToFileURL } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(path.join(skill, 'node_modules/@mattzh72/lodestone/lib/index.js')))
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M01-nether-intact.png'
const referenceSha256 = '625157979074e1cedb8e14c9d4b8616d3a507004a759b509f6a76a20ead22be2'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const size = structure.getSize()
const records = structure.getBlocks()
const byPosition = new Map(records.map(block => [block.pos.join(','), block]))
const nameAt = position => byPosition.get(position.join(','))?.state.getName().toString() ?? 'minecraft:air'
const checks = {}
const check = (label, predicate) => { if (!predicate) throw new Error(label); checks[label] = true }
check('reference_sha256_matches', sha256(await fs.readFile(reference)) === referenceSha256)
check('dimensions_15x13x40', JSON.stringify(size) === '[15,13,40]')
check('medium_length_32_to_48', size[2] >= 32 && size[2] <= 48)
check('full_volume_unique_voxel_records', byPosition.size === 7800 && records.length === 7800)
check('gzip_nbt', bytes[0] === 31 && bytes[1] === 139)
const forbidden = new Set(['command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'spawner', 'tnt', 'respawn_anchor'])
const materials = {}
const fluids = []
const lamps = []
for (const block of records) {
  const blockName = block.state.getName().toString()
  check('all_positions_in_bounds', block.pos.every((value, axis) => value >= 0 && value < size[axis]))
  check('vanilla_namespace_only', blockName.startsWith('minecraft:'))
  check('no_forbidden_blocks', !forbidden.has(blockName.split(':')[1]))
  check('no_block_entities_or_loot', !block.nbt)
  materials[blockName] = (materials[blockName] ?? 0) + 1
  if (blockName === 'minecraft:lava') fluids.push(block.pos)
  if (blockName === 'minecraft:lantern') lamps.push(block.pos)
}
for (let south = 0; south < 40; south++) {
  for (let east = 5; east <= 9; east++) {
    check('level_five_wide_main_route', !['minecraft:air', 'minecraft:lava'].includes(nameAt([east, 1, south])))
    for (let up = 2; up <= 4; up++) check('three_high_clearance_all_40_slices', nameAt([east, up, south]) === 'minecraft:air')
  }
  for (let up = 2; up <= 11; up++) check('no_east_west_branch_openings', nameAt([0, up, south]) !== 'minecraft:air' && nameAt([14, up, south]) !== 'minecraft:air')
  for (let east = 0; east < 15; east++) {
    check('complete_roof', nameAt([east, 12, south]) !== 'minecraft:air')
    check('complete_underfloor', nameAt([east, 0, south]) !== 'minecraft:air')
  }
}
const fluidSet = new Set(fluids.map(position => position.join(',')))
for (const [east, up, south] of fluids) {
  check('lava_only_in_below_foot_side_channels', [4, 10].includes(east) && up === 1 && south >= 2 && south <= 37)
  for (const neighbor of [[east - 1, up, south], [east + 1, up, south], [east, up, south - 1], [east, up, south + 1], [east, up - 1, south]]) {
    check('every_lava_cell_has_bottom_and_contained_sides', fluidSet.has(neighbor.join(',')) || !['minecraft:air', 'minecraft:lava'].includes(nameAt(neighbor)))
  }
}
check('lava_sources_48', fluids.length === 48)
check('ten_hanging_lanterns', lamps.length === 10)
for (const [east, up, south] of lamps) {
  for (let height = up + 1; height <= 10; height++) check('all_lanterns_chain_to_solid_roof', nameAt([east, height, south]) === 'minecraft:chain')
  check('all_lanterns_chain_to_solid_roof', nameAt([east, 11, south]) === 'minecraft:polished_blackstone_bricks')
}
check('zero_entities', file.root.getList('entities').length === 0)
const metadata = {
  id: 'M01', name: '狱火拱廊·完整段', kind: 'medium', size,
  entrance: [7, 2, 0], interior: [7, 2, 3], exit: [7, 2, 39], facing: 'north',
  ports: [{ side: 'north', feet: [7, 2, 0], width: 5, clear_height: 7 }, { side: 'south', feet: [7, 2, 39], width: 5, clear_height: 7 }],
  corridor_axis: 'z', length_blocks: 40, length_chunks: 2.5, floor_y: 1, player_feet_y: 2,
  main_route: { min: [5, 2, 0], max: [9, 4, 39], width: 5, clearance_height: 3, level: true },
  theme: 'nether', condition: 'intact', lighting: 'paired hanging lanterns and contained lava',
  reference, reference_sha256: referenceSha256, sha256: sha256(bytes),
  data_version: 3465, minecraft: '1.20.1', forge_verified_version: '47.4.10',
  nbt_air_policy: 'explicit air, full 7800-cell volume; requires full-volume occupancy preflight before integration',
  image_interpretation: 'Repeated blackstone stepped arch ribs, red Nether brick panels, dark floor borders, red central runner, paired hanging lanterns, interrupted recessed lava trenches. Broken-arches index interpreted as stepped haunches; intact shell retained.',
  deployment: 'not_deployed', production_saves_modified: false,
  materials, decorative_stairs_only_above_y_7: true,
  build_recipe: 'tools/recipe.mjs', preview: 'preview.png', qa: 'qa.json'
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
const report = { id: 'M01', status: 'PASS', evidence_level: 'static NBT geometry; native report separate', sha256: metadata.sha256, reference_sha256: referenceSha256, checks, size, records: records.length, materials, lava_sources: fluids.length, lanterns: lamps.length, entities: 0 }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256: metadata.sha256, size, records: records.length, checks: Object.keys(checks).length }, null, 2))
