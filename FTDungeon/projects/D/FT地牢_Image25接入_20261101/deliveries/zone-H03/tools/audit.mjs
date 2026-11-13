import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { design } from '../recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const requireCheck = (condition, message) => { if (!condition) throw new Error(message) }
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const file = NbtFile.read(bytes)
const structure = Structure.fromNbt(file.root)
const blocks = structure.getBlocks()
const at = (blockX, blockY, blockZ) => structure.getBlock([blockX, blockY, blockZ])
const nameAt = (blockX, blockY, blockZ) => at(blockX, blockY, blockZ)?.state.getName().toString() ?? 'minecraft:air'
const solidAt = (blockX, blockY, blockZ) => !/:(air|candle|chain|purple_wall_banner|stone_brick_stairs)$/.test(nameAt(blockX, blockY, blockZ))
const counts = {}
const coordinates = new Set()
requireCheck(bytes[0] === 31 && bytes[1] === 139 && file.root.getNumber('DataVersion') === 3465 && !file.root.has('palettes'), 'Wrong compression/version/palette format')
requireCheck(JSON.stringify(structure.getSize()) === JSON.stringify(design.size), 'Unexpected dimensions')
requireCheck(blocks.length === 16240 && file.root.getList('entities').length === 0, 'Wrong explicit volume or entities')
requireCheck(digest(await fs.readFile(design.reference)) === design.reference_sha256, 'Reference SHA-256 mismatch')
const batchRoot = path.resolve(path.dirname(design.reference), '../..')
const batchEvidence = {}
for (const filename of ['manifest.json', 'verify.json']) {
  const document = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs', filename), 'utf8'))
  const record = (document.files ?? document.records).find(entry => entry.file.endsWith('H03-halls-ceremonial-hall-candles.png'))
  requireCheck(record?.ok && record.sha256 === design.reference_sha256 && JSON.stringify(record.size) === '[1536,1024]', `Batch ${filename} mismatch`)
  batchEvidence[filename] = record
}
let bannerPayloads = 0
let candleCount = 0
for (const block of blocks) {
  const identifier = block.state.getName().toString()
  const [blockX, blockY, blockZ] = block.pos
  requireCheck(!coordinates.has(block.pos.join(',')) && block.pos.every((value, axis) => Number.isInteger(value) && value >= 0 && value < design.size[axis]), 'Duplicate or out-of-bounds coordinate')
  coordinates.add(block.pos.join(','))
  requireCheck(identifier.startsWith('minecraft:') && !/command_block|structure_block|jigsaw|spawner|:water$|:lava$|:fire$|soul_fire|campfire|magma_block|cobweb|powder_snow|sand$|gravel$/.test(identifier), `Forbidden block ${identifier}`)
  counts[identifier] = (counts[identifier] ?? 0) + 1
  if (identifier === 'minecraft:candle') {
    requireCheck(solidAt(blockX, blockY - 1, blockZ) && block.state.getProperty('lit') === 'true', `Unsupported or unlit candle ${block.pos}`)
    candleCount += Number(block.state.getProperty('candles'))
  }
  if (identifier === 'minecraft:chain') {
    let anchorY = blockY + 1
    while (nameAt(blockX, anchorY, blockZ) === 'minecraft:chain') anchorY += 1
    requireCheck(solidAt(blockX, anchorY, blockZ), `Unanchored chain ${block.pos}`)
  }
  if (identifier === 'minecraft:purple_wall_banner') {
    const offset = { north: [0, 1], south: [0, -1], west: [1, 0], east: [-1, 0] }[block.state.getProperty('facing')]
    requireCheck(solidAt(blockX + offset[0], blockY, blockZ + offset[1]), `Unsupported banner ${block.pos}`)
    requireCheck(block.nbt?.getString('id') === 'minecraft:banner' && block.nbt.getList('Patterns').length === 2, 'Banner pattern missing')
    bannerPayloads += 1
  } else requireCheck(!block.nbt, `Unexpected block-entity data ${block.pos}`)
}
for (let blockX = 0; blockX < 29; blockX += 1) for (let blockZ = 0; blockZ < 35; blockZ += 1) {
  requireCheck(solidAt(blockX, 0, blockZ) && solidAt(blockX, 1, blockZ), 'Foundation gap')
  for (const blockY of [14, 15]) requireCheck(solidAt(blockX, blockY, blockZ), 'Roof gap')
  for (const blockY of [2, 3, 4]) {
    if (blockX === 0 || blockX === 28 || blockZ === 34 || blockZ === 0 && (blockX < 12 || blockX > 16)) requireCheck(solidAt(blockX, blockY, blockZ), 'Unintended shell opening')
  }
}
for (let blockX = 12; blockX <= 16; blockX += 1) for (let blockZ = 0; blockZ <= 26; blockZ += 1) {
  requireCheck(solidAt(blockX, 1, blockZ), 'Main path floor missing')
  for (let blockY = 2; blockY <= 4; blockY += 1) requireCheck(nameAt(blockX, blockY, blockZ) === 'minecraft:air', 'Main route blocked')
}
for (const [pillarX, pillarZ] of design.pillars) for (let blockY = 0; blockY < 16; blockY += 1) requireCheck(solidAt(pillarX, blockY, pillarZ), 'Pillar-to-roof discontinuity')
for (const blockZ of [10, 24]) for (let blockX = 7; blockX <= 21; blockX += 1) requireCheck(solidAt(blockX, 13, blockZ), 'Broken crossbeam')
for (const blockX of [7, 21]) for (let blockZ = 10; blockZ <= 24; blockZ += 1) requireCheck(solidAt(blockX, 13, blockZ), 'Broken longitudinal beam')
for (let blockX = 11; blockX <= 17; blockX += 1) for (const [blockY, blockZ] of [[2, 27], [3, 28]]) requireCheck(nameAt(blockX, blockY, blockZ) === 'minecraft:stone_brick_stairs' && solidAt(blockX, blockY - 1, blockZ), 'Dais stair support missing')
const walkable = (blockX, blockZ) => solidAt(blockX, 1, blockZ) && nameAt(blockX, 2, blockZ) === 'minecraft:air' && nameAt(blockX, 3, blockZ) === 'minecraft:air'
const pending = [[14, 0]]
const visited = new Set(['14,0'])
while (pending.length) {
  const [blockX, blockZ] = pending.shift()
  for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const nextX = blockX + offsetX
    const nextZ = blockZ + offsetZ
    const key = `${nextX},${nextZ}`
    if (nextX < 0 || nextX >= 29 || nextZ < 0 || nextZ >= 35 || visited.has(key) || !walkable(nextX, nextZ)) continue
    visited.add(key)
    pending.push([nextX, nextZ])
  }
}
for (const position of [[14, 4], [3, 17], [25, 17], [14, 26]]) requireCheck(visited.has(position.join(',')), 'Ground-level route disconnected')
requireCheck(counts['minecraft:candle'] === 36 && counts['minecraft:chain'] === 10 && bannerPayloads === 11, 'Theme counts mismatch')
const sha256 = digest(bytes)
const checks = Object.fromEntries(['reference_and_both_batch_manifests', 'gzip_single_palette_java_1_20_1', 'explicit_volume_unique_coordinates_no_entities', 'vanilla_nonhazardous_palette', 'five_wide_three_high_main_route', 'north_entrance_floor_and_interior_connected', 'complete_double_layer_roof_and_closed_shell', 'four_continuous_pillars_and_joined_beams', 'all_candles_have_solid_top_support', 'two_chain_anchored_chandeliers', 'eleven_supported_patterned_banners', 'supported_altar_and_two_rows_of_stairs', 'no_inventory_loot_or_command_payload'].map(key => [key, 'PASS']))
const room = { ...design, sha256, data_version: 3465, minecraft: '1.20.1', explicit_air: true, placement_requires_empty_volume: true, deployment_status: 'NOT_DEPLOYED', connection_points: [{ side: 'north', position: design.entrance, opening_width: 5, opening_height: 4 }], materials: 'materials.json' }
const report = { id: design.id, status: 'PASS', sha256, reference_sha256: design.reference_sha256, size: design.size, checks, explicit_records: blocks.length, non_air_blocks: blocks.length - counts['minecraft:air'], candle_groups: counts['minecraft:candle'], individual_candles: candleCount, patterned_banners: bannerPayloads, ground_level_reachable_foot_positions: visited.size, materials: counts, limits: ['Static ground flood-fill approximates collision. Native Forge verifies actual collision, attachments and stairs separately.'] }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
for (const [filename, value] of [['room.json', room], ['qa/static.json', report], ['qa/reference.json', { image: design.reference, sha256: design.reference_sha256, batch_readme: path.join(batchRoot, 'README.md'), records: batchEvidence }]]) await fs.writeFile(path.join(root, filename), JSON.stringify(value, null, 2) + '\n')
process.stdout.write(`H03_STATIC_PASS ${sha256} candles=${candleCount} ground=${visited.size}\n`)
