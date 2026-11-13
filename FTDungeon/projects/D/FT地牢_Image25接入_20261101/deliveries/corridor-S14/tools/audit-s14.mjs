import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reference = 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S14-end-overgrown.png'
const expectedReference = '55d41c576a5d4061b182f784bc256c2ae56c7aee06df2d5b3020c825a442a1f2'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const nbtBytes = await fs.readFile(path.join(root, 'room.nbt'))
const sourceBytes = await fs.readFile(reference)
const structure = Structure.fromNbt(NbtFile.read(nbtBytes).root)
const size = structure.getSize()
const blocks = structure.getBlocks()
const lookup = new Map(blocks.map(entry => [entry.pos.join(','), entry]))
const blockAt = position => lookup.get(position.join(','))?.state.getName().toString() ?? 'minecraft:air'
const checks = []
const check = (name, pass, evidence) => checks.push({ name, status: pass ? 'PASS' : 'FAIL', evidence })
const counts = {}
for (const entry of blocks) {
  const name = entry.state.getName().toString()
  counts[name] = (counts[name] ?? 0) + 1
}
check('reference_sha256', digest(sourceBytes) === expectedReference, digest(sourceBytes))
check('short_length_16', size.join(',') === '13,10,16', size)
check('explicit_complete_volume', blocks.length === 2080 && lookup.size === 2080, { records: blocks.length, unique: lookup.size })
check('all_coordinates_in_bounds', blocks.every(({ pos }) => pos.every((coordinate, axis) => coordinate >= 0 && coordinate < size[axis])), size)
check('no_block_entities', blocks.every(entry => !entry.nbt || entry.nbt.size === 0), { block_entities: blocks.filter(entry => entry.nbt && entry.nbt.size > 0).length })
const solidNames = new Set(['minecraft:end_stone_bricks', 'minecraft:end_stone', 'minecraft:purpur_block', 'minecraft:purpur_pillar', 'minecraft:obsidian', 'minecraft:moss_block'])
check('full_solid_floor', blocks.filter(({ pos }) => pos[1] === 0).length === 208 && blocks.filter(({ pos }) => pos[1] === 0).every(entry => solidNames.has(entry.state.getName().toString())), { cells: 208 })
check('full_enclosed_roof', blocks.filter(({ pos }) => pos[1] === 9).length === 208 && blocks.filter(({ pos }) => pos[1] === 9).every(entry => solidNames.has(entry.state.getName().toString())), { cells: 208, roof_y: 9 })
const mainRoute = []
for (let blockZ = 0; blockZ < 16; blockZ++) {
  for (let blockX = 5; blockX <= 7; blockX++) mainRoute.push([blockX, 1, blockZ])
}
check('three_wide_three_high_straight_dry_lane', mainRoute.every(([blockX, height, blockZ]) => [1, 2, 3].every(level => blockAt([blockX, level, blockZ]) === 'minecraft:air') && solidNames.has(blockAt([blockX, height - 1, blockZ]))), { lane_x: [5, 7], lane_y: [1, 3], lane_z: [0, 15], no_steps_or_branches: true })
check('parallel_closed_side_walls', [0, 12].every(blockX => Array.from({ length: 16 }, (_, blockZ) => Array.from({ length: 8 }, (_, index) => blockAt([blockX, index + 1, blockZ]))).flat().every(name => solidNames.has(name))), { walls_x: [0, 12] })
check('vanilla_1201_palette_allowlist', Object.keys(counts).every(name => solidNames.has(name) || ['minecraft:air', 'minecraft:purpur_stairs', 'minecraft:purpur_slab', 'minecraft:wall_torch', 'minecraft:iron_bars', 'minecraft:end_rod', 'minecraft:vine', 'minecraft:hanging_roots'].includes(name)), Object.keys(counts))
check('reference_features_present', counts['minecraft:wall_torch'] === 6 && counts['minecraft:end_rod'] === 12 && counts['minecraft:vine'] >= 60 && counts['minecraft:obsidian'] > 0, counts)
const attachments = blocks.filter(entry => ['minecraft:wall_torch', 'minecraft:vine', 'minecraft:hanging_roots'].includes(entry.state.getName().toString()))
const vectors = { north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0], up: [0, 1, 0] }
const supported = entry => {
  const name = entry.state.getName().toString()
  const properties = entry.state.getProperties()
  if (name === 'minecraft:hanging_roots') return solidNames.has(blockAt([entry.pos[0], entry.pos[1] + 1, entry.pos[2]]))
  if (name === 'minecraft:wall_torch') {
    const vector = vectors[properties.facing]
    return solidNames.has(blockAt(entry.pos.map((value, axis) => value - vector[axis])))
  }
  return Object.entries(vectors).some(([facing, vector]) => properties[facing] === 'true' && solidNames.has(blockAt(entry.pos.map((value, axis) => value + vector[axis]))))
}
check('explicit_plant_and_torch_supports', attachments.every(supported), { attachment_checks: attachments.length })
const nonAir = blocks.filter(entry => entry.state.getName().toString() !== 'minecraft:air')
const reached = new Set()
const frontier = [nonAir[0].pos]
while (frontier.length) {
  const position = frontier.pop()
  const coordinate = position.join(',')
  if (reached.has(coordinate) || blockAt(position) === 'minecraft:air') continue
  reached.add(coordinate)
  for (const vector of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
    const neighbor = position.map((value, axis) => value + vector[axis])
    if (lookup.has(neighbor.join(',')) && !reached.has(neighbor.join(','))) frontier.push(neighbor)
  }
}
check('all_non_air_face_connected_to_floor', reached.size === nonAir.length, { connected: reached.size, non_air: nonAir.length })
check('north_and_south_open_interfaces', [0, 15].every(blockZ => [5, 6, 7].every(blockX => [1, 2, 3].every(height => blockAt([blockX, height, blockZ]) === 'minecraft:air'))), { north: [6, 1, 0], south: [6, 1, 15] })
check('no_stairs_in_main_route', mainRoute.every(position => !blockAt(position).includes('stairs') && !blockAt([position[0], 0, position[2]]).includes('stairs')), { floor_y: 0, feet_y: 1 })
const metadata = {
  id: 'CORRIDOR_S14', sourceId: 'S14', deliveryId: 'corridor-S14', name: '末地蔓生短廊', size,
  entrance: [6, 1, 0], interior: [6, 1, 2], exit: [6, 1, 15], facing: 'north',
  connectors: [{ name: 'north', facing: 'north', feet: [6, 1, 0], width: 3, height: 3 }, { name: 'south', facing: 'south', feet: [6, 1, 15], width: 3, height: 3 }],
  sha256: digest(nbtBytes), reference, referenceSha256: expectedReference, dataVersion: 3465, compression: 'gzip',
  type: 'straight-through-corridor', lengthClass: 'short', lengthBlocks: 16, axis: 'z', floorY: 0, roofY: 9,
  reservedMainLane: { from: [5, 1, 0], to: [7, 3, 15] },
  materialCounts: counts,
  designNotes: [
    'End-stone brick walls, purpur piers and arches, obsidian torch recesses, end-rod light strips, moss and supported vines/roots.',
    'Two chipped purpur soffit segments are decorative only; structural lintels remain intact and connect to a complete roof.',
    'The reference suggests a closed far purpur door; the delivery uses an open south arch to meet the straight-through corridor brief.',
    'End rods retain original vanilla white light; cyan bloom in the reference is not claimed as an in-game shader effect.',
    'S14 is the image-batch source ID, not a replacement for another room S14; CORRIDOR_S14 is an independent candidate ID for integrator allocation.'
  ],
  safety: { entities: 0, blockEntities: 0, containers: 0, realFluids: 0, commandBlocks: 0, spawners: 0, loot: 0, completeRoof: true },
  integration: { status: 'independent-delivery-only', deployed: false, playerSavesOpened: false, coreModified: false, slotAssigned: false }
}
await fs.mkdir(path.join(root, 'qa'), { recursive: true })
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(metadata, null, 2) + '\n')
const report = { id: metadata.id, status: checks.every(entry => entry.status === 'PASS') ? 'PASS' : 'FAIL', sourceNbt: path.join(root, 'room.nbt'), sha256: metadata.sha256, checks, materialCounts: counts, totalRecords: blocks.length, nonAirBlocks: blocks.length - counts['minecraft:air'], routes: [{ from: metadata.entrance, via: metadata.interior, to: metadata.exit, width: 3, bidirectional: true }], nativeCollisionTest: 'Separate Forge evidence required; this file is static only.' }
await fs.writeFile(path.join(root, 'qa/static-qa.json'), JSON.stringify(report, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256: metadata.sha256, records: blocks.length, checks: checks.length, failed: checks.filter(entry => entry.status === 'FAIL') }, null, 2))
if (report.status !== 'PASS') process.exitCode = 1
