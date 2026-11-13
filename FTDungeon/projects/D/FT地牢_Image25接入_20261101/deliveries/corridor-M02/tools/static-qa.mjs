import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification } from './build.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const hash = data => crypto.createHash('sha256').update(data).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const data = NbtFile.read(bytes).root.toSimplifiedJson()
const tests = []
const check = (name, passed, details = {}) => { tests.push({ name, passed, ...details }); if (!passed) throw new Error(`Static QA failed: ${name}`) }
const blocks = new Map(data.blocks.map(block => [block.pos.join(','), data.palette[block.state]]))
const id = (posX, posY, posZ) => blocks.get(`${posX},${posY},${posZ}`)?.Name
const air = (posX, posY, posZ) => id(posX, posY, posZ) === 'minecraft:air'
const solid = (posX, posY, posZ) => /minecraft:(stone_bricks|mossy_stone_bricks|cracked_stone_bricks|chiseled_stone_bricks|polished_andesite|dark_oak_planks|dark_oak_log|bookshelf|shroomlight|orange_stained_glass)$/.test(id(posX, posY, posZ))
const counts = {}
for (const block of data.blocks) {
  const name = data.palette[block.state].Name
  counts[name] = (counts[name] ?? 0) + 1
}
check('reference_sha256', hash(await fs.readFile(specification.reference)) === specification.reference_sha256)
check('java_1_20_1_single_palette', data.DataVersion === 3465 && !data.palettes && Array.isArray(data.palette))
check('medium_2_to_3_chunks', data.size.join(',') === '17,13,41' && data.size[2] >= 32 && data.size[2] <= 48, { length_blocks: 41, chunks: 41 / 16 })
check('explicit_full_volume_no_duplicate_coordinates', data.blocks.length === 17 * 13 * 41 && blocks.size === data.blocks.length, { records: blocks.size })
check('all_coordinates_and_palette_indexes_valid', data.blocks.every(block => block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < data.size[axis]) && block.state >= 0 && block.state < data.palette.length))
check('entities_and_block_entities_empty', data.entities.length === 0 && data.blocks.every(block => !block.nbt))
const forbidden = /(?:command_block|structure_block|structure_void|jigsaw|spawner|lava|fire|soul_fire|magma_block|campfire|powder_snow|cobweb|wither_rose|sweet_berry_bush|cactus)$/
check('no_forbidden_or_dangerous_blocks', data.palette.every(block => block.Name.startsWith('minecraft:') && !forbidden.test(block.Name)))
let clearRecords = 0
for (let posX = 7; posX <= 9; posX++) {
  for (let posZ = 0; posZ <= 40; posZ++) {
    if (!solid(posX, 1, posZ)) throw new Error(`Unsafe aisle floor ${posX},1,${posZ}`)
    for (let posY = 2; posY <= 4; posY++) { if (!air(posX, posY, posZ)) throw new Error(`Obstructed aisle ${posX},${posY},${posZ}`); clearRecords++ }
  }
}
check('flat_continuous_dry_3x3_main_path', clearRecords === 369, { floor_positions: 123, clear_air_positions: clearRecords, turns: 0, branches: 0, stairs: 0 })
check('complete_floor_and_roof', Array.from({ length: 17 }, (_, posX) => Array.from({ length: 41 }, (_, posZ) => solid(posX, 0, posZ) && solid(posX, 11, posZ) && solid(posX, 12, posZ)).every(Boolean)).every(Boolean))
check('both_ends_open_and_sides_sealed', [0, 40].every(posZ => [7, 8, 9].every(posX => [2, 3, 4].every(posY => air(posX, posY, posZ)))) && Array.from({ length: 41 }, (_, posZ) => [0, 16].every(posX => [2, 3, 4].every(posY => solid(posX, posY, posZ)))).every(Boolean))
const water = data.blocks.filter(block => data.palette[block.state].Name === 'minecraft:water')
check('flood_channels_fully_contained', water.length === 222 && water.every(block => {
  const [posX, posY, posZ] = block.pos
  return posY === 1 && solid(posX, 0, posZ) && [[1, 0], [-1, 0], [0, 1], [0, -1]].every(([deltaX, deltaZ]) => id(posX + deltaX, 1, posZ + deltaZ) === 'minecraft:water' || solid(posX + deltaX, 1, posZ + deltaZ))
}), { sources: water.length, max_depth_blocks: 1, main_path_wet_blocks: 0 })
check('candles_have_full_support', data.blocks.filter(block => data.palette[block.state].Name === 'minecraft:candle').every(block => solid(block.pos[0], block.pos[1] - 1, block.pos[2])))
check('five_cages_have_chains_and_roof_anchors', specification.cages.every(cage => id(cage.center[0], 8, cage.center[2]) === 'minecraft:iron_block' && id(cage.center[0], 9, cage.center[2]) === 'minecraft:chain' && solid(cage.center[0], 10, cage.center[2]) && solid(cage.center[0], 11, cage.center[2])))
check('archive_flood_lavaglow_cage_theme_present', counts['minecraft:bookshelf'] >= 80 && counts['minecraft:iron_bars'] === 80 && counts['minecraft:shroomlight'] === 100 && counts['minecraft:water'] === 222 && counts['minecraft:stripped_birch_log'] === 20)
const metadata = { ...specification, sha256: hash(bytes), data_version: 3465, block_count: blocks.size, palette_count: data.palette.length, builder: 'Minecraft Builder skill / Lodestone 0.6.0', deployment: 'not_deployed', entities: 0, containers: 0 }
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify({ status: 'PASS', structure_sha256: metadata.sha256, reference_sha256: specification.reference_sha256, tests, block_counts: counts }, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/blocks.json'), JSON.stringify({ size: data.size, palette: data.palette, blocks: data.blocks }) + '\n')
console.log(JSON.stringify({ status: 'PASS', tests: tests.length, sha256: metadata.sha256, blocks: blocks.size, palette: data.palette.length, counts }, null, 2))
