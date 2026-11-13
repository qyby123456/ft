import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S06-flooded-intact.png'
const referenceHash = '72daa317fc6e42fb149d4fc138f88c73beea0b01177fb7d4eeef58868c014200'
const digest = data => crypto.createHash('sha256').update(data).digest('hex')
const nbtBytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(nbtBytes)
const structure = Structure.fromNbt(nbt.root)
const blocks = structure.getBlocks()
const checks = []
const failures = []
const check = (name, condition, details) => {
  checks.push({ name, passed: Boolean(condition), details })
  if (!condition) failures.push(name)
}
const size = structure.getSize()
const nameAt = position => structure.getBlock(position)?.state.getName().toString()
const fullBlocks = new Set(['minecraft:stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:chiseled_stone_bricks'])
const isSolid = position => fullBlocks.has(nameAt(position))
check('reference_sha256', digest(await fs.readFile(reference)) === referenceHash, referenceHash)
check('gzip', nbtBytes[0] === 31 && nbtBytes[1] === 139, 'gzip compressed Java structure')
check('size', JSON.stringify(size) === '[13,10,16]', size)
check('explicit_volume_unique', blocks.length === 2080 && new Set(blocks.map(block => block.pos.join(','))).size === 2080, blocks.length)
check('data_version', nbt.root.getNumber('DataVersion') === 3465, nbt.root.getNumber('DataVersion'))
const palette = {}
for (const block of blocks) {
  const name = block.state.getName().toString()
  palette[name] = (palette[name] ?? 0) + 1
}
const allowed = new Set([...fullBlocks, 'minecraft:air', 'minecraft:water', 'minecraft:stone_brick_stairs', 'minecraft:chain', 'minecraft:soul_lantern', 'minecraft:vine'])
check('vanilla_allowlist', Object.keys(palette).every(name => allowed.has(name)), Object.keys(palette))
check('no_block_entities_containers_or_loot', blocks.every(block => !block.nbt), 'No containers, block entities, commands, loot or spawners')
check('no_entities', nbt.root.getList('entities').length === 0, 0)
let foundation = true
let roof = true
for (let posX = 0; posX < 13; posX++) {
  for (let posZ = 0; posZ < 16; posZ++) {
    foundation &&= isSolid([posX, 0, posZ])
    roof &&= isSolid([posX, 8, posZ]) && isSolid([posX, 9, posZ])
  }
}
check('complete_basin_floor', foundation, '208 full blocks at y=0')
check('complete_two_layer_roof', roof, '416 full blocks at y=8,9; preview cutaways do not edit final NBT')
const archStations = [0, 4, 8, 12, 15]
check('five_complete_arch_ribs', archStations.every(posZ => [3, 9].every(posX => [1, 2, 3, 4, 5, 6, 7].every(posY => isSolid([posX, posY, posZ]))) && [4, 8].every(posX => isSolid([posX, 7, posZ])) && isSolid([6, 7, posZ])), archStations)
const stairs = blocks.filter(block => block.state.is('minecraft:stone_brick_stairs'))
check('arch_voussoirs_have_masonry_above', stairs.length === 20 && stairs.every(block => isSolid([block.pos[0], block.pos[1] + 1, block.pos[2]])), stairs.length)
for (let posX = 5; posX <= 7; posX++) {
  for (let posZ = 0; posZ < 16; posZ++) {
    check(`dry_route_${posX}_${posZ}`, isSolid([posX, 1, posZ]) && [2, 3, 4].every(posY => nameAt([posX, posY, posZ]) === 'minecraft:air'), '3-wide, 3-high dry straight route')
  }
}
const waters = blocks.filter(block => block.state.is('minecraft:water'))
check('water_source_count', waters.length === 42, waters.length)
const sealed = waters.every(block => {
  const [posX, posY, posZ] = block.pos
  if (posY !== 1 || posZ < 2 || posZ > 13 || ![3, 4, 8, 9].includes(posX) || block.state.getProperty('level') !== '0') return false
  return [[0, -1, 0], [-1, 0, 0], [1, 0, 0], [0, 0, -1], [0, 0, 1]].every(offset => {
    const target = block.pos.map((value, axis) => value + offset[axis])
    return isSolid(target) || nameAt(target) === 'minecraft:water'
  })
})
check('water_no_downward_or_horizontal_escape', sealed, 'Every source has a solid floor; neighbors are full masonry or another source; two-block dry end dams')
const lanterns = blocks.filter(block => block.state.is('minecraft:soul_lantern'))
check('eight_supported_soul_lanterns', lanterns.length === 8 && lanterns.every(block => {
  const [posX, posY, posZ] = block.pos
  return block.state.getProperty('hanging') === 'true' && [posY + 1, posY + 2].every(height => nameAt([posX, height, posZ]) === 'minecraft:chain') && isSolid([posX, 8, posZ])
}), lanterns.length)
const vines = blocks.filter(block => block.state.is('minecraft:vine'))
check('wall_backed_vines', vines.every(block => {
  const [posX, posY, posZ] = block.pos
  const offset = posX === 3 ? -1 : 1
  return [3, 9].includes(posX) && block.state.getProperty(posX === 3 ? 'west' : 'east') === 'true' && isSolid([posX + offset, posY, posZ])
}), vines.length)
const metadata = {
  id: 'corridor-S06', design_id: 'S06', name: '浅淹苔石短拱廊', schema_version: 1,
  size, entrance: [6, 2, 0], interior: [6, 2, 2], exit: [6, 2, 15], facing: 'north',
  shaft_axis: 'north-south', length_blocks: 16, corridor_type: 'short_straight',
  sha256: digest(nbtBytes), reference, reference_sha256: referenceHash,
  data_version: 3465, minecraft_version: '1.20.1',
  generator: { skill: 'minecraft-builder-skill', recipe: path.join(root, 'recipe.mjs') },
  route: { feet_y: 2, min_x: 5, max_x: 7, min_z: 0, max_z: 15, clear_height: 3, floor_y: 1, floor_type: 'full_blocks', step_free: true },
  connectors: [
    { face: 'north', feet: [6, 2, 0], width: 3, clear_height: 3 },
    { face: 'south', feet: [6, 2, 15], width: 3, clear_height: 3 },
  ],
  fluids: { type: 'minecraft:water', source_blocks: waters.length, y: 1, x_columns: [3, 4, 8, 9], z_range: [2, 13], contained: sealed },
  visual_adjustments: [
    'Reference shallow flooding is retained in sealed side basins; a level 3-block-wide dry center causeway protects traversal.',
    'The concept far-end wall becomes an open south arch so the corridor actually connects north to south.',
    'Five complete stone arch ribs retain the intact condition; moss and supported vines supply the damp atmosphere.',
  ],
  scope: { independent_asset: true, deployed: false, player_saves_modified: false, shared_core_modified: false },
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), `${JSON.stringify(metadata, null, 2)}\n`)
const report = { id: metadata.id, status: failures.length ? 'FAIL' : 'PASS', nbt_sha256: metadata.sha256, source_image_sha256: referenceHash, size, palette, checks, failures, limitations: ['Static topology and declared-state checks; actual Minecraft/Forge registry, collision, survival and fluid ticks are reported separately.'] }
await fs.writeFile(path.join(root, 'qa', 'static.json'), `${JSON.stringify(report, null, 2)}\n`)
process.stdout.write(`${JSON.stringify({ status: report.status, sha256: metadata.sha256, size, blocks: blocks.length, waters: waters.length, lanterns: lanterns.length, checks: checks.length, failures })}\n`)
if (failures.length) process.exitCode = 1
