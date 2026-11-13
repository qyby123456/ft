import assert from 'node:assert/strict'
import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createRequire } from 'node:module'
import { fileURLToPath, pathToFileURL } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skillRequire = createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const { NbtFile, Structure } = await import(pathToFileURL(skillRequire.resolve('@mattzh72/lodestone')))
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R03-ruins-overgrown-ruined-shrine.png'
const referenceHash = 'b96b07a7f353a684e808da98efafdc11feca3b17fc72606d9859a8750be8fa58'
const digest = value => crypto.createHash('sha256').update(value).digest('hex')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(bytes).root
const raw = nbt.toSimplifiedJson()
const structure = Structure.fromNbt(nbt)
const size = structure.getSize()
const records = structure.getBlocks()
const checks = []
const counts = {}
const stateAt = position => structure.getBlock(position)?.state
const nameAt = position => stateAt(position)?.getName().toString()
const airAt = position => nameAt(position) === 'minecraft:air'
const isFull = position => { const name = nameAt(position); return name === 'minecraft:sea_lantern' || (!!name && !/(air|water|vine|leaves|fern|azalea_bush|carpet|slab|stairs|lantern|chain)$/.test(name)) }
const check = (name, callback) => { callback(); checks.push({ name, status: 'PASS' }) }
const directions = { north: [0, 0, -1], east: [1, 0, 0], south: [0, 0, 1], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] }
const offset = (position, delta) => position.map((coordinate, axis) => coordinate + delta[axis])
for (const entry of records) { const name = entry.state.getName().toString(); counts[name] = (counts[name] ?? 0) + 1 }
const referenceBytes = await fs.readFile(reference)
check('reference_image_sha256', () => assert.equal(digest(referenceBytes), referenceHash))
check('gzip_single_palette_java_1_20_1', () => { assert.equal(bytes[0], 31); assert.equal(bytes[1], 139); assert.equal(raw.DataVersion, 3465); assert.ok(raw.palette && !raw.palettes) })
check('full_unique_in_bounds_volume', () => { assert.deepEqual(size, [25, 16, 27]); assert.equal(records.length, 10800); assert.equal(new Set(records.map(entry => entry.pos.join(','))).size, 10800); assert.ok(records.every(entry => entry.pos.every((coordinate, axis) => Number.isInteger(coordinate) && coordinate >= 0 && coordinate < size[axis]))) })
check('vanilla_no_entities_block_entities_inventory_or_hazards', () => { assert.equal(raw.entities.length, 0); assert.ok(raw.blocks.every(entry => !entry.nbt)); assert.ok(Object.keys(counts).every(name => name.startsWith('minecraft:') && !/(command_block|structure_block|structure_void|jigsaw|spawner|tnt|chest|barrel|hopper|dispenser|dropper|lava|fire|campfire|magma_block|powder_snow|cobweb|sand|gravel|anvil)$/.test(name))) })
check('complete_foundation_and_sealed_skylight_roof', () => { for (let blockX = 0; blockX < 25; blockX += 1) for (let blockZ = 0; blockZ < 27; blockZ += 1) { assert.ok(isFull([blockX, 0, blockZ])); assert.ok(isFull([blockX, 13, blockZ])); } assert.equal(counts['minecraft:glass'], 135) })
check('sealed_walls_except_north_portal', () => { for (let blockY = 2; blockY <= 12; blockY += 1) { for (let blockZ = 0; blockZ < 27; blockZ += 1) for (const blockX of [0, 24]) assert.ok(isFull([blockX, blockY, blockZ])); for (let blockX = 0; blockX < 25; blockX += 1) { assert.ok(isFull([blockX, blockY, 26])); if (blockX < 10 || blockX > 14 || blockY > 6) assert.ok(isFull([blockX, blockY, 0])); } } })
check('five_wide_five_high_north_entrance', () => { for (let blockX = 10; blockX <= 14; blockX += 1) { assert.ok(isFull([blockX, 1, 0])); for (let blockY = 2; blockY <= 6; blockY += 1) assert.ok(airAt([blockX, blockY, 0])); } })
const safeFeet = position => airAt(position) && airAt(offset(position, directions.up)) && isFull(offset(position, directions.down))
const reached = new Set(['12,2,0'])
const queue = [[12, 2, 0]]
for (let cursor = 0; cursor < queue.length; cursor += 1) for (const direction of ['north', 'east', 'south', 'west']) {
  const next = offset(queue[cursor], directions[direction])
  if (next[0] < 0 || next[0] >= 25 || next[2] < 0 || next[2] >= 27 || reached.has(next.join(',')) || !safeFeet(next)) continue
  reached.add(next.join(',')); queue.push(next)
}
check('five_wide_three_high_aisle_and_return_path', () => { for (let blockX = 10; blockX <= 14; blockX += 1) for (let blockZ = 0; blockZ <= 19; blockZ += 1) { assert.ok(reached.has([blockX, 2, blockZ].join(','))); for (let blockY = 2; blockY <= 4; blockY += 1) assert.ok(airAt([blockX, blockY, blockZ])); } for (const target of [[12, 2, 4], [12, 2, 16], [12, 2, 19]]) assert.ok(reached.has(target.join(','))) })
const waterPositions = records.filter(entry => entry.state.getName().toString() === 'minecraft:water').map(entry => entry.pos)
check('two_recessed_shallow_pools_with_floor_and_four_solid_sides', () => { assert.equal(waterPositions.length, 24); for (const position of waterPositions) { assert.equal(position[1], 1); assert.ok(position[2] >= 4 && position[2] <= 6 && ((position[0] >= 3 && position[0] <= 6) || (position[0] >= 18 && position[0] <= 21))); assert.ok(isFull(offset(position, directions.down))); for (const direction of ['north', 'east', 'south', 'west']) { const neighbor = offset(position, directions[direction]); assert.ok(nameAt(neighbor) === 'minecraft:water' || isFull(neighbor)); } } })
check('each_vine_has_direct_masonry_attachment', () => { for (const entry of records.filter(entry => entry.state.getName().toString() === 'minecraft:vine')) { const properties = entry.state.getProperties(); const active = Object.keys(properties).filter(key => properties[key] === 'true'); assert.ok(active.length > 0); for (const face of active) { const anchor = offset(entry.pos, directions[face]); assert.ok(isFull(anchor) && !/glass$/.test(nameAt(anchor)), 'Unsupported vine ' + entry.pos); } } assert.ok(counts['minecraft:vine'] >= 60) })
check('lanterns_chains_plants_and_rubble_have_support', () => { for (const entry of records) { const name = entry.state.getName().toString(); if (name.endsWith(':lantern')) { assert.equal(nameAt(offset(entry.pos, directions.up)), 'minecraft:chain'); } if (name.endsWith(':chain')) { const above = offset(entry.pos, directions.up); assert.ok(nameAt(above) === 'minecraft:chain' || isFull(above)); } if (/(soul_lantern|potted_fern|potted_flowering_azalea_bush|moss_carpet|stone_brick_slab)$/.test(name)) assert.ok(isFull(offset(entry.pos, directions.down)), 'Unsupported object ' + entry.pos); if (name === 'minecraft:fern') assert.equal(nameAt(offset(entry.pos, directions.down)), 'minecraft:moss_block'); if (name === 'minecraft:oak_leaves') { assert.equal(entry.state.getProperties().persistent, 'true'); assert.ok(isFull(offset(entry.pos, directions.up))); } } assert.equal(counts['minecraft:lantern'], 4); assert.equal(counts['minecraft:chain'], 8); assert.equal(counts['minecraft:soul_lantern'], 2) })
check('four_intact_columns_connect_floor_to_beam_and_roof', () => { for (const columnX of [5, 19]) for (const stationZ of [9, 18]) for (let blockY = 1; blockY <= 13; blockY += 1) assert.ok(isFull([columnX, blockY, stationZ])); })
const connected = new Set()
const solidQueue = [[0, 0, 0]]
connected.add('0,0,0')
for (let cursor = 0; cursor < solidQueue.length; cursor += 1) for (const delta of Object.values(directions)) {
  const next = offset(solidQueue[cursor], delta)
  const name = nameAt(next)
  if (!name || name === 'minecraft:air' || name === 'minecraft:water' || connected.has(next.join(','))) continue
  connected.add(next.join(',')); solidQueue.push(next)
}
check('no_disconnected_floating_arch_roof_shrine_or_foliage_components', () => { for (const entry of records) { const name = entry.state.getName().toString(); if (name !== 'minecraft:air' && name !== 'minecraft:water') assert.ok(connected.has(entry.pos.join(',')), 'Floating component ' + entry.pos); } })
check('shrine_table_has_pedestal_and_rear_arch_has_wall_backing', () => { for (let blockY = 1; blockY <= 4; blockY += 1) assert.ok(isFull([12, blockY, 23])); for (const entry of records.filter(entry => entry.pos[2] === 25 && entry.pos[0] >= 8 && entry.pos[0] <= 16 && entry.pos[1] >= 4 && entry.pos[1] <= 11 && entry.state.getName().toString() !== 'minecraft:air')) assert.ok(isFull([entry.pos[0], entry.pos[1], 26])); })
const sha256 = digest(bytes)
const metadata = { id: 'zone-R03', name: '蔓生废祠', category: 'ruins', kind: 'dungeon_zone_room', size, entrance: [12, 2, 0], interior: [12, 2, 4], safe_point: [12, 2, 16], shrine_approach: [12, 2, 19], facing: 'north', sha256, reference, reference_sha256: referenceHash, minecraft: '1.20.1', data_version: 3465, generator: 'Minecraft Builder / Lodestone 0.6.0', recipe: 'recipe.mjs', portal: { width: 5, clear_height: 5, feet_y: 2 }, main_route: { x_range: [10, 14], z_range: [0, 19], width: 5, clear_height: 3, floor_y: 1 }, roof: { complete: true, y: 13, glass_skylight_cells: 135, cutaway_preview_only: true }, design: { intact_columns: 4, hanging_lanterns: 4, soul_lanterns: 2, shallow_water_cells: 24, fallen_column_segments: 2, broken_column_stumps: 1, vine_cells: counts['minecraft:vine'], changes_from_concept: ['Open concept roof replaced by continuous roof with glass skylight.', 'Round shrine aperture is a sealed stained glass recess.', 'Glowing mushrooms represented by supported soul lanterns.', 'Crate/barrel replaced by supported stone planter and floor rubble; no inventories.'] }, source_documents: ['D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/README.md', 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/manifest.json', 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/docs/verify.json', 'D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md'], integration: { deployed: false, player_saves_accessed: false, production_files_modified: false, slot: null, requires_unique_slot_and_full_volume_occupancy_check: true, authoritative_final_acceptance_thread: '01a19c8f-6747-7572-a77a-6e90084bbd34' } }
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
const result = { id: 'zone-R03', status: 'PASS', sha256, size, checks, recorded_cells: records.length, explicit_air_records: counts['minecraft:air'], conservative_reachable_feet: reached.size, connected_nonfluid_cells: connected.size, material_counts: counts, liquid_positions: waterPositions, limits: ['Static topology checks do not substitute for Forge collision and survival testing.'] }
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(result, null, 2) + '\n')
console.log(JSON.stringify({ status: result.status, sha256, checks: checks.length, water: waterPositions.length, vines: counts['minecraft:vine'], reachable: reached.size }))


