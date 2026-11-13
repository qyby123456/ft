import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { pathToFileURL, fileURLToPath } from 'node:url'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const { NbtFile, Structure } = await import(pathToFileURL(skill + '/node_modules/@mattzh72/lodestone/lib/index.js').href)
const reference = 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A03-abandoned-derelict-kitchen.png'
const expectedReferenceHash = '413f30992df8f88ee93bdffd0c1ada33f1e33d0ccc4a47e1186f45fa5ded37b5'
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const assert = (condition, message) => { if (!condition) throw new Error(message) }
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const nbt = NbtFile.read(new Uint8Array(bytes)).root
const structure = Structure.fromNbt(nbt)
const records = structure.getBlocks()
const sha256 = digest(bytes)
const name = position => structure.getBlock(position)?.state.getName().toString()
const properties = position => structure.getBlock(position)?.state.getProperties()
assert(digest(await fs.readFile(reference)) === expectedReferenceHash, 'Reference hash mismatch')
const batchRoot = path.resolve(path.dirname(reference), '../..')
const manifest = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/manifest.json'), 'utf8'))
const verification = JSON.parse(await fs.readFile(path.join(batchRoot, 'docs/verify.json'), 'utf8'))
const locateReference = data => {
  if (Array.isArray(data)) return data.flatMap(locateReference)
  if (!data || typeof data !== 'object') return []
  if (data.file === 'out/abandoned/A03-abandoned-derelict-kitchen.png') return [data]
  return Object.values(data).flatMap(locateReference)
}
for (const data of [manifest, verification]) {
  const entries = locateReference(data)
  assert(entries.length === 1 && entries[0].sha256 === expectedReferenceHash && entries[0].ok === true, 'Batch source evidence mismatch')
}
assert(bytes[0] === 31 && bytes[1] === 139, 'Expected gzip structure')
assert(nbt.getNumber('DataVersion') === 3465, 'Expected Java 1.20.1 DataVersion')
assert(JSON.stringify(structure.getSize()) === '[19,11,19]', 'Wrong room size')
assert(records.length === 3971 && new Set(records.map(block => block.pos.join(','))).size === 3971, 'Explicit full volume or duplicate coordinate failure')
assert(nbt.getList('entities').length === 0 && !nbt.has('palettes'), 'Entities or multiple palettes')
const forbidden = new Set(['command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'spawner', 'end_portal', 'nether_portal', 'end_gateway', 'lava', 'water', 'fire', 'soul_fire', 'powder_snow', 'tnt', 'sand', 'gravel'])
const inventory = {}
const containers = []
let liveFire = 0
for (const block of records) {
  const identity = block.state.getName().toString()
  const settings = block.state.getProperties()
  inventory[identity] = (inventory[identity] ?? 0) + 1
  assert(identity.startsWith('minecraft:') && !forbidden.has(identity.slice(10)), 'Forbidden/non-vanilla block ' + identity)
  assert(settings.waterlogged !== 'true', 'Unexpected contained fluid')
  if (identity.endsWith('campfire') || identity.endsWith('furnace')) {
    if (settings.lit === 'true') liveFire++
  }
  if (identity.endsWith('cobweb')) assert(block.pos[1] >= 4, 'Web in walking space')
  if (block.nbt) {
    const payload = block.nbt.toSimplifiedJson()
    assert(['minecraft:barrel', 'minecraft:furnace', 'minecraft:campfire'].includes(payload.id), 'Unexpected block entity')
    assert(!('LootTable' in payload) && !('LootTableSeed' in payload) && Array.isArray(payload.Items) && payload.Items.length === 0, 'Unexpected inventory/loot')
    containers.push({ pos: block.pos, block: identity, id: payload.id, items: 0, lootTable: false })
  }
}
assert(liveFire === 0 && containers.length === 5, 'Cold kitchen / empty inventory failure')
const fullFloor = new Set(['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:mossy_stone_bricks', 'minecraft:polished_basalt'])
for (let width = 0; width < 19; width++) {
  for (let depth = 0; depth < 19; depth++) {
    assert(fullFloor.has(name([width, 0, depth])), 'Incomplete full floor')
    assert(['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks'].includes(name([width, 9, depth])), 'Roof leak')
  }
}
for (let width = 8; width <= 10; width++) {
  for (let depth = 0; depth <= 6; depth++) {
    for (let height = 1; height <= 3; height++) assert(name([width, height, depth]) === 'minecraft:air', 'Three wide / three high approach blocked')
  }
  assert(name([width, 4, 0]) === 'minecraft:air', 'Entrance four high opening obstructed')
}
const clearCell = (width, depth) => width >= 0 && depth >= 0 && width < 19 && depth < 19 && name([width, 1, depth]) === 'minecraft:air' && name([width, 2, depth]) === 'minecraft:air' && fullFloor.has(name([width, 0, depth]))
const queue = [[9, 0]]
const reached = new Set(['9,0'])
for (let cursor = 0; cursor < queue.length; cursor++) {
  const [width, depth] = queue[cursor]
  for (const [deltaX, deltaZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
    const next = [width + deltaX, depth + deltaZ]
    const key = next.join(',')
    if (!reached.has(key) && clearCell(...next)) { reached.add(key); queue.push(next) }
  }
}
const waypoints = [[9, 1, 0], [9, 1, 4], [5, 1, 9], [5, 1, 11], [9, 1, 6], [14, 1, 10], [14, 1, 14], [9, 1, 15]]
for (const [width, height, depth] of waypoints) assert(reached.has([width, depth].join(',')), 'Unreachable safety/inspection point')
const columns = [[3, 14, 5], [7, 14, 5], [2, 4, 5], [2, 7, 5], [16, 12, 5], [16, 16, 5], [7, 10, 2], [7, 12, 2], [12, 10, 2], [12, 12, 2]]
for (const [width, depth, top] of columns) {
  for (let height = 0; height <= top; height++) assert(name([width, height, depth]) && name([width, height, depth]) !== 'minecraft:air', 'Furniture support column gap')
}
const lamps = [[4, 5, 5], [14, 5, 5], [10, 5, 10], [14, 5, 15], [4, 5, 12]]
for (const [width, height, depth] of lamps) {
  assert(name([width, height, depth]) === 'minecraft:lantern' && properties([width, height, depth]).hanging === 'true', 'Hanging lantern missing')
  for (let chainY = height + 1; chainY <= 7; chainY++) assert(name([width, chainY, depth]) === 'minecraft:chain', 'Lamp chain has a gap')
  assert(name([width, 8, depth]) === 'minecraft:stripped_spruce_log', 'Lamp ceiling beam missing')
  assert(name([width, 9, depth]) === 'minecraft:stone_bricks' || name([width, 9, depth]) === 'minecraft:cracked_stone_bricks', 'Lamp roof anchor missing')
}
let supportedDecorations = 0
for (const block of records) {
  const identity = block.state.getName().toString()
  const [width, height, depth] = block.pos
  if (identity.includes('flower_pot') || identity.includes('potted_') || identity.endsWith('_carpet') || identity === 'minecraft:cauldron') {
    assert(name([width, height - 1, depth]) !== 'minecraft:air', 'Unsupported tabletop object')
    supportedDecorations++
  }
  if (identity === 'minecraft:grindstone') {
    assert(name([width, height + 1, depth]) === 'minecraft:stripped_spruce_log', 'Utensil ceiling beam missing')
    assert(name([width, height + 1, depth + 1])?.includes('stone_bricks'), 'Utensil beam wall anchor missing')
  }
}
for (const width of [4, 5, 6]) assert(name([width, 0, 16]) === 'minecraft:polished_basalt', 'Hearth stone floor missing')
assert(name([5, 1, 16]) === 'minecraft:polished_basalt', 'Cauldron needs a full-height stone plinth')
assert(properties([5, 1, 15]).lit === 'false' && properties([9, 1, 16]).lit === 'false', 'Heat sources must remain cold')
const report = {
  id: 'A03', status: 'PASS', structure_sha256: sha256, reference_sha256: expectedReferenceHash,
  source_manifest_match: true, source_verify_match: true, gzip: true, dataVersion: 3465,
  size: [19, 11, 19], explicit_records: records.length, explicit_air: inventory['minecraft:air'], single_palette: true,
  entrance: [9, 1, 0], interior: [9, 1, 4], opening: [3, 4], dry_three_wide_approach_cells: 21,
  reachable_dry_floor_cells: reached.size, waypoint_reachability: waypoints.map(pos => ({ pos, reachable: true })),
  full_floor_cells: 361, full_roof_cells: 361, roof_y: 9,
  furniture_support_columns: columns.length, roof_anchored_lanterns: lamps.length, chain_blocks: 10,
  wall_anchored_hanging_utensils: 3, supported_tabletop_decorations: supportedDecorations,
  containers, nonempty_inventories: 0, loot_tables: 0, entities: 0, active_fire_sources: liveFire, liquids: 0,
  fire_policy: 'Unlit campfire beside an empty cauldron on a full-height basalt plinth; unlit furnace, basalt hearth floor and stone cheeks/canopy; warm light is five lanterns.',
  safety_scope: 'Static footprint and floor flood-fill; exact block registry, player AABB, canSurvive and neighbor/tick stability are separately checked on an isolated native Forge server.',
  inventory,
}
const room = {
  id: 'A03', name: '荒废厨房', category: 'abandoned', size: [19, 11, 19], entrance: [9, 1, 0], interior: [9, 1, 4], facing: 'north',
  sha256, reference, reference_sha256: expectedReferenceHash, minecraft: '1.20.1', dataVersion: 3465,
  opening: { min: [8, 1, 0], max: [10, 4, 0], width: 3, height: 4, feet_y: 1, floor_y: 0 },
  approach: { min: [8, 1, 0], max: [10, 3, 6], width: 3, min_clear_height: 3 },
  inspection_points: waypoints, roof: { plane_y: 9, complete: true, cutaway_display_only: true },
  contents: { barrels: 3, furnace: 1, cold_campfire: 1, cauldrons: 2, hanging_lanterns: 5, hanging_utensil_analogues: 3, inventories_empty: true, loot_tables: false },
  reference_features: ['灰色老化石砖与苔痕', '石砌壁炉与烟囱', '中央四脚木长桌与矮凳', '两侧木柜架和空储物桶', '壁挂锅具与暖色链灯', '高处蛛网、旧布和枯盆栽'],
  safe_adaptations: ['补齐完整石屋顶', '北侧中央三格宽四格高入口', '入口至安全点三格宽净空', '冷营火与熄灭熔炉保持无伤害状态', '锅具以梁下砂轮表现，家具和吊挂均有支撑', '仅三个空木桶，无战利品'],
  deployment: { authorized: false, deployed: false, target: 'independent review only', integration_required: ['validate full cuboid including explicit air and approach', 'reject occupancy/liquids/entities/boundary conflicts', 'persistent idempotent per-room marker', 'rotate room metadata with the same StructureTemplate origin transform'] },
  coordinate_note: 'Relative XYZ, entrance and interior use player feet positions. North is z=0. Explicit air never grants permission to clear occupied world space.',
}
await fs.writeFile(path.join(root, 'room.json'), JSON.stringify(room, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/static.json'), JSON.stringify(report, null, 2) + '\n')
await fs.writeFile(path.join(root, 'qa/materials.json'), JSON.stringify(inventory, null, 2) + '\n')
console.log(JSON.stringify({ status: report.status, sha256, records: records.length, reachable: reached.size, inventories: containers.length, roof: 361 }))

