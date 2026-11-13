import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { createRequire } from 'node:module'
import { createHash } from 'node:crypto'

const require = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M10-abandoned-mine-burnt.png'
const referenceHash = 'f0bfc91e258dab253c9d0272f516d54821e82b60357cbc6dcdb1510a722f3ab0'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const data = NbtFile.read(bytes).root
const structure = Structure.fromNbt(data)
const blocks = structure.getBlocks()
const size = structure.getSize()
const checks = {}
const check = (name, condition) => {
  checks[name] = condition ? 'PASS' : 'FAIL'
  if (!condition) throw new Error(name)
}
const blockAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const isClear = position => ['minecraft:air', 'minecraft:rail'].includes(blockAt(position))
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
check('reference_sha256', digest(await fs.readFile(reference)) === referenceHash)
check('gzip', bytes[0] === 31 && bytes[1] === 139)
check('java_1_20_1', data.getNumber('DataVersion') === 3465)
check('dimensions_15x11x40_medium_2_5_chunks', JSON.stringify(size) === '[15,11,40]')
check('single_palette', !data.has('palettes'))
check('no_entities', data.getList('entities').length === 0)
check('full_explicit_volume', blocks.length === size.reduce((product, value) => product * value, 1))
check('unique_positions', new Set(blocks.map(block => block.pos.join(','))).size === blocks.length)
const hazards = new Set(['lava', 'water', 'fire', 'soul_fire', 'magma_block', 'campfire', 'soul_campfire', 'cactus', 'powder_snow', 'cobweb', 'tnt', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw'])
check('vanilla_namespace_no_hazards_or_forbidden_blocks', blocks.every(block => {
  const identity = block.state.getName().toString()
  return identity.startsWith('minecraft:') && !hazards.has(identity.split(':')[1])
}))
check('no_block_entities_inventories_or_loot', blocks.every(block => block.nbt === undefined))
check('all_records_inside_bounds', blocks.every(block => block.pos.every((value, axis) => value >= 0 && value < size[axis])))
check('deepslate_axis_explicit_for_lodestone', blocks.every(block => block.state.getName().toString() !== 'minecraft:deepslate' || block.state.getProperties().axis === 'y'))
check('solid_foundation_floor_roof', [0, 1, 9, 10].every(posY => {
  for (let posX = 0; posX < 15; posX++) for (let posZ = 0; posZ < 40; posZ++) if (blockAt([posX, posY, posZ]) === 'minecraft:air') return false
  return true
}))
check('continuous_dry_straight_three_wide_three_high_path', Array.from({ length: 40 }, (_, posZ) => {
  for (let posX = 6; posX <= 8; posX++) for (let posY = 2; posY <= 4; posY++) if (!isClear([posX, posY, posZ])) return false
  return true
}).every(Boolean))
check('no_side_branches', [0, 1, 13, 14].every(posX => {
  for (let posY = 2; posY < 9; posY++) for (let posZ = 0; posZ < 40; posZ++) if (blockAt([posX, posY, posZ]) === 'minecraft:air') return false
  return true
}))
check('no_stairs_in_main_lane', blocks.every(block => !(block.pos[0] >= 6 && block.pos[0] <= 8 && block.pos[1] === 2 && block.state.getName().toString().endsWith('_stairs'))))
const materials = {}
for (const block of blocks) {
  const identity = block.state.getName().toString()
  materials[identity] = (materials[identity] ?? 0) + 1
  if (identity === 'minecraft:rail' || identity === 'minecraft:gravel') check('support_' + block.pos.join('_'), !['minecraft:air', 'minecraft:rail'].includes(blockAt([block.pos[0], block.pos[1] - 1, block.pos[2]])))
}
const metadata = {
  id: 'corridor-M10', source_id: 'M10', name: '焚毁废弃矿道', kind: 'medium', size,
  entrance: [7, 2, 0], interior: [7, 2, 3], exit: [7, 2, 39], facing: 'north',
  sha256: digest(bytes), reference, reference_sha256: referenceHash,
  data_version: 3465, length_chunks: 2.5, axis: 'z', walkable_width: 3, walkable_height: 3,
  connections: [{ side: 'north', feet: [7, 2, 0], aperture: [3, 3] }, { side: 'south', feet: [7, 2, 39], aperture: [3, 3] }],
  features: { timber_frames: 7, hanging_cages: 3, rails: materials['minecraft:rail'], lanterns: materials['minecraft:lantern'], sealed_cosmetic_ember_pockets: 4 },
  safety: { real_fire: false, real_lava: false, fluids: false, entities: false, containers: false, loot: false },
  reference_adaptations: ['以黑石、煤矿、深板岩表现焦痕，保留承重木框，不以真火焚烧木构件。', '余烬为菌光体加橙色玻璃和铁盖的装饰，无火伤与流体。', '单格原版铁轨代表概念图的宽铁轨；主路保持三格宽三格高。'],
  integration: { deployed: false, production_save_modified: false, slot_assigned: false }
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', sha256: digest(bytes), reference_sha256: referenceHash, checks, materials, records: blocks.length, non_air: blocks.length - materials['minecraft:air'], palette_count: data.getList('palette').length }, null, 2) + '\n')
process.stdout.write(JSON.stringify({ status: 'PASS', size, sha256: digest(bytes), features: metadata.features, checks: Object.keys(checks).length }) + '\n')
